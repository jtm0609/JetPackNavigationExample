package kr.co.kdone.airone.activity.info;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import kr.co.kdone.airone.R;

/**
 * KDAirOne_Prism
 * Class: AirMonitorDetailActivity
 * Created by ikHwang
 * Since : 2021-07-08 오전 10:15.
 * Description : 에어모니터 확인하기 (상세보기) 화면
 */
public class AirMonitorDetailActivity extends AppCompatActivity implements View.OnClickListener {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_airmonitor_detail);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btnBack:
                finish();
                break;
        }
    }
}
