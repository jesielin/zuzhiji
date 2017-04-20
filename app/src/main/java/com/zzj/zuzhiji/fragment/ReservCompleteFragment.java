package com.zzj.zuzhiji.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog;
import com.zzj.zuzhiji.R;
import com.zzj.zuzhiji.app.Constant;
import com.zzj.zuzhiji.network.Network;
import com.zzj.zuzhiji.util.DebugLog;
import com.zzj.zuzhiji.util.SharedPreferencesUtils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.Subscriber;

/**
 * Created by shawn on 17/4/10.
 */

public class ReservCompleteFragment extends BaseFragment implements DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener {

    @BindView(R.id.title)
    TextView tvTitle;
    @BindView(R.id.price)
    TextView tvPrice;
    @BindView(R.id.time)
    TextView tvTime;
    Calendar now = Calendar.getInstance();
    DatePickerDialog dpd = DatePickerDialog.newInstance(
            ReservCompleteFragment.this,
            now.get(Calendar.YEAR),
            now.get(Calendar.MONTH),
            now.get(Calendar.DAY_OF_MONTH)
    );
    TimePickerDialog tpd = TimePickerDialog.newInstance(
            ReservCompleteFragment.this,
            now.get(Calendar.HOUR_OF_DAY),
            now.get(Calendar.MINUTE),
            true
    );
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
    private String title;
    private String price;
    private String service_id;
    private String uuid;
    private int year = 0;
    private int month = 0;
    private int day = 0;
    private int minute = 0;
    private int hour = 0;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View contentView = View.inflate(getActivity(), R.layout.fragment_reserv_complete, null);
        ButterKnife.bind(this, contentView);

        resolveArgs();

        return contentView;
    }

    @OnClick(R.id.complete)
    public void complete(View view) {

        if (TextUtils.isEmpty(tvTime.getText().toString().trim())) {
            Toast.makeText(getActivity(), "请选择时间.", Toast.LENGTH_SHORT).show();
            return;
        }


        DebugLog.e("time:"+tvTime.getText().toString());
        Network.getInstance().reserv(SharedPreferencesUtils.getInstance().getValue(Constant.SHARED_KEY.UUID),
                uuid,
                tvTime.getText().toString(),
                service_id)
                .subscribe(new Subscriber<Object>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();

                    }

                    @Override
                    public void onNext(Object o) {

                    }
                });
    }

    @OnClick(R.id.choose_date)
    public void choose_date() {


        Calendar future = Calendar.getInstance();
        future.add(Calendar.DAY_OF_YEAR, 8);
        Calendar rightnow = Calendar.getInstance();
        rightnow.add(Calendar.DAY_OF_YEAR,1);
        dpd.setMaxDate(future);
        dpd.setMinDate(rightnow);
        dpd.show(getActivity().getFragmentManager(), "Datepickerdialog");
    }

    private void resolveArgs() {
        Bundle arguments = getArguments();
        service_id = arguments.getString("ID");
        uuid = arguments.getString("UUID");
        price = arguments.getString("PRICE");
        title = arguments.getString("TITLE");

        tvTitle.setText(title);
        tvPrice.setText(price);

    }

    @OnClick(R.id.back)
    public void back() {
        getActivity().onBackPressed();
    }

    @Override
    public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth) {

        this.year = year;
        this.month = monthOfYear;
        this.day = dayOfMonth;


        DebugLog.e("year:"+year);
        tpd.show(getActivity().getFragmentManager(), "Timepickerdialog");

    }

    @Override
    public void onTimeSet(TimePickerDialog view, int hourOfDay, int minute, int second) {

        this.hour = hourOfDay;
        this.minute = minute;
        Date date = new Date(this.year-1900, this.month, this.day, this.hour, this.minute, 0);

        int year = date.getYear();
        DebugLog.e("date year:"+year);
        String format = sdf.format(date);
        DebugLog.e("format:"+format);
        tvTime.setText(format);

    }
}
