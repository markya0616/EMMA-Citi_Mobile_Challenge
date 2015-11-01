package com.example.doggyeh.emma;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.speech.RecognizerIntent;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.app.ListActivity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.support.v7.widget.Toolbar;
import android.text.TextWatcher;
import android.text.Editable;
import android.os.Message;
import org.json.JSONException;
import org.json.JSONObject;


//MessageActivity is a main Activity to ask EMMA questions

public class MessageActivity extends AppCompatActivity {
	ListView activityRootView;
	ArrayList<MyMessage> messages;
	AwesomeAdapter adapter;
	EditText text;
	LinearLayout recorder;
	static Random rand = new Random();	
	static final String sender = "EMMA";
	static SQLiteDatabase db;
	static final String TAG = "Robin";
	Boolean keyboard = false;	//keyboard status
	Boolean record = false;		//recorder status
	Boolean record1 = false;	//switch between keyboard and recorder
	private static final int REQUEST_CODE = 1234;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.chat);
		activityRootView = (ListView) findViewById(android.R.id.list);
		Toolbar toolbar = (Toolbar) findViewById(R.id.id_toolbar);
		//toolbar.setLogo(R.drawable.ic_launcher);
		//toolbar.setContentInsetsAbsolute(0, 0);
		setSupportActionBar(toolbar);
		getSupportActionBar().setHomeButtonEnabled(true);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setDisplayShowTitleEnabled(false);

		//Change the send icon between SEND and VOICE_INPUT
		text = (EditText) this.findViewById(R.id.text);
		text.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				Button sendbutton = (Button) findViewById(R.id.send_button);
				if (count > 0)
					sendbutton.setBackgroundResource(R.drawable.send_button);
				else
					sendbutton.setBackgroundResource(R.drawable.record_button);
			}
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}
			@Override
			public void afterTextChanged(Editable s) {
			}

		});

		db=openOrCreateDatabase("MessageDB", MessageActivity.this.MODE_PRIVATE, null);
		db.execSQL("CREATE TABLE IF NOT EXISTS message(sender INTEGER,message text);");
		
		//sender = Utility.sender[rand.nextInt( Utility.sender.length-1)];
		this.setTitle(sender);
		messages = new ArrayList<MyMessage>();

		Cursor c=db.rawQuery("SELECT * FROM message", null);
		//Hello message from EMMA!
		if(c.getCount()==0){
			MyMessage m = new MyMessage("Hi! I'm Emma.", false);
			messages.add(m);
			db.execSQL("INSERT INTO message VALUES('"+(m.isMine()?1:0)+"','"+m.getMessage().replaceAll("'", "''")+"');");
			m = new MyMessage("what can I do for you?", false);
			messages.add(m);
			db.execSQL("INSERT INTO message VALUES('"+(m.isMine()?1:0)+"','"+m.getMessage()+"');");
		}else{
			while(c.moveToNext()){
				messages.add(new MyMessage(c.getString(1),c.getInt(0) != 0));
			}
		}
		adapter = new AwesomeAdapter(this, messages);
		//setListAdapter(adapter);
		activityRootView.setAdapter(adapter);
		activityRootView.setSelection(messages.size() - 1);
		//Log.d("Robin",""+((ListView) findViewById(android.R.id.list)).getChildAt(0));
		recorder = (LinearLayout)findViewById(R.id.recoder_image);

		//Monitor the keyboard and recorder status and adjust it.
		//Only one can be show at one time or too crowded.
		activityRootView.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
			@Override
			public void onGlobalLayout() {
				//Log.d("Robin",""+activityRootView.getRootView().getHeight()+" , "+activityRootView.getHeight());
				int heightDiff = activityRootView.getRootView().getHeight() - activityRootView.getHeight();
				if (heightDiff > 1500 && !record1) {
					record = false;
					recorder.setVisibility(LinearLayout.GONE);
				}
				if (!keyboard) {
					if (heightDiff > 500) { // if more than 500 pixels, its probably a keyboard...
						activityRootView.setSelection(messages.size() - 1);
						keyboard = true;
					}
				} else {
					if (heightDiff < 500) {
						//getListView().setSelection(messages.size()-1);
						keyboard = false;
					}
				}
				if (record1)
					record1 = false;
			}
		});
	}

	public void sendMessage(View v)
	{
		String newMessage = text.getText().toString().trim(); 
		if(newMessage.length() > 0)
		{
			text.setText("");
			if(messages.get(messages.size()-1).isStatusMessage)//check if there is any status message, now remove it.
			{
				messages.remove(messages.size()-1);
			}
			MyMessage m = new MyMessage(newMessage, true);
			addNewMessage(m);
			db.execSQL("INSERT INTO message VALUES('" + (m.isMine() ? 1 : 0) + "','" + m.getMessage().replaceAll("'", "''") + "');");
			//new SendMessage().execute();

			//Send message to server
			new ServerTask().execute(newMessage);

		}else{
			if(!record){
				if (keyboard)
					record1 = true;
				InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(this.getCurrentFocus().getWindowToken(), 0);

				record = true;
				recorder.setVisibility(LinearLayout.VISIBLE);
			}else{
				record = false;
				recorder.setVisibility(LinearLayout.GONE);
			}
		}
	}
	public static final String serviceUrl="https://doggyeh.pagekite.me/todo/api/v1.0/tasks";
	public static final int CONNECTION_TIMEOUT = 10000;
	public static final int DATARETRIEVAL_TIMEOUT = 10000;
	private class ServerTask extends AsyncTask<String, Integer, String> {
		@Override
		protected String doInBackground(String... params) {

				HttpURLConnection urlConnection = null;
				try {

					// create connection
					Log.d(TAG,"robin");
					URL urlToRequest = new URL(serviceUrl);
					urlConnection = (HttpURLConnection) urlToRequest.openConnection();
					urlConnection.setConnectTimeout(CONNECTION_TIMEOUT);
					urlConnection.setReadTimeout(DATARETRIEVAL_TIMEOUT);
					urlConnection.setRequestMethod("POST");
					urlConnection.setDoInput(true);
					urlConnection.setDoOutput(true);
					urlConnection.setRequestProperty("Content-Type", "application/json;charset=UTF-8");

					//Create JSONObject here
					JSONObject jsonParam = new JSONObject();
					jsonParam.put("question", params[0]);

					OutputStream os = urlConnection.getOutputStream();
					os.write(jsonParam.toString().getBytes("UTF-8"));
					os.close();
					Log.d(TAG,jsonParam.toString());

					// handle issues
					int statusCode = urlConnection.getResponseCode();
					if (statusCode == HttpURLConnection.HTTP_UNAUTHORIZED) {
						// handle unauthorized (if service requires user login)
						Log.d(TAG,"robin error1");
					} else if (statusCode != HttpURLConnection.HTTP_OK) {
						// handle any other errors, like 404, 500,..
						Log.d(TAG,"robin error2");
					}

					// create JSON object from content
					InputStream in = new BufferedInputStream(urlConnection.getInputStream());
					JSONObject jobj = new JSONObject(getResponseText(in));
					Log.d(TAG, jobj.getString("answer"));
					MyMessage m = new MyMessage(jobj.getString("answer"), false);
					Message msg = new Message();
					msg.obj = m;
					mHandler.sendMessage(msg);

				} catch (MalformedURLException e) {
					// URL is invalid
				} catch (SocketTimeoutException e) {
					// data retrieval or connection timed out
				} catch (IOException e) {
					// could not read response body
					// (could not create input stream)
				} catch (JSONException e) {
					// response body is no valid JSON string
				} finally {
					if (urlConnection != null) {
						urlConnection.disconnect();
					}
				}
		return "";
		}
	}
	private static String getResponseText(InputStream inStream) {
		// very nice trick from
		// http://weblogs.java.net/blog/pat/archive/2004/10/stupid_scanner_1.html
		return new Scanner(inStream).useDelimiter("\\A").next();
	}

	Handler mHandler = new Handler(){
		public void handleMessage(Message msg){
			MyMessage m = (MyMessage) msg.obj;
			addNewMessage(m); // add the orignal message from server.
			db.execSQL("INSERT INTO message VALUES('" + (m.isMine() ? 1 : 0) + "','" + m.getMessage().replaceAll("'", "''") + "');");

		}
	};

	//Voice input
	public void startRecord(View v)
	{
		Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
		intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
				RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
		intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speaking to EMMA...");
		startActivityForResult(intent, REQUEST_CODE);
	}
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		if (requestCode == REQUEST_CODE && resultCode == RESULT_OK)
		{
			ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
			String firstMatch = (String)result.get(0);
			text.setText(firstMatch);
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	private class SendMessage extends AsyncTask<Void, String, String>
	{
		@Override
		protected String doInBackground(Void... params) {
			/*
			this.publishProgress(String.format("%s is typing", sender));
			try {
				Thread.sleep(1000); //simulate a network call
			}catch (InterruptedException e) {
				e.printStackTrace();
			}
			*/
			return Utility.messages[rand.nextInt(Utility.messages.length-1)];
		}
		@Override
		public void onProgressUpdate(String... v) {
			
			if(messages.get(messages.size()-1).isStatusMessage)//check wether we have already added a status message
			{
				messages.get(messages.size()-1).setMessage(v[0]); //update the status for that
				adapter.notifyDataSetChanged();
				activityRootView.setSelection(messages.size() - 1);
			}
			else{
				addNewMessage(new MyMessage(true,v[0])); //add new message, if there is no existing status message
			}
		}
		@Override
		protected void onPostExecute(String text) {
			if(messages.get(messages.size()-1).isStatusMessage)//check if there is any status message, now remove it.
			{
				messages.remove(messages.size()-1);
			}
			MyMessage m = new MyMessage(text, false);
			addNewMessage(m); // add the orignal message from server.
			db.execSQL("INSERT INTO message VALUES('"+(m.isMine()?1:0)+"','"+m.getMessage().replaceAll("'","''")+"');");
		}
	}
	void addNewMessage(MyMessage m)
	{
		messages.add(m);
		adapter.notifyDataSetChanged();
		activityRootView.setSelection(messages.size() - 1);

	}
	@Override
	public boolean onOptionsItemSelected(MenuItem menuItem) {
		Log.d("Robin", "" + menuItem.getItemId());
		if (menuItem.getItemId() == android.R.id.home) {
			finish();
			overridePendingTransition(R.transition.slide_in_right, R.transition.slide_out_right);
		}
		return super.onOptionsItemSelected(menuItem);
	}

	@Override
	public void onBackPressed(){
		finish();
		overridePendingTransition(R.transition.slide_in_right, R.transition.slide_out_right);
		super.onBackPressed();
	}

}