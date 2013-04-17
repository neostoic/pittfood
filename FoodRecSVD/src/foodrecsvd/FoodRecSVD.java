/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package foodrecsvd;

import Jama.*;
import java.io.*;
import java.net.*;
import java.util.*;
import org.json.*;
import java.net.URLEncoder;

import org.apache.http.*;
import org.apache.http.client.*;
import org.apache.http.client.methods.*;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.*;
import org.apache.http.message.BasicHeader;

/**
 *
 * @author Robo-Laptop
 */
public class FoodRecSVD {
    // constants

    private final String KFILE = "../k.txt";
    private final String URL_REST = "https://api.mongolab.com/api/1/databases/yelptest/collections/<reatuarant_table>?f=";
    private final String URL_RATE = "https://api.mongolab.com/api/1/databases/yelptest/collections/<ratings_table>?f=";
    private final String URL_PRED = "https://api.mongolab.com/api/1/databases/yelptest/collections/<predict_table>?f=";
    private final String KEY = "uUA22oxSPz3xkYkVkYY8ju3hYPMDugfK";
    private final int MAX_SCORE = 5;
    private final int MIN_SCORE = 1;
    // global variables
    private Matrix ratingsMat, predMat;
    private double[] movieAvgRate;
    private ArrayList<String> restID, userID;
    private Map<String, Integer> restIndex, userIndex;

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
        new FoodRecSVD();
    }

    // DONT TOUCH
    private FoodRecSVD() {
        init();
        LoadMatrix();
        FillBlanks();
        CalcPred();
    }

    // DONT TOUCH
    private void init() {
        restID = new ArrayList<String>();
        userID = new ArrayList<String>();

        restIndex = new TreeMap();
        userIndex = new TreeMap();
    }

    private void LoadMatrix() {
        fillRestData();
        fillUserData();

        ratingsMat = new Matrix(userID.size(),restID.size());
        fillRatings();
        predMat = new Matrix(userID.size(), restID.size());
    }

    // DONT TOUCH
    private void FillBlanks() {
        movieAvgRate = new double[ratingsMat.getColumnDimension()];
        double movieSumRate;
        int movieRateCount;

        // get avg rating for each movie
        for (int i = 0; i < ratingsMat.getColumnDimension(); i++) {
            movieSumRate = 0;
            movieRateCount = 0;

            for (int j = 0; j < ratingsMat.getRowDimension(); j++) {
                if (ratingsMat.get(j, i) != -1) {
                    movieSumRate += ratingsMat.get(j, i);
                    movieRateCount++;
                }
                movieAvgRate[i] = movieSumRate / movieRateCount;
            }
        }

        // fill in all blanks with avg rating for each movie as a start position
        for (int i = 0; i < ratingsMat.getColumnDimension(); i++) {
            for (int j = 0; j < ratingsMat.getRowDimension(); j++) {
                if (ratingsMat.get(j, i) == -1) {
                    ratingsMat.set(j, i, movieAvgRate[i]);
                    predMat.set(j, i, 0); // prediction needed
                } else {
                    predMat.set(j, i, -1); // prediction not needed
                }
            }
        }
    }

    // DONT TOUCH
    private void CalcPred() {
        SingularValueDecomposition svd = ratingsMat.svd();
        Matrix temp, temp2;
        int k;
        double t;

        try {
            Scanner scan = new Scanner(new File(KFILE));
            k = scan.nextInt();
        } catch (Exception e) {
            System.err.println(e.getMessage() + ". Continuing with svd rank.");
            k = svd.rank() - 1;
        }

        Matrix Uk = svd.getU().getMatrix(0, ratingsMat.getRowDimension() - 1, 0, k);
        Matrix Sk = svd.getS().getMatrix(0, k, 0, k);
        Matrix VkT = svd.getV().getMatrix(0, ratingsMat.getColumnDimension() - 1, 0, k).transpose();

        // sqrt the S_k matrix for computation
        for (int i = 0; i < k; i++) {
            for (int j = 0; j < k; j++) {
                Sk.set(i, j, Math.sqrt(Sk.get(i, j)));
            }
        }

        temp = Uk.times(Sk);
        temp2 = Sk.times(VkT);
        temp = temp.times(temp2);

        // fill only zeros in the predMat and upload prediction
        for (int i = 0; i < predMat.getRowDimension(); i++) {
            for (int j = 0; j < predMat.getColumnDimension(); j++) {
                if (predMat.get(i, j) == 0) {
                    t = temp.get(i, j);
                    if (t > MAX_SCORE) {
                        t = MAX_SCORE;
                    } else if (t < MIN_SCORE) {
                        t = MIN_SCORE;
                    }
                    predMat.set(i, j, t);
                    upload(i, j, t);
                }
            }
        }
    }

    private void fillRestData() {
        // get all restaurant data from DB
        try {
            JSONObject select = new JSONObject();
            String url;
            InputStream isr;
            Scanner scan;
            String line;
            HttpClient httpclient = new DefaultHttpClient();
            
            select.put("business_id", 1);
            select.put("_id", 0);
            
            url = URL_REST+URLEncoder.encode(select.toString(),"ISO-8859-1")+"&apiKey="+KEY;
            HttpGet httpget = new HttpGet(url);
	    HttpResponse response = httpclient.execute(httpget);
	    HttpEntity entity = response.getEntity();
	    isr = entity.getContent();

            scan = new Scanner(isr);
            while((line = scan.nextLine()) != null){
                // parse line
                restIndex.put(line,restID.size());
                restID.add(line);
            }
        } catch (Exception e) {
            System.err.append(e.getMessage());
        }
    }

    private void fillUserData() {
        // get all user data from DB
        try {
            JSONObject select = new JSONObject();
            String url;
            InputStream isr;
            Scanner scan;
            String line;
            HttpClient httpclient = new DefaultHttpClient();
            
            select.put("user_id", 1);
            select.put("_id", 0);
            
            url = URL_RATE+URLEncoder.encode(select.toString(),"ISO-8859-1")+"&apiKey="+KEY;
            HttpGet httpget = new HttpGet(url);
	    HttpResponse response = httpclient.execute(httpget);
	    HttpEntity entity = response.getEntity();
	    isr = entity.getContent();

            scan = new Scanner(isr);
            while((line = scan.nextLine()) != null){
                // parse data
                if(!userID.contains(line)){
                    userIndex.put(line,userID.size());
                    userID.add(line);
                }
            }
        } catch (Exception e) {
            System.err.append(e.getMessage());
        }
    }

    private void fillRatings() {
        // start with empty ratings matrix
        for(int i = 0; i < ratingsMat.getRowDimension(); i++){
            for(int j = 0; j < ratingsMat.getColumnDimension(); j++){
                ratingsMat.set(i,j,-1);
            }
        }
        
        // get all ratings data from DB
        try {
            JSONObject select = new JSONObject();
            String url;
            InputStream isr;
            Scanner scan;
            String line;
            HttpClient httpclient = new DefaultHttpClient();
            
            select.put("user_id", 1);
            select.put("business_id", 1);
            select.put("rating", 1);
            select.put("_id", 0);
            
            url = URL_RATE+URLEncoder.encode(select.toString(),"ISO-8859-1")+"&apiKey="+KEY;
            HttpGet httpget = new HttpGet(url);
	    HttpResponse response = httpclient.execute(httpget);
	    HttpEntity entity = response.getEntity();
	    isr = entity.getContent();

            scan = new Scanner(isr);
            while((line = scan.nextLine()) != null){
                // parse line
                // get user_id, business_id and user tree to put rating in matrix
                
            }
        } catch (Exception e) {
            System.err.append(e.getMessage());
        }
    }
    
    // DONT TOUCH
    private void upload(int i, int j, double t) {
        // delete previous prediction
        
        
        // upload prediction t for user i and restaurant j
        HttpClient client = new DefaultHttpClient();
        HttpResponse response;
                JSONObject json = new JSONObject();
                String registerURL = URL_PRED + "&apiKey=" + KEY;

                try {
                    HttpPost post = new HttpPost(registerURL);
                    json.put("userid", userID.get(i));
                    json.put("restid", restID.get(j));
                    json.put("prediction", t);
                    StringEntity se = new StringEntity( json.toString());  
                    se.setContentType(new BasicHeader("Content-Type", "application/json"));
                    post.setEntity(se);
                    response = client.execute(post);

                } catch(Exception e) {
                    e.printStackTrace();
                }
    }
}