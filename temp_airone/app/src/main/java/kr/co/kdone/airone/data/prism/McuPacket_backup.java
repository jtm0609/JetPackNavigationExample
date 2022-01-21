package kr.co.kdone.airone.data.prism;

import android.util.Log;

import kr.co.kdone.airone.utils.CommonUtils;

public class McuPacket_backup {
    private final String TAG = getClass().getSimpleName();

    public static final int STX = 0xF7;
    public static final int APP = 0x02;
    public static final int DEVICE = 0x03;
    public static final int PROTOCOL = 0x04;

    public static final int REQ_COMMAND_GET_DEV_ID = 0xB0; // 현재 룸콘 아이디 조회 요청
    public static final int REQ_COMMAND_CHANGE_DEV_ID = 0xB1; // 신규 룸콘 아이디 조회 요청 (룸콘아이디 변경)
    public static final int REQ_COMMAND_WIFI_SCAN = 0xB4; // AP 리스트 조회 요청
    public static final int REQ_COMMAND_AP_CONNECT = 0xB5; // AP 연결 요청
    public static final int REQ_COMMAND_SERVER_CONNECT = 0xB9; // 룸콘아이디 서버 등록 완료 수신 완료 응답 요청

    public static final int RES_COMMAND_GET_DEV_ID = 0xC0; // 현재 룸콘 아이디 조회 응답
    public static final int RES_COMMAND_CHANGE_DEV_ID = 0xC1; // 신규 룸콘 아이디 조회 응답 (룸콘아이디 변경)
    public static final int RES_COMMAND_WIFI_SCAN = 0xCB4; // AP 리스트 조회 응답
    public static final int RES_COMMAND_AP_CONNECT = 0xC5; // AP 연결 상태 수신
    public static final int RES_COMMAND_SERVER_CONNECT = 0xC9; // 룸콘아이디 서버 등록 완료 수신 (해당 커맨드 수신후 0xB9로 수신 결과 전달)

    private byte[] stx = new byte[1]; // 0xF7 고정
    private byte[] sourceID = new byte[1]; // 송신 디바이스 (APP : 0x02, Device : 0x03)
    private byte[] destinationID = new byte[1]; // 수신 디바이스 (APP : 0x02, Device : 0x03)
    private byte[] protocolID = new byte[1]; // 프로토콜 버전 ID (청정환기 3차 프리즘 : 0x04)
    private byte[] command = new byte[1]; // 명령 command
    private byte[] length = new byte[2]; // 데이터 필드 길이
    private byte[] data = null; // 데이터
    private byte[] crc = new byte[2]; // CRC-16

    private boolean isCrcSuccess = false;

    public McuPacket_backup(){
        setStx(STX);
        setSourceID(APP);
        setDestinationID(DEVICE);
        setProtocolID(PROTOCOL);
    }

    public McuPacket_backup(byte[] packet){
        System.arraycopy(packet, 0, stx, 0, 1);
        System.arraycopy(packet, 0, sourceID, 1, 1);
        System.arraycopy(packet, 0, destinationID, 2, 1);
        System.arraycopy(packet, 0, protocolID, 3, 1);
        System.arraycopy(packet, 0, command, 4, 1);
        System.arraycopy(packet, 0, length, 5, 2);
        System.arraycopy(packet, 0, data, 7, getLength());
        System.arraycopy(packet, 0, crc, (7 + data.length), 2);


        byte[]checkCrc = getCreateCrc(packet);
        if (checkCrc[0] == crc[0] && checkCrc[1] == crc[1]) {
            isCrcSuccess = true;
        } else {
            isCrcSuccess = false;
        }
    }

    public byte[] getMcuPacket(){
        int dataLength = 0;
        if(null != data) dataLength = data.length;

        byte packetData[] = new byte[(9 + dataLength)];

        System.arraycopy(stx, 0, packetData, 0, 1);
        System.arraycopy(sourceID, 0, packetData, 1, 1);
        System.arraycopy(destinationID, 0, packetData, 2, 1);
        System.arraycopy(protocolID, 0, packetData, 3, 1);
        System.arraycopy(command, 0, packetData, 4, 1);
        System.arraycopy(length, 0, packetData, 5, 2);
        if(null != data) System.arraycopy(data, 0, packetData, 7, dataLength);
        System.arraycopy(getCreateCrc(packetData), 0, packetData, (7 + dataLength), 2);

        String packet = "";
        for (int i = 0; i < packetData.length; i++) {
            packet += String.format("%02x", packetData[i]) + " ";
        }

        CommonUtils.customLog(TAG, "SendPacket : " + packet, Log.ERROR);

        return packetData;
    }

    public int getStx() {
        return (int)stx[0];
    }

    public void setStx(int nStx) {
        stx[0] = (byte)nStx;
    }

    public int getSourceID() {
        return (int)sourceID[0];
    }

    public void setSourceID(int nSourceID) {
        sourceID[0] = (byte)nSourceID;
    }

    public int getDestinationID() {
        return (int)destinationID[0];
    }

    public void setDestinationID(int nDestinationID) {
        destinationID[0] = (byte)nDestinationID;
    }

    public int getProtocolID() {
        return (int)protocolID[0];
    }

    public void setProtocolID(int nProtocolID) {
        protocolID[0] = (byte)nProtocolID;
    }

    public int getCommand() {
        return (int)command[0];
    }

    public void setCommand(int nCommand) {
        command[0] = (byte)nCommand;
    }

    public int getLength() {
        return ((length[0] & 0xFF) << 8) + (length[1] & 0xFF);
    }

    public void setLength(int dataLength) {
        length[0] = (byte)(dataLength >> 8);
        length[1] = (byte)dataLength;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;

        setLength(data.length);
    }

    public byte[] getCrc() {
        return crc;
    }

    public void setCrc(byte[] bCrc) {
        this.crc = bCrc;
    }

    private byte[] getCreateCrc(byte[] data){
        byte[] returnData = new byte[2];

        int crc = CommonUtils.updateCRC(data, data.length-2);

        returnData[0] = (byte)(crc>>8);
        returnData[1] = (byte)crc;

        return returnData;
    }

    public boolean isCrcSuccess() {
        return isCrcSuccess;
    }
}
