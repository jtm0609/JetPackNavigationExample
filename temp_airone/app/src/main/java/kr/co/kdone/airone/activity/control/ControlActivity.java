package kr.co.kdone.airone.activity.control;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.StrictMode;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.google.android.material.tabs.TabLayout;
import com.google.firebase.crashlytics.FirebaseCrashlytics;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import kr.co.kdone.airone.CleanVentilationApplication;
import kr.co.kdone.airone.R;
import kr.co.kdone.airone.activity.PopupActivity;
import kr.co.kdone.airone.activity.info.RoomControlerDetailActivity;
import kr.co.kdone.airone.activity.more.MoreActivity;
import kr.co.kdone.airone.activity.more.MoreHelpActivity;
import kr.co.kdone.airone.aws.AWSIoTMqttHelper;
import kr.co.kdone.airone.aws.DataChangeListener;
import kr.co.kdone.airone.aws.PubSubData;
import kr.co.kdone.airone.components.KDNumberPicker;
import kr.co.kdone.airone.components.MainMenu;
import kr.co.kdone.airone.data.DeviceManager;
import kr.co.kdone.airone.data.DidPacket;
import kr.co.kdone.airone.utils.CommonUtils;
import kr.co.kdone.airone.utils.HomeInfoDataParser;
import kr.co.kdone.airone.utils.HttpApi;
import kr.co.kdone.airone.widget.WidgetUtils;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

import static kr.co.kdone.airone.aws.AWSIoTMqttHelper.MQTT_CONNECTION_TIMEOUT;
import static kr.co.kdone.airone.aws.AWSIoTMqttHelper.MQTT_CONNECT_ERROR;
import static kr.co.kdone.airone.utils.CommonUtils.CONTROL_AUTO;
import static kr.co.kdone.airone.utils.CommonUtils.CONTROL_CLEAN;
import static kr.co.kdone.airone.utils.CommonUtils.CONTROL_DEHUMIDIFICATIOIN;
import static kr.co.kdone.airone.utils.CommonUtils.CONTROL_VENTILATION;
import static kr.co.kdone.airone.utils.CommonUtils.DismissConnectDialog;
import static kr.co.kdone.airone.utils.CommonUtils.IsRunningProgress;
import static kr.co.kdone.airone.utils.CommonUtils.MODE_DEVICE_ROOM_CON;
import static kr.co.kdone.airone.utils.CommonUtils.PROGRESSBAR_1STEP;
import static kr.co.kdone.airone.utils.CommonUtils.PROGRESSBAR_HUMI_MAX;
import static kr.co.kdone.airone.utils.CommonUtils.PROGRESSBAR_HUMI_MIN;
import static kr.co.kdone.airone.utils.CommonUtils.RESULT_CANT_CONTROL;
import static kr.co.kdone.airone.utils.CommonUtils.RESULT_DETAIL_CHECK;
import static kr.co.kdone.airone.utils.CommonUtils.RESULT_ERROR_CONNECTION_WITH_SERVER;
import static kr.co.kdone.airone.utils.CommonUtils.RESULT_ERROR_CONNECT_TIMEOUT;
import static kr.co.kdone.airone.utils.CommonUtils.RESULT_ERROR_FIRTST_SENDMASSAGE;
import static kr.co.kdone.airone.utils.CommonUtils.initiateTwoButtonAlert;
import static kr.co.kdone.airone.utils.CommonUtils.isUserInfoSection;
import static kr.co.kdone.airone.utils.CommonUtils.startActivityAni;
import static kr.co.kdone.airone.utils.ProtocolType.COMMAND_DID;
import static kr.co.kdone.airone.utils.ProtocolType.COMMAND_SD;
import static kr.co.kdone.airone.utils.ProtocolType.MODE_OPTION_BYPASS;
import static kr.co.kdone.airone.utils.ProtocolType.MODE_OPTION_NOTING;
import static kr.co.kdone.airone.utils.ProtocolType.MODE_OPTION_PWR_SAVING;
import static kr.co.kdone.airone.utils.ProtocolType.MODE_OPTION_SLEEP;
import static kr.co.kdone.airone.utils.ProtocolType.MODE_OPTION_TURBO;
import static kr.co.kdone.airone.utils.ProtocolType.MODE_USED;
import static kr.co.kdone.airone.utils.ProtocolType.OPERATION_MODE_AIR_CLEAN;
import static kr.co.kdone.airone.utils.ProtocolType.OPERATION_MODE_AUTO;
import static kr.co.kdone.airone.utils.ProtocolType.OPERATION_MODE_BYPASS;
import static kr.co.kdone.airone.utils.ProtocolType.OPERATION_MODE_CLEAN;
import static kr.co.kdone.airone.utils.ProtocolType.OPERATION_MODE_DEHUMI;
import static kr.co.kdone.airone.utils.ProtocolType.OPERATION_MODE_DUST;
import static kr.co.kdone.airone.utils.ProtocolType.OPERATION_MODE_EXHAUST;
import static kr.co.kdone.airone.utils.ProtocolType.OPERATION_MODE_IDLE;
import static kr.co.kdone.airone.utils.ProtocolType.OPERATION_MODE_KITCHEN;
import static kr.co.kdone.airone.utils.ProtocolType.OPERATION_MODE_VENTI;
import static kr.co.kdone.airone.utils.ProtocolType.OPERATION_STATUS_OFF;
import static kr.co.kdone.airone.utils.ProtocolType.OPERATION_STATUS_ON;
import static kr.co.kdone.airone.utils.ProtocolType.PROTOCOL_VER_ID1;
import static kr.co.kdone.airone.utils.ProtocolType.SCHEDULE_TYPE_NO;
import static kr.co.kdone.airone.utils.ProtocolType.SCHEDULE_TYPE_OFF;
import static kr.co.kdone.airone.utils.ProtocolType.SCHEDULE_TYPE_ON;
import static kr.co.kdone.airone.utils.ProtocolType.SCHEDULE_TYPE_SLEEP;
import static kr.co.kdone.airone.utils.ProtocolType.USE;
import static kr.co.kdone.airone.utils.ProtocolType.USE_NOT;
import static kr.co.kdone.airone.utils.ProtocolType.WIND_LEVEL_1;
import static kr.co.kdone.airone.utils.ProtocolType.WIND_LEVEL_2;
import static kr.co.kdone.airone.utils.ProtocolType.WIND_LEVEL_3;
import static kr.co.kdone.airone.utils.ProtocolType.WIND_LEVEL_AUTO;

/**
 * // ikHwang 2019-06-04 ?????? 8:39 ????????????
 */
public class ControlActivity extends AppCompatActivity implements View.OnClickListener, DataChangeListener {
    private final String TAG = getClass().getSimpleName();

    private MainMenu mMainMenu;

    private int mImgModes1_Normal[];
    private int mImgModes1_Select[];
    private int mImgModes1_Normal_disable[];
    private int mImgModes1_Select_disable[];
    private String mTxtModes1[];
    private int mCtrlMode1[];
    private int mCtrlModeOption1[];

    private int mImgModes2_Normal[];
    private int mImgModes2_Select[];
    private int mImgModes2_Normal_disable[];
    private int mImgModes2_Select_disable[];
    private String mTxtModes2[];
    private int mCtrlMode2[];
    private int mCtrlModeOption2[];

    // ikHwang 2020-02-28 ?????? 10:16 ?????? ?????? ??????
    private int mImgModes3_Normal[];
    private int mImgModes3_Select[];
    private int mImgModes3_Normal_disable[];
    private int mImgModes3_Select_disable[];
    private String mTxtModes3[];
    private int mCtrlMode3[];
    private int mCtrlModeOption3[];

    private int mImgModes4_Normal[];
    private int mImgModes4_Select[];
    private int mImgModes4_Normal_disable[];
    private int mImgModes4_Select_disable[];
    private String mTxtModes4[];
    private int mCtrlMode4[];
    private int mCtrlModeOption4[];

    private int mImgWindLevel_Normal[];
    private int mImgWindLevel_Select[];
    private int mImgWindLevel_Normal_disable[];
    private int mImgWindLevel_Select_disable[];
    private int mCtrlWindLevel[];

    // ikHwang 2020-02-28 ?????? 10:18  ?????? ???????????? ??? ?????? ??????
    private LinearLayout layout_dehumi;
    private FrameLayout layout_dehumi_line;

    private LinearLayout layoutPower;
    private ImageView mImgPower;
    private TextView mTxtPower;
    private LinearLayout[] mLayoutModes;
    private ImageView[] mImgModes;
    private TextView[] mTxtModes;
    private ImageView[] mImgWindLevels;

    private LinearLayout mLayoutCurrentHumi;
    private TextView mTxtCurrentHumi;
    private TextView mTxtSettingHumi;
    private TextView mTxtReservation;
    private TextView mTxtDeepSleep;
    private ImageView mImgPercent;
    private ImageView mImgMinus;
    private ImageView mImgPlus;

    private SeekBar mSeekBarSettingHumi;

    private LinearLayout mLayoutReservation;
    private LinearLayout mLayoutDeepSleep;
    
    private LinearLayout    layoutUV;       // UV ?????? ????????????
    private ToggleButton    toggle_status;

//    private String mDeviceID = "";
    private Timer mCanNotControlTimer;
    private long CONTROL_DELAYTIME = 10 * 1000;
    private long SOCKET_CLOSE_DELAYTIME = 10 * 60 * 1000;

    private boolean mIsSendFirstMessage = false;
    private boolean mIsShowErrorPopup;
    private int mSelectedMainMode;
    private int mPrevMainMode;

    private boolean mIsPower = false;
    private int mSelectedMode = OPERATION_MODE_IDLE;
    private int mNextdMode = OPERATION_MODE_IDLE;
    private int mSelectedModeOption = MODE_OPTION_NOTING;
    private int mSelectedWindLevel = WIND_LEVEL_AUTO;
    private int mCurruntHumi = 0;
    private int mSettingHumi = 0;
    private int mSettingReservationTime = 0;
    private int mRemaintReservationTime = 0;
    private int mSettingReservationType = SCHEDULE_TYPE_NO;
    private int mRoomZone = 0;
    private int mSleep = USE_NOT; //??????????????????. ?????? ?????? ??? ????????? ??????x
    private int mSleepTimeStart = 0;
    private int mSleepTimeEnd = 0;
    private int mUVLedSMode = 1; // UV LED ?????? ??????

    private int mMode_use = USE_NOT;
    private int mHumidity_use = USE_NOT;
    private int mMode_option_use = USE_NOT;
    private int mRun_use = USE_NOT;
    private int mWind_use = USE_NOT;
    private int mRoom_use = USE_NOT;
    private int mSchedule_time_use = USE_NOT;
    private int mSchedule_type_use = USE_NOT;
    private int mSleep_use = USE_NOT; //????????? ???????????? ????????? ?????? ??????. ?????? ?????? ??? ????????? ??????o

    private ConstraintLayout mLayoutTimeSet;
    private KDNumberPicker mTimeSetAmPmPicker;
    private KDNumberPicker mTimeSetHourPicker;
    private KDNumberPicker mTimeSetMinutePicker;
    private ToggleButton mTimeSetBtnOnOff;
    private int mSetTimeType;

    private TabLayout mTabLayout;
    private boolean mIsChangeCurrentMainMode;

    private AlertDialog mDialog;

    public static CallbackHandler callback;
    public static final int MSG_DEEP_SLEEP = 1;
    public static final int MSG_DEEP_SLEEP_COMPLETE = 2;
    public static final int MSG_DEEP_SLEEP_CANCEL = 3;
    public static final int MSG_ERROR = 10;

    private LinearLayout powerOffLayout;
    private LinearLayout powerOnLayout;

    private boolean isActivityForeground = false;

    private ArrayList<String> tabName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_control);

        callback = new CallbackHandler();
        isUserInfoSection(this);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        mMainMenu = findViewById(R.id.layoutMenus);

        initComponents();
        initTimeSetComponents();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        callback = null;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(CleanVentilationApplication.isAnyType()) overridePendingTransition(R.anim.slide_none, 0);
        CleanVentilationApplication.setAnyType(false);

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
                // ?????? ?????? ???????????? ??? UI ?????? ??????
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        updateTabUI();
                    }
                });

                // ikHwang 2020-11-03 ?????? 8:41 ?????? ?????? ????????? DID ?????? ??? ???????????? ?????? ???????????? ????????? ?????? ??????SD??? ??????????????? ??????
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case RESULT_CANT_CONTROL:
            case RESULT_ERROR_CONNECTION_WITH_SERVER:
                if(1 == DeviceManager.getInstance().getCurrentDevice().sd.errorLevel_L){
                    // ?????? ????????? ????????? ????????? ???????????? UI ?????? ??????
/*                    updateAllComponents();
                    if (IsRunningProgress()) {
                        CommonUtils.DismissConnectDialog();
                    }*/
                    return;
                }

                CleanVentilationApplication.getInstance().awsMqttDisConnect();
                mMainMenu.gotoOldActivity();
                finish();
                break;
                
            case RESULT_ERROR_CONNECT_TIMEOUT: // ?????? ?????? 10??? ????????????
                CleanVentilationApplication.getInstance().awsMqttDisConnect();
                mMainMenu.gotoOldActivity();
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
                    mMainMenu.gotoOldActivity();
                    finish();
                }
                break;

            case RESULT_DETAIL_CHECK:
                mIsShowErrorPopup = false;
                break;
        }
    }

    /**
     * ?????? ????????? ????????? ????????? ??????
     */
    public void sendToServer(int type) {
        mIsChangeCurrentMainMode = true;
        setCanNotControlTimer();

        CommonUtils.displayProgress(this, "", "");

        String sessionToken = CleanVentilationApplication.authData.sessionToken;
        String modelCode = CleanVentilationApplication.getInstance().getRoomControllerDeviceModelCode();
        String deviceId = CleanVentilationApplication.getInstance().getRoomControllerDeviceID();

        String topic = "";
        String requestData = "";

        mIsSendFirstMessage = true;

        switch (type){
            case 0 : // ???????????? ??????
                topic = PubSubData.getRemoteAppStopPubTopic(modelCode, deviceId);
                requestData = PubSubData.getAppStopPubData(sessionToken, modelCode, deviceId);
                break;

            case 1 : // DID (device info data) = ?????? ???????????????????, ?????????????????? ????????? ?????? ????????? ??????
                topic = PubSubData.getDidPubTopic(modelCode, deviceId);
                requestData = PubSubData.getDidPubData(sessionToken, modelCode, deviceId);
                break;

            case 2 : // SD (status data) = ?????? ??????????????? ?????? ?????????????, ?????? ????????? ????????????????
                topic = PubSubData.getSdPubTopic(modelCode, deviceId);
                requestData = PubSubData.getSdPubData(sessionToken, modelCode, deviceId);
                break;

            case 3 : // ?????? On/Off
                topic = PubSubData.getRemotePowerPubTopic(modelCode, deviceId);
                requestData = PubSubData.getPowerPubData(sessionToken, modelCode, deviceId, (mIsPower ? OPERATION_STATUS_ON : OPERATION_STATUS_OFF));
                break;

            case 4 : // ?????? ?????? ??? ???????????? ??????
                // ?????? ?????? ????????? ???????????? 0?????? ???????????? ?????? ?????? (sp3??? ??????)
                if(mWind_use == USE_NOT) {
                    mSelectedWindLevel = 0;
                }

                CommonUtils.customLog(TAG, "&&&& Mode : " + mSelectedMode + ", option : " + mSelectedModeOption + ", wind : " + mSelectedWindLevel, Log.ERROR);
                if (mSelectedMode == OPERATION_MODE_BYPASS) {
                    mSelectedModeOption = MODE_OPTION_NOTING;
                    mSelectedWindLevel = WIND_LEVEL_AUTO;
                }
                topic = PubSubData.getRemoteChangeModePubTopic(modelCode, deviceId);
                requestData = PubSubData.getChangeModePubData(sessionToken, modelCode, deviceId, mSelectedMode, mSelectedModeOption, mSelectedWindLevel);
                break;

            case 5 : // ??????, ?????? ?????? ??????
                topic = PubSubData.getRemoteSchedulePubTopic(modelCode, deviceId);
                requestData = PubSubData.getSchedulePubData(sessionToken, modelCode, deviceId, mSettingReservationType, mSettingReservationTime);
                break;

            case 6 : // ????????????
                topic = PubSubData.getRemoteSleepPubTopic(modelCode, deviceId);
                requestData = PubSubData.getSleepPubData(sessionToken, modelCode, deviceId, mSleep, mSleepTimeStart, mSleepTimeEnd);
                break;

            case 7 : // ???????????? ??????
                topic = PubSubData.getRemoteHumidityPubTopic(modelCode, deviceId);
                requestData = PubSubData.getHumidityPubData(sessionToken, modelCode, deviceId, mSettingHumi);
                break;

            case 8 : // UV LED ??????
                topic = PubSubData.getRemoteUVLedPubTopic(modelCode, deviceId);
                requestData = PubSubData.getUVLedPubData(sessionToken, modelCode, deviceId, mUVLedSMode);
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
     * @param cmd
     */
    @Override
    public void onDataChanged(int cmd) {
        // ?????? ????????? ?????? ??????
        mIsSendFirstMessage = false;

        if (cmd == COMMAND_DID) {
            CommonUtils.customLog(TAG, "onDataChanged() : COMMAND_DID", Log.ERROR);

            if (mCanNotControlTimer != null) mCanNotControlTimer.cancel();

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    // ikHwang 2020-03-30 ?????? 10:03 DID ?????? ???????????? min/max ?????? ?????? ??????????????? ??????
                    try {
                        PROGRESSBAR_HUMI_MIN = DeviceManager.getInstance().getCurrentDevice().did.dehumiSetHumi.min/2;
                        PROGRESSBAR_HUMI_MAX = DeviceManager.getInstance().getCurrentDevice().did.dehumiSetHumi.max/2;
                        mSeekBarSettingHumi.setMax((PROGRESSBAR_HUMI_MAX - PROGRESSBAR_HUMI_MIN) / PROGRESSBAR_1STEP);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    updateTabUI();
                }
            });

            // DID ?????? ??? SD ?????? ??????
            sendToServer(2);

            mIsChangeCurrentMainMode = true;
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
                Intent intent = new Intent(ControlActivity.this, PopupActivity.class);
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
                    updateAllComponents();
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
                sendToServer(0); // ???????????? ??????
                errorToConnectWithServer(true);

                CommonUtils.customLog(TAG, "&&&& onError", Log.ERROR);
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
                CommonUtils.customLog(TAG, "&&&& onStatusChanged", Log.ERROR);

                if(!CleanVentilationApplication.getInstance().isAppFinishStart()){
                    // ?????? ?????? ??????
                    errorToConnectWithServer(true);
                }

                break;
        }
    }

    /**
     * ikHwang 2020-03-26 ?????? 4:20 ?????? ?????? ??? UI ????????????
     */
    private void updateTabUI(){

        try {
            tabName.clear();
            mTabLayout.removeAllTabs();
            mTabLayout.setOnTabSelectedListener(null);

            // ?????? ??????
            mTabLayout.addTab(mTabLayout.newTab().setText(getString(R.string.ventilation)));
            tabName.add(getString(R.string.ventilation));

            // ????????? ????????? ??????????????? ???????????? ?????? ??????????????? ??????
            if(DeviceManager.getInstance().getCurrentDevice().did.airCleanMode.modeUse == 2){
                mTabLayout.addTab(mTabLayout.newTab().setText(getString(R.string.air_clean)));
                tabName.add(getString(R.string.air_clean));
            }

            // ikHwang 2020-02-28 ?????? 10:19  ?????? PCD ????????? ???????????? ?????? ?????????
            if(DeviceManager.getInstance().getCurrentDevice().did.dehumiMode.modeUse == 2){
                mTabLayout.addTab(mTabLayout.newTab().setText(getString(R.string.dehumidification)));
                tabName.add(getString(R.string.dehumidification));
            }

            // ikHwang 2020-11-02 ?????? 4:31 ?????? ?????? ??????
            if(DeviceManager.getInstance().getCurrentDevice().did.autoMode.modeUse == 2){
                mTabLayout.addTab(mTabLayout.newTab().setText(getString(R.string.auto)));
                tabName.add(getString(R.string.auto));
            }

            mTabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
                @Override
                public void onTabSelected(TabLayout.Tab tab) {
                    if(getString(R.string.air_clean).equals(tabName.get(tab.getPosition()))){
                        // ????????????
                        updateModeComponents(CONTROL_CLEAN, mSelectedModeOption);
                    }else if(getString(R.string.dehumidification).equals(tabName.get(tab.getPosition()))){
                        // ????????????
                        updateModeComponents(CONTROL_DEHUMIDIFICATIOIN, mSelectedModeOption);
                    }else if(getString(R.string.auto).equals(tabName.get(tab.getPosition()))){
                        // ??????
                        updateModeComponents(CONTROL_AUTO, mSelectedModeOption);
                    }else{
                        // ????????????
                        updateModeComponents(CONTROL_VENTILATION, mSelectedModeOption);
                    }
                }

                @Override
                public void onTabUnselected(TabLayout.Tab tab) {

                }

                @Override
                public void onTabReselected(TabLayout.Tab tab) {

                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }

        // UV LED ?????? ???????????? ?????? ?????? ??????
        try {
            layoutUV.setVisibility(DeviceManager.getInstance().getCurrentDevice().did.uvLED == 2 ? View.VISIBLE : View.GONE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * ???????????? ????????? ?????? ????????? ?????? ?????????
     */
    private void resetUsedFuncContorl() {
        mMode_use = USE_NOT;
        mHumidity_use = USE_NOT;
        mMode_option_use = USE_NOT;
        mRun_use = USE_NOT;
        mWind_use = USE_NOT;
        mRoom_use = USE_NOT;
        mSchedule_time_use = USE_NOT;
        mSchedule_type_use = USE_NOT;
        mSleep_use = USE_NOT;
    }

    private void sendSleepTimeSet(){ //???????????? ?????? ????????? ?????? setting ?????????
        mMode_use = USE;
        mMode_option_use = USE;
        mSelectedMode = mNextdMode;
        mSelectedModeOption = MODE_OPTION_SLEEP;

        sendToServer(4);
    }
    //****************************************************

    private void usedForModeChange() {
        mMode_use = USE;
        mMode_option_use = USE;

        if (mSettingReservationType == SCHEDULE_TYPE_NO) {
            mSchedule_type_use = USE_NOT;
            mSchedule_time_use = USE_NOT;
        } else {
            mSchedule_type_use = USE;
            mSchedule_time_use = USE;
        }
    }

    /**
     * ??????/?????? ?????? ??????
     */
    private void saveAndSendTimeSet() {
        int ap = 0, hour = 0, min = 0;
        if (mTimeSetBtnOnOff.isChecked()) {
            Calendar cal = Calendar.getInstance();
            ap = mTimeSetAmPmPicker.getValue();
            hour = (mTimeSetHourPicker.getValue() + 1) % 12;
            min = mTimeSetMinutePicker.getValue() * 10;

            mSettingReservationType = mSetTimeType;
            mSettingReservationTime = (ap * 12 + hour) * 60 + min;
        } else {
            mSettingReservationType = SCHEDULE_TYPE_NO;
            mSettingReservationTime = 0;
        }

        String txtType = "";
        String txtTime = String.format(getString(R.string.reservation_string), ap == Calendar.AM ? "??????" : "??????", hour == 0 ? 12 : hour, min);
        if (SCHEDULE_TYPE_NO == mSettingReservationType) {
            txtType = "";
            txtTime = "";
        } else if (SCHEDULE_TYPE_SLEEP == mSettingReservationType) {
            txtType = getString(R.string.reservation_sleep) + " ";
        } else if (SCHEDULE_TYPE_ON == mSettingReservationType) {
            txtType = getString(R.string.reservation_on) + " ";
        } else if (SCHEDULE_TYPE_OFF == mSettingReservationType) {
            txtType = getString(R.string.reservation_off) + " ";
        } else {
            txtType = getString(R.string.reservation_no_type) + " ";
        }
        
        mTxtReservation.setText(txtType + txtTime);

        mSchedule_time_use = USE;
        mSchedule_type_use = USE;

        sendToServer(5);
    }

    /**
     * ?????? ?????? ?????? ??????
     * @param type
     */
    private void showTimeSet(int type) {
        /**
         * ?????? OFF ???????????? ???????????? ????????? ?????? ????????????
         */
        if (!mIsPower && type == SCHEDULE_TYPE_SLEEP) {
            if (IsRunningProgress()) CommonUtils.DismissConnectDialog();
            mIsShowErrorPopup = true;
            Intent intent = new Intent(ControlActivity.this, PopupActivity.class);
            intent.putExtra(PopupActivity.POPUP_MODE, PopupActivity.MODE_NO_BUTTON);
            intent.putExtra(PopupActivity.POPUP_TIMEOUT, 3000);
            intent.putExtra(PopupActivity.POPUP_BODY_TEXT, getString(R.string.error_reservation_sleep_mode_until_poweroff));
            startActivity(intent);
            overridePendingTransition(0, 0);
        } else {
            mSetTimeType = type;
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.MINUTE, 10);

            mTimeSetAmPmPicker.setValue(cal.get(Calendar.AM_PM));
            mTimeSetHourPicker.setValue((cal.get(Calendar.HOUR) + 11) % 12);
            mTimeSetMinutePicker.setValue(cal.get(Calendar.MINUTE) / 10);

            if (type == SCHEDULE_TYPE_OFF) {
                ((TextView) findViewById(R.id.txtTimeSetTitle)).setText(getString(R.string.reservation_off));
            } else if (type == SCHEDULE_TYPE_ON) {
                ((TextView) findViewById(R.id.txtTimeSetTitle)).setText(getString(R.string.reservation_on));
            } else {
                ((TextView) findViewById(R.id.txtTimeSetTitle)).setText(getString(R.string.reservation_off));
            }

            mTimeSetBtnOnOff.setChecked(mSettingReservationType != SCHEDULE_TYPE_NO);
            mTimeSetAmPmPicker.setEnabled(mTimeSetBtnOnOff.isChecked());
            mTimeSetHourPicker.setEnabled(mTimeSetBtnOnOff.isChecked());
            mTimeSetMinutePicker.setEnabled(mTimeSetBtnOnOff.isChecked());
            mTimeSetBtnOnOff.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    mTimeSetAmPmPicker.setEnabled(isChecked);
                    mTimeSetHourPicker.setEnabled(isChecked);
                    mTimeSetMinutePicker.setEnabled(isChecked);
                }
            });

            Animation anim = AnimationUtils.loadAnimation
                    (getApplicationContext(), // ???????????? ????????????
                            R.anim.alpha_0_to_100);      // ??????????????? ????????? ??????
            ((FrameLayout) findViewById(R.id.layoutBackground)).startAnimation(anim);
            ((FrameLayout) findViewById(R.id.layoutBackground)).setAlpha(1f);

            mLayoutTimeSet.setVisibility(View.VISIBLE);
        }
    }

    /**
     * ?????? ?????? ?????? ??????
     */
    private void hideTimeSet() {
        Animation anim = AnimationUtils.loadAnimation
                (getApplicationContext(), // ???????????? ????????????
                        R.anim.alpha_100_to_0);      // ??????????????? ????????? ??????
        ((FrameLayout) findViewById(R.id.layoutBackground)).startAnimation(anim);
        ((FrameLayout) findViewById(R.id.layoutBackground)).setAlpha(0f);
        mLayoutTimeSet.setVisibility(View.INVISIBLE);
    }

    /**
     * ?????? ?????? ???????????? ????????? ??????
     */
    private void initTimeSetComponents() {
        mSetTimeType = SCHEDULE_TYPE_NO;

        mLayoutTimeSet = findViewById(R.id.layoutTimeSet);
        mLayoutTimeSet.setVisibility(View.INVISIBLE);
        mTimeSetBtnOnOff = findViewById(R.id.btnOnOff);

        /**
         * ????????? AM/PM number picker ??????.
         */
        mTimeSetAmPmPicker = findViewById(R.id.amPmPicker);
        String[] listAmPmString = {"??????", "??????"};
        for (int count = 0; count < listAmPmString.length; count++) {
            EditText txtItem = new EditText(this);
            txtItem.setTypeface(Typeface.create("sans-serif-light", Typeface.NORMAL));
            txtItem.setTextColor(Color.BLACK);
            txtItem.setText(listAmPmString[count]);
            txtItem.setClickable(false);
            mTimeSetAmPmPicker.addView(txtItem);
        }
        mTimeSetAmPmPicker.setDisplayedValues(listAmPmString);
        mTimeSetAmPmPicker.setMinValue(0);
        mTimeSetAmPmPicker.setMaxValue(1);
        mTimeSetAmPmPicker.setWrapSelectorWheel(false);

        /**
         * ????????? ?????? number picker ??????.
         */
        mTimeSetHourPicker = findViewById(R.id.hourPicker);
        String[] listHourString = new String[12];
        for (int count = 0; count < listHourString.length; count++) {
            listHourString[count] = String.format("%d", count + 1);
            EditText txtItem = new EditText(this);
            txtItem.setTypeface(Typeface.create("sans-serif-light", Typeface.NORMAL));
            txtItem.setTextColor(Color.BLACK);
            txtItem.setText(String.format("%d", count + 1));
            txtItem.setClickable(false);
            mTimeSetHourPicker.addView(txtItem);
        }
        mTimeSetHourPicker.setDisplayedValues(listHourString);
        mTimeSetHourPicker.setMinValue(0);
        mTimeSetHourPicker.setMaxValue(listHourString.length - 1);
        mTimeSetHourPicker.setWrapSelectorWheel(true);

        /**
         * ????????? ??? number picker ??????.
         */
        mTimeSetMinutePicker = findViewById(R.id.minutePicker);
        String[] listMiniteString = new String[6];
        for (int count = 0; count < listMiniteString.length; count++) {
            EditText txtItem = new EditText(this);
            txtItem.setTypeface(Typeface.create("sans-serif-light", Typeface.NORMAL));
            txtItem.setTextColor(Color.BLACK);
            listMiniteString[count] = String.format("%02d", count * 10);
            txtItem.setText(String.format("%02d", count * 10));
            txtItem.setClickable(false);
            mTimeSetMinutePicker.addView(txtItem);
        }
        mTimeSetMinutePicker.setDisplayedValues(listMiniteString);
        mTimeSetMinutePicker.setMinValue(0);
        mTimeSetMinutePicker.setMaxValue(listMiniteString.length - 1);
        mTimeSetMinutePicker.setWrapSelectorWheel(true);
    }

    /**
     * ?????? ???????????? ????????? ??? ????????? ??????.
     */
    private void initComponents() {
        mIsShowErrorPopup = false;
        mIsChangeCurrentMainMode = true;

        tabName = new ArrayList<>();

        mTabLayout = findViewById(R.id.tabs);
        mTabLayout.addTab(mTabLayout.newTab().setText(getString(R.string.ventilation)));
        tabName.add(getString(R.string.ventilation));

        int pcd = CleanVentilationApplication.getInstance().getRoomControllerDevicePCD();

        // ????????? ????????? ??????????????? ???????????? ?????? ??????????????? ??????
        if(9 != pcd) {
            mTabLayout.addTab(mTabLayout.newTab().setText(getString(R.string.air_clean)));
            tabName.add(getString(R.string.air_clean));
        }

        // ikHwang 2020-02-28 ?????? 10:19  ?????? PCD ????????? ???????????? ?????? ?????????
        if(7 == pcd) {
            mTabLayout.addTab(mTabLayout.newTab().setText(getString(R.string.dehumidification)));
        }

        mTabLayout.addTab(mTabLayout.newTab().setText(getString(R.string.auto)));

        mTabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
//                updateModeComponents(tab.getPosition(), mSelectedModeOption);

                try {
                    if(getString(R.string.air_clean).equals(tabName.get(tab.getPosition()))){
                        // ????????????
                        updateModeComponents(CONTROL_CLEAN, mSelectedModeOption);
                    }else if(getString(R.string.dehumidification).equals(tabName.get(tab.getPosition()))){
                        // ????????????
                        updateModeComponents(CONTROL_DEHUMIDIFICATIOIN, mSelectedModeOption);
                    }else if(getString(R.string.auto).equals(tabName.get(tab.getPosition()))){
                        // ??????
                        updateModeComponents(CONTROL_AUTO, mSelectedModeOption);
                    }else{
                        // ????????????
                        updateModeComponents(CONTROL_VENTILATION, mSelectedModeOption);
                    }

                    // ikHwang 2020-03-26 ?????? 4:47 ?????? ??? ??????
                    if(DeviceManager.getInstance().getCurrentDevice().did.dehumiMode.modeUse == 2){
                        updateModeComponents(tab.getPosition(), mSelectedModeOption);
                    }else{
                        if(2 == tab.getPosition()){
                            updateModeComponents(3, mSelectedModeOption);
                        }else{
                            updateModeComponents(tab.getPosition(), mSelectedModeOption);
                        }
                    }

                    if(DeviceManager.getInstance().getCurrentDevice().did.dehumiMode.modeUse == 2){
                        if(9 != CleanVentilationApplication.getInstance().getRoomControllerDevicePCD()) {
                            if(1 == tab.getPosition()){
                                updateModeComponents(3, mSelectedModeOption);
                            }else{
                                updateModeComponents(tab.getPosition(), mSelectedModeOption);
                            }
                        }else{
                            updateModeComponents(tab.getPosition(), mSelectedModeOption);
                        }
                    }else{
                        if(2 == tab.getPosition()){
                            updateModeComponents(3, mSelectedModeOption);
                        }else{
                            updateModeComponents(tab.getPosition(), mSelectedModeOption);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    FirebaseCrashlytics.getInstance().log("ControlActivity onTabSelected : " + e.getLocalizedMessage());
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });


        mImgModes1_Normal = new int[]{
                R.drawable.icon_control_1_1,
                R.drawable.icon_control_1_2,
                R.drawable.icon_control_1_3,
                R.drawable.icon_control_1_4,
                R.drawable.icon_control_common_turbo,
                R.drawable.icon_control_common_eco,
                R.drawable.icon_control_common_sleep,
                R.drawable.icon_control_common_pass};

        mImgModes1_Select = new int[]{
                R.drawable.icon_control_1_1_on,
                R.drawable.icon_control_1_2_on,
                R.drawable.icon_control_1_3_on,
                R.drawable.icon_control_1_4_on,
                R.drawable.icon_control_common_turbo_on,
                R.drawable.icon_control_common_eco_on,
                R.drawable.icon_control_common_sleep_on,
                R.drawable.icon_control_common_pass_on};

        mImgModes1_Normal_disable = new int[]{
                R.drawable.icon_control_1_1_disable,
                R.drawable.icon_control_1_2_disable,
                R.drawable.icon_control_1_3_disable,
                R.drawable.icon_control_1_4_disable,
                R.drawable.icon_control_common_turbo_disable,
                R.drawable.icon_control_common_eco_disable,
                R.drawable.icon_control_common_sleep_disable,
                R.drawable.icon_control_common_pass};

        mImgModes1_Select_disable = new int[]{
                R.drawable.icon_control_1_1_on_disable,
                R.drawable.icon_control_1_2_on_disable,
                R.drawable.icon_control_1_3_on_disable,
                R.drawable.icon_control_1_4_on_disable,
                R.drawable.icon_control_common_turbo_on_disable,
                R.drawable.icon_control_common_eco_on_disable,
                R.drawable.icon_control_common_sleep_on_disable,
                R.drawable.icon_control_common_pass};

        mTxtModes1 = new String[]{
                getString(R.string.mode_vent),
                getString(R.string.mode_exhaust),
                getString(R.string.mode_clean),
                getString(R.string.mode_kitchen),
                getString(R.string.mode_turbo),
                getString(R.string.mode_pwr_saving),
                getString(R.string.mode_sleep),
                getString(R.string.bypass)};

        mCtrlMode1 = new int[]{
                OPERATION_MODE_VENTI,
                OPERATION_MODE_EXHAUST,
                OPERATION_MODE_CLEAN,
                OPERATION_MODE_KITCHEN,
                OPERATION_MODE_VENTI,
                OPERATION_MODE_VENTI,
                OPERATION_MODE_VENTI,
                OPERATION_MODE_BYPASS};

        mCtrlModeOption1 = new int[]{
                MODE_OPTION_NOTING,
                MODE_OPTION_NOTING,
                MODE_OPTION_NOTING,
                MODE_OPTION_NOTING,
                MODE_OPTION_TURBO,
                MODE_OPTION_PWR_SAVING,
                MODE_OPTION_SLEEP,
                MODE_OPTION_NOTING};

        mImgModes2_Normal = new int[]{R.drawable.icon_control_2_1, R.drawable.icon_control_2_2,
                R.drawable.icon_control_common_turbo, R.drawable.icon_control_common_sleep, R.drawable.icon_control_common_eco,};
        mImgModes2_Select = new int[]{R.drawable.icon_control_2_1_on, R.drawable.icon_control_2_2_on,
                R.drawable.icon_control_common_turbo_on, R.drawable.icon_control_common_sleep_on, R.drawable.icon_control_common_eco_on,};
        mImgModes2_Normal_disable = new int[]{R.drawable.icon_control_2_1_disable, R.drawable.icon_control_2_2_disable,
                R.drawable.icon_control_common_turbo_disable, R.drawable.icon_control_common_sleep_disable, R.drawable.icon_control_common_eco_disable,};
        mImgModes2_Select_disable = new int[]{R.drawable.icon_control_2_1_on_disable, R.drawable.icon_control_2_2_on_disable,
                R.drawable.icon_control_common_turbo_on_disable, R.drawable.icon_control_common_sleep_on_disable, R.drawable.icon_control_common_eco_on_disable,};
        mTxtModes2 = new String[]{getString(R.string.mode_air_clean), getString(R.string.mode_dust), getString(R.string.mode_turbo), getString(R.string.mode_sleep), getString(R.string.mode_pwr_saving),};
        mCtrlMode2 = new int[]{OPERATION_MODE_AIR_CLEAN, OPERATION_MODE_DUST, OPERATION_MODE_AIR_CLEAN, OPERATION_MODE_AIR_CLEAN, OPERATION_MODE_AIR_CLEAN};
        mCtrlModeOption2 = new int[]{MODE_OPTION_NOTING, MODE_OPTION_NOTING, MODE_OPTION_TURBO, MODE_OPTION_SLEEP, MODE_OPTION_PWR_SAVING};


        mImgModes3_Normal = new int[]{R.drawable.icon_control_3_1, R.drawable.icon_control_common_turbo,
                R.drawable.icon_control_common_sleep, R.drawable.icon_control_common_eco,};
        mImgModes3_Select = new int[]{R.drawable.icon_control_3_1_on, R.drawable.icon_control_common_turbo_on,
                R.drawable.icon_control_common_sleep_on, R.drawable.icon_control_common_eco_on,};
        mImgModes3_Normal_disable = new int[]{R.drawable.icon_control_3_1_disable, R.drawable.icon_control_common_turbo_disable,
                R.drawable.icon_control_common_sleep_disable, R.drawable.icon_control_common_eco_disable,};
        mImgModes3_Select_disable = new int[]{R.drawable.icon_control_3_1_on_disable, R.drawable.icon_control_common_turbo_on_disable,
                R.drawable.icon_control_common_sleep_on_disable, R.drawable.icon_control_common_eco_on_disable,};
        mTxtModes3 = new String[]{getString(R.string.mode_dehumi), getString(R.string.mode_turbo_dehumi), getString(R.string.mode_sleep), getString(R.string.mode_pwr_saving),};
        mCtrlMode3 = new int[]{OPERATION_MODE_DEHUMI, OPERATION_MODE_DEHUMI, OPERATION_MODE_DEHUMI, OPERATION_MODE_DEHUMI};
        mCtrlModeOption3 = new int[]{MODE_OPTION_NOTING, MODE_OPTION_TURBO, MODE_OPTION_SLEEP, MODE_OPTION_PWR_SAVING};


        mImgModes4_Normal = new int[]{R.drawable.icon_control_4_1,};
        mImgModes4_Select = new int[]{R.drawable.icon_control_4_1_on,};
        mImgModes4_Normal_disable = new int[]{R.drawable.icon_control_4_1_disable,};
        mImgModes4_Select_disable = new int[]{R.drawable.icon_control_4_1_on_disable,};
        mTxtModes4 = new String[]{getString(R.string.auto_control)};
        mCtrlMode4 = new int[]{OPERATION_MODE_AUTO};
        mCtrlModeOption4 = new int[]{MODE_OPTION_NOTING};


        mImgWindLevel_Normal = new int[]{R.drawable.icon_control_wind_auto, R.drawable.icon_control_wind01,
                R.drawable.icon_control_wind02, R.drawable.icon_control_wind03,};
        mImgWindLevel_Select = new int[]{R.drawable.icon_control_wind_auto_on, R.drawable.icon_control_wind01_on,
                R.drawable.icon_control_wind02_on, R.drawable.icon_control_wind03_on,};
        mImgWindLevel_Normal_disable = new int[]{R.drawable.icon_control_wind_auto_disable, R.drawable.icon_control_wind01_disable,
                R.drawable.icon_control_wind02_disable, R.drawable.icon_control_wind03_disable,};
        mImgWindLevel_Select_disable = new int[]{R.drawable.icon_control_wind_auto_on_disable, R.drawable.icon_control_wind01_on_disable,
                R.drawable.icon_control_wind02_on_disable, R.drawable.icon_control_wind03_on_disable,};

        mCtrlWindLevel = new int[]{WIND_LEVEL_AUTO, WIND_LEVEL_1, WIND_LEVEL_2, WIND_LEVEL_3,};

        mSelectedMainMode = CONTROL_VENTILATION;
        mPrevMainMode = mSelectedMainMode;

        layoutPower = findViewById(R.id.layoutPower);
        mImgPower = (ImageView) findViewById(R.id.btnPower);
        mTxtPower = findViewById(R.id.txtPower);
        mLayoutModes = new LinearLayout[]{
                (LinearLayout) findViewById(R.id.layoutMode1),
                (LinearLayout) findViewById(R.id.layoutMode2),
                (LinearLayout) findViewById(R.id.layoutMode3),
                (LinearLayout) findViewById(R.id.layoutMode4),
                (LinearLayout) findViewById(R.id.layoutMode5),
                (LinearLayout) findViewById(R.id.layoutMode6),
                (LinearLayout) findViewById(R.id.layoutMode7),
                (LinearLayout) findViewById(R.id.layoutMode8)
        };
        mImgModes = new ImageView[]{
                (ImageView) findViewById(R.id.imgMode1),
                (ImageView) findViewById(R.id.imgMode2),
                (ImageView) findViewById(R.id.imgMode3),
                (ImageView) findViewById(R.id.imgMode4),
                (ImageView) findViewById(R.id.imgMode5),
                (ImageView) findViewById(R.id.imgMode6),
                (ImageView) findViewById(R.id.imgMode7),
                (ImageView) findViewById(R.id.imgMode8)
        };
        mTxtModes = new TextView[]{
                (TextView) findViewById(R.id.txtMode1),
                (TextView) findViewById(R.id.txtMode2),
                (TextView) findViewById(R.id.txtMode3),
                (TextView) findViewById(R.id.txtMode4),
                (TextView) findViewById(R.id.txtMode5),
                (TextView) findViewById(R.id.txtMode6),
                (TextView) findViewById(R.id.txtMode7),
                (TextView) findViewById(R.id.txtMode8)
        };

        mImgWindLevels = new ImageView[]{
                (ImageView) findViewById(R.id.imgWind1),
                (ImageView) findViewById(R.id.imgWind2),
                (ImageView) findViewById(R.id.imgWind3),
                (ImageView) findViewById(R.id.imgWind4),
        };

        mLayoutCurrentHumi = (LinearLayout) findViewById(R.id.layoutCurrentHumi);
        mTxtCurrentHumi = (TextView) findViewById(R.id.txtCurrentHumi);
        mTxtSettingHumi = (TextView) findViewById(R.id.txtSettingHumi);
        mTxtReservation = (TextView) findViewById(R.id.txtReservation);
        mTxtDeepSleep = (TextView) findViewById(R.id.txtDeepSleep);
        mImgPercent = (ImageView) findViewById(R.id.imgPercent);

        /**
         * ?????? ?????????????????? ????????? ??????
         */
        mSeekBarSettingHumi = (SeekBar) findViewById(R.id.seekBarSettingHumi);
//        mSeekBarSettingHumi.setMax((PROGRESSBAR_HUMI_MAX - PROGRESSBAR_HUMI_MIN) / PROGRESSBAR_1STEP);
        mSeekBarSettingHumi.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (progress == 0) {
//                    mTxtSettingHumi.setText(getString(R.string.progressbar_humi_min));
                    mImgPercent.setVisibility(View.INVISIBLE);
                } else if (progress == seekBar.getMax()) {
//                    mTxtSettingHumi.setText(getString(R.string.progressbar_humi_max));
                    mImgPercent.setVisibility(View.INVISIBLE);
                } else {
//                    mTxtSettingHumi.setText(progress * PROGRESSBAR_1STEP + PROGRESSBAR_HUMI_MIN + "");
                    mImgPercent.setVisibility(View.VISIBLE);
                }
                mTxtSettingHumi.setText(progress * PROGRESSBAR_1STEP + PROGRESSBAR_HUMI_MIN + "");
                mSettingHumi = progress * PROGRESSBAR_1STEP + PROGRESSBAR_HUMI_MIN;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mHumidity_use = USE;
                sendToServer(7);
            }
        });

        /**
         * ????????????
         */
        mImgMinus = (ImageView) findViewById(R.id.imgMinus);
        mImgMinus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSeekBarSettingHumi.setProgress(mSeekBarSettingHumi.getProgress() <= 0 ? 0 : mSeekBarSettingHumi.getProgress() - 1);
                mHumidity_use = USE;
                sendToServer(7);
            }
        });
        mImgPlus = (ImageView) findViewById(R.id.imgPlus);
        mImgPlus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSeekBarSettingHumi.setProgress(mSeekBarSettingHumi.getProgress() >= mSeekBarSettingHumi.getMax() ? mSeekBarSettingHumi.getMax() : mSeekBarSettingHumi.getProgress() + 1);
                mHumidity_use = USE;
                sendToServer(7);
            }
        });

        mLayoutReservation = (LinearLayout) findViewById(R.id.layoutReservation);
        mLayoutReservation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mSelectedModeOption == MODE_OPTION_SLEEP && DeviceManager.getInstance().getCurrentDevice().isPowerOn()) {
                    showTimeSet(SCHEDULE_TYPE_SLEEP);
                } else {
                    showTimeSet(DeviceManager.getInstance().getCurrentDevice().isPowerOn() ? SCHEDULE_TYPE_OFF : SCHEDULE_TYPE_ON);
                }
            }
        });

        mLayoutDeepSleep = findViewById(R.id.layoutDeepSleep);
        mLayoutDeepSleep.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ControlActivity.this, DeepSleepActivity.class);
                intent.putExtra("sleepUse", mSleep);
                intent.putExtra("startTime", mSleepTimeStart);
                intent.putExtra("endTime", mSleepTimeEnd);
                startActivityAni(ControlActivity.this, intent, false, 0);
            }
        });

        /**
         * ???????????? ?????? ????????? ??????.
         */
        for (int index = 0; index < mImgWindLevels.length; index++) {
            final int temp_index = index;
            mImgWindLevels[index].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    updateWindLevelComponents(mCtrlWindLevel[temp_index], mSelectedMode, mSelectedModeOption);
                    mWind_use = USE;
                    usedForModeChange();
                    sendToServer(4);
                }
            });
        }
        //updateAllComponents();

        /**
         * Power On/Off??? ?????? ????????????
         */
        powerOnLayout = findViewById(R.id.layout_control_power_on);
        powerOffLayout = findViewById(R.id.layout_control_power_off);

        // ikHwang 2020-02-28 ?????? 10:19  ?????? ???????????? ?????? ????????????
        layout_dehumi = findViewById(R.id.layout_dehumi);
        layout_dehumi_line = findViewById(R.id.layout_dehumi_line);

        // Create by ikHwang on 2021-09-02 ?????? 10:01 UV ?????? ???????????? 
        layoutUV = findViewById(R.id.layoutUV);
        toggle_status = findViewById(R.id.toggle_status);

        toggle_status.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(buttonView.isPressed()){
                    mUVLedSMode = isChecked ? 2 : 1;
                    sendToServer(8);
                }
            }
        });

        toggle_status.setChecked(true);
    }

    private void updateAllComponents() {
        /**
         * ?????? ??????
         */
        mIsPower = DeviceManager.getInstance().getCurrentDevice().isPowerOn();
        mSelectedMode = DeviceManager.getInstance().getCurrentDevice().getOperationMode();
        mSelectedModeOption = DeviceManager.getInstance().getCurrentDevice().getModeOption();
        mSelectedWindLevel = DeviceManager.getInstance().getCurrentDevice().getSetWindLevel();
        mCurruntHumi = DeviceManager.getInstance().getCurrentDevice().getCurruntHumi();
        mSettingHumi = DeviceManager.getInstance().getCurrentDevice().getSettingHumi();
        mSettingReservationTime = DeviceManager.getInstance().getCurrentDevice().getSettingReservationTimer();
        mRemaintReservationTime = DeviceManager.getInstance().getCurrentDevice().getRemainReservationTimer();
        mSettingReservationType = DeviceManager.getInstance().getCurrentDevice().getRemainReservationType();
        mSleep = DeviceManager.getInstance().getCurrentDevice().sd.deepSleepUse;
        mSleepTimeStart = DeviceManager.getInstance().getCurrentDevice().getDeepSleepStart();
        mSleepTimeEnd = DeviceManager.getInstance().getCurrentDevice().getDeepSleepEnd();
        mUVLedSMode = DeviceManager.getInstance().getCurrentDevice().getUVLedMode();

        String log = "RECEIVED - ";
        log += "PWR : " + (mIsPower ? "ON" : "OFF") + " / ";
        log += "Mode : " + mSelectedMode + " / ";
        log += "ModeOption : " + mSelectedModeOption + " / ";
        log += "WindLevel : " + mSelectedWindLevel + " / ";
        log += "SetResTime : " + mSettingReservationTime + " / ";
        log += "RemResTime : " + mRemaintReservationTime + " / ";
        log += "SetResType : " + mSettingReservationType + " / ";
        log += "Set Sleep : " + mSleep + " / ";
        log += "DeepSleep : " + mSleepTimeStart + " ~ " + mSleepTimeEnd + " / ";
        log += "Humi : " + mSettingHumi + " / ";
        log += "UVLEDMode: " + mUVLedSMode + " / ";
        log += "UVLEDState: " + DeviceManager.getInstance().getCurrentDevice().getUVLedState();
        CommonUtils.customLog(TAG, log, Log.ERROR);

        // ikHwang 2019-05-24 ?????? 2:03 ???????????? ?????? ??????
        mImgPower.setImageResource(mIsPower ? R.drawable.icon_power_active : R.drawable.icon_power_normal);
        mTxtPower.setTextColor(ContextCompat.getColor(ControlActivity.this, mIsPower ? R.color.control_power_on : R.color.control_power_off));
        layoutPower.setBackgroundResource(mIsPower ? R.drawable.bg_btn_round_p : R.drawable.bg_btn_round_n);

        // ikHwang 2019-05-24 ?????? 2:03 ?????? ????????? ?????? ??? ??????
        if(mIsPower){
            mTabLayout.setEnabled(true);
            mTabLayout.setTabTextColors(getResources().getColor(R.color.text_tab_color), getResources().getColor(R.color.text_tab_select_color));
            mTabLayout.setSelectedTabIndicatorColor(getResources().getColor(R.color.text_tab_select_color));
        }else{
            mTabLayout.setEnabled(false);
            mTabLayout.setTabTextColors(getResources().getColor(R.color.control_tab_off), getResources().getColor(R.color.control_tab_off));
            mTabLayout.setSelectedTabIndicatorColor(getResources().getColor(R.color.transparent));
        }

        //?????? ???????????? ??????.
        int receiveMainMode = getMainMode(mSelectedMode);

        if (mIsChangeCurrentMainMode || mPrevMainMode != receiveMainMode) {
            mIsChangeCurrentMainMode = false;
            mPrevMainMode = receiveMainMode;
            mSelectedMainMode = getMainMode(mSelectedMode);
        }

        // ikHwang 2020-03-30 ?????? 9:32 ????????? (???????????? ????????? ?????? ?????? ??? ?????? 2??? ??????)
        if(DeviceManager.getInstance().getCurrentDevice().did.dehumiMode.modeUse != 2 && receiveMainMode == 3){
            receiveMainMode = 2;
        }

        if(receiveMainMode >= mTabLayout.getTabCount()){
            receiveMainMode = mTabLayout.getTabCount()-1;
        }

        // ikHwang 2019-07-24 ?????? 12:03 ?????? ???????????? ?????? ??? ??????????????? ??????
        try {
            mTabLayout.getTabAt(receiveMainMode).select();
        } catch (Exception e) {
            e.printStackTrace();
        }

        //?????? ??????N
        mTxtCurrentHumi.setText(String.valueOf(mCurruntHumi) + "%"); //???????????? TEXT

        //?????? ??????(Seekbar, TextView)
        setSeekbarSettingHumi(mSelectedMode, mSelectedModeOption, mSeekBarSettingHumi, mTxtSettingHumi);

        int ap = mSettingReservationTime / 60 / 12;
        int hour = mSettingReservationTime / 60;
        int min = mSettingReservationTime % 60;

        String txtType = "";
        String txtTime = String.format(getString(R.string.reservation_string), ap == Calendar.AM ? "??????" : "??????", (hour + 11) % 12 + 1, min);

        if (SCHEDULE_TYPE_SLEEP == mSettingReservationType) {
            txtType = getString(R.string.reservation_sleep);
        } else if (SCHEDULE_TYPE_ON == mSettingReservationType) {
            txtType = getString(R.string.reservation_on);
        } else if (SCHEDULE_TYPE_OFF == mSettingReservationType) {
            txtType = getString(R.string.reservation_off);
        }else{
            txtType = "";
            txtTime = "";
        }

        // ikHwang 2019-11-27 ?????? 8:38 ???????????? ???????????? ????????????
        if(3 == mSettingReservationType || 4 == mSettingReservationType){
            mTxtReservation.setText(txtType + " " + txtTime);
        }else{
            mTxtReservation.setText("");
        }

        // ???????????? ?????? ?????? (1??? ??? ????????? ????????? ???????????? ??????)
        mLayoutDeepSleep.setVisibility(((DeviceManager.getInstance().getCurrentDevice().head.ver == PROTOCOL_VER_ID1) || (CleanVentilationApplication.getInstance().getRoomControllerDevicePCD() == 9)) ? View.GONE : View.VISIBLE); //1???, 2???????????? ?????? layout ??????
        String txtSleepTime = "";
        if (mSleep == USE) {
            mSleepTimeStart = mSleepTimeStart / 60;
            mSleepTimeEnd = mSleepTimeEnd / 60;

            String startTime = (mSleepTimeStart / 12 == Calendar.AM ? "?????? " : "?????? ") + (((mSleepTimeStart + 11) % 12) + 1) + "???";
            String endTime = (mSleepTimeEnd / 12 == Calendar.AM ? "?????? " : "?????? ") + (((mSleepTimeEnd + 11) % 12) + 1) + "???";
            txtSleepTime = getString(R.string.deep_sleep_on, startTime) + " ~ " + getString(R.string.deep_sleep_off, endTime);
        }
        mTxtDeepSleep.setText(txtSleepTime);

        updateModeComponents(mSelectedMainMode, mSelectedModeOption); //SELECTED MODE
        updateWindLevelComponents(mSelectedWindLevel % 4, mSelectedMode, mSelectedModeOption); //????????????

        //?????? on, off
        togglePowerOnOffLayout();

        // UV LED ?????? ??????
        toggle_status.setChecked(mUVLedSMode == 2);
    }

    /**
     * ???????????? ?????? ?????? ??????
     *
     * @param selectedWindLevel ????????? ????????????
     * @param mode              ????????????
     * @param option            ????????? ??????
     */
    private void updateWindLevelComponents(int selectedWindLevel, final int mode, final int option) {
        boolean isForcedSelected = false;

        switch (option) {
            case MODE_OPTION_NOTING:
                switch (mode) {
                    case OPERATION_MODE_DEHUMI:
                    case OPERATION_MODE_AIR_CLEAN:
                    case OPERATION_MODE_DUST:
                    case OPERATION_MODE_VENTI:
                    case OPERATION_MODE_EXHAUST:
                        isForcedSelected = false;
                        break;
                    case OPERATION_MODE_KITCHEN:
                    case OPERATION_MODE_CLEAN:
                        isForcedSelected = true;
                        selectedWindLevel = WIND_LEVEL_3 % 4;
                        break;
                    case OPERATION_MODE_AUTO:
                        isForcedSelected = true;
                        selectedWindLevel = WIND_LEVEL_AUTO % 4;
                        break;
                }
                break;
            case MODE_OPTION_PWR_SAVING:
            case MODE_OPTION_SLEEP:
                isForcedSelected = true;
                selectedWindLevel = WIND_LEVEL_AUTO % 4;
                break;
            case MODE_OPTION_TURBO:
                isForcedSelected = true;
                selectedWindLevel = WIND_LEVEL_3 % 4;
                break;
        }

        mSelectedWindLevel = selectedWindLevel;

        // ikHwang 2019-06-04 ?????? 2:12 ???????????? AUTO??? ?????????????????? ??????
        selectedWindLevel %= 4;

        for (int index = 0; index < mImgWindLevels.length; index++) {
            if (isForcedSelected || !mIsPower) {
                if (index == selectedWindLevel) {
                    mImgWindLevels[index].setImageResource(mImgWindLevel_Select_disable[index]);
                } else {
                    mImgWindLevels[index].setImageResource(mImgWindLevel_Normal_disable[index]);
                }
            } else {
                if (index == selectedWindLevel) {
                    mImgWindLevels[index].setImageResource(mImgWindLevel_Select[index]);
                } else {
                    mImgWindLevels[index].setImageResource(mImgWindLevel_Normal[index]);
                }
            }
            mImgWindLevels[index].setEnabled(mIsPower ? !isForcedSelected : false);
        }
    }

    /**
     * ?????? ?????? ?????? ??????
     *
     * @param mode   ??????
     * @param option ????????? ??????
     * @return
     */
    private boolean isUsedModeOption(final int mode, final int option) {
        CommonUtils.customLog(TAG, "&&& isUsedModeOption mode : " + mode + ", option : " + option, Log.ERROR);

        boolean isUsedMode = false;
        boolean isUsedOption = false;
        switch (mode) {
            case OPERATION_MODE_DEHUMI:
                isUsedMode = DeviceManager.getInstance().getCurrentDevice().did.dehumiMode.modeUse == MODE_USED ? true : false;
                isUsedOption = isUsedModeOption(DeviceManager.getInstance().getCurrentDevice().did.dehumiMode, option);
                break;
            case OPERATION_MODE_VENTI:
                isUsedMode = DeviceManager.getInstance().getCurrentDevice().did.ventiMode.modeUse == MODE_USED ? true : false;
                isUsedOption = isUsedModeOption(DeviceManager.getInstance().getCurrentDevice().did.ventiMode, option);
                break;
            case OPERATION_MODE_EXHAUST:
                isUsedMode = DeviceManager.getInstance().getCurrentDevice().did.exhaustMode.modeUse == MODE_USED ? true : false;
                isUsedOption = isUsedModeOption(DeviceManager.getInstance().getCurrentDevice().did.exhaustMode, option);
                break;
            case OPERATION_MODE_KITCHEN:
                isUsedMode = DeviceManager.getInstance().getCurrentDevice().did.kitchenMode.modeUse == MODE_USED ? true : false;
                isUsedOption = isUsedModeOption(DeviceManager.getInstance().getCurrentDevice().did.kitchenMode, option);
                break;
            case OPERATION_MODE_CLEAN:
                isUsedMode = DeviceManager.getInstance().getCurrentDevice().did.cleanMode.modeUse == MODE_USED ? true : false;
                isUsedOption = isUsedModeOption(DeviceManager.getInstance().getCurrentDevice().did.cleanMode, option);
                break;
            case OPERATION_MODE_AIR_CLEAN:
                isUsedMode = DeviceManager.getInstance().getCurrentDevice().did.airCleanMode.modeUse == MODE_USED ? true : false;
                isUsedOption = isUsedModeOption(DeviceManager.getInstance().getCurrentDevice().did.airCleanMode, option);
                break;
            case OPERATION_MODE_DUST:
                isUsedMode = DeviceManager.getInstance().getCurrentDevice().did.dustMode.modeUse == MODE_USED ? true : false;
                isUsedOption = isUsedModeOption(DeviceManager.getInstance().getCurrentDevice().did.dustMode, option);
                break;
            case OPERATION_MODE_AUTO:
                isUsedMode = DeviceManager.getInstance().getCurrentDevice().did.autoMode.modeUse == MODE_USED ? true : false;
                isUsedOption = isUsedModeOption(DeviceManager.getInstance().getCurrentDevice().did.autoMode, option);
                break;
            case OPERATION_MODE_BYPASS:
                isUsedMode = DeviceManager.getInstance().getCurrentDevice().did.supportByPass == MODE_USED ? true : false;
                isUsedOption = isUsedModeOption(DeviceManager.getInstance().getCurrentDevice().did.bypassMode, option);
                CommonUtils.customLog(TAG,"isUsedMode > " + isUsedMode + ", isUsedOption > " + isUsedOption, Log.DEBUG);
                break;
        }
        return (isUsedMode && isUsedOption);
    }

    /**
     * ?????? ????????? ?????? ??????
     *
     * @param mode   ??????
     * @param option ????????? ??????
     * @return
     */
    private boolean isUsedModeOption(final DidPacket.ModeOperation mode, final int option) {
        boolean isUsedOption = false;
        switch (option) {
            case MODE_OPTION_NOTING:
            case MODE_OPTION_BYPASS:
                isUsedOption = mode.modeUse == MODE_USED;
                break;
            case MODE_OPTION_PWR_SAVING:
                isUsedOption = mode.pwrSavingUse == MODE_USED;
                break;
            case MODE_OPTION_SLEEP:
                isUsedOption = mode.sleepUse == MODE_USED; //????????????.
                break;
            case MODE_OPTION_TURBO:
                isUsedOption = mode.turboUse == MODE_USED;
                break;
        }
        return isUsedOption;
    }

    /**
     * TAC ????????? ?????? ?????? ?????? ??????
     *
     * @param mode
     * @return
     */
    private int getMainMode(int mode) {
        int mainMode = CONTROL_VENTILATION;
        switch (mode) {
            case OPERATION_MODE_VENTI:
            case OPERATION_MODE_EXHAUST:
            case OPERATION_MODE_CLEAN:
            case OPERATION_MODE_KITCHEN:
            case OPERATION_MODE_BYPASS:
                mainMode = CONTROL_VENTILATION;
                break;

            // ikHwang 2020-03-06 ?????? 3:38 ?????? ??????
            case OPERATION_MODE_DEHUMI:
                mainMode = CONTROL_DEHUMIDIFICATIOIN;
                break;

            case OPERATION_MODE_AIR_CLEAN:
            case OPERATION_MODE_DUST:
                mainMode = CONTROL_CLEAN;
                break;
            case OPERATION_MODE_AUTO:
                mainMode = CONTROL_AUTO;

                break;
        }
        return mainMode;
    }

    /**
     * ?????? ???????????????, TEXT, Enable, Disable ????????? ??????.
     *
     * @param mode
     * @param option
     * @param seekbar
     * @param textView
     */
    private void setSeekbarSettingHumi(final int mode, final int option, SeekBar seekbar, TextView textView) {
        final int NOTHING = -1;
        final int DISABLE = 0;
        final int ENABLE = 1;
        final int INVISIBLE = 2;
        int componentMode = NOTHING;
        int humi = mSettingHumi;

        switch (mode) {
            case OPERATION_MODE_DEHUMI:
                switch (option) {
                    case MODE_OPTION_NOTING:
                    case MODE_OPTION_PWR_SAVING:
                    case MODE_OPTION_SLEEP:
                    default:
                        componentMode = ENABLE;
                        break;
                    case MODE_OPTION_TURBO:
                        componentMode = DISABLE;
                        humi = PROGRESSBAR_HUMI_MAX;
                        break;
                }
                break;

            case OPERATION_MODE_AIR_CLEAN:
                componentMode = DISABLE;
                humi = 40;
                break;

            case OPERATION_MODE_DUST:
                componentMode = DISABLE;
                humi = 40;
                break;

            case OPERATION_MODE_VENTI:
                componentMode = DISABLE;
                humi = 40;
                break;

            case OPERATION_MODE_EXHAUST:
                componentMode = DISABLE;
                humi = 40;
                break;

            case OPERATION_MODE_KITCHEN:
                componentMode = DISABLE;
                humi = 40;
                break;

            case OPERATION_MODE_CLEAN:
                componentMode = DISABLE;
                humi = 40;
                break;

            case OPERATION_MODE_AUTO:
                componentMode = ENABLE;
                break;
        }

        if (humi <= PROGRESSBAR_HUMI_MIN) {
            humi = PROGRESSBAR_HUMI_MIN;
            mImgPercent.setVisibility(View.INVISIBLE);
//            textView.setText(getString(R.string.progressbar_humi_min));
        } else if (humi >= PROGRESSBAR_HUMI_MAX) {
            humi = PROGRESSBAR_HUMI_MAX;
            mImgPercent.setVisibility(View.INVISIBLE);
//            textView.setText(getString(R.string.progressbar_humi_max));
        } else {
            mImgPercent.setVisibility(View.VISIBLE);
//            textView.setText(String.valueOf(humi)); //???????????? TEXT
        }

        textView.setText(String.valueOf(humi)); //???????????? TEXT

        seekbar.setProgress((humi - PROGRESSBAR_HUMI_MIN) / PROGRESSBAR_1STEP); //???????????? Progress
        mSettingHumi = humi;

        if (componentMode == ENABLE) {
            if (mIsPower) {
                mImgMinus.setEnabled(true);
                mImgPlus.setEnabled(true);
                seekbar.setEnabled(true);
                textView.setEnabled(true);
                mImgPercent.setImageResource(R.drawable.control_hum);
            } else {
                mImgMinus.setEnabled(false);
                mImgPlus.setEnabled(false);
                seekbar.setEnabled(false);
                textView.setEnabled(false);
                mImgPercent.setImageResource(R.drawable.control_hum_disable);
            }
        } else if (componentMode == DISABLE) {
            mImgMinus.setEnabled(false);
            mImgPlus.setEnabled(false);
            seekbar.setEnabled(false);
            textView.setEnabled(false);
            mImgPercent.setImageResource(R.drawable.control_hum_disable);
        } else {
            mImgMinus.setEnabled(false);
            mImgPlus.setEnabled(false);
            seekbar.setEnabled(false);
            textView.setEnabled(false);
            textView.setText("--");
            mImgPercent.setVisibility(View.INVISIBLE);
        }

        if (textView.isEnabled()) {
            mLayoutCurrentHumi.setAlpha(1f);
        } else {
            mLayoutCurrentHumi.setAlpha(0.3f);
        }
    }

    /**
     * ????????? ????????? ?????? ???????????? ??????. ??????/??????/??????/??????
     * @param selectedMainMode,
     * @param selectedModeOption
     */
    private void updateModeComponents(final int selectedMainMode, int selectedModeOption) {
        mSelectedMainMode = selectedMainMode;
        mSelectedModeOption = selectedModeOption;

        // ikHwang 2020-02-28 ?????? 10:26 ???????????? ???????????? ???????????? ??????
        layout_dehumi.setVisibility(View.GONE);
        layout_dehumi_line.setVisibility(View.GONE);
        
        switch (selectedMainMode) {
            case CONTROL_VENTILATION:
                for (int index = 0; index < mLayoutModes.length; index++) {
                    if (index < mImgModes1_Normal.length && isUsedModeOption(mCtrlMode1[index], mCtrlModeOption1[index])) {//???????????? ???????????? ??????.
                        final int temp_index = index;

                        // ikHwang 2019-10-21 ?????? 9:56 1??? ???????????? ?????????????????? ???????????? ????????? ????????? ??????
                        try {
                            if (mCtrlModeOption1[temp_index] == MODE_OPTION_SLEEP && CleanVentilationApplication.getInstance().getRoomControllerDevicePCD() < 3) {
                                mLayoutModes[index].setVisibility(View.GONE);
                                continue;
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        if (mIsPower) { //?????? on
                            mLayoutModes[index].setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    //?????? ?????? send ?????? - jh.j
                                    if (mCtrlModeOption1[temp_index] == MODE_OPTION_SLEEP) {
                                        mNextdMode = mCtrlMode1[temp_index];
                                        updateModeComponents(selectedMainMode, mCtrlModeOption1[temp_index]);
//                                        showTimeSet(SCHEDULE_TYPE_SLEEP);
                                        sendSleepTimeSet();
                                    } else {
                                        mSelectedMode = mCtrlMode1[temp_index];
                                        updateModeComponents(selectedMainMode, mCtrlModeOption1[temp_index]);
                                        usedForModeChange();
                                        sendToServer(4);
                                    }
                                }
                            });
                            mLayoutModes[index].setVisibility(View.VISIBLE);
                            mTxtModes[index].setText(mTxtModes1[index]);
                            if (mCtrlMode1[index] == mSelectedMode && selectedModeOption == mCtrlModeOption1[index]) {
                                mImgModes[index].setImageResource(mImgModes1_Select[index]);
                                mTxtModes[index].setTextColor(Color.rgb(0x36, 0x90, 0xb8));
                            } else {
                                mImgModes[index].setImageResource(mImgModes1_Normal[index]);
                                mTxtModes[index].setTextColor(Color.rgb(0xAA, 0xAA, 0xAA));
                            }
                        } else {
                            mLayoutModes[index].setOnClickListener(null);
                            mLayoutModes[index].setVisibility(View.VISIBLE);
                            mTxtModes[index].setText(mTxtModes1[index]);
                            if (mCtrlMode1[index] == mSelectedMode && selectedModeOption == mCtrlModeOption1[index]) {
                                mImgModes[index].setImageResource(mImgModes1_Select_disable[index]);
                                mTxtModes[index].setTextColor(Color.rgb(0xAA, 0xAA, 0xAA));
                            } else {
                                mImgModes[index].setImageResource(mImgModes1_Normal_disable[index]);
                                mTxtModes[index].setTextColor(Color.rgb(0xAA, 0xAA, 0xAA));
                            }
                        }
                    } else {
                        mLayoutModes[index].setVisibility(View.GONE);
                    }
                }
                break;

            case CONTROL_CLEAN:
                for (int index = 0; index < mLayoutModes.length; index++) {
                    if (index < mImgModes2_Normal.length && isUsedModeOption(mCtrlMode2[index], mCtrlModeOption2[index])) {
                        final int temp_index = index;
                        if (mIsPower) {
                            mLayoutModes[index].setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    if (mCtrlModeOption2[temp_index] == MODE_OPTION_SLEEP) {
                                        mNextdMode = mCtrlMode2[temp_index];
                                        updateModeComponents(selectedMainMode, mCtrlModeOption2[temp_index]);
                                        sendSleepTimeSet();
                                    } else {
                                        mSelectedMode = mCtrlMode2[temp_index];
                                        updateModeComponents(selectedMainMode, mCtrlModeOption2[temp_index]);
                                        usedForModeChange();
                                        sendToServer(4);
                                    }
                                }
                            });
                            mLayoutModes[index].setVisibility(View.VISIBLE);
                            mTxtModes[index].setText(mTxtModes2[index]);
                            if (mCtrlMode2[index] == mSelectedMode && selectedModeOption == mCtrlModeOption2[index]) {
                                mImgModes[index].setImageResource(mImgModes2_Select[index]);
                                mTxtModes[index].setTextColor(Color.rgb(0x36, 0x90, 0xb8));
                            } else {
                                mImgModes[index].setImageResource(mImgModes2_Normal[index]);
                                mTxtModes[index].setTextColor(Color.rgb(0xAA, 0xAA, 0xAA));
                            }
                        } else {
                            mLayoutModes[index].setOnClickListener(null);
                            mLayoutModes[index].setVisibility(View.VISIBLE);
                            mTxtModes[index].setText(mTxtModes2[index]);
                            if (mCtrlMode2[index] == mSelectedMode && selectedModeOption == mCtrlModeOption2[index]) {
                                mImgModes[index].setImageResource(mImgModes2_Select_disable[index]);
                                mTxtModes[index].setTextColor(Color.rgb(0xAA, 0xAA, 0xAA));
                            } else {
                                mImgModes[index].setImageResource(mImgModes2_Normal_disable[index]);
                                mTxtModes[index].setTextColor(Color.rgb(0xAA, 0xAA, 0xAA));
                            }
                        }
                    } else {
                        mLayoutModes[index].setVisibility(View.GONE);
                    }
                }
                break;

            // ikHwang 2020-02-28 ?????? 10:22 ??????
            case CONTROL_DEHUMIDIFICATIOIN:
                layout_dehumi.setVisibility(View.VISIBLE);
                layout_dehumi_line.setVisibility(View.VISIBLE);

                for (int index = 0; index < mLayoutModes.length; index++) {
                    if (index < mImgModes3_Normal.length && isUsedModeOption(mCtrlMode3[index], mCtrlModeOption3[index])) {
                        final int temp_index = index;
                        if(mIsPower) {
                            mLayoutModes[index].setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    if (mCtrlModeOption3[temp_index] == MODE_OPTION_SLEEP) {
                                        mNextdMode = mCtrlMode3[temp_index];
                                        sendSleepTimeSet();
                                    } else {
                                        mSelectedMode = mCtrlMode3[temp_index];
                                        updateModeComponents(selectedMainMode, mCtrlModeOption3[temp_index]);
                                        usedForModeChange();
                                        sendToServer(4);
                                    }
                                }
                            });
                            mLayoutModes[index].setVisibility(View.VISIBLE);
                            mTxtModes[index].setText(mTxtModes3[index]);
                            if (mCtrlMode3[index] == mSelectedMode && selectedModeOption == mCtrlModeOption3[index]) {
                                mImgModes[index].setImageResource(mImgModes3_Select[index]);
                                mTxtModes[index].setTextColor(Color.rgb(0x36, 0x90, 0xb8));
                            } else {
                                mImgModes[index].setImageResource(mImgModes3_Normal[index]);
                                mTxtModes[index].setTextColor(Color.rgb(0xAA, 0xAA, 0xAA));
                            }
                        } else {
                            mLayoutModes[index].setOnClickListener(null);
                            mLayoutModes[index].setVisibility(View.VISIBLE);
                            mTxtModes[index].setText(mTxtModes3[index]);
                            if (mCtrlMode3[index] == mSelectedMode && selectedModeOption == mCtrlModeOption3[index]) {
                                mImgModes[index].setImageResource(mImgModes3_Select_disable[index]);
                                mTxtModes[index].setTextColor(Color.rgb(0xAA, 0xAA, 0xAA));
                            } else {
                                mImgModes[index].setImageResource(mImgModes3_Normal_disable[index]);
                                mTxtModes[index].setTextColor(Color.rgb(0xAA, 0xAA, 0xAA));
                            }
                        }
                    } else {
                        mLayoutModes[index].setVisibility(View.GONE);
                    }
                }
                break;

            case CONTROL_AUTO:
                for (int index = 0; index < mLayoutModes.length; index++) {
                    if (index < mImgModes4_Normal.length && isUsedModeOption(mCtrlMode4[index], mCtrlModeOption4[index])) {
                        final int temp_index = index;
                        if (mIsPower) {
                            mLayoutModes[index].setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    if (mCtrlModeOption4[temp_index] == MODE_OPTION_SLEEP) {
                                        //showTimeSet(SCHEDULE_TYPE_SLEEP);
                                        mNextdMode = mCtrlMode4[temp_index];
                                        updateModeComponents(selectedMainMode, mCtrlModeOption4[temp_index]);
                                        sendSleepTimeSet();
                                    } else {
                                        mSelectedMode = mCtrlMode4[temp_index];
                                        updateModeComponents(selectedMainMode, mCtrlModeOption4[temp_index]);
                                        usedForModeChange();
                                        sendToServer(4);
                                    }
                                }
                            });
                            mLayoutModes[index].setVisibility(View.VISIBLE);
                            mTxtModes[index].setText(mTxtModes4[index]);
                            if (mCtrlMode4[index] == mSelectedMode && selectedModeOption == mCtrlModeOption4[index]) {
                                mImgModes[index].setImageResource(mImgModes4_Select[index]);
                                mTxtModes[index].setTextColor(Color.rgb(0x36, 0x90, 0xb8));
                            } else {
                                mImgModes[index].setImageResource(mImgModes4_Normal[index]);
                                mTxtModes[index].setTextColor(Color.rgb(0xAA, 0xAA, 0xAA));
                            }
                        } else {
                            mLayoutModes[index].setOnClickListener(null);
                            mLayoutModes[index].setVisibility(View.VISIBLE);
                            mTxtModes[index].setText(mTxtModes4[index]);
                            if (mCtrlMode4[index] == mSelectedMode && selectedModeOption == mCtrlModeOption4[index]) {
                                mImgModes[index].setImageResource(mImgModes4_Select_disable[index]);
                                mTxtModes[index].setTextColor(Color.rgb(0xAA, 0xAA, 0xAA));
                            } else {
                                mImgModes[index].setImageResource(mImgModes4_Normal_disable[index]);
                                mTxtModes[index].setTextColor(Color.rgb(0xAA, 0xAA, 0xAA));
                            }
                        }
                    } else {
                        mLayoutModes[index].setVisibility(View.GONE);
                    }
                }
                break;
        }
    }

    @Override
    public void onBackPressed() {
        if (mLayoutTimeSet.getVisibility() == View.VISIBLE) {
            mSetTimeType = SCHEDULE_TYPE_NO;
            mNextdMode = OPERATION_MODE_IDLE;
            hideTimeSet();
        } else {
            initiateTwoButtonAlert(this, getString(R.string.exit_app), getString(R.string.confirm), getString(R.string.no));
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.layoutPower: // ?????? ??????
                mIsPower = !mIsPower;
                sendToServer(3);
                break;

            case R.id.btnCancel:
                mSetTimeType = SCHEDULE_TYPE_NO;
                mNextdMode = OPERATION_MODE_IDLE;
                hideTimeSet();
                break;

            case R.id.btnSave:
                if (mTimeSetBtnOnOff.isChecked() || (!mTimeSetBtnOnOff.isChecked() && mSettingReservationType != SCHEDULE_TYPE_NO)) {
                    if (mSetTimeType == SCHEDULE_TYPE_SLEEP) {
                        mSetTimeType = SCHEDULE_TYPE_OFF;
                        updateModeComponents(mSelectedMode, MODE_OPTION_SLEEP); //SELECTED MODE
                    }
                    saveAndSendTimeSet();
                    mNextdMode = OPERATION_MODE_IDLE;
                    mSetTimeType = SCHEDULE_TYPE_NO;
                }

                hideTimeSet();
                break;

            case R.id.btnMore: // ????????? (??????)
                Intent intentMore = new Intent(this, MoreActivity.class);
                intentMore.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivityAni(ControlActivity.this, intentMore, false, 1);
                break;

            case R.id.btnHelp: // ????????? ?????? ??????
                Intent intentMoreHelp = new Intent(this, MoreHelpActivity.class);
                startActivityAni(ControlActivity.this, intentMoreHelp, false, 1);
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
            HttpApi.PostV2GetDeviceInfo( //V2A ??????.
                    CleanVentilationApplication.getInstance().getUserInfo().getId(),
                    new Callback() {
                        @Override
                        public void onFailure(Call call, IOException e) {
                            mIsShowErrorPopup = true;
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    DismissConnectDialog();
                                    mIsShowErrorPopup = true;
                                    Intent intent = new Intent(ControlActivity.this, PopupActivity.class);
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
                                    mIsShowErrorPopup = true;
                                    DismissConnectDialog();
                                    Intent intent = new Intent(ControlActivity.this, PopupActivity.class);
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
                                                    CleanVentilationApplication.getInstance().awsMqttConnect();
                                                } catch (Exception e) {
                                                    errorToConnectWithServer(false);
                                                }
                                            }else{
                                                runOnUiThread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        mIsShowErrorPopup = true;
                                                        Intent intent = new Intent(ControlActivity.this, PopupActivity.class);
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
            CommonUtils.showToast(ControlActivity.this, getString(R.string.toast_result_can_not_search));
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
    private void errorToConnectWithServer(boolean isTimeout) {
        CommonUtils.customLog(TAG, "errorToConnectWithServer()", Log.ERROR);

        CleanVentilationApplication.getInstance().awsMqttDisConnect();

        if(isTimeout){
            // 10??? ????????????
            mIsShowErrorPopup = true;
            Intent intent = new Intent(ControlActivity.this, PopupActivity.class);
            intent.putExtra(PopupActivity.POPUP_MODE, PopupActivity.MODE_NO_BUTTON);
            intent.putExtra(PopupActivity.POPUP_TIMEOUT, 3000);
            intent.putExtra(PopupActivity.POPUP_BODY_TEXT, getString(R.string.error_connect_for_control_device));
            startActivityForResult(intent, RESULT_ERROR_CONNECT_TIMEOUT);
            overridePendingTransition(0, 0);
        }else{
            if(isActivityForeground){
                if (!mIsShowErrorPopup) {
                    mIsShowErrorPopup = true;

                    if (IsRunningProgress()) CommonUtils.DismissConnectDialog();

                    if (!mIsSendFirstMessage) {
                        // DID ?????? ??? ?????? ?????? ?????? ?????? ?????????
                        Intent intent = new Intent(ControlActivity.this, PopupActivity.class);
                        intent.putExtra(PopupActivity.POPUP_MODE, PopupActivity.MODE_YESNO);
                        intent.putExtra(PopupActivity.POPUP_BODY_TEXT, getString(R.string.error_connect_room_con));
                        intent.putExtra(PopupActivity.POPUP_CONFIRM_BUTTON_TEXT, getString(R.string.detail_view));
                        intent.putExtra(PopupActivity.POPUP_CANCEL_BUTTON_TEXT, getString(R.string.cancel));
                        startActivityForResult(intent, RESULT_ERROR_FIRTST_SENDMASSAGE);
                        overridePendingTransition(0, 0);
                    } else {
                        // ?????? ????????? ????????? ?????? ?????? ??????
                        DeviceManager.getInstance().getCurrentDevice().sd.errorLevel_L = 0;
                        Intent intent = new Intent(ControlActivity.this, PopupActivity.class);
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

    class CallbackHandler extends Handler {
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Bundle args = msg.getData();
            switch (msg.what) {
                case MSG_DEEP_SLEEP:
                    mSleep_use = USE;
                    mSleep = args.getInt("sleepUse");
                    mSleepTimeStart = args.getInt("sleepStart");
                    mSleepTimeEnd = args.getInt("sleepEnd");
                    sendToServer(6);
                    break;

                case MSG_DEEP_SLEEP_CANCEL:
                    mSleep_use = USE;
                    mSleep = args.getInt("sleepUse");
                    mSleepTimeStart = 0;
                    mSleepTimeEnd = 0;
                    sendToServer(6);
                    break;

                case MSG_DEEP_SLEEP_COMPLETE:
                    if (mCanNotControlTimer != null) {
                        mCanNotControlTimer.cancel();
                    }
                    break;
            }
        }
    }

    /**
     * Powe On/ Off??? ?????? layout show/hide
     */
    private void togglePowerOnOffLayout(){
        powerOnLayout.setVisibility(mIsPower ? View.VISIBLE : View.GONE);
        powerOffLayout.setVisibility(mIsPower ? View.GONE : View.VISIBLE);
    }
}












