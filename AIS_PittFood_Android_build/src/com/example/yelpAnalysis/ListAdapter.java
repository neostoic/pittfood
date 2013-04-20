package com.example.yelpAnalysis;

import java.util.ArrayList;

import org.json.JSONException;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class ListAdapter extends ArrayAdapter<String>{
	// ready to retrieve data from database
	ConnMongoLab conn = new ConnMongoLab();
	
	public ListAdapter(Context context, int resource) {
	    super(context, resource);
	}
	
	// passed in parameters
	private ArrayList<String> items;
	
	public ListAdapter(Context context, int resource, ArrayList<String> items) {
		super(context, resource, items);
		this.items=items;
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) { 
	    String bid = items.get(position);
		// pre-fetch the data
	    String categoryData ="";
	    String starData = "";
	    String nameData = "";
		try {
			categoryData = conn.getCategory(conn, bid);
			starData = conn.getStar(conn, bid);
			nameData = conn.getName(conn, bid);
		} catch (JSONException e) {
			categoryData = starData = nameData = "error";
		}

		View v = convertView;
		
	    if (v == null) {
	        LayoutInflater vi;
	        vi = LayoutInflater.from(getContext());
	        v = vi.inflate(R.layout.listtab_content, null);
	    }
	    
	    if (bid != null) {
	    	TextView title = (TextView)v.findViewById(R.id.title);
	    	ImageView iv = (ImageView)v.findViewById(R.id.image);
	    	TextView category = (TextView)v.findViewById(R.id.intro);
	    	TextView star = (TextView)v.findViewById(R.id.intro2);
	    	
	    	if(title!=null){
	    		title.setText(nameData);
	    	}
	    	if(iv!=null){
	    		switch(position){
	    			case 0:
	    				iv.setImageResource(R.drawable.qsl);
	    				break;
	    			case 1:
	    				iv.setImageResource(R.drawable.gpb);
	    				break;
	    			case 2:
	    				iv.setImageResource(R.drawable.pb);
	    				break;
	    			case 3:
	    				iv.setImageResource(R.drawable.fgb);
	    				break;
	    		} 
	    	}
	    	if(category!=null){
	    		category.setText(categoryData);
	    	}
	    	if(star!=null){
	    		star.setText(starData);
	    	}
	    }
	    return v;
	}
}
