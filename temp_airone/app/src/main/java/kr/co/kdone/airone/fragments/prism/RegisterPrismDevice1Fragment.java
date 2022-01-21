package kr.co.kdone.airone.fragments.prism;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.PorterDuff;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.core.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import kr.co.kdone.airone.R;
import kr.co.kdone.airone.activity.PopupActivity;
import kr.co.kdone.airone.activity.register.prism.RegisterPrismDeviceActivity;

import static kr.co.kdone.airone.utils.CommonUtils.RESULT_GPS_OFF;
import static kr.co.kdone.airone.utils.CommonUtils.RESULT_PERMISSION_CHECK;
import static kr.co.kdone.airone.utils.CommonUtils.RESULT_PERMISSION_SYSTEM;

/**
 * ikHwang 2019-06-05 오전 8:55 프리즘 모델 BLE 연동 1페이지 (룸콘 블루투스 설정안내 화면)
 */
public class RegisterPrismDevice1Fragment extends Fragment implements View.OnClickListener {
    private final String TAG = getClass().getSimpleName();

    private RegisterPrismDeviceActivity act;    // 프래그먼트 부모 액티비티

    private LinearLayout layoutOK;  // 에어 룸콘트롤러 찾기 버튼
    private ImageView   img_rc_1;   // 룸콘 연동 이미지
    private ImageView   img_rc_2;   // 룸콘 연동 이미지
    private ImageView   img_rc_3;   //  룸콘 연동 이미지

    private ImageView   imageView3;
    private ProgressBar progressBar3;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_register_prism_device1, container, false);

        act = (RegisterPrismDeviceActivity) getActivity();

        initLayout(view);

        if(act.devType == 1){
            // 룸콘 설정 안내 이미지 제습청정 룸콘 이미지로 변경
            img_rc_1.setImageResource(R.drawable.img_room_20l_02);
        }else if(act.devType == 2){
            // 룸콘 설정 안내 이미지 대용량 룸콘 이미지로 변경
            img_rc_1.setImageResource(R.drawable.img_room_21d_02);
            img_rc_2.setImageResource(R.drawable.img_room_21d_03);
            img_rc_3.setImageResource(R.drawable.img_room_21d_04);
        }
        // 블루투스 매니저  초기화
//        act.initBlueTooth();

        return view;
    }

    /**
     * ikHwang 2019-06-05 오후 5:58 레이아웃 초기화
     * @param v
     */
    private void initLayout(View v){
        layoutOK = v.findViewById(R.id.layoutOK);
        img_rc_1 = v.findViewById(R.id.img_rc_1);
        img_rc_2 = v.findViewById(R.id.img_rc_2);
        img_rc_3 = v.findViewById(R.id.img_rc_3);

        imageView3 = v.findViewById(R.id.imageView3);
        progressBar3 = v.findViewById(R.id.progressBar3);

        progressBar3.setIndeterminate(true);
        progressBar3.getIndeterminateDrawable().setColorFilter(getResources().getColor(R.color.color_white), PorterDuff.Mode.MULTIPLY);

        layoutOK.setOnClickListener(this);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == RESULT_GPS_OFF){
            if(resultCode == Activity.RESULT_OK ){
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                intent.addCategory(Intent.CATEGORY_DEFAULT);
                startActivityForResult(intent, 1001);
            }else{
                checkGPSService();
            }
        }else if(requestCode == 1001){
            checkGPSService();
        }else if(requestCode == RESULT_PERMISSION_CHECK){
            if(resultCode == Activity.RESULT_OK ){
                startActivityForResult(new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).setData(Uri.parse("package:" + act.getPackageName())), 1002);
            }
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.layoutOK: { // 에어 룸콘트롤러 찾기
                if(checkGPSService() && (imageView3.getVisibility() == View.VISIBLE)){
                    // 블루투스 매니저  초기화
                    act.initBlueTooth();
                    act.moveToPage(1);
                }
                break;
            }
        }
    }

    public void setLoadingUI(boolean isLoading){
        if(isLoading){
            imageView3.setVisibility(View.GONE);
            progressBar3.setVisibility(View.VISIBLE);
        }else{
            imageView3.setVisibility(View.VISIBLE);
            progressBar3.setVisibility(View.GONE);
        }
    }

    // ikHwang 2019.03.22 안드로이드 8.1 이상부터 wifi ssid를 가져오기 위해서 GPS를 ON시켜야함
    // OFF시 GPS 설정 페이지로 이동하도록 처리
    public boolean checkGPSService() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int permissionCheck1 = ContextCompat.checkSelfPermission(act, Manifest.permission.ACCESS_FINE_LOCATION);

            if (permissionCheck1 == PackageManager.PERMISSION_DENIED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(act, Manifest.permission.ACCESS_FINE_LOCATION)){
                    Intent intent = new Intent(act, PopupActivity.class);
                    intent.putExtra(PopupActivity.POPUP_MODE, PopupActivity.MODE_NORMAL);
                    intent.putExtra(PopupActivity.POPUP_CONFIRM_BUTTON_TEXT, getString(R.string.ok));
                    intent.putExtra(PopupActivity.POPUP_BODY_TEXT, getString(R.string.permission_off));
                    startActivityForResult(intent, RESULT_PERMISSION_CHECK);
                    act.overridePendingTransition(0,0);
                    return false;
                }else{
                    ActivityCompat.requestPermissions(act, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, RESULT_PERMISSION_SYSTEM);
                    return false;
                }
            }else{
                LocationManager manager = (LocationManager) act.getSystemService(Context.LOCATION_SERVICE);
                if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    // GPS OFF 일때 Dialog 표시
                    Intent intent = new Intent(act, PopupActivity.class);
                    intent.putExtra(PopupActivity.POPUP_MODE, PopupActivity.MODE_NORMAL);
                    intent.putExtra(PopupActivity.POPUP_CONFIRM_BUTTON_TEXT, getString(R.string.ok));
                    intent.putExtra(PopupActivity.POPUP_BODY_TEXT, getString(R.string.gps_off));
                    startActivityForResult(intent, RESULT_GPS_OFF);
                    act.overridePendingTransition(0,0);
                    return false;
                }
            }
        }

        act.setGpsLocation();
        return true;
    }
}
