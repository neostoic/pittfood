package foodrecsvd;

import Jama.Matrix;
import Jama.SingularValueDecomposition;
import java.io.File;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeMap;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.json.JSONObject;

/**
 *
 * @author Steven Nunnally part of the PittFood group
 */
public class FoodRecSVD {
    // constants

    private final String KFILE = "../k.txt";
    private final String URL_REST = "https://api.mongolab.com/api/1/databases/yelptest/collections/newrestaurant";
    private final String URL_RATE = "https://api.mongolab.com/api/1/databases/yelptest/collections/newrating";
    private final String URL_PRED = "https://api.mongolab.com/api/1/databases/yelptest/collections/newprediction";
    private final String KEY = "uUA22oxSPz3xkYkVkYY8ju3hYPMDugfK";
    private final String USERID = "user_id";
    private final String RESTID = "business_id";
    private final String RATING = "rating";
    private final String PRED = "prediction";
    private final String OUR_USER = "pf_";
    private final int LIMIT = 10000;
    private final int MAX_SCORE = 5;
    private final int MIN_SCORE = 1;
    // global variables
    private Matrix ratingsMat, predMat;
    private double[] restAvgRate;
    private ArrayList<String> restID, userID;
    private Map<String, Integer> restIndex, userIndex;

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        new FoodRecSVD();
    }

    private FoodRecSVD() {
        init();
        LoadMatrix();
        FillBlanks();
        CalcPred();
    }

    private void init() {
        restID = new ArrayList<String>();
        userID = new ArrayList<String>();

        restIndex = new TreeMap();
        userIndex = new TreeMap();
    }

    private void LoadMatrix() {
        fillRestData();
        fillUserData();

        ratingsMat = new Matrix(userID.size(), restID.size());
        fillRatings();
        predMat = new Matrix(userID.size(), restID.size());
    }

    // fill in blank ratings with restaurant averages rating or middle rating 
    //  if no ratings exist for that restaurant
    private void FillBlanks() {
        restAvgRate = new double[ratingsMat.getColumnDimension()];
        double restSumRate;
        int restRateCount;

        // get avg rating for each restaurant
        for (int i = 0; i < ratingsMat.getColumnDimension(); i++) {
            restSumRate = 0;
            restRateCount = 0;

            for (int j = 0; j < ratingsMat.getRowDimension(); j++) {
                if (ratingsMat.get(j, i) != -1) {
                    restSumRate += ratingsMat.get(j, i);
                    restRateCount++;
                }
            }

            if (restRateCount == 0) {
                // no ratings exist, make a middle of the line average
                restAvgRate[i] = ((MAX_SCORE - MIN_SCORE) / 2) + MIN_SCORE;
            } else {
                restAvgRate[i] = restSumRate / restRateCount;
            }
        }

        // fill in all blanks with avg rating for each movie as a start position
        for (int i = 0; i < ratingsMat.getColumnDimension(); i++) {
            for (int j = 0; j < ratingsMat.getRowDimension(); j++) {
                if (ratingsMat.get(j, i) == -1) {
                    // blank fill in
                    ratingsMat.set(j, i, restAvgRate[i]);
                    predMat.set(j, i, 0); // prediction needed
                } else {
                    predMat.set(j, i, -1); // prediction not needed

                    if (userID.get(j).contains(OUR_USER)) {
                        // delete duplicate in prediction table if it exists
                        //  only would be our users, not yelp users
                        try {
                            delete(j, i);
                        } catch (Exception e) {
                            System.err.println(e.getMessage());
                        }
                    }
                }
            }
        }
    }

    private void CalcPred() {
        SingularValueDecomposition svd = ratingsMat.svd();
        Matrix temp, temp2;
        int k;
        double t;

        try {
            // get trained k value for rank-k SVD
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

        // get predictions using SVD
        temp = Uk.times(Sk);
        temp2 = Sk.times(VkT);
        temp = temp.times(temp2);

        // fill only zeros in the predMat and upload prediction
        for (int i = 0; i < predMat.getRowDimension(); i++) {
            for (int j = 0; j < predMat.getColumnDimension(); j++) {
                if (predMat.get(i, j) == 0) {
                    t = temp.get(i, j);
                    // trim prediction
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

    // get restaurant ids from the restaurant table
    private void fillRestData() {
        // get all restaurant data from DB
        try {
            // access rating table data
            JSONObject select = new JSONObject();
            InputStream isr;
            Scanner scan;
            String line, url;
            HttpClient httpclient = new DefaultHttpClient();

            select.put(RESTID, 1);
            select.put("_id", 0);

            url = URL_REST + "?f=" + URLEncoder.encode(select.toString(), "ISO-8859-1") + "&l=" + LIMIT + "&apiKey=" + KEY;
            HttpGet httpget = new HttpGet(url);
            HttpResponse response = httpclient.execute(httpget);
            HttpEntity entity = response.getEntity();
            isr = entity.getContent();

            scan = new Scanner(isr);
            while (scan.hasNextLine()) {
                line = scan.nextLine();
                if (line.contains(RESTID)) {
                    // extract restaurant ids
                    getRestData(line);
                }
            }
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }

    // get user ids from the rating table
    private void fillUserData() {
        // get all user ids from DB
        try {
            // access rating table data
            JSONObject select = new JSONObject();
            InputStream isr;
            Scanner scan;
            String line, url;
            HttpClient httpclient = new DefaultHttpClient();

            select.put(USERID, 1);
            select.put(RESTID, 1);
            select.put(RATING, 1);
            select.put("_id", 0);

            url = URL_RATE + "?f=" + URLEncoder.encode(select.toString(), "ISO-8859-1") + "&l=" + LIMIT + "&apiKey=" + KEY;
            HttpGet httpget = new HttpGet(url);
            HttpResponse response = httpclient.execute(httpget);
            HttpEntity entity = response.getEntity();
            isr = entity.getContent();

            scan = new Scanner(isr);
            while (scan.hasNextLine()) {
                line = scan.nextLine();
                if (line.contains(USERID)) {
                    // extract user ids
                    getUserData(line);
                }
            }
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }
    
    // fill in ratings matrix
    private void fillRatings() {
        // start with empty ratings matrix
        for (int i = 0; i < ratingsMat.getRowDimension(); i++) {
            for (int j = 0; j < ratingsMat.getColumnDimension(); j++) {
                ratingsMat.set(i, j, -1);
            }
        }

        // get all ratings data from DB
        try {
            JSONObject select = new JSONObject();
            InputStream isr;
            Scanner scan;
            String line, url;
            HttpClient httpclient = new DefaultHttpClient();

            select.put(USERID, 1);
            select.put(RESTID, 1);
            select.put(RATING, 1);
            select.put("_id", 0);

            url = URL_RATE + "?f=" + URLEncoder.encode(select.toString(), "ISO-8859-1") + "&l=" + LIMIT + "&apiKey=" + KEY;
            HttpGet httpget = new HttpGet(url);
            HttpResponse response = httpclient.execute(httpget);
            HttpEntity entity = response.getEntity();
            isr = entity.getContent();

            scan = new Scanner(isr);
            while (scan.hasNextLine()) {
                line = scan.nextLine();
                if (line.contains(USERID) && line.contains(RESTID) && line.contains(RATING)) {
                    // extract rating data and fill matrix
                    getRateData(line);
                }
            }
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }

    // upload rating from matrix at i,j of rating t
    private void upload(int i, int j, double t) {
        if (userID.get(i).contains(OUR_USER)) {
            try {
                // delete previous prediction to avoid duplicates
                delete(i, j);

                // upload prediction t for user i and restaurant j
                String url;
                HttpClient httpclient = new DefaultHttpClient();
                JSONObject json = new JSONObject();
                HttpPost post;
                StringEntity se;
                HttpResponse response;

                httpclient = new DefaultHttpClient();
                url = URL_PRED + "?l=" + LIMIT + "&apiKey=" + KEY;

                post = new HttpPost(url);
                json.put(USERID, userID.get(i));
                json.put(RESTID, restID.get(j));
                json.put(PRED, t);
                se = new StringEntity(json.toString());
                se.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
                post.setEntity(se);
                response = httpclient.execute(post);

                /*Checking response */
                if (response != null) {
                    InputStream in = response.getEntity().getContent(); //Get the data in the entity
                }
            } catch (Exception e) {
                System.err.println(e.getMessage());
            }
        }
    }

    // extract all restaurant ids from the line
    private void getRestData(String line) {
        int open, closed;
        String rid;

        while (line.contains(RESTID)) {
            open = line.indexOf(RESTID);
            open = line.indexOf(":", open);
            open = line.indexOf("\"", open);
            closed = line.indexOf("\"", open + 1);
            rid = line.substring(open + 1, closed);
            line = line.substring(closed);

            restIndex.put(rid, restID.size());
            restID.add(rid);
        }
    }

    // extract all user ids from the line
    private void getUserData(String line) {
        int open, closed;
        String uid;

        while (line.contains(USERID)) {
            open = line.indexOf(USERID);
            open = line.indexOf(":", open);
            open = line.indexOf("\"", open);
            closed = line.indexOf("\"", open + 1);
            uid = line.substring(open + 1, closed);
            line = line.substring(closed);

            if (!userID.contains(uid)) {
                userIndex.put(uid, userID.size());
                userID.add(uid);
            }
        }
    }

    // extract all rating data from the line
    private void getRateData(String line) {
        String uid, rid, rat;
        int open, closed;

        while (line.contains(USERID) && line.contains(RESTID) && line.contains(RATING)) {
            open = line.indexOf(RESTID);
            open = line.indexOf(":", open);
            open = line.indexOf("\"", open);
            closed = line.indexOf("\"", open + 1);
            rid = line.substring(open + 1, closed);
            line = line.substring(closed);

            open = line.indexOf(USERID);
            open = line.indexOf(":", open);
            open = line.indexOf("\"", open);
            closed = line.indexOf("\"", open + 1);
            uid = line.substring(open + 1, closed);
            line = line.substring(closed);

            open = line.indexOf(RATING);
            open = line.indexOf(":", open);
            closed = line.indexOf("}", open);
            rat = line.substring(open + 1, closed).trim();

            ratingsMat.set(userIndex.get(uid), restIndex.get(rid), Double.parseDouble(rat));
        }
    }

    // delete the prediction document with user id and restaurant id given
    private void delete(int ui, int ri) throws Exception {
        // find prediction document id
        String url;
        HttpClient httpclient = new DefaultHttpClient();
        JSONObject match = new JSONObject();
        JSONObject select = new JSONObject();
        String line, id = "";

        match.put(USERID, userID.get(ui));
        match.put(RESTID, restID.get(ri));
        select.put("_id", 1);
        url = URL_PRED + "?q=" + URLEncoder.encode(match.toString(), "ISO-8859-1") + "&f=" + URLEncoder.encode(select.toString(), "ISO-8859-1") + "&l=" + LIMIT + "&apiKey=" + KEY;
        HttpGet httpget = new HttpGet(url);
        HttpResponse response = httpclient.execute(httpget);
        HttpEntity entity = response.getEntity();
        InputStream isr = entity.getContent();

        Scanner scan = new Scanner(isr);
        while (scan.hasNextLine()) {
            line = scan.nextLine();
            if (line.contains("_id")) {
                // get user_id, business_id and user tree to put rating in matrix
                id = getID("$oid", line);
            }
        }

        if (id.length() > 0) {
            // document exists delete it
            url = URL_PRED + "/" + id + "?l=" + LIMIT + "&apiKey=" + KEY;
            URL u = new URL(url);

            HttpURLConnection conn = (HttpURLConnection) u.openConnection();
            conn.setRequestMethod("DELETE");
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type", "application/json");
            scan = new Scanner(conn.getInputStream());

        }
    }

    // get the first id string of the given type from line
    private String getID(String type, String line) {
        int open, closed;
        String result;

        open = line.indexOf(type);
        open = line.indexOf(":", open);
        open = line.indexOf("\"", open);
        closed = line.indexOf("\"", open + 1);
        result = line.substring(open + 1, closed);

        return result;
    }
}