package com.zzj.zuzhiji;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class LoginActivity extends AppCompatActivity {


    @BindView(R.id.tel)
    EditText etTel;
    @BindView(R.id.verify)
    EditText etVerify;
    @BindView(R.id.get_verify)
    TextView tvGetVerify;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        ButterKnife.bind(this);
    }

    @OnClick(R.id.login)
    public void login(View view) {
        Toast.makeText(this, "登录", Toast.LENGTH_SHORT).show();
    }

    @OnClick(R.id.register)
    public void register(View view) {
        Toast.makeText(this, "注册", Toast.LENGTH_SHORT).show();
    }

    @OnClick(R.id.get_verify)
    public void getVerify(View view) {
        if (TextUtils.isEmpty(etTel.getText().toString().trim())) {
            Toast.makeText(this, "请输入正确的手机号", Toast.LENGTH_SHORT).show();
            return;
        }


    }


}
