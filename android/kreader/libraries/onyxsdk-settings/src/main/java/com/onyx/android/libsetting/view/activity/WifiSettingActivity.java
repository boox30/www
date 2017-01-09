package com.onyx.android.libsetting.view.activity;

import android.databinding.DataBindingUtil;
import android.net.NetworkInfo;
import android.net.wifi.WifiConfiguration;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import com.onyx.android.libsetting.R;
import com.onyx.android.libsetting.data.wifi.AccessPoint;
import com.onyx.android.libsetting.databinding.ActivityWifiSettingBinding;
import com.onyx.android.libsetting.databinding.WifiInfoItemBinding;
import com.onyx.android.libsetting.manager.WifiAdmin;
import com.onyx.android.libsetting.util.CommonUtil;
import com.onyx.android.libsetting.util.SettingRecyclerViewUtil;
import com.onyx.android.libsetting.util.WifiUtil;
import com.onyx.android.libsetting.view.BindingViewHolder;
import com.onyx.android.libsetting.view.PageRecyclerViewItemClickListener;
import com.onyx.android.libsetting.view.SettingPageAdapter;
import com.onyx.android.libsetting.view.dialog.WifiConnectedDialog;
import com.onyx.android.libsetting.view.dialog.WifiLoginDialog;
import com.onyx.android.libsetting.view.dialog.WifiSavedDialog;
import com.onyx.android.sdk.ui.activity.OnyxAppCompatActivity;
import com.onyx.android.sdk.ui.view.DisableScrollLinearManager;
import com.onyx.android.sdk.ui.view.OnyxPageDividerItemDecoration;
import com.onyx.android.sdk.ui.view.PageRecyclerView;

import java.net.SocketException;
import java.util.List;

import static android.net.wifi.WifiInfo.LINK_SPEED_UNITS;
import static com.onyx.android.libsetting.util.Constant.ARGS_BAND;
import static com.onyx.android.libsetting.util.Constant.ARGS_IP_ADDRESS;
import static com.onyx.android.libsetting.util.Constant.ARGS_LINK_SPEED;
import static com.onyx.android.libsetting.util.Constant.ARGS_SECURITY_MODE;
import static com.onyx.android.libsetting.util.Constant.ARGS_SIGNAL_LEVEL;
import static com.onyx.android.libsetting.util.Constant.ARGS_SSID;

public class WifiSettingActivity extends OnyxAppCompatActivity {
    static final String TAG = WifiSettingActivity.class.getSimpleName();
    ActivityWifiSettingBinding binding;
    WifiAdmin wifiAdmin;
    OnyxPageDividerItemDecoration itemDecoration;
    SettingPageAdapter<WifiResultItemViewHolder,AccessPoint> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView();
        wifiAdmin = new WifiAdmin(this, new WifiAdmin.Callback() {
            @Override
            public void onWifiStateChange(boolean isWifiEnable) {
                Log.e(TAG, "onWifiStateChange: " );
                updateUI(isWifiEnable);
            }

            @Override
            public void onScanResultReady(List<AccessPoint> scanResult) {
                Log.e(TAG, "onScanResultReady: " );
                adapter.setDataList(scanResult);
                updateSummary(true);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onSupplicantStateChanged(NetworkInfo.DetailedState state) {
                Log.e(TAG, "onSupplicantStateChanged: " +state);
                updateAccessPointDetailedState(state);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onNetworkConnectionChange(NetworkInfo.DetailedState state) {
                Log.e(TAG, "onNetworkConnectionChange: "+state );
                updateAccessPointDetailedState(state);
                adapter.notifyDataSetChanged();
            }
        });
    }

    private void updateAccessPointDetailedState(NetworkInfo.DetailedState state) {
        for (AccessPoint accessPoint : adapter.getDataList()) {
            WifiConfiguration config = accessPoint.getWifiConfiguration();
            if (config == null) {
                continue;
            }
            if (accessPoint.getWifiConfiguration().networkId ==
                    wifiAdmin.getCurrentConnectionInfo().getNetworkId()) {
                accessPoint.setDetailedState(state);
                if (state == NetworkInfo.DetailedState.CONNECTED) {
                    accessPoint.setSecurityString(getString(R.string.wifi_connected));
                    accessPoint.updateWifiInfo();
                    adapter.getDataList().remove(accessPoint);
                    adapter.getDataList().add(0, accessPoint);
                }
                break;
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        wifiAdmin.registerReceiver();
    }

    @Override
    protected void onPause() {
        super.onPause();
        wifiAdmin.unregisterReceiver();
    }

    private void updateUI(boolean isWifiEnable) {
        binding.wifSwitch.setChecked(isWifiEnable);
        binding.wifiScanResultRecyclerView.setVisibility(isWifiEnable ? View.VISIBLE : View.GONE);
        if (!isWifiEnable) {
            adapter.getDataList().clear();
            adapter.notifyDataSetChanged();
        }
        updateSummary(isWifiEnable);
        binding.rescanBtn.setVisibility(isWifiEnable ? View.VISIBLE : View.GONE);
    }

    private void updateSummary(boolean isWifiEnable) {
        binding.textViewWifiSummary.setText(isWifiEnable ?
                getString(R.string.current_available_wifi_count, adapter.getDataCount())
                : getString(R.string.open_wifi));
    }

    private void initView() {
        binding = DataBindingUtil.setContentView(this, R.layout.activity_wifi_setting);
        initSupportActionBarWithCustomBackFunction();
        binding.wifiToggleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                wifiAdmin.toggleWifi();
            }
        });
        binding.rescanBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                wifiAdmin.triggerWifiScan();
            }
        });
        initRecyclerView();
    }

    private void initRecyclerView() {
        PageRecyclerView resultRecyclerView = binding.wifiScanResultRecyclerView;
        resultRecyclerView.setHasFixedSize(true);
        resultRecyclerView.setLayoutManager(new DisableScrollLinearManager(this));
        buildAdapter();
        itemDecoration = new OnyxPageDividerItemDecoration(this, OnyxPageDividerItemDecoration.VERTICAL);
        resultRecyclerView.addItemDecoration(itemDecoration);
        itemDecoration.setActualChildCount(adapter.getRowCount());
        resultRecyclerView.setItemDecorationHeight(itemDecoration.getDivider().getIntrinsicHeight());
        resultRecyclerView.setAdapter(adapter);
    }

    private void buildAdapter() {
        adapter = new SettingPageAdapter<WifiResultItemViewHolder, AccessPoint>() {
            @Override
            public int getRowCount() {
                return WifiSettingActivity.this.getResources().getInteger(R.integer.wifi_per_page_item_count);
            }

            @Override
            public int getColumnCount() {
                return 1;
            }

            @Override
            public WifiResultItemViewHolder onPageCreateViewHolder(ViewGroup parent, int viewType) {
                return new WifiResultItemViewHolder(WifiInfoItemBinding.inflate(getLayoutInflater(), parent, false));
            }

            @Override
            public void onPageBindViewHolder(WifiResultItemViewHolder holder, int position) {
                super.onPageBindViewHolder(holder, position);
                SettingRecyclerViewUtil.updateItemDecoration(pageRecyclerView,this,itemDecoration);
                holder.bindTo(getDataList().get(position));
            }
        };

        adapter.setItemClickListener(new PageRecyclerViewItemClickListener<AccessPoint>() {
            @Override
            public void itemClick(AccessPoint accessPoint) {
                if (accessPoint.isConnected()) {
                    showConnectDialog(accessPoint);
                } else if (accessPoint.getWifiConfiguration() == null) {
                    showLoginDialog(accessPoint);
                } else {
                    showSaveDialog(accessPoint);
                }
            }
        });
    }

    private void showConnectDialog(final AccessPoint accessPoint) {
        WifiConnectedDialog wifiConnectedDialog = new WifiConnectedDialog();
        Bundle args = new Bundle();
        args.putString(ARGS_SSID, accessPoint.getScanResult().SSID);
        args.putString(ARGS_LINK_SPEED, accessPoint.getWifiInfo().getLinkSpeed() + LINK_SPEED_UNITS);
        try {
            args.putString(ARGS_IP_ADDRESS, wifiAdmin.getLocalIPAddress());
        } catch (SocketException e) {
            e.printStackTrace();
        }
        args.putString(ARGS_SECURITY_MODE, accessPoint.getSecurityMode());
        args.putString(ARGS_SIGNAL_LEVEL,
                wifiAdmin.getSignalString(accessPoint.getSignalLevel()));
        if (CommonUtil.apiLevelCheck(Build.VERSION_CODES.LOLLIPOP)) {
            args.putString(ARGS_BAND, WifiUtil.getBandString(WifiSettingActivity.this,
                    accessPoint.getWifiInfo().getFrequency()));
        }
        wifiConnectedDialog.setArguments(args);
        wifiConnectedDialog.setCallback(new WifiConnectedDialog.Callback() {
            @Override
            public void onForgetAccessPoint() {
                wifiAdmin.forget(accessPoint);
                wifiAdmin.triggerWifiScan();
            }
        });
        wifiConnectedDialog.show(getFragmentManager());
    }

    private void showLoginDialog(final AccessPoint accessPoint) {
        WifiLoginDialog wifiLoginDialog = new WifiLoginDialog();
        Bundle args = new Bundle();
        args.putString(ARGS_SECURITY_MODE, accessPoint.getSecurityMode());
        args.putString(ARGS_SIGNAL_LEVEL, wifiAdmin.getSignalString(accessPoint.getSignalLevel()));
        args.putString(ARGS_SSID, accessPoint.getScanResult().SSID);
        wifiLoginDialog.setArguments(args);
        wifiLoginDialog.setCallback(new WifiLoginDialog.Callback() {
            @Override
            public void onConnectToAccessPoint(String password) {
                accessPoint.setPassword(password);
                wifiAdmin.connectWifi(accessPoint);
            }
        });
        wifiLoginDialog.show(getFragmentManager());
    }

    private void showSaveDialog(final AccessPoint accessPoint) {
        WifiSavedDialog wifiSavedDialog = new WifiSavedDialog();
        Bundle args = new Bundle();
        args.putString(ARGS_SSID, accessPoint.getScanResult().SSID);
        args.putString(ARGS_SIGNAL_LEVEL, wifiAdmin.getSignalString(accessPoint.getSignalLevel()));
        args.putString(ARGS_SECURITY_MODE, accessPoint.getSecurityMode());
        wifiSavedDialog.setArguments(args);
        wifiSavedDialog.setCallback(new WifiSavedDialog.Callback() {
            @Override
            public void onForgetAccessPoint() {
                wifiAdmin.forget(accessPoint);
                wifiAdmin.triggerWifiScan();
            }

            @Override
            public void onConnectToAccessPoint() {
                wifiAdmin.connectWifi(accessPoint);
            }
        });
        wifiSavedDialog.show(getFragmentManager());
    }

    private class WifiResultItemViewHolder extends BindingViewHolder<WifiInfoItemBinding, AccessPoint> {
        WifiResultItemViewHolder(WifiInfoItemBinding binding) {
            super(binding);
        }

        public void bindTo(AccessPoint accessPoint) {
            mBinding.setAccessPoint(accessPoint);
            mBinding.executePendingBindings();
        }
    }
}
