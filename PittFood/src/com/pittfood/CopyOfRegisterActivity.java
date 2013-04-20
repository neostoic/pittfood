package com.pittfood;

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.protocol.HTTP;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;


public class CopyOfRegisterActivity extends Activity {
	
	public static final String DATE_FORMAT_NOW = "yyyy-MM-dd HH:mm:ss";
	
	Button btnRegister;
    Button btnLinkToLogin;
    EditText inputUsername;
    EditText inputPassword;
    EditText inputName;
    TextView registerErrorMsg;
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.register);
		
		inputUsername = (EditText) findViewById(R.id.registerUsername);
        inputPassword = (EditText) findViewById(R.id.registerPassword);
        inputName = (EditText) findViewById(R.id.registerName);
        btnRegister = (Button) findViewById(R.id.btnRegister);
        btnLinkToLogin = (Button) findViewById(R.id.btnLinkToLoginScreen);
        registerErrorMsg = (TextView) findViewById(R.id.register_error);
	
        btnRegister.setOnClickListener(new View.OnClickListener() {
        	@SuppressLint("SimpleDateFormat")
			public void onClick(View view) {
        		
        		Thread t = new Thread() {

                    public void run() {
                        Looper.prepare(); //For Preparing Message Pool for the child Thread
                        HttpClient client = new DefaultHttpClient();
                        HttpConnectionParams.setConnectionTimeout(client.getParams(), 10000); //Timeout Limit
                        HttpResponse response;
                        JSONObject json = new JSONObject();
                        String url = "https://api.mongolab.com/api/1/databases/yelptest/collections/user?apiKey=uUA22oxSPz3xkYkVkYY8ju3hYPMDugfK";
                        
                        String username = inputUsername.getText().toString();
        	            String password = inputPassword.getText().toString();
        	            String name = inputName.getText().toString();
        	            
        	            Calendar cal = Calendar.getInstance();
                		SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT_NOW);
                		String created_date = sdf.format(cal.getTime());
                		
                		// check if user already exists
                		

                        try {
                            HttpPost post = new HttpPost(url);
                            json.put("userid", "3");
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
        });          
	}
}
