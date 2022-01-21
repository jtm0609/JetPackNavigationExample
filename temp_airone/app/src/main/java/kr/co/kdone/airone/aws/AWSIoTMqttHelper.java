package kr.co.kdone.airone.aws;

import android.content.Context;
import android.os.Handler;
import android.util.Log;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.BasicSessionCredentials;
import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.internal.StaticCredentialsProvider;
import com.amazonaws.mobileconnectors.iot.AWSIotMqttClientStatusCallback;
import com.amazonaws.mobileconnectors.iot.AWSIotMqttManager;
import com.amazonaws.mobileconnectors.iot.AWSIotMqttMessageDeliveryCallback;
import com.amazonaws.mobileconnectors.iot.AWSIotMqttNewMessageCallback;
import com.amazonaws.mobileconnectors.iot.AWSIotMqttQos;
import com.amazonaws.mobileconnectors.iot.AWSIotMqttSubscriptionStatusCallback;
import com.amazonaws.regions.Regions;
import com.google.firebase.crashlytics.FirebaseCrashlytics;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import kr.co.kdone.airone.utils.CommonUtils;


/**
 * AWS MQTT를 사용하기 위한 클래스
 * mqtt 연결 후 10분 타임아웃 사용하는 서비스가 있을 수 있으므로 해당 사항 고려하여 추가할 것 (기본 타임아웃은 10분, 사용자 설정에 따라 변경 가능하도록 구현 할 것)
 * subscribe는 DisConnect 및 디바이스 변경등 일괄 등록 및 삭제가 필요함 해당 사항 고려하여 추가 할 것
 * publish또한 일괄 적용 해야할 필요가 있는지 고민 해볼것
 */
public class AWSIoTMqttHelper {
    private final String TAG = AWSIotMqttManager.class.getSimpleName();

    // 매니저 클래스에서 공통으로 사용하기 위한 멤버 변수 정의
    private Context mContext;
    private String mClientId;
    private String mEndPoint;
    private String mAccesskey;
    private String mSecretkey;
    private String mSessionToken;

    private static AWSIoTMqttHelper awsIoTMqttHelper;
    private static AWSIotMqttManager mqttManager;
    private KDmqttListener kdmqttListener;                      // 메시지 수신 및 에러처리 콜백
    private CognitoCachingCredentialsProvider credentialsProvider;
    private AWSIotMqttNewMessageCallback awsIotMqttNewMessageCallback;

    private Handler timeoutHandler;                     // 타임아웃 처리 핸들러
    private Handler connectionTimeoutHandler;           // 커넥션 타임아웃 처리 핸들러

    public final static int MQTT_CONNECT_ERROR = 0;        // 소켓 연결 에러
    public final static int MQTT_CONNECTION_TIMEOUT = 1;   // 타임아웃 에러
    public final static int MQTT_SEND_DATA_FAIL = 2;       // 데이터 전송 에러
    public final static int MQTT_RECEIVE_DATA_FAIL = 3;    // 데이터 전송 에러


    public final static int MQTT_INIT_CONNECTING        = 0;    // 
    public final static int MQTT_INIT_CONNECTED         = 1;    // 연결 됨
    public final static int MQTT_INIT_CONNECTION_LOST   = 2;    // 연결 해제
    public final static int MQTT_INIT_RECONNECTING      = 3;    // 
    public final static int MQTT_INIT_DISCONNECTED      = 4;    // 
    public final static int MQTT_INIT_SUBCRIBE_SUCCESS  = 5;    // subscribe 성공


    private int CONNECT_TIMEOUT = 10 * 60 * 1000;   // 제어 연결 타임아웃
    private int MQTT_CONNECTION_TIMER = 10 * 1000; //10 초 connect 타임아웃
    private int mState;
    private ArrayList<String> mlistTopic = new ArrayList<>();

    private boolean isAWSConn = false;
    private int subCnt = 0;

    /**
     * KD_BJS 2020-08-24 MQTT 연결 상태 및 수신 메시지 전달하기 위한 interface 정의
     */
    // AwsIotMqttManager를 사용하는 프로젝트에서 별도의 AWS 라이브러리 Gradle에 추가하지 않고 사용할 수 있도록 고려하여 추가 할것
    public interface KDmqttListener {
        void onMessageReceived(String topic, String message); // MQTT 데이터 수신 콜백

        void onError(int nState); // 연결 에러 콜백

        void onStatusChanged(int status, Throwable throwable);
    }

    /**
     * KD_BJS 2020-08-24 AwsIotMqttManager 객체 생성자 정의
     */
    public AWSIoTMqttHelper(Context context, String clientId, String iot_end_point, String sessionToken, String accessKey, String secretKey, KDmqttListener listener) {
        mContext = context;
        mClientId = clientId;
        mEndPoint = iot_end_point;
        mAccesskey = accessKey;
        mSecretkey = secretKey;
        mSessionToken = sessionToken;
        kdmqttListener = listener; // 메시지 수신 및 에러처리 콜백
        timeoutHandler = new Handler();
        connectionTimeoutHandler = new Handler();

        awsIotMqttNewMessageCallback = new AWSIotMqttNewMessageCallback() {
            @Override
            public void onMessageArrived(String topic, byte[] data) {
                CommonUtils.customLog(TAG, "&&&& mqttManager.subscribeToTopic : " + topic + ", resevicedData : " + ((null == data) ? "null" : new String(data)), Log.ERROR);
                if(null != kdmqttListener) kdmqttListener.onMessageReceived(topic, ((null == data) ? "null" : new String(data)));
            }
        };
     }

    public void setKdmqttListener(KDmqttListener listener){
        kdmqttListener = listener; // 메시지 수신 및 에러처리 콜백
    }

    /**
     * ikHwang 2020-08-25 10분 타이머 시간 변경
     * @param nTime
     */
    public void setConnectionTime(int nTime) {
        CONNECT_TIMEOUT = nTime;
    }

    /**
     * KD_BJS 2020-01-28 오후 2:05 mqtt 연결 해제
     */
    public void closeMqttManager() {
        if(awsIoTMqttHelper!=null){
            CommonUtils.customLog(TAG, "&&&& closeMqttManager unsubscribeAll", Log.ERROR);

            awsIoTMqttHelper.unsubscribeAll(); //구독하였던것 모두 취소
        }

        if (timeoutHandler != null) { //10분 타임아웃 핸들러 제거
            CommonUtils.customLog(TAG, "&&&& closeMqttManager timeoutHandler.removeCallbacks", Log.ERROR);

            timeoutHandler.removeCallbacks(temminuteTimeRunnable);
        }

        if(connectionTimeoutHandler != null){
            CommonUtils.customLog(TAG, "&&&& closeMqttManager connectionTimeoutHandler.removeCallbacks", Log.ERROR);

            connectionTimeoutHandler.removeCallbacks(connectionTimeRunnable);
        }

        CommonUtils.customLog(TAG, "&&&& closeMqttManager isAWSConn : " + isAWSConn, Log.ERROR);

        disconnect(); //mqttManager.disconnect()
        kdmqttListener = null;
        mState = -1;
        isAWSConn = false;
    }

    /**
     * KD_BJS 2020-08-24 MQTT Connect 정의
     */
    public void connect() {
        try {
            if (mqttManager == null) {
                mqttManager = new AWSIotMqttManager(mClientId, mEndPoint);
            }

            /**
             * KD_BJS 접속성공
             */
            BasicSessionCredentials basicSessionCredentials = new BasicSessionCredentials(mAccesskey, mSecretkey, mSessionToken);
            AWSCredentialsProvider credentialsProviderNew = new StaticCredentialsProvider(basicSessionCredentials);

            mqttManager.connect(credentialsProviderNew, new AWSIotMqttClientStatusCallback() {
                @Override
                public void onStatusChanged(final AWSIotMqttClientStatus status, final Throwable throwable) {
                    CommonUtils.customLog(TAG, "&&&& Status = " + status.toString(), Log.ERROR);

                    try {
                        //30초동안 connection 안될 경우 예외처리
                        if (status == AWSIotMqttClientStatusCallback.AWSIotMqttClientStatus.Connecting) {
                            // Client is attempting to connect.
                            mState = MQTT_INIT_CONNECTING;
                        } else if (status == AWSIotMqttClientStatusCallback.AWSIotMqttClientStatus.Connected) {
                            // Client successfully connected.
                            mState = MQTT_INIT_CONNECTED;
                            connectionTimeoutHandler.removeCallbacks(connectionTimeRunnable);
                            CommonUtils.customLog(TAG, "MQTT Connect TimeOut 30sec remove", Log.ERROR);

                            timeoutHandler.postDelayed(temminuteTimeRunnable, CONNECT_TIMEOUT);
                            CommonUtils.customLog(TAG, "MQTT Connect TimeOut 10min Start", Log.ERROR);

                            if(!isAWSConn){
                                isAWSConn = true;
                                CommonUtils.customLog(TAG, "&&&& mqttManager.connect isAWSConn : " + isAWSConn, Log.ERROR);
                            }
                            if (kdmqttListener != null) kdmqttListener.onStatusChanged(mState, throwable);
                        } else if (status == AWSIotMqttClientStatusCallback.AWSIotMqttClientStatus.ConnectionLost) {
                            // Connection was lost. Can be user initiated disconnect or network.
                            if(isAWSConn){
                                mState = MQTT_INIT_CONNECTION_LOST;
                                isAWSConn = false;
                                if (kdmqttListener != null) kdmqttListener.onStatusChanged(mState, throwable);
                            }
                        } else if (status == AWSIotMqttClientStatusCallback.AWSIotMqttClientStatus.Reconnecting) {
                            mState = MQTT_INIT_RECONNECTING;
                            //Automatically reconnecting after connection loss.
                        } else {
                            mState = MQTT_INIT_DISCONNECTED;
                        }
                    } catch (Exception e) {
                        mState = MQTT_INIT_DISCONNECTED;
                        isAWSConn = false;
                        if (kdmqttListener != null) kdmqttListener.onStatusChanged(mState, throwable);

                        CommonUtils.customLog(TAG, "Connection error : " + e.getMessage(), Log.ERROR);
                        FirebaseCrashlytics.getInstance().log("AWSIotMqttHelper connect method Exception : " + e.getMessage());
                    }
                }
            });
        } catch (Exception e) {
            if (kdmqttListener != null) {
                kdmqttListener.onError(MQTT_CONNECT_ERROR);
                closeMqttManager();
            }

            CommonUtils.customLog(TAG, "Connection error : " + e.getMessage(), Log.ERROR);
            FirebaseCrashlytics.getInstance().log("mqtt Connect Exception : " + e.getMessage());
            return;
        }

        isAWSConn = false;
        timeoutHandler.postDelayed(connectionTimeRunnable, MQTT_CONNECTION_TIMER);
        CommonUtils.customLog(TAG, "MQTT Connect TimeOut 30sec Start", Log.ERROR);
    }

    /**
     * KD_BJS 2020-08-24 MQTT DisConnect 정의
     */
    public void disconnect() {
        try {
            if (mqttManager != null) {
                CommonUtils.customLog(TAG, "&&&& disconnect mqttManager.disconnect()", Log.ERROR);
                mqttManager.disconnect();
            }
        } catch (Exception e) {
            CommonUtils.customLog(TAG, "Disconnect error : " + e.getMessage(), Log.ERROR);
            FirebaseCrashlytics.getInstance().log("mqtt disconnect Exception : " + e.getMessage());
        }
    }

    /**
     * KD_BJS 2020-08-24 MQTT publish 정의
     * @param topic
     * @param msg
     */
    public void publish(String topic, String msg) {
        try {
            CommonUtils.customLog(TAG, "PUBLISH topic = " + topic + "\n" + "payload : " + msg, Log.ERROR);
            mqttManager.publishString(msg, topic, AWSIotMqttQos.QOS1, new AWSIotMqttMessageDeliveryCallback() {
                @Override
                public void statusChanged(MessageDeliveryStatus status, Object userData) {
                    CommonUtils.customLog(TAG, "&&&& mqttManager.publishString : " + status + ", userData : " + userData, Log.ERROR);
                }
            }, null);
        } catch (Exception e) {
            CommonUtils.customLog(TAG, "Publish error : "   + e.getMessage(), Log.ERROR);
            FirebaseCrashlytics.getInstance().log("Publish error Exception : " + e.getMessage());
        }
    }

    /**
     * KD_BJS 2020-08-24 MQTT subscribe 정의
     * 일괄적 subscribe
     * @param topic
     */
    public void subscribe(String topic) {
        CommonUtils.customLog(TAG, "SUBSCRIBE topic = " + topic, Log.ERROR);
        try {
            mlistTopic.add(topic);

            mqttManager.subscribeToTopic(topic, AWSIotMqttQos.QOS1, new AWSIotMqttNewMessageCallback() {
                @Override
                public void onMessageArrived(String topic, byte[] data) {
                    if(null != kdmqttListener) kdmqttListener.onMessageReceived(topic, ((null == data) ? "null" : new String(data)));
                }
            });
        } catch (Exception e) {
            CommonUtils.customLog(TAG, "Subscription error : " + e.getMessage(), Log.ERROR);
            FirebaseCrashlytics.getInstance().log("Subscription error Exception : " + e.getMessage());
        }
    }

    /**
     * KD_BJS 2020-08-24 MQTT subscribe 정의
     * 일괄적 subscribe
     * @param
     */
    public void subscribeAll(ArrayList<String> listTopic) {
        CommonUtils.customLog(TAG, "SUBSCRIBE topic = " + listTopic, Log.ERROR);

        try {
            mlistTopic.addAll(listTopic);

            subCnt = 0;
            subCnt = listTopic.size();

            for (int i = 0; i < listTopic.size(); i++) {
                int finalI = i;
                mqttManager.subscribeToTopic(listTopic.get(i), AWSIotMqttQos.QOS1, new AWSIotMqttSubscriptionStatusCallback() {
                    @Override
                    public void onSuccess() {
                        CommonUtils.customLog(TAG, "&&&& subscribeToTopic : success " + finalI + ", subData : " + listTopic.get(finalI), Log.ERROR);
                        subCnt--;

                        if(subCnt == 0) kdmqttListener.onStatusChanged(MQTT_INIT_SUBCRIBE_SUCCESS, new Throwable());
                    }

                    @Override
                    public void onFailure(Throwable exception) {
                        CommonUtils.customLog(TAG, "&&&& subscribeToTopic : onFailure", Log.ERROR);
                    }
                },  awsIotMqttNewMessageCallback);
            }
        } catch (Exception e) {
            CommonUtils.customLog(TAG, "Subscription error : " + e.getMessage(), Log.ERROR);
            FirebaseCrashlytics.getInstance().log("Subscription error Exception : " + e.getMessage());
        }
    }

    /**
     * KD_BJS 2020-08-25 MQTT unsubscribe 정의
     * 일괄적 unsubscribe
     */
    public void unsubscribeAll() {
        try {
            if(mlistTopic !=null) {
                CommonUtils.customLog(TAG, "UNSUBSCRIBE topic = " + mlistTopic, Log.ERROR);

                for (int i = 0; i < mlistTopic.size(); i++) {
                    mqttManager.unsubscribeTopic(mlistTopic.get(i));
                }
            }
        } catch (Exception e) {
            CommonUtils.customLog(TAG, "UnSubscription error." + e, Log.ERROR);
            FirebaseCrashlytics.getInstance().log("UnSubscriptionAll error Exception : " + e.getMessage());
        }
    }

    /**
     * KD_BJS 2020-08-24 10분 타임아웃 처리
     */
    private Runnable temminuteTimeRunnable = new Runnable() {
        @Override
        public void run() {
            if (null != kdmqttListener) {
                CommonUtils.customLog(TAG, "MQTT Connect TimeOut 10min Finish", Log.ERROR);

                kdmqttListener.onError(MQTT_CONNECTION_TIMEOUT);
//                closeMqttManager();
            }
        }
    };


    private Runnable connectionTimeRunnable = new Runnable() {
        @Override
        public void run() {
            if (mState != 1 && null != kdmqttListener) {
                kdmqttListener.onError(MQTT_CONNECTION_TIMEOUT);
                closeMqttManager();
            }
        }
    };

    /**
     * KD_BJS 2020-08-27 credentialsProvider identity 리턴
     * @return getIdentity()
     */
    public String getIdetityId() {
        if (null == credentialsProvider) return "";

        return credentialsProvider.getIdentityId();
    }
}