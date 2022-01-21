package kr.co.kdone.airone.data.home;

import kr.co.kdone.airone.data.WidgetRCMode;

// ikHwang 2019-05-21 오전 11:20 프리즘을 고려하여(List 처리)
public class CommonHomeInfo {
    private RoomController roomController;  // 룸콘 정보
    private CommonDevice airMonitor;        // 에어 모니터 정보
    private Inside inside;                  // 실내 정보
    private OutSide outSide;                // 실외 정보
    private WidgetRCMode widgetRCMode;      // 위젯 룸콘 상태

    public RoomController getRoomController() {
        return roomController;
    }

    public void setRoomController(RoomController roomController) {
        this.roomController = roomController;
    }

    public CommonDevice getAirMonitor() {
        return airMonitor;
    }

    public void setAirMonitor(CommonDevice airMonitor) {
        this.airMonitor = airMonitor;
    }

    public Inside getInside() {
        return inside;
    }

    public void setInside(Inside inside) {
        this.inside = inside;
    }

    public OutSide getOutSide() {
        return outSide;
    }

    public void setOutSide(OutSide outSide) {
        this.outSide = outSide;
    }

    public WidgetRCMode getWidgetRCMode() {
        return widgetRCMode;
    }

    public void setWidgetRCMode(WidgetRCMode widgetRCMode) {
        this.widgetRCMode = widgetRCMode;
    }
}
