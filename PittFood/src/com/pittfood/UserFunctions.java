package com.pittfood;

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

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

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Looper;
import android.util.Log;

public class UserFunctions {
	
	public static final String DATE_FORMAT_NOW = "yyyy-MM-dd HH:mm:ss";
	
	private static String baseURL = "https://api.mongolab.com/api/1/databases/yelptest/collections/user?";
	private static String myAPIKEY = "apiKey=uUA22oxSPz3xkYkVkYY8ju3hYPMDugfK";
	
	@SuppressLint("SimpleDateFormat")
	public void registerUser(final String username, final String password, final String name) {
		Thread t = new Thread() {

            public void run() {
                Looper.prepare(); //For Preparing Message Pool for the child Thread
                HttpClient client = new DefaultHttpClient();
                HttpConnectionParams.setConnectionTimeout(client.getParams(), 10000); //Timeout Limit
                HttpResponse response;
                JSONObject json = new JSONObject();
                String registerURL = baseURL + myAPIKEY;
	            
	            Calendar cal = Calendar.getInstance();
        		SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT_NOW);
        		String created_date = sdf.format(cal.getTime());

                try {
                	int userid = countUser() + 1;
                    HttpPost post = new HttpPost(registerURL);
                    json.put("userid", userid);
                    json.put("username", username);
                    json.put("password", password);
                    json.put("name", name);
                    json.put("created_date", created_date);
                    StringEntity se = new StringEntity( json.toString());  
                    se.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
                    post.setEntity(se);
                    response = client.execute(post);

                    /*Checking response */
                    if(response!=null){
                        InputStream in = response.getEntity().getContent(); //Get the data in the entity
                    }

                } catch(Exception e) {
                    e.printStackTrace();
                }

                Looper.loop(); //Loop in the message queue
            }
        };

        t.start();
	}
	
	public String getUserid(final String username) throws JSONException {
		String url = baseURL + "q=%7B'username':'" + username + "'%7D&f=%7B'userid':1%7D&" + myAPIKEY;
		RestClient rc = new RestClient();
		String gu = rc.connect(url);
		JSONArray jsonarray = new JSONArray(gu);
		JSONObject json = jsonarray.getJSONObject(0);
		int id = json.getInt("userid");
		String userid = Integer.toString(id);
		return userid;
	}
	
	public String getName(final String username) throws JSONException {
		String url = baseURL + "q=%7B'username':'" + username + "'%7D&f=%7B'name':1%7D&" + myAPIKEY;
		RestClient rc = new RestClient();
		String gn = rc.connect(url);
		JSONArray jsonarray = new JSONArray(gn);
		JSONObject json = jsonarray.getJSONObject(0);
		String name = json.getString("name");
		Log.i("hellohello", name);
		return name;
	}
	
	public String getCreatedDate(final String username) throws JSONException {
		String url = baseURL + "q=%7B'username':'" + username + "'%7D&f=%7B'created_date':1%7D&" + myAPIKEY;
		RestClient rc = new RestClient();
		String gcd = rc.connect(url);
		JSONArray jsonarray = new JSONArray(gcd);
		JSONObject json = jsonarray.getJSONObject(0);
		String created_date = json.getString("created_date");
		return created_date;
	}
	
	public int countUser() {
		String url = baseURL + "&c=true&" + myAPIKEY;
		RestClient rc = new RestClient();
		String cu = rc.connect(url);
		cu = cu.replace("\n", "");
		int count = Integer.parseInt(cu);
		return count;
	}
	
	public int userExists(final String username) {
		String url = baseURL + "q=%7B'username':'" + username + "'%7D&c=true&" + myAPIKEY;
		RestClient rc = new RestClient();
		String ue = rc.connect(url);
		ue = ue.replace("\n", "");
		int exist = Integer.parseInt(ue);
		return exist;
	}
	
	public int checkUser(final String username, final String password) {
		String url = baseURL + "q=%7B'username':'" + username + "','password':'" + password +"'%7D&c=true&" + myAPIKEY;
		RestClient rc = new RestClient();
		String cu = rc.connect(url);
		cu = cu.replace("\n", "");
		int check = Integer.parseInt(cu);
		return check;
	}
	
	public String isUserLoggedIn(Context context){
		String name = null;
        DatabaseHandler db = new DatabaseHandler(context);
        int count = db.getRowCount();
        if(count > 0){
            HashMap <String,String> userDetail = db.getUserDetails();
            name = userDetail.get("name");
        }
        return name;
    }
	
	public boolean logoutUser(Context context){
        DatabaseHandler db = new DatabaseHandler(context);
        db.resetTables();
        return true;
    }
	
}