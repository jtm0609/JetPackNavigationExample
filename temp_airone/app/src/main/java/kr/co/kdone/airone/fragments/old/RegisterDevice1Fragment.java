package kr.co.kdone.airone.fragments.old;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
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
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import kr.co.kdone.airone.R;
import kr.co.kdone.airone.activity.PopupActivity;
import kr.co.kdone.airone.activity.register.RegisterDevice2Activity;

import static kr.co.kdone.airone.utils.CommonUtils.MODE_DEVICE_MONITOR;
import static kr.co.kdone.airone.utils.CommonUtils.MODE_DEVICE_ROOM_CON;
import static kr.co.kdone.airone.utils.CommonUtils.RESULT_GPS_OFF;
import static kr.co.kdone.airone.utils.CommonUtils.RESULT_PERMISSION_CHECK;
import static kr.co.kdone.airone.utils.CommonUtils.RESULT_PERMISSION_SYSTEM;

public class RegisterDevice1Fragment extends Fragment implements View.OnClickListener {

    private final String TAG = "RegisterDevice1Fragment";
    private RegisterDevice2Activity act;
    private ScrollView layoutScroll;
    private TextView tvTitle;
    private LinearLayout layoutRoomCon, layoutMonitor;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        act = (RegisterDevice2Activity) getActivity();
        View view = inflater.inflate(R.layout.fragment_register_device1, container, false);

        layoutScroll = view.findViewById(R.id.layoutScroll);
        layoutRoomCon = view.findViewById(R.id.layoutRoomCon);
        layoutMonitor = view.findViewById(R.id.layoutMonitor);
        tvTitle = view.findViewById(R.id.device1_title);
        view.findViewById(R.id.layoutOK).setOnClickListener(this);

        int device = act.subMode;
        if (device == MODE_DEVICE_ROOM_CON) {
            layoutRoomCon.setVisibility(View.VISIBLE);
            layoutMonitor.setVisibility(View.GONE);
            tvTitle.setText(getString(R.string.fragment_register_device1_title));
        } else if (device == MODE_DEVICE_MONITOR) {
            layoutRoomCon.setVisibility(View.GONE);
            layoutMonitor.setVisibility(View.VISIBLE);
            tvTitle.setText(getString(R.string.fragment_register_device1_monitor_title));
        }
        return view;
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
            case R.id.layoutOK: {
                if(checkGPSService()){
                    act.moveRegisterPage(1);
                }
                break;
            }
        }
    }

    public void setScrollTop() {
        layoutScroll.fullScroll(ScrollView.FOCUS_UP);
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
