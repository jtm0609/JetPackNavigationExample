package kr.co.kdone.airone.adapter;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import kr.co.kdone.airone.fragments.old.RegisterDevice1Fragment;
import kr.co.kdone.airone.fragments.old.RegisterDevice2Fragment;
import kr.co.kdone.airone.fragments.old.RegisterDevice3Fragment;
import kr.co.kdone.airone.fragments.old.RegisterDevice4Fragment;

public class RegisterDevicePagerAdapter extends FragmentPagerAdapter {
    private RegisterDevice1Fragment register1;
    private RegisterDevice2Fragment register2;
    private RegisterDevice3Fragment register3;
    private RegisterDevice4Fragment register4;
    private int pageCount;

    public RegisterDevicePagerAdapter(FragmentManager fm, int pageCount) {
        super(fm);

        this.pageCount = pageCount;
        register1 = new RegisterDevice1Fragment();
        register2 = new RegisterDevice2Fragment();
        register3 = new RegisterDevice3Fragment();
        register4 = new RegisterDevice4Fragment();
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return register1;

            case 1:
                return register2;

            case 2:
                return register3;

            case 3:
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
