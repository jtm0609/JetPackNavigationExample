package kr.co.kdone.airone.adapter;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import java.util.ArrayList;

import kr.co.kdone.airone.data.home.CommonHomeInfo;
import kr.co.kdone.airone.fragments.HomePrismFragment;

/**
 * ikHwang 2019-06-10 오전 11:21 메인화면 룸콘 페이저 어댑터
 */
public class MainPagerAdapter extends FragmentPagerAdapter {
    private ArrayList<CommonHomeInfo> mHomeList;
    private ArrayList<HomePrismFragment> homeFragments = new ArrayList<>();

    public MainPagerAdapter(FragmentManager fm, ArrayList<CommonHomeInfo> list) {
        super(fm);

        mHomeList = list;

        for(int i=0; i<mHomeList.size(); i++){
            homeFragments.add(new HomePrismFragment());
        }
    }

    @Override
    public Fragment getItem(int position) {
        return homeFragments.get(position);
    }

    @Override
    public int getCount() {
        return homeFragments.size();
    }
}
