package kr.co.kdone.airone.adapter.viewholder;

import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import kr.co.kdone.airone.R;

/**
 * ikHwang 2019-06-10 오전 9:44  AP 리스트 아이템
 */
public class WiFiAPViewHolder extends RecyclerView.ViewHolder{
    private View container;
    private TextView ap_name;
    private ImageView ap_lock;
    private ImageView ap_signal;

    public WiFiAPViewHolder(View v) {
        super(v);

        container = v.findViewById(R.id.ap_container);
        ap_name = v.findViewById(R.id.ap_name);
        ap_lock = v.findViewById(R.id.ap_lock);
        ap_signal = v.findViewById(R.id.ap_signal);
    }

    public View getContainer() {
        return container;
    }

    public void setContainer(View container) {
        this.container = container;
    }

    public TextView getAp_name() {
        return ap_name;
    }

    public void setAp_name(TextView ap_name) {
        this.ap_name = ap_name;
    }

    public ImageView getAp_lock() {
        return ap_lock;
    }

    public void setAp_lock(ImageView ap_lock) {
        this.ap_lock = ap_lock;
    }

    public ImageView getAp_signal() {
        return ap_signal;
    }

    public void setAp_signal(ImageView ap_signal) {
        this.ap_signal = ap_signal;
    }
}
