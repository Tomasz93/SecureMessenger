package com.tmdroid.securemessenger;

import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteOrder;
import java.util.ArrayList;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.ActionListener;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.net.wifi.p2p.WifiP2pManager.ConnectionInfoListener;
import android.net.wifi.p2p.WifiP2pManager.PeerListListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

public class PeersActivity extends Activity {
	
	private ConnectingTask mConnectingTask = null;
	private View mPeersProgress;
	private View mPeersList;
	private ListView mPeersListView;
	private Button mPeersRefreshButton;
	private Button mPeersExitButton;
	
	private Activity mActivity;
	
	private BroadcastReceiver mConnection;
	private WifiP2pManager mManager;
	private Channel mChannel;
	private IntentFilter mIntentFilter;
	private PeerListListener mPeerListListener;
	private WifiAdapter mWifiAdapter;
	private ConnectionInfoListener mConnectionListener;
	private ArrayList<WifiDevice> mPeers = new ArrayList<WifiDevice>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_peers);
		
		mActivity = this;
		
		mPeersProgress = this.findViewById(R.id.peersLoadLayout);
		mPeersList = this.findViewById(R.id.peersListLayout);
		mPeersListView = (ListView) this.findViewById(R.id.peersListView);
		mPeersRefreshButton = (Button) this.findViewById(R.id.peersRefreshButton);
		mPeersExitButton = (Button) this.findViewById(R.id.peersExitButton);
		
		mConnectingTask = new ConnectingTask();
		mConnectingTask.execute();
		
		mPeersRefreshButton.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View arg0) {
				mManager.discoverPeers(mChannel, new WifiP2pManager.ActionListener() {
			        @Override
			        public void onSuccess() {
			        }

			        @Override
			        public void onFailure(int reasonCode) {
			        }
				});
				if(mManager != null){
		    		mManager.requestPeers(mChannel, mPeerListListener);
		    	}
			}
		});
		mPeersExitButton.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View arg0) {
				finish();
			}
		});
		
		mWifiAdapter = new WifiAdapter(mPeers, this);
		mPeersListView.setAdapter(mWifiAdapter);
		mPeerListListener = new PeerListListener() {
	        @Override
	        public void onPeersAvailable(WifiP2pDeviceList peerList) {
	            mPeers.clear();
	            ArrayList<WifiP2pDevice> tmpPeers = new ArrayList<WifiP2pDevice>(); 
	            tmpPeers.addAll(peerList.getDeviceList());

                Log.d("WiFi!!!!!", "Peers: "+tmpPeers.size());
	            
	            for(int i=0; i<tmpPeers.size(); i++){
	            	WifiDevice device = new WifiDevice();
	            	device.device = tmpPeers.get(i);
	            	device.status = 0;
	            	mPeers.add(device);
	            }

	            mWifiAdapter.notifyDataSetChanged();
	            if (mPeers.size() == 0) {
	                Log.d("WiFi", "No devices found");
	                return;
	            }
	        }
	    };
	    
	    mConnectionListener = new ConnectionInfoListener(){
			@Override
			public void onConnectionInfoAvailable(WifiP2pInfo info) {
				String host = info.groupOwnerAddress.getHostAddress().toString();
				//mActivity.getActionBar().setTitle(info.groupOwnerAddress.getHostAddress().toString());
				
				if(info.groupFormed){
					Intent notificationIntent = new Intent(getApplicationContext(), ChatActivity.class);
					Bundle extras = new Bundle();
					extras.putBoolean("Server", false);
					extras.putString("OwnerAddress", host);
					notificationIntent.putExtras(extras);
			        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
			        PendingIntent intent = PendingIntent.getActivity(getApplicationContext(), 0, notificationIntent, 0);
		
			        NotificationCompat.Builder builder = new NotificationCompat.Builder(mActivity);
			        builder.setContentTitle("Secure Messenger")
				        .setContentText("Pending chat request from ...")
				        .setSmallIcon(R.drawable.ic_launcher)
			            .setLights(Color.RED, 2000, 99999999)
			            .setContentIntent(intent);
		
					NotificationManager mNotifyMgr = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
					mNotifyMgr.notify(1234, builder.build());
				}
	    	}
	    };
		
		mManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
		mChannel = mManager.initialize(this, getMainLooper(), null);
		mConnection = new Connection(mManager,mChannel, mPeerListListener, mConnectionListener);
		mIntentFilter = new IntentFilter();
		mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
		mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
		mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
		mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
		mManager.discoverPeers(mChannel, new WifiP2pManager.ActionListener() {

	        @Override
	        public void onSuccess() {
	        }

	        @Override
	        public void onFailure(int reasonCode) {
	        }
		});
		
		mPeersListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
				connect(mPeers.get(position));
			}
		});
		

		WifiManager wifiManager = (WifiManager) this.getSystemService(WIFI_SERVICE);
        int ipAddress = wifiManager.getConnectionInfo().getIpAddress();

        if (ByteOrder.nativeOrder().equals(ByteOrder.LITTLE_ENDIAN)) {
            ipAddress = Integer.reverseBytes(ipAddress);
        }

        byte[] ipByteArray = BigInteger.valueOf(ipAddress).toByteArray();

        String ipAddressString;
        try {
            ipAddressString = InetAddress.getByAddress(ipByteArray).getHostAddress();
        } catch (UnknownHostException ex) {
            Log.e("WIFIIP", "Unable to get ip address.");
            ipAddressString = mActivity.getString(R.string.ip_address_error);
        }
		//this.getActionBar().setTitle(ipAddressString);
	}
	
	protected void onResume(){
		super.onResume();
		registerReceiver(mConnection,mIntentFilter);
	}
	
	protected void onPause(){
		super.onPause();
		unregisterReceiver(mConnection);
	}
	
	private void hideProgress(final boolean hide) {
		int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

		mPeersList.setVisibility(View.VISIBLE);
		mPeersList.animate().setDuration(shortAnimTime).alpha(hide ? 1 : 0).setListener(new AnimatorListenerAdapter() {
			@Override
			public void onAnimationEnd(Animator animation) {
				mPeersList.setVisibility(hide ? View.VISIBLE : View.GONE);
			}
		});

		mPeersProgress.setVisibility(View.VISIBLE);
		mPeersProgress.animate().setDuration(shortAnimTime).alpha(hide ? 0 : 1).setListener(new AnimatorListenerAdapter() {
			@Override
			public void onAnimationEnd(Animator animation) {
				mPeersProgress.setVisibility(hide ? View.GONE: View.VISIBLE);
			}
		});
	}
	
	public void connect(final WifiDevice dev){
		WifiP2pDevice device = dev.device;
		WifiP2pConfig config = new WifiP2pConfig();
		config.deviceAddress = device.deviceAddress;
		mManager.connect(mChannel, config, new ActionListener() {
		    @Override
		    public void onSuccess() {
		    	dev.status = 1;
		    	mWifiAdapter.notifyDataSetChanged();
		    	
		    	Intent intent = new Intent(mActivity, ChatActivity.class);
				Bundle extras = new Bundle();
				extras.putBoolean("Server", true);
				extras.putString("OwnerAddress", null);
				intent.putExtras(extras);
		    	mActivity.startActivity(intent);
		        Toast.makeText(mActivity, "Po³¹czono", Toast.LENGTH_SHORT).show();
		    }

		    @Override
		    public void onFailure(int reason) {
		        Toast.makeText(mActivity, "Dupa", Toast.LENGTH_SHORT).show();
		    }
		});
	}
	
	public class ConnectingTask extends AsyncTask<Void, Void, Boolean> {
		@Override
		protected Boolean doInBackground(Void... params) {
			// TODO: tutaj laczenie.

			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				return false;
			}

			return true;
		}
		
		

		@Override
		protected void onPostExecute(final Boolean success) {
			mConnectingTask = null;
			hideProgress(true);
		}

		@Override
		protected void onCancelled() {
			mConnectingTask = null;
			hideProgress(true);
		}
	}
}
