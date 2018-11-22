package com.deanlib.lordshunter.ui.view;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

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
import io.realm.RealmResults;
import q.rorbin.badgeview.Badge;
import q.rorbin.badgeview.QBadgeView;

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
    @BindView(R.id.imgSort)
    ImageView imgSort;
    @BindView(R.id.imgHide)
    ImageView imgHide;
    @BindView(R.id.layoutHide)
    LinearLayout layoutHide;

    long startTime, endTime;
    List<Member> mMemberList;//总列表
    List<Member> mShowMemberList;//显示用的列表
    MemberReportAdapter mMemberReportAdapter;

    boolean isDescSort = true;//降序
    boolean isHided = true;


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
        mMemberList = new ArrayList<>();
        listView.setAdapter(mMemberReportAdapter = new MemberReportAdapter(mShowMemberList = new ArrayList<>()));
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ViewJump.toReportList(MemberReportListActivity.this, mShowMemberList.get((int) id).getGroup(), mShowMemberList.get((int) id).getName(), startTime, endTime);
            }
        });
        listView.setMenuCreator(new SwipeMenuCreator() {
            @Override
            public void create(SwipeMenu menu) {
                SwipeMenuItem hideItem = new SwipeMenuItem(MemberReportListActivity.this);
                hideItem.setBackground(R.color.colorAccent);
                hideItem.setWidth(DeviceUtils.dp2px(100));
                hideItem.setTitle(menu.getViewType() == Member.STATE_HIDE ? R.string.show : R.string.hide);
                hideItem.setTitleSize(18);
                hideItem.setTitleColor(getResources().getColor(R.color.textWhite));
                menu.addMenuItem(hideItem);
            }
        });
        listView.setOnMenuItemClickListener(new SwipeMenuListView.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(int position, SwipeMenu menu, int index) {
                switch (index) {
                    case 0:
                        new AlertDialog.Builder(MemberReportListActivity.this).setTitle(R.string.attention)
                                .setMessage(menu.getViewType() == Member.STATE_HIDE?R.string.show_item_tag:R.string.hide_item_tag).setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (menu.getViewType() == Member.STATE_HIDE) {
                                    mShowMemberList.get(position).setHide(false);
                                    for (int i = 0; i < Constant.hideMemberList.size(); i++) {
                                        if (Constant.hideMemberList.get(i).getName().equals(mShowMemberList.get(position).getName())
                                                && Constant.hideMemberList.get(i).getGroup().equals(mShowMemberList.get(position).getGroup())) {
                                            Constant.hideMemberList.remove(i);
                                            break;
                                        }
                                    }
                                } else {
                                    mShowMemberList.get(position).setHide(true);
                                    Member member = mShowMemberList.get(position);
                                    if (isHided) {
                                        mShowMemberList.remove(position);
                                    }
                                    Constant.hideMemberList.add(member);
                                }
                                mMemberReportAdapter.notifyDataSetChanged();
                                checkHideMemberHaveData();
                                SharedPUtils sharedP = new SharedPUtils();
                                sharedP.setCache("hideMember", Constant.hideMemberList);
                            }
                        }).setNegativeButton(R.string.cancel, null).show();
                        break;
                }
                return true;// false : close the menu; true : not close the menu
            }
        });

    }

    private void loadData() {
        mMemberList.clear();
        mShowMemberList.clear();
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

        //当前参加活动的成员中是否带隐藏属性的
        checkHideMemberHaveData();

        //未参与活动成员追加到列表中
        for (Report report : nameList) {
            Member member = new Member(report.getName(), report.getGroup(), 0);
            mMemberList.add(Utils.memberFiler(member));
        }

        if (mMemberList.size() != 0){
            if (isHided) {
                for (Member member : mMemberList) {
                    if (!member.isHide()) {
                        mShowMemberList.add(member);
                    }
                }
            }else {
                mShowMemberList.addAll(mMemberList);
            }
        }

        if (mShowMemberList.size() == 0) {
            tvEmpty.setVisibility(View.VISIBLE);
        } else {
            tvEmpty.setVisibility(View.GONE);
            Collections.sort(mShowMemberList);
            mMemberReportAdapter.notifyDataSetChanged();
        }
    }


    @OnClick({R.id.layoutBack, R.id.layoutSort,R.id.layoutHide})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.layoutBack:
                finish();
                break;
            case R.id.layoutSort:
                //排序
                isDescSort = !isDescSort;
                if (isDescSort) {
                    //降序
                    imgSort.setImageResource(R.mipmap.desc);
                    PopupUtils.sendToast(R.string.desc_order);
                } else {
                    imgSort.setImageResource(R.mipmap.esc);
                    PopupUtils.sendToast(R.string.esc_order);
                }
                Collections.reverse(mShowMemberList);
                mMemberReportAdapter.notifyDataSetChanged();

                break;
            case R.id.layoutHide:
                //隐藏/显示
                mShowMemberList.clear();
                isHided = !isHided;
                if (isHided){
                    //隐藏
                    for (Member member : mMemberList) {
                        if (!member.isHide()) {
                            mShowMemberList.add(member);
                        }
                    }
                    imgHide.setImageResource(R.drawable.hide);
                    PopupUtils.sendToast(R.string.hide);
                }else {
                    //显示
                    mShowMemberList.addAll(mMemberList);
                    imgHide.setImageResource(R.drawable.show);
                    PopupUtils.sendToast(R.string.show);
                }

                Collections.sort(mShowMemberList);
                if (!isDescSort) {
                    //升序
                    Collections.reverse(mShowMemberList);
                }
                mMemberReportAdapter.notifyDataSetChanged();
                break;
        }
    }

    Badge mHideBadge;
    /**
     * 检查当前参加活动的成员中是否带隐藏属性的
     */
    private void checkHideMemberHaveData(){
        if (mHideBadge == null) {
            mHideBadge = new QBadgeView(this).bindTarget(imgHide);
        }
        boolean isHideMemberHaveData = false;//隐藏成员有数据时
        for (Member member : mMemberList){
            if (member.isHide() && member.getCount()!=0){
                isHideMemberHaveData = true;
                break;
            }
        }
        mHideBadge.setBadgeNumber(isHideMemberHaveData ? -1 : 0);

    }

}
