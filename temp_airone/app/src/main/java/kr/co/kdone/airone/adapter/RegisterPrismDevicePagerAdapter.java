package kr.co.kdone.airone.adapter;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import kr.co.kdone.airone.fragments.prism.RegisterPrismDevice1Fragment;
import kr.co.kdone.airone.fragments.prism.RegisterPrismDevice3Fragment;
import kr.co.kdone.airone.fragments.prism.RegisterPrismDevice4Fragment;

/**
 * ikHwang 2019-06-05 오전 10:24 프리즘 모델 제품 연결화면 어댑터
 */
public class RegisterPrismDevicePagerAdapter extends FragmentPagerAdapter {
    private RegisterPrismDevice1Fragment register1;
//    private RegisterPrismDevice2Fragment register2;
    private RegisterPrismDevice3Fragment register3;
    private RegisterPrismDevice4Fragment register4;
    private int pageCount;

    public RegisterPrismDevicePagerAdapter(FragmentManager fm, int pageCount) {
        super(fm);

        this.pageCount = pageCount;
        register1 = new RegisterPrismDevice1Fragment();
//        register2 = new RegisterPrismDevice2Fragment();
        register3 = new RegisterPrismDevice3Fragment();
        register4 = new RegisterPrismDevice4Fragment();
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return register1;

//            case 1:
//                return register2;

            case 1:
                return register3;

            case 2:
                return register4;

            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return pageCount;
    }
}
