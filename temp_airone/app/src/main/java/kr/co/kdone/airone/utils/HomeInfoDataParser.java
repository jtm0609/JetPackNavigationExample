package kr.co.kdone.airone.utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import kr.co.kdone.airone.CleanVentilationApplication;
import kr.co.kdone.airone.data.AirMonitor;
import kr.co.kdone.airone.data.WidgetRCMode;
import kr.co.kdone.airone.data.home.CommonDevice;
import kr.co.kdone.airone.data.home.CommonHomeInfo;
import kr.co.kdone.airone.data.home.Inside;
import kr.co.kdone.airone.data.home.OutSide;
import kr.co.kdone.airone.data.home.RoomController;

// ikHwang 2019-05-22 오전 8:30 홈화면에 표시하기 위한 정보를 파싱하기 위한 클래스
public class HomeInfoDataParser {
    /**
     * ikHwang 2019-05-22 오전 8:30 홈화면 json 파싱
     * @param appication
     * @param obj
     * @param isMainInfo
     */
    public static void paserHomeInfo(CleanVentilationApplication appication, JSONObject obj, boolean isMainInfo){
        if(null == appication || null == obj) return;

        Inside tempInside = null;
        OutSide tempOutSide = null;

        ArrayList<CommonHomeInfo> tempHomeList = new ArrayList<>();

        try {
            // ikHwang 2020-09-09 오전 8:36
            // 20200908 듀얼링크 에어모니터 연결해제 상태 수신 불가문제로 신규 API 문서 수신시 해당정보 추가된것 확인하여 추가함
            if(obj.has("mainInterval")) SharedPrefUtil.putInt(SharedPrefUtil.SAVE_MAIN_INTERVAL, obj.optInt("mainInterval", 10));
            if(obj.has("email")) CleanVentilationApplication.getInstance().getUserInfo().setEmail(obj.optString("email", ""));
            if(obj.has("mobilePhone")) CleanVentilationApplication.getInstance().getUserInfo().setMobile(obj.optString("mobilePhone", ""));
            if(obj.has("name")) CleanVentilationApplication.getInstance().getUserInfo().setName(obj.optString("name", ""));
            if(obj.has("userId")) CleanVentilationApplication.getInstance().getUserInfo().setId(obj.optString("userId", ""));
            if(obj.has("uid")){
                CleanVentilationApplication.getInstance().getUserInfo().setUid(obj.optInt("uid", 0));
                SharedPrefUtil.putInt(SharedPrefUtil.U_ID, CleanVentilationApplication.getInstance().getUserInfo().getUid()); // 위젯에서 정보 조회하기 위해 사용
            }
            if(obj.has("isNewNotice")) CleanVentilationApplication.getInstance().getUserInfo().setIsNewNotice(obj.optInt("isNewNotice", 0));
            if(obj.has("isNewSmart")) CleanVentilationApplication.getInstance().getUserInfo().setIsNewSmart(obj.optInt("isNewSmart", 0));
            if(obj.has("joinDate")) CleanVentilationApplication.getInstance().getUserInfo().setJoinDate(obj.optString("joinDate", ""));

            CommonHomeInfo hInfo = new CommonHomeInfo();

            // 룸콘 정보 파싱
            if(obj.has("roomController")){
                JSONObject rcObj = null;

                try {
                    rcObj = obj.getJSONObject("roomController");
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                if(null != rcObj){
                    RoomController roomController = new RoomController();

                    roomController.setLat(rcObj.optString("lat", "37.4821258"));
                    roomController.setLng(rcObj.optString("lng", "126.87688569999997"));
                    roomController.setGid(rcObj.optString("gid", ""));
                    roomController.setPcd(rcObj.optInt("pcd", 0));
                    roomController.setState(rcObj.optInt("state", 0));
                    roomController.setDeviceVer(rcObj.optInt("deviceVer", 0));
                    roomController.setSensingBoxConnect(rcObj.optInt("sensingBoxConnect", 0));
                    roomController.setNickName(rcObj.optString("nickName", ""));
                    roomController.setArea(rcObj.optString("area", ""));
                    roomController.setPower(rcObj.optInt("power", 0));
                    roomController.setSsid(rcObj.optString("ssid", ""));
                    roomController.setPusheUse(rcObj.optInt("pusheUse", 1));
                    roomController.setSmartAlarm(rcObj.optInt("smartAlarm", 1));
                    roomController.setFilterAlarm(rcObj.optInt("filterAlarm", 1));
                    roomController.setModelCode(rcObj.optString("modelCode", "0"));

                    // 위젯 정보 조회하기 위해 룸콘 아이디 저장
                    SharedPrefUtil.putString(SharedPrefUtil.ROOM_CONTROLLER_ID, roomController.getGid());

                    hInfo.setRoomController(roomController);

                    if(roomController.getPcd() <= 4){
                        appication.setIsOldUser(true);
                    }else{
                        appication.setIsOldUser(false);
                    }
                }else{
                    hInfo.setRoomController(new RoomController());
                }
            }

            // 에어 모니터 정보 파싱
            if(obj.has("airMonitor")){
                JSONObject airObj = null;

                try {
                    airObj = obj.getJSONObject("airMonitor");
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                if(null != airObj){
                    CommonDevice airMonitor = new CommonDevice();

                    airMonitor.setGid(airObj.optString("gid", ""));
                    airMonitor.setPcd(airObj.optInt("pcd", 0));
                    airMonitor.setState(airObj.optInt("state", 0));
                    airMonitor.setDeviceVer(airObj.optInt("deviceVer", 0));
                    airMonitor.setSensingBoxConnect(airObj.optInt("sensingBoxConnect", 0));
                    airMonitor.setNickName(airObj.optString("nickName", ""));
                    airMonitor.setArea(airObj.optString("area", ""));
                    airMonitor.setPower(airObj.optInt("power", 0));
                    airMonitor.setSsid(airObj.optString("ssid", ""));
                    airMonitor.setPusheUse(airObj.optInt("pusheUse", 1));
                    airMonitor.setSmartAlarm(airObj.optInt("smartAlarm", 1));
                    airMonitor.setFilterAlarm(airObj.optInt("filterAlarm", 1));

                    // 에어모니터 연결 타입 정보 파싱 (신규 추가)
                    if(airObj.has("type")){
                        airMonitor.setAirMonitorType(AirMonitor.find(airObj.getString("type")));
                    }

                    hInfo.setAirMonitor(airMonitor);
                }
            }

            // 실내 정보 파싱
            if(obj.has("inSide")){
                JSONObject inObj = null;

                try {
                    inObj = obj.getJSONObject("inSide");
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                if(null != inObj){
                    Inside inside = new Inside();

                    inside.setSensorGid(inObj.optString("sensorGid", ""));
                    inside.setDustLevel(inObj.optInt("dustLevel", 0));
                    inside.setDustValue(inObj.optDouble("dustValue", 0));
                    inside.setDust1Level(inObj.optInt("dust1Level", 0));
                    inside.setDust1Value(inObj.optDouble("dust1Value", 0));
                    inside.setCo2Level(inObj.optInt("co2Level", 0));
                    inside.setCo2Value(inObj.optDouble("co2Value", 0));
                    inside.setTvocLevel(inObj.optInt("tvocLevel", 0));
                    inside.setTvocValue(inObj.optDouble("tvocValue", 0));
                    // TVOC index 추가
                    inside.setTvocIndex(inObj.optInt("tvocIndex", -1));
                    inside.setTotalLevel(inObj.optInt("totalLevel", 0));
                    inside.setTotalValue(inObj.optDouble("totalValue", 0));
                    // 라돈 센서 정보 추가
                    inside.setRaLevel(inObj.optInt("raLevel", 0));
                    inside.setRaValue(inObj.optDouble("raValue", 0));
                    inside.setInsideHeat(inObj.optDouble("insideHeat", 0));
                    inside.setInsideHum(inObj.optDouble("insideHum", 0));
                    inside.setPower(inObj.optInt("power", 0));
                    inside.setSensorSendTime(inObj.optString("sensorSendTime", ""));
                    inside.setTimdDiff(inObj.optString("timeDiffs", ""));

                    hInfo.setInside(inside);
                }
            }else{
                try {
                    if(!isMainInfo){
                        tempInside = CleanVentilationApplication.getInstance().getHomeList().get(0).getInside();
                        hInfo.setInside(tempInside);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            // 실외 정보 파싱
            if(obj.has("outSide")){
                JSONObject outObj = null;

                try {
                    outObj = obj.getJSONObject("outSide");
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                if(null != outObj){
                    OutSide outSide = new OutSide();

                    outSide.setCityDept1(outObj.optString("cityDept1", ""));
                    outSide.setCityDept2(outObj.optString("cityDept2", ""));
                    outSide.setIcon(outObj.optInt("icon", 0));
                    outSide.setTemp(outObj.optDouble("temp", 0));
                    outSide.setSensTemp(outObj.optDouble("sensTemp", 0));
                    outSide.setHumi(outObj.optDouble("humi", 0));
                    outSide.setRainFall(outObj.optDouble("rainFall", 0));
                    outSide.setSnowFall(outObj.optDouble("snowFall", 0));
                    outSide.setPressure(outObj.optDouble("pressure", 0));
                    outSide.setWindDir(outObj.optDouble("windDir", 0));
                    outSide.setVs(outObj.optString("vs", ""));
                    outSide.setUpdateTime(outObj.optString("updateTime", ""));
                    outSide.setDust10Sensor(outObj.optInt("dust10Sensor", 0));
                    outSide.setDust10Value(outObj.optDouble("dust10Value", 0));
                    outSide.setDust25Sensor(outObj.optInt("dust25Sensor", 0));
                    outSide.setDust25Value(outObj.optDouble("dust25Value", 0));

                    hInfo.setOutSide(outSide);
                }
            }else{
                try {
                    tempOutSide = CleanVentilationApplication.getInstance().getHomeList().get(0).getOutSide();
                    hInfo.setOutSide(tempOutSide);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            // 위젯 갱신시 룸콘 동작상태 표시하기 위한 데이터 파싱
            if(obj.has("roomControllerMode")){
                JSONObject widget = null;

                try {
                    widget = obj.getJSONObject("roomControllerMode");
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                if(null != widget){
                    WidgetRCMode widgetRCMode = new WidgetRCMode();

                    widgetRCMode.setPower(widget.optInt("power", 0));
                    widgetRCMode.setMode(widget.optInt("mode", 0));
                    widgetRCMode.setOptionMode(widget.optInt("optionMode", 0));
                    widgetRCMode.setOduMode(widget.optInt("oduMode", 0));
                    widgetRCMode.setUvLedMode(widget.optInt("uvLedMode", 0));
                    widgetRCMode.setUvLedState(widget.optInt("uvLedState", 0));

                    hInfo.setWidgetRCMode(widgetRCMode);
                }
            }

            // AWS 인증 정보 파싱
            if(obj.has("auth")){
                JSONObject objAuth = obj.getJSONObject("auth");

                CleanVentilationApplication.awsAuthParser(objAuth);
            }

            tempHomeList.add(hInfo);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        appication.getHomeList().clear();
        appication.getHomeList().addAll(tempHomeList);
    }
}
