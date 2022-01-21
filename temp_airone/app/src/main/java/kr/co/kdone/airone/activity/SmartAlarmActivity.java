package kr.co.kdone.airone.activity;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import androidx.appcompat.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.applidium.headerlistview.HeaderListView;
import com.applidium.headerlistview.SectionAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

import kr.co.kdone.airone.CleanVentilationApplication;
import kr.co.kdone.airone.R;
import kr.co.kdone.airone.activity.more.MoreActivity;
import kr.co.kdone.airone.activity.more.MoreHelpActivity;
import kr.co.kdone.airone.data.DeviceManager;
import kr.co.kdone.airone.utils.CommonUtils;
import kr.co.kdone.airone.utils.HttpApi;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

import static kr.co.kdone.airone.utils.CommonUtils.DismissConnectDialog;
import static kr.co.kdone.airone.utils.CommonUtils.displayProgress;
import static kr.co.kdone.airone.utils.CommonUtils.getNewSmartAlarmIcon;
import static kr.co.kdone.airone.utils.CommonUtils.initiateTwoButtonAlert;
import static kr.co.kdone.airone.utils.CommonUtils.isUserInfoSection;
import static kr.co.kdone.airone.utils.CommonUtils.startActivityAni;

/**
 * ikHwang 2019-06-04 오전 9:54 스마트 알람 화면
 */
public class SmartAlarmActivity extends AppCompatActivity implements View.OnClickListener{
    private String TAG = this.getClass().getSimpleName();

    private Map<String, ArrayList<SmartListData>> mSmartAlarmLIst2;
    private SectionAdapter sectionAdapter;
    private HeaderListView mListView;
    private TextView mTxtNoData;
    private String mSelectedSensorBoxID = "";

    // ikHwang 2019-05-15 오전 10:27 더보기 처리 하기위한 변수
    private int MAX_COUNT = 10; // 스마트알림 총 갯수
    private int pageNo = 1; // 현재 페이지
    private int pageRowCount = 10; // 페이지당 글 수
    private boolean getSmartAlarmStart = false; // 데이터 로딩 상태 변수
    private boolean mIsShowErrorPopup = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_smart_alarm);

        isUserInfoSection(this);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        mTxtNoData = findViewById(R.id.txtNoData);
        mSmartAlarmLIst2 = new LinkedHashMap<>();

        sectionAdapter = new SectionAdapter() {
            @Override
            public int numberOfSections() {
                return mSmartAlarmLIst2.size();
            }

            @Override
            public int numberOfRows(int section) {
                if(section < 0) return 0;

                String strHeader = (String) mSmartAlarmLIst2.keySet().toArray()[section];
                return mSmartAlarmLIst2.get(strHeader).size();
            }

            @Override
            public Object getRowItem(int section, int row) {
                return null;
            }

            @Override
            public boolean hasSectionHeaderView(int section) {
                return true;
            }

            @Override
            public int getSectionHeaderViewTypeCount() {
                return 2;
            }

            @Override
            public int getSectionHeaderItemViewType(int section) {
                return section % 2;
            }

            @Override
            public View getRowView(int section, int row, View convertView, ViewGroup parent) {
                SmartAlarmListViewHolder holder;
                if (convertView == null) {
                    holder = new SmartAlarmListViewHolder();

                    LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    convertView = inflater.inflate(R.layout.listview_smartalarm, null);

                    holder.txtDate = (TextView) convertView.findViewById(R.id.txtDate);
                    holder.imgIcon = (ImageView) convertView.findViewById(R.id.imgIcon);
                    holder.layoutLine2 = (FrameLayout) convertView.findViewById(R.id.layoutLine2);
                    holder.layoutLine3 = (FrameLayout) convertView.findViewById(R.id.layoutLine3);
                    holder.txtTime = (TextView) convertView.findViewById(R.id.txtTime);
                    holder.txtTitle = (TextView) convertView.findViewById(R.id.txtTitle);
                    holder.txtSubTitle = (TextView) convertView.findViewById(R.id.txtSubTitle);
                    holder.txtNavienhouse = (TextView) convertView.findViewById(R.id.txtNavienhouse);

                    convertView.setTag(holder);
                } else {
                    holder = (SmartAlarmListViewHolder) convertView.getTag();
                }

                String strHeader = (String) mSmartAlarmLIst2.keySet().toArray()[section];
                SmartListData listData = mSmartAlarmLIst2.get(strHeader).get(row);

                Date date = new Date();
                date.setTime(listData.time);

                SimpleDateFormat formatTime = new SimpleDateFormat("hh:mm aa");

                holder.imgIcon.setImageResource(getNewSmartAlarmIcon(listData.iconType));
                holder.txtTime.setText(formatTime.format(date));
                holder.txtTitle.setText(listData.txtTitleType);
                holder.txtSubTitle.setText(listData.txtSubTitleType);

                if(TextUtils.isEmpty(listData.txtNavienhouse)){
                    holder.txtNavienhouse.setVisibility(View.GONE);
                }else{
                    holder.txtNavienhouse.setVisibility(View.VISIBLE);
                }

                holder.txtNavienhouse.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(listData.txtNavienhouse)); //수정 jh.j
                        startActivityAni(SmartAlarmActivity.this, intent, false, 1);
                    }
                });

                holder.txtDate.setVisibility(View.GONE);

                return convertView;
            }

            @Override
            public View getSectionHeaderView(int section, View convertView, ViewGroup parent) {
                SmartAlarmListViewHolder holder;
                if (convertView == null) {
                    holder = new SmartAlarmListViewHolder();

                    LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    convertView = inflater.inflate(R.layout.listview_smartalarm_header, null);

                    holder.txtDate = convertView.findViewById(R.id.txtDate);

                    convertView.setTag(holder);
                } else {
                    holder = (SmartAlarmListViewHolder) convertView.getTag();
                }

                String strHeader = (String) mSmartAlarmLIst2.keySet().toArray()[section];
                holder.txtDate.setText(strHeader);

                return convertView;
            }
        };

        mListView = findViewById(R.id.listViewSmartAlarm);
        mListView.setAdapter(sectionAdapter);

        mListView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                int lastItem = firstVisibleItem + visibleItemCount;

                CommonUtils.customLog(TAG, "aa : " + firstVisibleItem + " bb : " + visibleItemCount + " cc : " + totalItemCount + " // cnt : " + sectionAdapter.getCount() + "// max : " + MAX_COUNT, Log.ERROR);

                if(0 == lastItem) return;

                if(lastItem == totalItemCount && ((pageRowCount * pageNo) < MAX_COUNT)){
                    if(!getSmartAlarmStart && sectionAdapter.getCount() < MAX_COUNT){
                        getSmartAlarmStart = true;

                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                pageNo++;
                                getSmartAlarm();
                            }
                        }, 500);
                    }
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(CleanVentilationApplication.isAnyType()) overridePendingTransition(R.anim.slide_none, 0);
        CleanVentilationApplication.setAnyType(false);

        if(!mIsShowErrorPopup){
            pageNo = 1;
            getSmartAlarmStart = true;
            getSmartAlarm();
        }

        mIsShowErrorPopup = false;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnMore: // 더보기 (설정)
                Intent intentMore = new Intent(this, MoreActivity.class);
                intentMore.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivityAni(SmartAlarmActivity.this, intentMore, false, 1);
                break;

            case R.id.btnHelp: // 공기질 정보 화면
                Intent intentMoreHelp = new Intent(this, MoreHelpActivity.class);
                startActivityAni(SmartAlarmActivity.this, intentMoreHelp, false, 1);
                break;
        }
    }

    @Override
    public void onBackPressed() {
        initiateTwoButtonAlert(this, getString(R.string.exit_app), getString(R.string.confirm), getString(R.string.no));
    }

    @Override
    protected void onPause() {
        super.onPause();

        if(!mIsShowErrorPopup){
            DeviceManager.getInstance().setDataChangedListener(null);
        }
    }

    /**
     * ikHwang 2019.05.15 스마트 알림 조회 메소드처리
     */
    private void getSmartAlarm(){
        if(CleanVentilationApplication.getInstance().hasRoomController() && 3 == CleanVentilationApplication.getInstance().getRoomControllerDevicePCD()){
            mSelectedSensorBoxID = CleanVentilationApplication.getInstance().getRoomControllerDeviceID();
        }else{
            mSelectedSensorBoxID = CleanVentilationApplication.getInstance().getAirMonitorDeviceID();
        }

        if (mSelectedSensorBoxID != null && mSelectedSensorBoxID.length() > 0) {
            getSmartAlarmData(mSelectedSensorBoxID);
        } else {
            mTxtNoData.setText(getString(R.string.result_no_data_smartalarm));
            mTxtNoData.setVisibility(View.VISIBLE);
            mListView.setVisibility(View.GONE);
        }
    }

    private class SmartListData {
        public int seq;
        public int iconType;
        public long time;
        public String txtTitleType;
        public String txtSubTitleType;
        public String txtNavienhouse;
    }

    private class SmartAlarmListViewHolder {
        public TextView txtDate;
        public ImageView imgIcon;
        //        public FrameLayout layoutLine1;
        public FrameLayout layoutLine2;
        public FrameLayout layoutLine3;
        public TextView txtTime;
        public TextView txtTitle;
        public TextView txtSubTitle;
        public TextView txtNavienhouse;
    }

    private void getSmartAlarmData(String sensorboxId) {
        try {
            displayProgress(this, "", "");

            HttpApi.PostV2GetSmartAlarm( //V2A 작업.
                    pageNo,
                    pageRowCount,
                    CleanVentilationApplication.getInstance().getUserInfo().getId(),
                    sensorboxId,
                    new Callback() {
                        @Override
                        public void onFailure(Call call, IOException e) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    DismissConnectDialog();
                                    if (mSmartAlarmLIst2.size() == 0) {
                                        mTxtNoData.setText(getString(R.string.result_no_data_smartalarm));
                                        mTxtNoData.setVisibility(View.VISIBLE);
                                        mListView.setVisibility(View.GONE);
                                    }
                                }
                            });
                        }

                        @Override
                        public void onResponse(Call call, Response response) throws IOException {
                            try {
                                JSONObject json_data = new JSONObject(response.body().string());

                                CommonUtils.customLog(TAG, "board/smart-alarm : " + json_data.toString(), Log.ERROR);

                                switch (json_data.getInt("code")){
                                    case HttpApi.RESPONSE_SUCCESS:
                                        // 첫번째 페이지일 경우 에만 리스트 초기화 하도록 처리
                                        if(1 == pageNo) mSmartAlarmLIst2.clear();

                                        JSONArray jarray = new JSONArray(json_data.getString("data"));

                                        for (int i = 0; i < jarray.length(); i++) {
                                            SmartListData listdata = new SmartListData();

                                            JSONObject jObject = jarray.getJSONObject(i);  // JSONObject 추출
                                            listdata.seq = jObject.has("seq") ? jObject.getInt("seq") : 0;
                                            listdata.iconType = jObject.has("icon") ? jObject.getInt("icon") : 0;
                                            listdata.time = jObject.has("registTime") ? jObject.getLong("registTime") * 1000 : 0;

                                            JSONObject msgJsonObject = new JSONObject(jObject.has("content") ? jObject.getString("content") : "");

                                            //V2A에 없음. 수정해야함..
                                            listdata.txtTitleType = msgJsonObject.has("title") ? msgJsonObject.getString("title").trim() : "";
                                            listdata.txtSubTitleType = msgJsonObject.has("message") ? msgJsonObject.getString("message").trim() : "";

                                            String strMessageSub1 = msgJsonObject.has("messageSub1") ? msgJsonObject.getString("messageSub1").trim() : "";
                                            if(!TextUtils.isEmpty(strMessageSub1)) listdata.txtSubTitleType += ("\n" + strMessageSub1);

                                            String strMessageSub2 = msgJsonObject.has("messageSub2") ? msgJsonObject.getString("messageSub2").trim() : "";
                                            if(!TextUtils.isEmpty(strMessageSub2)) listdata.txtSubTitleType += ("\n" + strMessageSub2);

                                            String strMessageSub3 = msgJsonObject.has("customerCenterUrl") ? msgJsonObject.getString("customerCenterUrl").trim() : "";
//                                                if(i % 2 == 0) strMessageSub3 = "https://www.navienhouse.com/categories/index/clean_ventilation_system";
                                            listdata.txtNavienhouse = strMessageSub3;

                                            Date date = new Date();
                                            date.setTime(listdata.time);
                                            SimpleDateFormat formatDate = new SimpleDateFormat("yyyy/MM/dd/E요일");
                                            String key = formatDate.format(date);

                                            if(mSmartAlarmLIst2.containsKey(key)){
                                                mSmartAlarmLIst2.get(key).add(listdata);
                                            }else{
                                                ArrayList<SmartListData> arrayList = new ArrayList<>();
                                                arrayList.add(listdata);
                                                mSmartAlarmLIst2.put(key, arrayList);
                                            }
                                        }

                                        mListView.post(new Runnable() {
                                            @Override
                                            public void run() {
                                                if (sectionAdapter != null) {
                                                    sectionAdapter.notifyDataSetChanged();
                                                    if(1 == pageNo) mListView.getListView().setSelection(0);
                                                }
                                            }
                                        });

                                        MAX_COUNT = json_data.has("totalCount") ? json_data.getInt("totalCount") : 10;
                                        break;

                                    case HttpApi.RESPONSE_INVALID_PARAMETERS: // 파라미터 오류
                                        CommonUtils.showToast(SmartAlarmActivity.this, getString(R.string.error_code_api_502));
                                        break;

                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    DismissConnectDialog();
                                    if (mSmartAlarmLIst2.size() == 0) {
                                        mTxtNoData.setText(getString(R.string.result_no_data_smartalarm));
                                        mTxtNoData.setVisibility(View.VISIBLE);
                                        mListView.setVisibility(View.GONE);
                                    }else{
                                        mTxtNoData.setVisibility(View.GONE);
                                        mListView.setVisibility(View.VISIBLE);
                                    }
                                }
                            });
                        }
                    });
        } catch (Exception e) {
            e.printStackTrace();
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    DismissConnectDialog();
                }
            });
        } finally {
            getSmartAlarmStart = false;
        }
    }
}
