package kr.co.kdone.airone.activity.more;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import androidx.appcompat.app.AppCompatActivity;
import kr.co.kdone.airone.CleanVentilationApplication;
import kr.co.kdone.airone.R;
import kr.co.kdone.airone.data.NotificationListInfo;
import kr.co.kdone.airone.utils.CommonUtils;
import kr.co.kdone.airone.utils.HttpApi;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

import static kr.co.kdone.airone.utils.CommonUtils.DismissConnectDialog;
import static kr.co.kdone.airone.utils.CommonUtils.displayProgress;
import static kr.co.kdone.airone.utils.CommonUtils.isUserInfoSection;

/**
 * ikHwang 2019-06-04 오전 9:52 공지사항 화면
 */
public class MoreNotificationActivity extends AppCompatActivity implements View.OnClickListener {
    private final String TAG = getClass().getSimpleName();

    private Activity mActivity;
    private ListView mListView;
    private ListViewAdapter mListAdapter;
    private ArrayList<NotificationListInfo> mNotiList;

    private LinearLayout mLayoutNotice;
    private TextView mTxtSubTitle;
    private TextView mTxtTime;
    private TextView mTxtContents;

    private int MAX_COUNT = 10;
    private boolean mIsMoreShow = false;
    private LinearLayout mLayoutMore;
    private int pageNo = 1;
    private int pageRowCount = 10;
    private boolean getNoticeStart = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_more_notification);
        isUserInfoSection(this);

        mActivity = this;
        mIsMoreShow = false;
        mLayoutMore = findViewById(R.id.layoutMore);

        mLayoutNotice = findViewById(R.id.layoutNotice);
        mLayoutNotice.setVisibility(View.INVISIBLE);
        mTxtSubTitle = findViewById(R.id.txtSubTitle);
        mTxtTime = findViewById(R.id.txtTime);
        mTxtContents = findViewById(R.id.txtContents);

        mListView = findViewById(R.id.listView);
        mNotiList = new ArrayList<NotificationListInfo>();
        mListAdapter = new ListViewAdapter(this, mNotiList);
        mListView.setAdapter(mListAdapter);

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                NotificationListInfo item = (NotificationListInfo) parent.getItemAtPosition(position);
                mTxtSubTitle.setText(item.Title);
                mTxtTime.setText(item.registTime);
                mTxtContents.setText(item.Content);
                mListView.setVisibility(View.INVISIBLE);
                mLayoutNotice.setVisibility(View.VISIBLE);
            }
        });

        mLayoutMore.setVisibility(View.GONE);
        setMarginBottom(mListView, 0);

        mListView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                int lastItem = firstVisibleItem + visibleItemCount;

                if(0 == lastItem) return;

                if(lastItem == totalItemCount && ((pageRowCount * pageNo) < MAX_COUNT)){
                    if(!getNoticeStart && mListAdapter.getCount() < MAX_COUNT){
                        getNoticeStart = true;

                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                pageNo++;
                                getnoticeList();
                            }
                        }, 500);
                    }
                }
            }
        });

        getNoticeStart = true;
        getnoticeList();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public void onBackPressed() {
        if (mLayoutNotice.getVisibility() == View.VISIBLE) {
            mListView.setVisibility(View.VISIBLE);
            mLayoutNotice.setVisibility(View.INVISIBLE);
        } else {
            finish();
        }
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(0, R.anim.slide_right_out);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnBack:
                onBackPressed();
                break;
            case R.id.layoutMore:
                mIsMoreShow = true;
                v.setVisibility(View.GONE);
                setMarginBottom(mListView, 0);
                mListAdapter.notifyDataSetChanged();
                break;
            default:
                break;
        }
    }

    private void setMarginBottom(View v, int bottomMargin) {
        ViewGroup.MarginLayoutParams parameter = (ViewGroup.MarginLayoutParams) v.getLayoutParams();
        parameter.setMargins(parameter.leftMargin, parameter.topMargin, parameter.rightMargin, bottomMargin); // left, top, right, bottom
        v.setLayoutParams(parameter);
    }

    private class ViewHolder {
        public ImageView mImgNew;
        public TextView mTxtTitle;
        public TextView mTxtTime;
    }

    private class ListViewAdapter extends BaseAdapter {
        private Context mContext = null;
        private ArrayList<NotificationListInfo> mListData;

        public ListViewAdapter(Context context, ArrayList<NotificationListInfo> list) {
            mContext = context;
            mListData = list;
        }

        @Override
        public int getCount() {
            return (!mIsMoreShow && mListData.size() > MAX_COUNT) ? MAX_COUNT : mListData.size();
        }

        @Override
        public Object getItem(int position) {
            return mListData.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final int pos = position;
            final ViewHolder holder;
            if (convertView == null) {
                holder = new ViewHolder();
                LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.listview_notice_list_item, null);

                holder.mImgNew = convertView.findViewById(R.id.img_notice_new);
                holder.mTxtTime = convertView.findViewById(R.id.txtTime);
                holder.mTxtTitle = convertView.findViewById(R.id.txtTitle);

                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            NotificationListInfo mData = mListData.get(position);

            if(mData.IsNew > 0){
                holder.mImgNew.setVisibility(View.VISIBLE);
            }else{
                holder.mImgNew.setVisibility(View.INVISIBLE);
            }

            holder.mTxtTime.setText(mData.registTime);
            holder.mTxtTitle.setText(mData.Title);

            return convertView;
        }
    }

    // ikHwang 2019.05.14 공지사항 리스트 조회
    private void getnoticeList(){
        displayProgress(this, "", "");
        try {
            HttpApi.PostV2GetNoticeList(pageNo, pageRowCount, CleanVentilationApplication.getInstance().getUserInfo().getId(), new Callback() { //V2A 작업.
                @Override
                public void onFailure(Call call, IOException e) {
                    DismissConnectDialog();
                    CommonUtils.showToast(MoreNotificationActivity.this, getString(R.string.toast_result_can_not_search));
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    try {
                        JSONObject json_data = new JSONObject(response.body().string());

                        CommonUtils.customLog(TAG, "board/notice : " + json_data.toString(), Log.ERROR);

                        switch (json_data.getInt("code")){
                            case HttpApi.RESPONSE_SUCCESS:
                                if(json_data.has("data")){
                                    mActivity.runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            findViewById(R.id.constraint_not_noti).setVisibility(View.GONE);
                                            findViewById(R.id.constraint_noti).setVisibility(View.VISIBLE);
                                        }
                                    });

                                    JSONArray jarray = new JSONArray(json_data.getString("data"));

                                    for (int i = 0; i < jarray.length(); i++) {
                                        NotificationListInfo notice = new NotificationListInfo();

                                        JSONObject jObject = jarray.getJSONObject(i);  // JSONObject 추출
                                        notice.SEQ = jObject.has("seq") ? jObject.getInt("seq") : 0;
                                        notice.Title = jObject.has("title") ? jObject.getString("title") : "";
                                        notice.Content = jObject.has("content") ? jObject.getString("content") : "";
                                        notice.registTime = jObject.has("registTime") ? jObject.getString("registTime") : "";
                                        notice.IsNew = jObject.has("isNew") ? jObject.getInt("isNew") : 0;

                                        mNotiList.add(notice);
                                    }
                                }

                                // 전체 글수
                                if(json_data.has("totalCount")){
                                    MAX_COUNT = json_data.getInt("totalCount");
                                }else{
                                    MAX_COUNT = 10;
                                }
                                break;
                                
                            case HttpApi.RESPONSE_DATA_NO_EXIST: // 데이터 없음
                                mActivity.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        if(mNotiList.size() <= 0){
                                            findViewById(R.id.constraint_not_noti).setVisibility(View.VISIBLE);
                                            findViewById(R.id.constraint_noti).setVisibility(View.GONE);
                                        }
                                    }
                                });

                                break;

                            default:
                                CommonUtils.showToast(MoreNotificationActivity.this, getString(R.string.toast_result_can_not_search));
                                break;
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        CommonUtils.showToast(MoreNotificationActivity.this, getString(R.string.toast_result_can_not_search));
                    } finally {
                        mActivity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mListAdapter.notifyDataSetChanged();
                                DismissConnectDialog();
                            }
                        });
                    }
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
            CommonUtils.showToast(MoreNotificationActivity.this, getString(R.string.toast_result_can_not_search));
            DismissConnectDialog();
        } finally {
            getNoticeStart = false;
        }
    }
}
