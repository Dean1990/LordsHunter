package com.deanlib.lordshunter.ui.view;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.baoyz.swipemenulistview.SwipeMenu;
import com.baoyz.swipemenulistview.SwipeMenuCreator;
import com.baoyz.swipemenulistview.SwipeMenuItem;
import com.baoyz.swipemenulistview.SwipeMenuListView;
import com.deanlib.lordshunter.R;
import com.deanlib.lordshunter.Utils;
import com.deanlib.lordshunter.app.Constant;
import com.deanlib.lordshunter.data.entity.Member;
import com.deanlib.lordshunter.data.entity.Report;
import com.deanlib.lordshunter.ui.adapter.MemberReportAdapter;
import com.deanlib.ootblite.data.SharedPUtils;
import com.deanlib.ootblite.utils.DLog;
import com.deanlib.ootblite.utils.DeviceUtils;
import com.deanlib.ootblite.utils.PopupUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.realm.Realm;
import io.realm.RealmQuery;
import io.realm.RealmResults;

/**
 * 成员报名统计列表
 *
 * @author dean
 * @time 2018/10/12 下午4:21
 */
public class MemberReportListActivity extends BaseActivity {

    @BindView(R.id.listView)
    SwipeMenuListView listView;
    @BindView(R.id.tvEmpty)
    TextView tvEmpty;

    long startTime, endTime;
    List<Member> mMemberList;
    MemberReportAdapter mMemberReportAdapter;
    @BindView(R.id.imgSort)
    ImageView imgSort;

    boolean isDescSort = true;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_member_report_list);
        ButterKnife.bind(this);

        startTime = getIntent().getLongExtra("startTime", 0);
        endTime = getIntent().getLongExtra("endTime", 0);

        init();
        loadData();
    }

    private void init() {

        listView.setAdapter(mMemberReportAdapter = new MemberReportAdapter(mMemberList = new ArrayList<>()));
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ViewJump.toReportList(MemberReportListActivity.this, mMemberList.get((int) id).getGroup(), mMemberList.get((int) id).getName(), startTime, endTime);
            }
        });
        listView.setMenuCreator(new SwipeMenuCreator() {
            @Override
            public void create(SwipeMenu menu) {
                SwipeMenuItem hideItem = new SwipeMenuItem(MemberReportListActivity.this);
                hideItem.setBackground(R.color.colorAccent);
                hideItem.setWidth(DeviceUtils.dp2px(100));
                hideItem.setTitle(R.string.hide);
                hideItem.setTitleSize(20);
                hideItem.setTitleColor(getResources().getColor(R.color.textWhite));
                menu.addMenuItem(hideItem);
            }
        });
        listView.setOnMenuItemClickListener(new SwipeMenuListView.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(int position, SwipeMenu menu, int index) {
                switch (index){
                    case 0:
                        new AlertDialog.Builder(MemberReportListActivity.this).setTitle(R.string.attention)
                                .setMessage(R.string.hide_item_tag).setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (!mMemberList.get(position).isHide()) {
                                    mMemberList.get(position).setHide(true);
                                    mMemberReportAdapter.notifyDataSetChanged();
                                    SharedPUtils sharedP = new SharedPUtils();
                                    Constant.hideMemberList.add(mMemberList.get(position));
                                    sharedP.setCache("hideMember",Constant.hideMemberList);
                                }
                            }
                        }).setNegativeButton(R.string.cancel,null).show();
                        break;
                }
                return true;// false : close the menu; true : not close the menu
            }
        });

    }

    private void loadData() {
        String[] chiNum = getResources().getStringArray(R.array.chi_num);
        Realm realm = Realm.getDefaultInstance();
        List<Report> nameList = realm.copyFromRealm(realm.where(Report.class).distinct("name", "group").findAll());
        RealmResults<Report> nameResults = realm.where(Report.class).between("timestamp", startTime, endTime).distinct("name", "group").findAll();
        for (Report report : nameResults) {
            long total = 0;
            for (int j = 0; j < chiNum.length; j++) {
                int level = chiNum.length - j;
                long count = realm.where(Report.class).between("timestamp", startTime, endTime).and().equalTo("name", report.getName())
                        .and().equalTo("group", report.getGroup()).and().equalTo("image.preyLevel", level)
                        .count();
                DLog.d("name:" + report.getName() + "  level:" + level + "  count:" + count);
                if (count != 0) {
                    total += Utils.equivalentLv1(level, count);
                }
            }
            Member member = new Member(report.getName(), report.getGroup(), total);
            mMemberList.add(Utils.memberFiler(member));

            //剔除参与成员，剩下未参与活动的
            for (int j = 0; j < nameList.size(); j++) {
                if (nameList.get(j).getName().equals(report.getName())
                        && nameList.get(j).getGroup().equals(report.getGroup())) {
                    nameList.remove(j);
                    break;
                }
            }
        }

        //未参与活动成员追加到列表中
        for (Report report : nameList) {
            Member member = new Member(report.getName(), report.getGroup(), 0);
            mMemberList.add(Utils.memberFiler(member));
        }

        if (mMemberList.size() == 0) {
            tvEmpty.setVisibility(View.VISIBLE);
        } else {
            Collections.sort(mMemberList);
            tvEmpty.setVisibility(View.GONE);
            mMemberReportAdapter.notifyDataSetChanged();
        }
    }


    @OnClick({R.id.layoutBack, R.id.layoutSort})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.layoutBack:
                finish();
                break;
            case R.id.layoutSort:
                //排序
                isDescSort = !isDescSort;
                if (isDescSort) {
                    imgSort.setImageResource(R.mipmap.desc);
                    PopupUtils.sendToast(R.string.desc_order);
                } else {
                    imgSort.setImageResource(R.mipmap.esc);
                    PopupUtils.sendToast(R.string.esc_order);
                }
                Collections.reverse(mMemberList);
                mMemberReportAdapter.notifyDataSetChanged();

                break;
        }
    }
}
