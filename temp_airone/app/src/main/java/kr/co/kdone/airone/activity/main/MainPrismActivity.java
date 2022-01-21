package kr.co.kdone.airone.activity.main;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;
import kr.co.kdone.airone.CleanVentilationApplication;
import kr.co.kdone.airone.R;
import kr.co.kdone.airone.activity.info.RoomControlerDetailActivity;
import kr.co.kdone.airone.activity.more.MoreActivity;
import kr.co.kdone.airone.activity.more.MoreHelpActivity;
import kr.co.kdone.airone.adapter.MainPagerAdapter;
import kr.co.kdone.airone.fragments.HomePrismFragment;
import kr.co.kdone.airone.utils.CommonUtils;
import kr.co.kdone.airone.utils.HomeInfoDataParser;
import kr.co.kdone.airone.utils.HttpApi;
import kr.co.kdone.airone.utils.RoomControllerDialog;
import kr.co.kdone.airone.utils.SharedPrefUtil;
import kr.co.kdone.airone.widget.WidgetUtils;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

import static kr.co.kdone.airone.utils.CommonUtils.DismissConnectDialog;
import static kr.co.kdone.airone.utils.CommonUtils.IsRunningProgress;
import static kr.co.kdone.airone.utils.CommonUtils.MODE_DEVICE_ROOM_CON;
import static kr.co.kdone.airone.utils.CommonUtils.RESULT_DETAIL_CHECK;
import static kr.co.kdone.airone.utils.CommonUtils.RESULT_ERROR_STATE_DEVICE_NO_CONNECTED;
import static kr.co.kdone.airone.utils.CommonUtils.RESULT_ERROR_STATE_DEVICE_NO_DB;
import static kr.co.kdone.airone.utils.CommonUtils.initiateTwoButtonAlert;
import static kr.co.kdone.airone.utils.CommonUtils.isUserInfoSection;
import static kr.co.kdone.airone.utils.CommonUtils.setDisableViewFew;
import static kr.co.kdone.airone.utils.CommonUtils.startActivityAni;
import static kr.co.kdone.airone.utils.SharedPrefUtil.CURRENT_CONTROL_DEVICE_ID;

/**
 * ikHwang 2019-06-04 오전 9:48 홈 화면
 */
public class MainPrismActivity extends AppCompatActivity implements View.OnClickListener {
    private final String TAG = getClass().getSimpleName();

    private Context mContext;

    private ViewPager mPager;                   // 메인화면 룸콘 구성 페이저
    private MainPagerAdapter mainPagerAdapter;  // 메인화면 구성 룸콘 페이저 어댑터

    private Timer mLoadingTimer;    // 메인화면 리프레시 타이머
    private long LOADING_TIME = 10; // 메인화면 기본 조회 시간 10초

    // ikHwang 2019-05-22 오후 3:18 메인화면 데이터 주기적으로 호출하기 위한 타이머
    private Timer timer;
    private TimerTask timerTask;
    private int TIMER_DELAY = 10; // 업데이트 딜레이 시간 30초

    // ikHwang 2019-05-24 오후 1:58 룸콘 미연결 다이얼로그 중복 표시 방지하기 위해 추가
    private RoomControllerDialog dialog;

    boolean aniType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_prism);

        mContext = this;

        initLayout();

        // ikHwang 2019-05-23 오후 6:00 타이머 시간 설정
        TIMER_DELAY = (SharedPrefUtil.getInt(SharedPrefUtil.SAVE_MAIN_INTERVAL, TIMER_DELAY) * 1000);

        isUserInfoSection(this);

        if (getIntent().getExtras() != null) {
            onNewIntent(getIntent());
        }

        if (getIntent().hasExtra("MsgCode") && getIntent().getStringExtra("MsgCode").equals("15")) {
            findViewById(R.id.layoutMenu03).performClick();
        }

        // ikHwang 2019-05-22 오전 10:02 로그인시 조회된 데이터를 통해 메인화면 구성하기위해 추가
        try {
            if(CleanVentilationApplication.getInstance().isConnectedRoomController()){
                // 룸콘이 정상적으로 연결되어 있음
                mPager.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        startFragmentAnimation();
                    }
                }, 500);
            }else{
                showRoomControllerConnectPopup(); // 룸콘 정보가 없거나, 룸콘이 연결되어 있지 않아 룸콘 등록 팝업 표시
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        CommonUtils.DismissConnectDialog();
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(CleanVentilationApplication.isAnyType()) overridePendingTransition(R.anim.slide_none, 0);
        CleanVentilationApplication.setAnyType(false);

        // ikHwang 2019.04.18 메인화면 주기적 업데이트 처리위한 타이머
        timer = new Timer();
        timerTask = new TimerTask(){

            @Override
            public void run() {
                getDeviceInfoList(false);
                CommonUtils.customLog(TAG, "Call Timer", Log.ERROR);
            }
        };

        timer.schedule(timerTask, 0, TIMER_DELAY);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (IsRunningProgress()) {
            CommonUtils.DismissConnectDialog();
        }

        // ikHwang 2019-05-22 오후 3:37 업데이트 타이머 제거
        try {
            if(null != timer) timer.cancel();
            if(null != timerTask) timerTask.cancel();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onClick(View v) {
        setDisableViewFew(v);
        switch (v.getId()) {
            case R.id.btnMore: // 더보기 (설정)
                Intent intentMore = new Intent(mContext, MoreActivity.class);
                intentMore.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivityAni(MainPrismActivity.this, intentMore, false, 1);
                break;

            case R.id.btnHelp: // 공기질 정보 화면
                Intent intentMoreHelp = new Intent(mContext, MoreHelpActivity.class);
                startActivityAni(MainPrismActivity.this, intentMoreHelp, false, 1);
                break;
        }
    }

    @Override
    public void onBackPressed() {
        initiateTwoButtonAlert(this, getString(R.string.exit_app), getString(R.string.confirm), getString(R.string.no));
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case RESULT_ERROR_STATE_DEVICE_NO_DB: {
                Log.d("tak", "1");
                Intent intent = new Intent(this, MoreActivity.class);
                startActivityAni(MainPrismActivity.this, intent, false, 1);

                new Timer().schedule(new TimerTask() {
                    @Override
                    public void run() {
                        finish();
                    }
                }, 500);
                break;
            }
            case RESULT_ERROR_STATE_DEVICE_NO_CONNECTED: {
                Log.d("tak", "2");
                if (resultCode == RESULT_OK) {
                }
                break;
            }
            case RESULT_DETAIL_CHECK: {
                Log.d("tak", "3");
                if (resultCode == RESULT_OK) {
                    if (SharedPrefUtil.getString(CURRENT_CONTROL_DEVICE_ID, "").equals("")) {
                        getDeviceInfoList(true);
                    }
                }
                break;
            }
        }
    }
    
    // ikHwang 2019-06-10 오전 11:38 레이아웃 초기화
    private void initLayout(){
        mPager = findViewById(R.id.viewPager);

        mainPagerAdapter = new MainPagerAdapter(getSupportFragmentManager(), CleanVentilationApplication.getInstance().getHomeList());
        mPager.setAdapter(mainPagerAdapter);

        mPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i1) {
            }

            @Override
            public void onPageSelected(int i) {
                // 페이지 변경에 따라 현재 선택된 페이지 저장
            }

            @Override
            public void onPageScrollStateChanged(int i) {
            }
        });
    }

    /**
     * 로딩화면 타이머 설정.
     */
    private void setLoadingTimer() {
        if (mLoadingTimer != null) {
            mLoadingTimer.cancel();
        }

        mLoadingTimer = new Timer();
        mLoadingTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (IsRunningProgress())
                    DismissConnectDialog();
            }
        }, LOADING_TIME);
    }

    private void cancelLoadingTimer() {
        if (mLoadingTimer != null) {
            mLoadingTimer.cancel();
        }
    }

    public void dataRefresh() {
        findViewById(R.id.btnReplace).performClick();
    }

    /**
     * 제품 리스트 획득 함수.
     */
    private void getDeviceInfoList(boolean showProgress) {
        if(showProgress){
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        if (!IsRunningProgress()) {
                            CommonUtils.displayProgress(MainPrismActivity.this, "", "");
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        }

        try {
            HttpApi.PostV2GetMainInfo( //V2A 적용.
                    CleanVentilationApplication.getInstance().getUserInfo().getId(),
                    new Callback() {
                        @Override
                        public void onFailure(Call call, IOException e) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    cancelLoadingTimer();
                                    DismissConnectDialog();
                                }
                            });
                        }

                        @Override
                        public void onResponse(Call call, Response response) throws IOException {
                            try {
                                String strResponse =  response.body().string();

                                CommonUtils.customLog(TAG, "main/info : " + strResponse, Log.ERROR);

                                JSONObject json_data = new JSONObject(strResponse);
                                Log.d("tak", String.valueOf(json_data));

                                switch (json_data.optInt("code", 0)) {
                                    case HttpApi.RESPONSE_SUCCESS: // 메인 정보 조회 성공
                                        if(json_data.has("data")){
                                            // ikHwang 2019-05-21 오후 1:32 메인화면 이동시 화면을 구성하기 위해 데이터 파싱
                                            HomeInfoDataParser.paserHomeInfo(CleanVentilationApplication.getInstance(), json_data.getJSONObject("data"), true);

                                            Intent sendIntent = new Intent(WidgetUtils.WIDGET_UPDATE_ACTION);
                                            sendBroadcast(sendIntent, getString(R.string.br_permission));
                                        }

                                        mainPagerAdapter.notifyDataSetChanged();

                                        // ikHwang 2019-05-27 오전 10:28 룸콘이 연결된 경우 팝업 제거 하기위해 사용
                                        switch (CleanVentilationApplication.getInstance().getHomeState()){
                                            case CleanVentilationApplication.STATE_AIR_MONITOR_NO_REGISTER: // 1차 사용자 에어 모니터 미등록
                                            case CleanVentilationApplication.STATE_AIR_MONITOR_NOT_CONNECTED: // 1차 사용자 에어 모니터 미연결
                                            case CleanVentilationApplication.STATE_AIR_MONITOR_UNKNOWN_INDISEINFO: // 1,2차 에어 모니터 센싱 정보 없음
                                            case CleanVentilationApplication.STATE_AIR_MONITOR_HAS_INDISEINFO: // 1,2차 에어 모니터 센싱 정보 있음 (정상처리 UI 갱신)
                                                if(null != dialog && dialog.isShowing()){
                                                    dialog.dismiss();
                                                }
                                                break;

                                            default: // 룸콘 미등록 및 미연결 처리
                                                break;
                                        }

                                        Intent sendIntent = new Intent(WidgetUtils.WIDGET_UPDATE_ACTION);
                                        sendBroadcast(sendIntent, getString(R.string.br_permission));

                                        if(CleanVentilationApplication.getInstance().isConnectedRoomController()){
                                            runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    startFragmentAnimation(); // 룸콘이 정상적으로 연결되어 있음
                                                }
                                            });
                                        }else{
                                            showRoomControllerConnectPopup(); // 룸콘 정보가 없거나, 룸콘이 연결되어 있지 않아 룸콘 등록 팝업 표시
                                        }
                                        break;
                                }

                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        cancelLoadingTimer();
                                        DismissConnectDialog();
                                    }
                                });
                            } catch (JSONException e) {
                                e.printStackTrace();
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        cancelLoadingTimer();
                                        DismissConnectDialog();
                                    }
                                });
                            }
                        }
                    });
        } catch (Exception e) {
            e.printStackTrace();
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    cancelLoadingTimer();
                    DismissConnectDialog();
                }
            });
        }
    }

    //룸콘 경고 팝업
    private void showRoomControllerConnectPopup(){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(null == dialog) dialog = new RoomControllerDialog(MainPrismActivity.this, R.style.DialogTheme);

                if(!dialog.isShowing()){
                    startFragmentAnimation(); // 룸콘이 정상적으로 연결되어 있음

                    dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
                    dialog.setDialogListener(new RoomControllerDialog.OnDialogClickButtonListener() {
                        @Override
                        public void onDetailClicked() {
                            dialog.dismiss();
                            Intent intent = new Intent(MainPrismActivity.this, RoomControlerDetailActivity.class);
                            intent.putExtra("mode", MODE_DEVICE_ROOM_CON); //룸콘 재연결
                            startActivityForResult(intent, RESULT_DETAIL_CHECK);
                            overridePendingTransition(R.anim.slide_down_in, R.anim.slide_none);
                        }

                        @Override
                        public void onCancelClicked() {
                            dialog.dismiss();
                        }
                    });
                    dialog.show();
                }
            }
        });
    }

    /**
     * 애니메이션 시작 함수.
     */
    private void startFragmentAnimation() {
        try {
            ((HomePrismFragment) mainPagerAdapter.getItem(mPager.getCurrentItem())).drawBottomLayout();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}