package kr.co.kdone.airone.data;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.google.firebase.crashlytics.FirebaseCrashlytics;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import kr.co.kdone.airone.utils.CommonUtils;

import static kr.co.kdone.airone.utils.ProtocolType.COMMAND_CONTROL_DATA;
import static kr.co.kdone.airone.utils.ProtocolType.COMMAND_DID;
import static kr.co.kdone.airone.utils.ProtocolType.COMMAND_EID;
import static kr.co.kdone.airone.utils.ProtocolType.COMMAND_FILTER_DATA;
import static kr.co.kdone.airone.utils.ProtocolType.COMMAND_FILTER_STATUS_INFO;
import static kr.co.kdone.airone.utils.ProtocolType.COMMAND_LED_DATA;
import static kr.co.kdone.airone.utils.ProtocolType.COMMAND_LED_STATUS_INFO;
import static kr.co.kdone.airone.utils.ProtocolType.COMMAND_PSD;
import static kr.co.kdone.airone.utils.ProtocolType.COMMAND_SD;
import static kr.co.kdone.airone.utils.ProtocolType.MODE_NOT_USED;
import static kr.co.kdone.airone.utils.ProtocolType.MODE_USED;
import static kr.co.kdone.airone.utils.ProtocolType.PROTOCOL_VER_ID1;
import static kr.co.kdone.airone.utils.ProtocolType.REQ_DID;
import static kr.co.kdone.airone.utils.ProtocolType.REQ_REMOTE_CONTROL;
import static kr.co.kdone.airone.utils.ProtocolType.REQ_STATUS_DATA;
import static kr.co.kdone.airone.utils.ProtocolType.STX;

/**
 * 제품 제어통신을 위한 매니저
 */
public class DeviceManager {
    private static final String TAG = DeviceManager.class.getSimpleName();
    private final boolean DBG = true;

    private static DeviceManager sDeviceManager = null;
    private Context mContext;
    private DataChangedListener mDataChangedListener;
    private static Device sCurrentDevice = null;
    private static ArrayList<DeviceInfo> mDeviceList;
    private static ArrayList<DeviceInfo> mDeviceAirMonitorList;

    public interface DataChangedListener {
        void onDataChanged(int cmd);
    }

    public static DeviceManager getInstance() {
        if (sDeviceManager == null) {
            sDeviceManager = new DeviceManager();
        }
        if(sCurrentDevice == null){
            sCurrentDevice = new Device();
        }
        if(mDeviceList == null){
            mDeviceList = new ArrayList<DeviceInfo>();
        }
        if(mDeviceAirMonitorList == null){
            mDeviceAirMonitorList = new ArrayList<>();
        }
        return sDeviceManager;
    }

    public void init(Context context) {
        mContext = context;
    }

    public void setDataChangedListener(DataChangedListener listener) {
        mDataChangedListener = listener;
    }

    public Device getCurrentDevice() {
        return sCurrentDevice;
    }

   /* public void clearDeviceList(){
        if(mDeviceList != null){
            mDeviceList.clear();
        }
        if(mDeviceAirMonitorList!=null){
            mDeviceAirMonitorList.clear();
        }
    }

    public void addDeviceList(DeviceInfo d){
        if(mDeviceList == null){
            mDeviceList = new ArrayList<DeviceInfo>();
        }
        if(mDeviceList != null){
            for(int i = 0; i < mDeviceList.size();i++){
                if(d.GID.equals(mDeviceList.get(i).GID)){
                    mDeviceList.remove(i);
                }
            }
            mDeviceList.add(d);
        }
    }

    public void addDeviceList(JSONObject jObject) throws JSONException {
        if(mDeviceList == null){
            mDeviceList = new ArrayList<DeviceInfo>();
        }
        if(mDeviceList != null){
            DeviceInfo deviceInfo = new DeviceInfo();

            deviceInfo.GID = jObject.getString("gid");
            deviceInfo.PCD = jObject.getString("pcd");
            deviceInfo.State = jObject.getInt("state");
            deviceInfo.DeviceVer = jObject.getInt("deviceVer");
            deviceInfo.SensingBoxConnect = jObject.getString("sensingBoxConnect");
            deviceInfo.NickName = jObject.getString("nickName");
            deviceInfo.SensingBox1 = jObject.getString("sensingBox1");
            deviceInfo.Power = jObject.isNull("power") ? 0 : jObject.getInt("power");
            deviceInfo.WifiLevel = deviceInfo.State == 0 ? 0 : 4;
            deviceInfo.lat = jObject.getString("lat");
            deviceInfo.lng = jObject.getString("lng");
            deviceInfo.ssId = jObject.getString("ssid");
            deviceInfo.PushUse = jObject.getInt("pushUse");
            deviceInfo.SmartAlarm = jObject.getString("smartAlarm");
            deviceInfo.FilterAlarm = jObject.getString("filterAlarm");

            for(int i = 0; i < mDeviceList.size();i++){
                if(deviceInfo.GID.equals(mDeviceList.get(i).GID)){
                    mDeviceList.remove(i);
                }
            }
            mDeviceList.add(deviceInfo);
        }
    }

    public void addDeviceAirMonitorList(JSONObject jObject) throws JSONException {
        if(mDeviceAirMonitorList == null){
            mDeviceAirMonitorList = new ArrayList<DeviceInfo>();
        }
        if(mDeviceAirMonitorList != null){
            DeviceInfo deviceInfo = new DeviceInfo();

            deviceInfo.GID = jObject.getString("gid");
            deviceInfo.PCD = jObject.getString("pcd");
            deviceInfo.State = jObject.getInt("state");
            deviceInfo.DeviceVer = jObject.getInt("deviceVer");
            deviceInfo.SensingBoxConnect = jObject.getString("sensingBoxConnect");
            deviceInfo.NickName = jObject.getString("nickName");
            deviceInfo.SensingBox1 = jObject.getString("sensingBox1");
            deviceInfo.Power = jObject.isNull("power") ? 0 : jObject.getInt("power");
            deviceInfo.WifiLevel = deviceInfo.State == 0 ? 0 : 4;
            deviceInfo.ssId = jObject.getString("ssid");
            deviceInfo.PushUse = jObject.getInt("pushUse");
            deviceInfo.SmartAlarm = jObject.getString("smartAlarm");
            deviceInfo.FilterAlarm = jObject.getString("filterAlarm");

            for(int i = 0; i < mDeviceAirMonitorList.size();i++){
                if(deviceInfo.GID.equals(mDeviceAirMonitorList.get(i).GID)){
                    mDeviceAirMonitorList.remove(i);
                }
            }
            mDeviceAirMonitorList.add(deviceInfo);
        }
    }

    public ArrayList<DeviceInfo> getDeviceAirMonitorList(){
        if(mDeviceAirMonitorList!=null){
            return mDeviceAirMonitorList;
        }
        return null;
    }

    public DeviceInfo getDeviceAirMonitor(){
        if(mDeviceAirMonitorList != null){
            for(int i = 0; i < mDeviceAirMonitorList.size();i++){
                return mDeviceAirMonitorList.get(i);
            }
        }
        return null;
    }*/

    public DeviceInfo getDeviceForDeviceList(String DeviceID){
        if(mDeviceList != null){
            for(int i = 0; i < mDeviceList.size();i++){
                if(DeviceID.equalsIgnoreCase(mDeviceList.get(i).GID)){
                    return mDeviceList.get(i);
                }
            }
        }
        return null;
    }

    /*public ArrayList<DeviceInfo> getDeviceList() {
        return mDeviceList;
    }


    public void updateUseNotForDevice(){
        DeviceManager.getInstance().getCurrentDevice().control.setRunUse(USE);
        DeviceManager.getInstance().getCurrentDevice().control.setModeUse(USE);
        DeviceManager.getInstance().getCurrentDevice().control.setModeOptionUse(USE);
        DeviceManager.getInstance().getCurrentDevice().control.setWindUse(USE);
        DeviceManager.getInstance().getCurrentDevice().control.setHumidityUse(USE);
        DeviceManager.getInstance().getCurrentDevice().control.setRoomUse(USE);
        DeviceManager.getInstance().getCurrentDevice().control.setScheduleTimeUse(USE);
        DeviceManager.getInstance().getCurrentDevice().control.setScheduleTypeUse(USE);
    }*/

    public byte[] getSendDataToDevice(int src, int dst, int[]productID){
        int stx = STX;
        int ver = sCurrentDevice.head.ver == 0 ? PROTOCOL_VER_ID1 : sCurrentDevice.head.ver;
        int cmd = COMMAND_CONTROL_DATA;

        int crc;

        byte[] data = sCurrentDevice.control.toBytes();
        int dLen;
        if(data == null){
            dLen = 0;
        } else {
            dLen = data.length;
        }
        byte[] returnData = new byte[9+8+dLen];
        int index = 0;
        returnData[index++] = (byte)stx;
        returnData[index++] = (byte)src;
        returnData[index++] = (byte)dst;
        returnData[index++] = (byte)ver;

        if(productID !=null && productID.length == 8){
            for(int p:productID){
                returnData[index++] = (byte)p;
            }
        } else if (productID !=null && productID.length < 8){
            for(int p:productID){
                returnData[index++] = (byte)p;
            }
            for(int i = productID.length; i < 8 ; i++){
                returnData[index++] = (byte)0x00;
            }
        } else if (productID !=null && productID.length > 8){
            for(int i = 0; i < 8 ; i++){
                returnData[index++] = (byte)productID[i];
            }
        }
        else {
            for(int i = 0; i<8;i++){
                returnData[index++] = (byte)0;
            }
        }

        returnData[index++] = (byte)cmd;
        returnData[index++] = (byte)(data.length>>8);
        returnData[index++] = (byte)data.length;

        if(data !=null){
            for(int d:data){
                returnData[index++] = (byte)d;
            }
        }

        crc = CommonUtils.updateCRC(returnData,returnData.length-2);

        returnData[index++] = (byte)(crc>>8);
        returnData[index++] = (byte)crc;

        return returnData;
    }

    public byte[] getSendDidData(int src, int dst, int[]productID, int ver){
        sCurrentDevice.control.setReq(REQ_DID);

        sCurrentDevice.head.ver = CommonUtils.getHeaderPcd(ver);

        return getSendDataToDevice(src, dst, productID);
    }

    public byte[] getSendSdData(int src, int dst, int[]productID, int ver){
        sCurrentDevice.control.setReq(REQ_STATUS_DATA);

        sCurrentDevice.head.ver = CommonUtils.getHeaderPcd(ver);

        return getSendDataToDevice(src, dst, productID);
    }

    public byte[] getSendRemoteControlData(int src, int dst, int[]productID){
        sCurrentDevice.control.setReq(REQ_REMOTE_CONTROL);
        return getSendDataToDevice(src, dst, productID);
    }

    /**
     * ikHwang 2019-08-26 오후 4:52 LED 상태조회 및 제어
     * @param src
     * @param dst
     * @param productID
     * @return
     */
    public byte[] getSendDataToDeviceLEDSetting(int src, int dst, int[]productID){
        int stx = STX;
        int ver = sCurrentDevice.head.ver == 0 ? PROTOCOL_VER_ID1 : sCurrentDevice.head.ver;
        int cmd = COMMAND_LED_DATA;

        int crc;

        byte[] data = sCurrentDevice.led.toBytes();
        int dLen;
        if(data == null){
            dLen = 0;
        } else {
            dLen = data.length;
        }
        byte[] returnData = new byte[9+8+dLen];
        int index = 0;
        returnData[index++] = (byte)stx;
        returnData[index++] = (byte)src;
        returnData[index++] = (byte)dst;
        returnData[index++] = (byte)ver;

        if(productID !=null && productID.length == 8){
            for(int p:productID){
                returnData[index++] = (byte)p;
            }
        } else if (productID !=null && productID.length < 8){
            for(int p:productID){
                returnData[index++] = (byte)p;
            }
            for(int i = productID.length; i < 8 ; i++){
                returnData[index++] = (byte)0x00;
            }
        } else if (productID !=null && productID.length > 8){
            for(int i = 0; i < 8 ; i++){
                returnData[index++] = (byte)productID[i];
            }
        }
        else {
            for(int i = 0; i<8;i++){
                returnData[index++] = (byte)0;
            }
        }

        returnData[index++] = (byte)cmd;
        returnData[index++] = (byte)(data.length>>8);
        returnData[index++] = (byte)data.length;

        if(data !=null){
            for(int d:data){
                returnData[index++] = (byte)d;
            }
        }

        crc = CommonUtils.updateCRC(returnData,returnData.length-2);

        returnData[index++] = (byte)(crc>>8);
        returnData[index++] = (byte)crc;

        return returnData;
    }

    /**
     * ikHwang 2019-08-26 오후 4:53 필터 상태 조회 및 제어
     * @param src
     * @param dst
     * @param productID
     * @return
     */
    public byte[] getSendDataToDeviceFilterSetting(int src, int dst, int[]productID){
        int stx = STX;
        int ver = sCurrentDevice.head.ver == 0 ? PROTOCOL_VER_ID1 : sCurrentDevice.head.ver;
        int cmd = COMMAND_FILTER_DATA;

        int crc;

        byte[] data = sCurrentDevice.filter.toBytes();
        int dLen;
        if(data == null){
            dLen = 0;
        } else {
            dLen = data.length;
        }
        byte[] returnData = new byte[9+8+dLen];
        int index = 0;
        returnData[index++] = (byte)stx;
        returnData[index++] = (byte)src;
        returnData[index++] = (byte)dst;
        returnData[index++] = (byte)ver;

        if(productID !=null && productID.length == 8){
            for(int p:productID){
                returnData[index++] = (byte)p;
            }
        } else if (productID !=null && productID.length < 8){
            for(int p:productID){
                returnData[index++] = (byte)p;
            }
            for(int i = productID.length; i < 8 ; i++){
                returnData[index++] = (byte)0x00;
            }
        } else if (productID !=null && productID.length > 8){
            for(int i = 0; i < 8 ; i++){
                returnData[index++] = (byte)productID[i];
            }
        }
        else {
            for(int i = 0; i<8;i++){
                returnData[index++] = (byte)0;
            }
        }

        returnData[index++] = (byte)cmd;
        returnData[index++] = (byte)(data.length>>8);
        returnData[index++] = (byte)data.length;

        if(data !=null){
            for(int d:data){
                returnData[index++] = (byte)d;
            }
        }

        crc = CommonUtils.updateCRC(returnData,returnData.length-2);

        returnData[index++] = (byte)(crc>>8);
        returnData[index++] = (byte)crc;

        return returnData;
    }

    public void setReceivedData(byte[] bytes){
        int idx = 0;

        if(sCurrentDevice == null) sCurrentDevice = new Device();

        //Head Packet
        if(sCurrentDevice.head == null) sCurrentDevice.head = new HeadPacket();

        sCurrentDevice.head.stx = (int)bytes[idx++] & 0xFF;
        sCurrentDevice.head.src = (int)bytes[idx++] & 0xFF;
        sCurrentDevice.head.dst = (int)bytes[idx++] & 0xFF;
        sCurrentDevice.head.ver = (int)bytes[idx++] & 0xFF;

        System.arraycopy(bytes,idx,sCurrentDevice.head.productID,0,sCurrentDevice.head.productID.length);
        idx+=8;
        sCurrentDevice.head.cmd = (int)bytes[idx++] & 0xFF;
        sCurrentDevice.head.len_data = (int)((bytes[idx++] & 0xFF) << 8) + (int)(bytes[idx++] & 0xFF);
        sCurrentDevice.head.lastUpdateTime = System.currentTimeMillis();

        // ikHwang 2019-10-16 오후 3:37 추가된 필터 관리 및 에어모니터 LED 밝기 설정에서는 아래의 패킷정보 없음
        switch (sCurrentDevice.head.cmd){
            case COMMAND_DID:
            case COMMAND_EID:
            case COMMAND_PSD:
            case COMMAND_SD:
                //Common Packet
                if(sCurrentDevice.common == null)
                    sCurrentDevice.common = new CommonPacket();
                sCurrentDevice.common.countryCode = (int)bytes[idx++] & 0xFF;
                sCurrentDevice.common.modelCode = (int)bytes[idx++] & 0xFF;
                sCurrentDevice.common.deviceVer  = (int)bytes[idx++] & 0xFF;
                sCurrentDevice.common.senboxConnected  = (int)bytes[idx++] & 0xFF;
                sCurrentDevice.common.lastUpdateTime = System.currentTimeMillis();
                break;
        }

        //Command Type에 따른 각각의 Packet
        switch (sCurrentDevice.head.cmd){
            //디바이스 인포 데이터
            case COMMAND_DID:
                CommonUtils.customLog(TAG, "Receive COMMAND_DID", Log.ERROR);
                if(sCurrentDevice.did == null)
                    sCurrentDevice.did = new DidPacket();
                sCurrentDevice.did.vspRpmControl  = (int)bytes[idx++] & 0xFF;
                sCurrentDevice.did.oduModel  = (int)bytes[idx++] & 0xFF;

                // 냉방모드
                sCurrentDevice.did.coldMode.modeUse = (int)bytes[idx++] & 0xFF;
                sCurrentDevice.did.coldMode.turboUse = (int)bytes[idx++] & 0xFF;
                sCurrentDevice.did.coldMode.pwrSavingUse = (int)bytes[idx++] & 0xFF;
                sCurrentDevice.did.coldMode.sleepUse = (int)bytes[idx++] & 0xFF;
                sCurrentDevice.did.coldMode.reservationOffUse = (int)bytes[idx++] & 0xFF;
                sCurrentDevice.did.coldMode.reservationOnUse = (int)bytes[idx++] & 0xFF;

                // 난방모드
                sCurrentDevice.did.hotMode.modeUse = (int)bytes[idx++] & 0xFF;
                sCurrentDevice.did.hotMode.turboUse = (int)bytes[idx++] & 0xFF;
                sCurrentDevice.did.hotMode.pwrSavingUse = (int)bytes[idx++] & 0xFF;
                sCurrentDevice.did.hotMode.sleepUse = (int)bytes[idx++] & 0xFF;
                sCurrentDevice.did.hotMode.reservationOffUse = (int)bytes[idx++] & 0xFF;
                sCurrentDevice.did.hotMode.reservationOnUse = (int)bytes[idx++] & 0xFF;

                // 제습모드
                sCurrentDevice.did.dehumiMode.modeUse = (int)bytes[idx++] & 0xFF;
                sCurrentDevice.did.dehumiMode.turboUse = (int)bytes[idx++] & 0xFF;
                sCurrentDevice.did.dehumiMode.pwrSavingUse = (int)bytes[idx++] & 0xFF;
                sCurrentDevice.did.dehumiMode.sleepUse = (int)bytes[idx++] & 0xFF;
                sCurrentDevice.did.dehumiMode.reservationOffUse = (int)bytes[idx++] & 0xFF;
                sCurrentDevice.did.dehumiMode.reservationOnUse = (int)bytes[idx++] & 0xFF;

                // ikHwang 2020-02-28 오전 10:25 제습모드 UI를 확인하기 위한 데이터 강제 설정
                /*sCurrentDevice.did.dehumiMode.modeUse = 2;
                sCurrentDevice.did.dehumiMode.turboUse = 2;
                sCurrentDevice.did.dehumiMode.pwrSavingUse = 2;
                sCurrentDevice.did.dehumiMode.sleepUse = 1;
                sCurrentDevice.did.dehumiMode.reservationOffUse = 0;
                sCurrentDevice.did.dehumiMode.reservationOnUse = 0;*/

                // 가습모드
                sCurrentDevice.did.humiMode.modeUse = (int)bytes[idx++] & 0xFF;
                sCurrentDevice.did.humiMode.turboUse = (int)bytes[idx++] & 0xFF;
                sCurrentDevice.did.humiMode.pwrSavingUse = (int)bytes[idx++] & 0xFF;
                sCurrentDevice.did.humiMode.sleepUse = (int)bytes[idx++] & 0xFF;
                sCurrentDevice.did.humiMode.reservationOffUse = (int)bytes[idx++] & 0xFF;
                sCurrentDevice.did.humiMode.reservationOnUse = (int)bytes[idx++] & 0xFF;

                // 환기모드
                sCurrentDevice.did.ventiMode.modeUse = (int)bytes[idx++] & 0xFF;
                sCurrentDevice.did.ventiMode.turboUse = (int)bytes[idx++] & 0xFF;
                sCurrentDevice.did.ventiMode.pwrSavingUse = (int)bytes[idx++] & 0xFF;
                sCurrentDevice.did.ventiMode.sleepUse = (int)bytes[idx++] & 0xFF;
                sCurrentDevice.did.ventiMode.reservationOffUse = (int)bytes[idx++] & 0xFF;
                sCurrentDevice.did.ventiMode.reservationOnUse = (int)bytes[idx++] & 0xFF;

                // 배기모드
                sCurrentDevice.did.exhaustMode.modeUse = (int)bytes[idx++] & 0xFF;
                sCurrentDevice.did.exhaustMode.turboUse = (int)bytes[idx++] & 0xFF;
                sCurrentDevice.did.exhaustMode.pwrSavingUse = (int)bytes[idx++] & 0xFF;
                sCurrentDevice.did.exhaustMode.sleepUse = (int)bytes[idx++] & 0xFF;
                sCurrentDevice.did.exhaustMode.reservationOffUse = (int)bytes[idx++] & 0xFF;
                sCurrentDevice.did.exhaustMode.reservationOnUse = (int)bytes[idx++] & 0xFF;

                // 공기청정모드
                sCurrentDevice.did.airCleanMode.modeUse = (int)bytes[idx++] & 0xFF;
                sCurrentDevice.did.airCleanMode.turboUse = (int)bytes[idx++] & 0xFF;
                sCurrentDevice.did.airCleanMode.pwrSavingUse = (int)bytes[idx++] & 0xFF;
                sCurrentDevice.did.airCleanMode.sleepUse = (int)bytes[idx++] & 0xFF;
                sCurrentDevice.did.airCleanMode.reservationOffUse = (int)bytes[idx++] & 0xFF;
                sCurrentDevice.did.airCleanMode.reservationOnUse = (int)bytes[idx++] & 0xFF;

                // 요리모드 (주방)
                sCurrentDevice.did.kitchenMode.modeUse = (int)bytes[idx++] & 0xFF;
                sCurrentDevice.did.kitchenMode.turboUse = (int)bytes[idx++] & 0xFF;
                sCurrentDevice.did.kitchenMode.pwrSavingUse = (int)bytes[idx++] & 0xFF;
                sCurrentDevice.did.kitchenMode.sleepUse = (int)bytes[idx++] & 0xFF;
                sCurrentDevice.did.kitchenMode.reservationOffUse = (int)bytes[idx++] & 0xFF;
                sCurrentDevice.did.kitchenMode.reservationOnUse = (int)bytes[idx++] & 0xFF;

                // 청소모드
                sCurrentDevice.did.cleanMode.modeUse = (int)bytes[idx++] & 0xFF;
                sCurrentDevice.did.cleanMode.turboUse = (int)bytes[idx++] & 0xFF;
                sCurrentDevice.did.cleanMode.pwrSavingUse = (int)bytes[idx++] & 0xFF;
                sCurrentDevice.did.cleanMode.sleepUse = (int)bytes[idx++] & 0xFF;
                sCurrentDevice.did.cleanMode.reservationOffUse = (int)bytes[idx++] & 0xFF;
                sCurrentDevice.did.cleanMode.reservationOnUse = (int)bytes[idx++] & 0xFF;

                // 황사모드
                sCurrentDevice.did.dustMode.modeUse = (int)bytes[idx++] & 0xFF;
                sCurrentDevice.did.dustMode.turboUse = (int)bytes[idx++] & 0xFF;
                sCurrentDevice.did.dustMode.pwrSavingUse = (int)bytes[idx++] & 0xFF;
                sCurrentDevice.did.dustMode.sleepUse = (int)bytes[idx++] & 0xFF;
                sCurrentDevice.did.dustMode.reservationOffUse = (int)bytes[idx++] & 0xFF;
                sCurrentDevice.did.dustMode.reservationOnUse = (int)bytes[idx++] & 0xFF;

                // 건강호흡 모드
                sCurrentDevice.did.healthMode.modeUse = (int)bytes[idx++] & 0xFF;
                sCurrentDevice.did.healthMode.turboUse = (int)bytes[idx++] & 0xFF;
                sCurrentDevice.did.healthMode.pwrSavingUse = (int)bytes[idx++] & 0xFF;
                sCurrentDevice.did.healthMode.sleepUse = (int)bytes[idx++] & 0xFF;
                sCurrentDevice.did.healthMode.reservationOffUse = (int)bytes[idx++] & 0xFF;
                sCurrentDevice.did.healthMode.reservationOnUse = (int)bytes[idx++] & 0xFF;

                // 피부보습 모드
                sCurrentDevice.did.skinMode.modeUse = (int)bytes[idx++] & 0xFF;
                sCurrentDevice.did.skinMode.turboUse = (int)bytes[idx++] & 0xFF;
                sCurrentDevice.did.skinMode.pwrSavingUse = (int)bytes[idx++] & 0xFF;
                sCurrentDevice.did.skinMode.sleepUse = (int)bytes[idx++] & 0xFF;
                sCurrentDevice.did.skinMode.reservationOffUse = (int)bytes[idx++] & 0xFF;
                sCurrentDevice.did.skinMode.reservationOnUse = (int)bytes[idx++] & 0xFF;

                // 자동모드
                sCurrentDevice.did.autoMode.modeUse = (int)bytes[idx++] & 0xFF;
                sCurrentDevice.did.autoMode.turboUse = (int)bytes[idx++] & 0xFF;
                sCurrentDevice.did.autoMode.pwrSavingUse = (int)bytes[idx++] & 0xFF;
                sCurrentDevice.did.autoMode.sleepUse = (int)bytes[idx++] & 0xFF;
                sCurrentDevice.did.autoMode.reservationOffUse = (int)bytes[idx++] & 0xFF;
                sCurrentDevice.did.autoMode.reservationOnUse = (int)bytes[idx++] & 0xFF;

                // 냉방 희망온도 설정값 (문서 상으로는 ODU 용량, ODU 히터 용량)
                sCurrentDevice.did.coldSetTemp.min = (int)bytes[idx++] & 0xFF;
                sCurrentDevice.did.coldSetTemp.max = (int)bytes[idx++] & 0xFF;

                // 제습 희망습도 설정값
                sCurrentDevice.did.dehumiSetHumi.min = (int)bytes[idx++] & 0xFF;
                sCurrentDevice.did.dehumiSetHumi.max = (int)bytes[idx++] & 0xFF;

                // ikHwang 2020-03-30 오전 9:41 제습 희망습도 min/max 값 강제 설정
                /*sCurrentDevice.did.dehumiSetHumi.min = 40;
                sCurrentDevice.did.dehumiSetHumi.max = 90;*/

                // 가습 희망습도 설정값
                sCurrentDevice.did.humiSetHumi.min = (int)bytes[idx++] & 0xFF;
                sCurrentDevice.did.humiSetHumi.max = (int)bytes[idx++] & 0xFF;

                // 자동 희망온도 설정값
                sCurrentDevice.did.autoSetTemp.min = (int)bytes[idx++] & 0xFF;
                sCurrentDevice.did.autoSetTemp.max = (int)bytes[idx++] & 0xFF;

                // 자동 희망습도 설정값
                sCurrentDevice.did.autoSetHumi.min = (int)bytes[idx++] & 0xFF;
                sCurrentDevice.did.autoSetHumi.max = (int)bytes[idx++] & 0xFF;

                // 온풍 희망온도 설정값 (필터 사용 시간으로 변경됨)
//                sCurrentDevice.did.hotSetTemp.min = (int)bytes[idx++];
//                sCurrentDevice.did.hotSetTemp.max = (int)bytes[idx++];
                sCurrentDevice.did.maxFilterUseTime = (int)((bytes[idx++] & 0xFF) << 8) + (int)(bytes[idx++] & 0xFF);

                CommonUtils.customLog(TAG, "FilterResetTime : " + sCurrentDevice.did.maxFilterUseTime, Log.ERROR);

                // ikHwang 2020-04-20 오전 10:17 ODU APS 적용 유무
                sCurrentDevice.did.apsHasFlag = (int)bytes[idx++] & 0xFF;
                sCurrentDevice.did.pwrSavingColdSetTemp.max = (int)bytes[idx++] & 0xFF;

                // 급기모드
                sCurrentDevice.did.aerationMode.modeUse = (int)bytes[idx++] & 0xFF;
                sCurrentDevice.did.aerationMode.turboUse = (int)bytes[idx++] & 0xFF;
                sCurrentDevice.did.aerationMode.pwrSavingUse = (int)bytes[idx++] & 0xFF;
                sCurrentDevice.did.aerationMode.sleepUse = (int)bytes[idx++] & 0xFF;
                sCurrentDevice.did.aerationMode.reservationOffUse = (int)bytes[idx++] & 0xFF;
                sCurrentDevice.did.aerationMode.reservationOnUse = (int)bytes[idx++] & 0xFF;

                sCurrentDevice.did.lastUpdateTime = System.currentTimeMillis();
                break;

            //디바이스 에러 데이터
            case COMMAND_EID:
                CommonUtils.customLog(TAG, "Receive COMMAND_EID", Log.ERROR);
                if(sCurrentDevice.head.len_data != 18) {
                    break;
                }
                if(sCurrentDevice.err == null) {
                    sCurrentDevice.err = new EidPacket();
                }
                sCurrentDevice.err.swVersion = (int)bytes[idx++] & 0xFF;
                sCurrentDevice.err.reserve1 = (int)bytes[idx++] & 0xFF;
                sCurrentDevice.err.reserve2 = (int)bytes[idx++] & 0xFF;
                sCurrentDevice.err.errorLevel = (int)bytes[idx++] & 0xFF;
                sCurrentDevice.err.errorCode = (int)(bytes[idx++] & 0xFF) + (int)((bytes[idx++] & 0xFF) << 8);
                sCurrentDevice.err.reserve3 = (int)bytes[idx++] & 0xFF;
                sCurrentDevice.err.reserve4 = (int)bytes[idx++] & 0xFF;
                sCurrentDevice.err.errorTime = (int)((bytes[idx++] & 0xFF) << 8) + (int)(bytes[idx++] & 0xFF);
                sCurrentDevice.err.errorCount = (int)((bytes[idx++] & 0xFF) << 8) + (int)(bytes[idx++] & 0xFF);
                sCurrentDevice.err.reserve5 = (int)bytes[idx++] & 0xFF;
                sCurrentDevice.err.reserve6 = (int)bytes[idx++] & 0xFF;
                break;

            //디바이스 상태 데이터
            case COMMAND_SD:
                CommonUtils.customLog(TAG, "Receive COMMAND_SD", Log.ERROR);
                if(sCurrentDevice.sd == null)
                    sCurrentDevice.sd = new SdPacket();
                sCurrentDevice.sd.errorCD = (int)((bytes[idx++]&0xFF)<<8)+(int)(bytes[idx++]&0xFF); //2byte 02 CD(715)통신에러(룸콘/컨트롤러) *기기별코드표 확인
                sCurrentDevice.sd.errorLevel = (int)bytes[idx++]&0xFF; //1byte 01:default, N:Level값

                try {
                    // ikHwang 2020-02-06 오후 4:58 에러 레벨 처리 현재 하위 4bit만 사용
                    String levelHex = String.format("%02X", sCurrentDevice.sd.errorLevel);
                    sCurrentDevice.sd.errorLevel_L = Integer.parseInt(levelHex.substring(1, 2), 16);
                    sCurrentDevice.sd.errorLevel_H = Integer.parseInt(levelHex.substring(0, 1), 16);
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }

                sCurrentDevice.sd.operationStatus = (int)bytes[idx++]&0xFF; //1byte 정지/운전 상태  01:정지, 02:가동
                sCurrentDevice.sd.operationMode = (int)bytes[idx++]&0xFF; //1byte 동작모드 01:대기, 02:냉방, 03:제습, 04:일반환기, 05:배기, 06:요리, 07:청소, 08:공기청정 09:황사, 0A:난방, 0B:가습, 0C:자동운전, 0D:건강호흡, 0E:피부보호, 0F:급기
                sCurrentDevice.sd.oduOperationStatus = (int)bytes[idx++]&0xFF; //1byte ODU 동작상태  01:대기, 02:냉방, 03:제습, 04:일반환기, 05:배기, 06:요리, 07:청소, 08:공기청정 09:황사, 0A:난방, 0B:가습, 0C:자동운전, 0D:건강호흡, 0E:피부보호, 0F:급기
                sCurrentDevice.sd.option = (int)bytes[idx++] & 0xFF; //1byte 옵션기능 01:없음, 02:터보운전, 03:절전운전

                /*sCurrentDevice.sd.operationMode = 3;
                sCurrentDevice.sd.oduOperationStatus = 3;
                sCurrentDevice.sd.option = 1;*/
                sCurrentDevice.sd.reservation = (int)bytes[idx++] & 0xFF; //1byte 예약기능   01:없음, 02:취침예약, 03:운전예약, 04:정지예약
                CommonUtils.customLog(TAG, "$$$$$ operation Mode : " + sCurrentDevice.sd.operationMode  + ", oduOperationStatus : " + sCurrentDevice.sd.oduOperationStatus + ", option : " + sCurrentDevice.sd.option + ", reservation : " + sCurrentDevice.sd.reservation, Log.ERROR);

                sCurrentDevice.sd.setWindLevel = (int)bytes[idx++] & 0xFF;//1byte 풍량 설정 값 01:미풍, 02:약풍, 03:강풍, 04:자동
                sCurrentDevice.sd.currontOduWindLevel = (int)bytes[idx++] & 0xFF; //1byte ODU 풍량 현재값 01:미풍, 02:약풍, 03:강풍, 04:자동
                sCurrentDevice.sd.windLevelPercent = (int)bytes[idx++] & 0xFF; //1byte 풍량 설정 값(중국향) 5~99% -> HEX 변환 후 전달 ex> 95 -> 0x5F
                sCurrentDevice.sd.setTemp = (int)((bytes[idx++] & 0xFF) << 8) + (int)(bytes[idx++] & 0xFF);//1byte 설정온도 온도*2 ->HEX변환 후 전달.
                sCurrentDevice.sd.curruntTemp = (int)((bytes[idx++] & 0xFF) << 8) + (int)(bytes[idx++] & 0xFF); //1byte 현재온도 온도*2 ->HEX변환 후 전달.
                sCurrentDevice.sd.setHumi = (int)(bytes[idx++] & 0xFF); //1byte 설정습도  온도*2 ->HEX변환 후 전달.
                sCurrentDevice.sd.curruntHumi = (int)bytes[idx++] & 0xFF; //1byte 현재습도  온도*2 ->HEX변환 후 전달.
//                sCurrentDevice.sd.setReservationTimer = (int)((bytes[idx++]&0xFF)<<8)+(int)(bytes[idx++]&0xFF); //2byte 예약 운전 설정 값   분단위 -> HEX 전달
                sCurrentDevice.sd.setReservationTimer = ((int)bytes[idx++]) * 100 + (int)bytes[idx++]; //2byte 예약 운전 설정 값   시:분 -> HEX 전달
                sCurrentDevice.sd.remainReservationTimer = (int)((bytes[idx++] & 0xFF) << 8) + (int)(bytes[idx++] & 0xFF); //2byte 남은 운전 예약 시간 값    분단위 -> HEX 전달
                sCurrentDevice.sd.setSwing = (int)bytes[idx++] & 0xFF; //1byte 루퍼 스윙 동작 설정 값  01:swing off, 02:swing on
                sCurrentDevice.sd.flagHepaFilter = (int)bytes[idx++] & 0xFF; //1byte 헤파필터 시간경과 플래그  01:Normal, 02:리셋시간 Over
                sCurrentDevice.sd.flagFreeFilter = (int)bytes[idx++] & 0xFF; //1byte 프리필터 시간경과 플래그  01:Normal, 02:리셋시간 Over
                sCurrentDevice.sd.usingTimeOduFilter = (int)((bytes[idx++] & 0xFF) << 8) + (int) (bytes[idx++] & 0xFF); //2byte ODU 필터 시간   1시간 단위 -> HEX 전달 ex>10시간 -> 0x0A

                sCurrentDevice.sd.dustSensor.level = (int)bytes[idx++] & 0xFF; //먼지센서 레벨
                sCurrentDevice.sd.dustSensor.value = (int)((bytes[idx++] & 0xFF) << 8) + (int)(bytes[idx++] & 0xFF);//먼지센서 절대값

                sCurrentDevice.sd.co2Sensor.level = (int)bytes[idx++] & 0xFF; //co2센서 레벨
                sCurrentDevice.sd.co2Sensor.value = (int)((bytes[idx++] & 0xFF) << 8) + (int)(bytes[idx++] & 0xFF);//co2센서 절대값

                sCurrentDevice.sd.vocSensor.level = (int)bytes[idx++] & 0xFF; //VOC센서 레벨
                sCurrentDevice.sd.vocSensor.value = (int)((bytes[idx++] & 0xFF) << 8) + (int)(bytes[idx++] & 0xFF); //VOC센서 절대값

                sCurrentDevice.sd.flagInnerHeaterOperation = (int)bytes[idx++] & 0xFF;  //1byte 내부 히터 가동 플래그   01:비가동, 02:가동
                sCurrentDevice.sd.flagOuterCloseInput = (int)bytes[idx++] & 0xFF; //1byte  외부 접점 입력 플래그   01:내부, 02:내부접점입력(후드입력)
                sCurrentDevice.sd.oduHeatSourceOperationRequest = (int)bytes[idx++] & 0xFF; //1byte ODU 열원 가동/정지 요청상태  01:비요청, 02:요청
                sCurrentDevice.sd.oduRoomsFanOperationRequest = (int)bytes[idx++] & 0xFF; //1byte ODU 각실 FLAP/FAN동작 요청상태   01:비요청, 02:요청
                sCurrentDevice.sd.indoorUnitLightingLevel = (int)bytes[idx++] & 0xFF; //1byte 실내기 라이팅 밝기 값  HEX(0~100%)
                sCurrentDevice.sd.indoorUnitSoundLevel = (int)bytes[idx++] & 0xFF; //1byte 실내기 부저음 음량 값  HEX(0~100%)
                sCurrentDevice.sd.controlRoom1 = (int)bytes[idx++] & 0xFF; //1byte 1번방 제어
                sCurrentDevice.sd.controlRoom2 = (int)bytes[idx++] & 0xFF; //1byte 2번방 제어
                sCurrentDevice.sd.controlRoom3 = (int)bytes[idx++] & 0xFF; //1byte 3번방 제어
                sCurrentDevice.sd.controlRoom4 = (int)bytes[idx++] & 0xFF; //1byte 4번방 제어
                sCurrentDevice.sd.controlRoom5 = (int)bytes[idx++] & 0xFF; //1byte 5번방 제어
                sCurrentDevice.sd.controlRoom6 = (int)bytes[idx++] & 0xFF; //1byte 6번방 제어
                sCurrentDevice.sd.controlRoom7 = (int)bytes[idx++] & 0xFF; //1byte 7번방 제어
                sCurrentDevice.sd.controlRoom8 = (int)bytes[idx++] & 0xFF; //1byte 8번방 제어
                sCurrentDevice.sd.controlDressRoom = (int)bytes[idx++] & 0xFF; //1byte 각실 제어(드레스룸)
                sCurrentDevice.sd.usingTimeOduFreeFilter = (int)((bytes[idx++] & 0xFF) << 8) + (int)(bytes[idx++] & 0xFF); //2byte ODU 프리필터 사용시간
                sCurrentDevice.sd.deepSleepUse = (int)bytes[idx++] & 0xFF; //1byte 숙면모드 설정/해제
                sCurrentDevice.sd.deepSleepStart = (int)bytes[idx++] & 0xFF; //1byte 숙면모드 켜짐시간
                sCurrentDevice.sd.deepSleepEnd = (int)bytes[idx++] & 0xFF; //1byte 숙면모드 꺼짐시간

                if (bytes.length >= idx + 3) {
                    sCurrentDevice.sd.oduFreeFilterAlarm = (int)bytes[idx++] & 0xFF; //1byte 프리필터 청소 알림  (사용안함)
                    sCurrentDevice.sd.oduHepaFilterAlarm = (int)bytes[idx++] & 0xFF; //1byte 전자헤파필터 청소 알림 (사용안함)
                    sCurrentDevice.sd.oduFreeAndHepaFilterAlarm = (int)bytes[idx++] & 0xFF; //1byte 프리 및 전자헤파필터 청소 알림 (사용안함)
                }

                // 키친플러스 디퓨저 에러 및 방위치
                if (bytes.length >= idx + 2) {
                    try {
                        String levelHex = String.format("%02X", (int)bytes[idx++] & 0xFF);
                        sCurrentDevice.sd.diffuserNumber = Integer.parseInt(levelHex.substring(1, 2), 16); //1byte 방 및 디퓨저 에러 위치
                        sCurrentDevice.sd.diffuserModel = Integer.parseInt(levelHex.substring(0, 1), 16); //1byte 프리 및 전자헤파필터 청소 알림 (사용안함)
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                    }
                    sCurrentDevice.sd.diffuserRoomNumber = (int)bytes[idx++] & 0xFF; //1byte 프리 및 전자헤파필터 청소 알림 (사용안함)
                }

                if (bytes.length >= idx + 22) {
                    sCurrentDevice.sd.sbErrorCD = (int)((bytes[idx++] & 0xFF) << 8) + (int)(bytes[idx++] & 0xFF);
                    sCurrentDevice.sd.sbErrorLevel = (int)bytes[idx++] & 0xFF;
                    sCurrentDevice.sd.sbDustSensor = (int)bytes[idx++] & 0xFF;
                    sCurrentDevice.sd.sbCO2Sensor = (int)bytes[idx++] & 0xFF;
                    sCurrentDevice.sd.sbVocSensor = (int)bytes[idx++] & 0xFF;
                    sCurrentDevice.sd.sbDustLevel = (int)bytes[idx++] & 0xFF;
                    sCurrentDevice.sd.sbDustValue = (int)((bytes[idx++] & 0xFF) << 8) + (int)(bytes[idx++] & 0xFF);
                    sCurrentDevice.sd.sbCO2Level = (int)bytes[idx++] & 0xFF;
                    sCurrentDevice.sd.sbCO2Value = (int)((bytes[idx++] & 0xFF) << 8) + (int)(bytes[idx++] & 0xFF);
                    sCurrentDevice.sd.sbVocLevel = (int)bytes[idx++] & 0xFF;
                    sCurrentDevice.sd.sbVocValue = (int)((bytes[idx++] & 0xFF) << 8) + (int)(bytes[idx++] & 0xFF);
                    //2019.04.15 추가
                    sCurrentDevice.sd.dustPMSensorLevel = (int)bytes[idx++] & 0xFF;
                    sCurrentDevice.sd.dustPMSensorValue = (int)((bytes[idx++] & 0xFF) << 8) + (int)(bytes[idx++] & 0xFF);

                    sCurrentDevice.sd.sbAirLevel = (int)bytes[idx++] & 0xFF;
                    sCurrentDevice.sd.sbAirValue = (int)((bytes[idx++] & 0xFF) << 8) + (int)(bytes[idx++] & 0xFF);
                    sCurrentDevice.sd.sbInsideHeat = (int)((bytes[idx++] & 0xFF) << 8) + (int)(bytes[idx++] & 0xFF);
                    sCurrentDevice.sd.sbInsideHum = (int)bytes[idx++] & 0xFF;
                    sCurrentDevice.sd.sbPower = (int)bytes[idx++] & 0xFF;
                }

                sCurrentDevice.sd.lastUpdateTime = System.currentTimeMillis();
                break;

            //PUSH 데이터
            case COMMAND_PSD:
                if(sCurrentDevice.push == null)
                    sCurrentDevice.push = new PushPacket();
                sCurrentDevice.push.co2CD  = (int)bytes[idx++] & 0xFF;
                sCurrentDevice.push.tvocCD  = (int)bytes[idx++] & 0xFF;
                sCurrentDevice.push.pm25CD  = (int)bytes[idx++] & 0xFF;

                sCurrentDevice.push.lastUpdateTime = System.currentTimeMillis();
                break;

            case COMMAND_LED_STATUS_INFO: // ikHwang 2019-08-26 오후 3:59 에어모니터 상태
                if(sCurrentDevice.led == null)
                    sCurrentDevice.led = new LedSettingPacket();

                idx+=8; // 디바이스 아이디가 함께 전달되어 추가함

                sCurrentDevice.led.brightness = bytes[idx++] & 0xFF;
                break;

            case COMMAND_FILTER_STATUS_INFO: // ikHwang 2019-08-26 오후 3:59 필터 상태
                if(sCurrentDevice.filter == null)
                    sCurrentDevice.filter = new FilterPacket();

                sCurrentDevice.filter.flagHepaFilter = (int)bytes[idx++] & 0xFF;
                sCurrentDevice.filter.flagFreeFilter = (int)bytes[idx++] & 0xFF;
                sCurrentDevice.filter.flagEHepaFilter = (int)bytes[idx++] & 0xFF;
                sCurrentDevice.filter.usingTimeOduFilter = (int)((bytes[idx++] & 0xFF)); //1byte 필터 남은 수명 %단위
//                sCurrentDevice.filter.usingTimeOduFilter = (int)((bytes[idx++]&0xFF)<<8)+(int)(bytes[idx++]&0xFF); //2byte ODU 프리필터 사용시간
                break;
        }
        notifyDataSetChanged(sCurrentDevice.head.cmd);
    }

    public void notifyDataSetChanged(int cmd) {
        if (mDataChangedListener != null)
            mDataChangedListener.onDataChanged(cmd);
    }

    /**
     * ikHwang 2020-11-06 오전 10:48 수신 데이터 파싱
     * @param cmdType
     * @param receivedData
     */
    public void setReceivedData(int cmdType, String receivedData){
        if(sCurrentDevice == null) sCurrentDevice = new Device();

        //Head Packet
        if(sCurrentDevice.head == null) sCurrentDevice.head = new HeadPacket();
        sCurrentDevice.head.lastUpdateTime = System.currentTimeMillis();

        if(!TextUtils.isEmpty(receivedData)){
            try {
                JSONObject receivedObject = new JSONObject(receivedData);

                if(!receivedObject.has("request")) return;

                JSONObject jsonObject = receivedObject.getJSONObject("request");

                if(sCurrentDevice.common == null) sCurrentDevice.common = new CommonPacket();
                // 1-korea, 2-china
                if(jsonObject.has("countryCode")) sCurrentDevice.common.countryCode = jsonObject.optInt("countryCode", 1);
                // 1:NTR-10PW-CN(중국향), 2:NTR-10PW(일반[1차]), 3:NTR-10DW(TAC), 4:NRT-20D(프리즘[키친플러스]), 5:NRT-20L(제습), 6:NRT-21D(대용량)
                if(jsonObject.has("modelCode")) sCurrentDevice.common.modelCode = jsonObject.optInt("modelCode", 1);
                // 프로토콜 문서상 설명 - MAJ값 전송 (ex. V10-0 의 경우 10(Major) 전송
                if(jsonObject.has("deviceVersion")) sCurrentDevice.common.deviceVer  = jsonObject.optInt("deviceVersion", 0);
                // 연결안됨 : 01, 연결됨 : 02 (SB : default 0x00)
                if(jsonObject.has("connectedSensingBox")) sCurrentDevice.common.senboxConnected = jsonObject.optInt("connectedSensingBox", 1);
                sCurrentDevice.common.lastUpdateTime = System.currentTimeMillis();

                switch (cmdType){
                    case COMMAND_DID: // DID 정보 파싱
                        if(sCurrentDevice.did == null) sCurrentDevice.did = new DidPacket();

                        // RPM 제어 : 01, VSP 제어 : 02, DF-VSP 제어 : 03, KPV-VSP 제어 : 04
                        if(jsonObject.has("vspRpm")) sCurrentDevice.did.vspRpmControl = jsonObject.optInt("vspRpm", 1);
                        // 확인불가 : 01, TAC510 : 02, TAC550 : 03
                        if(jsonObject.has("oduModel")) sCurrentDevice.did.oduModel = jsonObject.optInt("oduModel", 1);

                        // 냉방운전 무:01, 유:02
                        if(jsonObject.has("airConditioningMode") && jsonObject.getBoolean("airConditioningMode") && jsonObject.has("airConditioning")){
                            sCurrentDevice.did.coldMode.modeUse = MODE_USED;

                            JSONObject obj = jsonObject.getJSONObject("airConditioning");
                            if(obj.has("turbo")) sCurrentDevice.did.coldMode.turboUse = obj.optInt("turbo", 1);
                            if(obj.has("powerSaving")) sCurrentDevice.did.coldMode.pwrSavingUse = obj.optInt("powerSaving", 1);
                            if(obj.has("sleep")) sCurrentDevice.did.coldMode.sleepUse = obj.optInt("sleep", 1);
                            if(obj.has("offReservation")) sCurrentDevice.did.coldMode.reservationOffUse = obj.optInt("offReservation", 1);
                            if(obj.has("onReservation")) sCurrentDevice.did.coldMode.reservationOnUse = obj.optInt("onReservation", 1);
                        }else{
                            sCurrentDevice.did.coldMode.modeUse = MODE_NOT_USED;
                        }

                        // 난방운전 무:01, 유:02
                        if(jsonObject.has("airHeatingMode") && jsonObject.getBoolean("airHeatingMode") && jsonObject.has("airHeating")){
                            sCurrentDevice.did.hotMode.modeUse = MODE_USED;

                            JSONObject obj = jsonObject.getJSONObject("airHeating");
                            if(obj.has("turbo")) sCurrentDevice.did.hotMode.turboUse = obj.optInt("turbo", 1);
                            if(obj.has("powerSaving")) sCurrentDevice.did.hotMode.pwrSavingUse = obj.optInt("powerSaving", 1);
                            if(obj.has("sleep")) sCurrentDevice.did.hotMode.sleepUse = obj.optInt("sleep", 1);
                            if(obj.has("offReservation")) sCurrentDevice.did.hotMode.reservationOffUse = obj.optInt("offReservation", 1);
                            if(obj.has("onReservation")) sCurrentDevice.did.hotMode.reservationOnUse = obj.optInt("onReservation", 1);
                        }else{
                            sCurrentDevice.did.hotMode.modeUse = MODE_NOT_USED;
                        }

                        // 제습운전 무:01, 유:02
                        if(jsonObject.has("dehumidificationMode") && jsonObject.getBoolean("dehumidificationMode") && jsonObject.has("dehumidification")){
                            sCurrentDevice.did.dehumiMode.modeUse = MODE_USED;

                            JSONObject obj = jsonObject.getJSONObject("dehumidification");
                            if(obj.has("turbo")) sCurrentDevice.did.dehumiMode.turboUse = obj.optInt("turbo", 1);
                            if(obj.has("powerSaving")) sCurrentDevice.did.dehumiMode.pwrSavingUse = obj.optInt("powerSaving", 1);
                            if(obj.has("sleep")) sCurrentDevice.did.dehumiMode.sleepUse = obj.optInt("sleep", 1);
                            if(obj.has("offReservation")) sCurrentDevice.did.dehumiMode.reservationOffUse = obj.optInt("offReservation", 1);
                            if(obj.has("onReservation")) sCurrentDevice.did.dehumiMode.reservationOnUse = obj.optInt("onReservation", 1);
                        }else{
                            sCurrentDevice.did.dehumiMode.modeUse = MODE_NOT_USED;
                        }

                        // 가습운전 무:01, 유:02
                        if(jsonObject.has("humidificationMode") && jsonObject.getBoolean("humidificationMode") && jsonObject.has("humidification")){
                            sCurrentDevice.did.humiMode.modeUse = MODE_USED;

                            JSONObject obj = jsonObject.getJSONObject("humidification");
                            if(obj.has("turbo")) sCurrentDevice.did.humiMode.turboUse = obj.optInt("turbo", 1);
                            if(obj.has("powerSaving")) sCurrentDevice.did.humiMode.pwrSavingUse = obj.optInt("powerSaving", 1);
                            if(obj.has("sleep")) sCurrentDevice.did.humiMode.sleepUse = obj.optInt("sleep", 1);
                            if(obj.has("offReservation")) sCurrentDevice.did.humiMode.reservationOffUse = obj.optInt("offReservation", 1);
                            if(obj.has("onReservation")) sCurrentDevice.did.humiMode.reservationOnUse = obj.optInt("onReservation", 1);
                        }else{
                            sCurrentDevice.did.humiMode.modeUse = MODE_NOT_USED;
                        }

                        // 환기운전 무:01, 유:02
                        if(jsonObject.has("ventilationMode") && jsonObject.getBoolean("ventilationMode") && jsonObject.has("ventilation")){
                            sCurrentDevice.did.ventiMode.modeUse = MODE_USED;

                            JSONObject obj = jsonObject.getJSONObject("ventilation");
                            if(obj.has("turbo")) sCurrentDevice.did.ventiMode.turboUse = obj.optInt("turbo", 1);
                            if(obj.has("powerSaving")) sCurrentDevice.did.ventiMode.pwrSavingUse = obj.optInt("powerSaving", 1);
                            if(obj.has("sleep")) sCurrentDevice.did.ventiMode.sleepUse = obj.optInt("sleep", 1);
                            if(obj.has("offReservation")) sCurrentDevice.did.ventiMode.reservationOffUse = obj.optInt("offReservation", 1);
                            if(obj.has("onReservation")) sCurrentDevice.did.ventiMode.reservationOnUse = obj.optInt("onReservation", 1);
                        }else{
                            sCurrentDevice.did.ventiMode.modeUse = MODE_NOT_USED;
                        }

                        // 배기운전 무:01, 유:02
                        if(jsonObject.has("exhaustMode") && jsonObject.getBoolean("exhaustMode") && jsonObject.has("exhaust")){
                            sCurrentDevice.did.exhaustMode.modeUse = MODE_USED;

                            JSONObject obj = jsonObject.getJSONObject("exhaust");
                            if(obj.has("turbo")) sCurrentDevice.did.exhaustMode.turboUse = obj.optInt("turbo", 1);
                            if(obj.has("powerSaving")) sCurrentDevice.did.exhaustMode.pwrSavingUse = obj.optInt("powerSaving", 1);
                            if(obj.has("sleep")) sCurrentDevice.did.exhaustMode.sleepUse = obj.optInt("sleep", 1);
                            if(obj.has("offReservation")) sCurrentDevice.did.exhaustMode.reservationOffUse = obj.optInt("offReservation", 1);
                            if(obj.has("onReservation")) sCurrentDevice.did.exhaustMode.reservationOnUse = obj.optInt("onReservation", 1);

                        }else{
                            sCurrentDevice.did.exhaustMode.modeUse = MODE_NOT_USED;
                        }

                        // 공기청정운전 무:01, 유:02
                        if(jsonObject.has("airCleaningMode") && jsonObject.getBoolean("airCleaningMode") && jsonObject.has("airCleaning")){
                            sCurrentDevice.did.airCleanMode.modeUse = MODE_USED;

                            JSONObject obj = jsonObject.getJSONObject("airCleaning");
                            if(obj.has("turbo")) sCurrentDevice.did.airCleanMode.turboUse = obj.optInt("turbo", 1);
                            if(obj.has("powerSaving")) sCurrentDevice.did.airCleanMode.pwrSavingUse = obj.optInt("powerSaving", 1);
                            if(obj.has("sleep")) sCurrentDevice.did.airCleanMode.sleepUse = obj.optInt("sleep", 1);
                            if(obj.has("offReservation")) sCurrentDevice.did.airCleanMode.reservationOffUse = obj.optInt("offReservation", 1);
                            if(obj.has("onReservation")) sCurrentDevice.did.airCleanMode.reservationOnUse = obj.optInt("onReservation", 1);
                        }else{
                            sCurrentDevice.did.airCleanMode.modeUse = MODE_NOT_USED;
                        }

                        // 주방(요리모드)운전 무:01, 유:02
                        if(jsonObject.has("cookingMode") && jsonObject.getBoolean("cookingMode") && jsonObject.has("cooking")){
                            sCurrentDevice.did.kitchenMode.modeUse = MODE_USED;

                            JSONObject obj = jsonObject.getJSONObject("cooking");
                            if(obj.has("turbo")) sCurrentDevice.did.kitchenMode.turboUse = obj.optInt("turbo", 1);
                            if(obj.has("powerSaving")) sCurrentDevice.did.kitchenMode.pwrSavingUse = obj.optInt("powerSaving", 1);
                            if(obj.has("sleep")) sCurrentDevice.did.kitchenMode.sleepUse = obj.optInt("sleep", 1);
                            if(obj.has("offReservation")) sCurrentDevice.did.kitchenMode.reservationOffUse = obj.optInt("offReservation", 1);
                            if(obj.has("onReservation")) sCurrentDevice.did.kitchenMode.reservationOnUse = obj.optInt("onReservation", 1);
                        }else{
                            sCurrentDevice.did.kitchenMode.modeUse = MODE_NOT_USED;
                        }

                        // 청소운전 무:01, 유:02
                        if(jsonObject.has("cleaningMode") && jsonObject.getBoolean("cleaningMode") && jsonObject.has("cleaning")){
                            sCurrentDevice.did.cleanMode.modeUse = MODE_USED;

                            JSONObject obj = jsonObject.getJSONObject("cleaning");
                            if(obj.has("turbo")) sCurrentDevice.did.cleanMode.turboUse = obj.optInt("turbo", 1);
                            if(obj.has("powerSaving")) sCurrentDevice.did.cleanMode.pwrSavingUse = obj.optInt("powerSaving", 1);
                            if(obj.has("sleep")) sCurrentDevice.did.cleanMode.sleepUse = obj.optInt("sleep", 1);
                            if(obj.has("offReservation")) sCurrentDevice.did.cleanMode.reservationOffUse = obj.optInt("offReservation", 1);
                            if(obj.has("onReservation")) sCurrentDevice.did.cleanMode.reservationOnUse = obj.optInt("onReservation", 1);
                        }else{
                            sCurrentDevice.did.cleanMode.modeUse = MODE_NOT_USED;
                        }

                        // 황사운전 무:01, 유:02
                        if(jsonObject.has("yellowDustMode") && jsonObject.getBoolean("yellowDustMode") && jsonObject.has("yellowDust")){
                            sCurrentDevice.did.dustMode.modeUse = MODE_USED;

                            JSONObject obj = jsonObject.getJSONObject("yellowDust");
                            if(obj.has("turbo")) sCurrentDevice.did.dustMode.turboUse = obj.optInt("turbo", 1);
                            if(obj.has("powerSaving")) sCurrentDevice.did.dustMode.pwrSavingUse = obj.optInt("powerSaving", 1);
                            if(obj.has("sleep")) sCurrentDevice.did.dustMode.sleepUse = obj.optInt("sleep", 1);
                            if(obj.has("offReservation")) sCurrentDevice.did.dustMode.reservationOffUse = obj.optInt("offReservation", 1);
                            if(obj.has("onReservation")) sCurrentDevice.did.dustMode.reservationOnUse = obj.optInt("onReservation", 1);
                        }else{
                            sCurrentDevice.did.dustMode.modeUse = MODE_NOT_USED;
                        }

                        // 건강호흡운전 무:01, 유:02
                        if(jsonObject.has("healthyBreathingMode") && jsonObject.getBoolean("healthyBreathingMode") && jsonObject.has("healthyBreathing")){
                            sCurrentDevice.did.healthMode.modeUse = MODE_USED;

                            JSONObject obj = jsonObject.getJSONObject("healthyBreathing");
                            if(obj.has("turbo")) sCurrentDevice.did.healthMode.turboUse = obj.optInt("turbo", 1);
                            if(obj.has("powerSaving")) sCurrentDevice.did.healthMode.pwrSavingUse = obj.optInt("powerSaving", 1);
                            if(obj.has("sleep")) sCurrentDevice.did.healthMode.sleepUse = obj.optInt("sleep", 1);
                            if(obj.has("offReservation")) sCurrentDevice.did.healthMode.reservationOffUse = obj.optInt("offReservation", 1);
                            if(obj.has("onReservation")) sCurrentDevice.did.healthMode.reservationOnUse = obj.optInt("onReservation", 1);
                        }else{
                            sCurrentDevice.did.healthMode.modeUse = MODE_NOT_USED;
                        }

                        // 피부보습운전 무:01, 유:02
                        if(jsonObject.has("skinMoisturizingMode") && jsonObject.getBoolean("skinMoisturizingMode") && jsonObject.has("skinMoisturizing")){
                            sCurrentDevice.did.skinMode.modeUse = MODE_USED;

                            JSONObject obj = jsonObject.getJSONObject("skinMoisturizing");
                            if(obj.has("turbo")) sCurrentDevice.did.skinMode.turboUse = obj.optInt("turbo", 1);
                            if(obj.has("powerSaving")) sCurrentDevice.did.skinMode.pwrSavingUse = obj.optInt("powerSaving", 1);
                            if(obj.has("sleep")) sCurrentDevice.did.skinMode.sleepUse = obj.optInt("sleep", 1);
                            if(obj.has("offReservation")) sCurrentDevice.did.skinMode.reservationOffUse = obj.optInt("offReservation", 1);
                            if(obj.has("onReservation")) sCurrentDevice.did.skinMode.reservationOnUse = obj.optInt("onReservation", 1);
                        }else{
                            sCurrentDevice.did.skinMode.modeUse = MODE_NOT_USED;
                        }

                        // 자동운전 무:01, 유:02
                        if(jsonObject.has("autoOperationMode") && jsonObject.getBoolean("autoOperationMode") && jsonObject.has("autoOperation")){
                            sCurrentDevice.did.autoMode.modeUse = MODE_USED;

                            JSONObject obj = jsonObject.getJSONObject("autoOperation");
                            if(obj.has("turbo")) sCurrentDevice.did.autoMode.turboUse = obj.optInt("turbo", 1);
                            if(obj.has("powerSaving")) sCurrentDevice.did.autoMode.pwrSavingUse = obj.optInt("powerSaving", 1);
                            if(obj.has("sleep")) sCurrentDevice.did.autoMode.sleepUse = obj.optInt("sleep", 1);
                            if(obj.has("offReservation")) sCurrentDevice.did.autoMode.reservationOffUse = obj.optInt("offReservation", 1);
                            if(obj.has("onReservation")) sCurrentDevice.did.autoMode.reservationOnUse = obj.optInt("onReservation", 1);
                        }else{
                            sCurrentDevice.did.autoMode.modeUse = MODE_NOT_USED;
                        }

                        // 급기운전 무:01, 유:02
                        if(jsonObject.has("airSupplyMode") && jsonObject.getBoolean("airSupplyMode") && jsonObject.has("airSupply")){
                            sCurrentDevice.did.aerationMode.modeUse = MODE_USED;

                            JSONObject obj = jsonObject.getJSONObject("airSupply");
                            if(obj.has("turbo")) sCurrentDevice.did.aerationMode.turboUse = obj.optInt("turbo", 1);
                            if(obj.has("powerSaving")) sCurrentDevice.did.aerationMode.pwrSavingUse = obj.optInt("powerSaving", 1);
                            if(obj.has("sleep")) sCurrentDevice.did.aerationMode.sleepUse = obj.optInt("sleep", 1);
                            if(obj.has("offReservation")) sCurrentDevice.did.aerationMode.reservationOffUse = obj.optInt("offReservation", 1);
                            if(obj.has("onReservation")) sCurrentDevice.did.aerationMode.reservationOnUse = obj.optInt("onReservation", 1);
                        }else{
                            sCurrentDevice.did.aerationMode.modeUse = MODE_NOT_USED;
                        }

                        // ODU 용량 - 확인불가 : 01 , 10S: 02, 15S : 03, 20S : 04, 25S : 05, 30S : 06
                        if(jsonObject.has("oduCapacity")) sCurrentDevice.did.oduCapacity = jsonObject.optInt("oduCapacity", 1);
                        // ODU 히터 용량 - 확인불가 : 01 , S0(히터 X) : 02, S6(600W) : 03
                        if(jsonObject.has("oduHeaterCapacity")) sCurrentDevice.did.oduHeaterCapacity = jsonObject.optInt("oduHeaterCapacity", 1);

                        // 제습 희망습도 최저, 최고
                        if(jsonObject.has("dehumidificationDesiredHumidityMin")){
                            JSONObject obj = jsonObject.getJSONObject("dehumidificationDesiredHumidityMin");

                            // 제습 희망습도 최저 설정값
                            sCurrentDevice.did.dehumiSetHumi.min = obj.optInt("min", 0);
                            // 제습 희망습도 최고 설정값
                            sCurrentDevice.did.dehumiSetHumi.max = obj.optInt("max", 0);
                        }

                        // 가습 희망습도 최저, 최고
                        if(jsonObject.has("humidificationDesiredHumidityMin")) {
                            JSONObject obj = jsonObject.getJSONObject("humidificationDesiredHumidityMin");

                            // 가습 희망습도 최저 설정값
                            sCurrentDevice.did.humiSetHumi.min = obj.optInt("min", 0);
                            // 가습 희망습도 최고 설정값
                            sCurrentDevice.did.humiSetHumi.max = obj.optInt("max", 0);
                        }

                        // 희망온도 자동 최저, 최고
                        if(jsonObject.has("autoDesiredTemperatureMin")) {
                            JSONObject obj = jsonObject.getJSONObject("autoDesiredTemperatureMin");
                            
                            // 희망온도 자동 최저 설정값
                            sCurrentDevice.did.autoSetTemp.min = obj.optInt("min", 0);
                            // 희망온도 자동 최고 설정값
                            sCurrentDevice.did.autoSetTemp.max = obj.optInt("max", 0);
                        }

                        // 희망습도 자동 최저, 최고
                        if(jsonObject.has("autoDesiredHumidityMin")) {
                            JSONObject obj = jsonObject.getJSONObject("autoDesiredHumidityMin");
                            
                            // 희망습도 자동 최저 설정값
                            sCurrentDevice.did.autoSetHumi.min = jsonObject.optInt("min", 1);
                            // 희망습도 자동 최고 설정값
                            sCurrentDevice.did.autoSetHumi.max = jsonObject.optInt("max", 1);
                        }

                        // 냉방 최저, 최고
                        if(jsonObject.has("airConditioningDesiredTemperature")) {
                            JSONObject obj = jsonObject.getJSONObject("airConditioningDesiredTemperature");

                            // 냉방 최저 설정값
                            sCurrentDevice.did.coldSetTemp.min = jsonObject.optInt("min", 1);
                            // 냉방 최고 설정값
                            sCurrentDevice.did.coldSetTemp.max = jsonObject.optInt("max", 1);
                        }

                        // 난방 최저, 최고
                        if(jsonObject.has("airHeatingDesiredTemperature")) {
                            JSONObject obj = jsonObject.getJSONObject("airHeatingDesiredTemperature");

                            // 냉방 최저 설정값
                            sCurrentDevice.did.hotSetTemp.min = jsonObject.optInt("min", 1);
                            // 냉방 최고 설정값
                            sCurrentDevice.did.hotSetTemp.max = jsonObject.optInt("max", 1);
                        }

                        // 절전냉방 최저, 최고
                        if(jsonObject.has("powerSavingAirConditioningDesiredTemperature")) {
                            JSONObject obj = jsonObject.getJSONObject("powerSavingAirConditioningDesiredTemperature");

                            // 냉방 최저 설정값
                            sCurrentDevice.did.pwrSavingColdSetTemp.min = jsonObject.optInt("min", 1);
                            // 냉방 최고 설정값
                            sCurrentDevice.did.pwrSavingColdSetTemp.max = jsonObject.optInt("max", 1);
                        }

                        // 필터 교체 설정 시간 (프리즘 이전일 경우 사용 시간, 프리즘 이후는 남은 용량 %정보)
                        if(jsonObject.has("filterReplacementDesiredTime")) sCurrentDevice.did.maxFilterUseTime = jsonObject.optInt("filterReplacementDesiredTime", 0);

                        // APS 적용 유무 - APS 적용안함 : 01, APS 적용함 : 02
                        if(jsonObject.has("useApsStatus")) sCurrentDevice.did.apsHasFlag = jsonObject.optInt("useApsStatus", 1);

                        // UV LED 적용 유무 - UV 적용안함 : 01, UV 적용함 : 02
                        if(jsonObject.has("supportUvcLed")) sCurrentDevice.did.uvLED = jsonObject.optInt("supportUvcLed", 1);

                        // 바이패스 지원 유무
                        if(jsonObject.has("supportByPass")) {
                            sCurrentDevice.did.bypassMode.modeUse = MODE_USED;
                            sCurrentDevice.did.supportByPass = jsonObject.optInt("supportByPass", 1);
                        } else {
                            sCurrentDevice.did.bypassMode.modeUse = MODE_NOT_USED;
                            CommonUtils.customLog("did-check", "there's NO supportByPass data", Log.ERROR);
                        }
                        break;

                    case COMMAND_SD: // SD 정보 파싱
                        /*
                        * 에러레벨 상하위 바이트 분리해서 전달 필요
                        * 예약운전 설정시간 분으로 변경해서 전달
                        * 센서 파라미터 정성/정량 키값 동일함
                        * 각실제어 리스트로 할건지 현재와 동일하게 고정할 것인지
                        * 방 및 디퓨저 에러 위치 추가 (상하위 바이트 분리 필요)
                        * 에러 방 위치 추가
                        * */
                        if(sCurrentDevice.sd == null) sCurrentDevice.sd = new SdPacket();

                        // 에러코드 - 별도 문서 확인
                        if(jsonObject.has("errorCode")) sCurrentDevice.sd.errorCD = jsonObject.optInt("errorCode", 0);

                        // 에러레벨 하위 - 0:Not Error, 1:Level 1 (에러상태표시, 정상운전), 2: Level 2 (에러상태표시, 운전정지), 3: Level 3 (에러상태표시, 운전정지)
                        if(jsonObject.has("errorState")) sCurrentDevice.sd.errorLevel_L = jsonObject.optInt("errorState", 0);
                        // 에러레벨 상위 - 0:Not Alarm, 1: Level 1 (화면표시), 2: Level 2 (화면표시 & 소리 알림), 3: Level 3 (화면표시 & 소리 알림)
                        if(jsonObject.has("errorDisplay")) sCurrentDevice.sd.errorLevel_H = jsonObject.optInt("errorDisplay", 0);

                        // 동작상태 - 정지: 01, 가동 : 02
                        if(jsonObject.has("isRunning")) sCurrentDevice.sd.operationStatus = jsonObject.optInt("isRunning", 1);
                        // 동작모드 - 대기:01, 냉방:02, 제습:03, 일반환기:04, 배기모드:05, 요리모드:06, 청소모드:07, 공기청정:08, 황사청정:09, 난방:0A, 가습:0B, 자동운전:0C, 기관지보호운전:0D, 피부보호운전:0E, 급기:0F
                        if(jsonObject.has("supportedOperationMode")) sCurrentDevice.sd.operationMode = jsonObject.optInt("supportedOperationMode", 1);
                        // ODU 동작상태 - 대기:01, 냉방:02, 제습:03, 일반환기:04, 배기모드:05, 요리모드:06, 청소모드:07, 공기청정:08, 황사청정:09, 난방:0A, 가습:0B, 자동운전:0C, 기관지보호운전:0D, 피부보호운전:0E, 급기:0F
                        if(jsonObject.has("oduOperationMode")) sCurrentDevice.sd.oduOperationStatus = jsonObject.optInt("oduOperationMode", 1);
                        // 동작옵션 - 옵션기능 01:없음, 02:터보운전, 03:절전운전, 04:숙면
                        if(jsonObject.has("optionFunction")) sCurrentDevice.sd.option = jsonObject.optInt("optionFunction", 1);
                        // 예약기능 - 없음:01, 취침예약:02, 운전예약:03, 정지예약:04
                        if(jsonObject.has("reservationFunction")) sCurrentDevice.sd.reservation = jsonObject.optInt("reservationFunction", 1);

                        // 풍량 설정값 - 미풍:01, 약풍:02, 강풍:03, 자동:04
                        if(jsonObject.has("desiredAirVolume")) sCurrentDevice.sd.setWindLevel = jsonObject.optInt("desiredAirVolume", 1);
                        // ODU 풍량 현재값 - 미풍:01, 약풍:02, 강풍:03, 자동:04
                        if(jsonObject.has("airVolume")) sCurrentDevice.sd.currontOduWindLevel = jsonObject.optInt("airVolume", 1);
                        // 풍량 설정 값(중국향) - 설정값 : 5~99%
//                        if(jsonObject.has("desiredAirVolumeChina")) sCurrentDevice.sd.windLevelPercent = jsonObject.optInt("desiredAirVolumeChina", 1);

                        // 설정 온도
                        if(jsonObject.has("desiredTemperature")) sCurrentDevice.sd.setTemp = jsonObject.optInt("desiredTemperature", 0);
                        // 현재 온도
                        if(jsonObject.has("currentTemperature")) sCurrentDevice.sd.curruntTemp = jsonObject.optInt("currentTemperature", 0);

                        // 설정 습도
                        if(jsonObject.has("desiredHumidity")) sCurrentDevice.sd.setHumi = jsonObject.optInt("desiredHumidity", 0);
                        // 현재 습도
                        if(jsonObject.has("currentHumidity")) sCurrentDevice.sd.curruntHumi = jsonObject.optInt("currentHumidity", 0);

                        // 예약운전 설정 시간
                        if(jsonObject.has("reservationOperationTime")) sCurrentDevice.sd.setReservationTimer = jsonObject.optInt("reservationOperationTime", 0);

                        // 루버스윙 동작 설정 - Swing Off:01, Swing On:02
                        if(jsonObject.has("louverSwingValue")) sCurrentDevice.sd.setSwing = jsonObject.optInt("louverSwingValue", 1);

                        // ODU 헤파필터 시간경과 플래그 (필터 관련 동작 상태) - 정상:01, 헤파필터 리셋시간 Over:02, 필터 수명 계산중:03 [에어룸콘], 04:리셋 불가(요리모드)
                        if(jsonObject.has("hepaFilterElapsedFlag")) sCurrentDevice.sd.flagHepaFilter = jsonObject.optInt("hepaFilterElapsedFlag", 1);
                        // ODU 프리필터 시간경과 플래그 (필터 관련 동작 상태) - 정상:01, 헤파필터 리셋시간 Over:02
                        if(jsonObject.has("freeFilterElapsedFlag")) sCurrentDevice.sd.flagFreeFilter = jsonObject.optInt("freeFilterElapsedFlag", 1);
                        // 필터 수명 (남은시간 %)
                        if(jsonObject.has("filterLifespan")) sCurrentDevice.sd.usingTimeOduFilter = jsonObject.optInt("filterLifespan", 0);

                        // 먼지센서 정성 값 - 좋음:01, 보통:02, 나쁨:03, 매우나쁨:04
//                        if(jsonObject.has("dustSensorQualitativeValue")) sCurrentDevice.sd.dustSensor.level = jsonObject.optInt("dustSensorQualitativeValue", 1);
                        // 먼지센서 정량 값
//                        if(jsonObject.has("dustSensorQuantitativeValue")) sCurrentDevice.sd.dustSensor.value = jsonObject.optInt("dustSensorQuantitativeValue", 0);

                        // CO2센서 정성 값 - 좋음:01, 보통:02, 나쁨:03, 매우나쁨:04
//                        if(jsonObject.has("co2SensorQualitativeValue")) sCurrentDevice.sd.co2Sensor.level = jsonObject.optInt("co2SensorQualitativeValue", 1);
                        // C02센서 정량 값
//                        if(jsonObject.has("co2SensorQuantitativeValue")) sCurrentDevice.sd.co2Sensor.value = jsonObject.optInt("co2SensorQuantitativeValue", 0);

                        // VOC 센서 정성 값 - 좋음:01, 보통:02, 나쁨:03, 매우나쁨:04
//                        if(jsonObject.has("gasVocSensorQualitativeValue")) sCurrentDevice.sd.vocSensor.level = jsonObject.optInt("gasVocSensorQualitativeValue", 1);
                        // VOC 센서 정량 값
//                        if(jsonObject.has("gasVocSensorQuantitativeValue")) sCurrentDevice.sd.vocSensor.value = jsonObject.optInt("gasVocSensorQuantitativeValue", 0);

                        // 내부 히터 가동 플래그 - 비가동:01, 가동:02
                        if(jsonObject.has("innerHeaterOperationFlag")) sCurrentDevice.sd.flagInnerHeaterOperation = jsonObject.optInt("innerHeaterOperationFlag", 1);
                        // 회부 접점 입력 - 내부:01, 내부접점입력(후드입력):02
                        if(jsonObject.has("outerContactInputFlag")) sCurrentDevice.sd.flagOuterCloseInput = jsonObject.optInt("outerContactInputFlag", 1);
                        // ODU 열원 가동/정지 요청상태 - 비요청:01, 요청:02
                        if(jsonObject.has("heatSourceOperationRequest")) sCurrentDevice.sd.oduHeatSourceOperationRequest = jsonObject.optInt("heatSourceOperationRequest", 1);
                        // 결로운전 동작 - 비가동:01, 가동:02
                        if(jsonObject.has("condensationOperation")) sCurrentDevice.sd.condensationOperation = jsonObject.optInt("condensationOperation", 1);
                        // 바이패스 동작 - 비가동:01, 가동:02
                        if(jsonObject.has("bypassOperation")) sCurrentDevice.sd.condensationOperation = jsonObject.optInt("bypassOperation", 1);
                        // 홈넷 연동상태 - 미연동:01, 연동:02
                        if(jsonObject.has("connectedHomenet")) sCurrentDevice.sd.connectedHomenet = jsonObject.optInt("connectedHomenet", 1);

                        // 각방제어 - 정지:01, 동작:02, Default:00
                        /*if(jsonObject.has("roomControl1st")) sCurrentDevice.sd.controlRoom1 = jsonObject.optInt("roomControl1st", 0);
                        if(jsonObject.has("roomControl2nd")) sCurrentDevice.sd.controlRoom2 = jsonObject.optInt("roomControl2nd", 0);
                        if(jsonObject.has("roomControl3rd")) sCurrentDevice.sd.controlRoom3 = jsonObject.optInt("roomControl3rd", 0);
                        if(jsonObject.has("roomControl4th")) sCurrentDevice.sd.controlRoom4 = jsonObject.optInt("roomControl4th", 0);
                        if(jsonObject.has("roomControl5th")) sCurrentDevice.sd.controlRoom5 = jsonObject.optInt("roomControl5th", 0);
                        if(jsonObject.has("roomControl6th")) sCurrentDevice.sd.controlRoom6 = jsonObject.optInt("roomControl6th", 0);
                        if(jsonObject.has("roomControl7th")) sCurrentDevice.sd.controlRoom7 = jsonObject.optInt("roomControl7th", 0);
                        if(jsonObject.has("roomControl8th")) sCurrentDevice.sd.controlRoom8 = jsonObject.optInt("roomControl8th", 0);
                        if(jsonObject.has("roomControlDress")) sCurrentDevice.sd.controlDressRoom = jsonObject.optInt("roomControlDress", 0);*/

                        // ODU 프리 필터 사용 시간
                        if(jsonObject.has("freeFilterUsedTime")) sCurrentDevice.sd.usingTimeOduFreeFilter = jsonObject.optInt("freeFilterUsedTime", 0);

                        // 숙면모드 설정/해제 - 해제:01, 설정:02
                        if(jsonObject.has("deepSleepMode")) sCurrentDevice.sd.deepSleepUse = jsonObject.optInt("deepSleepMode", 1);
                        // 숙면모드 시작시간
                        if(jsonObject.has("deepSleepModeStartTime")) sCurrentDevice.sd.deepSleepStart = jsonObject.optInt("deepSleepModeStartTime", 0);
                        // 숙면모드 종료시간
                        if(jsonObject.has("deepSleepModeEndTime")) sCurrentDevice.sd.deepSleepEnd = jsonObject.optInt("deepSleepModeEndTime", 0);

                        // 프리필터 청소 알림 - 정상:01, 청소알림 시간 경과 : 02
//                        if(jsonObject.has("freeFilterCleanAlarmFlag")) sCurrentDevice.sd.oduFreeFilterAlarm = jsonObject.optInt("freeFilterCleanAlarmFlag", 1);
                        // 전자헤파필터 청소 알림 - 정상:01, 청소알림 시간 경과 : 02
//                        if(jsonObject.has("hepaFilterCleanAlarmFlag")) sCurrentDevice.sd.oduHepaFilterAlarm = jsonObject.optInt("hepaFilterCleanAlarmFlag", 1);
                        // 프리+전자헤파필터 청소 알림 - 정상:01, 청소알림 시간 경과 : 02
//                        if(jsonObject.has("freeHepaFilterCleanAlarmFlag")) sCurrentDevice.sd.oduFreeAndHepaFilterAlarm = jsonObject.optInt("freeHepaFilterCleanAlarmFlag", 1);

                        // 전동 디퓨저 번호
                        if(jsonObject.has("roomAndDefuserErrorLocationDefuserNumber")) sCurrentDevice.sd.diffuserNumber = jsonObject.optInt("roomAndDefuserErrorLocationDefuserNumber", 0);
                        // 디퓨저 종류
                        if(jsonObject.has("roomAndDefuserErrorLocationType")) sCurrentDevice.sd.diffuserModel = jsonObject.optInt("roomAndDefuserErrorLocationType", 1);
                        // 방번호
                        if(jsonObject.has("errorRoomLocation")) sCurrentDevice.sd.diffuserRoomNumber = jsonObject.optInt("errorRoomLocation", 0);

                        // 필터 사용시간
                        if(jsonObject.has("oduFilterUseTimeOnlyPrism")) sCurrentDevice.sd.filterUseTime = jsonObject.optInt("oduFilterUseTimeOnlyPrism", 0);

                        // UV LED 상태 (설정/해제)
                        if(jsonObject.has("uvLedMode")) sCurrentDevice.sd.uvLedMode = jsonObject.optInt("uvLedMode", 0);

                        // UV LED 상태 (on/off)
                        if(jsonObject.has("uvLedState")) sCurrentDevice.sd.uvLedState = jsonObject.optInt("uvLedState", 0);
                        break;

                    case COMMAND_LED_STATUS_INFO: // 에어모니터 밝기 상태
                        if(sCurrentDevice.led == null) sCurrentDevice.led = new LedSettingPacket();

                        if(jsonObject.has("brightness")) sCurrentDevice.led.brightness  = jsonObject.optInt("brightness", 0);
                        break;
                }
            } catch (JSONException e) {
                FirebaseCrashlytics.getInstance().log("received Mqtt Data Parsing Exception : " + e.getMessage());
            }
        }
    }
}
