package kr.co.kdone.airone.data;

/**
 * 제어통신 공통 패킷
 */
public class CommonPacket {
    public int countryCode; //1byte 국가코드   01:한국, 02:중국
    public int modelCode; //1byte 모델코드
    public int deviceVer; //1byte 디바이스 버전
    public int senboxConnected; //1byte 센서박스 연결 유무   01:연결안됨, 02:연결됨,(SB: default 0x00)
    public long lastUpdateTime;
}
