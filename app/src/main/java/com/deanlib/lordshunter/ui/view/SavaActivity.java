package com.deanlib.lordshunter.ui.view;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.ListView;

import com.deanlib.lordshunter.R;
import com.deanlib.lordshunter.Utils;
import com.deanlib.lordshunter.entity.Report;
import com.deanlib.lordshunter.ui.adapter.ReportAdapter;
import com.deanlib.ootblite.utils.PopupUtils;
import com.tbruyelle.rxpermissions2.RxPermissions;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import io.realm.Progress;
import io.realm.Realm;

/**
 * 接收并保存数据
 *
 * @author dean
 * @time 2018/9/4 下午5:26
 */
public class SavaActivity extends AppCompatActivity {

    String text;
    ArrayList<Uri> images;
    @BindView(R.id.listView)
    ListView listView;
    List<Report> mReportList;
    ReportAdapter mReportAdapter;

    static ProgressDialog mDialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sava);
        ButterKnife.bind(this);

        Intent intent = getIntent();
//        String subject = intent.getStringExtra(Intent.EXTRA_SUBJECT);
        text = intent.getStringExtra(Intent.EXTRA_TEXT);
        images = intent.getParcelableArrayListExtra(Intent.EXTRA_STREAM);

        init();
        if (!TextUtils.isEmpty(text) && images != null && images.size() > 0) {
            RxPermissions permissions = new RxPermissions(this);
            permissions.request(Manifest.permission.READ_EXTERNAL_STORAGE)
                    .subscribe(granted -> {
                        if (granted){
                            loadData();
                        }else {
                            PopupUtils.sendToast(R.string.permission_not_granted);
                        }
                    });
        } else {
            PopupUtils.sendToast(R.string.error_data);
        }

    }

    private void init() {
        listView.setAdapter(mReportAdapter = new ReportAdapter(mReportList = new ArrayList<>()));
    }

    private void loadData() {
        Observable.create(new ObservableOnSubscribe<List<Report>>() {
            @Override
            public void subscribe(ObservableEmitter<List<Report>> emitter) throws Exception {
                //解析
                mHandler.sendMessage(mHandler.obtainMessage(SEND_MESSAGE, getString(R.string.parse_data)));
                List<Report> reports = Utils.parseText(text, images);
                //重复性验证
                mHandler.sendMessage(mHandler.obtainMessage(SEND_MESSAGE, getString(R.string.repet_data)));
                List<Report> reports1 = Utils.checkRepet(reports);
                //文字识别
                mHandler.sendMessage(mHandler.obtainMessage(SEND_MESSAGE, getString(R.string.text_extraction)));
                List<Report> reports2 = Utils.ocr(reports1);

                emitter.onNext(reports2);
                emitter.onComplete();
            }
        }).subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<List<Report>>() {

                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(List<Report> reports) {
                        if (reports != null && reports.size() > 0) {
                            mReportList.addAll(reports);
                            mReportAdapter.notifyDataSetChanged();
                        }
                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onComplete() {
                        mHandler.sendEmptyMessage(DISMISS);
                    }
                });
    }

    public static final int SEND_MESSAGE = 1;
    public static final int DISMISS = 2;
    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case SEND_MESSAGE:
                    if (mDialog == null || !mDialog.isShowing()) {
                        mDialog = ProgressDialog.show(SavaActivity.this, "", (String) msg.obj);
                    } else {
                        mDialog.setMessage((String) msg.obj);
                    }

                    break;
                case DISMISS:
                    if (mDialog != null && mDialog.isShowing()) {
                        mDialog.dismiss();
                    }
                    break;
            }
        }
    };


    @OnClick({R.id.layoutHome, R.id.tvSave})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.layoutHome:
                //home
                ViewJump.toMain(this);
                finish();
                break;
            case R.id.tvSave:
                //保存数据到数据库
                new AlertDialog.Builder(this).setTitle(R.string.save)
                        .setMessage(R.string.save_tag)
                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ProgressDialog dialog1 = ProgressDialog.show(SavaActivity.this,"",getString(R.string.save_data));
                                Realm realm = Realm.getDefaultInstance();
                                realm.executeTransaction(new Realm.Transaction() {
                                    @Override
                                    public void execute(Realm realm) {
                                        for (Report report : mReportList){
                                            if (report.getStatus()==Report.STATUS_NEW){
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
                        }).setNegativeButton(R.string.cancel,null).show();

                break;
        }
    }
}
