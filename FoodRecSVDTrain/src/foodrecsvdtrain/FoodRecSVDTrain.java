package foodrecsvdtrain;

import Jama.Matrix;
import Jama.SingularValueDecomposition;
import java.io.File;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.URLEncoder;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeMap;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;


/**
 *
 * @author Robo-Laptop
 */
public class FoodRecSVDTrain {
    // constants
    private final String KFILE = "../k.txt";
    private final String URL_REST = "https://api.mongolab.com/api/1/databases/yelptest/collections/restaurant";
    private final String URL_RATE = "https://api.mongolab.com/api/1/databases/yelptest/collections/rating";
    private final String KEY = "uUA22oxSPz3xkYkVkYY8ju3hYPMDugfK";
    private final String USERID = "user_id";
    private final String RESTID = "business_id";
    private final String RATING = "rating";
    private final double DIFF = 0.0;
    private final int MAX_SCORE = 5;
    private final int MIN_SCORE = 1;
    private final int STARTING_K = 0;
    
    // global variables
    private Matrix ratingsMat, predMat;
    private double[] restAvgRate;
    private Map<String, Integer> restIndex, userIndex;
    private SingularValueDecomposition svd;

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        new FoodRecSVDTrain();
    }

    private FoodRecSVDTrain() {
        try {
            double bestMSE = Double.MAX_VALUE, currMSE = bestMSE;
            int k = STARTING_K, bestK=0;
            PrintWriter out = new PrintWriter(new File("kPlot.txt"));
            init();
            LoadMatrix();
            FillBlanks();
            svd = ratingsMat.svd();

            // use MSE to smallest k to give good results
            do {
                k++;
                currMSE = CalcPred(k);
                if(currMSE+DIFF < bestMSE){
                    bestMSE = currMSE;
                    bestK = k;
                }
                if(currMSE != Double.MAX_VALUE){
                    out.println(k + " " + currMSE);
                }
            } while (currMSE!= Double.MAX_VALUE);

            // print MSE and upload k for regular prediction program
            out.close();
            System.out.println("MSE: " + bestMSE);
            uploadK(bestK);
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }

    private void init() {
        restIndex = new TreeMap();
        userIndex = new TreeMap();
    }

    // load rating matrix
    private void LoadMatrix() {
        fillRestData();
        fillUserData();

        ratingsMat = new Matrix(userIndex.size(), restIndex.size());
        fillRatings();
        predMat = new Matrix(userIndex.size(), restIndex.size());
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
                    ratingsMat.set(j, i, restAvgRate[i]);
                    predMat.set(j, i, 0); // prediction needed
                } else {
                    // prediction not needed, user for cross validation
                    predMat.set(j, i, -1);
                }
            }
        }
    }

    // calculate prediction and return mse from cross validaton
    private double CalcPred(int k) {
        Matrix temp, temp2;
        double t, ss = 0;
        int count = 0;

        if (k < svd.rank()) {
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

            // sum of square errors from previously rated matrix
            for (int i = 0; i < predMat.getRowDimension(); i++) {
                for (int j = 0; j < predMat.getColumnDimension(); j++) {
                    if (predMat.get(i, j) == -1) {
                        // cross validate
                        t = temp.get(i, j);
                        if (t > MAX_SCORE) {
                            t = MAX_SCORE;
                        } else if (t < MIN_SCORE) {
                            t = MIN_SCORE;
                        }

                        count++;
                        ss += Math.pow(t - ratingsMat.get(i, j), 2);
                    }
                }
            }
        } else {
            // max k
            ss = Double.MAX_VALUE;
            count++;
        }
        return (ss / count);
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

            url = URL_REST + "?f=" + URLEncoder.encode(select.toString(), "ISO-8859-1") + "&apiKey=" + KEY;
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
            select.put("_id", 0);

            url = URL_RATE + "?f=" + URLEncoder.encode(select.toString(), "ISO-8859-1") + "&apiKey=" + KEY;
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

            url = URL_RATE + "?f=" + URLEncoder.encode(select.toString(), "ISO-8859-1") + "&apiKey=" + KEY;
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
    
    // upload the k value to the config file for reg. prediction program
    private void uploadK(int k) {
        try {
            PrintWriter out = new PrintWriter(new File(KFILE));
            out.println(k);
            out.close();
        } catch (Exception e) {
            System.err.println(e.getMessage());
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

            restIndex.put(rid, restIndex.size());
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

            if (!userIndex.containsKey(uid)) {
                userIndex.put(uid, userIndex.size());
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
}