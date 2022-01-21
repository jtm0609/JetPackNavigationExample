package kr.co.kdone.airone.data;

/**
 * 에러 정보 패킷
 */

public class EidPacket {
    public int swVersion; //1byte value * 10
    public int reserve1; //1byte 00:default
    public int reserve2; //1byte 00:default
    public int errorLevel;
    public int errorCode;
    public int reserve3; //1byte 00:default
    public int reserve4; //1byte 00:default
    public int errorTime;
    public int errorCount;
    public int reserve5; //1byte 00:default
    public int reserve6; //1byte 00:default
    public long lastUpdateTime;

    public EidPacket() {
        lastUpdateTime = -1;
    }
}
