package com.example.yelpAnalysis;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URLEncoder;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

public class ConnMongoLab {
	// Retrieve data from database
		public String getData(String bid) throws JSONException{
			String KEY = "uUA22oxSPz3xkYkVkYY8ju3hYPMDugfK";
			JSONObject match = new JSONObject();
			JSONObject select = new JSONObject();
			match.put("business_id", bid);
			select.put("review_count", 1);
			select.put("stars", 1);
			select.put("name", 1);
			select.put("_id", 0);
	    	String result = "";
	    	String url = "";
	    	InputStream isr = null;
	    	try{
	            HttpClient httpclient = new DefaultHttpClient();
	            url = "https://api.mongolab.com/api/1/databases/yelptest/collections/yelpInfo?q="
	            		+URLEncoder.encode(match.toString(),"ISO-8859-1")+"&f="
	            		+URLEncoder.encode(select.toString(),"ISO-8859-1")+"&apiKey="+KEY;
	            HttpGet httpget = new HttpGet(url);
	            HttpResponse response = httpclient.execute(httpget);
	            HttpEntity entity = response.getEntity();
	            isr = entity.getContent();
	    	}
	    	catch(Exception e){
	            Log.e("log_tag", "Error in http connection "+e.toString());
	            result = "Error";
	    	}

	    	//convert response to string
	    	try{
	    		BufferedReader reader = new BufferedReader(new InputStreamReader(isr,"iso-8859-1"),8);
	    	    StringBuilder sb = new StringBuilder();
	    	    String line = null;
	    	    while ((line = reader.readLine()) != null) {
	    	    	sb.append(line + "\n");
	    	    }
	    	    isr.close();
	    	    result=sb.toString();
	    	}
	    	catch(Exception e){
	    		Log.e("log_tag", "Error  converting result "+e.toString());
	    		result = "Error";
	        }
	    	return result;
		}
}
