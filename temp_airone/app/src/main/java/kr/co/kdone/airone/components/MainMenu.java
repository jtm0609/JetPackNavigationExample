package kr.co.kdone.airone.components;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Color;
import androidx.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Timer;
import java.util.TimerTask;

import kr.co.kdone.airone.CleanVentilationApplication;
import kr.co.kdone.airone.activity.TipAndEventActivity;
import kr.co.kdone.airone.activity.control.ControlActivity;
import kr.co.kdone.airone.activity.LifeReportActivity;
import kr.co.kdone.airone.activity.main.MainActivity;
import kr.co.kdone.airone.activity.main.MainPrismActivity;
import kr.co.kdone.airone.R;
import kr.co.kdone.airone.activity.SmartAlarmActivity;
import kr.co.kdone.airone.utils.CommonUtils;

import static kr.co.kdone.airone.utils.CommonUtils.MAIN_MENU_CCONTROL;
import static kr.co.kdone.airone.utils.CommonUtils.MAIN_MENU_HOME;
import static kr.co.kdone.airone.utils.CommonUtils.MAIN_MENU_LIFEREPORT;
import static kr.co.kdone.airone.utils.CommonUtils.MAIN_MENU_MORE;
import static kr.co.kdone.airone.utils.CommonUtils.MAIN_MENU_SMARTALARM;
import static kr.co.kdone.airone.utils.CommonUtils.setDisableViewFew;

/**
 * 메인 메뉴 컴퍼넌트.
 */
public class MainMenu extends LinearLayout {

    private String TAG = MainMenu.class.getSimpleName();
    private static Class<?> mOldActivity = null;

    private int mSelectedIndex = 0;
    private Context mContext;
    private static boolean mIsStartNextActivity = false;

    public MainMenu(Context context) {
        this(context,null);
        mContext = context;
    }

    public MainMenu(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        mContext = context;

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.MainMenu, 0, 0);
        mSelectedIndex = a.getInteger(R.styleable.MainMenu_selectedIndex,0);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.main_menu, this, true);

        if(CleanVentilationApplication.getInstance().isIsOldUser()){
            ((TextView) findViewById(R.id.txtMenu04)).setText(context.getString(R.string.main_menu_str_4));
        }else{
            ((TextView) findViewById(R.id.txtMenu04)).setText(context.getString(R.string.main_menu_str_6));
        }

        initComponents(mSelectedIndex);
        showNewIcon();
    }

    private void initComponents(int index) {
        switch (mSelectedIndex) {
            case 0:
            default:
                ((ImageView) findViewById(R.id.imgMenu01)).setImageResource(R.drawable.icon_tab01_active);
                ((ImageView) findViewById(R.id.imgMenu02)).setImageResource(R.drawable.icon_tab02_normal);
                ((ImageView) findViewById(R.id.imgMenu03)).setImageResource(R.drawable.icon_tab03_normal);
                ((ImageView) findViewById(R.id.imgMenu04)).setImageResource(R.drawable.icon_tab04_normal);
                ((ImageView) findViewById(R.id.imgMenu05)).setImageResource(R.drawable.icon_tab05_normal);
                ((TextView) findViewById(R.id.txtMenu01)).setTextColor(Color.rgb(0xf4, 0x9c, 0x00));
                ((TextView) findViewById(R.id.txtMenu02)).setTextColor(Color.rgb(0x66, 0x66, 0x66));
                ((TextView) findViewById(R.id.txtMenu03)).setTextColor(Color.rgb(0x66, 0x66, 0x66));
                ((TextView) findViewById(R.id.txtMenu04)).setTextColor(Color.rgb(0x66, 0x66, 0x66));
                ((TextView) findViewById(R.id.txtMenu05)).setTextColor(Color.rgb(0x66, 0x66, 0x66));
                break;

            case 1:
                ((ImageView) findViewById(R.id.imgMenu01)).setImageResource(R.drawable.icon_tab01_normal);
                ((ImageView) findViewById(R.id.imgMenu02)).setImageResource(R.drawable.icon_tab02_active);
                ((ImageView) findViewById(R.id.imgMenu03)).setImageResource(R.drawable.icon_tab03_normal);
                ((ImageView) findViewById(R.id.imgMenu04)).setImageResource(R.drawable.icon_tab04_normal);
                ((ImageView) findViewById(R.id.imgMenu05)).setImageResource(R.drawable.icon_tab05_normal);
                ((TextView) findViewById(R.id.txtMenu01)).setTextColor(Color.rgb(0x66, 0x66, 0x66));
                ((TextView) findViewById(R.id.txtMenu02)).setTextColor(Color.rgb(0xf4, 0x9c, 0x00));
                ((TextView) findViewById(R.id.txtMenu03)).setTextColor(Color.rgb(0x66, 0x66, 0x66));
                ((TextView) findViewById(R.id.txtMenu04)).setTextColor(Color.rgb(0x66, 0x66, 0x66));
                ((TextView) findViewById(R.id.txtMenu05)).setTextColor(Color.rgb(0x66, 0x66, 0x66));
                break;

            case 2:
                ((ImageView) findViewById(R.id.imgMenu01)).setImageResource(R.drawable.icon_tab01_normal);
                ((ImageView) findViewById(R.id.imgMenu02)).setImageResource(R.drawable.icon_tab02_normal);
                ((ImageView) findViewById(R.id.imgMenu03)).setImageResource(R.drawable.icon_tab03_active);
                ((ImageView) findViewById(R.id.imgMenu04)).setImageResource(R.drawable.icon_tab04_normal);
                ((ImageView) findViewById(R.id.imgMenu05)).setImageResource(R.drawable.icon_tab05_normal);
                ((TextView) findViewById(R.id.txtMenu01)).setTextColor(Color.rgb(0x66, 0x66, 0x66));
                ((TextView) findViewById(R.id.txtMenu02)).setTextColor(Color.rgb(0x66, 0x66, 0x66));
                ((TextView) findViewById(R.id.txtMenu03)).setTextColor(Color.rgb(0xf4, 0x9c, 0x00));
                ((TextView) findViewById(R.id.txtMenu04)).setTextColor(Color.rgb(0x66, 0x66, 0x66));
                ((TextView) findViewById(R.id.txtMenu05)).setTextColor(Color.rgb(0x66, 0x66, 0x66));
                break;

            case 3:
                ((ImageView) findViewById(R.id.imgMenu01)).setImageResource(R.drawable.icon_tab01_normal);
                ((ImageView) findViewById(R.id.imgMenu02)).setImageResource(R.drawable.icon_tab02_normal);
                ((ImageView) findViewById(R.id.imgMenu03)).setImageResource(R.drawable.icon_tab03_normal);
                ((ImageView) findViewById(R.id.imgMenu04)).setImageResource(R.drawable.icon_tab04_active);
                ((ImageView) findViewById(R.id.imgMenu05)).setImageResource(R.drawable.icon_tab05_normal);
                ((TextView) findViewById(R.id.txtMenu01)).setTextColor(Color.rgb(0x66, 0x66, 0x66));
                ((TextView) findViewById(R.id.txtMenu02)).setTextColor(Color.rgb(0x66, 0x66, 0x66));
                ((TextView) findViewById(R.id.txtMenu03)).setTextColor(Color.rgb(0x66, 0x66, 0x66));
                ((TextView) findViewById(R.id.txtMenu04)).setTextColor(Color.rgb(0xf4, 0x9c, 0x00));
                ((TextView) findViewById(R.id.txtMenu05)).setTextColor(Color.rgb(0x66, 0x66, 0x66));
                break;

            case 4:
                ((ImageView) findViewById(R.id.imgMenu01)).setImageResource(R.drawable.icon_tab01_normal);
                ((ImageView) findViewById(R.id.imgMenu02)).setImageResource(R.drawable.icon_tab02_normal);
                ((ImageView) findViewById(R.id.imgMenu03)).setImageResource(R.drawable.icon_tab03_normal);
                ((ImageView) findViewById(R.id.imgMenu04)).setImageResource(R.drawable.icon_tab04_normal);
                ((ImageView) findViewById(R.id.imgMenu05)).setImageResource(R.drawable.icon_tab05_active);
                ((TextView) findViewById(R.id.txtMenu01)).setTextColor(Color.rgb(0x66, 0x66, 0x66));
                ((TextView) findViewById(R.id.txtMenu02)).setTextColor(Color.rgb(0x66, 0x66, 0x66));
                ((TextView) findViewById(R.id.txtMenu03)).setTextColor(Color.rgb(0x66, 0x66, 0x66));
                ((TextView) findViewById(R.id.txtMenu04)).setTextColor(Color.rgb(0x66, 0x66, 0x66));
                ((TextView) findViewById(R.id.txtMenu05)).setTextColor(Color.rgb(0xf4, 0x9c, 0x00));
                break;
        }

        findViewById(R.id.layoutMenu01).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
//                if(SharedPrefUtil.getString(SharedPrefUtil.CURRENT_CONTROL_DEVICE_ID,"").length()==0){
//                    Toast.makeText(v.getContext(),v.getContext().getString(R.string.toast_noexist_control_device),Toast.LENGTH_SHORT).show();
//                } else {
                    setDisableViewFew(v);
                    changeActivityForMainMenu(CommonUtils.MAIN_MENU_HOME, getContext());
//                }
            }
        });

        findViewById(R.id.layoutMenu02).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
//                if(SharedPrefUtil.getString(SharedPrefUtil.CURRENT_CONTROL_DEVICE_ID,"").length()==0){
//                    Toast.makeText(v.getContext(),v.getContext().getString(R.string.toast_noexist_control_device),Toast.LENGTH_SHORT).show();
//                } else {
                    setDisableViewFew(v);
                    changeActivityForMainMenu(CommonUtils.MAIN_MENU_CCONTROL, getContext());
//                }
            }
        });

        findViewById(R.id.layoutMenu03).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
//                if(SharedPrefUtil.getString(SharedPrefUtil.CURRENT_CONTROL_DEVICE_ID,"").length()==0){
//                    Toast.makeText(v.getContext(),v.getContext().getString(R.string.toast_noexist_control_device),Toast.LENGTH_SHORT).show();
//                } else {
                    setDisableViewFew(v);
                    changeActivityForMainMenu(CommonUtils.MAIN_MENU_SMARTALARM, getContext());
//                }
            }
        });

        findViewById(R.id.layoutMenu04).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
//                if(SharedPrefUtil.getString(SharedPrefUtil.CURRENT_CONTROL_DEVICE_ID,"").length()==0){
//                    Toast.makeText(v.getContext(),v.getContext().getString(R.string.toast_noexist_control_device),Toast.LENGTH_SHORT).show();
//                } else {
                    setDisableViewFew(v);
                    changeActivityForMainMenu(CommonUtils.MAIN_MENU_LIFEREPORT, getContext());
//                }
            }
        });

        findViewById(R.id.layoutMenu05).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                setDisableViewFew(v);
                changeActivityForMainMenu(MAIN_MENU_MORE, getContext());
            }
        });
    }

    private void changeActivityForMainMenu(int index, final Context context){
        if(mSelectedIndex == index || mIsStartNextActivity){
            return;
        }

        mIsStartNextActivity = true;
        mOldActivity = ((Activity)context).getClass();

        Intent intent;
        switch (index) {
            case MAIN_MENU_HOME:
            default:
                if(CleanVentilationApplication.getInstance().isIsOldUser()){
                    intent = new Intent(context, MainActivity.class);
                }else{
                    intent = new Intent(context, MainPrismActivity.class);
                }
                break;

            case MAIN_MENU_CCONTROL:
                intent = new Intent(context, ControlActivity.class);
                break;

            case MAIN_MENU_SMARTALARM:
                try {
                    CleanVentilationApplication.getInstance().getUserInfo().setIsNewSmart(0);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                intent = new Intent(context, SmartAlarmActivity.class);
                break;

            case MAIN_MENU_LIFEREPORT:
                intent = new Intent(context, LifeReportActivity.class);
                break;

            case MAIN_MENU_MORE:
                intent = new Intent(context, TipAndEventActivity.class);
                break;
        }

        CleanVentilationApplication.setAnyType(true);

        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        context.startActivity(intent);

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                mIsStartNextActivity = false;
            }
        }, 500);
    }

    private void showNewIcon(){
        ((ImageView)findViewById(R.id.imgMenuNew01)).setVisibility(INVISIBLE);
        ((ImageView)findViewById(R.id.imgMenuNew02)).setVisibility(INVISIBLE);

        if(CleanVentilationApplication.getInstance().getUserInfo().getIsNewSmart() > 0){
            ((ImageView)findViewById(R.id.imgMenuNew03)).setVisibility(VISIBLE);
        }else{
            ((ImageView)findViewById(R.id.imgMenuNew03)).setVisibility(INVISIBLE);
        }

        ((ImageView)findViewById(R.id.imgMenuNew04)).setVisibility(INVISIBLE);

        if(CleanVentilationApplication.getInstance().getUserInfo().getIsNewNotice() > 0){
            ((ImageView)findViewById(R.id.imgMenuNew05)).setVisibility(VISIBLE);
        }else{
            ((ImageView)findViewById(R.id.imgMenuNew05)).setVisibility(INVISIBLE);
        }
    }

    public void gotoOldActivity() {
        if(mOldActivity != null) {
            Intent intent;

            if(CleanVentilationApplication.getInstance().isIsOldUser()){
                intent = new Intent(mContext, MainActivity.class);
            }else{
                intent = new Intent(mContext, MainPrismActivity.class);
            }

            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            mContext.startActivity(intent);
        }
    }
}


