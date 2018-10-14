package com.deanlib.lordshunter.ui.adapter;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.deanlib.lordshunter.R;
import com.deanlib.lordshunter.data.entity.Report;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ReportAdapter extends BaseAdapter {
    List<Report> list;

    public ReportAdapter(List<Report> list) {
        this.list = list;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = View.inflate(parent.getContext(), R.layout.layout_report_item, null);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        Glide.with(convertView).load(list.get(position).getImage().getUri())
                .apply(new RequestOptions().placeholder(R.mipmap.default_img).error(R.mipmap.default_img))
                .into(holder.imgCover);
        holder.tvGroup.setText(list.get(position).getGroup());
        holder.tvName.setText(list.get(position).getName());
        holder.tvDate.setText(list.get(position).getDate() + " " + list.get(position).getTime());
        holder.tvPreyName.setText(parent.getContext().getString(R.string.prey_name_, list.get(position).getImage().getPreyName()));
        holder.tvPreyLevel.setText(parent.getContext().getString(R.string.prey_level_, list.get(position).getImage().getPreyLevel()));

        switch (list.get(position).getStatus()) {
            case Report.STATUS_NEW:
                holder.imgTag.setVisibility(View.GONE);
                holder.layoutItem.setBackgroundResource(R.color.colorWhite);
                break;
            case Report.STATUS_EXIST:
                holder.imgTag.setImageResource(R.mipmap.repeat);
                holder.imgTag.setVisibility(View.VISIBLE);
                holder.layoutItem.setBackgroundResource(R.color.colorBlueAlpha);
                break;
            case Report.STATUS_REPET:
                holder.imgTag.setImageResource(R.mipmap.ban);
                holder.imgTag.setVisibility(View.VISIBLE);
                holder.layoutItem.setBackgroundResource(R.color.colorRedAlpha);
                break;
        }

        return convertView;
    }

    static class ViewHolder {
        @BindView(R.id.imgCover)
        ImageView imgCover;
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
        @BindView(R.id.layoutItem)
        LinearLayout layoutItem;
        @BindView(R.id.imgTag)
        ImageView imgTag;

        ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }
}
