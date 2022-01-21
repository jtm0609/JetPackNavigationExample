package kr.co.kdone.airone.activity.more;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import kr.co.kdone.airone.CleanVentilationApplication;
import kr.co.kdone.airone.R;
import kr.co.kdone.airone.activity.PopupActivity;
import kr.co.kdone.airone.activity.info.DeviceDetailActivity;
import kr.co.kdone.airone.activity.info.RoomControlerDetailActivity;
import kr.co.kdone.airone.activity.register.RegisterDevice2Activity;
import kr.co.kdone.airone.utils.CommonUtils;
import kr.co.kdone.airone.utils.HomeInfoDataParser;
import kr.co.kdone.airone.utils.HttpApi;
import kr.co.kdone.airone.widget.WidgetUtils;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

import static kr.co.kdone.airone.utils.CommonUtils.DismissConnectDialog;
import static kr.co.kdone.airone.utils.CommonUtils.MODE_DEVICE_MONITOR;
import static kr.co.kdone.airone.utils.CommonUtils.MODE_DEVICE_ONLY_REG;
import static kr.co.kdone.airone.utils.CommonUtils.MODE_DEVICE_ROOM_CON;
import static kr.co.kdone.airone.utils.CommonUtils.RESULT_DETAIL_CHECK;
import static kr.co.kdone.airone.utils.CommonUtils.RESULT_REGISTER_DEVICE;
import static kr.co.kdone.airone.utils.CommonUtils.RESULT_REGISTER_ROOMCON_COMFIRM;
import static kr.co.kdone.airone.utils.CommonUtils.RESULT_REGISTER_SENSORBOX_COMFIRM;
import static kr.co.kdone.airone.utils.CommonUtils.displayProgress;
import static kr.co.kdone.airone.utils.CommonUtils.startActivityAni;

/**
 * ikHwang 2019-06-04 오전 8:46 제품 정보 (룸콘, 에어모니터 선택 화면)
 */
public class DeviceSelectActivity extends AppCompatActivity implements View.OnClickListener {
    private final String TAG = getClass().getSimpleName();

    private Activity act;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_select);

        act = this;
    }

    @Override
    protected void onResume() {
        super.onResume();
        getDeviceInfo();
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
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case RESULT_REGISTER_SENSORBOX_COMFIRM: {
                    int pcd = CleanVentilationApplication.getInstance().getRoomControllerDevicePCD();

                    if(1 == pcd){
                        Intent intent = new Intent(act, RegisterDevice2Activity.class);
                        intent.putExtra("mode", MODE_DEVICE_ONLY_REG);
                        intent.putExtra("sub_mode", MODE_DEVICE_MONITOR);
                        startActivityForResult(intent, RESULT_REGISTER_DEVICE);
                    }else if(3 <= pcd){
                        Intent intent = new Intent(act, DeviceDetailActivity.class);
                        intent.putExtra("mode", MODE_DEVICE_MONITOR);
                        startActivityForResult(intent, RESULT_DETAIL_CHECK);
                    }
                    break;
                }
                case RESULT_REGISTER_ROOMCON_COMFIRM: {
                    if(CleanVentilationApplication.getInstance().isIsOldUser()){
                        Intent intent = new Intent(act, RegisterDevice2Activity.class);
                        intent.putExtra("mode", MODE_DEVICE_ONLY_REG);
                        intent.putExtra("sub_mode", MODE_DEVICE_ROOM_CON);
                        startActivityForResult(intent, RESULT_REGISTER_DEVICE);
                    }else{
                        Intent intent = new Intent(act, RoomControlerDetailActivity.class);
                        intent.putExtra("mode", MODE_DEVICE_ROOM_CON); //룸콘 재연결
                        startActivityForResult(intent, RESULT_DETAIL_CHECK);
                    }
                    break;
                }
            }
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.btnBack:
                onBackPressed();
                break;

            case R.id.layoutRoomController:
                if (TextUtils.isEmpty(CleanVentilationApplication.getInstance().getRoomControllerDeviceID())) {
                    Intent intent = new Intent(this, PopupActivity.class);
                    intent.putExtra(PopupActivity.POPUP_MODE, PopupActivity.MODE_YESNO);
                    intent.putExtra(PopupActivity.POPUP_CONFIRM_BUTTON_TEXT, getString(R.string.registration));
                    intent.putExtra(PopupActivity.POPUP_BODY_TEXT, getString(R.string.register_room_con_dialog));
                    startActivityForResult(intent, RESULT_REGISTER_ROOMCON_COMFIRM);
                    overridePendingTransition(0, 0);
                } else {
                    Intent intent = new Intent(act, DeviceInfoActivity.class);
                    intent.putExtra("mode", MODE_DEVICE_ROOM_CON);
                    intent.putExtra("deviceID", CleanVentilationApplication.getInstance().getRoomControllerDeviceID());
                    startActivityAni(act, intent, false, 0);
                }
                break;

            case R.id.layoutAirMonitor:
                if(null == CleanVentilationApplication.getInstance().getHomeList() || CleanVentilationApplication.getInstance().getHomeList().size() <= 0) return;

                if(null == CleanVentilationApplication.getInstance().getHomeList().get(0).getAirMonitor()){
                    Intent intent = new Intent(this, PopupActivity.class);
                    intent.putExtra(PopupActivity.POPUP_MODE, PopupActivity.MODE_YESNO);
                    intent.putExtra(PopupActivity.POPUP_CONFIRM_BUTTON_TEXT, getString(R.string.registration));
                    intent.putExtra(PopupActivity.POPUP_BODY_TEXT, getString(R.string.register_air_monitor_dialog));
                    startActivityForResult(intent, RESULT_REGISTER_SENSORBOX_COMFIRM);
                    overridePendingTransition(0, 0);
                }else if(1 == CleanVentilationApplication.getInstance().getHomeList().get(0).getAirMonitor().getState()){
                    Intent intent = new Intent(act, DeviceInfoActivity.class);
                    intent.putExtra("mode", MODE_DEVICE_MONITOR);
                    intent.putExtra("deviceID", CleanVentilationApplication.getInstance().getAirMonitorDeviceID());
                    startActivityAni(DeviceSelectActivity.this, intent, false, 0);
                }else {
                    Intent intent = new Intent(this, PopupActivity.class);
                    intent.putExtra(PopupActivity.POPUP_MODE, PopupActivity.MODE_YESNO);
                    intent.putExtra(PopupActivity.POPUP_CONFIRM_BUTTON_TEXT, getString(R.string.detail_view));
                    intent.putExtra(PopupActivity.POPUP_BODY_TEXT, getString(R.string.register_air_monitor_dialog2));
                    startActivityForResult(intent, RESULT_REGISTER_SENSORBOX_COMFIRM);
                    overridePendingTransition(0, 0);
                }
                break;
        }
    }

    private void getDeviceInfo() {
        displayProgress(this, "", "");

        try {
            HttpApi.PostV2GetDeviceInfo( //V2A 작업.
                    CleanVentilationApplication.getInstance().getUserInfo().getId(),
                    new Callback() {
                        @Override
                        public void onFailure(Call call, IOException e) {
                            DismissConnectDialog();
                            CommonUtils.showToast(DeviceSelectActivity.this, getString(R.string.toast_result_can_not_search));
                            finish();
                        }

                        @Override
                        public void onResponse(Call call, Response response) throws IOException {
                            try {
                                String strResponse =  response.body().string();

                                CommonUtils.customLog(TAG, "device/info : " + strResponse, Log.ERROR);

                                if(TextUtils.isEmpty(strResponse)){
                                    CommonUtils.showToast(DeviceSelectActivity.this, getString(R.string.toast_result_can_not_request));
                                    finish();
                                }else{
                                    JSONObject json_data = new JSONObject(strResponse);

                                    switch (json_data.optInt("code", 0)){
                                        case HttpApi.RESPONSE_SUCCESS:
                                            if(json_data.has("data")){
                                                // ikHwang 2019-05-21 오후 1:32 메인화면 이동시 화면을 구성하기 위해 데이터 파싱
                                                HomeInfoDataParser.paserHomeInfo(CleanVentilationApplication.getInstance(), json_data.getJSONObject("data"), false);

                                                /*Intent sendIntent = new Intent(WidgetUtils.WIDGET_UPDATE_ACTION);
                                                sendBroadcast(sendIntent, getString(R.string.br_permission));*/
                                            }
                                            break;

                                        default:
                                            CommonUtils.showToast(DeviceSelectActivity.this, getString(R.string.toast_result_can_not_request));
                                            finish();
                                            break;
                                    }
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                                CommonUtils.showToast(DeviceSelectActivity.this, getString(R.string.toast_result_can_not_request));
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
            CommonUtils.showToast(DeviceSelectActivity.this, getString(R.string.toast_result_can_not_search));
        }
    }
}
