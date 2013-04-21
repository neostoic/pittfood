package com.pittfood;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class GetRes {
	
	private static String baseURL = "https://api.mongolab.com/api/1/databases/yelptest/collections/restaurant?";
	private static String myAPIKEY = "apiKey=uUA22oxSPz3xkYkVkYY8ju3hYPMDugfK";
	
	public static JSONArray jsonarray = null;
	public static JSONObject json = null;
	
	public void getRes() throws JSONException {
		String url = baseURL + "f=%7B'business_id':1,'full_address':1,'name':1%7D&" + myAPIKEY;
		RestClient rc = new RestClient();
		String reslist = rc.connect(url);
		jsonarray = new JSONArray(reslist);
	
		for (int i = 0; i < jsonarray.length(); i++) {
			json = jsonarray.getJSONObject(i);
			System.out.println(json.getString("business_id"));
			System.out.println(json.getString("name"));
			System.out.println(json.getString("full_address"));
		}
	}
}
