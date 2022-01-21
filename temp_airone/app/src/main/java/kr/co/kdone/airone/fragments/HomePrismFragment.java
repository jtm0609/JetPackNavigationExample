package kr.co.kdone.airone.fragments;

import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import kr.co.kdone.airone.CleanVentilationApplication;
import kr.co.kdone.airone.R;
import kr.co.kdone.airone.activity.info.DeviceDetailActivity;
import kr.co.kdone.airone.activity.info.DeviceHelpActivity;
import kr.co.kdone.airone.activity.main.HomeNotiPopupActivity;
import kr.co.kdone.airone.utils.CommonUtils;

import static kr.co.kdone.airone.utils.CommonUtils.MODE_DEVICE_MONITOR;
import static kr.co.kdone.airone.utils.CommonUtils.getColorIdIdToSensorLevel;
import static kr.co.kdone.airone.utils.CommonUtils.getDrawableIdToMainSensorLevel;
import static kr.co.kdone.airone.utils.CommonUtils.getDrawableIdToMainSensorLevel_Small;
import static kr.co.kdone.airone.utils.CommonUtils.getDrawableIdToMainTotalSensorLevel;
import static kr.co.kdone.airone.utils.CommonUtils.getIconIDWeather;
import static kr.co.kdone.airone.utils.CommonUtils.getStringIDWeather;
import static kr.co.kdone.airone.utils.CommonUtils.getStringIdToSenerLevel;

/**
 * 홈화면의 첫번째 화면(실외/실내정보 표시) - 프리즘 사용자
 */
public class HomePrismFragment extends Fragment implements View.OnClickListener {
    private String TAG = this.getClass().getSimpleName();

    // ikHwang 2019-06-10 오후 8:10 실외 데이터 처리 컴포넌트 선언
    private TextView            txtCurrentLocation;             // 에어 룸콘트롤러 설치 주소 표시 텍스트뷰
    private ImageView           imgInfoAir;                     // 실외 데이터 수집정보 팝업 버튼 이미지뷰
    private ImageView           imgWeather;                     // 실외 날씨 표시 이미지뷰
    private TextView            txtWeather;                     // 실외 날씨 표시 텍스트뷰
    private TextView            txtOutsideTemp;                 // 실외 온도 표시 텍스트뷰
    private TextView            txtOutsideHumidity;             // 실외 습도 표시 텍스트뷰
    private View                outside_pm25;                   // 실외 pm2.5 level 표시뷰
    private TextView            text_outside_pm25;              // 실외 pm2.5 level 표시 텍스트뷰
    private View                outside_pm10;                   // 실외 pm10 level 표시뷰
    private TextView            text_outside_pm10;              // 실외 pm10 level 표시 텍스트뷰

    // ikHwang 2019-06-10 오후 8:18 어에 룸콘트롤러, 에어 모니터 정상등록 및 센싱데이터 없음 레이아웃
    private LinearLayout        layout_sensor_1;                // 센싱정보 표시 레이아웃
    private LinearLayout        layout_sensor_2;                // 센싱정보 표시 레이아웃
    private LinearLayout        layout_sensor_3;                // 센싱정보 표시 레이아웃
    private LinearLayout        layout_sensor_4;                // 센싱정보 표시 레이아웃
    private LinearLayout        layout_sensor_5;                // 센싱정보 표시 레이아웃
    private LinearLayout        layout_sensor_6;                // 센싱정보 표시 레이아웃
//    private LinearLayout        layout_sensor_7;                  // 센싱정보 표시 레이아웃
    private ConstraintLayout    layout_sensor_8;                // 청정환기 동작상태 표시 레이아웃

    private TextView            txtInsideTemp;                  // 실내 온도 표시 텍스트뷰
    private TextView            txtInsideHumidity;              // 실내 습도 표시 텍스트뷰
    private LinearLayout        layout_total;                   // 실내 통합 공기질 정보 레이아웃
    private TextView            text_total_info;                // 실내 통합 공기질 타이틀 텍스트뷰
    private TextView            text_total_value;               // 실내 통합 공기질 수치 표시 텍스트뷰
    private TextView            text_total_level;               // 실내 통합 골기질 level 표시 텍스트뷰

    private LinearLayout        base_pm1;                       // 실내 pm1.0 상태 표시 레이아웃
    private TextView            text_pm1_level;                 // 실내 pm1.0 level 표시 텍스트뷰
    private TextView            text_pm1_value;                 // 실내 pm1.0 수치 표시 텍스트뷰
    private TextView            text_pm1_unit;                  // 실내 pm1.0 단위 표시 텍스트뷰
    private TextView            text_pm1_title;                 // 실내 pm1.0 타이틀 표시 텍스트뷰

    private LinearLayout        base_pm25;                      // 실내 pm2.5 상태 표시 레이아웃
    private TextView            text_pm25_level;                // 실내 pm2.5 level 표시 텍스트뷰
    private TextView            text_pm25_value;                // 실내 pm2.5 수치 표시 텍스트뷰
    private TextView            text_pm25_unit;                 // 실내 pm2.5 단위 표시 텍스트뷰
    private TextView            text_pm25_title;                // 실내 pm2.5 타이틀 표시 텍스트뷰

    private LinearLayout        base_co2;                       // 실내 co2 상태 표시 레이아웃
    private TextView            text_co2_level;                 // 실내 co2 level 표시 텍스트뷰
    private TextView            text_co2_value;                 // 실내 co2 수치 표시 텍스트뷰
    private TextView            text_co2_unit;                  // 실내 co2 단위 표시 텍스트뷰
    private TextView            text_co2_title;                 // 실내 co2 타이틀 표시 텍스트뷰

    private LinearLayout        base_tvoc;                      // 실내 tvoc 상태 표시 레이아웃
    private TextView            text_tvoc_level;                // 실내 tvoc level 표시 텍스트뷰
    private TextView            text_tvoc_value;                // 실내 tvoc 수치 표시 텍스트뷰
    private TextView            text_tvoc_unit;                 // 실내 tvoc 단위 표시 텍스트뷰
    private TextView            text_tvoc_title;                // 실내 tvoc 타이틀 표시 텍스트뷰

//    private RelativeLayout      layout_connect_airmonitor;      // 에어 모니터 연결됨 문구 표시 레이아웃
//    private TextView            text_disconnect_airmonitor;     // 에어 모니터 미연결 문구 표시 레이아웃

    // ikHwang 2019-06-10 오후 8:30 에어 모니터 연결 해제 레이아웃 (2차, 프리즘 사용자)
    private LinearLayout        layout_no_sensor;               // 에어 모니터 연결 해제 레이아웃
    private Button              btn_help_paring;                // 에어 모니터 연결 도움말 버튼

    private AnimationDrawable   frameAnimation;                 // 실내공기질 애니메이션

    private AnimationDrawable   frameAnimationState;            // 청정환기 동작상태 애니메이션
    private ImageView           img_rc_state;                   // 청정환기 동작 상태 이미지
    private TextView            txt_rc_state1;                  // 청정환기 동작 상태 이미지
    private TextView            txt_rc_state2;                  // 청정환기 동작 상태 이미지
    
    private LinearLayout        base_uv;                        // UV 동작상태 표시 레이아웃
    private TextView            text_uv;                        // UV 설정 및 동작 상태

    private TextView            text_airmonitor_not_connect;    // 에어모니터 연결안됨 텍스트뷰

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home_prism, container, false);

        initLayout(view);

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        if (frameAnimation != null) {
            frameAnimation.stop();
            frameAnimation.selectDrawable(0);
        }

        if (frameAnimationState != null) {
            frameAnimationState.stop();
            frameAnimationState.selectDrawable(0);
        }
    }

    /**
     * ikHwang 2019-06-10 오후 8:37 레이아웃 초기화
     * @param v
     */
    private void initLayout(View v){
        // ikHwang 2019-06-10 오후 8:10 실외 데이터 처리 컴포넌트
        txtCurrentLocation              = v.findViewById(R.id.txtCurrentLocation);              // 에어 룸콘트롤러 설치 주소 표시 텍스트뷰
        imgInfoAir                      = v.findViewById(R.id.imgInfoAir);                      // 실외 데이터 수집정보 팝업 버튼 이미지뷰
        imgWeather                      = v.findViewById(R.id.imgWeather);                      // 실외 날씨 표시 이미지뷰
        txtWeather                      = v.findViewById(R.id.txtWeather);                      // 실외 날씨 표시 텍스트뷰
        txtOutsideTemp                  = v.findViewById(R.id.txtOutsideTemp);                  // 실외 온도 표시 텍스트뷰
        txtOutsideHumidity              = v.findViewById(R.id.txtOutsideHumidity);              // 실외 습도 표시 텍스트뷰
        outside_pm25                    = v.findViewById(R.id.outside_pm25);                    // 실외 pm2.5 level 표시뷰
        text_outside_pm25               = v.findViewById(R.id.text_outside_pm25);               // 실외 pm2.5 level 표시 텍스트뷰
        outside_pm10                    = v.findViewById(R.id.outside_pm10);                    // 실외 pm10 level 표시뷰
        text_outside_pm10               = v.findViewById(R.id.text_outside_pm10);               // 실외 pm10 level 표시 텍스트뷰

        // ikHwang 2019-06-10 오후 8:18 어에 룸콘트롤러, 에어 모니터 정상등록 및 센싱데이터 없음 레이아웃
        layout_sensor_1                 = v.findViewById(R.id.layout_sensor_1);                   // 센싱정보 표시 레이아웃
        layout_sensor_2                 = v.findViewById(R.id.layout_sensor_2);                   // 센싱정보 표시 레이아웃
        layout_sensor_3                 = v.findViewById(R.id.layout_sensor_3);                   // 센싱정보 표시 레이아웃
        layout_sensor_4                 = v.findViewById(R.id.layout_sensor_4);                   // 센싱정보 표시 레이아웃
        layout_sensor_5                 = v.findViewById(R.id.layout_sensor_5);                   // 센싱정보 표시 레이아웃
        layout_sensor_6                 = v.findViewById(R.id.layout_sensor_6);                   // 센싱정보 표시 레이아웃
//        layout_sensor_7                 = v.findViewById(R.id.layout_sensor_7);                   // 센싱정보 표시 레이아웃
        layout_sensor_8                 = v.findViewById(R.id.layout_sensor_8);                   // 청정환기 동작상태 표시 레이아웃

        txtInsideTemp                   = v.findViewById(R.id.txtInsideTemp);                   // 실내 온도 표시 텍스트뷰
        txtInsideHumidity               = v.findViewById(R.id.txtInsideHumidity);               // 실내 습도 표시 텍스트뷰
        layout_total                    = v.findViewById(R.id.layout_total);                    // 실내 통합 공기질 정보 레이아웃
        text_total_info                 = v.findViewById(R.id.text_total_info);                 // 실내 통합 공기질 타이틀 텍스트뷰
        text_total_value                = v.findViewById(R.id.text_total_value);                // 실내 통합 공기질 수치 표시 텍스트뷰
        text_total_level                = v.findViewById(R.id.text_total_level);                // 실내 통합 골기질 level 표시 텍스트뷰

        base_pm1                        = v.findViewById(R.id.base_pm1);                        // 실내 pm1.0 표시 레이아웃
        text_pm1_level                  = v.findViewById(R.id.text_pm1_level);                  // 실내 pm1.0 level 표시 텍스트뷰
        text_pm1_value                  = v.findViewById(R.id.text_pm1_value);                  // 실내 pm1.0 수치 표시 텍스트뷰
        text_pm1_unit                   = v.findViewById(R.id.text_pm1_unit);                   // 실내 pm1.0 단위 표시 텍스트뷰
        text_pm1_title                  = v.findViewById(R.id.text_pm1_title);                  // 실내 pm1.0 타이틀 표시 텍스트뷰

        base_pm25                       = v.findViewById(R.id.base_pm25);                       // 실내 pm2.5 표시 레이아웃
        text_pm25_level                 = v.findViewById(R.id.text_pm25_level);                 // 실내 pm2.5 level 표시 텍스트뷰
        text_pm25_value                 = v.findViewById(R.id.text_pm25_value);                 // 실내 pm2.5 수치 표시 텍스트뷰
        text_pm25_unit                  = v.findViewById(R.id.text_pm25_unit);                  // 실내 pm2.5 단위 표시 텍스트뷰
        text_pm25_title                 = v.findViewById(R.id.text_pm25_title);                 // 실내 pm2.5 타이틀 표시 텍스트뷰

        base_co2                        = v.findViewById(R.id.base_co2);                        // 실내 co2 표시 레이아웃
        text_co2_level                  = v.findViewById(R.id.text_co2_level);                 // 실내 co2 level 표시 텍스트뷰
        text_co2_value                  = v.findViewById(R.id.text_co2_value);                  // 실내 co2 수치 표시 텍스트뷰
        text_co2_unit                   = v.findViewById(R.id.text_co2_unit);                   // 실내 co2 단위 표시 텍스트뷰
        text_co2_title                  = v.findViewById(R.id.text_co2_title);                  // 실내 co2 타이틀 표시 텍스트뷰

        base_tvoc                       = v.findViewById(R.id.base_tvoc);                       // 실내 tvoc 표시 레이아웃
        text_tvoc_level                 = v.findViewById(R.id.text_tvoc_level);                 // 실내 tvoc level 표시 텍스트뷰
        text_tvoc_value                 = v.findViewById(R.id.text_tvoc_value);                 // 실내 tvoc 수치 표시 텍스트뷰
        text_tvoc_unit                  = v.findViewById(R.id.text_tvoc_unit);                  // 실내 tvoc 단위 표시 텍스트뷰
        text_tvoc_title                 = v.findViewById(R.id.text_tvoc_title);                 // 실내 tvoc 타이틀 표시 텍스트뷰

//        layout_connect_airmonitor       = v.findViewById(R.id.layout_connect_airmonitor);       // 에어 모니터 연결됨 문구 표시 레이아웃
//        text_disconnect_airmonitor      = v.findViewById(R.id.text_disconnect_airmonitor);        // 에어 모니터 미연결 문구 표시 레이아웃

        // ikHwang 2019-06-10 오후 8:30 에어 모니터 연결 해제 레이아웃 (2차, 프리즘 사용자)
        layout_no_sensor                = v.findViewById(R.id.layout_no_sensor);                // 에어 모니터 연결 해제 레이아웃
        btn_help_paring                 = v.findViewById(R.id.btn_help_paring);                 // 에어 모니터 연결 도움말 버튼

        img_rc_state                    = v.findViewById(R.id.img_rc_state);                    // 청정환기 동작 상태 이미지
        txt_rc_state1                   = v.findViewById(R.id.txt_rc_state1);                   // 청정환기 텍스트
        txt_rc_state2                   = v.findViewById(R.id.txt_rc_state2);                   // 꺼짐 켜짐 텍스트

        text_airmonitor_not_connect     = v.findViewById(R.id.text_airmonitor_not_connect);     // 에어모니터 연결안됨 텍스트뷰

        base_uv                         = v.findViewById(R.id.base_uv);                         // UV 동작상태 표시 레이아웃
        text_uv                         = v.findViewById(R.id.text_uv);

        // ikHwang 2019-06-10 오후 8:52 클릭 이벤트 설정
        imgInfoAir.setOnClickListener(this);        // 실외 공기질 데이터 조회 알림 팝업
        imgInfoAir.setOnClickListener(this);        // 실외 공기질 데이터 조회 알림 팝업
        btn_help_paring.setOnClickListener(this);   // 에어 모니터 연결해제 재연결 안내 도움말 버튼 (2차, 프리즘 사용자)
        text_airmonitor_not_connect.setOnClickListener(this); // 에어모니터 연결안됨
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
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.imgInfoAir: // 실외 공기질 데이터 조회 알림 팝업
                HomeNotiPopupActivity homeNotiPopup = new HomeNotiPopupActivity(getContext());
                homeNotiPopup.show();
                break;

            case R.id.btn_help_paring: { // 프리즘 사용자 에어모니터 등록된적 없어서 연결 화면으로 이동
                Intent intent = new Intent(getActivity(), DeviceHelpActivity.class);
                CommonUtils.startActivityAni(getActivity(), intent, false, 1);
            }
                break;

            case R.id.text_airmonitor_not_connect: { // 에어모니터 연결안됨 텍스트뷰
                Intent intent = new Intent(getActivity(), DeviceDetailActivity.class);
                intent.putExtra("mode", MODE_DEVICE_MONITOR);
                CommonUtils.startActivityAni(getActivity(), intent, false, 1);
            }
                break;
        }
    }

    /**
     * ikHwang 2019-06-10 오후 8:45 메인 레이아웃 데이터 표시 처리
     */
    public void drawBottomLayout() {
        /*if(base_uv.getVisibility() == View.GONE){
            base_uv.setVisibility(View.VISIBLE);
        }else{
            base_uv.setVisibility(View.GONE);
        }*/

        // ikHwang 2019-05-22 오전 11:01 홈정보 처리시 index 0에 바로 접근하여 강제종료 방지하기 위해 Array의 사이즈 체크함
        if(CleanVentilationApplication.getInstance().getHomeList().size() <= 0) return;

        // ikHwang 2019-05-22 오전 10:52 실외 정보 데이터 세팅 처리
        if(null != CleanVentilationApplication.getInstance().getHomeList().get(0).getOutSide()){
            // 날씨 아이콘
            imgWeather.setImageResource(getIconIDWeather(CleanVentilationApplication.getInstance().getHomeList().get(0).getOutSide().getIcon()));

            // 날씨 텍스트
            txtWeather.setText(getString(getStringIDWeather(CleanVentilationApplication.getInstance().getHomeList().get(0).getOutSide().getIcon())));

            String area = CleanVentilationApplication.getInstance().getHomeList().get(0).getRoomController().getArea();
            StringBuffer sb = new StringBuffer();
            sb.append(TextUtils.isEmpty(area) ? getString(R.string.fragment_home_first_str_1) : area);
            sb.append(" ");
//            sb.append(CleanVentilationApplication.getInstance().getHomeList().get(0).getOutSide().getUpdateTime());
//            sb.append(" ");
//            sb.append(getString(R.string.HomeFirstFragment_str_1));

            // 주소
            txtCurrentLocation.setText(sb.toString());

            // 실외 온도
            txtOutsideTemp.setText(String.valueOf(Math.round(CleanVentilationApplication.getInstance().getHomeList().get(0).getOutSide().getTemp())));

            // 실외 습도
            txtOutsideHumidity.setText(String.valueOf(Math.round(CleanVentilationApplication.getInstance().getHomeList().get(0).getOutSide().getHumi())));

            // 실외 pm2.5
            int outMicroDustLevel = CleanVentilationApplication.getInstance().getHomeList().get(0).getOutSide().getDust25Sensor();
            outside_pm25.setBackgroundResource(getDrawableIdToMainSensorLevel_Small(outMicroDustLevel));
            text_outside_pm25.setText(getStringIdToSenerLevel(outMicroDustLevel));
            text_outside_pm25.setTextColor(getResources().getColor(getColorIdIdToSensorLevel(outMicroDustLevel)));

            // 실외 pm10
            int outDustLevel = CleanVentilationApplication.getInstance().getHomeList().get(0).getOutSide().getDust10Sensor();
            outside_pm10.setBackgroundResource(getDrawableIdToMainSensorLevel_Small(outDustLevel));
            text_outside_pm10.setText(getStringIdToSenerLevel(outDustLevel));
            text_outside_pm10.setTextColor(getResources().getColor(getColorIdIdToSensorLevel(outDustLevel)));
        }

        // ikHwang 2019-05-24 오전 10:03 홈화면 처리하기위한 메소드 신규 생성으로 추가함
        switch (CleanVentilationApplication.getInstance().getHomeState()){
            case CleanVentilationApplication.STATE_AIR_MONITOR_NO_REGISTER: // 에어 모니터 미등록
                CommonUtils.customLog(TAG, "에어 모니터 등록된적 없음, 최초 등록 필요함", Log.ERROR);

                layout_sensor_1.setVisibility(View.GONE); // 에어 룸콘트롤러, 에어 모니터 정상 연결 및 실내정보 있음 데이터 정상 표시 처리
                layout_sensor_2.setVisibility(View.GONE);
                layout_sensor_3.setVisibility(View.GONE);
                layout_sensor_4.setVisibility(View.GONE);
                layout_sensor_5.setVisibility(View.GONE);
                layout_sensor_6.setVisibility(View.GONE);
//                layout_sensor_7.setVisibility(View.GONE);
                layout_sensor_8.setVisibility(View.VISIBLE);

//                layout_connect_airmonitor.setVisibility(View.GONE);     // 에어 모니터 연결됨 문구 표시 보이게 처리
//                text_disconnect_airmonitor.setVisibility(View.GONE);    // 에어 모니터 미연결 문구 표시 안보이게 처리

                text_pm1_level.setVisibility(View.INVISIBLE);                // 실내 pm1.0 level 표시 텍스트뷰
                text_pm1_value.setVisibility(View.INVISIBLE);                // 실내 pm1.0 수치 표시 텍스트뷰
                text_pm1_unit.setVisibility(View.INVISIBLE);                 // 실내 pm1.0 단위 표시 텍스트뷰
//                text_pm1_title.setVisibility(View.INVISIBLE);                // 실내 pm1.0 타이틀 표시 텍스트뷰

                text_pm25_level.setVisibility(View.INVISIBLE);               // 실내 pm2.5 level 표시 텍스트뷰
                text_pm25_value.setVisibility(View.INVISIBLE);               // 실내 pm2.5 수치 표시 텍스트뷰
                text_pm25_unit.setVisibility(View.INVISIBLE);                // 실내 pm2.5 단위 표시 텍스트뷰
//                text_pm25_title.setVisibility(View.INVISIBLE);               // 실내 pm2.5 타이틀 표시 텍스트뷰

                text_co2_level.setVisibility(View.INVISIBLE);                // 실내 co2 level 표시 텍스트뷰
                text_co2_value.setVisibility(View.INVISIBLE);                // 실내 co2 수치 표시 텍스트뷰
                text_co2_unit.setVisibility(View.INVISIBLE);                 // 실내 co2 단위 표시 텍스트뷰
//                text_co2_title.setVisibility(View.INVISIBLE);                // 실내 co2 타이틀 표시 텍스트뷰

                text_tvoc_level.setVisibility(View.INVISIBLE);               // 실내 tvoc level 표시 텍스트뷰
                text_tvoc_value.setVisibility(View.INVISIBLE);               // 실내 tvoc 수치 표시 텍스트뷰
                text_tvoc_unit.setVisibility(View.INVISIBLE);                // 실내 tvoc 단위 표시 텍스트뷰
//                text_tvoc_title.setVisibility(View.INVISIBLE);               // 실내 tvoc 타이틀 표시 텍스트뷰

                layout_no_sensor.setVisibility(View.VISIBLE);           // 에어 모니터 미연결 레이아웃 숨김 처리
                break;

            case CleanVentilationApplication.STATE_AIR_MONITOR_NOT_CONNECTED: // 에어모니터 연결 해제됨
                CommonUtils.customLog(TAG, "2차, 프리즘 사용자 에어 모니터가 등록되어있지 않거나, 연결이 해제된 경우", Log.ERROR);

                layout_sensor_1.setVisibility(View.VISIBLE);              // 에어 룸콘트롤러, 에어 모니터 정상 연결 및 실내정보 있음 데이터 정상 표시 처리
                layout_sensor_2.setVisibility(View.VISIBLE);
                layout_sensor_3.setVisibility(View.VISIBLE);
                layout_sensor_4.setVisibility(View.VISIBLE);
                layout_sensor_5.setVisibility(View.VISIBLE);
                layout_sensor_6.setVisibility(View.VISIBLE);
//                layout_sensor_7.setVisibility(View.VISIBLE);
                layout_sensor_8.setVisibility(View.VISIBLE);

                text_airmonitor_not_connect.setVisibility(View.VISIBLE); // 연결안됨 표시

//                layout_connect_airmonitor.setVisibility(View.GONE);     // 에어 모니터 연결됨 문구 표시 보이게 처리
//                text_disconnect_airmonitor.setVisibility(View.GONE);    // 에어 모니터 미연결 문구 표시 안보이게 처리

                text_pm1_level.setVisibility(View.INVISIBLE);                // 실내 pm1.0 level 표시 텍스트뷰
                text_pm1_value.setVisibility(View.INVISIBLE);                // 실내 pm1.0 수치 표시 텍스트뷰
                text_pm1_unit.setVisibility(View.INVISIBLE);                 // 실내 pm1.0 단위 표시 텍스트뷰
//                text_pm1_title.setVisibility(View.INVISIBLE);                // 실내 pm1.0 타이틀 표시 텍스트뷰

                text_pm25_level.setVisibility(View.INVISIBLE);               // 실내 pm2.5 level 표시 텍스트뷰
                text_pm25_value.setVisibility(View.INVISIBLE);               // 실내 pm2.5 수치 표시 텍스트뷰
                text_pm25_unit.setVisibility(View.INVISIBLE);                // 실내 pm2.5 단위 표시 텍스트뷰
//                text_pm25_title.setVisibility(View.INVISIBLE);               // 실내 pm2.5 타이틀 표시 텍스트뷰

                text_co2_level.setVisibility(View.INVISIBLE);                // 실내 co2 level 표시 텍스트뷰
                text_co2_value.setVisibility(View.INVISIBLE);                // 실내 co2 수치 표시 텍스트뷰
                text_co2_unit.setVisibility(View.INVISIBLE);                 // 실내 co2 단위 표시 텍스트뷰
//                text_co2_title.setVisibility(View.INVISIBLE);                // 실내 co2 타이틀 표시 텍스트뷰

                text_tvoc_level.setVisibility(View.INVISIBLE);               // 실내 tvoc level 표시 텍스트뷰
                text_tvoc_value.setVisibility(View.INVISIBLE);               // 실내 tvoc 수치 표시 텍스트뷰
                text_tvoc_unit.setVisibility(View.INVISIBLE);                // 실내 tvoc 단위 표시 텍스트뷰
//                text_tvoc_title.setVisibility(View.INVISIBLE);               // 실내 tvoc 타이틀 표시 텍스트뷰

                layout_no_sensor.setVisibility(View.GONE);           // 에어 모니터 미연결 레이아웃 숨김 처리
                break;

            case CleanVentilationApplication.STATE_AIR_MONITOR_UNKNOWN_INDISEINFO: // 에어 모니터 센싱 정보 없음
                CommonUtils.customLog(TAG, "에어 모니터가 연결되어 있으나, 실내 정보 없음", Log.ERROR);

                layout_sensor_1.setVisibility(View.VISIBLE);              // 에어 룸콘트롤러, 에어 모니터 정상 연결 및 실내정보 있음 데이터 정상 표시 처리
                layout_sensor_2.setVisibility(View.VISIBLE);
                layout_sensor_3.setVisibility(View.VISIBLE);
                layout_sensor_4.setVisibility(View.VISIBLE);
                layout_sensor_5.setVisibility(View.VISIBLE);
                layout_sensor_6.setVisibility(View.VISIBLE);
//                layout_sensor_7.setVisibility(View.VISIBLE);
                layout_sensor_8.setVisibility(View.VISIBLE);

                text_airmonitor_not_connect.setVisibility(View.GONE); // 연결안됨 표시

//                layout_connect_airmonitor.setVisibility(View.GONE);     // 에어 모니터 연결됨 문구 표시 보이게 처리
//                text_disconnect_airmonitor.setVisibility(View.GONE);    // 에어 모니터 미연결 문구 표시 안보이게 처리

                text_pm1_level.setVisibility(View.INVISIBLE);                // 실내 pm1.0 level 표시 텍스트뷰
                text_pm1_value.setVisibility(View.INVISIBLE);                // 실내 pm1.0 수치 표시 텍스트뷰
                text_pm1_unit.setVisibility(View.INVISIBLE);                 // 실내 pm1.0 단위 표시 텍스트뷰
//                text_pm1_title.setVisibility(View.INVISIBLE);                // 실내 pm1.0 타이틀 표시 텍스트뷰

                text_pm25_level.setVisibility(View.INVISIBLE);               // 실내 pm2.5 level 표시 텍스트뷰
                text_pm25_value.setVisibility(View.INVISIBLE);               // 실내 pm2.5 수치 표시 텍스트뷰
                text_pm25_unit.setVisibility(View.INVISIBLE);                // 실내 pm2.5 단위 표시 텍스트뷰
//                text_pm25_title.setVisibility(View.INVISIBLE);               // 실내 pm2.5 타이틀 표시 텍스트뷰

                text_co2_level.setVisibility(View.INVISIBLE);                // 실내 co2 level 표시 텍스트뷰
                text_co2_value.setVisibility(View.INVISIBLE);                // 실내 co2 수치 표시 텍스트뷰
                text_co2_unit.setVisibility(View.INVISIBLE);                 // 실내 co2 단위 표시 텍스트뷰
//                text_co2_title.setVisibility(View.INVISIBLE);                // 실내 co2 타이틀 표시 텍스트뷰

                text_tvoc_level.setVisibility(View.INVISIBLE);               // 실내 tvoc level 표시 텍스트뷰
                text_tvoc_value.setVisibility(View.INVISIBLE);               // 실내 tvoc 수치 표시 텍스트뷰
                text_tvoc_unit.setVisibility(View.INVISIBLE);                // 실내 tvoc 단위 표시 텍스트뷰
//                text_tvoc_title.setVisibility(View.INVISIBLE);               // 실내 tvoc 타이틀 표시 텍스트뷰

                layout_no_sensor.setVisibility(View.GONE);           // 에어 모니터 미연결 레이아웃 숨김 처리
                break;

            case CleanVentilationApplication.STATE_AIR_MONITOR_HAS_INDISEINFO: // 에어 모니터 센싱 정보 있음 (정상처리 UI 갱신)
                CommonUtils.customLog(TAG, "모니터가 연결되어 있고, 실내 정보 있음, 정상 표시", Log.ERROR);

                layout_sensor_1.setVisibility(View.VISIBLE);              // 에어 룸콘트롤러, 에어 모니터 정상 연결 및 실내정보 있음 데이터 정상 표시 처리
                layout_sensor_2.setVisibility(View.VISIBLE);
                layout_sensor_3.setVisibility(View.VISIBLE);
                layout_sensor_4.setVisibility(View.VISIBLE);
                layout_sensor_5.setVisibility(View.VISIBLE);
                layout_sensor_6.setVisibility(View.VISIBLE);
//                layout_sensor_7.setVisibility(View.VISIBLE);
                layout_sensor_8.setVisibility(View.VISIBLE);

                text_airmonitor_not_connect.setVisibility(View.GONE); // 연결안됨 표시

//                layout_connect_airmonitor.setVisibility(View.VISIBLE);  // 에어 모니터 연결됨 문구 표시 보이게 처리
//                text_disconnect_airmonitor.setVisibility(View.GONE);    // 에어 모니터 미연결 문구 표시 안보이게 처리

                int insidePM1Level = CleanVentilationApplication.getInstance().getHomeList().get(0).getInside().getDustLevel();
                text_pm1_level.setVisibility(insidePM1Level == 0 ? View.GONE: View.VISIBLE);             // 실내 pm1.0 level 표시 텍스트뷰
                text_pm1_value.setVisibility(insidePM1Level == 0 ? View.GONE: View.VISIBLE);             // 실내 pm1.0 수치 표시 텍스트뷰
                text_pm1_unit.setVisibility(insidePM1Level == 0 ? View.GONE: View.VISIBLE);              // 실내 pm1.0 단위 표시 텍스트뷰
//                text_pm1_title.setVisibility(View.VISIBLE);             // 실내 pm1.0 타이틀 표시 텍스트뷰

                int insidePM25Level = CleanVentilationApplication.getInstance().getHomeList().get(0).getInside().getDustLevel();
                text_pm25_level.setVisibility(insidePM25Level == 0 ? View.GONE: View.VISIBLE);            // 실내 pm2.5 level 표시 텍스트뷰
                text_pm25_value.setVisibility(insidePM25Level == 0 ? View.GONE: View.VISIBLE);            // 실내 pm2.5 수치 표시 텍스트뷰
                text_pm25_unit.setVisibility(insidePM25Level == 0 ? View.GONE: View.VISIBLE);             // 실내 pm2.5 단위 표시 텍스트뷰
//                text_pm25_title.setVisibility(View.VISIBLE);            // 실내 pm2.5 타이틀 표시 텍스트뷰

                int insideCO2Level = CleanVentilationApplication.getInstance().getHomeList().get(0).getInside().getCo2Level();
                text_co2_level.setVisibility(insideCO2Level == 0 ? View.GONE: View.VISIBLE);             // 실내 co2 level 표시 텍스트뷰
                text_co2_value.setVisibility(insideCO2Level == 0 ? View.GONE: View.VISIBLE);             // 실내 co2 수치 표시 텍스트뷰
                text_co2_unit.setVisibility(insideCO2Level == 0 ? View.GONE: View.VISIBLE);              // 실내 co2 단위 표시 텍스트뷰
//                text_co2_title.setVisibility(View.VISIBLE);             // 실내 co2 타이틀 표시 텍스트뷰

                int insideTVOCLevel = CleanVentilationApplication.getInstance().getHomeList().get(0).getInside().getTvocLevel();
                text_tvoc_level.setVisibility(insideTVOCLevel == 0 ? View.GONE: View.VISIBLE);            // 실내 tvoc level 표시 텍스트뷰
                text_tvoc_value.setVisibility(insideTVOCLevel == 0 ? View.GONE: View.VISIBLE);            // 실내 tvoc 수치 표시 텍스트뷰
                text_tvoc_unit.setVisibility(insideTVOCLevel == 0 ? View.GONE: View.VISIBLE);             // 실내 tvoc 단위 표시 텍스트뷰
//                text_tvoc_title.setVisibility(View.VISIBLE);            // 실내 tvoc 타이틀 표시 텍스트뷰

                layout_no_sensor.setVisibility(View.GONE);              // 에어 모니터 미연결 레이아웃 숨김 처리

                // 실내 온도 표시, 반올림
                txtInsideTemp.setText(String.valueOf(Math.round(CleanVentilationApplication.getInstance().getHomeList().get(0).getInside().getInsideHeat())));

                // 실내 습도 표시, 반올림
                txtInsideHumidity.setText(String.valueOf(Math.round(CleanVentilationApplication.getInstance().getHomeList().get(0).getInside().getInsideHum())));
                break;

            default: // 룸콘 미등록 및 미연결 처리
                text_airmonitor_not_connect.setVisibility(View.VISIBLE); // 연결안됨 표시
                break;
        }

        /*// ikHwang 2019-10-14 오후 1:27  청정환기 가동상태 처리
        if(0 == CleanVentilationApplication.getInstance().getHomeList().get(0).getRoomController().getState()){
            img_rc_state.setImageResource(R.drawable.main_img_fan_off);
            txt_rc_state1.setTextColor(getResources().getColor(R.color.control_off_normal));
            txt_rc_state2.setTextColor(getResources().getColor(R.color.control_off_border));
            txt_rc_state2.setText(getString(R.string.HomeFirstFragment_str_2));
        }*/

        // ikHwang 2019-08-30 오전 9:34 청정환기 가동상태 처리
        if(null != CleanVentilationApplication.getInstance().getHomeList().get(0).getWidgetRCMode()){
            if(2 == CleanVentilationApplication.getInstance().getHomeList().get(0).getWidgetRCMode().getPower()){ // ON
                img_rc_state.setImageResource(R.drawable.animation_fan);
                txt_rc_state1.setTextColor(getResources().getColor(R.color.main_power_on));
                txt_rc_state1.setText(getString(R.string.HomeFirstFragment_str_4));
                /*txt_rc_state2.setTextColor(getResources().getColor(R.color.main_power_on));
                txt_rc_state2.setText(getString(R.string.HomeFirstFragment_str_4));*/

                try {
                    frameAnimationState = (AnimationDrawable) img_rc_state.getDrawable();
                    if(!frameAnimationState.isRunning()){
                        img_rc_state.post(new Runnable() {
                            @Override
                            public void run() {
                                frameAnimationState.start();
                            }
                        });
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
            }else{ // OFF
                img_rc_state.setImageResource(R.drawable.main_img_fan_off);
                txt_rc_state1.setTextColor(getResources().getColor(R.color.main_power_off));
                txt_rc_state1.setText(getString(R.string.HomeFirstFragment_str_3));
                /*txt_rc_state2.setTextColor(getResources().getColor(R.color.main_power_off));
                txt_rc_state2.setText(getString(R.string.HomeFirstFragment_str_3));*/
            }
        }else{
            img_rc_state.setImageResource(R.drawable.main_img_fan_off);
            txt_rc_state1.setTextColor(getResources().getColor(R.color.main_power_off));
            txt_rc_state1.setText(getString(R.string.HomeFirstFragment_str_3));
            /*txt_rc_state2.setTextColor(getResources().getColor(R.color.main_power_off));
            txt_rc_state2.setText(getString(R.string.HomeFirstFragment_str_3));*/
        }

        if(null == CleanVentilationApplication.getInstance().getHomeList().get(0).getWidgetRCMode() || 0 == CleanVentilationApplication.getInstance().getHomeList().get(0).getWidgetRCMode().getUvLedMode()){
            base_uv.setVisibility(View.GONE);
        }else{
            base_uv.setVisibility(View.VISIBLE);

            if(2 == CleanVentilationApplication.getInstance().getHomeList().get(0).getWidgetRCMode().getUvLedMode()){
                text_uv.setBackgroundResource(R.drawable.bg_uv_on);

                if(2 == CleanVentilationApplication.getInstance().getHomeList().get(0).getWidgetRCMode().getUvLedState()){
                    text_uv.setText(getString(R.string.HomeFirstFragment_str_7));
                }else{
                    text_uv.setText(getString(R.string.HomeFirstFragment_str_8));
                }
            }else{
                text_uv.setBackgroundResource(R.drawable.bg_uv_off);
                text_uv.setText(getString(R.string.HomeFirstFragment_str_9));
            }
        }

        // ikHwang 2019-06-10 오후 9:20 // 1,2차 프리즘 에어 모니터 센싱 정보 표시
        if (layout_sensor_1.getVisibility() == View.VISIBLE) {
            if(null != CleanVentilationApplication.getInstance().getHomeList().get(0).getInside()){
                int insideTotalLevel = CleanVentilationApplication.getInstance().getHomeList().get(0).getInside().getTotalLevel();
                layout_total.setBackgroundResource(getDrawableIdToMainTotalSensorLevel(insideTotalLevel));

                if(insideTotalLevel > 0){
                    text_total_info.setVisibility(View.VISIBLE);
                    text_total_value.setVisibility(View.VISIBLE);

                    text_total_value.setText(String.valueOf(Math.round(CleanVentilationApplication.getInstance().getHomeList().get(0).getInside().getTotalValue())));
                    text_total_level.setText(getStringIdToSenerLevel(insideTotalLevel));

                    try {
                        frameAnimation = (AnimationDrawable) layout_total.getBackground();
                        layout_total.post(new Runnable() {
                            @Override
                            public void run() {
                                frameAnimation.start();
                            }
                        });
                    }catch (Exception e){
                        e.printStackTrace();

                        text_total_info.setVisibility(View.GONE);
                        text_total_value.setVisibility(View.GONE);
                        text_total_level.setText(getString(R.string.fragment_home_first_str_4));
                    }
                }else{
                    text_total_info.setVisibility(View.GONE);
                    text_total_value.setVisibility(View.GONE);
                    text_total_level.setText(getString(R.string.fragment_home_first_str_4));
                }

                // 실내 pm1.0 센서
                int insidePM1Level = CleanVentilationApplication.getInstance().getHomeList().get(0).getInside().getDust1Level();
                base_pm1.setBackgroundResource(getDrawableIdToMainSensorLevel(insidePM1Level));

                if(insidePM1Level > 0){
                    text_pm1_level.setVisibility(View.VISIBLE);                // 실내 pm1.0 level 표시 텍스트뷰
                    text_pm1_value.setVisibility(View.VISIBLE);                // 실내 pm1.0 수치 표시 텍스트뷰
                    text_pm1_unit.setVisibility(View.VISIBLE);                 // 실내 pm1.0 단위 표시 텍스트뷰

                    text_pm1_level.setText(getStringIdToSenerLevel(insidePM1Level));
//                    text_pm1_level.setTextColor(getResources().getColor(getColorIdIdToSensorLevel(insidePM1Level)));
                    text_pm1_value.setText(String.valueOf(Math.round(CleanVentilationApplication.getInstance().getHomeList().get(0).getInside().getDust1Value())));
                }else{
                    text_pm1_level.setVisibility(View.INVISIBLE);                // 실내 pm1.0 level 표시 텍스트뷰
                    text_pm1_value.setVisibility(View.INVISIBLE);                // 실내 pm1.0 수치 표시 텍스트뷰
                    text_pm1_unit.setVisibility(View.INVISIBLE);                 // 실내 pm1.0 단위 표시 텍스트뷰
                }

                // 실내 pm2.5 센서
                int insidePM25Level = CleanVentilationApplication.getInstance().getHomeList().get(0).getInside().getDustLevel();
                base_pm25.setBackgroundResource(getDrawableIdToMainSensorLevel(insidePM25Level));

                if(insidePM25Level > 0){
                    text_pm25_level.setVisibility(View.VISIBLE);               // 실내 pm2.5 level 표시 텍스트뷰
                    text_pm25_value.setVisibility(View.VISIBLE);               // 실내 pm2.5 수치 표시 텍스트뷰
                    text_pm25_unit.setVisibility(View.VISIBLE);                // 실내 pm2.5 단위 표시 텍스트뷰

                    text_pm25_level.setText(getStringIdToSenerLevel(insidePM25Level));
//                    text_pm25_level.setTextColor(getResources().getColor(getColorIdIdToSensorLevel(insidePM25Level)));
                    text_pm25_value.setText(String.valueOf(Math.round(CleanVentilationApplication.getInstance().getHomeList().get(0).getInside().getDustValue())));
                }else{
                    text_pm25_level.setVisibility(View.INVISIBLE);               // 실내 pm2.5 level 표시 텍스트뷰
                    text_pm25_value.setVisibility(View.INVISIBLE);               // 실내 pm2.5 수치 표시 텍스트뷰
                    text_pm25_unit.setVisibility(View.INVISIBLE);                // 실내 pm2.5 단위 표시 텍스트뷰
                }


                // 실내 co2 센서
                int insideCO2Level = CleanVentilationApplication.getInstance().getHomeList().get(0).getInside().getCo2Level();
                base_co2.setBackgroundResource(getDrawableIdToMainSensorLevel(insideCO2Level));

                if(insideCO2Level > 0){
                    text_co2_level.setVisibility(View.VISIBLE);                // 실내 co2 level 표시 텍스트뷰
                    text_co2_value.setVisibility(View.VISIBLE);                // 실내 co2 수치 표시 텍스트뷰
                    text_co2_unit.setVisibility(View.VISIBLE);                 // 실내 co2 단위 표시 텍스트뷰

                    text_co2_level.setText(getStringIdToSenerLevel(insideCO2Level));
//                    text_co2_level.setTextColor(getResources().getColor(getColorIdIdToSensorLevel(insideCO2Level)));
                    text_co2_value.setText(String.valueOf(Math.round(CleanVentilationApplication.getInstance().getHomeList().get(0).getInside().getCo2Value())));
                }else{
                    text_co2_level.setVisibility(View.INVISIBLE);                // 실내 co2 level 표시 텍스트뷰
                    text_co2_value.setVisibility(View.INVISIBLE);                // 실내 co2 수치 표시 텍스트뷰
                    text_co2_unit.setVisibility(View.INVISIBLE);                 // 실내 co2 단위 표시 텍스트뷰
                }

                // 실내 TVOC 센서
                int insideTVOCLevel = CleanVentilationApplication.getInstance().getHomeList().get(0).getInside().getTvocLevel();
                base_tvoc.setBackgroundResource(getDrawableIdToMainSensorLevel(insideTVOCLevel));

                if(insideTVOCLevel > 0){
                    text_tvoc_level.setVisibility(View.VISIBLE);               // 실내 tvoc level 표시 텍스트뷰
                    text_tvoc_value.setVisibility(View.VISIBLE);               // 실내 tvoc 수치 표시 텍스트뷰
                    text_tvoc_unit.setVisibility(View.VISIBLE);                // 실내 tvoc 단위 표시 텍스트뷰

                    text_tvoc_level.setText(getStringIdToSenerLevel(insideTVOCLevel));
//                    text_tvoc_level.setTextColor(getResources().getColor(getColorIdIdToSensorLevel(insideTVOCLevel)));
                    text_tvoc_value.setText(String.valueOf(Math.round(CleanVentilationApplication.getInstance().getHomeList().get(0).getInside().getTvocValue())));
                }else{
                    text_tvoc_level.setVisibility(View.INVISIBLE);               // 실내 tvoc level 표시 텍스트뷰
                    text_tvoc_value.setVisibility(View.INVISIBLE);               // 실내 tvoc 수치 표시 텍스트뷰
                    text_tvoc_unit.setVisibility(View.INVISIBLE);                // 실내 tvoc 단위 표시 텍스트뷰
                }
            }else{
                layout_total.setBackgroundResource(getDrawableIdToMainTotalSensorLevel(0));
                text_total_info.setVisibility(View.GONE);
                text_total_value.setVisibility(View.GONE);
                text_total_level.setText(getString(R.string.fragment_home_first_str_4));

                base_pm1.setBackgroundResource(getDrawableIdToMainSensorLevel(0));
                base_pm25.setBackgroundResource(getDrawableIdToMainSensorLevel(0));
                base_co2.setBackgroundResource(getDrawableIdToMainSensorLevel(0));
                base_tvoc.setBackgroundResource(getDrawableIdToMainSensorLevel(0));

                text_pm1_level.setVisibility(View.INVISIBLE);                // 실내 pm1.0 level 표시 텍스트뷰
                text_pm1_value.setVisibility(View.INVISIBLE);                // 실내 pm1.0 수치 표시 텍스트뷰
                text_pm1_unit.setVisibility(View.INVISIBLE);                 // 실내 pm1.0 단위 표시 텍스트뷰

                text_pm25_level.setVisibility(View.INVISIBLE);               // 실내 pm2.5 level 표시 텍스트뷰
                text_pm25_value.setVisibility(View.INVISIBLE);               // 실내 pm2.5 수치 표시 텍스트뷰
                text_pm25_unit.setVisibility(View.INVISIBLE);                // 실내 pm2.5 단위 표시 텍스트뷰

                text_co2_level.setVisibility(View.INVISIBLE);                // 실내 co2 level 표시 텍스트뷰
                text_co2_value.setVisibility(View.INVISIBLE);                // 실내 co2 수치 표시 텍스트뷰
                text_co2_unit.setVisibility(View.INVISIBLE);                 // 실내 co2 단위 표시 텍스트뷰

                text_tvoc_level.setVisibility(View.INVISIBLE);               // 실내 tvoc level 표시 텍스트뷰
                text_tvoc_value.setVisibility(View.INVISIBLE);               // 실내 tvoc 수치 표시 텍스트뷰
                text_tvoc_unit.setVisibility(View.INVISIBLE);                // 실내 tvoc 단위 표시 텍스트뷰

                // 실내 온도 표시
                txtInsideTemp.setText("");
                // 실내 습도 표시, 반올림
                txtInsideHumidity.setText("");
            }
        }
    }
}