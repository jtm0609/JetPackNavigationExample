package kr.co.kdone.airone.utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import androidx.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class KDBlueToothManager {
    private final String TAG = KDBlueToothManager.class.getSimpleName();

    public static final String PREFIX = "NRT";
//    public static final String PREFIX = "NTR";

    public static final String BLE_SEARCH_TIME_OUT = "airone.permission.BLE_SEARCH_TIME_OUT";

    public static final int REQUEST_ENABLE_BT = 10000; // 블루투스 연결 사용자 동의

    public static final int BLUETOOTH_STATE_NO_MODULE = 1; // 블루투스 모듈 없음
    public static final int BLUETOOTH_STATE_ON = 2; // 블루투스 모듈 켜져있음
    public static final int BLUETOOTH_STATE_OFF = 3; // 블루투스 모듈 꺼져있음
    public static final int BLUETOOTH_STATE_ACTIVATION_SUCCESS = 4; // 블루투스 활성화 성공
    public static final int BLUETOOTH_STATE_ACTIVATION_FAIL = 5; // 블루투스 활성화 실패
    public static final int BLUETOOTH_STATE_SEARCHING_START = 6; // 블루투스 검색 시작
    public static final int BLUETOOTH_STATE_SEARCHING_END = 7; // 블루투스 검색 완료
    public static final int BLUETOOTH_STATE_CONNECT = 8; // 블루투스 연결 성공
    public static final int BLUETOOTH_STATE_DISCONNECT = 9; // 블루투스 연결 해제

    public static final int BLUETOOTH_STATE_SOCKET_CONNECT_ERROR = 100; // 블루투스 연결 오류
    public static final int BLUETOOTH_STATE_SEND_MSG_FAIL = 101; // 블루투스 메시지 전송 실패 (김수성 대리님의 요청으로 100번)

    private final int THREAD_SLEEP_TIME = 1 * 1000; // 스레드 딜레이

    private String strPrefix = ""; // 블루투스 리스트 필터링하기 위한 문구

    private Activity mActivity;
    private BluetoothListener mListener;

    private BluetoothAdapter bluetoothAdapter;
    private BluetoothSocket bluetoothSocket; // 블루투스 연결 소켓
    private Thread dataReceiveThread = null; // 데이터 수신에 사용되는 쓰레드

    // ikHwang 2020-04-03 오전 8:23 BT 검색 브로드캐스트에서 스캐너로 변경
    private BluetoothLeScanner mLEScanner;
    private ScanSettings settings;
    private List<ScanFilter> filters;
    private HashMap<String, String> deviceList;
    private Handler mHandler;
    private static int MAX_SCAN_TIME = 5 * 1000;            // BLE 디바이스 최대 검색 시간

    private OutputStream outputStream = null; // 블루투스에 데이터를 출력하기 위한 출력 스트림
    private InputStream inputStream = null; // 블루투스에 데이터를 입력하기 위한 입력 스트림

    public interface BluetoothListener {
        void onNoModule();
        void onBluetoothState(int state);
        void onFindDevices(BluetoothDevice device);
        void onError(int error);
        void onReceiveData(byte[] data);
    }

    public KDBlueToothManager(Activity act, String prefix, BluetoothListener listener){
        this.mActivity = act;
        this.mListener = listener;
        this.strPrefix = prefix;

        filters = new ArrayList<>();
        deviceList = new HashMap<>();
        mHandler = new Handler();

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
//        registerbluetoothReceiver();

        isAvailableBlutooth();
    }

    /**
     * ikHwang 2019-05-28 오전 8:42 블루투스 사용가능 디바이스 체크
     */
    private void isAvailableBlutooth(){
        if(mActivity.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH) && null != bluetoothAdapter){
            CommonUtils.customLog(TAG, "BlueTooth has Module", Log.ERROR);
            isBluetoothActive();
        }else{
            CommonUtils.customLog(TAG, "BlueTooth No Module", Log.ERROR);
            // ikHwang 2019-05-28 오후 12:22 블루투스 없어서 사용 불가능 하다는 메시지 팝업 표시 필요
            mListener.onNoModule();
        }
    }

    /**
     * ikHwang 2019-05-28 오전 10:51 블루투스 활성화 상태 조회
     * @return
     */
    public boolean isBluetoothActive(){
        if(bluetoothAdapter.isEnabled()){
            CommonUtils.customLog(TAG, "BLUETOOTH_STATE_ON", Log.ERROR);

            // ikHwang 2019-05-28 오전 8:47 블루투스 활성화 되어 있음
            mListener.onBluetoothState(BLUETOOTH_STATE_ON);
            return true;
        }else{
            CommonUtils.customLog(TAG, "BLUETOOTH_STATE_OFF", Log.ERROR);

            // ikHwang 2019-05-28 오전 8:47 블루투스 활성화 작업 진행
            // 콜백에서 startBluetoothEnable()를 호출하여 ON 상태로 변경 필요
            // 또는 setBluetoothEnable를 이용하여 직접 상태 변경
            mListener.onBluetoothState(BLUETOOTH_STATE_OFF);
            return false;
        }
    }

    /**
     * ikHwang 2019-05-28 오후 12:24 사용자의 동의 얻어 블루투스 상태를 ON 시킨다
     */
    public void startBluetoothEnable(){
        Intent i = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        mActivity.startActivityForResult(i, REQUEST_ENABLE_BT);
    }

    /**
     * ikHwang 2019-05-28 오전 10:53 블루투스 ON/OFF 상태 처리
     * @param state
     * enable 처리가 불가능할경우 호출하여 처리
     * startBluetoothEnable 호출하여 시스템 팝업 추가하여 처리 가능함
     */
    public void setBluetoothEnable(boolean state){
        if(state){
            bluetoothAdapter.enable();
        }else{
            bluetoothAdapter.disable();
        }
    }

    /**
     * ikHwang 2019-05-28 오전 8:57 블루투스 ON 사용권한 요청
     * @param requestCode
     * @param resultCode
     * @param data
     */
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data){
        switch (requestCode){
            case REQUEST_ENABLE_BT:
                if(resultCode == Activity.RESULT_OK){
                    // ikHwang 2019-05-28 오후 1:41 블루투스 활성화 성공
                    // 콜백에서 startDisvocery()를 호출하여 기기를 검색하도록 추가 필요
                    mListener.onBluetoothState(BLUETOOTH_STATE_ACTIVATION_SUCCESS); // 블루투스 ON 활성화 성공
                }else{
                    // 블루투스 활서화 실패에 대한 예외처리 추가 필요
                    mListener.onBluetoothState(BLUETOOTH_STATE_ACTIVATION_FAIL); // 블루투스 ON 활성화 실패
                }
                break;

            default:
                break;
        }
    }

    /**
     * ikHwang 2019-05-28 오전 11:19 블루투스 기기 검색 시작
     */
    public void startDisvocery(){
        bluetoothAdapter.startDiscovery();
    }

    /**
     * ikHwang 2019-05-28 오전 9:31 블루투스 상태 변경등 이벤트를 수신하기 위한 리시버 처리
     */
    public void registerbluetoothReceiver(){
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED); // BluetoothAdapter.ACTION_STATE_CHANGED : 블루투스 상태변화 액션
        intentFilter.addAction(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED);
        intentFilter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED); // 연결 확인
        intentFilter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED); // 연결 끊김 확인
        intentFilter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        intentFilter.addAction(BluetoothDevice.ACTION_FOUND);    // 기기 검색됨
        intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);   // 기기 검색 시작
        intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);  // 기기 검색 종료
        intentFilter.addAction(BluetoothDevice.ACTION_PAIRING_REQUEST);
        intentFilter.addAction(BLE_SEARCH_TIME_OUT);
        mActivity.registerReceiver(mReceiver, intentFilter);
    }

    /**
     * ikHwang 2019-06-05 오후 5:07 브로드캐스트 리시버 해제
     */
    public void unRegisterbluetoothReceiver(){
        try {
            if(null != mReceiver) mActivity.unregisterReceiver(mReceiver);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * ikHwang 2019-05-28 오전 9:59 새로운 블루투스 장비를 조회하여 처리하는 리시버
     */
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if(TextUtils.isEmpty(action)) return;

//            CommonUtils.customLog(TAG, "Bluetooth BroadcastReceiver action : " + action, Log.ERROR);

            switch (action){
                case BluetoothAdapter.ACTION_STATE_CHANGED: //블루투스의 연결 상태 변경
                    final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);

                    CommonUtils.customLog(TAG, "Bluetooth BroadcastReceiver Ble State : " + state, Log.ERROR);

                    switch(state) {
                        case BluetoothAdapter.STATE_OFF:

                            break;

                        case BluetoothAdapter.STATE_TURNING_OFF:

                            break;

                        case BluetoothAdapter.STATE_ON:

                            break;

                        case BluetoothAdapter.STATE_TURNING_ON:

                            break;
                    }
                    break;

                case BluetoothDevice.ACTION_ACL_CONNECTED:  //블루투스 기기 연결
                    mListener.onBluetoothState(BLUETOOTH_STATE_CONNECT);
                    break;

                case BluetoothDevice.ACTION_BOND_STATE_CHANGED:
                    break;

                case BluetoothDevice.ACTION_ACL_DISCONNECTED:   //블루투스 기기 끊어짐
                    mListener.onBluetoothState(BLUETOOTH_STATE_DISCONNECT);
                    break;

                case BluetoothAdapter.ACTION_DISCOVERY_STARTED:
                    CommonUtils.customLog(TAG, "ACTION_DISCOVERY_STARTED", Log.ERROR);

                    // ikHwang 2019-05-28 오후 2:11 블루투스 기기 검색 시작
                    mListener.onBluetoothState(BLUETOOTH_STATE_SEARCHING_START);
                    break;

                case BluetoothDevice.ACTION_FOUND:  //블루투스 기기가 검색될 때마다 수행됨
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                    if(null != device){
                        if(TextUtils.isEmpty(strPrefix)){
                            CommonUtils.customLog(TAG, "ACTION_FOUND noFilter : " + device.getName()
                                    + ", "
                                    + device.getAddress()
                                    + ", "
                                    + device.getType()
                                    + ", "
                                    + device.getBondState()
                                    + ", "
                                    + Arrays.toString(device.getUuids()), Log.ERROR);

                            // ikHwang 2019-05-28 오후 2:12 검색된 디바이스 전달하여 리스트 갱신하도록 처리
                            mListener.onFindDevices(device);
                        }else{
                            // ikHwang 2019-05-28 오후 2:13 검색된 디바이스를 필터링 하여 리스트 갱신하도록 처리
                            if(!TextUtils.isEmpty(device.getName()) && device.getName().contains(strPrefix)){
                                CommonUtils.customLog(TAG, "ACTION_FOUND Filter : " + device.getName()
                                        + ", "
                                        + device.getAddress()
                                        + ", "
                                        + device.getType()
                                        + ", "
                                        + device.getBondState()
                                        + ", "
                                        + Arrays.toString(device.getUuids()), Log.ERROR);

                                mListener.onFindDevices(device);
                            }
                        }
                    }
                    break;

                case BluetoothAdapter.ACTION_DISCOVERY_FINISHED: //블루투스 기기 검색 종료
                case BLE_SEARCH_TIME_OUT: // BLE 검색 타임아웃
                    CommonUtils.customLog(TAG, "ACTION_DISCOVERY_FINISHED", Log.ERROR);

                    mListener.onBluetoothState(BLUETOOTH_STATE_SEARCHING_END);
                    break;
            }
        }
    };

    /**
     * ikHwang 2019-05-28 오전 10:55 다른 기기에서 검색 가능하도록 설정
     * @param durationTime 0, 3600 미만 및 초과시 120초로 자동 설정
     */
    public void startDiscoverable(int durationTime){
        Intent dIntent = new Intent( BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        dIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, durationTime);
        mActivity.startActivity(dIntent);
    }

    /**
     * ikHwang 2019-05-28 오전 10:59 블루투스 연결
     * @param device
     */
    public void connectDevice(BluetoothDevice device){
        if(null == device){
            mListener.onError(BLUETOOTH_STATE_SOCKET_CONNECT_ERROR);
            return;
        }

        boolean already_bonded_flag = false;

        Set<BluetoothDevice> mDeviceList = bluetoothAdapter.getBondedDevices();

        if(null != mDeviceList && mDeviceList.size() > 0){
            for(BluetoothDevice mDevice : mDeviceList){
                if(!TextUtils.isEmpty(mDevice.getName()) && mDevice.getName().equals(device.getName())){
                    already_bonded_flag = true;
                    break;
                }
            }
        }

        if(already_bonded_flag){
            try {
                // ikHwang 2019-05-28 오후 2:29 블루투스 페어링 된적 있음 연결 진행
                bluetoothSocket = device.createRfcommSocketToServiceRecord(getUUID());
                bluetoothSocket.connect();

                outputStream = bluetoothSocket.getOutputStream();
                inputStream = bluetoothSocket.getInputStream();

                receiveBluetoothData();
            } catch (IOException e) {
                e.printStackTrace();
                mListener.onError(BLUETOOTH_STATE_SOCKET_CONNECT_ERROR);
            }
        }else{
            // ikHwang 2019-05-28 오후 2:30 블루투스 페어링 된적 없음 페어링 진행
            try {
                device.createBond();
            } catch (Exception e) {
                e.printStackTrace();
                mListener.onError(BLUETOOTH_STATE_SOCKET_CONNECT_ERROR);
            }
        }
    }

    /**
     * ikHwang 2019-05-28 오전 10:14 UUID 생성
     * @return
     */
    @SuppressLint("HardwareIds")
    public UUID getUUID(){
        String device = Build.DEVICE;
        String bId = Build.ID;
        String androidId = android.provider.Settings.Secure.getString(mActivity.getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);

        UUID uuid = new UUID(androidId.hashCode(), ((long) device.hashCode() << 32) | bId.hashCode());

        return uuid;
    }

    /**
     * ikHwang 2019-05-28 오전 10:37 데이터 전송
     * @param sendData
     */
    public void sendData(byte[] sendData){
        try {
            outputStream.write(sendData);
        } catch (IOException e) {
            e.printStackTrace();
            mListener.onBluetoothState(BLUETOOTH_STATE_SEND_MSG_FAIL); // 메시지 전송 실패
        }
    }

    /**
     * ikHwang 2019-05-28 오전 10:36 블루투스 수신 처리
     */
    private void receiveBluetoothData(){
        dataReceiveThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (Thread.currentThread().isInterrupted()){
                    try {
                        // 데이터 수신 확인
                        int byteAvailable = inputStream.available();

                        if(byteAvailable > 0){
                            // 입력 스트림에서 바이트 단위로 읽어 옵니다.
                            byte[] bytes = new byte[byteAvailable];
                            inputStream.read(bytes);

                            mListener.onReceiveData(bytes);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    try {
                        Thread.sleep(THREAD_SLEEP_TIME);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        dataReceiveThread.start();
    }

    /**
     * ikHwang 2019-09-26 오후 3:28 BLE 검색 및 종료
     * @param isStart
     */
    @SuppressLint("NewApi")
    public void scanBLE(boolean isStart) {
        if (isStart) {
            if (bluetoothAdapter != null && bluetoothAdapter.isEnabled()) {
                //OS버전이 롤리팝 이상이라면 BT LE스캐너와 스캔 세팅을 만들어준다.
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    mLEScanner = bluetoothAdapter.getBluetoothLeScanner();
                    settings = new ScanSettings.Builder()
                            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                            .setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES)
                            .build();
                }

                scanBLEDevice(true);
            } else {
                if(null != mListener) mListener.onBluetoothState(BLUETOOTH_STATE_ON);
            }
        }
    }

    /**
     * // ikHwang 2019-09-26 오후 3:42 BLE 디바이스 검색 및 검색 종료
     * @param isStart
     */
    private void scanBLEDevice(boolean isStart) {
        if (isStart) {
            if(null != mListener) mListener.onBluetoothState(BLUETOOTH_STATE_SEARCHING_START);

            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                        bluetoothAdapter.stopLeScan(mLeScanCallback);
                    } else {
                        mLEScanner.stopScan(mScanCallback);
                    }

                    if(null != mListener) mListener.onBluetoothState(BLUETOOTH_STATE_SEARCHING_END);
                }
            }, MAX_SCAN_TIME);

            deviceList.clear();

            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                bluetoothAdapter.startLeScan(mLeScanCallback);
            } else {
                mLEScanner.startScan(filters, settings, mScanCallback);
            }
        } else {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                bluetoothAdapter.stopLeScan(mLeScanCallback);
            } else {
                mLEScanner.stopScan(mScanCallback);
            }

            if(null != mListener) mListener.onBluetoothState(BLUETOOTH_STATE_SEARCHING_END);
        }
    }

    @SuppressLint("NewApi")
    private ScanCallback mScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            try {
                if(TextUtils.isEmpty(result.getDevice().getName())) return;
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }

            if(deviceList.containsKey(result.getDevice().getAddress())) return;

            CommonUtils.customLog(TAG, "onScanResult BLE Info : " + result.getDevice().getName() + ", mac : " + result.getDevice().getAddress(), Log.ERROR);

            if(TextUtils.isEmpty(strPrefix)){
                if(null != mListener) mListener.onFindDevices(result.getDevice());
                CommonUtils.customLog(TAG, "onScanResult BLE Info : " + result.getDevice().getName() + ", mac : " + result.getDevice().getAddress(), Log.ERROR);
            }else{
                if(result.getDevice().getName().contains(strPrefix)){
                    if(null != mListener) mListener.onFindDevices(result.getDevice());
                    CommonUtils.customLog(TAG, "onScanResult BLE Info : " + result.getDevice().getName() + ", mac : " + result.getDevice().getAddress(), Log.ERROR);
                }
            }

            deviceList.put(result.getDevice().getAddress(), result.getDevice().getAddress());
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            for (ScanResult sr : results) {
                if(null != mListener) mListener.onFindDevices(sr.getDevice());

                CommonUtils.customLog(TAG, "onBatchScanResults BLE Info : " + sr.getDevice().getName() + ", mac : " + sr.getDevice().getAddress(), Log.ERROR);
            }
        }

        @Override
        public void onScanFailed(int errorCode) {
            if(null != mListener) mListener.onBluetoothState(BLUETOOTH_STATE_SEARCHING_END);
        }
    };

    private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
            try {
                if(TextUtils.isEmpty(device.getName())) return;
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }

            if(deviceList.containsKey(device.getAddress())) return;

            CommonUtils.customLog(TAG, "mLeScanCallback BLE Info : " + device.getName() + ", mac : " + device.getAddress(), Log.ERROR);

            if(TextUtils.isEmpty(strPrefix)){
                if(null != mListener) mListener.onFindDevices(device);
                CommonUtils.customLog(TAG, "mLeScanCallback BLE Info : " + device.getName() + ", mac : " + device.getAddress(), Log.ERROR);
            }else{
                if(device.getName().contains(strPrefix)){
                    if(null != mListener) mListener.onFindDevices(device);
                    CommonUtils.customLog(TAG, "mLeScanCallback BLE Info : " + device.getName() + ", mac : " + device.getAddress(), Log.ERROR);
                }
            }

            deviceList.put(device.getAddress(), device.getAddress());
        }
    };
}
