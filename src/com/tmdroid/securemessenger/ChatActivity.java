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
import java.net.UnknownHostException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;

public class ChatActivity extends Activity {
	
	private ListView mChatListView;
	private EditText mMessageField;
	private ImageButton mChatSendButton;
	private ArrayList<Message> mMessagesList = new ArrayList<Message>();
	private ChatAdapter mChatAdapter;
	
	private Boolean mAmIServer = false;
	private String mIpAddress = null;
	private int mPort = 0;
	private Socket mSocket = null;
	private ServerSocket mServerSocket = null;
	private Handler updateConversationHandler;
	private Thread mainThread = null;
	
	private ProgressDialog dialog;
	private boolean mCancel = false;
	
	private DH mDH;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_chat);
        getActionBar().setDisplayHomeAsUpEnabled(true);

        dialog = new ProgressDialog(this);
		dialog.setMessage("Waiting for friend...");
		dialog.setOnCancelListener(new OnCancelListener() {
			@Override
			public void onCancel(DialogInterface dialog) {
				finish();
			}
		});
		dialog.show();
        
        String tmp[] = getIntent().getStringExtra("IP_ADDRESS").split(":");
        mIpAddress = tmp[0];
        mPort = Integer.parseInt(tmp[1]);
        if(tmp[2].contains("true")){
        	mAmIServer = true;
            getActionBar().setTitle("IP: Waiting..., Port: "+mPort);
        }else{
        	mAmIServer = false;
            getActionBar().setTitle("IP: "+mIpAddress+", Port: "+mPort);
        }
        
        mChatListView = (ListView) this.findViewById(R.id.chatListView);
        mMessageField = (EditText) this.findViewById(R.id.messageField);
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
					} catch (UnknownHostException e) {
						Log.e("SendMessageFailed","UnknownHostException -> "+e);
					} catch (IOException e) {
						Log.e("SendMessageFailed","IOException -> "+e);
					} catch (Exception e) {
						Log.e("SendMessageFailed","Exception -> "+e);
					}
					 
					 mMessagesList.add(new Message(mMessageField.getText().toString(), true));
					 mChatAdapter.notifyDataSetChanged();
					 mMessageField.setText("");
					 mChatListView.setSelection(mChatAdapter.getCount()-1);
					 
				}
			}
		});
        
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
				Log.e("ServerSocket","Can't close socket -> "+e);
			}
		}
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
	
	public void startCommunication(){
		if(mAmIServer){
			ServerMethod();
		}else{
			ClientMethod();
		}
	}
	
	public void ServerMethod(){
		updateConversationHandler = new Handler();
		mainThread = new Thread(new ServerThread());
		mainThread.start();
	}
	
	public void ClientMethod(){
		updateConversationHandler = new Handler();
		mainThread = new Thread(new ClientThread());
		mainThread.start();
	}
	
	public void keyExchange(String input) throws IOException {
		boolean flag = false;
		
		if(mDH == null){
			flag = true;
		}else if(mDH.secretKey == null){
			flag = true;
		}
		if(flag){
			String tmp[];
			tmp = input.split(":");
			PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(mSocket.getOutputStream())),true);
			Log.d("KeyExchange", tmp[0]);
			switch(tmp[0]){	
			case "STARTDH":
				mDH = new DH();
				mDH.initialize();
				out.println("FIRSTDH:"+mDH.publicP+":"+mDH.publicG);
				addMessage("FIRSTDH:"+mDH.publicP+":"+mDH.publicG);
				break;
			case "FIRSTDH":
				mDH = new DH();
				mDH.getInitializedValues(new BigInteger(tmp[1]), new BigInteger(tmp[2]));
				mDH.calculateMyKeys();
				out.println("SECONDDH:"+mDH.myPublicKey);
				addMessage("SECONDDH:"+mDH.myPublicKey);
				break;
			case "SECONDDH":
				mDH.getYourPublicKey(new BigInteger(tmp[1]));
				mDH.calculateMyKeys();
				out.println("THIRDDH:"+mDH.myPublicKey);
				addMessage("THIRDDH:"+mDH.myPublicKey);
				mDH.calculateSecretKey();
				out.println("Mój klucz to: "+mDH.secretKey);
				addMessage("Mój klucz to: "+mDH.secretKey);
				out.println("Dlugosc klucza: "+mDH.secretKey.toByteArray().length);
				addMessage("Dlugosc klucza: "+mDH.secretKey.toByteArray().length);
				if(dialog != null)
					dialog.dismiss();
				break;
			case "THIRDDH":
				mDH.getYourPublicKey(new BigInteger(tmp[1]));
				mDH.calculateSecretKey();
				
				out.println("Mój klucz to: "+mDH.secretKey);
				addMessage("Mój klucz to: "+mDH.secretKey);
				out.println("Dlugosc klucza: "+mDH.secretKey.toByteArray().length);
				addMessage("Dlugosc klucza: "+mDH.secretKey.toByteArray().length);
				if(dialog != null)
					dialog.dismiss();
				break;
			}
		}
	}
	
	public String encryption(String text){
		//TODO: implementacja szyfrowania
		//byte[] code = text.getBytes();
		
		return text;
	}
	
	public String decryption(String code){
		//TODO: implementacja deszyfrowania
		//byte[] text = code.getBytes();
		return code;
	}
	
	public byte[] hashSecretKey(byte[] key){
		MessageDigest md = null;
		try {
			md = MessageDigest.getInstance("MD5");
			byte[] hashedKey = md.digest(key);
			return hashedKey;
		} catch (NoSuchAlgorithmException e) {
			return key;
		}
	}
	
	public void addMessage(String msg){
		mMessagesList.add(new Message(msg, true));
		mChatAdapter.notifyDataSetChanged();
		mChatListView.setSelection(mChatAdapter.getCount()-1);
	}
	
	class ClientThread implements Runnable {

		@Override
		public void run() {
			try {
				mSocket = new Socket(mIpAddress, mPort);
				
				/*if(dialog != null)
					dialog.dismiss();*/
				keyExchange("STARTDH");

				CommunicationThread commThread = new CommunicationThread(mSocket);
				new Thread(commThread).start();

			} catch (UnknownHostException e1) {
				Log.e("ClientThread","UnknownHost! -> "+e1);
			} catch (IOException e1) {
				Log.e("ClientThread","IOException! -> "+e1);
			}

		}

	}
	
	class ServerThread implements Runnable {

		public void run() {
			mSocket = null;
			try {
				mServerSocket = new ServerSocket(mPort);
			} catch (IOException e) {
				Log.e("ServerThread","Can't create server socket on this port -> "+e);
			}
			while (!Thread.currentThread().isInterrupted() || !mCancel) {

				try {
					mSocket = mServerSocket.accept();
					mIpAddress = mSocket.getInetAddress().getHostAddress();
					//mActivity.getActionBar().setTitle("IP: "+mIpAddress+", Port: "+mPort);
					/*if(dialog != null)
						dialog.dismiss();*/

					CommunicationThread commThread = new CommunicationThread(mSocket);
					new Thread(commThread).start();
				} catch (IOException e) {
					//Log.e("ServerThread","Accept() problem. -> "+e);
				}
			}
		}
	}
	
	class CommunicationThread implements Runnable {

		private BufferedReader input;

		public CommunicationThread(Socket clientSocket) {

			mSocket = clientSocket;

			try {

				this.input = new BufferedReader(new InputStreamReader(mSocket.getInputStream()));

			} catch (IOException e) {
				Log.e("CommunicationThread","BufferedReader problem. -> "+e);
			}
		}

		public void run() {

			while (!Thread.currentThread().isInterrupted() || !mCancel) {

				try {

					String read = input.readLine();
					keyExchange(read);
					updateConversationHandler.post(new updateUIThread(decryption(read)));
					
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

	}

	class updateUIThread implements Runnable {
		private Message msg;

		public updateUIThread(String str) {
			msg = new Message(str, false);
		}

		@Override
		public void run() {
			mMessagesList.add(msg);
			mChatAdapter.notifyDataSetChanged();
			mChatListView.setSelection(mChatAdapter.getCount()-1);
		}
	}
}
