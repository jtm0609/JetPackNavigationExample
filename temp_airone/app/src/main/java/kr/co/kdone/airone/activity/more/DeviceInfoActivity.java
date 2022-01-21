package kr.co.kdone.airone.activity.more;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import kr.co.kdone.airone.CleanVentilationApplication;
import kr.co.kdone.airone.R;
import kr.co.kdone.airone.activity.register.RegisterDevice2Activity;
import kr.co.kdone.airone.activity.register.prism.RegisterPrismDeviceActivity;
import kr.co.kdone.airone.utils.CommonUtils;
import kr.co.kdone.airone.utils.HomeInfoDataParser;
import kr.co.kdone.airone.utils.HttpApi;
import kr.co.kdone.airone.widget.WidgetUtils;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

import static kr.co.kdone.airone.utils.CommonUtils.DismissConnectDialog;
import static kr.co.kdone.airone.utils.CommonUtils.MODE_CHANGE_AP;
import static kr.co.kdone.airone.utils.CommonUtils.MODE_DEVICE_MONITOR;
import static kr.co.kdone.airone.utils.CommonUtils.MODE_DEVICE_ROOM_CON;
import static kr.co.kdone.airone.utils.CommonUtils.MODE_NONE;
import static kr.co.kdone.airone.utils.CommonUtils.displayProgress;
import static kr.co.kdone.airone.utils.CommonUtils.startActivityAni;

/**
 * ikHwang 2019-06-04 오전 8:44 룸콘 정보 확인 및 수정 화면
 */
public class DeviceInfoActivity extends AppCompatActivity implements View.OnClickListener {
    private final String TAG = getClass().getSimpleName();

    private Activity act;
    private TextView txtTitle;
    private TextView txtDeviceLocation, txtDeviceNetwork, txtDevicePush, txtDeviceAirMonitor, txtAirmonitorLED;
    private LinearLayout layoutDeviceLocation, layoutDeviceNetwork, layoutDevicePush, layoutDeviceAirMonitor, layoutAirmonitorLED;
    private FrameLayout lineDeviceInfo1, lineDeviceInfo2, lineDeviceInfo3, lineDeviceInfo4;

    private int mode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_info);
        act = this;

        mode = getIntent().getIntExtra("mode", MODE_NONE);

        txtTitle = findViewById(R.id.txtTitle);
        txtDeviceLocation = findViewById(R.id.txtDeviceLocation);
        txtDeviceNetwork = findViewById(R.id.txtDeviceNetwork);
        txtDevicePush = findViewById(R.id.txtDevicePush);
        txtDeviceAirMonitor = findViewById(R.id.txtDeviceAirMonitor);
        txtAirmonitorLED = findViewById(R.id.txtAirmonitorLED);

        layoutDeviceLocation = findViewById(R.id.layoutDeviceLocation);
        layoutDeviceNetwork = findViewById(R.id.layoutDeviceNetwork);
        layoutDevicePush = findViewById(R.id.layoutDevicePush);
        layoutDeviceAirMonitor = findViewById(R.id.layoutDeviceAirMonitor);
        layoutAirmonitorLED = findViewById(R.id.layoutAirmonitorLED);

        lineDeviceInfo1 = findViewById(R.id.lineDeviceInfo1);
        lineDeviceInfo2 = findViewById(R.id.lineDeviceInfo2);
        lineDeviceInfo3 = findViewById(R.id.lineDeviceInfo3);
        lineDeviceInfo4 = findViewById(R.id.lineDeviceInfo4);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mode == MODE_DEVICE_ROOM_CON) {
            txtTitle.setText(R.string.device_air_roomcon);
            layoutDeviceLocation.setVisibility(View.VISIBLE);
            layoutDeviceNetwork.setVisibility(View.VISIBLE);
            layoutDevicePush.setVisibility(View.VISIBLE);
            layoutDeviceAirMonitor.setVisibility(View.VISIBLE);
            layoutAirmonitorLED.setVisibility(View.GONE);

            lineDeviceInfo1.setVisibility(View.VISIBLE);
            lineDeviceInfo2.setVisibility(View.VISIBLE);
            lineDeviceInfo3.setVisibility(View.VISIBLE);
            lineDeviceInfo4.setVisibility(View.GONE);
        } else if (mode == MODE_DEVICE_MONITOR) {
            txtTitle.setText(R.string.device_air_monitor);
            layoutDeviceLocation.setVisibility(View.GONE);
            layoutDeviceNetwork.setVisibility(View.VISIBLE);
            layoutDevicePush.setVisibility(View.GONE);
            layoutDeviceAirMonitor.setVisibility(View.GONE);

            lineDeviceInfo1.setVisibility(View.GONE);
            lineDeviceInfo2.setVisibility(View.VISIBLE);
            lineDeviceInfo3.setVisibility(View.GONE);


            try {
                if(2 < CleanVentilationApplication.getInstance().getRoomControllerDevicePCD()){
                    findViewById(R.id.img_arrow).setVisibility(View.GONE);
                }else{
                    findViewById(R.id.img_arrow).setVisibility(View.VISIBLE);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            if(CleanVentilationApplication.getInstance().isIsOldUser()){
                layoutAirmonitorLED.setVisibility(View.GONE);
                lineDeviceInfo4.setVisibility(View.GONE);
            }else{
                layoutAirmonitorLED.setVisibility(View.VISIBLE);
                lineDeviceInfo4.setVisibility(View.GONE);
            }
        }

        getDeviceInfo();
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.btnBack:
                onBackPressed();
                break;

            case R.id.layoutDeviceLocation: {
                if (requestPermission()) {
                    Intent intent = new Intent(act, MoreDeviceLocationActivity.class);
                    intent.putExtra("gid", CleanVentilationApplication.getInstance().getRoomControllerDeviceID());

                    //V2A zipcode 삭제로 lat, lng값 넘김.
                    if(CleanVentilationApplication.getInstance().hasRoomController()){
                        intent.putExtra("lat", CleanVentilationApplication.getInstance().getHomeList().get(0).getRoomController().getLat());
                        intent.putExtra("lng", CleanVentilationApplication.getInstance().getHomeList().get(0).getRoomController().getLng());
                    }
                    startActivityAni(DeviceInfoActivity.this, intent, false, 0);
                }
                break;
            }

            case R.id.layoutDeviceNetwork: {
                if (mode == MODE_DEVICE_MONITOR){
                    try {
                        if(5 <= CleanVentilationApplication.getInstance().getRoomControllerDevicePCD()){
                            return;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                Intent intent;

                if(!CleanVentilationApplication.getInstance().isIsOldUser()){
                    intent = new Intent(act, RegisterPrismDeviceActivity.class);
                }else{
                    intent = new Intent(act, RegisterDevice2Activity.class);
                }

                intent.putExtra("mode", MODE_CHANGE_AP);
                intent.putExtra("sub_mode", mode);
                startActivityAni(DeviceInfoActivity.this, intent, false, 0);
                break;
            }

            case R.id.layoutDevicePush: {
                Intent intent = new Intent(act, DevicePushSettingActivity.class);
                intent.putExtra("gid", CleanVentilationApplication.getInstance().getRoomControllerDeviceID());

                if(CleanVentilationApplication.getInstance().hasRoomController()){
                    intent.putExtra("pushUse", CleanVentilationApplication.getInstance().getHomeList().get(0).getRoomController().getPusheUse());
                    intent.putExtra("smartAlarm", CleanVentilationApplication.getInstance().getHomeList().get(0).getRoomController().getSmartAlarm());
                    intent.putExtra("filterAlarm", CleanVentilationApplication.getInstance().getHomeList().get(0).getRoomController().getFilterAlarm());
                }
                startActivityAni(DeviceInfoActivity.this, intent, false, 0);
                break;
            }

            case R.id.layoutDeviceAirMonitor:
                break;

            case R.id.layoutAirmonitorLED: { // 에어모니터 LED 밝기 조절 화면
                Intent intent = new Intent(act, AirMonitorLEDSettingActivity.class);
                startActivityAni(DeviceInfoActivity.this, intent, false, 0);
            }
            break;
        }
    }

    private boolean requestPermission() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ) {
            ActivityCompat.requestPermissions(this,
                    new String[]{
                            android.Manifest.permission.ACCESS_FINE_LOCATION
                    }, 123);
            return false;
        }
        return true;
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
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            findViewById(R.id.layoutDeviceLocation).performClick();
        }
    }

    private void getDeviceInfo() {
        displayProgress(this, "", "");
        try {
            HttpApi.PostV2GetDeviceInfo(//V2A 적용.
                    CleanVentilationApplication.getInstance().getUserInfo().getId(),
                    new Callback() {
                        @Override
                        public void onFailure(Call call, IOException e) {
                            DismissConnectDialog();
                            CommonUtils.showToast(DeviceInfoActivity.this, getString(R.string.toast_result_can_not_search));
                            finish();
                        }

                        @Override
                        public void onResponse(Call call, Response response) throws IOException {
                            try {
                                String strResponse = response.body().string();

                                CommonUtils.customLog(TAG, "device/info  : " + strResponse, Log.ERROR);

                                if(TextUtils.isEmpty(strResponse)){
                                    CommonUtils.showToast(DeviceInfoActivity.this, getString(R.string.toast_result_can_not_search));
                                    finish();
                                }else{
                                    JSONObject json_data = new JSONObject(strResponse);

                                    switch (json_data.optInt("code", 0)){
                                        case HttpApi.RESPONSE_SUCCESS: // 디바이스 정보 조회 성공
                                            if(json_data.has("data")){
                                                // ikHwang 2019-05-21 오후 1:32 메인화면 이동시 화면을 구성하기 위해 데이터 파싱
                                                HomeInfoDataParser.paserHomeInfo(CleanVentilationApplication.getInstance(), json_data.getJSONObject("data"), false);
                                            }

                                            runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    if(CleanVentilationApplication.getInstance().hasRoomController()){
                                                        txtDeviceLocation.setText(CleanVentilationApplication.getInstance().getHomeList().get(0).getRoomController().getArea());
                                                        txtDeviceNetwork.setText(CleanVentilationApplication.getInstance().getHomeList().get(0).getRoomController().getSsid());
                                                        txtDevicePush.setText(1 == CleanVentilationApplication.getInstance().getHomeList().get(0).getRoomController().getSmartAlarm()
                                                                || 1 == CleanVentilationApplication.getInstance().getHomeList().get(0).getRoomController().getFilterAlarm() ? R.string.on : R.string.off);
                                                        txtDeviceAirMonitor.setText((null == CleanVentilationApplication.getInstance().getHomeList().get(0).getAirMonitor()
                                                                || 0 == CleanVentilationApplication.getInstance().getHomeList().get(0).getAirMonitor().getState()) ? R.string.none_connect : R.string.connected);
                                                    }
                                                }
                                            });
                                            break;

                                        default:
                                            CommonUtils.showToast(DeviceInfoActivity.this, getString(R.string.toast_result_can_not_search));
                                            finish();
                                            break;
                                    }
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                                CommonUtils.showToast(DeviceInfoActivity.this, getString(R.string.toast_result_can_not_search));
                                finish();
                            } finally {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        DismissConnectDialog();
                                    }
                                });
                            }
                        }
                    });
        } catch (Exception e) {
            e.printStackTrace();
            DismissConnectDialog();
            CommonUtils.showToast(DeviceInfoActivity.this, getString(R.string.toast_result_can_not_search));
        }
    }
}