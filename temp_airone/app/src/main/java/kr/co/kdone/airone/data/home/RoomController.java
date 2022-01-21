package kr.co.kdone.airone.data.home;

// ikHwang 2019-05-21 오전 9:44 룸콘트롤러 정보 클래스
public class RoomController extends CommonDevice{
    private String lat; // 위도
    private String lng; // 경도
    private String modelCode; // 룸 컨트롤러 모델 코드 MQTT Topic modelCode로 사용

    public String getLat() {
        return lat;
    }

    public void setLat(String lat) {
        this.lat = lat;
    }

    public String getLng() {
        return lng;
    }

    public void setLng(String lng) {
        this.lng = lng;
    }

    public String getModelCode() {
        return modelCode;
    }

    public void setModelCode(String modelCode) {
        this.modelCode = modelCode;
    }
}
