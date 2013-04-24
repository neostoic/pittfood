package com.example.yelpAnalysis;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.Toast;

import com.Yuwei.pittfood.RestClient;
import com.Yuwei.pittfood.UserFunctions;

public class SelectRecActivity extends Activity {
	
	UserFunctions userFunctions = new UserFunctions();
	
	private static String baseURL = "https://api.mongolab.com/api/1/databases/yelptest/collections/";
	private static String preTable = "prediction";
	private static String ratingTable = "rating";
	private static String apikey = "apiKey=uUA22oxSPz3xkYkVkYY8ju3hYPMDugfK";
	public static JSONArray jsonarray = null;
	public static JSONObject json = null;
	
	static ArrayList<String> bidList = new ArrayList<String>();
	static ArrayList<Double> ratingList = new ArrayList<Double>();
	static ArrayList<String> values = new ArrayList<String>();
	static String user_id;
	static String url;
	static int count;
	static String prerating;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_select_rec);
		StrictMode.enableDefaults();
		
        // Set up action bar.
        final ActionBar actionBar = getActionBar();
        actionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#002649")));
        
        String userName = userFunctions.isUserLoggedIn(getApplicationContext());
        try {
			user_id = userFunctions.getUserid(userName);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
        // get radio button
        final RadioButton newSelect= (RadioButton)findViewById(R.id.rec_0);
        final RadioButton oldSelect= (RadioButton)findViewById(R.id.rec_1);
        final RadioButton bothSelect= (RadioButton)findViewById(R.id.rec_2);
        final RadioButton fiveRest= (RadioButton)findViewById(R.id.amount_0);
        final RadioButton tenRest= (RadioButton)findViewById(R.id.amount_1);
        final RadioButton fiftRest= (RadioButton)findViewById(R.id.amount_2);
        
        newSelect.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View arg0) {
				prerating = "prediction";
	        	url = baseURL + preTable + "?q=%7b'user_id':'" + user_id + "'%7d&f=%7b'business_id':1,'prediction':1%7d&" + apikey;
			}	
        });
        
        oldSelect.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View arg0) {
				prerating = "rating";
	        	url = baseURL + ratingTable + "?q=%7b'user_id':'" + user_id + "'%7d&f=%7b'business_id':1,'rating':1%7d&" + apikey;
			}	
        });
        
        bothSelect.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0) {
				prerating = "both";
			}
        	
        });
        
        fiveRest.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View arg0) {
				count = 5;
			}	
        });
        
        tenRest.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View arg0) {
				count = 10;
			}       	
        });
        
        fiftRest.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View arg0) {
				count = 15;
			}
        });
        
		// generate key button
		Button button = (Button)findViewById(R.id.rec_button);
		button.setOnClickListener(new OnClickListener(){
			
			public void onClick(View v){
				if((newSelect.isChecked()||oldSelect.isChecked()||bothSelect.isChecked())==false){
					Toast.makeText(getApplicationContext(), "Please select a type and amount", Toast.LENGTH_SHORT).show();
				} else if((fiveRest.isChecked()||tenRest.isChecked()||fiftRest.isChecked())==false){
					Toast.makeText(getApplicationContext(), "Please select a type and amount", Toast.LENGTH_SHORT).show();
				} else{
					// test arraylist
					try {
						getRes(prerating);
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
					getBid(count);
					
					Intent intent = new Intent(SelectRecActivity.this, LoadingActivity.class);
					intent.putExtra("ratingList", values);
					SelectRecActivity.this.startActivity(intent);
					values.clear();
					SelectRecActivity.this.finish();
				}		
			}
		});	
	}

	
    // this is for 'setting' menu
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
	    //check selected menu item
	    if(item.getItemId()==R.id.menu_exit) {
	    //close the Activity
	    	this.finish();
	    	return true;
	    }
	    if(item.getItemId()==android.R.id.home) {
	    	// back to last activity
	    	return true;
	    }
	    return false;
	}
    
    public void getRes(String state) throws JSONException {
    	if(state.equals("both")){
    		getResBoth();
    	} else{
    		RestClient rc = new RestClient();
    		String reslist = rc.connect(url);
    		jsonarray = new JSONArray(reslist);
    		for (int i=0; i < jsonarray.length(); i++) {
    			json = jsonarray.getJSONObject(i);
    			bidList.add(json.getString("business_id"));
    			ratingList.add(json.getDouble(state));
    		}
    		sort();
    	}
	}
    
    public void getResBoth() throws JSONException {
    	RestClient rc = new RestClient();
    	String newurl1 = "https://api.mongolab.com/api/1/databases/yelptest/collections/prediction?q=%7b'user_id':'" + user_id + "'%7d&f=%7b'business_id':1,'prediction':1%7d&apiKey=uUA22oxSPz3xkYkVkYY8ju3hYPMDugfK";
    	String reslist1 = rc.connect(newurl1);
    	jsonarray = new JSONArray(reslist1);
    	for (int i=0; i < jsonarray.length(); i++) {
			json = jsonarray.getJSONObject(i);
			bidList.add(json.getString("business_id"));
			ratingList.add(json.getDouble("prediction"));
		}
    	String newurl2 = "https://api.mongolab.com/api/1/databases/yelptest/collections/rating?q=%7b'user_id':'" + user_id + "'%7d&f=%7b'business_id':1,'rating':1%7d&apiKey=uUA22oxSPz3xkYkVkYY8ju3hYPMDugfK";
    	String reslist2 = rc.connect(newurl2);
    	jsonarray = new JSONArray(reslist2);
    	for (int i=0; i < jsonarray.length(); i++) {
			json = jsonarray.getJSONObject(i);
			bidList.add(json.getString("business_id"));
			ratingList.add(json.getDouble("rating"));
		}
    	sort();
    }
    
    private void sort(){
    	int n = ratingList.size();
    	for (int p=1; p<n; p++) {
    		for (int i=0; i<n-p; i++) {
    			if(ratingList.get(i) > ratingList.get(i+1)) {
    				double temp = ratingList.get(i);
    				String str = bidList.get(i);
    				ratingList.set(i, ratingList.get(i+1));
    				bidList.set(i, bidList.get(i+1));
    				ratingList.set(i+1, temp);
    				bidList.set(i+1, str);
    			}
    		}
    	}
    }
    
    private void getBid(int n) {
    	for (int i = 0; i < n; i++)
    		values.add(bidList.get(i));
    }

}
