package com.deanlib.lordshunter.ui.view;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.baoyz.swipemenulistview.SwipeMenu;
import com.baoyz.swipemenulistview.SwipeMenuCreator;
import com.baoyz.swipemenulistview.SwipeMenuItem;
import com.baoyz.swipemenulistview.SwipeMenuListView;
import com.deanlib.lordshunter.R;
import com.deanlib.lordshunter.Utils;
import com.deanlib.lordshunter.app.Constant;
import com.deanlib.lordshunter.data.entity.Member;
import com.deanlib.lordshunter.data.entity.Report;
import com.deanlib.lordshunter.ui.adapter.MemberEditAdapter;
import com.deanlib.ootblite.data.SharedPUtils;
import com.deanlib.ootblite.utils.DeviceUtils;
import com.deanlib.ootblite.utils.PopupUtils;

import java.security.PublicKey;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.realm.Realm;
import io.realm.RealmResults;

/**
 * 成员管理
 *
 * @anthor dean
 * @time 2018/11/21 3:35 PM
 */
public class MemberManageActivity extends AppCompatActivity {

    @BindView(R.id.layoutBack)
    LinearLayout layoutBack;
    @BindView(R.id.listView)
    SwipeMenuListView listView;
    @BindView(R.id.imgMerge)
    ImageView imgMerge;
    @BindView(R.id.imgSearch)
    ImageView imgSearch;

    List<Member> mMemberList;
    MemberEditAdapter mMeberEditAdapter;
    int mMergePosition;
    List<Member> mMergeList;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_member_manage);
        ButterKnife.bind(this);
        init();
        loadData();
    }

    private void init(){
        listView.setAdapter(mMeberEditAdapter = new MemberEditAdapter(mMemberList = new ArrayList<>()));
        listView.setMenuCreator(new SwipeMenuCreator() {
            @Override
            public void create(SwipeMenu menu) {
                SwipeMenuItem hideItem = new SwipeMenuItem(MemberManageActivity.this);
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
                switch (index){
                    case 0:
                        new AlertDialog.Builder(MemberManageActivity.this).setTitle(R.string.attention)
                                .setMessage(menu.getViewType() == Member.STATE_HIDE?R.string.show_item_tag:R.string.hide_item_tag).setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (menu.getViewType() == Member.STATE_HIDE) {
                                    mMemberList.get(position).setHide(false);
                                    for (int i = 0; i < Constant.hideMemberList.size(); i++) {
                                        if (Constant.hideMemberList.get(i).getName().equals(mMemberList.get(position).getName())
                                                && Constant.hideMemberList.get(i).getGroup().equals(mMemberList.get(position).getGroup())) {
                                            Constant.hideMemberList.remove(i);
                                            break;
                                        }
                                    }
                                } else {
                                    mMemberList.get(position).setHide(true);
                                    Member member = mMemberList.get(position);
                                    Constant.hideMemberList.add(member);
                                }
                                mMeberEditAdapter.notifyDataSetChanged();
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

    private void loadData(){
        Realm realm = Realm.getDefaultInstance();
        List<Report> nameList = realm.copyFromRealm(realm.where(Report.class).distinct("name", "group").findAll());
        for (Report report : nameList) {
            Member member = new Member(report.getName(), report.getGroup(), 0);
            mMemberList.add(Utils.memberFiler(member));
        }
        mMeberEditAdapter.notifyDataSetChanged();
    }

    @OnClick({R.id.layoutBack, R.id.layoutMerge, R.id.imgSearch})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.layoutBack:
                finish();
                break;
            case R.id.layoutMerge:
                mMergeList = new ArrayList<>();
                for (Member member : mMemberList){
                    if (member.isChecked()){
                        mMergeList.add(member);
                    }
                }
                if (mMergeList.size() < 2){
                    PopupUtils.sendToast(R.string.member_merge_tag1);
                    return;
                }
                String[] names = new String[mMergeList.size()];
                for (int i = 0;i < mMergeList.size(); i++){
                    names[i] = "["+mMergeList.get(i).getGroup()+"] " + mMergeList.get(i).getName();
                }
                mMergePosition = 0;
                new AlertDialog.Builder(this).setTitle(R.string.member_merge_tag2)
                        .setSingleChoiceItems(names,0, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                mMergePosition = which;
                            }
                        })
                        .setPositiveButton(R.string.merge, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //合并
                                if (mMergePosition>=0){
                                    Realm realm = Realm.getDefaultInstance();
                                    realm.beginTransaction();
                                    for (int i = 0;i < mMergeList.size();i++){
                                        if (i != mMergePosition) {
                                            RealmResults<Report> all = realm.where(Report.class).equalTo("name", mMergeList.get(i).getName())
                                                    .and().equalTo("group", mMergeList.get(i).getGroup())
                                                    .findAll();
                                            for (Report report : all){
                                                report.setName(mMergeList.get(mMergePosition).getName());
                                                report.setGroup(mMergeList.get(mMergePosition).getGroup());
                                            }
                                        }
                                    }
                                    realm.commitTransaction();
                                    init();
                                    loadData();
                                    PopupUtils.sendToast(R.string.merge_complete);
                                }else {
                                    PopupUtils.sendToast(R.string.member_merge_tag2);
                                }
                            }
                        }).setNegativeButton(R.string.cancel,null).show();
                break;
            case R.id.imgSearch:
                break;
        }
    }
}
