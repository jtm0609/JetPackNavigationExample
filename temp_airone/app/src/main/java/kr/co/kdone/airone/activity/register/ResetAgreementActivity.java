package kr.co.kdone.airone.activity.register;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.json.JSONObject;

import java.io.IOException;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import kr.co.kdone.airone.CleanVentilationApplication;
import kr.co.kdone.airone.R;
import kr.co.kdone.airone.activity.SplashActivity;
import kr.co.kdone.airone.activity.login.LoginActivity;
import kr.co.kdone.airone.activity.main.MainActivity;
import kr.co.kdone.airone.activity.main.MainPrismActivity;
import kr.co.kdone.airone.activity.more.SmartGuideActivity;
import kr.co.kdone.airone.utils.CommonUtils;
import kr.co.kdone.airone.utils.HttpApi;
import kr.co.kdone.airone.utils.SharedPrefUtil;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

import static kr.co.kdone.airone.utils.CommonUtils.DismissConnectDialog;
import static kr.co.kdone.airone.utils.CommonUtils.displayProgress;
import static kr.co.kdone.airone.utils.CommonUtils.startActivityAni;

/**
 * 이용약관 재동의;
 */
public class ResetAgreementActivity extends AppCompatActivity implements View.OnClickListener {
    private String TAG = this.getClass().getSimpleName();

    private LinearLayout layout_terms1;
    private LinearLayout layout_terms2;
    private LinearLayout layout_terms3;
    private LinearLayout layout_terms_all;

    private CheckBox chkPrivacyTerms;
    private CheckBox chkPrivacyTerms2;
    private CheckBox chkPrivacyTerms3;
    private CheckBox chkAll;

    private TextView txtPrivacyTerms;
    private TextView txtPrivacyTerms2;
    private TextView txtPrivacyTerms3;
    private TextView txtPrivacyTermsHint3;
    private TextView txtAll;

    private boolean         isAgreeServiceInfo;     // 서비스 이용약관 동의 상태
    private boolean         isAgreePersnalInfo;     // 개인정보 수집 및 이용 동의 상태
    private boolean         isAgreeUpper14Info;     // 만14세 이상 동의

    private int             terms ;    // 이용약관 재동의 상태
    private int             privacyPolicy ;  // 개인정보 수집 및 이용동의 재동의 상태
    private int             over14age ;    // 만14세 이상 재동의 상태

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_agreement);
        CommonUtils.setupUI(this, findViewById(R.id.layoutMain));

        if(getIntent().hasExtra("terms")) terms = getIntent().getIntExtra("terms",0);
        if(getIntent().hasExtra("privacyPolicy")) privacyPolicy = getIntent().getIntExtra("privacyPolicy",0);
        if(getIntent().hasExtra("over14age")) over14age = getIntent().getIntExtra("over14age",0);

        layout_terms1 = findViewById(R.id.layout_terms1);
        layout_terms2 = findViewById(R.id.layout_terms2);
        layout_terms3 = findViewById(R.id.layout_terms3);
        layout_terms_all = findViewById(R.id.layout_terms_all);

        chkPrivacyTerms = findViewById(R.id.chkPrivacyTerms);
        chkPrivacyTerms2 = findViewById(R.id.chkPrivacyTerms2);
        chkPrivacyTerms3 = findViewById(R.id.chkPrivacyTerms3);
        chkAll = findViewById(R.id.chkAll);

        txtPrivacyTerms = findViewById(R.id.txtPrivacyTerms);
        txtPrivacyTerms2 = findViewById(R.id.txtPrivacyTerms2);
        txtPrivacyTerms3 = findViewById(R.id.txtPrivacyTerms3);
        txtPrivacyTermsHint3 = findViewById(R.id.txtPrivacyTermsHint3);
        txtAll = findViewById(R.id.txtAll);

        int reAgreeCnt = 3;

        // 이용약관 재동의 필요 없음 처리 (UI숨김)
        if(!(terms ==0) ){
            chkPrivacyTerms.setChecked(true);
            isAgreeServiceInfo = true;
            layout_terms1.setVisibility(View.GONE);
            reAgreeCnt--;
        }

        // 개인정보 수집 및 이용동의 재동의 필요 없음 처리 (UI숨김)
        if(!(privacyPolicy ==0) ){
            chkPrivacyTerms2.setChecked(true);
            isAgreePersnalInfo = true;
            layout_terms2.setVisibility(View.GONE);
            reAgreeCnt--;
        }

        // 만 14세 이상 동의 필요 없음 처리  (UI숨김)
        if(!(over14age ==0) ){
            chkPrivacyTerms3.setChecked(true);
            isAgreeUpper14Info = true;
            layout_terms3.setVisibility(View.GONE);
            reAgreeCnt--;
        }

        // 동의 항목이 1개 이하인 경우 전체동의 숨김
        if(reAgreeCnt <= 1){
            layout_terms_all.setVisibility(View.GONE);
        }

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
        Intent intent = new Intent(ResetAgreementActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivityAni(ResetAgreementActivity.this, intent, true, 0);
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
            case R.id.btnBack:{
                intent = new Intent(ResetAgreementActivity.this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivityAni(ResetAgreementActivity.this, intent, true, 0);
            }
                break;

            case R.id.layoutOK:
                if (checkValueBeforeSend()) {
                    resetAgreement();
                }
                break;

            case R.id.txtPrivacyTerms:
            case R.id.chkPrivacyTerms:
                chkPrivacyTerms.setChecked(false);
                intent = new Intent(ResetAgreementActivity.this, TermsOfServiceActivity.class);
                intent.putExtra("type", 0);
                startActivityForResult(intent, 1001);
                overridePendingTransition(R.anim.slide_down_in, R.anim.slide_none);
                break;

            case R.id.txtPrivacyTerms2:
            case R.id.chkPrivacyTerms2:
                chkPrivacyTerms2.setChecked(false);
                intent = new Intent(ResetAgreementActivity.this, TermsOfServiceActivity.class);
                intent.putExtra("type", 1);
                startActivityForResult(intent, 1001);
                overridePendingTransition(R.anim.slide_down_in, R.anim.slide_none);
                break;

            case R.id.txtPrivacyTerms3:
                chkPrivacyTerms3.setChecked(!chkPrivacyTerms3.isChecked());
                break;
        }
    }

    private boolean checkValueBeforeSend() {
        boolean check = chkPrivacyTerms.isChecked();
        boolean check2 = chkPrivacyTerms2.isChecked();
        boolean check3 = chkPrivacyTerms3.isChecked();

        isAgreePersnalInfo = check;
        isAgreeServiceInfo = check2;
        isAgreeUpper14Info = check3;

        if (!check) {
            CommonUtils.showToast(ResetAgreementActivity.this, getString(R.string.RegisterInfoActivity_str_3));
        }  else if (!check2) {
            CommonUtils.showToast(ResetAgreementActivity.this, getString(R.string.RegisterInfoActivity_str_4));
        }  else if (!check3) {
            CommonUtils.showToast(ResetAgreementActivity.this, getString(R.string.RegisterInfoActivity_str_5));
        } else {
            return true;
        }
        return false;
    }

    /**
     * ikHwang 2020-12-08 오전 10:45 이용약관 재동의
     */
    private void resetAgreement(){
        try {
            String userId = SharedPrefUtil.getString(SharedPrefUtil.USER_ID, "");
            String pw = SharedPrefUtil.getString(SharedPrefUtil.USER_PASS, "");

            int agreeTerms = terms == 1 ? -1 : (isAgreePersnalInfo ? 1 : 0);
            int agreePersnal = privacyPolicy == 1 ? -1 : (isAgreeServiceInfo ? 1 : 0);
            int agree14 = over14age == 1 ? -1 : (isAgreeUpper14Info ? 1 : 0);

            displayProgress(this, "", "");
            HttpApi.PostV2SetAgreement(userId, pw, agreeTerms, agreePersnal, agree14, new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    DismissConnectDialog();
                    CommonUtils.showToast(ResetAgreementActivity.this, getString(R.string.ResetAgreementActivity_str_3));
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    try {
                        String strResponse =  response.body().string();

                        if(TextUtils.isEmpty(strResponse)){
                            CommonUtils.showToast(ResetAgreementActivity.this, getString(R.string.ResetAgreementActivity_str_3));
                        }else {
                            CommonUtils.customLog(TAG, "user/set-agreement : " + strResponse, Log.ERROR);

                            JSONObject json_data = new JSONObject(strResponse);

                            switch (json_data.optInt("code", 0)) {
                                case HttpApi.RESPONSE_SUCCESS:
                                    Intent intent;
                                    if (SharedPrefUtil.getBoolean(SharedPrefUtil.DONT_SHOW_SMARTGUIDE, false)) {
                                        if(CleanVentilationApplication.getInstance().isIsOldUser()){
                                            intent = new Intent(ResetAgreementActivity.this, MainActivity.class);
                                        }else{
                                            intent = new Intent(ResetAgreementActivity.this, MainPrismActivity.class);
                                        }

                                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                    } else {
                                        intent = new Intent(ResetAgreementActivity.this, SmartGuideActivity.class);
                                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                        intent.putExtra("MODE", SmartGuideActivity.MODE_NEW);
                                    }

                                    CommonUtils.startActivityAni(ResetAgreementActivity.this, intent, true, 0);
                                    break;

                                default:
                                    CommonUtils.showToast(ResetAgreementActivity.this, getString(R.string.ResetAgreementActivity_str_3));
                                    break;
                            }
                        }
                    }catch (Exception e){
                        e.printStackTrace();
                        CommonUtils.showToast(ResetAgreementActivity.this, getString(R.string.ResetAgreementActivity_str_3));
                    }finally {
                        DismissConnectDialog();
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
