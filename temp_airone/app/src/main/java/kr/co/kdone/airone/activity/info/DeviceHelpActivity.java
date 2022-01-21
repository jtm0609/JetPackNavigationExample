package kr.co.kdone.airone.activity.info;

import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import kr.co.kdone.airone.CleanVentilationApplication;
import kr.co.kdone.airone.R;

/**
 * ikHwang 2019-06-04 오전 8:43 2차 사용자 에어 모니터 재연결 안내 화면
 * 프리즘 모델과 동일하여 모델 타입을 구분하여 1,4 이미지만 변경하여 재사용 하도록 수정 필요
 * ikHwang 2020-09-07 오전 10:34 듀얼링크 및 프리즘 이후 모델 룸콘과 에어모니터 연동 안내 화면
 */
public class DeviceHelpActivity extends AppCompatActivity implements View.OnClickListener {
    private final String TAG = getClass().getSimpleName();

    private ImageView image_device_info1;   // 에어모니터 연결하기 센싱박스 포함된 이미지뷰

    private ImageView image_device_info4;   // 에어모니터 연결하기 센싱박스 포함된 이미지뷰

    private ImageView img_room_con3;
    private ImageView img_room_con4;

    private TextView text_device_info1;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_help);

        initLayout();

        if(!CleanVentilationApplication.getInstance().isIsOldUser()){
            image_device_info1.setImageResource(R.drawable.help_airmonitor_20d_01);

            text_device_info1.setText(R.string.detail_monitor_content21);

            switch (CleanVentilationApplication.getInstance().getRoomControllerDevicePCD()){
                case  7:
                    // 제습청정
                    img_room_con3.setImageResource(R.drawable.help_airmonitor_20l_02);
                    image_device_info4.setImageResource(R.drawable.help_airmonitor_20l_04);
                    break;

                case 9:
                    // 대용량
                    image_device_info1.setImageResource(R.drawable.help_airmonitor_21d_01);
                    img_room_con3.setImageResource(R.drawable.help_airmonitor_21d_02);
                    img_room_con4.setImageResource(R.drawable.help_airmonitor_21d_03);
                    image_device_info4.setImageResource(R.drawable.help_airmonitor_21d_04);
                    break;

                default:
                    // 프리즘
                    img_room_con3.setImageResource(R.drawable.help_airmonitor_20d_02);
                    image_device_info4.setImageResource(R.drawable.help_airmonitor_20d_04);
                    break;
            }
        }
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(0, R.anim.slide_up_out);
    }

    /**
     * ikHwang 2019-06-07 오후 4:49 레이아웃 초기화
     */
    private void initLayout(){
        image_device_info1 = findViewById(R.id.image_device_info1);
        image_device_info4 = findViewById(R.id.image_device_info4);

        img_room_con3 = findViewById(R.id.img_room_con3);
        img_room_con4 = findViewById(R.id.img_room_con4);

        text_device_info1 = findViewById(R.id.text_device_info1);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btnBack :
                finish();
                break;

            case R.id.layoutMonitorWifi2 :
                setResult(RESULT_OK);
                finish();
                break;
        }
    }
}
