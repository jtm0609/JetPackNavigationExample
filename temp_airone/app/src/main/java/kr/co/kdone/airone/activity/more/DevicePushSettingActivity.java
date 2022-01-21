package kr.co.kdone.airone.activity.more;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.ToggleButton;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import androidx.appcompat.app.AppCompatActivity;
import kr.co.kdone.airone.CleanVentilationApplication;
import kr.co.kdone.airone.R;
import kr.co.kdone.airone.utils.CommonUtils;
import kr.co.kdone.airone.utils.HttpApi;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

import static kr.co.kdone.airone.utils.CommonUtils.DismissConnectDialog;
import static kr.co.kdone.airone.utils.CommonUtils.IsRunningProgress;
import static kr.co.kdone.airone.utils.CommonUtils.displayProgress;

/**
 * ikHwang 2019-06-04 오전 8:45 푸시 설정 화면
 */
public class DevicePushSettingActivity extends AppCompatActivity implements View.OnClickListener {
    private final String TAG = getClass().getSimpleName();

    private Activity act;
    private LinearLayout layoutPushAlarm;
    private ToggleButton btnOnOff;
    private CheckBox chkSmartAlarm, chkFilterAlarm;

    private String gid;
    private int pushUse;
    private int smartAlarm;
    private int filterAlarm;
    private boolean isFirst, isUpdate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_push_setting);
        act = this;
        isFirst = true;
        isUpdate = true;

        Intent intent = getIntent();

        if(intent!=null){
            gid = intent.getStringExtra("gid");
            pushUse = intent.getIntExtra("pushUse",0);
            smartAlarm = intent.getIntExtra("smartAlarm", 0);
            filterAlarm = intent.getIntExtra("filterAlarm", 0);
        }

        layoutPushAlarm = findViewById(R.id.layoutPushAlarm);
        btnOnOff = findViewById(R.id.btnOnOff);
        chkSmartAlarm = findViewById(R.id.chkSmartAlarm);
        chkFilterAlarm = findViewById(R.id.chkFilterAlarm);

        btnOnOff.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (!isFirst) {
                    if (isChecked) {
                        smartAlarm = 1;
                        filterAlarm = 1;
                        sendData(1, 1);
                    } else {
                        smartAlarm = 0;
                        filterAlarm = 0;
                        sendData(0, 0);
                    }
                }
            }
        });

        chkSmartAlarm.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                smartAlarm = isChecked ? 1 : 0;
                if (!isFirst && !isUpdate) {
                    sendData(isChecked ? 1 : 0, filterAlarm);
                }
            }
        });

        chkFilterAlarm.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                filterAlarm = isChecked ? 1 : 0;
                if (!isFirst && !isUpdate) {
                    sendData(smartAlarm, isChecked ? 1 : 0);
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateUI();
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.btnBack:
                onBackPressed();
                break;
        }
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

    private void sendData(final int smart, final int filter) {
        if(smart == 1 || filter == 1){
            pushUse = 1;
        }else  {
            pushUse = 0;
        }

        displayProgress(this, "", "");

        try {
            HttpApi.PostV2ChangePush(CleanVentilationApplication.getInstance().getUserInfo().getId(), gid, pushUse, smart, filter,
                    new Callback() {
                        @Override
                        public void onFailure(Call call, IOException e) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    DismissConnectDialog();
                                }
                            });
                            CommonUtils.showToast(DevicePushSettingActivity.this, getString(R.string.toast_result_can_not_request));
                        }

                        @Override
                        public void onResponse(Call call, Response response) throws IOException {
                            try {
                                String strResponse = response.body().string();

                                if(TextUtils.isEmpty(strResponse)){
                                    CommonUtils.showToast(DevicePushSettingActivity.this, getString(R.string.toast_result_can_not_request));
                                }else {
                                    CommonUtils.customLog(TAG, "device/change-push-receive-setting : " + strResponse, Log.ERROR);

                                    JSONObject json_data = new JSONObject(strResponse);
                                    switch (json_data.getInt("code")){
                                        case HttpApi.RESPONSE_SUCCESS:
                                            updateUI();
                                            break;

                                        default:
                                            CommonUtils.showToast(DevicePushSettingActivity.this, getString(R.string.toast_result_can_not_request));
                                            break;
                                    }
                                }


                            } catch (JSONException e) {
                                e.printStackTrace();
                                CommonUtils.showToast(DevicePushSettingActivity.this, getString(R.string.toast_result_can_not_request));
                            }

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if (IsRunningProgress()) {
                                        DismissConnectDialog();
                                    }
                                }
                            });
                        }
                    });
        } catch (Exception e) {
            e.printStackTrace();
            DismissConnectDialog();
            CommonUtils.showToast(DevicePushSettingActivity.this, getString(R.string.toast_result_can_not_change_device_info));
        }
    }

    private void updateUI() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                isUpdate = true;
                chkSmartAlarm.setChecked(smartAlarm == 1);
                chkFilterAlarm.setChecked(filterAlarm == 1);
                if (smartAlarm == 0 && filterAlarm == 0) {
                    layoutPushAlarm.setVisibility(View.GONE);
                    btnOnOff.setChecked(false);
                } else {
                    layoutPushAlarm.setVisibility(View.VISIBLE);
                    btnOnOff.setChecked(true);
                }
                isUpdate = false;
                isFirst = false;
            }
        });
    }
}