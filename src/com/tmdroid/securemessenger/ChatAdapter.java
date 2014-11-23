package com.tmdroid.securemessenger;

import java.util.ArrayList;
import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
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
	boolean mShowInfo;
	
	public ChatAdapter(ArrayList<Message> messages, Context context){
		mMessages = messages;
		mContext = context;
		mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mShowInfo = false;
	}
	
	public void showInfo(boolean show){
		mShowInfo = show;
	}

	@Override
	public int getCount() {
		int count = 0;
		for(int i=0; i<mMessages.size(); i++){
			if(!(!mShowInfo && mMessages.get(i).isInfo()))
				count++;
		}
		return count;
	}

	@Override
	public Object getItem(int position) {
		if(mShowInfo)
			return mMessages.get(position);
		else{
			int a = 0, i = 0;
			while(a <= position){
				if(!mMessages.get(i).isInfo())
					a++;
				i++;
			}
			return mMessages.get(i-1);
		}
	}	

	@Override
	public long getItemId(int position) {
		return 0;
	}

	@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1) @Override
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
		
		
		if(!message.isInfo()){
			if(message.isMine())
			{
				oneMsg.setTextColor(mContext.getResources().getColor(R.color.messengertheme_color));
				if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR1)
					oneMsg.setTextAlignment(TextView.TEXT_ALIGNMENT_TEXT_END);
				lp.gravity = Gravity.RIGHT | Gravity.CENTER_VERTICAL;
			}
			else
			{
				oneMsg.setTextColor(Color.WHITE);
				lp.gravity = Gravity.LEFT | Gravity.CENTER_VERTICAL;
			}
		}else{
			oneMsg.setTextColor(Color.BLUE);
			if(message.isMine()){
				oneMsg.setText("(ME) "+oneMsg.getText());
			}else{
				oneMsg.setText("(FRIEND) "+oneMsg.getText());
			}
		}
		oneMsg.setLayoutParams(lp);
		
		/*if(!mShowInfo && message.isInfo()){
			convertView.setVisibility(View.GONE);
		}
		else{
			convertView.setVisibility(View.VISIBLE);
		}*/
		
		return convertView;
	}

}
