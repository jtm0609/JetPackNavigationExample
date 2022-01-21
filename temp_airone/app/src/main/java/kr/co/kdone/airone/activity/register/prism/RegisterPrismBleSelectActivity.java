package kr.co.kdone.airone.activity.register.prism;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.RecyclerView;

import android.view.View;

import java.util.ArrayList;
import java.util.LinkedHashMap;

import kr.co.kdone.airone.R;
import kr.co.kdone.airone.activity.PopupActivity;
import kr.co.kdone.airone.adapter.BleListAdapter;
import kr.co.kdone.airone.utils.CommonUtils;
import kr.co.kdone.airone.utils.KDBlueToothManager;

import static kr.co.kdone.airone.utils.CommonUtils.DismissConnectDialog;
import static kr.co.kdone.airone.utils.CommonUtils.IsRunningProgress;
import static kr.co.kdone.airone.utils.CommonUtils.displayProgress;

/**
 * ikHwang 2019-06-07 오전 10:07 블루투스 리스트 선택 화면 별도 UI 처리 (사용시 매니페스트에 등록 필요)
 */
public class RegisterPrismBleSelectActivity extends AppCompatActivity {
    private final int                       REQUEST_ACTIVE_BLUETOOTH_DEVICE   = 1002;               // 블루투스 활성화 ON 상태 변경 안내 팝업

    private final int                       REQUEST_TIME_CNT                  = 6 * 10 * 1000;      // BLE 검색 타임아웃

    private RecyclerView                    mBleListView;                                           // 블루투스 디바이스 리스트
    private BleListAdapter                  mBleAdapter;                                            // 블루투스 디바이스 리스트 구성 어댑터
    private ArrayList<BluetoothDevice>      bleList;                                                // 블루투스 디바이스 목록
    private LinkedHashMap<String, BluetoothDevice> tempBleList;                                    // 블루투스 디바이스 목록 (중복을 제거하기 위해 사용)

    private KDBlueToothManager              kdBlueToothManager;

    private boolean                         isBleSearching = false;                                 // BLE 검색 상태
    private Handler                         mHandler;                                               // 프래그먼트 이동시 CustomData 요청시 처리가 누락되어 요청에대한 딜레이를 주기위해 사용

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_prism_ble_select);

        bleList = new ArrayList<>();
        tempBleList = new LinkedHashMap<>();

        bleList = getIntent().getParcelableArrayListExtra("bleList");

        initLayout();

        initBlueTooth();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // 블루투스 브로드캐스트 리시버 제거
        if(null != kdBlueToothManager){
            kdBlueToothManager.unRegisterbluetoothReceiver();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode){
            case REQUEST_ACTIVE_BLUETOOTH_DEVICE: // 블루투스 활성화 안내 팝업
                if(Activity.RESULT_OK == resultCode){ // 확인 버튼 선택시 처리 (블루투스 설정 화면으로 이동)
                    startActivityForResult(new Intent(Settings.ACTION_BLUETOOTH_SETTINGS), 0);
                }else{ // 취소 팝업 선택시 처리하지 않음 (현재화면 유지)
                    // 팝업 추가 필요시 작업
                }
                break;
        }
    }

    /**
     * ikHwang 2019-06-07 오전 10:01 레이아웃 초기화
     */
    private void initLayout(){
        mHandler = new Handler();

        mBleListView = findViewById(R.id.listBle);

        mBleAdapter = new BleListAdapter(bleList, new BleListAdapter.OnItemClickListener() {
            @Override
            public void onItemClickListener(int position) {
                // 블루투스 디바이스 선택 처리
                Intent intent = new Intent();
                intent.putExtra("selectBleDev", bleList.get(position));
                setResult(Activity.RESULT_OK, intent);
                finish();
                overridePendingTransition(0, R.anim.slide_right_out);
            }
        });
        mBleListView.setAdapter(mBleAdapter);
        mBleListView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
    }

    public void onClick(View v){
        switch (v.getId()){
            case R.id.btnBack: // 연결하기 화면 X버튼 선택
                finish();
                overridePendingTransition(0, R.anim.slide_right_out);
                break;

            case R.id.layoutRefresh: // 새로고침 버튼
                // 블루투스 화성화시 검색 요청
                if(kdBlueToothManager.isBluetoothActive()){
//                    kdBlueToothManager.startDisvocery();
                    kdBlueToothManager.scanBLE(true);
                }else{ // 블루투스 꺼져있는 경우 활성화 요청
                    Intent intent = new Intent(RegisterPrismBleSelectActivity.this, PopupActivity.class);
                    intent.putExtra(PopupActivity.POPUP_MODE, PopupActivity.MODE_YESNO);
                    intent.putExtra(PopupActivity.POPUP_BODY_TEXT, getString(R.string.RegisterPrismDeviceActivity_str_2));
                    intent.putExtra(PopupActivity.POPUP_CONFIRM_BUTTON_TEXT, getString(R.string.RegisterPrismDeviceActivity_str_11));
                    intent.putExtra(PopupActivity.POPUP_CANCEL_BUTTON_TEXT, getString(R.string.RegisterPrismDeviceActivity_str_12));
                    startActivityForResult(intent, REQUEST_ACTIVE_BLUETOOTH_DEVICE);
                    overridePendingTransition(0, 0);
                }
                break;
        }
    }

    /**
     * ikHwang 2019-07-02 오전 9:02 블루투스 검색 하기위한 매니저 초기화
     */
    public void initBlueTooth(){
        kdBlueToothManager = new KDBlueToothManager(this, KDBlueToothManager.PREFIX, new KDBlueToothManager.BluetoothListener() {
            @Override
            public void onNoModule() {
                // 블루투스 검색 완료 후 진입 가능한 화면으로 별도로 처리하지 않음
            }

            @Override
            public void onBluetoothState(int state) {
                switch (state){
                    case KDBlueToothManager.BLUETOOTH_STATE_OFF: // 블루투스가 OFF인 경우 ON으로 강제 처리
//                        kdBlueToothManager.setBluetoothEnable(true);
                        break;

                    case KDBlueToothManager.BLUETOOTH_STATE_SEARCHING_START: // 블루투스 검색 시작 다이얼로그 표시처리\
                        if(isBleSearching) return;

                        if (!IsRunningProgress()) displayProgress(RegisterPrismBleSelectActivity.this,"", "");
                        bleList.clear(); // 블루투스 리스트 초기화

                        isBleSearching = true;
                        mHandler.postDelayed(bleSearchTimeOutRunnable, REQUEST_TIME_CNT);
                        break;

                    case KDBlueToothManager.BLUETOOTH_STATE_SEARCHING_END: // 블루투스 검색 완료 다이얼로그 제거
                        if(!isBleSearching) return;

                        isBleSearching = false;
                        mHandler.removeCallbacks(bleSearchTimeOutRunnable);

                        DismissConnectDialog();

                        bleList.addAll(tempBleList.values());
                        tempBleList.clear();

                        if(bleList.size() == 0) { // 검색된 블루투스 디바이스 없음, 안내 팝업 표시
                            CommonUtils.showToast(RegisterPrismBleSelectActivity.this, getString(R.string.RegisterPrismBleSelectActivity));
                        }else{
                            mBleAdapter.notifyDataSetChanged();
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
            }

            @Override
            public void onReceiveData(byte[] data) {
            }
        });

        kdBlueToothManager.registerbluetoothReceiver();
    }

    Runnable bleSearchTimeOutRunnable = new Runnable() {
        @Override
        public void run() {
            Intent sendIntent = new Intent(KDBlueToothManager.BLE_SEARCH_TIME_OUT);
            sendBroadcast(sendIntent);
        }
    };
}
