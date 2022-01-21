package kr.co.kdone.airone;

import android.app.Application;
import android.content.Context;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;

import com.google.firebase.crashlytics.FirebaseCrashlytics;

import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.UUID;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.multidex.MultiDex;
import kr.co.kdone.airone.aws.AWSIoTMqttHelper;
import kr.co.kdone.airone.aws.DataChangeListener;
import kr.co.kdone.airone.aws.PubSubData;
import kr.co.kdone.airone.data.AuthData;
import kr.co.kdone.airone.data.DeviceManager;
import kr.co.kdone.airone.data.UserInfo;
import kr.co.kdone.airone.data.home.CommonHomeInfo;
import kr.co.kdone.airone.utils.CommonUtils;
import kr.co.kdone.airone.utils.HttpApi;
import kr.co.kdone.airone.utils.KeyStoreClass;
import kr.co.kdone.airone.utils.ProtocolType;
import kr.co.kdone.airone.utils.SharedPrefUtil;

import static kr.co.kdone.airone.aws.AWSIoTMqttHelper.MQTT_CONNECTION_TIMEOUT;
import static kr.co.kdone.airone.utils.ProtocolType.COMMAND_DID;
import static kr.co.kdone.airone.utils.ProtocolType.COMMAND_LED_STATUS_INFO;
import static kr.co.kdone.airone.utils.ProtocolType.COMMAND_SD;
import static kr.co.kdone.airone.utils.ProtocolType.OPERATION_STATUS_OFF;
import static kr.co.kdone.airone.utils.ProtocolType.OPERATION_STATUS_ON;

/**
 * Created by LIM on 2017-10-30.
 */

public class CleanVentilationApplication extends Application {
    public static final String TAG = CleanVentilationApplication.class.getSimpleName();
    public static Context mContext;
    private static CleanVentilationApplication instance;
    private static UserInfo mUserInfo;

    // ikHwang 2019-05-21 오전 9:27 메인화면 구성 데이터
    private static ArrayList<CommonHomeInfo> mHomeList;

    public static final int STATE_ROOM_CONTROL_NO_REGISTER          = 0;    // 룸콘 미등록 상태
    public static final int STATE_ROOM_CONTROL_NOT_CONNECTED        = 1;    // 룸콘 등록했으나 미연결 상태
    public static final int STATE_AIR_MONITOR_NO_REGISTER           = 2;    // 에어 모니터 미등록 상태
    public static final int STATE_AIR_MONITOR_NOT_CONNECTED         = 3;    // 에어 모니터 등록 했으나 미연결 상태
    public static final int STATE_AIR_MONITOR_UNKNOWN_INDISEINFO    = 4;    // 에어모니터 연결되어 있으나 실내 정보 없는 상태
    public static final int STATE_AIR_MONITOR_HAS_INDISEINFO        = 5;    // 에어 모니터 센싱 정보 있음
    public static final int STATE_AIR_MONITOR_UNKNOWN_2ND           = 6;    // 2차 에어모니터 미등록 되어 있거나 연결이 해제된 경우

    private static boolean isOldUser = false;   // 1,2차 사용자와 프리즘 사용자 구분 플래그 (메인 UI 분기처리 하기 위해 사용)
    private static boolean anyType = false;

    private String clientId;
    private String endPoint;

    public static AWSIoTMqttHelper awsIoTMqttHelper;                       // AWS Mqtt 연결 객체

    private static DataChangeListener dataChangedListener;

    public static AuthData authData;

    private Handler mqttConnectHandler;

    private boolean isAppFinishStart = false;

    @Override
    public void onCreate() {
        super.onCreate();

        // 다크모드 미적용
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        instance = this;
        mUserInfo = new UserInfo();
        authData = new AuthData();

        // ikHwang 2019-05-21 오전 11:24 홈화면 구성 리스트 초기화
        mHomeList = new ArrayList<>();
        mqttConnectHandler = new Handler();

        if (mContext == null) {
            mContext = getApplicationContext();
            SharedPrefUtil.init(mContext);
            DeviceManager.getInstance().init(mContext);
        }
        KeyStoreClass.createNewKeys(this);
        //doBindService();
    }

    @Override
    public void onTerminate() {
        super.onTerminate();

        if(null != awsIoTMqttHelper) awsIoTMqttHelper.closeMqttManager();
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    public static CleanVentilationApplication getInstance() {
        return instance;
    }

    public static Context getContext() {
        return mContext;
    }

    /**
     * 앱 캐시 삭제
     */
    public void clearApplicationData() {
        File cacheDirectory = getCacheDir();
        File applicationDirectory = new File(cacheDirectory.getParent());
        if (applicationDirectory.exists()) {
            String[] fileNames = applicationDirectory.list();
            for (String fileName : fileNames) {
                if (!fileName.equals("lib")) {
                    deleteFile(new File(applicationDirectory, fileName));
                }
            }
        }
    }

    /**
     * 파일 삭제 루틴.
     * @param file
     * @return
     */
    public static boolean deleteFile(File file) {
        boolean deletedAll = true;
        if (file != null) {
            if (file.isDirectory()) {
                String[] children = file.list();
                for (int i = 0; i < children.length; i++) {
                    deletedAll = deleteFile(new File(file, children[i])) && deletedAll;
                }
            } else {
                deletedAll = file.delete();
            }
        }
        return deletedAll;
    }

    public UserInfo getUserInfo(){
        return mUserInfo;
    }

    // ikHwang 2019-05-21 오전 9:34 홈화면 구성 데이터 리턴
    public ArrayList<CommonHomeInfo> getHomeList(){
        return mHomeList;
    }

    public boolean hasRoomController(){
        boolean returnValue = false;

        try {
            if(null != mHomeList && mHomeList.size() > 0 && null != mHomeList.get(0).getRoomController()){
                returnValue = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return returnValue;
    }

    /**
     * ikHwang 2019-05-24 오전 9:06 메인화면 구성하기위해 룸콘 및 에어모니터 등록 상태 확인
     * @return
     */
    public int getHomeState(){
        int returnValue = STATE_ROOM_CONTROL_NO_REGISTER;

        if(0 < mHomeList.size()){
            if(null == mHomeList.get(0).getRoomController()){
                returnValue = STATE_ROOM_CONTROL_NO_REGISTER; // 룸콘 미등록
                CommonUtils.customLog(TAG, "HomeStat STATE_ROOM_CONTROL_NO_REGISTER (1)", Log.ERROR);
            }else{
                if(1 == mHomeList.get(0).getRoomController().getState()){
                    // 에어 모니터에 대한 상태 터리
                    if(null == getHomeList().get(0).getAirMonitor()){
                        if(3 == mHomeList.get(0).getRoomController().getPcd()){
                            returnValue = STATE_AIR_MONITOR_UNKNOWN_2ND; // 2차(듀얼링크) 에어모니터 미등록 및 연결해제
                            CommonUtils.customLog(TAG, "HomeStat STATE_AIR_MONITOR_UNKNOWN_2ND", Log.ERROR);
                        }else{
                            returnValue = STATE_AIR_MONITOR_NO_REGISTER; // 에어모니터 등록된적 없음
                            CommonUtils.customLog(TAG, "HomeStat STATE_AIR_MONITOR_NO_REGISTER", Log.ERROR);
                        }
                    }else{
                        if(1 == getHomeList().get(0).getAirMonitor().getState()){
                            if(null == mHomeList.get(0).getInside()){
                                returnValue = STATE_AIR_MONITOR_UNKNOWN_INDISEINFO; // 에어모니터 연결되었으나 센서 데이터 없음
                                CommonUtils.customLog(TAG, "HomeStat STATE_AIR_MONITOR_UNKNOWN_INDISEINFO", Log.ERROR);
                            }else{
                                returnValue = STATE_AIR_MONITOR_HAS_INDISEINFO; // 에어모니터 센서 정보 있음 정상처리
                                CommonUtils.customLog(TAG, "HomeStat STATE_AIR_MONITOR_HAS_INDISEINFO", Log.ERROR);
                            }
                        }else {
                            if (3 == mHomeList.get(0).getRoomController().getPcd()) {
                                returnValue = STATE_AIR_MONITOR_UNKNOWN_2ND; // 2차(듀얼링크) 에어모니터 미등록 및 연결해제
                                CommonUtils.customLog(TAG, "HomeStat STATE_AIR_MONITOR_UNKNOWN_2ND", Log.ERROR);
                            } else {
                                returnValue = STATE_AIR_MONITOR_NOT_CONNECTED; // 에어모니터 등록되었으나 연결 해제됨
                                CommonUtils.customLog(TAG, "HomeStat STATE_AIR_MONITOR_NOT_CONNECTED", Log.ERROR);
                            }
                        }
                    }
                }else{
                    returnValue = STATE_ROOM_CONTROL_NOT_CONNECTED; // 룸콘 미연결
                    CommonUtils.customLog(TAG, "HomeStat STATE_ROOM_CONTROL_NOT_CONNECTED", Log.ERROR);
                }
            }
        }else{
            returnValue = STATE_ROOM_CONTROL_NO_REGISTER; // 룸콘 미등록
            CommonUtils.customLog(TAG, "HomeStat STATE_ROOM_CONTROL_NO_REGISTER (0)", Log.ERROR);
        }

        return returnValue;
    }

    // ikHwang 2019-05-24 오후 1:47 룸콘 연결 상태 확인
    public boolean isConnectedRoomController(){
        try {
            if(0 < CleanVentilationApplication.getInstance().getHomeList().size()
                    && null != CleanVentilationApplication.getInstance().getHomeList().get(0).getRoomController()
                    && 0 != CleanVentilationApplication.getInstance().getHomeList().get(0).getRoomController().getState()){
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    /**
     * ikHwang 2019-05-24 오전 9:46 룸콘트롤러 DEVICEID 조회
     * @return
     */
    public String getRoomControllerDeviceID(){
        String returnValue = "";

        try {
            if(0 < mHomeList.size() && null != mHomeList.get(0).getRoomController()){
                returnValue = mHomeList.get(0).getRoomController().getGid();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return returnValue;
    }

    /**
     * ikHwang 2019-05-24 오전 9:46 룸콘트롤러 PCD 조회
     * @return
     */
    public int getRoomControllerDevicePCD() {
        int returnValue = 0;

        try {
            if(null != mHomeList && 0 < mHomeList.size() && null != mHomeList.get(0).getRoomController()){
                returnValue = mHomeList.get(0).getRoomController().getPcd();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return returnValue;
    }

    /**
     * ikHwang 2020-12-22 오후 4:45 mqtt topic의 모델 코드에 사용 
     * @return
     */
    public String getRoomControllerDeviceModelCode() {
        String returnValue = "0";

        try {
            if(0 < mHomeList.size() && null != mHomeList.get(0).getRoomController()){
                returnValue = String.valueOf(mHomeList.get(0).getRoomController().getModelCode());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return returnValue;
    }

    /**
     * ikHwang 2019-05-24 오전 9:47 에어모니터 DEIVCEID 조회
     * @return
     */
    public String getAirMonitorDeviceID(){
        String returnValue = "";

        try {
            if(0 < mHomeList.size() && null != mHomeList.get(0).getAirMonitor()){
                returnValue = mHomeList.get(0).getAirMonitor().getGid();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return returnValue;
    }

    public boolean isIsOldUser() {
        return isOldUser;
    }

    public void setIsOldUser(boolean oldUser) {
        isOldUser = oldUser;
    }

    public static boolean isAnyType() {
        return anyType;
    }

    public static void setAnyType(boolean anyType) {
        CleanVentilationApplication.anyType = anyType;
    }

    /**
     * ikHwang 2020-01-16 오전 11:41 홈화면 정보 초기화
     */
    public void clearHomeInfo(){
        if(null != mHomeList) mHomeList.clear();
        isOldUser = false;
    }

    /**
     * ikHwang 2020-11-27 오전 9:06 AWS 인증 정보 파싱
     * @param objAuth
     */
    public static void awsAuthParser(JSONObject objAuth){
        // AWS 인증 정보
        if(null != objAuth){
            if(objAuth.has("idToken")) CleanVentilationApplication.authData.idToken = objAuth.optString("idToken", "");
            if(objAuth.has("accessToken")) CleanVentilationApplication.authData.accessToken = objAuth.optString("accessToken", "");
            if(objAuth.has("refreshToken")) {
                String refreshToken = objAuth.optString("refreshToken", "");
                CleanVentilationApplication.authData.refreshToken = refreshToken;
                SharedPrefUtil.putString(SharedPrefUtil.TOKEN, refreshToken);
            }
            if(objAuth.has("authenticationExpiresIn")) {
                int authoExpiredIn = objAuth.optInt("authenticationExpiresIn", 0);
                CleanVentilationApplication.authData.authenticationExpiredIn = authoExpiredIn;
                long currentTime = System.currentTimeMillis() + ((authoExpiredIn-10) * 1000);
                SharedPrefUtil.putLong(SharedPrefUtil.AUTHEXPIRED, currentTime);
            }
            if(objAuth.has("accessKeyId")) CleanVentilationApplication.authData.accessKeyId = objAuth.optString("accessKeyId", "");
            if(objAuth.has("secretKey")) CleanVentilationApplication.authData.secretKey = objAuth.optString("secretKey", "");
            if(objAuth.has("sessionToken")) CleanVentilationApplication.authData.sessionToken = objAuth.optString("sessionToken", "");
            if(objAuth.has("authorizationExpiresIn")) CleanVentilationApplication.authData.authorizationExpiresIn = objAuth.optInt("authorizationExpiresIn", 0);
        }
    }

    /**
     * // ikHwang 2020-11-03 오후 3:47 데이터 수신 상태 콜백 등록
     * @param listener
     */
    public void setDataChangedListener(DataChangeListener listener){
        dataChangedListener = listener;
    }

    /**
     * ikHwang 2020-11-03 오후 3:50 Mqtt 연결
     */
    public void awsMqttConnect(){
        mqttConnectHandler.post(mqttConnectRunnable);
    }

    private void awsMqttConnector(){
        if(null == awsIoTMqttHelper){
            awsIoTMqttHelper = new AWSIoTMqttHelper(getApplicationContext(), getUserUUID(), HttpApi.IOT_END_POINT, authData.sessionToken, authData.accessKeyId, authData.secretKey, new AWSIoTMqttHelper.KDmqttListener() {
                @Override
                public void onMessageReceived(String topic, String message) {
//                    CommonUtils.customLog(TAG, "&&&& received topic : " + topic + "\nmessage : " + message, Log.ERROR);

                    switch (PubSubData.getPubType(topic)){
                        case PubSubData.PUB_TYPE_DID: // DID 정보 수신
                            // 데이터 파싱 부분 추가 필요
                            DeviceManager.getInstance().setReceivedData(COMMAND_DID, message);
                            if(null != dataChangedListener) dataChangedListener.onDataChanged(ProtocolType.COMMAND_DID);
                            break;

                        case PubSubData.PUB_TYPE_SD: // SD 정보 수신
                            // 데이터 파싱 부분 추가 필요
                            DeviceManager.getInstance().setReceivedData(COMMAND_SD, message);
                            if(null != dataChangedListener) dataChangedListener.onDataChanged(ProtocolType.COMMAND_SD);
                            break;

                        case PubSubData.PUB_TYPE_BRIGHTNESS: // 에어모니터 밝기 상태 수신
                            // 데이터 파싱 부분 추가 필요
                            DeviceManager.getInstance().setReceivedData(COMMAND_LED_STATUS_INFO, message);
                            if(null != dataChangedListener) dataChangedListener.onDataChanged(ProtocolType.COMMAND_LED_STATUS_INFO);
                            break;
                    }
                }

                @Override
                public void onError(int nState) {
                    if(MQTT_CONNECTION_TIMEOUT == nState){
                        awsRemoteFinish();
                    }

                    try {
                        Thread.sleep(1000);
                        
                        awsIoTMqttHelper.closeMqttManager();
                        awsIoTMqttHelper = null;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    if(null != dataChangedListener) dataChangedListener.onError(nState);
                }

                @Override
                public void onStatusChanged(int status, Throwable throwable) {
                    switch (status){
                        // mqtt 연결이 끊어진 경우 awsIoTMqttHelper객체 초기화
                        case AWSIoTMqttHelper.MQTT_INIT_CONNECTION_LOST:
                        case AWSIoTMqttHelper.MQTT_INIT_DISCONNECTED:
                            try {
                                awsIoTMqttHelper.closeMqttManager();
                                awsIoTMqttHelper = null;
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            break;
                    }

                    if(null != dataChangedListener) dataChangedListener.onStatusChanged(status);
                }
            });

            awsIoTMqttHelper.connect();
        }
    }

    /**
     * ikHwang 2021-01-07 오후 2:06 앱 연결 종료 호출
     */
    public void awsRemoteFinish(){
        try {
            String sessionToken = CleanVentilationApplication.authData.sessionToken;
            String modelCode = CleanVentilationApplication.getInstance().getRoomControllerDeviceModelCode();
            String deviceId = CleanVentilationApplication.getInstance().getRoomControllerDeviceID();

            String topic = PubSubData.getRemoteAppStopPubTopic(modelCode, deviceId);
            String requestData = PubSubData.getAppStopPubData(sessionToken, modelCode, deviceId);

            publishData(topic, requestData);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * ikHwang 2020-11-19 오후 2:31 mqtt 연결 종료
     */
    public void awsMqttDisConnect(){
        CommonUtils.customLog(TAG, "&&&& awsMqttDisConnect()", Log.ERROR);

        if(null != awsIoTMqttHelper){
            awsIoTMqttHelper.closeMqttManager();
            awsIoTMqttHelper = null;
        }
    }

    /**
     *
     */
    public void subscribeData(){
        if(null != awsIoTMqttHelper){
            try {
                String modelCode = getRoomControllerDeviceModelCode();
                String deviceId = getRoomControllerDeviceID();

                if(TextUtils.isEmpty(modelCode) || TextUtils.isEmpty(deviceId)) return;

                ArrayList<String> subList = new ArrayList<>();
                subList.add(PubSubData.getDidSubTopic(modelCode, deviceId));
                subList.add(PubSubData.getSdSubTopic(modelCode, deviceId));
                subList.add(PubSubData.getBrightnessSubTopic(modelCode, deviceId));
                awsIoTMqttHelper.subscribeAll(subList);

//                CommonUtils.customLog(TAG, "SubscribeData  : " + Arrays.toString(new ArrayList[]{subList}), Log.ERROR);
            } catch (Exception e) {
                FirebaseCrashlytics.getInstance().log("subscribeData Exception  : " + e.getLocalizedMessage());
            }
        }
    }

    /**
     *
     */
    public void publishData(String pubTopic, String data){
        if(null != awsIoTMqttHelper){
//            CommonUtils.customLog(TAG, "PublishData topic : " + pubTopic + ", data : " + data, Log.ERROR);

            if(!TextUtils.isEmpty(pubTopic) && !TextUtils.isEmpty(data)) awsIoTMqttHelper.publish(pubTopic, data);
        }
    }

    /**
     * UUDI 생성 및 조회
     * @return
     */
    public String getUserUUID() {
        String userUUID = "";

        if (TextUtils.isEmpty(userUUID)) {
            userUUID = SharedPrefUtil.getString(SharedPrefUtil.STR_UUID, "");

            if (TextUtils.isEmpty(userUUID)) {
                userUUID = UUID.randomUUID().toString();

                SharedPrefUtil.putString(SharedPrefUtil.STR_UUID, userUUID);
            }
        }

        return userUUID;
    }

    private Runnable mqttConnectRunnable = new Runnable() {
        @Override
        public void run() {
            awsMqttConnector();
        }
    };

    // 앱 종료 요청 상태 플래그 (제어화면에서 앱 종료시 mqtt 연결해제 팝업이 표시되어 추가)
    public boolean isAppFinishStart() {
        return isAppFinishStart;
    }

    public void setAppFinishStart(boolean appFinishStart) {
        isAppFinishStart = appFinishStart;
    }
}
