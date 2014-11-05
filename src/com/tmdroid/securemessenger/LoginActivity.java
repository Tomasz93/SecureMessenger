package com.tmdroid.securemessenger;

import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteOrder;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

public class LoginActivity extends Activity {

	private TextView mIpAddressField;
	private EditText mFriendIpAddressField;
	private BroadcastReceiver mWifiReceiver;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        
        mFriendIpAddressField = (EditText) this.findViewById(R.id.friendIpAddressField);
        if(mFriendIpAddressField.requestFocus()) {
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        }
        mFriendIpAddressField.setImeActionLabel("Search Model", EditorInfo.IME_ACTION_GO);
        mFriendIpAddressField.setOnEditorActionListener(new TextView.OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
				if(TextUtils.isEmpty(textView.getText().toString())) {
					//odpalamy DH
				}
				return true;
			}
		});
     
        setWifiIpAddress(this);
        
        mWifiReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
	        	setWifiIpAddress(context);
            }
        };
        IntentFilter filter = new IntentFilter();
        filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        super.registerReceiver(mWifiReceiver, filter);
       
    }

    @Override
	public void onResume() {
        super.onResume();
        IntentFilter filter = new IntentFilter();
        filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        LocalBroadcastManager.getInstance(this).registerReceiver(mWifiReceiver, filter);
    }
    
    @Override
	public void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mWifiReceiver);
    }
    
    private void setWifiIpAddress(Context context) {
        WifiManager wifiManager = (WifiManager) context.getSystemService(WIFI_SERVICE);
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
            ipAddressString = context.getString(R.string.ip_address_error);
        }
        
        mIpAddressField = (TextView) this.findViewById(R.id.ipAddressField);
        mIpAddressField.setText(ipAddressString);
    }
}
