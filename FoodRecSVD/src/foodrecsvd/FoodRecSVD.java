/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package foodrecsvd;

import Jama.Matrix;
import Jama.SingularValueDecomposition;

/**
 *
 * @author Robo-Laptop
 */
public class FoodRecSVD {
    private final int MAX_RANK = -1;
    
    // global variables
    private Matrix ratingsMat;
    private Matrix predMat;
    private double[] movieAvgRate;
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
        new FoodRecSVD();
    }
    
    private FoodRecSVD(){
        init();
        LoadMatrix();
        
        ratingsMat.print(ratingsMat.getColumnDimension(), 2);
        
        FillBlanks();
        CalcPred();
        
        predMat.print(predMat.getColumnDimension(), 2);
    }
    
    private void init(){
        
    }
    
    private void LoadMatrix(){
        double[][] ratings = {{5,5,3,-1,5,5},{5,-1,4,-1,4,4},{-1,3,-1,5,4,5},{5,4,3,3,5,5},{5,5,-1,-1,-1,5}};
        ratingsMat = new Matrix(ratings);
    }
    
    private void FillBlanks(){
        movieAvgRate = new double[ratingsMat.getColumnDimension()];
        double movieSumRate;
        int movieRateCount;
        
        // get avg rating for each movie
        for(int i = 0; i < ratingsMat.getColumnDimension(); i++){
            movieSumRate = 0;
            movieRateCount = 0;
            
            for(int j = 0; j < ratingsMat.getRowDimension(); j++){
                if(ratingsMat.get(j,i)!=-1){
                    movieSumRate += ratingsMat.get(j,i);
                    movieRateCount++;
                }
                movieAvgRate[i] = movieSumRate/movieRateCount;
            }
        }
        
        // fill in all blanks with avg rating for each movie as a start position
        for(int i = 0; i < ratingsMat.getColumnDimension(); i++){
            for(int j = 0; j < ratingsMat.getRowDimension(); j++){
                if(ratingsMat.get(j,i)==-1){
                    ratingsMat.set(j, i, movieAvgRate[i]);
                }
            }
        }
    }
    
    private void CalcPred(){
        SingularValueDecomposition svd = ratingsMat.svd();
        Matrix temp;
        int k;
        
        // find k based on matrix rank or defined maximum
        if(MAX_RANK > 0 && svd.rank() > MAX_RANK){
            k = MAX_RANK;
        }else{
            k = svd.rank()-1;
        }
        
        Matrix Uk = svd.getU().getMatrix(0,ratingsMat.getRowDimension()-1,0,k);
        Matrix Sk = svd.getS().getMatrix(0,k,0,k);
        Matrix VkT = svd.getV().getMatrix(0,ratingsMat.getColumnDimension()-1,0,k).transpose();
        
        // sqrt the S_k matrix for computation
        for(int i = 0; i < k; i++){
            for(int j = 0; j < k; j++){
                Sk.set(i, j, Math.sqrt(Sk.get(i,j)));
            }
        }
        
        predMat = Uk.times(Sk);
        temp = Sk.times(VkT);
        predMat = predMat.times(temp);
    }
}