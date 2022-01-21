package kr.co.kdone.airone.activity.main;

import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.TextView;

import kr.co.kdone.airone.CleanVentilationApplication;
import kr.co.kdone.airone.R;
import kr.co.kdone.airone.utils.CommonUtils;

import static kr.co.kdone.airone.utils.CommonUtils.getColorIdIdToSensorLevel;
import static kr.co.kdone.airone.utils.CommonUtils.getStringIdToSenerLevel;

/**
 * ikHwang 2019-06-04 오전 9:46 메인화면 공기질 상세 보기 팝업
 */
public class  HomeInfoPopupActivity extends Dialog{
    private final String TAG = getClass().getSimpleName();

    private TextView txtOutsidePm25Sensor;
    private TextView txtOutsidePm10Sensor;
    private TextView txtInsidePm25Sensor;
    private TextView txtInsideCo2Sensor;
    private TextView txtInsideTVOCSensor;
    private TextView txtOutsidePm25Value;
    private TextView txtOutsidePm10Value;
    private TextView txtInsidePm25Value;
    private TextView txtInsideCo2Value;
    private TextView txtInsideTVOCValue;
    private LinearLayout contentsMain;

    public HomeInfoPopupActivity(@NonNull Context context) {
        super(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE); //타이틀 바 삭제
        getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        setContentView(R.layout.dialog_home_info);

        txtOutsidePm25Sensor = findViewById(R.id.txtOutsidePm25Sensor);
        txtOutsidePm10Sensor = findViewById(R.id.txtOutsidePm10Sensor);
        txtInsidePm25Sensor = findViewById(R.id.txtInsidePm25Sensor);
        txtInsideCo2Sensor = findViewById(R.id.txtInsideCo2Sensor);
        txtInsideTVOCSensor = findViewById(R.id.txtInsideTVOCSensor);
        txtOutsidePm25Value = findViewById(R.id.txtOutsidePm25Value);
        txtOutsidePm10Value = findViewById(R.id.txtOutsidePm10Value);
        txtInsidePm25Value = findViewById(R.id.txtInsidePm25Value);
        txtInsideCo2Value = findViewById(R.id.txtInsideCo2Value);
        txtInsideTVOCValue = findViewById(R.id.txtInsideTVOCValue);
        contentsMain = findViewById(R.id.contentsMain);

        contentsMain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });

        updateSensorData();
    }

    private void setSensorLevel(TextView tv, int level) {
        level = level > 4 ? 0 : level;

        CommonUtils.customLog(HomeInfoPopupActivity.class.getSimpleName(), "setSensorLevel : " + level, Log.ERROR);

        tv.setText(getStringIdToSenerLevel(level));
        tv.setTextColor(ContextCompat.getColor(getContext(), getColorIdIdToSensorLevel(level)));
    }

    /**
     * 2019.04.22 공기질 정보 상세화면 표시시에 자동 갱신하기 위해 사용
     */
    public void updateSensorData(){
        int outSidePm25 = 0;
        int outSidePm10 = 0;
        int outSidePm25Level = 0;
        int outSidePm10Level = 0;

        if(null != CleanVentilationApplication.getInstance().getHomeList() && CleanVentilationApplication.getInstance().getHomeList().size() > 0 && null != CleanVentilationApplication.getInstance().getHomeList().get(0).getOutSide()){
            outSidePm25Level = CleanVentilationApplication.getInstance().getHomeList().get(0).getOutSide().getDust25Sensor();
            outSidePm10Level = CleanVentilationApplication.getInstance().getHomeList().get(0).getOutSide().getDust10Sensor();

            outSidePm25 = (int) Math.round(CleanVentilationApplication.getInstance().getHomeList().get(0).getOutSide().getDust25Value());
            outSidePm10 = (int) Math.round(CleanVentilationApplication.getInstance().getHomeList().get(0).getOutSide().getDust10Value());
        }

        setSensorLevel(txtOutsidePm25Sensor, outSidePm25Level);
        setSensorLevel(txtOutsidePm10Sensor, outSidePm10Level);

        // ikHwang 2019-05-27 오후 3:08 수치는 소수자리 없이 표시하기위해 int형으로 변환
        txtOutsidePm25Value.setText(outSidePm25 + " ㎍/㎥");
        txtOutsidePm10Value.setText(outSidePm10 + " ㎍/㎥");

        int insideDust = 0;
        int insideCo2 = 0;
        int insideTvoc = 0;
        int insideDustValue = 0;
        int insideCo2Value = 0;
        int insideTvocValue = 0;

        if(null != CleanVentilationApplication.getInstance().getHomeList() && CleanVentilationApplication.getInstance().getHomeList().size() > 0 && null != CleanVentilationApplication.getInstance().getHomeList().get(0).getInside()){
            try {
                insideDust = CleanVentilationApplication.getInstance().getHomeList().get(0).getInside().getDustLevel();
                insideCo2 = CleanVentilationApplication.getInstance().getHomeList().get(0).getInside().getCo2Level();
                insideTvoc = CleanVentilationApplication.getInstance().getHomeList().get(0).getInside().getTvocLevel();

                insideDustValue = (int) Math.round(CleanVentilationApplication.getInstance().getHomeList().get(0).getInside().getDustValue());
                insideCo2Value = (int) Math.round(CleanVentilationApplication.getInstance().getHomeList().get(0).getInside().getCo2Value());
                insideTvocValue = (int) Math.round(CleanVentilationApplication.getInstance().getHomeList().get(0).getInside().getTvocValue());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        setSensorLevel(txtInsidePm25Sensor, insideDust);
        setSensorLevel(txtInsideCo2Sensor, insideCo2);
        setSensorLevel(txtInsideTVOCSensor, insideTvoc);

        txtInsidePm25Value.setText(insideDustValue + " ㎍/㎥");
        txtInsideCo2Value.setText(insideCo2Value + " ppm");
        txtInsideTVOCValue.setText(insideTvocValue + " ppb");

        CommonUtils.customLog(HomeInfoPopupActivity.class.getSimpleName(), "===== Call updstaSensorData", Log.ERROR);
    }
}
