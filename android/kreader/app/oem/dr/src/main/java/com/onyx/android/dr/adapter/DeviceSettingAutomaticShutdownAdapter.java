package com.onyx.android.dr.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.onyx.android.dr.R;
import com.onyx.android.dr.common.Constants;
import com.onyx.android.dr.util.AppConfig;
import com.onyx.android.dr.view.PageRecyclerView;
import com.onyx.android.sdk.utils.StringUtils;

/**
 * Created by yangzhongping on 17-5-5.
 */
public class DeviceSettingAutomaticShutdownAdapter extends PageRecyclerView.PageAdapter {
    private static final String TAG = DeviceSettingAutomaticShutdownAdapter.class.getSimpleName();
    private int row = AppConfig.DeviceSettingInfo.DEVICE_SETTING_PAGE_VIEW_ROW;
    private int column = AppConfig.DeviceSettingInfo.DEVICE_SETTING_PAGE_VIEW_COLUMN;
    private String[] times;
    private String[] values;
    private String[] explains;
    private String currentTimeValue;
    private int selectItem = 0;

    public DeviceSettingAutomaticShutdownAdapter(Context context) {
        row = AppConfig.sharedInstance(context).getDeviceSettingPageViewRow();
        column = AppConfig.sharedInstance(context).getDeviceSettingPageViewColumn();
    }

    public String getCurrentTimeValue() {
        return currentTimeValue;
    }

    public void setAutomaticShutdownTimes(final String[] times, final String[] values, final String[] explains, final String currentTimeValue) {
        this.times = times;
        this.values = values;
        this.explains = explains;
        this.currentTimeValue = currentTimeValue;
    }

    @Override
    public int getRowCount() {
        if (times == null) {
            return row;
        } else {
            return times.length;
        }
    }

    @Override
    public int getColumnCount() {
        return column;
    }

    @Override
    public int getDataCount() {
        return times == null ? Constants.VALUE_ZERO : times.length;
    }

    @Override
    public RecyclerView.ViewHolder onPageCreateViewHolder(ViewGroup parent, int viewType) {
        return new Holder(LayoutInflater.from(parent.getContext()).inflate(R.layout.automatic_shutdown_view_item, parent, false));
    }

    @Override
    public void onPageBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        String title = times[position];
        String explain = explains[position];
        Holder view = (Holder) holder;
        view.tvTime.setText(title);
        if(StringUtils.isNullOrEmpty(explain)){
            view.tvExplain.setVisibility(View.GONE);
        }else {
            view.tvExplain.setVisibility(View.VISIBLE);
            view.tvExplain.setText(explain);
        }
        if (currentTimeValue.equals(values[position])) {
            view.ivCheck.setVisibility(View.VISIBLE);
        } else {
            view.ivCheck.setVisibility(View.GONE);
        }
        view.itemView.setTag(position);
    }

    @Override
    public void setOnItemClick(OnRecyclerViewItemClickListener onRecyclerViewItemClickListener) {
        super.setOnItemClick(onRecyclerViewItemClickListener);
    }

    @Override
    public void onClick(View v) {
        Object object = v.getTag();
        if (object == null) {
            return;
        }
        selectItem = (Integer) object;
        currentTimeValue = values[selectItem];
        if (onRecyclerViewItemClickListener != null) {
            onRecyclerViewItemClickListener.onItemClick(v, selectItem);
        }
        notifyDataSetChanged();
    }

    private class Holder extends RecyclerView.ViewHolder {
        TextView tvTime;
        TextView tvExplain;
        ImageView ivCheck;

        public Holder(View view) {
            super(view);
            tvTime = (TextView) view.findViewById(R.id.automatic_shutdown_time);
            tvExplain  = (TextView) view.findViewById(R.id.automatic_shutdown_explain);
            ivCheck = (ImageView) view.findViewById(R.id.automatic_shutdown_check);
            view.setOnClickListener(DeviceSettingAutomaticShutdownAdapter.this);
        }
    }
}