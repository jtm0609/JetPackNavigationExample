package kr.co.kdone.airone.fragments.prism;

import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

import kr.co.kdone.airone.R;
import kr.co.kdone.airone.activity.register.prism.RegisterPrismDeviceActivity;
import kr.co.kdone.airone.adapter.BleListAdapter;

import static kr.co.kdone.airone.utils.CommonUtils.DismissConnectDialog;
import static kr.co.kdone.airone.utils.CommonUtils.IsRunningProgress;

/**
 * ikHwang 2019-06-05 오전 10:21 블루투스 검색 화면
 */
public class RegisterPrismDevice2Fragment extends Fragment{
    private final String TAG = getClass().getSimpleName();

    private RegisterPrismDeviceActivity act;    // 프래그먼트의 부모 액티비티
    private RecyclerView mBleListView;          // 블루투스 디바이스 리스트
    private BleListAdapter mBleAdapter;         // 블루투스 디바이스 리스트 구성 어댑터
    private ArrayList<BluetoothDevice> bleList; // 블루투스 디바이스 목록

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_register_prism_device2, container, false);

        act = (RegisterPrismDeviceActivity) getActivity();

        initLayout(view);

        if (IsRunningProgress()) DismissConnectDialog();

        return view;
    }

    /**
     * ikHwang 2019-06-05 오후 3:41 레이아웃 초기화
     * @param v
     */
    private void initLayout(View v){
        mBleListView = v.findViewById(R.id.listBle);

        bleList = new ArrayList<>();
        mBleAdapter = new BleListAdapter(bleList, new BleListAdapter.OnItemClickListener() {
            @Override
            public void onItemClickListener(int position) {
                // 블루투스 디바이스 선택 처리
            }
        });
        mBleListView.setAdapter(mBleAdapter);
        mBleListView.addItemDecoration(new DividerItemDecoration(act, DividerItemDecoration.VERTICAL));
    }
}
