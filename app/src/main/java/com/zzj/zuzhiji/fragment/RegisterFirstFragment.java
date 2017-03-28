package com.zzj.zuzhiji.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.zzj.zuzhiji.R;

import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by shawn on 2017-03-28.
 */

public class RegisterFirstFragment extends Fragment {

    private String type = "0";
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View contentView = View.inflate(container.getContext(), R.layout.fragment_register_first,null);
        ButterKnife.bind(this,contentView);
        return contentView;

    }

    @OnClick({R.id.operator,R.id.single})
    public void typeCheck(View view){
        switch (view.getId()){
            case R.id.operator:
                type = "1";
                break;
            case R.id.single:
                type = "0";
                break;
        }
    }

}
