package com.deanlib.lordshunter.ui.adapter;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.deanlib.lordshunter.R;
import com.deanlib.lordshunter.data.entity.Member;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MemberReportAdapter extends BaseAdapter {

    List<Member> mMemberList;

    public MemberReportAdapter(List<Member> mMemberList) {
        this.mMemberList = mMemberList;
    }

    @Override
    public int getCount() {
        return mMemberList.size();
    }

    @Override
    public Object getItem(int position) {
        return mMemberList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public int getItemViewType(int position) {
        return mMemberList.get(position).isHide()?Member.STATE_HIDE:Member.STATE_SHOW;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = View.inflate(parent.getContext(), R.layout.layout_member_report_item, null);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        }else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.tvName.setText(mMemberList.get(position).getName());
        holder.tvGroup.setText(mMemberList.get(position).getGroup());
        holder.tvCount.setText(mMemberList.get(position).getCount()+"");

        if (mMemberList.get(position).isHide()){
            holder.tvName.setTextColor(parent.getContext().getResources().getColor(R.color.textGray));
            holder.tvGroup.setTextColor(parent.getContext().getResources().getColor(R.color.textGrayTint));
            holder.tvCount.setTextColor(parent.getContext().getResources().getColor(R.color.textBlueTint));
        }else {
            holder.tvName.setTextColor(parent.getContext().getResources().getColor(R.color.textBlack));
            holder.tvGroup.setTextColor(parent.getContext().getResources().getColor(R.color.textGray));
            holder.tvCount.setTextColor(parent.getContext().getResources().getColor(R.color.textBlue));
        }

        return convertView;
    }

    static class ViewHolder {
        @BindView(R.id.tvName)
        TextView tvName;
        @BindView(R.id.tvGroup)
        TextView tvGroup;
        @BindView(R.id.tvCount)
        TextView tvCount;
        @BindView(R.id.layoutView)
        LinearLayout layoutView;

        ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }
}
