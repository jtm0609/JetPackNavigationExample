package kr.co.kdone.airone.activity;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.messaging.FirebaseMessaging;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import kr.co.kdone.airone.BuildConfig;
import kr.co.kdone.airone.CleanVentilationApplication;
import kr.co.kdone.airone.R;
import kr.co.kdone.airone.activity.login.LoginActivity;
import kr.co.kdone.airone.activity.main.MainActivity;
import kr.co.kdone.airone.activity.main.MainPrismActivity;
import kr.co.kdone.airone.activity.more.MoreNotificationActivity;
import kr.co.kdone.airone.activity.more.SmartGuideActivity;
import kr.co.kdone.airone.activity.register.ResetAgreementActivity;
import kr.co.kdone.airone.utils.CommonUtils;
import kr.co.kdone.airone.utils.HomeInfoDataParser;
import kr.co.kdone.airone.utils.HttpApi;
import kr.co.kdone.airone.utils.SharedPrefUtil;
import kr.co.kdone.airone.widget.WidgetUtils;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

import static kr.co.kdone.airone.utils.CommonUtils.DismissConnectDialog;
import static kr.co.kdone.airone.utils.CommonUtils.RESULT_AGREEMENT;
import static kr.co.kdone.airone.utils.CommonUtils.RESULT_SERVER_MAINTENANCE;
import static kr.co.kdone.airone.utils.CommonUtils.displayProgress;
import static kr.co.kdone.airone.utils.CommonUtils.startActivityAni;

/**
 * ikHwang 2019-06-04 오전 9:55 앱 진입 화면
 */
public class SplashActivity extends AppCompatActivity {
    private final String TAG = getClass().getSimpleName();

    private Context mAppContext = null;
    private Handler mHandler = new Handler();
    private static final int SPLASH_DELAY_MILLIS = 1500;

    private LinearLayout app_update_view;
    private TextView app_update_confirm;
    private TextView app_update_next;

    private int             terms = 0;    // 이용약관 재동의 상태
    private int             privacyPolicy = 0;  // 개인정보 수집 및 이용동의 재동의 상태
    private int             over14age = 0;    // 만14세 이상 재동의 상태

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        mAppContext = getApplicationContext();

        try {
            Uri schemeData = getIntent().getData();

            if(null != schemeData){
                CommonUtils.customLog(TAG, "uri : " + schemeData.toString(), Log.ERROR);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        FirebaseAnalytics.getInstance(this);

        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(new OnCompleteListener<String>() {
                    @Override
                    public void onComplete(@NonNull Task<String> task) {
                        if (!task.isSuccessful()) {
                            CommonUtils.customLog(TAG, "getInstanceId failed", Log.ERROR);
                            return;
                        }

                        // Get new Instance ID token
                        String token = task.getResult();
                        CommonUtils.customLog(TAG, "Refreshed token : " + token, Log.ERROR);
                        SharedPrefUtil.putString(SharedPrefUtil.FCMKEY, token);
                    }
                });

        getKeyHash(this);

        app_update_view = findViewById(R.id.app_update_view);
        app_update_confirm = findViewById(R.id.app_update_confirm);
        app_update_next = findViewById(R.id.app_update_next); //다음에 하기 버튼

        NotificationManager notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancelAll();

        initListener();
    }

    private void initListener(){
        app_update_next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                mHandler.postDelayed(mStartMainUIActivityRunnable, SPLASH_DELAY_MILLIS);
                mHandler.post(mStartMainUIActivityRunnable);
            }
        });
        app_update_confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                appUpdate();
            }
        });
    }

    private void appUpdate(){
        final String appPackageName = getPackageName(); // getPackageName() from Context or Activity object
        try {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
        } catch (android.content.ActivityNotFoundException anfe) {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        checkAppVersion();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case RESULT_SERVER_MAINTENANCE:
                finish();
                break;

            case RESULT_AGREEMENT: // 이용약관 재동의 화면
                Intent intent = new Intent(SplashActivity.this, ResetAgreementActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra("terms", terms);
                intent.putExtra("privacyPolicy", privacyPolicy);
                intent.putExtra("over14age", over14age);
                CommonUtils.startActivityAni(SplashActivity.this, intent, true, 0);
                break;
        }
    }

    private void checkAppVersion(){
        try {
            HttpApi.PostCheckAppVersion(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    runOnUiThread(new Runnable() {
                        public void run() {
                            CommonUtils.DismissConnectDialog();
                            CommonUtils.showToast(SplashActivity.this, getString(R.string.toast_result_can_not_request));
                            new Handler().postDelayed(new Runnable() {
                                public void run() {
                                    SplashActivity.this.finish();
                                }
                            }, 1000);
                        }
                    });
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    try {
                        String strResponse =  response.body().string();

                        CommonUtils.customLog(TAG, "app/version : " + strResponse, Log.ERROR);

                        JSONObject json_data = new JSONObject(strResponse);

                        switch (json_data.optInt("code", 0)){
                                                        case HttpApi.RESPONSE_SUCCESS: { // 버전체크 성공
                                                            if(json_data.has("data")){
                                                                JSONObject objData = json_data.getJSONObject("data");

                                                                if(objData.has("currentVer") && objData.has("currentVer")){
                                                                    String currentVer = objData.getString("currentVer");
                                                                    final String forceUpdate = objData.getString("forceUpdate");

                                                                    String tempLocal = BuildConfig.VERSION_NAME.replace(".","");
                                                                    String tempServer = currentVer.replace(".","");

                                                                    int localVersion = 0;
                                                                    int serverVersion = 0;

                                                                    try {
                                                                        localVersion = Integer.parseInt(tempLocal);
                                                                        serverVersion = Integer.parseInt(tempServer);
                                                                    } catch (NumberFormatException e) {
                                                                        CommonUtils.sendFirebaseLog(mAppContext, "versionCheck local : " + tempLocal + ", server : " + tempServer + ", exception : " + e.getLocalizedMessage());
                                                                        mHandler.postDelayed(mStartMainUIActivityRunnable, SPLASH_DELAY_MILLIS);
                                                                        return;
                                                                    }

                                                                    if(localVersion < serverVersion){
                                                                        runOnUiThread(new Runnable() {
                                                                            @Override
                                                                            public void run() {
                                                                                app_update_view.setVisibility(View.VISIBLE);
                                                    if(forceUpdate.equals("1")) app_update_next.setVisibility(View.GONE);
                                                }
                                            });

                                            return;
                                        }
                                    }
                                }

                                mHandler.postDelayed(mStartMainUIActivityRunnable, SPLASH_DELAY_MILLIS);
                            }
                                break;

                            case HttpApi.RESPONSE_SERVER_MAINTENANCE: { // 서버 점검중
                                JSONArray jsonArray = new JSONArray(json_data.optString("data", ""));

                                StringBuffer sb = new StringBuffer();
                                String  title = "";

                                for(int i=0; i<jsonArray.length(); i++){
                                    JSONObject obj = jsonArray.getJSONObject(i);

                                    if(obj.has("title")){
                                        title = obj.optString("title", "");
                                    }

                                    if(obj.has("title") && obj.has("msg") && obj.has("period")){
                                        sb.append(obj.optString("msg", ""));
                                        sb.append("\n");
                                        sb.append(obj.optString("period", ""));
                                    }
                                }

                                Intent intent = new Intent(SplashActivity.this, PopupActivity.class);
                                intent.putExtra(PopupActivity.POPUP_MODE, PopupActivity.MODE_NORMAL);
                                intent.putExtra(PopupActivity.POPUP_TITLE_TEXT, title);
                                intent.putExtra(PopupActivity.POPUP_BODY_TEXT, sb.toString());
                                intent.putExtra(PopupActivity.POPUP_CONFIRM_BUTTON_TEXT, getString(R.string.ok));
                                startActivityForResult(intent, RESULT_SERVER_MAINTENANCE);
                                overridePendingTransition(0, 0);
                            }
                                break;

                            default:
                                runOnUiThread(new Runnable() {
                                    public void run() {
                                        CommonUtils.DismissConnectDialog();
                                        CommonUtils.showToast(SplashActivity.this, getString(R.string.toast_result_can_not_request));
                                        new Handler().postDelayed(new Runnable() {
                                            public void run() {
                                                SplashActivity.this.finish();
                                            }
                                        }, 1000);
                                    }
                                });
                                break;
                        }
                    } catch (JSONException e) {
                        runOnUiThread(new Runnable() {
                            public void run() {
                                CommonUtils.DismissConnectDialog();
                                CommonUtils.showToast(SplashActivity.this, getString(R.string.toast_result_can_not_request));

                                new Handler().postDelayed(new Runnable() {
                                    public void run() {
                                        SplashActivity.this.finish();
                                    }
                                }, 1000);
                            }
                        });
                    }
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
            runOnUiThread(new Runnable() {
                public void run() {
                    CommonUtils.DismissConnectDialog();
                    CommonUtils.showToast(SplashActivity.this, getString(R.string.toast_result_can_not_request));

                    new Handler().postDelayed(new Runnable() {
                        public void run() {
                            SplashActivity.this.finish();
                        }
                    }, 1000);
                }
            });
        }
    }

    /**
     * 도움말 화면 표시 설정 ON일 경우 도움말 화면으로, OFF시 우리집 번호 입력 화면으로 이동.
     */
    private Runnable mStartMainUIActivityRunnable = new Runnable() {
        @Override
        public void run() {
            //자동 로그인 처리
            if(SharedPrefUtil.getBoolean(SharedPrefUtil.AUTO_LOGIN, false)) {
                final String id = SharedPrefUtil.getString(SharedPrefUtil.USER_ID, "");
                final String pw = SharedPrefUtil.getString(SharedPrefUtil.USER_PASS, "");
                final String pushToken = SharedPrefUtil.getString(SharedPrefUtil.FCMKEY, "");

                if(!TextUtils.isEmpty(id) && !TextUtils.isEmpty(pw)){
                    displayProgress(SplashActivity.this, "", "");
                    try {
                        HttpApi.PostV2UserLogin(id, pw, getPackageManager().getPackageInfo(getPackageName(), 0).versionName, pushToken, null, new Callback() { //V2A 작업.
                            @Override
                            public void onFailure(Call call, IOException e) {
                                DismissConnectDialog();
                                CommonUtils.showToast(SplashActivity.this, getString(R.string.toast_result_error_login));
                                startLoginActivity();
                            }

                            @Override
                            public void onResponse(Call call, Response response) throws IOException {
                                try {
                                    String strResponse =  response.body().string();

                                    if(TextUtils.isEmpty(strResponse)){
                                        CommonUtils.showToast(SplashActivity.this, getString(R.string.toast_result_error_login));
                                        startLoginActivity();
                                    }else{
                                        CommonUtils.customLog(TAG, "user/sign-in : " + strResponse, Log.ERROR);

                                        JSONObject json_data = new JSONObject(strResponse);

                                        switch (json_data.optInt("code", 0)) {
                                            case HttpApi.RESPONSE_SUCCESS: { // 로그인 성공
                                                CleanVentilationApplication.getInstance().getUserInfo().setId(id);
                                                CleanVentilationApplication.getInstance().getUserInfo().setPassword(pw);

                                                if(json_data.has("data")){
                                                    // ikHwang 2019-05-21 오후 1:32 메인화면 이동시 화면을 구성하기 위해 데이터 파싱
                                                    HomeInfoDataParser.paserHomeInfo(CleanVentilationApplication.getInstance(), json_data.getJSONObject("data"), true);

                                                    Intent sendIntent = new Intent(WidgetUtils.WIDGET_UPDATE_ACTION);
                                                    sendBroadcast(sendIntent, getString(R.string.br_permission));

                                                    DismissConnectDialog();
                                                }

                                                if(json_data.has("data") && json_data.getJSONObject("data").has("agreement")){
                                                    JSONObject ageement = json_data.getJSONObject("data").getJSONObject("agreement");

                                                    if(ageement.has("terms")) terms = ageement.getInt("terms");
                                                    if(ageement.has("privacyPolicy")) privacyPolicy = ageement.getInt("privacyPolicy");
                                                    if(ageement.has("over14age")) over14age = ageement.getInt("over14age");

                                                    if(terms == 0 || privacyPolicy == 0 || over14age ==0){
                                                        Intent intent = new Intent(SplashActivity.this, PopupActivity.class);
                                                        intent.putExtra(PopupActivity.POPUP_MODE, PopupActivity.MODE_NORMAL);
                                                        intent.putExtra(PopupActivity.POPUP_TITLE_TEXT, getString(R.string.login_activity_msg_3));
                                                        intent.putExtra(PopupActivity.POPUP_BODY_TEXT, getString(R.string.login_activity_msg_6));
                                                        intent.putExtra(PopupActivity.POPUP_CONFIRM_BUTTON_TEXT, getString(R.string.ok));
                                                        startActivityForResult(intent, RESULT_AGREEMENT);
                                                        overridePendingTransition(0, 0);
                                                        return;
                                                    }
                                                }

                                                Intent intent = new Intent();
                                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);

                                                if(getIntent().hasExtra("Args")){
                                                    String args = getIntent().getStringExtra("Args");

                                                    JSONObject jsonObject = new JSONObject(args);

                                                    if(jsonObject.has("Type")){
                                                        switch (jsonObject.getInt("Type")){
                                                            case 1: // 공지사항
                                                                intent.setClass(SplashActivity.this, MoreNotificationActivity.class);
                                                                break;

                                                            case 2: // 스마트 알림
                                                                intent.setClass(SplashActivity.this, SmartAlarmActivity.class);
                                                                break;

                                                            case 3: // 필터 알림
                                                                intent.setClass(SplashActivity.this, SmartAlarmActivity.class);
                                                                break;

                                                            default:
                                                                // 스마트 가이드 초기 상태에 따라 화면 이동 처리
                                                                if (SharedPrefUtil.getBoolean(SharedPrefUtil.DONT_SHOW_SMARTGUIDE, false)) {
                                                                    if(CleanVentilationApplication.getInstance().isIsOldUser()){
                                                                        intent = new Intent(SplashActivity.this, MainActivity.class);
                                                                    }else{
                                                                        intent = new Intent(SplashActivity.this, MainPrismActivity.class);
                                                                    }
                                                                } else {
                                                                    intent.setClass(SplashActivity.this, SmartGuideActivity.class);
                                                                    intent.putExtra("MODE", SmartGuideActivity.MODE_NEW);
                                                                }
                                                                break;
                                                        }
                                                    }else{
                                                        // 스마트 가이드 초기 상태에 따라 화면 이동 처리
                                                        if (SharedPrefUtil.getBoolean(SharedPrefUtil.DONT_SHOW_SMARTGUIDE, false)) {
                                                            if(CleanVentilationApplication.getInstance().isIsOldUser()){
                                                                intent = new Intent(SplashActivity.this, MainActivity.class);
                                                            }else{
                                                                intent = new Intent(SplashActivity.this, MainPrismActivity.class);
                                                            }

                                                        } else {
                                                            intent.setClass(SplashActivity.this, SmartGuideActivity.class);
                                                            intent.putExtra("MODE", SmartGuideActivity.MODE_NEW);
                                                        }
                                                    }
                                                }else{
                                                    // 스마트 가이드 초기 상태에 따라 화면 이동 처리
                                                    if (SharedPrefUtil.getBoolean(SharedPrefUtil.DONT_SHOW_SMARTGUIDE, false)) {
                                                        if(CleanVentilationApplication.getInstance().isIsOldUser()){
                                                            intent = new Intent(SplashActivity.this, MainActivity.class);
                                                        }else{
                                                            intent = new Intent(SplashActivity.this, MainPrismActivity.class);
                                                        }
                                                    } else {
                                                        intent.setClass(SplashActivity.this, SmartGuideActivity.class);
                                                        intent.putExtra("MODE", SmartGuideActivity.MODE_NEW);
                                                    }
                                                }

                                                startActivityAni(SplashActivity.this, intent, true, 0);
                                            }
                                                break;

                                            case HttpApi.RESPONSE_SERVER_MAINTENANCE: { // 서버 점검중
                                                if(json_data.has("data")){
                                                    JSONObject obj = json_data.getJSONObject("data");

                                                    StringBuffer sb = new StringBuffer();

                                                    if(obj.has("title") && obj.has("msg") && obj.has("period")){
                                                        sb.append(obj.optString("title", ""));
                                                        sb.append("\n");
                                                        sb.append(obj.optString("msg", ""));
                                                        sb.append("\n");
                                                        sb.append(obj.optString("period", ""));
                                                    }

                                                    CommonUtils.showToast(SplashActivity.this, sb.toString());
                                                    finish();
                                                }else{
                                                    startLoginActivity();
                                                }
                                            }
                                                break;

                                            case HttpApi.RESPONSE_WRONG_USER_INFO: // 미등록 사용자
                                                CommonUtils.showToast(SplashActivity.this, getString(R.string.toast_result_wrong_id_password));
                                                startLoginActivity();
                                                break;

                                            default:
                                                CommonUtils.showToast(SplashActivity.this, getString(R.string.toast_result_error_login));
                                                startLoginActivity();
                                                break;
                                        }
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                    CommonUtils.showToast(SplashActivity.this, getString(R.string.toast_result_error_login));
                                    startLoginActivity();
                                } finally {
                                    DismissConnectDialog();
                                }
                            }
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                        CommonUtils.showToast(SplashActivity.this, getString(R.string.toast_result_error_login));
                        startLoginActivity();
                        DismissConnectDialog();
                    }
                }else{
                    startLoginActivity();
                }
            }else{
                startLoginActivity();
            }
        }
    };

    private void startLoginActivity(){
        Intent intent = getIntent();
        intent.setClass(mAppContext, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivityAni(SplashActivity.this, intent, true, 0);
    }

    public static final String getKeyHash(Context context) {
        try {
            PackageInfo info = context.getPackageManager().getPackageInfo(context.getPackageName(), PackageManager.GET_SIGNATURES);

            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                String keyHash = Base64.encodeToString(md.digest(), Base64.DEFAULT);
                Log.e("KeyHash:%s", keyHash);
                return keyHash;
            }
        } catch (PackageManager.NameNotFoundException e) {
            Log.e("getKeyHash Error:%s", e.getMessage());
        } catch (NoSuchAlgorithmException e) {
            Log.e("getKeyHash Error:%s", e.getMessage());
        }

        return "";
    }
}
