package com.example.yelpAnalysis;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;

import org.json.JSONException;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class ListAdapter extends ArrayAdapter<String> {
	// passed in parameters
	private ArrayList<String> items;
	private ArrayList<String> category;
	private ArrayList<String> star;
	private ArrayList<String> name;
	private ArrayList<String> pic;

	// pre-fetch the data
	public ListAdapter(Context context, int resource, ArrayList<String> ratingList, ArrayList<String> categoryList, 
			ArrayList<String> starList, ArrayList<String> nameList, ArrayList<String> picList) throws JSONException {
		super(context, resource, ratingList);
		this.items = ratingList;
		this.category = categoryList;
		this.star = starList;
		this.name = nameList;
		this.pic = picList;
	}
	
	public View getView(int position, View convertView, ViewGroup parent) { 
	    String bid = items.get(position);
	    String picURL = "";
		View v = convertView;
		
	    if (v == null) {
	        LayoutInflater vi;
	        vi = LayoutInflater.from(getContext());
	        v = vi.inflate(R.layout.listtab_content, null);
	    }
	    
	    if (bid != null) {
	    	TextView titleView = (TextView)v.findViewById(R.id.title);
	    	ImageView iv = (ImageView)v.findViewById(R.id.image);
	    	TextView categoryView = (TextView)v.findViewById(R.id.intro);
	    	TextView starView = (TextView)v.findViewById(R.id.intro2);
	    	
	    	if(titleView!=null){
	    		titleView.setText(name.get(position));
	    	}
	    	if(iv!=null){
	    		picURL = pic.get(position);
	    		try {
					Bitmap bmp = BitmapFactory.decodeStream(new java.net.URL(picURL).openStream());
					iv.setImageBitmap(bmp);
				} catch (MalformedURLException e) {
					
				} catch (IOException e) {
					
				}
	    	}
	    	if(categoryView!=null){
	    		categoryView.setText(category.get(position));
	    	}
	    	if(starView!=null){
	    		starView.setText(star.get(position));
	    	}
	    }
	    return v;
	}
}
