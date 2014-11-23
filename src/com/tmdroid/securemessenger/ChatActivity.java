package com.tmdroid.securemessenger;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

import javax.crypto.Cipher;

import org.apache.commons.codec.binary.Base64;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;

public class ChatActivity extends Activity {
	
	private ListView mChatListView;
	private EditText mMessageField;
	private ImageButton mChatSendButton;
	private CheckBox mInfoCheckBox;
	private ArrayList<Message> mMessagesList = new ArrayList<Message>();
	private ChatAdapter mChatAdapter;
	
	private ProgressDialog mDialog = null;
	private Handler mUpdateConversationHandler = null;
	private static boolean mCancel = false;
	private Boolean mAmIServer = false;
	private String mIpAddress = null;
	private int mPort = 0;
	private Socket mSocket = null;
	private ServerSocket mServerSocket = null;
	private Thread mMainThread = null;
	
	private DH mDH;
	private Cipher mCipher;

	@TargetApi(Build.VERSION_CODES.HONEYCOMB) 
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_chat);
		if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR1)
			getActionBar().setDisplayHomeAsUpEnabled(true);

        showProgressDialog();
        whoAmI();
        setUpViews();
        startCommunication();
	}

	@Override
	protected void onStop() {
		super.onStop();
		if(mAmIServer){
			try {
				mServerSocket.close();
				mCancel = true;
			} catch (IOException e) {
			}
		}
		if(mDialog != null)
			mDialog.dismiss();
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
	
	private void startCommunication(){
		mUpdateConversationHandler = new Handler();
		if(mAmIServer)
			mMainThread = new Thread(new ServerThread());
		else
			mMainThread = new Thread(new ClientThread());
		mMainThread.start();
	}
	
	private boolean keyExchange(String input) throws IOException {
		boolean flag = false;
		if(mDH == null)
			flag = true;
		else if(mDH.secretKey == null || !mDH.success)
			flag = true;
		
		if(flag){
			String tmp[];
			tmp = input.split(":");
			PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(mSocket.getOutputStream())),true);
			switch(tmp[0]){	
			case "STARTDH":
				mUpdateConversationHandler.post(new Thread(){
					@Override
					public void run(){
						mDialog.setMessage("Initiating a call...");
					}
				});
				mDH = new DH();
				mDH.initialize();
				out.println("FIRSTDH:"+mDH.publicP+":"+mDH.publicG);
				out.println("LOG-Prime P: "+mDH.publicP);
				out.println("LOG-Generator G: "+mDH.publicG);
				addMessage("Prime P: "+mDH.publicP, true);
				addMessage("Generator G:"+mDH.publicG, true);
				break;
			case "FIRSTDH":
				mUpdateConversationHandler.post(new Thread(){
					@Override
					public void run(){
						mDialog.setMessage("Initiating a call...");
					}
				});
				mDH = new DH();
				mDH.getInitializedValues(new BigInteger(tmp[1]), new BigInteger(tmp[2]));
				mDH.calculateMyKeys();
				out.println("SECONDDH:"+mDH.myPublicKey);
				out.println("LOG-PublicKey: "+mDH.myPublicKey);
				addMessage("PublicKey: "+mDH.myPublicKey, true);
				break;
			case "SECONDDH":
				mDH.getYourPublicKey(new BigInteger(tmp[1]));
				mDH.calculateMyKeys();
				out.println("THIRDDH:"+mDH.myPublicKey);
				out.println("LOG-PublicKey: "+mDH.myPublicKey);
				addMessage("PublicKey: "+mDH.myPublicKey, true);
				mDH.calculateSecretKey();
				break;
			case "THIRDDH":
				mDH.getYourPublicKey(new BigInteger(tmp[1]));
				mDH.calculateSecretKey();
				mDH.success = true;
				out.println("SUCCESSDH");
				out.println("LOG-Success!");
				addMessage("Success!", true);
				addMessage("Klucz: "+new BigInteger(mDH.secretKey), true);
				addMessage("Dlugosc: "+mDH.secretKey.length, true);
				if(mDialog != null)
					mDialog.dismiss();
				break;
			case "SUCCESSDH":
				mDH.success = true;
				addMessage("Klucz: "+new BigInteger(mDH.secretKey), true);
				addMessage("Dlugosc: "+mDH.secretKey.length, true);
				if(mDialog != null)
					mDialog.dismiss();
				break;
			default:
				return true;
			}
			return false;
		}else{
			return true;
		}
	}
	
	private String encryption(String text){
		if(mDH.key != null){
			String code = null;
			try {
				mCipher = Cipher.getInstance("AES");
				mCipher.init(Cipher.ENCRYPT_MODE, mDH.key);
				code = new String(Base64.encodeBase64(mCipher.doFinal(text.getBytes("UTF-8"))));
			} catch (Exception e) {
				catchException(e);
			}
			return code;
		}else
			return text;
	}
	
	private String decryption(String code){
		if(mDH.key != null){
			String text = null;
			try {
				mCipher = Cipher.getInstance("AES");
				mCipher.init(Cipher.DECRYPT_MODE, mDH.key);
				text = new String(mCipher.doFinal(Base64.decodeBase64(code.getBytes())), "UTF-8");
			} catch (Exception e) {
				catchException(e);
			} 
			return text;
		}else
			return code;
	}
	
	private void addMessage(String msg, boolean info){
		mUpdateConversationHandler.post(new updateUIThread(new Message(msg, true, info)));
	}
	
	private void catchException(final Exception e){
		mUpdateConversationHandler.post(new Thread(){
			@Override
			public void run(){
				setResult(Activity.RESULT_OK, new Intent().putExtra("ERROR_DESC", e.toString()));
				finish();
			}
		});
	}
	
	@TargetApi(Build.VERSION_CODES.HONEYCOMB) 
	private void setActionBarTitle(){
		if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR1){
			mIpAddress = mSocket.getInetAddress().getHostAddress();
			mUpdateConversationHandler.post(new Thread(){
				@Override
				public void run(){
					getActionBar().setTitle("IP: "+mIpAddress+", Port: "+mPort);
				}
			});
		}
	}
	
	private void showProgressDialog(){
		mDialog = new ProgressDialog(this);
		mDialog.setMessage("Waiting for friend...");
		mDialog.setOnCancelListener(new OnCancelListener() {
			@Override
			public void onCancel(DialogInterface dialog) {
				finish();
			}
		});
		mDialog.show();
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private void whoAmI(){
		String tmp[] = getIntent().getStringExtra("IP_ADDRESS").split(":");
        mIpAddress = tmp[0];
        mPort = Integer.parseInt(tmp[1]);
        if(tmp[2].contains("true")){
        	mAmIServer = true;
    		if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR1)
    			getActionBar().setTitle("IP: Waiting..., Port: "+mPort);
        }else{
        	mAmIServer = false;
    		if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR1)
    			getActionBar().setTitle("IP: "+mIpAddress+", Port: "+mPort);
        }
	}
	
	private void setUpViews(){
		mChatListView = (ListView) this.findViewById(R.id.chatListView);
        mMessageField = (EditText) this.findViewById(R.id.messageField);
        mInfoCheckBox = (CheckBox) this.findViewById(R.id.infoCheckBox);
        mChatSendButton	= (ImageButton) this.findViewById(R.id.chatSendButton);
        mChatSendButton.setEnabled(false);

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
					
					try {
						String msg = mMessageField.getText().toString();
						PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(mSocket.getOutputStream())),true);
						out.println(encryption(msg));
					} catch (final Exception e) {
						catchException(e);
					}

					 mMessagesList.add(new Message(encryption(mMessageField.getText().toString()), true, true));
					 mMessagesList.add(new Message(mMessageField.getText().toString(), true, false));
					 mChatAdapter.notifyDataSetChanged();
					 mMessageField.setText("");
					 mChatListView.setSelection(mChatAdapter.getCount()-1);
					 
				}
			}
		}); 
        mInfoCheckBox.setOnClickListener(new OnClickListener(){
        	@Override
        	public void onClick(View v) {
        		mChatAdapter.showInfo(mInfoCheckBox.isChecked());
        		mChatAdapter.notifyDataSetChanged();
        	}
        });
	}
	
	class ClientThread implements Runnable {
		@Override
		public void run() {
			try{
				mSocket = new Socket(mIpAddress, mPort);
				keyExchange("STARTDH");
				CommunicationThread commThread = new CommunicationThread(mSocket);
				new Thread(commThread).start();
			}catch(final Exception e){
				catchException(e);
			}
		}
	}
	
	class ServerThread implements Runnable {
		@Override
		public void run() {
			try{
				mServerSocket = new ServerSocket(mPort);
			}catch(final IOException e){
				catchException(e);
			}
			
			while(!Thread.currentThread().isInterrupted() && !mCancel){
				try{
					mSocket = mServerSocket.accept();
					setActionBarTitle();
					CommunicationThread commThread = new CommunicationThread(mSocket);
					new Thread(commThread).start();
				}catch(final IOException e){
					catchException(e);
				}
			}
		}
	}
	
	
	class CommunicationThread implements Runnable {
		private BufferedReader input;
		public CommunicationThread(Socket clientSocket) {
			mSocket = clientSocket;
			try{
				this.input = new BufferedReader(new InputStreamReader(mSocket.getInputStream()));
			}catch(IOException e){
				catchException(e);
			}
		}
		
		public void run() {
			while(!Thread.currentThread().isInterrupted() && !mCancel){
				try{
					String read = input.readLine();
					if(keyExchange(read)){
						if(read.contains("LOG")){
							String tmp[] = read.split("-");
							mUpdateConversationHandler.post(new updateUIThread(new Message(tmp[1], false, true)));
						}else{
							mUpdateConversationHandler.post(new updateUIThread(new Message(read, false, true)));
							mUpdateConversationHandler.post(new updateUIThread(new Message(decryption(read), false, false)));
						}
					}
				}catch(IOException e){
					catchException(e);
				}
			}
		}

	}
	

	class updateUIThread implements Runnable {
		private Message msg;

		public updateUIThread(Message msg) {
			this.msg = msg;
		}

		@Override
		public void run() {
			mMessagesList.add(msg);
			mChatAdapter.notifyDataSetChanged();
			mChatListView.setSelection(mChatAdapter.getCount()-1);
		}
	}	

}
