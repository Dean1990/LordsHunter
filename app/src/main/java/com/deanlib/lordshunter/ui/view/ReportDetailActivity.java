package com.deanlib.lordshunter.ui.view;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.CustomViewTarget;
import com.bumptech.glide.request.transition.Transition;
import com.deanlib.lordshunter.R;
import com.deanlib.lordshunter.entity.Report;
import com.deanlib.ootblite.utils.PopupUtils;
import com.github.chrisbanes.photoview.PhotoView;

import org.greenrobot.eventbus.Subscribe;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.realm.ObjectChangeSet;
import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmModel;
import io.realm.RealmObject;
import io.realm.RealmObjectChangeListener;

public class ReportDetailActivity extends AppCompatActivity {

    @BindView(R.id.photoView)
    PhotoView photoView;
    @BindView(R.id.tvName)
    TextView tvName;
    @BindView(R.id.tvPreyName)
    TextView tvPreyName;
    @BindView(R.id.tvPreyLevel)
    TextView tvPreyLevel;
    @BindView(R.id.tvDate)
    TextView tvDate;
    @BindView(R.id.tvGroup)
    TextView tvGroup;

    String mId;
    Report mReport;
    boolean isDBObj = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_detail);
        ButterKnife.bind(this);

        mId = getIntent().getStringExtra("id");
        if (!TextUtils.isEmpty(mId)){
            Realm realm = Realm.getDefaultInstance();
            mReport = realm.where(Report.class).equalTo("id", mId).findFirst();
            if (mReport!=null) {
                isDBObj = true;
                loadData();
            }else {
                PopupUtils.sendToast(R.string.invalid_id);
            }
        }else {
            //从SavaActivity来
            mReport = getIntent().getParcelableExtra("report");
            if (mReport!=null){
                isDBObj = false;
                loadData();
            }else {
                PopupUtils.sendToast(R.string.invalid_id);
            }

        }

    }

    private void loadData(){
        loadTextInfo();
        Glide.with(this).load(mReport.getImage().getUri())
                .apply(new RequestOptions().placeholder(R.mipmap.ic_launcher).error(R.mipmap.ic_launcher))
                .into(new CustomViewTarget(photoView) {
                    @Override
                    public void onLoadFailed(@Nullable Drawable errorDrawable) {

                    }

                    @Override
                    public void onResourceReady(@NonNull Object resource, @Nullable Transition transition) {
                        photoView.setImageDrawable((Drawable) resource);
                    }

                    @Override
                    protected void onResourceCleared(@Nullable Drawable placeholder) {

                    }
                });
    }

    private void loadTextInfo(){
        if (mReport!=null) {
            tvGroup.setText(getString(R.string.group_,mReport.getGroup()));
            tvName.setText(getString(R.string.member_,mReport.getName()));
            tvDate.setText(mReport.getDate() + " " + mReport.getTime());
            tvPreyName.setText(getString(R.string.prey_name_, mReport.getImage().getPreyName()));
            tvPreyLevel.setText(getString(R.string.prey_level_, mReport.getImage().getPreyLevel()));
        }
    }

    @OnClick({R.id.layoutBack, R.id.tvGroup,R.id.tvName, R.id.tvPreyName, R.id.tvPreyLevel, R.id.tvDate})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.layoutBack:
                finish();
                break;
            case R.id.tvGroup:
                break;
            case R.id.tvName:
                break;
            case R.id.tvPreyName:
                String[] names = getResources().getStringArray(R.array.prey_name);
                new AlertDialog.Builder(this).setTitle(R.string.correction_prey).setItems(names, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (mReport!=null) {
                            if (isDBObj) {
                                Realm realm = Realm.getDefaultInstance();
                                realm.beginTransaction();
                                mReport.getImage().setPreyName(names[which]);
                                realm.commitTransaction();
                            }else {
                                mReport.getImage().setPreyName(names[which]);
                            }
                            loadTextInfo();
                        }
                        dialog.dismiss();
                    }
                }).setNegativeButton(R.string.cancel,null).show();
                break;
            case R.id.tvPreyLevel:
                String[] levels = {"1","2","3","4","5"};
                new AlertDialog.Builder(this).setTitle(R.string.correction_level).setItems(levels, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (mReport!=null) {
                            if (isDBObj) {
                                Realm realm = Realm.getDefaultInstance();
                                realm.beginTransaction();
                                mReport.getImage().setPreyLevel(Integer.valueOf(levels[which]));
                                realm.commitTransaction();
                            }else {
                                mReport.getImage().setPreyLevel(Integer.valueOf(levels[which]));
                            }
                            loadTextInfo();
                        }
                        dialog.dismiss();
                    }
                }).setNegativeButton(R.string.cancel,null).show();
                break;
            case R.id.tvDate:
                break;
        }
    }

    @Override
    public void finish() {
        Intent data = new Intent();
        data.putExtra("report",mReport);
        setResult(RESULT_OK,data);
        super.finish();
    }
}
