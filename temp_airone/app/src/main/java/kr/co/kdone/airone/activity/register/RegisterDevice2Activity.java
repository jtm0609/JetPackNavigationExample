package kr.co.kdone.airone.activity.register;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import net.daum.mf.map.api.MapPoint;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import kr.co.kdone.airone.BuildConfig;
import kr.co.kdone.airone.CleanVentilationApplication;
import kr.co.kdone.airone.activity.PopupActivity;
import kr.co.kdone.airone.R;
import kr.co.kdone.airone.adapter.RegisterDevicePagerAdapter;
import kr.co.kdone.airone.components.NonSwipeableViewPager;
import kr.co.kdone.airone.data.DeviceInfo;
import kr.co.kdone.airone.data.DeviceManager;
import kr.co.kdone.airone.fragments.old.RegisterDevice1Fragment;
import kr.co.kdone.airone.fragments.old.RegisterDevice3Fragment;
import kr.co.kdone.airone.fragments.old.RegisterDevice4Fragment;
import kr.co.kdone.airone.utils.CommonUtils;
import kr.co.kdone.airone.utils.HttpApi;
import kr.co.kdone.airone.utils.SharedPrefUtil;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

import static kr.co.kdone.airone.utils.CommonUtils.DismissConnectDialog;
import static kr.co.kdone.airone.utils.CommonUtils.IsRunningProgress;
import static kr.co.kdone.airone.utils.CommonUtils.MODE_CHANGE_AP;
import static kr.co.kdone.airone.utils.CommonUtils.MODE_DEVICE_MONITOR;
import static kr.co.kdone.airone.utils.CommonUtils.MODE_DEVICE_ONLY_REG;
import static kr.co.kdone.airone.utils.CommonUtils.MODE_DEVICE_REG;
import static kr.co.kdone.airone.utils.CommonUtils.MODE_DEVICE_ROOM_CON;
import static kr.co.kdone.airone.utils.CommonUtils.MODE_FIND_PASS;
import static kr.co.kdone.airone.utils.CommonUtils.MODE_NONE;
import static kr.co.kdone.airone.utils.CommonUtils.MODE_SUCCESS_REG_ID;
import static kr.co.kdone.airone.utils.CommonUtils.RESULT_REGISTER_SENSORBOX_SUCCESSED;
import static kr.co.kdone.airone.utils.CommonUtils.RESULT_UNREGISTER_DEVICE_POPUP;
import static kr.co.kdone.airone.utils.CommonUtils.displayProgress;
import static kr.co.kdone.airone.utils.SharedPrefUtil.DEFAULT_CURRENT_CONTROL_DEVICE_NAME;
import static kr.co.kdone.airone.utils.SharedPrefUtil.DEFAULT_HOMEDATA_JSON;
import static kr.co.kdone.airone.utils.SharedPrefUtil.HOMEDATA_JSON;
import static kr.co.kdone.airone.utils.SharedPrefUtil.HOMEDATA_UPDATA_TIME;

/**
 * ikHwang 2019-06-04 오전 9:53 룸콘 등록 화면
 */
public class RegisterDevice2Activity extends AppCompatActivity {
    private final String TAG = getClass().getSimpleName();

    private Activity act;

    private NonSwipeableViewPager pagerRegisterDevice;
    private RegisterDevicePagerAdapter pagerAdapter;
    private ImageView[] headerSteps;
    private View[] headerStepDividers;
    private Fragment currFragment;

    private String userId;
    private String password;
    private String name;
    private String phone;
    private String deviceID;
    private String ssid;
    private Handler registerHandler = new Handler();

    public MapPoint gpsLatLng;
    public MapPoint selectLatLng;
    public String mArea;
    public int mode; //룸콘 등록 or 재등록
    public int subMode; //룸콘 or 에어모니터
    public int registerCount;

    private boolean mIsShowToast=false;
    private TextView mTxtToast;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_device2);
        act = this;

        mTxtToast = findViewById(R.id.txtToast);

        mode = getIntent().getIntExtra("mode", MODE_NONE);
        subMode = getIntent().getIntExtra("sub_mode", MODE_NONE);
        userId = getIntent().getStringExtra("userID");
        password = getIntent().getStringExtra("password");
        name = getIntent().getStringExtra("name");
        phone = getIntent().getStringExtra("phone");

        headerSteps = new ImageView[]{
                findViewById(R.id.header_step1),
                findViewById(R.id.header_step2),
                findViewById(R.id.header_step3),
                findViewById(R.id.header_step4)
        };
        headerStepDividers = new View[]{
                findViewById(R.id.header_step_divider1),
                findViewById(R.id.header_step_divider2),
                findViewById(R.id.header_step_divider3)
        };

        int pageCount = 4;
        if (mode == MODE_CHANGE_AP || subMode == MODE_DEVICE_MONITOR ) {
            pageCount = 3;
            headerSteps[3].setVisibility(View.GONE);
            headerStepDividers[2].setVisibility(View.GONE);
        }else if(mode == MODE_FIND_PASS){
            pageCount = 2;
            headerSteps[2].setVisibility(View.GONE);
            headerStepDividers[1].setVisibility(View.GONE);
            headerSteps[3].setVisibility(View.GONE);
            headerStepDividers[2].setVisibility(View.GONE);
        }else    {
//            setGpsLocation();
        }

        pagerRegisterDevice = findViewById(R.id.pagerRegisterDevice);
        pagerAdapter = new RegisterDevicePagerAdapter(getSupportFragmentManager(), pageCount);
        pagerRegisterDevice.setAdapter(pagerAdapter);
        pagerRegisterDevice.setOffscreenPageLimit(5);
        pagerRegisterDevice.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                currFragment = pagerAdapter.getItem(position);
                for (int i = 0; i < 4; i++) {
                    if (position < i) {
                        headerSteps[i].setEnabled(true);
                        headerSteps[i].setSelected(false);
                        if (i < 3) {
                            headerStepDividers[i].setSelected(false);
                        }
                    } else if (position == i) {
                        headerSteps[i].setEnabled(true);
                        headerSteps[i].setSelected(true);
                        if (i < 3) {
                            headerStepDividers[i].setSelected(false);
                        }
                    } else if (position > i) {
                        headerSteps[i].setEnabled(false);
                        headerSteps[i].setSelected(false);
                        if (i < 3) {
                            headerStepDividers[i].setSelected(true);
                        }
                    }
                }

                if (position == 0) {
                    ((RegisterDevice1Fragment) pagerAdapter.getItem(position)).setScrollTop();
                } else if (position == 1) {
                    deviceID = "";
                } else if (position == 2) {
                    ((RegisterDevice3Fragment) pagerAdapter.getItem(position)).initList();
                } else if (position == 3) {
                    ((RegisterDevice4Fragment) pagerAdapter.getItem(position)).initMap();
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });

        currFragment = pagerAdapter.getItem(pagerRegisterDevice.getCurrentItem());
    }

    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.btnBack:
                onBackPressed();
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == RESULT_UNREGISTER_DEVICE_POPUP) {
                moveRegisterPage(0);
            } else if (requestCode == RESULT_REGISTER_SENSORBOX_SUCCESSED) {
                setResult(RESULT_OK);
                finish();
            } else {
                if (currFragment != null) {
                    currFragment.onActivityResult(requestCode, resultCode, data);
                }
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (currFragment instanceof RegisterDevice4Fragment) {
            Intent intent = new Intent(this, PopupActivity.class);
            intent.putExtra(PopupActivity.POPUP_MODE, PopupActivity.MODE_YESNO);
            intent.putExtra(PopupActivity.POPUP_BODY_TEXT, getString(R.string.error_register_device_complete));
            startActivityForResult(intent, RESULT_UNREGISTER_DEVICE_POPUP);
            overridePendingTransition(0, 0);
        } else if (currFragment instanceof RegisterDevice1Fragment) {
            finish();
        } else {
            moveRegisterPage(pagerRegisterDevice.getCurrentItem() - 1);
        }
    }

    public void moveRegisterPage(int index) {
        pagerRegisterDevice.setCurrentItem(index);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            setGpsLocation();
        }
    }

    @SuppressLint("MissingPermission")
    public void setGpsLocation() {
        /*if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{
                            android.Manifest.permission.ACCESS_FINE_LOCATION
                    }, 123);
            return;
        }*/
        LocationManager locationManager = (LocationManager) act.getSystemService(Context.LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 100, 1,
                new LocationListener() {
                    @Override
                    public void onLocationChanged(Location location) {
                        double latitude = location.getLatitude();
                        double longitude = location.getLongitude();

//                        CommonUtils.customLog(TAG, "GPS_PROVIDER lat : " + latitude + ", lng : " + longitude, Log.ERROR);

                        gpsLatLng = MapPoint.mapPointWithGeoCoord(latitude, longitude);
                    }

                    @Override
                    public void onStatusChanged(String provider, int status, Bundle extras) {
                    }

                    @Override
                    public void onProviderEnabled(String provider) {
                    }

                    @Override
                    public void onProviderDisabled(String provider) {
                    }
                });

        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 100, 1,
                new LocationListener() {
                    @Override
                    public void onLocationChanged(Location location) {
                        double latitude = location.getLatitude();
                        double longitude = location.getLongitude();

//                        CommonUtils.customLog(TAG, "NETWORK_PROVIDER lat : " + latitude + ", lng : " + longitude, Log.ERROR);

                        gpsLatLng = MapPoint.mapPointWithGeoCoord(latitude, longitude);
                    }

                    @Override
                    public void onStatusChanged(String provider, int status, Bundle extras) {
                    }

                    @Override
                    public void onProviderEnabled(String provider) {
                    }

                    @Override
                    public void onProviderDisabled(String provider) {
                    }
                });
    }

    // 에어모니터 등록
    public void sendRegisterMonitorDevice() {
        try {
            HttpApi.PostV2AddAirMonitor(
                    CleanVentilationApplication.getInstance().getUserInfo().getId(),
                    CleanVentilationApplication.getInstance().getHomeList().get(0).getRoomController().getGid(),
                    deviceID,
                    ssid,
                    new Callback() {
                        @Override
                        public void onFailure(Call call, IOException e) {
                            DismissConnectDialog();
                            CommonUtils.showToast(RegisterDevice2Activity.this, getString(R.string.toast_result_can_not_register));
                        }

                        @Override
                        public void onResponse(Call call, Response response) throws IOException {
                            try {
                                JSONObject json_data = new JSONObject(response.body().string());

                                CommonUtils.customLog(TAG, "device/addAirMonitor : " + json_data.toString(), Log.ERROR);

                                switch (json_data.getInt("code")) {
                                    case HttpApi.RESPONSE_SUCCESS:
                                        if (subMode == MODE_DEVICE_ROOM_CON) {
                                            SharedPrefUtil.putString(SharedPrefUtil.CURRENT_CONTROL_DEVICE_ID, deviceID);
                                            SharedPrefUtil.putString(HOMEDATA_JSON, DEFAULT_HOMEDATA_JSON);
                                            SharedPrefUtil.putLong(HOMEDATA_UPDATA_TIME, 0);
                                            setResult(RESULT_OK);
                                            finish();
                                        } else if (subMode == MODE_DEVICE_MONITOR) {
                                            runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    DismissConnectDialog();

                                                    Intent intent = new Intent(act, PopupActivity.class);
                                                    intent.putExtra(PopupActivity.POPUP_MODE, PopupActivity.MODE_NORMAL);
                                                    intent.putExtra(PopupActivity.POPUP_BODY_TEXT, getString(R.string.successed_reg_sensorbox));
                                                    startActivityForResult(intent, RESULT_REGISTER_SENSORBOX_SUCCESSED);
                                                    overridePendingTransition(0, 0);
                                                }
                                            });
                                        }
                                        break;
                                        
                                    case HttpApi.RESPONSE_DEVICE_NOT_EXIST: // 디바이스 등록되지 않았음
                                        if (registerCount == 24) {
                                            registerCount = 0;
                                            CommonUtils.showToast(RegisterDevice2Activity.this, getString(R.string.toast_result_no_exsist_device));
                                            DismissConnectDialog();
                                        } else {
                                            registerHandler.postDelayed(new Runnable() {
                                                @Override
                                                public void run() {
                                                    registerCount++;
                                                    sendRegisterMonitorDevice();
                                                }
                                            }, 5000);
                                            return;
                                        }
                                        break;

                                    default:
                                        CommonUtils.showToast(RegisterDevice2Activity.this, getString(R.string.toast_result_can_not_register));
                                        break;
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                                CommonUtils.showToast(RegisterDevice2Activity.this, getString(R.string.toast_result_can_not_register));
                            }
                        }
                    });
        } catch (Exception e) {
            e.printStackTrace();
            DismissConnectDialog();
            CommonUtils.showToast(RegisterDevice2Activity.this, getString(R.string.toast_result_can_not_register));
        }
    }

    //디바이스 상태 확인
    public void checkRegisterDevice(){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (!IsRunningProgress()) {
                    displayProgress(RegisterDevice2Activity.this,"", "");
                }
            }
        });
        try {
            HttpApi.PostV2CheckDeviceState(//V2A 작업.
                    deviceID,
                    new Callback() {
                        @Override
                        public void onFailure(Call call, IOException e) {
                            CommonUtils.showToast(RegisterDevice2Activity.this, getString(R.string.toast_result_can_not_request));
                        }

                        @Override
                        public void onResponse(Call call, Response response) throws IOException {
                            try {
                                JSONObject json_data = new JSONObject(response.body().string());

                                CommonUtils.customLog(TAG, "device/checkState : " + json_data.toString(), Log.ERROR);

                                switch (json_data.getInt("code")) {
                                    case HttpApi.RESPONSE_SUCCESS:
                                        if (mode == MODE_DEVICE_REG) {
                                            sendRegisterMember(); //회원가입시 신규 등록
                                        }else if (mode == MODE_DEVICE_ONLY_REG){
                                            sendRoomConRegistRenew(); //룸컨트롤러 재등록
                                        }
                                        break;

                                    default:
                                        CommonUtils.customLog(TAG, "registerCount : " + registerCount, Log.ERROR);

                                        if (registerCount == 24) {
                                            registerCount = 0;
                                            DismissConnectDialog();
                                            CommonUtils.showToast(RegisterDevice2Activity.this, getString(R.string.toast_result_no_exsist_device));
                                        } else {
                                            registerHandler.postDelayed(new Runnable() {
                                                @Override
                                                public void run() {
                                                    registerCount++;
                                                    checkRegisterDevice();
                                                }
                                            }, 5000);
                                            return;
                                        }
                                        break;
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                                CommonUtils.showToast(RegisterDevice2Activity.this, getString(R.string.toast_result_can_not_request));
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        DismissConnectDialog();
                                    }
                                });
                            }
                        }
                    });

        }catch (Exception e){
            e.printStackTrace();
            DismissConnectDialog();
            CommonUtils.showToast(RegisterDevice2Activity.this, getString(R.string.toast_result_can_not_register));
        }
    }

    //룸콘트롤러 재등록
    private void sendRoomConRegistRenew(){
        try {
            float lat = (float) 37.566535;
            float lng = (float) 126.97796919999996;

            if(selectLatLng!=null){
                lat = (float)selectLatLng.getMapPointGeoCoord().latitude;
                lng = (float) selectLatLng.getMapPointGeoCoord().longitude;
            }

            if (!IsRunningProgress()) {
                displayProgress(this,"", "");
            }

            HttpApi.PostV2ReNewRoomController(
                    CleanVentilationApplication.getInstance().getUserInfo().getId(),
                    deviceID,
                    ssid,
                    lat,
                    lng,
                    mArea,
                    new Callback() {
                        @Override
                        public void onFailure(Call call, IOException e) {
                            DismissConnectDialog();
                            CommonUtils.showToast(RegisterDevice2Activity.this, getString(R.string.toast_result_can_not_register));
                        }

                        @Override
                        public void onResponse(Call call, Response response) throws IOException {
                            try {
                                JSONObject json_data = new JSONObject(response.body().string());

                                CommonUtils.customLog(TAG, "device/add-room-controller : " + json_data.toString(), Log.ERROR);

                                switch (json_data.getInt("code")) {
                                    case HttpApi.RESPONSE_SUCCESS:
                                        if (subMode == MODE_DEVICE_ROOM_CON) {
                                            SharedPrefUtil.putString(SharedPrefUtil.CURRENT_CONTROL_DEVICE_ID, deviceID);
                                            SharedPrefUtil.putString(HOMEDATA_JSON, DEFAULT_HOMEDATA_JSON);
                                            SharedPrefUtil.putLong(HOMEDATA_UPDATA_TIME, 0);
                                            setResult(RESULT_OK);
                                            finish();
                                        } else if (subMode == MODE_DEVICE_MONITOR) {
                                            sendPairingAirMonitor();
                                        }
                                        break;
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                                CommonUtils.showToast(RegisterDevice2Activity.this, getString(R.string.toast_result_can_not_request));
                            } finally {
                                DismissConnectDialog();
                            }
                        }
                    });
        }catch (Exception e){
            e.printStackTrace();
            CommonUtils.showToast(RegisterDevice2Activity.this, getString(R.string.toast_result_can_not_request));
            DismissConnectDialog();
        }
    }

    // 회원가입
    private void sendRegisterMember() {
        try {
            float lat = (float) 37.566535;
            float lng = (float) 126.97796919999996;

            if(selectLatLng!=null){
                lat = (float)selectLatLng.getMapPointGeoCoord().latitude;
                lng = (float) selectLatLng.getMapPointGeoCoord().longitude;
            }

            if (!IsRunningProgress()) {
                displayProgress(this,"", "");
            }

            HttpApi.PostV2UserJoin( //V2A 작업.
                    userId, password, name, phone, deviceID, lat, lng, mArea, String.valueOf(BuildConfig.VERSION_CODE), Build.MODEL, ssid,
                    new Callback() {
                        @Override
                        public void onFailure(Call call, IOException e) {
                            DismissConnectDialog();
                            CommonUtils.showToast(RegisterDevice2Activity.this, getString(R.string.toast_result_can_not_register));
                        }

                        @Override
                        public void onResponse(Call call, Response response) throws IOException {
                            try {
                                JSONObject json_data = new JSONObject(response.body().string());

                                CommonUtils.customLog(TAG, "user/sign-up : " + json_data.toString(), Log.ERROR);

                                switch (json_data.getInt("code")){
                                    case HttpApi.RESPONSE_SUCCESS:
                                        SharedPrefUtil.putString(SharedPrefUtil.CURRENT_CONTROL_DEVICE_ID, deviceID);
                                        SharedPrefUtil.putString(SharedPrefUtil.CURRENT_CONTROL_DEVICE_NAME, DEFAULT_CURRENT_CONTROL_DEVICE_NAME);
                                        SharedPrefUtil.putString(HOMEDATA_JSON, DEFAULT_HOMEDATA_JSON);
                                        SharedPrefUtil.putLong(HOMEDATA_UPDATA_TIME, 0);

                                        Intent intent = new Intent(act, RegisterResultActivity.class);
                                        intent.putExtra("mode", MODE_SUCCESS_REG_ID);
                                        intent.putExtra("userID", userId);
                                        intent.putExtra("password", password);
                                        startActivity(intent);
                                        break;

                                    case HttpApi.RESPONSE_USER_ID_ALREADY_EXIST:
                                        CommonUtils.showToast(RegisterDevice2Activity.this, getString(R.string.toast_result_use_already_email));
                                        break;

                                    default:
                                        CommonUtils.showToast(RegisterDevice2Activity.this, getString(R.string.toast_result_can_not_request));
                                        break;
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                                CommonUtils.showToast(RegisterDevice2Activity.this, getString(R.string.toast_result_can_not_request));
                            }
                            DismissConnectDialog();
                        }
                    });
        } catch (Exception e) {
            e.printStackTrace();
            CommonUtils.showToast(RegisterDevice2Activity.this, getString(R.string.toast_result_can_not_request));
            DismissConnectDialog();
        }
    }

    // 센싱박스 페어링
    private void sendPairingAirMonitor() {
        try {
            DeviceInfo currentRoomCon = DeviceManager.getInstance().getDeviceForDeviceList(SharedPrefUtil.getString(SharedPrefUtil.CURRENT_CONTROL_DEVICE_ID, ""));
            if (currentRoomCon.PCD.equals("1")) {
                if (currentRoomCon.SensingBox1 != null && !currentRoomCon.SensingBox1.equals("null") && !currentRoomCon.SensingBox1.equals("")) {
                    HttpApi.PostUnregisterPairingSensorBox(
                            CleanVentilationApplication.getInstance().getUserInfo().getId(),
                            SharedPrefUtil.getString(SharedPrefUtil.CURRENT_CONTROL_DEVICE_ID, ""),
                            currentRoomCon.SensingBox1,
                            new Callback() {
                                @Override
                                public void onFailure(Call call, IOException e) {
                                    DismissConnectDialog();
                                    CommonUtils.showToast(RegisterDevice2Activity.this, getString(R.string.toast_result_can_not_search));
                                }

                                @Override
                                public void onResponse(Call call, Response response) throws IOException {
                                    try {
                                        JSONObject json_data = new JSONObject(response.body().string());

                                        CommonUtils.customLog(TAG, "deleteParing : " + json_data.toString(), Log.ERROR);

                                        if (json_data.getString("msg").equals("DELETE_PAIRING_SUCCESS")) {
                                            registerPairingSensorBox();
                                        } else if (json_data.getString("msg").equals("NO_EXIST_INFORMATION")) {
                                            CommonUtils.showToast(RegisterDevice2Activity.this, getString(R.string.toast_result_wrong_data));
                                        } else if (json_data.getString("msg").equals("NOT_TAC")) {
                                            CommonUtils.showToast(RegisterDevice2Activity.this, getString(R.string.toast_result_no_tac_model));
                                        } else if (json_data.getString("msg").equals("ALREADY_OTHER_PAIRING_SENSINGBOX")) {
                                            CommonUtils.showToast(RegisterDevice2Activity.this, getString(R.string.toast_result_already_other_pairing_sensorbox));
                                        } else if (json_data.getString("msg").equals("SENSINGBOX_ALL_USE")) {
                                            CommonUtils.showToast(RegisterDevice2Activity.this, getString(R.string.toast_result_full_db));
                                        } else if (json_data.getString("msg").equals("ALREADY_PAIRING_SENSINGBOX")) {
                                            CommonUtils.showToast(RegisterDevice2Activity.this, getString(R.string.toast_result_already_pairing_sensorbox));
                                        } else if (json_data.getString("msg").equals("NO_EXIST_SENSINGBOX")) {
                                            CommonUtils.showToast(RegisterDevice2Activity.this, getString(R.string.toast_result_no_sensorbox));
                                        } else {
                                            CommonUtils.showToast(RegisterDevice2Activity.this, getString(R.string.toast_result_can_not_search));
                                        }
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                        CommonUtils.showToast(RegisterDevice2Activity.this, getString(R.string.toast_result_can_not_search));
                                    }
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            DismissConnectDialog();
                                        }
                                    });
                                }
                            });
                } else {
                    registerPairingSensorBox();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            DismissConnectDialog();
            CommonUtils.showToast(RegisterDevice2Activity.this, getString(R.string.toast_result_can_not_search));
        }
    }

    private void registerPairingSensorBox() {
        try {
            HttpApi.PostRegisterPairingSensorBox(
                    CleanVentilationApplication.getInstance().getUserInfo().getId(),
                    SharedPrefUtil.getString(SharedPrefUtil.CURRENT_CONTROL_DEVICE_ID, ""),
                    deviceID,
                    new Callback() {
                        @Override
                        public void onFailure(Call call, IOException e) {
                            DismissConnectDialog();
                            CommonUtils.showToast(RegisterDevice2Activity.this, getString(R.string.toast_result_can_not_search));
                        }

                        @Override
                        public void onResponse(Call call, Response response) throws IOException {
                            try {
                                JSONObject json_data = new JSONObject(response.body().string());

                                CommonUtils.customLog(TAG, "registerParing : " + json_data.toString(), Log.ERROR);

                                SharedPrefUtil.putString(HOMEDATA_JSON, DEFAULT_HOMEDATA_JSON);
                                SharedPrefUtil.putLong(HOMEDATA_UPDATA_TIME, 0);
                                if (json_data.getString("msg").equals("PAIRING_SUCCESS")) {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Intent intent = new Intent(act, PopupActivity.class);
                                            intent.putExtra(PopupActivity.POPUP_MODE, PopupActivity.MODE_NORMAL);
                                            intent.putExtra(PopupActivity.POPUP_BODY_TEXT, getString(R.string.successed_reg_sensorbox));
                                            startActivityForResult(intent, RESULT_REGISTER_SENSORBOX_SUCCESSED);
                                            overridePendingTransition(0, 0);
                                        }
                                    });
                                } else if (json_data.getString("msg").equals("NO_EXIST_INFORMATION")) {
                                    CommonUtils.showToast(RegisterDevice2Activity.this, getString(R.string.toast_result_wrong_data));
                                } else if (json_data.getString("msg").equals("NOT_TAC")) {
                                    CommonUtils.showToast(RegisterDevice2Activity.this, getString(R.string.toast_result_no_tac_model));
                                } else if (json_data.getString("msg").equals("ALREADY_OTHER_PAIRING_SENSINGBOX")) {
                                    CommonUtils.showToast(RegisterDevice2Activity.this, getString(R.string.toast_result_already_other_pairing_sensorbox));
                                } else if (json_data.getString("msg").equals("SENSINGBOX_ALL_USE")) {
                                    CommonUtils.showToast(RegisterDevice2Activity.this, getString(R.string.toast_result_full_db));
                                } else if (json_data.getString("msg").equals("ALREADY_PAIRING_SENSINGBOX")) {
                                    CommonUtils.showToast(RegisterDevice2Activity.this, getString(R.string.toast_result_already_pairing_sensorbox));
                                } else if (json_data.getString("msg").equals("NOT_SENSINGBOX")) {
                                    CommonUtils.showToast(RegisterDevice2Activity.this, getString(R.string.toast_result_no_sensorbox));
                                } else {
                                    CommonUtils.showToast(RegisterDevice2Activity.this, getString(R.string.toast_result_can_not_search));
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                                CommonUtils.showToast(RegisterDevice2Activity.this, getString(R.string.toast_result_can_not_search));
                            }
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    DismissConnectDialog();
                                }
                            });
                        }
                    });
        } catch (Exception e) {
            e.printStackTrace();
            DismissConnectDialog();
            CommonUtils.showToast(RegisterDevice2Activity.this, getString(R.string.toast_result_can_not_search));
        }
    }

    public void setDeviceID(String deviceID) {
        this.deviceID = deviceID;
    }

    public void setSsid(String ssid) {
        this.ssid = ssid;
    }
}
