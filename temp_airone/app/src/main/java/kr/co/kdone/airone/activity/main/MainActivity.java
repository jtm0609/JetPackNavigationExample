package kr.co.kdone.airone.activity.main;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;
import kr.co.kdone.airone.CleanVentilationApplication;
import kr.co.kdone.airone.R;
import kr.co.kdone.airone.activity.info.RoomControlerDetailActivity;
import kr.co.kdone.airone.activity.more.MoreActivity;
import kr.co.kdone.airone.activity.more.MoreHelpActivity;
import kr.co.kdone.airone.data.DeviceInfo;
import kr.co.kdone.airone.data.HomeInfo;
import kr.co.kdone.airone.fragments.HomeFirstFragment;
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
import static kr.co.kdone.airone.utils.SharedPrefUtil.DEFAULT_GRAPH_START_POSITION;
import static kr.co.kdone.airone.utils.SharedPrefUtil.GRAPH_START_POSITION;

/**
 * ikHwang 2019-06-04 ?????? 9:48 ??? ??????
 */
public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private final String TAG = getClass().getSimpleName();

    private LinearLayout mLayoutIndicator;
    private int ID = 100;
    private final int HOME_PAGER_COUNT = 1;
    public static HomeInfo mHomeInfo;
    public static GraphPosition mGraphPosition;
    public static DeviceInfo mControlDeviceInfo = new DeviceInfo();

    private Timer mLoadingTimer;
    private long LOADING_TIME = 10 * 1000;

    ViewPager mPager;
    private RefleshListener mRefleshListener;

    public interface RefleshListener {
        void refleshInfo();
    }

    // ikHwang 2019-05-22 ?????? 3:18 ???????????? ????????? ??????????????? ???????????? ?????? ?????????
    private Timer timer;
    private TimerTask timerTask;
    private int TIMER_DELAY = 10; // ???????????? ????????? ?????? 30???

    // ikHwang 2019-05-24 ?????? 1:58 ?????? ????????? ??????????????? ?????? ?????? ???????????? ?????? ??????
    private RoomControllerDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        overridePendingTransition(R.anim.slide_none, 0);

        // ikHwang 2019-05-23 ?????? 6:00 ????????? ?????? ??????
        TIMER_DELAY = (SharedPrefUtil.getInt(SharedPrefUtil.SAVE_MAIN_INTERVAL, TIMER_DELAY) * 1000);

        isUserInfoSection(this);

        if (getIntent().getExtras() != null) {
            onNewIntent(getIntent());
        }

        mRefleshListener = new RefleshListener() {
            @Override
            public void refleshInfo() {
                getDeviceList(true);
            }
        };

        mHomeInfo = new HomeInfo();
        mGraphPosition = new GraphPosition();
        mGraphPosition.getData();

        mPager = (ViewPager) findViewById(R.id.viewPager);
        mPager.setAdapter(new MyPagerAdapter(getSupportFragmentManager()));

        mLayoutIndicator = (LinearLayout) findViewById(R.id.layoutIndicator);
        generateIndicators(HOME_PAGER_COUNT);
        mPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            int oldPosition = 0;

            @Override
            public void onPageSelected(int position) {
                try {
                    ((ImageView) mLayoutIndicator.getChildAt(oldPosition)).setBackgroundResource(R.drawable.paging_default);
                    ((ImageView) mLayoutIndicator.getChildAt(position)).setBackgroundResource(R.drawable.paging_current);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                oldPosition = position;
                startFragmentAnimation();
            }

            //this method will be called repeatedly upto another item comes as front one(active one)
            @Override
            public void onPageScrolled(int arg0, float arg1, int arg2) {

            }

            //this will be called as per scroll state
            @Override
            public void onPageScrollStateChanged(int arg0) {

            }
        });

        //?????? Notification ??????
        if (getIntent().hasExtra("MsgCode") && getIntent().getStringExtra("MsgCode").equals("15")) {
            findViewById(R.id.layoutMenu03).performClick();
        }

        // ikHwang 2019-05-22 ?????? 10:02 ???????????? ????????? ???????????? ?????? ???????????? ?????????????????? ??????
        try {
            if(CleanVentilationApplication.getInstance().isConnectedRoomController()){
                // ????????? ??????????????? ???????????? ??????
                mPager.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        startFragmentAnimation();
                    }
                }, 500);
            }else{
                showRoomControllerConnectPopup(); // ?????? ????????? ?????????, ????????? ???????????? ?????? ?????? ?????? ?????? ?????? ??????
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

        // ikHwang 2019.04.18 ???????????? ????????? ???????????? ???????????? ?????????
        timer = new Timer();
        timerTask = new TimerTask(){

            @Override
            public void run() {
                getDeviceList(false);
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

        // ikHwang 2019-05-22 ?????? 3:37 ???????????? ????????? ??????
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
            /*case R.id.btnReplace:
                getDeviceList(true);
                break;*/

            case R.id.btnMore: // ????????? (??????)
                Intent intentMore = new Intent(v.getContext(), MoreActivity.class);
                intentMore.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivityAni(MainActivity.this, intentMore, false, 1);
                break;

            case R.id.btnHelp:
                Intent intent = new Intent(v.getContext(), MoreHelpActivity.class);
                startActivityAni(MainActivity.this, intent, false, 1);
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
                Intent intent = new Intent(this, MoreActivity.class);
                startActivityAni(MainActivity.this, intent, false, 1);

                new Timer().schedule(new TimerTask() {
                    @Override
                    public void run() {
                        finish();
                    }
                }, 500);
                break;
            }
            case RESULT_ERROR_STATE_DEVICE_NO_CONNECTED: {
                if (resultCode == RESULT_OK) {
                }
                break;
            }
            case RESULT_DETAIL_CHECK: {
                if (resultCode == RESULT_OK) {
                    if (SharedPrefUtil.getString(CURRENT_CONTROL_DEVICE_ID, "").equals("")) {
                        getDeviceList(true);
                    }
                }
                break;
            }
        }
    }

    /**
     * ???????????? ????????? ??????.
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

    private class MyPagerAdapter extends FragmentPagerAdapter {

        public MyPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int pos) {
            Fragment f;
            switch (pos) {
                case 0:
                    f = HomeFirstFragment.newInstance(getString(R.string.total));
                    ((HomeFirstFragment) f).setListener(mRefleshListener);
                    break;
                /*case 1:
                    f = HomeSecondFragment.newInstance(getString(R.string.inside));
                    ((HomeSecondFragment)f).setListener(mRefleshListener);
                    break;
                case 2:
                    f =  HomeThirdFragment.newInstance(getString(R.string.outside));
                    break;*/
                default:
                    f = HomeFirstFragment.newInstance(getString(R.string.total));
                    break;
            }
            return f;

        }

        @Override
        public int getCount() {
            return HOME_PAGER_COUNT;
        }

        @Override
        public int getItemPosition(@NonNull Object object) {
            return POSITION_NONE;
        }
    }

    private void generateIndicators(int count) {

        mLayoutIndicator.removeAllViews();

        for (int i = 0; i < count; i++) {
            ImageView iv = new ImageView(this);
            iv.setId(ID + i);
            final int currentItem = i;
            iv.setBackgroundResource(R.drawable.paging_default);
            /// Converts 14 dip into its equivalent px
            int width = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4, getResources().getDisplayMetrics());
            int height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4, getResources().getDisplayMetrics());
            int marginLeft = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 7, getResources().getDisplayMetrics());
            int marginRight = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 7, getResources().getDisplayMetrics());

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(width, height);
            params.setMargins(marginLeft, 0, marginRight, 0);
            mLayoutIndicator.addView(iv, params);
        }
        ((ImageView) mLayoutIndicator.getChildAt(0)).setBackgroundResource(R.drawable.paging_current);
    }

    /**
     * ?????? ????????? ?????? ??????.
     */
    private void getDeviceList(boolean showProgress) {
        if(showProgress){
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        if (!IsRunningProgress()) {
                            CommonUtils.displayProgress(MainActivity.this, "", "");
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        }

        try {
            HttpApi.PostV2GetMainInfo( //V2A ??????.
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
                            CommonUtils.showToast(MainActivity.this, getString(R.string.toast_result_can_not_request));
                        }

                        @Override
                        public void onResponse(Call call, Response response) throws IOException {
                            try {
                                String strResponse =  response.body().string();

                                CommonUtils.customLog(TAG, "main/info : " + strResponse, Log.ERROR);

                                JSONObject json_data = new JSONObject(strResponse);
                                
                                switch (json_data.optInt("code", 0)){
                                    case HttpApi.RESPONSE_SUCCESS: // ?????? ?????? ?????? ??????
                                        if(json_data.has("data")){
                                            // ikHwang 2019-05-21 ?????? 1:32 ???????????? ????????? ????????? ???????????? ?????? ????????? ??????
                                            HomeInfoDataParser.paserHomeInfo(CleanVentilationApplication.getInstance(), json_data.getJSONObject("data"), true);

                                            Intent sendIntent = new Intent(WidgetUtils.WIDGET_UPDATE_ACTION);
                                            sendBroadcast(sendIntent, getString(R.string.br_permission));
                                        }

                                        // ikHwang 2019-05-27 ?????? 10:28 ????????? ????????? ?????? ?????? ?????? ???????????? ??????
                                        switch (CleanVentilationApplication.getInstance().getHomeState()){
                                            case CleanVentilationApplication.STATE_AIR_MONITOR_NO_REGISTER: // 1??? ????????? ?????? ????????? ?????????
                                            case CleanVentilationApplication.STATE_AIR_MONITOR_NOT_CONNECTED: // 1??? ????????? ?????? ????????? ?????????
//                                            case CleanVentilationApplication.STATE_AIR_MONITOR_NOT_CONNECTED_2ND: // 2??? ?????? ????????? ?????????, ????????? ??????
                                            case CleanVentilationApplication.STATE_AIR_MONITOR_UNKNOWN_INDISEINFO: // 1,2??? ?????? ????????? ?????? ?????? ??????
                                            case CleanVentilationApplication.STATE_AIR_MONITOR_HAS_INDISEINFO: // 1,2??? ?????? ????????? ?????? ?????? ?????? (???????????? UI ??????)
                                                if(null != dialog && dialog.isShowing()){
                                                    dialog.dismiss();
                                                }
                                                break;

                                            default: // ?????? ????????? ??? ????????? ??????
                                                break;
                                        }

                                        Intent sendIntent = new Intent(WidgetUtils.WIDGET_UPDATE_ACTION);
                                        sendBroadcast(sendIntent, getString(R.string.br_permission));

                                        if(CleanVentilationApplication.getInstance().isConnectedRoomController()){
                                            runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    startFragmentAnimation(); // ????????? ??????????????? ???????????? ??????
                                                }
                                            });
                                        }else{
                                            showRoomControllerConnectPopup(); // ?????? ????????? ?????????, ????????? ???????????? ?????? ?????? ?????? ?????? ?????? ??????
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

    //?????? ?????? ??????
    private void showRoomControllerConnectPopup(){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(null == dialog) dialog = new RoomControllerDialog(MainActivity.this, R.style.DialogTheme);

                if(!dialog.isShowing()){
                    startFragmentAnimation(); // ????????? ??????????????? ???????????? ??????

                    dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
                    dialog.setDialogListener(new RoomControllerDialog.OnDialogClickButtonListener() {
                        @Override
                        public void onDetailClicked() {
                            dialog.dismiss();
                            Intent intent = new Intent(MainActivity.this, RoomControlerDetailActivity.class);
                            intent.putExtra("mode", MODE_DEVICE_ROOM_CON); //?????? ?????????
                            startActivityForResult(intent, RESULT_DETAIL_CHECK);
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
     * ??????????????? ?????? ??????.
     */
    private void startFragmentAnimation() {
        Fragment page = getSupportFragmentManager().findFragmentByTag("android:switcher:" + R.id.viewPager + ":" + mPager.getCurrentItem());
        if (page != null) {
            if (mPager.getCurrentItem() == 0) {
                ((HomeFirstFragment) page).drawBottomLayout();
            }
        }
    }

    /**
     * ???????????? ????????? ????????? ????????? ??????.
     */
    public class GraphPosition {
        public int first_outside_dustlevel;
        public int first_outside_microdustlevel;
        public int first_inside_dustlevel;
        public int first_inside_co2level;
        public int first_inside_voclevel;
        public int second_inside_dustlevel;
        public int second_inside_co2level;
        public int second_inside_voclevel;
        public int third_outside_dustlevel;
        public int third_outside_micorlevel;

        public GraphPosition() {
            first_outside_dustlevel = 0;
            first_outside_microdustlevel = 0;
            first_inside_dustlevel = 0;
            first_inside_co2level = 0;
            first_inside_voclevel = 0;
            second_inside_dustlevel = 0;
            second_inside_co2level = 0;
            second_inside_voclevel = 0;
            third_outside_dustlevel = 0;
            third_outside_micorlevel = 0;
        }

        public void getData() {
            String[] data = SharedPrefUtil.getString(GRAPH_START_POSITION, DEFAULT_GRAPH_START_POSITION).split(",");
            if (data.length == 10) {
                first_outside_dustlevel = Integer.valueOf(data[0]);
                first_outside_microdustlevel = Integer.valueOf(data[1]);
                first_inside_dustlevel = Integer.valueOf(data[2]);
                first_inside_co2level = Integer.valueOf(data[3]);
                first_inside_voclevel = Integer.valueOf(data[4]);
                second_inside_dustlevel = Integer.valueOf(data[5]);
                second_inside_co2level = Integer.valueOf(data[6]);
                second_inside_voclevel = Integer.valueOf(data[7]);
                third_outside_dustlevel = Integer.valueOf(data[8]);
                third_outside_micorlevel = Integer.valueOf(data[9]);
            }
        }

        public void saveData() {
            String s =
                    first_outside_dustlevel + "," +
                            first_outside_microdustlevel + "," +
                            first_inside_dustlevel + "," +
                            first_inside_co2level + "," +
                            first_inside_voclevel + "," +
                            second_inside_dustlevel + "," +
                            second_inside_co2level + "," +
                            second_inside_voclevel + "," +
                            third_outside_dustlevel + "," +
                            third_outside_micorlevel;

            SharedPrefUtil.putString(GRAPH_START_POSITION, s);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case 1001:
                break;
        }
    }
}
