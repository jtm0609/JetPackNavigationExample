package kr.co.kdone.airone.activity.register.prism;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import kr.co.kdone.airone.R;
import kr.co.kdone.airone.activity.register.RegisterDevice2Activity;
import kr.co.kdone.airone.utils.CommonUtils;

import static kr.co.kdone.airone.utils.CommonUtils.MODE_NONE;
import static kr.co.kdone.airone.utils.CommonUtils.MODE_SELECT_DEVICE;

/**
 * ikHwang 2019-06-04 오후 2:20 1,2차 사용자와 프리즘 모델 회원가입시 분기처리하기 위해 사용
 */
public class SelectJoinDeviceActivity extends AppCompatActivity {
    private final String TAG = getClass().getSimpleName();

    private final int REQUESTCODE_PERMISSION = 1001;    // 위치 권한 사용동의 요청 코드

    private Context mContext;

    private String userId;      // 회원가입시 아이디
    private String password;    // 회원가입시 비밀번호
    private String name;        // 회원가입시 이름
    private String phone;       // 회원가입시 전화번호
    public int mode;            // 룸콘 등록 or 재등록
    public int subMode;         // 룸콘 or 에어모니터

    private boolean isClickOldDevice    = false;    // 1,2차 사용자 false, 프리즘 사용자 true
    private int devType = 0;    // 프리즘 - 0, 제습청정 -1

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_join_device);

        mContext = SelectJoinDeviceActivity.this;

        if(getIntent().hasExtra("mode")) mode = getIntent().getIntExtra("mode", MODE_NONE);
        if(getIntent().hasExtra("sub_mode"))subMode = getIntent().getIntExtra("sub_mode", MODE_NONE);
        if(getIntent().hasExtra("userID"))  userId = getIntent().getStringExtra("userID");
        if(getIntent().hasExtra("password")) password = getIntent().getStringExtra("password");
        if(getIntent().hasExtra("name")) name = getIntent().getStringExtra("name");
        if(getIntent().hasExtra("phone")) phone = getIntent().getStringExtra("phone");
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case REQUESTCODE_PERMISSION: // 위치 권한동의 성공
                if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    Log.d("tak","Test2");
                    Intent intent;

                    if(MODE_SELECT_DEVICE == mode){
                        intent = new Intent();
                        intent.putExtra("selectDevice", isClickOldDevice);
                        setResult(RESULT_OK, intent);
                        finish();
                    }else{
                        if(isClickOldDevice){ // 프리즘 제품연동 화면 이동
                            intent= new Intent(mContext, RegisterPrismDeviceActivity.class);
                        }else{ // 1,2차 제품연동 화면 이동
                            intent= new Intent(mContext, RegisterDevice2Activity.class);
                        }

                        intent.putExtras(getIntent());
                        CommonUtils.startActivityAni(this, intent, false, 0);
                    }
                }else{ // 위치 권한 동의 실패
                    CommonUtils.showToast(SelectJoinDeviceActivity.this, getString(R.string.SelectJoinDeviceActivity_str_1));
                }
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

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnBack: // 뒤로가기
                onBackPressed();
                break;

            case R.id.layout_old_model:// 1, 2차 사용자 회원가입
                isClickOldDevice = false;

                if(requestPermission()){
                    if(MODE_SELECT_DEVICE == mode){
                        Intent intent = new Intent();
                        intent.putExtra("selectDevice", isClickOldDevice);
                        setResult(RESULT_OK, intent);
                        finish();
                    }else{
                        Intent intent = new Intent(mContext, RegisterDevice2Activity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                        intent.putExtras(getIntent());
                        CommonUtils.startActivityAni(this, intent, false, 0);
                    }
                }
                break;

            case R.id.layout_prism_model: // 프리즘 모델 사용자 회원가입
                isClickOldDevice = true;

                if(requestPermission()) {
                    if(MODE_SELECT_DEVICE == mode){
                        Intent intent = new Intent();
                        intent.putExtra("selectDevice", isClickOldDevice);
                        setResult(RESULT_OK, intent);
                        finish();
                    }else{
                        Intent intent = new Intent(mContext, RegisterPrismDeviceActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                        intent.putExtra("devType", devType);
                        intent.putExtras(getIntent());
                        CommonUtils.startActivityAni(this, intent, false, 0);
                    }
                }
                break;

            case R.id.layout_prism_dehumi: // 제습 청정
                devType = 1;
                isClickOldDevice = true;

                if(requestPermission()) {
                    if(MODE_SELECT_DEVICE == mode){
                        Intent intent = new Intent();
                        intent.putExtra("selectDevice", isClickOldDevice);
                        setResult(RESULT_OK, intent);
                        finish();
                    }else{
                        Intent intent = new Intent(mContext, RegisterPrismDeviceActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                        intent.putExtra("devType", devType);
                        intent.putExtras(getIntent());
                        CommonUtils.startActivityAni(this, intent, false, 0);
                    }
                }
                break;

            case R.id.layout_larg_capacity_model: // 대용량
                devType = 2;
                isClickOldDevice = true;

                if(requestPermission()) {
                    if(MODE_SELECT_DEVICE == mode){
                        Intent intent = new Intent();
                        intent.putExtra("selectDevice", isClickOldDevice);
                        setResult(RESULT_OK, intent);
                        finish();
                    }else{
                        Intent intent = new Intent(mContext, RegisterPrismDeviceActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                        intent.putExtra("devType", devType);
                        intent.putExtras(getIntent());
                        CommonUtils.startActivityAni(this, intent, false, 0);
                    }
                }
                break;

            case R.id.layout_prism_model_20ds: // 대용량
                devType = 3;
                isClickOldDevice = true;

                if(requestPermission()) {
                    if(MODE_SELECT_DEVICE == mode){
                        Intent intent = new Intent();
                        intent.putExtra("selectDevice", isClickOldDevice);
                        setResult(RESULT_OK, intent);
                        finish();
                    }else{
                        Intent intent = new Intent(mContext, RegisterPrismDeviceActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                        intent.putExtra("devType", devType);
                        intent.putExtras(getIntent());
                        CommonUtils.startActivityAni(this, intent, false, 0);
                    }
                }
                break;
        }
    }

    /**
     * ikHwang 2019-06-05 오후 4:44 제품연동 전 위치권한 획득 후 진행 SSID를 조회하기 위해 해당 권한 필요함
     * @return
     */
    private boolean requestPermission() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUESTCODE_PERMISSION);
            return false;
        }
        return true;
    }
}
