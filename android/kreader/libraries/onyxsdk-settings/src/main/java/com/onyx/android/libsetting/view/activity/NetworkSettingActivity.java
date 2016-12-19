package com.onyx.android.libsetting.view.activity;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;

import com.onyx.android.libsetting.R;
import com.onyx.android.libsetting.SettingConfig;
import com.onyx.android.libsetting.data.wifi.AccessPoint;
import com.onyx.android.libsetting.databinding.ActivityNetworkSettingBinding;
import com.onyx.android.libsetting.manager.BluetoothAdmin;
import com.onyx.android.libsetting.manager.WifiAdmin;
import com.onyx.android.libsetting.util.DeviceFeatureUtil;
import com.onyx.android.libsetting.view.OnyxCustomSwitchPreference;
import com.onyx.android.sdk.ui.activity.OnyxAppCompatActivity;
import com.onyx.android.sdk.utils.ActivityUtil;

import java.util.List;

public class NetworkSettingActivity extends OnyxAppCompatActivity {
    ActivityNetworkSettingBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView();
    }

    private void initView() {
        binding = DataBindingUtil.setContentView(this, R.layout.activity_network_setting);
        initSupportActionBarWithCustomBackFunction();
        getSupportFragmentManager().beginTransaction().replace(R.id.network_setting,
                new NetworkSettingPreferenceFragment()).commit();
    }

    public static class NetworkSettingPreferenceFragment extends PreferenceFragmentCompat {
        OnyxCustomSwitchPreference wifiSwitchPreference;
        OnyxCustomSwitchPreference bluetoothSwitchPreference;
        Preference vpnPreference;
        WifiAdmin wifiAdmin;
        BluetoothAdmin bluetoothAdmin;
        SettingConfig config;

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            addPreferencesFromResource(R.xml.network_setting);
            config = SettingConfig.sharedInstance(getContext());
            initView();
            initAdmin();
            hidePreferenceByDeviceFeature();
        }

        private void initAdmin() {
            wifiAdmin = new WifiAdmin(getContext(), new WifiAdmin.Callback() {
                @Override
                public void onWifiStateChange(boolean isWifiEnable) {
                    wifiSwitchPreference.setSwitchChecked(isWifiEnable);
                }

                @Override
                public void onScanResultReady(List<AccessPoint> scanResult) {

                }

                @Override
                public void onSupplicantStateChanged(NetworkInfo.DetailedState state) {

                }

                @Override
                public void onNetworkConnectionChange(NetworkInfo.DetailedState state) {

                }
            });
            bluetoothAdmin = new BluetoothAdmin(getContext(), new BluetoothAdmin.Callback() {
                @Override
                public void onBluetoothStateChange(boolean isBluetoothEnable) {
                    bluetoothSwitchPreference.setSwitchChecked(isBluetoothEnable);
                }
            });
        }

        private void initView() {
            wifiSwitchPreference = (OnyxCustomSwitchPreference) findPreference(getString(R.string.wifi_setting_key));
            bluetoothSwitchPreference = (OnyxCustomSwitchPreference) findPreference(getString(R.string.bluetooth_setting_key));
            vpnPreference = findPreference(getString(R.string.vpn_setting_key));
            vpnPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    ActivityUtil.startActivitySafely(getContext(), config.getVPNSettingIntent());
                    return true;
                }
            });
            wifiSwitchPreference.setCallback(new OnyxCustomSwitchPreference.Callback() {
                @Override
                public void onSwitchClicked() {
                    wifiAdmin.toggleWifi();
                }

                @Override
                public void onSwitchReady() {
                    wifiSwitchPreference.setSwitchChecked(wifiAdmin.isWifiEnabled());
                }
            });

            wifiSwitchPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    ActivityUtil.startActivitySafely(getContext(), new Intent(getContext(), WifiSettingActivity.class));
                    return true;
                }
            });
            bluetoothSwitchPreference.setCallback(new OnyxCustomSwitchPreference.Callback() {
                @Override
                public void onSwitchClicked() {
                    bluetoothAdmin.toggleBluetoothEnabled();
                }

                @Override
                public void onSwitchReady() {
                    bluetoothSwitchPreference.setSwitchChecked(bluetoothAdmin.isEnabled());
                }
            });
            bluetoothSwitchPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    ActivityUtil.startActivitySafely(getContext(), config.getBluetoothSettingIntent());
                    return true;
                }
            });
        }

        private void hidePreferenceByDeviceFeature() {
            if (!DeviceFeatureUtil.hasWifi(getContext())) {
                getPreferenceScreen().removePreference(wifiSwitchPreference);
                getPreferenceScreen().removePreference(vpnPreference);
            }
            if (!DeviceFeatureUtil.hasBluetooth(getContext())) {
                getPreferenceScreen().removePreference(bluetoothSwitchPreference);
            }
        }

        @Override
        public void onResume() {
            super.onResume();
            wifiAdmin.registerReceiver();
            bluetoothAdmin.registerReceiver();
        }

        @Override
        public void onPause() {
            super.onPause();
            wifiAdmin.unregisterReceiver();
            bluetoothAdmin.unregisterReceiver();
        }
    }
}
