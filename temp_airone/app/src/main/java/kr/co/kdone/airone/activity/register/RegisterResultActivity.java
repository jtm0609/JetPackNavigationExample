package kr.co.kdone.airone.activity.register;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import androidx.appcompat.app.AppCompatActivity;
import kr.co.kdone.airone.CleanVentilationApplication;
import kr.co.kdone.airone.R;
import kr.co.kdone.airone.activity.SplashActivity;
import kr.co.kdone.airone.activity.login.LoginActivity;
import kr.co.kdone.airone.activity.main.MainActivity;
import kr.co.kdone.airone.activity.main.MainPrismActivity;
import kr.co.kdone.airone.activity.more.SmartGuideActivity;
import kr.co.kdone.airone.utils.CommonUtils;
import kr.co.kdone.airone.utils.HomeInfoDataParser;
import kr.co.kdone.airone.utils.HttpApi;
import kr.co.kdone.airone.utils.SharedPrefUtil;
import kr.co.kdone.airone.widget.WidgetUtils;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

import static kr.co.kdone.airone.utils.CommonUtils.DismissConnectDialog;
import static kr.co.kdone.airone.utils.CommonUtils.MODE_FIND_ID;
import static kr.co.kdone.airone.utils.CommonUtils.MODE_FIND_PASS;
import static kr.co.kdone.airone.utils.CommonUtils.MODE_NONE;
import static kr.co.kdone.airone.utils.CommonUtils.MODE_SUCCESS_REG_ID;
import static kr.co.kdone.airone.utils.CommonUtils.displayProgress;
import static kr.co.kdone.airone.utils.CommonUtils.startActivityAni;
import static kr.co.kdone.airone.utils.SharedPrefUtil.DEFAULT_CURRENT_CONTROL_DEVICE_ID;
import static kr.co.kdone.airone.utils.SharedPrefUtil.DEFAULT_CURRENT_CONTROL_DEVICE_NAME;
import static kr.co.kdone.airone.utils.SharedPrefUtil.DEFAULT_HOMEDATA_JSON;
import static kr.co.kdone.airone.utils.SharedPrefUtil.HOMEDATA_JSON;
import static kr.co.kdone.airone.utils.SharedPrefUtil.HOMEDATA_UPDATA_TIME;

/**
 * ikHwang 2019-06-04 오전 9:54 회원가입 완료 화면
 */
public class RegisterResultActivity extends AppCompatActivity implements View.OnClickListener {
    private final String TAG = getClass().getSimpleName();

    private Activity act;
    private ImageView imgRegisterResult;
    private TextView txtRegisterResult;
    private TextView txtOK;

    private String userID;
    private String password;
    private int mode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_result);
        act = this;

        userID = getIntent().getStringExtra("userID");
        password = getIntent().getStringExtra("password");
        mode = getIntent().getIntExtra("mode", MODE_NONE);

        imgRegisterResult = findViewById(R.id.img_register_result);
        txtRegisterResult = findViewById(R.id.txt_register_result);
        txtOK = findViewById(R.id.txtOK);

        changeUIMode();
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.layoutOK:
                if (mode == MODE_SUCCESS_REG_ID && !userID.equals("") && !password.equals("")) {
                    login(userID, password);
                } else {
                    Intent intent = new Intent(act, LoginActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    CommonUtils.startActivityAni(act, intent, true, 2);
                }
                break;
        }
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(act, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivityAni(RegisterResultActivity.this, intent, true, 2);
    }

    private void changeUIMode() {
        switch (mode) {
            case MODE_SUCCESS_REG_ID: {
                imgRegisterResult.setImageResource(R.drawable.icon_find_complete);
                String textRegisterComplete = getString(R.string.register_result_complete1);
                SpannableStringBuilder ssb = new SpannableStringBuilder(textRegisterComplete);
                ssb.setSpan(new ForegroundColorSpan(Color.parseColor("#3690b8")), 0, 6, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                txtRegisterResult.setText(ssb);
                txtOK.setText(R.string.confirm);
                break;
            }
            case MODE_FIND_ID: {
                imgRegisterResult.setImageResource(R.drawable.icon_find_user);
                if (userID != null && userID.length() != 0) {
                    String textRegisterComplete = getString(R.string.register_result_complete2, userID);
                    SpannableStringBuilder ssb = new SpannableStringBuilder(textRegisterComplete);
                    ssb.setSpan(new ForegroundColorSpan(Color.parseColor("#3690b8")), textRegisterComplete.indexOf(userID), textRegisterComplete.indexOf(userID) + userID.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    txtRegisterResult.setText(ssb);
                } else {
                    txtRegisterResult.setText(getString(R.string.activity_register_user_text5));
                }
                txtOK.setText(R.string.login);
                break;
            }
            case MODE_FIND_PASS: {
                imgRegisterResult.setImageResource(R.drawable.icon_find_complete);
                txtRegisterResult.setText(R.string.register_result_complete3);
                txtOK.setText(R.string.login);
                break;
            }
        }
    }

    private void login(final String id, final String pw) {
        displayProgress(this, "", "");
        try {
            final String pushToken = SharedPrefUtil.getString(SharedPrefUtil.FCMKEY, "");

            HttpApi.PostV2UserLogin(id, pw, getPackageManager().getPackageInfo(getPackageName(), 0).versionName, pushToken, null, new Callback() { // V2A 적용
                @Override
                public void onFailure(Call call, IOException e) {
                    DismissConnectDialog();
                    CommonUtils.showToast(RegisterResultActivity.this, getString(R.string.toast_result_error_login));
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    try {
                        String strResponse =  response.body().string();

                        if(TextUtils.isEmpty(strResponse)){
                            CommonUtils.showToast(RegisterResultActivity.this, getString(R.string.toast_result_error_login));
                        }else{
                            CommonUtils.customLog(TAG, "Login : " + strResponse, Log.ERROR);

                            JSONObject rawJson = new JSONObject(strResponse);

                            switch (rawJson.optInt("code", 0)){
                                case HttpApi.RESPONSE_SUCCESS:
                                    if (!SharedPrefUtil.getString(SharedPrefUtil.USER_ID, "").equals(id)) {
                                        SharedPrefUtil.putString(SharedPrefUtil.CURRENT_CONTROL_DEVICE_ID, DEFAULT_CURRENT_CONTROL_DEVICE_ID);
                                        SharedPrefUtil.putString(SharedPrefUtil.CURRENT_CONTROL_DEVICE_NAME, DEFAULT_CURRENT_CONTROL_DEVICE_NAME);
                                        SharedPrefUtil.putString(HOMEDATA_JSON, DEFAULT_HOMEDATA_JSON);
                                        SharedPrefUtil.putLong(HOMEDATA_UPDATA_TIME, 0);
                                    }

                                    SharedPrefUtil.putString(SharedPrefUtil.USER_ID, id);
                                    SharedPrefUtil.putString(SharedPrefUtil.USER_PASS, pw);
                                    SharedPrefUtil.putBoolean(SharedPrefUtil.IS_LOGIN, true);

                                    // 자동로그인 상태 저장
                                    SharedPrefUtil.putBoolean(SharedPrefUtil.AUTO_LOGIN, true);

                                    CleanVentilationApplication.getInstance().getUserInfo().setId(id);
                                    CleanVentilationApplication.getInstance().getUserInfo().setPassword(pw);

                                    if(rawJson.has("data")){
                                        // ikHwang 2019-05-21 오후 1:32 메인화면 이동시 화면을 구성하기 위해 데이터 파싱
                                        HomeInfoDataParser.paserHomeInfo(CleanVentilationApplication.getInstance(), rawJson.getJSONObject("data"), true);

                                        Intent sendIntent = new Intent(WidgetUtils.WIDGET_UPDATE_ACTION);
                                        sendBroadcast(sendIntent, getString(R.string.br_permission));
                                    }

                                    // 스마트 가이드 초기 상태에 따라 화면 이동 처리
                                    Intent intent;
                                    if (SharedPrefUtil.getBoolean(SharedPrefUtil.DONT_SHOW_SMARTGUIDE, false)) {
                                        if(CleanVentilationApplication.getInstance().isIsOldUser()){
                                            intent = new Intent(RegisterResultActivity.this, MainActivity.class);
                                        }else{
                                            intent = new Intent(RegisterResultActivity.this, MainPrismActivity.class);
                                        }

                                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                    } else {
                                        intent = new Intent(RegisterResultActivity.this, SmartGuideActivity.class);
                                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                        intent.putExtra("MODE", SmartGuideActivity.MODE_NEW);
                                    }
                                    DismissConnectDialog();
                                    CommonUtils.startActivityAni(act, intent, true, 0);
                                    break;

                                case HttpApi.RESPONSE_SERVER_MAINTENANCE:
                                    if(rawJson.has("data")){
                                        JSONObject obj = rawJson.getJSONObject("data");

                                        StringBuffer sb = new StringBuffer();

                                        if(obj.has("title") && obj.has("msg") && obj.has("period")){
                                            sb.append(obj.optString("title", ""));
                                            sb.append("\n");
                                            sb.append(obj.optString("msg", ""));
                                            sb.append("\n");
                                            sb.append(obj.optString("period", ""));
                                        }

                                        CommonUtils.showToast(RegisterResultActivity.this, sb.toString());
                                    }

                                    startLoginActivity();
                                    break;

                                case HttpApi.RESPONSE_WRONG_USER_INFO:
                                    CommonUtils.showToast(RegisterResultActivity.this, getString(R.string.toast_result_wrong_id_password));
                                    startLoginActivity();
                                    break;

                                default:
                                    CommonUtils.showToast(RegisterResultActivity.this, getString(R.string.toast_result_error_login));
                                    startLoginActivity();
                                    break;
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        CommonUtils.showToast(RegisterResultActivity.this, getString(R.string.toast_result_error_login));
                        startLoginActivity();
                    } finally {
                        DismissConnectDialog();
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            DismissConnectDialog();
        }
    }

    private void startLoginActivity(){
        Intent intent = getIntent();
        intent.setClass(act, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivityAni(act, intent, true, 0);
    }
}
