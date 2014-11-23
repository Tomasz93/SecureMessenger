package com.tmdroid.securemessenger;

import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteOrder;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;

public class LoginActivity extends Activity {

	private TextView mIpAddressField;
	private Button mInviteButton;
	private EditText mFriendIpAddressField;
	private EditText mFriendPortField;
	private RadioButton mHostCheckBox;
	private BroadcastReceiver mWifiReceiver;
	private Activity mActivity;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        mActivity = this;
        
        mInviteButton = (Button) this.findViewById(R.id.inviteButton);
        mFriendIpAddressField = (EditText) this.findViewById(R.id.friendIpAddressField);
        mFriendPortField = (EditText) this.findViewById(R.id.friendPortField);
        mHostCheckBox = (RadioButton) this.findViewById(R.id.radioHostYes);
        if(mFriendIpAddressField.requestFocus()) {
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        }
        mFriendIpAddressField.setImeActionLabel("Next", EditorInfo.IME_ACTION_GO);
        mFriendPortField.setImeActionLabel("Invite", EditorInfo.IME_ACTION_GO);
        mFriendIpAddressField.setOnEditorActionListener(new TextView.OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
				if(TextUtils.isEmpty(textView.getText().toString())) {
					textView.setError("Put your friend IP address here");
				}else{
					mFriendPortField.requestFocus();
				}
				return true;
			}
		});
        mFriendPortField.setOnEditorActionListener(new TextView.OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
				if(TextUtils.isEmpty(textView.getText().toString())) {
					textView.setError("Put your port connection here");
				}else{
					if(mFriendIpAddressField.getText().length() < 7 && !mHostCheckBox.isChecked()){
						mFriendIpAddressField.setError("Put your friend IP address here");
						mFriendIpAddressField.requestFocus();
					}else{
						Intent intent = new Intent(mActivity, ChatActivity.class);
						intent.putExtra("IP_ADDRESS", mFriendIpAddressField.getText().toString()+":"+textView.getText().toString()+":"+mHostCheckBox.isChecked());
						mActivity.startActivityForResult(intent, 0);
					}
				}
				return true;
			}
		});
        
        mInviteButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if(mFriendIpAddressField.getText().length() < 7 && !mHostCheckBox.isChecked()){
					mFriendIpAddressField.setError("Put your friend IP address here");
					mFriendIpAddressField.requestFocus();
				}else if(mFriendPortField.getText().length() < 4){
					mFriendPortField.setError("Put your port connection here");
					mFriendPortField.requestFocus();
					
				}else{
					Intent intent = new Intent(mActivity, ChatActivity.class);
					intent.putExtra("IP_ADDRESS", mFriendIpAddressField.getText().toString()+":"+mFriendPortField.getText().toString()+":"+mHostCheckBox.isChecked());
					mActivity.startActivityForResult(intent, 0);
				}
			}
		});
     
        setWifiIpAddress(this);
        
        mWifiReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
	        	setWifiIpAddress(context);
            }
        };
       
    }

    @Override
	public void onResume() {
        super.onResume();
        IntentFilter filter = new IntentFilter();
        filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        registerReceiver(mWifiReceiver, filter);
    }
    
    @Override
	public void onPause() {
        super.onPause();
        unregisterReceiver(mWifiReceiver);
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
            ipAddressString = context.getString(R.string.ip_address_error);
        }
        
        mIpAddressField = (TextView) this.findViewById(R.id.ipAddressField);
        mIpAddressField.setText(ipAddressString);
    }
    
    public void onHostRadioYes(View view){
    	mFriendIpAddressField.setEnabled(false);
    	mFriendPortField.requestFocus();
    }
    
    public void onHostRadioNo(View view){
    	mFriendIpAddressField.setEnabled(true);
    	mFriendIpAddressField.requestFocus();
    	
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 0 && resultCode == Activity.RESULT_OK) {
        	 AlertDialog.Builder builder = new AlertDialog.Builder(this);
             builder.setMessage(data.getStringExtra("ERROR_DESC"))
             		.setTitle("Error")
                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            
                        }
                    });
             builder.create().show();
        }
    }
}
