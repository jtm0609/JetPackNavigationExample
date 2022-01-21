package kr.co.kdone.airone.utils;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import kr.co.kdone.airone.R;

import static kr.co.kdone.airone.utils.CommonUtils.MODE_DEVICE_ROOM_CON;
import static kr.co.kdone.airone.utils.CommonUtils.RESULT_DETAIL_CHECK;

public class RoomControllerDialog extends Dialog {

    private Button btnCancel,btnDetail;
    private OnDialogClickButtonListener mListener;

    public RoomControllerDialog(Context context) {
        super(context);
    }

    public RoomControllerDialog(Context context, int themeResId) {
        super(context, themeResId);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_room_controller_popup);

        btnCancel = findViewById(R.id.btnCancel);
        btnDetail = findViewById(R.id.btnDetail);
        initListener();
    }

    public void setDialogListener(OnDialogClickButtonListener listener){
        this.mListener = listener;
    }

    private void initListener(){
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onCancelClicked();
            }
        });

        btnDetail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onDetailClicked();
            }
        });

    }

    public interface OnDialogClickButtonListener{
        void onDetailClicked();
        void onCancelClicked();
    }
}
