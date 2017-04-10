package com.zzj.zuzhiji;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.zzj.zuzhiji.fragment.ReservTechListFragment;

/**
 * Created by shawn on 17/4/10.
 */

public class ReservationActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reservation);

        getSupportFragmentManager().beginTransaction().replace(R.id.container, new ReservTechListFragment()).commit();
    }
}
