package com.tmdroid.securemessenger;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.p2p.WifiP2pManager;

public class Connection extends BroadcastReceiver{

	@Override
	public void onReceive(Context context, Intent intent) {
		
		String action = intent.getAction();
	    if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
	        int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
	        if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
	            // Wifi P2P is enabled
	        } else {
	            // Wi-Fi P2P is not enabled
	        }
	    }
		
	}

}
