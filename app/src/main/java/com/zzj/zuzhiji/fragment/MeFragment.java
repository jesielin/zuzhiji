package com.zzj.zuzhiji.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.zzj.zuzhiji.R;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by shawn on 2017-03-29.
 */

public class MeFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View contentView = View.inflate(getActivity(), R.layout.fragment_me, null);
        ButterKnife.bind(this, contentView);
        test.setText("Me");
        return contentView;
    }

    @BindView(R.id.test)
    TextView test;
}
