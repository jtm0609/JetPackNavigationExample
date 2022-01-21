package kr.co.kdone.airone.activity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;

import kr.co.kdone.airone.R;
import libs.espressif.utils.TextUtils;

import static kr.co.kdone.airone.utils.CommonUtils.isUserInfoSection;

/**
 * ikHwang 2019-07-10 오후 3:20 팁&이벤트
 */
public class TipAndEventDetailActivity extends AppCompatActivity implements View.OnClickListener {
    private String TAG = this.getClass().getSimpleName();

    private TextView txtTitle;
    private WebView mWebView;

    private String strUrl = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tip_and_event_detail);

        isUserInfoSection(this);

        if(getIntent().hasExtra("url")){
            strUrl = getIntent().getStringExtra("url");
        }

        if(TextUtils.isEmpty(strUrl)){
            finish();

            return;
        }

        setLayoutInit();
        initWebView();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mWebView.loadUrl(strUrl);
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(0, R.anim.slide_up_out);
    }

    private void setLayoutInit() {
        mWebView = findViewById(R.id.webview);
        txtTitle = findViewById(R.id.txtTitle);
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void initWebView() {
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.getSettings().setSavePassword(false);
        mWebView.getSettings().setSaveFormData(false);
        mWebView.getSettings().setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
        mWebView.getSettings().setAppCacheEnabled(true);

        mWebView.setWebViewClient(new KDWebViewClient());
        mWebView.setWebChromeClient(new KDWebChromeClient());

        mWebView.setVerticalScrollBarEnabled(true);
        mWebView.setVerticalScrollbarOverlay(true);
        mWebView.setHorizontalScrollBarEnabled(true);
        mWebView.setHorizontalScrollbarOverlay(true);
        mWebView.setScrollbarFadingEnabled(true);

        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.O) {
            mWebView.getSettings().setRenderPriority(WebSettings.RenderPriority.HIGH);
            mWebView.getSettings().setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
            mWebView.getSettings().setEnableSmoothTransition(true);
        }

        if(Build.VERSION_CODES.LOLLIPOP <= Build.VERSION.SDK_INT){
            mWebView.getSettings().setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnBack: // 뒤로가기
                finish();
                break;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    private class KDWebViewClient extends WebViewClient{
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);

            return true;
        }

        @Override
        public void onReceivedSslError(WebView view, final SslErrorHandler handler, SslError error) {
            String msg = "";
            switch(error.getPrimaryError()){
                case SslError.SSL_EXPIRED:
                case SslError.SSL_IDMISMATCH:
                case SslError.SSL_NOTYETVALID:
                case SslError.SSL_UNTRUSTED:
                    msg = getString(R.string.ssl_msg_1);
                    break;
                default:
                    msg = getString(R.string.ssl_msg_2);
                    break;
            }

            new AlertDialog.Builder(TipAndEventDetailActivity.this)
                    .setTitle(R.string.app_name)
                    .setMessage(msg)
                    .setPositiveButton(R.string.activity_tip_and_event_str_2, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            handler.proceed();
                        }
                    })
                    .setNegativeButton(R.string.activity_tip_and_event_str_3, new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            handler.cancel();
                        }
                    })
                    .setCancelable(false).show();
        }
    }

    private class KDWebChromeClient extends WebChromeClient{
        @Override
        public void onReceivedTitle(WebView view, String title) {
            super.onReceivedTitle(view, title);

            txtTitle.setText(view.getTitle());
        }
    }
}
