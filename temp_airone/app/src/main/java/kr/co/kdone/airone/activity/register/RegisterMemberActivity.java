package kr.co.kdone.airone.activity.register;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.appcompat.app.AppCompatActivity;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import kr.co.kdone.airone.R;
import kr.co.kdone.airone.activity.register.prism.SelectJoinDeviceActivity;
import kr.co.kdone.airone.utils.CommonUtils;
import kr.co.kdone.airone.utils.HttpApi;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

import static kr.co.kdone.airone.utils.CommonUtils.DismissConnectDialog;
import static kr.co.kdone.airone.utils.CommonUtils.IsRunningProgress;
import static kr.co.kdone.airone.utils.CommonUtils.MODE_DEVICE_REG;
import static kr.co.kdone.airone.utils.CommonUtils.MODE_DEVICE_ROOM_CON;
import static kr.co.kdone.airone.utils.CommonUtils.displayProgress;
import static kr.co.kdone.airone.utils.CommonUtils.isNumAndStringValid;
import static kr.co.kdone.airone.utils.CommonUtils.setEditText;
import static kr.co.kdone.airone.utils.CommonUtils.showKeyboard;

/**
 * ikHwang 2019-06-04 오전 11:34 회원가입2 (아이디, 비밀번호 입력)
 */
public class RegisterMemberActivity extends AppCompatActivity implements View.OnClickListener {
    private String TAG = getClass().getSimpleName();

    private EditText txtID;
    private EditText txtPass;
    private EditText txtPassConfirm;
    private ImageView btnDeleteID;
    private ImageView btnDeletePass;
    private ImageView btnDeletePassConfirm;
    private TextView txtErrorPassword;
    private TextView text_hint_pw;

    // ikHwang 2019-06-03 오전 11:48 아이디 중복체크 상태
    private boolean duplicationID = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_member);

        CommonUtils.setupUI(this, findViewById(R.id.layoutMain));

        txtID = findViewById(R.id.txtID);
        txtPass = findViewById(R.id.txtPass);
        txtPassConfirm = findViewById(R.id.txtPassConfirm);
        btnDeleteID = findViewById(R.id.btnDeleteID);
        btnDeletePass = findViewById(R.id.btnDeletePass);
        btnDeletePassConfirm = findViewById(R.id.btnDeletePassConfirm);
        txtErrorPassword = findViewById(R.id.txtErrorPassword);

        text_hint_pw = findViewById(R.id.text_hint_pw);

        txtID.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                btnDeleteID.setVisibility(s.length() == 0 ? View.GONE : View.VISIBLE);

                // ikHwang 2019-06-03 오후 12:02 아이디 텍스트 변경시 중복체크 재확인 하기위해 적용
                duplicationID = false;
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        txtID.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    showKeyboard(txtID);
                    btnDeleteID.setVisibility(txtID.getText().length() == 0 ? View.GONE : View.VISIBLE);
                } else {
                    btnDeleteID.setVisibility(hasFocus ? View.VISIBLE : View.INVISIBLE);
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
                    txtErrorPassword.setVisibility(View.GONE);
                }else{
                    txtErrorPassword.setVisibility(View.VISIBLE);
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

                if (TextUtils.isEmpty(password) || TextUtils.isEmpty(password_confirm) || password.equals(password_confirm)) {
                    txtErrorPassword.setVisibility(View.GONE);
                }else{
                    txtErrorPassword.setVisibility(View.VISIBLE);
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

//        setEditText(findViewById(R.id.layoutMain));
        txtID.setFilters(new InputFilter[]{CommonUtils.IDCharacterFilter});
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
            case R.id.btnBack: // 뒤로가기
                this.onBackPressed();
                break;

            case R.id.layoutOK: // 다음
                if(duplicationID){ // 아이디 중복체크 완료
                    String userID = txtID.getText().toString();
                    String password = txtPass.getText().toString();
                    String password_confirm = txtPassConfirm.getText().toString();

                    if (checkValueBeforeSend(userID, password, password_confirm)) {
                        // 제품 선택 화면으로 이동
//                      Intent intent = new Intent(RegisterMemberActivity.this, RegisterDevice2Activity.class);
                        Intent intent = new Intent(RegisterMemberActivity.this, SelectJoinDeviceActivity.class);
                        intent.putExtra("mode", MODE_DEVICE_REG);
                        intent.putExtra("sub_mode", MODE_DEVICE_ROOM_CON);
                        intent.putExtra("userID", userID);
                        intent.putExtra("password", password);
//                        intent.putExtra("name", getIntent().getStringExtra("name"));
//                        intent.putExtra("phone", getIntent().getStringExtra("phone"));
                        intent.putExtras(getIntent());
                        CommonUtils.startActivityAni(this, intent, false, 0);
                    }
                }else{
                    CommonUtils.showToast(RegisterMemberActivity.this ,getString(R.string.RegisterMemberActivity_str_1));
                }
                break;

            case R.id.btnDeleteID: // 아이디 삭제
                txtID.setText("");
                break;

            case R.id.btnDeletePass: // 비밀번호 삭제
                txtPass.setText("");
                break;

            case R.id.btnDeletePassConfirm: // 비밀번호 확인 삭제
                txtPassConfirm.setText("");
                break;

            case R.id.btn_member_check: // 아이디 중복 체크
                String userID = txtID.getText().toString();

                if (TextUtils.isEmpty(userID)) {
                    CommonUtils.showToast(RegisterMemberActivity.this, getString(R.string.RegisterMemberActivity_str_3));
                    return;
                }

                checkDuplicateUser(userID);
                break;
        }
    }

    /**
     * ikHwang 2019-06-03 오전 11:55 사용자 아이디 중복 확인
     * @param userId
     */
    private void checkDuplicateUser(final String userId){
        if (!IsRunningProgress()) {
            displayProgress(RegisterMemberActivity.this,"", "");
        }

        try {
            HttpApi.PostCheckDuplicateAccount(userId, new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            DismissConnectDialog();
                            CommonUtils.showToast(RegisterMemberActivity.this, getString(R.string.Common_str_1));
                        }
                    });
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    try {
                        String strResponse = response.body().string();

                        CommonUtils.customLog(TAG, "user/check-account : " + strResponse, Log.ERROR);

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                DismissConnectDialog();
                            }
                        });

                        if(TextUtils.isEmpty(strResponse)){
                            CommonUtils.showToast(RegisterMemberActivity.this, getString(R.string.Common_str_1));
                        }else{
                            JSONObject json_data = new JSONObject(strResponse);

                            switch (json_data.optInt("code", 0)){
                                case HttpApi.RESPONSE_SUCCESS: // 아이비 중복체크 성공, 사용가능한 아이디
                                    // ikHwang 2019-06-04 오후 2:32 사용가능한 아이디 안내
                                    duplicationID = true;
                                    CommonUtils.showToast(RegisterMemberActivity.this, getString(R.string.RegisterMemberActivity_str_6));
                                    break;

                                case HttpApi.RESPONSE_USER_ID_ALREADY_EXIST: // 이미 사용중인 아이디
                                    CommonUtils.showToast(RegisterMemberActivity.this, getString(R.string.RegisterMemberActivity_str_2));
                                    break;

                                default:
                                    CommonUtils.showToast(RegisterMemberActivity.this, getString(R.string.Common_str_1));
                                    break;
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        CommonUtils.showToast(RegisterMemberActivity.this, getString(R.string.Common_str_1));
                    }
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    DismissConnectDialog();
                    CommonUtils.showToast(RegisterMemberActivity.this, getString(R.string.Common_str_1));
                }
            });
        }
    }

    private boolean requestPermission() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{
                            android.Manifest.permission.ACCESS_FINE_LOCATION}, 123);
            return false;
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            findViewById(R.id.layoutOK).performClick();
        }
    }

    private boolean checkValueBeforeSend(String id, String pass, String pass_confirm) {
        if (id.equals("") || pass.equals("") || pass_confirm.equals("")) {
            CommonUtils.showToast(RegisterMemberActivity.this, getString(R.string.RegisterMemberActivity_str_3));
        } else if (pass.length() < 10 || !isNumAndStringValid(pass)) {
            txtErrorPassword.setEnabled(false);
            CommonUtils.showToast(RegisterMemberActivity.this, getString(R.string.RegisterMemberActivity_str_4));
        } else if (!pass.equals(pass_confirm)) {
            CommonUtils.showToast(RegisterMemberActivity.this, getString(R.string.RegisterMemberActivity_str_5));
        } else {
            return true;
        }
        return false;
    }
}