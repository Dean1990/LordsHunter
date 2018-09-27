package com.deanlib.lordshunter.ui.view;

import android.Manifest;
import android.app.AlertDialog;

import android.app.ProgressDialog;

import android.content.DialogInterface;
import android.content.Intent;

import android.net.Uri;

import android.os.Bundle;

import android.support.annotation.Nullable;

import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;


import com.deanlib.lordshunter.R;
import com.deanlib.lordshunter.app.Constant;
import com.deanlib.lordshunter.entity.Report;
import com.deanlib.lordshunter.event.CollectTaskEvent;
import com.deanlib.lordshunter.service.CollectTaskService;
import com.deanlib.lordshunter.ui.adapter.ReportAdapter;
import com.deanlib.ootblite.utils.PopupUtils;
import com.tbruyelle.rxpermissions2.RxPermissions;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.realm.Realm;

/**
 * 接收并保存数据
 *
 * @author dean
 * @time 2018/9/4 下午5:26
 */
public class SavaActivity extends AppCompatActivity {

    public static boolean isRunForeground = false;

    String text;
    ArrayList<Uri> images;
    @BindView(R.id.listView)
    ListView listView;
    List<Report> mReportList;
    List<Report> mDataReportList;
    ReportAdapter mReportAdapter;
    @BindView(R.id.btnSave)
    Button btnSave;

    static AlertDialog mDialog;
    int mClickPosition = -1;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sava);
        ButterKnife.bind(this);

        Intent intent = getIntent();
//        String subject = intent.getStringExtra(Intent.EXTRA_SUBJECT);
        text = intent.getStringExtra(Intent.EXTRA_TEXT);
        images = intent.getParcelableArrayListExtra(Intent.EXTRA_STREAM);

        //从广播传过来的数据
        mDataReportList = intent.getParcelableArrayListExtra("reports");

        init();
        RxPermissions permissions = new RxPermissions(this);
        permissions.request(Manifest.permission.READ_EXTERNAL_STORAGE)
                .subscribe(granted -> {
                    if (granted) {
                        loadData();
                    } else {
                        PopupUtils.sendToast(R.string.permission_not_granted);
                    }
                });

    }

    private void init() {
        btnSave.setEnabled(false);
        listView.setAdapter(mReportAdapter = new ReportAdapter(mReportList = new ArrayList<>()));
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mClickPosition = (int) id;
                ViewJump.toReportDetail(SavaActivity.this, mReportList.get(mClickPosition));
            }
        });
    }

    private void loadData() {
        if (mDataReportList != null) {
            mReportList.addAll(mDataReportList);
            mReportAdapter.notifyDataSetChanged();
            btnSave.setEnabled(true);
        } else if (!TextUtils.isEmpty(text) && images != null && images.size() > 0) {
            File traineddata = Constant.APP_FILE_OCR_TRAINEDDATA;
            if (traineddata.exists()) {
                Intent intent = new Intent(this, CollectTaskService.class);
                intent.putExtra("text", text);
                intent.putExtra("images", images);
                startService(intent);
            } else {
                //不存在，去下载
                PopupUtils.sendToast(R.string.data_package_not_exist);
            }
        } else {
            PopupUtils.sendToast(R.string.error_data);
        }

    }


    @OnClick({R.id.layoutHome, R.id.btnSave})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.layoutHome:
                //home
                ViewJump.toMain(this);
                finish();
                break;
            case R.id.btnSave:
                //保存数据到数据库
                new AlertDialog.Builder(this).setTitle(R.string.save)
                        .setMessage(R.string.save_tag)
                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ProgressDialog dialog1 = ProgressDialog.show(SavaActivity.this, "", getString(R.string.save_data));
                                Realm realm = Realm.getDefaultInstance();
                                realm.executeTransaction(new Realm.Transaction() {
                                    @Override
                                    public void execute(Realm realm) {
                                        for (Report report : mReportList) {
                                            if (report.getStatus() == Report.STATUS_NEW) {
                                                report.setId(UUID.randomUUID().toString());
                                                report.getImage().setId(UUID.randomUUID().toString());
                                                realm.copyToRealm(report);
                                            }
                                        }
                                    }
                                });
                                dialog1.dismiss();
                                PopupUtils.sendToast(R.string.save_success);
                                ViewJump.toMain(SavaActivity.this);
                                finish();
                            }
                        }).setNegativeButton(R.string.cancel, null).show();

                break;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        isRunForeground = true;
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        isRunForeground = false;
        EventBus.getDefault().unregister(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onCollectTaskEvent(CollectTaskEvent event) {

        switch (event.getAction()) {
            case CollectTaskEvent.ACTION_UPDATE_UI:
                List<Report> reports = (List<Report>) event.getObj();
                if (mReportList != null && mReportAdapter != null) {
                    mReportList.addAll(reports);
                    mReportAdapter.notifyDataSetChanged();
                    btnSave.setEnabled(true);
                }
                break;
            case CollectTaskEvent.ACTION_MESSAGE:
                String msg = (String) event.getObj();
                if (mDialog == null || !mDialog.isShowing()) {
                    View progressView = View.inflate(this, R.layout.layout_progress, null);
                    mDialog = new AlertDialog.Builder(this).setMessage(msg).setView(progressView)
                            .setCancelable(false).setPositiveButton(R.string.to_background, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    PopupUtils.sendToast(R.string.background_task_notify);
                                    dialog.dismiss();
                                    finish();
                                }
                            }).show();
                } else {
                    mDialog.setMessage(msg);
                }
                break;
            case CollectTaskEvent.ACTION_COMPLETE:
                if (mDialog != null && mDialog.isShowing()) {
                    mDialog.dismiss();
                }
                break;
            case CollectTaskEvent.ACTION_ERROR:
                if (mDialog != null && mDialog.isShowing()) {
                    mDialog.dismiss();
                }
                String errorMsg = (String) event.getObj();
                PopupUtils.sendToast(errorMsg);
                break;

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case ViewJump.CODE_REPORT_SAVE_TO_DETAIL:
                    if (data != null && listView != null && mReportList != null && mReportAdapter != null && mClickPosition >= 0) {
                        Report report = data.getParcelableExtra("report");
                        if (report != null) {
                            mReportList.remove(mClickPosition);
                            mReportList.add(mClickPosition, report);
                            mReportAdapter.notifyDataSetChanged();
                        }
                    }
                    mClickPosition = -1;
                    break;
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
