package com.tmdroid.securemessenger;

import java.util.ArrayList;

import android.app.Activity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;

public class ChatActivity extends Activity {
	
	ListView mChatListView;
	LinearLayout mMessageLayout;
	EditText mMessageField;
	ImageButton mChatSendButton;
	ArrayList<Message> mMessagesList = new ArrayList<Message>();
	ChatAdapter mChatAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_chat);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        
        mChatListView = (ListView) this.findViewById(R.id.chatListView);
        mMessageLayout = (LinearLayout) this.findViewById(R.id.messageLinearLayout);
        mMessageField = (EditText) this.findViewById(R.id.messageField);
        mChatSendButton	= (ImageButton) this.findViewById(R.id.chatSendButton);
        
        mChatSendButton.setEnabled(false);

        mMessagesList.add(new Message("Lorem Ipsum jest tekstem stosowanym jako przyk³adowy wype³niacz w przemyœle poligraficznym.", true));
        mMessagesList.add(new Message("Lorem Ipsum jest tekstem stosowanym jako przyk³adowy wype³niacz w przemyœle poligraficznym.", false));
        mMessagesList.add(new Message("Lorem Ipsum jest tekstem stosowanym jako przyk³adowy wype³niacz w przemyœle poligraficznym.", true));
        mMessagesList.add(new Message("Lorem Ipsum jest tekstem stosowanym jako przyk³adowy wype³niacz w przemyœle poligraficznym.", false));
       
        mChatAdapter = new ChatAdapter(mMessagesList, this);
        mChatListView.setAdapter(mChatAdapter);
        mMessageField.setOnFocusChangeListener(new OnFocusChangeListener() {
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if(hasFocus){
					mMessageField.setText("");
					mChatSendButton.setEnabled(true);
				}
			}
		});
        
        mChatSendButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if(mMessageField.getText().toString().length() > 0){
					 mMessagesList.add(new Message(mMessageField.getText().toString(), true));
					 mChatAdapter.notifyDataSetChanged();
					 mMessageField.setText("");
					 mChatListView.setSelection(mChatAdapter.getCount()-1);
				}
			}
		});
	}
	
	@Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case android.R.id.home:
            finish();
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }
}
