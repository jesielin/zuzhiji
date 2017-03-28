package com.zzj.zuzhiji;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.zzj.zuzhiji.fragment.RegisterFirstFragment;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by shawn on 2017-03-28.
 */

public class RegisterActivity extends AppCompatActivity {

//    @BindView(R.id.avator)
//    ImageView imvAvator;



    private List<String> selectedPhotos = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        getSupportFragmentManager().beginTransaction().replace(R.id.container, new RegisterFirstFragment(), "first").commit();
    }


//
}
