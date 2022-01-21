package kr.co.kdone.airone.fragments.old;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import kr.co.kdone.airone.activity.PopupActivity;
import kr.co.kdone.airone.R;
import kr.co.kdone.airone.activity.register.RegisterDevice2Activity;
import kr.co.kdone.airone.utils.CommonUtils;
import kr.co.kdone.airone.utils.WifiUtils;

import static android.app.Activity.RESULT_OK;
import static kr.co.kdone.airone.utils.CommonUtils.MODE_DEVICE_MONITOR;
import static kr.co.kdone.airone.utils.CommonUtils.MODE_DEVICE_ROOM_CON;
import static kr.co.kdone.airone.utils.CommonUtils.MODE_FIND_PASS;

public class RegisterDevice2Fragment extends Fragment implements View.OnClickListener {

    private final String TAG = "RegisterDevice2Fragment";
    private RegisterDevice2Activity act;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        act = (RegisterDevice2Activity) getActivity();
        View view = inflater.inflate(R.layout.fragment_register_device2, container, false);

        TextView txtRegisterDevice2 = view.findViewById(R.id.txtRegisterDevice2);
        ImageView imgDeviceWifi = view.findViewById(R.id.imgDeviceWifi);

        String textRegiDevice2 = "";
        if (act.subMode == MODE_DEVICE_ROOM_CON) {
            imgDeviceWifi.setImageResource(R.drawable.set_roomcon_10pw_01);
            textRegiDevice2 = getString(R.string.fragment_register_device2_content2);
        } else if (act.subMode == MODE_DEVICE_MONITOR) {
            imgDeviceWifi.setImageResource(R.drawable.d_10pw_set_monitor03);
            textRegiDevice2 = getString(R.string.fragment_register_device2_content2_1);
        }
        int start = textRegiDevice2.indexOf("\"");
        int end = textRegiDevice2.lastIndexOf("\"") + 1;
        SpannableStringBuilder ssb = new SpannableStringBuilder(textRegiDevice2);
        ssb.setSpan(new ForegroundColorSpan(Color.parseColor("#3690b8")), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        txtRegisterDevice2.setText(ssb);

        view.findViewById(R.id.layoutOK).setOnClickListener(this);
        return view;
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.layoutOK: {
                String ssid = "";

                WifiManager wifiManager = (WifiManager) act.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                ssid = wifiInfo.getSSID();

                if(TextUtils.isEmpty(ssid)){
                    ConnectivityManager connectivityManager = (ConnectivityManager) act.getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
                    NetworkInfo netInfo = connectivityManager.getActiveNetworkInfo();
                    if (netInfo != null && netInfo.isConnected()) {
                        ssid = netInfo.getExtraInfo();
                    }
                }

                if (ssid != null && ssid.length() > 0) {
                    if (act.mode == MODE_FIND_PASS && ssid.toUpperCase().contains(WifiUtils.SSID_VALID_ROOMCON)) {
                        Intent intent = new Intent();
                        act.setResult(RESULT_OK, intent);
                        act.finish();
                    } else if ((act.subMode == MODE_DEVICE_ROOM_CON && ssid.toUpperCase().contains(WifiUtils.SSID_VALID_ROOMCON))
                            || (act.subMode == MODE_DEVICE_MONITOR && ssid.toUpperCase().contains(WifiUtils.SSID_VALID_MONITOR))) {
                        act.moveRegisterPage(2);
                    } else {
                        CommonUtils.showToast(act, getString(R.string.check_wifi_for_get_id));
                    }
                } else {
                    Intent intent = new Intent(act, PopupActivity.class);
                    intent.putExtra(PopupActivity.POPUP_MODE, PopupActivity.MODE_NO_BUTTON);
                    intent.putExtra(PopupActivity.POPUP_BODY_TEXT, getString(R.string.check_wifi_for_get_id));
                    intent.putExtra(PopupActivity.POPUP_BODY_COLOR_MARK, getString(R.string.check_wifi_for_get_id_mark));
                    startActivity(intent);
                    act.overridePendingTransition(0, 0);
                }
                break;
            }
        }
    }
}
