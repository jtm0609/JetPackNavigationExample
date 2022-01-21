package kr.co.kdone.airone.utils;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import java.util.UUID;

/**
 * Created by LIM on 2017-10-30.
 */

public class SharedPrefUtil {
    private static SharedPreferences mPreferences;
    private final static String PREF_NAME = "kr.co.kdone.airone";
    public final static String DONT_SHOW_SMARTGUIDE = "dont_show_smartguide";
    public final static String AUTO_LOGIN = "auto_login";
    public final static String IS_LOGIN = "is_login";
    public final static String USER_ID = "user_id";
    public final static String U_ID = "u_id"; //V2A 적용.
    public final static String USER_PASS = "user_pass";
    public final static String AUTH_CODE = "auth_code";
    public final static String HOMEDATA_JSON = "homedata_json";
    public final static String HOMEDATA_UPDATA_TIME = "homedate_updata_time";
    public final static String AUTH_CODE_CREATED_TIME = "auth_code_created_time";
    public final static String AUTH_CODE_CREATED_USER = "auth_code_created_user";
    public final static String CURRENT_CONTROL_DEVICE_ID = "current_control_device_id";
    public final static String CURRENT_CONTROL_DEVICE_NAME = "current_control_device_name";
    public final static String CURRENT_CONTROL_DEVICE_PRE_NAME = "current_control_device_pre_name";
    public final static String GRAPH_START_POSITION = "graph_start_position";
    public final static String HAS_ROOM_CON = "has_room_con";
    public final static String WIDGET_UPDATE_TIME = "widget_update_time";
    public final static String WIDGET_LAST_UPDATE_TIME = "widget_last_update_time";
    public final static String FCMKEY = "fcm_device_key";
    public final static String TOKEN = "refreshToken";
    public final static String AUTHEXPIRED = "authoExpired";
    public final static String STR_UUID = "str_uuid";
    public final static String ROOM_CONTROLLER_ID = "roomController_id";


    // ikHwang 2019-05-15 오후 2:36 에어모니터 GID
    public final static String CURRENT_AIRMONITORL_DEVICE_ID = "current_airmonitor_device_id";

    // ikHwang 2019-05-22 오후 3:21 메인화면 자동갱신 시간 정보 저장
    public final static String SAVE_MAIN_INTERVAL = "save_main_interval";

    public final static boolean DEFAULT_DONT_SHOW_SMARTGUIDE = false;
    public final static boolean DEFAULT_AUTO_LOGIN = false;
    public final static boolean DEFAULT_IS_LOGIN = false;
    public final static String DEFAULT_USER_ID = "";
    public final static String DEFAULT_USER_PASS = "";
    public final static String DEFAULT_HOMEDATA_JSON = "";
    public final static long DEFAULT_HOMEDATA_UPDATA_TIME = 0;
    public final static String DEFAULT_AUTH_CODE = "";
    public final static long DEFAULT_AUTH_CODE_CREATED_TIME = -1;
    public final static String DEFAULT_AUTH_CODE_CREATED_USER = "";
    public final static String DEFAULT_CURRENT_CONTROL_DEVICE_ID = "";
    public final static String DEFAULT_CURRENT_CONTROL_DEVICE_NAME = "";
    public final static String DEFAULT_GRAPH_START_POSITION = "0,0,0,0,0,0,0,0,0,0";

    public static void init(Context context) {
        if (mPreferences == null) {
            //mPreferences = context.getSharedPreferences(PREF_NAME, Activity.MODE_PRIVATE);
            mPreferences = context.getSharedPreferences(PREF_NAME,  Activity.MODE_MULTI_PROCESS);

        }
    }

    public static void reset() {
        if (mPreferences == null) return;
        putBoolean(DONT_SHOW_SMARTGUIDE, SharedPrefUtil.DEFAULT_DONT_SHOW_SMARTGUIDE);
        putBoolean(AUTO_LOGIN, SharedPrefUtil.DEFAULT_AUTO_LOGIN);
        putBoolean(IS_LOGIN, SharedPrefUtil.DEFAULT_IS_LOGIN);
        putString(USER_ID,DEFAULT_USER_ID);
        putString(USER_PASS,DEFAULT_USER_PASS);
        putString(HOMEDATA_JSON,DEFAULT_HOMEDATA_JSON);
        putLong(HOMEDATA_UPDATA_TIME,DEFAULT_HOMEDATA_UPDATA_TIME);
        putString(AUTH_CODE,DEFAULT_AUTH_CODE);
        putLong(AUTH_CODE_CREATED_TIME,DEFAULT_AUTH_CODE_CREATED_TIME);
        putString(AUTH_CODE_CREATED_USER,DEFAULT_AUTH_CODE_CREATED_USER);
        putString(CURRENT_CONTROL_DEVICE_ID,DEFAULT_CURRENT_CONTROL_DEVICE_ID);
        putString(CURRENT_CONTROL_DEVICE_NAME,DEFAULT_CURRENT_CONTROL_DEVICE_NAME);
        putString(GRAPH_START_POSITION,DEFAULT_GRAPH_START_POSITION);
    }

    public static boolean getBoolean(String key, boolean defaultValue) {
        if (mPreferences == null) return defaultValue;
        return mPreferences.getBoolean(key, defaultValue);
    }

    public static void putBoolean(String key, boolean Value) {
        if (mPreferences == null) return;
        SharedPreferences.Editor mEditer = mPreferences.edit();
        mEditer.putBoolean(key, Value);
        mEditer.commit();
    }

    public static String getString(String key, String defaultValue) {
        if (mPreferences == null) return defaultValue;
        if(useKeyStore(key)){
            return KeyStoreClass.decryptString(mPreferences.getString(key, defaultValue));
        }
        return mPreferences.getString(key, defaultValue);
    }

    public static void putString(String key, String value) {
        if (mPreferences == null) return;

        if(useKeyStore(key)){
            value = KeyStoreClass.encryptString(value);
        }

        SharedPreferences.Editor mEditer = mPreferences.edit();
        mEditer.putString(key, value);
        mEditer.commit();
    }

    public static int getInt(String key, int defaultValue) {
        if (mPreferences == null) return defaultValue;
        return mPreferences.getInt(key, defaultValue);
    }

    public static void putInt(String key, int value) {
        if (mPreferences == null) return;
        SharedPreferences.Editor mEditer = mPreferences.edit();
        mEditer.putInt(key, value);
        mEditer.commit();
    }

    public static long getLong(String key, long defaultValue) {
        if (mPreferences == null) return defaultValue;
        return mPreferences.getLong(key, defaultValue);
    }

    public static void putLong(String key, long value) {
        if (mPreferences == null) return;
        SharedPreferences.Editor mEditer = mPreferences.edit();
        mEditer.putLong(key, value);
        mEditer.commit();
    }

    public static float getFloat(String key, float defaultValue) {
        if (mPreferences == null) return defaultValue;
        return mPreferences.getFloat(key, defaultValue);
    }

    public static void putFloat(String key, float value) {
        if (mPreferences == null) return;
        SharedPreferences.Editor mEditer = mPreferences.edit();
        mEditer.putFloat(key, value);
        mEditer.commit();
    }

    public static boolean containsKey(String key) {
        return mPreferences.contains(key);
    }

    /**
     * 암호화 사용하여 저장해야 하는 경우.
     * @param key
     * @return
     */
    private static boolean useKeyStore(String key){
        if(key == USER_ID || key == USER_PASS){
            return true;
        }
        return false;
    }



}
