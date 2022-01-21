package kr.co.kdone.airone.activity.more;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import java.util.Timer;
import java.util.TimerTask;

import androidx.appcompat.app.AppCompatActivity;
import kr.co.kdone.airone.CleanVentilationApplication;
import kr.co.kdone.airone.R;
import kr.co.kdone.airone.activity.PopupActivity;
import kr.co.kdone.airone.activity.login.LoginActivity;
import kr.co.kdone.airone.utils.CommonUtils;
import kr.co.kdone.airone.utils.SharedPrefUtil;
import kr.co.kdone.airone.widget.WidgetUtils;

import static kr.co.kdone.airone.utils.CommonUtils.DismissConnectDialog;
import static kr.co.kdone.airone.utils.CommonUtils.IsRunningProgress;
import static kr.co.kdone.airone.utils.CommonUtils.RESULT_LOGOUT_COMFIRM;
import static kr.co.kdone.airone.utils.CommonUtils.isUserInfoSection;
import static kr.co.kdone.airone.utils.CommonUtils.startActivityAni;

/**
 * ikHwang 2019-06-04 오전 9:50 더보기 화면
 */
public class MoreActivity extends AppCompatActivity implements View.OnClickListener {
    private final String TAG = getClass().getSimpleName();

    private Timer mLoadingTimer;
    private long LOADING_TIME = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_more);

        isUserInfoSection(this);
    }

    @Override
    protected void onResume() {
        super.onResume();

        ((TextView) findViewById(R.id.txtName)).setText(CleanVentilationApplication.getInstance().getUserInfo().getName());
        ((TextView) findViewById(R.id.txtEmail)).setText(CleanVentilationApplication.getInstance().getUserInfo().getId());

        if(CleanVentilationApplication.getInstance().getUserInfo().getIsNewNotice() > 0){
            findViewById(R.id.img_notice_new).setVisibility(View.VISIBLE);
        }else{
            findViewById(R.id.img_notice_new).setVisibility(View.INVISIBLE);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(IsRunningProgress()){
            CommonUtils.DismissConnectDialog();
        }
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(0, R.anim.slide_up_out);
    }

    @Override
    public void onClick(View v) {
        Intent intent;
        switch (v.getId()) {
            case R.id.layoutSetting:
                intent = new Intent(v.getContext(), MoreSettingActivity.class);
                startActivityAni(MoreActivity.this, intent, false, 0);
                break;

            case R.id.layoutControlDevice:
                intent = new Intent(v.getContext(), DeviceSelectActivity.class);
                startActivityAni(MoreActivity.this, intent, false, 0);
                break;

            case R.id.layoutNotification:
                try {
                    CleanVentilationApplication.getInstance().getUserInfo().setIsNewNotice(0);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                intent = new Intent(v.getContext(), MoreNotificationActivity.class);
                startActivityAni(MoreActivity.this, intent, false, 0);
                break;

            case R.id.layoutHelp:
                intent = new Intent(v.getContext(), MoreHelpActivity.class);
                intent.putExtra("backType", true);
                startActivityAni(MoreActivity.this, intent, false, 0);
                break;

            case R.id.layoutFilterManage :
                intent = new Intent(v.getContext(), MoreFilterManagePrismActivity.class);
                startActivityAni(MoreActivity.this, intent, false, 0);
                break;

            case R.id.layoutAppInfo:
                intent = new Intent(v.getContext(), AppInfoActivity.class);
                startActivityAni(MoreActivity.this, intent, false, 0);
                break;

            case R.id.layoutServiceCenter:
                intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.navienairone.com/m/#customerservicewrap")); //수정 jh.j
                startActivityAni(MoreActivity.this, intent, false, 1);
                break;

            case R.id.txtLogout:
                intent = new Intent(v.getContext(), PopupActivity.class);
                intent.putExtra(PopupActivity.POPUP_MODE, PopupActivity.MODE_YESNO);
                intent.putExtra(PopupActivity.POPUP_TITLE_TEXT, getString(R.string.delete_device));
                intent.putExtra(PopupActivity.POPUP_BODY_TEXT, getString(R.string.do_logout));
                intent.putExtra(PopupActivity.POPUP_CONFIRM_BUTTON_TEXT, getString(R.string.logout));
                startActivityForResult(intent, RESULT_LOGOUT_COMFIRM);
                overridePendingTransition(0, 0);
                break;

            case R.id.btnBack:
                finish();
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.e(TAG, "onActivityResult : " + requestCode + "/" + resultCode);
        switch (requestCode) {
            case RESULT_LOGOUT_COMFIRM:
                if (resultCode == RESULT_OK) {
                    /**
                     * Clear ID & Password
                     */
                    SharedPrefUtil.putBoolean(SharedPrefUtil.AUTO_LOGIN, false);
                    SharedPrefUtil.putString(SharedPrefUtil.USER_ID, "");
                    SharedPrefUtil.putString(SharedPrefUtil.USER_PASS, "");
                    SharedPrefUtil.putString(SharedPrefUtil.ROOM_CONTROLLER_ID, "");

                    CleanVentilationApplication.getInstance().clearHomeInfo();

                    Intent sendIntent = new Intent(WidgetUtils.WIDGET_REPLACE_ACTION);
                    sendBroadcast(sendIntent);

                    Intent intent = new Intent(this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivityAni(MoreActivity.this, intent, true, 2);
                }
                break;
        }
    }

    private void setLoadingTimer() {
        if(mLoadingTimer != null){
            mLoadingTimer.cancel();
        }
        mLoadingTimer = new Timer();
        mLoadingTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                if(IsRunningProgress())
                    DismissConnectDialog();
            }
        },LOADING_TIME);
    }

    private void cancelLoadingTimer() {
        if(mLoadingTimer != null){
            mLoadingTimer.cancel();
        }
    }
}
