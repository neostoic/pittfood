package com.example.yelpAnalysis;

import java.util.ArrayList;

import android.net.ConnectivityManager;
import android.net.NetworkInfo.State;
import android.os.Bundle;
import android.provider.Settings;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

public class SplashActivity extends Activity {

	private static String TAG = SplashActivity.class.getName();
	private static long SLEEP_TIME = 3;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		int state = 0;
		
        // Check if there's Internet connection
        state = checkNetworkState();
		
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.activity_splash);
		
		View view = this.getWindow().getDecorView();
	    view.setBackgroundColor(Color.parseColor("#002649"));
		
		// Start timer and launch main activity
		IntentLauncher launcher = new IntentLauncher();
		if(state==0){
			launcher.start();
		}
	}

	private class IntentLauncher extends Thread {
		@Override
		public void run(){
			try {
				Thread.sleep(SLEEP_TIME*500);
			} catch (Exception e){
				Log.e(TAG, "HAHAHA: "+e);
			}
			
			// test arraylist
			ArrayList<String> values = new ArrayList<String>();
		 	values.add("tHLJ1pDaaHptb-EbFM2q_A");
			values.add("74wRr6PP6lbaL1YzUcYGAA");
			values.add("caGXS6ubNTlv91ZZyoirjQ");
			values.add("P8nY22PirIp-d1GpDn7qnA");
			values.add("_rKz-rQPzUAIZWlUHCKgow");
			values.add("pCwOCE-cycl6cMEa9O6bZQ");
			values.add("1TUn8LfJBfOdWes5bL_DOA");
			values.add("WjkjrsOzMAQ2wgY9tVyS0Q");
			values.add("XqNDr54eLDLRfZwo4l4dVA");
			values.add("Eg_M8eeu9SDEdbP7iUmVbQ");
			
			Intent intent = new Intent(SplashActivity.this, TabActivity.class);
			intent.putExtra("ratingList", values);
			SplashActivity.this.startActivity(intent);
			SplashActivity.this.finish();
		}
	}

	public int checkNetworkState() {
		ConnectivityManager manager = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
		State mobile = manager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState();
        State wifi = manager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState();
        if(mobile == State.CONNECTED||mobile==State.CONNECTING)
        	return 0;
        if(wifi == State.CONNECTED||wifi==State.CONNECTING)
        	return 0;
        else{
        	showTips();
        	return -1;
        }	
	}
	
	public void showTips() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setIcon(android.R.drawable.ic_dialog_alert)
				.setTitle("No connection")
				.setMessage("Check your mobile connection")
				.setPositiveButton("OK", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which){
						startActivity(new Intent(Settings.ACTION_WIRELESS_SETTINGS));
						SplashActivity.this.finish();
					}
				});
		builder.create();
        builder.show();
	}
	
}
