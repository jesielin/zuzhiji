package com.zzj.zuzhiji;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

import com.zzj.zuzhiji.fragment.ReservTechListFragment;
import com.zzj.zuzhiji.fragment.ReservTechStudioListFragment;

/**
 * Created by shawn on 17/4/10.
 */

public class ReservationActivity extends BaseActivity {

    public static final int FROM_HOME = 1;
    public static final int FROM_TECH = 2;
    public static final String KEY_FROM = "KEY_FROM";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reservation);


        Intent intent = getIntent();
        int from = intent.getIntExtra(KEY_FROM, 1);
        switch (from) {
            case 1:
                getSupportFragmentManager().beginTransaction().replace(R.id.container, new ReservTechListFragment()).commit();
                break;
            case 2:
                String tech_id = intent.getStringExtra("TECH_ID");
                Fragment f = new ReservTechStudioListFragment();
                Bundle bundle = new Bundle();
                bundle.putString("TECH_ID", tech_id);
                f.setArguments(bundle);
                getSupportFragmentManager().beginTransaction().replace(R.id.container, f).commit();
                break;
        }


    }
}
