package com.zzj.zuzhiji.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.tencent.mm.opensdk.modelpay.PayReq;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;
import com.zzj.zuzhiji.R;
import com.zzj.zuzhiji.app.Constant;
import com.zzj.zuzhiji.network.Network;
import com.zzj.zuzhiji.network.entity.PayResult;
import com.zzj.zuzhiji.util.DialogUtils;
import com.zzj.zuzhiji.util.SharedPreferencesUtils;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.Subscriber;

/**
 * Created by shawn on 2017-05-19.
 */

public class PayFragment extends BaseFragment {

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

    private String pay_type = "2";

    // IWXAPI 是第三方app和微信通信的openapi接口
    private IWXAPI api;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View contentView = View.inflate(getActivity(), R.layout.activity_pay, null);
        ButterKnife.bind(this, contentView);

        resolveArgs();

        tvPrice.setText(price);


        return contentView;
    }


    @OnClick(R.id.pay)
    public void pay() {
        mProgressDialog = DialogUtils.showProgressDialog(getActivity(), "请稍候....");
        Network.getInstance().pay(
                SharedPreferencesUtils.getInstance().getValue(Constant.SHARED_KEY.UUID),
                service_id,
                tech_uuid,
                studio_id,
                pay_type
        ).subscribe(new Subscriber<PayResult>() {
            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {

                Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
                DialogUtils.dismissDialog(mProgressDialog);
            }

            @Override
            public void onNext(PayResult payResult) {

                api = WXAPIFactory.createWXAPI(getActivity(), null);
                api.registerApp(Constant.APP_ID);
                PayReq request = new PayReq();
                request.appId = payResult.appid;
                request.partnerId = payResult.partnerid;
                request.prepayId = payResult.prepayid;
                request.packageValue = "Sign=WXPay";
                request.nonceStr = payResult.noncestr;
                request.timeStamp = payResult.timestamp;
                request.sign = payResult.sign;
                api.sendReq(request);

                DialogUtils.dismissDialog(mProgressDialog);


            }
        });
    }

    @OnClick({R.id.wechat, R.id.underline})
    public void choose(View view) {
        switch (view.getId()) {
            case R.id.wechat:
                pay_type = "2";
                rbWechat.setChecked(true);
                rbUnderline.setChecked(false);
                Toast.makeText(getActivity(), "微信支付", Toast.LENGTH_SHORT).show();
                break;
            case R.id.underline:
                pay_type = "3";
                rbWechat.setChecked(false);
                rbUnderline.setChecked(true);
                Toast.makeText(getActivity(), "线下支付", Toast.LENGTH_SHORT).show();
                break;
        }

    }

    private void resolveArgs() {
        Bundle arguments = getArguments();
        service_id = arguments.getString("SERVICE_ID");
        studio_id = arguments.getString("STUDIO_ID");
        tech_uuid = arguments.getString("TECH_ID");
        price = arguments.getString("PRICE");
        title = arguments.getString("TITLE");
    }

    @OnClick(R.id.back)
    public void back() {
        getActivity().onBackPressed();
    }
}
