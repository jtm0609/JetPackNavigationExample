package kr.co.kdone.airone.data.home;

// ikHwang 2019-05-21 오전 11:11 메인화면 실외 정보
public class OutSide {
    private String  cityDept1;      // 시,도
    private String  cityDept2;      // 시,군,구
    private int     icon;           // 날씨 아이콘
    private double  temp;           // 기온
    private double  sensTemp;       // 체감 온도
    private double  humi;           // 습도
    private double  rainFall;       // 강수량
    private double  snowFall;       // 강설량
    private double  pressure;       // 기압
    private double  windDir;        // 풍향
    private String  vs;             // 확인필요
    private String  updateTime;     // 측정 일시
    private int     dust10Sensor;   // pm10 측정 등급
    private double  dust10Value;    // pm10 측정 수치
    private int     dust25Sensor;   // pm2.5 측정 등급
    private double  dust25Value;    // pm2.5 측정 수치

    public String getCityDept1() {
        return cityDept1;
    }

    public void setCityDept1(String cityDept1) {
        this.cityDept1 = cityDept1;
    }

    public String getCityDept2() {
        return cityDept2;
    }

    public void setCityDept2(String cityDept2) {
        this.cityDept2 = cityDept2;
    }

    public int getIcon() {
        return icon;
    }

    public void setIcon(int icon) {
        this.icon = icon;
    }

    public double getTemp() {
        return temp;
    }

    public void setTemp(double temp) {
        this.temp = temp;
    }

    public double getSensTemp() {
        return sensTemp;
    }

    public void setSensTemp(double sensTemp) {
        this.sensTemp = sensTemp;
    }

    public double getHumi() {
        return humi;
    }

    public void setHumi(double humi) {
        this.humi = humi;
    }

    public double getRainFall() {
        return rainFall;
    }

    public void setRainFall(double rainFall) {
        this.rainFall = rainFall;
    }

    public double getSnowFall() {
        return snowFall;
    }

    public void setSnowFall(double snowFall) {
        this.snowFall = snowFall;
    }

    public double getPressure() {
        return pressure;
    }

    public void setPressure(double pressure) {
        this.pressure = pressure;
    }

    public double getWindDir() {
        return windDir;
    }

    public void setWindDir(double windDir) {
        this.windDir = windDir;
    }

    public String getVs() {
        return vs;
    }

    public void setVs(String vs) {
        this.vs = vs;
    }

    public String getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(String updateTime) {
        this.updateTime = updateTime;
    }

    public int getDust10Sensor() {
        return dust10Sensor;
    }

    public void setDust10Sensor(int dust10Sensor) {
        this.dust10Sensor = dust10Sensor;
    }

    public double getDust10Value() {
        return dust10Value;
    }

    public void setDust10Value(double dust10Value) {
        this.dust10Value = dust10Value;
    }

    public int getDust25Sensor() {
        return dust25Sensor;
    }

    public void setDust25Sensor(int dust25Sensor) {
        this.dust25Sensor = dust25Sensor;
    }

    public double getDust25Value() {
        return dust25Value;
    }

    public void setDust25Value(double dust25Value) {
        this.dust25Value = dust25Value;
    }
}
