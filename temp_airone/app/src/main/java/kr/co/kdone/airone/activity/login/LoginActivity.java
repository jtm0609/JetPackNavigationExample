package kr.co.kdone.airone.activity.login;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.safetynet.SafetyNet;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import kr.co.kdone.airone.CleanVentilationApplication;
import kr.co.kdone.airone.R;
import kr.co.kdone.airone.activity.PopupActivity;
import kr.co.kdone.airone.activity.login.FindIdPwActivity;
import kr.co.kdone.airone.activity.main.MainActivity;
import kr.co.kdone.airone.activity.main.MainPrismActivity;
import kr.co.kdone.airone.activity.more.SmartGuideActivity;
import kr.co.kdone.airone.activity.register.RegisterInfoActivity;
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
import static kr.co.kdone.airone.utils.CommonUtils.MODE_DEVICE_REG;
import static kr.co.kdone.airone.utils.CommonUtils.MODE_DEVICE_ROOM_CON;
import static kr.co.kdone.airone.utils.CommonUtils.MODE_FIND_ID;
import static kr.co.kdone.airone.utils.CommonUtils.MODE_FIND_PASS;
import static kr.co.kdone.airone.utils.CommonUtils.RESULT_AGREEMENT;
import static kr.co.kdone.airone.utils.CommonUtils.RESULT_DEVICE_NOT_EXIST;
import static kr.co.kdone.airone.utils.CommonUtils.RESULT_LOGIN_FAIL;
import static kr.co.kdone.airone.utils.CommonUtils.RESULT_LOGIN_RECAPCHA;
import static kr.co.kdone.airone.utils.CommonUtils.RESULT_SERVER_MAINTENANCE;
import static kr.co.kdone.airone.utils.CommonUtils.displayProgress;
import static kr.co.kdone.airone.utils.CommonUtils.setEditText;
import static kr.co.kdone.airone.utils.SharedPrefUtil.DEFAULT_CURRENT_CONTROL_DEVICE_ID;
import static kr.co.kdone.airone.utils.SharedPrefUtil.DEFAULT_CURRENT_CONTROL_DEVICE_NAME;
import static kr.co.kdone.airone.utils.SharedPrefUtil.DEFAULT_HOMEDATA_JSON;
import static kr.co.kdone.airone.utils.SharedPrefUtil.HOMEDATA_JSON;
import static kr.co.kdone.airone.utils.SharedPrefUtil.HOMEDATA_UPDATA_TIME;

/**
 * // ikHwang 2019-06-04 ?????? 9:48 ????????? ??????
 */
public class LoginActivity extends AppCompatActivity implements View.OnClickListener {
    private final String TAG = getClass().getSimpleName();

    private EditText txtID;
    private EditText txtPass;
    private ImageView btnDeleteID;
    private ImageView btnDeletePass;
    private CheckBox chkAutoLogin;

    private String          reCaptchaToken;     // ?????? ?????? ?????? ??????

    private int terms = 0;           // ?????? ?????? ??????
    private int privacyPolicy = 0;  // ???????????? ?????? ??? ???????????? ??????
    private int over14age = 0;      // ???14??? ?????? ?????? ??????

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        CleanVentilationApplication.getInstance().awsMqttDisConnect();

        CommonUtils.setupUI(this, (ConstraintLayout) findViewById(R.id.layoutMain));

        String email = getIntent().getStringExtra("EMAIL");

        txtID = findViewById(R.id.txtID);
        txtPass = findViewById(R.id.txtPass);
        btnDeleteID = findViewById(R.id.btnDeleteID);
        btnDeletePass = findViewById(R.id.btnDeletePass);
        chkAutoLogin = findViewById(R.id.chkAutoLogin);

        txtID.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                btnDeleteID.setVisibility(s.length() == 0 ? View.GONE : View.VISIBLE);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        txtID.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    btnDeleteID.setVisibility(txtID.getText().length() == 0 ? View.GONE : View.VISIBLE);
                } else {
                    btnDeleteID.setVisibility(hasFocus ? View.VISIBLE : View.GONE);
                }
            }
        });

        txtPass.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                btnDeletePass.setVisibility(s.length() == 0 ? View.GONE : View.VISIBLE);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        txtPass.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    btnDeletePass.setVisibility(txtPass.getText().length() == 0 ? View.GONE : View.VISIBLE);
                } else {
                    btnDeletePass.setVisibility(hasFocus ? View.VISIBLE : View.GONE);
                }
            }
        });

        if (email != null && email.length() > 0) {
            txtID.setText(email);
        }

        if (SharedPrefUtil.getBoolean(SharedPrefUtil.AUTO_LOGIN, false)) {
            chkAutoLogin.setChecked(true);
            chkAutoLogin.setTextColor(Color.parseColor("#3690B8"));

            String id = SharedPrefUtil.getString(SharedPrefUtil.USER_ID, "");
            String pw = SharedPrefUtil.getString(SharedPrefUtil.USER_PASS, "");

            txtID.setText(id);
            txtPass.setText(pw);
        }

        chkAutoLogin.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    chkAutoLogin.setTextColor(Color.parseColor("#3690B8"));
                } else {
                    chkAutoLogin.setTextColor(Color.parseColor("#acb0b7"));
                }
            }
        });

//        setEditText(findViewById(R.id.layoutMain));
        txtID.setFilters(new InputFilter[]{CommonUtils.IDCharacterFilter});
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case RESULT_SERVER_MAINTENANCE:
                finish();
                break;

            case RESULT_DEVICE_NOT_EXIST: // ????????? ?????? ??????????????? ??????
                 if(Activity.RESULT_OK == resultCode){

                 }else{
                     finish();
                 }
                break;

            case RESULT_LOGIN_RECAPCHA:
                reCaptcha();
                break;
                
            case RESULT_AGREEMENT: // ???????????? ????????? ??????
                Intent intent = new Intent(LoginActivity.this, ResetAgreementActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra("terms", terms);
                intent.putExtra("privacyPolicy", privacyPolicy);
                intent.putExtra("over14age", over14age);
                CommonUtils.startActivityAni(LoginActivity.this, intent, true, 0);
                break;
        }
    }

    @Override
    public void onClick(View v) {
        Intent intent;
        switch (v.getId()) {
            case R.id.layoutLogin:
                if (txtID.length() == 0) {
                    CommonUtils.showToast(LoginActivity.this, getString(R.string.toast_result_input_id));
                }else if (txtPass.length() == 0) {
                    CommonUtils.showToast(LoginActivity.this, getString(R.string.toast_result_input_password));
                } else {
                    displayProgress(this, "", "");
                    final String pushToken = SharedPrefUtil.getString(SharedPrefUtil.FCMKEY, "");

                    try {
                        HttpApi.PostV2UserLogin(txtID.getText().toString(), txtPass.getText().toString(), getPackageManager().getPackageInfo(getPackageName(), 0).versionName, pushToken, reCaptchaToken, new Callback() { //V2A ??????.
                            @Override
                            public void onFailure(Call call, IOException e) {
                                DismissConnectDialog();
                                CommonUtils.showToast(LoginActivity.this, getString(R.string.toast_result_error_login));
                            }

                            @Override
                            public void onResponse(Call call, Response response) throws IOException {
                                try {
                                    String strResponse =  response.body().string();


                                    if(TextUtils.isEmpty(strResponse)){
                                        CommonUtils.showToast(LoginActivity.this, getString(R.string.toast_result_error_login));
                                    }else{
                                        CommonUtils.customLog(TAG, "user/sign-in : " + strResponse, Log.ERROR);

                                        JSONObject json_data = new JSONObject(strResponse);

                                        switch (json_data.optInt("code", 0)) {
                                            case HttpApi.RESPONSE_SUCCESS: { // ????????? ??????
                                                if (!SharedPrefUtil.getString(SharedPrefUtil.USER_ID, "").equals(txtID.getText().toString())) {
                                                    SharedPrefUtil.putString(SharedPrefUtil.CURRENT_CONTROL_DEVICE_ID, DEFAULT_CURRENT_CONTROL_DEVICE_ID);
                                                    SharedPrefUtil.putString(SharedPrefUtil.CURRENT_CONTROL_DEVICE_NAME, DEFAULT_CURRENT_CONTROL_DEVICE_NAME);
                                                    SharedPrefUtil.putString(HOMEDATA_JSON, DEFAULT_HOMEDATA_JSON);
                                                    SharedPrefUtil.putLong(HOMEDATA_UPDATA_TIME, 0);
                                                }

                                                // ??????????????? ?????? ??????
                                                if (chkAutoLogin.isChecked()) {
                                                    SharedPrefUtil.putBoolean(SharedPrefUtil.AUTO_LOGIN, true);
                                                }else{
                                                    SharedPrefUtil.putBoolean(SharedPrefUtil.AUTO_LOGIN, false);
                                                }

                                                SharedPrefUtil.putString(SharedPrefUtil.USER_ID, txtID.getText().toString());
                                                SharedPrefUtil.putString(SharedPrefUtil.USER_PASS, txtPass.getText().toString());
                                                SharedPrefUtil.putBoolean(SharedPrefUtil.IS_LOGIN, true);

                                                CleanVentilationApplication.getInstance().getUserInfo().setId(txtID.getText().toString());
                                                CleanVentilationApplication.getInstance().getUserInfo().setPassword(txtPass.getText().toString());

                                                if(json_data.has("data")){
                                                    // ikHwang 2019-05-21 ?????? 1:32 ???????????? ????????? ????????? ???????????? ?????? ????????? ??????
                                                    HomeInfoDataParser.paserHomeInfo(CleanVentilationApplication.getInstance(), json_data.getJSONObject("data"), true);

                                                    Intent sendIntent = new Intent(WidgetUtils.WIDGET_UPDATE_ACTION);
                                                    sendBroadcast(sendIntent, getString(R.string.br_permission));
                                                }

                                                if(json_data.has("data") && json_data.getJSONObject("data").has("agreement")){
                                                    JSONObject ageement = json_data.getJSONObject("data").getJSONObject("agreement");

                                                    if(ageement.has("terms")) terms = ageement.getInt("terms");
                                                    if(ageement.has("privacyPolicy")) privacyPolicy = ageement.getInt("privacyPolicy");
                                                    if(ageement.has("over14age")) over14age = ageement.getInt("over14age");

                                                    if(terms == 0 || privacyPolicy == 0 || over14age ==0){
                                                        Intent intent = new Intent(LoginActivity.this, PopupActivity.class);
                                                        intent.putExtra(PopupActivity.POPUP_MODE, PopupActivity.MODE_NORMAL);
                                                        intent.putExtra(PopupActivity.POPUP_TITLE_TEXT, getString(R.string.login_activity_msg_3));
                                                        intent.putExtra(PopupActivity.POPUP_BODY_TEXT, getString(R.string.login_activity_msg_6));
                                                        intent.putExtra(PopupActivity.POPUP_CONFIRM_BUTTON_TEXT, getString(R.string.ok));
                                                        startActivityForResult(intent, RESULT_AGREEMENT);
                                                        overridePendingTransition(0, 0);
                                                        return;
                                                    }
                                                }

                                                // ????????? ????????? ?????? ????????? ?????? ?????? ?????? ??????
                                                Intent intent;
                                                if (SharedPrefUtil.getBoolean(SharedPrefUtil.DONT_SHOW_SMARTGUIDE, false)) {
                                                    if(CleanVentilationApplication.getInstance().isIsOldUser()){
                                                        intent = new Intent(LoginActivity.this, MainActivity.class);
                                                    }else{
                                                        intent = new Intent(LoginActivity.this, MainPrismActivity.class);
                                                    }

                                                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                                } else {
                                                    intent = new Intent(LoginActivity.this, SmartGuideActivity.class);
                                                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                                    intent.putExtra("MODE", SmartGuideActivity.MODE_NEW);
                                                }

                                                DismissConnectDialog();

                                                CommonUtils.startActivityAni(LoginActivity.this, intent, true, 0);
                                            }
                                            break;

                                            case HttpApi.RESPONSE_SERVER_MAINTENANCE: { // ?????? ?????????
                                                if(json_data.has("data")) {
                                                    JSONObject obj = json_data.getJSONObject("data");

                                                    String  title = "";
                                                    StringBuffer sb = new StringBuffer();

                                                    if(obj.has("title")){
                                                        title = obj.optString("title", "");
                                                    }

                                                    if(obj.has("title") && obj.has("msg") && obj.has("period")){
                                                        sb.append(obj.optString("msg", ""));
                                                        sb.append("\n");
                                                        sb.append(obj.optString("period", ""));
                                                    }

                                                    Intent intent = new Intent(LoginActivity.this, PopupActivity.class);
                                                    intent.putExtra(PopupActivity.POPUP_MODE, PopupActivity.MODE_NORMAL);
                                                    intent.putExtra(PopupActivity.POPUP_TITLE_TEXT, title);
                                                    intent.putExtra(PopupActivity.POPUP_BODY_TEXT, sb.toString());
                                                    intent.putExtra(PopupActivity.POPUP_CONFIRM_BUTTON_TEXT, getString(R.string.ok));
                                                    startActivityForResult(intent, RESULT_SERVER_MAINTENANCE);
                                                    overridePendingTransition(0, 0);
                                                }
                                            }
                                                break;

                                            case HttpApi.RESPONSE_WRONG_USER_INFO: //  ????????? ???????????? ?????? ??????
                                                if(json_data.has("data")) {
                                                    JSONObject obj = json_data.getJSONObject("data");

                                                    if(obj.has("loginCount")){
                                                        int loginCnt = 5 - obj.getInt("loginCount");

                                                        Intent intent = new Intent(LoginActivity.this, PopupActivity.class);
                                                        intent.putExtra(PopupActivity.POPUP_MODE, PopupActivity.MODE_NORMAL);
                                                        intent.putExtra(PopupActivity.POPUP_TITLE_TEXT, getString(R.string.login_activity_msg_3));
                                                        intent.putExtra(PopupActivity.POPUP_BODY_TEXT, String.format(getString(R.string.login_activity_msg_1), String.valueOf(loginCnt)));
                                                        intent.putExtra(PopupActivity.POPUP_CONFIRM_BUTTON_TEXT, getString(R.string.ok));
                                                        startActivityForResult(intent, RESULT_LOGIN_FAIL);
                                                        overridePendingTransition(0, 0);
                                                    }
                                                }
                                                break;

                                            case HttpApi.RESPONSE_USER_NO_EXIST: // ????????? ?????????
                                                CommonUtils.showToast(LoginActivity.this, getString(R.string.toast_result_wrong_id_password));
                                                break;

                                            case HttpApi.RESPONSE_USER_INVALID_CAPTCHA: // ????????? ?????? ??????
                                            case HttpApi.RESPONSE_USER_LOGIN_ATTEMPT_EXCEEDED: // ????????? ?????? ??????
                                                Intent intent = new Intent(LoginActivity.this, PopupActivity.class);
                                                intent.putExtra(PopupActivity.POPUP_MODE, PopupActivity.MODE_NORMAL);
                                                intent.putExtra(PopupActivity.POPUP_TITLE_TEXT, getString(R.string.login_activity_msg_3));
                                                intent.putExtra(PopupActivity.POPUP_BODY_TEXT, getString(R.string.login_activity_msg_2));
                                                intent.putExtra(PopupActivity.POPUP_CONFIRM_BUTTON_TEXT, getString(R.string.ok));
                                                startActivityForResult(intent, RESULT_LOGIN_RECAPCHA);
                                                overridePendingTransition(0, 0);
                                                break;

                                            default:
                                                CommonUtils.showToast(LoginActivity.this, getString(R.string.toast_result_error_login));
                                                break;
                                        }
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                    CommonUtils.showToast(LoginActivity.this, getString(R.string.toast_result_error_login));
                                } finally {
                                    DismissConnectDialog();
                                }
                            }
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                        CommonUtils.showToast(LoginActivity.this, getString(R.string.toast_result_error_login));
                        DismissConnectDialog();
                    }
                }
                break;

            case R.id.txtFindID:
                intent = new Intent(v.getContext(), FindIdPwActivity.class);
                intent.putExtra("MODE", MODE_FIND_ID);

                CommonUtils.startActivityAni(this, intent, false, 0);
                break;

            case R.id.txtFindPass:
                intent = new Intent(v.getContext(), FindIdPwActivity.class);
                intent.putExtra("MODE", MODE_FIND_PASS);

                CommonUtils.startActivityAni(this, intent, false, 0);
                break;

            /*case R.id.txtprivacy: // ???????????? ???????????? URL ??????
                Intent i = new Intent(Intent.ACTION_VIEW);
                Uri uri = Uri.parse("http://privacy.naviensmartcontrol.com/docs/view?serviceCode=3&docType=P");
                i.setData(uri);
                startActivity(i);
                break;*/

            case R.id.layoutRegister:
                intent = new Intent(v.getContext(), RegisterInfoActivity.class);
                intent.putExtra("mode", MODE_DEVICE_REG);
                intent.putExtra("sub_mode", MODE_DEVICE_ROOM_CON);
                CommonUtils.startActivityAni(this, intent, false, 0);
                break;

            case R.id.btnDeleteID:
                txtID.setText("");
                break;

            case R.id.btnDeletePass:
                txtPass.setText("");
                break;
        }
    }

    @Override
    protected void onDestroy() {
        CommonUtils.DismissConnectDialog();
        super.onDestroy();
    }

    /**
     * ikHwang 2020-12-03 ?????? 10:12 ????????? ??????
     */
    private void reCaptcha(){
        reCaptchaToken = "";
        SafetyNet.getClient(LoginActivity.this).verifyWithRecaptcha("6LchtvUZAAAAADz6tE1ZZwCYxiCCu-5jCVkBAIhI")
                .addOnSuccessListener(tokenResponse -> { //?????? ?????? ??????
                    reCaptchaToken = tokenResponse.getTokenResult();
                    CommonUtils.showToast(LoginActivity.this, getString(R.string.login_activity_msg_5));
                    CommonUtils.customLog(TAG, "SafetyNet token : " + reCaptchaToken, Log.ERROR);
                })
                .addOnFailureListener(e -> {
                    if (e instanceof ApiException) {
                        ApiException apiException = (ApiException) e;

                        int statusCode = apiException.getStatusCode();

                        switch (statusCode){
                            case 12007: // RECAPTCHA_INVALID_SITEKEY ???????????? ?????? ??????
                                break;

                            case 12008: // RECAPTCHA_INVALID_KEYTYPE ???????????? ?????? ??????
                                break;

                            case 12013: // RECAPTCHA_INVALID_PACKAGE_NAME ?????? ?????? ????????? ????????? ????????? ?????? ????????? ????????? ???????????? ??????
                                break;

                            case 12006: // UNSUPPORTED_SDK_VERSION API??? ????????? Android SDK ???????????? ???????????? ??????
                                break;

                            case 15: // TIMEOUT API??? ????????? ????????? ??? ????????? ???????????????????????????
                                break;

                            case 7: // NETWORK_ERROR ???????????? ???????????? ???????????????
                                break;

                            case 13: // ERROR ???????????? ???????????? ????????? ??????????????????.
                                break;
                        }

                        CommonUtils.showToast(LoginActivity.this, String.format(getString(R.string.login_activity_msg_4), statusCode));
                    } else {
                        // A different, unknown type of error occurred.
                        CommonUtils.showToast(LoginActivity.this, String.format(getString(R.string.login_activity_msg_4), 0));
                    }
                });
    }
}