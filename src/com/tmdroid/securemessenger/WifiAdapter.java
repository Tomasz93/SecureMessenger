package com.tmdroid.securemessenger;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class WifiAdapter extends BaseAdapter{
	
	private ArrayList<WifiDevice> mPeers;
	private LayoutInflater mInflater;
	
	public WifiAdapter(ArrayList<WifiDevice> peers, Context context){
		mPeers = peers;
		mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	@Override
	public int getCount() {
		return mPeers.size();
	}

	@Override
	public Object getItem(int position) {
		return mPeers.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}
	
	public class OnePeer{
		TextView mDeviceName;
		TextView mDeviceMacAddress;
		ImageView mStatus;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		WifiDevice peer = (WifiDevice) this.getItem(position);
		OnePeer onePeer;
		if(convertView == null)
		{
			onePeer = new OnePeer();
			convertView = mInflater.inflate(R.layout.row_wifi_item, parent, false);
			onePeer.mDeviceName = (TextView) convertView.findViewById(R.id.deviceNameText);
			onePeer.mDeviceMacAddress = (TextView) convertView.findViewById(R.id.deviceMacText);
			onePeer.mStatus = (ImageView) convertView.findViewById(R.id.deviceStatusImage);
			convertView.setTag(onePeer);
		}
		else
			onePeer = (OnePeer) convertView.getTag();
		
		onePeer.mDeviceName.setText(peer.device.deviceName);
		onePeer.mDeviceMacAddress.setText(peer.device.deviceAddress);
		
		switch(peer.status){
		case 0:
			onePeer.mStatus.setImageResource(android.R.drawable.presence_invisible);
			break;
		case 1:
			onePeer.mStatus.setImageResource(android.R.drawable.presence_away);
			break;
		case 2:
			onePeer.mStatus.setImageResource(android.R.drawable.presence_online);
			break;
		}

		return convertView;
	}

}
