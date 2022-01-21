package kr.co.kdone.airone.activity.more;

import android.app.Activity;
import android.content.Intent;
import android.os.Handler;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import java.io.File;

import kr.co.kdone.airone.BuildConfig;
import kr.co.kdone.airone.activity.PopupActivity;
import kr.co.kdone.airone.R;
import kr.co.kdone.airone.activity.SplashActivity;
import kr.co.kdone.airone.activity.login.LoginActivity;
import kr.co.kdone.airone.utils.CommonUtils;
import kr.co.kdone.airone.utils.SharedPrefUtil;

import static kr.co.kdone.airone.utils.CommonUtils.RESULT_INIT_APP_CONFIRM;
import static kr.co.kdone.airone.utils.CommonUtils.RESULT_INIT_APP_SUCCESS;
import static kr.co.kdone.airone.utils.CommonUtils.isUserInfoSection;
import static kr.co.kdone.airone.utils.CommonUtils.startActivityAni;

/**
 * ikHwang 2019-06-04 오전 8:38 앱 버전정보 확인 화면
 */
public class AppInfoActivity extends AppCompatActivity implements View.OnClickListener {
    private final String TAG = getClass().getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_info);
        isUserInfoSection(this);

        TextView txtVersion = (TextView)findViewById(R.id.txtVersion);
        txtVersion.setText(getString(R.string.current_version) + " : " + BuildConfig.VERSION_NAME);
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case RESULT_INIT_APP_CONFIRM:
                if (resultCode == Activity.RESULT_OK) {
                    SharedPrefUtil.reset();
                    File cacheDirectory = getCacheDir();
                    File applicationDirectory = new File(cacheDirectory.getParent());
                    if (applicationDirectory.exists()) {
                        String[] fileNames = applicationDirectory.list();
                        for (String fileName : fileNames) {
                            if (!fileName.equals("lib")) {
                                deleteFile(new File(applicationDirectory, fileName));
                            }
                        }
                    }

                    CommonUtils.showToast(AppInfoActivity.this, getString(R.string.init_app_popup_success));

                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            SharedPrefUtil.putBoolean(SharedPrefUtil.AUTO_LOGIN, true);
                            Intent intent = new Intent(AppInfoActivity.this, LoginActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            finish();
                            overridePendingTransition(R.anim.slide_right_in, R.anim.slide_none);
                        }
                    }, 500);
                }
                break;

            case RESULT_INIT_APP_SUCCESS: {
                Intent intent = new Intent(this, SplashActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivityAni(AppInfoActivity.this, intent, true, 2);
                }
                break;
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
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

            case R.id.layoutInitApp:
                Intent intent = new Intent(this, PopupActivity.class);
                intent.putExtra(PopupActivity.POPUP_MODE, PopupActivity.MODE_YESNO);
                intent.putExtra(PopupActivity.POPUP_BODY_TEXT, getString(R.string.init_app_popup_confirm));
                intent.putExtra(PopupActivity.POPUP_CONFIRM_BUTTON_TEXT, getString(R.string.init));
                intent.putExtra(PopupActivity.POPUP_CANCEL_BUTTON_TEXT, getString(R.string.cancel));
                startActivityForResult(intent, RESULT_INIT_APP_CONFIRM);
                overridePendingTransition(0,0);
                break;

            default:
                break;
        }
    }

    boolean deleteFile(File file) {
        boolean deletedAll = true;
        if (file != null) {
            if (file.isDirectory()) {
                String[] children = file.list();
                for (int i = 0; i < children.length; i++) {
                    deletedAll = deleteFile(new File(file, children[i])) && deletedAll;
                }
            } else {
                deletedAll = file.delete();
            }
        }
        return deletedAll;
    }
}
