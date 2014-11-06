package com.tmdroid.securemessenger;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.ActionListener;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.net.wifi.p2p.WifiP2pManager.PeerListListener;
import android.net.wifi.p2p.WifiP2pManager.ConnectionInfoListener;
import android.nfc.Tag;
import android.util.Log;
import android.widget.Toast;

public class Connection extends BroadcastReceiver{
	
	private WifiP2pManager mManager;
	private ConnectionInfoListener connectionListener;
	private Channel mChannel;
	private BackupActivity mActivity;
	private List<WifiP2pDevice> peers = new ArrayList();
	public Connection (WifiP2pManager manager, Channel channel,BackupActivity activity){
		super();
		this.mManager = manager;
		this.mChannel = channel;
		this.mActivity = activity;
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		
		String action = intent.getAction();
	    if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
	        int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
	        if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
	        	Toast.makeText(context, "Wi-Fi Direct is enabled", Toast.LENGTH_LONG).show();
	        } else {
	        	Toast.makeText(context, "Wi-Fi Direct is not enabled", Toast.LENGTH_LONG).show();
	        }
	    }else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)){
	    	if(mManager != null){
	    		mManager.requestPeers(mChannel, peerListListener);
	    	}
	    	Log.d("WiFi","Peers has changed !");
	    } else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {
            if (mManager == null) {
                return;
            }
            NetworkInfo networkInfo = (NetworkInfo) intent.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);
            if (networkInfo.isConnected()) {
                // We are connected with the other device, request connection
                // info to find group owner IP

                mManager.requestConnectionInfo(mChannel, connectionListener);
            	}
            }
		
	}
	private PeerListListener peerListListener = new PeerListListener() {
        @Override
        public void onPeersAvailable(WifiP2pDeviceList peerList) {

            peers.clear();
            peers.addAll(peerList.getDeviceList());

            //((WiFiPeerListAdapter) getListAdapter()).notifyDataSetChanged();
            if (peers.size() == 0) {
                Log.d("WiFi", "No devices found");
                return;
            }
        }
    };
    public void connect(){
    	WifiP2pDevice device = peers.get(0);
    	
    	WifiP2pConfig config = new WifiP2pConfig();
    	config.deviceAddress = device.deviceAddress;
    	config.wps.setup = WpsInfo.PBC;
    	
    	mManager.connect(mChannel, config, new ActionListener(){
    		public void onSuccess(){}
    		public void onFailure(int reason){
    			Toast.makeText(mActivity, "Connect failed. Retry.",Toast.LENGTH_SHORT).show();   
    			}
    	});
    }
    
    public void onConnectionInfoAvailable(final WifiP2pInfo info) {

    	//powinno byc InetAddress ...
        String groupOwnerAddress = info.groupOwnerAddress.getHostAddress();

        // After the group negotiation, we can determine the group owner.
        if (info.groupFormed && info.isGroupOwner) {
            // Do whatever tasks are specific to the group owner.
            // One common case is creating a server thread and accepting
            // incoming connections.
        } else if (info.groupFormed) {
            // The other device acts as the client. In this case,
            // you'll want to create a client thread that connects to the group
            // owner.
        }
    }
	
}
