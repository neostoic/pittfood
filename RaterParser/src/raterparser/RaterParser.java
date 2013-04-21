/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package raterparser;

import java.io.File;
import java.io.InputStream;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Scanner;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.protocol.HTTP;
import org.json.JSONObject;

/**
 *
 * @author Robo-Laptop
 */
public class RaterParser {
    private static final String UID = "user_id";
    private static final String RID = "business_id";
    private static final String STAR = "stars";
    private static final String URL_REST = "https://api.mongolab.com/api/1/databases/yelptest/collections/newrestaurant";
    private static final String URL_RAT = "https://api.mongolab.com/api/1/databases/yelptest/collections/newrating?";
    private static final String RAT = "rating";
    private static final String KEY = "uUA22oxSPz3xkYkVkYY8ju3hYPMDugfK";
    
    private static ArrayList<String> rests;
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
        try{
            Scanner scan = new Scanner(new File("yelp_academic_dataset 2.json"));
            String line;
            int cnt = 0;
            
            getRests();
            
            while(scan.hasNextLine()){
                line = scan.nextLine();
                System.out.println("Line: " + (++cnt));
                if(line.contains(UID) && line.contains(RID) && line.contains(STAR)){
                    parseNLoadLine(line);
                }
            }
        }catch (Exception e){
            System.err.println(e.getMessage());
        }
    }

    private static void parseNLoadLine(String line) {
       String uid, rid, rating;
       int open, close;
       
       uid = getData(UID,line);
       rid = getData(RID,line);
       open = line.indexOf(STAR);
       open = line.indexOf(":",open);
       close = line.indexOf(",",open);
       rating = line.substring(open+1,close).trim();
       
       if(rests.contains(rid)){
           System.out.println("posted");
           upload(uid,rid,rating);
       }
    }

    private static void upload(String uid, String rid, String rating) {
        try {
            String url;
            HttpClient httpclient = new DefaultHttpClient();
            HttpConnectionParams.setConnectionTimeout(httpclient.getParams(), 10000); //Timeout Limit
            HttpPost post;
            StringEntity se;
            
            // upload prediction t for user i and restaurant j
            httpclient = new DefaultHttpClient();
            JSONObject json = new JSONObject();
            url = URL_RAT + "apiKey=" + KEY;

            post = new HttpPost(url);
            json.put(UID, uid);
            json.put(RID, rid);
            json.put(RAT, Double.parseDouble(rating));
            se = new StringEntity(json.toString());
            se.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
            post.setEntity(se);
            HttpResponse response = httpclient.execute(post);
            
            /*Checking response */
            if (response != null) {
                InputStream in = response.getEntity().getContent(); //Get the data in the entity
            }
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }

    private static String getData(String dataType, String line) {
        String result;
        int open, close;
        
        open = line.indexOf(dataType);
        open = line.indexOf(":",open);
        open = line.indexOf("\"",open);
        close = line.indexOf("\"",open+1);
        result = line.substring(open+1,close);
        
        return result;
    }

    private static void getRests() {
        rests = new ArrayList<String>();
        // get all restaurant data from DB
        try {
            JSONObject select = new JSONObject();
            InputStream isr;
            Scanner scan;
            String line, url;
            HttpClient httpclient = new DefaultHttpClient();

            select.put(RID, 1);
            select.put("_id", 0);

            url = URL_REST + "?f=" + URLEncoder.encode(select.toString(), "ISO-8859-1") + "&apiKey=" + KEY;
            HttpGet httpget = new HttpGet(url);
            HttpResponse response = httpclient.execute(httpget);
            HttpEntity entity = response.getEntity();
            isr = entity.getContent();

            scan = new Scanner(isr);
            while (scan.hasNextLine()) {
                line = scan.nextLine();
                if (line.contains(RID)) {
                    getRestData(line);
                }
            }
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }
    
    private static void getRestData(String line) {
        int open, closed;
        String rid;

        while (line.contains(RID)) {
            open = line.indexOf(RID);
            open = line.indexOf(":", open);
            open = line.indexOf("\"", open);
            closed = line.indexOf("\"", open + 1);
            rid = line.substring(open + 1, closed);
            line = line.substring(closed);

            rests.add(rid);
        }
    }
}
