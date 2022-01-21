package kr.co.kdone.airone.adapter;

import android.annotation.SuppressLint;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

import kr.co.kdone.airone.R;
import kr.co.kdone.airone.adapter.viewholder.WiFiAPViewHolder;
import kr.co.kdone.airone.udp.WifiAP;

import static kr.co.kdone.airone.utils.CommonUtils.SECURITY_NONE;

/**
 * ikHwang 2019-06-10 오전 9:48  WiFi AP 리스트 구성 어댑터
 */
public class WifiAPListAdapter extends RecyclerView.Adapter<WiFiAPViewHolder> {
    private ArrayList<WifiAP> listWifiAP;
    private OnItemClickListener onItemClickListener;

    private final int MAX_COUNT = 10;

    public interface OnItemClickListener {
        void onItemClickListener(int position);
    }

    public WifiAPListAdapter(ArrayList<WifiAP> list, OnItemClickListener listener){
        listWifiAP = list;
        onItemClickListener = listener;
    }

    @NonNull
    @Override
    public WiFiAPViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        final LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
        View v = inflater.inflate(R.layout.listview_setting_ap_item, viewGroup, false);

        WiFiAPViewHolder wiFiAPViewHolder = new WiFiAPViewHolder(v);

        return wiFiAPViewHolder;
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onBindViewHolder(@NonNull WiFiAPViewHolder wiFiAPViewHolder, final int position) {
        wiFiAPViewHolder.getAp_name().setText(listWifiAP.get(position).ssid);
        wiFiAPViewHolder.getAp_lock().setVisibility(listWifiAP.get(position).security == SECURITY_NONE ? View.INVISIBLE : View.VISIBLE);
        wiFiAPViewHolder.getAp_signal().setImageLevel(listWifiAP.get(position).rssi);

        wiFiAPViewHolder.getContainer().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onItemClickListener.onItemClickListener(position);
            }
        });
    }

    @Override
    public int getItemCount() {
//        return listWifiAP.size() > MAX_COUNT ? MAX_COUNT : listWifiAP.size();
        return listWifiAP.size();
    }
}
