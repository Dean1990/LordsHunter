package com.deanlib.lordshunter.ui.view;

import android.Manifest;
import android.app.AlertDialog;

import android.app.ProgressDialog;

import android.content.DialogInterface;
import android.content.Intent;

import android.net.Uri;

import android.os.Bundle;

import android.support.annotation.Nullable;

import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;


import com.baoyz.swipemenulistview.SwipeMenu;
import com.baoyz.swipemenulistview.SwipeMenuCreator;
import com.baoyz.swipemenulistview.SwipeMenuItem;
import com.baoyz.swipemenulistview.SwipeMenuListView;
import com.deanlib.lordshunter.R;
import com.deanlib.lordshunter.Utils;
import com.deanlib.lordshunter.app.Constant;
import com.deanlib.lordshunter.data.entity.Report;
import com.deanlib.lordshunter.event.CollectTaskEvent;
import com.deanlib.lordshunter.service.CollectTaskService;
import com.deanlib.lordshunter.ui.adapter.ReportAdapter;
import com.deanlib.ootblite.data.SharedPUtils;
import com.deanlib.ootblite.utils.DeviceUtils;
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
public class SavaActivity extends BaseActivity {

    public static boolean isRunForeground = false;

    String text;
    ArrayList<Uri> images;
    @BindView(R.id.listView)
    SwipeMenuListView listView;
    List<Report> mReportList;
    List<Report> mDataReportList;//从通知传过来的数据
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

        getData(getIntent());

    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        getData(intent);
    }

    private void getData(Intent data){
        //String subject = intent.getStringExtra(Intent.EXTRA_SUBJECT);
        text = data.getStringExtra(Intent.EXTRA_TEXT);
        images = data.getParcelableArrayListExtra(Intent.EXTRA_STREAM);

        //从通知传过来的数据
        mDataReportList = data.getParcelableArrayListExtra("reports");
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
        listView.setMenuCreator(new SwipeMenuCreator() {
            @Override
            public void create(SwipeMenu menu) {
                SwipeMenuItem deleteItem = new SwipeMenuItem(SavaActivity.this);
                deleteItem.setBackground(R.color.colorAccent);
                deleteItem.setWidth(DeviceUtils.dp2px(100));
                deleteItem.setTitle(R.string.delete);
                deleteItem.setTitleSize(20);
                deleteItem.setTitleColor(getResources().getColor(R.color.textWhite));
                menu.addMenuItem(deleteItem);
            }
        });
        listView.setOnMenuItemClickListener(new SwipeMenuListView.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(int position, SwipeMenu menu, int index) {
                switch (index){
                    case 0:
                        new AlertDialog.Builder(SavaActivity.this).setTitle(R.string.attention)
                                .setMessage(R.string.delete_item_tag).setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                mReportList.remove(position);
                                mReportAdapter.notifyDataSetChanged();
                            }
                        }).setNegativeButton(R.string.cancel,null).show();
                        break;
                }
                return true;// false : close the menu; true : not close the menu
            }
        });
    }

    private void loadData() {
        if (mDataReportList != null) {
            //重复性验证
            EventBus.getDefault().post(new CollectTaskEvent(CollectTaskEvent.ACTION_MESSAGE,getString(R.string.repet_data)));
            List<Report> reports = Utils.checkRepet(mDataReportList);
            mReportList.addAll(reports);
            mReportAdapter.notifyDataSetChanged();
            btnSave.setEnabled(true);
            EventBus.getDefault().post(new CollectTaskEvent(CollectTaskEvent.ACTION_COMPLETE,null));

//            Observable.create(new ObservableOnSubscribe<List<Report>>() {
//                @Override
//                public void subscribe(ObservableEmitter<List<Report>> emitter) throws Exception {
//                    //重复性验证
//                    EventBus.getDefault().post(new CollectTaskEvent(CollectTaskEvent.ACTION_MESSAGE,getString(R.string.repet_data)));
//                    List<Report> reports = Utils.checkRepet(mDataReportList);
//                    emitter.onNext(reports);
//                    emitter.onComplete();
//                }
//            }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
//                    .subscribe(new Observer<List<Report>>() {
//                @Override
//                public void onSubscribe(Disposable d) {
//
//                }
//
//                @Override
//                public void onNext(List<Report> reports) {
//                    mReportList.addAll(reports);
//                    mReportAdapter.notifyDataSetChanged();
//                    btnSave.setEnabled(true);
//                }
//
//                @Override
//                public void onError(Throwable e) {
//                    e.printStackTrace();
//                    EventBus.getDefault().post(new CollectTaskEvent(CollectTaskEvent.ACTION_ERROR,e.getMessage()));
//                }
//
//                @Override
//                public void onComplete() {
//                    EventBus.getDefault().post(new CollectTaskEvent(CollectTaskEvent.ACTION_COMPLETE,null));
//                }
//            });

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
                int unidentificationPosition = -1;//未识别的list的位置
                for (int i = 0;i < mReportList.size();i++) {
                    if (mReportList.get(i).getStatus() == Report.STATUS_NEW && (mReportList.get(i).getImage().getPreyLevel() == 0 || mReportList.get(i).getImage().getPreyName() == null)) {
                        unidentificationPosition = i;
                        break;
                    }
                }
                if (unidentificationPosition>=0){
                    listView.setSelection(unidentificationPosition);
                    PopupUtils.sendToast(R.string.unidentification_tag);
                }else {
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

                    //清缓存
                    SharedPUtils sharedPUtils = new SharedPUtils();
                    sharedPUtils.remove("unsavareports");

                }
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
                mDataReportList = reports;
                mReportList.clear();
                loadData();
                break;
            case CollectTaskEvent.ACTION_MESSAGE:
                if (mDialog != null && mDialog.isShowing()) {
                    mDialog.dismiss();
                }
                String msg2 = (String) event.getObj();
                mDialog = ProgressDialog.show(this,"",msg2);
                break;
            case CollectTaskEvent.ACTION_SERVICE_MESSAGE:
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
