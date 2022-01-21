package kr.co.kdone.airone.aws;

/**
 * ikHwang 2020-11-03 오후 4:14 MQTT 연결 및 데이터 수신 상태 콜백
 */
public interface DataChangeListener {
    void onDataChanged(int cmd);
    void onError(int nState); // 연결 에러 콜백
    void onStatusChanged(int status); // 연결 상태 콜백
}