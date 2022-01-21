package kr.co.kdone.airone.utils;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.DhcpInfo;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.text.InputFilter;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.analytics.FirebaseAnalytics;

import org.json.JSONObject;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import kr.co.kdone.airone.BuildConfig;
import kr.co.kdone.airone.CleanVentilationApplication;
import kr.co.kdone.airone.R;
import kr.co.kdone.airone.activity.SplashActivity;

import static kr.co.kdone.airone.utils.ProtocolType.MODEL_CODE_TAC500_20;
import static kr.co.kdone.airone.utils.ProtocolType.MODEL_CODE_TAC500_20S;
import static kr.co.kdone.airone.utils.ProtocolType.MODEL_CODE_TAC510_20S;
import static kr.co.kdone.airone.utils.ProtocolType.MODEL_CODE_TAC530_20S;
import static kr.co.kdone.airone.utils.ProtocolType.MODEL_CODE_TAC550_20S;
import static kr.co.kdone.airone.utils.ProtocolType.MODEL_CODE_TAC710_20S;
import static kr.co.kdone.airone.utils.ProtocolType.MODEL_CODE_TAC730_20S;
import static kr.co.kdone.airone.utils.ProtocolType.MODEL_CODE_TAC740_20S;
import static kr.co.kdone.airone.utils.ProtocolType.MODEL_CODE_TAC750_20S;
import static kr.co.kdone.airone.utils.ProtocolType.SENSOR_LEVEL_BAD;
import static kr.co.kdone.airone.utils.ProtocolType.SENSOR_LEVEL_GOOD;
import static kr.co.kdone.airone.utils.ProtocolType.SENSOR_LEVEL_NORMAL;
import static kr.co.kdone.airone.utils.ProtocolType.SENSOR_LEVEL_UNKNOWN;
import static kr.co.kdone.airone.utils.ProtocolType.SENSOR_LEVEL_VERY_BAD;

/**
 * 공통 함수 및 변수 선언.
 */
public class CommonUtils {
    private static final String TAG = CommonUtils.class.getSimpleName();

    // ikHwang 2019-05-15 오전 10:28 로그 출력 플래그
    private static final boolean DEBUG = BuildConfig.DEBUG; // 디버그모드 로그 보기

    private static CustomProgressDialog mConnectProgressDialog;
    private static Timer mDialogTimer;

    public static final int RESULT_INIT_APP_CONFIRM = 0x0123;
    public static final int RESULT_INIT_APP_SUCCESS = 0x0124;
    public static final int RESULT_WITHDRAW_CONFIRM = 0x0125;
    public static final int RESULT_WITHDRAW_SUCCESS = 0x0126;
    public static final int RESULT_SET_TIME_SUCCESS = 0x0127;
    public static final int RESULT_CHANGE_ADMIN_SUCCESS = 0x0128;
    public static final int RESULT_DELETE_USER = 0x0129;
    public static final int RESULT_DELETE_USER_SUCCESSED = 0x01230;
    public static final int RESULT_ERROR_FIRTST_SENDMASSAGE = 0x01231;
    public static final int RESULT_ERROR_STATE_DEVICE_NO_DB = 0x01232;
    public static final int RESULT_ERROR_STATE_DEVICE_NO_CONNECTED = 0x01233;
    public static final int RESULT_GET_NEW_WEATHER_INFO = 0x01234;
    public static final int RESULT_SET_AP_DEVICE = 0x1235;
    public static final int RESULT_INPUT_TEXT_COMFIRM = 0x1236;
    public static final int RESULT_DELETE_DEVICE_COMFIRM = 0x1237;
    public static final int RESULT_PUSH_TIME = 0x1238;
    public static final int RESULT_REGISTER_SENSORBOX_COMFIRM = 0x1239;
    public static final int RESULT_REGISTER_SENSORBOX_SUCCESSED = 0x1240;
    public static final int RESULT_SCAN_NO_SENSORBAX = 0x1241;
    public static final int RESULT_UNREGISTER_SENSORBOX_COMFIRM = 0x1242;
    public static final int RESULT_UNREGISTER_SENSORBOX_SUCCESSED = 0x1243;
    public static final int RESULT_FIND_LOCATION = 0x1244;
    public static final int RESULT_NO_DEVICE = 0x1245;
    public static final int RESULT_NO_SENSORBOX = 0x1246;
    public static final int RESULT_ADD_DEVICE = 0x1247;
    public static final int RESULT_CANT_CONTROL = 0x1248;
    public static final int RESULT_LOGOUT_COMFIRM = 0x1249;
    public static final int RESULT_CHANGE_AP_COMFIRM = 0x1250;
    public static final int RESULT_CHANGE_LOCATION_SUCCESSED = 0x1251;
    public static final int RESULT_ERROR_CONNECTION_WITH_SERVER = 0x01252;
    public static final int RESULT_ERROR_CONNECT_TIMEOUT = 0x1253;
    public static final int RESULT_SELECTED_CONTROL_DEVICE = 0x1254;
    public static final int RESULT_CHANGE_ADMIN = 0x01255;
    public static final int RESULT_SUCCESSED_SAVING_PUSH = 0x1256;
    public static final int RESULT_GET_ID_COMFIRM = 0x1257;
    public static final int RESULT_CHECK_NETWORK = 0x1258;
    public static final int RESULT_PERMISSION = 0x1259;
    public static final int RESULT_GPS_OFF = 0x1260;
    public static final int RESULT_AP_PASSWORD = 0x1261;
    public static final int RESULT_UNREGISTER_DEVICE_POPUP = 0x1262;
    public static final int RESULT_REGISTER_DEVICE = 0x1263;
    public static final int RESULT_DETAIL_CHECK = 0x1264;
    public static final int RESULT_REGISTER_ROOMCON_COMFIRM = 0x1265;
    public static final int RESULT_GET_DEVICE_ID = 0x1266;
    public static final int RESULT_SELECT_DEVICE_TYPE = 0x1267;
    public static final int RESULT_FILTER_RESET = 0x1268;
    public static final int RESULT_GET_DEVICE_PRISM_ID = 0x1269;
    public static final int RESULT_FILTER_CLEAN = 0x1270;
    public static final int RESULT_SERVER_MAINTENANCE = 0x1271;
    public static final int RESULT_FILTER_ALARM_1 = 0x1272;
    public static final int RESULT_FILTER_ALARM_2 = 0x1273;
    public static final int RESULT_DEVICE_NOT_EXIST = 0x1274;
    public static final int RESULT_PERMISSION_CHECK = 0x1275;
    public static final int RESULT_PERMISSION_SYSTEM = 0x1276;
    public static final int RESULT_ERROR_ODU = 0x1277;
    public static final int RESULT_LOGIN_FAIL = 0x1278;
    public static final int RESULT_LOGIN_RECAPCHA = 0x1279;
    public static final int RESULT_AGREEMENT = 0x1280;

    public static final int MODE_NONE = 0;
    public static final int MODE_DEVICE_REG = 1;
    public static final int MODE_FIND_ID = 2;
    public static final int MODE_FIND_PASS = 3;
    public static final int MODE_SUCCESS_REG_ID = 4;
    public static final int MODE_EDIT_USER = 5;
    public static final int MODE_SUCCESS_USER_INFO = 6;
    public static final int MODE_SUB_USER_REG = 7;
    public static final int MODE_SUCCESS_REG_SUB_USER = 8;
    public static final int MODE_DEVICE_ONLY_REG = 9;
    public static final int MODE_CHANGE_AP = 10;
    public static final int MODE_MODIFY_PASSWORD = 11;
    public static final int MODE_MODIFY_NAME = 12;
    public static final int MODE_MODIFY_PHONE = 13;
    public static final int MODE_DEVICE_ROOM_CON = 14;
    public static final int MODE_DEVICE_MONITOR = 15;
    public static final int MODE_SELECT_DEVICE = 16;

    public static final String EXTRA_BUNDLE = "extra_bundle";
    public static final String EXTRA_TEXT_LOCATION = "extra_text_location";
    public static final String EXTRA_POSTAL_CODE = "extra_postal_code";
    public static final String EXTRA_DEVICE_ID = "extra_device_id";
    public static final String EXTRA_DEVICE_NICKNAME = "extra_device_nickname";
    public static final String EXTRA_USER_ID = "extra_user_id";
    public static final String EXTRA_ADDRESS = "extra_address";

    public static final String FIRST_PRODUCT_USER = "FirstProduct";
    public static final String SECOND_PRODUCT_USER = "SecondProduct";

    public static final int CONTROL_VENTILATION = 0;
    public static final int CONTROL_CLEAN = 1;
    public static final int CONTROL_DEHUMIDIFICATIOIN = 2; // ikHwang 2020-02-28 오전 10:20 제습
    public static final int CONTROL_AUTO = 3;

    public static final int CHART_SELECTED_DAYS = 0;
    public static final int CHART_SELECTED_WEEKS = 1;
    public static final int CHART_SELECTED_MONTHS = 2;
    public static final int CHART_TYPE_INSIDE = 0;
    public static final int CHART_TYPE_OUTSIDE = 1;

    public static final int SMARTALARM_ICON_TYPE_FILTER = 0;
    public static final int SMARTALARM_ICON_TYPE_GOOD = 1;
    public static final int SMARTALARM_ICON_TYPE_NORMAL = 2;
    public static final int SMARTALARM_ICON_TYPE_BAD = 3;
    public static final int SMARTALARM_ICON_TYPE_VERY_BAD = 4;

    public static int PROGRESSBAR_HUMI_MIN = 35;
    public static int PROGRESSBAR_HUMI_MAX = 65;
    public static final int PROGRESSBAR_1STEP = 5;

    public static final int ANIMATION_TIME = 500;
    public static final int SHOW_TOAST_TIME = 2000;

    /*public static int getSmartAlarmIcon(int i) {
        switch (i) {
            case SMARTALARM_ICON_TYPE_FILTER:
            default:
                return R.drawable.icon_smart_noti_filter;
            case SMARTALARM_ICON_TYPE_GOOD:
                return R.drawable.icon_smart_noti_blue;
            case SMARTALARM_ICON_TYPE_NORMAL:
                return R.drawable.icon_smart_noti_green;
            case SMARTALARM_ICON_TYPE_BAD:
                return R.drawable.icon_smart_noti_yellow;
            case SMARTALARM_ICON_TYPE_VERY_BAD:
                return R.drawable.icon_smart_noti_red;
        }
    }*/

    public static int getNewSmartAlarmIcon(int i) {
        switch (i) {
            case 1:
                return R.drawable.icon_smart_noti_co2_yellow;

            case 2:
                return R.drawable.icon_smart_noti_co2_red;

            case 3:
                return R.drawable.icon_smart_noti_tvoc_yellow;

            case 4:
                return R.drawable.icon_smart_noti_pm25_yellow;

            case 5:
                return R.drawable.icon_smart_noti_pm25_red;

            case 6:
                return R.drawable.icon_smart_noti_filter;

            case 7:
                return R.drawable.icon_smart_noti_filter_ok;

            default:
                return 0;
        }
    }

    public static final int MAIN_MENU_HOME = 0;
    public static final int MAIN_MENU_CCONTROL = 1;
    public static final int MAIN_MENU_SMARTALARM = 2;
    public static final int MAIN_MENU_LIFEREPORT = 3;
    public static final int MAIN_MENU_MORE = 4;

    public static int MAIN_PRE_FRAGMENT_NO = 0; //메인 이전화면 유지용 상수


    //WiFi
    public static final int SECURITY_NONE = 0;
    public static final int SECURITY_WEP = 1;
    public static final int SECURITY_PSK = 2;
    public static final int SECURITY_EAP = 3;

    public enum PskType {
        UNKNOWN,
        WPA,
        WPA2,
        WPA_WPA2
    }

    public static int getSecurity(ScanResult result) {
        if (result.capabilities.contains("WEP")) {
            return SECURITY_WEP;
        } else if (result.capabilities.contains("PSK")) {
            return SECURITY_PSK;
        } else if (result.capabilities.contains("EAP")) {
            return SECURITY_EAP;
        }
        return SECURITY_NONE;
    }

    public static int getSecurity(WifiConfiguration config) {
        if (config.allowedKeyManagement.get(WifiConfiguration.KeyMgmt.WPA_PSK)) {
            return SECURITY_PSK;
        }
        if (config.allowedKeyManagement.get(WifiConfiguration.KeyMgmt.WPA_EAP) ||
                config.allowedKeyManagement.get(WifiConfiguration.KeyMgmt.IEEE8021X)) {
            return SECURITY_EAP;
        }
        return (config.wepKeys[0] != null) ? SECURITY_WEP : SECURITY_NONE;
    }

    public static PskType getPskType(ScanResult result) {
        boolean wpa = result.capabilities.contains("WPA-PSK");
        boolean wpa2 = result.capabilities.contains("WPA2-PSK");
        if (wpa2 && wpa) {
            return PskType.WPA_WPA2;
        } else if (wpa2) {
            return PskType.WPA2;
        } else if (wpa) {
            return PskType.WPA;
        } else {
            return PskType.UNKNOWN;
        }
    }

    public static String convertToQuotedString(String string) {
        return "\"" + string + "\"";
    }

    public static String removeDoubleQuotes(String string) {
        int length = string.length();
        if ((length > 1) && (string.charAt(0) == '"')
                && (string.charAt(length - 1) == '"')) {
            return string.substring(1, length - 1);
        }
        return string;
    }

    public static final int MICOM_PORT = 48888;
    public static final int AT_COMMAND_PORT = 48899;

    //COMMAND INDEX
    public static final int UDP_CMD_NONE = 0;
    public static final int UDP_CMD_DEVICE_GET_ID = 1;
    public static final int UDP_CMD_DEVICE_GEN_ID = 2;
    public static final int UDP_CMD_PASS = 3;
    public static final int UDP_CMD_AT_OK = 4;
    public static final int UDP_CMD_AT_SCAN = 5;
    public static final int UDP_CMD_STN_MODE = 6;

    //UDP Micom source
    public static final int MICOM_SOURCE_SERVER = 0x01;
    public static final int MICOM_SOURCE_APP = 0x02;
    public static final int MICOM_SOURCE_DEVICE = 0x03;

    //UDP Micom command
    public static final int MICOM_COMMAND_NONE = 0x00;
    public static final int MICOM_COMMAND_DEVICE_ID_REQ = 0x80;
    public static final int MICOM_COMMAND_DEVICE_GEN_REQ = 0x82;
    public static final int MICOM_COMMAND_STN_MODE_START_REQ = 0x84;
    public static final int MICOM_COMMAND_STN_MODE_FORCE_ENTRY_REQ = 0x86;

    //UDP Micom response
    public static final int MICOM_RESPONSE_NONE = 0x00;
    public static final int MICOM_RESPONSE_DEVICE_ID_RSP = 0x81;
    public static final int MICOM_RESPONSE_DEVICE_GEN_RSP = 0x83;
    public static final int MICOM_RESPONSE_STN_MODE_START_RSP = 0x85;
    public static final int MICOM_COMMAND_STN_MODE_FORCE_ENTRY_RSP = 0x87;

    public static final int SENSOR_PROGRESSBAR_1STEP = 90;


    //Service & Receive Action
    public static String START_SERVICE_ACTION = "kr.co.kdone.airone.START_SERVICE_ACTION";
    public static String STOP_SERVICE_ACTION = "kr.co.kdone.airone.STOP_SERVICE_ACTION";
    public static String TCP_SEND_MESSAGE_ACTION = "kr.co.kdone.airone.TCP_SEND_MESSAGE_ACTION";
    public static String TCP_SEND_BYTES_ACTION = "kr.co.kdone.airone.TCP_SEND_BYTES_ACTION";
    public static String TCP_RECEIVED_MESSAGE_ACTION = "kr.co.kdone.airone.TCP_RECEIVED_MESSAGE_ACTION";
    public static String TCP_RECEIVED_BYTES_ACTION = "kr.co.kdone.airone.TCP_RECEIVED_BYTES_ACTION";
    public static String TCP_FIRST_SEND_ACTION = "kr.co.kdone.airone.TCP_FIRST_SEND_ACTION";
    public static String TCP_GET_DID_ACTION = "kr.co.kdone.airone.TCP_GET_DID_ACTION";
    public static String TCP_GET_SD_ACTION = "kr.co.kdone.airone.TCP_GET_SD_ACTION";

    public static String getApIpAddr(Context context) {
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        DhcpInfo dhcpInfo = wifiManager.getDhcpInfo();

        byte[] ipAddress = convert2Bytes(dhcpInfo.serverAddress);

        try {
            String apIpAddr = InetAddress.getByAddress(ipAddress).getHostAddress();
            return apIpAddr;
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static byte[] convert2Bytes(int hostAddress) {
        byte[] addressBytes = {(byte) (0xff & hostAddress),
                (byte) (0xff & (hostAddress >> 8)),
                (byte) (0xff & (hostAddress >> 16)),
                (byte) (0xff & (hostAddress >> 24))};
        return addressBytes;
    }

    public static void initiateTwoButtonAlert(final Activity activity, String displayText,
                                              String positiveButtonText, String negativeButtonText) {
        new AlertDialog.Builder(activity)
                .setTitle(activity.getResources().getString(R.string.app_name))
                .setMessage(displayText)
                //.setIcon(R.drawable.alert_icon)
                .setPositiveButton(positiveButtonText, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        CleanVentilationApplication.getInstance().setAppFinishStart(true);

                        // aws mqtt 제어 종료 및 연결 종료
                        CleanVentilationApplication.getInstance().awsRemoteFinish();
                        CleanVentilationApplication.getInstance().awsMqttDisConnect();

                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                activity.finishAffinity();
                                System.exit(0);
                            }
                        }, 1000);

                    }
                })
                .setNegativeButton(negativeButtonText, null)
                .show();
    }

    /**
     * 로딩화면 시작 함수
     *
     * @param activity
     * @param str
     * @param str2
     */
    public static void displayProgress(final Activity activity, String str, String str2) {
        try {
            if (mConnectProgressDialog != null && IsRunningProgress()) {
                return;
            }

            mConnectProgressDialog = new CustomProgressDialog(activity, str, str2);
            mConnectProgressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    DismissConnectDialog();
                }
            });

            mConnectProgressDialog.setCancelable(false);
            if (mDialogTimer != null) {
                mDialogTimer.cancel();
                mDialogTimer.purge();
            }
            mDialogTimer = new Timer();
            mDialogTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    if (mConnectProgressDialog != null)
                        //mConnectProgressDialog.setCancelable(true);
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (mConnectProgressDialog != null) {
                                    mConnectProgressDialog.showTitle();
                                    mConnectProgressDialog.showSubTitle();
                                }
                            }
                        });
                }
            }, 3000);
            mConnectProgressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    DismissConnectDialog();
                }
            });
            mConnectProgressDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {

                @Override
                public void onDismiss(DialogInterface dialog) {
                    DismissConnectDialog();
                }
            });
            if (!activity.isFinishing()) {
                mConnectProgressDialog.show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 로딩화면 종료 함수
     */
    public static void DismissConnectDialog() {
        if (mDialogTimer != null) {
            mDialogTimer.cancel();
            mDialogTimer.purge();
        }
        if (mConnectProgressDialog != null) {
            try {
                mConnectProgressDialog.dismiss();
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            }
            mConnectProgressDialog = null;
        }
    }

    public static boolean IsRunningProgress() {
        return (mConnectProgressDialog != null && mConnectProgressDialog.isShowing());
    }

    /**
     * 로딩화면 타이틀 문구 표시
     *
     * @param str
     */
    public static void progressDialogShowTitle(String str) {
        if (mConnectProgressDialog != null) {
            mConnectProgressDialog.showTitle(str);
        }
    }

    /**
     * 로딩화면 서버타이틀 문구 표시
     *
     * @param str
     */
    public static void progressDialogShowSubTitle(String str) {
        if (mConnectProgressDialog != null) {
            mConnectProgressDialog.showSubTitle(str);
        }
    }

    public static int getDrawableIdToOutSensorLevel(int level) {
        switch (level) {
            case SENSOR_LEVEL_UNKNOWN:
                return R.drawable.circular_progress_bar_unknown;
            case SENSOR_LEVEL_GOOD:
                return R.drawable.circular_progress_bar_good;
            case SENSOR_LEVEL_NORMAL:
                return R.drawable.circular_progress_bar_normal;
            case SENSOR_LEVEL_BAD:
                return R.drawable.circular_progress_bar_bad;
            case SENSOR_LEVEL_VERY_BAD:
                return R.drawable.circular_progress_bar_very_bad;
        }
        return R.drawable.circular_progress_bar_unknown;
    }

    public static int getDrawableIdToSensorLevel(int level) {
        switch (level) {
            case SENSOR_LEVEL_UNKNOWN:
                return R.drawable.circular_in_progress_bar_unknown;
            case SENSOR_LEVEL_GOOD:
                return R.drawable.circular_in_progress_bar_good;
            case SENSOR_LEVEL_NORMAL:
                return R.drawable.circular_in_progress_bar_normal;
            case SENSOR_LEVEL_BAD:
                return R.drawable.circular_in_progress_bar_bad;
            case SENSOR_LEVEL_VERY_BAD:
                return R.drawable.circular_in_progress_bar_very_bad;
        }
        return R.drawable.circular_in_progress_bar_unknown;
    }

    /**
     * 센서 단계별 Color 선택 함수.
     *
     * @param level
     * @return
     */
    public static int getColorIdIdToSensorLevel(int level) {
        switch (level) {
            case SENSOR_LEVEL_UNKNOWN:
                return R.color.text_color_unknown;
            case SENSOR_LEVEL_GOOD:
                return R.color.text_color_good;
            case SENSOR_LEVEL_NORMAL:
                return R.color.text_color_normal;
            case SENSOR_LEVEL_BAD:
                return R.color.text_color_bad;
            case SENSOR_LEVEL_VERY_BAD:
                return R.color.text_color_very_bad;
        }
        return R.color.text_color_unknown;
    }

    /**
     * 센서 단계별 String 변환 함수.
     *
     * @param level
     * @return
     */
    public static int getStringIdToSensorLevelForProgressBar(int level) {
        switch (level) {
            case SENSOR_LEVEL_UNKNOWN:
                return R.string.unknown2line;
            case SENSOR_LEVEL_GOOD:
                return R.string.good;
            case SENSOR_LEVEL_NORMAL:
                return R.string.normal;
            case SENSOR_LEVEL_BAD:
                return R.string.bad;
            case SENSOR_LEVEL_VERY_BAD:
                return R.string.very_bad_2line;
        }
        return R.string.unknown2line;
    }

    public static int getStringIdToSensorLevel(int level) {
        switch (level) {
            case SENSOR_LEVEL_UNKNOWN:
                return R.string.unknown;
            case SENSOR_LEVEL_GOOD:
                return R.string.good;
            case SENSOR_LEVEL_NORMAL:
                return R.string.normal;
            case SENSOR_LEVEL_BAD:
                return R.string.bad;
            case SENSOR_LEVEL_VERY_BAD:
                return R.string.very_bad;
        }
        return R.string.unknown;
    }

    public static int getStringIdToSenerLevel(int level) {
        switch (level) {
            case SENSOR_LEVEL_UNKNOWN:
                return R.string.unknown;
            case SENSOR_LEVEL_GOOD:
                return R.string.good;
            case SENSOR_LEVEL_NORMAL:
                return R.string.normal;
            case SENSOR_LEVEL_BAD:
                return R.string.bad;
            case SENSOR_LEVEL_VERY_BAD:
                return R.string.very_bad;
        }
        return R.string.unknown;
    }

    public static int getIconIdToSensorLevel(int level) {
        switch (level) {
            case SENSOR_LEVEL_UNKNOWN:
                return R.drawable.icon_life_report_sensor_unkown;
            case SENSOR_LEVEL_GOOD:
                return R.drawable.icon_life_report_sensor_good;
            case SENSOR_LEVEL_NORMAL:
                return R.drawable.icon_life_report_sensor_normal;
            case SENSOR_LEVEL_BAD:
                return R.drawable.icon_life_report_sensor_bad;
            case SENSOR_LEVEL_VERY_BAD:
                return R.drawable.icon_life_report_sensor_very_bed;
        }
        return R.drawable.icon_life_report_sensor_unkown;
    }

    public static int getIconIdToTotalLevel(int level) {
        switch (level) {
            case SENSOR_LEVEL_UNKNOWN:
                return R.drawable.icon_widget_total_circle;
            case SENSOR_LEVEL_GOOD:
                return R.drawable.icon_widget_total_circle_good;
            case SENSOR_LEVEL_NORMAL:
                return R.drawable.icon_widget_total_circle_normal;
            case SENSOR_LEVEL_BAD:
                return R.drawable.icon_widget_total_circle_bad;
            case SENSOR_LEVEL_VERY_BAD:
                return R.drawable.icon_widget_total_circle_very_bad;
        }
        return R.drawable.icon_widget_total_circle;
    }

    /**
     * TAC 모델 체크 함수.
     *
     * @param modelCD
     * @return
     */
    public static boolean isTacModel(int modelCD) {
        if (modelCD == MODEL_CODE_TAC750_20S ||
                modelCD == MODEL_CODE_TAC710_20S ||
                modelCD == MODEL_CODE_TAC730_20S ||
                modelCD == MODEL_CODE_TAC740_20S ||
                modelCD == MODEL_CODE_TAC550_20S ||
                modelCD == MODEL_CODE_TAC510_20S ||
                modelCD == MODEL_CODE_TAC530_20S ||
                modelCD == MODEL_CODE_TAC500_20 ||
                modelCD == MODEL_CODE_TAC500_20S) {
            return true;
        }
        return false;
    }

    public static int getStringIDWeather(double rainfall, double snowfall) {
        if (rainfall > 0 && snowfall > 0) {
            return R.string.rain_snow;
        } else if (rainfall > 0) {
            return R.string.rain;
        } else if (snowfall > 0) {
            return R.string.snow;
        }
        return R.string.sunny;
    }

    /**
     * 날씨 정보 String 변환 함수.
     *
     * @param type
     * @return
     */
    public static int getStringIDWeather(final int type) {
        switch (type) {
            case 1:
                return R.string.weather_type_1;
            case 2:
                return R.string.weather_type_2;
            case 3:
                return R.string.weather_type_3;
            case 4:
                return R.string.weather_type_4;
            case 5:
                return R.string.weather_type_5;
            case 6:
                return R.string.weather_type_6;
            case 7:
                return R.string.weather_type_7;
            case 8:
                return R.string.weather_type_8;
            case 9:
                return R.string.weather_type_9;
            case 10:
                return R.string.weather_type_10;
            case 11:
                return R.string.weather_type_11;
            case 12:
                return R.string.weather_type_12;
            case 13:
                return R.string.weather_type_13;
            case 14:
                return R.string.weather_type_14;
            case 15:
                return R.string.weather_type_15;
            case 16:
                return R.string.weather_type_16;
            case 17:
                return R.string.weather_type_17;
            case 18:
                return R.string.weather_type_18;
            case 19:
                return R.string.weather_type_19;
            case 20:
                return R.string.weather_type_20;
            case 21:
                return R.string.weather_type_21;
            case 22:
                return R.string.weather_type_22;
            case 23:
                return R.string.weather_type_23;
            case 24:
                return R.string.weather_type_24;
            case 25:
                return R.string.weather_type_25;
            case 26:
                return R.string.weather_type_26;
            case 27:
                return R.string.weather_type_27;
            case 28:
                return R.string.weather_type_28;
            case 29:
                return R.string.weather_type_29;
            case 30:
                return R.string.weather_type_30;
            case 31:
                return R.string.weather_type_31;
            case 32:
                return R.string.weather_type_32;
            case 33:
                return R.string.weather_type_33;
            case 34:
                return R.string.weather_type_34;
            case 35:
                return R.string.weather_type_35;
            case 36:
                return R.string.weather_type_36;
            case 37:
                return R.string.weather_type_37;
            case 38:
                return R.string.weather_type_38;
            case 39:
                return R.string.weather_type_39;
            case 40:
                return R.string.weather_type_40;
        }
        return R.string.weather_type_1;
    }

    public static int getIconIDWeather(double rainfall, double snowfall) {
        if (rainfall > 0 && snowfall > 0) {
            return R.drawable.free_icon_main_weather05;
        } else if (rainfall > 0) {
            return R.drawable.free_icon_main_weather02;
        } else if (snowfall > 0) {
            return R.drawable.free_icon_main_weather05;
        }
        return R.drawable.free_icon_main_weather01;
    }

    /**
     * 날씨 아이콘 획득 함수
     *
     * @param type
     * @return
     */
    public static int getIconIDWeather(final int type) {
        switch (type) {
            //맑음
            case 1:
                return R.drawable.free_icon_main_weather01;
            //비
            case 7:
            case 8:
            case 9:
            case 10:
            case 11:
            case 12:
            case 13:
            case 14:
            case 15:
            case 16:
            case 17:
                return R.drawable.free_icon_main_weather02;
            //구름 적음
            case 2:
            case 5:
            case 6:
                return R.drawable.free_icon_main_weather03;
            //구름많음
            case 3:
            case 4:
            case 40:
                return R.drawable.free_icon_main_weather04;
            //눈
            case 18:
            case 19:
            case 20:
            case 21:
            case 22:
            case 23:
            case 24:
            case 25:
                return R.drawable.free_icon_main_weather05;
            //눈 또는 비
            case 26:
            case 27:
            case 28:
            case 29:
            case 30:
            case 31:
            case 32:
            case 33:
            case 34:
            case 35:
            case 36:
            case 37:
            case 38:
                return R.drawable.free_icon_main_weather07;
            //천둥, 안개
            case 39:
                return R.drawable.free_icon_main_weather06;
        }
        return R.drawable.free_icon_main_weather01;
    }

    public static void setDisableViewFew(final View v) {
        v.setEnabled(false);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                v.setEnabled(true);
            }
        }, 500);
    }

    /**
     * 프로그래스바 애니메이션
     *
     * @param pb
     * @param progressTo
     */
    public static void setProgressAnimate(ProgressBar pb, int progressTo) {
        ObjectAnimator animation = ObjectAnimator.ofInt(pb, "progress", pb.getProgress(), progressTo);
        animation.setDuration(500);
        animation.setInterpolator(new DecelerateInterpolator());
        animation.start();
    }

    /**
     * 원형 프로그래스바 애니메이션 함수.
     *
     * @param c
     * @param type
     * @param pb
     * @param t
     * @param level
     */
    public static void setProgressAnimate(final Context c, final int type, final ProgressBar pb, final TextView t, final int level) {
        final int PROGRESSBAR_OUT = 0;
        final int PROGRESSBAR_IN = 1;

        pb.setVisibility(View.VISIBLE);
        t.setText(getStringIdToSensorLevelForProgressBar(level));
        t.setTextColor(c.getResources().getColor(getColorIdIdToSensorLevel(level)));
        if (type == PROGRESSBAR_IN) {
            pb.setBackground(c.getResources().getDrawable(R.drawable.circle_in_shape));
            pb.setProgressDrawable(c.getResources().getDrawable(getDrawableIdToSensorLevel(level)));
        } else {
            pb.setBackground(c.getResources().getDrawable(R.drawable.circle_shape));
            pb.setProgressDrawable(c.getResources().getDrawable(getDrawableIdToOutSensorLevel(level)));
        }

        // ikHwang 2019-05-15 오후 7:16 프로그래스 애니메이션 제거
        /*ValueAnimator valueAnimator = ValueAnimator.ofInt(0, pb.getProgress() != 0 ? 360 : 0);
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                pb.setProgress((int) animation.getAnimatedValue());
//                pb.setProgress(pb.getMax());
            }
        });
        valueAnimator.setInterpolator(new DecelerateInterpolator());
        valueAnimator.setDuration(500);
        valueAnimator.start();*/

        pb.setProgress(pb.getMax());
    }

    /**
     * 세로바 그래프 애니메이션 시작 함수.
     *
     * @param c
     * @param v
     * @param t
     * @param level
     * @param step_height
     * @param durationTime
     */
    public static void setFrameLayoutAnimate(final Context c, final View v, final TextView t, final int level, final int step_height, final int durationTime) {
        int prevHeight = v.getHeight();
        v.setVisibility(View.VISIBLE);
        ValueAnimator valueAnimator = ValueAnimator.ofInt(prevHeight, step_height * level);
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                v.getLayoutParams().height = (int) animation.getAnimatedValue();
                int current_level = 0;
                if (v.getLayoutParams().height == 0) {
                    current_level = 0;
                } else if (v.getLayoutParams().height <= step_height * 1) {
                    current_level = 1;
                } else if (v.getLayoutParams().height <= step_height * 2) {
                    current_level = 2;
                } else if (v.getLayoutParams().height <= step_height * 3) {
                    current_level = 3;
                } else {
                    current_level = 4;
                }
                t.setText(getStringIdToSenerLevel(current_level));
                t.setTextColor(c.getResources().getColor(getColorIdIdToSensorLevel(current_level)));
                v.setBackgroundColor(c.getResources().getColor(getColorIdIdToSensorLevel(current_level)));
                v.requestLayout();
            }
        });
        valueAnimator.setInterpolator(new DecelerateInterpolator());
        valueAnimator.setDuration(durationTime);
        valueAnimator.start();
    }

    //Hide Keypad
    public static void setupUI(final Activity activity, View view) {
        //Set up touch listener for non-text box views to hide keyboard.
        if (!(view instanceof EditText)) {
            view.setOnTouchListener(new View.OnTouchListener() {
                public boolean onTouch(View v, MotionEvent event) {
                    hideSoftKeyboard(activity);
                    return false;
                }
            });
        }

        //If a layout container, iterate over children and seed recursion.
        if (view instanceof ViewGroup) {

            for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {

                View innerView = ((ViewGroup) view).getChildAt(i);

                setupUI(activity, innerView);
            }
        }
    }
    public static float dpToPx(float dp) {
        return dp * Resources.getSystem().getDisplayMetrics().density;
    }

    public static float pxToDp(float px) {
        return px / Resources.getSystem().getDisplayMetrics().density;
    }


    public static int[] hexStringToBArray(String s) {
        int len = s.length();
        int[] data = new int[len / 2];

        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4) + Character.digit(s.charAt(i + 1), 16));
        }

        return data;
    }

    final protected static char[] hexArray = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};

    public static String byteArrayToHexString(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        int v;

        for (int j = 0; j < bytes.length; j++) {
            v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }

        return new String(hexChars);
    }

    // 네트워크 상태 체크
    public static boolean checkNetwork(Activity activity) {
        WifiInfo wifiInfo = ((WifiManager) activity.getApplicationContext().getSystemService(Context.WIFI_SERVICE)).getConnectionInfo();
        ConnectivityManager cm = (ConnectivityManager) activity.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        if (activeNetwork == null) {
            return false;
        }

        boolean isWifi = activeNetwork.getType() == ConnectivityManager.TYPE_WIFI;
        boolean isMobile = activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE;
        boolean isConnected = activeNetwork.isConnectedOrConnecting() && (isMobile || (isWifi && !wifiInfo.getSSID().toUpperCase().contains(WifiUtils.SSID_VALID_ROOMCON) && !wifiInfo.getSSID().toUpperCase().contains(WifiUtils.SSID_VALID_MONITOR)));
        return isConnected;
    }

    public static void isUserInfoSection(Activity act) {
        String id = CleanVentilationApplication.getInstance().getUserInfo().getId();
        if (id == null || id.equals("")) {
            CleanVentilationApplication.getInstance().awsMqttDisConnect();
            Intent intent = new Intent(act, SplashActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            act.startActivity(intent);

        }
    }

    /**
     * 이메일 형식 유효성 체크 함수.
     *
     * @param email
     * @return
     */
    public static boolean isEmailValid(String email) {
        String expression = "^[\\w\\.-]+@([\\w\\-]+\\.)+[\\w\\-]+$";
        Pattern pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }

    /**
     * 암호 설정을 위한 유효성 체크 함수
     *
     * @param str
     * @return
     */
    public static boolean isNumAndStringValid(String str) {
        int countEng = 0; // 영어 갯수
        int countNum = 0; // 숫자 갯수

        if (TextUtils.isEmpty(str)) return false;

        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);

            if ((0x61 <= c && c <= 0x7A) || (0x41 <= c && c <= 0x5A)) {
                countEng++;
            } else if (0x30 <= c && c <= 0x39) {
                countNum++;
            }
        }

        if (countEng > 0 && countNum > 0) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 전화번호 형식 유효성 체크 함수
     *
     * @param s
     * @return
     */
    public static boolean isNumValid(String s) {
        if (s.matches("01[016789][0-9]{7,8}")) {
            return true;
        }
        return false;
    }

    /**
     * 이모티콘 필터
     */
    private static InputFilter specialCharacterFilter = new InputFilter() {
        public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
            for (int i = start; i < end; i++) { // 이모티콘 패턴
                int type = Character.getType(source.charAt(i));
                Pattern unicodeOutliers = Pattern.compile("[\\uD83C-\\uDBFF\\uDC00-\\uDFFF]+");
                if (type == Character.SURROGATE || type == Character.OTHER_SYMBOL || unicodeOutliers.matcher(source).matches()) {
                    return "";
                }

                Pattern space = Pattern.compile("^[\\S]*$");
                if (!space.matcher(source).matches()) {
                    return "";
                }
            }
            return null;
        }
    };

    /**
     * ID 필터
     */
    public static InputFilter IDCharacterFilter = new InputFilter() {
        public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
            Pattern ps = Pattern.compile("[a-zA-Z0-9]*$");
//            Pattern ps = Pattern.compile("^[0-9a-zA-Z-ㅣ`~!@#$%^&*()\\\\-_=+\\\\[{\\\\]}|₩;:‘\\“‘”“’”,<.>/?\\\\s]*$");

            boolean keepOriginal = true;
            StringBuilder sb = new StringBuilder(end - start);
            for (int i = start; i < end; i++) {
                char c = source.charAt(i);
                if (ps.matcher(String.valueOf(c)).matches()) // put your condition here
                    sb.append(c);
                else
                    keepOriginal = false;
            }

            if (keepOriginal)
                return null;
            else {
                if (source instanceof Spanned) {
                    SpannableString sp = new SpannableString(sb);
                    TextUtils.copySpansFrom((Spanned) source, start, sb.length(), null, sp, 0);
                    return sp;
                } else {
                    return sb;
                }
            }
        }
    };


    /**
     * 이름 필터
     */
    public static InputFilter nameCharacterFilter = new InputFilter() {
        public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
            Pattern ps = Pattern.compile("[ㄱ-ㅣ가-힣a-zA-Z_|\u318D\u119E\u11A2\u2022\u2025a\u00B7\uFE55]*$");

            boolean keepOriginal = true;
            StringBuilder sb = new StringBuilder(end - start);
            for (int i = start; i < end; i++) {
                char c = source.charAt(i);
                if (ps.matcher(String.valueOf(c)).matches()) // put your condition here
                    sb.append(c);
                else
                    keepOriginal = false;
            }
            if (keepOriginal)
                return null;
            else {
                if (source instanceof Spanned) {
                    SpannableString sp = new SpannableString(sb);
                    TextUtils.copySpansFrom((Spanned) source, start, sb.length(), null, sp, 0);
                    return sp;
                } else {
                    return sb;
                }
            }
        }
    };



    public static void showKeyboard(EditText editText) {
        InputMethodManager inputMethodManager =
                (InputMethodManager) editText.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
    }

    public static void setEditText(View view) {
        if (view instanceof EditText) {
            EditText edit = (EditText) view;
            edit.setFilters(new InputFilter[]{specialCharacterFilter});
        }

        //If a layout container, iterate over children and seed recursion.
        if (view instanceof ViewGroup) {
            for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
                View innerView = ((ViewGroup) view).getChildAt(i);
                setEditText(innerView);
            }
        }
    }

    /**
     * 소프트 키보드 숨김 함수
     *
     * @param activity
     */
    public static void hideSoftKeyboard(Activity activity) {
        if (activity != null && activity.getCurrentFocus() != null && activity.getCurrentFocus().getWindowToken() != null) {
            InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
        }
    }

    /**
     * CRC 체크 함수.
     *
     * @param data
     * @param dataLength
     * @return
     */
    public static int updateCRC(byte[] data, int dataLength) {
        int[] CRC_TABLE = {
                0x0000, 0x8005, 0x800F, 0x000A, 0x801B, 0x001E, 0x0014, 0x8011,
                0x8033, 0x0036, 0x003C, 0x8039, 0x0028, 0x802D, 0x8027,
                0x0022, 0x8063, 0x0066, 0x006C, 0x8069, 0x0078, 0x807D,
                0x8077, 0x0072, 0x0050, 0x8055, 0x805F, 0x005A, 0x804B,
                0x004E, 0x0044, 0x8041, 0x80C3, 0x00C6, 0x00CC, 0x80C9,
                0x00D8, 0x80DD, 0x80D7, 0x00D2, 0x00F0, 0x80F5, 0x80FF,
                0x00FA, 0x80EB, 0x00EE, 0x00E4, 0x80E1, 0x00A0, 0x80A5,
                0x80AF, 0x00AA, 0x80BB, 0x00BE, 0x00B4, 0x80B1, 0x8093,
                0x0096, 0x009C, 0x8099, 0x0088, 0x808D, 0x8087, 0x0082,
                0x8183, 0x0186, 0x018C, 0x8189, 0x0198, 0x819D, 0x8197,
                0x0192, 0x01B0, 0x81B5, 0x81BF, 0x01BA, 0x81AB, 0x01AE,
                0x01A4, 0x81A1, 0x01E0, 0x81E5, 0x81EF, 0x01EA, 0x81FB,
                0x01FE, 0x01F4, 0x81F1, 0x81D3, 0x01D6, 0x01DC, 0x81D9,
                0x01C8, 0x81CD, 0x81C7, 0x01C2, 0x0140, 0x8145, 0x814F,
                0x014A, 0x815B, 0x015E, 0x0154, 0x8151, 0x8173, 0x0176,
                0x017C, 0x8179, 0x0168, 0x816D, 0x8167, 0x0162, 0x8123,
                0x0126, 0x012C, 0x8129, 0x0138, 0x813D, 0x8137, 0x0132,
                0x0110, 0x8115, 0x811F, 0x011A, 0x810B, 0x010E, 0x0104,
                0x8101, 0x8303, 0x0306, 0x030C, 0x8309, 0x0318, 0x831D,
                0x8317, 0x0312, 0x0330, 0x8335, 0x833F, 0x033A, 0x832B,
                0x032E, 0x0324, 0x8321, 0x0360, 0x8365, 0x836F, 0x036A,
                0x837B, 0x037E, 0x0374, 0x8371, 0x8353, 0x0356, 0x035C,
                0x8359, 0x0348, 0x834D, 0x8347, 0x0342, 0x03C0, 0x83C5,
                0x83CF, 0x03CA, 0x83DB, 0x03DE, 0x03D4, 0x83D1, 0x83F3,
                0x03F6, 0x03FC, 0x83F9, 0x03E8, 0x83ED, 0x83E7, 0x03E2,
                0x83A3, 0x03A6, 0x03AC, 0x83A9, 0x03B8, 0x83BD, 0x83B7,
                0x03B2, 0x0390, 0x8395, 0x839F, 0x039A, 0x838B, 0x038E,
                0x0384, 0x8381, 0x0280, 0x8285, 0x828F, 0x028A, 0x829B,
                0x029E, 0x0294, 0x8291, 0x82B3, 0x02B6, 0x02BC, 0x82B9,
                0x02A8, 0x82AD, 0x82A7, 0x02A2, 0x82E3, 0x02E6, 0x02EC,
                0x82E9, 0x02F8, 0x82FD, 0x82F7, 0x02F2, 0x02D0, 0x82D5,
                0x82DF, 0x02DA, 0x82CB, 0x02CE, 0x02C4, 0x82C1, 0x8243,
                0x0246, 0x024C, 0x8249, 0x0258, 0x825D, 0x8257, 0x0252,
                0x0270, 0x8275, 0x827F, 0x027A, 0x826B, 0x026E, 0x0264,
                0x8261, 0x0220, 0x8225, 0x822F, 0x022A, 0x823B, 0x023E,
                0x0234, 0x8231, 0x8213, 0x0216, 0x021C, 0x8219, 0x0208,
                0x820D, 0x8207, 0x0202
        };

        int i, j;
        int crc_accum = 0;
        for (j = 0; j < dataLength; j++) {
            i = (int) ((crc_accum >> 8) ^ data[j]) & 0xff;
            crc_accum = (int) ((crc_accum << 8) ^ CRC_TABLE[i]);
        }
        return crc_accum;
    }

    /**
     * ikHwang 2019-05-15 오전 10:28
     * 로그 공통 처리 함수
     * @param cls
     * @param message
     * @param type
     */
    public static void customLog(String cls, String message, int type) {
        if (TextUtils.isEmpty(cls) || TextUtils.isEmpty(message))
            return;

        try {
            if (DEBUG) {
                cls += " [" + new Throwable().getStackTrace()[1].getLineNumber() + "]";

                switch (type) {
                    case Log.ERROR:
                        Log.e(cls, message);
                        break;

                    case Log.DEBUG:
                        Log.d(cls, message);
                        break;

                    case Log.INFO:
                        Log.i(cls, message);
                        break;

                    case Log.VERBOSE:
                        Log.v(cls, message);
                        break;

                    case Log.WARN:
                        Log.w(cls, message);
                        break;

                    case Log.ASSERT:
                        Log.wtf(cls, message);
                        break;

                    default:
                        break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * ikHwang 2019-06-13 오전 9:15 액티비티 화면 전환시 애니메니션 추가
     * @param act
     * @param intent
     * @param isFinish
     * @param aniType 0-우측에서 좌측 in,
     */
    public static void startActivityAni(Activity act, Intent intent, boolean isFinish, int aniType){
        act.startActivity(intent);
        if(isFinish) act.finish();

        if(1 == aniType) act.overridePendingTransition(R.anim.slide_down_in, R.anim.slide_none);
        else if(2 == aniType) act.overridePendingTransition(R.anim.slide_right_in, R.anim.slide_none);
        else act.overridePendingTransition(R.anim.slide_left_in, R.anim.slide_none);
    }

    /**
     * ikHwang 2019-06-17 오후 5:56 메인화면 센서 정보 색상 설정 (프리즘 모델)
     * @param level
     * @return
     */
    public static int getDrawableIdToMainSensorLevel(int level) {
        switch (level) {
            case SENSOR_LEVEL_UNKNOWN:
                return R.drawable.icon_main_circle;
            case SENSOR_LEVEL_GOOD:
                return R.drawable.icon_main_circle_good;
            case SENSOR_LEVEL_NORMAL:
                return R.drawable.icon_main_circle_normal;
            case SENSOR_LEVEL_BAD:
                return R.drawable.icon_main_circle_bad;
            case SENSOR_LEVEL_VERY_BAD:
                return R.drawable.icon_main_circle_very_bad;
        }
        return R.drawable.icon_main_circle;
    }

    /**
     * ikHwang 2019-07-25 오후 3:20 메인화면 통합 공기질 정보
     * @param level
     * @return
     */
    public static int getDrawableIdToMainTotalSensorLevel(int level) {
        switch (level) {
            case SENSOR_LEVEL_UNKNOWN:
                return R.drawable.total_disable;
            case SENSOR_LEVEL_GOOD:
                return R.drawable.animation_blue;
            case SENSOR_LEVEL_NORMAL:
                return R.drawable.animation_green;
            case SENSOR_LEVEL_BAD:
                return R.drawable.animation_orange;
            case SENSOR_LEVEL_VERY_BAD:
                return R.drawable.animation_red;
        }
        return R.drawable.total_disable;
    }

    /**
     * ikHwang 2019-06-18 오전 11:40 실외 센서 정보 색상
     * @param level
     * @return
     */
    public static int getDrawableIdToMainSensorLevel_Small(int level) {
        switch (level) {
            case SENSOR_LEVEL_UNKNOWN:
                return R.drawable.icon_main_circle_small;
            case SENSOR_LEVEL_GOOD:
                return R.drawable.icon_main_circle_good_small;
            case SENSOR_LEVEL_NORMAL:
                return R.drawable.icon_main_circle_normal_small;
            case SENSOR_LEVEL_BAD:
                return R.drawable.icon_main_circle_bad_small;
            case SENSOR_LEVEL_VERY_BAD:
                return R.drawable.icon_main_circle_very_bad_small;
        }
        return R.drawable.icon_main_circle;
    }

    /**
     * ikHwang 2019-06-25 오후 5:07 공통 토스트
     * @param act
     * @param msg
     */
    public static void showToast(final Activity act, final String msg){
        act.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                View toastView = act.getLayoutInflater().inflate(R.layout.toast_layout, null);

                TextView text = toastView.findViewById(R.id.txtToast);
                text.setText(msg);

                Toast toast = new Toast(act);
                toast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM, 0, 0);
                toast.setDuration(Toast.LENGTH_SHORT);
                toast.setView(toastView);
                toast.show();
            }
        });
    }

    public static byte[] intToByteArray(final int integer) {
        byte result[] = new byte[1];

        ByteBuffer buff = ByteBuffer.allocate(Integer.SIZE / 8);
        buff.putInt(integer);
        buff.order(ByteOrder.BIG_ENDIAN);
        byte[] nValue = buff.array();
        result[0] = nValue[3];

        return result;
    }

    /**
     * ikHwang 2019-10-14 오후 3:18 청정환기 버전에 따른 HEADER 버전값 리턴
     * @param pcd
     * @return
     */
    public static int getHeaderPcd(int pcd) {
        if(5 == pcd){ // 프리즘
            return ProtocolType.PROTOCOL_VER_ID4;
        }else if(7 == pcd){ // 프리즘 제습 청정
//            return ProtocolType.PROTOCOL_VER_ID4;
            return ProtocolType.PROTOCOL_VER_ID5;
        }

        return ProtocolType.PROTOCOL_VER_ID1; // 1차, 2차 (듀얼링크)
    }

    public static void sendFirebaseLog(Context context, String smg){
        try {
            FirebaseAnalytics.getInstance(context).logEvent(smg, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
