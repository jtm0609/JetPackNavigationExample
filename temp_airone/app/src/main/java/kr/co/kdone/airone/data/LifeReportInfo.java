package kr.co.kdone.airone.data;


/**
 * 생활리포트 정보 저장을 위한 클래스
 */

public class LifeReportInfo {
    public String DateFlag;
    public String SelectFlag;

    // ikHwang 2019-06-11 오전 11:26 프리즘 모델 추가
    public double totalSensor;
    public double totalValue;
    public double pm1Sensor;
    public double pm1Value;

    public double DustSensor;
    public double DustValue;
    public double CO2Sensor;
    public double CO2Value;
    public double GASSensor;
    public double GASValue;
    public double SensorTemp;
    public double SensorHum;
    public String Time;
    public double pm10Value;
    public double pm25Value;
    public double pm10Sensor;
    public double pm25Sensor;
    public double outsideTemp;
    public double outsideHum;

    public LifeReportInfo() {
        DateFlag = "";
        SelectFlag = "";
        DustSensor = 0;
        DustValue = 0;
        CO2Sensor = 0;
        CO2Value = 0;
        GASSensor = 0;
        GASValue = 0;
        Time = "";
        pm10Value= 0;
        pm25Value= 0;
        pm10Sensor= 0;
        pm25Sensor= 0;
        outsideTemp= 0;
        outsideHum= 0;

        totalSensor = 0;
        totalValue = 0;
        pm1Sensor = 0;
        pm1Value = 0;
    }
}
