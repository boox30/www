package com.onyx.android.libsetting.view.fragment.edu;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.databinding.DataBindingUtil;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.onyx.android.libsetting.R;
import com.onyx.android.libsetting.data.PowerSettingTimeoutCategory;
import com.onyx.android.libsetting.data.TimeoutItem;
import com.onyx.android.libsetting.databinding.FragmentPowerManagerBinding;
import com.onyx.android.libsetting.databinding.TimeoutItemBinding;
import com.onyx.android.libsetting.util.BatteryUtil;
import com.onyx.android.libsetting.util.PowerUtil;
import com.onyx.android.libsetting.view.BindingViewHolder;
import com.onyx.android.libsetting.view.PageRecyclerViewItemClickListener;
import com.onyx.android.libsetting.view.SettingPageAdapter;
import com.onyx.android.sdk.ui.view.DisableScrollGridManager;
import com.onyx.android.sdk.utils.DeviceUtils;

import java.util.ArrayList;
import java.util.List;

public class PowerManagerFragment extends Fragment {
    private FragmentPowerManagerBinding binding;
    private SettingPageAdapter<TimeoutItemViewHolder, TimeoutItem> autoSleepAdapter, autoPowerOffAdapter;
    private BroadcastReceiver powerReceiver;
    private IntentFilter powerFilter;
    private int status, level;

    @Override
    public void onResume() {
        super.onResume();
        buildReceiverAndFilter();
        getActivity().registerReceiver(powerReceiver, powerFilter);
    }

    private void buildReceiverAndFilter() {
        if (powerFilter == null || powerReceiver == null) {
            powerReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    if (!intent.getAction().equalsIgnoreCase(Intent.ACTION_TIME_TICK)) {
                        status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, BatteryManager.BATTERY_STATUS_NOT_CHARGING);
                        level = DeviceUtils.getBatteryPercentLevel(getContext());
                    }
                    updateBatteryStatus();
                }
            };
            powerFilter = new IntentFilter();
            powerFilter.addAction(Intent.ACTION_TIME_TICK);
            powerFilter.addAction(Intent.ACTION_POWER_CONNECTED);
            powerFilter.addAction(Intent.ACTION_POWER_DISCONNECTED);
            powerFilter.addAction(Intent.ACTION_BATTERY_CHANGED);
        }
    }


    @Override
    public void onPause() {
        super.onPause();
        if (powerReceiver != null) {
            getActivity().unregisterReceiver(powerReceiver);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(
                inflater, R.layout.fragment_power_manager, container, false);
        initView();
        return binding.getRoot();
    }

    private void initView() {
        binding.autoSleepRecyclerView.setHasFixedSize(true);
        binding.autoPowerOffRecyclerView.setHasFixedSize(true);
        binding.autoSleepRecyclerView.setLayoutManager(new DisableScrollGridManager(getContext()));
        binding.autoPowerOffRecyclerView.setLayoutManager(new DisableScrollGridManager(getContext()));
        buildAdapter(PowerSettingTimeoutCategory.SCREEN_TIMEOUT);
        buildAdapter(PowerSettingTimeoutCategory.POWER_OFF_TIMEOUT);
        buildDataList(autoSleepAdapter, PowerSettingTimeoutCategory.SCREEN_TIMEOUT);
        buildDataList(autoPowerOffAdapter, PowerSettingTimeoutCategory.POWER_OFF_TIMEOUT);
        binding.autoSleepRecyclerView.setAdapter(autoSleepAdapter);
        binding.autoPowerOffRecyclerView.setAdapter(autoPowerOffAdapter);
    }

    private void updateBatteryStatus(){
        binding.batteryLevel.setText(BatteryUtil.getVisualBatteryLevel(getActivity(), level));
        binding.batteryStatus.setText(BatteryUtil.getBatteryStatusByStatusCode(getActivity(),status));
        binding.devicePowerOnTime.setText(BatteryUtil.getVisualDevicePowerOnTime(getActivity()));
        binding.batteryTotalTime.setText(BatteryUtil.getVisualBatteryTotalTime(getActivity()));
        binding.batteryRemainTime.setText(BatteryUtil.getVisualBatteryRemainTime(getActivity()));
    }

    private void buildDataList(SettingPageAdapter<TimeoutItemViewHolder, TimeoutItem> adapter, @PowerSettingTimeoutCategory.PowerSettingTimeoutCategoryDef int category) {
        List<TimeoutItem> dataList = new ArrayList<>();
        int timeoutOptionSize = PowerUtil.getTimeoutEntries(getContext(),
                category).length;
        CharSequence[] timeoutEntries = PowerUtil.getTimeoutEntries(getContext(),
                category);
        CharSequence[] timeoutEntryValues = PowerUtil.getTimeoutEntryValues(getContext(),
                category);
        int currentValue = PowerUtil.getCurrentTimeoutIntValue(getContext(), category);
        for (int i = 0; i < timeoutOptionSize; i++) {
            TimeoutItem item = new TimeoutItem();
            item.setTimeoutEntry(timeoutEntries[i].toString());
            item.setTimeoutEntryValue(Integer.parseInt(timeoutEntryValues[i].toString()));
            if (item.getTimeoutEntryValue() == currentValue) {
                item.setChecked(true);
            }
            dataList.add(item);
        }
        adapter.setDataList(dataList);
    }

    private void buildAdapter(@PowerSettingTimeoutCategory.PowerSettingTimeoutCategoryDef final int category) {
        final SettingPageAdapter<TimeoutItemViewHolder, TimeoutItem> adapter = new SettingPageAdapter<TimeoutItemViewHolder, TimeoutItem>() {
            @Override
            public int getRowCount() {
                return 2;
            }

            @Override
            public int getColumnCount() {
                return 3;
            }

            @Override
            public TimeoutItemViewHolder onPageCreateViewHolder(ViewGroup parent, int viewType) {
                return new TimeoutItemViewHolder(TimeoutItemBinding.inflate(getActivity().getLayoutInflater(), parent, false));
            }

            @Override
            public void onPageBindViewHolder(TimeoutItemViewHolder holder, int position) {
                super.onPageBindViewHolder(holder, position);
                holder.bindTo(getDataList().get(position));
            }
        };

        adapter.setItemClickListener(new PageRecyclerViewItemClickListener<TimeoutItem>() {
            @Override
            public void itemClick(TimeoutItem timeoutItem) {
                timeoutItem.setChecked(true);
                for (TimeoutItem item : adapter.getDataList()) {
                    if (item.getTimeoutEntryValue() != timeoutItem.getTimeoutEntryValue()) {
                        item.setChecked(false);
                    }
                }
                PowerUtil.setCurrentTimeoutValue(getContext(), category, timeoutItem.getTimeoutEntryValue());
                switch (category) {
                    case PowerSettingTimeoutCategory.SCREEN_TIMEOUT:
                        binding.autoSleepRecyclerView.notifyDataSetChanged();
                        break;
                    case PowerSettingTimeoutCategory.POWER_OFF_TIMEOUT:
                        binding.autoPowerOffRecyclerView.notifyDataSetChanged();
                        break;
                    default:
                        break;
                }
            }
        });
        switch (category) {
            case PowerSettingTimeoutCategory.SCREEN_TIMEOUT:
                autoSleepAdapter = adapter;
                break;
            case PowerSettingTimeoutCategory.POWER_OFF_TIMEOUT:
                autoPowerOffAdapter = adapter;
                break;
            default:
                break;
        }
    }

    private class TimeoutItemViewHolder extends BindingViewHolder<TimeoutItemBinding, TimeoutItem> {
        TimeoutItemViewHolder(TimeoutItemBinding binding) {
            super(binding);
        }

        public void bindTo(TimeoutItem timeoutItem) {
            mBinding.setTimeoutItem(timeoutItem);
            mBinding.executePendingBindings();
        }
    }

}