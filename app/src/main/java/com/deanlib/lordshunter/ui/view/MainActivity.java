package com.deanlib.lordshunter.ui.view;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.webkit.WebView;
import android.widget.TextView;

import com.deanlib.lordshunter.R;
import com.deanlib.lordshunter.entity.Report;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.realm.Realm;
import io.realm.RealmResults;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.tvDate)
    TextView tvDate;
    @BindView(R.id.tvSpan)
    TextView tvSpan;
    @BindView(R.id.lineChart)
    LineChart lineChart;

    String[] mItems;
    int mSpanPosition = 2;//默认 周
    long startTime,endTime;
    long grain = 60*60*1000*24;//颗粒度 默认 周的颗粒度是 一天
    SimpleDateFormat mDateFormat3 = new SimpleDateFormat("yyyy/MM/dd");
    SimpleDateFormat mDateFormat2 = new SimpleDateFormat("MM/dd");
    SimpleDateFormat mDateFormat1 = new SimpleDateFormat("dd");
    SimpleDateFormat mDateFormat0 = new SimpleDateFormat("HH:mm");


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        init();
        loadData();
    }

    private void init(){
        mItems = getResources().getStringArray(R.array.span);
        tvSpan.setText(mItems[mSpanPosition]);
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int week = calendar.get(Calendar.DAY_OF_WEEK);
        int date = calendar.get(Calendar.DATE);
        switch (mSpanPosition){
            case 0:
                //年
                calendar.set(year,0,1,0,0,0);
                startTime = calendar.getTimeInMillis();
                calendar.add(Calendar.YEAR,1);
                endTime = calendar.getTimeInMillis();
                grain = 60*60*1000*24*30L;
                break;
            case 1:
                //月
                calendar.set(year,month,1,0,0,0);
                startTime = calendar.getTimeInMillis();
                calendar.set(Calendar.MONTH,1);
                endTime = calendar.getTimeInMillis();
                grain = 60*60*1000*24;
                break;
            case 2:
                //周
                calendar.set(year,month,date,0,0,0);
                calendar.add(Calendar.DATE,-week+1);
                startTime = calendar.getTimeInMillis();
                calendar.add(Calendar.DATE,6);
                endTime = calendar.getTimeInMillis();
                grain = 60*60*1000*24;
                break;
            case 3:
                //日
                calendar.set(year,month,date,0,0,0);
                startTime = calendar.getTimeInMillis();
                calendar.add(Calendar.DATE,1);
                endTime = calendar.getTimeInMillis();
                grain = 60*60*1000;
                break;
        }

        tvDate.setText(mDateFormat3.format(new Date(startTime))+ (mSpanPosition!=3?(" - "+mDateFormat3.format(new Date(endTime))):""));
    }

    private void loadData(){
        Realm realm = Realm.getDefaultInstance();
//        RealmResults<Report> reports = realm.where(Report.class).between("timestamp", startTime, endTime).findAll();

        List<Entry> values = new ArrayList<>();
        for (long i = startTime;i<=endTime;i = i+grain){
            long start = i;
            long end = i+grain;
            long y = realm.where(Report.class).between("timestamp", start, end).count();
            values.add(new Entry(i,y));
        }

//        lineChart.clear();
//        lineChart.setData(new LineData());
//        lineChart.invalidate();

        LineDataSet set = new LineDataSet(values,"total");
        LineData data = new LineData(set);

        lineChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        lineChart.getXAxis().setValueFormatter(new IAxisValueFormatter() {
            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                switch (mSpanPosition){
                    case 0:
                        return mDateFormat2.format(value);
                    case 1:
                        return mDateFormat1.format(value);
                    case 2:
                        return mDateFormat1.format(value);
                    case 3:
                        return mDateFormat0.format(value);
                    default:
                        return mDateFormat1.format(value);
                }

            }
        });

        lineChart.getAxisLeft().setAxisMinimum(0f);
        lineChart.getAxisLeft().setGranularity(1f);
        lineChart.getAxisRight().setEnabled(false);

        lineChart.setData(data);

    }

    @OnClick(R.id.tvSpan)
    public void onViewClicked() {
        new AlertDialog.Builder(this).setItems(mItems, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mSpanPosition = which;
                init();
                loadData();
                dialog.dismiss();
            }
        }).show();
    }
}
