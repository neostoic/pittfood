package com.pittfood;

import org.json.JSONException;

import com.pittfood.RegisterActivity;

import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;

@TargetApi(Build.VERSION_CODES.GINGERBREAD)
public class LoginActivity extends Activity {
	
	Button btnLogin;
    Button btnLinkToRegister;
    EditText inputUsername;
    EditText inputPassword;
    TextView loginErrorMsg;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.login);
		
		StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
		
        inputUsername = (EditText) findViewById(R.id.loginUsername);
        inputPassword = (EditText) findViewById(R.id.loginPassword);
        btnLogin = (Button) findViewById(R.id.btnLogin);
        btnLinkToRegister = (Button) findViewById(R.id.btnLinkToRegisterScreen);
        loginErrorMsg = (TextView) findViewById(R.id.login_error);
		
        btnLogin.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
	    		String username = inputUsername.getText().toString();
	    		String password = inputPassword.getText().toString();
	    	    
	    	    UserFunctions userFunction = new UserFunctions();
	    	    
	    	    try {
	    	    	if (userFunction.checkUser(username, password) == 1) {
	    	    		DatabaseHandler db = new DatabaseHandler(getApplicationContext());
						String userid = userFunction.getUserid(username);
						String name = userFunction.getName(username);
						String created_date = userFunction.getCreatedDate(username);
						
						userFunction.logoutUser(getApplicationContext());
						db.addUser(userid, username, name, created_date);
						
						// Launch Dashboard Screen
                        Intent dashboard = new Intent(getApplicationContext(), DashboardActivity.class);

                        // Close all views before launching Dashboard
                        dashboard.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(dashboard);

                        // Close Login Screen
                        finish();
					} 
	    	    	
	    	    	else {      
	    	    		loginErrorMsg.setText("Incorrect username or password.");
	    	    	}
	    	    }
	    	    catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
				}
	    	}
        });      
        
        // Link to Register Screen
        btnLinkToRegister.setOnClickListener(new View.OnClickListener() {
 
            public void onClick(View view) {
                Intent register = new Intent(getApplicationContext(), RegisterActivity.class);
                startActivity(register);
                finish();
            }
        });
	}

	

}
