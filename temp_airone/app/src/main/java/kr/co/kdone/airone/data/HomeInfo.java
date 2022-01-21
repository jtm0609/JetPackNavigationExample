package kr.co.kdone.airone.data;

/**
 * 홈 화면 구성을 위한 정보 저장 클래스
 */

public class HomeInfo {
    //실외정보
    public String region;
    public String state;
    public String city;
    public int ICON;
    public double TEMP;
    public double SENSTEMP;
    public int HUMI;
    public double RAINFALL;
    public double SNOWFALL;
    public double PRESSURE;
    public double WINDDIR;
    public double WINDSPEED;
    public double VS;
    public String UPDATETIME;
    public int DUST10SENSOR;
    public int DUST10VALUE;
    public int DUST25SENSOR;
    public int DUST25VALUE;
//    public int DUSTSENSOR;
//    public int DUSTVALUE;
//    public int CO2SENSOR;
//    public int CO2VALUE;
//    public int GASSENSOR;
//    public int GASVALUE;

    public boolean mIsHaveSensorData;
    //실내정보
    public String SEQ;
    public String GID;
    public int CountryCD;
    public int ModelCD;
    public int DeviceVer;
    public int SensingBoxConnect;
    public int SDATA01;
    public int SDATA02;
    public int SDATA03;
    public int SDATA04;
    public int SDATA05;
    public int dustLevel; //먼지센서 정성값, 00:오류, 01:좋음, 02:보통, 03:나쁨, 04,매우나쁨
    public int dustValue; //먼지센서 정량값
    public int co2Level; //CO2센서 정성값, FF:오류, 01:좋음, 02:보통, 03:나쁨, 04,매우나쁨
    public int co2Value; //CO2센서 정량값
    public int vocLevel; //가스,VOC센서 정성값, FF:오류, 01:좋음, 02:보통, 03:나쁨, 04,매우나쁨
    public int vocValue; //가스,VOC센서 정량값
    public int airLevel; //통합공기질 정성값, FF:오류, 01:좋음, 02:보통, 03:나쁨, 04,매우나쁨
    public int airValue; //통합공기질 정량값
    public String SENDTIME; //업데이트 시간
    public double INSIDEHEAT; //센서박스 온도
    public double INSIDEHUM; //센서박스 습도
    public String timeDiff; //측정 후 지난 시간

    public HomeInfo(){
        region = "00000";
        state = "";
        city = "";
        ICON = 0;
        TEMP = 0;
        SENSTEMP = 0;
        HUMI = 0;
        RAINFALL = 0;
        SNOWFALL = 0;
        PRESSURE = 0;
        WINDDIR = 0;
        WINDSPEED = 0;
        VS = 0;
        UPDATETIME = "";

        //실내정보
        SEQ = "0";
        GID = "0";
        CountryCD = 0;
        ModelCD = 0;
        DeviceVer = 0;
        SensingBoxConnect = 0;
        SDATA01 = 0;
        SDATA02 = 0;
        SDATA03 = 0;
        SDATA04 = 0;
        SDATA05 = 0;
        dustLevel = 0x00;
        dustValue = 0;
        co2Level = 0xFF;
        co2Value = 0;
        vocLevel = 0xFF;
        vocValue = 0;
        airLevel = 0xFF;
        airValue = 0;
        SENDTIME = "";
        INSIDEHEAT = 0.0;
        INSIDEHUM = 0.0;
    }
}
