package com.example.yelpAnalysis;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ConnMongoLab {
	private static String baseURL = "https://api.mongolab.com/api/1/databases/yelptest/collections/restaurant?";
	private static String myAPIKEY = "apiKey=uUA22oxSPz3xkYkVkYY8ju3hYPMDugfK";
	public static JSONArray jsonArray = null;
	public static JSONObject json = null;
	
	public JSONArray getData(String bid) throws JSONException {
		String url = baseURL + "f=%7B'business_id':1,'full_address':1,'categories':1," +
				"'photo_url':1,'longitude':1,'latitude':1,'name':1,'stars':1,'_id':0%7D&" +
				"q=%7B'business_id':'"+ bid +"'%7D&" + myAPIKEY;
		RestClient rc = new RestClient();
		String reslist = rc.connect(url);
		jsonArray = new JSONArray(reslist);
		
		return jsonArray;
	}
	
	public double getLatitude(ConnMongoLab test, String bid) throws JSONException {
		double value = 0;
		JSONObject json = null;
		JSONArray jsonarray = null;
		jsonarray = test.getData(bid);
		json = jsonarray.getJSONObject(0);
		value = Double.parseDouble(json.getString("latitude"));
		
		return value;
	}
		
	public double getLongitude(ConnMongoLab test, String bid) throws JSONException {
		double value = 0;
		JSONObject json = null;
		JSONArray jsonarray = null;
		jsonarray = test.getData(bid);
		json = jsonarray.getJSONObject(0);
		value = Double.parseDouble(json.getString("longitude"));
		
		return value;
	}
		
	public String getName(ConnMongoLab test, String bid) throws JSONException {
		String name = "";
		JSONObject json = null;
		JSONArray jsonarray = null;
		jsonarray = test.getData(bid);
		json = jsonarray.getJSONObject(0);
		name = json.getString("name");
		
		return name;
	}
		
	public String getStar(ConnMongoLab test, String bid) throws JSONException {
		String star = "";
		JSONObject json = null;
		JSONArray jsonarray = null;
		jsonarray = test.getData(bid);
		json = jsonarray.getJSONObject(0);
		star = "stars: "+json.getString("stars");
		
		return star;
	}
	
	public String getCategory(ConnMongoLab test, String bid) throws JSONException {
		String category = "";
		JSONObject json = null;
		JSONArray jsonarray = null;
		jsonarray = test.getData(bid);
		json = jsonarray.getJSONObject(0);
		category = json.getString("categories");
		
		return category;
	}
}
