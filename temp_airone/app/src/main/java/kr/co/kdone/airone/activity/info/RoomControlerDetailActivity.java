package kr.co.kdone.airone.activity.info;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import kr.co.kdone.airone.CleanVentilationApplication;
import kr.co.kdone.airone.R;
import kr.co.kdone.airone.activity.register.RegisterDevice2Activity;
import kr.co.kdone.airone.activity.register.prism.RegisterPrismDeviceActivity;
import kr.co.kdone.airone.utils.SharedPrefUtil;

import static kr.co.kdone.airone.utils.CommonUtils.MODE_CHANGE_AP;
import static kr.co.kdone.airone.utils.CommonUtils.MODE_DEVICE_ONLY_REG;
import static kr.co.kdone.airone.utils.CommonUtils.MODE_NONE;
import static kr.co.kdone.airone.utils.CommonUtils.RESULT_CHANGE_AP_COMFIRM;
import static kr.co.kdone.airone.utils.CommonUtils.RESULT_REGISTER_DEVICE;

/**
 * ikHwang 2020-09-07 오전 9:46 룸콘트롤러 연결 안내 화면
 * 제품별 이미지가 각각 달라 에어모니터와 화면 분리함
 */
public class RoomControlerDetailActivity extends AppCompatActivity implements View.OnClickListener {
    private final String TAG = getClass().getSimpleName();

    private Activity act;
    private int mode;

    private ImageView img_room_con1;
    private ImageView img_room_con2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_roomcontroler_detail);
        act = this;

        img_room_con1 = findViewById(R.id.img_room_con1);
        img_room_con2 = findViewById(R.id.img_room_con2);

        mode = getIntent().getIntExtra("mode", MODE_NONE);

        if(!CleanVentilationApplication.getInstance().isIsOldUser()){
            try {
                switch (CleanVentilationApplication.getInstance().getRoomControllerDevicePCD()){
                    case 7:
                        // ikHwang 2020-02-28 오전 10:51 프리즘 제습 청정 이미지
                        img_room_con1.setImageResource(R.drawable.detail_roomcon_20l_02);
                        img_room_con2.setImageResource(R.drawable.detail_roomcon_20l_03);
                        break;

                    case 9:
                        // ikHwang 2020-09-22 오전 8:44 대용량 이미지
                        img_room_con1.setImageResource(R.drawable.detail_roomcon_21d_02);
                        img_room_con2.setImageResource(R.drawable.detail_roomcon_21d_03);
                        break;

                    default:
                        // ikHwang 2020-02-28 오전 10:51 프리즘 이미지
                        img_room_con1.setImageResource(R.drawable.detail_roomcon_20d_02);
                        img_room_con2.setImageResource(R.drawable.detail_roomcon_20d_03);
                        break;
                }
            } catch (Exception e) {
                e.printStackTrace();
                // ikHwang 2020-02-28 오전 10:51 프리즘 이미지
                img_room_con1.setImageResource(R.drawable.detail_roomcon_20d_02);
                img_room_con2.setImageResource(R.drawable.detail_roomcon_20d_03);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            setResult(resultCode);
            finish();
        }
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(0, R.anim.slide_up_out);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.btnBack:
                finish();
                break;

            case R.id.layoutRoomConWifi: { //공유기 재설정
                Intent intent = null;

                if(!CleanVentilationApplication.getInstance().isIsOldUser()) {
                    intent = new Intent(act, RegisterPrismDeviceActivity.class);
                }else{ // 1,2차 사용자
                    intent = new Intent(act, RegisterDevice2Activity.class);
                }

                intent.putExtra("mode", MODE_CHANGE_AP);
                intent.putExtra("sub_mode", mode);
                startActivityForResult(intent, RESULT_CHANGE_AP_COMFIRM);
                overridePendingTransition(R.anim.slide_left_in, R.anim.slide_none);
                break;
            }

            case R.id.layoutRoomConConnect: { //룸콘트롤러 재등록(연결하기)
                Intent intent = null;

                if(!CleanVentilationApplication.getInstance().isIsOldUser()) {
                    intent = new Intent(act, RegisterPrismDeviceActivity.class);
                }else{ // 1,2차 사용자
                    intent = new Intent(act, RegisterDevice2Activity.class);
                }

                intent.putExtra("mode", MODE_DEVICE_ONLY_REG);
                intent.putExtra("sub_mode", mode);
                intent.putExtra("userID", SharedPrefUtil.getString(SharedPrefUtil.USER_ID, ""));
                intent.putExtra("password", SharedPrefUtil.getString(SharedPrefUtil.USER_PASS, ""));
                intent.putExtra("name", CleanVentilationApplication.getInstance().getUserInfo().getName());
                startActivityForResult(intent, RESULT_REGISTER_DEVICE);
                overridePendingTransition(R.anim.slide_left_in, R.anim.slide_none);
                break;
            }

            case R.id.txtCsCenter: {
                Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:1588-1144"));
                startActivity(intent);
                overridePendingTransition(R.anim.slide_down_in, R.anim.slide_none);
                break;
            }
        }
    }
}
