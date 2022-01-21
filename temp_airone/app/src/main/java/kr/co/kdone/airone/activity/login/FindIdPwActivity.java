package kr.co.kdone.airone.activity.login;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import androidx.appcompat.app.AppCompatActivity;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.regex.Pattern;

import kr.co.kdone.airone.R;
import kr.co.kdone.airone.activity.register.RegisterDevice2Activity;
import kr.co.kdone.airone.activity.register.RegisterResultActivity;
import kr.co.kdone.airone.activity.register.prism.RegisterPrismDeviceActivity;
import kr.co.kdone.airone.activity.register.prism.SelectJoinDeviceActivity;
import kr.co.kdone.airone.data.MicomPacket;
import kr.co.kdone.airone.udp.UdpUnicast;
import kr.co.kdone.airone.utils.CommonUtils;
import kr.co.kdone.airone.utils.HttpApi;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

import static kr.co.kdone.airone.utils.CommonUtils.DismissConnectDialog;
import static kr.co.kdone.airone.utils.CommonUtils.IsRunningProgress;
import static kr.co.kdone.airone.utils.CommonUtils.MICOM_COMMAND_DEVICE_ID_REQ;
import static kr.co.kdone.airone.utils.CommonUtils.MICOM_COMMAND_STN_MODE_FORCE_ENTRY_REQ;
import static kr.co.kdone.airone.utils.CommonUtils.MICOM_PORT;
import static kr.co.kdone.airone.utils.CommonUtils.MICOM_RESPONSE_DEVICE_GEN_RSP;
import static kr.co.kdone.airone.utils.CommonUtils.MICOM_RESPONSE_DEVICE_ID_RSP;
import static kr.co.kdone.airone.utils.CommonUtils.MICOM_RESPONSE_STN_MODE_START_RSP;
import static kr.co.kdone.airone.utils.CommonUtils.MICOM_SOURCE_APP;
import static kr.co.kdone.airone.utils.CommonUtils.MICOM_SOURCE_DEVICE;
import static kr.co.kdone.airone.utils.CommonUtils.MODE_DEVICE_ROOM_CON;
import static kr.co.kdone.airone.utils.CommonUtils.MODE_FIND_ID;
import static kr.co.kdone.airone.utils.CommonUtils.MODE_FIND_PASS;
import static kr.co.kdone.airone.utils.CommonUtils.MODE_NONE;
import static kr.co.kdone.airone.utils.CommonUtils.MODE_SELECT_DEVICE;
import static kr.co.kdone.airone.utils.CommonUtils.RESULT_GET_DEVICE_ID;
import static kr.co.kdone.airone.utils.CommonUtils.RESULT_GET_DEVICE_PRISM_ID;
import static kr.co.kdone.airone.utils.CommonUtils.RESULT_GET_ID_COMFIRM;
import static kr.co.kdone.airone.utils.CommonUtils.RESULT_SELECT_DEVICE_TYPE;
import static kr.co.kdone.airone.utils.CommonUtils.checkNetwork;
import static kr.co.kdone.airone.utils.CommonUtils.displayProgress;
import static kr.co.kdone.airone.utils.CommonUtils.getApIpAddr;
import static kr.co.kdone.airone.utils.CommonUtils.hexStringToBArray;
import static kr.co.kdone.airone.utils.CommonUtils.setEditText;
import static kr.co.kdone.airone.utils.CommonUtils.showKeyboard;

/**
 * ikHwang 2019-06-04 오전 8:50 제품 아이디 및 비밀번호 찾기
 */
public class FindIdPwActivity extends AppCompatActivity implements View.OnClickListener {
    private final String TAG = getClass().getSimpleName();

    private Activity act;
    private TextView txtTitle, txtDescription, txtOK;
    private RelativeLayout layoutInputID;
    private EditText txtID, txtDeviceID;
    private ImageView btnDeleteID;

    private int mode;
    private UdpUnicast mUdpDevice;
    final Handler handlerDevice = new Handler();

    private boolean selectDevice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_id_pw);

        CommonUtils.setupUI(this, findViewById(R.id.layoutMain));

        act = this;
        mode = getIntent().getIntExtra("MODE", MODE_NONE);

        txtTitle = findViewById(R.id.txtTitle);
        txtDescription = findViewById(R.id.txtDescription);
        layoutInputID = findViewById(R.id.layoutInputID);
        txtID = findViewById(R.id.txtID);
        txtDeviceID = findViewById(R.id.txtDeviceID);
        btnDeleteID = findViewById(R.id.btnDeleteID);
        txtOK = findViewById(R.id.txtOK);

        txtID.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                btnDeleteID.setVisibility(s.length() == 0 ? View.GONE : View.VISIBLE);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        txtDeviceID.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence s, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        txtID.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    showKeyboard(txtID);
                    btnDeleteID.setVisibility(txtID.getText().length() == 0 ? View.GONE : View.VISIBLE);
                } else {
                    btnDeleteID.setVisibility(hasFocus ? View.VISIBLE : View.GONE);
                }
            }
        });

//        setEditText(findViewById(R.id.layoutMain));
        txtID.setFilters(new InputFilter[]{CommonUtils.IDCharacterFilter});
        txtDeviceID.setFilters(new InputFilter[]{new InputFilter.LengthFilter(16), CommonUtils.IDCharacterFilter});
        changeUI();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(0, R.anim.slide_right_out);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case RESULT_GET_ID_COMFIRM:
                if (resultCode == RESULT_OK) {
                    String ssid = "";

                    WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                    WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                    ssid = wifiInfo.getSSID();

                    if(TextUtils.isEmpty(ssid)){
                        ConnectivityManager connectivityManager = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
                        NetworkInfo netInfo = connectivityManager.getActiveNetworkInfo();
                        if (netInfo != null && netInfo.isConnected()) {
                            ssid = netInfo.getExtraInfo();
                        }
                    }

                    if(!TextUtils.isEmpty(ssid)) {
                        if (ssid.contains("NTR") || ssid.contains("NTS")) {
                            setUDP();
                            startGetDeviceId();
                        }
                    }
                }
                break;

            case RESULT_GET_DEVICE_ID: // 1,2차 사용자 아이디 조회
                if (resultCode == RESULT_OK) {
                    String ssid = "";

                    WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                    WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                    ssid = wifiInfo.getSSID();

                    if(TextUtils.isEmpty(ssid)){
                        ConnectivityManager connectivityManager = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
                        NetworkInfo netInfo = connectivityManager.getActiveNetworkInfo();
                        if (netInfo != null && netInfo.isConnected()) {
                            ssid = netInfo.getExtraInfo();
                        }
                    }

                    if(!TextUtils.isEmpty(ssid)){
                        if (ssid.contains("NTR") || ssid.contains("NTS")) {
                            setUDP();
                            startGetDeviceId();
                        }
                    }
                }
                break;

            case RESULT_GET_DEVICE_PRISM_ID: // 프리즘 사용자 아이디 조회
                if(null != data){
                    Bundle bundle = data.getExtras();

                    if(null != bundle && bundle.containsKey("deviceID")){
                        String devID = bundle.getString("deviceID");

                        if(!TextUtils.isEmpty(devID)){
                            txtDeviceID.setText(devID);
                        }
                    }
                }
                break;

            case RESULT_SELECT_DEVICE_TYPE:
                if (resultCode == RESULT_OK) {
                    if(data.hasExtra("selectDevice")){
                        selectDevice = data.getBooleanExtra("selectDevice", false);

                        Intent intent;

                        if(selectDevice){ // 프림즘 사용자
                            intent = new Intent(this, RegisterPrismDeviceActivity.class);intent.putExtra("mode", MODE_FIND_PASS);
                            intent.putExtra("sub_mode", MODE_DEVICE_ROOM_CON);
                            startActivityForResult(intent, RESULT_GET_DEVICE_PRISM_ID);
                            act.overridePendingTransition(R.anim.slide_left_in, R.anim.slide_none);
                        }else{ // 1,2차 사용자
                            intent = new Intent(this, RegisterDevice2Activity.class);
                            intent.putExtra("mode", MODE_FIND_PASS);
                            intent.putExtra("sub_mode", MODE_DEVICE_ROOM_CON);
                            startActivityForResult(intent, RESULT_GET_DEVICE_ID);
                            act.overridePendingTransition(R.anim.slide_left_in, R.anim.slide_none);
                        }
                    }
                }
                break;
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.btnBack:
                finish();
                break;

            case R.id.layoutOK:
                if (checkValueBeforeSend()) {
                    if (mode == MODE_FIND_ID) {
                        if (checkNetwork(act)) {
                            displayProgress(this, "", "");
                            try {
                                HttpApi.PostV2FindId(  //V2A 적용.
                                        txtDeviceID.getText().toString().toUpperCase(),
                                        new Callback() {
                                            @Override
                                            public void onFailure(Call call, IOException e) {
                                                DismissConnectDialog();
                                                CommonUtils.showToast(FindIdPwActivity.this, getString(R.string.toast_result_can_not_search));
                                            }

                                            @Override
                                            public void onResponse(Call call, Response response) throws IOException {
                                                try {
                                                    JSONObject json_data = new JSONObject(response.body().string());

                                                    CommonUtils.customLog(TAG, "user/find-id : " + json_data.toString(), Log.ERROR);

                                                    switch (json_data.optInt("code", 0)){
                                                        case HttpApi.RESPONSE_SUCCESS: // 아이디 찾기 성공
                                                            if(json_data.has("data")){
                                                                JSONObject objData = json_data.getJSONObject("data");

                                                                String userId = null;
                                                                if(objData.has("userId")){
                                                                    userId = objData.optString("userId", "");
                                                                }

                                                                if (!TextUtils.isEmpty(userId)) {
                                                                    final String finalUserId = userId;
                                                                    runOnUiThread(new Runnable() {
                                                                        @Override
                                                                        public void run() {
                                                                            Intent intent = new Intent(FindIdPwActivity.this.act, RegisterResultActivity.class);
                                                                            intent.putExtra("mode", FindIdPwActivity.this.mode);
                                                                            intent.putExtra("userID", finalUserId);
                                                                            CommonUtils.startActivityAni(FindIdPwActivity.this, intent, true, 0);
                                                                        }
                                                                    });
                                                                    break;
                                                                }else{
                                                                    CommonUtils.showToast(FindIdPwActivity.this, getString(R.string.toast_result_check_user));
                                                                }
                                                            }
                                                            break;

                                                        case HttpApi.RESPONSE_DATA_NO_EXIST: // 룸콘아이디에 연결된 계정 정보 없음
                                                        case HttpApi.RESPONSE_DEVICE_NOT_EXIST: // 디바이스 정보 찾을 수 없음
                                                            CommonUtils.showToast(FindIdPwActivity.this, getString(R.string.toast_result_check_user));
                                                            break;

                                                        default:
                                                            CommonUtils.showToast(FindIdPwActivity.this, getString(R.string.toast_result_wrong_sql));
                                                            break;
                                                    }
                                                } catch (JSONException e) {
                                                    e.printStackTrace();
                                                    CommonUtils.showToast(FindIdPwActivity.this, getString(R.string.toast_result_can_not_search));
                                                }
                                                DismissConnectDialog();
                                            }
                                        });
                            } catch (Exception e) {
                                e.printStackTrace();
                                DismissConnectDialog();
                                CommonUtils.showToast(FindIdPwActivity.this, getString(R.string.toast_result_can_not_search));
                            }
                        } else {
                            CommonUtils.showToast(FindIdPwActivity.this, getString(R.string.toast_result_check_network));
                        }
                    } else if (mode == MODE_FIND_PASS) {
                        Intent intent = new Intent(act, FindPwChangeActivity.class);
                        intent.putExtra("userID", txtID.getText().toString());
                        intent.putExtra("deviceID", txtDeviceID.getText().toString());

                        CommonUtils.startActivityAni(act, intent, false, 0);
                    }

                }
                break;

            case R.id.btnDeleteID:
                txtID.setText("");
                break;

            case R.id.btnDeviceID:
                Intent intent = new Intent(this, SelectJoinDeviceActivity.class);
                intent.putExtra("mode", MODE_SELECT_DEVICE);
                startActivityForResult(intent, RESULT_SELECT_DEVICE_TYPE);
                act.overridePendingTransition(R.anim.slide_left_in, R.anim.slide_none);
                break;
        }
    }

    private void changeUI() {
        switch (mode) {
            case MODE_FIND_ID:
                txtTitle.setText(R.string.find_id);
                txtDescription.setText(R.string.input_find_id_content);
                layoutInputID.setVisibility(View.GONE);
                txtOK.setText(R.string.ok);
                break;

            case MODE_FIND_PASS:
                txtTitle.setText(R.string.modify_password);
                txtDescription.setText(R.string.input_find_pw_content);
                layoutInputID.setVisibility(View.VISIBLE);
                txtOK.setText(R.string.next);
                break;
        }
    }

    private boolean checkValueBeforeSend() {
        if (mode == MODE_FIND_ID) {
            if (txtDeviceID.getText().toString().length() == 0) {
                CommonUtils.showToast(FindIdPwActivity.this, getString(R.string.toast_result_input_device_id));
            } else if (txtDeviceID.getText().toString().length() < 16) {
                CommonUtils.showToast(FindIdPwActivity.this, getString(R.string.toast_result_check_device_id));
            } else {
                return true;
            }
        } else if (mode == MODE_FIND_PASS) {
            if (txtID.getText().toString().length() == 0) {
                CommonUtils.showToast(FindIdPwActivity.this, getString(R.string.toast_result_input_id));
            } else if (txtDeviceID.getText().toString().length() == 0) {
                CommonUtils.showToast(FindIdPwActivity.this, getString(R.string.toast_result_input_device_id));
            } else if (txtDeviceID.getText().toString().length() < 16) {
                CommonUtils.showToast(FindIdPwActivity.this, getString(R.string.toast_result_check_device_id));
            } else {
                return true;
            }
        }
        return false;
    }

    /**
     * UDP 통신을 위한 설정 함수
     */
    private void setUDP() {
        if (mUdpDevice != null)
            mUdpDevice.close();

        mUdpDevice = new UdpUnicast();
        mUdpDevice.setIp(getApIpAddr(getApplicationContext()));
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
                        CommonUtils.customLog(TAG, "UDP Received(48888) : " + d, Log.ERROR);

                        if ((length == 17) &&
                                (((data[12] & 0xff) == MICOM_RESPONSE_DEVICE_ID_RSP) ||
                                        ((data[12] & 0xff) == MICOM_RESPONSE_STN_MODE_START_RSP) ||
                                        ((data[12] & 0xff) == MICOM_RESPONSE_DEVICE_GEN_RSP))) {
                            int stx = data[0] & 0xff;
                            int src = data[1] & 0xff;
                            int dst = data[2] & 0xff;
                            int ver = data[3] & 0xff;

                            String deviceID = "";
                            for (int i = 0; i < 8; i++) {
                                deviceID += String.format("%02x", data[4 + i]);
                            }
                            CommonUtils.customLog(TAG, "DeviceID : " + deviceID, Log.ERROR);

                            final String fixedDeviceID = deviceID.toUpperCase();

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    txtDeviceID.setText(fixedDeviceID);
                                    if (IsRunningProgress())
                                        DismissConnectDialog();
                                    endApMode(fixedDeviceID);
                                }
                            });

                            int cmd = data[12] & 0xff;
                            int len = (data[13] & 0xff) << 8 | (data[14] & 0xff);
                            int crc = (data[15] & 0xff) << 8 | (data[16] & 0xff);
                        }
                    }
                });
            }

            @Override
            public void onError(Exception e) {

            }
        });
        mUdpDevice.open();
    }

    private void startGetDeviceId() {
        displayProgress(this, "", "");

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (IsRunningProgress())
                    DismissConnectDialog();
                if (txtDeviceID.length() == 0) {
                    CommonUtils.showToast(FindIdPwActivity.this, getString(R.string.toast_result_check_user));
                }
            }
        }, 5000);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                getProductIdentifier();
            }
        }, 500);
    }

    private void getProductIdentifier() {
        MicomPacket packet = new MicomPacket(MICOM_SOURCE_APP, MICOM_SOURCE_DEVICE, MICOM_COMMAND_DEVICE_ID_REQ);
        mUdpDevice.send(packet.toBytes());
    }

    private void endApMode(String deviceId) {
        int[] productID = hexStringToBArray(deviceId);
        MicomPacket packet = new MicomPacket(MICOM_SOURCE_APP, MICOM_SOURCE_DEVICE, productID, MICOM_COMMAND_STN_MODE_FORCE_ENTRY_REQ, 0, null);
        mUdpDevice.send(packet.toBytes());
    }
}
