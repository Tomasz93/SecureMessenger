package com.tmdroid.securemessenger;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

public class ChatAdapter extends BaseAdapter{
	
	ArrayList<Message> mMessages;
	Context mContext;
	LayoutInflater mInflater;
	
	public ChatAdapter(ArrayList<Message> messages, Context context){
		mMessages = messages;
		mContext = context;
		mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	@Override
	public int getCount() {
		return mMessages.size();
	}

	@Override
	public Object getItem(int position) {
		return mMessages.get(position);
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		Message message = (Message) this.getItem(position);
		TextView oneMsg;
		if(convertView == null)
		{
			convertView = mInflater.inflate(R.layout.row_chat_item, parent, false);
			oneMsg = (TextView) convertView.findViewById(R.id.chatMessageText);
			convertView.setTag(oneMsg);
		}
		else
			oneMsg = (TextView) convertView.getTag();
	 
		oneMsg.setText(message.getMessage());
	 
		LayoutParams lp = (LayoutParams) oneMsg.getLayoutParams();
		if(message.isMine())
		{
			//oneMsg.setBackgroundResource(R.drawable.white_chat);
			oneMsg.setTextColor(mContext.getResources().getColor(R.color.messengertheme_color));
			oneMsg.setTextAlignment(TextView.TEXT_ALIGNMENT_TEXT_END);
			lp.gravity = Gravity.RIGHT;
		}
		else
		{
			//oneMsg.setBackgroundResource(R.drawable.red_chat);
			oneMsg.setTextColor(Color.WHITE);
			lp.gravity = Gravity.LEFT;
		}
		oneMsg.setLayoutParams(lp);
		
		return convertView;
	}

}
