package kr.co.kdone.airone.data;

/**
 * Created by LIM on 2018-01-08.
 *
 * 2019.04.25 api 수정으로 인한 필드 변경.
 */

public class DeviceInfo {
    public String GID; //디바이스 아이디
    public String PCD; //디바이스 아이디의 뒤에서 4번째 숫자.Product Code
    public int State; //현재 접속상태. 1은 서버연결, 0은 서버연결 x
    public int DeviceVer; //디바이스에서 받은 Ver
    public String SensingBoxConnect; //json int -> string 으로 변경됨 //현재 TAC는 1로 저장. 쓸지 안쓸지 정해야..
    public String NickName; //디바이스 닉네임
    public String Area;//디바이스위치 (주소)
    public String SensingBox1; //현재 페어링된 센싱박스 ID
    public int Power; //전원 OFF상태 1, ON상태 2
    public String lat;
    public String lng;
    public String ssId;
    public int PushUse;
    public String SmartAlarm;
    public String FilterAlarm;
    public String area;

    public String TimeZone; //V2A 에서 사용안함.
    public int ModelCD; //V2A server response에 없음..
//    public String ZipCode;

//    public String SensingBox2; //페어링이 안되있으면 좌측편처럼 null로 오게됨.

    public int WifiLevel;

    public DeviceInfo() {
        GID = "";
        PCD = "";
        State = 0;
        DeviceVer = 0;
        SensingBoxConnect = "";
        NickName = "";
        Area = "";
        SensingBox1 = "null";
        PushUse = 1;
        WifiLevel = 0;
        Power = 0;
        lat = "37.4821258";
        lng = "126.87688569999997";
        ssId = "";
        SmartAlarm = "null";
        FilterAlarm = "null";
        TimeZone = "";
        area = "";
        ModelCD = 2; //V2A server response에 없음..//todo 실제 model code로 변경할것.
    }

}
