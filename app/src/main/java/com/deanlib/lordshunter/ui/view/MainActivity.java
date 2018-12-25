package com.deanlib.lordshunter.ui.view;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.DatePicker;
import android.widget.ScrollView;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.deanlib.lordshunter.R;
import com.deanlib.lordshunter.Utils;
import com.deanlib.lordshunter.app.Constant;
import com.deanlib.lordshunter.data.entity.Report;
import com.deanlib.ootblite.OotbConfig;
import com.deanlib.ootblite.data.SharedPUtils;
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

public class MainActivity extends BaseActivity {

    @BindView(R.id.tvDate)
    TextView tvDate;
    @BindView(R.id.tvSpan)
    TextView tvSpan;

    String[] mItems;
    int mSpanPosition = 2;//默认 周
    long startTime, endTime;
    long grain = 60 * 60 * 1000 * 24;//颗粒度 默认 周的颗粒度是 一天
    SimpleDateFormat mDateFormat5 = new SimpleDateFormat("yyyy/MM");
    SimpleDateFormat mDateFormat4 = new SimpleDateFormat("MM/dd");
    SimpleDateFormat mDateFormat3 = new SimpleDateFormat("yyyy/MM/dd");
    SimpleDateFormat mDateFormat2 = new SimpleDateFormat("M");
    SimpleDateFormat mDateFormat1 = new SimpleDateFormat("d");
    SimpleDateFormat mDateFormat0 = new SimpleDateFormat("H:mm");
    @BindView(R.id.scrollViewContainer)
    ScrollView scrollViewContainer;

    LineChart lineChart;
    PieChart pieChart;

    File mTraineddata;
    Calendar mCalendar;
    long selectTime;//选中日期

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        selectTime = System.currentTimeMillis();

        //字库文件
        mTraineddata = Utils.getOCR(Constant.OCR_LANGUAGE).getFile();

        if (!mTraineddata.exists()) {
            //第一次使用
            OotbConfig.task().postDelayed(new Runnable() {
                @Override
                public void run() {
                    ViewJump.toWebView(MainActivity.this, Constant.README_URL_HEADER + "readme_"+Constant.OCR_LANGUAGE+".html");
                }
            },2000);
        }

        RxPermissions permissions = new RxPermissions(this);
        permissions.request(Manifest.permission.READ_EXTERNAL_STORAGE)
                .subscribe(granted -> {
                    if (granted) {

                    } else {
//                        PopupUtils.sendToast(R.string.permission_not_granted);
                    }

                    if (!mTraineddata.exists()) {
                        new AlertDialog.Builder(MainActivity.this).setTitle(R.string.guide).setMessage(R.string.guide_download_data_package)
                                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        ViewJump.toOCRManage(MainActivity.this,true);
                                    }
                                }).setNegativeButton(R.string.cancel, null).show();
                    }
                });

        //查看是否有缓存
        SharedPUtils sharedPUtils = new SharedPUtils();
        List<Report> reports = JSON.parseArray(sharedPUtils.getCache("unsavareports"), Report.class);
        if (reports != null) {
            new AlertDialog.Builder(this).setTitle(R.string.attention).setMessage(R.string.data_not_save)
                    .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            startActivity(ViewJump.getSaveIntent(MainActivity.this, reports));
                        }
                    }).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    sharedPUtils.remove("unsavareports");
                }
            }).show();
        }

        OotbConfig.task().post(new Runnable() {
            @Override
            public void run() {
                //广告
                String adShowDate = sharedPUtils.getCache("ad_show_date");
                if (!FormatUtils.convertDateTimestampToString(System.currentTimeMillis(),FormatUtils.DATE_FORMAT_YMD).equals(adShowDate))
                    startActivity(new Intent(MainActivity.this,AdActivity.class));
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        init();
        loadData();
    }

    private void init() {
        mItems = getResources().getStringArray(R.array.span);
        tvSpan.setText(mItems[mSpanPosition]);
        if (mCalendar == null) {
            mCalendar = Calendar.getInstance();
        }
        int year = mCalendar.get(Calendar.YEAR);
        int month = mCalendar.get(Calendar.MONTH);
        int week = mCalendar.get(Calendar.DAY_OF_WEEK);
        int date = mCalendar.get(Calendar.DATE);
        mCalendar.set(year, month, date, 0, 0, 0);
        mCalendar.set(Calendar.MILLISECOND, 0);
        switch (mSpanPosition) {
            case 0:
                //年
                mCalendar.set(year, 0, 1);
                startTime = mCalendar.getTimeInMillis();
                mCalendar.add(Calendar.YEAR, 1);
                endTime = mCalendar.getTimeInMillis()-1;
                grain = 60 * 60 * 1000 * 24 * 30L;
                tvDate.setText(mDateFormat5.format(new Date(startTime)) + " - " + mDateFormat5.format(new Date(endTime)));
                break;
            case 1:
                //月
                mCalendar.set(year, month, 1);
                startTime = mCalendar.getTimeInMillis();
                mCalendar.add(Calendar.MONTH, 1);
                endTime = mCalendar.getTimeInMillis() - 1;
                grain = 60 * 60 * 1000 * 24L;
                tvDate.setText(mDateFormat4.format(new Date(startTime)) + " - " + mDateFormat4.format(new Date(endTime)));
                break;
            case 2:
                //周
                mCalendar.set(year, month, date);
                mCalendar.add(Calendar.DATE, -week + 1);
                startTime = mCalendar.getTimeInMillis();
                mCalendar.add(Calendar.DATE, 7);
                endTime = mCalendar.getTimeInMillis()-1;
                grain = 60 * 60 * 1000 * 24L;
                tvDate.setText(mDateFormat4.format(new Date(startTime)) + " - " + mDateFormat4.format(new Date(endTime)));
                break;
            case 3:
                //日
                mCalendar.set(year, month, date, 0, 0, 0);
                startTime = mCalendar.getTimeInMillis();
                mCalendar.add(Calendar.DATE, 1);
                endTime = mCalendar.getTimeInMillis()-1;
                grain = 60 * 60 * 1000L;
                tvDate.setText(mDateFormat3.format(new Date(startTime)));
                break;
        }

        //还原成初始日期
        mCalendar.set(year, month, date, 0, 0, 0);

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
        List<Entry> equivalents = new ArrayList<>();//等效1级
        String[] chiNum = getResources().getStringArray(R.array.chi_num);
        for (long i = startTime; i <= endTime; i = i + grain) {
            DLog.d("value:" + i);
            long start = i;
            long end = i + grain;
            float x = (int) (i / grain) + 1;
            long killCount = realm.where(Report.class).between("timestamp", start, end).count();
            kills.add(new Entry(x, killCount));

            long memberCount = realm.where(Report.class).between("timestamp", start, end).distinct("name","group").count();
            members.add(new Entry(x, memberCount));

            long euivalentCount = 0;
            for (int j = 0; j < chiNum.length; j++) {
                int level = chiNum.length - j;
                long count = realm.where(Report.class).between("timestamp", start, end)
                        .and().equalTo("image.preyLevel", level)
                        .count();
                if (count != 0) {
                    euivalentCount += Utils.equivalentLv1(level, count);
                }
            }
            equivalents.add(new Entry(x,euivalentCount));

        }

        LineData lineData = new LineData();
        if (kills.size() > 0) {

            int killColor = Color.RED;
            LineDataSet killSet = new LineDataSet(kills, "Kill");
            killSet.setColor(killColor);
            killSet.setValueTextColor(killColor);
            lineData.addDataSet(killSet);

            int memberColor = Color.BLUE;
            LineDataSet memberSet = new LineDataSet(members, "Member");
            memberSet.setColor(memberColor);
            memberSet.setValueTextColor(memberColor);
            lineData.addDataSet(memberSet);

            int equivalentColor = Color.GREEN;
            LineDataSet equivalentSet = new LineDataSet(equivalents, "Eq Lv.1");
            equivalentSet.setColor(equivalentColor);
            equivalentSet.setValueTextColor(equivalentColor);
            lineData.addDataSet(equivalentSet);

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
                long longValue = ((long) value) * grain;
                switch (mSpanPosition) {
                    case 0:
                        return mDateFormat2.format(longValue);
                    case 1:
                        return mDateFormat1.format(longValue);
                    case 2:
                        return mDateFormat1.format(longValue);
                    case 3:
                        return mDateFormat0.format(longValue);
                    default:
                        return mDateFormat1.format(longValue);
                }
//                return value+"";

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
            int i = report.getImage().getPreyLevel() - 1;
            if (i < 0)
                i = 0;
            else if (i > 4)
                i = 4;
            nums[i] = nums[i] + 1;
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

    @OnClick({R.id.tvSpan, R.id.btnShareData, R.id.btnDetail, R.id.layoutSettings, R.id.tvDate})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.tvDate:
                if (mCalendar == null)
                    mCalendar = Calendar.getInstance();
                mCalendar.setTimeInMillis(selectTime);
                View dateLayout = View.inflate(this, R.layout.layout_date, null);
                DatePicker datePicker = dateLayout.findViewById(R.id.datePicker);
                datePicker.init(mCalendar.get(Calendar.YEAR), mCalendar.get(Calendar.MONTH), mCalendar.get(Calendar.DATE),
                        new DatePicker.OnDateChangedListener() {
                            @Override
                            public void onDateChanged(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                                mCalendar.set(year, monthOfYear, dayOfMonth, 0, 0, 0);
                                selectTime = mCalendar.getTimeInMillis();
                            }
                        });
                new AlertDialog.Builder(this).setView(dateLayout)
                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                init();
                                loadData();
                            }
                        })
                        .setNegativeButton(R.string.cancel, null).show();
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
//                    new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
//                        @Override
//                        public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
//                            mCalendar.set(year, month, dayOfMonth, 0, 0, 0);
//                            init();
//                            loadData();
//                        }
//                    },mCalendar.get(Calendar.YEAR),mCalendar.get(Calendar.MONTH),mCalendar.get(Calendar.DATE)).show();
//                }
                break;
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
                long memberNum = realm.where(Report.class).between("timestamp", startTime, endTime).distinct("name","group").count();
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
                } else {
                    PopupUtils.sendToast(R.string.share_data_empty);
                }
                break;
            case R.id.btnDetail:
                ViewJump.toMemberReportList(this, startTime, endTime);
                break;
            case R.id.layoutSettings:
                ViewJump.toSettings(this);
                /*PopupMenu menu = new PopupMenu(this, view);
                menu.getMenuInflater().inflate(R.menu.menu_settings, menu.getMenu());
                menu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        switch (menuItem.getItemId()) {
                            case R.id.downloadData:
                                //下载字库文件
                                if (!mTraineddata.exists()) {
                                    checkWifi2DownloadDataPackage();
                                } else {
                                    PopupUtils.sendToast(R.string.data_package_exist);
                                }
                                break;
                            case R.id.exportData:
                                //导出猎魔数据
                                RxPermissions rxPermissions1 = new RxPermissions(MainActivity.this);
                                rxPermissions1.request(Manifest.permission.WRITE_EXTERNAL_STORAGE).subscribe(granted->{
                                    if (granted){
                                        new AlertDialog.Builder(MainActivity.this).setTitle(R.string.attention)
                                                .setMessage(getString(R.string.export_data_date_,tvDate.getText().toString()))
                                                .setPositiveButton(R.string.export, new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                Realm realm = Realm.getDefaultInstance();
                                                RealmResults<Report> reports = realm.where(Report.class).between("timestamp", startTime, endTime).findAll();
                                                if (reports == null || reports.size()==0){
                                                    PopupUtils.sendToast(R.string.invalid_data);
                                                }else {
                                                    FilePicker picker = new FilePicker(MainActivity.this, FilePicker.DIRECTORY);
                                                    picker.setFillScreen(true);
                                                    picker.setOnFilePickListener(new FilePicker.OnFilePickListener() {
                                                        @Override
                                                        public void onFilePicked(String currentPath) {
                                                            //realm 区分托管对象和非托管对象，托管对象及查询出的对象，自动更新，自动持久化
                                                            //非托管对象及java new出的普通对象
                                                            //二者可以通过Realm.copyToRealm和Realm.copyFromRealm相互转换
                                                            //托管对象是不能直接访问成员变量的，会返回null,访问需要使用get方式，当Realm.close()的时候，托管对象失效，访问get方法会报错。
                                                            List<Report> temp = realm.copyFromRealm(reports);
                                                            String filepath = Persistence.exportData(temp, currentPath);
                                                            if (filepath != null) {
                                                                PopupUtils.sendToast(getString(R.string.export_data_success, filepath),Toast.LENGTH_LONG);
                                                            } else {
                                                                PopupUtils.sendToast(R.string.export_data_fail);
                                                            }
                                                        }
                                                    });
                                                    picker.show();
                                                }
                                            }
                                        }).setNegativeButton(R.string.cancel,null).show();
                                    }
                                });

                                break;
                            case R.id.importData:
                                //导入猎魔数据
                                RxPermissions rxPermissions2 = new RxPermissions(MainActivity.this);
                                rxPermissions2.request(Manifest.permission.READ_EXTERNAL_STORAGE).subscribe(granted->{
                                    if (granted){
                                        Realm realm = Realm.getDefaultInstance();
                                        FilePicker picker = new FilePicker(MainActivity.this,FilePicker.FILE);
                                        picker.setAllowExtensions(new String[]{"lhdata"});
                                        picker.setFillScreen(true);
                                        picker.setOnFilePickListener(new FilePicker.OnFilePickListener() {
                                            @Override
                                            public void onFilePicked(String currentPath) {
                                                List<Report> reports = Persistence.importData(currentPath);
                                                if (reports!=null && reports.size()>0){
                                                    int insertCount = 0;
                                                    int repetCount = 0;
                                                    realm.beginTransaction();
                                                    for (Report report : reports) {
                                                        ImageInfo find = realm.where(ImageInfo.class)
                                                                .equalTo("md5", report.getImage().getMd5())
                                                                .findFirst();
                                                        if (find==null) {
//                                                                    report.setId(UUID.randomUUID().toString());
//                                                                    report.getImage().setId(UUID.randomUUID().toString());
                                                            realm.copyToRealm(report);
                                                            insertCount++;
                                                        }else {
                                                            //重复
                                                            DLog.d(report.getImage().toString());
                                                            DLog.d(find.toString());
                                                            repetCount++;
                                                        }
                                                    }
                                                    realm.commitTransaction();
                                                    init();
                                                    loadData();
                                                    PopupUtils.sendToast(getString(R.string.import_data_success,reports.size(),insertCount,repetCount), Toast.LENGTH_LONG);
                                                }else {
                                                    PopupUtils.sendToast(R.string.invalid_data);
                                                }
                                            }
                                        });
                                        picker.show();
                                    }
                                });
                                break;
                            case R.id.deleteData:
                                new AlertDialog.Builder(MainActivity.this).setTitle(R.string.attention)
                                        .setMessage(getString(R.string.delete_data_tag,tvDate.getText().toString()))
                                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                Realm realm1 = Realm.getDefaultInstance();
                                                RealmResults<Report> reports = realm1.where(Report.class).between("timestamp", startTime, endTime).findAll();
                                                realm1.executeTransaction(new Realm.Transaction() {
                                                    @Override
                                                    public void execute(Realm realm) {
                                                        for (Report report : reports){
                                                            ImageInfo imageInfo = realm.where(ImageInfo.class)
                                                                    .equalTo("md5", report.getImage().getMd5())
                                                                    .findFirst();
                                                            imageInfo.deleteFromRealm();
                                                        }
                                                        reports.deleteAllFromRealm();
                                                        init();
                                                        loadData();
                                                        PopupUtils.sendToast(R.string.delete_success);
                                                    }
                                                });
                                            }
                                        }).setNegativeButton(R.string.cancel,null).show();
                                break;
                            case R.id.specification:
                                //使用说明
                                //地址
                                ViewJump.toWebView(MainActivity.this, Constant.README_URL_HEADER + "readme_"+Constant.OCR_LANGUAGE+".html");
                                break;
                            case R.id.shareApp:
                                //分享应用
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
                menu.show();*/

                break;
        }

    }




}
