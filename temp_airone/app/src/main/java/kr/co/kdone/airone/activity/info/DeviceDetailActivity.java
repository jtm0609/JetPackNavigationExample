package kr.co.kdone.airone.activity.info;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import kr.co.kdone.airone.CleanVentilationApplication;
import kr.co.kdone.airone.R;
import kr.co.kdone.airone.activity.register.RegisterDevice2Activity;
import kr.co.kdone.airone.activity.register.prism.RegisterPrismDeviceActivity;
import kr.co.kdone.airone.utils.SharedPrefUtil;

import static kr.co.kdone.airone.utils.CommonUtils.MODE_CHANGE_AP;
import static kr.co.kdone.airone.utils.CommonUtils.MODE_DEVICE_ONLY_REG;
import static kr.co.kdone.airone.utils.CommonUtils.MODE_NONE;
import static kr.co.kdone.airone.utils.CommonUtils.RESULT_CHANGE_AP_COMFIRM;
import static kr.co.kdone.airone.utils.CommonUtils.RESULT_REGISTER_DEVICE;

/**
 * ikHwang 2019-06-04 오전 8:42 룸콘 연결해제 상세보기 (재등록 및 재연결 화면)
 * ikHwang 2020-09-07 오전 10:06 에어모니터 연결해제 상세보기 (룸콘, 에어모니터 처리 분리)
 */
public class DeviceDetailActivity extends AppCompatActivity implements View.OnClickListener {
    private final String TAG = getClass().getSimpleName();

    private Activity act;
    private int mode;

    /*private ImageView img_room_con1;
    private ImageView img_room_con2;*/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_detail);
        act = this;

        mode = getIntent().getIntExtra("mode", MODE_NONE);

        //수정 - jh.j V2A 2차 사용자 적용.

        int pcd = CleanVentilationApplication.getInstance().getRoomControllerDevicePCD();
        switch (pcd){
            case 1:
                findViewById(R.id.layoutMonitorDetail).setVisibility(View.VISIBLE);
                findViewById(R.id.layoutMonitorDetail2).setVisibility(View.GONE);
                findViewById(R.id.layoutMonitorDetail3).setVisibility(View.GONE);
                findViewById(R.id.layoutMonitorDetail4).setVisibility(View.GONE);
                break;

            case 3:
                findViewById(R.id.layoutMonitorDetail).setVisibility(View.GONE);
                findViewById(R.id.layoutMonitorDetail2).setVisibility(View.VISIBLE);
                findViewById(R.id.layoutMonitorDetail3).setVisibility(View.GONE);
                findViewById(R.id.layoutMonitorDetail4).setVisibility(View.GONE);
                break;

            case 9:
                findViewById(R.id.layoutMonitorDetail).setVisibility(View.GONE);
                findViewById(R.id.layoutMonitorDetail2).setVisibility(View.GONE);
                findViewById(R.id.layoutMonitorDetail3).setVisibility(View.GONE);
                findViewById(R.id.layoutMonitorDetail4).setVisibility(View.VISIBLE);
                break;

            default:
                findViewById(R.id.layoutMonitorDetail).setVisibility(View.GONE);
                findViewById(R.id.layoutMonitorDetail2).setVisibility(View.GONE);
                findViewById(R.id.layoutMonitorDetail3).setVisibility(View.VISIBLE);
                findViewById(R.id.layoutMonitorDetail4).setVisibility(View.GONE);
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            setResult(resultCode);
            finish();
        }
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(0, R.anim.slide_up_out);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.btnBack:
                finish();
                break;

            case R.id.layoutMonitorWifi:{
                Intent intent = null;

                if(!CleanVentilationApplication.getInstance().isIsOldUser()) {
                    intent = new Intent(act, RegisterPrismDeviceActivity.class);
                }else{ // 1,2차 사용자
                    intent = new Intent(act, RegisterDevice2Activity.class);

                }

                intent.putExtra("mode", MODE_CHANGE_AP);
                intent.putExtra("sub_mode", mode);
                startActivityForResult(intent, RESULT_CHANGE_AP_COMFIRM);
                overridePendingTransition(R.anim.slide_left_in, R.anim.slide_none);
                break;
            }

            case R.id.layoutMonitorConnect:{ //룸콘트롤러 재등록
                Intent intent = null;

                if(!CleanVentilationApplication.getInstance().isIsOldUser()) {
                    intent = new Intent(act, RegisterPrismDeviceActivity.class);
                }else{ // 1,2차 사용자
                    intent = new Intent(act, RegisterDevice2Activity.class);
                }

                intent.putExtra("mode", MODE_DEVICE_ONLY_REG);
                intent.putExtra("sub_mode", mode);
                intent.putExtra("userID", SharedPrefUtil.getString(SharedPrefUtil.USER_ID, ""));
                intent.putExtra("password", SharedPrefUtil.getString(SharedPrefUtil.USER_PASS, ""));
                intent.putExtra("name", CleanVentilationApplication.getInstance().getUserInfo().getName());
                startActivityForResult(intent, RESULT_REGISTER_DEVICE);
                overridePendingTransition(R.anim.slide_left_in, R.anim.slide_none);
                break;
            }

            case R.id.txtCsCenter: {
                Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:1588-1144"));
                startActivity(intent);
                overridePendingTransition(R.anim.slide_down_in, R.anim.slide_none);
                break;
            }

            //V2A 2차 사용자용.
            case R.id.layoutMonitorWifi2 :
            case R.id.layoutMonitorConnect2 :
            case R.id.layoutMonitorWifi3 :
            case R.id.layoutMonitorConnect3 :
            case R.id.layoutMonitorWifi4 :
            case R.id.layoutMonitorConnect4 :{
                Intent intent = new Intent(act, DeviceHelpActivity.class);
                startActivityForResult(intent, RESULT_REGISTER_DEVICE);
                overridePendingTransition(R.anim.slide_down_in, R.anim.slide_none);
                break;
            }
        }
    }
}
