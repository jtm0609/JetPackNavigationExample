package kr.co.kdone.airone.activity.more;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import kr.co.kdone.airone.CleanVentilationApplication;
import kr.co.kdone.airone.R;
import kr.co.kdone.airone.data.UserInfo;
import kr.co.kdone.airone.utils.CommonUtils;
import kr.co.kdone.airone.utils.HttpApi;
import kr.co.kdone.airone.utils.SharedPrefUtil;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

import static kr.co.kdone.airone.utils.CommonUtils.DismissConnectDialog;
import static kr.co.kdone.airone.utils.CommonUtils.MODE_MODIFY_NAME;
import static kr.co.kdone.airone.utils.CommonUtils.MODE_MODIFY_PASSWORD;
import static kr.co.kdone.airone.utils.CommonUtils.MODE_MODIFY_PHONE;
import static kr.co.kdone.airone.utils.CommonUtils.MODE_NONE;
import static kr.co.kdone.airone.utils.CommonUtils.displayProgress;
import static kr.co.kdone.airone.utils.CommonUtils.isNumAndStringValid;
import static kr.co.kdone.airone.utils.CommonUtils.isNumValid;
import static kr.co.kdone.airone.utils.CommonUtils.setEditText;
import static kr.co.kdone.airone.utils.CommonUtils.showKeyboard;

/**
 * ikHwang 2019-06-04 오전 9:49 사용자 정보 변경
 */
public class ModifyUserActivity extends AppCompatActivity implements View.OnClickListener {
    private final String TAG = getClass().getSimpleName();

    private LinearLayout layoutChangePassword, layoutChangeName, layoutChangePhone;
    private EditText txtCurrentPassword, txtNewPassword, txtNewPasswordConfirm;
    private EditText txtUserName, txtUserPhone;
    private TextView txtTitle;
    private ImageView btnDeleteCurrentPW, btnDeleteNewPW, btnDeleteNewConfirmPW, btnDeleteUserName, btnDeletePhone;
    private TextView text_hint_new_pw, text_hint_confirm_pw, text_hint_phone;

    private int mode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_modify_user);

        CommonUtils.setupUI(this, findViewById(R.id.layoutMain));

        mode = getIntent().getIntExtra("mode", MODE_NONE);

        layoutChangePassword = findViewById(R.id.layoutChangePassword);
        layoutChangeName = findViewById(R.id.layoutChangeName);
        layoutChangePhone = findViewById(R.id.layoutChangePhone);
        txtCurrentPassword = findViewById(R.id.txtCurrentPassword);
        txtNewPassword = findViewById(R.id.txtNewPassword);
        txtNewPasswordConfirm = findViewById(R.id.txtNewPasswordConfirm);
        txtUserName = findViewById(R.id.txtUserName);
        txtUserPhone = findViewById(R.id.txtUserPhone);
        txtTitle = findViewById(R.id.txtTitle);

        btnDeleteCurrentPW = findViewById(R.id.btnDeleteCurrentPW);
        btnDeleteNewPW = findViewById(R.id.btnDeleteNewPW);
        btnDeleteNewConfirmPW = findViewById(R.id.btnDeleteNewConfirmPW);
        btnDeleteUserName = findViewById(R.id.btnDeleteUserName);
        btnDeletePhone = findViewById(R.id.btnDeletePhone);

        text_hint_new_pw = findViewById(R.id.text_hint_new_pw);
        text_hint_confirm_pw = findViewById(R.id.text_hint_confirm_pw);
        text_hint_phone = findViewById(R.id.text_hint_phone);

        txtUserName.setText(CleanVentilationApplication.getInstance().getUserInfo().getName());
        txtUserName.setSelection(txtUserName.length());

        txtUserPhone.setText(CleanVentilationApplication.getInstance().getUserInfo().getMobile());
        txtUserPhone.setSelection(txtUserPhone.length());

        txtCurrentPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                btnDeleteCurrentPW.setVisibility(s.length() == 0 ? View.GONE : View.VISIBLE);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        txtNewPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                btnDeleteNewPW.setVisibility(s.length() == 0 ? View.GONE : View.VISIBLE);

                String password = txtNewPassword.getText().toString();
                String password_confirm = txtNewPasswordConfirm.getText().toString();

                if(password.length() == 0){
                    text_hint_new_pw.setVisibility(View.GONE);
                }else if ((password.length() < 10) || !isNumAndStringValid(password)) {
                    text_hint_new_pw.setVisibility(View.VISIBLE);
                }else{
                    text_hint_new_pw.setVisibility(View.GONE);
                }

                if (TextUtils.isEmpty(password) || TextUtils.isEmpty(password_confirm) || password.equals(password_confirm)) {
                    text_hint_confirm_pw.setVisibility(View.GONE);
                }else{
                    text_hint_confirm_pw.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        txtNewPasswordConfirm.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                btnDeleteNewConfirmPW.setVisibility(s.length() == 0 ? View.GONE : View.VISIBLE);

                String password = txtNewPassword.getText().toString();
                String password_confirm = txtNewPasswordConfirm.getText().toString();

                if (TextUtils.isEmpty(password) || TextUtils.isEmpty(password_confirm) || password.equals(password_confirm)) {
                    text_hint_confirm_pw.setVisibility(View.GONE);
                }else{
                    text_hint_confirm_pw.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        txtUserPhone.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                btnDeletePhone.setVisibility(s.length() == 0 ? View.GONE : View.VISIBLE);

                if(s.length() > 0){
                    if(isNumValid(s.toString())){
                        text_hint_phone.setVisibility(View.GONE);
                    }else{
                        text_hint_phone.setVisibility(View.VISIBLE);
                    }
                }else{
                    text_hint_phone.setVisibility(View.GONE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        txtUserName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                btnDeleteUserName.setVisibility(s.length() == 0 ? View.GONE : View.VISIBLE);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        changeUI();
//        setEditText(findViewById(R.id.layoutMain));
        txtUserName.setFilters(new InputFilter[]{CommonUtils.nameCharacterFilter});
        txtUserPhone.setFilters(new InputFilter[]{new InputFilter.LengthFilter(11)});
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
                finish();
                break;

            case R.id.btnDeleteCurrentPW:
                txtCurrentPassword.setText("");
                break;

            case R.id.btnDeleteNewPW:
                txtNewPassword.setText("");
                break;

            case R.id.btnDeleteNewConfirmPW:
                txtNewPasswordConfirm.setText("");
                break;

            case R.id.btnDeleteUserName:
                txtUserName.setText("");
                break;

            case R.id.btnDeletePhone:
                txtUserPhone.setText("");
                break;

            case R.id.layoutOK:
                if (checkValueBeforeSend()) {
                    //V2A 추가. 비밀번호 변경전 id, device id 확인
                    displayProgress(this, "", "");

                    if(mode == MODE_MODIFY_PASSWORD){ //비밀번호 변경 V2 : 회원정보수정에서 따로 분리되었음.
                        postModifyPassword();
                    }else {
                        postModifyUserInfo();
                    }
                }
                break;
        }
    }

    //V2A 작업.
    private void postModifyUserInfo(){
        final String name = txtUserName.getText().toString();
        final String mobile = txtUserPhone.getText().toString();

        String password = SharedPrefUtil.getString(SharedPrefUtil.USER_PASS, "");

        if (mode == MODE_MODIFY_NAME) {
            try {
                HttpApi.PostV2ChangeUserName( //V2A 작업.
                        CleanVentilationApplication.getInstance().getUserInfo().getId(),
                        password,
                        name,
                        new Callback() {
                            @Override
                            public void onFailure(Call call, IOException e) {
                                DismissConnectDialog();
                                CommonUtils.showToast(ModifyUserActivity.this, getString(R.string.toast_result_can_not_change));
                            }

                            @Override
                            public void onResponse(Call call, Response response) throws IOException {
                                try {
                                    JSONObject json_data = new JSONObject(response.body().string());

                                    CommonUtils.customLog(TAG, "user/change-name : " + json_data.toString(), Log.ERROR);

                                    switch (json_data.getInt("code")) {
                                        case HttpApi.RESPONSE_SUCCESS:
                                            UserInfo userInfo = CleanVentilationApplication.getInstance().getUserInfo();
                                            userInfo.setName(name);
                                            CommonUtils.showToast(ModifyUserActivity.this, getString(R.string.toast_result_change_save));
                                            finish();
                                            break;

                                        default:
                                            CommonUtils.showToast(ModifyUserActivity.this, getString(R.string.toast_result_can_not_change));
                                            break;
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                    CommonUtils.showToast(ModifyUserActivity.this, getString(R.string.toast_result_can_not_change));
                                }
                                DismissConnectDialog();
                            }
                        });
            } catch (Exception e) {
                e.printStackTrace();
                DismissConnectDialog();
                CommonUtils.showToast(ModifyUserActivity.this, getString(R.string.toast_result_can_not_change));
            }
        }else if (mode == MODE_MODIFY_PHONE){
            try {
                HttpApi.PostV2ChangeUserMobile( //V2A 작업.
                        CleanVentilationApplication.getInstance().getUserInfo().getId(),
                        password,
                        mobile,
                        new Callback() {
                            @Override
                            public void onFailure(Call call, IOException e) {
                                DismissConnectDialog();
                                CommonUtils.showToast(ModifyUserActivity.this, getString(R.string.toast_result_can_not_change));
                            }

                            @Override
                            public void onResponse(Call call, Response response) throws IOException {
                                try {
                                    JSONObject json_data = new JSONObject(response.body().string());

                                    CommonUtils.customLog(TAG, "user/change-name : " + json_data.toString(), Log.ERROR);

                                    switch (json_data.getInt("code")) {
                                        case HttpApi.RESPONSE_SUCCESS:
                                            UserInfo userInfo = CleanVentilationApplication.getInstance().getUserInfo();
                                            userInfo.setMobile(mobile);
                                            CommonUtils.showToast(ModifyUserActivity.this, getString(R.string.toast_result_change_save));
                                            finish();
                                            break;

                                        default:
                                            CommonUtils.showToast(ModifyUserActivity.this, getString(R.string.toast_result_can_not_change));
                                            break;
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                    CommonUtils.showToast(ModifyUserActivity.this, getString(R.string.toast_result_can_not_change));
                                }
                                DismissConnectDialog();
                            }
                        });
            } catch (Exception e) {
                e.printStackTrace();
                DismissConnectDialog();
                CommonUtils.showToast(ModifyUserActivity.this, getString(R.string.toast_result_can_not_change));
            }
        }
    }

    public static boolean isNumAndStringValid(String str) {
        int countEng = 0; // 영어 갯수
        int countNum = 0; // 숫자 갯수

        if (TextUtils.isEmpty(str)) return false;

        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);

            if ((0x61 <= c && c <= 0x7A) || (0x41 <= c && c <= 0x5A)) {
                countEng++;
            } else if (0x30 <= c && c <= 0x39) {
                countNum++;
            }
        }

        if (countEng > 0 && countNum > 0) {
            return true;
        } else {
            return false;
        }
    }

    //V2A 추가. V2 비밀번호 변경
    private void postModifyPassword(){
        final String newPassword = txtNewPassword.getText().toString();
        String password = txtCurrentPassword.getText().toString();
        try{
            HttpApi.PostV2ChangePassword(newPassword, password,
                    CleanVentilationApplication.getInstance().getRoomControllerDeviceID(),
                    CleanVentilationApplication.getInstance().getUserInfo().getId(),
                    new Callback() {
                        @Override
                        public void onFailure(Call call, IOException e) {
                            DismissConnectDialog();
                            CommonUtils.showToast(ModifyUserActivity.this, getString(R.string.toast_result_can_not_change));
                        }

                        @Override
                        public void onResponse(Call call, Response response) throws IOException {
                            try {
                                JSONObject json_data = new JSONObject(response.body().string());

                                CommonUtils.customLog(TAG, "user/change-password : " + json_data.toString(), Log.ERROR);

                                CommonUtils.DismissConnectDialog();

                                switch (json_data.optInt("code", 0)){
                                    case HttpApi.RESPONSE_SUCCESS: // 비밀번호 변경 성공
                                        CleanVentilationApplication.getInstance().getUserInfo().setPassword(newPassword);
                                        SharedPrefUtil.putString(SharedPrefUtil.USER_PASS, newPassword);
                                        CommonUtils.showToast(ModifyUserActivity.this, ModifyUserActivity.this.getString(R.string.toast_result_change_save));
                                        finish();
                                        break;

                                    case HttpApi.RESPONSE_WRONG_USER_INFO: // 사용자 정보 일치하지 않음
                                        CommonUtils.showToast(ModifyUserActivity.this, ModifyUserActivity.this.getString(R.string.toast_result_change_save_fail));
                                        break;

                                    case HttpApi.RESPONSE_INVALID_PARAMETERS: // 파라미터 오류
                                    case HttpApi.RESPONSE_DATA_NO_EXIST: // 데이터 없음
                                    default:
                                        CommonUtils.showToast(ModifyUserActivity.this, ModifyUserActivity.this.getString(R.string.toast_result_can_not_change_userinfo));
                                        break;
                                }
                            }catch (Exception e){
                                e.printStackTrace();
                                CommonUtils.DismissConnectDialog();
                                CommonUtils.showToast(ModifyUserActivity.this, getString(R.string.toast_result_can_not_change_userinfo));
                            }
                        }
                    });
        }catch (Exception e){
            e.printStackTrace();
            DismissConnectDialog();
            CommonUtils.showToast(ModifyUserActivity.this, getString(R.string.toast_result_can_not_change_userinfo));
        }
    }

    private void changeUI() {
        switch (mode) {
            case MODE_MODIFY_PASSWORD:
                txtTitle.setText(R.string.modify_password);
                layoutChangePassword.setVisibility(View.VISIBLE);
                layoutChangeName.setVisibility(View.GONE);
                layoutChangePhone.setVisibility(View.GONE);

                txtCurrentPassword.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                    @Override
                    public void onFocusChange(View v, boolean hasFocus) {
                        if (hasFocus) {
                            showKeyboard(txtCurrentPassword);
                            btnDeleteCurrentPW.setVisibility(txtCurrentPassword.length() == 0 ? View.GONE : View.VISIBLE);
                        } else {
                            btnDeleteCurrentPW.setVisibility(hasFocus ? View.VISIBLE : View.GONE);
                        }
                    }
                });

                txtNewPassword.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                    @Override
                    public void onFocusChange(View v, boolean hasFocus) {
                        if (hasFocus) {
                            btnDeleteNewPW.setVisibility(txtNewPassword.length() == 0 ? View.GONE : View.VISIBLE);
                        } else {
                            btnDeleteNewPW.setVisibility(hasFocus ? View.VISIBLE : View.GONE);
                        }
                    }
                });

                txtNewPasswordConfirm.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                    @Override
                    public void onFocusChange(View v, boolean hasFocus) {
                        if (hasFocus) {
                            btnDeleteNewConfirmPW.setVisibility(txtNewPasswordConfirm.length() == 0 ? View.GONE : View.VISIBLE);
                        } else {
                            btnDeleteNewConfirmPW.setVisibility(hasFocus ? View.VISIBLE : View.GONE);
                        }
                    }
                });
                break;

            case MODE_MODIFY_NAME:
                txtTitle.setText(R.string.change_user_name);
                layoutChangePassword.setVisibility(View.GONE);
                layoutChangeName.setVisibility(View.VISIBLE);
                layoutChangePhone.setVisibility(View.GONE);
                txtUserName.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                    @Override
                    public void onFocusChange(View v, boolean hasFocus) {
                        if (hasFocus) {
                            showKeyboard(txtUserName);
                            btnDeleteUserName.setVisibility(txtUserName.length() == 0 ? View.GONE : View.VISIBLE);
                        } else {
                            btnDeleteUserName.setVisibility(hasFocus ? View.VISIBLE : View.GONE);
                        }
                    }
                });
                break;

            case MODE_MODIFY_PHONE:
                txtTitle.setText(R.string.change_phone);
                layoutChangePassword.setVisibility(View.GONE);
                layoutChangeName.setVisibility(View.GONE);
                layoutChangePhone.setVisibility(View.VISIBLE);
                txtUserPhone.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                    @Override
                    public void onFocusChange(View v, boolean hasFocus) {
                        if (hasFocus) {
                            showKeyboard(txtUserPhone);
                            btnDeletePhone.setVisibility(txtUserPhone.length() == 0 ? View.GONE : View.VISIBLE);
                        } else {
                            btnDeletePhone.setVisibility(hasFocus ? View.VISIBLE : View.GONE);
                        }
                    }
                });
                break;
        }
    }

    private boolean checkValueBeforeSend() {
        switch (mode) {
            case MODE_MODIFY_PASSWORD:
                String currPassword = txtCurrentPassword.getText().toString();
                String newPassword = txtNewPassword.getText().toString();
                String newPasswordConfirm = txtNewPasswordConfirm.getText().toString();

                if (currPassword.equals("")) {
                    CommonUtils.showToast(ModifyUserActivity.this, getString(R.string.toast_result_input_password_new2));
                } else if (newPassword.equals("")) {
                    CommonUtils.showToast(ModifyUserActivity.this, getString(R.string.toast_result_input_password_new2));
                } else if (newPasswordConfirm.equals("")) {
                    CommonUtils.showToast(ModifyUserActivity.this, getString(R.string.toast_result_input_password_new2));
                } else if (newPassword.length() < 10 || !isNumAndStringValid(newPassword)) {
                    CommonUtils.showToast(ModifyUserActivity.this, getString(R.string.error_inpput_not_form_password));
                } else if (!newPassword.equals(newPasswordConfirm)) {
                    CommonUtils.showToast(ModifyUserActivity.this, getString(R.string.error_inpput_not_same_password));
                }
                else {
                    return true;
                }
                break;

            case MODE_MODIFY_NAME:
                String name = txtUserName.getText().toString();

                if (name.equals("")) {
                    CommonUtils.showToast(ModifyUserActivity.this, getString(R.string.toast_result_input_user_name));
                } else {
                    return true;
                }
                break;

            case MODE_MODIFY_PHONE:
                String phone = txtUserPhone.getText().toString();

                if (phone.equals("")) {
                    CommonUtils.showToast(ModifyUserActivity.this, getString(R.string.error_inpput_phone));
                } else if (!isNumValid(phone)) {
                    CommonUtils.showToast(ModifyUserActivity.this, getString(R.string.error_inpput_phone_confirm));
                } else {
                    return true;
                }
                break;
        }
        return false;
    }
}
