package com.deanlib.lordshunter.ui.view;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.deanlib.lordshunter.R;
import com.deanlib.lordshunter.entity.Report;
import com.deanlib.lordshunter.ui.adapter.ReportAdapter;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnLoadMoreListener;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;

public class ReportListActivity extends AppCompatActivity {

    @BindView(R.id.listView)
    ListView listView;
    @BindView(R.id.srl)
    SmartRefreshLayout srl;
    @BindView(R.id.tvEmpty)
    TextView tvEmpty;

    long startTime, endTime;
    ReportAdapter mReportAdapter;
    List<Report> mReportList;
    RealmResults<Report> mReports;
//    int mPage = 0;
//    int mPageSize = 20;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_list);
        ButterKnife.bind(this);

        startTime = getIntent().getLongExtra("startTime",0);
        endTime = getIntent().getLongExtra("endTime",0);
        init();
        loadData();
    }

    private void init(){
        listView.setAdapter(mReportAdapter = new ReportAdapter(mReportList = new ArrayList<>()));
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ViewJump.toReportDetail(ReportListActivity.this,mReportList.get((int)id).getId());
            }
        });
        srl.setEnableRefresh(false);
        srl.setEnableLoadMore(false);
//        srl.setOnLoadMoreListener(new OnLoadMoreListener() {
//            @Override
//            public void onLoadMore(@NonNull RefreshLayout refreshLayout) {
//                mPage++;
//                loadData();
//            }
//        });
    }

    private void loadData(){
        if (mReports==null) {
            Realm realm = Realm.getDefaultInstance();
            mReports = realm.where(Report.class).between("timestamp", startTime, endTime).findAll();
            mReports.addChangeListener(new RealmChangeListener<RealmResults<Report>>() {
                @Override
                public void onChange(RealmResults<Report> reports) {
                    mReports = reports;
                    loadData();
                }
            });
        }

//        for (int i = mPage*mPageSize;i<(mPage+1)*mPageSize && i<reports.size();i++){
//            mReportList.add(reports.get(i));
//        }
        if (mReports.size() == 0){
            tvEmpty.setVisibility(View.VISIBLE);
        }else {
            tvEmpty.setVisibility(View.GONE);
            mReportList.addAll(mReports);
            mReportAdapter.notifyDataSetChanged();
//        srl.finishLoadMore();
        }
    }

    @OnClick(R.id.layoutBack)
    public void onViewClicked() {
        finish();
    }
}
