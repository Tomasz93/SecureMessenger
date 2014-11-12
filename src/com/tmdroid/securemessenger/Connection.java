package com.tmdroid.securemessenger;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.net.wifi.p2p.WifiP2pManager.PeerListListener;
import android.net.wifi.p2p.WifiP2pManager.ConnectionInfoListener;
import android.util.Log;
import android.widget.Toast;

public class Connection extends BroadcastReceiver{
	
	private WifiP2pManager mManager;
	private ConnectionInfoListener mConnectionListener;
	private Channel mChannel;
	private PeerListListener mPeerListListener;
	
	public Connection (WifiP2pManager manager, Channel channel, PeerListListener peerListListener, ConnectionInfoListener connectionListener){
		super();
		this.mManager = manager;
		this.mChannel = channel;
		this.mPeerListListener = peerListListener;
		this.mConnectionListener = connectionListener;
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		
		String action = intent.getAction();
	    if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
	        int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
	        if (state != WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
	        	Toast.makeText(context, "Wi-Fi Direct is disabled", Toast.LENGTH_LONG).show();
	        }
	    }else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)){
	    	if(mManager != null){
	    		mManager.requestPeers(mChannel, mPeerListListener);
	    	}
	    	Log.d("WiFi","Peers has changed !");
	    } else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {
            if (mManager == null) {
                return;
            }
            NetworkInfo networkInfo = (NetworkInfo) intent.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);
            if (networkInfo.isConnected()) {
                mManager.requestConnectionInfo(mChannel, mConnectionListener);
                
        	}
        }
		
	}
	
}
