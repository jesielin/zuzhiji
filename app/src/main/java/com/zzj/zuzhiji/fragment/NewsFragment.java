package com.zzj.zuzhiji.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.zzj.zuzhiji.R;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by shawn on 2017-03-29.
 */

public class NewsFragment extends Fragment {

    @BindView(R.id.tablayout)
    TabLayout tabLayout;

    @BindView(R.id.viewpager)
    ViewPager viewPager;

    private int TAB_COUNT = 2;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View contentView = View.inflate(getActivity(), R.layout.fragment_news, null);
        ButterKnife.bind(this, contentView);

        setupViewPager();

        return contentView;
    }

    private void setupViewPager() {

        viewPager.setAdapter(new NewsPagerAdapter(getChildFragmentManager()));
        tabLayout.setupWithViewPager(viewPager);
    }


    private class NewsPagerAdapter extends FragmentPagerAdapter {


        public NewsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            Fragment f = null;
            switch (position) {
                case 0:
                    f = new VideoNewsFragment();
                    break;
                case 1:
                    f = new TextNewsFragment();
                    break;
            }
            return f;
        }


        @Override
        public int getCount() {
            return TAB_COUNT;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            String title = "";
            switch (position) {
                case 0:
                    title = "视频";
                    break;
                case 1:
                    title = "咨询";
                    break;

            }
            return title;
        }
    }


}
