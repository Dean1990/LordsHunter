package com.deanlib.lordshunter.ui.adapter;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import com.deanlib.lordshunter.R;
import com.deanlib.lordshunter.data.entity.OCR;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * OCR数据管理
 *
 * @author dean
 * @time 2018/12/25 3:37 PM
 */
public class OCRListAdapter extends BaseAdapter {

    List<OCR> list;

    public OCRListAdapter(List<OCR> list) {
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
            convertView = View.inflate(parent.getContext(), R.layout.layout_ocr_item, null);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        }else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.tvName.setText(list.get(position).getName());
        holder.cbExist.setChecked(list.get(position).isExist());

        return convertView;
    }

    static
    class ViewHolder {
        @BindView(R.id.tvName)
        TextView tvName;
        @BindView(R.id.cbExist)
        CheckBox cbExist;

        ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }
}
