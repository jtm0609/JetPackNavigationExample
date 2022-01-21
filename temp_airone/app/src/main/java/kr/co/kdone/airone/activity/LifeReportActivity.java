package kr.co.kdone.airone.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.google.android.material.tabs.TabLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;
import kr.co.kdone.airone.CleanVentilationApplication;
import kr.co.kdone.airone.R;
import kr.co.kdone.airone.activity.more.MoreActivity;
import kr.co.kdone.airone.activity.more.MoreHelpActivity;
import kr.co.kdone.airone.components.NonSwipeableViewPager;
import kr.co.kdone.airone.data.LifeReportInfo;
import kr.co.kdone.airone.utils.CommonUtils;
import kr.co.kdone.airone.utils.HttpApi;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

import static kr.co.kdone.airone.utils.CommonUtils.CHART_SELECTED_DAYS;
import static kr.co.kdone.airone.utils.CommonUtils.CHART_SELECTED_MONTHS;
import static kr.co.kdone.airone.utils.CommonUtils.CHART_SELECTED_WEEKS;
import static kr.co.kdone.airone.utils.CommonUtils.CHART_TYPE_INSIDE;
import static kr.co.kdone.airone.utils.CommonUtils.CHART_TYPE_OUTSIDE;
import static kr.co.kdone.airone.utils.CommonUtils.DismissConnectDialog;
import static kr.co.kdone.airone.utils.CommonUtils.displayProgress;
import static kr.co.kdone.airone.utils.CommonUtils.dpToPx;
import static kr.co.kdone.airone.utils.CommonUtils.getIconIdToSensorLevel;
import static kr.co.kdone.airone.utils.CommonUtils.initiateTwoButtonAlert;
import static kr.co.kdone.airone.utils.CommonUtils.isUserInfoSection;
import static kr.co.kdone.airone.utils.CommonUtils.startActivityAni;

/**
 * ikHwang 2019-06-04 오전 9:47 생활 리포트 화면
 */
public class LifeReportActivity extends AppCompatActivity  implements View.OnClickListener{
    private final String TAG = getClass().getSimpleName();

    private static String DAY = "day";
    private static String WEEK = "week";
    private static String MONTH = "month";

    private final static int CHART_ANIMATION_TIME = 1000;
    private static ImageView mBtnReplace;

    private boolean mIsReflesh;

    private SectionsPagerAdapter mSectionsPagerAdapter;

    private static Locale locale = Locale.KOREA;
    private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", locale);
    private static boolean mIsLoading;
    private boolean isFirst = true;
    private boolean isResume = false;

    private static ArrayList<LifeReportInfo> mLifeReportInfoInsideDay;
    private static ArrayList<LifeReportInfo> mLifeReportInfoInsideWeek;
    private static ArrayList<LifeReportInfo> mLifeReportInfoInsideMonth;
    private static ArrayList<LifeReportInfo> mLifeReportInfoOutsideDay;
    private static ArrayList<LifeReportInfo> mLifeReportInfoOutsideWeek;
    private static ArrayList<LifeReportInfo> mLifeReportInfoOutsideMonth;
    private static LifeReportInfo mCurrLifeReportDay;
    private static LifeReportInfo mCurrLifeReportWeek;
    private static LifeReportInfo mCurrLifeReportMonth;
    private static DisplayMetrics dm = Resources.getSystem().getDisplayMetrics();

    private NonSwipeableViewPager mViewPager;
    private PlaceholderFragment[] placeholderFragments = new PlaceholderFragment[2];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_life_report);

        isUserInfoSection(this);

        mIsReflesh = false;
        mIsLoading = false;

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mLifeReportInfoInsideDay = new ArrayList<LifeReportInfo>();
        mLifeReportInfoInsideWeek = new ArrayList<LifeReportInfo>();
        mLifeReportInfoInsideMonth = new ArrayList<LifeReportInfo>();
        mLifeReportInfoOutsideDay = new ArrayList<LifeReportInfo>();
        mLifeReportInfoOutsideWeek = new ArrayList<LifeReportInfo>();
        mLifeReportInfoOutsideMonth = new ArrayList<LifeReportInfo>();
        mCurrLifeReportDay = new LifeReportInfo();
        mCurrLifeReportWeek = new LifeReportInfo();
        mCurrLifeReportMonth = new LifeReportInfo();

        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                int flag_type = placeholderFragments[position].mSelectedType;
                String flag;
                String date;
                switch (flag_type) {
                    case CHART_SELECTED_DAYS :
                        flag = DAY;
                        date = sdf.format(placeholderFragments[position].daysDate);
                        break;

                    case CHART_SELECTED_WEEKS :
                        flag = WEEK;
                        date = sdf.format(placeholderFragments[position].weeksDate);
                        break;

                    case CHART_SELECTED_MONTHS :
                        flag = MONTH;
                        date = sdf.format(placeholderFragments[position].monthsDate);
                        break;

                    default :
                        return;
                }

                Calendar cal = Calendar.getInstance();
                placeholderFragments[position].daysDate = cal.getTime();
                date = sdf.format(cal.getTime());

                cal.add(Calendar.DATE, ((cal.get(Calendar.DAY_OF_WEEK) + 5) % 7) * -1);
                placeholderFragments[position].weeksDate = cal.getTime();

                cal = Calendar.getInstance();
                cal.set(Calendar.DATE, 1);
                placeholderFragments[position].monthsDate = cal.getTime();

                getLifeReportData(CHART_TYPE_INSIDE+position, "day", date);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        PlaceholderFragment.setChangeChartListener(new PlaceholderFragment.ChangeChartListener() {
            @Override
            public void setChartData(int chart_type, int flag_type, String date) {
                String flag;
                switch (flag_type) {
                    case CHART_SELECTED_DAYS :
                        flag = DAY;
                        break;

                    case CHART_SELECTED_WEEKS :
                        flag = WEEK;
                        break;

                    case CHART_SELECTED_MONTHS :
                        flag = MONTH;
                        break;

                    default :
                        return;
                }
                getLifeReportData(chart_type, flag, date);
            }

            @Override
            public void setFragment(PlaceholderFragment fragment, int position) {
                placeholderFragments[position] = fragment;
            }
        });

        TabLayout tabLayout = findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);

    }

    @Override
    protected void onResume() {
        super.onResume();
        if(CleanVentilationApplication.isAnyType()) overridePendingTransition(R.anim.slide_none, 0);
        CleanVentilationApplication.setAnyType(false);

        if(0 != mViewPager.getCurrentItem()){
            mViewPager.setCurrentItem(0);
        }

        Calendar cal = Calendar.getInstance();
        String date = sdf.format(cal.getTime());
        getLifeReportData(CHART_TYPE_INSIDE, DAY, date);

        Log.e("DisplayManager", dm.widthPixels + " " + dm.heightPixels + " " + dm.density);

        isFirst = false;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btnReplace:
                if (placeholderFragments != null) {
                    final View vv = v;
                    vv.setEnabled(false);
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    vv.setEnabled(true);
                                }
                            });
                        }
                    }, CHART_ANIMATION_TIME + 200);
                    mIsReflesh = !isResume;
                    isResume = false;

                    int chart_type = mViewPager.getCurrentItem();
                    int flag_type = placeholderFragments[chart_type].mSelectedType;
                    String flag;
                    String date;
                    switch (flag_type) {
                        case CHART_SELECTED_DAYS:
                            flag = DAY;
                            date = sdf.format(placeholderFragments[chart_type].daysDate);
                            break;

                        case CHART_SELECTED_WEEKS:
                            flag = WEEK;
                            date = sdf.format(placeholderFragments[chart_type].weeksDate);
                            break;

                        case CHART_SELECTED_MONTHS:
                            flag = MONTH;
                            date = sdf.format(placeholderFragments[chart_type].monthsDate);
                            break;

                        default:
                            return;
                    }

                    getLifeReportData(chart_type, flag, date);
                }
                break;

            case R.id.btnMore: // 더보기 (설정)
                Intent intentMore = new Intent(this, MoreActivity.class);
                intentMore.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivityAni(LifeReportActivity.this, intentMore, false, 1);
                break;

            case R.id.btnHelp: // 공기질 정보 화면
                Intent intentMoreHelp = new Intent(this, MoreHelpActivity.class);
                startActivityAni(LifeReportActivity.this, intentMoreHelp, false, 1);
                break;
        }
    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        initiateTwoButtonAlert(this, getString(R.string.exit_app), getString(R.string.confirm), getString(R.string.no));
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    /**
     * 생활리포터 데이터 획득 함수.
     */
    private void getLifeReportData(final int type, final String flag, final String date) {
        try {
            if (mIsLoading) {
                return;
            }
            if (type != CHART_TYPE_INSIDE && type != CHART_TYPE_OUTSIDE) {
                return;
            }

            mIsLoading = true;
            displayProgress(this, "", "");

            final String selectFlag = type == CHART_TYPE_OUTSIDE ? "outside" : "inside";
            // 2차 API
            HttpApi.PostV2GetReport(
                    CleanVentilationApplication.getInstance().getUserInfo().getId(),
                    flag,
                    selectFlag,
                    date,
                    new Callback() {
                        @Override
                        public void onFailure(Call call, IOException e) {
                            e.printStackTrace();
                            DismissConnectDialog();
                            CommonUtils.showToast(LifeReportActivity.this, getString(R.string.toast_result_can_not_request));
                            mIsLoading = false;
                        }

                        @Override
                        public void onResponse(Call call, Response response) throws IOException {
                            try {
                                if (flag.equals(DAY)) {
                                    if (type == CHART_TYPE_INSIDE) {
                                        mLifeReportInfoInsideDay.clear();
                                    } else {
                                        mLifeReportInfoOutsideDay.clear();
                                    }
                                } else if (flag.equals(WEEK)) {
                                    if (type == CHART_TYPE_INSIDE) {
                                        mLifeReportInfoInsideWeek.clear();
                                    } else {
                                        mLifeReportInfoOutsideWeek.clear();
                                    }
                                } else if (flag.equals(MONTH)) {
                                    if (type == CHART_TYPE_INSIDE) {
                                        mLifeReportInfoInsideMonth.clear();
                                    } else {
                                        mLifeReportInfoOutsideMonth.clear();
                                    }
                                } else {
                                    mIsLoading = false;
                                    return;
                                }

                                JSONObject json_data = new JSONObject(response.body().string());

                                CommonUtils.customLog(TAG, "report/info : " + json_data.toString(), Log.ERROR);

                                switch (json_data.getInt("code")){
                                    case HttpApi.RESPONSE_SUCCESS:
                                        JSONArray jarray = new JSONArray(json_data.getString("data"));

                                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.KOREA);
                                        Calendar today = Calendar.getInstance();
                                        Calendar compare = Calendar.getInstance();

                                        for (int i = 0; i < jarray.length(); i++) {
                                            LifeReportInfo lifeReportInfo = new LifeReportInfo();
                                            JSONObject jObject = jarray.getJSONObject(i);  // JSONObject 추출
                                            lifeReportInfo.DateFlag = jObject.getString("dateFlag");
                                            lifeReportInfo.SelectFlag = jObject.getString("selectFlag");

                                            //inside
                                            // ikHwang 2019-06-11 오전 11:42 프리즘 모델 추가
                                            lifeReportInfo.totalSensor = jObject.isNull("totalSensor") ? 0 : jObject.optDouble("totalSensor", 0);
                                            lifeReportInfo.totalValue = jObject.isNull("totalValue") ? 0 : jObject.optDouble("totalValue", 0);
                                            lifeReportInfo.pm1Sensor = jObject.isNull("dust1Sensor") ? 0 : jObject.optDouble("dust1Sensor", 0);
                                            lifeReportInfo.pm1Value = jObject.isNull("dust1Value") ? 0 : jObject.optDouble("dust1Value", 0);

                                            lifeReportInfo.DustSensor = jObject.isNull("dustSensor") ? 0 : jObject.optDouble("dustSensor", 0);
                                            lifeReportInfo.DustValue = jObject.isNull("dustValue") ? 0 : jObject.optDouble("dustValue", 0);
                                            lifeReportInfo.CO2Sensor = jObject.isNull("co2Sensor") ? 0 : jObject.optDouble("co2Sensor", 0);
                                            lifeReportInfo.CO2Value = jObject.isNull("co2Value") ? 0 : jObject.optDouble("co2Value", 0);
                                            lifeReportInfo.GASSensor = jObject.isNull("gasSensor") ? 0 : jObject.optDouble("gasSensor", 0);
                                            lifeReportInfo.GASValue = jObject.isNull("gasValue") ? 0 : jObject.optDouble("gasValue", 0);
                                            lifeReportInfo.SensorTemp = jObject.isNull("sensorTemp") ? 0 : jObject.optDouble("sensorTemp", 0);
                                            lifeReportInfo.SensorHum = jObject.isNull("sensorHum") ? 0: jObject.optDouble("sensorHum", 0);
                                            lifeReportInfo.Time = jObject.isNull("time") ? "" : jObject.getString("time");

                                            //outside
                                            lifeReportInfo.pm25Value = jObject.isNull("pm25Value") ? 0 : jObject.optDouble("pm25Value", 0);
                                            lifeReportInfo.pm25Sensor = jObject.isNull("pm25Sensor") ? 0 : jObject.optDouble("pm25Sensor", 0);
                                            lifeReportInfo.pm10Value = jObject.isNull("pm10Value") ? 0 : jObject.optDouble("pm10Value", 0);
                                            lifeReportInfo.pm10Sensor = jObject.isNull("pm10Sensor") ? 0 : jObject.optDouble("pm10Sensor", 0);
                                            lifeReportInfo.outsideTemp = jObject.isNull("outsideTemp") ? 0 : jObject.optDouble("outsideTemp", 0);
                                            lifeReportInfo.outsideHum = jObject.isNull("outsideHum") ? 0 : jObject.optDouble("outsideHum", 0);

                                            if (lifeReportInfo.DateFlag.equals("day")) {
                                                if (date.equalsIgnoreCase(sdf.format(today.getTime()))) {
                                                    int hour = Integer.parseInt(lifeReportInfo.Time.split(" ")[1]);
                                                    // 공기리포트 그래프 표시시 현재 시간은 표시되지 않도록 기획팀에서 요청하였으나
                                                    // 본부장
                                                    if (hour > today.get(Calendar.HOUR_OF_DAY)) {
                                                        break;
                                                    }

                                                    // ikHwang 2019-06-11 오전 11:43 프리즘 모델 추가
                                                    mCurrLifeReportDay.totalSensor = lifeReportInfo.totalSensor;
                                                    mCurrLifeReportDay.totalValue = lifeReportInfo.totalValue;
                                                    mCurrLifeReportDay.pm1Sensor = lifeReportInfo.pm1Sensor;
                                                    mCurrLifeReportDay.pm1Value = lifeReportInfo.pm1Value;


                                                    mCurrLifeReportDay.DustSensor = lifeReportInfo.DustSensor;
                                                    mCurrLifeReportDay.DustValue = lifeReportInfo.DustValue;
                                                    mCurrLifeReportDay.CO2Sensor = lifeReportInfo.CO2Sensor;
                                                    mCurrLifeReportDay.CO2Value = lifeReportInfo.CO2Value;
                                                    mCurrLifeReportDay.GASSensor = lifeReportInfo.GASSensor;
                                                    mCurrLifeReportDay.GASValue = lifeReportInfo.GASValue;
                                                    mCurrLifeReportDay.SensorTemp = lifeReportInfo.SensorTemp;
                                                    mCurrLifeReportDay.SensorHum = lifeReportInfo.SensorHum;
                                                    mCurrLifeReportDay.pm10Value = lifeReportInfo.pm10Value;
                                                    mCurrLifeReportDay.pm25Value = lifeReportInfo.pm25Value;
                                                    mCurrLifeReportDay.pm10Sensor = lifeReportInfo.pm10Sensor;
                                                    mCurrLifeReportDay.pm25Sensor = lifeReportInfo.pm25Sensor;
                                                    mCurrLifeReportDay.outsideTemp = lifeReportInfo.outsideTemp;
                                                    mCurrLifeReportDay.outsideHum = lifeReportInfo.outsideHum;
                                                }

                                                if (type == CHART_TYPE_INSIDE) {
                                                    mLifeReportInfoInsideDay.add(lifeReportInfo);
                                                } else {
                                                    mLifeReportInfoOutsideDay.add(lifeReportInfo);
                                                }

                                            } else if (lifeReportInfo.DateFlag.equals("week")) {
                                                compare.setTime(today.getTime());
                                                compare.add(Calendar.DATE, ((today.get(Calendar.DAY_OF_WEEK) + 5) % 7) * (-1));
                                                if (date.equalsIgnoreCase(sdf.format(compare.getTime()))) {
                                                    // ikHwang 2019-11-19 오후 2:35 매달 1일이 이전달의 주와 같은 주일 경우 그래프 표시가 문제가 되어 년, 월, 일 모두 체크하여 처리
                                                    int reportDate = Integer.parseInt(lifeReportInfo.Time.replaceAll("-", ""));
                                                    int currentDate = Integer.parseInt(sdf.format(today.getTime()).replaceAll("-", ""));

                                                    if (reportDate > currentDate) {
                                                        break;
                                                    }

                                                    // ikHwang 2019-06-11 오전 11:43 프리즘 모델 추가
                                                    mCurrLifeReportDay.totalSensor = lifeReportInfo.totalSensor;
                                                    mCurrLifeReportDay.totalValue = lifeReportInfo.totalValue;
                                                    mCurrLifeReportDay.pm1Sensor = lifeReportInfo.pm1Sensor;
                                                    mCurrLifeReportDay.pm1Value = lifeReportInfo.pm1Value;



                                                    mCurrLifeReportWeek.DustSensor = lifeReportInfo.DustSensor;
                                                    mCurrLifeReportWeek.DustValue = lifeReportInfo.DustValue;
                                                    mCurrLifeReportWeek.CO2Sensor = lifeReportInfo.CO2Sensor;
                                                    mCurrLifeReportWeek.CO2Value = lifeReportInfo.CO2Value;
                                                    mCurrLifeReportWeek.GASSensor = lifeReportInfo.GASSensor;
                                                    mCurrLifeReportWeek.GASValue = lifeReportInfo.GASValue;
                                                    mCurrLifeReportWeek.SensorTemp = lifeReportInfo.SensorTemp;
                                                    mCurrLifeReportWeek.SensorHum = lifeReportInfo.SensorHum;
                                                    mCurrLifeReportWeek.pm10Value = lifeReportInfo.pm10Value;
                                                    mCurrLifeReportWeek.pm25Value = lifeReportInfo.pm25Value;
                                                    mCurrLifeReportWeek.pm10Sensor = lifeReportInfo.pm10Sensor;
                                                    mCurrLifeReportWeek.pm25Sensor = lifeReportInfo.pm25Sensor;
                                                    mCurrLifeReportWeek.outsideTemp = lifeReportInfo.outsideTemp;
                                                    mCurrLifeReportWeek.outsideHum = lifeReportInfo.outsideHum;
                                                }

                                                if (type == CHART_TYPE_INSIDE) {
                                                    mLifeReportInfoInsideWeek.add(lifeReportInfo);
                                                } else {
                                                    mLifeReportInfoOutsideWeek.add(lifeReportInfo);
                                                }
                                            } else if (lifeReportInfo.DateFlag.equals("month")) {
                                                compare.setTime(today.getTime());
                                                compare.set(Calendar.DATE, 1);

                                                if (date.equalsIgnoreCase(sdf.format(compare.getTime()))) {
                                                    int month = Integer.parseInt(lifeReportInfo.Time.split("-")[2]);
                                                    if (month > today.get(Calendar.DAY_OF_MONTH)) {
                                                        break;
                                                    }

                                                    // ikHwang 2019-06-11 오전 11:43 프리즘 모델 추가
                                                    mCurrLifeReportDay.totalSensor = lifeReportInfo.totalSensor;
                                                    mCurrLifeReportDay.totalValue = lifeReportInfo.totalValue;
                                                    mCurrLifeReportDay.pm1Sensor = lifeReportInfo.pm1Sensor;
                                                    mCurrLifeReportDay.pm1Value = lifeReportInfo.pm1Sensor;


                                                    mCurrLifeReportMonth.DustSensor = lifeReportInfo.DustSensor;
                                                    mCurrLifeReportMonth.DustValue = lifeReportInfo.DustValue;
                                                    mCurrLifeReportMonth.CO2Sensor = lifeReportInfo.CO2Sensor;
                                                    mCurrLifeReportMonth.CO2Value = lifeReportInfo.CO2Value;
                                                    mCurrLifeReportMonth.GASSensor = lifeReportInfo.GASSensor;
                                                    mCurrLifeReportMonth.GASValue = lifeReportInfo.GASValue;
                                                    mCurrLifeReportMonth.SensorTemp = lifeReportInfo.SensorTemp;
                                                    mCurrLifeReportMonth.SensorHum = lifeReportInfo.SensorHum;
                                                    mCurrLifeReportMonth.pm10Value = lifeReportInfo.pm10Value;
                                                    mCurrLifeReportMonth.pm25Value = lifeReportInfo.pm25Value;
                                                    mCurrLifeReportMonth.pm10Sensor = lifeReportInfo.pm10Sensor;
                                                    mCurrLifeReportMonth.pm25Sensor = lifeReportInfo.pm25Sensor;
                                                    mCurrLifeReportMonth.outsideTemp = lifeReportInfo.outsideTemp;
                                                    mCurrLifeReportMonth.outsideHum = lifeReportInfo.outsideHum;
                                                }

                                                if (type == CHART_TYPE_INSIDE) {
                                                    mLifeReportInfoInsideMonth.add(lifeReportInfo);
                                                } else {
                                                    mLifeReportInfoOutsideMonth.add(lifeReportInfo);
                                                }
                                            }
                                        }

                                        Log.e("life report", type + " " + flag + " " + date);
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                if (mViewPager != null) {
                                                    if (flag.equals(DAY)) {
                                                        placeholderFragments[type].mSelectedType = CHART_SELECTED_DAYS;
                                                    } else if (flag.equals(WEEK)) {
                                                        placeholderFragments[type].mSelectedType = CHART_SELECTED_WEEKS;
                                                    } else if (flag.equals(MONTH)) {
                                                        placeholderFragments[type].mSelectedType = CHART_SELECTED_MONTHS;
                                                    }
                                                    mViewPager.getAdapter().notifyDataSetChanged();

                                                    if (mIsReflesh) {
                                                        mIsReflesh = false;
                                                        CommonUtils.showToast(LifeReportActivity.this, getString(R.string.toast_result_updated_infomation));
                                                    }
                                                }
                                            }
                                        });
                                        break;

                                    case HttpApi.RESPONSE_DATA_NO_EXIST:
                                        CommonUtils.showToast(LifeReportActivity.this, getString(R.string.toast_result_no_data2));
                                        mIsLoading = false;
                                        break;

                                    default:
                                        CommonUtils.showToast(LifeReportActivity.this, getString(R.string.toast_result_can_not_request));
                                        mIsLoading = false;
                                        break;
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                                CommonUtils.showToast(LifeReportActivity.this, getString(R.string.toast_result_can_not_request));
                                mIsLoading = false;
                            }finally {
                                DismissConnectDialog();
                            }
                        }
                    });
        } catch (Exception e) {
            e.printStackTrace();
            DismissConnectDialog();
            CommonUtils.showToast(LifeReportActivity.this, getString(R.string.toast_result_can_not_request));
            mIsLoading = false;
        }
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        private int[] mLevelColors = new int[] {
                Color.argb(0xff,0x00,0x00,0x00),
                Color.argb(0xff,0x1d,0x87,0xd8),
                Color.argb(0xff,0x26,0xc5,0x88),
                Color.argb(0xff,0xf8,0x9c,0x22),
                Color.argb(0xff,0xe4,0x42,0x22),
        };

        private int mColorWhite = Color.argb(0xff,0xff,0xff,0xff);
        private int mColorBlack = Color.argb(0xff,0x00,0x00,0x00);
        private int mColorTrans = Color.argb(0xff,0x00,0x00,0x00);

        private int mColorGray66 = Color.argb(0xff,0x66,0x66,0x66);

        private int mColorchart = Color.argb(0xff,0x36,0x90,0xb8);

        private LineChart mInsideTotalChart;    // 실내 통합 공기질 그래프
        private LineChart mInsidePm1Chart;      // 실내 PM1.0 그래프
        private LineChart mInsidePm25Chart;     // 실내 PM2.5 그래프
        private LineChart mInsideCo2Chart;      // 실내 CO2 그래프
        private LineChart mInsideTVOCChart;     // 실내 TVOC 그래프
        private LineChart mInsideTempChart;     // 실내 온도 그래프
        private LineChart mInsideHumiChart;     // 실내 습도 그래프
        private LineChart mOutsidePm25Chart;    // 실외 PM2.5 그래프
        private LineChart mOutsidePm10Chart;    // 실외 PM10 그래프
        private LineChart mOutsideTempChart;    // 실외 온도 그래프
        private LineChart mOutsideHumiChart;    // 실외 습도 그래프

        private LinearLayout mLayoutInsideTotalGraphUnderInfos; // 하단 x축 표시 레이아웃
        private LinearLayout mLayoutInsidePm1GraphUnderInfos;
        private LinearLayout mLayoutInsidePm25GraphUnderInfos;
        private LinearLayout mLayoutInsideCo2GraphUnderInfos;
        private LinearLayout mLayoutInsideTVOCGraphUnderInfos;
        private LinearLayout mLayoutInsideTempGraphUnderInfos;
        private LinearLayout mLayoutInsideHumiGraphUnderInfos;
        private LinearLayout mLayoutOutsidePm25GraphUnderInfos;
        private LinearLayout mLayoutOutsidePm10GraphUnderInfos;
        private LinearLayout mLayoutOutsideTempGraphUnderInfos;
        private LinearLayout mLayoutOutsideHumiGraphUnderInfos;

        private LinearLayout mLayoutInsideTotalCurrent; // 현새 시간 레이아웃
        private LinearLayout mLayoutInsidePm1Current;
        private LinearLayout mLayoutInsidePm25Current;
        private LinearLayout mLayoutInsideCo2Current;
        private LinearLayout mLayoutInsideTVOCCurrent;
        private LinearLayout mLayoutInsideTempCurrent;
        private LinearLayout mLayoutInsideHumiCurrent;
        private LinearLayout mLayoutOutsidePm25Current;
        private LinearLayout mLayoutOutsidePm10Current;
        private LinearLayout mLayoutOutsideTempCurrent;
        private LinearLayout mLayoutOutsideHumiCurrent;

        public int mSelectedType = CHART_SELECTED_DAYS;

        private TextView mTxtCurrentTime;
        private TextView mTxtDays;
        private TextView mTxtWeeks;
        private TextView mTxtMonths;

        private ImageView btnPrevTime;
        private ImageView btnNextTime;

        private TextView mTxtInsideTotalInfo;
        private TextView mTxtInsidePm1Info;
        private TextView mTxtInsidePm25Info;
        private TextView mTxtInsideCo2Info;
        private TextView mTxtInsideTVOCInfo;
        private TextView mTxtInsideTempInfo;
        private TextView mTxtInsideHumiInfo;
        private TextView mTxtOutsidePm25Info;
        private TextView mTxtOutsidePm10Info;
        private TextView mTxtOutsideTempInfo;
        private TextView mTxtOutsideHumiInfo;


        //수정 jh.j
        //센서레벨에 따른 아이콘
        private View mTxtInsideTotalInfoData;
        private View mTxtInsidePm1InfoData;
        private View mTxtInsidePm25InfoData;
        private View mTxtInsideCo2InfoData;
        private View mTxtInsideTVOCInfoData;
        private View mTxtOutsidePm25InfoData;
        private View mTxtOutsidePm10InfoData;

        //수정 jh.j
        //실내 - 차트에서 선택된 date 정보
        private TextView mTxtInsideTotalInfoTime;
        private TextView mTxtInsidePm1InfoTime;
        private TextView mTxtInsidePm25InfoTime;
        private TextView mTxtInsideCo2InfoTime;
        private TextView mTxtInsideTVOCInfoTime;
        private TextView mTxtInsideTempInfoTime;
        private TextView mTxtInsideHumiInfoTime;
        //실외 - 차트에서 선택된 date 정보
        private TextView mTxtOutsidePm25InfoDataTime;
        private TextView mTxtOutsidePm10InfoDataTime;
        private TextView mTxtOutsideTempInfoTime;
        private TextView mTxtOutsideHumiInfoTime;

        private TextView[] mTxtInsideTotalGraphUnderInfos;
        private TextView[] mTxtInsidePm1GraphUnderInfos;
        private TextView[] mTxtInsidePm25GraphUnderInfos;
        private TextView[] mTxtInsideCo2GraphUnderInfos;
        private TextView[] mTxtInsideTVOCGraphUnderInfos;
        private TextView[] mTxtInsideTempGraphUnderInfos;
        private TextView[] mTxtInsideHumiGraphUnderInfos;
        private TextView[] mTxtOutsidePm25GraphUnderInfos;
        private TextView[] mTxtOutsidePm10GraphUnderInfos;
        private TextView[] mTxtOutsideTempGraphUnderInfos;
        private TextView[] mTxtOutsideHumiGraphUnderInfos;

        public Date daysDate;
        public Date weeksDate;
        public Date monthsDate;

        private boolean isUpScroll = false;
        private boolean isScroll = true;
        private float preX, preY;
        private static final int OFF_SET = 10;

        private static ChangeChartListener mListener;
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        public interface ChangeChartListener {
            //            void changeChart(int type, Date date);
            void setChartData(int chart_type, int flag, String date);
            void setFragment(PlaceholderFragment fragment, int position);
        }
        public static void setChangeChartListener(ChangeChartListener l){
            mListener = l;
        }

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);

            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_lifereport, container, false);

            if (daysDate == null || weeksDate == null || monthsDate == null) {
                Calendar cal = Calendar.getInstance();
                daysDate = cal.getTime();

                cal.add(Calendar.DATE, ((cal.get(Calendar.DAY_OF_WEEK) + 5) % 7) * -1);
                weeksDate = cal.getTime();

                cal = Calendar.getInstance();
                cal.set(Calendar.DATE, 1);
                monthsDate = cal.getTime();
            }

            initComponent(rootView);
            changeSelectedType(mSelectedType);
            changeChartXlabelText(mSelectedType);
            changeCurrentValue(mSelectedType);

            if(getArguments().getInt(ARG_SECTION_NUMBER) == CHART_TYPE_INSIDE) {
                rootView.findViewById(R.id.layoutOutsidePm25Graph).setVisibility(View.GONE);
                rootView.findViewById(R.id.layoutOutsidePm10Graph).setVisibility(View.GONE);
                rootView.findViewById(R.id.layoutOutsideTempGraph).setVisibility(View.GONE);
                rootView.findViewById(R.id.layoutOutsideHumiGraph).setVisibility(View.GONE);

                // ikHwang 2019-06-11 오전 11:14 프리즘 실내공기질 및 pm1.0 추가
                if(CleanVentilationApplication.getInstance().isIsOldUser()){
                    rootView.findViewById(R.id.layoutInsideTotalGraph).setVisibility(View.GONE);
                    rootView.findViewById(R.id.layoutInsidePm1Graph).setVisibility(View.GONE);
                }else{
                    initChart(mInsideTotalChart);
                    initChart(mInsidePm1Chart);
                }

                initChart(mInsidePm25Chart);
                initChart(mInsideCo2Chart);
                initChart(mInsideTVOCChart);
                initChart(mInsideTempChart);
                initChart(mInsideHumiChart);
            } else {
                // ikHwang 2019-06-11 오전 11:41 프리즘 모델 추가
                rootView.findViewById(R.id.layoutInsideTotalGraph).setVisibility(View.GONE);
                rootView.findViewById(R.id.layoutInsidePm1Graph).setVisibility(View.GONE);

                rootView.findViewById(R.id.layoutInsidePm25Graph).setVisibility(View.GONE);
                rootView.findViewById(R.id.layoutInsideCo2Graph).setVisibility(View.GONE);
                rootView.findViewById(R.id.layoutInsideTVOCGraph).setVisibility(View.GONE);
                rootView.findViewById(R.id.layoutInsideTempGraph).setVisibility(View.GONE);
                rootView.findViewById(R.id.layoutInsideHumiGraph).setVisibility(View.GONE);

                initChart(mOutsidePm25Chart);
                initChart(mOutsidePm10Chart);
                initChart(mOutsideTempChart);
                initChart(mOutsideHumiChart);
            }

            if (mListener != null) {
                mListener.setFragment(this, getArguments().getInt(ARG_SECTION_NUMBER));
            }

            mIsLoading = false;
            return rootView;
        }


        /**
         * 차트 초기화 함수
         * @param chart
         */
        @SuppressLint("ClickableViewAccessibility")
        private void initChart(final LineChart chart){
            chart.setDrawGridBackground(false);
            chart.getDescription().setEnabled(true);
            chart.setDrawBorders(false);
            chart.getAxisLeft().setEnabled(false);
            chart.getAxisRight().setEnabled(false);
            chart.getAxisRight().setDrawAxisLine(false);
            chart.getAxisRight().setDrawGridLines(false);
            chart.getXAxis().setDrawAxisLine(false);
            chart.getXAxis().setDrawGridLines(false);

            // Y값 최대,최소값 설정.
            if(chart.getId() == R.id.layoutInsideTempChart || chart.getId() == R.id.layoutOutsideTempChart) {
                chart.getAxisLeft().setAxisMinimum(-20f);
                chart.getAxisLeft().setAxisMaximum(40f);
            } else if(chart.getId() == R.id.layoutInsideHumiChart || chart.getId() == R.id.layoutOutsideHumiChart) {
                chart.getAxisLeft().setAxisMinimum(0f);
                chart.getAxisLeft().setAxisMaximum(100f);
            } else {
                chart.getAxisLeft().setAxisMinimum(0.55f);
                chart.getAxisLeft().setAxisMaximum(4);

            }
            // enable scaling and dragging
            chart.setDragEnabled(true);
            chart.setScaleEnabled(false);

            // if disabled, scaling can be done on x- and y-axis separately
            chart.setPinchZoom(false);

            Legend l = chart.getLegend();
            l.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
            l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.RIGHT);
            l.setOrientation(Legend.LegendOrientation.VERTICAL);
            l.setDrawInside(false);

            //Hide All label
            chart.setDescription(null);    // Hide the description
            chart.getAxisLeft().setDrawLabels(false);
            chart.getAxisRight().setDrawLabels(false);
            chart.getXAxis().setDrawLabels(false);
            chart.getLegend().setEnabled(false);

            //dot line
            if(mSelectedType == CHART_SELECTED_DAYS){
                ArrayList<LifeReportInfo> reportList = getArguments().getInt(ARG_SECTION_NUMBER) == CHART_TYPE_INSIDE ? mLifeReportInfoInsideDay : mLifeReportInfoOutsideDay;
                if(dm.density == 4){
                    chart.setExtraOffsets(16, 0, 26+(24-reportList.size())*9.83f, 0);
                } else if (dm.density == 3.5) {
                    chart.setExtraOffsets(20, 0, 30+(24-reportList.size())*11.72f, 0);
                } else if (dm.density == 3.0) {
                    chart.setExtraOffsets(16, 0, 26+(24-reportList.size())*9.83f, 0);
                } else if (dm.density == 2.0) {
                    chart.setExtraOffsets(16, 0, 26+(24-reportList.size())*9.83f, 0);
                } else if(dm.density == 2.625){ // 23시까지 표시되도록 수정
                    chart.setExtraOffsets(20, 0, 30+(24-reportList.size())*11.71f, 0);
                } else if(dm.density == 2.25){ // ikHwang 2019-10-17 오후 12:11 갤럭시 폴드 그래프
                    if(dm.widthPixels < 900){
                        chart.setExtraOffsets(16, 0, 24+(24-reportList.size())*10.42f, 0);
                    }else{
                        chart.setExtraOffsets(33, 0, 49+(24-reportList.size())*22.12f, 0);
                    }
                } else if(dm.density == 2.75){ // ikHwang 2019-10-17 오후 12:11 샤오미 MI9
                    chart.setExtraOffsets(18, 0, 26+(24-reportList.size())*11.20f, 0);
                }
            } else if(mSelectedType == CHART_SELECTED_WEEKS){
                ArrayList<LifeReportInfo> reportList = getArguments().getInt(ARG_SECTION_NUMBER) == CHART_TYPE_INSIDE ? mLifeReportInfoInsideWeek : mLifeReportInfoOutsideWeek;
                if(dm.density == 4){
                    chart.setExtraOffsets(19, 0, 20+(7-reportList.size())*38.1f, 0);
                } else if (dm.density == 3.5) {
                    chart.setExtraOffsets(22, 0, 22+(7-reportList.size())*45.95f, 0);
                } else if (dm.density == 3.0) {
                    chart.setExtraOffsets(20, 0, 20+(7-reportList.size())*38.1f, 0);
                } else if (dm.density == 2.0) {
                    chart.setExtraOffsets(20, 0, 20+(7-reportList.size())*38.16f, 0);
                } else if(dm.density == 2.625){
                    chart.setExtraOffsets(22, 0, 22+(7-reportList.size())*45.95f, 0);
                } else if(dm.density == 2.25){ // ikHwang 2019-10-17 오후 12:11 갤럭시 폴드 그래프
                    if(dm.widthPixels < 900){
                        chart.setExtraOffsets(16, 0, 16+(7-reportList.size())*41.42f, 0);
                    }else{
                        chart.setExtraOffsets(40, 0, 40+(7-reportList.size())*85.12f, 0);
                    }
                } else if(dm.density == 2.75){ // ikHwang 2019-10-17 오후 12:11 샤오미 MI9
                    chart.setExtraOffsets(20, 0, 20+(7-reportList.size())*43.37f, 0);
                }
            } else if(mSelectedType == CHART_SELECTED_MONTHS){
                ArrayList<LifeReportInfo> reportList = getArguments().getInt(ARG_SECTION_NUMBER) == CHART_TYPE_INSIDE ? mLifeReportInfoInsideMonth : mLifeReportInfoOutsideMonth;
                if(dm.density == 4){
                    chart.setExtraOffsets(15, 0, 15+(31-reportList.size())*7.95f, 0);
                } else if (dm.density == 3.5) {
                    chart.setExtraOffsets(20, 0, 16+(31-reportList.size())*9.45f, 0);
                } else if (dm.density == 3.0) {
                    chart.setExtraOffsets(15, 0, 15+(31-reportList.size())*7.95f, 0);
                } else if (dm.density == 2.0) {
                    chart.setExtraOffsets(15, 0, 15+(31-reportList.size())*7.96f, 0);
                } else if(dm.density == 2.625){
                    chart.setExtraOffsets(20, 0, 16+(31-reportList.size())*9.45f, 0);
                } else if(dm.density == 2.25){ // ikHwang 2019-10-17 오후 12:11 갤럭시 폴드 그래프
                    if(dm.widthPixels < 900){
                        chart.setExtraOffsets(16, 0, 13+(31-reportList.size())*8.35f, 0);
                    }else{
                        chart.setExtraOffsets(38, 0, 26+(31-reportList.size())*17.56f, 0);
                    }
                } else if(dm.density == 2.75){ // ikHwang 2019-10-17 오후 12:11 샤오미 MI9
                    chart.setExtraOffsets(18, 0, 15+(31-reportList.size())*8.90f, 0);
                }
            }

            chart.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    int action = event.getAction() & MotionEvent.ACTION_MASK;

                    switch (action){
                        case MotionEvent.ACTION_DOWN:
                            preX = event.getX();
                            preY = event.getY();
                        case MotionEvent.ACTION_UP:
                        case MotionEvent.ACTION_CANCEL:
                            isScroll = false;
                            v.getParent().requestDisallowInterceptTouchEvent(false);
                            CommonUtils.customLog("==", "setOnTouchListener down, up, cancel false", Log.ERROR);
                            break;

                        case MotionEvent.ACTION_MOVE:
                            if (!isScroll) {
                                float x = Math.abs(event.getX() - preX);
                                float y = Math.abs(event.getY() - preY);
                                if (x > OFF_SET) {
                                    isScroll = true;
                                    isUpScroll = false;
                                    v.getParent().requestDisallowInterceptTouchEvent(true);
                                    CommonUtils.customLog("==", "setOnTouchListener move true", Log.ERROR);
                                } else if (y > OFF_SET) {
                                    isScroll = true;
                                    isUpScroll = true;
                                    v.getParent().requestDisallowInterceptTouchEvent(false);
                                    CommonUtils.customLog("==", "setOnTouchListener move false", Log.ERROR);
                                }
                            }
                    }
                    return v.onTouchEvent(event);
                }
            });


            //dot line selected value
            chart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
                @Override
                public void onValueSelected(Entry e, Highlight h) {
                    if(isUpScroll) return;

                    CommonUtils.customLog("==", "setOnChartValueSelectedListener", Log.ERROR);

                    int marginEnd = 0;
                    if (mSelectedType == CHART_SELECTED_DAYS) {
                        if(dm.density == 4){
                            marginEnd = (int) dpToPx(11+(24-e.getX()-1)*9.83f);
                        } else if (dm.density == 3.5) {
                            marginEnd = (int) dpToPx(15+(24-e.getX()-1)*11.72f);
                        } else if (dm.density == 3.0) {
                            marginEnd = (int) dpToPx(11+(24-e.getX()-1)*9.83f);
                        } else if (dm.density == 2.0) {
                            marginEnd = (int) dpToPx(11+(24-e.getX()-1)*9.83f);
                        } else if (dm.density == 2.625) {
                            marginEnd = (int) dpToPx(15+(24-e.getX()-1)*11.71f);
                        } else if(dm.density == 2.25){ // ikHwang 2019-10-17 오후 12:11 갤럭시 폴드 그래프
                            if(dm.widthPixels < 900){
                                marginEnd = (int) dpToPx(9+(24-e.getX()-1)*10.42f);
                            }else{
                                marginEnd = (int) dpToPx(34+(24-e.getX()-1)*22.12f);
                            }
                        } else if (dm.density == 2.75) {
                            marginEnd = (int) dpToPx(11+(24-e.getX()-1)*11.20f);
                        }
                    } else if (mSelectedType == CHART_SELECTED_WEEKS) {
                        if(dm.density == 4){
                            marginEnd = (int) dpToPx(5+(7-e.getX()-1)*38.1f);
                        } else if (dm.density == 3.5) {
                            marginEnd = (int) dpToPx(7+(7-e.getX()-1)*45.95f);
                        } else if (dm.density == 3.0) {
                            marginEnd = (int) dpToPx(5+(7-e.getX()-1)*38.1f);
                        } else if (dm.density == 2.0) {
                            marginEnd = (int) dpToPx(5+(7-e.getX()-1)*38.16f);
                        } else if (dm.density == 2.625) {
                            marginEnd = (int) dpToPx(7+(7-e.getX()-1)*45.95f);
                        } else if(dm.density == 2.25){ // ikHwang 2019-10-17 오후 12:11 갤럭시 폴드 그래프
                            if(dm.widthPixels < 900){
                                marginEnd = (int) dpToPx(1+(7-e.getX()-1)*41.42f);
                            }else{
                                marginEnd = (int) dpToPx(25+(7-e.getX()-1)*85.12f);
                            }
                        } else if (dm.density == 2.75) {
                            marginEnd = (int) dpToPx(5+(7-e.getX()-1)*43.37f);
                        }
                    } else if (mSelectedType == CHART_SELECTED_MONTHS) {
                        if(dm.density == 4){
                            marginEnd = (int) dpToPx((31-e.getX()-1)*7.95f);
                        } else if (dm.density == 3.5) {
                            marginEnd = (int) dpToPx(1+(31-e.getX()-1)*9.45f);
                        } else if (dm.density == 3.0) {
                            marginEnd = (int) dpToPx((31-e.getX()-1)*7.95f);
                        } else if (dm.density == 2.0) {
                            marginEnd = (int) dpToPx((31-e.getX()-1)*7.96f);
                        } else if (dm.density == 2.625) {
                            marginEnd = (int) dpToPx(1+(31-e.getX()-1)*9.45f);
                        } else if(dm.density == 2.25){ // ikHwang 2019-10-17 오후 12:11 갤럭시 폴드 그래프
                            if(dm.widthPixels < 900){
                                marginEnd = (int) dpToPx(1+(31-e.getX()-1)*8.35f);
                            }else{
                                marginEnd = (int) dpToPx(10+(31-e.getX()-1)*17.56f);
                            }
                        } else if (dm.density == 2.75) {
                            marginEnd = (int) dpToPx(1+(31-e.getX()-1)*8.90f);
                        }
                    }

                    //수정 - jh.j
                    String value = String.valueOf(Math.round(getChartHighlight(mSelectedType, getArguments().getInt(ARG_SECTION_NUMBER), chart.getId(), (int) e.getX())));//정량값
                    double sensorValue = Math.round(getChartSensorHighlight(mSelectedType, getArguments().getInt(ARG_SECTION_NUMBER), chart.getId(), (int) e.getX()));//정성값

                    //수정 - jh.j
                    switch (chart.getId()) {
                        // ikHwang 2019-06-11 오전 11:39 프리즘 모델 추가
                        case R.id.layoutInsideTotalChart: {
                            RelativeLayout.LayoutParams currentParams = (RelativeLayout.LayoutParams) mLayoutInsideTotalCurrent.getLayoutParams();
                            currentParams.setMarginEnd(marginEnd);
                            mLayoutInsideTotalCurrent.setLayoutParams(currentParams);
                            mTxtInsideTotalInfoData.setBackgroundResource(getIconIdToSensorLevel(geValueLevel(chart.getId(), Double.parseDouble(value))));
                            mTxtInsideTotalInfo.setText(value + "/100");
                            mTxtInsideTotalInfoTime.setText(getChartDateString(mSelectedType,(int)e.getX()));
                            break;
                        }

                        case R.id.layoutInsidePm1Chart: {
                            RelativeLayout.LayoutParams currentParams = (RelativeLayout.LayoutParams) mLayoutInsidePm1Current.getLayoutParams();
                            currentParams.setMarginEnd(marginEnd);
                            mLayoutInsidePm1Current.setLayoutParams(currentParams);
//                            mTxtInsidePm1Value.setText(value);
//                            mTxtInsidePm1InfoData.setBackgroundResource(getIconIdToSensorLevel(sensorValue == 0 ? 0 :(int)(sensorValue + 0.55)));
                            mTxtInsidePm1InfoData.setBackgroundResource(getIconIdToSensorLevel(geValueLevel(chart.getId(), Double.parseDouble(value))));
                            mTxtInsidePm1Info.setText(value+getString(R.string.graph_pm25_unit));
                            mTxtInsidePm1InfoTime.setText(getChartDateString(mSelectedType,(int)e.getX()));
                            break;
                        }

                        case R.id.layoutInsidePm25Chart: {
                            RelativeLayout.LayoutParams currentParams = (RelativeLayout.LayoutParams) mLayoutInsidePm25Current.getLayoutParams();
                            currentParams.setMarginEnd(marginEnd);
                            mLayoutInsidePm25Current.setLayoutParams(currentParams);
                            mTxtInsidePm25InfoData.setBackgroundResource(getIconIdToSensorLevel(geValueLevel(chart.getId(), Double.parseDouble(value))));
                            mTxtInsidePm25Info.setText(value+getString(R.string.graph_pm25_unit));
                            mTxtInsidePm25InfoTime.setText(getChartDateString(mSelectedType,(int)e.getX()));
                            break;
                        }

                        case R.id.layoutInsideCo2Chart: {
                            RelativeLayout.LayoutParams currentParams = (RelativeLayout.LayoutParams) mLayoutInsideCo2Current.getLayoutParams();
                            currentParams.setMarginEnd(marginEnd);
                            mLayoutInsideCo2Current.setLayoutParams(currentParams);
                            mTxtInsideCo2InfoData.setBackgroundResource(getIconIdToSensorLevel(geValueLevel(chart.getId(), Double.parseDouble(value))));
                            mTxtInsideCo2Info.setText(value+getString(R.string.graph_co2_unit));
                            mTxtInsideCo2InfoTime.setText(getChartDateString(mSelectedType,(int)e.getX()));
                            break;
                        }

                        case R.id.layoutInsideTVOCChart: {
                            RelativeLayout.LayoutParams currentParams = (RelativeLayout.LayoutParams) mLayoutInsideTVOCCurrent.getLayoutParams();
                            currentParams.setMarginEnd(marginEnd);
                            mLayoutInsideTVOCCurrent.setLayoutParams(currentParams);
                            mTxtInsideTVOCInfoData.setBackgroundResource(getIconIdToSensorLevel(geValueLevel(chart.getId(), Double.parseDouble(value))));
                            mTxtInsideTVOCInfo.setText(value+getString(R.string.graph_tvoc_unit));
                            mTxtInsideTVOCInfoTime.setText(getChartDateString(mSelectedType,(int)e.getX()));
                            break;
                        }

                        case R.id.layoutInsideTempChart: {
                            RelativeLayout.LayoutParams currentParams = (RelativeLayout.LayoutParams) mLayoutInsideTempCurrent.getLayoutParams();
                            currentParams.setMarginEnd(marginEnd);
                            mLayoutInsideTempCurrent.setLayoutParams(currentParams);
                            mTxtInsideTempInfo.setText(value+getString(R.string.graph_temp_unit));
                            mTxtInsideTempInfoTime.setText(getChartDateString(mSelectedType,(int)e.getX()));
                            break;
                        }

                        case R.id.layoutInsideHumiChart: {
                            RelativeLayout.LayoutParams currentParams = (RelativeLayout.LayoutParams) mLayoutInsideHumiCurrent.getLayoutParams();
                            currentParams.setMarginEnd(marginEnd);
                            mLayoutInsideHumiCurrent.setLayoutParams(currentParams);
                            mTxtInsideHumiInfo.setText(value+getString(R.string.graph_humi_unit));
                            mTxtInsideHumiInfoTime.setText(getChartDateString(mSelectedType,(int)e.getX()));
                            break;
                        }

                        case R.id.layoutOutsidePm25Chart: {
                            RelativeLayout.LayoutParams currentParams = (RelativeLayout.LayoutParams) mLayoutOutsidePm25Current.getLayoutParams();
                            currentParams.setMarginEnd(marginEnd);
                            mLayoutOutsidePm25Current.setLayoutParams(currentParams);
                            mTxtOutsidePm25InfoData.setBackgroundResource(getIconIdToSensorLevel(geValueLevel(chart.getId(), Double.parseDouble(value))));
                            mTxtOutsidePm25Info.setText(value+getString(R.string.graph_pm25_unit));
                            mTxtOutsidePm25InfoDataTime.setText(getChartDateString(mSelectedType,(int)e.getX()));
                            break;
                        }

                        case R.id.layoutOutsidePm10Chart: {
                            RelativeLayout.LayoutParams currentParams = (RelativeLayout.LayoutParams) mLayoutOutsidePm10Current.getLayoutParams();
                            currentParams.setMarginEnd(marginEnd);
                            mLayoutOutsidePm10Current.setLayoutParams(currentParams);
                            mTxtOutsidePm10InfoData.setBackgroundResource(getIconIdToSensorLevel(geValueLevel(chart.getId(), Double.parseDouble(value))));
                            mTxtOutsidePm10Info.setText(value+getString(R.string.graph_pm10_unit));
                            mTxtOutsidePm10InfoDataTime.setText(getChartDateString(mSelectedType,(int)e.getX()));
                            break;
                        }

                        case R.id.layoutOutsideTempChart: {
                            RelativeLayout.LayoutParams currentParams = (RelativeLayout.LayoutParams) mLayoutOutsideTempCurrent.getLayoutParams();
                            currentParams.setMarginEnd(marginEnd);
                            mLayoutOutsideTempCurrent.setLayoutParams(currentParams);
                            mTxtOutsideTempInfo.setText(value+getString(R.string.graph_temp_unit));
                            mTxtOutsideTempInfoTime.setText(getChartDateString(mSelectedType,(int)e.getX()));
                            break;
                        }

                        case R.id.layoutOutsideHumiChart: {
                            RelativeLayout.LayoutParams currentParams = (RelativeLayout.LayoutParams) mLayoutOutsideHumiCurrent.getLayoutParams();
                            currentParams.setMarginEnd(marginEnd);
                            mLayoutOutsideHumiCurrent.setLayoutParams(currentParams);
                            mTxtOutsideHumiInfo.setText(value+getString(R.string.graph_humi_unit));
                            mTxtOutsideHumiInfoTime.setText(getChartDateString(mSelectedType,(int)e.getX()));
                            break;
                        }
                    }

                }

                @Override
                public void onNothingSelected() {}
            });

            setDataForChart(chart);
        }

        /**
         * 차트내 Data 설정 함수
         * @param chart
         */
        private void setDataForChart(LineChart chart){
            chart.resetTracking();

            ArrayList<ILineDataSet> dataSets = new ArrayList<>();
            int XLen;
            if(getArguments().getInt(ARG_SECTION_NUMBER) == CHART_TYPE_INSIDE) {
                if (CHART_SELECTED_DAYS == mSelectedType) {
                    XLen = mLifeReportInfoInsideDay.size();
                } else if (CHART_SELECTED_WEEKS == mSelectedType) {
                    XLen = mLifeReportInfoInsideWeek.size();
                } else {
                    XLen = mLifeReportInfoInsideMonth.size();
                }
            } else {
                if (CHART_SELECTED_DAYS == mSelectedType) {
                    XLen = mLifeReportInfoOutsideDay.size();
                } else if (CHART_SELECTED_WEEKS == mSelectedType) {
                    XLen = mLifeReportInfoOutsideWeek.size();
                } else {
                    XLen = mLifeReportInfoOutsideMonth.size();
                }
            }

            //데이터가 존재하지 않는 경우 그래프 동작 안되게 변경.
            if(XLen == 0){
                return;
            }

            int[] circleColors =  new int[XLen];
            int[] lineColors =  new int[XLen];

            ArrayList<Entry> values = new ArrayList<Entry>();
            for (int i = 0; i < XLen; i++) {

                double val;
                if(getArguments().getInt(ARG_SECTION_NUMBER) == CHART_TYPE_INSIDE) {
                    val = getChartInsideValue(mSelectedType, chart.getId(), i);
                } else {
                    val = getChartOutsideValue(mSelectedType, chart.getId(), i);
                }

                values.add(new Entry(i, (float) val));

                circleColors[i] = mColorchart;
                lineColors[i] = mColorchart;

            }

            LineDataSet d = new LineDataSet(values, "DataSet");
            d.setLineWidth(1f);
            d.setCircleRadius(2f);


            d.setColors(lineColors);
            d.setCircleColors(circleColors);
            d.setDrawCircleHole(false);
            d.setHighlightEnabled(true);
            d.setDrawHighlightIndicators(false);
            d.setValueTextColor(Color.argb(0,0,0,0));
            dataSets.add(d);

            LineData data = new LineData(dataSets);
            chart.setData(data);
            chart.invalidate();
//            chart.animateX(CHART_ANIMATION_TIME);

            //수정 jh.j
            int todayX = (int) values.get(XLen-1).getX();

            String value = String.valueOf(Math.round(getChartHighlight(mSelectedType, getArguments().getInt(ARG_SECTION_NUMBER), chart.getId(), todayX)));//정량값
            double sensorValue = Math.round(getChartSensorHighlight(mSelectedType, getArguments().getInt(ARG_SECTION_NUMBER), chart.getId(), (int) todayX)); //정성값

            switch (chart.getId()) {
                // ikHwang 2019-06-11 오전 11:38 프리즘 모델 추가
                case R.id.layoutInsideTotalChart: {
//                    mTxtInsideTotalInfoData.setBackgroundResource(getIconIdToSensorLevel(sensorValue == 0 ? 0 :(int)(sensorValue + 0.55)));
                    mTxtInsideTotalInfoData.setBackgroundResource(getIconIdToSensorLevel(geValueLevel(chart.getId(), Double.parseDouble(value))));
                    mTxtInsideTotalInfo.setText(value + "/100");
//                    mTxtInsideTotalInfo.setText(value+getString(R.string.graph_pm25_unit));
                    mTxtInsideTotalInfoTime.setText(getChartDateString(mSelectedType,todayX));
                    break;
                }

                case R.id.layoutInsidePm1Chart: {
//                    mTxtInsidePm1InfoData.setBackgroundResource(getIconIdToSensorLevel(sensorValue == 0 ? 0 :(int)(sensorValue + 0.55)));
                    mTxtInsidePm1InfoData.setBackgroundResource(getIconIdToSensorLevel(geValueLevel(chart.getId(), Double.parseDouble(value))));
                    mTxtInsidePm1Info.setText(value+getString(R.string.graph_pm25_unit));
                    mTxtInsidePm1InfoTime.setText(getChartDateString(mSelectedType,todayX));
                    break;
                }

                case R.id.layoutInsidePm25Chart: {
//                    mTxtInsidePm25InfoData.setBackgroundResource(getIconIdToSensorLevel(sensorValue == 0 ? 0 :(int)(sensorValue + 0.55)));
                    mTxtInsidePm25InfoData.setBackgroundResource(getIconIdToSensorLevel(geValueLevel(chart.getId(), Double.parseDouble(value))));
                    mTxtInsidePm25Info.setText(value+getString(R.string.graph_pm25_unit));
                    mTxtInsidePm25InfoTime.setText(getChartDateString(mSelectedType,todayX));
                    break;
                }

                case R.id.layoutInsideCo2Chart: {
//                    mTxtInsideCo2InfoData.setBackgroundResource(getIconIdToSensorLevel(sensorValue == 0 ? 0 :(int)(sensorValue + 0.55)));
                    mTxtInsideCo2InfoData.setBackgroundResource(getIconIdToSensorLevel(geValueLevel(chart.getId(), Double.parseDouble(value))));
                    mTxtInsideCo2Info.setText(value+getString(R.string.graph_co2_unit));
                    mTxtInsideCo2InfoTime.setText(getChartDateString(mSelectedType,todayX));
                    break;
                }

                case R.id.layoutInsideTVOCChart: {
//                    mTxtInsideTVOCInfoData.setBackgroundResource(getIconIdToSensorLevel(sensorValue == 0 ? 0 :(int)(sensorValue + 0.55)));
                    mTxtInsideTVOCInfoData.setBackgroundResource(getIconIdToSensorLevel(geValueLevel(chart.getId(), Double.parseDouble(value))));
                    mTxtInsideTVOCInfo.setText(value+getString(R.string.graph_tvoc_unit));
                    mTxtInsideTVOCInfoTime.setText(getChartDateString(mSelectedType,todayX));
                    break;
                }

                case R.id.layoutInsideTempChart: {
                    mTxtInsideTempInfo.setText(value+getString(R.string.graph_temp_unit));
                    mTxtInsideTempInfoTime.setText(getChartDateString(mSelectedType,todayX));
                    break;
                }

                case R.id.layoutInsideHumiChart: {
                    mTxtInsideHumiInfo.setText(value+getString(R.string.graph_humi_unit));
                    mTxtInsideHumiInfoTime.setText(getChartDateString(mSelectedType,todayX));
                    break;
                }

                case R.id.layoutOutsidePm25Chart: {
//                    mTxtOutsidePm25InfoData.setBackgroundResource(getIconIdToSensorLevel(sensorValue == 0 ? 0 :(int)(sensorValue + 0.55)));
                    mTxtOutsidePm25InfoData.setBackgroundResource(getIconIdToSensorLevel(geValueLevel(chart.getId(), Double.parseDouble(value))));
                    mTxtOutsidePm25Info.setText(value+getString(R.string.graph_pm25_unit));
                    mTxtOutsidePm25InfoDataTime.setText(getChartDateString(mSelectedType,todayX));
                    break;
                }

                case R.id.layoutOutsidePm10Chart: {
//                    mTxtOutsidePm10InfoData.setBackgroundResource(getIconIdToSensorLevel(sensorValue == 0 ? 0 :(int)(sensorValue + 0.55)));
                    mTxtOutsidePm10InfoData.setBackgroundResource(getIconIdToSensorLevel(geValueLevel(chart.getId(), Double.parseDouble(value))));
                    mTxtOutsidePm10Info.setText(value+getString(R.string.graph_pm10_unit));
                    mTxtOutsidePm10InfoDataTime.setText(getChartDateString(mSelectedType,todayX));
                    break;
                }

                case R.id.layoutOutsideTempChart: {
                    mTxtOutsideTempInfo.setText(value+getString(R.string.graph_temp_unit));
                    mTxtOutsideTempInfoTime.setText(getChartDateString(mSelectedType,todayX));
                    break;
                }

                case R.id.layoutOutsideHumiChart: {
                    mTxtOutsideHumiInfo.setText(value+getString(R.string.graph_humi_unit));
                    mTxtOutsideHumiInfoTime.setText(getChartDateString(mSelectedType,todayX));
                    break;
                }
            }

//            mBtnReplace.setEnabled(false);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
//                    mBtnReplace.setEnabled(true);
                    if(getArguments().getInt(ARG_SECTION_NUMBER) == CHART_TYPE_INSIDE) {
                        // ikHwang 2019-06-11 오전 11:37 프리즘 모델 추가
                        mLayoutInsideTotalCurrent.setVisibility(View.VISIBLE);
                        mLayoutInsidePm1Current.setVisibility(View.VISIBLE);

                        mLayoutInsidePm25Current.setVisibility(View.VISIBLE);
                        mLayoutInsideCo2Current.setVisibility(View.VISIBLE);
                        mLayoutInsideTVOCCurrent.setVisibility(View.VISIBLE);
                        mLayoutInsideTempCurrent.setVisibility(View.VISIBLE);
                        mLayoutInsideHumiCurrent.setVisibility(View.VISIBLE);
                    } else {
                        mLayoutOutsidePm25Current.setVisibility(View.VISIBLE);
                        mLayoutOutsidePm10Current.setVisibility(View.VISIBLE);
                        mLayoutOutsideTempCurrent.setVisibility(View.VISIBLE);
                        mLayoutOutsideHumiCurrent.setVisibility(View.VISIBLE);
                    }
                }
            },CHART_ANIMATION_TIME+200);
        }

        /**
         * 일,주,월 별 선택된 버튼 설정
         * @param type
         */
        private void changeSelectedType(int type){
            switch (type){
                case CHART_SELECTED_DAYS: {
                    mTxtDays.setTextColor(Color.rgb(0xFF, 0xFF, 0xFF));
                    mTxtDays.setBackgroundResource(R.drawable.bg_report_day_active);
                    mTxtWeeks.setTextColor(Color.rgb(0x66, 0x66, 0x66));
                    mTxtWeeks.setBackgroundResource(R.drawable.bg_report_week);
                    mTxtMonths.setTextColor(Color.rgb(0x66, 0x66, 0x66));
                    mTxtMonths.setBackgroundResource(R.drawable.bg_report_month);

                    //수정 - jh.j
                    SimpleDateFormat sdf = new SimpleDateFormat(getString(R.string.LifeReportActivity_str_1), locale);
                    String currentTime = sdf.format(daysDate);
                    String today = sdf.format(new Date());
                    try {
                        /*Date currentTemp = sdf.parse(currentTime);
                        Date todayTemp = sdf.parse(today);*/

                        int compare = currentTime.compareTo(today);
                        if(compare == 0){
                            mTxtCurrentTime.setText(getString(R.string.current_day));
                        }else {
                            mTxtCurrentTime.setText(currentTime);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                }

                case CHART_SELECTED_WEEKS: {
                    mTxtDays.setTextColor(Color.rgb(0x66, 0x66, 0x66));
                    mTxtDays.setBackgroundResource(R.drawable.bg_report_day);
                    mTxtWeeks.setTextColor(Color.rgb(0xFF, 0xFF, 0xFF));
                    mTxtWeeks.setBackgroundResource(R.drawable.bg_report_week_active);
                    mTxtMonths.setTextColor(Color.rgb(0x66, 0x66, 0x66));
                    mTxtMonths.setBackgroundResource(R.drawable.bg_report_month);

                    SimpleDateFormat sdf1 = new SimpleDateFormat(getString(R.string.LifeReportActivity_str_2), locale);
                    SimpleDateFormat sdf2 = new SimpleDateFormat(getString(R.string.LifeReportActivity_str_3), locale);
                    Calendar cal = Calendar.getInstance();
                    cal.setTime(weeksDate);
                    cal.add(Calendar.DATE, 6);

                    //수정 - jh.j
                    String beginTime = sdf1.format(weeksDate);
                    String endTime = sdf1.format(cal.getTime());

                    String today = sdf1.format(new Date());
                    try {
                        Date beginTemp = sdf1.parse(beginTime);
                        Date endTemp = sdf1.parse(endTime);
                        Date todayTemp = sdf1.parse(today);

                        if(todayTemp.before(endTemp) && todayTemp.after(beginTemp) || todayTemp.compareTo(endTemp) == 0 || todayTemp.compareTo(beginTemp) == 0){
                            mTxtCurrentTime.setText(getString(R.string.current_week));
                        }else {
                            String currentTime = sdf1.format(weeksDate) + "~" + sdf2.format(cal.getTime());
                            mTxtCurrentTime.setText(currentTime);
                        }

                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    break;
                }

                case CHART_SELECTED_MONTHS: {
                    mTxtDays.setTextColor(Color.rgb(0x66, 0x66, 0x66));
                    mTxtDays.setBackgroundResource(R.drawable.bg_report_day);
                    mTxtWeeks.setTextColor(Color.rgb(0x66, 0x66, 0x66));
                    mTxtWeeks.setBackgroundResource(R.drawable.bg_report_week);
                    mTxtMonths.setTextColor(Color.rgb(0xFF, 0xFF, 0xFF));
                    mTxtMonths.setBackgroundResource(R.drawable.bg_report_month_active);

                    SimpleDateFormat sdf = new SimpleDateFormat(getString(R.string.LifeReportActivity_str_4), locale);
                    Calendar cal1 = Calendar.getInstance();
                    cal1.setTime(monthsDate);
                    Calendar cal2 = Calendar.getInstance();
                    cal2.setTime(new Date());
                    int selectedMonth = cal1.get(Calendar.MONTH)+1;
                    int todayMonth = cal2.get(Calendar.MONTH)+1;
                    int selectedYear = cal1.get(Calendar.YEAR);
                    int todayYear = cal2.get(Calendar.YEAR);

                    if(selectedYear >= todayYear){ //선택날짜 년도가 오늘날짜 년도보다 크거나 같을때만
                        if(selectedMonth == todayMonth){
                            mTxtCurrentTime.setText(getString(R.string.current_month));
                        }else {
                            String currentTime = sdf.format(monthsDate);
                            mTxtCurrentTime.setText(currentTime);
                        }
                    }else {
                        String currentTime = sdf.format(monthsDate);
                        mTxtCurrentTime.setText(currentTime);
                    }
                    break;
                }
            }
            mSelectedType = type;
            changeChartXlabelText(type);
        }

        /**
         * 현재 공기질 및 온도, 습도 표시 함수
         * @param type
         */
        private void changeCurrentValue(int type){
            LifeReportInfo lifeReportInfo = null;
            switch (type){
                case CHART_SELECTED_DAYS:
                    lifeReportInfo = mCurrLifeReportDay;
                    break;

                case CHART_SELECTED_WEEKS:
                    lifeReportInfo = mCurrLifeReportWeek;
                    break;

                case CHART_SELECTED_MONTHS:
                    lifeReportInfo = mCurrLifeReportMonth;
                    break;
            }

            if(lifeReportInfo != null){

                //수정 - jh.j
                // ikHwang 2019-06-11 오전 11:35 프리즘 모델 추가
                mTxtInsideTotalInfoData.setBackgroundResource(getIconIdToSensorLevel(lifeReportInfo.totalSensor == 0 ? 0 :(int)(lifeReportInfo.totalSensor + 0.55)));
                mTxtInsidePm1InfoData.setBackgroundResource(getIconIdToSensorLevel(lifeReportInfo.pm1Sensor == 0 ? 0 :(int)(lifeReportInfo.pm1Sensor + 0.55)));

                mTxtInsidePm25InfoData.setBackgroundResource(getIconIdToSensorLevel(lifeReportInfo.DustSensor == 0 ? 0 :(int)(lifeReportInfo.DustSensor + 0.55)));
                mTxtInsideCo2InfoData.setBackgroundResource(getIconIdToSensorLevel(lifeReportInfo.CO2Sensor == 0 ? 0 :(int)(lifeReportInfo.CO2Sensor + 0.55)));
                mTxtInsideTVOCInfoData.setBackgroundResource(getIconIdToSensorLevel(lifeReportInfo.GASSensor == 0 ? 0 :(int)(lifeReportInfo.GASSensor + 0.55)));
                mTxtOutsidePm25InfoData.setBackgroundResource(getIconIdToSensorLevel(lifeReportInfo.pm25Sensor == 0 ? 0 :(int)(lifeReportInfo.pm25Sensor + 0.55)));
                mTxtOutsidePm10InfoData.setBackgroundResource(getIconIdToSensorLevel(lifeReportInfo.pm10Sensor == 0 ? 0 :(int)(lifeReportInfo.pm10Sensor + 0.55)));

                // ikHwang 2019-06-11 오전 11:34 프리즘 모델 추가
                mTxtInsideTotalInfo.setText(String.format("%d",Math.round(lifeReportInfo.totalValue)) + "/100");
//                mTxtInsideTotalInfo.setText(String.format("%d",Math.round(lifeReportInfo.totalValue))+getString(R.string.graph_pm25_unit));

                mTxtInsidePm1Info.setText(String.format("%d",Math.round(lifeReportInfo.pm1Value))+getString(R.string.graph_pm25_unit));

                mTxtInsidePm25Info.setText(String.format("%d",Math.round(lifeReportInfo.DustValue))+getString(R.string.graph_pm25_unit));
                mTxtInsideCo2Info.setText(String.format("%d",Math.round(lifeReportInfo.CO2Value))+getString(R.string.graph_co2_unit));
                mTxtInsideTVOCInfo.setText(String.format("%d",Math.round(lifeReportInfo.GASValue))+getString(R.string.graph_tvoc_unit));
                mTxtInsideTempInfo.setText(String.format("%d",Math.round(lifeReportInfo.SensorTemp))+getString(R.string.graph_temp_unit));
                mTxtInsideHumiInfo.setText(String.format("%d",Math.round(lifeReportInfo.SensorHum))+getString(R.string.graph_humi_unit));
                mTxtOutsidePm25Info.setText(String.format("%d",Math.round(lifeReportInfo.pm25Value))+getString(R.string.graph_pm25_unit));
                mTxtOutsidePm10Info.setText(String.format("%d",Math.round(lifeReportInfo.pm10Value))+getString(R.string.graph_pm10_unit));
                mTxtOutsideTempInfo.setText(String.format("%d",Math.round(lifeReportInfo.outsideTemp))+getString(R.string.graph_temp_unit));
                mTxtOutsideHumiInfo.setText(String.format("%d",Math.round(lifeReportInfo.outsideHum))+getString(R.string.graph_humi_unit));
            }
        }

        /**
         * 선택된 그래프 날짜 반환.
         * @param type 일/주/월 타입
         * @param chartX 선택된 그래프 x축 index
         * @return 날짜 String
         */
        //추가 jh.j
        private String getChartDateString(int type, int chartX){
            switch (type){
                case CHART_SELECTED_DAYS:
                    String tempStr = chartX + ":00";

                    SimpleDateFormat sdf = new SimpleDateFormat("M월 d일", locale);
                    String currentTime = sdf.format(daysDate);
                    String today = sdf.format(new Date());
                    try {
                        Date currentTemp = sdf.parse(currentTime);
                        Date todayTemp = sdf.parse(today);

                        int compare = currentTemp.compareTo(todayTemp);
                        if(compare == 0){
                            return getString(R.string.current_day)+ " " + tempStr;
                        }else {
                            return currentTime + " " + tempStr;
                        }
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    break;

                case CHART_SELECTED_WEEKS:
                    SimpleDateFormat sdf1 = new SimpleDateFormat("M월 d일", locale);
                    Calendar cal = Calendar.getInstance();
                    cal.setTime(weeksDate);
                    cal.add(Calendar.DATE, chartX);
                    String weekDay = sdf1.format(cal.getTime());
                    return weekDay;

                case CHART_SELECTED_MONTHS:
                    SimpleDateFormat sdf2 = new SimpleDateFormat("M월 d일", locale);
                    Calendar cal1 = Calendar.getInstance();
                    cal1.setTime(monthsDate);
                    cal1.add(Calendar.DATE, chartX);
                    String monthDay = sdf2.format(cal1.getTime());
                    return monthDay;
            }
            return getString(R.string.unknown);
        }

        /**
         * 그래프 X좌표 Label 설정.
         * @param type
         */
        private void changeChartXlabelText(int type){
            SimpleDateFormat sdf = new SimpleDateFormat("E d", Locale.KOREA);
            switch (getArguments().getInt(ARG_SECTION_NUMBER)) {
                case CHART_TYPE_INSIDE:
                    switch (type){
                        case CHART_SELECTED_DAYS:
                            for(int i = 0; i < mTxtInsidePm25GraphUnderInfos.length; i++){
                                // ikHwang 2019-06-11 오전 11:30 프리즘 모델 추가
                                mTxtInsideTotalGraphUnderInfos[i].setText(String.format("%02d", i * 3));
                                mTxtInsidePm1GraphUnderInfos[i].setText(String.format("%02d", i * 3));

                                mTxtInsidePm25GraphUnderInfos[i].setText(String.format("%02d", i * 3));
                                mTxtInsideCo2GraphUnderInfos[i].setText(String.format("%02d", i * 3));
                                mTxtInsideTVOCGraphUnderInfos[i].setText(String.format("%02d", i * 3));
                                mTxtInsideTempGraphUnderInfos[i].setText(String.format("%02d", i * 3));
                                mTxtInsideHumiGraphUnderInfos[i].setText(String.format("%02d", i * 3));
                            }
                            break;

                        case CHART_SELECTED_WEEKS:
                            for(int i = 0; i < mTxtInsidePm25GraphUnderInfos.length; i++){
                                Calendar cal = Calendar.getInstance();
                                cal.setTime(weeksDate);
                                cal.add(Calendar.DATE, i);

                                if (i < 7) {
                                    // ikHwang 2019-06-11 오전 11:30 프리즘 모델 추가
                                    mTxtInsideTotalGraphUnderInfos[i].setText(sdf.format(cal.getTime()));
                                    mTxtInsidePm1GraphUnderInfos[i].setText(sdf.format(cal.getTime()));

                                    mTxtInsidePm25GraphUnderInfos[i].setText(sdf.format(cal.getTime()));
                                    mTxtInsideCo2GraphUnderInfos[i].setText(sdf.format(cal.getTime()));
                                    mTxtInsideTVOCGraphUnderInfos[i].setText(sdf.format(cal.getTime()));
                                    mTxtInsideTempGraphUnderInfos[i].setText(sdf.format(cal.getTime()));
                                    mTxtInsideHumiGraphUnderInfos[i].setText(sdf.format(cal.getTime()));
                                } else {
                                    // ikHwang 2019-06-11 오전 11:30 프리즘 모델 추가
                                    ((LinearLayout) mTxtInsideTotalGraphUnderInfos[i].getParent()).setVisibility(View.GONE);
                                    ((LinearLayout) mTxtInsidePm1GraphUnderInfos[i].getParent()).setVisibility(View.GONE);

                                    ((LinearLayout) mTxtInsidePm25GraphUnderInfos[i].getParent()).setVisibility(View.GONE);
                                    ((LinearLayout) mTxtInsideCo2GraphUnderInfos[i].getParent()).setVisibility(View.GONE);
                                    ((LinearLayout) mTxtInsideTVOCGraphUnderInfos[i].getParent()).setVisibility(View.GONE);
                                    ((LinearLayout) mTxtInsideTempGraphUnderInfos[i].getParent()).setVisibility(View.GONE);
                                    ((LinearLayout) mTxtInsideHumiGraphUnderInfos[i].getParent()).setVisibility(View.GONE);
                                }
                            }
                            break;

                        case CHART_SELECTED_MONTHS:
                            for(int i = 0; i < mTxtInsidePm25GraphUnderInfos.length; i++){
                                if (i < 7) {
                                    // ikHwang 2019-06-11 오전 11:33 프리즘 모델 추가
                                    mTxtInsideTotalGraphUnderInfos[i].setText(String.format("%d", i == 0 ? 1 : i * 5));
                                    mTxtInsidePm1GraphUnderInfos[i].setText(String.format("%d", i == 0 ? 1 : i * 5));

                                    mTxtInsidePm25GraphUnderInfos[i].setText(String.format("%d", i == 0 ? 1 : i * 5));
                                    mTxtInsideCo2GraphUnderInfos[i].setText(String.format("%d", i == 0 ? 1 : i * 5));
                                    mTxtInsideTVOCGraphUnderInfos[i].setText(String.format("%d", i == 0 ? 1 : i * 5));
                                    mTxtInsideTempGraphUnderInfos[i].setText(String.format("%d", i == 0 ? 1 : i * 5));
                                    mTxtInsideHumiGraphUnderInfos[i].setText(String.format("%d", i == 0 ? 1 : i * 5));
                                } else {
                                    // ikHwang 2019-06-11 오전 11:33 프리즘 모델 추가
                                    ((LinearLayout) mTxtInsideTotalGraphUnderInfos[i].getParent()).setVisibility(View.GONE);
                                    ((LinearLayout) mTxtInsidePm1GraphUnderInfos[i].getParent()).setVisibility(View.GONE);

                                    ((LinearLayout) mTxtInsidePm25GraphUnderInfos[i].getParent()).setVisibility(View.GONE);
                                    ((LinearLayout) mTxtInsideCo2GraphUnderInfos[i].getParent()).setVisibility(View.GONE);
                                    ((LinearLayout) mTxtInsideTVOCGraphUnderInfos[i].getParent()).setVisibility(View.GONE);
                                    ((LinearLayout) mTxtInsideTempGraphUnderInfos[i].getParent()).setVisibility(View.GONE);
                                    ((LinearLayout) mTxtInsideHumiGraphUnderInfos[i].getParent()).setVisibility(View.GONE);
                                }
                            }
                            break;
                    }
                    break;

                case CHART_TYPE_OUTSIDE:
                    switch (type){
                        case CHART_SELECTED_DAYS:
                            for(int i = 0; i < mTxtOutsidePm25GraphUnderInfos.length; i++){
                                mTxtOutsidePm25GraphUnderInfos[i].setText(String.format("%02d", i * 3));
                                mTxtOutsidePm10GraphUnderInfos[i].setText(String.format("%02d", i * 3));
                                mTxtOutsideTempGraphUnderInfos[i].setText(String.format("%02d", i * 3));
                                mTxtOutsideHumiGraphUnderInfos[i].setText(String.format("%02d", i * 3));
                            }
                            break;

                        case CHART_SELECTED_WEEKS:
                            for(int i = 0; i < mTxtOutsidePm25GraphUnderInfos.length; i++){
                                Calendar cal = Calendar.getInstance();
                                cal.setTime(weeksDate);
                                cal.add(Calendar.DATE, i);

                                if (i < 7) {
                                    mTxtOutsidePm25GraphUnderInfos[i].setText(sdf.format(cal.getTime()));
                                    mTxtOutsidePm10GraphUnderInfos[i].setText(sdf.format(cal.getTime()));
                                    mTxtOutsideTempGraphUnderInfos[i].setText(sdf.format(cal.getTime()));
                                    mTxtOutsideHumiGraphUnderInfos[i].setText(sdf.format(cal.getTime()));
                                } else {
                                    ((LinearLayout) mTxtOutsidePm25GraphUnderInfos[i].getParent()).setVisibility(View.GONE);
                                    ((LinearLayout) mTxtOutsidePm10GraphUnderInfos[i].getParent()).setVisibility(View.GONE);
                                    ((LinearLayout) mTxtOutsideTempGraphUnderInfos[i].getParent()).setVisibility(View.GONE);
                                    ((LinearLayout) mTxtOutsideHumiGraphUnderInfos[i].getParent()).setVisibility(View.GONE);
                                }
                            }
                            break;

                        case CHART_SELECTED_MONTHS:
                            for(int i = 0; i < mTxtOutsidePm25GraphUnderInfos.length; i++){
                                if (i < 7) {
                                    mTxtOutsidePm25GraphUnderInfos[i].setText(String.format("%d", i == 0 ? 1 : i * 5));
                                    mTxtOutsidePm10GraphUnderInfos[i].setText(String.format("%d", i == 0 ? 1 : i * 5));
                                    mTxtOutsideTempGraphUnderInfos[i].setText(String.format("%d", i == 0 ? 1 : i * 5));
                                    mTxtOutsideHumiGraphUnderInfos[i].setText(String.format("%d", i == 0 ? 1 : i * 5));
                                } else {
                                    ((LinearLayout) mTxtOutsidePm25GraphUnderInfos[i].getParent()).setVisibility(View.GONE);
                                    ((LinearLayout) mTxtOutsidePm10GraphUnderInfos[i].getParent()).setVisibility(View.GONE);
                                    ((LinearLayout) mTxtOutsideTempGraphUnderInfos[i].getParent()).setVisibility(View.GONE);
                                    ((LinearLayout) mTxtOutsideHumiGraphUnderInfos[i].getParent()).setVisibility(View.GONE);
                                }
                            }
                            break;
                    }
                    break;
            }
        }

        /**
         * 차트 내에 필요한 컴퍼넌트 초기화 함수.
         * @param rootView
         */
        private void initComponent(View rootView){
            mTxtCurrentTime = rootView.findViewById(R.id.txtCurrentTime);
            mTxtDays = rootView.findViewById(R.id.txtDays);
            mTxtWeeks = rootView.findViewById(R.id.txtWeeks);
            mTxtMonths = rootView.findViewById(R.id.txtMonths);
            btnPrevTime = rootView.findViewById(R.id.btnPrevTime);
            btnNextTime = rootView.findViewById(R.id.btnNextTime);

            // ikHwang 2019-06-11 오전 11:14 프리즘 모델 추가
            mTxtInsideTotalInfo = rootView.findViewById(R.id.txtInsideTotalInfo);
            mTxtInsidePm1Info = rootView.findViewById(R.id.txtInsidePm1Info);

            mTxtInsidePm25Info = rootView.findViewById(R.id.txtInsidePm25Info);
            mTxtInsideCo2Info = rootView.findViewById(R.id.txtInsideCo2Info);
            mTxtInsideTVOCInfo = rootView.findViewById(R.id.txtInsideTVOCInfo);
            mTxtInsideTempInfo = rootView.findViewById(R.id.txtInsideTempInfo);
            mTxtInsideHumiInfo = rootView.findViewById(R.id.txtInsideHumiInfo);
            mTxtOutsidePm25Info = rootView.findViewById(R.id.txtOutsidePm25Info);
            mTxtOutsidePm10Info = rootView.findViewById(R.id.txtOutsidePm10Info);
            mTxtOutsideTempInfo = rootView.findViewById(R.id.txtOutsideTempInfo);
            mTxtOutsideHumiInfo = rootView.findViewById(R.id.txtOutsideHumiInfo);

            // ikHwang 2019-06-11 오전 11:15 프리즘 모델 추가
            mTxtInsideTotalInfoData = rootView.findViewById(R.id.txtInsideTotalInfoData);
            mTxtInsidePm1InfoData = rootView.findViewById(R.id.txtInsidePm1InfoData);

            mTxtInsidePm25InfoData = rootView.findViewById(R.id.txtInsidePm25InfoData);
            mTxtInsideCo2InfoData = rootView.findViewById(R.id.txtInsideCo2InfoData);
            mTxtInsideTVOCInfoData = rootView.findViewById(R.id.txtInsideTVOCInfoData);
            mTxtOutsidePm25InfoData = rootView.findViewById(R.id.txtOutsidePm25InfoData);
            mTxtOutsidePm10InfoData = rootView.findViewById(R.id.txtOutsidePm10InfoData);

            //추가 jh.j
            //실내 - 시간정보
            // ikHwang 2019-06-11 오전 11:16 프리즘 모델 추가
            mTxtInsideTotalInfoTime = rootView.findViewById(R.id.txtInsideTotalInfoTime);
            mTxtInsidePm1InfoTime = rootView.findViewById(R.id.txtInsidePm1InfoTime);

            mTxtInsidePm25InfoTime = rootView.findViewById(R.id.txtInsidePm25InfoTime);
            mTxtInsideCo2InfoTime = rootView.findViewById(R.id.txtInsideCo2InfoTime);
            mTxtInsideTVOCInfoTime = rootView.findViewById(R.id.txtInsideTVOCInfoTime);
            mTxtInsideTempInfoTime = rootView.findViewById(R.id.txtInsideTempInfoTime);
            mTxtInsideHumiInfoTime = rootView.findViewById(R.id.txtInsideHumiInfoTime);
            //실외 - 시간정보
            mTxtOutsidePm25InfoDataTime = rootView.findViewById(R.id.txtOutsidePm25InfoTime);
            mTxtOutsidePm10InfoDataTime = rootView.findViewById(R.id.txtOutsidePm10InfoTime);
            mTxtOutsideTempInfoTime = rootView.findViewById(R.id.txtOutsideTempInfoTime);
            mTxtOutsideHumiInfoTime = rootView.findViewById(R.id.txtOutsideHumiInfoTime);

            // ikHwang 2019-06-11 오전 11:16 프리즘 모델 추가
            mInsideTotalChart = rootView.findViewById(R.id.layoutInsideTotalChart);
            mInsidePm1Chart = rootView.findViewById(R.id.layoutInsidePm1Chart);

            mInsidePm25Chart = rootView.findViewById(R.id.layoutInsidePm25Chart);
            mInsideCo2Chart = rootView.findViewById(R.id.layoutInsideCo2Chart);
            mInsideTVOCChart = rootView.findViewById(R.id.layoutInsideTVOCChart);
            mInsideTempChart = rootView.findViewById(R.id.layoutInsideTempChart);
            mInsideHumiChart = rootView.findViewById(R.id.layoutInsideHumiChart);
            mOutsidePm25Chart = rootView.findViewById(R.id.layoutOutsidePm25Chart);
            mOutsidePm10Chart = rootView.findViewById(R.id.layoutOutsidePm10Chart);
            mOutsideTempChart = rootView.findViewById(R.id.layoutOutsideTempChart);
            mOutsideHumiChart = rootView.findViewById(R.id.layoutOutsideHumiChart);

            // ikHwang 2019-06-11 오전 11:17 프리즘 모델 추가
            mLayoutInsideTotalGraphUnderInfos = rootView.findViewById(R.id.layoutInsideTotalGraphUnderInfos);
            mLayoutInsidePm1GraphUnderInfos = rootView.findViewById(R.id.layoutInsidePm1GraphUnderInfos);

            mLayoutInsidePm25GraphUnderInfos = rootView.findViewById(R.id.layoutInsidePm25GraphUnderInfos);
            mLayoutInsideCo2GraphUnderInfos = rootView.findViewById(R.id.layoutInsideCo2GraphUnderInfos);
            mLayoutInsideTVOCGraphUnderInfos = rootView.findViewById(R.id.layoutInsideTVOCGraphUnderInfos);
            mLayoutInsideTempGraphUnderInfos = rootView.findViewById(R.id.layoutInsideTempGraphUnderInfos);
            mLayoutInsideHumiGraphUnderInfos = rootView.findViewById(R.id.layoutInsideHumiGraphUnderInfos);
            mLayoutOutsidePm25GraphUnderInfos = rootView.findViewById(R.id.layoutOutsidePm25GraphUnderInfos);
            mLayoutOutsidePm10GraphUnderInfos = rootView.findViewById(R.id.layoutOutsidePm10GraphUnderInfos);
            mLayoutOutsideTempGraphUnderInfos = rootView.findViewById(R.id.layoutOutsideTempGraphUnderInfos);
            mLayoutOutsideHumiGraphUnderInfos = rootView.findViewById(R.id.layoutOutsideHumiGraphUnderInfos);

            // ikHwang 2019-06-11 오전 11:17 프리즘 모델 추가
            mLayoutInsideTotalCurrent = rootView.findViewById(R.id.layoutInsideTotalCurrent);
            mLayoutInsidePm1Current = rootView.findViewById(R.id.layoutInsidePm1Current);

            mLayoutInsidePm25Current = rootView.findViewById(R.id.layoutInsidePm25Current);
            mLayoutInsideCo2Current = rootView.findViewById(R.id.layoutInsideCo2Current);
            mLayoutInsideTVOCCurrent = rootView.findViewById(R.id.layoutInsideTVOCCurrent);
            mLayoutInsideTempCurrent = rootView.findViewById(R.id.layoutInsideTempCurrent);
            mLayoutInsideHumiCurrent = rootView.findViewById(R.id.layoutInsideHumiCurrent);
            mLayoutOutsidePm25Current = rootView.findViewById(R.id.layoutOutsidePm25Current);
            mLayoutOutsidePm10Current = rootView.findViewById(R.id.layoutOutsidePm10Current);
            mLayoutOutsideTempCurrent = rootView.findViewById(R.id.layoutOutsideTempCurrent);
            mLayoutOutsideHumiCurrent = rootView.findViewById(R.id.layoutOutsideHumiCurrent);

            // ikHwang 2019-06-11 오전 11:18 프리즘 모델 추가
            mLayoutInsideTotalCurrent.setVisibility(View.INVISIBLE);
            mLayoutInsidePm1Current.setVisibility(View.INVISIBLE);

            mLayoutInsidePm25Current.setVisibility(View.INVISIBLE);
            mLayoutInsideCo2Current.setVisibility(View.INVISIBLE);
            mLayoutInsideTVOCCurrent.setVisibility(View.INVISIBLE);
            mLayoutInsideTempCurrent.setVisibility(View.INVISIBLE);
            mLayoutInsideHumiCurrent.setVisibility(View.INVISIBLE);
            mLayoutOutsidePm25Current.setVisibility(View.INVISIBLE);
            mLayoutOutsidePm10Current.setVisibility(View.INVISIBLE);
            mLayoutOutsideTempCurrent.setVisibility(View.INVISIBLE);
            mLayoutOutsideHumiCurrent.setVisibility(View.INVISIBLE);

            // ikHwang 2019-06-11 오전 11:18 프리즘 모델 추가
            mTxtInsideTotalGraphUnderInfos = new TextView[]{
                    rootView.findViewById(R.id.txtInsideTotalGraphUnderInfo0),
                    rootView.findViewById(R.id.txtInsideTotalGraphUnderInfo1),
                    rootView.findViewById(R.id.txtInsideTotalGraphUnderInfo2),
                    rootView.findViewById(R.id.txtInsideTotalGraphUnderInfo3),
                    rootView.findViewById(R.id.txtInsideTotalGraphUnderInfo4),
                    rootView.findViewById(R.id.txtInsideTotalGraphUnderInfo5),
                    rootView.findViewById(R.id.txtInsideTotalGraphUnderInfo6),
                    rootView.findViewById(R.id.txtInsideTotalGraphUnderInfo7),
                    rootView.findViewById(R.id.txtInsideTotalGraphUnderInfo8),
            };

            mTxtInsidePm1GraphUnderInfos = new TextView[]{
                    rootView.findViewById(R.id.txtInsidePm1GraphUnderInfo0),
                    rootView.findViewById(R.id.txtInsidePm1GraphUnderInfo1),
                    rootView.findViewById(R.id.txtInsidePm1GraphUnderInfo2),
                    rootView.findViewById(R.id.txtInsidePm1GraphUnderInfo3),
                    rootView.findViewById(R.id.txtInsidePm1GraphUnderInfo4),
                    rootView.findViewById(R.id.txtInsidePm1GraphUnderInfo5),
                    rootView.findViewById(R.id.txtInsidePm1GraphUnderInfo6),
                    rootView.findViewById(R.id.txtInsidePm1GraphUnderInfo7),
                    rootView.findViewById(R.id.txtInsidePm1GraphUnderInfo8),
            };

            mTxtInsidePm25GraphUnderInfos = new TextView[]{
                    rootView.findViewById(R.id.txtInsidePm25GraphUnderInfo0),
                    rootView.findViewById(R.id.txtInsidePm25GraphUnderInfo1),
                    rootView.findViewById(R.id.txtInsidePm25GraphUnderInfo2),
                    rootView.findViewById(R.id.txtInsidePm25GraphUnderInfo3),
                    rootView.findViewById(R.id.txtInsidePm25GraphUnderInfo4),
                    rootView.findViewById(R.id.txtInsidePm25GraphUnderInfo5),
                    rootView.findViewById(R.id.txtInsidePm25GraphUnderInfo6),
                    rootView.findViewById(R.id.txtInsidePm25GraphUnderInfo7),
                    rootView.findViewById(R.id.txtInsidePm25GraphUnderInfo8),
            };

            mTxtInsideCo2GraphUnderInfos = new TextView[]{
                    rootView.findViewById(R.id.txtInsideCo2GraphUnderInfo0),
                    rootView.findViewById(R.id.txtInsideCo2GraphUnderInfo1),
                    rootView.findViewById(R.id.txtInsideCo2GraphUnderInfo2),
                    rootView.findViewById(R.id.txtInsideCo2GraphUnderInfo3),
                    rootView.findViewById(R.id.txtInsideCo2GraphUnderInfo4),
                    rootView.findViewById(R.id.txtInsideCo2GraphUnderInfo5),
                    rootView.findViewById(R.id.txtInsideCo2GraphUnderInfo6),
                    rootView.findViewById(R.id.txtInsideCo2GraphUnderInfo7),
                    rootView.findViewById(R.id.txtInsideCo2GraphUnderInfo8),
            };

            mTxtInsideTVOCGraphUnderInfos = new TextView[]{
                    rootView.findViewById(R.id.txtInsideTVOCGraphUnderInfo0),
                    rootView.findViewById(R.id.txtInsideTVOCGraphUnderInfo1),
                    rootView.findViewById(R.id.txtInsideTVOCGraphUnderInfo2),
                    rootView.findViewById(R.id.txtInsideTVOCGraphUnderInfo3),
                    rootView.findViewById(R.id.txtInsideTVOCGraphUnderInfo4),
                    rootView.findViewById(R.id.txtInsideTVOCGraphUnderInfo5),
                    rootView.findViewById(R.id.txtInsideTVOCGraphUnderInfo6),
                    rootView.findViewById(R.id.txtInsideTVOCGraphUnderInfo7),
                    rootView.findViewById(R.id.txtInsideTVOCGraphUnderInfo8),
            };

            mTxtInsideTempGraphUnderInfos = new TextView[]{
                    rootView.findViewById(R.id.txtInsideTempGraphUnderInfo0),
                    rootView.findViewById(R.id.txtInsideTempGraphUnderInfo1),
                    rootView.findViewById(R.id.txtInsideTempGraphUnderInfo2),
                    rootView.findViewById(R.id.txtInsideTempGraphUnderInfo3),
                    rootView.findViewById(R.id.txtInsideTempGraphUnderInfo4),
                    rootView.findViewById(R.id.txtInsideTempGraphUnderInfo5),
                    rootView.findViewById(R.id.txtInsideTempGraphUnderInfo6),
                    rootView.findViewById(R.id.txtInsideTempGraphUnderInfo7),
                    rootView.findViewById(R.id.txtInsideTempGraphUnderInfo8),
            };
            mTxtInsideHumiGraphUnderInfos = new TextView[]{
                    rootView.findViewById(R.id.txtInsideHumiGraphUnderInfo0),
                    rootView.findViewById(R.id.txtInsideHumiGraphUnderInfo1),
                    rootView.findViewById(R.id.txtInsideHumiGraphUnderInfo2),
                    rootView.findViewById(R.id.txtInsideHumiGraphUnderInfo3),
                    rootView.findViewById(R.id.txtInsideHumiGraphUnderInfo4),
                    rootView.findViewById(R.id.txtInsideHumiGraphUnderInfo5),
                    rootView.findViewById(R.id.txtInsideHumiGraphUnderInfo6),
                    rootView.findViewById(R.id.txtInsideHumiGraphUnderInfo7),
                    rootView.findViewById(R.id.txtInsideHumiGraphUnderInfo8),
            };

            mTxtOutsidePm25GraphUnderInfos = new TextView[]{
                    rootView.findViewById(R.id.txtOutsidePm25GraphUnderInfo0),
                    rootView.findViewById(R.id.txtOutsidePm25GraphUnderInfo1),
                    rootView.findViewById(R.id.txtOutsidePm25GraphUnderInfo2),
                    rootView.findViewById(R.id.txtOutsidePm25GraphUnderInfo3),
                    rootView.findViewById(R.id.txtOutsidePm25GraphUnderInfo4),
                    rootView.findViewById(R.id.txtOutsidePm25GraphUnderInfo5),
                    rootView.findViewById(R.id.txtOutsidePm25GraphUnderInfo6),
                    rootView.findViewById(R.id.txtOutsidePm25GraphUnderInfo7),
                    rootView.findViewById(R.id.txtOutsidePm25GraphUnderInfo8),
            };

            mTxtOutsidePm10GraphUnderInfos = new TextView[]{
                    rootView.findViewById(R.id.txtOutsidePm10GraphUnderInfo0),
                    rootView.findViewById(R.id.txtOutsidePm10GraphUnderInfo1),
                    rootView.findViewById(R.id.txtOutsidePm10GraphUnderInfo2),
                    rootView.findViewById(R.id.txtOutsidePm10GraphUnderInfo3),
                    rootView.findViewById(R.id.txtOutsidePm10GraphUnderInfo4),
                    rootView.findViewById(R.id.txtOutsidePm10GraphUnderInfo5),
                    rootView.findViewById(R.id.txtOutsidePm10GraphUnderInfo6),
                    rootView.findViewById(R.id.txtOutsidePm10GraphUnderInfo7),
                    rootView.findViewById(R.id.txtOutsidePm10GraphUnderInfo8),
            };

            mTxtOutsideTempGraphUnderInfos = new TextView[]{
                    rootView.findViewById(R.id.txtOutsideTempGraphUnderInfo0),
                    rootView.findViewById(R.id.txtOutsideTempGraphUnderInfo1),
                    rootView.findViewById(R.id.txtOutsideTempGraphUnderInfo2),
                    rootView.findViewById(R.id.txtOutsideTempGraphUnderInfo3),
                    rootView.findViewById(R.id.txtOutsideTempGraphUnderInfo4),
                    rootView.findViewById(R.id.txtOutsideTempGraphUnderInfo5),
                    rootView.findViewById(R.id.txtOutsideTempGraphUnderInfo6),
                    rootView.findViewById(R.id.txtOutsideTempGraphUnderInfo7),
                    rootView.findViewById(R.id.txtOutsideTempGraphUnderInfo8),
            };

            mTxtOutsideHumiGraphUnderInfos = new TextView[]{
                    rootView.findViewById(R.id.txtOutsideHumiGraphUnderInfo0),
                    rootView.findViewById(R.id.txtOutsideHumiGraphUnderInfo1),
                    rootView.findViewById(R.id.txtOutsideHumiGraphUnderInfo2),
                    rootView.findViewById(R.id.txtOutsideHumiGraphUnderInfo3),
                    rootView.findViewById(R.id.txtOutsideHumiGraphUnderInfo4),
                    rootView.findViewById(R.id.txtOutsideHumiGraphUnderInfo5),
                    rootView.findViewById(R.id.txtOutsideHumiGraphUnderInfo6),
                    rootView.findViewById(R.id.txtOutsideHumiGraphUnderInfo7),
                    rootView.findViewById(R.id.txtOutsideHumiGraphUnderInfo8),
            };

            if(mSelectedType == CHART_SELECTED_DAYS){
                SimpleDateFormat sdf = new SimpleDateFormat(getString(R.string.LifeReportActivity_str_1), locale);
                String currentTime = sdf.format(daysDate);
                mTxtCurrentTime.setText(currentTime);

                Calendar cal = Calendar.getInstance();
                cal.setTime(daysDate);
                cal.add(Calendar.DATE, 1);
                btnNextTime.setEnabled(cal.getTimeInMillis() < new Date().getTime());

                float px_left = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 73, getResources().getDisplayMetrics());
                float px_right = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 19, getResources().getDisplayMetrics());
                ViewGroup.MarginLayoutParams l1 = (ViewGroup.MarginLayoutParams) mLayoutInsidePm25GraphUnderInfos.getLayoutParams();
                l1.setMarginStart((int) px_left);
                l1.setMarginEnd((int) px_right);

                // ikHwang 2019-06-11 오전 11:20 프리즘 모델 추가
                mLayoutInsideTotalGraphUnderInfos.setLayoutParams(l1);
                mLayoutInsidePm1GraphUnderInfos.setLayoutParams(l1);

                mLayoutInsidePm25GraphUnderInfos.setLayoutParams(l1);
                mLayoutInsideCo2GraphUnderInfos.setLayoutParams(l1);
                mLayoutInsideTVOCGraphUnderInfos.setLayoutParams(l1);
                mLayoutInsideTempGraphUnderInfos.setLayoutParams(l1);
                mLayoutInsideHumiGraphUnderInfos.setLayoutParams(l1);
                mLayoutOutsidePm25GraphUnderInfos.setLayoutParams(l1);
                mLayoutOutsidePm10GraphUnderInfos.setLayoutParams(l1);
                mLayoutOutsideTempGraphUnderInfos.setLayoutParams(l1);
                mLayoutOutsideHumiGraphUnderInfos.setLayoutParams(l1);

                // ikHwang 2019-06-11 오전 11:20 프리즘 모델 추가
                mTxtInsideTotalGraphUnderInfos[0].setPadding(0, 0, 0, 0);
                mTxtInsidePm1GraphUnderInfos[0].setPadding(0, 0, 0, 0);

                mTxtInsidePm25GraphUnderInfos[0].setPadding(0, 0, 0, 0);
                mTxtInsideCo2GraphUnderInfos[0].setPadding(0, 0, 0, 0);
                mTxtInsideTVOCGraphUnderInfos[0].setPadding(0, 0, 0, 0);
                mTxtInsideTempGraphUnderInfos[0].setPadding(0, 0, 0, 0);
                mTxtInsideHumiGraphUnderInfos[0].setPadding(0, 0, 0, 0);
                mTxtOutsidePm25GraphUnderInfos[0].setPadding(0, 0, 0, 0);
                mTxtOutsidePm10GraphUnderInfos[0].setPadding(0, 0, 0, 0);
                mTxtOutsideTempGraphUnderInfos[0].setPadding(0, 0, 0, 0);
                mTxtOutsideHumiGraphUnderInfos[0].setPadding(0, 0, 0, 0);

                ArrayList<LifeReportInfo> reportList = getArguments().getInt(ARG_SECTION_NUMBER) == CHART_TYPE_INSIDE ? mLifeReportInfoInsideDay : mLifeReportInfoOutsideDay;
                //dot vertical line
                RelativeLayout.LayoutParams currentParams = (RelativeLayout.LayoutParams) mLayoutInsidePm25Current.getLayoutParams();
                if(dm.density == 4){
                    currentParams.setMarginEnd((int) dpToPx(11+(24-reportList.size())*9.83f));
                } else if (dm.density == 3.5) {
                    currentParams.setMarginEnd((int) dpToPx(15+(24-reportList.size())*11.72f));
                } else if (dm.density == 3.0) {
                    currentParams.setMarginEnd((int) dpToPx(11+(24-reportList.size())*9.83f));
                } else if (dm.density == 2.0) {
                    currentParams.setMarginEnd((int) dpToPx(11+(24-reportList.size())*9.83f));
                } else if (dm.density == 2.625) {
                    currentParams.setMarginEnd((int) dpToPx(15+(24-reportList.size())*11.71f));
                } else if (dm.density == 2.25) { // 갤럭시 폴드
                    if(dm.widthPixels < 900){
                        currentParams.setMarginEnd((int) dpToPx(9+(24-reportList.size())*10.42f));
                    }else{
                        currentParams.setMarginEnd((int) dpToPx(34+(24-reportList.size())*22.12f));
                    }
                } else if (dm.density == 2.75) {
                    currentParams.setMarginEnd((int) dpToPx(11+(24-reportList.size())*11.20f));
                }

                // ikHwang 2019-06-11 오전 11:21 프리즘 모델 추가
                mLayoutInsideTotalCurrent.setLayoutParams(currentParams);
                mLayoutInsidePm1Current.setLayoutParams(currentParams);

                mLayoutInsidePm25Current.setLayoutParams(currentParams);
                mLayoutInsideCo2Current.setLayoutParams(currentParams);
                mLayoutInsideTVOCCurrent.setLayoutParams(currentParams);
                mLayoutInsideTempCurrent.setLayoutParams(currentParams);
                mLayoutInsideHumiCurrent.setLayoutParams(currentParams);
                mLayoutOutsidePm25Current.setLayoutParams(currentParams);
                mLayoutOutsidePm10Current.setLayoutParams(currentParams);
                mLayoutOutsideTempCurrent.setLayoutParams(currentParams);
                mLayoutOutsideHumiCurrent.setLayoutParams(currentParams);
            } else if(mSelectedType == CHART_SELECTED_WEEKS){
                SimpleDateFormat sdf1 = new SimpleDateFormat(getString(R.string.LifeReportActivity_str_2), locale);
                SimpleDateFormat sdf2 = new SimpleDateFormat(getString(R.string.LifeReportActivity_str_3), locale);
                Calendar cal = Calendar.getInstance();
                cal.setTime(weeksDate);
                cal.add(Calendar.DATE, 6);
                String currentTime = sdf1.format(weeksDate) + "~" + sdf2.format(cal.getTime());
                mTxtCurrentTime.setText(currentTime);

                cal.add(Calendar.DATE, 1);
                btnNextTime.setEnabled(cal.getTimeInMillis() < new Date().getTime());

                float px_left = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 73, getResources().getDisplayMetrics());
                float px_right = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 19, getResources().getDisplayMetrics());
                ViewGroup.MarginLayoutParams l1 = (ViewGroup.MarginLayoutParams) mLayoutInsidePm25GraphUnderInfos.getLayoutParams();
                l1.setMarginStart((int) px_left);
                l1.setMarginEnd((int) px_right);

                // ikHwang 2019-06-11 오전 11:21 프리즘 모델 추가
                mLayoutInsideTotalGraphUnderInfos.setLayoutParams(l1);
                mLayoutInsidePm1GraphUnderInfos.setLayoutParams(l1);

                mLayoutInsidePm25GraphUnderInfos.setLayoutParams(l1);
                mLayoutInsideCo2GraphUnderInfos.setLayoutParams(l1);
                mLayoutInsideTVOCGraphUnderInfos.setLayoutParams(l1);
                mLayoutInsideTempGraphUnderInfos.setLayoutParams(l1);
                mLayoutInsideHumiGraphUnderInfos.setLayoutParams(l1);
                mLayoutOutsidePm25GraphUnderInfos.setLayoutParams(l1);
                mLayoutOutsidePm10GraphUnderInfos.setLayoutParams(l1);
                mLayoutOutsideTempGraphUnderInfos.setLayoutParams(l1);
                mLayoutOutsideHumiGraphUnderInfos.setLayoutParams(l1);

                // ikHwang 2019-06-11 오전 11:22 프리즘 모델 추가
                mTxtInsideTotalGraphUnderInfos[0].setPadding(0, 0, 0, 0);
                mTxtInsidePm1GraphUnderInfos[0].setPadding(0, 0, 0, 0);

                mTxtInsidePm25GraphUnderInfos[0].setPadding(0, 0, 0, 0);
                mTxtInsideCo2GraphUnderInfos[0].setPadding(0, 0, 0, 0);
                mTxtInsideTVOCGraphUnderInfos[0].setPadding(0, 0, 0, 0);
                mTxtInsideTempGraphUnderInfos[0].setPadding(0, 0, 0, 0);
                mTxtInsideHumiGraphUnderInfos[0].setPadding(0, 0, 0, 0);
                mTxtOutsidePm25GraphUnderInfos[0].setPadding(0, 0, 0, 0);
                mTxtOutsidePm10GraphUnderInfos[0].setPadding(0, 0, 0, 0);
                mTxtOutsideTempGraphUnderInfos[0].setPadding(0, 0, 0, 0);
                mTxtOutsideHumiGraphUnderInfos[0].setPadding(0, 0, 0, 0);

                ArrayList<LifeReportInfo> reportList = getArguments().getInt(ARG_SECTION_NUMBER) == CHART_TYPE_INSIDE ? mLifeReportInfoInsideWeek : mLifeReportInfoOutsideWeek;
                //dot vertical line
                RelativeLayout.LayoutParams currentParams = (RelativeLayout.LayoutParams) mLayoutInsidePm25Current.getLayoutParams();
                if(dm.density == 4){
                    currentParams.setMarginEnd((int) dpToPx(5+(7-reportList.size())*38.1f));
                } else if (dm.density == 3.5) {
                    currentParams.setMarginEnd((int) dpToPx(7+(7-reportList.size())*45.95f));
                } else if (dm.density == 3.0) {
                    currentParams.setMarginEnd((int) dpToPx(5+(7-reportList.size())*38.1f));
                } else if (dm.density == 2.0) {
                    currentParams.setMarginEnd((int) dpToPx(5+(7-reportList.size())*38.16f));
                } else if (dm.density == 2.625) {
                    currentParams.setMarginEnd((int) dpToPx(7+(7-reportList.size())*45.95f));
                } else if (dm.density == 2.25) { // 갤럭시 폴드
                    if(dm.widthPixels < 900){
                        currentParams.setMarginEnd((int) dpToPx(1+(7-reportList.size())*41.42f));
                    }else{
                        currentParams.setMarginEnd((int) dpToPx(25+(7-reportList.size())*85.12f));
                    }
                } else if (dm.density == 2.75) {
                    currentParams.setMarginEnd((int) dpToPx(5+(7-reportList.size())*43.37f));
                }

                // ikHwang 2019-06-11 오전 11:22 프리즘 모델 추가
                mLayoutInsideTotalCurrent.setLayoutParams(currentParams);
                mLayoutInsidePm1Current.setLayoutParams(currentParams);

                mLayoutInsidePm25Current.setLayoutParams(currentParams);
                mLayoutInsideCo2Current.setLayoutParams(currentParams);
                mLayoutInsideTVOCCurrent.setLayoutParams(currentParams);
                mLayoutInsideTempCurrent.setLayoutParams(currentParams);
                mLayoutInsideHumiCurrent.setLayoutParams(currentParams);
                mLayoutOutsidePm25Current.setLayoutParams(currentParams);
                mLayoutOutsidePm10Current.setLayoutParams(currentParams);
                mLayoutOutsideTempCurrent.setLayoutParams(currentParams);
                mLayoutOutsideHumiCurrent.setLayoutParams(currentParams);
            } else if(mSelectedType == CHART_SELECTED_MONTHS) {
                SimpleDateFormat sdf = new SimpleDateFormat(getString(R.string.LifeReportActivity_str_4), locale);
                String currentTime = sdf.format(monthsDate);
                mTxtCurrentTime.setText(currentTime);

                Calendar cal = Calendar.getInstance();
                cal.setTime(monthsDate);
                cal.add(Calendar.MONTH, 1);
                btnNextTime.setEnabled(cal.getTimeInMillis() < new Date().getTime());

                float px_left = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 60, getResources().getDisplayMetrics());
                float px_right = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 21, getResources().getDisplayMetrics());
                ViewGroup.MarginLayoutParams l1 = (ViewGroup.MarginLayoutParams) mLayoutInsidePm25GraphUnderInfos.getLayoutParams();
                l1.setMarginStart((int) px_left);
                l1.setMarginEnd((int) px_right);

                // ikHwang 2019-06-11 오전 11:22 프리즘 모델 추가
                mLayoutInsideTotalGraphUnderInfos.setLayoutParams(l1);
                mLayoutInsidePm1GraphUnderInfos.setLayoutParams(l1);

                mLayoutInsidePm25GraphUnderInfos.setLayoutParams(l1);
                mLayoutInsideCo2GraphUnderInfos.setLayoutParams(l1);
                mLayoutInsideTVOCGraphUnderInfos.setLayoutParams(l1);
                mLayoutInsideTempGraphUnderInfos.setLayoutParams(l1);
                mLayoutInsideHumiGraphUnderInfos.setLayoutParams(l1);
                mLayoutOutsidePm25GraphUnderInfos.setLayoutParams(l1);
                mLayoutOutsidePm10GraphUnderInfos.setLayoutParams(l1);
                mLayoutOutsideTempGraphUnderInfos.setLayoutParams(l1);
                mLayoutOutsideHumiGraphUnderInfos.setLayoutParams(l1);

                // ikHwang 2019-06-11 오전 11:23 프리즘 모델 추가
                mTxtInsideTotalGraphUnderInfos[0].setPadding((int) dpToPx(16), 0, 0, 0);
                mTxtInsidePm1GraphUnderInfos[0].setPadding((int) dpToPx(16), 0, 0, 0);

                mTxtInsidePm25GraphUnderInfos[0].setPadding((int) dpToPx(16), 0, 0, 0);
                mTxtInsideCo2GraphUnderInfos[0].setPadding((int) dpToPx(16), 0, 0, 0);
                mTxtInsideTVOCGraphUnderInfos[0].setPadding((int) dpToPx(16), 0, 0, 0);
                mTxtInsideTempGraphUnderInfos[0].setPadding((int) dpToPx(16), 0, 0, 0);
                mTxtInsideHumiGraphUnderInfos[0].setPadding((int) dpToPx(16), 0, 0, 0);
                mTxtOutsidePm25GraphUnderInfos[0].setPadding((int) dpToPx(16), 0, 0, 0);
                mTxtOutsidePm10GraphUnderInfos[0].setPadding((int) dpToPx(16), 0, 0, 0);
                mTxtOutsideTempGraphUnderInfos[0].setPadding((int) dpToPx(16), 0, 0, 0);
                mTxtOutsideHumiGraphUnderInfos[0].setPadding((int) dpToPx(16), 0, 0, 0);

                ArrayList<LifeReportInfo> reportList = getArguments().getInt(ARG_SECTION_NUMBER) == CHART_TYPE_INSIDE ? mLifeReportInfoInsideMonth : mLifeReportInfoOutsideMonth;
                //dot vertical line
                RelativeLayout.LayoutParams currentParams = (RelativeLayout.LayoutParams) mLayoutInsidePm25Current.getLayoutParams();
                if(dm.density == 4){
                    currentParams.setMarginEnd((int) dpToPx((31-reportList.size())*7.95f));
                } else if (dm.density == 3.5) {
                    currentParams.setMarginEnd((int) dpToPx(1+(31-reportList.size())*9.45f));
                } else if (dm.density == 3.0) {
                    currentParams.setMarginEnd((int) dpToPx((31-reportList.size())*7.95f));
                } else if (dm.density == 2.0) {
                    currentParams.setMarginEnd((int) dpToPx((31-reportList.size())*7.96f));
                } else if (dm.density == 2.625) {
                    currentParams.setMarginEnd((int) dpToPx(1+(31-reportList.size())*9.45f));
                } else if (dm.density == 2.25) { // 갤럭시 폴드
                    if(dm.widthPixels < 900){
                        currentParams.setMarginEnd((int) dpToPx(1+(31-reportList.size())*8.35f));
                    }else{
                        currentParams.setMarginEnd((int) dpToPx(10+(31-reportList.size())*17.56f));
                    }
                } else if (dm.density == 2.75) {
                    currentParams.setMarginEnd((int) dpToPx(1+(31-reportList.size())*8.90f));
                }

                // ikHwang 2019-06-11 오전 11:23 프리즘 모델 추가
                mLayoutInsideTotalCurrent.setLayoutParams(currentParams);
                mLayoutInsidePm1Current.setLayoutParams(currentParams);

                mLayoutInsidePm25Current.setLayoutParams(currentParams);
                mLayoutInsideCo2Current.setLayoutParams(currentParams);
                mLayoutInsideTVOCCurrent.setLayoutParams(currentParams);
                mLayoutInsideTempCurrent.setLayoutParams(currentParams);
                mLayoutInsideHumiCurrent.setLayoutParams(currentParams);
                mLayoutOutsidePm25Current.setLayoutParams(currentParams);
                mLayoutOutsidePm10Current.setLayoutParams(currentParams);
                mLayoutOutsideTempCurrent.setLayoutParams(currentParams);
                mLayoutOutsideHumiCurrent.setLayoutParams(currentParams);
            }

            mTxtDays.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mListener != null) {
                        String date = sdf.format(daysDate);
                        mListener.setChartData(getArguments().getInt(ARG_SECTION_NUMBER), CHART_SELECTED_DAYS, date);
                    }
                }
            });

            mTxtWeeks.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mListener != null) {
                        String date = sdf.format(weeksDate);
                        mListener.setChartData(getArguments().getInt(ARG_SECTION_NUMBER), CHART_SELECTED_WEEKS, date);
                    }
                }
            });

            mTxtMonths.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mListener != null) {
                        String date = sdf.format(monthsDate);
                        mListener.setChartData(getArguments().getInt(ARG_SECTION_NUMBER), CHART_SELECTED_MONTHS, date);
                    }
                }
            });

            btnPrevTime.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(mListener != null) {
                        Calendar cal = Calendar.getInstance();
                        Date newDate = null;
                        if (mSelectedType == CHART_SELECTED_DAYS) {
                            cal.setTime(daysDate);
                            cal.add(Calendar.DATE, -1);
                            daysDate = cal.getTime();
                            newDate = daysDate;
                        } else if (mSelectedType == CHART_SELECTED_WEEKS) {
                            cal.setTime(weeksDate);
                            cal.add(Calendar.DATE, -7);
                            weeksDate = cal.getTime();
                            newDate = weeksDate;
                        } else if (mSelectedType == CHART_SELECTED_MONTHS) {
                            cal.setTime(monthsDate);
                            cal.add(Calendar.MONTH, -1);
                            monthsDate = cal.getTime();
                            newDate = monthsDate;
                        }

                        if (newDate != null) {
                            mListener.setChartData(getArguments().getInt(ARG_SECTION_NUMBER), mSelectedType, sdf.format(newDate));
                        }
                    }
                }
            });

            btnNextTime.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(mListener != null) {
                        Calendar cal = Calendar.getInstance();
                        Date newDate = null;
                        if (mSelectedType == CHART_SELECTED_DAYS) {
                            cal.setTime(daysDate);
                            cal.add(Calendar.DATE, 1);
                            daysDate = cal.getTime();
                            newDate = daysDate;
                        } else if (mSelectedType == CHART_SELECTED_WEEKS) {
                            cal.setTime(weeksDate);
                            cal.add(Calendar.DATE, 7);
                            weeksDate = cal.getTime();
                            newDate = weeksDate;
                        } else if (mSelectedType == CHART_SELECTED_MONTHS) {
                            cal.setTime(monthsDate);
                            cal.add(Calendar.MONTH, 1);
                            monthsDate = cal.getTime();
                            newDate = monthsDate;
                        }

                        if (newDate != null) {
                            mListener.setChartData(getArguments().getInt(ARG_SECTION_NUMBER), mSelectedType, sdf.format(newDate));
                        }
                    }
                }
            });
        }
    }

    /**
     * 그래프 실내 공기질 VALUE 값 획득 함수
     * @param type : 일, 주, 월 단위
     * @param chartId : 구성할 차트 아이디
     * @param position : 표시되는 위치
     * @return val : 출력해야하는 그래프값
     */
    private static double getChartInsideValue(int type, int chartId, int position){
        double val = 0;
        ArrayList<LifeReportInfo> list = null;
        switch (type){
            case CHART_SELECTED_DAYS:
                list = mLifeReportInfoInsideDay;
                break;

            case CHART_SELECTED_WEEKS:
                list = mLifeReportInfoInsideWeek;
                break;

            case CHART_SELECTED_MONTHS:
                list = mLifeReportInfoInsideMonth;
                break;
        }

        if(list != null) {
            LifeReportInfo reportInfo = list.get(position);
            switch (chartId){
                case R.id.layoutInsideTotalChart:    // 실내 통합 공기질
                    val = getChartValue(reportInfo.totalValue,chartId);
                    break;

                case R.id.layoutInsidePm1Chart:    // 실내 PM1.0
                    val = getChartValue(reportInfo.pm1Value,chartId);
                    break;

                case R.id.layoutInsidePm25Chart:    // 실내 PM2.5
                    val = getChartValue(reportInfo.DustValue,chartId);
                    break;

                case R.id.layoutInsideCo2Chart:     // 실내 Co2
                    val = getChartValue(reportInfo.CO2Value,chartId);
                    break;

                case R.id.layoutInsideTVOCChart:    // 실내 TVOC
                    val = getChartValue(reportInfo.GASValue,chartId);
                    break;

                case R.id.layoutInsideTempChart:    // 실내 온도
                    // ikHwang 2019-06-07 오전 11:38 실내온도 그래프 최대 수치인 40 ~ -20 까지 표시하도록 처리
                    if(reportInfo.SensorTemp > 40){
                        val = 40;
                    }else if(reportInfo.SensorTemp < -20){
                        val = -20;
                    }else{
                        val = reportInfo.SensorTemp;
                    }
                    break;

                case R.id.layoutInsideHumiChart:    // 실내 습도
                    val = reportInfo.SensorHum;
                    break;
            }
        }
        return val;
    }

    private static double getChartOutsideValue(int type, int chartId, int position){
        double val = 0;
        ArrayList<LifeReportInfo> list = null;
        switch (type){
            case CHART_SELECTED_DAYS:
                list = mLifeReportInfoOutsideDay;
                break;

            case CHART_SELECTED_WEEKS:
                list = mLifeReportInfoOutsideWeek;
                break;

            case CHART_SELECTED_MONTHS:
                list = mLifeReportInfoOutsideMonth;
                break;
        }

        if(list != null) {
            LifeReportInfo reportInfo = list.get(position);
            switch (chartId){
                case R.id.layoutOutsidePm25Chart:   // 실외 PM2.5
                    val = getChartValue(reportInfo.pm25Value,chartId);
                    break;

                case R.id.layoutOutsidePm10Chart:   // 실외 PM10
                    val = getChartValue(reportInfo.pm10Value,chartId);
                    break;

                case R.id.layoutOutsideTempChart:   // 실외 온도
                    val = reportInfo.outsideTemp;
                    break;

                case R.id.layoutOutsideHumiChart:   // 실외 습도
                    val = reportInfo.outsideHum;
                    break;
            }
        }
        return val;
    }

    public static int geValueLevel(int chartID, double vlaue){
        if(vlaue == 0) return 0;

        double[] chartValue = getMaxChartValue(chartID);

        int cnt = 0;
        for(int i= 0; i<chartValue.length; i++){
            if(vlaue <= (chartValue[i] - 1)){
                if(R.id.layoutInsideTotalChart == chartID){
                    cnt = chartValue.length - i;
                }else{
                    cnt = i+1;
                }
                break;
            }
        }

        return cnt;
    }


    //차트 각 구간별 max 배열
    private static double[] getMaxChartValue(int chartId){
        switch (chartId){
            case R.id.layoutInsideTotalChart:    // 실내 통합 공기질
                return new double[]{40.0, 60.0, 80.0, 100.0};

            case R.id.layoutInsidePm1Chart:     // 실내 PM1.0
                return new double[]{15.0, 35.0, 75.0, 150.0};

            case R.id.layoutInsidePm25Chart:    // 실내 PM2.5
                return new double[]{15.0, 35.0, 75.0, 150.0};

            case R.id.layoutInsideCo2Chart:     // 실내 Co2
                return new double[]{700.0, 1000.0, 1500.0, 3000.0};

            case R.id.layoutInsideTVOCChart:    // 실내 TVOC
                return new double[]{500.0, 3000.0, 8000.0, 25000.0};

            case R.id.layoutOutsidePm25Chart:   // 실외 PM2.5
                return new double[]{15.0, 35.0, 75.0, 150.0};

            case R.id.layoutOutsidePm10Chart:   // 실외 PM10
                return new double[]{30.0, 80.0, 150.0, 300.0};
        }
        return null;
    }

    private static double[] getRatioValue(int chartId) {
        switch (chartId){
        }
        return new double[]{1.42, 2.27, 3.13, 4};
    }

    //차트에 맞는 return value
    private static double getChartValue(double val, int charId){
        final double defaultValue = 0.55;
        double returnVal = 0;
        double[] maxValueArr = getMaxChartValue(charId);
        double[] ratioArr = getRatioValue(charId);

        if(val != 0){
            for(int i=0; i<maxValueArr.length; i++){
                double value = maxValueArr[i];

                if(val == value){
                    returnVal = ratioArr[i];
                    break;
                }else if(val < value){
                    if(i == 0){
                        returnVal = defaultValue + ((ratioArr[i] - defaultValue) * val) / maxValueArr[i];
                        break;
                    }else{
//                        returnVal = ratioArr[i-1] + (ratioArr[i-1] - (ratioArr[i]) * (val - maxValueArr[i-1])) / maxValueArr[i];
                        double ratioRange =  Math.abs(ratioArr[i] - ratioArr[i-1]);
                        double valueRange = Math.abs(maxValueArr[i] - maxValueArr[i-1]);
                        double increase = ratioRange / valueRange;

                        returnVal = ratioArr[i-1] + (increase * (val - maxValueArr[i-1]));
                        break;
                    }
                }else if((i == maxValueArr.length-1) && val > value){
                    returnVal = ratioArr[i];
                    break;
                }
            }
        } else {
            returnVal = defaultValue;
        }

        if (returnVal <= 4) {
            return returnVal;
        }

        return 4;
    }

    /**
     * 그래프 공기질 HIGHLIGHT 값 획득 함수
     * @param type : 일, 주, 월 단위
     * @param section : 실내, 실외
     * @param chartId : 구성할 차트 아이디
     * @param position : 표시되는 위치
     * @return val : 출력해야하는 그래프값
     */
    private static float getChartHighlight(int type, int section, int chartId, int position){
        double val = 0;
        ArrayList<LifeReportInfo> list = null;
        switch (type){
            case CHART_SELECTED_DAYS:
                if (section == 0) {
                    list = mLifeReportInfoInsideDay;
                } else if (section == 1) {
                    list = mLifeReportInfoOutsideDay;
                }
                break;

            case CHART_SELECTED_WEEKS:
                if (section == 0) {
                    list = mLifeReportInfoInsideWeek;
                } else if (section == 1) {
                    list = mLifeReportInfoOutsideWeek;
                }
                break;

            case CHART_SELECTED_MONTHS:
                if (section == 0) {
                    list = mLifeReportInfoInsideMonth;
                } else if (section == 1) {
                    list = mLifeReportInfoOutsideMonth;
                }
                break;
        }

        if(list != null) {
            LifeReportInfo reportInfo = list.get(position);
            switch (chartId){
                case R.id.layoutInsideTotalChart:    // 실내 통합 공기질
                    val = reportInfo.totalValue;
//                    val = reportInfo.DustValue;
                    break;

                case R.id.layoutInsidePm1Chart:    // 실내 PM1.0
                    val = reportInfo.pm1Value;
//                    val = reportInfo.DustValue;
                    break;

                case R.id.layoutInsidePm25Chart:    // 실내 PM2.5
                    val = reportInfo.DustValue;
                    break;

                case R.id.layoutInsideCo2Chart:     // 실내 Co2
                    val = reportInfo.CO2Value;
                    break;

                case R.id.layoutInsideTVOCChart:    // 실내 TVOC
                    val = reportInfo.GASValue;
                    break;

                case R.id.layoutInsideTempChart:    // 실내 온도
                    val = reportInfo.SensorTemp;
                    break;

                case R.id.layoutInsideHumiChart:    // 실내 습도
                    val = reportInfo.SensorHum;
                    break;

                case R.id.layoutOutsidePm25Chart:   // 실외 PM2.5
                    val = reportInfo.pm25Value;
                    break;

                case R.id.layoutOutsidePm10Chart:   // 실외 PM10
                    val = reportInfo.pm10Value;
                    break;

                case R.id.layoutOutsideTempChart:   // 실외 온도
                    val = reportInfo.outsideTemp;
                    break;

                case R.id.layoutOutsideHumiChart:   // 실외 습도
                    val = reportInfo.outsideHum;
                    break;
            }
        }
        return (float) val;
    }

    /**
     * 그래프 공기질 HIGHLIGHT 값 획득 함수(센서값 표기)
     * @param type : 일, 주, 월 단위
     * @param section : 실내, 실외
     * @param chartId : 구성할 차트 아이디
     * @param position : 표시되는 위치
     * @return val : 출력해야하는 그래프값
     */
    private static double getChartSensorHighlight(int type, int section, int chartId, int position){
        double val = 0;
        ArrayList<LifeReportInfo> list = null;
        switch (type){
            case CHART_SELECTED_DAYS:
                if (section == 0) {
                    list = mLifeReportInfoInsideDay;
                } else if (section == 1) {
                    list = mLifeReportInfoOutsideDay;
                }
                break;

            case CHART_SELECTED_WEEKS:
                if (section == 0) {
                    list = mLifeReportInfoInsideWeek;
                } else if (section == 1) {
                    list = mLifeReportInfoOutsideWeek;
                }
                break;

            case CHART_SELECTED_MONTHS:
                if (section == 0) {
                    list = mLifeReportInfoInsideMonth;
                } else if (section == 1) {
                    list = mLifeReportInfoOutsideMonth;
                }
                break;
        }

        if(list != null) {
            LifeReportInfo reportInfo = list.get(position);
            switch (chartId){
                case R.id.layoutInsideTotalChart:    // 실내 통합 공기질
//                    val = reportInfo.totalSensor;
                    val = reportInfo.DustSensor;
                    break;

                case R.id.layoutInsidePm1Chart:    // 실내 PM1.0
//                    val = reportInfo.pm1Sensor;
                    val = reportInfo.DustSensor;
                    break;

                case R.id.layoutInsidePm25Chart:    // 실내 PM2.5
                    val = reportInfo.DustSensor;
                    break;

                case R.id.layoutInsideCo2Chart:     // 실내 Co2
                    val = reportInfo.CO2Sensor;
                    break;

                case R.id.layoutInsideTVOCChart:    // 실내 TVOC
                    val = reportInfo.GASSensor;
                    break;

                case R.id.layoutOutsidePm25Chart:   // 실외 PM2.5
                    val = reportInfo.pm25Sensor;
                    break;

                case R.id.layoutOutsidePm10Chart:   // 실외 PM10
                    val = reportInfo.pm10Sensor;
                    break;
            }
        }
        return val;
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            return PlaceholderFragment.newInstance(CHART_TYPE_INSIDE+position);
        }

        @Override
        public int getItemPosition(@NonNull Object object) {
            return POSITION_NONE;
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case CHART_TYPE_INSIDE:
                    return getString(R.string.inside);
                case CHART_TYPE_OUTSIDE:
                    return getString(R.string.outside);
            }
            return null;
        }
    }
}