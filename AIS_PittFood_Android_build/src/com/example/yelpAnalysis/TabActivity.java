package com.example.yelpAnalysis;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONException;

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.FragmentTransaction;
import android.content.Intent;
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

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class TabActivity extends FragmentActivity implements ActionBar.TabListener {
	
	// test for passed-in array list
	static ArrayList<String> ratingList = new ArrayList<String>();
	static ArrayList<String> categoryList = new ArrayList<String>();
	static ArrayList<String> starList = new ArrayList<String>();
	static ArrayList<String> nameList = new ArrayList<String>();
	static ArrayList<String> picList = new ArrayList<String>();
    
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

    @SuppressWarnings("unchecked")
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState); 
        setContentView(R.layout.activity_main); 
        StrictMode.enableDefaults();
        
        getActionBar().setDisplayHomeAsUpEnabled(true);
 
        // get data from intent
     	Bundle bundle = this.getIntent().getExtras();
     	ratingList= (ArrayList<String>) bundle.getSerializable("ratingList");
     	
     	try {
			createSeparList();
		} catch (JSONException e) {
			Toast.makeText(getApplicationContext(), "Check Internet connection", Toast.LENGTH_SHORT).show();
		}
     	
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
	
	public static String convertData(String s){
		Pattern pattern = Pattern.compile("stars\\W+\\d\\.?\\d*");
		Matcher matcher = pattern.matcher(s);
		if(matcher.find()){
			return matcher.group(0).replaceAll("\"", "");
		}
		return "Error";
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
	    	Intent intent = new Intent(TabActivity.this, SelectRecActivity.class);
	    	TabActivity.this.startActivity(intent);
	    	TabActivity.this.finish();
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
    	ConnMongoLab conn = new ConnMongoLab();
    	
    	@Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            View mapView = inflater.inflate(R.layout.activity_map, container, false);
            
            map = ((SupportMapFragment) getActivity().getSupportFragmentManager().findFragmentById(R.id.map)).getMap();
            for(int i=0;i<ratingList.size();i++){
            	String bid = ratingList.get(i);	
            	try {
                	map.addMarker(new MarkerOptions().position(new LatLng(conn.getLatitude(conn,bid),conn.getLongitude(conn,bid)))
        					.title(conn.getName(conn,bid))
        					.snippet(conn.getStar(conn,bid))
        					.icon(BitmapDescriptorFactory.fromResource(R.drawable.icon)));
        		} catch (JSONException e) {
        			Log.e("log_tag", "Error in http connection "+e.toString());
                    Toast.makeText(getActivity().getApplicationContext(), "Miss JSON objects", Toast.LENGTH_SHORT).show();
        		}
            }
            // Move the camera instantly with a zoom of 18.
    	    map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(40.441091, -79.957626), 14));
    	    // Zoom in, animating the camera.
    	    map.animateCamera(CameraUpdateFactory.zoomTo(14), 2000, null);
            return mapView;
        }
    }
    
    // list tab
    public static class ListRest extends Fragment {
    	ListView listView;
    	
    	@Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
    		
    		View view = inflater.inflate(R.layout.listtab_view, container, false);
    		
    		listView = (ListView) view.findViewById(R.id.list);	
    		
    		ListAdapter customAdapter;
    		try {
				customAdapter = new ListAdapter(getActivity(), R.layout.listtab_content, ratingList, 
						categoryList, starList, nameList, picList);
				listView.setAdapter(customAdapter);
			} catch (JSONException e) {
				
			}
            return view;
        }
    }
    
    public void createSeparList() throws JSONException{
    	ConnMongoLab conn = new ConnMongoLab();
    	String bidData = "";
    	
    	for(int i=0;i<ratingList.size();i++){
			bidData = ratingList.get(i);
			categoryList.add(conn.getCategory(conn, bidData));
			starList.add(conn.getStar(conn, bidData));
			nameList.add(conn.getName(conn, bidData));
			picList.add(conn.getPic(conn, bidData));
		}
    }
    
}
