package com.Yuwei.pittfood;

import org.json.JSONException;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.yelpAnalysis.R;

@TargetApi(Build.VERSION_CODES.GINGERBREAD)
public class RegisterActivity extends Activity {
	
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
		
		StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
		
		inputUsername = (EditText) findViewById(R.id.registerUsername);
        inputPassword = (EditText) findViewById(R.id.registerPassword);
        inputName = (EditText) findViewById(R.id.registerName);
        btnRegister = (Button) findViewById(R.id.btnRegister);
        btnLinkToLogin = (Button) findViewById(R.id.btnLinkToLoginScreen);
        registerErrorMsg = (TextView) findViewById(R.id.register_error);
	
        btnRegister.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
	    		String username = inputUsername.getText().toString();
	    		String password = inputPassword.getText().toString();
	    	    String name = inputName.getText().toString();
	    	    
	    	    UserFunctions userFunction = new UserFunctions();
	    	    
	    	    if (userFunction.userExists(username) == 0 ) {
	    	    	userFunction.registerUser(username, password, name);
	    	    	
	    	    	
	    	    		DatabaseHandler db = new DatabaseHandler(getApplicationContext());
						String userid = null;
						try {
							userid = userFunction.getUserid(username);
						} catch (JSONException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						String created_date = null;
						try {
							created_date = userFunction.getCreatedDate(username);
						} catch (JSONException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						
						userFunction.logoutUser(getApplicationContext());
						db.addUser(userid, username, name, created_date);
						
						// Launch Dashboard Screen
                        Intent rating = new Intent(getApplicationContext(), RatingActivity.class);

                        // Close all views before launching Dashboard
                        rating.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(rating);

                        // Close Login Screen
                        finish();
	                 
	                
	               
	    	    	
	    	    }
	    	    else {      
	    	    	registerErrorMsg.setText("Username already exists.");
	    	    }
        	}
        });
        
        // Link to Login Screen
        btnLinkToLogin.setOnClickListener(new View.OnClickListener() {
 
            public void onClick(View view) {
                Intent login = new Intent(getApplicationContext(),
                        LoginActivity.class);
                startActivity(login);
                // Close Registration View
                finish();
            }
        });
	}
}
