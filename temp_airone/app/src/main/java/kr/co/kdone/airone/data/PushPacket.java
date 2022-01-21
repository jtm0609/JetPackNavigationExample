package kr.co.kdone.airone.data;


/**
 * 푸시 패킷
 */

public class PushPacket {
    public int co2CD; //1byte co2
    public int tvocCD; //1byte tvco
    public int pm25CD; //1byte pm2.5
    public long lastUpdateTime;

    public PushPacket() {
        lastUpdateTime = -1;
    }
}
