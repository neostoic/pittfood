package com.Yuwei.pittfood;

import org.json.JSONException;

import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.yelpAnalysis.R;
import com.example.yelpAnalysis.SelectRecActivity;

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
        
        // Set up action bar.
        final ActionBar actionBar = getActionBar();
        actionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#002649")));
        getActionBar().setDisplayHomeAsUpEnabled(true);
		
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
                        Intent dashboard = new Intent(getApplicationContext(), SelectRecActivity.class);

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
	    	LoginActivity.this.finish();
	    	return true;
	    }
	    return false;
	}

}
