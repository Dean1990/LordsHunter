package com.deanlib.lordshunter.ui.adapter;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.baoyz.swipemenulistview.SwipeMenuListView;
import com.deanlib.lordshunter.R;
import com.deanlib.lordshunter.data.entity.Member;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MemberEditAdapter extends BaseAdapter {

    List<Member> mMemberList;

    public MemberEditAdapter(List<Member> mMemberList) {
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
            convertView = View.inflate(parent.getContext(), R.layout.layout_member_item, null);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        }else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.tvName.setText(mMemberList.get(position).getName());
        holder.tvGroup.setText(mMemberList.get(position).getGroup());
        holder.checkbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mMemberList.get(position).setChecked(isChecked);
            }
        });
        holder.imgState.setImageResource(mMemberList.get(position).isHide()?R.drawable.hide_black :R.drawable.show_black);
        holder.imgState.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((SwipeMenuListView)parent).smoothOpenMenu(position);
            }
        });

        if (mMemberList.get(position).isHide()){
            holder.tvName.setTextColor(parent.getContext().getResources().getColor(R.color.textGray));
            holder.tvGroup.setTextColor(parent.getContext().getResources().getColor(R.color.textGrayTint));
        }else {
            holder.tvName.setTextColor(parent.getContext().getResources().getColor(R.color.textBlack));
            holder.tvGroup.setTextColor(parent.getContext().getResources().getColor(R.color.textGray));
        }

        return convertView;
    }

    static class ViewHolder {
        @BindView(R.id.checkbox)
        CheckBox checkbox;
        @BindView(R.id.tvName)
        TextView tvName;
        @BindView(R.id.tvGroup)
        TextView tvGroup;
        @BindView(R.id.imgState)
        ImageView imgState;
        @BindView(R.id.layoutView)
        LinearLayout layoutView;

        ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }
}