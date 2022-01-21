package kr.co.kdone.airone.activity.register.prism;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import net.daum.mf.map.api.MapPoint;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;

import blufi.espressif.BlufiCallback;
import blufi.espressif.BlufiClient;
import blufi.espressif.constant.BlufiConstants;
import blufi.espressif.response.BlufiScanResult;
import blufi.espressif.response.BlufiStatusResponse;
import blufi.espressif.response.BlufiVersionResponse;
import kr.co.kdone.airone.BuildConfig;
import kr.co.kdone.airone.CleanVentilationApplication;
import kr.co.kdone.airone.R;
import kr.co.kdone.airone.activity.PopupActivity;
import kr.co.kdone.airone.activity.register.APPasswordActivity;
import kr.co.kdone.airone.activity.register.RegisterResultActivity;
import kr.co.kdone.airone.adapter.RegisterPrismDevicePagerAdapter;
import kr.co.kdone.airone.components.NonSwipeableViewPager;
import kr.co.kdone.airone.data.prism.McuPacket;
import kr.co.kdone.airone.fragments.prism.RegisterPrismDevice1Fragment;
import kr.co.kdone.airone.fragments.prism.RegisterPrismDevice3Fragment;
import kr.co.kdone.airone.fragments.prism.RegisterPrismDevice4Fragment;
import kr.co.kdone.airone.udp.WifiAP;
import kr.co.kdone.airone.utils.CommonUtils;
import kr.co.kdone.airone.utils.HttpApi;
import kr.co.kdone.airone.utils.KDBlueToothManager;
import kr.co.kdone.airone.utils.SharedPrefUtil;
import libs.espressif.app.SdkUtil;
import libs.espressif.net.NetUtil;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

import static kr.co.kdone.airone.utils.CommonUtils.DismissConnectDialog;
import static kr.co.kdone.airone.utils.CommonUtils.IsRunningProgress;
import static kr.co.kdone.airone.utils.CommonUtils.MODE_CHANGE_AP;
import static kr.co.kdone.airone.utils.CommonUtils.MODE_DEVICE_ONLY_REG;
import static kr.co.kdone.airone.utils.CommonUtils.MODE_DEVICE_REG;
import static kr.co.kdone.airone.utils.CommonUtils.MODE_FIND_PASS;
import static kr.co.kdone.airone.utils.CommonUtils.MODE_NONE;
import static kr.co.kdone.airone.utils.CommonUtils.MODE_SUCCESS_REG_ID;
import static kr.co.kdone.airone.utils.CommonUtils.SECURITY_NONE;
import static kr.co.kdone.airone.utils.CommonUtils.displayProgress;
import static kr.co.kdone.airone.utils.HttpApi.HTTP_OK;
import static kr.co.kdone.airone.utils.SharedPrefUtil.DEFAULT_CURRENT_CONTROL_DEVICE_NAME;
import static kr.co.kdone.airone.utils.SharedPrefUtil.DEFAULT_HOMEDATA_JSON;
import static kr.co.kdone.airone.utils.SharedPrefUtil.HOMEDATA_JSON;
import static kr.co.kdone.airone.utils.SharedPrefUtil.HOMEDATA_UPDATA_TIME;

/**
 * ikHwang 2019-06-05 오전 8:30 프리즘 모델 사용자 회원가입
 */
public class RegisterPrismDeviceActivity extends AppCompatActivity {
    private final String TAG = getClass().getSimpleName();

    private Context mContext;

    private final int                       REQUEST_NO_BLUETOOTH_DEVICE         = 1001;             // 블루투스를 사용 할 수 없는 디바이스 팝업 요청
    private final int                       REQUEST_ACTIVE_BLUETOOTH_DEVICE     = 1002;             // 블루투스 활성화 ON 상태 변경 안내 팝업
    private final int                       REQUEST_BLE_DEVICE_SELECT           = 1003;             // 블루투스 디바이스 선택 화면
    private final int                       REQUEST_UNREGISTER_DEVICE_POPUP     = 1004;             // 제품 등록 미완료 팝업
    private final int                       RESULT_AP_PASSWORD                  = 1005;             // AP 비밀번호 입력 결과

    private final int                       REQUEST_TIME_CNT                    = 6 * 10 * 1000;    // AP 연결 딜레이 카운트
    private final int                       SERVER_REQUEST_TIME                 = 5 * 1000;         // 서버에 제품 등록 확인 딜레이 카운트

    private final String                    BLUFIKEY                            = "blufiKey";       // BluFi CustomData 전달 Key

    private NonSwipeableViewPager           pagerRegisterDevice;                                    // 프래그먼트 뷰페이저 레이아웃
    private RegisterPrismDevicePagerAdapter pagerAdapter;                                           // 단계진행 뷰페이저 어댑터
    private ImageView[]                     headerSteps;                                            // 페이지 상태 페이징 이미지뷰
    private View[]                          headerStepDividers;                                     // 페이징 이미지뷰 간 하이픈 뷰
    private Fragment                        currFragment;                                           // 현재 화면 프래그먼트

    private KDBlueToothManager              kdBlueToothManager;                                     // 블루투스 연결 매니저
    private boolean                         isBlueToothNoModule = false;                            // 블루투스 모듈 상태 true일 경우 블루투스 사용불가 단말
    private ArrayList<BluetoothDevice>      bleList;                                                // 블루투스 디바이스 목록
    private LinkedHashMap<String, BluetoothDevice>  tempBleList;                                    // 블루투스 디바이스 목록 (중복을 제거하기 위해 사용)
    private boolean                         bRCServerSendSuccess = false;                           // 0xB9 전달 성공 상태값 (true일 경우 BLE 연결 해제 콜백 수신시 에러팝업 표시하지 않음)

    private boolean                         isBleSearching = false;                                 // BLE 검색 상태
    private final int                       REGISTER_RETRY_MAX_CNT = 10;                            // BLE 재연결 재시도 횟수
    public int                              registerCount = 0;                                      // 룸콘 서버 등록 확인 횟수
    private final int                       RETRY_MAX_CNT = 3;                                      // BLE 재연결 재시도 횟수
    private int                             retryCnt = 0;                                           // BLE 연결시 133 오류로 연결이 불가능한 경우 발생하여 RETRY_MAX_CNT 값 만큼 재시도함
    private BluetoothDevice                 currentSelectDev;                                       // 선택된 블루투스 디바이스
    private BluetoothGatt                   mGatt;                                                  // BLE 연결 객체
    private BlufiClient                     mBlufiClient;                                           // 블루파이 연동 객체
    public volatile boolean                 mConnected;                                             // BLE 연결 상태
    private final String                    REQ_MAX_AP_CNT = "30";                                  // AP 리스트 요청 카운트 (해당 카운트 갯수 리스폰스함)

    private ArrayList<WifiAP>               mWifiAPs;                                               // 연결 가능한 WiFi 리스트 목록
    private ArrayList<WifiAP>               mTempWifiAPs;                                           // MUC에서 수신한 WiFi 리스트 목록
    private WifiAP                          mSelectedAP;                                            // 선택된 WiFi AP

    private Handler                         mHandler;                                               // 프래그먼트 이동시 CustomData 요청시 처리가 누락되어 요청에대한 딜레이를 주기위해 사용
    private Handler                         mTimeoutHandler;                                        // BluFi CustomData 전달시 요청에대한 타임아웃 처리 핸들러

    public MapPoint                         gpsLatLng;                                              // 마지막 위치 조회
    public MapPoint                         selectLatLng;                                           // 선택된 위치
    public String                           mArea;                                                  // 선택된 위치의 주소

    public int                              mode;                                                   // 룸콘 등록 or 재등록
    public int                              subMode;                                                // 룸콘 or 에어모니터
    private String                          userId;                                                 // 사용자 아이디
    private String                          password;                                               // 사용자 비밀번호
    private String                          name;                                                   // 사용자 이름
    private String                          phone;                                                  // 사용자 전화번호
    private String                          ssid;                                                   // 디바이스 연결 SSID
    private String                          deviceID;                                               // 디바이스 아이디
    public int                              devType;                                                // 프리즘-0, 제습청정-1 구분 타입

    private String                          lastCommand;                                            // MCU에 전달한 COMMAND
    public boolean                          isRoomControlerConnected;                               // 룸콘 서버 연결 상태

    private HandlerThread                   blufiReceiveHandlerThread;                              // Blufi CustomData 수신 처리 핸들러
    private Handler                         blufiReceiveHandler;                                    // CustomData 수신 알림 핸들러

    private String                          apPassword = "";                                        // 연결될 AP 비밀번호
    private int                             apConnectRetryCnt = 0;                                  // SSID, PW 재전송 횟수

    private LinearLayout                    layout_indicator;
    private TextView                        txtTitle;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_prism_device);

        mContext = this;

        // ikHwang 2019-07-09 오후 5:38 수신할 수 있는 mode, sub_mode
        // mode : MODE_FIND_ID, sub_mode : MODE_DEVICE_ROOM_CON - 아이디 찾기
        // mode : MODE_FIND_PASS, sub_mode : MODE_DEVICE_ROOM_CON - 비밀번호 찾기
        // mode : MODE_DEVICE_REG, sub_mode : MODE_DEVICE_ROOM_CON - 회원가입
        // mode : MODE_DEVICE_ONLY_REG, sub_mode : MODE_DEVICE_ROOM_CON - 룸콘 재등록
        // mode : MODE_CHANGE_AP, sub_mode : MODE_DEVICE_ROOM_CON - AP 변경
        mode = getIntent().getIntExtra("mode", MODE_NONE);
        subMode = getIntent().getIntExtra("sub_mode", MODE_NONE);
        userId = getIntent().getStringExtra("userID");
        password = getIntent().getStringExtra("password");
        name = getIntent().getStringExtra("name");
        phone = getIntent().getStringExtra("phone");
        devType = getIntent().getIntExtra("devType", 0);

        initLayout();
        initHandler();

//        setGpsLocation();

        currFragment = pagerAdapter.getItem(pagerRegisterDevice.getCurrentItem());
    }

    @Override
    protected void onPause() {
        super.onPause();

        // 블루투스 브로드캐스트 리시버 제거
        if(null != kdBlueToothManager){
            kdBlueToothManager.unRegisterbluetoothReceiver();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        bleClose();

        if(blufiReceiveHandlerThread != null){
            blufiReceiveHandlerThread.quitSafely();
        }
    }

    @Override
    public void onBackPressed() {
        if (currFragment instanceof RegisterPrismDevice4Fragment) {
            Intent intent = new Intent(this, PopupActivity.class);
            intent.putExtra(PopupActivity.POPUP_MODE, PopupActivity.MODE_YESNO);
            intent.putExtra(PopupActivity.POPUP_BODY_TEXT, getString(R.string.RegisterPrismDeviceActivity_str_3));
            startActivityForResult(intent, REQUEST_UNREGISTER_DEVICE_POPUP);
            overridePendingTransition(0, 0);
        } else if (currFragment instanceof RegisterPrismDevice1Fragment) {
            finish();
            overridePendingTransition(0, R.anim.slide_right_out);
        } else {
            if(currFragment instanceof RegisterPrismDevice3Fragment){
                bleClose();
            }

            moveToPage(pagerRegisterDevice.getCurrentItem() - 1);
        }
    }

    /**
     * ikHwang 2019-06-05 오전 8:46 레이아웃 초기화
     */
    private void initLayout(){
        bleList = new ArrayList<>();        // BLE 리스트 초기화
        tempBleList = new LinkedHashMap<>();
        mWifiAPs = new ArrayList<>();       // WiFi AP 리스트 초기화 (리스트 화면 구성시 사용)
        mTempWifiAPs = new ArrayList<>();   // 검색된 AP 리스트

        pagerRegisterDevice = findViewById(R.id.pagerRegisterDevice);
        layout_indicator = findViewById(R.id.layout_indicator);
        txtTitle = findViewById(R.id.txtTitle);

        headerSteps = new ImageView[]{
                findViewById(R.id.header_step1),
                findViewById(R.id.header_step2),
                findViewById(R.id.header_step3),
                findViewById(R.id.header_step4)
        };

        headerStepDividers = new View[]{
                findViewById(R.id.header_step_divider1),
                findViewById(R.id.header_step_divider2),
                findViewById(R.id.header_step_divider3)
        };

        int pageCount = 3;
        if (mode == MODE_CHANGE_AP) {
            pageCount = 2;

            headerSteps[2].setVisibility(View.GONE);
            headerStepDividers[1].setVisibility(View.GONE);
        }else if(mode == MODE_FIND_PASS){
            pageCount = 1;

            headerSteps[2].setVisibility(View.GONE);
            headerSteps[1].setVisibility(View.GONE);
            headerStepDividers[1].setVisibility(View.GONE);
            headerStepDividers[0].setVisibility(View.GONE);

            layout_indicator.setVisibility(View.GONE);
            txtTitle.setVisibility(View.VISIBLE);
        }else {
//            setGpsLocation();
        }

        pagerAdapter = new RegisterPrismDevicePagerAdapter(getSupportFragmentManager(), pageCount);
        pagerRegisterDevice.setAdapter(pagerAdapter);
        pagerRegisterDevice.setOffscreenPageLimit(5);

        pagerRegisterDevice.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                // 페이지 전환시 현재 프래그먼트 확인하기위해 사용 position으로 변경해서 사용 할 수 있는지 테스트 할 것
                currFragment = pagerAdapter.getItem(position);

                // 현재 페이지에 따라 상단 진행단계 페이징 UI 변경
                for (int i = 0; i < 4; i++) {
                    if (position < i) {
                        headerSteps[i].setEnabled(true);
                        headerSteps[i].setSelected(false);
                        if (i < 3) {
                            headerStepDividers[i].setSelected(false);
                        }
                    } else if (position == i) {
                        headerSteps[i].setEnabled(true);
                        headerSteps[i].setSelected(true);
                        if (i < 3) {
                            headerStepDividers[i].setSelected(false);
                        }
                    } else if (position > i) {
                        headerSteps[i].setEnabled(false);
                        headerSteps[i].setSelected(false);
                        if (i < 3) {
                            headerStepDividers[i].setSelected(true);
                        }
                    }
                }

                CommonUtils.customLog(TAG, "onPageSelected : " + position + ", mConnected : " + mConnected, Log.ERROR);

                // ikHwang 2019-06-19 오전 9:48 AP리스트 화면 전환시 AP목록 조회 시작
                if(currFragment instanceof RegisterPrismDevice3Fragment){
                    // ikHwang 2019-06-19 오전 10:04
                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            ((RegisterPrismDevice3Fragment) currFragment).setLoadingUI(true);
                            startScanWiFiList();
                        }
                    }, 500);
                }

                if(!(currFragment instanceof RegisterPrismDevice1Fragment)){
                    // 블루투스 브로드캐스트 리시버 제거
                    if(null != kdBlueToothManager){
                        kdBlueToothManager.unRegisterbluetoothReceiver();
                    }
                }

                if (position == 0) {
                    ((RegisterPrismDevice1Fragment) pagerAdapter.getItem(position)).setLoadingUI(false);
                } else if (position == 1) {

                } else if (position == 2) {
                    ((RegisterPrismDevice4Fragment) pagerAdapter.getItem(position)).initMap();
                } else if (position == 3) {

                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
    }

    /**
     * ikHwang 2019-07-26 오전 8:26 BluFi CustomData 수신 처리하기위해 사용함
     * CustomData를 통해 AP 리스트 전달시 CustomData에서 128byte만 처리가 가능하여
     * AP리스트를 3개씩 전달하함 (큐 대신 순차 처리가 가능한 핸들러 스레드 이용함)
     */
    private void initHandler(){
        mHandler = new Handler();
        mTimeoutHandler = new Handler();

        blufiReceiveHandlerThread = new HandlerThread(TAG);
        blufiReceiveHandlerThread.start();

        blufiReceiveHandler = new Handler(blufiReceiveHandlerThread.getLooper()){
            @Override
            public void handleMessage(Message msg) {
                if(null == msg) return;

                String receiveData = null;

                try {
                    receiveData = new String(msg.getData().getByteArray(BLUFIKEY));
                    
                    CommonUtils.customLog(TAG, "blufiReceiveHandler - receiveData : " + receiveData, Log.ERROR);

                    String[] splitData = receiveData.split(McuPacket.RECV_CMD_SEPARATOR);

                    if(!TextUtils.isEmpty(lastCommand) && splitData.length > 0){
                        switch (splitData[0]){
                            case McuPacket.RES_COMMAND_GET_DEV_ID: // 룸콘 아이디 조회 응답
                            case McuPacket.RES_COMMAND_CHANGE_DEV_ID: // 룸콘 아이디 생성 응답
                                DismissConnectDialog();

                                if(splitData.length == 2){
                                    deviceID = splitData[1].toUpperCase();
//                                    deviceID = "000000000000000A";

                                    // ID, PW 찾기 시에는 이전 화면으로 디바이스 아이디 전달
                                    if(mode == MODE_FIND_PASS){
                                        // ikHwang 2019-08-30 오후 5:24 0xB9 패킷 수신시 룸콘에서 Blufi 연결 해제하기로하여 추가함 (feat.이정미주임)
                                        // ikHwang 2019-09-02 오전 10:56 제품아이디 이전 액티비티에 전달하는 부분은 onPostCustomDataResult 위치로 이동
//                                        Thread.sleep(200);
                                        sendPacket(McuPacket.REQ_COMMAND_SERVER_CONNECT, null);
                                    }else{
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                pagerRegisterDevice.setCurrentItem(1);
                                            }
                                        });
                                    }
                                }else{
                                    errorRequest();
                                }

                                mTimeoutHandler.removeCallbacks(timeRunnable);
                                break;

                            case McuPacket.RES_COMMAND_WIFI_SCAN: // WiFi 스캔 응답
                                CommonUtils.customLog(TAG, "WiFi Scan Call : " + receiveData, Log.ERROR);

                                if(2 == splitData.length){
                                    int listCnt = parseWifiScanList(splitData[1]);

                                    if(0 > listCnt){
                                        mTimeoutHandler.removeCallbacks(timeRunnable);
                                        errorRequest();
                                    }else if (0 == listCnt){
                                        mTimeoutHandler.removeCallbacks(timeRunnable);
                                        DismissConnectDialog();

                                        mWifiAPs.clear();
                                        mWifiAPs.addAll(mTempWifiAPs);

                                        if(currFragment instanceof RegisterPrismDevice3Fragment){
                                            ((RegisterPrismDevice3Fragment) currFragment).updateApList(mWifiAPs);
                                        }
                                    }
                                }else{
                                    mTimeoutHandler.removeCallbacks(timeRunnable);
                                    errorRequest();
                                }
                                break;

                            case McuPacket.RES_COMMAND_AP_SSID: // SSID 전달 전달 결과 처리
                                boolean isAPSSIDConnect = false;

                                if(splitData.length == 2){
                                    if("1".equals(splitData[1])){
                                        // AP 연결 요청 성공
                                        isAPSSIDConnect = true;
                                    }else if("2".equals(splitData[1])){
                                        // 룸콘에서 수신된 SSID 길이가 맞지 않아 재전송함
                                        if(apConnectRetryCnt < 3){
                                            apConnectRetryCnt++;
                                            mTimeoutHandler.removeCallbacks(timeRunnable);
                                            sendPacket(McuPacket.REQ_COMMAND_AP_SSID, new String[] {mSelectedAP.ssid});
                                            return;
                                        }else{
                                            // AP 연결 요청 실패에 대한 예외처리
                                            DismissConnectDialog();
                                            CommonUtils.showToast(RegisterPrismDeviceActivity.this, getString(R.string.RegisterPrismDeviceActivity_str_7));
                                            return;
                                        }
                                    }
                                }

                                mTimeoutHandler.removeCallbacks(timeRunnable);

                                if(isAPSSIDConnect){
                                    // SSID 전달 성공 후 비밀번호 전달
                                    apConnectRetryCnt = 0; // 재전송 횟수 초기화
                                    sendPacket(McuPacket.REQ_COMMAND_AP_PW, new String[]{apPassword});
                                }else{
                                    // AP 연결 요청 실패에 대한 예외처리
                                    DismissConnectDialog();
                                    CommonUtils.showToast(RegisterPrismDeviceActivity.this, getString(R.string.RegisterPrismDeviceActivity_str_7));
                                }
                                break;

                            case McuPacket.RES_COMMAND_AP_PW: // AP PW 전달 결과 처리
                                boolean isAPPWConnect = false;

                                if(splitData.length == 2){
                                    if("1".equals(splitData[1])) {
                                        // AP 연결 요청 성공
                                        isAPPWConnect = true;
                                    }else if("2".equals(splitData[1])){
                                        // 룸콘에서 수신된 PW 길이가 맞지 않아 재전송함
                                        if(apConnectRetryCnt < 3){
                                            apConnectRetryCnt++;
                                            mTimeoutHandler.removeCallbacks(timeRunnable);
                                            sendPacket(McuPacket.REQ_COMMAND_AP_PW, new String[]{apPassword});
                                            return;
                                        }else{
                                            // AP 연결 요청 실패에 대한 예외처리
                                            DismissConnectDialog();
                                            CommonUtils.showToast(RegisterPrismDeviceActivity.this, getString(R.string.RegisterPrismDeviceActivity_str_7));
                                            return;
                                        }
                                    }
                                }

                                mTimeoutHandler.removeCallbacks(timeRunnable);

                                if(isAPPWConnect){
                                    lastCommand = McuPacket.REQ_COMMAND_SERVER_CONNECT;
                                    mTimeoutHandler.postDelayed(timeRunnable, REQUEST_TIME_CNT);
                                }else{
                                    // AP 연결 요청 실패에 대한 예외처리
                                    DismissConnectDialog();
                                    CommonUtils.showToast(RegisterPrismDeviceActivity.this, getString(R.string.RegisterPrismDeviceActivity_str_7));
                                }
                                break;

                            case McuPacket.RES_COMMAND_SERVER_CONNECT: // 룸콘 아이디 서버 등록 응답
                                boolean isServerConnect = false;

                                if(splitData.length == 2){
                                    if("1".equals(splitData[1])){
                                        isServerConnect = true;
                                    }
                                }

                                if(isServerConnect){
                                    sendPacket(McuPacket.REQ_COMMAND_SERVER_CONNECT, null);

                                    bRCServerSendSuccess = true;

                                    if (mode == MODE_CHANGE_AP) {
                                        deviceChangeInfo();
                                    }else{
                                        DismissConnectDialog();
                                        moveToPage(2);
                                    }
                                }else{
                                    errorRequest();
                                }

                                mTimeoutHandler.removeCallbacks(timeRunnable);
                                break;
                        }
                    }else{
                        mTimeoutHandler.removeCallbacks(timeRunnable);
                        errorRequest();
                    }
                } catch (Exception e){
                    e.printStackTrace();
                    CommonUtils.customLog(TAG, "onReceiveCustomData parsing Error", Log.ERROR);

                    mTimeoutHandler.removeCallbacks(timeRunnable);
                    errorRequest();
                }
            }
        };
    }

    /**
     * ikHwang 2019-06-05 오전 10:32 뷰 클릭 이벤트 (레이아웃에 클릭이벤트 설정함)
     * @param v
     */
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btnBack:
                onBackPressed();
                break;
        }
    }

    /**
     * ikHwang 2019-06-05 오후 5:39 프래그먼트에서 화면 전환을 하기위해 호출
     * @param page
     */
    public void moveToPage(final int page){
        if(1 == page){ // 에어 룸컨트롤러 찾기 버튼 선택시 다음 화면 진행
            if(isBlueToothNoModule){ // 블루투스 사용불가 단말에서 알림 팝업
                Intent intent = new Intent(RegisterPrismDeviceActivity.this, PopupActivity.class);
                intent.putExtra(PopupActivity.POPUP_MODE, PopupActivity.MODE_NORMAL);
                intent.putExtra(PopupActivity.POPUP_BODY_TEXT, getString(R.string.RegisterPrismDeviceActivity_str_1));
                startActivityForResult(intent, REQUEST_NO_BLUETOOTH_DEVICE);
                overridePendingTransition(0, 0);
            }else{ // 블루투스 검색 요청
                // 블루투스 활성화시 검색 요청
                if(kdBlueToothManager.isBluetoothActive()){
//                    kdBlueToothManager.startDisvocery();
                    kdBlueToothManager.scanBLE(true);
                }else{ // 블루투스 꺼져있는 경우 활성화 요청
                    Intent intent = new Intent(RegisterPrismDeviceActivity.this, PopupActivity.class);
                    intent.putExtra(PopupActivity.POPUP_MODE, PopupActivity.MODE_YESNO);
                    intent.putExtra(PopupActivity.POPUP_BODY_TEXT, getString(R.string.RegisterPrismDeviceActivity_str_2));
                    intent.putExtra(PopupActivity.POPUP_CONFIRM_BUTTON_TEXT, getString(R.string.RegisterPrismDeviceActivity_str_11));
                    intent.putExtra(PopupActivity.POPUP_CANCEL_BUTTON_TEXT, getString(R.string.RegisterPrismDeviceActivity_str_12));
                    startActivityForResult(intent, REQUEST_ACTIVE_BLUETOOTH_DEVICE);
                    overridePendingTransition(0, 0);

//                    CommonUtils.showToast(RegisterPrismDeviceActivity.this, getString(R.string.RegisterPrismDeviceActivity_str_2));
                }
            }
        }else{
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    pagerRegisterDevice.setCurrentItem(page);
                }
            });
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode){
            case REQUEST_NO_BLUETOOTH_DEVICE: // 블루투스 미지원 단말 처리 사항 없음
                break;

            case REQUEST_UNREGISTER_DEVICE_POPUP: // 제품 등록 중단 첫화면으로 이동
                if(Activity.RESULT_OK == resultCode){
                    bleClose();

                    moveToPage(0);
                }

                break;

            case REQUEST_ACTIVE_BLUETOOTH_DEVICE: // 블루투스 활성화 안내 팝업
                if(Activity.RESULT_OK == resultCode){ // 확인 버튼 선택시 처리 (블루투스 설정 화면으로 이동)
//                    startActivityForResult(new Intent(Settings.ACTION_BLUETOOTH_SETTINGS), 0);
                    kdBlueToothManager.setBluetoothEnable(true);
                }else{ // 취소 팝업 선택시 처리하지 않음 (현재화면 유지)
                    // 팝업 추가 필요시 작업
                }
                break;

            case REQUEST_BLE_DEVICE_SELECT: // 블루투스 디바이스 선택
                if(Activity.RESULT_OK == resultCode){ // 디바이스 선택 완료
                    if(data.hasExtra("selectBleDev")){
                        currentSelectDev = data.getParcelableExtra("selectBleDev");
                    }

                    if(null != currentSelectDev){ // 블루투스 선택 완료 블루투스 연결확인 후 Wifi AP 목록 화면으로 이동
                        retryCnt = 0; // 연결 시도 카운트 초기화
                        connectGatt(); // BLE 연결 요청
                    }else{ // 선택된 블루투스 디바이스 없음 예외처리 필요
                        // 팝업 추가 필요시 작업
                    }
                }else{ // 디바이스 미선택
                    // 팝업 추가 필요시 작업
                }
                break;

            case RESULT_AP_PASSWORD: // WiFi AP 비밀번호 입력 결과 처리
                if(Activity.RESULT_OK == resultCode) { // WiFi AP 비밀번호 입력 완료
                    if(data.hasExtra("password")){
                        apPassword = data.getStringExtra("password");
                    }

                    if (!IsRunningProgress()) displayProgress(RegisterPrismDeviceActivity.this,"", "");

                    apConnectRetryCnt = 0;
                    sendPacket(McuPacket.REQ_COMMAND_AP_SSID, new String[] {mSelectedAP.ssid});
                }
                break;
        }
    }

    /**
     * ikHwang 2019-06-05 오후 5:38 프리즘 제품 연동 첫 페이지에서 블루투스 초기화
     */
    public void initBlueTooth(){
        kdBlueToothManager = new KDBlueToothManager(this, KDBlueToothManager.PREFIX, new KDBlueToothManager.BluetoothListener() {
            @Override
            public void onNoModule() {
                isBlueToothNoModule = true;

                Intent intent = new Intent(RegisterPrismDeviceActivity.this, PopupActivity.class);
                intent.putExtra(PopupActivity.POPUP_MODE, PopupActivity.MODE_NORMAL);
                intent.putExtra(PopupActivity.POPUP_BODY_TEXT, getString(R.string.RegisterPrismDeviceActivity_str_1));
                startActivityForResult(intent, REQUEST_NO_BLUETOOTH_DEVICE);
                overridePendingTransition(0, 0);
            }

            @Override
            public void onBluetoothState(int state) {
                switch (state){
                    case KDBlueToothManager.BLUETOOTH_STATE_OFF: // 블루투스가 OFF인 경우 ON으로 강제 처리
//                        kdBlueToothManager.setBluetoothEnable(true);
                        break;

                    case KDBlueToothManager.BLUETOOTH_STATE_SEARCHING_START: // 블루투스 검색 시작 다이얼로그 표시처리
                        if(isBleSearching) return;

//                        if (!IsRunningProgress()) displayProgress(RegisterPrismDeviceActivity.this,"", "");

                        if(currFragment instanceof RegisterPrismDevice1Fragment){
                            ((RegisterPrismDevice1Fragment) currFragment).setLoadingUI(true);
                        }

                        bleList.clear(); // 블루투스 리스트 초기화

                        isBleSearching = true;
                        mHandler.postDelayed(bleSearchTimeOutRunnable, REQUEST_TIME_CNT);
                        break;

                    case KDBlueToothManager.BLUETOOTH_STATE_SEARCHING_END: // 블루투스 검색 완료 다이얼로그 제거
                        if(!isBleSearching) return;

                        isBleSearching = false;
                        mHandler.removeCallbacks(bleSearchTimeOutRunnable);

                        bleList.addAll(tempBleList.values());
                        tempBleList.clear();

                        if(currFragment instanceof RegisterPrismDevice1Fragment){
                            ((RegisterPrismDevice1Fragment) currFragment).setLoadingUI(false);
                        }

                        DismissConnectDialog();

                        if(bleList.size() == 0) { // 검색된 블루투스 디바이스 없음, 안내 팝업 표시

                            CommonUtils.showToast(RegisterPrismDeviceActivity.this, getString(R.string.RegisterPrismDeviceActivity_str_4));
                        }else{
                            if(bleList.size() == 1){ // 디바이스 1개 검샘됨 블루투스 연결 후 AP목록 조회 화면으로 이동
                                currentSelectDev = bleList.get(0);
                                retryCnt = 0; // 연결 시도 카운트 초기화
                                connectGatt(); // BLE 연결 요청
                            }else{ // 디바이스 2개 이상 검색됨 리스트 선택 화면으로 이동함
                                Intent devSelect = new Intent();
                                devSelect.setClass(mContext, RegisterPrismBleSelectActivity.class);
                                devSelect.putParcelableArrayListExtra("bleList", bleList);
                                startActivityForResult(devSelect, REQUEST_BLE_DEVICE_SELECT);
                                overridePendingTransition(R.anim.slide_left_in, R.anim.slide_left_out);
                            }
                        }
                        break;
                }
            }

            @Override
            public void onFindDevices(BluetoothDevice device) { // 블루투스 검색
                if(isBleSearching){
                    if(null == tempBleList.get(device.getAddress())){
                        tempBleList.put(device.getAddress(), device);
                    }

//                    bleList.add(device);
                }
            }

            @Override
            public void onError(int error) {
                CommonUtils.customLog(TAG, "BLE Error : " + error, Log.ERROR);
            }

            @Override
            public void onReceiveData(byte[] data) {

            }
        });

        kdBlueToothManager.registerbluetoothReceiver();
    }

    /**
     * ikHwang 2019-06-10 오전 10:36 WiFi AP 선택
     * @param selectPos
     */
    public void startAPPW(int selectPos){
        mSelectedAP = mWifiAPs.get(selectPos);

        if (mSelectedAP != null) {
            ssid = mSelectedAP.ssid;

            if (mSelectedAP.security != SECURITY_NONE) { // 비공개 AP가 아닌 경우 비밀번호 입력 화면 이동
                Intent intent = new Intent(mContext, APPasswordActivity.class);
                intent.putExtra("ssid", mSelectedAP.ssid);
                startActivityForResult(intent, RESULT_AP_PASSWORD);
                overridePendingTransition(R.anim.slide_down_in, R.anim.slide_none);
            } else { // 개방형 AP로 바로 연결
                if (!IsRunningProgress()) displayProgress(RegisterPrismDeviceActivity.this,"", "");
//                ssid = mSelectedAP.ssid;
                apConnectRetryCnt = 0;
                sendPacket(McuPacket.REQ_COMMAND_AP_SSID, new String[] {mSelectedAP.ssid});
            }
        }
    }

    /**
     * ikHwang 2019-06-12 오전 10:28 연결된 BLE 디바이스에 AP WiFi 목록 스캔 요청
     */
    public void startScanWiFiList(){
        CommonUtils.customLog(TAG, "startScanWiFiList mConnected : " + mConnected, Log.ERROR);

        if(mConnected){
            if(currFragment instanceof RegisterPrismDevice3Fragment) ((RegisterPrismDevice3Fragment) currFragment).setLoadingUI(true);

            mTempWifiAPs.clear();
            Log.d("tak","scanTest");
            // ikHwang 2019-07-10 오후 12:12 AP 리스트 요청 CustomData 전달
            sendPacket(McuPacket.REQ_COMMAND_WIFI_SCAN, new String[] {REQ_MAX_AP_CNT});
        }
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // ikHwang 2019-06-12 오전 9:07 BluFi 연동 처리
    private void connectGatt() {
        if (mBlufiClient != null) {
            mBlufiClient.close();
            mBlufiClient = null;
        }

        if (mGatt != null) {
            mGatt.close();
        }

        if (!IsRunningProgress()) displayProgress(RegisterPrismDeviceActivity.this,"", "");

        GattCallback callback = new GattCallback();

        if (SdkUtil.isAtLeastM_23()) {
            mGatt = currentSelectDev.connectGatt(this, false, callback, BluetoothDevice.TRANSPORT_LE);
        } else {
            mGatt = currentSelectDev.connectGatt(this, false, callback);
        }
    }

    /**
     * ikHwang 2019-06-12 오전 9:13 BLE 연결 및 상태 변경 처리
     */
    private class GattCallback extends BluetoothGattCallback{
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            // BLE 서비스 상태 변경
            CommonUtils.customLog(TAG, "GattCallback onConnectionStateChange - Device Name : " + gatt.getDevice().getName()
                            + ", Address : " + gatt.getDevice().getAddress()
                            + ", status : " + status
                            + ", newState : " + newState
                    , Log.ERROR);

            if(BluetoothGatt.GATT_SUCCESS == status){ // BLE 연결 성공
                switch (newState){
                    case BluetoothProfile.STATE_CONNECTED: // BLE 연결됨
                        gatt.discoverServices();
                        mConnected = true;
                        break;

                    case BluetoothProfile.STATE_DISCONNECTED: // BLE 연결 해제됨
                        gatt.close();
                        mConnected = false;

                        // 연결실패 팝업 추가
                        DismissConnectDialog();
                        CommonUtils.showToast(RegisterPrismDeviceActivity.this, getString(R.string.RegisterPrismDeviceActivity_str_5));
                        pagerRegisterDevice.setCurrentItem(0);
                        break;
                }
            }else{
                gatt.close();

                if(mConnected){
                    // 연결해제 팝업 추가
                    if(!bRCServerSendSuccess){
                        DismissConnectDialog();
                        CommonUtils.showToast(RegisterPrismDeviceActivity.this, getString(R.string.RegisterPrismDeviceActivity_str_9));
                        pagerRegisterDevice.setCurrentItem(0);
                        mTimeoutHandler.removeCallbacks(timeRunnable);
                    }
                }else{
                    if(retryCnt < RETRY_MAX_CNT){
                        retryCnt++;
                        connectGatt();
                        CommonUtils.customLog(TAG, "connectGatt retryCnt : " + retryCnt, Log.ERROR);
                    }else{
                        // 연결실패 팝업 추가
                        DismissConnectDialog();
                        CommonUtils.showToast(RegisterPrismDeviceActivity.this, getString(R.string.RegisterPrismDeviceActivity_str_5));
                        pagerRegisterDevice.setCurrentItem(0);
                        mTimeoutHandler.removeCallbacks(timeRunnable);
                    }
                }

                mConnected = false;
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            // BLE 연결 처리
            CommonUtils.customLog(TAG, "GattCallback onServicesDiscovered - Device Name : " + gatt.getDevice().getName()
                            + ", Address : " + gatt.getDevice().getAddress()
                            + ", status : " + status
                    , Log.ERROR);

            if (BluetoothGatt.GATT_SUCCESS == status) { // BLE 연결 성공
                BluetoothGattService service = null;
                BluetoothGattCharacteristic writeCharact = null;
                BluetoothGattCharacteristic notifyCharact = null;

                try {
                    service = gatt.getService(BlufiConstants.UUID_SERVICE);
                    writeCharact = service.getCharacteristic(BlufiConstants.UUID_WRITE_CHARACTERISTIC);
                    notifyCharact = service.getCharacteristic(BlufiConstants.UUID_NOTIFICATION_CHARACTERISTIC);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                // BLE 연동하기 위한 UUID 확인 성공
                if(null != service && null != writeCharact && null != notifyCharact){
                    if (mBlufiClient != null) {
                        mBlufiClient.close();
                    }

                    mBlufiClient = new BlufiClient(gatt, writeCharact, notifyCharact, new BlufiCallbackMain());

                    gatt.setCharacteristicNotification(notifyCharact, true);

                    if (SdkUtil.isAtLeastL_21()) {
                        gatt.requestConnectionPriority(BluetoothGatt.CONNECTION_PRIORITY_HIGH);
                        boolean requestMtu = gatt.requestMtu(BlufiConstants.DEFAULT_MTU_LENGTH);

                        CommonUtils.customLog(TAG, "requestMtu state : " + requestMtu, Log.ERROR);

                        if (!requestMtu) {
                            // ikHwang 2019-07-10 오전 9:09 회원가입 및 룸콘 재등록 시에만 룸콘 아이디 변경 요청, 그외 룸콘 아이디 조회
                            // MTU 설정 실패시 요청 처리, 성공시는 한당의 onMtuChanged 메소드 안에서 처리
                            if(mode == MODE_DEVICE_ONLY_REG || mode == MODE_DEVICE_REG){
                                sendPacket(McuPacket.REQ_COMMAND_CHANGE_DEV_ID, null);
                            }else{
                                sendPacket(McuPacket.REQ_COMMAND_GET_DEV_ID, null);
                            }
                        }
                    }else{
                        if(mode == MODE_DEVICE_ONLY_REG || mode == MODE_DEVICE_REG){
                            sendPacket(McuPacket.REQ_COMMAND_CHANGE_DEV_ID, null);
                        }else{
                            sendPacket(McuPacket.REQ_COMMAND_GET_DEV_ID, null);
                        }
                    }

                }else{ // BLE 연결 실패
                    gatt.disconnect();

                    // 연결실패 팝업 추가
                    DismissConnectDialog();
                    CommonUtils.showToast(RegisterPrismDeviceActivity.this, getString(R.string.RegisterPrismDeviceActivity_str_5));
                    pagerRegisterDevice.setCurrentItem(0);

                    mConnected = false;
                }
            } else { // BLE 연결 실패
                gatt.disconnect();

                // 연결실패 팝업 추가
                DismissConnectDialog();
                CommonUtils.showToast(RegisterPrismDeviceActivity.this, getString(R.string.RegisterPrismDeviceActivity_str_5));
                pagerRegisterDevice.setCurrentItem(0);

                mConnected = false;
            }
        }

        @Override
        public void onMtuChanged(BluetoothGatt gatt, int mtu, int status) {
            if (BluetoothGatt.GATT_SUCCESS == status) {
                CommonUtils.customLog(TAG, "onMtuChanged : " + String.format("Set mtu success, mtu=%d, status=%d", mtu, status), Log.ERROR);

                if (mBlufiClient != null) {
                    mBlufiClient.setPostPackageLengthLimit(mtu - 3);
                }
            } else {
                CommonUtils.customLog(TAG, "onMtuChanged : " + String.format("Set mtu failed, mtu=%d, status=%d", mtu, status), Log.ERROR);
            }

            // ikHwang 2019-07-10 오전 9:09 회원가입 및 룸콘 재등록 시에만 룸콘 아이디 변경 요청, 그외 룸콘 아이디 조회
            if(mode == MODE_DEVICE_ONLY_REG || mode == MODE_DEVICE_REG){
                sendPacket(McuPacket.REQ_COMMAND_CHANGE_DEV_ID, null);
            }else{
                sendPacket(McuPacket.REQ_COMMAND_GET_DEV_ID, null);
            }
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            CommonUtils.customLog(TAG, "onCharacteristicWrite - status : " + status, Log.ERROR);

            mBlufiClient.onCharacteristicWrite(gatt, characteristic, status);
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            CommonUtils.customLog(TAG, "onCharacteristicChanged", Log.ERROR);

            mBlufiClient.onCharacteristicChanged(gatt, characteristic);
        }

    }

    /**
     * BLE 연결 후 통신 결과 콜백
     */
    private class BlufiCallbackMain extends BlufiCallback {
        @Override
        public void onNegotiateSecurityResult(BlufiClient client, int status) {
            switch (status) {
                case STATUS_SUCCESS:
                    CommonUtils.customLog(TAG, "onNegotiateSecurityResult - Negotiate security complete status : " + status, Log.ERROR);
                    break;

                default:
                    CommonUtils.customLog(TAG, "onNegotiateSecurityResult - Negotiate security failed status : " + status, Log.ERROR);
                    break;
            }
        }

        @Override
        public void onConfigureResult(BlufiClient client, int status) {
            switch (status) {
                case STATUS_SUCCESS:
                    CommonUtils.customLog(TAG, "onConfigureResult - Post configure params complete status : " + status, Log.ERROR);
                    break;

                default:
                    CommonUtils.customLog(TAG, "onConfigureResult - Post configure params failed status : " + status, Log.ERROR);
                    break;
            }
        }

        @Override
        public void onDeviceStatusResponse(BlufiClient client, int status, BlufiStatusResponse response) {
            switch (status) {
                case STATUS_SUCCESS:
                    CommonUtils.customLog(TAG, "onDeviceStatusResponse - Receive device status response status : " + status
                            + ", response : " + response.generateValidInfo(), Log.ERROR);

                    DismissConnectDialog();
                    try {
                        mHandler.removeMessages(0);
                        if(mSelectedAP.ssid.equals(response.getStaSSID()) && 0 == response.getStaConnectionStatus()){
                            ssid = response.getStaSSID();
                            moveToPage(2);
                        }else{
                            // AP 미연결, 비밀번호 오류
                            CommonUtils.showToast(RegisterPrismDeviceActivity.this, getString(R.string.RegisterPrismDeviceActivity_str_7));
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;

                default:
                    CommonUtils.customLog(TAG, "onDeviceStatusResponse - Device status response error status : " + status, Log.ERROR);
                    break;
            }
        }

        @Override
        public boolean onGattNotification(BlufiClient client, int pkgType, int subType, byte[] data) {
            CommonUtils.customLog(TAG, "onGattNotification - pkgType : " + pkgType + ", subType : " + subType
                    + ", Data : " + (int)data[0], Log.ERROR);
            return false;
        }

        @Override
        public void onDeviceScanResult(BlufiClient client, int status, List<BlufiScanResult> results) {
            // AP WiFi 스캔 결과 처리
            switch (status) {
                case STATUS_SUCCESS:
                    mWifiAPs.clear();

                    WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                    List<ScanResult> wifiList = wifiManager.getScanResults();

                    StringBuilder msg = new StringBuilder();

                    for (BlufiScanResult scanResult : results) {
                        for(ScanResult wifi : wifiList){

                            try {
                                if(NetUtil.is24GHz(wifi.frequency)){
                                    if(wifi.SSID.equals(scanResult.getSsid())){
                                        msg.append(scanResult.toString()).append(" | ");

                                        WifiAP ap = new WifiAP();
                                        ap.ssid = scanResult.getSsid();
                                        ap.bssid = scanResult.getSsid();
                                        ap.rssi = scanResult.getRssi();
                                        ap.auth = wifi.capabilities;
                                        ap.security = ap.getSecurity(ap.auth);
                                        ap.info = wifi;

                                        mWifiAPs.add(ap);
                                        break;
                                    }
                                }
                            }catch (Exception e){
                                e.printStackTrace();
                            }
                        }
                    }

                    if(mWifiAPs.size() > 0){
                        Collections.sort(mWifiAPs, sortByRssi);
                        Collections.reverse(mWifiAPs);
                    }

                    if(currFragment instanceof RegisterPrismDevice3Fragment){
                        ((RegisterPrismDevice3Fragment) currFragment).updateApList(mWifiAPs);
                    }

                    CommonUtils.customLog(TAG, "onDeviceScanResult - Device scan result status : " + status
                            + ", result : " + msg.toString(), Log.ERROR);

                    DismissConnectDialog();
                    break;

                default:
                    CommonUtils.customLog(TAG, "onDeviceScanResult - Device scan result error status : " + status, Log.ERROR);
                    break;
            }
        }

        @Override
        public void onDeviceVersionResponse(BlufiClient client, int status, BlufiVersionResponse response) {
            switch (status) {
                case STATUS_SUCCESS:
                    CommonUtils.customLog(TAG, "onDeviceVersionResponse - Receive device version status : " + status
                            + ", response : " + response.getVersionString(), Log.ERROR);
                    break;

                default:
                    CommonUtils.customLog(TAG, "onDeviceVersionResponse - Device version error status : " + status, Log.ERROR);
                    break;
            }
        }

        @Override
        public void onPostCustomDataResult(BlufiClient client, int status, byte[] data) {
            String sendData = "";

            if(null != data) sendData = new String(data);

            switch (status) {
                case STATUS_SUCCESS:
                    CommonUtils.customLog(TAG, "onPostCustomDataResult - status : " + status + " sendData : " + sendData, Log.ERROR);

                    // ikHwang 2019-09-02 오전 10:51 아이디 or 비밀번호 찾기시 제품 아이디 전달
                    if(!TextUtils.isEmpty(sendData) && sendData.equals(McuPacket.REQ_COMMAND_SERVER_CONNECT)){
                        bRCServerSendSuccess = true;

                        if (mBlufiClient != null) {
                            mBlufiClient.close();
                            mBlufiClient = null;
                        }

                        if (mGatt != null) {
                            mGatt.close();
                        }

                        if(mode == MODE_FIND_PASS){
                            // ikHwang 2019-08-30 오후 5:24 0xB9 패킷 수신시 룸콘에서 Blufi 연결 해제하기로하여 추가함 (feat.이정미주임)
                            // ikHwang 2019-09-02 오전 10:55 0xB9 전달 상태 확인후 종료하도록 위치 수정
                            Intent intent = new Intent();
                            intent.putExtra("deviceID", deviceID);
                            setResult(RESULT_OK, intent);
                            finish();
                        }

                        mTimeoutHandler.removeCallbacks(timeRunnable);
                    }
                    break;

                default: // command 전달 실패 에러 처리
                    errorRequest();

                    CommonUtils.customLog(TAG, "onPostCustomDataResult - status : " + status + " sendData : " + sendData, Log.ERROR);
                    break;
            }
        }

        @Override
        public void onReceiveCustomData(BlufiClient client, int status, byte[] data) {
            if(null == data) {
                CommonUtils.customLog(TAG, "onReceiveCustomData - status : " + status + " receiveData is empty", Log.ERROR);
                return;
            }else{
                String receiveData = new String(data);
                CommonUtils.customLog(TAG, "onReceiveCustomData - status : " + status + ", data : " + receiveData, Log.ERROR);
            }

            Bundle bundle = new Bundle();
            bundle.putByteArray(BLUFIKEY, data);
            Message message = new Message();
            message.setData(bundle);

            blufiReceiveHandler.sendMessage(message);
        }

        @Override
        public void onGattClose(BlufiClient client) {
            CommonUtils.customLog(TAG, "onGattClose", Log.ERROR);
        }

        @Override
        public void onError(BlufiClient client, int errCode) {
            CommonUtils.customLog(TAG, "onError - error code : " + errCode, Log.ERROR);

            bleClose();
            DismissConnectDialog();
            CommonUtils.showToast(RegisterPrismDeviceActivity.this, getString(R.string.RegisterPrismDeviceActivity_str_5));
            pagerRegisterDevice.setCurrentItem(0);
        }
    }

    private final static Comparator<WifiAP> sortByRssi = new Comparator<WifiAP>() {
        @Override
        public int compare(WifiAP o1, WifiAP o2) {
            return Integer.compare(o1.rssi, o2.rssi);
        }
    };

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            setGpsLocation();
        }
    }

    /**
     * ikHwang 2019-07-02 오전 10:30 GPS 마지막 위치 조회
     */
    @SuppressLint("MissingPermission")
    public void setGpsLocation() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        Location lastLocation = null;

        lastLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

        if(null != lastLocation){
            gpsLatLng = MapPoint.mapPointWithGeoCoord(lastLocation.getLatitude(), lastLocation.getLongitude());
        }else{
            lastLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

            if(lastLocation != null){
                gpsLatLng = MapPoint.mapPointWithGeoCoord(lastLocation.getLatitude(), lastLocation.getLongitude());
            }else{
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 100, 1,
                        new LocationListener() {
                            @Override
                            public void onLocationChanged(Location location) {
                                double latitude = location.getLatitude();
                                double longitude = location.getLongitude();

                                CommonUtils.customLog(TAG, "NETWORK_PROVIDER lat : " + latitude + ", lng : " + longitude, Log.ERROR);

                                if(null == gpsLatLng){
                                    gpsLatLng = MapPoint.mapPointWithGeoCoord(latitude, longitude);
                                }
                            }

                            @Override
                            public void onStatusChanged(String provider, int status, Bundle extras) {
                            }

                            @Override
                            public void onProviderEnabled(String provider) {
                            }

                            @Override
                            public void onProviderDisabled(String provider) {
                            }
                        });

                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 100, 1,
                        new LocationListener() {
                            @Override
                            public void onLocationChanged(Location location) {
                                double latitude = location.getLatitude();
                                double longitude = location.getLongitude();

                                CommonUtils.customLog(TAG, "GPS_PROVIDER lat : " + latitude + ", lng : " + longitude, Log.ERROR);

                                if(null == gpsLatLng){
                                    gpsLatLng = MapPoint.mapPointWithGeoCoord(latitude, longitude);
                                }
                            }

                            @Override
                            public void onStatusChanged(String provider, int status, Bundle extras) {
                            }

                            @Override
                            public void onProviderEnabled(String provider) {
                            }

                            @Override
                            public void onProviderDisabled(String provider) {
                            }
                        });
            }
        }
    }

    /**
     * ikHwang 2019-07-02 오전 11:19 룸콘 서버 등록 상태 확인 요청
     */
    public void deviceCheckState(){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (!IsRunningProgress()) {
                    displayProgress(RegisterPrismDeviceActivity.this,"", "");
                }
            }
        });

        try {
            HttpApi.PostV2CheckDeviceState(//V2A 작업.
                    deviceID,
                    new Callback() {
                        @Override
                        public void onFailure(Call call, IOException e) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    DismissConnectDialog();
                                }
                            });
                            CommonUtils.showToast(RegisterPrismDeviceActivity.this, getString(R.string.RegisterPrismDeviceActivity_str_6));
                            mHandler.removeMessages(0);
                        }

                        @Override
                        public void onResponse(Call call, Response response) throws IOException {
                            try {
                                JSONObject json_data = new JSONObject(response.body().string());

                                CommonUtils.customLog(TAG, "device/state : " + json_data.toString(), Log.ERROR);

                                switch (json_data.getInt("code")) {
                                    case HttpApi.RESPONSE_SUCCESS:
                                        if (mode == MODE_DEVICE_REG) {
                                            userJoin(); // 신규 회원가입
                                        }else if (mode == MODE_DEVICE_ONLY_REG){
                                            addRoomController(); //룸컨트롤러 재등록
                                        }
                                        break;

                                    default:
                                        CommonUtils.customLog(TAG, "registerCount : " + registerCount, Log.ERROR);

                                        if (registerCount == REGISTER_RETRY_MAX_CNT) {
                                            DismissConnectDialog();
                                            CommonUtils.showToast(RegisterPrismDeviceActivity.this, getString(R.string.toast_result_no_exsist_device));
                                        } else {
                                            mHandler.postDelayed(new Runnable() {
                                                @Override
                                                public void run() {
                                                    registerCount++;
                                                    deviceCheckState();
                                                }
                                            }, SERVER_REQUEST_TIME);
                                        }
                                        break;
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                                CommonUtils.showToast(RegisterPrismDeviceActivity.this, getString(R.string.RegisterPrismDeviceActivity_str_6));
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        DismissConnectDialog();
                                    }
                                });
                            }
                        }
                    });
        }catch (Exception e){
            e.printStackTrace();
            DismissConnectDialog();
            CommonUtils.showToast(RegisterPrismDeviceActivity.this, getString(R.string.RegisterPrismDeviceActivity_str_6));
        }
    }

    /**
     * ikHwang 2019-07-02 오전 11:21 회원가입
     */
    private void userJoin() {
        try {
            float lat = (float) 37.566535;
            float lng = (float) 126.97796919999996;

            if(selectLatLng != null){
                lat = (float)selectLatLng.getMapPointGeoCoord().latitude;
                lng = (float) selectLatLng.getMapPointGeoCoord().longitude;
            }

            if (!IsRunningProgress()) {
                displayProgress(this,"", "");
            }

            HttpApi.PostV2UserJoin( //V2A 작업.
                    userId, password, name, phone, deviceID, lat, lng, mArea, String.valueOf(BuildConfig.VERSION_CODE), Build.MODEL, ssid,
                    new Callback() {
                        @Override
                        public void onFailure(Call call, IOException e) {
                            DismissConnectDialog();
                            CommonUtils.showToast(RegisterPrismDeviceActivity.this, getString(R.string.RegisterPrismDeviceActivity_str_6));
                        }

                        @Override
                        public void onResponse(Call call, Response response){
                            try {
                                JSONObject json_data = new JSONObject(response.body().string());

                                CommonUtils.customLog(TAG, "user/sign-up : " + json_data.toString(), Log.ERROR);

                                switch (json_data.getInt("code")) {
                                    case HttpApi.RESPONSE_SUCCESS:
                                        SharedPrefUtil.putString(SharedPrefUtil.CURRENT_CONTROL_DEVICE_ID, deviceID);
                                        SharedPrefUtil.putString(SharedPrefUtil.CURRENT_CONTROL_DEVICE_NAME, DEFAULT_CURRENT_CONTROL_DEVICE_NAME);
                                        SharedPrefUtil.putString(HOMEDATA_JSON, DEFAULT_HOMEDATA_JSON);
                                        SharedPrefUtil.putLong(HOMEDATA_UPDATA_TIME, 0);

                                        Intent intent = new Intent(RegisterPrismDeviceActivity.this, RegisterResultActivity.class);
                                        intent.putExtra("mode", MODE_SUCCESS_REG_ID);
                                        intent.putExtra("userID", userId);
                                        intent.putExtra("password", password);
                                        CommonUtils.startActivityAni(RegisterPrismDeviceActivity.this, intent, true, 0);
                                        break;

                                    case HttpApi.RESPONSE_USER_ID_ALREADY_EXIST:
                                        CommonUtils.showToast(RegisterPrismDeviceActivity.this, getString(R.string.toast_result_use_already_email));
                                        break;

                                    default:
                                        CommonUtils.showToast(RegisterPrismDeviceActivity.this, getString(R.string.toast_result_can_not_request));
                                        break;
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                                CommonUtils.showToast(RegisterPrismDeviceActivity.this, getString(R.string.RegisterPrismDeviceActivity_str_6));
                            } finally {
                                DismissConnectDialog();
                            }
                        }
                    });
        } catch (Exception e) {
            e.printStackTrace();
            CommonUtils.showToast(RegisterPrismDeviceActivity.this, getString(R.string.RegisterPrismDeviceActivity_str_6));
            DismissConnectDialog();
        }
    }

    /**
     * ikHwang 2019-07-02 오전 11:33 룸콘트롤러 재등록
     */
    private void addRoomController(){
        try {
            float lat = (float) 37.566535;
            float lng = (float) 126.97796919999996;

            if(selectLatLng!=null){
                lat = (float)selectLatLng.getMapPointGeoCoord().latitude;
                lng = (float) selectLatLng.getMapPointGeoCoord().longitude;
            }

            if (!IsRunningProgress()) {
                displayProgress(this,"", "");
            }

            HttpApi.PostV2ReNewRoomController(
                    CleanVentilationApplication.getInstance().getUserInfo().getId(),
                    deviceID,
                    ssid,
                    lat,
                    lng,
                    mArea,
                    new Callback() {
                        @Override
                        public void onFailure(Call call, IOException e) {
                            DismissConnectDialog();
                            CommonUtils.showToast(RegisterPrismDeviceActivity.this, getString(R.string.RegisterPrismDeviceActivity_str_6));
                        }

                        @Override
                        public void onResponse(Call call, Response response) throws IOException {
                            try {
                                JSONObject json_data = new JSONObject(response.body().string());

                                CommonUtils.customLog(TAG, "device/add-room-controller : " + json_data.toString(), Log.ERROR);

                                switch (json_data.getInt("code")) {
                                    case HttpApi.RESPONSE_SUCCESS:
                                        setResult(RESULT_OK);
                                        finish();
                                        overridePendingTransition(0, R.anim.slide_right_out);
                                        break;

                                    default:
                                        CommonUtils.showToast(RegisterPrismDeviceActivity.this, getString(R.string.RegisterPrismDeviceActivity_str_6));
                                        break;
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                                CommonUtils.showToast(RegisterPrismDeviceActivity.this, getString(R.string.RegisterPrismDeviceActivity_str_6));
                            }
                            DismissConnectDialog();
                        }
                    });
        }catch (Exception e){
            e.printStackTrace();
            CommonUtils.showToast(RegisterPrismDeviceActivity.this, getString(R.string.RegisterPrismDeviceActivity_str_6));
            DismissConnectDialog();
        }
    }

    /**
     * ikHwang 2019-07-10 오후 2:07 제품 AP 변경
     */
    public void deviceChangeInfo(){
        try {
            HttpApi.PostV2ChangeSSID(CleanVentilationApplication.getInstance().getUserInfo().getId(), deviceID, mSelectedAP.ssid,
                    new Callback() {
                        @Override
                        public void onFailure(Call call, IOException e) {
                            CommonUtils.showToast(RegisterPrismDeviceActivity.this, getString(R.string.RegisterPrismDeviceActivity_str_6));
                            DismissConnectDialog();
                        }

                        @Override
                        public void onResponse(Call call, Response response) throws IOException {
                            try {
                                JSONObject json_data = new JSONObject(response.body().string());

                                CommonUtils.customLog(TAG, "device/change-Info : " + json_data.toString(), Log.ERROR);

                                switch (json_data.getInt("code")) {
                                    case HttpApi.RESPONSE_SUCCESS:
                                        finish();
                                        overridePendingTransition(0, R.anim.slide_right_out);
                                        break;

                                    default:
                                        CommonUtils.showToast(RegisterPrismDeviceActivity.this, getString(R.string.toast_result_no_exsist_device));
                                        break;
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                                CommonUtils.showToast(RegisterPrismDeviceActivity.this, getString(R.string.RegisterPrismDeviceActivity_str_6));
                            } finally {
                                DismissConnectDialog();
                            }
                        }
                    });
        }catch (Exception e){
            e.printStackTrace();
            CommonUtils.showToast(RegisterPrismDeviceActivity.this, getString(R.string.RegisterPrismDeviceActivity_str_6));
            DismissConnectDialog();
        }
    }

    /**
     * ikHwang 2019-07-10 오전 10:47 customData 전달
     * @param strCmd
     * @param strSendData
     */
    private void sendPacket(String strCmd, String[] strSendData){
        lastCommand = strCmd;

        if(!TextUtils.isEmpty(strCmd)){
            if(McuPacket.REQ_COMMAND_AP_SSID.equals(strCmd) || McuPacket.REQ_COMMAND_AP_PW.equals(strCmd)){
//                ArrayList<String> sendDataArray = new ArrayList<>();
                ArrayList<ArrayList<Byte>> sendDataArray = new ArrayList<>();

                if(null != strSendData && 0 < strSendData.length){
                    String strData = strSendData[0].getBytes().length + McuPacket.SEND_PIPE_SEPARATOR + strSendData[0] + McuPacket.SEND_END_SEPARATOR;

                    byte[] sendDataByte = strData.getBytes();
                    int nDataLength = sendDataByte.length;

                    // 전달 데이타가 16byte를 초과하여 패킷 분리
                    int nDvide = nDataLength / McuPacket.PACKET_DATA_MAX_LENGTH;
                    int nMod = nDataLength % McuPacket.PACKET_DATA_MAX_LENGTH;

                    if (nMod > 0) {
                        nDvide++;
                    }

                    for (int i = 0; i < nDvide; i++) {
                        StringBuilder strCustomData = new StringBuilder();
                        strCustomData.append(strCmd);
                        strCustomData.append(McuPacket.SEND_PIPE_SEPARATOR);

                        int lastIdx = (i + 1) * McuPacket.PACKET_DATA_MAX_LENGTH;

                        ArrayList<Byte> arrByte = new ArrayList<>();

                        if (lastIdx <= nDataLength) {
                            for(int j=0; j<strCustomData.length(); j++){
                                arrByte.add((byte) strCustomData.charAt(j));
                            }

                            for(int j=i * McuPacket.PACKET_DATA_MAX_LENGTH; j<lastIdx; j++){
                                arrByte.add((byte) sendDataByte[j]);
                            }
                        } else {
                            for(int j=0; j<strCustomData.length(); j++){
                                arrByte.add((byte) strCustomData.charAt(j));
                            }

                            for(int j=i * McuPacket.PACKET_DATA_MAX_LENGTH; j<nDataLength; j++){
                                arrByte.add((byte) sendDataByte[j]);
                            }
                        }

                        sendDataArray.add(arrByte);
                    }

                    for (ArrayList sendPacket : sendDataArray) {
                        byte sendByte[] = new byte[sendPacket.size()];
                        for(int i=0; i<sendPacket.size(); i++){
                            sendByte[i] = (byte) sendPacket.get(i);
                        }

                        mBlufiClient.postCustomData(sendByte);

                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }else{
                StringBuilder strCustomSendData = new StringBuilder();
                strCustomSendData.append(strCmd);

                if(null != strSendData){
                    boolean isFirstData = true;

                    for(String data : strSendData){
                        if(isFirstData){
                            strCustomSendData.append(McuPacket.SEND_CMD_SEPARATOR);
                            isFirstData = false;
                        }else{
                            strCustomSendData.append(McuPacket.SEND_DATA_SEPARATOR);
                        }

                        strCustomSendData.append(data);
                    }
                }

                mBlufiClient.postCustomData(strCustomSendData.toString().getBytes());

                CommonUtils.customLog(TAG, "SendPacket Command : " + strCustomSendData.toString(), Log.ERROR);
            }
        }else{
            CommonUtils.customLog(TAG, "SendPacket Command : ", Log.ERROR);
        }

        mTimeoutHandler.postDelayed(timeRunnable, REQUEST_TIME_CNT);
    }



    /**
     * ikHwang 2019-07-26 오전 9:00 MCU로 부터 전달받은 AP 리스트 파싱
     * @param data
     * @return
     */
    private int parseWifiScanList(String data) {
        int returnValue = -1;

        if(TextUtils.isEmpty(data)) return returnValue;

        String[] arrayAp = data.split(McuPacket.ARRAY_SEPARATOR);

        if(0 < arrayAp.length){
            for(String strAp : arrayAp){
                try {
                    String[] apData = strAp.split(McuPacket.RECV_DATA_SEPARATOR);

                    if(3 == apData.length){
                        WifiAP ap = new WifiAP();
                        ap.security = Integer.valueOf(apData[0]);
//                        ap.rssi = Integer.valueOf(apData[1]);
                        ap.rssi = WifiManager.calculateSignalLevel(-Integer.valueOf(apData[1]), 5);
                        ap.ssid = apData[2];

                        if(!TextUtils.isEmpty(ap.ssid)){
                            mTempWifiAPs.add(ap);

                            CommonUtils.customLog(TAG, "Temp AP List ssid : " + ap.ssid, Log.ERROR);
                        }
                    }else if(1 == apData.length){ // MCU에서 전달해주는 AP 전달 횟수
//                        if("0".equals(apData[0])) returnValue = true;
                        returnValue = Integer.parseInt(apData[0]);

                        CommonUtils.customLog(TAG, "AP List Cnt : " + returnValue, Log.ERROR);
                    }
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
            }
        }

        return returnValue;
    }

    Runnable bleSearchTimeOutRunnable = new Runnable() {
        @Override
        public void run() {
            Intent sendIntent = new Intent(KDBlueToothManager.BLE_SEARCH_TIME_OUT);
            sendBroadcast(sendIntent);
        }
    };

    Runnable timeRunnable = new Runnable() {
        @Override
        public void run() {
            errorRequest();
        }
    };

    /**
     * ikHwang 2019-07-10 오전 9:58 Command 요청에 대한 실패 처리 (CustomData 전송 실패 및 타임아웃 경과)
     */
    private void errorRequest(){
        DismissConnectDialog();

        CommonUtils.customLog(TAG, "errorRequest LastCommand : " + lastCommand, Log.ERROR);

        switch (lastCommand){
            case McuPacket.REQ_COMMAND_GET_DEV_ID: // 룸콘 아이디 조회 실패 처리
            case McuPacket.REQ_COMMAND_CHANGE_DEV_ID: // 룸콘 아이디 생성 실패 처리
            case McuPacket.REQ_COMMAND_SERVER_CONNECT: // 서버 연결 오류
                CommonUtils.showToast(RegisterPrismDeviceActivity.this, getString(R.string.RegisterPrismDeviceActivity_str_6));

                bleClose();
                break;

            case McuPacket.REQ_COMMAND_WIFI_SCAN: // AP 리스트 스캔 요청 실패 처리
                // ikHwang 2019-07-26 오전 9:07 MCU에서 AP 리스트를 여러번 전달시 타임아웃 시간이 초과된 경우 수신된 데이터를 이용하여 화면 업데이트 진행
                if(mTempWifiAPs.size() > 0){
                    mWifiAPs.clear();
                    mWifiAPs.addAll(mTempWifiAPs);

                    if(currFragment instanceof RegisterPrismDevice3Fragment){
                        ((RegisterPrismDevice3Fragment) currFragment).updateApList(mWifiAPs);
                    }
                }else{
                    CommonUtils.showToast(RegisterPrismDeviceActivity.this, getString(R.string.RegisterPrismDeviceActivity_str_8));
                }
                break;

            case McuPacket.REQ_COMMAND_AP_SSID: // AP 연결 요청 실패 처리
            case McuPacket.REQ_COMMAND_AP_PW: // AP 연결 요청 실패 처리
                CommonUtils.showToast(RegisterPrismDeviceActivity.this, getString(R.string.RegisterPrismDeviceActivity_str_7));
                break;
        }

        lastCommand = "";
    }

    private void bleClose(){
        try {
            if(null != mBlufiClient){
                mBlufiClient.requestCloseConnection();
                mBlufiClient.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            if(null != mGatt) mGatt.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

