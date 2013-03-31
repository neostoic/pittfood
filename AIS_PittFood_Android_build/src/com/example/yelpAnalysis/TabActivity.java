package com.example.yelpAnalysis;

import java.util.ArrayList;

import org.json.JSONException;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.FragmentTransaction;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

public class TabActivity extends FragmentActivity implements ActionBar.TabListener {

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide fragments representing
     * each object in a collection. We use a {@link android.support.v4.app.FragmentStatePagerAdapter}
     * derivative, which will destroy and re-create fragments as needed, saving and restoring their
     * state in the process. This is important to conserve memory and is a best practice when
     * allowing navigation between objects in a potentially large collection.
     */
	AppSectionsPagerAdapter mAppSectionsPagerAdapter;

    /**
     * The {@link android.support.v4.view.ViewPager} that will display the object collection.
     */
    ViewPager mViewPager;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        StrictMode.enableDefaults();

        // Create an adapter that when requested, will return a fragment representing an object in
        // the collection.
        // ViewPager and its adapters use support library fragments, so we must use
        // getSupportFragmentManager.
        mAppSectionsPagerAdapter = new AppSectionsPagerAdapter(getSupportFragmentManager());

        // Set up action bar.
        final ActionBar actionBar = getActionBar();
        actionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#002649")));

        // Specify that the Home button should show an "Up" caret, indicating that touching the
        // button will take the user one step up in the application's hierarchy.
        actionBar.setDisplayHomeAsUpEnabled(false);
        
        // Specify that we will be displaying tabs in the action bar.
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        // Set up the ViewPager, attaching the adapter and setting up a listener for when the
        // user swipes between sections.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mAppSectionsPagerAdapter);
        mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                actionBar.setSelectedNavigationItem(position);
            }
        });

        for (int i = 0; i < mAppSectionsPagerAdapter.getCount(); i++) {
        	actionBar.addTab(
                    actionBar.newTab()
                            .setText(mAppSectionsPagerAdapter.getPageTitle(i))
                            .setTabListener(this));
        }
    }
    
	@Override
	public void onTabReselected(Tab arg0, FragmentTransaction arg1) {	
	}

	@Override
	public void onTabSelected(Tab arg0, FragmentTransaction arg1) {
		mViewPager.setCurrentItem(arg0.getPosition());
	}

	@Override
	public void onTabUnselected(Tab arg0, FragmentTransaction arg1) {	
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
	    return false;
	}

    public static class AppSectionsPagerAdapter extends FragmentPagerAdapter {

    	public AppSectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int i) {
        	if(i==0){
        		Fragment fragment = new ListRest();
                return fragment;
        	}
        	else{
        		Fragment fragment = new ConnMap();
        		return fragment;
        	}
        }

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            if(position==0){
            	return "Restaurant List";
            }
            else{
            	return "Map";
            }
        }
    }
    
    // map tab
    public static class ConnMap extends Fragment {
    	private GoogleMap map;
    	static final LatLng QUAKER = new LatLng(40.44074158649877, -79.9581527709961);
    	static final LatLng GOLDEN = new LatLng(40.440937557567004, -79.95813131332397);
    	static final LatLng PRIMANTI = new LatLng(40.441629575831264, -79.95686799287796);
    	static final LatLng FIVE = new LatLng(40.44258695808571, -79.95654344558716);
    	
    	@Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            View mapView = inflater.inflate(R.layout.activity_map, container, false);
            
            ConnMongoLab conn = new ConnMongoLab();
            
            map = ((SupportMapFragment) getActivity().getSupportFragmentManager().findFragmentById(R.id.map)).getMap();
            try {
    			map.addMarker(new MarkerOptions().position(QUAKER)
    					.title("Quaker Steak & Lube")
    					.snippet(conn.getData("tHLJ1pDaaHptb-EbFM2q_A").split("\"|,")[6])
    					.icon(BitmapDescriptorFactory.fromResource(R.drawable.icon)));
    			map.addMarker(new MarkerOptions().position(GOLDEN)
    					.title("Golden Palace Buffet")
    					.snippet(conn.getData("74wRr6PP6lbaL1YzUcYGAA").split("\"|,")[6])
    					.icon(BitmapDescriptorFactory.fromResource(R.drawable.icon)));
    	        map.addMarker(new MarkerOptions().position(PRIMANTI)
    			        .title("Primanti Brothers")
    			        .snippet(conn.getData("caGXS6ubNTlv91ZZyoirjQ").split("\"|,")[6])
    			        .icon(BitmapDescriptorFactory.fromResource(R.drawable.icon))
    	        );
    	        map.addMarker(new MarkerOptions().position(FIVE)
    			        .title("Five Guys")
    			        .snippet(conn.getData("P8nY22PirIp-d1GpDn7qnA").split("\"|,")[6])
    			        .icon(BitmapDescriptorFactory.fromResource(R.drawable.icon))
    	        );
    		} catch (JSONException e) {
    			Log.e("log_tag", "Error in http connection "+e.toString());
                Toast.makeText(getActivity().getApplicationContext(), "Miss JSON objects", Toast.LENGTH_SHORT).show();
    		}
            // Move the camera instantly with a zoom of 18.
    	    map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(40.441091, -79.957626), 14));
    	    // Zoom in, animating the camera.
    	    map.animateCamera(CameraUpdateFactory.zoomTo(17), 2000, null);
            return mapView;
        }
    }
    
    // list tab
    public static class ListRest extends Fragment {
    	ListView listView;
    	
    	@Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
    		
    		ConnMongoLab conn = new ConnMongoLab();
    		View view = inflater.inflate(R.layout.listtab_view, container, false);
    		
    		listView = (ListView) view.findViewById(R.id.list);
    		ArrayList<String> values = new ArrayList<String>();
    			try {
					values.add(conn.getData("tHLJ1pDaaHptb-EbFM2q_A"));
					values.add(conn.getData("74wRr6PP6lbaL1YzUcYGAA"));
	    			values.add(conn.getData("caGXS6ubNTlv91ZZyoirjQ"));
	    			values.add(conn.getData("P8nY22PirIp-d1GpDn7qnA")); 
				} catch (JSONException e) {
					Log.e("log_tag", "Error  converting result "+e.toString());
		            Toast.makeText(getActivity(), "Please check your connection", Toast.LENGTH_SHORT).show();
				}			
    		
    		ListAdapter customAdapter = new ListAdapter(getActivity(), R.layout.listtab_content, values);
    		listView.setAdapter(customAdapter);
            return view;
        }
    }

    
}
