package kr.co.kdone.airone.activity.main;

import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import androidx.annotation.NonNull;
import android.view.View;
import android.view.Window;
import android.widget.LinearLayout;

import kr.co.kdone.airone.R;

/**
 * ikHwang 2019-06-04 오전 9:46 공기 및 날씨 정보 조회 안내 팝업
 */
public class HomeNotiPopupActivity extends Dialog{
    private final String TAG = getClass().getSimpleName();

    public HomeNotiPopupActivity(@NonNull Context context) {
        super(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE); //타이틀 바 삭제
        getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

        setContentView(R.layout.dialog_home_noti);

        LinearLayout contentsMain = findViewById(R.id.contentsMain);
        contentsMain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
    }
}
