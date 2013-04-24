package com.example.yelpAnalysis;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

public class LoadingActivity extends Activity {

	static ArrayList<String> ratingList = new ArrayList<String>();
	
	//Introduce an delay
    private final int WAIT_TIME = 6000;
	
	@SuppressWarnings("unchecked")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// cancel action bar
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.activity_splash);
		
		setContentView(R.layout.activity_loading);
		findViewById(R.id.mainSpinner).setVisibility(View.VISIBLE);
	
        // get data from intent
     	Bundle bundle = this.getIntent().getExtras();
     	ratingList= (ArrayList<String>) bundle.getSerializable("ratingList");
		
		new Handler().postDelayed(new Runnable(){
			@Override
			public void run() {
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				Intent intent = new Intent(LoadingActivity.this,TabActivity.class);
				intent.putExtra("ratingList", ratingList);
				LoadingActivity.this.startActivity(intent);
				LoadingActivity.this.finish();
			}
		}, WAIT_TIME);
        
        // change background color
		View view = this.getWindow().getDecorView();
	    view.setBackgroundColor(Color.parseColor("#002649"));
	 
	}

	

}
