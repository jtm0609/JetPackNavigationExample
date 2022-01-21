package kr.co.kdone.airone.fragments.old;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Timer;
import java.util.TimerTask;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.RecyclerView;
import kr.co.kdone.airone.CleanVentilationApplication;
import kr.co.kdone.airone.R;
import kr.co.kdone.airone.activity.register.APPasswordActivity;
import kr.co.kdone.airone.activity.register.RegisterDevice2Activity;
import kr.co.kdone.airone.data.MicomPacket;
import kr.co.kdone.airone.udp.UdpUnicast;
import kr.co.kdone.airone.udp.WifiAP;
import kr.co.kdone.airone.utils.CommonUtils;
import kr.co.kdone.airone.utils.HttpApi;
import kr.co.kdone.airone.utils.WifiUtils;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

import static android.app.Activity.RESULT_OK;
import static kr.co.kdone.airone.utils.CommonUtils.AT_COMMAND_PORT;
import static kr.co.kdone.airone.utils.CommonUtils.DismissConnectDialog;
import static kr.co.kdone.airone.utils.CommonUtils.IsRunningProgress;
import static kr.co.kdone.airone.utils.CommonUtils.MICOM_COMMAND_DEVICE_GEN_REQ;
import static kr.co.kdone.airone.utils.CommonUtils.MICOM_COMMAND_DEVICE_ID_REQ;
import static kr.co.kdone.airone.utils.CommonUtils.MICOM_COMMAND_STN_MODE_START_REQ;
import static kr.co.kdone.airone.utils.CommonUtils.MICOM_PORT;
import static kr.co.kdone.airone.utils.CommonUtils.MICOM_RESPONSE_DEVICE_GEN_RSP;
import static kr.co.kdone.airone.utils.CommonUtils.MICOM_RESPONSE_DEVICE_ID_RSP;
import static kr.co.kdone.airone.utils.CommonUtils.MICOM_RESPONSE_STN_MODE_START_RSP;
import static kr.co.kdone.airone.utils.CommonUtils.MICOM_SOURCE_APP;
import static kr.co.kdone.airone.utils.CommonUtils.MICOM_SOURCE_DEVICE;
import static kr.co.kdone.airone.utils.CommonUtils.MODE_CHANGE_AP;
import static kr.co.kdone.airone.utils.CommonUtils.MODE_DEVICE_MONITOR;
import static kr.co.kdone.airone.utils.CommonUtils.MODE_DEVICE_ONLY_REG;
import static kr.co.kdone.airone.utils.CommonUtils.MODE_DEVICE_REG;
import static kr.co.kdone.airone.utils.CommonUtils.RESULT_AP_PASSWORD;
import static kr.co.kdone.airone.utils.CommonUtils.SECURITY_NONE;
import static kr.co.kdone.airone.utils.CommonUtils.UDP_CMD_AT_OK;
import static kr.co.kdone.airone.utils.CommonUtils.UDP_CMD_AT_SCAN;
import static kr.co.kdone.airone.utils.CommonUtils.UDP_CMD_DEVICE_GEN_ID;
import static kr.co.kdone.airone.utils.CommonUtils.UDP_CMD_DEVICE_GET_ID;
import static kr.co.kdone.airone.utils.CommonUtils.UDP_CMD_NONE;
import static kr.co.kdone.airone.utils.CommonUtils.UDP_CMD_PASS;
import static kr.co.kdone.airone.utils.CommonUtils.UDP_CMD_STN_MODE;
import static kr.co.kdone.airone.utils.CommonUtils.checkNetwork;
import static kr.co.kdone.airone.utils.CommonUtils.displayProgress;
import static kr.co.kdone.airone.utils.CommonUtils.getApIpAddr;
import static kr.co.kdone.airone.utils.CommonUtils.hexStringToBArray;

public class RegisterDevice3Fragment extends Fragment implements View.OnClickListener {

    private final String TAG = "RegisterPrismDevice3Fragment";
    private RegisterDevice2Activity act;
    private RecyclerView mWifiAPList;

    private final String CMD_PASS = "HF-A11ASSISTHREAD";
    private final String CMD_OK = "+ok";
    private final String CMD_AT_W = "AT+W\r\n";
    private final String CMD_AT_WSCAN = "AT+WSCAN\r\n";

    private final String WPAPSKWPA2PSK = "WPAPSKWPA2PSK";
    private final String OPEN = "OPEN";
    private final String WPAPSK = "WPAPSK";
    private final String WPA2PSK = "WPA2PSK";
    private final String TKIPAES = "TKIPAES";
    private final String AES = "AES";

    int mLastCommand = 0;

    private int mMode;
    private int mDeviceType;

    private UdpUnicast mUdpATCmd;
    private UdpUnicast mUdpDevice;

    final Handler handlerDevice = new Handler();
    final Handler handlerATCommand = new Handler();

    private WifiAPAdapter mWifiAPAdapter;
    private ArrayList<WifiAP> mWifiAPs;
    private int mSelectedItem;
    private WifiAP mSelectedAP;
    private int mSelectedPosition;

    private String mDeviceID = "";
    private String mSSID = "";

    private int MAX_COUNT = 30;
    private boolean mIsMoreShow = false;

    private boolean isGetIdCommandSuccess = false;
    private boolean isGenerateIdCommandSuccess = false;
    private boolean isOkCommandSuccess = false;
    private int getIdTryCount = 0;
    private int generateIdTryCount = 0;
    private int okTryCount = 0;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        act = (RegisterDevice2Activity) getActivity();
        View view = inflater.inflate(R.layout.fragment_register_device3, container, false);

        mMode = act.mode;
        mSelectedAP = null;
        mSelectedPosition = -1;

        mWifiAPs = new ArrayList<>();
        mWifiAPList = view.findViewById(R.id.listWifi);
        mWifiAPAdapter = new WifiAPAdapter(mWifiAPs);
        mWifiAPList.setAdapter(mWifiAPAdapter);
        mWifiAPList.addItemDecoration(new DividerItemDecoration(act, DividerItemDecoration.VERTICAL));

        view.findViewById(R.id.layoutRefresh).setOnClickListener(this);

        if (IsRunningProgress())
            DismissConnectDialog();

        return view;
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.layoutRefresh: {
                isOkCommandSuccess = false;
                okTryCount = 0;
                startScan(true);
                break;
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case RESULT_AP_PASSWORD:
                    String password = data.getStringExtra("password");
                    selectDeviceAP(password);
                    break;
            }
        }
    }

    /**
     *  제품 아이디 조회 및 생성, AP 목록조회 UDP 소켓 초기화
     */
    public void initList() {
        isGetIdCommandSuccess = false;
        isGenerateIdCommandSuccess = false;
        isOkCommandSuccess = false;
        getIdTryCount = 0;
        generateIdTryCount = 0;
        okTryCount = 0;

        startScan(false);
    }

    private void selectDeviceAP(String password) {
        displayProgress(act, getString(R.string.wifi_connecting), "");
        startSTNmode(mSelectedAP, mDeviceID, password);
    }

    /**
     * UDP 통신을 위한 설정 함수.
     */
    private void setUDP() {
        if (mUdpDevice != null) mUdpDevice.close();

        String currentIP = getApIpAddr(act);

        mUdpDevice = new UdpUnicast();
        mUdpDevice.setIp(currentIP);
        mUdpDevice.setPort(MICOM_PORT);
        mUdpDevice.setListener(new UdpUnicast.UdpUnicastListener() {
            @Override
            public void onReceived(final byte[] data, final int length) {
                handlerDevice.post(new Runnable() {
                    @Override
                    public void run() {
                        String d = "";
                        for (int i = 0; i < length; i++) {
                            d += String.format("%02x ", data[i]);
                        }
                        CommonUtils.customLog(TAG, "lastCommand : " + mLastCommand + ", Received(48888) : " + d, Log.ERROR);

                        if ((length == 17) &&
                                (((data[12] & 0xff) == MICOM_RESPONSE_DEVICE_ID_RSP) ||
                                        ((data[12] & 0xff) == MICOM_RESPONSE_STN_MODE_START_RSP) ||
                                        ((data[12] & 0xff) == MICOM_RESPONSE_DEVICE_GEN_RSP))) {

                            String deviceID = "";
                            for (int i = 0; i < 8; i++) {
                                deviceID += String.format("%02x", data[4 + i]);
                            }

                            if(!TextUtils.isEmpty(deviceID)) mDeviceID = deviceID;
                            CommonUtils.customLog(TAG, "UDP Received(48888) deviceId : " + mDeviceID, Log.ERROR);
                        }

                        if (mLastCommand == UDP_CMD_DEVICE_GET_ID) {
                            //
                            isGetIdCommandSuccess = true;
                            getIdHandler.removeCallbacks(getIdRunnable);

                            mUdpATCmd.send(CMD_PASS);
                            mLastCommand = UDP_CMD_PASS;
                        } else if (mLastCommand == UDP_CMD_DEVICE_GEN_ID) {
                            isGenerateIdCommandSuccess = true;
                            generateIdHandler.removeCallbacks(generateIdRunnable);

                            mUdpATCmd.send(CMD_PASS);
                            mLastCommand = UDP_CMD_PASS;
                        } else if (mLastCommand == UDP_CMD_STN_MODE) {
                            if (mMode == MODE_CHANGE_AP) {
                                final Timer timer = new Timer();
                                TimerTask task = new TimerTask() {
                                    @Override
                                    public void run() {
                                        if (checkNetwork(act)) {
                                            act.runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    try {
                                                        HttpApi.PostV2ChangeSSID(CleanVentilationApplication.getInstance().getUserInfo().getId(), mDeviceID, mSelectedAP.ssid,
                                                                new Callback() {
                                                                    @Override
                                                                    public void onFailure(Call call, IOException e) {
                                                                        if (CommonUtils.IsRunningProgress()) {
                                                                            CommonUtils.DismissConnectDialog();
                                                                        }
                                                                    }

                                                                    @Override
                                                                    public void onResponse(Call call, Response response) throws IOException {
                                                                        try {
                                                                            JSONObject json_data = new JSONObject(response.body().string());

                                                                            CommonUtils.customLog(TAG, "device/changeInfo : " + json_data.toString(), Log.ERROR);

                                                                            switch (json_data.getInt("code")) {
                                                                                case HttpApi.RESPONSE_SUCCESS:
                                                                                    if (CommonUtils.IsRunningProgress()) {
                                                                                        CommonUtils.DismissConnectDialog();
                                                                                    }

                                                                                    try {
                                                                                        mUdpATCmd.close();
                                                                                        mUdpDevice.close();
                                                                                    } catch (Exception e) {
                                                                                        e.printStackTrace();
                                                                                    }

                                                                                    act.setResult(RESULT_OK);
                                                                                    act.finish();
                                                                                    break;

                                                                                case HttpApi.RESPONSE_DEVICE_NOT_EXIST:
                                                                                    CommonUtils.showToast(act, getString(R.string.toast_result_no_exsist_device));
                                                                                    if (CommonUtils.IsRunningProgress()) {
                                                                                        CommonUtils.DismissConnectDialog();
                                                                                    }
                                                                                    break;

                                                                                default:
                                                                                    CommonUtils.showToast(act, getString(R.string.toast_result_can_not_register));
                                                                                    break;
                                                                            }
                                                                        } catch (JSONException e) {
                                                                            e.printStackTrace();
                                                                            CommonUtils.showToast(act, getString(R.string.toast_result_can_not_register));
                                                                        } finally {
                                                                            if (CommonUtils.IsRunningProgress()) {
                                                                                CommonUtils.DismissConnectDialog();
                                                                            }
                                                                        }
                                                                    }
                                                                });
                                                    } catch (Exception e) {
                                                        e.printStackTrace();
                                                        CommonUtils.showToast(act, getString(R.string.toast_result_can_not_register));

                                                        if (CommonUtils.IsRunningProgress()) {
                                                            CommonUtils.DismissConnectDialog();
                                                        }
                                                    }
                                                }
                                            });
                                            timer.cancel();
                                        }
                                    }
                                };
                                timer.schedule(task, 3000, 3000);
                            } else {
                                mLastCommand = UDP_CMD_NONE;
                                act.setDeviceID(mDeviceID);
                                act.setSsid(mSelectedAP.ssid);
                                final Timer timer = new Timer();
                                TimerTask task = new TimerTask() {
                                    @Override
                                    public void run() {
                                        if (checkNetwork(act)) {
                                            act.runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
//                                                    act.moveRegisterPage(3);
                                                    if (act.subMode == MODE_DEVICE_MONITOR) {
                                                        new Handler().postDelayed(new Runnable() {
                                                            @Override
                                                            public void run() {
                                                                act.registerCount = 0;
                                                                act.sendRegisterMonitorDevice();
                                                            }
                                                        }, 3000);
                                                    } else {
                                                        new Handler().postDelayed(new Runnable() {
                                                            @Override
                                                            public void run() {
                                                                DismissConnectDialog();
                                                                act.moveRegisterPage(3);
                                                            }
                                                        }, 3000);
                                                    }

                                                    if (mUdpDevice != null) {
                                                        mUdpDevice.setListener(null);
                                                        mUdpDevice.close();
                                                    }
                                                    if (mUdpATCmd != null) {
                                                        mUdpATCmd.setListener(null);
                                                        mUdpATCmd.close();
                                                    }
                                                }
                                            });
                                            timer.cancel();
                                        }
                                    }
                                };
                                timer.schedule(task, 3000, 3000);
                            }
                        }
                    }
                });
            }

            @Override
            public void onError(Exception e) {
//                CommonUtils.showToast(act, getString(R.string.toast_result_can_not_register));

                if (CommonUtils.IsRunningProgress()) {
                    CommonUtils.DismissConnectDialog();
                }
            }
        });
        mUdpDevice.open();

        CommonUtils.customLog(TAG, "mUdpDevice Connection open ip : " + currentIP + ", port : " + MICOM_PORT, Log.ERROR);

        if (mUdpATCmd != null) mUdpATCmd.close();

        mUdpATCmd = new UdpUnicast();
        mUdpATCmd.setIp(currentIP);
        mUdpATCmd.setPort(AT_COMMAND_PORT);
        mUdpATCmd.setListener(new UdpUnicast.UdpUnicastListener() {
            @Override
            public void onReceived(final byte[] data, final int length) {
                String d = "";

                if(data != null){
                    d = new String(data, 0, length);
                    d = d.replace("\r", "").replace("\n", "");
                }

                CommonUtils.customLog(TAG, "mUdpATCmd Received(48899) packet : " + d, Log.ERROR);

                if (mLastCommand == UDP_CMD_PASS) {
                    okHandler.postDelayed(okRunnable, 3000);
                } else if (mLastCommand == UDP_CMD_AT_OK) {
                    okHandler.removeCallbacks(okRunnable);

                    mUdpATCmd.send(CMD_AT_WSCAN);
                    mLastCommand = UDP_CMD_AT_SCAN;

                    stopHandler.postDelayed(stopRunnable, 10 * 1000);
                } else if (mLastCommand == UDP_CMD_AT_SCAN) {
                    if(TextUtils.isEmpty(d) || "\n".equals(d)) {
                        act.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                stopHandler.removeCallbacks(stopRunnable);
                                stopScan();
                            }
                        });
                    }else{
                        /*if (d.indexOf("\n") > 0) {
                            d = d.substring(0, d.indexOf("\n"));
                        }
                        if (d.indexOf("\r") > 0) {
                            d = d.substring(0, d.indexOf("\r"));
                        }*/
                        if (mWifiAPs == null) {
                            mWifiAPs = new ArrayList<WifiAP>();
                        }

                        if (d.split(",").length == 5 && !d.split(",")[1].isEmpty()) {
                            WifiAP wifiAP = new WifiAP(d);
                            boolean isExsist = false;
                            for (WifiAP w : mWifiAPs) {
                                if (w.ssid.equals(wifiAP.ssid) && w.bssid.equals(wifiAP.bssid)) {
                                    isExsist = true;
                                }
                            }
                            if (!isExsist) {
                                mWifiAPs.add(wifiAP);
                            }
                        }
                    }
                }

                /*handlerATCommand.post(new Runnable() {
                    @Override
                    public void run() {
                        String d = new String(data, 0, length);
                        if (mLastCommand == UDP_CMD_PASS) {
                            mUdpATCmd.send(CMD_OK);
                            mLastCommand = UDP_CMD_AT_OK;

                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    if (mLastCommand == UDP_CMD_AT_OK) {
                                        mUdpATCmd.send(CMD_AT_WSCAN);
                                        mLastCommand = UDP_CMD_AT_SCAN;

                                        new Handler().postDelayed(new Runnable() {
                                            @Override
                                            public void run() {
                                                stopScan();
                                            }
                                        }, 10000);
                                    }
                                }
                            }, 3000);
                        } else if (mLastCommand == UDP_CMD_AT_SCAN) {

                        }
                    }
                });*/
            }

            @Override
            public void onError(Exception e) {
//                CommonUtils.showToast(act, getString(R.string.toast_result_can_not_register));

                if (CommonUtils.IsRunningProgress()) {
                    CommonUtils.DismissConnectDialog();
                }
            }
        });
        mUdpATCmd.open();

        CommonUtils.customLog(TAG, "mUdpATCmd Connection open ip : " + currentIP + ", port : " + AT_COMMAND_PORT, Log.ERROR);
    }

    @SuppressLint("HandlerLeak")
    final Handler stopHandler = new Handler();

    public Runnable stopRunnable = new Runnable() {
        @Override
        public void run() {
            stopScan();
        }
    };

    @SuppressLint("HandlerLeak")
    final Handler okHandler = new Handler();

    // 제품 아이디 조회 요청 처리
    public Runnable okRunnable = new Runnable() {
        @Override
        public void run() {
            if (!isOkCommandSuccess && okTryCount < 3) {
                okTryCount++;

                CommonUtils.customLog(TAG, "okHandler tryCount : " + okTryCount, Log.ERROR);

                mUdpATCmd.send(CMD_OK);
                mLastCommand = UDP_CMD_AT_OK;

                okHandler.postDelayed(okRunnable, 3000);
            } else {
                if(IsRunningProgress()) DismissConnectDialog();

                CommonUtils.showToast(act, getString(R.string.fragment_register_device3_refresh_error));
                act.moveRegisterPage(1);
            }
        }
    };

    // 제품 아이디 조회 핸들러
    @SuppressLint("HandlerLeak")
    final Handler getIdHandler = new Handler();

    // 제품 아이디 조회 요청 처리
    public Runnable getIdRunnable = new Runnable() {
        @Override
        public void run() {
            if (!isGetIdCommandSuccess && getIdTryCount < 3) {
                getIdTryCount++;

                CommonUtils.customLog(TAG, "getIdHandler tryCount : " + getIdTryCount, Log.ERROR);

                getProductIdentifier();
                getIdHandler.postDelayed(getIdRunnable, 3000);
            } else {
                if(IsRunningProgress()) DismissConnectDialog();

                CommonUtils.showToast(act, getString(R.string.fragment_register_device3_getid_error));
                act.moveRegisterPage(1);
            }
        }
    };

    // 제품 아이디 생성 요청 핸들러
    @SuppressLint("HandlerLeak")
    final Handler generateIdHandler = new Handler();

    // 제품 아이이디 생성 요청 처리
    public Runnable generateIdRunnable = new Runnable() {
        @Override
        public void run() {
            if (!isGenerateIdCommandSuccess && generateIdTryCount < 3) {
                generateIdTryCount++;
                generateProductIdentifier();
                generateIdHandler.postDelayed(generateIdRunnable, 3000);
            } else {
                if(IsRunningProgress()) DismissConnectDialog();

                CommonUtils.showToast(act, getString(R.string.fragment_register_device3_generatorid_error));
                act.moveRegisterPage(1);
            }
        }
    };

    private final static Comparator<WifiAP> sortByRssi = new Comparator<WifiAP>() {
        @Override
        public int compare(WifiAP o1, WifiAP o2) {
            return Integer.compare(o1.rssi, o2.rssi);
        }
    };

    /**
     * 제품 ID 획득 함수
     */
    private void getProductIdentifier() {
        mLastCommand = UDP_CMD_DEVICE_GET_ID;
        MicomPacket packet = new MicomPacket(MICOM_SOURCE_APP, MICOM_SOURCE_DEVICE, MICOM_COMMAND_DEVICE_ID_REQ);
        mUdpDevice.send(packet.toBytes());
    }

    /**
     * 제품 ID 생성 함수.
     */
    private void generateProductIdentifier() {
        mLastCommand = UDP_CMD_DEVICE_GEN_ID;
        MicomPacket packet = new MicomPacket(MICOM_SOURCE_APP, MICOM_SOURCE_DEVICE, MICOM_COMMAND_DEVICE_GEN_REQ);
        mUdpDevice.send(packet.toBytes());
    }


    /**
     * 제품에 공유기 설정 함수.
     *
     * @param ap       공유기 정보
     * @param deviceId 제품 ID
     * @param ps       공유기 페스워드
     */
    private void startSTNmode(WifiAP ap, String deviceId, String ps) {
        mLastCommand = UDP_CMD_STN_MODE;
        int[] productID = hexStringToBArray(deviceId);

        int total_len = 0;
        byte[] ssID;
        byte[] bssID;
        byte[] auth;
        byte[] enc;
        byte[] pass;

        if (ap.ssid != null) {
            ssID = ap.ssid.getBytes();
            total_len += ssID.length + 1;
        } else {
            ssID = null;
            total_len++;
        }
        if (ap.bssid != null) {
            bssID = ap.bssid.replace(":", "").getBytes();
            total_len += bssID.length + 1;
        } else {
            bssID = null;
            total_len++;
        }
        if (ap.auth != null) {
            //동시 지원 auth의 경우 높은 등급으로 변경.
            if (ap.auth.equals(WPAPSKWPA2PSK)) {
                ap.auth = WPA2PSK;
            }
            auth = ap.auth.getBytes();
            total_len += auth.length + 1;
        } else {
            auth = null;
            total_len++;
        }
        if (ap.enc != null) {
            //동시 지원 enc의 경우 높은 등급으로 변경.
            if (ap.enc.equals(TKIPAES)) {
                ap.enc = AES;
            }
            enc = ap.enc.getBytes();
            total_len += enc.length + 1;
        } else {
            enc = null;
            total_len++;
        }
        if (ps != null && ps.length() > 0) {
            pass = ps.getBytes();
            total_len += pass.length + 1;
        } else {
            pass = null;
            total_len++;
        }

        int[] data = new int[total_len];
        int index = 0;
        if (ssID != null) {
            data[index++] = ssID.length;
            for (int i = 0; i < ssID.length; i++) {
                data[index++] = ssID[i];
            }
        } else {
            data[index++] = 0;
        }
        if (bssID != null) {
            data[index++] = bssID.length;
            for (int i = 0; i < bssID.length; i++) {
                data[index++] = bssID[i];
            }
        } else {
            data[index++] = 0;
        }
        if (auth != null) {
            data[index++] = auth.length;
            for (int i = 0; i < auth.length; i++) {
                data[index++] = auth[i];
            }
        } else {
            data[index++] = 0;
        }
        if (enc != null) {
            data[index++] = enc.length;
            for (int i = 0; i < enc.length; i++) {
                data[index++] = enc[i];
            }
        } else {
            data[index++] = 0;
        }
        if (pass != null) {
            data[index++] = pass.length;
            for (int i = 0; i < pass.length; i++) {
                data[index++] = pass[i];
            }
        } else {
            data[index++] = 0;
        }
        MicomPacket packet = new MicomPacket(MICOM_SOURCE_APP, MICOM_SOURCE_DEVICE, productID, MICOM_COMMAND_STN_MODE_START_REQ, data.length, data);
        mUdpDevice.send(packet.toBytes());
    }

    /**
     * WifiAP ViewHolder
     */
    private class WifiAPViewHolder extends RecyclerView.ViewHolder {
        private View container;
        private TextView nameTextView;
        private ImageView securityImageView;
        private ImageView signalStrengthImageView;
        private boolean isSelected = false;

        public WifiAPViewHolder(View v) {
            super(v);
            container = v.findViewById(R.id.ap_container);
            nameTextView = v.findViewById(R.id.ap_name);
            securityImageView = v.findViewById(R.id.ap_lock);
            signalStrengthImageView = v.findViewById(R.id.ap_signal);
        }
    }

    /**
     * WifiAP Adapter
     */
    private class WifiAPAdapter extends RecyclerView.Adapter<WifiAPViewHolder> {

        private ArrayList<WifiAP> listWifiAP;

        public WifiAPAdapter(ArrayList<WifiAP> list) {
            listWifiAP = list;
        }

        @Override
        public WifiAPViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            final LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            View v = inflater.inflate(R.layout.listview_setting_ap_item, parent, false);

            final WifiAPViewHolder itemVH = new WifiAPViewHolder(v);
            itemVH.container.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final int p = itemVH.getAdapterPosition();

                    final WifiAP ap = listWifiAP.get(p);

                    mSelectedAP = ap;
                    mSelectedPosition = p;

                    if (mSelectedAP != null) {
                        if (!TextUtils.isEmpty(mSelectedAP.auth) && !mSelectedAP.auth.equals(OPEN)) {
                            Intent intent = new Intent(act, APPasswordActivity.class);
                            intent.putExtra("ssid", mSelectedAP.ssid);
                            act.startActivityForResult(intent, RESULT_AP_PASSWORD);
                            act.overridePendingTransition(R.anim.slide_down_in, R.anim.slide_none);
                        } else {
                            selectDeviceAP(null);
                        }
                    }
                }
            });
            return itemVH;
        }

        @Override
        public void onBindViewHolder(WifiAPViewHolder holder, int position) {
            final WifiAP ap = listWifiAP.get(position);
            holder.nameTextView.setText(ap.ssid);
            holder.securityImageView.setVisibility(ap.security == SECURITY_NONE ? View.INVISIBLE : View.VISIBLE);
            holder.signalStrengthImageView.setImageLevel(ap.getLevel());
        }

        @Override
        public int getItemCount() {
//            return (!mIsMoreShow && listWifiAP.size() > MAX_COUNT) ? MAX_COUNT : listWifiAP.size();
            return listWifiAP.size();
        }

        public WifiAPViewHolder getItem(int p) {
            return (WifiAPViewHolder) mWifiAPList.findViewHolderForAdapterPosition(p);
        }
    }

    /**
     * 제품의 공유기 리스트 탐색 함수
     */
    private void startScan(final boolean isRefresh) {
        try {
            mSelectedAP = null;
            mSelectedPosition = -1;

            mWifiAPs.clear();
            mWifiAPAdapter.notifyDataSetChanged();
        } catch (Exception e) {
            e.printStackTrace();
        }

        mIsMoreShow = false;

        displayProgress(act, "", "");

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                String ssid = "";

                // ikHwang 20190305 9.0에서 getExtraInfo를 통해 SSID조회 불가능하여 OS 분기 처리
                WifiManager wifiManager = (WifiManager) act.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                ssid = wifiInfo.getSSID();

                if(TextUtils.isEmpty(ssid)){
                    ConnectivityManager connectivityManager = (ConnectivityManager) act.getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
                    NetworkInfo netInfo = connectivityManager.getActiveNetworkInfo();
                    if (netInfo != null && netInfo.isConnected()) {
                        ssid = netInfo.getExtraInfo();
                    }
                }

                if (ssid != null && ssid.length() > 0) {
                    if (ssid.toUpperCase().contains(WifiUtils.SSID_VALID_ROOMCON) || ssid.toUpperCase().contains(WifiUtils.SSID_VALID_MONITOR)) {
                        // 명령 Command 처리하기 위한 UDP 설정
                        setUDP();

                        if(isRefresh){
                            mUdpATCmd.send(CMD_PASS);
                            mLastCommand = UDP_CMD_PASS;
                        }else{
                            if(MODE_DEVICE_REG == mMode || MODE_DEVICE_ONLY_REG == mMode){
                                // 신규 등록 및 재등록시 시에만 아이디 생성하도록 변경
                                generateIdHandler.post(generateIdRunnable);
                            }else{
                                // AP 변경 및
                                getIdHandler.post(getIdRunnable);
                            }
                        }

                    } else {
                        CommonUtils.showToast(act, getString(R.string.toast_result_error_invalid_wifi));

                    }
                }
            }
        }, 1000);
    }

    private void stopScan() {
        if (mWifiAPs != null && mWifiAPs.size() != 0 && mSelectedAP == null) {
            Collections.sort(mWifiAPs, sortByRssi);
            Collections.reverse(mWifiAPs);

            CommonUtils.customLog(TAG, "stopScan : " + mWifiAPs, Log.ERROR);

            mWifiAPAdapter.notifyDataSetChanged();
        } else if (mWifiAPs == null || mWifiAPs.size() == 0) {
            CommonUtils.showToast(act, getString(R.string.fragment_register_device3_refresh_error));
            act.moveRegisterPage(1);
        }

        if (IsRunningProgress())
            DismissConnectDialog();
    }
}
