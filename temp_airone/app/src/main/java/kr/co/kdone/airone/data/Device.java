package kr.co.kdone.airone.data;

import static kr.co.kdone.airone.utils.ProtocolType.OPERATION_STATUS_ON;

/**
 * 제어기기 Device 정보
 */

public class Device {
    public HeadPacket head;
    public CommonPacket common;
    public DidPacket did;
    public EidPacket err;
    public SdPacket sd;
    public PushPacket push;
    public RemoteControlPacket control;
    public LedSettingPacket led;
    public FilterPacket filter;

    public Device() {
        head = new HeadPacket();
        common = new CommonPacket();
        did = new DidPacket();
        err = new EidPacket();
        sd = new SdPacket();
        push = new PushPacket();
        control = new RemoteControlPacket();
        led = new LedSettingPacket();
        filter = new FilterPacket();
    }

    public float getSettingTemp() {
        return sd.setTemp/2;
    }

    public float getCurruntTemp() {
        return sd.curruntTemp/2;
    }

    public int getSettingHumi() {
        return sd.setHumi/2;
    }

    public int getCurruntHumi() {
        return (int) Math.round(sd.curruntHumi/2.0);
    }

    public int getRemainReservationType() {
        return sd.reservation;
    }

    public int getRemainReservationTimer() {
        return sd.remainReservationTimer;
    }

    public int getSettingReservationTimer() {
        return sd.setReservationTimer;
    }

    public int getSetWindLevel() {
        return sd.setWindLevel;
    }

    public int getOperationMode() {
        return sd.operationMode;
    }

    public int getModeOption() {
        return sd.option;
    }

    public int getDeepSleepStart() {
        return sd.deepSleepStart;
    }

    public int getDeepSleepEnd() {
        return sd.deepSleepEnd;
    }

    public boolean isPowerOn() {
        return sd.operationStatus==OPERATION_STATUS_ON?true:false;
    }

    public int getUVLedMode() {
        return sd.uvLedMode;
    }

    public int getUVLedState() {
        return sd.uvLedState;
    }
}
