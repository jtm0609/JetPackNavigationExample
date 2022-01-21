package kr.co.kdone.airone.activity.more;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import kr.co.kdone.airone.CleanVentilationApplication;
import kr.co.kdone.airone.activity.PopupActivity;
import kr.co.kdone.airone.R;
import kr.co.kdone.airone.activity.SplashActivity;
import kr.co.kdone.airone.activity.login.LoginActivity;
import kr.co.kdone.airone.utils.CommonUtils;
import kr.co.kdone.airone.utils.HttpApi;
import kr.co.kdone.airone.utils.SharedPrefUtil;
import kr.co.kdone.airone.widget.WidgetUtils;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

import static kr.co.kdone.airone.utils.CommonUtils.DismissConnectDialog;
import static kr.co.kdone.airone.utils.CommonUtils.MODE_MODIFY_NAME;
import static kr.co.kdone.airone.utils.CommonUtils.MODE_MODIFY_PASSWORD;
import static kr.co.kdone.airone.utils.CommonUtils.MODE_MODIFY_PHONE;
import static kr.co.kdone.airone.utils.CommonUtils.RESULT_LOGOUT_COMFIRM;
import static kr.co.kdone.airone.utils.CommonUtils.RESULT_WITHDRAW_CONFIRM;
import static kr.co.kdone.airone.utils.CommonUtils.RESULT_WITHDRAW_SUCCESS;
import static kr.co.kdone.airone.utils.CommonUtils.displayProgress;
import static kr.co.kdone.airone.utils.CommonUtils.isUserInfoSection;
import static kr.co.kdone.airone.utils.CommonUtils.startActivityAni;

/**
 * ikHwang 2019-06-04 오전 9:20 사용자 설정 화면
 */
public class MoreSettingActivity extends AppCompatActivity implements View.OnClickListener {
    private final String TAG = getClass().getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_more_setting);
        isUserInfoSection(this);

        ((ToggleButton) findViewById(R.id.btnOnOff)).setChecked(SharedPrefUtil.getBoolean(SharedPrefUtil.AUTO_LOGIN, false));
        ((ToggleButton) findViewById(R.id.btnOnOff)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPrefUtil.putBoolean(SharedPrefUtil.AUTO_LOGIN, isChecked);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        ((TextView) findViewById(R.id.txtUserName)).setText(CleanVentilationApplication.getInstance().getUserInfo().getName());
        ((TextView) findViewById(R.id.txtUserPhone)).setText(CleanVentilationApplication.getInstance().getUserInfo().getMobile());
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnBack:
                onBackPressed();
                break;

            case R.id.layoutPassword: {
                Intent intent = new Intent(this, ModifyUserActivity.class);
                intent.putExtra("mode", MODE_MODIFY_PASSWORD);
                startActivityAni(MoreSettingActivity.this, intent, false, 0);
                break;
            }

            case R.id.layoutUserName: {
                Intent intent = new Intent(this, ModifyUserActivity.class);
                intent.putExtra("mode", MODE_MODIFY_NAME);
                startActivityAni(MoreSettingActivity.this, intent, false, 0);
                break;
            }

            case R.id.layoutUserPhone: {
                Intent intent = new Intent(this, ModifyUserActivity.class);
                intent.putExtra("mode", MODE_MODIFY_PHONE);
                startActivityAni(MoreSettingActivity.this, intent, false, 0);
                break;
            }

            case R.id.layoutLogout: {
                Intent intent = new Intent(v.getContext(), PopupActivity.class);
                intent.putExtra(PopupActivity.POPUP_MODE, PopupActivity.MODE_YESNO);
                intent.putExtra(PopupActivity.POPUP_TITLE_TEXT, getString(R.string.delete_device));
                intent.putExtra(PopupActivity.POPUP_BODY_TEXT, getString(R.string.do_logout));
                intent.putExtra(PopupActivity.POPUP_CONFIRM_BUTTON_TEXT, getString(R.string.logout));
                startActivityForResult(intent, RESULT_LOGOUT_COMFIRM);
                overridePendingTransition(0, 0);
                break;
            }

            case R.id.layoutUnSign: {
                Intent intent = new Intent(this, PopupActivity.class);
                intent.putExtra(PopupActivity.POPUP_MODE, PopupActivity.MODE_YESNO);
                intent.putExtra(PopupActivity.POPUP_BODY_TEXT, getString(R.string.withdraw_popup_confirm));
                intent.putExtra(PopupActivity.POPUP_CONFIRM_BUTTON_TEXT, getString(R.string.withdraw));
                intent.putExtra(PopupActivity.POPUP_CANCEL_BUTTON_TEXT, getString(R.string.cancel));
                startActivityForResult(intent, RESULT_WITHDRAW_CONFIRM);
                overridePendingTransition(0, 0);
                break;
            }

            default:
                break;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(0, R.anim.slide_right_out);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case RESULT_LOGOUT_COMFIRM:
                if (resultCode == RESULT_OK) {
                    //V2A 작업. 로그아웃 api 호출부 추가.
                    displayProgress(this, "", "");
                    String userId = SharedPrefUtil.getString(SharedPrefUtil.USER_ID, "");
                    String pushToken = SharedPrefUtil.getString(SharedPrefUtil.FCMKEY, "");
                    try {
                        HttpApi.PostV2UserLogout(userId, pushToken, new Callback() {
                            @Override
                            public void onFailure(Call call, IOException e) {
                                DismissConnectDialog();
                                CommonUtils.showToast(MoreSettingActivity.this, getString(R.string.toast_result_can_not_deleted));
                            }

                            @Override
                            public void onResponse(Call call, Response response) throws IOException {

                                try {
                                    String strResponse =  response.body().string();

                                    CommonUtils.customLog(TAG, "user/sign-out : " + strResponse, Log.ERROR);

                                    JSONObject json_data = new JSONObject(strResponse);

                                    switch (json_data.optInt("code", 0)) {
                                        case HttpApi.RESPONSE_SUCCESS:
                                            SharedPrefUtil.putString(SharedPrefUtil.USER_ID, "");
                                            SharedPrefUtil.putString(SharedPrefUtil.USER_PASS, "");
                                            SharedPrefUtil.putString(SharedPrefUtil.ROOM_CONTROLLER_ID, "");

                                            FirebaseMessaging.getInstance().deleteToken();

                                            // aws mqtt 제어 종료 및 연결 종료
                                            CleanVentilationApplication.getInstance().awsRemoteFinish();
                                            CleanVentilationApplication.getInstance().awsMqttDisConnect();

                                            // ikHwang 2020-01-16 오전 11:41 로그아웃시 홈화면 정보 초기화
                                            CleanVentilationApplication.getInstance().clearHomeInfo();

                                            Intent sendIntent = new Intent(WidgetUtils.WIDGET_REPLACE_ACTION);
                                            sendBroadcast(sendIntent);

                                            Intent intent = new Intent(MoreSettingActivity.this, LoginActivity.class);
                                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                            startActivityAni(MoreSettingActivity.this, intent, true, 2);
                                            break;

                                        default:
                                            DismissConnectDialog();
                                            CommonUtils.showToast(MoreSettingActivity.this, getString(R.string.toast_logout_fail));
                                            break;
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    DismissConnectDialog();
                                    CommonUtils.showToast(MoreSettingActivity.this, getString(R.string.toast_logout_fail));
                                }
                            }
                        });
                    }catch (Exception e){
                        e.printStackTrace();
                        DismissConnectDialog();
                        CommonUtils.showToast(MoreSettingActivity.this, getString(R.string.toast_result_can_not_deleted));
                    }
                }
                break;
            case RESULT_WITHDRAW_CONFIRM:
                if (resultCode == Activity.RESULT_OK) {
                    displayProgress(this, "", "");
                    try {
                        HttpApi.PostV2UserWithdrawal( //V2A 적용
                                SharedPrefUtil.getString(SharedPrefUtil.USER_ID, ""),
                                SharedPrefUtil.getString(SharedPrefUtil.USER_PASS, ""),
                                new Callback() {
                                    @Override
                                    public void onFailure(Call call, IOException e) {
                                        DismissConnectDialog();
                                        CommonUtils.showToast(MoreSettingActivity.this, getString(R.string.toast_result_can_not_deleted));
                                    }

                                    @Override
                                    public void onResponse(Call call, Response response) throws IOException {
                                        try {
                                            JSONObject json_data = new JSONObject(response.body().string());

                                            switch (json_data.getInt("code")) {
                                                case HttpApi.RESPONSE_SUCCESS:
                                                    SharedPrefUtil.putBoolean(SharedPrefUtil.AUTO_LOGIN, false);
                                                    SharedPrefUtil.putString(SharedPrefUtil.USER_ID, "");
                                                    SharedPrefUtil.putString(SharedPrefUtil.USER_PASS, "");
                                                    SharedPrefUtil.putString(SharedPrefUtil.ROOM_CONTROLLER_ID, "");

                                                    FirebaseMessaging.getInstance().deleteToken();

                                                    // aws mqtt 제어 종료 및 연결 종료
                                                    CleanVentilationApplication.getInstance().awsRemoteFinish();
                                                    CleanVentilationApplication.getInstance().awsMqttDisConnect();

                                                    // ikHwang 2020-01-16 오전 11:41 로그아웃시 홈화면 정보 초기화
                                                    CleanVentilationApplication.getInstance().clearHomeInfo();

                                                    Intent intent = new Intent(MoreSettingActivity.this, PopupActivity.class);
                                                    intent.putExtra(PopupActivity.POPUP_MODE, PopupActivity.MODE_NORMAL);
                                                    intent.putExtra(PopupActivity.POPUP_BODY_TEXT, getString(R.string.withdraw_popup_success));
                                                    startActivityForResult(intent, RESULT_WITHDRAW_SUCCESS);
                                                    overridePendingTransition(0, 0);
                                                    break;

                                                default:
                                                    CommonUtils.showToast(MoreSettingActivity.this, getString(R.string.toast_result_can_not_deleted));
                                                    break;
                                            }
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                            CommonUtils.showToast(MoreSettingActivity.this, getString(R.string.toast_result_can_not_deleted));
                                        } finally {
                                            DismissConnectDialog();
                                        }
                                    }
                                });
                    } catch (Exception e) {
                        e.printStackTrace();
                        DismissConnectDialog();
                        CommonUtils.showToast(MoreSettingActivity.this, getString(R.string.toast_result_can_not_deleted));
                    }
                }
                break;

            case RESULT_WITHDRAW_SUCCESS: {
                Intent intent = new Intent(this, SplashActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivityAni(MoreSettingActivity.this, intent, true, 2);
            }
            break;
        }
    }
}
