package kr.co.kdone.airone.data;

import static kr.co.kdone.airone.utils.ProtocolType.USE;
import static kr.co.kdone.airone.utils.ProtocolType.USE_NOT;


/**
 * 제어 명령을 보내기 위한 패킷
 */

public class RemoteControlPacket {

    private final int PACKET_LEN = 30;

    public int req; //01:DID요청, 02:원격제어요청, 03:상태데이터요청
    public int run_use; //01:사용안함, 02:사용
    public int run; //01:OFF, 02:ON
    public int mode_use; //01:사용안함, 02:사용
    public int mode; //01:제습, 02:환기, 03:배기 04:주방, 05:청소, 06:공기청정 07:황사청정, 08:자동운전
    public int mode_option_use; //01:사용안함, 02:사용
    public int mode_option; //01:없음, 02:터보, 03:절전
    public int wind_use; //01:사용안함, 02:사용
    public int wind; //01:미, 02:약, 03:강, 04:자동 (LEVEL)
    public int wind_value; //일반환기, 배기모드를 제외한 모든 사항에는 0xFF로 전달
    public int humidity_use; //01:사용안함, 02:사용
    public int humidity; //HEX값 0~100
    public int schedule_type_use; //01:사용안함, 02:사용
    public int schedule_type; //01:없음, 02:취침, 03:켜짐, 04:꺼짐
    public int schedule_time_use; //01:사용안함, 02:사용
    public int[] schedule_time; //HEX값 MIN 단위 ex>30분:001E, 300분012C
    public int room_use; //01:사용안함, 02:사용
    public int[] room; //01:OFF, 02:ON(BITMASK)(1byte:zoen,2byte:dress)(1byte:0000 0001:1번방 SET/2byte:0000 0001:Dress Room SET)
    public int req_err_alarm; //01:사용안함, 02:RESET
    public int req_hepar_filter; //01:사용안함, 02:RESET
    public int req_free_filter; //01:사용안함, 02:RESET
    public int sleep_use; //01:사용안함, 02:사용
    public int sleep; //01:숙면 사용안함, 02:숙면 사용
    public int sleep_time_start; //HEX값 0~23(오후 2시 : 0x0E)
    public int sleep_time_end; //HEX값 0~23(오후 3시 : 0x0F)
    //2019.04.16 추가
    public int req_hepar_push_filter; //01 : 사용안함, 02 : RESET
    public int req_free_push_filter; //01 : 사용안함, 02 : RESET
    public int req_free_hepar_push_filter; //01 : 사용안함, 02 : RESET

    public RemoteControlPacket() {
        req = 0x00;
        run_use = USE_NOT;
        run = 0x00;
        mode_use  = USE_NOT;
        mode = 0x00;
        mode_option_use = USE_NOT;
        mode_option = 0x00;
        wind_use  = USE_NOT;
        wind = 0x00;
        wind_value = 0xFF;
        humidity_use = USE_NOT;
        humidity = 0x00;
        schedule_type_use = USE_NOT;
        schedule_type = 0x00;
        schedule_time_use = USE_NOT;
        schedule_time = new int[]{0x00,0x00};
        room_use = USE;
        room = new int[]{0x00,0x00};
        req_err_alarm = 0x00;
        req_hepar_filter = 0x00;
        req_free_filter = 0x00;
        sleep_use = USE_NOT;
        sleep_time_start = 0x00;
        sleep_time_end = 0x00;
        req_hepar_push_filter = 0x00;
        req_free_push_filter = 0x00;
        req_free_hepar_push_filter = 0x00;
    }

    public void setReq(int req) {
        this.req = req;
    }

    public int getReq() {
        return req;
    }

    public void setRunUse(int run_use) {
        this.run_use = run_use;
    }

    public int getRunUse() {
        return run_use;
    }

    public void setRun(int run) {
        this.run = run;
    }

    public int getRun() {
        return run;
    }

    public void setModeUse(int mode_use) {
        this.mode_use = mode_use;
    }

    public int getModeUse() {
        return mode_use;
    }

    public void setMode(int mode) {
        this.mode = mode;
    }

    public int getMode() {
        return mode;
    }

    public void setModeOptionUse(int mode_option_use) {
        this.mode_option_use = mode_option_use;
    }

    public int getModeOptionUse() {
        return mode_option_use;
    }

    public void setModeOption(int mode_option) {
        this.mode_option = mode_option;
    }

    public int getModeOption() {
        return mode_option;
    }

    public void setWindUse(int wind_use) {
        this.wind_use = wind_use;
    }

    public int getWindUse() {
        return wind_use;
    }

    public void setWind(int wind) {
        this.wind = wind;
    }

    public int getWind() {
        return wind;
    }

    public void setWindValue(int wind_value) {
        this.wind_value = wind_value;
    }

    public int getWindValue() {
        return wind_value;
    }

    public void setHumidityUse(int humidity_use) {
        this.humidity_use = humidity_use;
    }

    public int getHumidityUse() {
        return humidity_use;
    }

    public void setHumidity(int humidity) {
        this.humidity = humidity*2;
    }

    public int getHumidity() {
        return humidity/2;
    }

    public void setScheduleTypeUse(int schedule_type_use) {
        this.schedule_type_use = schedule_type_use;
    }

    public int getScheduleTypeUse() {
        return schedule_type_use;
    }

    public void setScheduleType(int schedule_type) {
        this.schedule_type = schedule_type;
    }

    public int getScheduleType() {
        return schedule_type;
    }

    public void setScheduleTimeUse(int schedule_time_use) {
        this.schedule_time_use = schedule_time_use;
    }

    public int getScheduleTimeUse() {
        return schedule_time_use;
    }

    public void setScheduleTime(int[] set_schedule_time) {
        this.schedule_time = set_schedule_time;
    }

    public int[] getScheduleTime() {
        return schedule_time;
    }

    public void setRoomUse(int room_use) {
        this.room_use = room_use;
    }

    public int getRoomUse() {
        return room_use;
    }

    public void setRoom(int[] room) {
        this.room = room;
    }

    public int[] getRoom() {
        return room;
    }

    public void setReqErrAlarm(int req_err_alarm) {
        this.req_err_alarm = req_err_alarm;
    }

    public int getReqErrAlarm() {
        return req_err_alarm;
    }

    public void setReqHeparFilter(int req_hepar_filter) {
        this.req_hepar_filter = req_hepar_filter;
    }

    public int getReqHeparFilter() {
        return req_hepar_filter;
    }

    public void setReqFreeFilter(int req_free_filter) {
        this.req_free_filter = req_free_filter;
    }

    public int getReqFreeFilter() {
        return req_free_filter;
    }

    public int getSleepUse() {
        return sleep_use;
    }

    public void setSleepUse(int sleep_use) {
        this.sleep_use = sleep_use;
    }

    public int getSleep() {
        return sleep;
    }

    public void setSleep(int sleep) {
        this.sleep = sleep;
    }

    public int getSleepTimeStart() {
        return sleep_time_start;
    }

    public void setSleepTimeStart(int sleep_time_start) {
        this.sleep_time_start = sleep_time_start;
    }

    public int getSleepTimeEnd() {
        return sleep_time_end;
    }

    public void setSleepTimeEnd(int sleep_time_end) {
        this.sleep_time_end = sleep_time_end;
    }

    public int getReq_hepar_push_filter() {
        return req_hepar_push_filter;
    }

    public void setReqHeparPushFilter(int req_hepar_push_filter) {
        this.req_hepar_push_filter = req_hepar_push_filter;
    }

    public int getReqFreePushFilter() {
        return req_free_push_filter;
    }

    public void setReqFreePushFilter(int req_free_push_filter) {
        this.req_free_push_filter = req_free_push_filter;
    }

    public int getReq_free_hepar_push_filter() {
        return req_free_hepar_push_filter;
    }

    public void setReq_free_hepar_push_filter(int req_free_hepar_push_filter) {
        this.req_free_hepar_push_filter = req_free_hepar_push_filter;
    }

    public byte[] toBytes(){

        byte[] returnData = new byte[PACKET_LEN];
        int index = 0;

        returnData[index++] = (byte)req;
        returnData[index++] = (byte)run_use;
        returnData[index++] = (byte)run;
        returnData[index++] = (byte)mode_use;
        returnData[index++] = (byte)mode;
        returnData[index++] = (byte)mode_option_use;
        returnData[index++] = (byte)mode_option;
        returnData[index++] = (byte)wind_use;
        returnData[index++] = (byte)wind;
        returnData[index++] = (byte)wind_value;
        returnData[index++] = (byte)humidity_use;
        returnData[index++] = (byte)humidity;
        returnData[index++] = (byte)schedule_type_use;
        returnData[index++] = (byte)schedule_type;
        returnData[index++] = (byte)schedule_time_use;
        returnData[index++] = (byte)schedule_time[0];
        returnData[index++] = (byte)schedule_time[1];
        returnData[index++] = (byte)room_use;
        returnData[index++] = (byte)room[0];
        returnData[index++] = (byte)room[1];
        returnData[index++] = (byte)req_err_alarm;
        returnData[index++] = (byte)req_hepar_filter;
        returnData[index++] = (byte)req_free_filter;
        returnData[index++] = (byte)sleep_use;
        returnData[index++] = (byte)sleep;
        returnData[index++] = (byte)sleep_time_start;
        returnData[index++] = (byte)sleep_time_end;
        returnData[index++] = (byte)req_hepar_push_filter;
        returnData[index++] = (byte)req_free_push_filter;
        returnData[index++] = (byte)req_free_hepar_push_filter;

        return returnData;
    }

    public int[] toIntArrays(){

        int[] returnData = new int[PACKET_LEN];
        int index = 0;

        returnData[index++] = req;
        returnData[index++] = run_use;
        returnData[index++] = run;
        returnData[index++] = mode_use;
        returnData[index++] = mode;
        returnData[index++] = mode_option_use;
        returnData[index++] = mode_option;
        returnData[index++] = wind_use;
        returnData[index++] = wind;
        returnData[index++] = wind_value;
        returnData[index++] = humidity_use;
        returnData[index++] = humidity;
        returnData[index++] = schedule_type_use;
        returnData[index++] = schedule_type;
        returnData[index++] = schedule_time_use;
        returnData[index++] = schedule_time[0];
        returnData[index++] = schedule_time[1];
        returnData[index++] = room_use;
        returnData[index++] = room[0];
        returnData[index++] = room[1];
        returnData[index++] = req_err_alarm;
        returnData[index++] = req_hepar_filter;
        returnData[index++] = req_free_filter;
        returnData[index++] = sleep_use;
        returnData[index++] = sleep;
        returnData[index++] = sleep_time_start;
        returnData[index++] = sleep_time_end;
        returnData[index++] = req_hepar_push_filter;
        returnData[index++] = req_free_push_filter;
        returnData[index++] = req_free_hepar_push_filter;

        return returnData;
    }

}
