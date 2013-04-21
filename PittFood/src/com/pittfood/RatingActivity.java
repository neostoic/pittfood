package com.pittfood;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Random;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.protocol.HTTP;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

@TargetApi(Build.VERSION_CODES.GINGERBREAD)
public class RatingActivity extends Activity {
	
	Button btnLinkToRecScreen;
	
	private static String baseURL = "https://api.mongolab.com/api/1/databases/yelptest/collections/restaurant?";
	private static String myAPIKEY = "apiKey=uUA22oxSPz3xkYkVkYY8ju3hYPMDugfK";
	public static JSONArray jsonarray = null;
	public static JSONObject json = null;
	
	static ArrayList<String> bidList = new ArrayList<String>();
	static ArrayList<String> nameList = new ArrayList<String>();
	static ArrayList<String> addressList = new ArrayList<String>();
	
	static String uname = null;
	static String uid = null;
	
	static Random rd = new Random();
	
	UserFunctions userFunctions = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        
        userFunctions = new UserFunctions();	// Check login status in database
        
        if(userFunctions.isUserLoggedIn(getApplicationContext()) != null) {		// user already logged in show rating activity
        	
        	uname = userFunctions.isUserLoggedIn(getApplicationContext());
        	try {
				uid = userFunctions.getUserid(uname);
			} catch (JSONException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
        	
        	setContentView(R.layout.rating);
       
	    	try {
				getRes();
			} catch (JSONException e) {
				Toast.makeText(getApplicationContext(), "Check Internet connection", Toast.LENGTH_SHORT).show();
			}
	    	ListView listView = (ListView) findViewById(R.id.list);
	    	ListAdapter customAdapter;
			try {
				customAdapter = new ListAdapter(RatingActivity.this, R.layout.reslistview, bidList, nameList, addressList);
				listView.setAdapter(customAdapter);
			} catch (JSONException e) {
				
			}
			
			btnLinkToRecScreen = (Button) findViewById(R.id.btnLinkToRecScreen);
			btnLinkToRecScreen.setOnClickListener(new View.OnClickListener() {
				public void onClick(View view) {
					Thread t = new Thread() {
						public void run() {
							String ratingurl = "https://api.mongolab.com/api/1/databases/yelptest/collections/rating?apiKey=uUA22oxSPz3xkYkVkYY8ju3hYPMDugfK";
							HttpClient client = new DefaultHttpClient();
			                HttpConnectionParams.setConnectionTimeout(client.getParams(), 10000); //Timeout Limit
			                HttpResponse response;
			                JSONObject json = new JSONObject();
			                
			                float [] ratings = ListAdapter.getRating();
							
							for (int i = 0; i < ratings.length; i++) {
								if (ratings[i] != 0) {
									try {
					                    HttpPost post = new HttpPost(ratingurl);
					                    json.put("business_id", bidList.get(i));
					                    json.put("user_id", uid);
					                    json.put("rating", (int)(ratings[i]));
					                    StringEntity se = new StringEntity( json.toString());  
					                    se.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
					                    post.setEntity(se);
					                    response = client.execute(post);
		
					                    if(response!=null){
					                        InputStream in = response.getEntity().getContent(); //Get the data in the entity
					                    }
		
					                } catch(Exception e) {
					                    e.printStackTrace();
					                }
								}
							}
						 }
					};
					
					t.start();
					 
					Intent register = new Intent(getApplicationContext(), DashboardActivity.class);
					startActivity(register);
					finish();
				}
				
			});
        	
        }
        else {
            Intent login = new Intent(getApplicationContext(), LoginActivity.class);	// user is not logged in show login screen
            login.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(login);
            finish();							// Closing rating screen
        }
       
	}
	
	public void getRes() throws JSONException {
		String url = baseURL + "f=%7B'business_id':1,'full_address':1,'name':1%7D&" + myAPIKEY;
		RestClient rc = new RestClient();
		String reslist = rc.connect(url);
		jsonarray = new JSONArray(reslist);
		shuffle();
		
		for (int i = 0; i < 10; i++) {
			json = jsonarray.getJSONObject(i);
			bidList.add(json.getString("business_id"));
			nameList.add(json.getString("name"));
			addressList.add(json.getString("full_address"));
		}
	}
	
	private void shuffle() throws JSONException {
		int j;
		
		JSONObject temp;
		for (int i = jsonarray.length() - 1; i > 0; i--) {
			j = rd.nextInt(i+1);
			temp = jsonarray.getJSONObject(i);
			jsonarray.put(i,jsonarray.getJSONObject(j));
			jsonarray.put(j,temp);
		}
	}
	
}
