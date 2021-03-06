package kr.co.kdone.airone.activity.more;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Timer;
import java.util.TimerTask;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import kr.co.kdone.airone.CleanVentilationApplication;
import kr.co.kdone.airone.R;
import kr.co.kdone.airone.activity.PopupActivity;
import kr.co.kdone.airone.activity.control.ControlActivity;
import kr.co.kdone.airone.activity.info.RoomControlerDetailActivity;
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
import static kr.co.kdone.airone.utils.CommonUtils.MODE_DEVICE_ROOM_CON;
import static kr.co.kdone.airone.utils.CommonUtils.RESULT_CANT_CONTROL;
import static kr.co.kdone.airone.utils.CommonUtils.RESULT_DETAIL_CHECK;
import static kr.co.kdone.airone.utils.CommonUtils.RESULT_ERROR_CONNECTION_WITH_SERVER;
import static kr.co.kdone.airone.utils.CommonUtils.RESULT_ERROR_CONNECT_TIMEOUT;
import static kr.co.kdone.airone.utils.CommonUtils.RESULT_ERROR_FIRTST_SENDMASSAGE;
import static kr.co.kdone.airone.utils.CommonUtils.RESULT_FILTER_ALARM_1;
import static kr.co.kdone.airone.utils.CommonUtils.RESULT_FILTER_ALARM_2;
import static kr.co.kdone.airone.utils.CommonUtils.RESULT_FILTER_RESET;
import static kr.co.kdone.airone.utils.ProtocolType.COMMAND_DID;
import static kr.co.kdone.airone.utils.ProtocolType.COMMAND_SD;
import static kr.co.kdone.airone.utils.ProtocolType.USE;
import static kr.co.kdone.airone.utils.ProtocolType.USE_NOT;

/**
 * ikHwang 2019-06-04 ?????? 9:51 ????????? ?????? ??????
 */
public class MoreFilterManagePrismActivity extends AppCompatActivity implements View.OnClickListener, DataChangeListener {
    private final String TAG = getClass().getSimpleName();

    private int mReqErrAlarm = USE_NOT;
    private int mHepaFilterReset = USE_NOT;
    private int mHepaFilterPushReset = USE_NOT;

    private View filterLife;
    private TextView filterLifeText;
    private Button filterBuyBtn;
    private Button filterResetBtn;
    private ViewGroup.LayoutParams filterHeight;
    private View filterChangeDot;
    private TextView filterChangeText;
    private TextView filterRemainTimeText;
    private TextView filterUsedTimeText;

    private final float FILTER_MAX_HEIGHT = 220;
    private float MAX_HEPA_FILTER_TIME = 100; // ???????????? ?????? ?????? ??????

    //tcp
    private int mRoomZone = 0;
    private boolean mIsPower = false;
    private boolean mIsSendFirstMessage = false;
    private boolean mIsShowErrorPopup;
    private Timer mCanNotControlTimer;
    private long CONTROL_DELAYTIME = 10 * 1000;

    // ikHwang 2019-06-24 ?????? 11:57 ?????? ?????? ?????? ?????? ??????
    private boolean isFilterResetRequest = false;

    private boolean isActivityForeground = false;

    private LinearLayout layout_progress_time;
    private LinearLayout layout_filter_life;
    private TextView text_filter_life;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_more_filter_manage);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        init();
    }

    private void init(){
        filterLife = findViewById(R.id.filterLife);
        filterLifeText = findViewById(R.id.filterLifeText);
        filterBuyBtn = findViewById(R.id.btnFilterBuy);
        filterResetBtn = findViewById(R.id.btnFilterReset);
        filterChangeDot = findViewById(R.id.filterChangeDot);
        filterChangeText = findViewById(R.id.filterChangeText);
        filterUsedTimeText = findViewById(R.id.filterUsedTimeText);
        filterRemainTimeText = findViewById(R.id.filterRemainTimeText);

        text_filter_life = findViewById(R.id.text_filter_life);
        layout_filter_life = findViewById(R.id.layout_filter_life);

        layout_progress_time = findViewById(R.id.layout_progress_time);

        filterBuyBtn.setOnClickListener(this);
        filterResetBtn.setOnClickListener(this);

        layout_progress_time.setVisibility(View.INVISIBLE);
    }


    @Override
    protected void onResume() {
        super.onResume();

        isActivityForeground = true;

        /**
         * ???????????? ?????????
         */
        resetUsedFuncContorl();

        if (!mIsShowErrorPopup) {
            // ikHwang 2019-07-03 ????????? ???????????? ????????? ?????? ???????????? ?????????????????? ??? DID??? ?????? ????????????
            if(null == CleanVentilationApplication.awsIoTMqttHelper){
                getDeviceList();
            }else{
                sendToServer(2);
            }
        }

        CleanVentilationApplication.getInstance().setDataChangedListener(this);

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
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case RESULT_CANT_CONTROL:
            case RESULT_ERROR_CONNECTION_WITH_SERVER:
                if(715 == DeviceManager.getInstance().getCurrentDevice().sd.errorCD
                        || 160 == DeviceManager.getInstance().getCurrentDevice().sd.errorCD
                        || 161 == DeviceManager.getInstance().getCurrentDevice().sd.errorCD){
                    CleanVentilationApplication.getInstance().awsMqttDisConnect();
                    finish();
                }
                break;

            case RESULT_ERROR_CONNECT_TIMEOUT: // ?????? ?????? 10??? ????????????
                CleanVentilationApplication.getInstance().awsMqttDisConnect();
                finish();
                break;

            case RESULT_ERROR_FIRTST_SENDMASSAGE:
                CleanVentilationApplication.getInstance().awsMqttDisConnect();

                if (resultCode == RESULT_OK) {
                    Intent intent = new Intent(this, RoomControlerDetailActivity.class);
                    intent.putExtra("mode", MODE_DEVICE_ROOM_CON);
                    startActivityForResult(intent, RESULT_DETAIL_CHECK);
                } else {
//                    mIsShowErrorPopup = false;
                    finish();
                }
                break;

            case RESULT_DETAIL_CHECK:
            case RESULT_FILTER_ALARM_1:
            case RESULT_FILTER_ALARM_2:
                mIsShowErrorPopup = false;
                break;

            case RESULT_FILTER_RESET:
                mIsShowErrorPopup = false;
                if (resultCode == RESULT_OK) {
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    isFilterResetRequest = true;
                                    mReqErrAlarm = USE;
                                    mHepaFilterReset = USE;
                                    mHepaFilterPushReset = USE;

                                    sendToServer(3);
                                }
                            });
                        }
                    }, 300);
                }
                break;
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnBack:
                onBackPressed();
                break;

            case R.id.btnFilterBuy :
            {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.navienhouse.com/m/categories/index/replacement_filter_clean_ventilation_filter"));
                startActivity(intent);
            }
            break;

            case R.id.btnFilterReset :
            {
                if(DeviceManager.getInstance().getCurrentDevice().did.apsHasFlag == 2){
                    if(4 == DeviceManager.getInstance().getCurrentDevice().sd.flagHepaFilter){
                        mIsShowErrorPopup = true;
                        Intent intent = new Intent(MoreFilterManagePrismActivity.this, PopupActivity.class);
                        intent.putExtra(PopupActivity.POPUP_MODE, PopupActivity.MODE_NORMAL);
                        intent.putExtra(PopupActivity.POPUP_BODY_TEXT, getString(R.string.MoreFilterManageActivity_str_4));
                        startActivityForResult(intent, RESULT_FILTER_ALARM_2);
                        overridePendingTransition(0, 0);
                        return;
                    }else  if(5 == DeviceManager.getInstance().getCurrentDevice().sd.flagHepaFilter
                            || 3 == DeviceManager.getInstance().getCurrentDevice().sd.flagHepaFilter){
                        return;
                    }
                }

                mIsShowErrorPopup = true;
                Intent intent = new Intent(MoreFilterManagePrismActivity.this, PopupActivity.class);
                intent.putExtra(PopupActivity.POPUP_MODE, PopupActivity.MODE_YESNO);
                intent.putExtra(PopupActivity.POPUP_BODY_TEXT, getString(R.string.MoreFilterManageActivity_str_3));
                intent.putExtra(PopupActivity.POPUP_BODY_COLOR_MARK, getString(R.string.activity_more_filter_manager_str_7));
                intent.putExtra(PopupActivity.POPUP_CONFIRM_BUTTON_TEXT, getString(R.string.SmartAlarmActivity_str_1));
                intent.putExtra(PopupActivity.POPUP_CANCEL_BUTTON_TEXT, getString(R.string.SmartAlarmActivity_str_2));
                startActivityForResult(intent, RESULT_FILTER_RESET);
                overridePendingTransition(0, 0);
            }
            break;
        }
    }

    /**
     * ???????????? ????????? ?????? ??????
     */
    private void getDeviceList() {
        CommonUtils.customLog(TAG, "start getDeviceList", Log.ERROR);

        setCanNotControlTimer();
        CommonUtils.displayProgress(this, "", "");
        try {
            HttpApi.PostV2GetMainInfo( //V2A ??????.
                    CleanVentilationApplication.getInstance().getUserInfo().getId(),
                    new Callback() {
                        @Override
                        public void onFailure(Call call, IOException e) {
                            mIsShowErrorPopup = true;
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    DismissConnectDialog();
                                    Intent intent = new Intent(MoreFilterManagePrismActivity.this, PopupActivity.class);
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
                                    mIsShowErrorPopup = true;
                                    Intent intent = new Intent(MoreFilterManagePrismActivity.this, PopupActivity.class);
                                    intent.putExtra(PopupActivity.POPUP_MODE, PopupActivity.MODE_NORMAL);
                                    intent.putExtra(PopupActivity.POPUP_BODY_TEXT, getString(R.string.toast_result_can_not_search));
                                    startActivityForResult(intent, RESULT_CANT_CONTROL);
                                    overridePendingTransition(0, 0);
                                }else{
                                    JSONObject json_data = new JSONObject(strResponse);

                                    switch (json_data.optInt("code", 0)){
                                        case HttpApi.RESPONSE_SUCCESS:
                                            if(json_data.has("data")){
                                                // ikHwang 2019-05-21 ?????? 1:32 ???????????? ????????? ????????? ???????????? ?????? ????????? ??????
                                                HomeInfoDataParser.paserHomeInfo(CleanVentilationApplication.getInstance(), json_data.getJSONObject("data"), true);

                                                Intent sendIntent = new Intent(WidgetUtils.WIDGET_UPDATE_ACTION);
                                                sendBroadcast(sendIntent, getString(R.string.br_permission));
                                            }

                                            if(CleanVentilationApplication.getInstance().isConnectedRoomController()){
                                                try {
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
                                                                Intent intent = new Intent(MoreFilterManagePrismActivity.this, PopupActivity.class);
                                                                intent.putExtra(PopupActivity.POPUP_MODE, PopupActivity.MODE_YESNO);
                                                                intent.putExtra(PopupActivity.POPUP_BODY_TEXT, getString(R.string.error_connect_room_con));
                                                                intent.putExtra(PopupActivity.POPUP_CONFIRM_BUTTON_TEXT, getString(R.string.detail_view));
                                                                intent.putExtra(PopupActivity.POPUP_CANCEL_BUTTON_TEXT, getString(R.string.cancel));
                                                                startActivityForResult(intent, RESULT_ERROR_FIRTST_SENDMASSAGE);
                                                                overridePendingTransition(0, 0);
                                                            }
                                                        });
                                                    }
                                                } catch (Exception e) {
                                                    errorToConnectWithServer(false);
                                                }
                                            }else{
                                                runOnUiThread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        mIsShowErrorPopup = true;
                                                        Intent intent = new Intent(MoreFilterManagePrismActivity.this, PopupActivity.class);
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
            CommonUtils.showToast(MoreFilterManagePrismActivity.this, getString(R.string.toast_result_can_not_search));
        }
    }

    /**
     * ?????? ?????? ??? ????????? ??????.
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
     * ?????? ????????? ???????????? ??????
     */
    private void errorToConnectWithServer(boolean isTimeOut) {
        CommonUtils.customLog(TAG, "errorToConnectWithServer()", Log.ERROR);

        CleanVentilationApplication.getInstance().awsMqttDisConnect();

        if(isTimeOut){
            // 10??? ????????????
            mIsShowErrorPopup = true;
            Intent intent = new Intent(MoreFilterManagePrismActivity.this, PopupActivity.class);
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

                    mIsShowErrorPopup = true;
                    if (!mIsSendFirstMessage) {
                        Intent intent = new Intent(MoreFilterManagePrismActivity.this, PopupActivity.class);
                        intent.putExtra(PopupActivity.POPUP_MODE, PopupActivity.MODE_YESNO);
                        intent.putExtra(PopupActivity.POPUP_BODY_TEXT, getString(R.string.error_connect_room_con));
                        intent.putExtra(PopupActivity.POPUP_CONFIRM_BUTTON_TEXT, getString(R.string.detail_view));
                        intent.putExtra(PopupActivity.POPUP_CANCEL_BUTTON_TEXT, getString(R.string.cancel));
                        startActivityForResult(intent, RESULT_ERROR_FIRTST_SENDMASSAGE);
                        overridePendingTransition(0, 0);
                    } else {
                        Intent intent = new Intent(MoreFilterManagePrismActivity.this, PopupActivity.class);
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
     * ???????????? ????????? ?????? ????????? ?????? ?????????
     */
    private void resetUsedFuncContorl() {
        mReqErrAlarm = USE_NOT;
        mHepaFilterReset = USE_NOT;
        mHepaFilterPushReset = USE_NOT;
    }

    /**
     * ?????? ????????? ????????? ????????? ??????
     */
    public void sendToServer(int type) {
        setCanNotControlTimer();

        CommonUtils.displayProgress(this, "", "");

        String sessionToken = CleanVentilationApplication.authData.sessionToken;
        String modelCode = CleanVentilationApplication.getInstance().getRoomControllerDeviceModelCode();
        String deviceId = CleanVentilationApplication.getInstance().getRoomControllerDeviceID();

        String topic = "";
        String requestData = "";

        switch (type){
            case 1 : // DID
                topic = PubSubData.getDidPubTopic(modelCode, deviceId);
                requestData = PubSubData.getDidPubData(sessionToken, modelCode, deviceId);
                break;

            case 2 : // SD
                topic = PubSubData.getSdPubTopic(modelCode, deviceId);
                requestData = PubSubData.getSdPubData(sessionToken, modelCode, deviceId);
                break;

            case 3 : // ?????? ?????????
                topic = PubSubData.getRemoteFilterResetPubTopic(modelCode, deviceId);
                requestData = PubSubData.getFilterResetPubData(sessionToken, modelCode, deviceId);
                break;
        }

        if(null == CleanVentilationApplication.awsIoTMqttHelper){
            errorToConnectWithServer(false);
        }else{
            CleanVentilationApplication.getInstance().publishData(topic, requestData);
        }

        resetUsedFuncContorl();
    }

    /**
     * ?????? ????????? ????????? ???????????? ??????
     *
     * @param cmd
     */
    @Override
    public void onDataChanged(int cmd) {
        // ?????? ????????? ?????? ??????
        mIsSendFirstMessage = false;

        if (cmd == COMMAND_DID) {
            CommonUtils.customLog(TAG, "onDataChanged() : COMMAND_DID", Log.ERROR);

            if (mCanNotControlTimer != null) mCanNotControlTimer.cancel();

            sendToServer(2);
        } else if (cmd == COMMAND_SD) {
            CommonUtils.customLog(TAG, "onDataChanged() : COMMAND_SD", Log.ERROR);

            if (mCanNotControlTimer != null) mCanNotControlTimer.cancel();

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
                mIsShowErrorPopup = true;
                Intent intent = new Intent(MoreFilterManagePrismActivity.this, PopupActivity.class);
                intent.putExtra(PopupActivity.POPUP_MODE, PopupActivity.MODE_NORMAL);
//                intent.putExtra(PopupActivity.POPUP_TIMEOUT, 3000);
                intent.putExtra(PopupActivity.POPUP_BODY_TEXT, errorTxt);
                startActivityForResult(intent, RESULT_ERROR_CONNECTION_WITH_SERVER);
                overridePendingTransition(0, 0);
//                return;
            }

            /**
             * ?????? ????????????.
             */
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mIsPower = DeviceManager.getInstance().getCurrentDevice().isPowerOn();
                    int hepaFilterUsedTime = DeviceManager.getInstance().getCurrentDevice().sd.usingTimeOduFilter;

                    updateFilterUI(hepaFilterUsedTime);

                    if (IsRunningProgress()) CommonUtils.DismissConnectDialog();
                }
            });
        }
    }

    @Override
    public void onError(int nState) {
        switch (nState){
            case MQTT_CONNECT_ERROR: // mqtt ?????? ??????

                break;

            case MQTT_CONNECTION_TIMEOUT: // 10??? ???????????? ??????
                errorToConnectWithServer(true);
                break;
        }
    }

    @Override
    public void onStatusChanged(int status) {
        switch (status){
            case AWSIoTMqttHelper.MQTT_INIT_CONNECTED:
                // Mqtt ?????? ??????
                // ?????? ????????? ?????? ???????????? ?????? subscribe ??????
                CleanVentilationApplication.getInstance().subscribeData();
                break;

            case AWSIoTMqttHelper.MQTT_INIT_SUBCRIBE_SUCCESS:
                // subScribe ????????? DID pub ??????
                sendToServer(1);
                break;

            case AWSIoTMqttHelper.MQTT_INIT_CONNECTION_LOST:
                // ?????? ?????? ??????
                errorToConnectWithServer(true);
                break;
        }
    }

    //??????????????? view height UI ??????
    private void updateFilterUI(float usedTime){
        CommonUtils.customLog(TAG, "apsHasFlag : " + DeviceManager.getInstance().getCurrentDevice().did.apsHasFlag
                + " flagHepaFilter : " + DeviceManager.getInstance().getCurrentDevice().sd.flagHepaFilter
                + " deviceVer : " + DeviceManager.getInstance().getCurrentDevice().common.deviceVer, Log.ERROR);

        if(DeviceManager.getInstance().getCurrentDevice().did.apsHasFlag == 2){
            layout_progress_time.setVisibility(View.INVISIBLE);

            // ikHwang 2019-12-12 ?????? 7:43 ?????? ??? ??????????????? ?????? UI ?????? ??????
            if(3 == DeviceManager.getInstance().getCurrentDevice().sd.flagHepaFilter){
                usedTime = 100; //

                // ?????? ?????? ????????? ??????
                float height = (usedTime * FILTER_MAX_HEIGHT) / MAX_HEPA_FILTER_TIME;

                filterHeight = filterLife.getLayoutParams();
                filterHeight.height = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, Math.round(height), getResources().getDisplayMetrics());
                filterLife.setLayoutParams(filterHeight);

                layout_filter_life.setVisibility(View.GONE);
                text_filter_life.setText(R.string.activity_more_filter_manager_str_6);

                updateFilterChangeUI(false);
            }else{
                // ?????? ?????? ?????? ??????
                float height = (usedTime * FILTER_MAX_HEIGHT) / MAX_HEPA_FILTER_TIME;

                filterHeight = filterLife.getLayoutParams();
                filterHeight.height = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, Math.round(height), getResources().getDisplayMetrics());
                filterLife.setLayoutParams(filterHeight);

                // ?????????,????????? ??????
                filterLifeText.setText(String.valueOf((int)Math.ceil(usedTime)));

                layout_filter_life.setVisibility(View.VISIBLE);
                text_filter_life.setText(R.string.filter_life);

                if(usedTime < (MAX_HEPA_FILTER_TIME * 0.1)){ //????????? 10% ???????????? UI
                    updateFilterChangeUI(true);
                }else {
                    updateFilterChangeUI(false);
                }
            }
        }else{
            int pcd = CleanVentilationApplication.getInstance().getRoomControllerDevicePCD();

            if(pcd < 5 || (pcd == 5 && DeviceManager.getInstance().getCurrentDevice().common.deviceVer <= 11)){
                // APS ???????????? ???????????? ?????? ????????? ??????????????? ?????? ??????
                layout_progress_time.setVisibility(View.VISIBLE);

                if(isFilterResetRequest  && usedTime == 0){
                    CommonUtils.showToast(MoreFilterManagePrismActivity.this, getString(R.string.filter_time_initialize));
                }

                isFilterResetRequest = false;

                float calTime = usedTime;

                // ikHwang 2020-01-14 ?????? 9:03 ???????????? ???????????? ?????? ???????????? ????????? ???????????? ?????? ??????
                // ?????? ??????????????? ???????????? ?????? ????????? 4300?????? ??????
                if(0 < DeviceManager.getInstance().getCurrentDevice().did.maxFilterUseTime){
                    MAX_HEPA_FILTER_TIME = DeviceManager.getInstance().getCurrentDevice().did.maxFilterUseTime;
                }else{
                    MAX_HEPA_FILTER_TIME = 4300;
                }

                // ikHwang 2019-07-03 10:40 ???????????? ??????, ?????? ?????? ????????? ?????? MAX, MIN??? ??????
                if(usedTime > MAX_HEPA_FILTER_TIME) {
                    calTime = MAX_HEPA_FILTER_TIME;
                }else if(usedTime < 0){
                    calTime = 0;
                }

                if(calTime <= MAX_HEPA_FILTER_TIME){
                    float remainTime = MAX_HEPA_FILTER_TIME - calTime;
                    float height = (remainTime * FILTER_MAX_HEIGHT) / MAX_HEPA_FILTER_TIME;
                    float lifePercent = (remainTime * 100) / MAX_HEPA_FILTER_TIME;
                    filterHeight = filterLife.getLayoutParams();
                    filterHeight.height = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, Math.round(height), getResources().getDisplayMetrics());
                    filterLife.setLayoutParams(filterHeight);

                    //?????????,????????? ??????
                    filterUsedTimeText.setText(String.valueOf((int)usedTime));
                    filterRemainTimeText.setText(String.valueOf((int)remainTime));
                    filterLifeText.setText(String.valueOf((int)Math.ceil(lifePercent)));

                    if(remainTime < (MAX_HEPA_FILTER_TIME * 0.1)){ //????????? 10% ???????????? UI
                        updateFilterChangeUI(true);
                    }else {
                        updateFilterChangeUI(false);
                    }
                }
            }else{
                layout_progress_time.setVisibility(View.INVISIBLE);

                if(isFilterResetRequest  && usedTime >= 100){
                    CommonUtils.showToast(MoreFilterManagePrismActivity.this, getString(R.string.filter_time_initialize));
                }

                // ikHwang 2019-12-12 ?????? 7:43 ?????? ??? ??????????????? ?????? UI ?????? ??????
                if(3 == DeviceManager.getInstance().getCurrentDevice().sd.flagHepaFilter){
                    usedTime = 100; //

                    // ?????? ?????? ????????? ??????
                    float height = (usedTime * FILTER_MAX_HEIGHT) / MAX_HEPA_FILTER_TIME;

                    filterHeight = filterLife.getLayoutParams();
                    filterHeight.height = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, Math.round(height), getResources().getDisplayMetrics());
                    filterLife.setLayoutParams(filterHeight);

                    layout_filter_life.setVisibility(View.GONE);
                    text_filter_life.setText(R.string.activity_more_filter_manager_str_6);

                    updateFilterChangeUI(false);
                }else{
                    // ?????? ?????? ?????? ??????
                    float height = (usedTime * FILTER_MAX_HEIGHT) / MAX_HEPA_FILTER_TIME;

                    filterHeight = filterLife.getLayoutParams();
                    filterHeight.height = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, Math.round(height), getResources().getDisplayMetrics());
                    filterLife.setLayoutParams(filterHeight);

                    // ?????????,????????? ??????
                    filterLifeText.setText(String.valueOf((int)Math.ceil(usedTime)));

                    layout_filter_life.setVisibility(View.VISIBLE);
                    text_filter_life.setText(R.string.filter_life);

                    if(usedTime < (MAX_HEPA_FILTER_TIME * 0.1)){ //????????? 10% ???????????? UI
                        updateFilterChangeUI(true);
                    }else {
                        updateFilterChangeUI(false);
                    }
                }
            }
        }

        // ?????????????????? ?????? ????????????
        if(5 == DeviceManager.getInstance().getCurrentDevice().sd.flagHepaFilter
                || 3 == DeviceManager.getInstance().getCurrentDevice().sd.flagHepaFilter){
            filterResetBtn.setBackgroundResource(R.drawable.bg_btn_cancel_900);
        }else{
            filterResetBtn.setBackgroundResource(R.drawable.btn_confirm);
        }

        CommonUtils.customLog(TAG, "FilterUseTime : " + usedTime + ", FilterSettingTIme : " + MAX_HEPA_FILTER_TIME, Log.ERROR);
    }

    //?????? ?????? UI ??????
    private void updateFilterChangeUI(boolean isChange){
        if(isChange){
            filterChangeDot.setVisibility(View.VISIBLE);
            filterChangeText.setVisibility(View.VISIBLE);
        }else {
            filterChangeDot.setVisibility(View.INVISIBLE);
            filterChangeText.setVisibility(View.INVISIBLE);
        }
    }
}