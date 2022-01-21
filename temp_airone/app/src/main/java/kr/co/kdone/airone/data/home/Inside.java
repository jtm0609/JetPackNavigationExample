package kr.co.kdone.airone.data.home;

// ikHwang 2019-05-21 오전 11:08 홈화면 센싱박스 측정 정보
public class Inside {
    private String  sensorGid;      // 측정 디바이스 고유 ID
    private int     dustLevel;      // 먼지 센서 측정 등급 pm2.5
    private double  dustValue;      // 먼지 센서 츨정 수치 pm2.5
    private int     dust1Level;     // 먼지 센서 측정 등급 pm1.0
    private double  dust1Value;     // 먼지 센서 츨정 수치 pm1.0
    private int     co2Level;       // CO2 센서 측정 등급
    private double  co2Value;       // CO2 센서 측정 수치
    private int     tvocLevel;      // 가스 센서 측정 등급
    private int     tvocIndex = -1; // TVOC 측청 index
    private double  tvocValue;      // 가스 센서 측정 수치
    private int     raLevel;        // 라돈 측정 등급
    private double  raValue;        // 라돈 측정 수치
    private int     totalLevel;     // 통합 공기질 측정 등급
    private double  totalValue;       // 통합 공기질 측정 수치
    private double  insideHeat;     // 실내 온도
    private double  insideHum;      // 실내 습도
    private int     power;          // 에어모니터 전원 상태
    private String  sensorSendTime; // 측정 일시
    private String  timdDiff;       // 측정 후 지난 시간(s)

    public String getSensorGid() {
        return sensorGid;
    }

    public void setSensorGid(String sensorGid) {
        this.sensorGid = sensorGid;
    }

    public int getDustLevel() {
        return dustLevel;
    }

    public void setDustLevel(int dustLevel) {
        this.dustLevel = dustLevel;
    }

    public double getDustValue() {
        return dustValue;
    }

    public void setDustValue(double dustValue) {
        this.dustValue = dustValue;
    }

    public int getDust1Level() {
        return dust1Level;
    }

    public void setDust1Level(int dust1Level) {
        this.dust1Level = dust1Level;
    }

    public double getDust1Value() {
        return dust1Value;
    }

    public void setDust1Value(double dust1Value) {
        this.dust1Value = dust1Value;
    }

    public int getCo2Level() {
        return co2Level;
    }

    public void setCo2Level(int co2Level) {
        this.co2Level = co2Level;
    }

    public double getCo2Value() {
        return co2Value;
    }

    public void setCo2Value(double co2Value) {
        this.co2Value = co2Value;
    }

    public int getTvocLevel() {
        return tvocLevel;
    }

    public void setTvocLevel(int tvocLevel) {
        this.tvocLevel = tvocLevel;
    }

    public double getTvocValue() {
        return tvocValue;
    }

    public void setTvocValue(double tvocValue) {
        this.tvocValue = tvocValue;
    }

    public int getTotalLevel() {
        return totalLevel;
    }

    public void setTotalLevel(int totalLevel) {
        this.totalLevel = totalLevel;
    }

    public double getTotalValue() {
        return totalValue;
    }

    public void setTotalValue(double totalValue) {
        this.totalValue = totalValue;
    }

    public double getInsideHeat() {
        return insideHeat;
    }

    public void setInsideHeat(double insideHeat) {
        this.insideHeat = insideHeat;
    }

    public double getInsideHum() {
        return insideHum;
    }

    public void setInsideHum(double insideHum) {
        this.insideHum = insideHum;
    }

    public int getPower() {
        return power;
    }

    public void setPower(int power) {
        this.power = power;
    }

    public String getSensorSendTime() {
        return sensorSendTime;
    }

    public void setSensorSendTime(String sensorSendTime) {
        this.sensorSendTime = sensorSendTime;
    }

    public String getTimdDiff() {
        return timdDiff;
    }

    public void setTimdDiff(String timdDiff) {
        this.timdDiff = timdDiff;
    }

    public int getTvocIndex() {
        return tvocIndex;
    }

    public void setTvocIndex(int tvocIndex) {
        this.tvocIndex = tvocIndex;
    }

    public int getRaLevel() {
        return raLevel;
    }

    public void setRaLevel(int raLevel) {
        this.raLevel = raLevel;
    }

    public double getRaValue() {
        return raValue;
    }

    public void setRaValue(double raValue) {
        this.raValue = raValue;
    }
}
