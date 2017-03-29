package com.zzj.zuzhiji.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.zzj.zuzhiji.R;

import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by shawn on 2017-03-29.
 */

public class SocialFragment extends Fragment {


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View contentView = View.inflate(getActivity(), R.layout.fragment_social, null);
        ButterKnife.bind(this, contentView);
        return contentView;
    }

    @OnClick(R.id.publish)
    public void publish(View view) {
        Toast.makeText(getActivity(), "发表朋友圈", Toast.LENGTH_SHORT).show();
    }

}
