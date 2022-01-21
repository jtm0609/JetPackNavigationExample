package kr.co.kdone.airone.data.prism;

/**
 * ikHwang 2019-07-18 오전 8:30 MCU(디바이스)에서 앱으로 패킷전달시 0x00 데이터를 전달 할 수 없어 스트링 문자열로 전달하도록 변경함
 * 앱에서 MCU 측으로 전달 하는 것에는 문제 없음
 */
public class McuPacket {
    private final String TAG = getClass().getSimpleName();

    public static final int PACKET_DATA_MAX_LENGTH          = 13; //  OS 10에서 eps32 모듈을 통해 전달 가능한 최대 패킷 길이 (커맨드 및 구분자를 제외한 최대 길이)

    public static final String SEND_CMD_SEPARATOR           = "|+|"; // CMD, 데이터간 구분자
    public static final String SEND_DATA_SEPARATOR          = "|.|"; // 데이터간 구분자
    public static final String SEND_END_SEPARATOR           = "|;|"; // 데이터 마지막 알림 구분자
    public static final String SEND_PIPE_SEPARATOR          = "|"; // 데이터 마지막 알림 구분자
    public static final String RECV_CMD_SEPARATOR           = "\\|\\+\\|"; // CMD, 데이터간 구분자
    public static final String RECV_DATA_SEPARATOR          = "\\|\\.\\|"; // 데이터간 구분자
    public static final String ARRAY_SEPARATOR              = "\\|\\&\\|"; // 리스트 구분자

    // ikHwang 2019-12-20 오전 8:36 Android 갤럭시S10e 단만에서 16byte 이상 전달시 오류가 발생하여 패킷 사이즈 줄이기 위해 0x 제거
    public static final String REQ_COMMAND_GET_DEV_ID       = "B0"; // 현재 룸콘 아이디 조회 요청
    public static final String REQ_COMMAND_CHANGE_DEV_ID    = "B1"; // 신규 룸콘 아이디 조회 요청 (룸콘아이디 변경)
    public static final String REQ_COMMAND_WIFI_SCAN        = "B4"; // AP 리스트 조회 요청
    public static final String REQ_COMMAND_AP_SSID          = "B5"; // AP 연결 요청
    public static final String REQ_COMMAND_AP_PW            = "B6"; // AP 연결 요청
    public static final String REQ_COMMAND_SERVER_CONNECT   = "B9"; // 룸콘아이디 서버 등록 완료 수신 완료 응답 요청

    public static final String RES_COMMAND_GET_DEV_ID       = "C0"; // 현재 룸콘 아이디 조회 응답
    public static final String RES_COMMAND_CHANGE_DEV_ID    = "C1"; // 신규 룸콘 아이디 조회 응답 (룸콘아이디 변경)
    public static final String RES_COMMAND_WIFI_SCAN        = "C4"; // AP 리스트 조회 응답
    public static final String RES_COMMAND_AP_SSID          = "C5"; // AP 연결 상태 수신
    public static final String RES_COMMAND_AP_PW            = "C6"; // AP 연결 상태 수신
    public static final String RES_COMMAND_SERVER_CONNECT   = "C9"; // 룸콘아이디 서버 등록 완료 수신 (해당 커맨드 수신후 0xB9로 수신 결과 전달)
}