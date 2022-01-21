package kr.co.kdone.airone.fragments;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.text.DecimalFormat;

import kr.co.kdone.airone.CleanVentilationApplication;
import kr.co.kdone.airone.R;
import kr.co.kdone.airone.activity.info.DeviceDetailActivity;
import kr.co.kdone.airone.activity.main.HomeInfoPopupActivity;
import kr.co.kdone.airone.activity.main.HomeNotiPopupActivity;
import kr.co.kdone.airone.activity.main.MainActivity;
import kr.co.kdone.airone.activity.register.RegisterDevice2Activity;
import kr.co.kdone.airone.utils.CommonUtils;
import kr.co.kdone.airone.utils.SharedPrefUtil;

import static kr.co.kdone.airone.utils.CommonUtils.MODE_DEVICE_MONITOR;
import static kr.co.kdone.airone.utils.CommonUtils.MODE_DEVICE_ONLY_REG;
import static kr.co.kdone.airone.utils.CommonUtils.RESULT_DETAIL_CHECK;
import static kr.co.kdone.airone.utils.CommonUtils.RESULT_GET_NEW_WEATHER_INFO;
import static kr.co.kdone.airone.utils.CommonUtils.RESULT_REGISTER_DEVICE;
import static kr.co.kdone.airone.utils.CommonUtils.RESULT_REGISTER_SENSORBOX_COMFIRM;
import static kr.co.kdone.airone.utils.CommonUtils.getIconIDWeather;
import static kr.co.kdone.airone.utils.CommonUtils.getStringIDWeather;
import static kr.co.kdone.airone.utils.CommonUtils.setProgressAnimate;

/**
 * 홈화면의 첫번째 화면(실외/실내정보 표시)
 */

public class HomeFirstFragment extends Fragment {
    private String TAG = this.getClass().getSimpleName();

    private int PROGRESSBAR_OUT = 0;
    private int PROGRESSBAR_IN = 1;

    private LinearLayout layoutSensor;
    private LinearLayout layoutNoSensor;
    private LinearLayout layoutDisconnectSensor;
    private LinearLayout layoutSensorNoData;
    private LinearLayout layoutConnectSensor;
    private LinearLayout layoutConnectingSensor;
    private LinearLayout layoutDetailSensor;
    private LinearLayout layoutSensorRefresh;
    private LinearLayout layoutNoSensorSubUser;
    private LinearLayout layoutConnectSensorSubUser;

    private ImageView imgInfoAir;
    private ImageView imgWeather;
    private TextView txtWeather;
    private TextView txtCurrentLocation;
    private TextView txtOutsideTemp;
    private TextView txtOutsideHumidity;

    private TextView txtOutsideDust;
    private TextView txtOutsideMicroDust;

    private ProgressBar progressBarDust;
    private ProgressBar progressBarMicroDust;
    private ProgressBar progressBarInsideMicroDust;
    private ProgressBar progressBarInsideTvoc;
    private ProgressBar progressBarInsideCo2;

    private TextView txtInsideTemp;
    private TextView txtInsideHumidity;
    private TextView txtInsdeCo2;
    private TextView txtInsdeVoc;
    private TextView txtInsdeMicroDust;

    private ImageView           img_rc_state;                   // 청정환기 동작 상태 이미지
    private TextView            txt_rc_state1;                  // 청정환기 동작 상태 이미지
    private TextView            txt_rc_state2;                  // 청정환기 동작 상태 이미지

    private AnimationDrawable   frameAnimation;                 // 실내공기질 애니메이션

    private MainActivity.RefleshListener listerner;

    private HomeInfoPopupActivity dialog;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_home_first, container, false);

        layoutSensor = v.findViewById(R.id.layoutSensor);
        layoutNoSensor = v.findViewById(R.id.layoutNoSensor);
        layoutDisconnectSensor = v.findViewById(R.id.layoutDisconnectSensor);
        layoutSensorNoData = v.findViewById(R.id.layoutSensorNoData);
        layoutConnectSensor = v.findViewById(R.id.layoutConnectSensor);
        layoutConnectingSensor = v.findViewById(R.id.layoutConnectingSensor);
        layoutDetailSensor = v.findViewById(R.id.layoutDetailSensor);
        layoutSensorRefresh = v.findViewById(R.id.layoutSensorRefresh);
        layoutNoSensorSubUser = v.findViewById(R.id.layoutNoSensorSubUser);
        layoutConnectSensorSubUser = v.findViewById(R.id.layoutConnectSensorSubUser);

        imgInfoAir = v.findViewById(R.id.imgInfoAir);
        imgWeather = v.findViewById(R.id.imgWeather);
        txtWeather = v.findViewById(R.id.txtWeather);
        txtCurrentLocation = v.findViewById(R.id.txtCurrentLocation);
        txtOutsideTemp = v.findViewById(R.id.txtOutsideTemp);
        txtOutsideHumidity = v.findViewById(R.id.txtOutsideHumidity);

        txtOutsideDust = v.findViewById(R.id.txtOutsideDust);
        txtOutsideMicroDust = v.findViewById(R.id.txtOutsideMicroDust);

        progressBarDust = v.findViewById(R.id.progressBarDust);
        progressBarMicroDust = v.findViewById(R.id.progressBarMicroDust);
        progressBarInsideMicroDust = v.findViewById(R.id.progressBarInsideMicroDust);
        progressBarInsideTvoc = v.findViewById(R.id.progressBarInsideVoc);
        progressBarInsideCo2 = v.findViewById(R.id.progressBarInsideCo2);

        txtInsideTemp = v.findViewById(R.id.txtInsideTemp);
        txtInsideHumidity = v.findViewById(R.id.txtInsideHumidity);
        txtInsdeCo2 = v.findViewById(R.id.txtInsideCo2);
        txtInsdeVoc = v.findViewById(R.id.txtInsideVoc);
        txtInsdeMicroDust = v.findViewById(R.id.txtInsideMicroDust);

        img_rc_state                    = v.findViewById(R.id.img_rc_state);                    // 청정환기 동작 상태 이미지
        txt_rc_state1                   = v.findViewById(R.id.txt_rc_state1);                   // 청정환기 텍스트
        txt_rc_state2                   = v.findViewById(R.id.txt_rc_state2);                   // 꺼짐 켜짐 텍스트

        return v;
    }


    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        if (frameAnimation != null) {
            frameAnimation.stop();
            frameAnimation.selectDrawable(0);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case RESULT_GET_NEW_WEATHER_INFO:
            case RESULT_DETAIL_CHECK:
            case RESULT_REGISTER_SENSORBOX_COMFIRM:
                if (listerner != null) {
                    listerner.refleshInfo();
                }
                break;
        }
    }

    private boolean requestPermission() {
        if (ActivityCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{
                            android.Manifest.permission.ACCESS_FINE_LOCATION
                    }, 123);
            return false;
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        layoutConnectSensor.performClick();
    }

    public static HomeFirstFragment newInstance(String text) {

        HomeFirstFragment f = new HomeFirstFragment();
        Bundle b = new Bundle();
        b.putString("msg", text);

        f.setArguments(b);

        return f;
    }

    public void setListener(MainActivity.RefleshListener l) {
        listerner = l;
    }

    public void drawBottomLayout() {
        // ikHwang 2019-05-22 오전 11:01 홈정보 처리시 index 0에 바로 접근하여 강제종료 방지하기 위해 Array의 사이즈 체크함
        if(CleanVentilationApplication.getInstance().getHomeList().size() <= 0) return;

        // ikHwang 2019-05-22 오전 10:52 실외 정보 데이터 세팅 처리
        if(null != CleanVentilationApplication.getInstance().getHomeList().get(0).getOutSide()){
            imgWeather.setImageResource(getIconIDWeather(CleanVentilationApplication.getInstance().getHomeList().get(0).getOutSide().getIcon()));
            txtWeather.setText(getString(getStringIDWeather(CleanVentilationApplication.getInstance().getHomeList().get(0).getOutSide().getIcon())));

            StringBuffer sb = new StringBuffer();
            sb.append(CleanVentilationApplication.getInstance().getHomeList().get(0).getRoomController().getArea());
            sb.append(" ");
//            sb.append(CleanVentilationApplication.getInstance().getHomeList().get(0).getOutSide().getUpdateTime());
//            sb.append(" ");
//            sb.append(getString(R.string.poiont));
            txtCurrentLocation.setText(sb.toString());

            txtOutsideTemp.setText(String.valueOf(Math.round(CleanVentilationApplication.getInstance().getHomeList().get(0).getOutSide().getTemp())));
            txtOutsideHumidity.setText(String.valueOf(Math.round(CleanVentilationApplication.getInstance().getHomeList().get(0).getOutSide().getHumi())));

            //외부 Dust
            final int outMicroDustLevel = CleanVentilationApplication.getInstance().getHomeList().get(0).getOutSide().getDust25Sensor();
            progressBarMicroDust.setProgress(outMicroDustLevel);
            setProgressAnimate(getContext(), PROGRESSBAR_OUT, progressBarMicroDust, txtOutsideMicroDust, outMicroDustLevel);

            //외부 Micro
            final int outDustLevel = CleanVentilationApplication.getInstance().getHomeList().get(0).getOutSide().getDust10Sensor();
            progressBarDust.setProgress(outDustLevel);
            setProgressAnimate(getContext(), PROGRESSBAR_OUT, progressBarDust, txtOutsideDust, outDustLevel);

            imgInfoAir.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    HomeNotiPopupActivity homeNotiPopup = new HomeNotiPopupActivity(getContext());
                    homeNotiPopup.show();
                }
            });
        }

        // ikHwang 2019-05-24 오전 10:03 홈화면 처리하기위한 메소드 신규 생성으로 추가함
        switch (CleanVentilationApplication.getInstance().getHomeState()){
            case CleanVentilationApplication.STATE_AIR_MONITOR_NO_REGISTER: // 에어모니터 미등록 상태 에어모니터 추가하기
                // 환영합니다. 에어 모니터를 추가해보세요. ~~  "추가하기"
                CommonUtils.customLog(TAG, "에어 모니터 등록된적 없음, 최초 등록 필요함", Log.ERROR);

                layoutSensor.setVisibility(View.GONE);
                layoutNoSensor.setVisibility(View.VISIBLE);
                layoutSensorNoData.setVisibility(View.GONE);
                layoutDisconnectSensor.setVisibility(View.GONE);
                layoutNoSensorSubUser.setVisibility(View.GONE);
                layoutConnectSensor.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (requestPermission()) {
                            Intent intent = new Intent(getActivity(), RegisterDevice2Activity.class);
                            intent.putExtra("mode", MODE_DEVICE_ONLY_REG);
                            intent.putExtra("sub_mode", MODE_DEVICE_MONITOR);
                            intent.putExtra("userID", SharedPrefUtil.getString(SharedPrefUtil.USER_ID, ""));
                            intent.putExtra("password", SharedPrefUtil.getString(SharedPrefUtil.USER_PASS, ""));
                            intent.putExtra("name", CleanVentilationApplication.getInstance().getUserInfo().getName());
                            startActivityForResult(intent, RESULT_REGISTER_DEVICE);
                        }
                    }
                });
                break;

            case CleanVentilationApplication.STATE_AIR_MONITOR_NOT_CONNECTED: // 에어 모니터 등록 상태, 에어모니터 연결 해제됨
                // 에어 모니터가 연결되지 않았습니다~~~ "상세보기"
                CommonUtils.customLog(TAG, "에어 모니터가 등록된적 있으나, 연결이 해제됨", Log.ERROR);

                layoutNoSensor.setVisibility(View.GONE);
                layoutSensor.setVisibility(View.GONE);
                layoutDisconnectSensor.setVisibility(View.VISIBLE);
                layoutSensorNoData.setVisibility(View.GONE);
                layoutNoSensorSubUser.setVisibility(View.GONE);
                layoutDetailSensor.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(getActivity(), DeviceDetailActivity.class);
                        intent.putExtra("mode", MODE_DEVICE_MONITOR);
                        startActivityForResult(intent, RESULT_DETAIL_CHECK);
                    }
                });
                break;

            case CleanVentilationApplication.STATE_AIR_MONITOR_UNKNOWN_INDISEINFO: // 에어 모니터 연결되어 있으나 실내 정보 없음
                CommonUtils.customLog(TAG, "에어 모니터 연결되어 있으나 실내 정보 없음", Log.ERROR);

                layoutSensor.setVisibility(View.VISIBLE);
                layoutNoSensor.setVisibility(View.GONE);
                layoutDisconnectSensor.setVisibility(View.GONE);
                layoutSensorNoData.setVisibility(View.GONE);
                layoutNoSensorSubUser.setVisibility(View.GONE);

                txtInsideTemp.setText("0"); // 현재 온도, 반올림 처리
                txtInsideHumidity.setText("0"); // 현재 습도, 반올림 처리

                layoutSensor.setOnClickListener(null);

                /*layoutNoSensor.setVisibility(View.GONE);
                layoutSensor.setVisibility(View.GONE);
                layoutDisconnectSensor.setVisibility(View.GONE);
                layoutSensorNoData.setVisibility(View.VISIBLE);
                layoutSensorRefresh.setVisibility(View.VISIBLE);
                layoutSensorNoData.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ((MainActivity) getActivity()).dataRefresh();
                    }
                });*/
                break;

            case CleanVentilationApplication.STATE_AIR_MONITOR_UNKNOWN_2ND: // 2차(듀얼링크) 에어모니터 미등록 및 연결해제 상태
                CommonUtils.customLog(TAG, "2차(듀얼링크) 에어 모니터가 미등록 및 연결해제 상태", Log.ERROR);

                layoutSensor.setVisibility(View.GONE);
                layoutNoSensor.setVisibility(View.GONE);
                layoutSensorNoData.setVisibility(View.GONE);
                layoutDisconnectSensor.setVisibility(View.VISIBLE);
                layoutNoSensorSubUser.setVisibility(View.GONE);
                layoutDetailSensor.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(getActivity(), DeviceDetailActivity.class);
                        intent.putExtra("mode", MODE_DEVICE_MONITOR);
                        startActivityForResult(intent, RESULT_DETAIL_CHECK);
                    }
                });
                break;

            case CleanVentilationApplication.STATE_AIR_MONITOR_HAS_INDISEINFO: // 1,2차 에어 모니터 센싱 정보 있음 (정상처리 UI 갱신)
                CommonUtils.customLog(TAG, "에어 모니터가 연결되어 있고, 실내 정보 있음, 정상 표시", Log.ERROR);

                layoutSensor.setVisibility(View.VISIBLE);
                layoutNoSensor.setVisibility(View.GONE);
                layoutDisconnectSensor.setVisibility(View.GONE);
                layoutSensorNoData.setVisibility(View.GONE);
                layoutNoSensorSubUser.setVisibility(View.GONE);

                DecimalFormat form = new DecimalFormat("#.#");

                txtInsideTemp.setText(form.format(Math.round(CleanVentilationApplication.getInstance().getHomeList().get(0).getInside().getInsideHeat()))); // 현재 온도, 반올림 처리
                txtInsideHumidity.setText(form.format(Math.round(CleanVentilationApplication.getInstance().getHomeList().get(0).getInside().getInsideHum()))); // 현재 습도, 반올림 처리
//                layoutConnectingSensor.setVisibility(View.VISIBLE); // 에어모니터 연결됨 문구 표시

                layoutSensor.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog = new HomeInfoPopupActivity(getActivity());
                        dialog.show();
                    }
                });
                break;

            default: // 룸콘 미등록 및 미연결 처리
                break;
        }

        // ikHwang 2019-08-30 오전 9:34 청정환기 가동상태 처리
        if(null != CleanVentilationApplication.getInstance().getHomeList().get(0).getWidgetRCMode()){
            if(2 == CleanVentilationApplication.getInstance().getHomeList().get(0).getWidgetRCMode().getPower()){ // ON
                img_rc_state.setImageResource(R.drawable.animation_fan);
                txt_rc_state1.setTextColor(getResources().getColor(R.color.control_power_on));
                txt_rc_state2.setTextColor(getResources().getColor(R.color.control_power_on));
                txt_rc_state2.setText(getString(R.string.HomeFirstFragment_str_6));

                try {
                    frameAnimation = (AnimationDrawable) img_rc_state.getDrawable();
                    if(!frameAnimation.isRunning()){
                        img_rc_state.post(new Runnable() {
                            @Override
                            public void run() {
                                frameAnimation.start();
                            }
                        });
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
            }else{ // OFF
                img_rc_state.setImageResource(R.drawable.main_img_fan_off);
                txt_rc_state1.setTextColor(getResources().getColor(R.color.control_off_normal));
                txt_rc_state2.setTextColor(getResources().getColor(R.color.control_off_border));
                txt_rc_state2.setText(getString(R.string.HomeFirstFragment_str_5));
            }
        }else{
            img_rc_state.setImageResource(R.drawable.main_img_fan_off);
            txt_rc_state1.setTextColor(getResources().getColor(R.color.control_off_normal));
            txt_rc_state2.setTextColor(getResources().getColor(R.color.control_off_border));
            txt_rc_state2.setText(getString(R.string.HomeFirstFragment_str_5));
        }

        if (layoutSensor.getVisibility() == View.VISIBLE) {
            if(null != CleanVentilationApplication.getInstance().getHomeList().get(0).getInside()){
                //실내 초미세먼지 센서
                int dustLevel = CleanVentilationApplication.getInstance().getHomeList().get(0).getInside().getDustLevel();
                if (dustLevel > 4) {
                    dustLevel = 0;
                }

                CommonUtils.customLog(TAG, "Dust level : " + dustLevel, Log.ERROR);

                progressBarInsideMicroDust.setProgress(dustLevel);
                setProgressAnimate(getContext(), PROGRESSBAR_IN, progressBarInsideMicroDust, txtInsdeMicroDust, dustLevel);

                //실내 CO2 센서
                int co2Level = CleanVentilationApplication.getInstance().getHomeList().get(0).getInside().getCo2Level();
                if (co2Level > 4) {
                    co2Level = 0;
                }

                CommonUtils.customLog(TAG, "co2 level : " + co2Level, Log.ERROR);

                progressBarInsideCo2.setProgress(co2Level);
                setProgressAnimate(getContext(), PROGRESSBAR_IN, progressBarInsideCo2, txtInsdeCo2, co2Level);

                // 실내 TVOC 센서
                int tvocLevel = CleanVentilationApplication.getInstance().getHomeList().get(0).getInside().getTvocLevel();
                if (tvocLevel > 4) {
                    tvocLevel = 0;
                }

                CommonUtils.customLog(TAG, "tvoc level : " + tvocLevel, Log.ERROR);

                progressBarInsideTvoc.setProgress(tvocLevel);
                setProgressAnimate(getContext(), PROGRESSBAR_IN, progressBarInsideTvoc, txtInsdeVoc, tvocLevel);
            }else{
                layoutSensor.setVisibility(View.VISIBLE);
                layoutNoSensor.setVisibility(View.GONE);
                layoutDisconnectSensor.setVisibility(View.GONE);
                layoutSensorNoData.setVisibility(View.GONE);
                layoutNoSensorSubUser.setVisibility(View.GONE);

                txtInsideTemp.setText("0"); // 현재 온도, 반올림 처리
                txtInsideHumidity.setText("0"); // 현재 습도, 반올림 처리

                layoutSensor.setOnClickListener(null);

                progressBarInsideMicroDust.setProgress(0);
                progressBarInsideCo2.setProgress(0);
                progressBarInsideTvoc.setProgress(0);

                txtInsdeMicroDust.setText(CommonUtils.getStringIdToSensorLevelForProgressBar(0));
                txtInsdeMicroDust.setTextColor(getResources().getColor(CommonUtils.getColorIdIdToSensorLevel(0)));

                txtInsdeCo2.setText(CommonUtils.getStringIdToSensorLevelForProgressBar(0));
                txtInsdeCo2.setTextColor(getResources().getColor(CommonUtils.getColorIdIdToSensorLevel(0)));

                txtInsdeVoc.setText(CommonUtils.getStringIdToSensorLevelForProgressBar(0));
                txtInsdeVoc.setTextColor(getResources().getColor(CommonUtils.getColorIdIdToSensorLevel(0)));
            }
        }

        // ikHwang 2019-05-22 오후 2:50 팝업 표시시 업데이트 하기위해 적용
        if(null != dialog && dialog.isShowing()){
            dialog.updateSensorData();
        }
    }
}
