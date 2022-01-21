package kr.co.kdone.airone.adapter.viewholder;

import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import kr.co.kdone.airone.R;

/**
 * ikHwang 2019-06-05 오후 3:27 블루투스 리스트 아이템
 */
public class BleViewHolder extends RecyclerView.ViewHolder{
    private View container;
    private TextView nameTextView;

    public BleViewHolder(View v) {
        super(v);

        container = v.findViewById(R.id.ble_container);
        nameTextView = v.findViewById(R.id.ble_name);
    }

    public View getContainer() {
        return container;
    }

    public void setContainer(View container) {
        this.container = container;
    }

    public TextView getNameTextView() {
        return nameTextView;
    }

    public void setNameTextView(TextView nameTextView) {
        this.nameTextView = nameTextView;
    }
}
