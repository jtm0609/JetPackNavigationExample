package kr.co.kdone.airone.data;

/**
 * Created by LIM on 2018-03-28.
 */

public class WidgetInfo {
    public String tacID; //룸콘ID
    public int tacStatus; //1정지(전원Off), 2가동(전원On) - 센서박스 ID로 API 호출시 1 또는 NULL
    public int tacWifi; // TAC-서버 Connection(1연결,0끊김/센서박스로 검색시 0 또는 NULL)
    public String sensingBoxID; //센서박스 ID(센서박스 없는 룸콘으로 검색시 NULL/센서박스 ID로 API 호출시 호출한 센서박스 ID값이 표시되어야 함)
    public int sensingBoxWiFi; // 1정지(전원Off), 2가동(전원On) - 센서박스 없는 룸콘의 경우 1 또는 NULL
    public String area; //Device 등록된 위치 정보
    public int dust; //센서박스 미세먼지(센서박스 없는 룸콘의 경우  0 또는 NULL)
    public int tvoc; //센서박스 TVOC(센서박스 없는 룸콘의 경우  0 또는 NULL)
    public int co2; //센서박스 Co2(센서박스 없는 룸콘의 경우  0 또는 NULL)
    public int total; //센서박스 통합공기질(센서박스 없는 룸콘의 경우  0 또는 NULL)
    public long updatetime; //센서박스 업데이트 시간 (미세먼지,TVOC, CO2 받은 시간)
    public int msgSensor; //푸시메세지 코드
    public String message; //푸시메세지 (코드는 사용필요 X, Message만 사용)
    public long sendtime; //푸시 메시지 받은 시간

    public WidgetInfo() {
        tacID = "";
        tacStatus = 0;
        tacWifi = 0;
        sensingBoxID = "";
        sensingBoxWiFi = 0;
        area = "";
        dust = 0;
        tvoc = 0;
        co2 = 0;
        total = 0;
        updatetime = 0;
        msgSensor = 0;
        message = "";
        sendtime = 0;
    }
}
