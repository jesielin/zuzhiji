package com.zzj.zuzhiji.wxapi;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.tencent.mm.opensdk.constants.ConstantsAPI;
import com.tencent.mm.opensdk.modelbase.BaseReq;
import com.tencent.mm.opensdk.modelbase.BaseResp;
import com.tencent.mm.opensdk.modelpay.PayReq;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.IWXAPIEventHandler;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;
import com.yayandroid.theactivitymanager.TheActivityManager;
import com.zzj.zuzhiji.R;
import com.zzj.zuzhiji.ReservationActivity;
import com.zzj.zuzhiji.app.Constant;
import com.zzj.zuzhiji.network.Network;
import com.zzj.zuzhiji.network.entity.PayResult;
import com.zzj.zuzhiji.util.DebugLog;
import com.zzj.zuzhiji.util.DialogUtils;
import com.zzj.zuzhiji.util.SharedPreferencesUtils;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.Subscriber;

/**
 * Created by shawn on 2017-05-19.
 */

public class WXPayEntryActivity extends Activity implements IWXAPIEventHandler {
    @BindView(R.id.price)
    TextView tvPrice;

    @BindView(R.id.rb_underline)
    RadioButton rbUnderline;
    @BindView(R.id.rb_wechat)
    RadioButton rbWechat;


    private String title;
    private String price;
    private String service_id;
    private String studio_id;
    private String tech_uuid;
    private String time;

    private String pay_type = "2";

    private boolean isRegister = false;

    private Dialog mDialog;

    private boolean isAlreadyMakeOrder;
    private PayResult orderResult;

    // IWXAPI 是第三方app和微信通信的openapi接口
    private IWXAPI api;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pay);

        ButterKnife.bind(this);
        resolveArgs();
        api = WXAPIFactory.createWXAPI(WXPayEntryActivity.this, null);
        isRegister = api.registerApp(Constant.APP_ID);
        api.handleIntent(getIntent(), this);


    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        api.handleIntent(intent, this);
    }

    @OnClick(R.id.pay)
    public void pay() {

        if (isAlreadyMakeOrder) {
            payOrder();
            return;
        }

        mDialog = DialogUtils.showProgressDialog(this, "请稍候....");
        Network.getInstance().pay(
                SharedPreferencesUtils.getInstance().getValue(Constant.SHARED_KEY.UUID),
                service_id,
                tech_uuid,
                studio_id,
                pay_type,
                time
        ).subscribe(new Subscriber<PayResult>() {
            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {

                Toast.makeText(WXPayEntryActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                DialogUtils.dismissDialog(mDialog);
            }

            @Override
            public void onNext(PayResult payResult) {


                if (!isRegister) {
                    Toast.makeText(WXPayEntryActivity.this, "无法调起微信支付..", Toast.LENGTH_SHORT).show();
                    api.registerApp(payResult.appid);
                } else {

                    isAlreadyMakeOrder = true;
                    orderResult = payResult;
                    payOrder();
                }


                DialogUtils.dismissDialog(mDialog);


            }
        });
    }

    private void payOrder() {
        PayReq request = new PayReq();
        request.appId = orderResult.appid;
        request.partnerId = orderResult.partnerid;
        request.prepayId = orderResult.prepayid;
        request.packageValue = "Sign=WXPay";
        request.nonceStr = orderResult.noncestr;
        request.timeStamp = orderResult.timestamp;
        request.sign = orderResult.sign;
        api.sendReq(request);
    }

    @OnClick({R.id.wechat, R.id.underline})
    public void choose(View view) {
        switch (view.getId()) {
            case R.id.wechat:
                pay_type = "2";
                rbWechat.setChecked(true);
                rbUnderline.setChecked(false);
                Toast.makeText(this, "微信支付", Toast.LENGTH_SHORT).show();
                break;
            case R.id.underline:
                pay_type = "3";
                rbWechat.setChecked(false);
                rbUnderline.setChecked(true);
                Toast.makeText(this, "线下支付", Toast.LENGTH_SHORT).show();
                break;
        }

    }

    private void resolveArgs() {
        Bundle arguments = getIntent().getExtras();
        service_id = arguments.getString("SERVICE_ID");
        studio_id = arguments.getString("STUDIO_ID");
        tech_uuid = arguments.getString("TECH_ID");
        price = arguments.getString("PRICE");
        title = arguments.getString("TITLE");
        time = arguments.getString("TIME");

        tvPrice.setText(price);
    }

    @OnClick(R.id.back)
    public void back() {
        onBackPressed();
    }


    @Override
    public void onReq(BaseReq baseReq) {
        DebugLog.e("on req");
//        Toast.makeText(this, "req", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onResp(BaseResp resp) {
        DebugLog.e("on resp");
//        Toast.makeText(this, "resp", Toast.LENGTH_SHORT).show();
        DialogUtils.dismissDialog(mDialog);
        if (resp.getType() == ConstantsAPI.COMMAND_PAY_BY_WX) {
            DebugLog.d("onPayFinish,errCode=" + resp.errCode);
            switch (resp.errCode) {
                case 0:
                    //支付成功
                    Toast.makeText(this, "支付成功\nCode:" + resp.errCode, Toast.LENGTH_SHORT).show();
                    TheActivityManager.getInstance().finishInstance(WXPayEntryActivity.class);
                    TheActivityManager.getInstance().finishInstance(ReservationActivity.class);
                    break;
                case -1:
                    //错误	可能的原因：签名错误、未注册APPID、项目设置APPID不正确、注册的APPID与设置的不匹配、其他异常等。
                    Toast.makeText(this, "支付异常\nCode:" + resp.errCode + "\nMessage:" + resp.errStr, Toast.LENGTH_LONG).show();
                    break;
                case -2:
                    //取消
                    Toast.makeText(this, "取消支付\nCode:" + resp.errCode, Toast.LENGTH_SHORT).show();
                    break;
            }


        }
    }
}
