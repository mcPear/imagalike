import org.apache.commons.math3.exception.DimensionMismatchException;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class Ransac {

    private static RealMatrix matrixArgs = MatrixUtils.createRealMatrix(6, 6);
    private static RealMatrix matrixResults = MatrixUtils.createRealMatrix(6, 1);

    public static List<InterestPointsPair> getBestModelFittedPairs(List<InterestPointsPair> interestPointsPairs, int iterationsCount, int maxError) {//int sampleStrength
        int sampleStrength = 3;
        RealMatrix bestModel = null;
        int bestScore = 0;
        RealMatrix model;
        List<InterestPointsPair> sample;
        List<InterestPointsPair> modelFittedPairs = new ArrayList<InterestPointsPair>();
        List<InterestPointsPair> bestModelFittedPairs = null;
        for (int i = 0; i < iterationsCount; i++) {
            sample = getAffineSample(interestPointsPairs);
            model = getAffineModel(sample);
            modelFittedPairs.clear();
            int score = 0;
            double error;
            for (InterestPointsPair currentPair : interestPointsPairs) {
                error = getError(model, currentPair);
                System.out.println("Error: " + error);
                if (error < maxError) {
                    score++;
                    modelFittedPairs.add(currentPair);
                }
            }
            if (score > bestScore) {
                bestScore = score;
                bestModel = model;
                bestModelFittedPairs = new ArrayList<InterestPointsPair>(modelFittedPairs);
            }
            System.out.println("End of iteration: " + i);
        }
        return bestModelFittedPairs;
    }

    public static RealMatrix getAffineModel(List<InterestPointsPair> sample) { //sampleStrength is specified for each transform ?
        RealMatrix matrixArgsXY = getArgumentsXYMatrix(sample);
        RealMatrix matrixArgsUV = getArgumentsUVMatrix(sample);
        System.out.println(matrixArgsXY);
        double[] affineVector = MatrixUtils.inverse(matrixArgsXY).multiply(matrixArgsUV).getColumn(0);
        RealMatrix model = MatrixUtils.createRealMatrix(3, 3);
        double[] row1 = {affineVector[0], affineVector[1], affineVector[2]};
        double[] row2 = {affineVector[3], affineVector[4], affineVector[5]};
        double[] row3 = {0, 0, 1};
        model.setRow(0, row1);
        model.setRow(1, row2);
        model.setRow(2, row3);
        return model;
    }

    private static RealMatrix getArgumentsUVMatrix(List<InterestPointsPair> sample) {
        RealMatrix matrixArgsUV = MatrixUtils.createRealMatrix(6, 1);
        double[] matrixResultsArray = {sample.get(0).interestPoint2.x, sample.get(1).interestPoint2.x, sample.get(2).interestPoint2.x,
                sample.get(0).interestPoint2.y, sample.get(1).interestPoint2.y, sample.get(2).interestPoint2.y};
        matrixArgsUV.setColumn(0, matrixResultsArray);
        return matrixArgsUV;
    }

    private static RealMatrix getArgumentsXYMatrix(List<InterestPointsPair> sample) {
        RealMatrix matrixArgsXY = MatrixUtils.createRealMatrix(6, 6);
        double[] matrixArgsColumn1 = {sample.get(0).interestPoint1.x, sample.get(1).interestPoint1.x, sample.get(2).interestPoint1.x,
                0, 0, 0};
        double[] matrixArgsColumn2 = {sample.get(0).interestPoint1.y, sample.get(1).interestPoint1.y, sample.get(2).interestPoint1.y,
                0, 0, 0};
        double[] matrixArgsColumn3 = {1, 1, 1, 0, 0, 0};
        double[] matrixArgsColumn4 = {0, 0, 0,
                sample.get(0).interestPoint1.x, sample.get(1).interestPoint1.x, sample.get(2).interestPoint1.x};
        double[] matrixArgsColumn5 = {0, 0, 0,
                sample.get(0).interestPoint1.y, sample.get(1).interestPoint1.y, sample.get(2).interestPoint1.y};
        double[] matrixArgsColumn6 = {0, 0, 0, 1, 1, 1};
        matrixArgsXY.setColumn(0, matrixArgsColumn1);
        matrixArgsXY.setColumn(1, matrixArgsColumn2);
        matrixArgsXY.setColumn(2, matrixArgsColumn3);
        matrixArgsXY.setColumn(3, matrixArgsColumn4);
        matrixArgsXY.setColumn(4, matrixArgsColumn5);
        matrixArgsXY.setColumn(5, matrixArgsColumn6);
        return matrixArgsXY;
    }


    private static List<InterestPointsPair> getAffineSample(List<InterestPointsPair> allPairs) {
        int sampleStrength = 3;
        Collections.shuffle(allPairs, new Random(System.currentTimeMillis()));
        return allPairs.subList(0, sampleStrength);
    }

    private static double getError(RealMatrix model, InterestPointsPair pair) {
        double[] argsXYArr = {pair.interestPoint1.x, pair.interestPoint1.y, 1};
        RealMatrix argsXY = MatrixUtils.createColumnRealMatrix(argsXYArr);
        double[] result = model.multiply(argsXY).getColumn(0);
        return getDistance(result[0], result[1], pair);

//        /////
//        long start = System.nanoTime();
//        double error = 0;
//        setArgumentsMatrix(pair1, pair2, pair3);
//        System.out.println("sAM: " + (System.nanoTime() - start));
//        start = System.nanoTime();
//        multMatrixResultsBy(model);
//        System.out.println("sMR: " + (System.nanoTime() - start));
//        start = System.nanoTime();
//        double[] resultsColumn = matrixResults.getColumn(0);
//        Pair<Double, Double> resultPoint1 = new Pair<Double, Double>(resultsColumn[0], resultsColumn[3]);
//        Pair<Double, Double> resultPoint2 = new Pair<Double, Double>(resultsColumn[1], resultsColumn[4]);
//        Pair<Double, Double> resultPoint3 = new Pair<Double, Double>(resultsColumn[2], resultsColumn[5]);
//        error += getDistance(resultPoint1, pair1);
//        error += getDistance(resultPoint2, pair2);
//        error += getDistance(resultPoint3, pair3);
//        System.out.println("gE: " + (System.nanoTime() - start));
//        return error;
    }

    private static double getDistance(double u, double v, InterestPointsPair pair) {
        return Math.sqrt(Math.pow(u - pair.interestPoint2.x, 2) + Math.pow(v - pair.interestPoint2.y, 2));
    }

    private static void multMatrixResultsBy(RealMatrix model) throws DimensionMismatchException {
        MatrixUtils.checkMultiplicationCompatible(matrixArgs, model);
        int nRows = matrixArgs.getRowDimension();
        int nCols = model.getColumnDimension();
        int nSum = matrixArgs.getColumnDimension();

        for (int row = 0; row < nRows; ++row) {
            for (int col = 0; col < nCols; ++col) {
                double sum = 0.0D;

                for (int i = 0; i < nSum; ++i) {
                    sum += matrixArgs.getEntry(row, i) * model.getEntry(i, col);
                }

                matrixResults.setEntry(row, col, sum);
            }
        }

    }

}
