package kr.co.kdone.airone.activity.register;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.view.View;
import android.widget.TextView;

import kr.co.kdone.airone.R;

/**
 * ikHwang 2019-06-04 오전 9:55 개인 정보 및 서비스 사용 동의 화면
 */
public class TermsOfServiceActivity extends AppCompatActivity implements View.OnClickListener{
    private final String TAG = getClass().getSimpleName();
    private int type = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_terms_of_service);

        if(getIntent().hasExtra("type")){
            type = getIntent().getIntExtra("type", 0);
        }

        if(type == 0){
            setContentView(R.layout.activity_terms_of_service);
        }else{
            setContentView(R.layout.activity_terms_of_service2);

            TextView textView = findViewById(R.id.text_terms_content);
            String contentStr = textView.getText().toString();

            String content1 = getString(R.string.terms_6);
            String content2 = getString(R.string.terms_7);

            SpannableString content = new SpannableString(contentStr);
            content.setSpan(new UnderlineSpan(), contentStr.indexOf(content1), (contentStr.indexOf(content1) + getString(R.string.terms_6).length()),0);
            content.setSpan(new UnderlineSpan(), contentStr.indexOf(content2), (contentStr.indexOf(content2) + getString(R.string.terms_7).length()),0);
            textView.setText(content);
        }
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent();
        intent.putExtra("type", type);

        setResult(Activity.RESULT_OK, intent);
        finish();
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(0, R.anim.slide_up_out);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnBack:
                onBackPressed();
                break;
        }
    }
}
