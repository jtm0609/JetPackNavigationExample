package kr.co.kdone.airone.activity.more;

import android.content.Intent;
import android.os.Bundle;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ViewFlipper;

import kr.co.kdone.airone.CleanVentilationApplication;
import kr.co.kdone.airone.R;
import kr.co.kdone.airone.activity.main.MainActivity;
import kr.co.kdone.airone.activity.main.MainPrismActivity;
import kr.co.kdone.airone.utils.SharedPrefUtil;

import static kr.co.kdone.airone.utils.CommonUtils.isUserInfoSection;
import static kr.co.kdone.airone.utils.CommonUtils.startActivityAni;

/**
 * ikHwang 2019-06-04 오전 9:54 스마트 가이드 화면
 */
public class SmartGuideActivity extends AppCompatActivity implements View.OnClickListener {
    private String TAG = getClass().getSimpleName();

    public static final int MODE_NEW = 0;
    public static final int MODE_MORE = 1;
    private int mMode;

    private final int MAIN_HELP_PAGE_COUNT = 4;
    private int mPageCount;

    private boolean mNoShow;
    private ImageView mImgNoShow;

    private LinearLayout mLayoutButtons;
    private LinearLayout mLayoutActionBar;

    private ViewPager viewPager;
    private View indicator1;
    private View indicator2;
    private View indicator3;
    private View indicator4;
    private View indicator5;

    private ViewFlipper mViewFlipper;
    private int m_nPreTouchPosX = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_smart_guide);
        isUserInfoSection(this);

        mMode = getIntent().getIntExtra("MODE", MODE_NEW);
        mLayoutActionBar = findViewById(R.id.layoutActionBar);
        mLayoutButtons = findViewById(R.id.linearLayout);

        mLayoutButtons.setVisibility(View.GONE);
        mLayoutActionBar.setVisibility(View.VISIBLE);

        mImgNoShow = findViewById(R.id.imgNoShow);
        mNoShow = false;

        mPageCount = MAIN_HELP_PAGE_COUNT;
        indicator1 = findViewById(R.id.indicator1);
        indicator2 = findViewById(R.id.indicator2);
        indicator3 = findViewById(R.id.indicator3);
        indicator4 = findViewById(R.id.indicator4);
        indicator5 = findViewById(R.id.indicator5);
        mViewFlipper = findViewById(R.id.viewFlipper);

        changeHelpPage();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnBack:
                onBackPressed();
                break;

            case R.id.btnClose:
                SharedPrefUtil.putBoolean(SharedPrefUtil.DONT_SHOW_SMARTGUIDE, true);
                Intent intent;
                if(CleanVentilationApplication.getInstance().isIsOldUser()){
                    intent = new Intent(v.getContext(), MainActivity.class);
                }else{
                    intent = new Intent(v.getContext(), MainPrismActivity.class);
                }

                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivityAni(SmartGuideActivity.this, intent, true, 0);
                break;

            case R.id.layoutNoShow:
                mNoShow = !mNoShow;
                if (mNoShow) {
                    mImgNoShow.setImageResource(R.drawable.chkbox_smartguide_chked);
                } else {
                    mImgNoShow.setImageResource(R.drawable.chkbox_smartguide);
                }
                break;

            case R.id.layoutClose:
//                if (mNoShow) {
//                    SharedPrefUtil.putBoolean(SharedPrefUtil.DONT_SHOW_SMARTGUIDE, mNoShow);
//                }
//                Intent intent = new Intent(v.getContext(), MainPrismActivity.class);
//                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
//                startActivity(intent);
//                finish();
                break;

            case R.id.imgPrev:
                if (mViewFlipper.getDisplayedChild() > 0)
                    MovewPreviousView();
                break;

            case R.id.imgNext:
                if (mViewFlipper.getDisplayedChild() < mViewFlipper.getChildCount() - 1)
                    MoveNextView();
                break;

            default:
                break;
        }
    }

    private void changeHelpPage() {
        /**
         * ViewFlipper에 도움말 페이지 추가.
         */
        mViewFlipper.removeAllViews();
        int main_img[] = {R.drawable.smartguide_img01, R.drawable.smartguide_img02, R.drawable.smartguide_img03, R.drawable.smartguide_img04};
        int main_txtTitle[] = {R.string.title_guide01, R.string.title_guide03, R.string.title_guide04, R.string.title_guide05};
        int main_txtSubTitle[] = {R.string.subtitle_guide01, R.string.subtitle_guide03, R.string.subtitle_guide04, R.string.subtitle_guide05};
        for (int i = 0; i < mPageCount; i++) {
            View layoutGuide = LayoutInflater.from(this).inflate(R.layout.layout_smartguide, null);
            ((ImageView) layoutGuide.findViewById(R.id.img)).setImageResource(main_img[i]);
            ((TextView) layoutGuide.findViewById(R.id.txtTitle)).setText(main_txtTitle[i]);
            ((TextView) layoutGuide.findViewById(R.id.txtSubTitle)).setText(main_txtSubTitle[i]);
            mViewFlipper.addView(layoutGuide);
        }

        mViewFlipper.setOnTouchListener(MyTouchListener);
        updateIndicators(mViewFlipper.getDisplayedChild());
    }

    /**
     * 다음 페이지 변경 함수
     */
    private void MoveNextView() {
        mViewFlipper.setInAnimation(AnimationUtils.loadAnimation(this, R.anim.appear_from_right));
        mViewFlipper.setOutAnimation(AnimationUtils.loadAnimation(this, R.anim.disappear_to_left));
        mViewFlipper.showNext();
        updateIndicators(mViewFlipper.getDisplayedChild());
    }

    /**
     * 이전 페이지 변경 함수
     */
    private void MovewPreviousView() {
        mViewFlipper.setInAnimation(AnimationUtils.loadAnimation(this, R.anim.appear_from_left));
        mViewFlipper.setOutAnimation(AnimationUtils.loadAnimation(this, R.anim.disappear_to_right));
        mViewFlipper.showPrevious();
        updateIndicators(mViewFlipper.getDisplayedChild());
    }

    /**
     * 화면 터치 이벤트 Listener 추가.
     */
    View.OnTouchListener MyTouchListener = new View.OnTouchListener() {
        public boolean onTouch(View v, MotionEvent event) {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                m_nPreTouchPosX = (int) event.getX();
            }
            if (event.getAction() == MotionEvent.ACTION_UP) {
                int nTouchPosX = (int) event.getX();
                if (nTouchPosX < m_nPreTouchPosX) {
                    if (mViewFlipper.getDisplayedChild() < mViewFlipper.getChildCount() - 1)
                        MoveNextView();
                } else if (nTouchPosX > m_nPreTouchPosX) {
                    if (mViewFlipper.getDisplayedChild() > 0)
                        MovewPreviousView();
                }
                m_nPreTouchPosX = nTouchPosX;
            }
            return true;
        }

    };

    /**
     * 인디게이트 상태 갱신 함수
     *
     * @param position
     */
    public void updateIndicators(int position) {
        switch (position) {
            case 0:
                indicator1.setBackgroundResource(R.drawable.paging_current);
                indicator2.setBackgroundResource(R.drawable.paging_default);
                indicator3.setBackgroundResource(R.drawable.paging_default);
                indicator4.setBackgroundResource(R.drawable.paging_default);
                indicator5.setBackgroundResource(R.drawable.paging_default);
                break;

            case 1:
                indicator1.setBackgroundResource(R.drawable.paging_default);
                indicator2.setBackgroundResource(R.drawable.paging_current);
                indicator3.setBackgroundResource(R.drawable.paging_default);
                indicator4.setBackgroundResource(R.drawable.paging_default);
                indicator5.setBackgroundResource(R.drawable.paging_default);
                break;

            case 2:
                indicator1.setBackgroundResource(R.drawable.paging_default);
                indicator2.setBackgroundResource(R.drawable.paging_default);
                indicator3.setBackgroundResource(R.drawable.paging_current);
                indicator4.setBackgroundResource(R.drawable.paging_default);
                indicator5.setBackgroundResource(R.drawable.paging_default);
                break;

            case 3:
                indicator1.setBackgroundResource(R.drawable.paging_default);
                indicator2.setBackgroundResource(R.drawable.paging_default);
                indicator3.setBackgroundResource(R.drawable.paging_default);
                indicator4.setBackgroundResource(R.drawable.paging_current);
                indicator5.setBackgroundResource(R.drawable.paging_default);
                break;

            case 4:
                indicator1.setBackgroundResource(R.drawable.paging_default);
                indicator2.setBackgroundResource(R.drawable.paging_default);
                indicator3.setBackgroundResource(R.drawable.paging_default);
                indicator4.setBackgroundResource(R.drawable.paging_default);
                indicator5.setBackgroundResource(R.drawable.paging_current);
                break;
        }
    }
}