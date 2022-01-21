package kr.co.kdone.airone.activity.control;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.ToggleButton;

import java.util.Calendar;

import kr.co.kdone.airone.CleanVentilationApplication;
import kr.co.kdone.airone.R;
import kr.co.kdone.airone.aws.AWSIoTMqttHelper;
import kr.co.kdone.airone.aws.DataChangeListener;
import kr.co.kdone.airone.components.KDNumberPicker;
import kr.co.kdone.airone.data.DeviceManager;
import kr.co.kdone.airone.utils.CommonUtils;

import static kr.co.kdone.airone.activity.control.ControlActivity.MSG_DEEP_SLEEP;
import static kr.co.kdone.airone.activity.control.ControlActivity.MSG_DEEP_SLEEP_CANCEL;
import static kr.co.kdone.airone.activity.control.ControlActivity.MSG_DEEP_SLEEP_COMPLETE;
import static kr.co.kdone.airone.utils.ProtocolType.COMMAND_SD;
import static kr.co.kdone.airone.utils.ProtocolType.USE;
import static kr.co.kdone.airone.utils.ProtocolType.USE_NOT;

/**
 * ikHwang 2019-06-04 오전 8:42 숙면예약 화면
 */
public class DeepSleepActivity extends AppCompatActivity implements View.OnClickListener, DataChangeListener {
    private final String TAG = getClass().getSimpleName();

    private ToggleButton btnOnOff;
    private TextView txtDeepSleepStartLabel;
    private TextView txtDeepSleepEndLabel;
    private TextView txtDeepSleepStart;
    private TextView txtDeepSleepEnd;

    private ConstraintLayout layoutDeepSleepTimeSet;
    private TextView txtDeepSleepTimeSetTitle;
    private KDNumberPicker mTimeAmPmPicker;
    private KDNumberPicker mTimeHourPicker;
    private Button btnCancel;
    private Button btnSave;

    private int mSettingType;
    private int mDeepSleepUse;
    private int mDeepSleepStart;
    private int mDeepSleepEnd;

    public static CallbackHandler callback;

    public boolean isDataSend = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_deep_sleep);
        callback = new CallbackHandler();

        mDeepSleepUse = getIntent().getIntExtra("sleepUse", USE_NOT);
        mDeepSleepStart = getIntent().getIntExtra("startTime", 1);
        mDeepSleepEnd = getIntent().getIntExtra("endTime", 2);
        if (mDeepSleepStart == mDeepSleepEnd) {
            mDeepSleepEnd = (mDeepSleepEnd + 1) % 24;
        }

        initComponents();
        initTimeSetComponents();
    }

    @Override
    protected void onResume() {
        super.onResume();

        CleanVentilationApplication.getInstance().setDataChangedListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();

        CleanVentilationApplication.getInstance().setDataChangedListener(null);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        callback = null;
    }

    @Override
    public void onBackPressed() {
        if (layoutDeepSleepTimeSet.getVisibility() == View.VISIBLE) {
            layoutDeepSleepTimeSet.setVisibility(View.GONE);
        } else {
            finish();
            overridePendingTransition(0, R.anim.slide_right_out);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnBack:
                onBackPressed();
                break;
            case R.id.layoutDeepSleepStart:
                if (btnOnOff.isChecked()) {
                    mSettingType = 1;
                    layoutDeepSleepTimeSet.setVisibility(View.VISIBLE);
                    txtDeepSleepTimeSetTitle.setText(R.string.deep_sleep_start);
                    mTimeAmPmPicker.setValue(mDeepSleepStart / 12);
                    mTimeHourPicker.setValue(mDeepSleepStart % 12);
                }
                break;
            case R.id.layoutDeepSleepEnd:
                if (btnOnOff.isChecked()) {
                    mSettingType = 2;
                    layoutDeepSleepTimeSet.setVisibility(View.VISIBLE);
                    txtDeepSleepTimeSetTitle.setText(R.string.deep_sleep_end);
                    mTimeAmPmPicker.setValue(mDeepSleepEnd / 12);
                    mTimeHourPicker.setValue(mDeepSleepEnd % 12);
                }
                break;
            case R.id.layoutConfirm:
                isDataSend = true;

                if(btnOnOff.isChecked()){
                    if (ControlActivity.callback != null) {
                        Message msg = ControlActivity.callback.obtainMessage();
                        Bundle bundle = new Bundle();
                        bundle.putInt("sleepUse", mDeepSleepUse);
                        bundle.putInt("sleepStart", mDeepSleepStart * 60);
                        bundle.putInt("sleepEnd", mDeepSleepEnd * 60);

                        msg.what = MSG_DEEP_SLEEP;
                        msg.setData(bundle);
                        ControlActivity.callback.sendMessage(msg);
                        CommonUtils.displayProgress(this, "", "");
                    }
                }else{
                    if (ControlActivity.callback != null) {
                        Message msg = ControlActivity.callback.obtainMessage();
                        Bundle bundle = new Bundle();
                        bundle.putInt("sleepUse", mDeepSleepUse);
                        bundle.putInt("sleepStart", mDeepSleepStart);
                        bundle.putInt("sleepEnd", mDeepSleepEnd);

                        msg.what = MSG_DEEP_SLEEP_CANCEL;
                        msg.setData(bundle);
                        ControlActivity.callback.sendMessage(msg);
                        CommonUtils.displayProgress(this, "", "");
                    }
                }
                break;
        }
    }

    @Override
    public void onDataChanged(int cmd) {
        if(!isDataSend) return;

        isDataSend = false;

        if(cmd == COMMAND_SD) {
            CommonUtils.DismissConnectDialog();
            if (ControlActivity.callback != null) {
                Message msg = ControlActivity.callback.obtainMessage();
                msg.what = MSG_DEEP_SLEEP_COMPLETE;
                ControlActivity.callback.sendMessage(msg);
            }
            // 데이터 확인
            finish();
        }
    }

    @Override
    public void onError(int nState) {

    }

    @Override
    public void onStatusChanged(int status) {
       // 연결 해제에 대한 처리 필요
    }

    /**
     * 전체 컴퍼넌트 초기화 및 변수에 연결.
     */
    private void initComponents() {
        btnOnOff = findViewById(R.id.btnOnOff);
        txtDeepSleepStartLabel = findViewById(R.id.txtDeepSleepStartLabel);
        txtDeepSleepEndLabel = findViewById(R.id.txtDeepSleepEndLabel);
        txtDeepSleepStart = findViewById(R.id.txtDeepSleepStart);
        txtDeepSleepEnd = findViewById(R.id.txtDeepSleepEnd);

        btnOnOff.setChecked(mDeepSleepUse == USE);
        if (btnOnOff.isChecked()) {
            txtDeepSleepStartLabel.setTextColor(Color.parseColor("#000000"));
            txtDeepSleepEndLabel.setTextColor(Color.parseColor("#000000"));
            txtDeepSleepStart.setTextColor(Color.parseColor("#3690b8"));
            txtDeepSleepEnd.setTextColor(Color.parseColor("#3690b8"));
        } else {
            txtDeepSleepStartLabel.setTextColor(Color.parseColor("#dddddd"));
            txtDeepSleepEndLabel.setTextColor(Color.parseColor("#dddddd"));
            txtDeepSleepStart.setTextColor(Color.parseColor("#dddddd"));
            txtDeepSleepEnd.setTextColor(Color.parseColor("#dddddd"));
        }

        btnOnOff.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    mDeepSleepUse = USE;
                    txtDeepSleepStartLabel.setTextColor(Color.parseColor("#000000"));
                    txtDeepSleepEndLabel.setTextColor(Color.parseColor("#000000"));
                    txtDeepSleepStart.setTextColor(Color.parseColor("#3690b8"));
                    txtDeepSleepEnd.setTextColor(Color.parseColor("#3690b8"));
                } else {
                    mDeepSleepUse = USE_NOT;
                    txtDeepSleepStartLabel.setTextColor(Color.parseColor("#dddddd"));
                    txtDeepSleepEndLabel.setTextColor(Color.parseColor("#dddddd"));
                    txtDeepSleepStart.setTextColor(Color.parseColor("#dddddd"));
                    txtDeepSleepEnd.setTextColor(Color.parseColor("#dddddd"));
                }
            }
        });

        setTxtDeepSleepTime(mDeepSleepStart, true);
        setTxtDeepSleepTime(mDeepSleepEnd, false);
    }

    /**
     * 시간 설정 컴퍼넌트 초기화 함수
     */
    private void initTimeSetComponents() {
        layoutDeepSleepTimeSet = findViewById(R.id.layoutDeepSleepTimeSet);
        txtDeepSleepTimeSetTitle = findViewById(R.id.txtDeepSleepTimeSetTitle);
        mTimeAmPmPicker = findViewById(R.id.amPmPicker);
        mTimeHourPicker = findViewById(R.id.hourPicker);
        btnCancel = findViewById(R.id.btnCancel);
        btnSave = findViewById(R.id.btnSave);

        String[] listAmPmString = {"오전", "오후"};
        for(int count = 0; count < listAmPmString.length ; count++){
            EditText txtItem = new EditText(this);
            txtItem.setTypeface(Typeface.create("sans-serif-light", Typeface.NORMAL));
            txtItem.setTextColor(Color.BLACK);
            txtItem.setText(listAmPmString[count]);
            txtItem.setClickable(false);
            mTimeAmPmPicker.addView(txtItem);
        }
        mTimeAmPmPicker.setDisplayedValues(listAmPmString);
        mTimeAmPmPicker.setMinValue(0);
        mTimeAmPmPicker.setMaxValue(1);
        mTimeAmPmPicker.setWrapSelectorWheel(false);

        String[] listHourString = new String[12];
        for(int count = 0; count < listHourString.length ; count++){
            listHourString[count] = String.format("%d",count+1);
            EditText txtItem = new EditText(this);
            txtItem.setTypeface(Typeface.create("sans-serif-light", Typeface.NORMAL));
            txtItem.setTextColor(Color.BLACK);
            txtItem.setText(String.format("%d",count+1));
            txtItem.setClickable(false);
            mTimeHourPicker.addView(txtItem);
        }
        mTimeHourPicker.setDisplayedValues(listHourString);
        mTimeHourPicker.setMinValue(1);
        mTimeHourPicker.setMaxValue(listHourString.length);
        mTimeHourPicker.setWrapSelectorWheel(true);

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                layoutDeepSleepTimeSet.setVisibility(View.GONE);
            }
        });
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int hour = mTimeAmPmPicker.getValue() * 12 + (mTimeHourPicker.getValue() % 12);

                if (mSettingType == 1) {
                    mDeepSleepStart = hour;
                    setTxtDeepSleepTime(mDeepSleepStart, true);
                    if (mDeepSleepStart == mDeepSleepEnd) {
                        mDeepSleepEnd = (mDeepSleepEnd + 1) % 24;
                        setTxtDeepSleepTime(mDeepSleepEnd, false);
                    }
                } else if (mSettingType == 2) {
                    mDeepSleepEnd = hour;
                    setTxtDeepSleepTime(mDeepSleepEnd, false);
                    if (mDeepSleepStart == mDeepSleepEnd) {
                        mDeepSleepStart = (mDeepSleepStart + 23) % 24;
                        setTxtDeepSleepTime(mDeepSleepStart, true);
                    }
                }
                layoutDeepSleepTimeSet.setVisibility(View.GONE);
            }
        });
    }

    private void setTxtDeepSleepTime(int hour, boolean isOn) {
        String timeTxt = (hour / 12 == Calendar.AM ? "오전 " : "오후 ") + (((hour + 11) % 12) + 1) + "시";
        if (isOn) {
            txtDeepSleepStart.setText(getString(R.string.deep_sleep_on, timeTxt));
        } else {
            txtDeepSleepEnd.setText(getString(R.string.deep_sleep_off, timeTxt));
        }
    }

    class CallbackHandler extends Handler {
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case MSG_DEEP_SLEEP:
                    finish();
                    break;
            }
        }
    }
}
