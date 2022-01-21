package kr.co.kdone.airone.data;

/**
 * 메인 헤드 패킷
 */
public class HeadPacket {
    public int stx; //1byte
    public int src; //1byte
    public int dst; //1byte
    public int ver; //1byte
    public byte[] productID = new byte[8]; //8byte
    public int cmd; //1byte
    public int len_data; //2byte
    public long lastUpdateTime;

    public HeadPacket() {
        lastUpdateTime = -1;
    }
}
