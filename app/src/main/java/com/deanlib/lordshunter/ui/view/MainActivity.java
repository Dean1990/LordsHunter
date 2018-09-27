package com.deanlib.lordshunter.ui.view;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.PopupMenu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

import com.deanlib.lordshunter.R;
import com.deanlib.lordshunter.Utils;
import com.deanlib.lordshunter.app.Constant;
import com.deanlib.lordshunter.entity.Report;
import com.deanlib.ootblite.utils.DLog;
import com.deanlib.ootblite.utils.FormatUtils;
import com.deanlib.ootblite.utils.PopupUtils;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.formatter.IValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.github.mikephil.charting.utils.ViewPortHandler;
import com.liulishuo.filedownloader.BaseDownloadTask;
import com.liulishuo.filedownloader.FileDownloadListener;
import com.liulishuo.filedownloader.FileDownloader;
import com.tbruyelle.rxpermissions2.RxPermissions;

import java.io.File;
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

    String[] mItems;
    int mSpanPosition = 2;//默认 周
    long startTime, endTime;
    long grain = 60 * 60 * 1000 * 24;//颗粒度 默认 周的颗粒度是 一天
    SimpleDateFormat mDateFormat3 = new SimpleDateFormat("yyyy/M/d");
    SimpleDateFormat mDateFormat2 = new SimpleDateFormat("M/d");
    SimpleDateFormat mDateFormat1 = new SimpleDateFormat("d");
    SimpleDateFormat mDateFormat0 = new SimpleDateFormat("H:mm");
    @BindView(R.id.scrollViewContainer)
    ScrollView scrollViewContainer;

    LineChart lineChart;
    PieChart pieChart;

    File mTraineddata;
    AlertDialog mDownloadDialog;
    ProgressBar mDownloadProgressBar;
    TextView tvProgressInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        //字库文件
        mTraineddata = Constant.APP_FILE_OCR_TRAINEDDATA;

        RxPermissions permissions = new RxPermissions(this);
        permissions.request(Manifest.permission.READ_EXTERNAL_STORAGE)
                .subscribe(granted -> {
                    if (granted) {

                    } else {
//                        PopupUtils.sendToast(R.string.permission_not_granted);
                    }

                    if (!mTraineddata.exists()){
                        new AlertDialog.Builder(this).setTitle(R.string.guide).setMessage(R.string.guide_download_data_package)
                                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        downloadDataPackage();
                                    }
                                }).setNegativeButton(R.string.cancel,null).show();
                    }
                });


        init();
        loadData();



    }

    private void init() {
        mItems = getResources().getStringArray(R.array.span);
        tvSpan.setText(mItems[mSpanPosition]);
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int week = calendar.get(Calendar.DAY_OF_WEEK);
        int date = calendar.get(Calendar.DATE);
        switch (mSpanPosition) {
            case 0:
                //年
                calendar.set(year, 0, 1, 0, 0, 0);
                startTime = calendar.getTimeInMillis();
                calendar.add(Calendar.YEAR, 1);
                endTime = calendar.getTimeInMillis();
                grain = 60 * 60 * 1000 * 24 * 15L;
                break;
            case 1:
                //月
                calendar.set(year, month, 1, 0, 0, 0);
                startTime = calendar.getTimeInMillis();
                calendar.add(Calendar.MONTH, 1);
                endTime = calendar.getTimeInMillis();
                grain = 60 * 60 * 1000 * 24L;
                break;
            case 2:
                //周
                calendar.set(year, month, date, 0, 0, 0);
                calendar.add(Calendar.DATE, -week + 1);
                startTime = calendar.getTimeInMillis();
                calendar.add(Calendar.DATE, 6);
                endTime = calendar.getTimeInMillis();
                grain = 60 * 60 * 1000 * 24L;
                break;
            case 3:
                //日
                calendar.set(year, month, date, 0, 0, 0);
                startTime = calendar.getTimeInMillis();
                calendar.add(Calendar.DATE, 1);
                endTime = calendar.getTimeInMillis();
                grain = 60 * 60 * 1000L;
                break;
        }

        tvDate.setText(mDateFormat3.format(new Date(startTime)) + (mSpanPosition != 3 ? (" - " + mDateFormat3.format(new Date(endTime))) : ""));

        scrollViewContainer.removeAllViews();
        lineChart = null;
        pieChart = null;
        System.gc();

        View view = View.inflate(this, R.layout.layout_main, null);
        lineChart = view.findViewById(R.id.lineChart);
        pieChart = view.findViewById(R.id.pieChart);
        scrollViewContainer.addView(view);
    }

    private void loadData() {
        Realm realm = Realm.getDefaultInstance();

        //线性表
        List<Entry> kills = new ArrayList<>();
        List<Entry> members = new ArrayList<>();
        for (long i = startTime; i <= endTime; i = i + grain) {
            long start = i;
            long end = i + grain;
            long killNum = realm.where(Report.class).between("timestamp", start, end).count();
            kills.add(new Entry(i, killNum));
            long memberNum = realm.where(Report.class).between("timestamp", start, end).distinct("name").count();
            members.add(new Entry(i, memberNum));
        }

//        lineChart.clear();
//        lineChart.setData(new LineData());
//        lineChart.invalidate();

        LineData lineData = new LineData();
        if (kills.size() > 0) {

            int killColor = Color.RED;
            LineDataSet killSet = new LineDataSet(kills, "kill");
            killSet.setColor(killColor);
            killSet.setValueTextColor(killColor);
            lineData.addDataSet(killSet);

            int memberColor = Color.BLUE;
            LineDataSet memberSet = new LineDataSet(members, "member");
            memberSet.setColor(memberColor);
            memberSet.setValueTextColor(memberColor);
            lineData.addDataSet(memberSet);

        }
        lineData.setValueFormatter(new IValueFormatter() {
            @Override
            public String getFormattedValue(float value, Entry entry, int dataSetIndex, ViewPortHandler viewPortHandler) {
                return "" + (int) value;
            }
        });

        lineChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        lineChart.getXAxis().setValueFormatter(new IAxisValueFormatter() {
            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                switch (mSpanPosition) {
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


        lineChart.setDescription(null);
        lineChart.setData(lineData);

        //饼图
        List<PieEntry> levels = new ArrayList<>();
        RealmResults<Report> reports = realm.where(Report.class).between("timestamp", startTime, endTime).findAll();
        int[] nums = new int[5];
        //共5个等级
        for (Report report : reports) {
            nums[report.getImage().getPreyLevel() - 1] = nums[report.getImage().getPreyLevel() - 1] + 1;
        }

        for (int i = 0; i < nums.length; i++) {
            if (nums[i] > 0) {
//               levels.add(new PieEntry(nums[i] / (float) reports.size(), "Lv." + (i + 1)));
                levels.add(new PieEntry(nums[i], "Lv." + (i + 1)));
            }
        }
        PieDataSet levelSet = new PieDataSet(levels, null);
        levelSet.setValueTextSize(14);

        levelSet.setColors(ColorTemplate.COLORFUL_COLORS);
        PieData pieData = new PieData(levelSet);
        pieData.setValueFormatter(new IValueFormatter() {
            @Override
            public String getFormattedValue(float value, Entry entry, int dataSetIndex, ViewPortHandler viewPortHandler) {
                return "" + (int) value;
            }
        });
        pieChart.setRotationEnabled(false);
        pieChart.setTouchEnabled(false);
        pieChart.setDescription(null);
        pieChart.setData(pieData);

    }

    @OnClick({R.id.tvSpan, R.id.btnShareData, R.id.btnDetail, R.id.layoutSettings})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.tvSpan:
                new AlertDialog.Builder(this).setItems(mItems, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mSpanPosition = which;
                        init();
                        loadData();
                        dialog.dismiss();
                    }
                }).show();
                break;
            case R.id.btnShareData:
                Realm realm = Realm.getDefaultInstance();
                long memberNum = realm.where(Report.class).between("timestamp", startTime, endTime).distinct("name").count();
                if (memberNum != 0) {
                    StringBuilder data = new StringBuilder(getString(R.string.share_data_template_1, tvDate.getText().toString(), memberNum) + ",");
                    long equalLv1 = 0;
                    String[] chiNum = getResources().getStringArray(R.array.chi_num);
                    for (int i = 0; i < chiNum.length; i++) {
                        int level = chiNum.length - i;
                        long count = realm.where(Report.class).between("timestamp", startTime, endTime)
                                .and().equalTo("image.preyLevel", level)
                                .count();
                        if (count != 0) {
                            data.append(getString(R.string.share_data_template_2, chiNum[i], count) + ",");
                            equalLv1 += Utils.equivalentLv1(level, count);
                        }
                    }
                    data.append(getString(R.string.share_data_template_3, equalLv1));
                    data.append(getString(R.string.share_data_template));

                    Intent share = new Intent(Intent.ACTION_SEND);
//                share.setPackage("com.tencent.mm");
                    share.setType("text/plain");
                    share.putExtra(Intent.EXTRA_TEXT, data.toString());
                    startActivity(Intent.createChooser(share, getString(R.string.share_data)));
                }else {
                    PopupUtils.sendToast(R.string.share_data_empty);
                }
                break;
            case R.id.btnDetail:
                ViewJump.toReportList(this, startTime, endTime);
                break;
            case R.id.layoutSettings:

                PopupMenu menu = new PopupMenu(this, view);
                menu.getMenuInflater().inflate(R.menu.menu_settings, menu.getMenu());
                menu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        switch (menuItem.getItemId()) {
                            case R.id.downloadData:

                                //下载字库文件
                                if (!mTraineddata.exists()) {
                                    downloadDataPackage();
                                } else {
                                    PopupUtils.sendToast(R.string.data_package_exist);
                                }
                                break;
                            case R.id.shareApp:
                                //分享应用 todo 地址没写呢
                                String data = getString(R.string.share_app_url);
                                Intent share = new Intent(Intent.ACTION_SEND);
                                //share.setPackage("com.tencent.mm");
                                share.setType("text/plain");
                                share.putExtra(Intent.EXTRA_TEXT, data);
                                startActivity(Intent.createChooser(share, getString(R.string.share_app)));
                                break;
                        }
                        return true;
                    }
                });
                menu.show();


                break;
        }

    }

    private void downloadDataPackage(){
        FileDownloader.setup(MainActivity.this);
        FileDownloader.getImpl().create("http://file2001552359.nos-eastchina1.126.net/tessdata/" + Constant.OCR_LANGUAGE + ".traineddata")
                .setPath(mTraineddata.getAbsolutePath()).setListener(new FileDownloadListener() {
            @Override
            protected void pending(BaseDownloadTask task, int soFarBytes, int totalBytes) {
                DLog.d("FileDownloadListener.pending");
                View progressView = View.inflate(MainActivity.this,R.layout.layout_progress2,null);
                mDownloadProgressBar = progressView.findViewById(R.id.progress);
                tvProgressInfo = progressView.findViewById(R.id.tvProgressInfo);
                mDownloadDialog = new AlertDialog.Builder(MainActivity.this)
                        .setTitle(getString(R.string.download_data_package)).setView(progressView)
                        .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //取消
                                FileDownloader.getImpl().pauseAll();

                                dialog.dismiss();
                            }
                        }).setCancelable(false).show();
            }

            @Override
            protected void progress(BaseDownloadTask task, int soFarBytes, int totalBytes) {
                DLog.d("FileDownloadListener.progress ->"+soFarBytes+"   total:"+totalBytes);
                if (mDownloadDialog!=null && mDownloadDialog.isShowing()
                        && mDownloadProgressBar!=null && tvProgressInfo!=null){
                    tvProgressInfo.setText(getString(R.string.doalowning_info_,
                            FormatUtils.formatFileSize(totalBytes),
                            FormatUtils.formatFileSize(soFarBytes),
                            task.getSpeed()+"KB/s"));
                    mDownloadProgressBar.setMax(totalBytes);
                    mDownloadProgressBar.setProgress(soFarBytes);
                }
            }

            @Override
            protected void completed(BaseDownloadTask task) {
                DLog.d("FileDownloadListener.completed");
                FileDownloader.getImpl().clearAllTaskData();
                if (mDownloadDialog!=null && mDownloadDialog.isShowing()){
                    PopupUtils.sendToast(R.string.download_completed);
                    mDownloadDialog.dismiss();
                }
            }

            @Override
            protected void paused(BaseDownloadTask task, int soFarBytes, int totalBytes) {
                DLog.d("FileDownloadListener.paused");
                if (mTraineddata.exists()){
                    mTraineddata.delete();
                }
                FileDownloader.getImpl().clearAllTaskData();
            }

            @Override
            protected void error(BaseDownloadTask task, Throwable e) {
                e.printStackTrace();
                DLog.d("FileDownloadListener.error");
                if (mTraineddata.exists()){
                    mTraineddata.delete();
                }
                FileDownloader.getImpl().clearAllTaskData();
                PopupUtils.sendToast(R.string.download_error);
                if (mDownloadDialog!=null && mDownloadDialog.isShowing()){
                    mDownloadDialog.dismiss();
                }
            }

            @Override
            protected void warn(BaseDownloadTask task) {
                DLog.d("FileDownloadListener.warn");
            }
        }).start();
    }


}