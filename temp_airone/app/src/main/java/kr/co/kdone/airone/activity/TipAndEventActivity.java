package kr.co.kdone.airone.activity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.webkit.SslErrorHandler;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import kr.co.kdone.airone.CleanVentilationApplication;
import kr.co.kdone.airone.R;
import kr.co.kdone.airone.activity.more.MoreActivity;
import kr.co.kdone.airone.activity.more.MoreHelpActivity;
import kr.co.kdone.airone.utils.CommonUtils;

import static kr.co.kdone.airone.utils.CommonUtils.initiateTwoButtonAlert;
import static kr.co.kdone.airone.utils.CommonUtils.isUserInfoSection;
import static kr.co.kdone.airone.utils.CommonUtils.startActivityAni;

/**
 * ikHwang 2019-07-10 오후 3:20 팁&이벤트
 */
public class TipAndEventActivity extends AppCompatActivity implements View.OnClickListener {
    private String TAG = this.getClass().getSimpleName();

    private WebView mWebView;
    private boolean isFragDetail = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tip_and_event);

        isUserInfoSection(this);

        setLayoutInit();
        initWebView();

        if(!isFragDetail){
            mWebView.loadUrl("http://event.navienairone.com:8095/");
        }

        isFragDetail = false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(CleanVentilationApplication.isAnyType()) overridePendingTransition(R.anim.slide_none, 0);
        CleanVentilationApplication.setAnyType(false);

        /*if(!isFragDetail){
            mWebView.loadUrl("http://event.navienairone.com:8095/");
        }

        isFragDetail = false;*/
    }

    private void setLayoutInit() {
        mWebView = findViewById(R.id.webview);
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void initWebView() {
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.getSettings().setSavePassword(false);
        mWebView.getSettings().setSaveFormData(false);
        mWebView.getSettings().setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
        mWebView.getSettings().setAppCacheEnabled(true);

        mWebView.setWebViewClient(new KDWebViewClient());
//        mWebView.setWebChromeClient(new WebChromeClient());

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
            case R.id.btnMore: // 더보기 (설정)
                Intent intentMore = new Intent(this, MoreActivity.class);
                intentMore.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivityAni(TipAndEventActivity.this, intentMore, false, 1);
                break;

            case R.id.btnHelp: // 공기질 정보 화면
                Intent intentMoreHelp = new Intent(this, MoreHelpActivity.class);
                startActivityAni(TipAndEventActivity.this, intentMoreHelp, false, 1);
                break;
        }
    }

    @Override
    public void onBackPressed() {
        initiateTwoButtonAlert(this, getString(R.string.exit_app), getString(R.string.confirm), getString(R.string.no));
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    private class KDWebViewClient extends WebViewClient{
        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);

            CommonUtils.customLog(TAG, "onPageStarted", Log.ERROR);
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            if(url.startsWith("kdnavien")){
                Intent intent = new Intent(TipAndEventActivity.this, TipAndEventDetailActivity.class);
                intent.putExtra("url", url.replaceFirst("kdnavien:", ""));
                startActivityAni(TipAndEventActivity.this, intent, false, 1);
                isFragDetail = true;
            }else{
                view.loadUrl(url);
            }

            return true;
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            CommonUtils.customLog(TAG, "onPageFinished", Log.ERROR);
        }

        @Override
        public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
            super.onReceivedError(view, request, error);
            CommonUtils.customLog(TAG, "onReceivedError", Log.ERROR);
        }

        @Override
        public void onReceivedHttpError(WebView view, WebResourceRequest request, WebResourceResponse errorResponse) {
            super.onReceivedHttpError(view, request, errorResponse);
            CommonUtils.customLog(TAG, "onReceivedHttpError", Log.ERROR);
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

            new AlertDialog.Builder(TipAndEventActivity.this)
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
}
