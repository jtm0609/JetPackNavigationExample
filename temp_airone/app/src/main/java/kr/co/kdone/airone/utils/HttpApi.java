package kr.co.kdone.airone.utils;

import android.os.Build;
import android.text.TextUtils;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import kr.co.kdone.airone.BuildConfig;
import kr.co.kdone.airone.CleanVentilationApplication;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * 서버 통신을 위한 API 항목 기술
 */
public class HttpApi {
    private static final String TAG = HttpApi.class.getSimpleName();
    private static final boolean PRODUCT_MODE = false;
    private static final boolean DEBUG = true;
    // 사이닝키로 빌드시 자동 배포용 버전 설정, 디버그 빌드시 true 설정, false로 설정하면 운영서버용으로 빌드됨
    private static boolean isDebug = !DEBUG ? PRODUCT_MODE : true;

    // ikHwang 2019-05-22 오전 9:12 서버 리스폰스 코드 정의 추가
    public static final int RESPONSE_SUCCESS                                = 200;  // 성공
    public static final int RESPONSE_SERVER_EXCEPTION                       = 500;  // 서버 에러
    public static final int RESPONSE_DB_EXCEPTION                           = 501;  // DB 오류
    public static final int RESPONSE_INVALID_PARAMETERS                     = 502;  // 파라미터 오류
    public static final int RESPONSE_DATA_NO_EXIST                          = 503;  // 데이터 없음
    public static final int RESPONSE_SERVER_MAINTENANCE                     = 504;  // 서버 점검중
    public static final int RESPONSE_OUT_OF_BOUND                           = 505;  // 범위를 벗어나 데이터 없음
    public static final int RESPONSE_NOT_AUTHORIZED                         = 506;  // 권한 없음
    public static final int RESPONSE_USER_ID_ALREADY_EXIST                  = 510;  // 이미 등록된 사용자
    public static final int RESPONSE_WRONG_USER_INFO                        = 511;  // 사용자 정보 입력 오류 (미등록 사용자 포함)
    public static final int RESPONSE_DEVICE_NOT_EXIST                       = 520;  // 등록된 디바이스가 없는 사용자
    public static final int RESPONSE_DEVICE_NOT_CONNECTED                   = 521;  // 디바이스 연결 끊김
    public static final int RESPONSE_DEVICE_OLD_DATA                        = 522;  // 오래되어 신뢰할 수 없는 데이터
    public static final int RESPONSE_DEVICE_RC_ALREADY_USED_BY_OTHER_USER   = 523;  // 이미 사용 중인 디바이스
    public static final int RESPONSE_DEVICE_AM_ALREADY_USED_BY_OTHER_USER   = 524;  // 다른사용자가 에어모니터 사용중
    public static final int RESPONSE_DEVICE_NOT_REGISTERED                  = 525;  // 디바이스가 서버에 등록되어 있지 않음
    public static final int RESPONSE_DEVICE_ALREADY_EXIST                   = 526;  // 동일한 디바이스가 존재함
    public static final int RESPONSE_USER_NO_EXIST                          = 640;  // 가입된 회원 없음
    public static final int RESPONSE_USER_INVALID_CAPTCHA                   = 641;  // 유효하지 않은 captcha
    public static final int RESPONSE_USER_LOGIN_ATTEMPT_EXCEEDED            = 642;  // USER_LOGIN_ATTEMPT_EXCEEDED

    public static final int HTTP_OK = 200;
    public static final int HTTP_ERROR_DATA = 400;
    public static final int HTTP_ERROR_API = 500;

    private static final int TIMEOUT_CONNECT = 10;
    private static final int TIMEOUT_WRITE = 10;
    private static final int TIMEOUT_READ = 10;

    // 운영서버 연동 정보
    private static final String PROD_URL_MAIN        = "https://krtc-api.naviensmartcontrol.com:443";               // 1차 API 테스트 서버1111
    private static final String PROD_URL_MAIN_V2     = "https://krtc-api.naviensmartcontrol.com:443/api/v2/";       // 2차 API 2테스트 서버
    public static final String PROD_IOT_END_POINT    = "a1folro8nc3ln2-ats.iot.ap-northeast-2.amazonaws.com";       // mqtt 연동 EndPoint


    // Stage 서버 연동 정보
    private static final String DEV_URL_MAIN        = "https://krtc-stg-api.naviensmartcontrol.com:443";            // 1차 API 테스트 서버
    private static final String DEV_URL_MAIN_V2     = "https://krtc-stg-api.naviensmartcontrol.com:443/api/v2/";    // 2차 API 2테스트 서버
    public static final String DEV_IOT_END_POINT    = "a1js6fry7lcza0-ats.iot.ap-northeast-2.amazonaws.com";        // mqtt 연동 EndPoint

    // 빌드 상태에 따라 개발서버 및 운영서버 자동 변경
    private static final String URL_MAIN        = isDebug ? DEV_URL_MAIN : PROD_URL_MAIN;
    private static final String URL_MAIN_V2     = isDebug ? DEV_URL_MAIN_V2 : PROD_URL_MAIN_V2;
    public static final String IOT_END_POINT    = isDebug ? DEV_IOT_END_POINT : PROD_IOT_END_POINT;


    // ikHwang 2019-06-04 오후 3:46 1차 API 2차 로직에 포함된 부분이 있어서 제거하지 않음
    private static final String URL_SUB_REGISTER_PAIRING_SENSORBOX      = "registerPairing";
    private static final String URL_SUB_UNREGISTER_PAIRING_SENSORBOX    = "deletePairing";

    // ikHwang 2019-06-04 오후 3:46 2차 API
    // ikHwang 2020-11-03 오후 2:27 AWS Rest Api 연동 Path 수정
    private static final String URL_SUB_V2_CHECK_UPDATE             = "app/version";                            // 버전 체크
    private static final String URL_SUB_V2_AUTH_REFRESH             = "auth/refresh";                           // 사용자 토큰 갱싱

    private static final String URL_SUB_V2_DUPLE_CHECK_ACCOUNT      = "user/check-account";                     // 계정 중복 체크
    private static final String URL_SUB_V2_USER_JOIN                = "user/sign-up";                           // 회원가입
    private static final String URL_SUB_V2_USER_LOGIN               = "user/sign-in";                           // 로그인
    private static final String URL_SUB_V2_FIND_ID                  = "user/find-id";                           // 아이디 찾기
    private static final String URL_SUB_V2_CHECK_USER_ID_GID        = "user/auth";                              // 사용자 확인
    private static final String URL_SUB_V2_CHANGE_PASSWORD          = "user/change-password";                   // 비밀번호 변경
    private static final String URL_SUB_V2_RESET_PASSWORD           = "user/reset-password";                    // 비밀번호 찾기
    private static final String URL_SUB_API_V2_USER_WITHDRAWAL      = "user/withdrawal";                        // 회원 탈퇴
    private static final String URL_SUB_V2_CHANGE_USER_NAME         = "user/change-name";                       // 사용자 정보 변경 (이름)
    private static final String URL_SUB_V2_CHANGE_USER_MOBILE       = "user/change-mobile";                     // 사용자 정보 변경 (전화번호 등)
    private static final String URL_SUB_V2_USER_LOGOUT              = "user/sign-out";                          // 로그아웃
    private static final String URL_SUB_V2_USER_SET_AGREEMENT       = "user/set-agreement";                     // 약관 재동의

    private static final String URL_SUB_V2_CHANGE_LOCATION          = "device/change-location";                 // 디바이스 등록 위치 변경
    private static final String URL_SUB_V2_CHANGE_SSID              = "device/change-ssid";                     // 디바이스 SSID 변경
    private static final String URL_SUB_V2_CHANGE_PUSH              = "device/change-push-receive-setting";     // 디바이스 푸시 변경
    private static final String URL_SUB_V2_CHECK_DEVICE_STATE       = "device/state";                           // 룸콘 등록 상태 조회
    private static final String URL_SUB_V2_GET_DEVICE_INFO          = "device/info";                            // 디바이스 정보 조회
    private static final String URL_SUB_V2_ADD_AIR_MONITOR          = "device/add-air-monitor";                 // 1차 사용자 에어 모니터 등록
    private static final String URL_SUB_V2_ADD_ROOM_CONTROLLER      = "device/add-room-controller";             // 룸콘 등록

    private static final String URL_SUB_V2_GET_MAIN_INFO            = "main/info";                              // 메인화면 정보 조회
    private static final String URL_SUB_V2_GET_MAIN_WIDGET          = "main/widget";                            // 위젯 정보 조회

    private static final String URL_SUB_V2_GET_SMARTALARM           = "board/smart-alarm";                      // 스마트 알림 정보 조회
    private static final String URL_SUB_V2_GET_NOTICE_LIST          = "board/notice";                           // 공지사항 조회
    private static final String URL_SUB_V2_GET_REPORT               = "report/info";                            // 생활 리포트 조회

    /**
     * OKHTTP를 통해 통신
     *
     * @param url      : API 주소
     * @param json     : 전달될 파라미터(JSON 방식)
     * @param callback : 전송후 Callback 함수
     * @throws IOException
     */
    private static void post(String url, String json, Callback callback) throws IOException {
        CommonUtils.customLog(TAG, "Call url : " + url + " | params : " + json, Log.ERROR);

        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(TIMEOUT_CONNECT, TimeUnit.SECONDS)
                .writeTimeout(TIMEOUT_WRITE, TimeUnit.SECONDS)
                .readTimeout(TIMEOUT_READ, TimeUnit.SECONDS)
                .build();

        try {
            RequestBody body = RequestBody.create(JSON, json);

            Request request = new Request.Builder()
                    .url(url)
                    .post(body)
                    .build();

            client.newCall(request).enqueue(callback);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void post(String url, JSONObject json, final Callback callback) throws IOException {
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(TIMEOUT_CONNECT, TimeUnit.SECONDS)
                .writeTimeout(TIMEOUT_WRITE, TimeUnit.SECONDS)
                .readTimeout(TIMEOUT_READ, TimeUnit.SECONDS)
                .build();

        try {
            CommonUtils.customLog(TAG, "call Url : " + url + "\nParams : " + json.toString(), Log.ERROR);
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            final Request request;

            if (null != json) {
                MediaType JSON = MediaType.parse("application/json; charset=utf-8");
                RequestBody body = RequestBody.create(JSON, json.toString());

                request = new Request.Builder()
                        .url(url)
                        .post(body)
                        .build();
            } else {
                request = new Request.Builder()
                        .url(url)
                        .build();
            }

            client.newCall(request).enqueue(callback);
        } catch (Exception e) {
            e.printStackTrace();
            callback.onFailure(null, null);
        }
    }

    private static void post(final String url, final JSONObject json, final Map<String, String> header, final Callback callback) throws IOException {
        final OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(TIMEOUT_CONNECT, TimeUnit.SECONDS)
                .writeTimeout(TIMEOUT_WRITE, TimeUnit.SECONDS)
                .readTimeout(TIMEOUT_READ, TimeUnit.SECONDS)
                .build();

        try {
            CommonUtils.customLog(TAG, "call Url : " + url + "\nheader" + header.toString() + "\nParams : " + json.toString(), Log.ERROR);
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            Request request = getRequest(url, json, header);

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    callback.onFailure(call, e);
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    switch (response.code()){
                        // 로그인 인증 토큰 만료 처리
                        case 401:
                            CommonUtils.customLog(TAG, "call Url Unauthorized (401)", Log.ERROR);
                            if(PostCheckAuthRefresh(null, null)){
                                header.put("Authorization", CleanVentilationApplication.authData.accessToken);

                                client.newCall(getRequest(url, json, header)).enqueue(callback);
                            }else{
                                callback.onResponse(call, response);
                            }
                            break;

                        default:
                            callback.onResponse(call, response);
                            break;
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            callback.onFailure(null, null);
        }
    }

    private static Response post(String url, JSONObject json) throws IOException {
        Response response = null;

        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(TIMEOUT_CONNECT, TimeUnit.SECONDS)
                .writeTimeout(TIMEOUT_WRITE, TimeUnit.SECONDS)
                .readTimeout(TIMEOUT_READ, TimeUnit.SECONDS)
                .build();

        try {
            CommonUtils.customLog(TAG, "call Url : " + url + "\nParams : " + json.toString(), Log.ERROR);
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            final Request request;

            if (null != json) {
                MediaType JSON = MediaType.parse("application/json; charset=utf-8");
                RequestBody body = RequestBody.create(JSON, json.toString());

                request = new Request.Builder()
                        .url(url)
                        .post(body)
                        .build();
            } else {
                request = new Request.Builder()
                        .url(url)
                        .build();
            }

            response = client.newCall(request).execute();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            return response;
        }
    }


    private static Request getRequest(String url, JSONObject json, Map<String, String> header){
        Request request;
        Headers headers = Headers.of(header);

        if (null != json) {
            MediaType JSON = MediaType.parse("application/json; charset=utf-8");
            RequestBody body = RequestBody.create(JSON, json.toString());

            request = new Request.Builder()
                    .headers(headers)
                    .url(url)
                    .post(body)
                    .build();
        } else {
            request = new Request.Builder()
                    .headers(headers)
                    .url(url)
                    .build();
        }

        return request;
    }

    //====================================================================================================
    // 1차 API
    //====================================================================================================
    /**
     * 센서박스 페어링 등록
     */
    public static void PostRegisterPairingSensorBox(String userID, String pairingTacID, String pairingSensingBoxID, Callback c) throws IOException {
        String url = URL_MAIN + URL_SUB_REGISTER_PAIRING_SENSORBOX;
        post(url, PostRegisterPairingSensorBoxBowlingJson(userID, pairingTacID, pairingSensingBoxID), c);
    }

    private static String PostRegisterPairingSensorBoxBowlingJson(String userID, String pairingTacID, String pairingSensingBoxID) {
        return "{\"userID\":\"" + userID + "\","
                + "\"pairingTacID\":\"" + pairingTacID + "\","
                + "\"pairingSensingBoxID\":\"" + pairingSensingBoxID + "\"}";
    }

    /**
     * 센서박스 페어링 삭제
     */
    public static void PostUnregisterPairingSensorBox(String userID, String pairingTacID, String pairingSensingBoxID, Callback c) throws IOException {
        String url = URL_MAIN + URL_SUB_UNREGISTER_PAIRING_SENSORBOX;
        post(url, PostUnregisterPairingSensorBoxBowlingJson(userID, pairingTacID, pairingSensingBoxID), c);
    }

    private static String PostUnregisterPairingSensorBoxBowlingJson(String userID, String pairingTacID, String pairingSensingBoxID) {
        return "{\"userID\":\"" + userID + "\","
                + "\"pairingTacID\":\"" + pairingTacID + "\","
                + "\"pairingSensingBoxID\":\"" + pairingSensingBoxID + "\"}";
    }

    //====================================================================================================
    // 1차 API 끝
    //====================================================================================================


    //====================================================================================================
    // 2차 API
    //====================================================================================================
    /**
     * 앱 버전 확인
     **/
    public static void PostCheckAppVersion(Callback c) throws IOException{
        String url = URL_MAIN_V2 + URL_SUB_V2_CHECK_UPDATE;

        HashMap<String, Object> params = new HashMap<String, Object>() {{
            put("os", "a");
        }};

        JSONObject data = new JSONObject(params);

        post(url, data, c);
    }

    /**
     * ikHwang 2020-11-27 오전 9:02 AWS 인증 정보 갱신
     * @param refreshToken
     * @param c
     * @throws IOException
     */
    public static boolean PostCheckAuthRefresh(final String refreshToken, Callback c) throws IOException{
        boolean returnValue = false;

        String url = URL_MAIN_V2 + URL_SUB_V2_AUTH_REFRESH;

        HashMap<String, Object> params = new HashMap<String, Object>() {{
            put("refreshToken", SharedPrefUtil.getString(SharedPrefUtil.TOKEN, ""));
        }};

        JSONObject data = new JSONObject(params);

        Response response = post(url, data);
        if(response.code() == 200){
            try {
                String strResponse =  response.body().string();

                CommonUtils.customLog(TAG, "auth/refresh : " + strResponse, Log.ERROR);

                JSONObject json_data = new JSONObject(strResponse);

                switch (json_data.optInt("code", 0)) {
                    case HttpApi.RESPONSE_SUCCESS:
                        if(json_data.has("data")){
                            CleanVentilationApplication.awsAuthParser(json_data.getJSONObject("data"));
                            returnValue = true;
                        }
                        break;

                    default:
                        break;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return returnValue;
    }

    /**
     * 계정 중복 확인
     */
    public static void PostCheckDuplicateAccount(final String userId, Callback c) throws IOException{
        String url = URL_MAIN_V2 + URL_SUB_V2_DUPLE_CHECK_ACCOUNT;

        HashMap<String, Object> params = new HashMap<String, Object>() {{
            put("userId", TextUtils.isEmpty(userId) ? "" : userId);
        }};

        JSONObject data = new JSONObject(params);

        post(url, data, c);
    }

    /**
     * 공지사항 리스트
     */
    public static void PostV2GetNoticeList(final int pageNo, final int pageRowCount, final String userId, Callback c) throws IOException {
        String url = URL_MAIN_V2 + URL_SUB_V2_GET_NOTICE_LIST;

        HashMap<String, String> headers = new HashMap<String, String>() {{
            put("Content-Type", "application/json");
            put("Authorization", CleanVentilationApplication.authData.accessToken);
        }};

        HashMap<String, Object> params = new HashMap<String, Object>() {{
            put("page", pageNo);
            put("pageRowCount", pageRowCount);
            put("userId", TextUtils.isEmpty(userId) ? "" : userId);
        }};

        JSONObject data = new JSONObject(params);

        post(url, data, headers, c);
    }

    /**
     * 메인화면(V2A 기기 리스트 획득)
     */
    public static void PostV2GetMainInfo(final String userId, Callback c) throws IOException {
        String url = URL_MAIN_V2 + URL_SUB_V2_GET_MAIN_INFO;

        SimpleDateFormat formatTime = new SimpleDateFormat("aa hh:mm", Locale.getDefault());
        SharedPrefUtil.putString(SharedPrefUtil.WIDGET_UPDATE_TIME, formatTime.format(new Date()));

        HashMap<String, String> headers = new HashMap<String, String>() {{
            put("Content-Type", "application/json");
            put("Authorization", CleanVentilationApplication.authData.accessToken);
        }};

        HashMap<String, Object> params = new HashMap<String, Object>() {{
            put("userId", TextUtils.isEmpty(userId) ? "" : userId);
        }};

        JSONObject data = new JSONObject(params);

        post(url, data, headers, c);
    }

    /**
     * ikHwang 2020-12-08 오후 2:24 위젯 정보 조회
     * @param rcId
     * @param c
     * @throws IOException
     */
    public static void PostV2GetMainInfoWidget(final String rcId, Callback c) throws IOException {
        String url = URL_MAIN_V2 + URL_SUB_V2_GET_MAIN_WIDGET;

        SimpleDateFormat formatTime = new SimpleDateFormat("aa hh:mm", Locale.getDefault());
        SharedPrefUtil.putString(SharedPrefUtil.WIDGET_UPDATE_TIME, formatTime.format(new Date()));

        HashMap<String, Object> params = new HashMap<String, Object>() {{
            put("rcId", TextUtils.isEmpty(rcId) ? "" : rcId);
        }};

        JSONObject data = new JSONObject(params);

        post(url, data, c);
    }

    /**
     * 기기상태 체크
     */
    public static void PostV2CheckDeviceState(final String rcId, Callback c) throws IOException {
        String url = URL_MAIN_V2 + URL_SUB_V2_CHECK_DEVICE_STATE;

        HashMap<String, Object> params = new HashMap<String, Object>() {{
            put("rcId", TextUtils.isEmpty(rcId) ? "" : rcId);
        }};

        JSONObject data = new JSONObject(params);

        post(url, data, c);
    }

    /**
     * 룸컨트롤러 재등록
     */
    public static void PostV2ReNewRoomController(final String userId, final String rcId, final String ssid, final float lat, final float lng, final String area, Callback c) throws IOException{
        String url = URL_MAIN_V2 + URL_SUB_V2_ADD_ROOM_CONTROLLER;

        HashMap<String, String> headers = new HashMap<String, String>() {{
            put("Content-Type", "application/json");
            put("Authorization", CleanVentilationApplication.authData.accessToken);
        }};

        HashMap<String, Object> params = new HashMap<String, Object>() {{
            put("userId", TextUtils.isEmpty(userId) ? "" : userId);
            put("rcId", TextUtils.isEmpty(rcId) ? "" : rcId);
            put("ssid", TextUtils.isEmpty(ssid) ? "" : ssid);
            put("lat", String.valueOf(lat));
            put("lng", String.valueOf(lng));
            put("area", TextUtils.isEmpty(area) ? "" : area);
        }};

        JSONObject data = new JSONObject(params);

        post(url, data, headers, c);
    }

    /**
     * 에어모니터 추가
     */
    public static void PostV2AddAirMonitor(final String userId, final String rcId, final String sbid, final String ssid, Callback c) throws IOException {
        String url = URL_MAIN_V2 + URL_SUB_V2_ADD_AIR_MONITOR;

        HashMap<String, String> headers = new HashMap<String, String>() {{
            put("Content-Type", "application/json");
            put("Authorization", CleanVentilationApplication.authData.accessToken);
        }};

        HashMap<String, Object> params = new HashMap<String, Object>() {{
            put("userId", TextUtils.isEmpty(userId) ? "" : userId);
            put("rcId", TextUtils.isEmpty(rcId) ? "" : rcId);
            put("sbId", TextUtils.isEmpty(sbid) ? "" : sbid);
            put("ssid", TextUtils.isEmpty(ssid) ? "" : ssid);
        }};

        JSONObject data = new JSONObject(params);

        post(url, data, headers, c);
    }

    /**
     *  디바이스 정보 조회
     */
    public static void PostV2GetDeviceInfo(final String userId, Callback c) throws IOException {
        String url = URL_MAIN_V2 + URL_SUB_V2_GET_DEVICE_INFO;

        HashMap<String, String> headers = new HashMap<String, String>() {{
            put("Content-Type", "application/json");
            put("Authorization", CleanVentilationApplication.authData.accessToken);
        }};

        HashMap<String, Object> params = new HashMap<String, Object>() {{
            put("userId", TextUtils.isEmpty(userId) ? "" : userId);
        }};

        JSONObject data = new JSONObject(params);

        post(url, data, headers, c);
    }

    /**
     * 메인유저등록
     */
    public static void PostV2UserJoin(final String userId, final String pw, final String name, final String mobilePhone, final String rcId,
                                      final float lat, final float lng, final String area, final String osVer, final String modelInfo, final String ssid, Callback c) throws IOException {
        String url = URL_MAIN_V2 + URL_SUB_V2_USER_JOIN;

        HashMap<String, Object> params = new HashMap<String, Object>() {{
            put("userId", TextUtils.isEmpty(userId) ? "" : userId);
            put("pw", TextUtils.isEmpty(pw) ? "" : pw);
            put("name", TextUtils.isEmpty(name) ? "" : name);
            put("mobilePhone", TextUtils.isEmpty(mobilePhone) ? "" : mobilePhone);
            put("rcId", TextUtils.isEmpty(rcId) ? "" : rcId);
            put("lat", String.valueOf(lat));
            put("lng", String.valueOf(lng));
            put("os", "a");

            if(!TextUtils.isEmpty(area)) put("area", area);
            if(!TextUtils.isEmpty(osVer)) put("osVer", osVer);
            if(!TextUtils.isEmpty(modelInfo)) put("modelInfo", modelInfo);
            if(!TextUtils.isEmpty(ssid)) put("ssid", ssid);
        }};

        JSONObject data = new JSONObject(params);

        post(url, data, c);
    }

    /**
     * 사용자 정보 변경 (이름)
     */
    public static void PostV2ChangeUserName(final String userId, final String pw, final String name, Callback c) throws IOException {
        String url = URL_MAIN_V2 + URL_SUB_V2_CHANGE_USER_NAME;

        HashMap<String, String> headers = new HashMap<String, String>() {{
            put("Content-Type", "application/json");
            put("Authorization", CleanVentilationApplication.authData.accessToken);
        }};

        HashMap<String, Object> params = new HashMap<String, Object>() {{
            put("userId", TextUtils.isEmpty(userId) ? "" : userId);
            put("pw", TextUtils.isEmpty(pw) ? "" : pw);
            put("changeName", TextUtils.isEmpty(name) ? "" : name);
        }};

        JSONObject data = new JSONObject(params);

        post(url, data, headers, c);
    }

    /**
     * 사용자 정보 변경 (전화번호)
     */
    public static void PostV2ChangeUserMobile(final String userId, final String pw, final String mobilePhone, Callback c) throws IOException {
        String url = URL_MAIN_V2 + URL_SUB_V2_CHANGE_USER_MOBILE;

        HashMap<String, String> headers = new HashMap<String, String>() {{
            put("Content-Type", "application/json");
            put("Authorization", CleanVentilationApplication.authData.accessToken);
        }};

        HashMap<String, Object> params = new HashMap<String, Object>() {{
            put("userId", TextUtils.isEmpty(userId) ? "" : userId);
            put("pw", TextUtils.isEmpty(pw) ? "" : pw);
            put("mobilePhone", TextUtils.isEmpty(mobilePhone) ? "" : mobilePhone);
        }};

        JSONObject data = new JSONObject(params);

        post(url, data, headers, c);
    }
    
    /**
     * 회원탈퇴
     */
    public static void PostV2UserWithdrawal(final String userId, final String pw, Callback c) throws IOException {
        String url = URL_MAIN_V2 + URL_SUB_API_V2_USER_WITHDRAWAL;

        HashMap<String, String> headers = new HashMap<String, String>() {{
            put("Content-Type", "application/json");
            put("Authorization", CleanVentilationApplication.authData.accessToken);
        }};

        HashMap<String, Object> params = new HashMap<String, Object>() {{
            put("userId", TextUtils.isEmpty(userId) ? "" : userId);
            put("pw", TextUtils.isEmpty(pw) ? "" : pw);
        }};

        JSONObject data = new JSONObject(params);

        post(url, data, headers, c);
    }

    /**
     * 아이디찾기.
     */
    public static void PostV2FindId(final String rcId, Callback c) throws IOException {
        String url = URL_MAIN_V2 + URL_SUB_V2_FIND_ID;

        HashMap<String, Object> params = new HashMap<String, Object>() {{
            put("rcId", TextUtils.isEmpty(rcId) ? "" : rcId);
        }};

        JSONObject data = new JSONObject(params);

        post(url, data, c);
    }

    /**
     * ID & DEVICE ID 확인
     * (비밀번호 변경 전 체크)
     */
    public static void PostV2CheckUserIdGid(String rcId, int uid, Callback c) throws IOException {
        String url = URL_MAIN_V2 + URL_SUB_V2_CHECK_USER_ID_GID;
        post(url, PostV2CheckUserIdGidJson(rcId, uid), c);
    }

    private static String PostV2CheckUserIdGidJson(String rcId, int uid) {
        return "{\"rcId\":\"" + rcId + "\","
                + "\"uid\":\"" + uid + "\"}";
    }

    /**
     * 비밀번호 변경
     */
    public static void PostV2ChangePassword(final String newPassword, final String oldPassword, final String rcId, final String userId, Callback c) throws IOException {
        String url = URL_MAIN_V2 + URL_SUB_V2_CHANGE_PASSWORD;

        HashMap<String, String> headers = new HashMap<String, String>() {{
            put("Content-Type", "application/json");
            put("Authorization", CleanVentilationApplication.authData.accessToken);
        }};

        HashMap<String, Object> params = new HashMap<String, Object>() {{
            put("rcId", TextUtils.isEmpty(rcId) ? "" : rcId);
            put("userId", TextUtils.isEmpty(userId) ? "" : userId);
            put("newPw", TextUtils.isEmpty(newPassword) ? "" : newPassword);
            put("oldPw", TextUtils.isEmpty(oldPassword) ? "" : oldPassword);
        }};

        JSONObject data = new JSONObject(params);

        post(url, data, headers, c);
    }

    /**
     * ikHwang 2020-11-25 오전 8:32 사용자 비밀번호 변경
     * @param newPassword
     * @param rcId
     * @param userId
     * @param c
     * @throws IOException
     */
    public static void PostV2ResetPassword(final String newPassword, final String rcId, final String userId, Callback c) throws IOException {
        String url = URL_MAIN_V2 + URL_SUB_V2_RESET_PASSWORD;

        HashMap<String, Object> params = new HashMap<String, Object>() {{
            put("rcId", TextUtils.isEmpty(rcId) ? "" : rcId);
            put("userId", TextUtils.isEmpty(userId) ? "" : userId);
            put("newPw", TextUtils.isEmpty(newPassword) ? "" : newPassword);
        }};

        JSONObject data = new JSONObject(params);

        post(url, data, c);
    }

    /**
     * * 로그인
     */
    public static void PostV2UserLogin(final String userId, final String pw, final String appVer, final String pushToken, final String captchaToken, Callback c) throws IOException {
        String url = URL_MAIN_V2 + URL_SUB_V2_USER_LOGIN;

        HashMap<String, Object> params = new HashMap<String, Object>() {{
            put("userId", TextUtils.isEmpty(userId) ? "" : userId);
            put("pw", TextUtils.isEmpty(pw) ? "" : pw);

            put("os", "a");
            put("osVer", Build.VERSION.RELEASE);
            put("modelInfo", Build.MODEL);

            if(!TextUtils.isEmpty(appVer)) put("appVer", appVer);
            if(!TextUtils.isEmpty(pushToken)) put("pushToken", pushToken);
            if(!TextUtils.isEmpty(captchaToken)) put("captchaToken", captchaToken);
        }};

        JSONObject data = new JSONObject(params);

        post(url, data, c);
    }

    /**
     * 생활 리포트
     */
    public static void PostV2GetReport(final String userId, final String dateFlag, final String selectFlag, final String date, Callback c) throws IOException {
        String url = URL_MAIN_V2 + URL_SUB_V2_GET_REPORT;

        HashMap<String, String> headers = new HashMap<String, String>() {{
            put("Content-Type", "application/json");
            put("Authorization", CleanVentilationApplication.authData.accessToken);
        }};

        HashMap<String, Object> params = new HashMap<String, Object>() {{
            put("userId", TextUtils.isEmpty(userId) ? "" : userId);
            put("dateFlag", TextUtils.isEmpty(dateFlag) ? "" : dateFlag);
            put("selectFlag", TextUtils.isEmpty(selectFlag) ? "" : selectFlag);

            if(!TextUtils.isEmpty(date)) put("date", date);
        }};

        JSONObject data = new JSONObject(params);

        post(url, data, headers, c);
    }

    /**
     * 스마트 알림
     */
    public static void PostV2GetSmartAlarm(final int page, final int pageRowCount, final String userId, final String sbId, Callback c) throws IOException {
        String url = URL_MAIN_V2 + URL_SUB_V2_GET_SMARTALARM;

        HashMap<String, String> headers = new HashMap<String, String>() {{
            put("Content-Type", "application/json");
            put("Authorization", CleanVentilationApplication.authData.accessToken);
        }};

        HashMap<String, Object> params = new HashMap<String, Object>() {{
            put("page", page);
            put("pageRowCount", pageRowCount);
            put("userId", TextUtils.isEmpty(userId) ? "" : userId);
            put("sbId", TextUtils.isEmpty(sbId) ? "" : sbId);
        }};

        JSONObject data = new JSONObject(params);

        post(url, data, headers, c);
    }

    /**
     * 제품정보 변경
     */
    public static void PostV2ChangeLocation(final String userId, final float lat, final float lng, final String area, final String rcId, Callback c) throws IOException {
        String url = URL_MAIN_V2 + URL_SUB_V2_CHANGE_LOCATION;

        HashMap<String, String> headers = new HashMap<String, String>() {{
            put("Content-Type", "application/json");
            put("Authorization", CleanVentilationApplication.authData.accessToken);
        }};

        HashMap<String, Object> params = new HashMap<String, Object>() {{
            put("userId", TextUtils.isEmpty(userId) ? "" : userId);
            put("rcId", TextUtils.isEmpty(rcId) ? "" : rcId);
            put("lat", String.valueOf(lat));
            put("lng", String.valueOf(lng));

            if(!TextUtils.isEmpty(area))  put("area", area);
        }};

        JSONObject data = new JSONObject(params);

        post(url, data, headers, c);
    }

    /**
     * 제품 SSID 변경
     * @param userId
     * @param rcId
     * @param ssid
     * @param c
     * @throws IOException
     */
    public static void PostV2ChangeSSID(final String userId, final String rcId, final String ssid, Callback c) throws IOException {
        String url = URL_MAIN_V2 + URL_SUB_V2_CHANGE_SSID;

        HashMap<String, String> headers = new HashMap<String, String>() {{
            put("Content-Type", "application/json");
            put("Authorization", CleanVentilationApplication.authData.accessToken);
        }};

        HashMap<String, Object> params = new HashMap<String, Object>() {{
            put("userId", TextUtils.isEmpty(userId) ? "" : userId);
            put("rcId", TextUtils.isEmpty(rcId) ? "" : rcId);

            if(!TextUtils.isEmpty(ssid))  put("ssid", ssid);
        }};

        JSONObject data = new JSONObject(params);

        post(url, data, headers, c);
    }

    /**
     * 푸시 설정
     * @param userId
     * @param rcId
     * @param pushUse
     * @param smartAlarm
     * @param filterAlarm
     * @param c
     * @throws IOException
     */
    public static void PostV2ChangePush(final String userId, final String rcId, final int pushUse, final int smartAlarm, final int filterAlarm, Callback c) throws IOException {
        String url = URL_MAIN_V2 + URL_SUB_V2_CHANGE_PUSH;

        HashMap<String, String> headers = new HashMap<String, String>() {{
            put("Content-Type", "application/json");
            put("Authorization", CleanVentilationApplication.authData.accessToken);
        }};

        HashMap<String, Object> params = new HashMap<String, Object>() {{
            put("userId", TextUtils.isEmpty(userId) ? "" : userId);
            put("rcId", TextUtils.isEmpty(rcId) ? "" : rcId);

            put("pushUse", (1 == pushUse) ? pushUse : 0);
            put("smartAlarm", (1 == smartAlarm) ? smartAlarm : 0);
            put("filterAlarm", (1 == filterAlarm) ? filterAlarm : 0);
        }};

        JSONObject data = new JSONObject(params);

        post(url, data, headers, c);
    }

    /**
     * 로그아웃
     */
    public static void PostV2UserLogout(final String userId, final String pushToken, Callback c) throws IOException {
        String url = URL_MAIN_V2 + URL_SUB_V2_USER_LOGOUT;

        HashMap<String, String> headers = new HashMap<String, String>() {{
            put("Content-Type", "application/json");
            put("Authorization", CleanVentilationApplication.authData.accessToken);
        }};

        HashMap<String, Object> params = new HashMap<String, Object>() {{
            put("userId", TextUtils.isEmpty(userId) ? "" : userId);
            put("pushToken", TextUtils.isEmpty(pushToken) ? "" : pushToken);
        }};

        JSONObject data = new JSONObject(params);

        post(url, data, headers, c);
    }

    /**
     * ikHwang 2020-12-08 오전 10:35 이용약관 재동의
     * @param userId
     * @param pw
     * @param terms
     * @param privacyPolicy
     * @param over14age
     * @param c
     * @throws IOException
     */
    public static void PostV2SetAgreement(final String userId, final String pw, final int terms, final int privacyPolicy, final int over14age, Callback c) throws IOException {
        String url = URL_MAIN_V2 + URL_SUB_V2_USER_SET_AGREEMENT;

        HashMap<String, String> headers = new HashMap<String, String>() {{
            put("Content-Type", "application/json");
            put("Authorization", CleanVentilationApplication.authData.accessToken);
        }};

        HashMap<String, Object> params = new HashMap<String, Object>() {{
            put("userId", TextUtils.isEmpty(userId) ? "" : userId);
            put("pw", TextUtils.isEmpty(pw) ? "" : pw);

            if(terms > -1) put("terms", terms);
            if(privacyPolicy > -1) put("privacyPolicy", privacyPolicy);
            if(over14age > -1) put("over14age", over14age);
        }};

        JSONObject data = new JSONObject(params);

        post(url, data, headers, c);
    }
    //====================================================================================================
    // 2차 API 끝
    //====================================================================================================
}
