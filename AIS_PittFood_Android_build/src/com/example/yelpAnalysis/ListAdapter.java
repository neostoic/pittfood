package com.example.yelpAnalysis;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class ListAdapter extends ArrayAdapter<String>{

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
	    View v = convertView;

	    if (v == null) {
	        LayoutInflater vi;
	        vi = LayoutInflater.from(getContext());
	        v = vi.inflate(R.layout.listtab_content, null);
	    }
	    
	    String p = items.get(position);
	    
	    if (p != null) {
	    	TextView title = (TextView)v.findViewById(R.id.title);
	    	ImageView iv = (ImageView)v.findViewById(R.id.image);
	    	TextView review = (TextView)v.findViewById(R.id.intro);
	    	TextView star = (TextView)v.findViewById(R.id.intro2);
	    	
	    	if(title!=null){
	    		title.setText(p.split("\"")[5]);
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
	    	if(review!=null){
	    		review.setText(p.split("\"|,")[1]+p.split("\"|,")[2]);
	    	}
	    	if(star!=null){
	    		star.setText(p.split("\"|,")[9]+p.split("\"|,")[10]);
	    	}
	    }
	    return v;
	}
}
