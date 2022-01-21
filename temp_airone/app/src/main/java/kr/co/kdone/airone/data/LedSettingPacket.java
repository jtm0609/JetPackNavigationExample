package kr.co.kdone.airone.data;

import static kr.co.kdone.airone.utils.ProtocolType.LED_REQ;

/**
 * ikHwang 2019-08-26 오후 3:45 에어모니터 LED 설정하기위한 패킷
 */
public class LedSettingPacket {

    private final int PACKET_LEN = 2;

    public int req; // 01:조회, 02:제어
    public int brightness; // 01 ~ 0F

    public LedSettingPacket() {
        req = LED_REQ;
        brightness = 0x00;
    }

    public void setReq(int req) {
        this.req = req;
    }

    public int getReq() {
        return req;
    }

    public void setBrightness(int brightness) {
        this.brightness = brightness;
    }

    public int getBrightness() {
        return brightness;
    }

    public byte[] toBytes(){

        byte[] returnData = new byte[PACKET_LEN];
        int index = 0;

        returnData[index++] = (byte)req;
        returnData[index++] = (byte) brightness;

        return returnData;
    }

    public int[] toIntArrays(){
        int[] returnData = new int[PACKET_LEN];
        int index = 0;

        returnData[index++] = req;
        returnData[index++] = brightness;

        return returnData;
    }

}
