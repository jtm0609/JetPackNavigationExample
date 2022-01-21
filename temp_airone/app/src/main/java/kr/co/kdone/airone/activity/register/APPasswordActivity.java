package kr.co.kdone.airone.activity.register;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import kr.co.kdone.airone.R;
import kr.co.kdone.airone.utils.CommonUtils;

import static kr.co.kdone.airone.utils.CommonUtils.setEditText;

/**
 * ikHwang 2019-06-04 오전 8:37 AP 비밀번호 입력 화면
 */
public class APPasswordActivity extends AppCompatActivity implements View.OnClickListener {
    private final String TAG = getClass().getSimpleName();

    private LinearLayout    btn_show_and_hide;
    private ImageView       img_show_and_hide;
    private EditText        txtAPPassword;
    private boolean         isShowPW = false;   // 비밀번호 보기 상태

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ap_password);

        CommonUtils.setupUI(this, findViewById(R.id.layoutMain));

        btn_show_and_hide = findViewById(R.id.btn_show_and_hide);
        img_show_and_hide = findViewById(R.id.img_show_and_hide);
        txtAPPassword = findViewById(R.id.txtAPPassword);

        String ssid = getIntent().getStringExtra("ssid");
        ((TextView) findViewById(R.id.txtSSID)).setText(getString(R.string.label_ssid, ssid));

//        setEditText(findViewById(R.id.layoutMain));

        btn_show_and_hide.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isShowPW){
                    img_show_and_hide.setImageResource(R.drawable.icon_pw_hide);
                    txtAPPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    isShowPW = false;
                }else{
                    img_show_and_hide.setImageResource(R.drawable.icon_pw_show);
                    txtAPPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                    isShowPW = true;
                }

                txtAPPassword.setSelection(txtAPPassword.length());
            }
        });

        txtAPPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(txtAPPassword.length() > 0) btn_show_and_hide.setVisibility(View.VISIBLE);
                else btn_show_and_hide.setVisibility(View.GONE);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.btnBack:
                finish();
                break;

            case R.id.layoutOK:
                String password = ((EditText) findViewById(R.id.txtAPPassword)).getText().toString();
                if (password.length() < 8) {
                    CommonUtils.showToast(APPasswordActivity.this, getString(R.string.input_register_password_content));
                } else {
                    Intent intent = new Intent();
                    intent.putExtra("password", password);
                    setResult(RESULT_OK, intent);
                    finish();
                }
                break;
        }
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(0, R.anim.slide_up_out);
    }
}
