package kr.co.kdone.airone.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import androidx.appcompat.app.AppCompatActivity;
import android.text.Spannable;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import kr.co.kdone.airone.R;
import kr.co.kdone.airone.data.UserInfo;
import kr.co.kdone.airone.udp.WifiAP;
import kr.co.kdone.airone.utils.CommonUtils;

import static kr.co.kdone.airone.utils.CommonUtils.setEditText;

/**
 * 팝업 화면
 */
public class PopupActivity extends AppCompatActivity implements View.OnClickListener {
    private final String TAG = getClass().getSimpleName();

    /**
     * 일반적인 팝업
     */
    public final static int MODE_NORMAL = 1;
    /**
     * 붉은 글씨 포함된 팝업
     */
    public final static int MODE_YESNO = 2;
    /**
     * 입력 팝업
     */
    public final static int MODE_INPUT_TEXT = 3;
    /**
     * 버튼 없는 팝업
     */
    public final static int MODE_NO_BUTTON = 4;
    /**
     * 사용자 삭제 팝업
     */
    public final static int MODE_DELETE_USER = 5;
    /**
     * 관리자 지정 팝업
     */
    public final static int MODE_CHANGE_ADMIN = 6;
    /**
     * AP 선택 팝업
     */
    public final static int MODE_SELECT_AP = 7;

    public final static int MODE_GET_ID = 8;

    public final static String POPUP_MODE = "popup_mode";

    public final static String POPUP_TITLE_TEXT = "popup_title_text";
    public final static String POPUP_BODY_TEXT = "popup_body_text";
    public final static String POPUP_BODY_COLOR_MARK = "popup_body_color_mark";
    public final static String POPUP_INPUT_TEXT = "popup_input_text";
    public final static String POPUP_POSITION = "popup_position";
    public final static String POPUP_CONFIRM_BUTTON_TEXT = "popup_confirm_button_text";
    public final static String POPUP_CANCEL_BUTTON_TEXT = "popup_cancel_button_text";
    public final static String POPUP_USER_LIST = "popup_user_list";
    public final static String POPUP_AP_LIST = "popup_ap_list";

    public final static String POPUP_BUNDLE = "popup_bundle";
    public final static String POPUP_TIMEOUT = "popup_timeout";

    private int mEntryMode;
    private Activity mActivity;
    private Button mConfirmBtn;
    private Button mCancelBtn;
    private EditText mTxtInput;
    private String mBodyText;
    private String mTitleText;
    private String mBodyColorMark;
    private TextView mTxtBody;
    private TextView mTxtTitle;
    private Button mBtnConfirm;
    private Button mBtnCancel;
    private Bundle mBundle;
    private int mTimeout;
    private Handler mHandler;
    private Timer mTimer;
    private int mSelectedPosition;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        mEntryMode = 0;
        mEntryMode = intent.getIntExtra(POPUP_MODE, 0);
        mTimeout = intent.getIntExtra(POPUP_TIMEOUT, 0);

        if(mEntryMode == MODE_NORMAL ||
                mEntryMode == MODE_YESNO ||
                mEntryMode == MODE_GET_ID ||
                mEntryMode == MODE_NO_BUTTON
                ) {
            setContentView(R.layout.activity_popup_normal);

            mTxtBody = findViewById(R.id.txtBody);
            mBtnConfirm = findViewById(R.id.btnConfirm);
            mBtnCancel = findViewById(R.id.btnCancel);
            mBodyText=intent.getStringExtra(POPUP_BODY_TEXT);
            mBodyColorMark=intent.getStringExtra(POPUP_BODY_COLOR_MARK);

            if(mBodyColorMark != null && !mBodyColorMark.isEmpty()) {
                String bodyColorMark = mBodyColorMark;
                String popupBodyTextStr = mBodyText;
                Spannable wordtoSpan = Spannable.Factory.getInstance().newSpannable(popupBodyTextStr);
                wordtoSpan.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.text_tab_select_color)), popupBodyTextStr.indexOf(bodyColorMark), popupBodyTextStr.indexOf(bodyColorMark) + bodyColorMark.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                mTxtBody.setText(wordtoSpan);
            } else {
                mTxtBody.setText(mBodyText);
            }
            if(mEntryMode == MODE_NO_BUTTON && mTimeout == 0){
                mTimeout = 2000;
            }
            if (mTimeout > 0) {
                mHandler = new Handler();
                startDelayTimer(mTimeout);
            }

        } else if(mEntryMode == MODE_INPUT_TEXT) {
            setContentView(R.layout.activity_popup_input);

            mTitleText=intent.getStringExtra(POPUP_TITLE_TEXT);
            mTxtTitle = findViewById(R.id.txtTitle);
            mTxtTitle.setText(mTitleText);
            mTxtInput = findViewById(R.id.txtInput);
            mTxtInput.setText(intent.getStringExtra(POPUP_INPUT_TEXT));
        } else if(mEntryMode == MODE_DELETE_USER || mEntryMode == MODE_CHANGE_ADMIN){
            setContentView(R.layout.activity_popup_delete_user);
            mSelectedPosition = -1;

            mTxtTitle = findViewById(R.id.txtTitle);
            if(mEntryMode == MODE_DELETE_USER) {
                mTxtTitle.setText(getString(R.string.delete_user));
            } else {
                mTxtTitle.setText(getString(R.string.setting_admin));
            }
            ListView listView = findViewById(R.id.listView);
            ArrayList<UserInfo> list = (ArrayList<UserInfo>) intent.getSerializableExtra(POPUP_USER_LIST);
            final ListViewAdapter listAdapter = new ListViewAdapter(this,list);
            listView.setAdapter(listAdapter);

            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    UserInfo item = (UserInfo) parent.getItemAtPosition(position);
                    mSelectedPosition = position;
                    listAdapter.notifyDataSetChanged();
                    mBtnConfirm.setEnabled(true);
                }
            });
        } else if(mEntryMode == MODE_SELECT_AP){
            setContentView(R.layout.activity_popup_select_ap);
            mSelectedPosition = -1;

            mTxtTitle = findViewById(R.id.txtTitle);
            mTxtTitle.setText(getString(R.string.select_ap));

            ListView listView = findViewById(R.id.listView);
            ArrayList<WifiAP> list = (ArrayList<WifiAP>) intent.getSerializableExtra(POPUP_AP_LIST);
            final ApListViewAdapter listAdapter = new ApListViewAdapter(this,list);
            listView.setAdapter(listAdapter);

            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    WifiAP item = (WifiAP) parent.getItemAtPosition(position);
                    mSelectedPosition = position;
                    listAdapter.notifyDataSetChanged();
                    mBtnConfirm.setEnabled(true);
                }
            });
        }

        //Button Settting
        if(mEntryMode == MODE_NORMAL || mEntryMode == MODE_YESNO || mEntryMode == MODE_GET_ID || mEntryMode == MODE_INPUT_TEXT || mEntryMode == MODE_NO_BUTTON || mEntryMode == MODE_DELETE_USER || mEntryMode == MODE_CHANGE_ADMIN || mEntryMode == MODE_SELECT_AP) {
            mBtnConfirm = findViewById(R.id.btnConfirm);
            mBtnCancel = findViewById(R.id.btnCancel);

            if (mEntryMode == MODE_NORMAL) {
                mBtnCancel.setVisibility(View.GONE);
            } else if(mEntryMode == MODE_NO_BUTTON){
                mBtnConfirm.setVisibility(View.GONE);
                mBtnCancel.setVisibility(View.GONE);
            } else if(mEntryMode == MODE_DELETE_USER || mEntryMode == MODE_CHANGE_ADMIN || mEntryMode == MODE_SELECT_AP){
                mBtnConfirm.setEnabled(false);
            }
        }

        String confirmText =intent.getStringExtra(POPUP_CONFIRM_BUTTON_TEXT);
        String cancelText =intent.getStringExtra(POPUP_CANCEL_BUTTON_TEXT);
        if(mBtnConfirm != null && confirmText != null && !confirmText.isEmpty()){
            mBtnConfirm.setText(confirmText);
        }
        if(mBtnCancel != null && cancelText != null && !cancelText.isEmpty()){
            mBtnCancel.setText(cancelText);
        }

        setEditText(findViewById(R.id.layoutMain));
    }

    @Override
    protected void onResume() {
        super.onResume();

        Animation anim = new AlphaAnimation(0.0f, 1.0f);
        anim.setDuration(300); //You can manage the blinking time with this parameter
        anim.setStartOffset(100);
        anim.setRepeatMode(Animation.RESTART);
        anim.setRepeatCount(0);

        anim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationEnd(Animation animation) {
                if (PopupActivity.this.mEntryMode == MODE_INPUT_TEXT) {
                    mTxtInput.requestFocus();
                }
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }

            @Override
            public void onAnimationStart(Animation animation) {

            }
        });

        ((LinearLayout)findViewById(R.id.layoutContainer)).startAnimation(anim);
    }

    @Override
    protected void onPause() {
        super.onPause();
        overridePendingTransition(0,0);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mTimer != null){
            mTimer.cancel();
            mTimer = null;
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btnConfirm:
                Intent intent = getIntent();
                if(mEntryMode == MODE_GET_ID){
                    String ssid = "";

                    WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                    WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                    ssid = wifiInfo.getSSID();

                    if(TextUtils.isEmpty(ssid)){
                        ConnectivityManager connectivityManager = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
                        NetworkInfo netInfo = connectivityManager.getActiveNetworkInfo();
                        if (netInfo != null && netInfo.isConnected()) {
                            ssid = netInfo.getExtraInfo();
                        }
                    }

                    if(ssid.contains("NTR") || ssid.contains("NTS")) {
                        setResult(RESULT_OK, intent);
                        finish();
                    } else {
                        CommonUtils.showToast(PopupActivity.this, getString(R.string.check_wifi_for_get_id));
                    }
                } else {
                    if (mEntryMode == MODE_INPUT_TEXT) {
                        if (mTxtInput != null && !mTxtInput.getText().toString().isEmpty()) {
                            intent.putExtra(POPUP_INPUT_TEXT, mTxtInput.getText().toString());
                        }
                    } else if (mEntryMode == MODE_DELETE_USER || mEntryMode == MODE_CHANGE_ADMIN || mEntryMode == MODE_SELECT_AP) {
                        intent.putExtra(POPUP_POSITION, mSelectedPosition);
                    }
                    setResult(RESULT_OK, intent);
                    finish();
                }
                break;
            case R.id.btnCancel:
                setResult(RESULT_CANCELED, null);
                finish();
                break;
        }
    }

    /**
     * 일정 시간 후에 팝업 사라게하는 함수
     * @param period
     */
    private void startDelayTimer(int period){
        if(mTimer != null){
            mTimer.cancel();
        }
        mTimer = new Timer();
        mTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        Intent intent = getIntent();
                        setResult(RESULT_OK, intent);
                        finish();
                    }
                });
            }
        }, period);
    }

    private class ViewHolder {
        public TextView mTxtName;
        public TextView mTxtSetting;
        public LinearLayout mLayoutUser;
    }

    private class ListViewAdapter extends BaseAdapter {
        private Context mContext = null;
        private ArrayList<UserInfo> mListData;

        public ListViewAdapter(Context context,ArrayList<UserInfo> list) {
            mContext = context;
            mListData = list;
        }
        @Override
        public int getCount() {
            return mListData.size();
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
            final ViewHolder holder;
            if(convertView == null){
                holder = new ViewHolder();
                LayoutInflater inflater = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.listview_user_list_item,null);

                holder.mTxtName = convertView.findViewById(R.id.txtName);
                holder.mLayoutUser = convertView.findViewById(R.id.layoutUser);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder)convertView.getTag();
            }

            UserInfo mData = mListData.get(position);
            holder.mTxtName.setText(mData.getName());
            holder.mLayoutUser.setSelected(mSelectedPosition==position);
            return convertView;
        }
    }


    private class ApListViewHolder {
        public View container;
        public TextView nameTextView;
        public ImageView signalStrengthImageView;
    }

    private class ApListViewAdapter extends BaseAdapter {
        private Context mContext = null;
        private ArrayList<WifiAP> mListData;

        public ApListViewAdapter(Context context,ArrayList<WifiAP> list) {
            mContext = context;
            mListData = list;
        }
        @Override
        public int getCount() {
            return mListData.size();
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
            final ApListViewHolder holder;
            if(convertView == null){
                holder = new ApListViewHolder();
                LayoutInflater inflater = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.listview_setting_wifi_item,null);
                holder.container = convertView.findViewById(R.id.layoutMain);
                holder.nameTextView = (TextView) convertView.findViewById(R.id.ap_name);
                holder.signalStrengthImageView = (ImageView) convertView.findViewById(R.id.ap_signal);
                convertView.setTag(holder);
            } else {
                holder = (ApListViewHolder)convertView.getTag();
            }

            WifiAP mData = mListData.get(position);
            holder.nameTextView.setText(mData.ssid);
            holder.signalStrengthImageView.setImageLevel(mData.getLevel());
            holder.container.setSelected(mSelectedPosition==position);
            return convertView;
        }
    }
}
