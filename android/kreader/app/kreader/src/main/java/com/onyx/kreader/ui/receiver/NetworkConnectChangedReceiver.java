package com.onyx.kreader.ui.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

import com.onyx.kreader.ui.data.ReaderDataHolder;

/**
 * Created by ming on 2017/2/8.
 */

public class NetworkConnectChangedReceiver extends BroadcastReceiver {

    private static final String TAG = "NetworkConnectChangedRe";
    public interface NetworkChangedListener {
        void onNetworkChanged(boolean connected, int networkType);
    }

    private NetworkChangedListener networkChangedListener;

    public NetworkConnectChangedReceiver(final NetworkChangedListener networkChangedListener) {
        this.networkChangedListener = networkChangedListener;
    }


    @Override
    public void onReceive(Context context, Intent intent) {

        if (ConnectivityManager.CONNECTIVITY_ACTION.equals(intent.getAction())) {
            ConnectivityManager manager = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);

            NetworkInfo activeNetwork = manager.getActiveNetworkInfo();
            if (activeNetwork != null) { // connected to the internet
                if (networkChangedListener != null) {
                    networkChangedListener.onNetworkChanged(activeNetwork.isConnected(), activeNetwork.getType());
                }
            }

        }
    }
}
