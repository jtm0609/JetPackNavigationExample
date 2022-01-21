package kr.co.kdone.airone.adapter;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

import kr.co.kdone.airone.R;
import kr.co.kdone.airone.adapter.viewholder.BleViewHolder;

/**
 * ikHwang 2019-06-05 오후 3:30 블루투스 리스트 구성 어댑터
 */
public class BleListAdapter extends RecyclerView.Adapter<BleViewHolder> {
    private ArrayList<BluetoothDevice> bleList;
    private OnItemClickListener onItemClickListener;

    public interface OnItemClickListener {
        void onItemClickListener(int position);
    }

    public BleListAdapter(ArrayList<BluetoothDevice> list, OnItemClickListener listener){
        bleList = list;
        onItemClickListener = listener;
    }

    @NonNull
    @Override
    public BleViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        final LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
        View v = inflater.inflate(R.layout.listview_ble_item, viewGroup, false);

        BleViewHolder bleViewHolder = new BleViewHolder(v);

        return bleViewHolder;
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onBindViewHolder(@NonNull BleViewHolder bleViewHolder, final int i) {
        bleViewHolder.getNameTextView().setText(TextUtils.isEmpty(bleList.get(i).getName()) ? "Unknown" : bleList.get(i).getName().replaceFirst("BLUFI_", ""));
        bleViewHolder.getContainer().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onItemClickListener.onItemClickListener(i);
            }
        });
    }

    @Override
    public int getItemCount() {
        return bleList.size();
    }
}
