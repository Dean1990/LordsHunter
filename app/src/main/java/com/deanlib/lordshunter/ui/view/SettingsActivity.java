package com.deanlib.lordshunter.ui.view;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.deanlib.lordshunter.R;
import com.deanlib.lordshunter.app.Constant;
import com.deanlib.lordshunter.data.Persistence;
import com.deanlib.lordshunter.data.entity.ImageInfo;
import com.deanlib.lordshunter.data.entity.Report;
import com.deanlib.ootblite.utils.DLog;
import com.deanlib.ootblite.utils.FormatUtils;
import com.deanlib.ootblite.utils.PopupUtils;
import com.deanlib.ootblite.utils.VersionUtils;
import com.deanlib.ootblite.utils.network.NetworkManager;
import com.liulishuo.filedownloader.BaseDownloadTask;
import com.liulishuo.filedownloader.FileDownloadListener;
import com.liulishuo.filedownloader.FileDownloader;
import com.rich.library.CalendarSelectView;
import com.rich.library.ConfirmSelectDateCallback;
import com.rich.library.DayTimeEntity;
import com.tbruyelle.rxpermissions2.RxPermissions;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.qqtheme.framework.picker.FilePicker;
import io.realm.Realm;
import io.realm.RealmResults;

/**
 * 设置
 *
 * @anthor dean
 * @time 2018/11/20 4:02 PM
 */
public class SettingsActivity extends AppCompatActivity {

    @BindView(R.id.cbOCRData)
    CheckBox cbOCRData;
    @BindView(R.id.tvVersion)
    TextView tvVersion;
    File mTraineddata;
    AlertDialog mDownloadDialog;
    ProgressBar mDownloadProgressBar;
    TextView tvProgressInfo;
    AlertDialog mDateDialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        ButterKnife.bind(this);
        boolean autoDownloadOCRData = getIntent().getBooleanExtra("autoDownloadOCRData", false);
        init();
        if (autoDownloadOCRData) {
            checkWifi2DownloadDataPackage();
        }
    }

    private void init() {
        //字库文件
        mTraineddata = Constant.APP_FILE_OCR_TRAINEDDATA;
        if (mTraineddata.exists()) {
            cbOCRData.setChecked(true);
        }
        tvVersion.setText(VersionUtils.getAppVersionName() + "(" + VersionUtils.getAppVersionCode() + ")");
    }

    @OnClick({R.id.layoutBack, R.id.layoutDownloadOCRData, R.id.layoutMemberManage, R.id.tvExportData, R.id.tvImportData, R.id.tvDeleteData, R.id.tvSpecification, R.id.tvShareApp})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.layoutBack:
                finish();
                break;
            case R.id.layoutDownloadOCRData:
                //下载字库文件
                if (!mTraineddata.exists()) {
                    checkWifi2DownloadDataPackage();
                } else {
                    PopupUtils.sendToast(R.string.data_package_exist);
                }
                break;
            case R.id.layoutMemberManage:
                //成员管理
                ViewJump.toMemberManage(this);
                break;
            case R.id.tvExportData:
                //导出猎魔数据
                RxPermissions rxPermissions1 = new RxPermissions(this);
                rxPermissions1.request(Manifest.permission.WRITE_EXTERNAL_STORAGE).subscribe(granted -> {
                    if (granted) {
                        mDateDialog = selectDateRange(new ConfirmSelectDateCallback() {
                            @Override
                            public void selectSingleDate(DayTimeEntity timeEntity) {

                            }

                            @Override
                            public void selectMultDate(DayTimeEntity startTimeEntity, DayTimeEntity endTimeEntity) {
                                DLog.d(startTimeEntity.year + "/"+startTimeEntity.month+"/"+startTimeEntity.day +"/"+startTimeEntity.listPosition+"/"+startTimeEntity.monthPosition);
                                DLog.d(endTimeEntity.year + "/"+endTimeEntity.month+"/"+endTimeEntity.day +"/"+endTimeEntity.listPosition+"/"+endTimeEntity.monthPosition);
                                if (startTimeEntity.day == 0 && endTimeEntity.day == 0){
                                    //没有选择日期
                                    PopupUtils.sendToast(R.string.select_date);
                                }else {
                                    mDateDialog.dismiss();
                                    SelectDate date = new SelectDate(startTimeEntity,endTimeEntity);
                                    new AlertDialog.Builder(SettingsActivity.this).setTitle(R.string.attention)
                                            .setMessage(getString(R.string.export_data_date_, date.getTag()))
                                            .setPositiveButton(R.string.export, new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    Realm realm = Realm.getDefaultInstance();
                                                    RealmResults<Report> reports = realm.where(Report.class).between("timestamp", date.getStartTime(), date.getEndTime()).findAll();
                                                    if (reports == null || reports.size() == 0) {
                                                        PopupUtils.sendToast(R.string.invalid_data);
                                                    } else {
                                                        FilePicker picker = new FilePicker(SettingsActivity.this, FilePicker.DIRECTORY);
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
                                                                    PopupUtils.sendToast(getString(R.string.export_data_success, filepath), Toast.LENGTH_LONG);
                                                                } else {
                                                                    PopupUtils.sendToast(R.string.export_data_fail);
                                                                }
                                                            }
                                                        });
                                                        picker.show();
                                                    }
                                                }
                                            }).setNegativeButton(R.string.cancel, null).show();
                                }
                            }
                        });

                    }
                });
                break;
            case R.id.tvImportData:
                //导入猎魔数据
                RxPermissions rxPermissions2 = new RxPermissions(this);
                rxPermissions2.request(Manifest.permission.READ_EXTERNAL_STORAGE).subscribe(granted -> {
                    if (granted) {
                        Realm realm = Realm.getDefaultInstance();
                        FilePicker picker = new FilePicker(this, FilePicker.FILE);
                        picker.setAllowExtensions(new String[]{"lhdata"});
                        picker.setFillScreen(true);
                        picker.setOnFilePickListener(new FilePicker.OnFilePickListener() {
                            @Override
                            public void onFilePicked(String currentPath) {
                                List<Report> reports = Persistence.importData(currentPath);
                                if (reports != null && reports.size() > 0) {
                                    int insertCount = 0;
                                    int repetCount = 0;
                                    realm.beginTransaction();
                                    for (Report report : reports) {
                                        ImageInfo find = realm.where(ImageInfo.class)
                                                .equalTo("md5", report.getImage().getMd5())
                                                .findFirst();
                                        if (find == null) {
//                                                                    report.setId(UUID.randomUUID().toString());
//                                                                    report.getImage().setId(UUID.randomUUID().toString());
                                            realm.copyToRealm(report);
                                            insertCount++;
                                        } else {
                                            //重复
                                            DLog.d(report.getImage().toString());
                                            DLog.d(find.toString());
                                            repetCount++;
                                        }
                                    }
                                    realm.commitTransaction();
                                    PopupUtils.sendToast(getString(R.string.import_data_success, reports.size(), insertCount, repetCount), Toast.LENGTH_LONG);
                                } else {
                                    PopupUtils.sendToast(R.string.invalid_data);
                                }
                            }
                        });
                        picker.show();
                    }
                });
                break;
            case R.id.tvDeleteData:
                mDateDialog = selectDateRange(new ConfirmSelectDateCallback() {
                    @Override
                    public void selectSingleDate(DayTimeEntity timeEntity) {

                    }

                    @Override
                    public void selectMultDate(DayTimeEntity startTimeEntity, DayTimeEntity endTimeEntity) {
                        if (startTimeEntity.day == 0 && endTimeEntity.day == 0){
                            //没有选择日期
                            PopupUtils.sendToast(R.string.select_date);
                        }else {
                            mDateDialog.dismiss();
                            SelectDate date = new SelectDate(startTimeEntity, endTimeEntity);
                            new AlertDialog.Builder(SettingsActivity.this).setTitle(R.string.attention)
                                    .setMessage(getString(R.string.delete_data_tag, date.getTag()))
                                    .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            Realm realm1 = Realm.getDefaultInstance();
                                            RealmResults<Report> reports = realm1.where(Report.class).between("timestamp", date.getStartTime(), date.getEndTime()).findAll();
                                            realm1.executeTransaction(new Realm.Transaction() {
                                                @Override
                                                public void execute(Realm realm) {
                                                    for (Report report : reports) {
                                                        ImageInfo imageInfo = realm.where(ImageInfo.class)
                                                                .equalTo("md5", report.getImage().getMd5())
                                                                .findFirst();
                                                        imageInfo.deleteFromRealm();
                                                    }
                                                    reports.deleteAllFromRealm();
                                                    PopupUtils.sendToast(R.string.delete_success);
                                                }
                                            });
                                        }
                                    }).setNegativeButton(R.string.cancel, null).show();
                        }
                    }
                });

                break;
            case R.id.tvSpecification:
                //使用说明
                //地址
                ViewJump.toWebView(this, Constant.README_URL_HEADER + "readme_" + Constant.OCR_LANGUAGE + ".html");
                break;
            case R.id.tvShareApp:
                //分享应用
                String data = getString(R.string.share_app_url);
                Intent share = new Intent(Intent.ACTION_SEND);
                //share.setPackage("com.tencent.mm");
                share.setType("text/plain");
                share.putExtra(Intent.EXTRA_TEXT, data);
                startActivity(Intent.createChooser(share, getString(R.string.share_app)));
                break;
        }
    }

    private void checkWifi2DownloadDataPackage() {
        if (NetworkManager.getAPNType(this) != NetworkManager.TYPE_WIFI) {
            new AlertDialog.Builder(this).setTitle(R.string.attention)
                    .setMessage(R.string.attention_not_wifi)
                    .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            downloadDataPackage();
                        }
                    }).setNegativeButton(R.string.cancel, null)
                    .show();
        } else {
            downloadDataPackage();
        }
    }

    private void downloadDataPackage() {
        FileDownloader.setup(SettingsActivity.this);
        FileDownloader.getImpl().create("http://file2001552359.nos-eastchina1.126.net/tessdata/" + Constant.OCR_LANGUAGE + ".traineddata")
                .setPath(mTraineddata.getAbsolutePath()).setListener(new FileDownloadListener() {
            @Override
            protected void pending(BaseDownloadTask task, int soFarBytes, int totalBytes) {
                DLog.d("FileDownloadListener.pending");
                View progressView = View.inflate(SettingsActivity.this, R.layout.layout_progress2, null);
                mDownloadProgressBar = progressView.findViewById(R.id.progress);
                tvProgressInfo = progressView.findViewById(R.id.tvProgressInfo);
                mDownloadDialog = new AlertDialog.Builder(SettingsActivity.this)
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
                DLog.d("FileDownloadListener.progress ->" + soFarBytes + "   total:" + totalBytes);
                if (mDownloadDialog != null && mDownloadDialog.isShowing()
                        && mDownloadProgressBar != null && tvProgressInfo != null) {
                    tvProgressInfo.setText(getString(R.string.doalowning_info_,
                            FormatUtils.formatFileSize(totalBytes),
                            FormatUtils.formatFileSize(soFarBytes),
                            task.getSpeed() + "KB/s"));
                    mDownloadProgressBar.setMax(totalBytes);
                    mDownloadProgressBar.setProgress(soFarBytes);
                }
            }

            @Override
            protected void completed(BaseDownloadTask task) {
                DLog.d("FileDownloadListener.completed");
                FileDownloader.getImpl().clearAllTaskData();
                if (mDownloadDialog != null && mDownloadDialog.isShowing()) {
                    PopupUtils.sendToast(R.string.download_completed);
                    mDownloadDialog.dismiss();
                }
            }

            @Override
            protected void paused(BaseDownloadTask task, int soFarBytes, int totalBytes) {
                DLog.d("FileDownloadListener.paused");
                if (mTraineddata.exists()) {
                    mTraineddata.delete();
                }
                FileDownloader.getImpl().clearAllTaskData();
            }

            @Override
            protected void error(BaseDownloadTask task, Throwable e) {
                e.printStackTrace();
                DLog.d("FileDownloadListener.error");
                if (mTraineddata.exists()) {
                    mTraineddata.delete();
                }
                FileDownloader.getImpl().clearAllTaskData();
                PopupUtils.sendToast(R.string.download_error);
                if (mDownloadDialog != null && mDownloadDialog.isShowing()) {
                    mDownloadDialog.dismiss();
                }
            }

            @Override
            protected void warn(BaseDownloadTask task) {
                DLog.d("FileDownloadListener.warn");
            }
        }).start();
    }

    /**
     * 选择日期区间
     */
    private AlertDialog selectDateRange(ConfirmSelectDateCallback callback) {
        View dateLayout = View.inflate(this, R.layout.layout_date_mult, null);
        CalendarSelectView csvDatePicker = dateLayout.findViewById(R.id.csvDatePicker);
        csvDatePicker.setConfirmCallback(callback);
//        csvDatePicker.setCalendarRange(Calendar.getInstance().add(Calendar.MONTH,-12),Calendar.getInstance(),DayTimeEntity);
        return new AlertDialog.Builder(this).setView(dateLayout).show();
    }

    class SelectDate{
        long startTime;
        long endTime;
        String tag;

        public SelectDate(DayTimeEntity startTimeEntity,DayTimeEntity endTimeEntity){

            Calendar calendar = Calendar.getInstance();
                if (startTimeEntity.day == 0 ^ endTimeEntity.day == 0) {
                    //只选择了一个日期 就只取一天的数据
                    DayTimeEntity dayTime = startTimeEntity;
                    if (startTimeEntity.day == 0){
                        dayTime = endTimeEntity;
                    }
                    calendar.set(dayTime.year, dayTime.month, dayTime.day, 0, 0, 0);
                    startTime = calendar.getTimeInMillis();
                    calendar.add(Calendar.DAY_OF_MONTH, 1);
                    endTime = calendar.getTimeInMillis();
                    tag = getString(R.string._y_m_d, dayTime.year, dayTime.month, dayTime.day);
                } else {
                    calendar.set(startTimeEntity.year, startTimeEntity.month , startTimeEntity.day, 0, 0, 0);
                    startTime = calendar.getTimeInMillis();
                    calendar.set(endTimeEntity.year, endTimeEntity.month, endTimeEntity.day, 0, 0, 0);
                    calendar.add(Calendar.DAY_OF_MONTH, 1);
                    endTime = calendar.getTimeInMillis();
                    tag = getString(R.string._y_m_d, startTimeEntity.year, startTimeEntity.month+1, startTimeEntity.day)
                            + " - " + getString(R.string._y_m_d, endTimeEntity.year, endTimeEntity.month+1, endTimeEntity.day) ;
                }

        }

        public long getStartTime() {
            return startTime;
        }

        public void setStartTime(long startTime) {
            this.startTime = startTime;
        }

        public long getEndTime() {
            return endTime;
        }

        public void setEndTime(long endTime) {
            this.endTime = endTime;
        }

        public String getTag() {
            return tag;
        }

        public void setTag(String tag) {
            this.tag = tag;
        }
    }
}
