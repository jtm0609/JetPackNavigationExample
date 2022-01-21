package kr.co.kdone.airone.activity.login;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import kr.co.kdone.airone.R;
import kr.co.kdone.airone.activity.register.RegisterResultActivity;
import kr.co.kdone.airone.utils.CommonUtils;
import kr.co.kdone.airone.utils.HttpApi;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

import static kr.co.kdone.airone.utils.CommonUtils.DismissConnectDialog;
import static kr.co.kdone.airone.utils.CommonUtils.MODE_FIND_PASS;
import static kr.co.kdone.airone.utils.CommonUtils.checkNetwork;
import static kr.co.kdone.airone.utils.CommonUtils.displayProgress;
import static kr.co.kdone.airone.utils.CommonUtils.isNumAndStringValid;

/**
 * ikHwang 2019-06-04 오전 8:52 비밀번호 찾기 (새비밀번호 입력)
 */
public class FindPwChangeActivity extends AppCompatActivity implements View.OnClickListener {
    private final String TAG = getClass().getSimpleName();

    private Activity act;
    private TextView txtPass, txtPassConfirm;
    private ImageView btnDeletePass, btnDeletePassConfirm;
    private TextView txtErrorPassword;
    private TextView text_hint_pw;

    private String userID;
    private String deviceID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_pw_change);

        CommonUtils.setupUI(this, findViewById(R.id.layoutMain));

        act = this;

        userID = getIntent().getStringExtra("userID");
        deviceID = getIntent().getStringExtra("deviceID");

        txtPass = findViewById(R.id.txtPass);
        txtPassConfirm = findViewById(R.id.txtPassConfirm);
        btnDeletePass = findViewById(R.id.btnDeletePass);
        btnDeletePassConfirm = findViewById(R.id.btnDeletePassConfirm);
        txtErrorPassword = findViewById(R.id.txtErrorPassword);

        text_hint_pw = findViewById(R.id.text_hint_pw);

        txtPass.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                btnDeletePass.setVisibility(s.length() == 0 ? View.GONE : View.VISIBLE);

                txtErrorPassword.setEnabled(true);

                String password = txtPass.getText().toString();
                String password_confirm = txtPassConfirm.getText().toString();

                if(password.length() == 0){
                    text_hint_pw.setVisibility(View.GONE);
                }else if ((password.length() < 8) || !isNumAndStringValid(password)) {
                    text_hint_pw.setVisibility(View.VISIBLE);
                }else{
                    text_hint_pw.setVisibility(View.GONE);
                }

                if (TextUtils.isEmpty(password) || TextUtils.isEmpty(password_confirm) || password.equals(password_confirm)) {
//                    txtErrorPassword.setVisibility(View.GONE);
                    txtErrorPassword.setText(getString(R.string.hint_input_password));
                    txtErrorPassword.setTextColor(getResources().getColor(R.color.text_hint_error));
                }else{
//                    txtErrorPassword.setVisibility(View.VISIBLE);
                    txtErrorPassword.setText(getString(R.string.activity_register_member_str_7));
                    txtErrorPassword.setTextColor(getResources().getColor(R.color.color_validate));
                }
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

        txtPassConfirm.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                btnDeletePassConfirm.setVisibility(s.length() == 0 ? View.GONE : View.VISIBLE);

                txtErrorPassword.setEnabled(true);

                String password = txtPass.getText().toString();
                String password_confirm = txtPassConfirm.getText().toString();

                if(password_confirm.length() == 0){
                    txtErrorPassword.setVisibility(View.GONE);
                }else if (!password_confirm.equals(password)) {
                    txtErrorPassword.setVisibility(View.VISIBLE);
                }

                if (TextUtils.isEmpty(password) || TextUtils.isEmpty(password_confirm) || password.equals(password_confirm)) {
//                    txtErrorPassword.setVisibility(View.GONE);
                    txtErrorPassword.setText(getString(R.string.hint_input_password));
                    txtErrorPassword.setTextColor(getResources().getColor(R.color.text_hint_error));
                }else{
//                    txtErrorPassword.setVisibility(View.VISIBLE);
                    txtErrorPassword.setText(getString(R.string.activity_register_member_str_7));
                    txtErrorPassword.setTextColor(getResources().getColor(R.color.color_validate));
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        txtPassConfirm.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    btnDeletePassConfirm.setVisibility(txtPassConfirm.getText().length() == 0 ? View.GONE : View.VISIBLE);
                } else {
                    btnDeletePassConfirm.setVisibility(hasFocus ? View.VISIBLE : View.GONE);
                }
            }
        });

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
        int id = v.getId();
        switch (id) {
            case R.id.btnBack:
                onBackPressed();
                break;

            case R.id.layoutOK:
                if (checkValueBeforeSend()) {
                    if (checkNetwork(act)) {
                        displayProgress(this, "", "");
                        try {
                            HttpApi.PostV2ResetPassword(txtPass.getText().toString(), deviceID, userID,
                                    new Callback() {
                                        @Override
                                        public void onFailure(Call call, IOException e) {
                                            DismissConnectDialog();
                                            CommonUtils.showToast(FindPwChangeActivity.this, getString(R.string.toast_result_can_not_change_password));
                                        }

                                        @Override
                                        public void onResponse(Call call, Response response) throws IOException {
                                            try {
                                                JSONObject json_data = new JSONObject(response.body().string());

                                                CommonUtils.customLog(TAG, "user/reset-password : " + json_data.toString(), Log.ERROR);

                                                switch (json_data.optInt("code", 0)){
                                                    case HttpApi.RESPONSE_SUCCESS: // 비밀번호 변경 성공
                                                        CommonUtils.showToast(FindPwChangeActivity.this, FindPwChangeActivity.this.getString(R.string.toast_result_change_password));

                                                        Intent intent = new Intent(FindPwChangeActivity.this.act, RegisterResultActivity.class);
                                                        intent.putExtra("mode", MODE_FIND_PASS);
                                                        CommonUtils.startActivityAni(act, intent, true, 0);
                                                        break;

                                                    case HttpApi.RESPONSE_WRONG_USER_INFO: // 사용자 정보 없음
                                                    case HttpApi.RESPONSE_DEVICE_NOT_EXIST: // 디바이스 정보 없음
                                                        CommonUtils.showToast(FindPwChangeActivity.this, FindPwChangeActivity.this.getString(R.string.toast_result_no_find_device_or_id));
                                                        break;

                                                    case HttpApi.RESPONSE_SERVER_EXCEPTION: // 서버 오류
                                                    case HttpApi.RESPONSE_INVALID_PARAMETERS: // 파라미터 오류
                                                    default:
                                                        CommonUtils.showToast(FindPwChangeActivity.this, FindPwChangeActivity.this.getString(R.string.toast_result_can_not_change_password));
                                                        break;
                                                }
                                            } catch (JSONException e) {
                                                e.printStackTrace();
                                                CommonUtils.showToast(FindPwChangeActivity.this, getString(R.string.toast_result_can_not_change_password));
                                            }

                                            CommonUtils.DismissConnectDialog();
                                        }
                                    });
                        } catch (Exception e) {
                            e.printStackTrace();
                            DismissConnectDialog();
                            CommonUtils.showToast(FindPwChangeActivity.this, getString(R.string.toast_result_can_not_change_password));
                        }
                    } else {
                        CommonUtils.showToast(FindPwChangeActivity.this, getString(R.string.toast_result_check_network));
                    }
                }
                break;

            case R.id.btnDeletePass:
                txtPass.setText("");
                break;

            case R.id.btnDeletePassConfirm:
                txtPassConfirm.setText("");
                break;

        }
    }

    private boolean checkValueBeforeSend() {
        String pass = txtPass.getText().toString();
        String pass_confirm = txtPassConfirm.getText().toString();

        if (TextUtils.isEmpty(pass)) {
            CommonUtils.showToast(FindPwChangeActivity.this, getString(R.string.toast_result_input_password_new2));
        } else if (TextUtils.isEmpty(pass)) {
            CommonUtils.showToast(FindPwChangeActivity.this, getString(R.string.toast_result_input_password_new2));
        } else if (pass.length() < 10 || !isNumAndStringValid(pass)) {
            txtErrorPassword.setEnabled(false);
//            CommonUtils.showToast(FindPwChangeActivity.this, getString(R.string.error_inpput_not_form_password));
        } else if (!pass.equals(pass_confirm)) {
            CommonUtils.showToast(FindPwChangeActivity.this, getString(R.string.toast_result_change_save_fail));
        } else {
            return true;
        }
        return false;
    }
}