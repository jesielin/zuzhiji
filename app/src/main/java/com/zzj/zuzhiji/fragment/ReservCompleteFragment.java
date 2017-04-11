package com.zzj.zuzhiji.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
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
import com.zzj.zuzhiji.util.SharedPreferencesUtils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.Subscriber;

/**
 * Created by shawn on 17/4/10.
 */

public class ReservCompleteFragment extends Fragment implements DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener {

    private String title;
    private String price;
    private String service_id;
    private String uuid;

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

    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm",
            Locale.getDefault());
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
    public void choose_date(View view) {


        Calendar future = Calendar.getInstance();
        future.add(Calendar.DAY_OF_YEAR, 7);
        dpd.setMaxDate(future);
        dpd.setMinDate(now);
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
    public void back(View view) {
        getActivity().onBackPressed();
    }


    private int year;
    private int month;
    private int day;
    private int minute;
    private int hour;
    private boolean isSetTime;

    @Override
    public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth) {

        this.year = year;
        this.month = monthOfYear;
        this.day = dayOfMonth;

        Calendar calendar = Calendar.getInstance();
        if (dayOfMonth == calendar.get(Calendar.DAY_OF_MONTH)) {
            calendar.add(Calendar.HOUR_OF_DAY, 1);
            tpd.setMinTime(calendar.get(Calendar.HOUR_OF_DAY), 0, 0);
        } else {
            tpd.setMinTime(0, 0, 0);
        }

        tpd.show(getActivity().getFragmentManager(), "Timepickerdialog");

    }

    @Override
    public void onTimeSet(TimePickerDialog view, int hourOfDay, int minute, int second) {
        isSetTime = true;
        this.hour = hourOfDay;
        this.minute = minute;
        Date date = new Date(this.year, this.month, this.day, this.hour, this.minute, 0);

        String format = sdf.format(date);
        tvTime.setText(format);
    }
}
