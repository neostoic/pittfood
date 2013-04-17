/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package foodrecsvdtrain;

import Jama.*;
import java.io.*;
import java.net.*;
import java.util.*;
import org.json.*;

/**
 *
 * @author Robo-Laptop
 */
public class FoodRecSVDTrain {
    // constants

    private final String KFILE = "../k.txt";
    private final double DIFF = 0.0;
    private final int MAX_SCORE = 5;
    private final int MIN_SCORE = 1;
    private final int STARTING_K = 0;
    // global variables
    private Matrix ratingsMat, predMat;
    private double[] movieAvgRate;
    private Map<Integer, Integer> restIndex, userIndex;
    private int k;

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
        new FoodRecSVDTrain();
    }

    // DONT TOUCH
    private FoodRecSVDTrain() {
        double lastMSE = Double.MAX_VALUE, currMSE = Double.MAX_VALUE;
        init();
        LoadMatrix();
        FillBlanks();
        
        // use MSE to smallest k to give good results
        do {
            k++;
            lastMSE = currMSE;
            currMSE = CalcPred();
        } while(lastMSE > currMSE+DIFF);

        uploadK();
    }

    // DONT TOUCH
    private void init() {
        restIndex = new TreeMap();
        userIndex = new TreeMap();

        k = STARTING_K;
    }

    private void LoadMatrix() {
        fillRestData();

        double[][] ratings = {{5, 5, 3, -1, 5, 5}, {5, -1, 4, -1, 4, 4}, {-1, 3, -1, 5, 4, 5}, {5, 4, 3, 3, 5, 5}, {5, 5, -1, -1, -1, 5}};
        ratingsMat = new Matrix(ratings);
        predMat = new Matrix(ratingsMat.getRowDimension(), ratingsMat.getColumnDimension());
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
    private double CalcPred() {
        SingularValueDecomposition svd = ratingsMat.svd();
        Matrix temp, temp2;
        double t, ss = 0;
        int count = 0;

        if (k < svd.rank()-1) {
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
            ss = Double.MAX_VALUE;
            count++;
        }
        return (ss / count);
    }

    private void fillRestData() {
        // get all restaurant data from DB
    }

    // DONT TOUCH
    private void uploadK() {
        try {
            PrintWriter out = new PrintWriter(new File(KFILE));
            out.println(k);
            out.close();
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }
}