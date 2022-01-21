package kr.co.kdone.airone.aws;

import org.json.JSONException;
import org.json.JSONObject;

import libs.espressif.utils.TextUtils;

import static kr.co.kdone.airone.utils.ProtocolType.SCHEDULE_TYPE_OFF;
import static kr.co.kdone.airone.utils.ProtocolType.SCHEDULE_TYPE_ON;

/**
 * ikHwang 2020-11-03 오후 3:31 AWS Mqtt 정보를 조회하기위한 Pub 문자열 생성
 */
public class PubSubData {
    private static final String SUB_DID                     = "cmd/rc/%s/%s/remote/did/res";                // cmd/rc/{modelCode}/{clientID}/remote/did/res
    private static final String SUB_SD                      = "cmd/rc/%s/%s/remote/status/res";             // cmd/rc/{modelCode}/{deviceID}/remote/status/res
    private static final String SUB_BRIGHTNESS              = "cmd/rc/%s/%s/remote/brightness/res";         // cmd/rc/{modelCode}/{deviceID}/remote/brightness/res

    private static final String PUB_DID                     = "cmd/rc/%s/%s/remote/did";                    // cmd/rc/{modelCode}/{clientID}/remote/did
    private static final String PUB_SD                      = "cmd/rc/%s/%s/remote/status";                 // cmd/rc/{modelCode}/{deviceID}/remote/status
    private static final String PUB_BRIGHTNESS              = "cmd/rc/%s/%s/remote/brightness";             // cmd/rc/{modelCode}/{deviceID}/remote/brightness
    private static final String PUB_REMOTE_POWER            = "cmd/rc/%s/%s/remote/power";                  // cmd/rc/{modelCode}/{deviceID}/remote/power
    private static final String PUB_REMOTE_CHANGE_MODE      = "cmd/rc/%s/%s/remote/change-mode";            // cmd/rc/{modelCode}/{deviceID}/remote/change-mode
    private static final String PUB_REMOTE_SCHEDULE         = "cmd/rc/%s/%s/remote/schedule";               // cmd/rc/{modelCode}/{deviceID}/remote/schedule
    private static final String PUB_REMOTE_SLEEP            = "cmd/rc/%s/%s/remote/deep-sleep";             // cmd/rc/{modelCode}/{deviceID}/remote/deep-sleep
    private static final String PUB_REMOTE_HUMIDITY         = "cmd/rc/%s/%s/remote/humidification";         // cmd/rc/{modelCode}/{deviceID}/remote/humidification
    private static final String PUB_REMOTE_FILTER_RESET     = "cmd/rc/%s/%s/remote/filter/reset";           // cmd/rc/{modelCode}/{deviceID}/remote/filter/reset
    private static final String PUB_REMOTE_APP_STOP         = "cmd/rc/%s/%s/remote/app-stop";               // cmd/rc/{modelCode}/{deviceID}/remote/app-stop
    private static final String PUB_REMOTE_UV_LED           = "cmd/rc/%s/%s/remote/uvc-led";                // cmd/rc/{modelCode}/{deviceID}/remote/uvc-led

    public static final int PUB_TYPE_UNKNOWN                = 0;    // PUB 타입 알 수 없음
    public static final int PUB_TYPE_DID                    = 1;    // PUB 타입 DID 요청
    public static final int PUB_TYPE_SD                     = 2;    // PUB 타입 SD 상태 수신
    public static final int PUB_TYPE_BRIGHTNESS             = 3;    // PUB 타입 에어 모니터 밝기 요청
    public static final int PUB_TYPE_REMOTE_CONTROL         = 4;    // PUB 타입 제어 요청

    /**
     * ikHwang 2020-11-03 오후 3:31 DID Subscribe Topic
     * @param modelCode
     * @param clientId
     * @return
     */
    public static String getDidSubTopic(String modelCode, String clientId){
        return (TextUtils.isEmpty(modelCode) || TextUtils.isEmpty(clientId)) ? null : String.format(SUB_DID, modelCode, clientId);
    }

    /**
     * ikHwang 2020-11-03 오후 3:31 SD Subscribe Topic
     * @param modelCode
     * @param deviceId
     * @return
     */
    public static String getSdSubTopic(String modelCode, String deviceId){
        return (TextUtils.isEmpty(modelCode) || TextUtils.isEmpty(deviceId)) ? null : String.format(SUB_SD, modelCode, deviceId);
    }

    /**
     * ikHwang 2020-11-03 오후 3:39 에어모니터 상태 Subscribe Topic
     * @param modelCode
     * @param deviceId
     * @return
     */
    public static String getBrightnessSubTopic(String modelCode, String deviceId){
        return (TextUtils.isEmpty(modelCode) || TextUtils.isEmpty(deviceId)) ? null : String.format(SUB_BRIGHTNESS, modelCode, deviceId);
    }

    /**
     * ikHwang 2020-11-19 오전 8:53 DID Publish Topic
     * @param modelCode
     * @param clientId
     * @return
     */
    public static String getDidPubTopic(String modelCode, String clientId){
        return (TextUtils.isEmpty(modelCode) || TextUtils.isEmpty(clientId)) ? null : String.format(PUB_DID, modelCode, clientId);
    }

    /**
     * ikHwang 2020-11-19 오전 8:54 SD Publish Topic
     * @param modelCode
     * @param deviceId
     * @return
     */
    public static String getSdPubTopic(String modelCode, String deviceId){
        return (TextUtils.isEmpty(modelCode) || TextUtils.isEmpty(deviceId)) ? null : String.format(PUB_SD, modelCode, deviceId);
    }

    /**
     * ikHwang 2020-11-19 오전 8:54 에어모니터 상태 Publish Topic
     * @param modelCode
     * @param deviceId
     * @return
     */
    public static String getBrightnessPubTopic(String modelCode, String deviceId){
        return (TextUtils.isEmpty(modelCode) || TextUtils.isEmpty(deviceId)) ? null : String.format(PUB_BRIGHTNESS, modelCode, deviceId);
    }

    /**
     * ikHwang 2020-11-19 오전 8:28 전원 On/Off 설정 Topic
     * @param modelCode
     * @param deviceId
     * @return
     */
    public static String getRemotePowerPubTopic(String modelCode, String deviceId){
        return (TextUtils.isEmpty(modelCode) || TextUtils.isEmpty(deviceId)) ? null : String.format(PUB_REMOTE_POWER, modelCode, deviceId);
    }

    /**
     * ikHwang 2020-11-19 오전 8:29 제어 모드 변경 Topic
     * @param modelCode
     * @param deviceId
     * @return
     */
    public static String getRemoteChangeModePubTopic(String modelCode, String deviceId){
        return (TextUtils.isEmpty(modelCode) || TextUtils.isEmpty(deviceId)) ? null : String.format(PUB_REMOTE_CHANGE_MODE, modelCode, deviceId);
    }

    /**
     * ikHwang 2020-11-19 오전 8:30 꺼짐/켜짐 예약 설정 Topic
     * @param modelCode
     * @param deviceId
     * @return
     */
    public static String getRemoteSchedulePubTopic(String modelCode, String deviceId){
        return (TextUtils.isEmpty(modelCode) || TextUtils.isEmpty(deviceId)) ? null : String.format(PUB_REMOTE_SCHEDULE, modelCode, deviceId);
    }

    /**
     * ikHwang 2020-11-19 오전 9:18 숙면 설정 Topic
     * @param modelCode
     * @param deviceId
     * @return
     */
    public static String getRemoteSleepPubTopic(String modelCode, String deviceId){
        return (TextUtils.isEmpty(modelCode) || TextUtils.isEmpty(deviceId)) ? null : String.format(PUB_REMOTE_SLEEP, modelCode, deviceId);
    }

    /**
     * ikHwang 2020-11-19 오전 9:18 희망습도 설정 Topic
     * @param modelCode
     * @param deviceId
     * @return
     */
    public static String getRemoteHumidityPubTopic(String modelCode, String deviceId){
        return (TextUtils.isEmpty(modelCode) || TextUtils.isEmpty(deviceId)) ? null : String.format(PUB_REMOTE_HUMIDITY, modelCode, deviceId);
    }

    /**
     * Create by ikHwang on 2021-09-02 오전 10:22 UV LED 설정
     * @param modelCode
     * @param deviceId
     * @return
     */
    public static String getRemoteUVLedPubTopic(String modelCode, String deviceId){
        return (TextUtils.isEmpty(modelCode) || TextUtils.isEmpty(deviceId)) ? null : String.format(PUB_REMOTE_UV_LED, modelCode, deviceId);
    }

    /**
     * ikHwang 2020-11-20 오후 3:13 필터 리셋 Topic
     * @param modelCode
     * @param deviceId
     * @return
     */
    public static String getRemoteFilterResetPubTopic(String modelCode, String deviceId){
        return (TextUtils.isEmpty(modelCode) || TextUtils.isEmpty(deviceId)) ? null : String.format(PUB_REMOTE_FILTER_RESET, modelCode, deviceId);
    }

    /**
     * ikHwang 2021-01-07 오후 1:56 원격제어 종료 Topic
     * @param modelCode
     * @param deviceId
     * @return
     */
    public static String getRemoteAppStopPubTopic(String modelCode, String deviceId){
        return (TextUtils.isEmpty(modelCode) || TextUtils.isEmpty(deviceId)) ? null : String.format(PUB_REMOTE_APP_STOP, modelCode, deviceId);
    }

    /**
     * ikHwang 2020-11-03 오후 3:38 PUB 타입 확인
     * @param strPub
     * @return
     */
    public static int getPubType(String strPub){
        int type = PUB_TYPE_UNKNOWN;

        if(!TextUtils.isEmpty(strPub)){
            if(strPub.endsWith("/did/res")){ // DID
                type = PUB_TYPE_DID;
            }else if(strPub.endsWith("/status/res")){ // SD
                type = PUB_TYPE_SD;
            }else if(strPub.endsWith("/brightness/res")){ // 에어모니터 밝기 조절
                type = PUB_TYPE_BRIGHTNESS;
            }
        }

        return type;
    }

    private static JSONObject getPubCommonObj(String clientId, String sessionId, String requestTopic, String responseTopic){
        JSONObject jsonObject = new JSONObject();

        try {
            jsonObject.put("clientId", clientId);
//            jsonObject.put("sessionId", sessionId);
            jsonObject.put("sessionId", "");
            jsonObject.put("requestTopic", requestTopic);
            jsonObject.put("responseTopic", responseTopic);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return jsonObject;
    }

    /**
     * ikHwang 2020-11-19 오전 9:02 DID 요청 PUB data 생성
     * @param sessionId
     * @param modelCode
     * @param deviceId
     * @return
     */
    public static String getDidPubData(String sessionId, String modelCode, String deviceId){
        String pubString = "";

        JSONObject jsonObject = getPubCommonObj(deviceId, sessionId, getDidPubTopic(modelCode, deviceId), getDidSubTopic(modelCode, deviceId));
        pubString = jsonObject.toString();

        return pubString;
    }

    /**
     * ikHwang 2020-11-19 오전 9:03 SD 상태 조회 요청 PUB data 생성
     * @param sessionId
     * @param modelCode
     * @param deviceId
     * @return
     */
    public static String getSdPubData(String sessionId, String modelCode, String deviceId){
        String pubString = "";

        JSONObject jsonObject = getPubCommonObj(deviceId, sessionId, getSdPubTopic(modelCode, deviceId), getSdSubTopic(modelCode, deviceId));
        pubString = jsonObject.toString();

        return pubString;
    }

    /**
     * ikHwang 2020-11-19 오전 9:07 전원제어 Pub data
     * @param sessionId
     * @param modelCode
     * @param deviceId
     * @param power
     * @return
     */
    public static String getPowerPubData(String sessionId, String modelCode, String deviceId, int power){
        String pubString = "";

        JSONObject jsonObject = getPubCommonObj(deviceId, sessionId, getRemotePowerPubTopic(modelCode, deviceId), getSdSubTopic(modelCode, deviceId));

        JSONObject requestObject = new JSONObject();

        try {
            requestObject.put("power", power);

            jsonObject.put("request", requestObject);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        pubString = jsonObject.toString();

        return pubString;
    }

    /**
     * ikHwang 2020-11-19 오전 9:12 모드 변경 Pub data
     * @param sessionId
     * @param modelCode
     * @param deviceId
     * @param mode
     * @param option
     * @param windLevel
     * @return
     */
    public static String getChangeModePubData(String sessionId, String modelCode, String deviceId, int mode, int option, int windLevel){
        String pubString = "";

        JSONObject jsonObject = getPubCommonObj(deviceId, sessionId, getRemoteChangeModePubTopic(modelCode, deviceId), getSdSubTopic(modelCode, deviceId));

        JSONObject requestObject = new JSONObject();

        try {
            requestObject.put("operationMode", mode);
            requestObject.put("optionMode", option);
//            if(0 < windLevel) requestObject.put("windLevel", windLevel);
            requestObject.put("windLevel", windLevel);

            jsonObject.put("request", requestObject);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        pubString = jsonObject.toString();

        return pubString;
    }

    /**
     * ikHwang 2020-11-19 오전 9:16 예약 설정 Pub data
     * @param sessionId
     * @param modelCode
     * @param deviceId
     * @param type
     * @param time
     * @return
     */
    public static String getSchedulePubData(String sessionId, String modelCode, String deviceId, int type, int time){
        String pubString = "";

        JSONObject jsonObject = getPubCommonObj(deviceId, sessionId, getRemoteSchedulePubTopic(modelCode, deviceId), getSdSubTopic(modelCode, deviceId));

        JSONObject requestObject = new JSONObject();

        try {
            int enableType = 3;
            if(SCHEDULE_TYPE_OFF == type) enableType = 1;
            else if(SCHEDULE_TYPE_ON == type) enableType = 2;
            else time = 0;

            requestObject.put("enable", enableType);
            requestObject.put("scheduleTime", time);

            jsonObject.put("request", requestObject);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        pubString = jsonObject.toString();

        return pubString;
    }

    /**
     * ikHwang 2020-11-19 오전 9:21 숙면 설정 Pub data
     * @param sessionId
     * @param modelCode
     * @param deviceId
     * @param type
     * @param startTime
     * @param endTime
     * @return
     */
    public static String getSleepPubData(String sessionId, String modelCode, String deviceId, int type, int startTime, int endTime){
        String pubString = "";

        JSONObject jsonObject = getPubCommonObj(deviceId, sessionId, getRemoteSleepPubTopic(modelCode, deviceId), getSdSubTopic(modelCode, deviceId));

        JSONObject requestObject = new JSONObject();

        try {
            requestObject.put("enable", type);
            requestObject.put("startTime", startTime);
            requestObject.put("endTime", endTime);

            jsonObject.put("request", requestObject);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        pubString = jsonObject.toString();

        return pubString;
    }

    /**
     * ikHwang 2020-11-19 오전 9:22 희망습도 설정 Pub data
     * @param sessionId
     * @param modelCode
     * @param deviceId
     * @param humidity
     * @return
     */
    public static String getHumidityPubData(String sessionId, String modelCode, String deviceId,int humidity){
        String pubString = "";

        JSONObject jsonObject = getPubCommonObj(deviceId, sessionId, getRemoteHumidityPubTopic(modelCode, deviceId), getSdSubTopic(modelCode, deviceId));

        JSONObject requestObject = new JSONObject();

        try {
            requestObject.put("humidity", humidity);

            jsonObject.put("request", requestObject);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        pubString = jsonObject.toString();

        return pubString;
    }

    /**
     * Create by ikHwang on 2021-09-02 오전 10:23 UV LED 설정  
     * @param sessionId
     * @param modelCode
     * @param deviceId
     * @param uvLed
     * @return
     */
    public static String getUVLedPubData(String sessionId, String modelCode, String deviceId,int uvLed){
        String pubString = "";

        JSONObject jsonObject = getPubCommonObj(deviceId, sessionId, getRemoteUVLedPubTopic(modelCode, deviceId), getSdSubTopic(modelCode, deviceId));

        JSONObject requestObject = new JSONObject();

        try {
            requestObject.put("enable", uvLed);

            jsonObject.put("request", requestObject);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        pubString = jsonObject.toString();

        return pubString;
    }

    /**
     * ikHwang 2020-11-19 오전 9:24 에어모니터 밝기 조회 및 제어
     * @param sessionId
     * @param modelCode
     * @param deviceId
     * @param type
     * @param brightness
     * @return
     */
    public static String getBrightnessPubData(String sessionId, String modelCode, String deviceId, int type, int brightness){
        String pubString = "";

        JSONObject jsonObject = getPubCommonObj(deviceId, sessionId, getBrightnessPubTopic(modelCode, deviceId), getBrightnessSubTopic(modelCode, deviceId));

        JSONObject requestObject = new JSONObject();

        try {
            requestObject.put("requestType", type);
            requestObject.put("brightness", brightness);

            jsonObject.put("request", requestObject);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        pubString = jsonObject.toString();

        return pubString;
    }

    /**
     * ikHwang 2020-11-20 오후 3:14 필터 초기화
     * @param sessionId
     * @param modelCode
     * @param deviceId
     * @return
     */
    public static String getFilterResetPubData(String sessionId, String modelCode, String deviceId){
        String pubString = "";

        JSONObject jsonObject = getPubCommonObj(deviceId, sessionId, getRemoteFilterResetPubTopic(modelCode, deviceId), getSdSubTopic(modelCode, deviceId));

        JSONObject requestObject = new JSONObject();

        try {
            requestObject.put("requestType", 2);

            jsonObject.put("request", requestObject);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        pubString = jsonObject.toString();

        return pubString;
    }

    /**
     * ikHwang 2021-01-07 오후 1:58 원격제어 종료 
     * @param sessionId
     * @param modelCode
     * @param deviceId
     * @return
     */
    public static String getAppStopPubData(String sessionId, String modelCode, String deviceId){
        String pubString = "";

        JSONObject jsonObject = getPubCommonObj(deviceId, sessionId, getRemoteAppStopPubTopic(modelCode, deviceId), getSdSubTopic(modelCode, deviceId));

        /*JSONObject requestObject = new JSONObject();

        try {
            requestObject.put("requestType", 2);

            jsonObject.put("request", requestObject);
        } catch (JSONException e) {
            e.printStackTrace();
        }*/

        pubString = jsonObject.toString();

        return pubString;
    }

}
