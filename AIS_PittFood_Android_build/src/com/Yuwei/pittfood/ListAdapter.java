package com.Yuwei.pittfood;

import java.util.ArrayList;

import org.json.JSONException;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.RatingBar;
import android.widget.TextView;

import com.example.yelpAnalysis.R;

public class ListAdapter extends ArrayAdapter<String> {
	
	private ArrayList <String> bidList;
	private ArrayList <String> nameList;
	private ArrayList <String> addressList;
	
	public static float [] ratings;  

	public ListAdapter(Context context, int resource, ArrayList <String> bidList, ArrayList <String> nameList, ArrayList <String> addressList) throws JSONException {
	    super(context, resource, bidList);
	    this.bidList = bidList;
	    this.nameList = nameList;
	    this.addressList = addressList;
	}
	
	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		String bid = bidList.get(position);
		View v = convertView;
		ratings = new float[10];

        LayoutInflater vi;
        vi = LayoutInflater.from(getContext());
        v = vi.inflate(R.layout.reslistview, null);
	
		if (bid != null) {	
		    TextView nameView = (TextView)v.findViewById(R.id.resname);
		    TextView addView = (TextView)v.findViewById(R.id.resadd);
		    RatingBar ratingBar = (RatingBar)v.findViewById(R.id.ratingBar);
		    if (ratings[position] != 0)
		    	ratingBar.setRating(ratings[position]);
    	
			if (nameView != null) {
				nameView.setText(nameList.get(position));
				
				Log.e("name", nameList.get(position));
			}
			
			if (addView != null)
				addView.setText(addressList.get(position));
			
			if (ratingBar != null) {
				ratingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
					public void onRatingChanged(RatingBar ratingBar,float rating, boolean fromUser) {
						if (fromUser) {
							ratings[position] = rating;
							ratingBar.setRating(rating);
						}
                    }              
				});
			}
		}
	    return v;
	}
	
	public static float [] getRating() {
		return ratings;
	}
}
