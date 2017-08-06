package com.zzj.zuzhiji;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;

import com.zzj.zuzhiji.fragment.LectureFragment;
import com.zzj.zuzhiji.fragment.TrainFragment;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by shawn on 2017-08-06.
 */

public class TrainActivity extends BaseActivity {
    @BindView(R.id.tablayout)
    TabLayout tabLayout;

    @BindView(R.id.viewpager)
    ViewPager viewPager;

    private int TAB_COUNT = 2;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_train);
        ButterKnife.bind(this);


        setupViewPager();
    }

    @OnClick(R.id.back)
    public void back() {
        onBackPressed();
    }

    private void setupViewPager() {

        viewPager.setAdapter(new TrainPagerAdapter(getSupportFragmentManager()));
        tabLayout.setupWithViewPager(viewPager);

    }

    private class TrainPagerAdapter extends FragmentPagerAdapter {


        public TrainPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            Fragment f = null;
            switch (position) {
                case 0:
                    f = new TrainFragment();
                    break;
                case 1:
                    f = new LectureFragment();
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
                    title = "培训";
                    break;
                case 1:
                    title = "讲座";
                    break;

            }
            return title;
        }
    }
}
