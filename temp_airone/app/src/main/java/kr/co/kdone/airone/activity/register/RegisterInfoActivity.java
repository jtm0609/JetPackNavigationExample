package kr.co.kdone.airone.activity.register;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import kr.co.kdone.airone.R;
import kr.co.kdone.airone.utils.CommonUtils;

import static kr.co.kdone.airone.utils.CommonUtils.isNumValid;
import static kr.co.kdone.airone.utils.CommonUtils.setEditText;
import static kr.co.kdone.airone.utils.CommonUtils.showKeyboard;

/**
 * 회원가입1 (이름, 전화번호 입력)
 */
public class RegisterInfoActivity extends AppCompatActivity implements View.OnClickListener {
    private String TAG = this.getClass().getSimpleName();

    private EditText txtName;
    private EditText txtPhoneNumber;
    private ImageView btnDeleteName;
    private ImageView btnDeletePhone;
    private TextView txtErrorPhoneNumber;
    private CheckBox chkPrivacyTerms;
    private CheckBox chkPrivacyTerms2;
    private CheckBox chkPrivacyTerms3;
    private CheckBox chkAll;

    private TextView txtPrivacyTerms;
    private TextView txtPrivacyTerms2;
    private TextView txtPrivacyTerms3;
    private TextView txtPrivacyTermsHint3;
    private TextView txtAll;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_info);
        CommonUtils.setupUI(this, findViewById(R.id.layoutMain));

        txtName = findViewById(R.id.txtName);
        txtPhoneNumber = findViewById(R.id.txtPhoneNumber);
        btnDeleteName = findViewById(R.id.btnDeleteName);
        btnDeletePhone = findViewById(R.id.btnDeletePhone);
        txtErrorPhoneNumber = findViewById(R.id.txtErrorPhoneNumber);
        chkPrivacyTerms = findViewById(R.id.chkPrivacyTerms);
        chkPrivacyTerms2 = findViewById(R.id.chkPrivacyTerms2);
        chkPrivacyTerms3 = findViewById(R.id.chkPrivacyTerms3);
        chkAll = findViewById(R.id.chkAll);

        txtPrivacyTerms = findViewById(R.id.txtPrivacyTerms);
        txtPrivacyTerms2 = findViewById(R.id.txtPrivacyTerms2);
        txtPrivacyTerms3 = findViewById(R.id.txtPrivacyTerms3);
        txtPrivacyTermsHint3 = findViewById(R.id.txtPrivacyTermsHint3);
        txtAll = findViewById(R.id.txtAll);

        txtName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                btnDeleteName.setVisibility(s.length() == 0 ? View.GONE : View.VISIBLE);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        txtName.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    showKeyboard(txtName);
                    btnDeleteName.setVisibility(txtName.getText().length() == 0 ? View.GONE : View.VISIBLE);
                } else {
                    btnDeleteName.setVisibility(hasFocus ? View.VISIBLE : View.GONE);
                }
            }
        });

        txtPhoneNumber.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                btnDeletePhone.setVisibility(s.length() == 0 ? View.GONE : View.VISIBLE);
                txtErrorPhoneNumber.setEnabled(true);

                // ikHwang 2019-06-04 오전 11:20 전화번호 형식 체크하여 노티 UI 표시 처리
                if(s.length() > 0){
                    if(isNumValid(s.toString())){
                        txtErrorPhoneNumber.setVisibility(View.GONE);
                    }else{
                        txtErrorPhoneNumber.setVisibility(View.VISIBLE);
                    }
                }else{
                    txtErrorPhoneNumber.setVisibility(View.GONE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        txtPhoneNumber.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    btnDeletePhone.setVisibility(txtPhoneNumber.getText().length() == 0 ? View.GONE : View.VISIBLE);
                } else {
                    btnDeletePhone.setVisibility(hasFocus ? View.VISIBLE : View.GONE);
                }
            }
        });

//        setEditText(findViewById(R.id.layoutMain));
        txtName.setFilters(new InputFilter[]{CommonUtils.nameCharacterFilter});
        txtPhoneNumber.setFilters(new InputFilter[]{new InputFilter.LengthFilter(11)});

        chkAll.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                chkPrivacyTerms.setChecked(isChecked);
                chkPrivacyTerms2.setChecked(isChecked);
                chkPrivacyTerms3.setChecked(isChecked);

                txtPrivacyTerms.setSelected(isChecked);
                txtPrivacyTerms2.setSelected(isChecked);
                txtPrivacyTerms3.setSelected(isChecked);
                txtPrivacyTermsHint3.setSelected(isChecked);
                txtAll.setSelected(isChecked);
            }
        });

        chkPrivacyTerms3.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                txtPrivacyTerms3.setSelected(isChecked);
                txtPrivacyTermsHint3.setSelected(isChecked);

                if(chkPrivacyTerms.isChecked() && chkPrivacyTerms2.isChecked() && chkPrivacyTerms3.isChecked()){
                    chkAll.setChecked(true);
                    txtAll.setSelected(true);
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
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        int type = 0;

        if(requestCode == 1001){
            if(resultCode == Activity.RESULT_OK){
                if(null != data && data.hasExtra("type")){
                    type = data.getIntExtra("type", 0);

                    if(type == 0){
                        // 서비스 이용약관 동의 완료
                        chkPrivacyTerms.setChecked(true);
                        txtPrivacyTerms.setSelected(true);
                    }else{
                        // 개인정보 처리방침 동의 완료
                        chkPrivacyTerms2.setChecked(true);
                        txtPrivacyTerms2.setSelected(true);
                    }

                    if(chkPrivacyTerms.isChecked() && chkPrivacyTerms2.isChecked() && chkPrivacyTerms3.isChecked()){
                        chkAll.setChecked(true);
                        txtAll.setSelected(true);
                    }
                }
            }
        }
    }

    @Override
    public void onClick(View v) {
        Intent intent;
        switch (v.getId()) {
            case R.id.btnBack:
                this.onBackPressed();
                break;

            case R.id.layoutOK:
                String name = txtName.getText().toString();
                String phone = txtPhoneNumber.getText().toString();

                if (checkValueBeforeSend(name, phone)) {
                    intent = new Intent(RegisterInfoActivity.this, RegisterMemberActivity.class);
                    intent.putExtra("name", name);
                    intent.putExtra("phone", phone);
                    CommonUtils.startActivityAni(this, intent, false, 0);
                }
                break;

            case R.id.txtPrivacyTerms:
            case R.id.chkPrivacyTerms:
                chkPrivacyTerms.setChecked(false);
                intent = new Intent(RegisterInfoActivity.this, TermsOfServiceActivity.class);
                intent.putExtra("type", 0);
                startActivityForResult(intent, 1001);
                overridePendingTransition(R.anim.slide_down_in, R.anim.slide_none);
                break;

            case R.id.txtPrivacyTerms2:
            case R.id.chkPrivacyTerms2:
                chkPrivacyTerms2.setChecked(false);
                intent = new Intent(RegisterInfoActivity.this, TermsOfServiceActivity.class);
                intent.putExtra("type", 1);
                startActivityForResult(intent, 1001);
                overridePendingTransition(R.anim.slide_down_in, R.anim.slide_none);
                break;

            case R.id.txtPrivacyTerms3:
                chkPrivacyTerms3.setChecked(!chkPrivacyTerms3.isChecked());
                break;

            case R.id.btnDeleteName:
                txtName.setText("");
                break;

            case R.id.btnDeletePhone:
                txtPhoneNumber.setText("");
                break;

            case R.id.txtprivacy: // 개인정보 처리방침 URL 링크
                Intent i = new Intent(Intent.ACTION_VIEW);
                Uri uri = Uri.parse("http://privacy.naviensmartcontrol.com/docs/view?serviceCode=3&docType=P");
                i.setData(uri);
                startActivity(i);
                break;
        }
    }

    private boolean checkValueBeforeSend(String name, String phone) {
        boolean check = chkPrivacyTerms.isChecked();
        boolean check2 = chkPrivacyTerms2.isChecked();
        boolean check3 = chkPrivacyTerms3.isChecked();

        if (TextUtils.isEmpty(name)) {
            CommonUtils.showToast(RegisterInfoActivity.this, getString(R.string.RegisterInfoActivity_str_1));
        } else if (!isNumValid(phone) || TextUtils.isEmpty(name)) {
//            CommonUtils.showToast(RegisterInfoActivity.this, getString(R.string.RegisterInfoActivity_str_2));
            txtErrorPhoneNumber.setEnabled(false);
        } else if (!check) {
            CommonUtils.showToast(RegisterInfoActivity.this, getString(R.string.RegisterInfoActivity_str_3));
        }  else if (!check2) {
            CommonUtils.showToast(RegisterInfoActivity.this, getString(R.string.RegisterInfoActivity_str_4));
        }  else if (!check3) {
            CommonUtils.showToast(RegisterInfoActivity.this, getString(R.string.RegisterInfoActivity_str_5));
        } else {
            return true;
        }
        return false;
    }
}
