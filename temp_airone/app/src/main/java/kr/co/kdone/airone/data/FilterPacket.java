package kr.co.kdone.airone.data;

import static kr.co.kdone.airone.utils.ProtocolType.FILTER_NOT_USE;
import static kr.co.kdone.airone.utils.ProtocolType.FILTER_REQ;

/**
 * ikHwang 2019-08-26 오후 3:45 에어모니터 LED 설정하기위한 패킷
 */
public class FilterPacket {

    private final int PACKET_LEN = 4;

    public int req; // 01:조회, 02:제어
    public int flagHepaFilter; //1byte 헤파필터 교체 01:정상, 02:필터 수명 계산
    public int flagFreeFilter; //1byte 프리필터 청소 01:유지, 02:초기화
    public int flagEHepaFilter; //1byte 전자헤파필터 청소  01:유지, 02:초기화
    public int usingTimeOduFilter; //1byte 필터 남은 수명 %


    public FilterPacket() {
        req = FILTER_REQ;
        flagHepaFilter = FILTER_NOT_USE;
        flagFreeFilter = FILTER_NOT_USE;
        flagEHepaFilter = FILTER_NOT_USE;
    }

    public void setReq(int req) {
        this.req = req;
    }

    public int getReq() {
        return req;
    }

    public int getFlagHepaFilter() {
        return flagHepaFilter;
    }

    public void setFlagHepaFilter(int flagHepaFilter) {
        this.flagHepaFilter = flagHepaFilter;
    }

    public int getFlagFreeFilter() {
        return flagFreeFilter;
    }

    public void setFlagFreeFilter(int flagFreeFilter) {
        this.flagFreeFilter = flagFreeFilter;
    }

    public int getFlagEHepaFilter() {
        return flagEHepaFilter;
    }

    public void setFlagEHepaFilter(int flagEHepaFilter) {
        this.flagEHepaFilter = flagEHepaFilter;
    }

    public int getUsingTimeOduFilter() {
        return usingTimeOduFilter;
    }

    public void setUsingTimeOduFilter(int usingTimeOduFilter) {
        this.usingTimeOduFilter = usingTimeOduFilter;
    }

    public byte[] toBytes(){

        byte[] returnData = new byte[PACKET_LEN];
        int index = 0;

        returnData[index++] = (byte) req;
        returnData[index++] = (byte) flagHepaFilter;
        returnData[index++] = (byte) flagFreeFilter;
        returnData[index++] = (byte) flagEHepaFilter;

        return returnData;
    }

    public int[] toIntArrays(){
        int[] returnData = new int[PACKET_LEN];
        int index = 0;

        returnData[index++] = req;
        returnData[index++] = flagHepaFilter;
        returnData[index++] = flagFreeFilter;
        returnData[index++] = flagEHepaFilter;

        return returnData;
    }

}
