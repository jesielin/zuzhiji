package com.zzj.zuzhiji;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.View;
import android.widget.RadioGroup;

import com.aspsine.fragmentnavigator.FragmentNavigator;
import com.aspsine.fragmentnavigator.FragmentNavigatorAdapter;
import com.zzj.zuzhiji.app.Constant;
import com.zzj.zuzhiji.fragment.HomeFragment;
import com.zzj.zuzhiji.fragment.MeFragment;
import com.zzj.zuzhiji.fragment.MessageFragment;
import com.zzj.zuzhiji.fragment.NewsFragment2;
import com.zzj.zuzhiji.fragment.SocialFragment;
import com.zzj.zuzhiji.util.DebugLog;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends BaseActivity {


    @BindView(R.id.nav)
    RadioGroup rgNav;
    private FragmentNavigator mNavigator;

//    private UpdateHelper updateHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        mNavigator = new FragmentNavigator(getSupportFragmentManager(), new MainFragmentAdapter(), R.id.container);
        // set default tab position
        mNavigator.setDefaultPosition(0);
        mNavigator.onCreate(savedInstanceState);

//        updateHelper = new UpdateHelper(this, false);
        setCurrentTab(mNavigator.getCurrentPosition());

    }


    @Override
    protected void onResume() {
        super.onResume();

        //// TODO: 2017-05-20 取消版本更新监控
//        if (updateHelper != null)
//            updateHelper.check();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mNavigator.onSaveInstanceState(outState);
    }

    public void switchFragment(int index){
        setCurrentTab(index);

    }

    @OnClick({R.id.home, R.id.social, R.id.news, R.id.message, R.id.me})
    public void navCheck(View view) {
        switch (view.getId()) {
            case R.id.home:
                setCurrentTab(0);
                break;
            case R.id.social:
                setCurrentTab(1);
                break;
            case R.id.news:
                setCurrentTab(2);
                break;
            case R.id.message:
                setCurrentTab(3);
                break;
            case R.id.me:
                setCurrentTab(4);
                break;
        }
    }

    private void setCurrentTab(int position) {
        mNavigator.showFragment(position);


        switch (position) {
            case 0:
                rgNav.check(R.id.home);
                break;
            case 1:
                rgNav.check(R.id.social);
                break;
            case 2:
                rgNav.check(R.id.news);
                break;
            case 3:
                rgNav.check(R.id.message);
                break;
            case 4:
                rgNav.check(R.id.me);


        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Fragment f = null;
        switch (requestCode) {
            case Constant.ACTIVITY_CODE.REQUEST_CODE_SOCIAL_FRAGMENT:
            case Constant.ACTIVITY_CODE.REQUEST_CODE_SOCIAL_TO_DETAIL:
                f = mNavigator.getFragment(1);
                DebugLog.e("f:" + f);

                break;
            case Constant.ACTIVITY_CODE.REQUEST_CODE_MESSAGE_TO_CHAT:
                f = mNavigator.getFragment(3);
                break;
        }
        if (f != null)
            f.onActivityResult(requestCode, resultCode, data);
    }

    private class MainFragmentAdapter implements FragmentNavigatorAdapter {

        @Override
        public Fragment onCreateFragment(int i) {
            Fragment f = null;
            DebugLog.e("position:" + i);
            switch (i) {
                case 0:
                    f = new HomeFragment();
                    break;
                case 1:
                    f = new SocialFragment();
                    break;
                case 2:
                    //// TODO: 2017-05-29  
                    f = new NewsFragment2();
                    break;
                case 3:
                    f = new MessageFragment();
                    break;
                case 4:
                    f = new MeFragment();
                    break;
            }
            DebugLog.e("f:" + f);
            return f;
        }

        @Override
        public String getTag(int i) {
            String tag = "";
            switch (i) {
                case 0:
                    tag = "HOME";
                    break;
                case 1:
                    tag = "SOCIAL";
                    break;
                case 2:
                    tag = "NEWS";
                    break;
                case 3:
                    tag = "MESSAGE";
                    break;
                case 4:
                    tag = "ME";
                    break;
            }
            return tag;
        }

        @Override
        public int getCount() {
            return 5;
        }
    }
}
