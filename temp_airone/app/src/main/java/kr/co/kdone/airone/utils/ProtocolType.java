package kr.co.kdone.airone.utils;

/**
 * Created by LIM on 2018-01-17.
 */

public class ProtocolType {
    //FIRST BYTE
    public static final int STX = 0xF7;

    //SOURCE ID, DESTINATION ID
    public static final int SERVER = 0x01;
    public static final int APP = 0x02;
    public static final int DEVICE = 0x03;

    //PROTOCOL ID
    public static final int PROTOCOL_VER_ID1 = 0x01;
    public static final int PROTOCOL_VER_ID2 = 0x02;
    public static final int PROTOCOL_VER_ID3 = 0x03; // 듀얼링크
    public static final int PROTOCOL_VER_ID4 = 0x04; // 프리즘
    public static final int PROTOCOL_VER_ID5 = 0x05; // 제습청정

    //COMMAND
    //Device -> Server
    public static final int COMMAND_FIRST_CONNECTION = 0x00; //초기소켓접속 및 서버등록절차 수행
    public static final int COMMAND_ALIVE_CHECK = 0x01; //TAC Big Data 수ㅜ집을 위한 정보 전달
    public static final int COMMAND_HEALTH_CHECK = 0x02; //TAC 소켓 통신 체크
    public static final int COMMAND_DID = 0x03; //TAC 기기정보 전달
    public static final int COMMAND_EID = 0x04; //에러 발생 시 이벤트로 에러정보 전달
    public static final int COMMAND_SD = 0x05; //상태정보 발생시 이벤트로 전달(1.원결 제어 요청시, 2.로컬정보 갱신시)
    public static final int COMMAND_PSD = 0x06; //PUSH 정보 발생 시 이벤트로 전달
    public static final int COMMAND_SENSOR_STATUS_INFO = 0x07; //센서 상태 데이터 전달(1분 간격)<센싱박스 전용>(TAC 해당X)
    public static final int COMMAND_LED_STATUS_INFO = 0x89; // 에어모니터 LED 상태 조회
    public static final int COMMAND_FILTER_STATUS_INFO = 0x8B; // 필터 상태 조회

    //Server -> Device
    public static final int COMMAND_FIRST_CONNECTION_ACK = 0x64; //초기소켓접속 및 서버등록절차 수행
    public static final int COMMAND_ALIVE_CHECK_ACK = 0x65; //TAC Big Data 수ㅜ집을 위한 정보 전달
    public static final int COMMAND_HEALTH_CHECK_ACK = 0x66; //TAC 소켓 통신 체크
    public static final int COMMAND_DID_ACK = 0x67; //TAC 기기정보 전달
    public static final int COMMAND_EID_ACK = 0x68; //에러 발생 시 이벤트로 에러정보 전달
    public static final int COMMAND_SD_ACK = 0x69; //상태정보 발생시 이벤트로 전달(1.원결 제어 요청시, 2.로컬정보 갱신시)
    public static final int COMMAND_PSD_ACK = 0x70; //PUSH 정보 발생 시 이벤트로 전달
    public static final int COMMAND_SENSOR_STATUS_INFO_ACK = 0x71; //센서 상태 데이터 전달(1분 간격)<센싱박스 전용>(TAC 해당X)
    public static final int COMMAND_END_REMOTE_CONTROLL = 0xFF; //원격제어 종료 알림(CMD만 전달 3회 발송)
    //APP->SERVER->DEVICE
    public static final int COMMAND_CONTROL_DATA = 0xFA; //원격제어
    public static final int COMMAND_LED_DATA = 0x88; // 에어모니터 LED 설정
    public static final int COMMAND_FILTER_DATA = 0x8A; // 필터 관리


    //국가코드
    public static final int CONTURY_CODE_KOREA = 0x01;
    public static final int CONTURY_CODE_CHINA = 0x02;

    //모델코드
    public static final int MODEL_CODE_TAC750_20S = 0x01;
    public static final int MODEL_CODE_TAC710_20S = 0x02;
    public static final int MODEL_CODE_TAC730_20S = 0x03;
    public static final int MODEL_CODE_TAC740_20S = 0x04;
    public static final int MODEL_CODE_TAC550_20S = 0x05;
    public static final int MODEL_CODE_TAC510_20S = 0x06;
    public static final int MODEL_CODE_TAC530_20S = 0x07;
    public static final int MODEL_CODE_TAC500_20 = 0x08;
    public static final int MODEL_CODE_TAC500_20S = 0x09;
    public static final int MODEL_CODE_NTS_10S = 0x1E;
    public static final int MODEL_CODE_NTS_1S = 0x1F;

    //센서박스 연결 유무
    public static final int SENSOR_BOX_NO_CONNECTION = 0x01;
    public static final int SENSOR_BOX_CONNECTION = 0x02;

    //use,not use
    public final static int USE_NOT = 0x01;
    public final static int USE = 0x02;

    //filter not use, reset
    public final static int FILTER_NOT_USE = 0x01;
    public final static int FILTER_RESET = 0x02;

    //on, off
    public final static int OFF = 0x01;
    public final static int ON = 0x02;

    //REQUEST
    public final static int REQ_DID = 0x01;
    public final static int REQ_REMOTE_CONTROL = 0x02;
    public final static int REQ_STATUS_DATA = 0x03;

    //MODE
    public final static int MODE_DEHUMI = 0x01;
    public final static int MODE_VENTI = 0x02;
    public final static int MODE_EXHAUST = 0x03;
    public final static int MODE_KICHEN = 0x04;
    public final static int MODE_CLEAN = 0x05;
    public final static int MODE_AIR_CLEAN = 0x06;
    public final static int MODE_DUST = 0x07;
    public final static int MODE_AUTO = 0x08;

    //MODE_OPTION & 옵션기능
    public final static int MODE_OPTION_NOTING = 0x01;
    public final static int MODE_OPTION_TURBO = 0x02;
    public final static int MODE_OPTION_PWR_SAVING = 0x03;
    public final static int MODE_OPTION_SLEEP = 0x04; //앱에서만 사용 서버&디바이스 프로토콜 없음
    public final static int MODE_OPTION_BYPASS = 0x05;

    //예약 기능
    public final static int RESERVATION_NO = 0x01;
    public final static int RESERVATION_SLEEP = 0x02;
    public final static int RESERVATION_ON = 0x03;
    public final static int RESERVATION_OFF = 0x04;


    //WIND & 풍량설정값 & ODU 풍량현재값
    public final static int WIND_LEVEL_1 = 0x01;
    public final static int WIND_LEVEL_2 = 0x02;
    public final static int WIND_LEVEL_3 = 0x03;
    public final static int WIND_LEVEL_AUTO = 0x04;

    //SCHEDULE_TYPE
    public final static int SCHEDULE_TYPE_NO = 0x01;
    public final static int SCHEDULE_TYPE_SLEEP = 0x02;
    public final static int SCHEDULE_TYPE_ON = 0x03;
    public final static int SCHEDULE_TYPE_OFF = 0x04;

    //운전 유무
    public final static int OPERATION_EXIST = 0x01;
    public final static int OPERATION_NONEXIST = 0x02;

    //VSP/RPM 제거
    public final static int VSP_RPM_CONTROL_RPM = 0x01;
    public final static int VSP_RPM_CONTROL_VSP = 0x02;
    public final static int VSP_RPM_CONTROL_DF_VSP = 0x03;
    public final static int VSP_RPM_CONTROL_KPV_VSP = 0x04;

    //ODU MODEL 판단
    public final static int ODU_MODEL_11KW = 0x01;
    public final static int ODU_MODEL_09KW = 0x02;
    public final static int ODU_MODEL_07KW = 0x03;
    public final static int ODU_MODEL_05KW = 0x04;
    public final static int ODU_MODEL_PV_HEAT = 0x05;
    public final static int ODU_MODEL_HOT = 0x06;
    public final static int ODU_MODEL_NORMAL = 0x07;

    //모드 & 옵션 사용 유무
    public final static int MODE_USED = 0x02;
    public final static int MODE_NOT_USED = 0x01;

    //정지/동작 유무
    public final static int OPERATION_STATUS_OFF = 0x01;
    public final static int OPERATION_STATUS_ON = 0x02;

    //동작 모드 & ODU 동작 상태
    public final static int OPERATION_MODE_IDLE = 0x01;
    public final static int OPERATION_MODE_COLD = 0x02;
    public final static int OPERATION_MODE_DEHUMI = 0x03;
    public final static int OPERATION_MODE_VENTI = 0x04;
    public final static int OPERATION_MODE_EXHAUST = 0x05;
    public final static int OPERATION_MODE_KITCHEN = 0x06;
    public final static int OPERATION_MODE_CLEAN = 0x07;
    public final static int OPERATION_MODE_AIR_CLEAN = 0x08;
    public final static int OPERATION_MODE_DUST = 0x09;
    public final static int OPERATION_MODE_HOT = 0x0A;
    public final static int OPERATION_MODE_HUMI = 0x0B;
    public final static int OPERATION_MODE_AUTO = 0x0C;
    public final static int OPERATION_MODE_HEALTH = 0x0D;
    public final static int OPERATION_MODE_SKIN = 0x0E;
    public final static int OPERATION_MODE_AERATION = 0x0F;
    public final static int OPERATION_MODE_BYPASS = 0x11;

    //센서 도출 정성 값
    public final static int SENSOR_LEVEL_UNKNOWN = 0x00;
    public final static int SENSOR_LEVEL_GOOD = 0x01;
    public final static int SENSOR_LEVEL_NORMAL = 0x02;
    public final static int SENSOR_LEVEL_BAD = 0x03;
    public final static int SENSOR_LEVEL_VERY_BAD = 0x04;

    //ROOM
    public final static int ROOM_1 = 0x01; //LIVING ROOM
    public final static int ROOM_2 = 0x02; //KITCHEN
    public final static int ROOM_3 = 0x03;
    public final static int ROOM_4 = 0x04;
    public final static int ROOM_5 = 0x01;
    public final static int ROOM_6 = 0x02;
    public final static int ROOM_7 = 0x03;
    public final static int ROOM_88 = 0x04;

    public final static int LED_REQ = 0x01; // 에어모니터 LED 상태 조회
    public final static int LED_CONTROL = 0x02; // 에어모니터 LED 상태 변경

    public final static int FILTER_REQ = 0x01; // 필터 상태 조회
    public final static int FILTER_CONTROL = 0x02; // 필터 상태 변경
}
