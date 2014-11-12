package com.tmdroid.securemessenger;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
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
        getActionBar().setTitle("IP: "+mIpAddress+", Port: "+mPort);
        
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
						out.println(msg);
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
        mAmIServer = true;
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
	
	public void keyExchange(String input) throws IOException{
		if(mDH != null){
			if(mDH.key != null && !mDH.test){
				String tmp[];
				tmp = input.split(":");
				PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(mSocket.getOutputStream())),true);
				switch(tmp[0]){
				case "STARTDH":
					mDH = new DH();
					mDH.initialize();
					out.println("FIRSTDH:"+mDH.publicP+":"+mDH.publicG);
					break;
				case "FIRSTDH":
					mDH = new DH();
					mDH.getPublicPAndG(BigInteger.valueOf(Long.parseLong(tmp[1])),BigInteger.valueOf(Long.parseLong(tmp[2])));
					mDH.calculateMySecretValue();
					out.println("SECONDDH:"+mDH.publicA);
					break;
				case "SECONDDH":
					mDH.getPublicB(BigInteger.valueOf(Long.parseLong(tmp[1])));
					mDH.calculateMySecretValue();
					out.println("THIRDDH:"+mDH.publicA);
					mDH.calculateKey();
					break;
				case "THIRDDH":
					mDH.getPublicB(BigInteger.valueOf(Long.parseLong(tmp[1])));
					mDH.calculateKey();
					out.println("TESTDH:"+encryption("ALAMAKOTA_KOTMAALE"));
					break;
				case "TESTDH":
					if(tmp[1] != decryption("ALAMAKOTA_KOTMAALE")){
						out.println("FAILURE");
						mDH = null;
					}else{
						out.println("SUCCESSDH");
						if(dialog != null)
							dialog.dismiss();
					}
					break;
				case "FAILUREDH":
					keyExchange("STARTDH");
					break;
				case "SUCCESSDH":
					if(dialog != null)
						dialog.dismiss();
					break;
				}
			}
		}
	}
	
	public String encryption(String text){
		return text;
	}
	
	public String decryption(String code){
		return code;
	}
	
	public 
	
	class ClientThread implements Runnable {

		@Override
		public void run() {
			try {
				InetAddress serverAddr = InetAddress.getByName(mIpAddress);
				mSocket = new Socket(serverAddr, mPort);
				
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
					/*if(dialog != null)
						dialog.dismiss();*/

					CommunicationThread commThread = new CommunicationThread(mSocket);
					new Thread(commThread).start();
				} catch (IOException e) {
					Log.e("ServerThread","Accept() problem. -> "+e);
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
					updateConversationHandler.post(new updateUIThread(read));
					
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
		}
	}
}
