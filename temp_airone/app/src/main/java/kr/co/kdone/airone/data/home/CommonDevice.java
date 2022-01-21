package kr.co.kdone.airone.data.home;

import kr.co.kdone.airone.data.AirMonitor;
import kr.co.kdone.airone.utils.ProtocolType;

// ikHwang 2019-05-23 오후 5:43 룸콘과 에어모니터 공통 정보
public class CommonDevice {
    private String gid; // 디바이스 고유ID
    private int pcd; // PCD 제품구분 코드 (1:1차 룸콘트롤러, 2:1차 에어 모니터, 3:2차 룸 콘트롤러, 3:2차 에어 모니터) - 2차 에어모니터는 룸콘의 데이터를 강제 설정함
    private int state; // 디바이스 접속 상태 (0:연결 해제, 1: 연결됨)
    private int deviceVer; // 디바이스 버전
    private int sensingBoxConnect; // 에어 모니터 연결 상태 state 상태로 처리하여 사용하지 않음
    private String nickName; // 디바이스 별칭
    private String area; // 주소
    private String sensingBox1; // 에어모니터 고유 ID (에어 모니터를 별도로 내려주기 때문에 사용하지 않음)
    private int power; // 전원 상태 (state를 이용하여 사용하지 않음)
    private String ssid; // 접속된 공유기 ID
    private int pusheUse; // 푸시 사용 여부(1:미사용, 2:사용)
    private int smartAlarm; // 스마트알림 사용 여부(1:미사용, 2:사용)
    private int filterAlarm; // 필터알림 사용 여부(1:미사용, 2:사용)
    private AirMonitor airMonitorType;           // 에어모니터 연동 타입

    public String getGid() {
        return gid;
    }

    public void setGid(String gid) {
        this.gid = gid;
    }

    public int getPcd() {
        return pcd;
    }

    public void setPcd(int pcd) {
        this.pcd = pcd;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public int getDeviceVer() {
        return deviceVer;
    }

    public void setDeviceVer(int deviceVer) {
        this.deviceVer = deviceVer;
    }

    public int getSensingBoxConnect() {
        return sensingBoxConnect;
    }

    public void setSensingBoxConnect(int sensingBoxConnect) {
        this.sensingBoxConnect = sensingBoxConnect;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public String getArea() {
        return area;
    }

    public void setArea(String area) {
        this.area = area;
    }

    public String getSensingBox1() {
        return sensingBox1;
    }

    public void setSensingBox1(String sensingBox1) {
        this.sensingBox1 = sensingBox1;
    }

    public int getPower() {
        return power;
    }

    public void setPower(int power) {
        this.power = power;
    }

    public String getSsid() {
        return ssid;
    }

    public void setSsid(String ssid) {
        this.ssid = ssid;
    }

    public int getPusheUse() {
        return pusheUse;
    }

    public void setPusheUse(int pusheUse) {
        this.pusheUse = pusheUse;
    }

    public int getSmartAlarm() {
        return smartAlarm;
    }

    public void setSmartAlarm(int smartAlarm) {
        this.smartAlarm = smartAlarm;
    }

    public int getFilterAlarm() {
        return filterAlarm;
    }

    public void setFilterAlarm(int filterAlarm) {
        this.filterAlarm = filterAlarm;
    }

    public AirMonitor getAirMonitorType() {
        return airMonitorType;
    }

    public void setAirMonitorType(AirMonitor airMonitorType) {
        this.airMonitorType = airMonitorType;
    }
}
