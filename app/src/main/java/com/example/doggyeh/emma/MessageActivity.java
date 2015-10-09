package com.example.doggyeh.emma;

import java.util.ArrayList;
import java.util.Random;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
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


//MessageActivity is a main Activity to ask EMMA questions

public class MessageActivity extends AppCompatActivity {
	ListView activityRootView;
	ArrayList<Message> messages;
	AwesomeAdapter adapter;
	EditText text;
	LinearLayout recorder;
	static Random rand = new Random();	
	static String sender = "EMMA";
	static SQLiteDatabase db;
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
		messages = new ArrayList<Message>();

		Cursor c=db.rawQuery("SELECT * FROM message", null);
		//Hello message from EMMA!
		if(c.getCount()==0){
			Message m = new Message("Hi! I'm Emma.", false);
			messages.add(m);
			db.execSQL("INSERT INTO message VALUES('"+(m.isMine()?1:0)+"','"+m.getMessage().replaceAll("'", "''")+"');");
			m = new Message("what can I do for you?", false);
			messages.add(m);
			db.execSQL("INSERT INTO message VALUES('"+(m.isMine()?1:0)+"','"+m.getMessage()+"');");
		}else{
			while(c.moveToNext()){
				messages.add(new Message(c.getString(1),c.getInt(0) != 0));
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
				if(heightDiff>1500 && !record1){
					record = false;
					recorder.setVisibility(LinearLayout.GONE);
				}
				if (!keyboard) {
			        if (heightDiff > 500) { // if more than 500 pixels, its probably a keyboard...
						activityRootView.setSelection(messages.size() - 1);
			        	keyboard = true;
			        }
		    	}else{
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
			Message m = new Message(newMessage, true);
			addNewMessage(m);
			db.execSQL("INSERT INTO message VALUES('"+(m.isMine()?1:0)+"','"+m.getMessage().replaceAll("'","''")+"');");
			new SendMessage().execute();
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
				addNewMessage(new Message(true,v[0])); //add new message, if there is no existing status message
			}
		}
		@Override
		protected void onPostExecute(String text) {
			if(messages.get(messages.size()-1).isStatusMessage)//check if there is any status message, now remove it.
			{
				messages.remove(messages.size()-1);
			}
			Message m = new Message(text, false);
			addNewMessage(m); // add the orignal message from server.
			db.execSQL("INSERT INTO message VALUES('"+(m.isMine()?1:0)+"','"+m.getMessage().replaceAll("'","''")+"');");
		}
	}
	void addNewMessage(Message m)
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