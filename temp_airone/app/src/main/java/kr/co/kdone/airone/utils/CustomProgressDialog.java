package kr.co.kdone.airone.utils;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import kr.co.kdone.airone.R;

/**
 * Progress bar를 Custom화 함(화면 표시 방법 변경)
 */
public class CustomProgressDialog extends Dialog {

    private TextView mTxtTitle;
    private TextView mTxtSubTitle;
    private Context mContext;

    public CustomProgressDialog(Context context) {
        super(context);
        mContext = context;
        initComponent(null, null);
    }

    public CustomProgressDialog(Context context, String title) {
        super(context);
        mContext = context;
        initComponent(title, null);
    }

    public CustomProgressDialog(Context context, String title, String subTitle) {
        super(context);
        mContext = context;
        initComponent(title, subTitle);
    }

    private void initComponent(String t, String s) {
        requestWindowFeature(Window.FEATURE_NO_TITLE); // 지저분한(?) 다이얼 로그 제목을 날림
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        setContentView(R.layout.custom_dialog); // 다이얼로그에 박을 레이아웃

//        RelativeLayout layoutMain = (RelativeLayout) findViewById(R.id.layoutMain);
//        ImageView imageView = (ImageView) findViewById(R.id.imageView);

//        if (TextUtils.isEmpty(s)) {
//            ViewGroup.LayoutParams params = imageView.getLayoutParams();
//            params.width = params.width / 2;
//            params.height = params.height / 2;
//
//            imageView.setLayoutParams(params);
//
//        }
//        Animation animation = AnimationUtils.loadAnimation(mContext, R.anim.loading_image);
//        imageView.startAnimation(animation);

        mTxtTitle = (TextView) findViewById(R.id.txtTitle);
        mTxtTitle.setVisibility(View.INVISIBLE);
        if (t != null) {
            mTxtTitle.setText(t);
        }

        mTxtSubTitle = (TextView) findViewById(R.id.txtSubTitle);
        mTxtSubTitle.setVisibility(View.INVISIBLE);
        if (s != null) {
            mTxtSubTitle.setText(s);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    public void showTitle() {
        if (mTxtTitle.length() > 0) {
            mTxtTitle.setVisibility(View.VISIBLE);
        }
    }

    public void showTitle(String str) {
        if (str != null) {
            mTxtTitle.setText(str);
            mTxtTitle.setVisibility(View.VISIBLE);
        }
    }

    public void showSubTitle() {
        if (mTxtSubTitle.length() > 0) {
            mTxtSubTitle.setVisibility(View.VISIBLE);
        }
    }

    public void showSubTitle(String str) {
        if (str != null) {
            mTxtSubTitle.setText(str);
            mTxtSubTitle.setVisibility(View.VISIBLE);
        }
    }
}
