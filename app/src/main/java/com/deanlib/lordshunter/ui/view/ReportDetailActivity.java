package com.deanlib.lordshunter.ui.view;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.CustomViewTarget;
import com.bumptech.glide.request.transition.Transition;
import com.deanlib.lordshunter.R;
import com.deanlib.lordshunter.Utils;
import com.deanlib.lordshunter.data.entity.ImageInfo;
import com.deanlib.lordshunter.data.entity.LikeReport;
import com.deanlib.lordshunter.data.entity.Report;
import com.deanlib.ootblite.utils.PopupUtils;
import com.github.chrisbanes.photoview.PhotoView;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.realm.Realm;

public class ReportDetailActivity extends BaseActivity {

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
    @BindView(R.id.layoutLevelBlock)
    LinearLayout layoutLevelBlock;
    @BindView(R.id.layoutPreyInfo)
    LinearLayout layoutPreyInfo;
    @BindView(R.id.btnMultipleInput)
    Button btnMultipleInput;

    String mId;
    Report mReport;
    boolean isDBObj = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_detail);
        ButterKnife.bind(this);

        mId = getIntent().getStringExtra("id");
        if (!TextUtils.isEmpty(mId)) {
            Realm realm = Realm.getDefaultInstance();
            mReport = realm.where(Report.class).equalTo("id", mId).findFirst();
            if (mReport != null) {
                isDBObj = true;
                loadData();
            } else {
                PopupUtils.sendToast(R.string.invalid_id);
            }
            btnMultipleInput.setVisibility(View.GONE);
        } else {
            //从SavaActivity来
            mReport = getIntent().getParcelableExtra("report");
            if (mReport != null) {
                isDBObj = false;
                loadData();
            } else {
                PopupUtils.sendToast(R.string.invalid_id);
            }

        }

    }

    private void loadData() {
        loadTextInfo();
        Glide.with(this).load(mReport.getImage().getUri())
                .apply(new RequestOptions().placeholder(R.mipmap.default_img).error(R.mipmap.default_img))
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

    private void loadTextInfo() {
        if (mReport != null) {
            tvGroup.setText(getString(R.string.group_, mReport.getGroup()));
            tvName.setText(getString(R.string.member_, mReport.getName()));
            tvDate.setText(mReport.getDate() + " " + mReport.getTime());

            if (mReport.getImage().getAttachReports() != null && !mReport.getImage().getAttachReports().isEmpty()) {
                layoutLevelBlock.setVisibility(View.VISIBLE);
                layoutPreyInfo.setVisibility(View.GONE);
                for (int i = 0; i < layoutLevelBlock.getChildCount(); i++) {
                    if (i < mReport.getImage().getAttachReports().size()) {
                        layoutLevelBlock.getChildAt(i).setVisibility(View.VISIBLE);
                        ((TextView) layoutLevelBlock.getChildAt(i)).setText(mReport.getImage().getAttachReports().get(i).getImage().getPreyLevel() + "");
                    } else {
                        layoutLevelBlock.getChildAt(i).setVisibility(View.INVISIBLE);
                    }
                }
            } else {
                layoutLevelBlock.setVisibility(View.GONE);
                layoutPreyInfo.setVisibility(View.VISIBLE);
                tvPreyName.setText(getString(R.string.prey_name_, mReport.getImage().getPreyName()));
                tvPreyLevel.setText(getString(R.string.prey_level_, mReport.getImage().getPreyLevel()));
            }
        }
    }

    @OnClick({R.id.layoutBack, R.id.tvGroup, R.id.tvName, R.id.tvPreyName, R.id.tvPreyLevel, R.id.tvDate, R.id.btnMultipleInput})
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
                if (TextUtils.isEmpty(mId)) {
                    String[] names = getResources().getStringArray(R.array.prey_name);
                    new AlertDialog.Builder(this).setTitle(R.string.correction_prey).setItems(names, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (mReport != null) {
                                if (isDBObj) {
                                    Realm realm = Realm.getDefaultInstance();
                                    realm.beginTransaction();
                                    mReport.getImage().setPreyName(names[which]);
                                    realm.commitTransaction();
                                } else {
                                    mReport.getImage().setPreyName(names[which]);
                                }
                                loadTextInfo();
                            }
                            dialog.dismiss();
                        }
                    }).setNegativeButton(R.string.cancel, null).show();
                }
                break;
            case R.id.tvPreyLevel:
                if (TextUtils.isEmpty(mId)) {
                    String[] levels = {"1", "2", "3", "4", "5"};
                    new AlertDialog.Builder(this).setTitle(R.string.correction_level).setItems(levels, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (mReport != null) {
                                if (isDBObj) {
                                    Realm realm = Realm.getDefaultInstance();
                                    realm.beginTransaction();
                                    mReport.getImage().setPreyLevel(Integer.valueOf(levels[which]));
                                    realm.commitTransaction();
                                } else {
                                    mReport.getImage().setPreyLevel(Integer.valueOf(levels[which]));
                                }
                                loadTextInfo();
                            }
                            dialog.dismiss();
                        }
                    }).setNegativeButton(R.string.cancel, null).show();
                }
                break;
            case R.id.tvDate:
                break;
            case R.id.btnMultipleInput:
                if (TextUtils.isEmpty(mId)) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle(R.string.multiple_input);
                    View view1 = View.inflate(this, R.layout.layout_dialog_multiple_input, null);
                    ViewHolder holder = new ViewHolder(view1);
                    for (int i = 0; i < holder.layoutBlock.getChildCount(); i++) {

                        //初始化  设置当前选中数目
                        if (mReport.getImage().getAttachReports() != null && i < mReport.getImage().getAttachReports().size()) {
                            holder.layoutChecked.getChildAt(i).setVisibility(View.VISIBLE);
                            ((TextView) holder.layoutChecked.getChildAt(i)).setText(mReport.getImage().getAttachReports().get(i).getImage().getPreyLevel() + "");
                        } else {
                            holder.layoutChecked.getChildAt(i).setVisibility(View.INVISIBLE);
                        }
                        ((TextView) holder.layoutBlock.getChildAt(i)).setText((i + 1) + "");
                        if (mReport.getImage().getAttachReports() != null && mReport.getImage().getAttachReports().size() >= 5) {
                            holder.layoutBlock.getChildAt(i).setBackgroundResource(R.drawable.shape_rc_gray);
                        }

                        int finalI = i;
                        holder.layoutBlock.getChildAt(i).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                if (mReport.getImage().getAttachReports() == null) {
                                    mReport.getImage().setAttachReports(new ArrayList<>());
                                }
                                if (mReport.getImage().getAttachReports().size() < 5) {
                                    //下方的框中的被点击时，在上方框中显示一个
                                    ((TextView) holder.layoutChecked.getChildAt(mReport.getImage().getAttachReports().size())).setText((finalI + 1) + "");
                                    holder.layoutChecked.getChildAt(mReport.getImage().getAttachReports().size()).setVisibility(View.VISIBLE);

                                    LikeReport report = new LikeReport(mReport);
                                    report.getImage().setPreyName(Utils.UNDEFINDE);
                                    report.getImage().setPreyLevel(finalI + 1);
                                    mReport.getImage().getAttachReports().add(report);


                                    if (mReport.getImage().getAttachReports().size() >= 5) {
                                        for (int n = 0; n < holder.layoutBlock.getChildCount(); n++) {
                                            holder.layoutBlock.getChildAt(n).setBackgroundResource(R.drawable.shape_rc_gray);
                                        }
                                    }
                                } else {
                                    PopupUtils.sendToast(R.string.max_5);
                                }
                            }
                        });


                        holder.layoutChecked.getChildAt(i).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                if (mReport.getImage().getAttachReports().size() >= 5) {
                                    for (int n = 0; n < holder.layoutBlock.getChildCount(); n++) {
                                        holder.layoutBlock.getChildAt(n).setBackgroundResource(R.drawable.shape_rc_blue);
                                    }
                                }
                                //在选中框中的被点击，需要重新排序
                                mReport.getImage().getAttachReports().remove(finalI);

                                for (int j = 0; j < holder.layoutChecked.getChildCount(); j++) {
                                    if (j < mReport.getImage().getAttachReports().size()) {
                                        ((TextView) holder.layoutChecked.getChildAt(j)).setText(mReport.getImage().getAttachReports().get(j).getImage().getPreyLevel() + "");
                                        holder.layoutChecked.getChildAt(j).setVisibility(View.VISIBLE);
                                    } else {
                                        holder.layoutChecked.getChildAt(j).setVisibility(View.INVISIBLE);
                                    }
                                }
                            }
                        });
                    }

                    builder.setView(view1);
                    builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            //更新UI
                            loadTextInfo();
                        }
                    });
                    builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            mReport.getImage().setAttachReports(null);
                        }
                    });
                    builder.setCancelable(false);
                    builder.show();
                }
                break;
        }
    }

    @Override
    public void finish() {
        Intent data = new Intent();
        data.putExtra("report", mReport);
        setResult(RESULT_OK, data);
        super.finish();
    }

    class ViewHolder {
        @BindView(R.id.layoutChecked)
        LinearLayout layoutChecked;
        @BindView(R.id.layoutBlock)
        LinearLayout layoutBlock;

        public ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }
}
