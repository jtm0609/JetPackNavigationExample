package kr.co.kdone.airone.activity.more;

import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Timer;
import java.util.TimerTask;

import kr.co.kdone.airone.CleanVentilationApplication;
import kr.co.kdone.airone.R;
import kr.co.kdone.airone.activity.PopupActivity;
import kr.co.kdone.airone.activity.control.ControlActivity;
import kr.co.kdone.airone.aws.AWSIoTMqttHelper;
import kr.co.kdone.airone.aws.DataChangeListener;
import kr.co.kdone.airone.aws.PubSubData;
import kr.co.kdone.airone.data.DeviceManager;
import kr.co.kdone.airone.utils.CommonUtils;
import kr.co.kdone.airone.utils.HomeInfoDataParser;
import kr.co.kdone.airone.utils.HttpApi;
import kr.co.kdone.airone.widget.WidgetUtils;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

import static kr.co.kdone.airone.aws.AWSIoTMqttHelper.MQTT_CONNECTION_TIMEOUT;
import static kr.co.kdone.airone.aws.AWSIoTMqttHelper.MQTT_CONNECT_ERROR;
import static kr.co.kdone.airone.utils.CommonUtils.DismissConnectDialog;
import static kr.co.kdone.airone.utils.CommonUtils.IsRunningProgress;
import static kr.co.kdone.airone.utils.CommonUtils.RESULT_CANT_CONTROL;
import static kr.co.kdone.airone.utils.CommonUtils.RESULT_ERROR_CONNECTION_WITH_SERVER;
import static kr.co.kdone.airone.utils.CommonUtils.RESULT_ERROR_CONNECT_TIMEOUT;
import static kr.co.kdone.airone.utils.CommonUtils.RESULT_ERROR_FIRTST_SENDMASSAGE;
import static kr.co.kdone.airone.utils.ProtocolType.COMMAND_DID;
import static kr.co.kdone.airone.utils.ProtocolType.COMMAND_LED_STATUS_INFO;
import static kr.co.kdone.airone.utils.ProtocolType.COMMAND_SD;
import static kr.co.kdone.airone.utils.ProtocolType.LED_CONTROL;
import static kr.co.kdone.airone.utils.ProtocolType.LED_REQ;

/**
 * ikHwang 2019-08-26 오전 10:57 에어모니터 밝기 설정 액티비티
 */
public class AirMonitorLEDSettingActivity extends AppCompatActivity implements View.OnClickListener, DataChangeListener {
    private final String TAG = getClass().getSimpleName();

    private CheckBox[] checkBoxes;

    private boolean mIsSendFirstMessage = false;
    private boolean isActivityForeground = false;
    private boolean mIsShowErrorPopup;

    private Timer mCanNotControlTimer;
    private long CONTROL_DELAYTIME = 10 * 1000;

    private boolean isUpdateFlag = false;

    private boolean isSendFlag = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_airmonitor_led_setting);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        initLayout();
    }

    @Override
    protected void onResume() {
        super.onResume();

        isActivityForeground = true;

        if (!mIsShowErrorPopup) {
            // ikHwang 2019-07-03 제품과 연결되어 있으면 화면 재진입시 제품연결확인 및 DID등 처리 예외처리
            if(null == CleanVentilationApplication.awsIoTMqttHelper){
                getDeviceList();
            }else{
                sendToServer(LED_REQ, 0);
            }

            CleanVentilationApplication.getInstance().setDataChangedListener(this);
        }

        mIsShowErrorPopup = false;
    }

    @Override
    protected void onPause() {
        super.onPause();

        isActivityForeground = false;

        if (mCanNotControlTimer != null) {
            mCanNotControlTimer.cancel();
        }

        if (IsRunningProgress()) {
            CommonUtils.DismissConnectDialog();
        }

        CleanVentilationApplication.getInstance().setDataChangedListener(null);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(0, R.anim.slide_right_out);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.btnBack:
                onBackPressed();
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case RESULT_CANT_CONTROL:
            case RESULT_ERROR_FIRTST_SENDMASSAGE:
            case RESULT_ERROR_CONNECTION_WITH_SERVER:
                CleanVentilationApplication.getInstance().awsMqttDisConnect();
                finish();
                break;

            case RESULT_ERROR_CONNECT_TIMEOUT: // 제어 연결 10분 타임아웃
                CleanVentilationApplication.getInstance().awsMqttDisConnect();
                finish();
                break;
        }
    }

    private void initLayout(){
        checkBoxes = new CheckBox[]{findViewById(R.id.chk_brightness_0), findViewById(R.id.chk_brightness_1), findViewById(R.id.chk_brightness_2), findViewById(R.id.chk_brightness_3)};

        for(int i=0; i<checkBoxes.length; i++){
            final int finalI = i;
            checkBoxes[i].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(isUpdateFlag) return;

                    isUpdateFlag = true;
                    setCheckBoxesUpdate(finalI);
                    sendToServer(LED_CONTROL, finalI);
                }
            });
        }
    }

    private void updateUI() {
        isUpdateFlag = true;

        int nBrightness = DeviceManager.getInstance().getCurrentDevice().led.getBrightness();

        setCheckBoxesUpdate(nBrightness);

        isUpdateFlag = false;
    }

    /**
     * ikHwang 2020-04-09 오전 11:00 밝기 체크박스 설정
     * @param select
     */
    private void setCheckBoxesUpdate(int select){
        // 수신 데이터에 지정된 최대값 3을 초과하는 경우가 있어 최대값 지정
        if(3 < select) select = 3;

        for(int i=0; i<checkBoxes.length; i++){
            if(i == select){
                checkBoxes[i].setChecked(true);
                checkBoxes[i].setEnabled(false);
            }else{
                checkBoxes[i].setChecked(false);
                checkBoxes[i].setEnabled(true);
            }
        }
    }

    /**
     * 제어 명령 후 타이머 설정.
     */
    private void setCanNotControlTimer() {
        if (mCanNotControlTimer != null) {
            mCanNotControlTimer.cancel();
        }

        mCanNotControlTimer = new Timer();
        mCanNotControlTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                errorToConnectWithServer(false);
            }
        }, CONTROL_DELAYTIME);
    }

    /**
     * 에러 발생시 예외처리 함수
     */
    private void errorToConnectWithServer(boolean isTimeout) {
        CommonUtils.customLog(TAG, "errorToConnectWithServer()", Log.ERROR);

        CleanVentilationApplication.getInstance().awsMqttDisConnect();

        if(isTimeout){
            // 10분 타임아웃
            mIsShowErrorPopup = true;
            Intent intent = new Intent(AirMonitorLEDSettingActivity.this, PopupActivity.class);
            intent.putExtra(PopupActivity.POPUP_MODE, PopupActivity.MODE_NO_BUTTON);
            intent.putExtra(PopupActivity.POPUP_TIMEOUT, 3000);
            intent.putExtra(PopupActivity.POPUP_BODY_TEXT, getString(R.string.error_connect_for_control_device));
            startActivityForResult(intent, RESULT_ERROR_CONNECT_TIMEOUT);
            overridePendingTransition(0, 0);
        }else{
            if(isActivityForeground){
                if (!mIsShowErrorPopup) {
                    mIsShowErrorPopup = true;
                    if (IsRunningProgress())
                        CommonUtils.DismissConnectDialog();

                    if (!mIsSendFirstMessage) {
                        Intent intent = new Intent(AirMonitorLEDSettingActivity.this, PopupActivity.class);
                        intent.putExtra(PopupActivity.POPUP_MODE, PopupActivity.MODE_YESNO);
                        intent.putExtra(PopupActivity.POPUP_BODY_TEXT, getString(R.string.error_connect_room_con));
                        intent.putExtra(PopupActivity.POPUP_CONFIRM_BUTTON_TEXT, getString(R.string.detail_view));
                        intent.putExtra(PopupActivity.POPUP_CANCEL_BUTTON_TEXT, getString(R.string.cancel));
                        startActivityForResult(intent, RESULT_ERROR_FIRTST_SENDMASSAGE);
                        overridePendingTransition(0, 0);
                    } else {
                        Intent intent = new Intent(AirMonitorLEDSettingActivity.this, PopupActivity.class);
                        intent.putExtra(PopupActivity.POPUP_MODE, PopupActivity.MODE_NO_BUTTON);
                        intent.putExtra(PopupActivity.POPUP_TIMEOUT, 3000);
                        intent.putExtra(PopupActivity.POPUP_BODY_TEXT, getString(R.string.error_connect_for_control_device));
                        startActivityForResult(intent, RESULT_ERROR_CONNECTION_WITH_SERVER);
                        overridePendingTransition(0, 0);
                    }
                }
            }
        }
    }

    /**
     * 디바이스 리스트 획득 함수
     */
    private void getDeviceList() {
        CommonUtils.customLog(TAG, "start getDeviceList", Log.ERROR);

        setCanNotControlTimer();
        CommonUtils.displayProgress(this, "", "");
        try {
            HttpApi.PostV2GetMainInfo( //V2A 작업.
                    CleanVentilationApplication.getInstance().getUserInfo().getId(),
                    new Callback() {
                        @Override
                        public void onFailure(Call call, IOException e) {
                            mIsShowErrorPopup = true;
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    DismissConnectDialog();
                                    Intent intent = new Intent(AirMonitorLEDSettingActivity.this, PopupActivity.class);
                                    intent.putExtra(PopupActivity.POPUP_MODE, PopupActivity.MODE_NORMAL);
                                    intent.putExtra(PopupActivity.POPUP_BODY_TEXT, getString(R.string.toast_result_can_not_search));
                                    startActivityForResult(intent, RESULT_CANT_CONTROL);
                                    overridePendingTransition(0, 0);
                                }
                            });
                        }

                        @Override
                        public void onResponse(Call call, Response response) throws IOException {
                            try {
                                String strResponse =  response.body().string();

                                CommonUtils.customLog(TAG, "main/info : " + strResponse, Log.ERROR);

                                if(TextUtils.isEmpty(strResponse)){
                                    DismissConnectDialog();
                                    Intent intent = new Intent(AirMonitorLEDSettingActivity.this, PopupActivity.class);
                                    intent.putExtra(PopupActivity.POPUP_MODE, PopupActivity.MODE_NORMAL);
                                    intent.putExtra(PopupActivity.POPUP_BODY_TEXT, getString(R.string.toast_result_can_not_search));
                                    startActivityForResult(intent, RESULT_CANT_CONTROL);
                                    overridePendingTransition(0, 0);
                                }else{
                                    JSONObject json_data = new JSONObject(strResponse);

                                    switch (json_data.optInt("code", 0)){
                                        case HttpApi.RESPONSE_SUCCESS: // 디바이스 정보 조회 성공
                                            if(json_data.has("data")){
                                                // ikHwang 2019-05-21 오후 1:32 메인화면 이동시 화면을 구성하기 위해 데이터 파싱
                                                HomeInfoDataParser.paserHomeInfo(CleanVentilationApplication.getInstance(), json_data.getJSONObject("data"), true);

                                                Intent sendIntent = new Intent(WidgetUtils.WIDGET_UPDATE_ACTION);
                                                sendBroadcast(sendIntent, getString(R.string.br_permission));
                                            }

                                            if(CleanVentilationApplication.getInstance().isConnectedRoomController()){
                                                try {
                                                    CleanVentilationApplication.getInstance().awsMqttConnect();
                                                } catch (Exception e) {
                                                    errorToConnectWithServer(false);
                                                }
                                            }else{
                                                runOnUiThread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        mIsShowErrorPopup = true;
                                                        Intent intent = new Intent(AirMonitorLEDSettingActivity.this, PopupActivity.class);
                                                        intent.putExtra(PopupActivity.POPUP_MODE, PopupActivity.MODE_YESNO);
                                                        intent.putExtra(PopupActivity.POPUP_BODY_TEXT, getString(R.string.error_connect_room_con));
                                                        intent.putExtra(PopupActivity.POPUP_CONFIRM_BUTTON_TEXT, getString(R.string.detail_view));
                                                        intent.putExtra(PopupActivity.POPUP_CANCEL_BUTTON_TEXT, getString(R.string.cancel));
                                                        startActivityForResult(intent, RESULT_ERROR_FIRTST_SENDMASSAGE);
                                                        overridePendingTransition(0, 0);
                                                    }
                                                });
                                            }
                                            break;

                                        default:
                                            errorToConnectWithServer(false);
                                            break;
                                    }
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                                errorToConnectWithServer(false);
                            }
                        }
                    });
        } catch (Exception e) {
            e.printStackTrace();
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    DismissConnectDialog();
                }
            });
            CommonUtils.showToast(AirMonitorLEDSettingActivity.this, getString(R.string.toast_result_can_not_search));
        }
    }

    /**
     * 제어 명령을 서버로 보내는 함수
     */
    public void sendToServer(int req, int nLedLevel) {
        setCanNotControlTimer();

        CommonUtils.displayProgress(this, "", "");

        String sessionToken = CleanVentilationApplication.authData.sessionToken;
        String modelCode = CleanVentilationApplication.getInstance().getRoomControllerDeviceModelCode();
        String deviceId = CleanVentilationApplication.getInstance().getRoomControllerDeviceID();

        String topic = "";
        String requestData = "";

        mIsSendFirstMessage = true;

        switch (req){
            case 0 : // DID
                topic = PubSubData.getDidPubTopic(modelCode, deviceId);
                requestData = PubSubData.getDidPubData(sessionToken, modelCode, deviceId);
                break;

            case 1 : // SD
            case 2 : // SD
                topic = PubSubData.getBrightnessPubTopic(modelCode, deviceId);
                requestData = PubSubData.getBrightnessPubData(sessionToken, modelCode, deviceId, req, nLedLevel);
                break;
        }

        if(null == CleanVentilationApplication.awsIoTMqttHelper){
            errorToConnectWithServer(false);
        }else{
            CleanVentilationApplication.getInstance().publishData(topic, requestData);
        }
    }

    @Override
    public void onDataChanged(int cmd) {
        // 응답 수신후 상태 변경
        mIsSendFirstMessage = false;

        if (cmd == COMMAND_DID) {
            CommonUtils.customLog(TAG, "onDataChanged() : COMMAND_DID", Log.ERROR);

            if (mCanNotControlTimer != null) mCanNotControlTimer.cancel();

            // 최초 연결시 DID 조회후 LED 밝기 상태 조회
            sendToServer(LED_REQ, 0);
        } else if (cmd == COMMAND_SD) {
            CommonUtils.customLog(TAG, "onDataChanged() : COMMAND_SD", Log.ERROR);

            String errorTxt = "";
            try {
                DecimalFormat df = new DecimalFormat("000");
                int errorCode = DeviceManager.getInstance().getCurrentDevice().sd.errorCD;

                if(770 == errorCode || 170 == errorCode){
                    errorTxt = String.format(getString(getResources().getIdentifier("error_code_device_" + errorCode, "string", getPackageName())), errorCode, DeviceManager.getInstance().getCurrentDevice().sd.diffuserNumber);
                }else{
                    errorTxt = String.format(getString(getResources().getIdentifier("error_code_device_" + errorCode, "string", getPackageName())), errorCode);
                }
            } catch (Exception e) {
            }

            if (!errorTxt.equals("")) {
                Intent intent = new Intent(AirMonitorLEDSettingActivity.this, PopupActivity.class);
                intent.putExtra(PopupActivity.POPUP_MODE, PopupActivity.MODE_NORMAL);
                intent.putExtra(PopupActivity.POPUP_BODY_TEXT, errorTxt);
                startActivityForResult(intent, RESULT_ERROR_CONNECTION_WITH_SERVER);
                overridePendingTransition(0, 0);
                return;
            }
        }else if(cmd == COMMAND_LED_STATUS_INFO){ // ikHwang 2019-08-26 오후 4:09 에어모니터 밝기 상태 조회
            CommonUtils.customLog(TAG, "onDataChanged() : COMMAND_LED_STATUS_INFO", Log.ERROR);

            if (mCanNotControlTimer != null) mCanNotControlTimer.cancel();

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    updateUI();
                    if (IsRunningProgress()) CommonUtils.DismissConnectDialog();
                }
            });
        }
    }

    @Override
    public void onError(int nState) {
        switch (nState){
            case MQTT_CONNECT_ERROR: // mqtt 연결 오류

                break;

            case MQTT_CONNECTION_TIMEOUT: // 10분 타임아웃 종료
                errorToConnectWithServer(true);
                break;
        }
    }

    @Override
    public void onStatusChanged(int status) {
        switch (status){
            case AWSIoTMqttHelper.MQTT_INIT_CONNECTED:
                // Mqtt 연결 성공
                // 연결 성공후 상태 수신하기 위한 subscribe 등록
                CleanVentilationApplication.getInstance().subscribeData();
                break;

            case AWSIoTMqttHelper.MQTT_INIT_SUBCRIBE_SUCCESS:
                // subScribe 요청후 LED 밝기 조회 요청
                sendToServer(0, 0);
                break;

            case AWSIoTMqttHelper.MQTT_INIT_CONNECTION_LOST:
                // 서버 연결 해제
                errorToConnectWithServer(true);
                break;
        }
    }
}