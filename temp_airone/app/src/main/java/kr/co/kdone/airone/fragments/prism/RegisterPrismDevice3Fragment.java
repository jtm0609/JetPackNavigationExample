package kr.co.kdone.airone.fragments.prism;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import java.util.ArrayList;

import kr.co.kdone.airone.R;
import kr.co.kdone.airone.activity.register.prism.RegisterPrismDeviceActivity;
import kr.co.kdone.airone.adapter.WifiAPListAdapter;
import kr.co.kdone.airone.udp.WifiAP;
import kr.co.kdone.airone.utils.CommonUtils;

/**
 * ikHwang 2019-06-05 오전 10:21 AP 검색 결과 화면
 */
public class RegisterPrismDevice3Fragment extends Fragment implements View.OnClickListener {
    private final String TAG = getClass().getSimpleName();

    private RegisterPrismDeviceActivity act;        // 프래그먼트의 부모 액티비티
    private RecyclerView mWifiAPList;               // WiFi AP 리스트
    private WifiAPListAdapter mWifiAPListAdapter;   // WiFi 리스트 구성 어댑터
    private ArrayList<WifiAP> mWifiAPs;             // 연결 가능한 WiFi 리스트 목록

    private LinearLayout layoutRefresh;         // AP 목록 조회 새로코침 버튼
    private ProgressBar progressBar3;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_register_prism_device3, container, false);

        act = (RegisterPrismDeviceActivity) getActivity();

        initLayout(view);

        return view;
    }

    /**
     * ikHwang 2019-06-10 오전 9:51 레이아웃 초기화
     * @param v
     */
    private void initLayout(View v){
        mWifiAPs = new ArrayList<>();

        mWifiAPList = v.findViewById(R.id.listWifi);
        layoutRefresh = v.findViewById(R.id.layoutRefresh);
        progressBar3 = v.findViewById(R.id.progressBar3);

        layoutRefresh.setOnClickListener(this);

        mWifiAPListAdapter = new WifiAPListAdapter(mWifiAPs, new WifiAPListAdapter.OnItemClickListener() {
            @Override
            public void onItemClickListener(int position) {
                act.startAPPW(position);
            }
        });
        mWifiAPList.setAdapter(mWifiAPListAdapter);
        mWifiAPList.addItemDecoration(new DividerItemDecoration(act, DividerItemDecoration.VERTICAL));
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.layoutRefresh: // WiFi AP 목록 갱신
                if(act.mConnected){
                    setLoadingUI(true);

                    act.startScanWiFiList();
                }else{
                    CommonUtils.showToast(act, getString(R.string.fragment_register_prism_device3_str_1));
                }
                break;
        }
    }

    /**
     * ikHwang 2019-06-19 오전 9:49 AP목록 업데이트
     * @param list
     */
    public void updateApList(final ArrayList<WifiAP> list){
        act.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                setLoadingUI(false);

                mWifiAPs.clear();
                mWifiAPs.addAll(list);
                mWifiAPListAdapter.notifyDataSetChanged();
                mWifiAPList.smoothScrollToPosition(0);
            }
        });
    }

    public void setLoadingUI(boolean isLoading){
        try {
            if(isLoading){
                if(null != progressBar3) progressBar3.setVisibility(View.VISIBLE);
                if(null != mWifiAPList) mWifiAPList.setVisibility(View.GONE);
            }else{
                if(null != progressBar3) progressBar3.setVisibility(View.GONE);
                if(null != mWifiAPList) mWifiAPList.setVisibility(View.VISIBLE);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
