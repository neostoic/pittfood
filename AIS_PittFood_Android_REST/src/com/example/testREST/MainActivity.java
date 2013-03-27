package com.example.testREST;

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

import com.example.testphp.R;

import android.os.Bundle;
import android.os.StrictMode;
import android.app.Activity;
import android.util.Log;
import android.view.Menu;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
	TextView resultView;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		StrictMode.enableDefaults(); //STRICT MODE ENABLED
		resultView = (TextView) findViewById(R.id.result);
		try {
			getData();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void getData() throws JSONException{
		String KEY = "uUA22oxSPz3xkYkVkYY8ju3hYPMDugfK";
		JSONObject match = new JSONObject();
		JSONObject select = new JSONObject();
		match.put("business_id", "P8nY22PirIp-d1GpDn7qnA");
		select.put("name", 1);
		select.put("stars", 1);
		select.put("categories", 1);
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
            resultView.setText("Couldnt connect to MongoDB");
            Toast.makeText(getApplicationContext(), "Please check your connection", Toast.LENGTH_LONG).show();
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
            resultView.setText(result);
    }
    catch(Exception e){
            Log.e("log_tag", "Error  converting result "+e.toString());
    }
    }

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

}
