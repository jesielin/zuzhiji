package com.zzj.zuzhiji;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.zzj.zuzhiji.util.SharedPreferencesUtils;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (!SharedPreferencesUtils.getInstance().isLogin()) {
            startActivity(new Intent(this, LoginActivity.class));

        }
    }
}
