import org.apache.commons.math3.exception.DimensionMismatchException;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.SingularMatrixException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class Ransac {

    private static RealMatrix matrixArgs = MatrixUtils.createRealMatrix(6, 6);
    private static RealMatrix matrixResults = MatrixUtils.createRealMatrix(6, 1);

    public static List<InterestPointsPair> getBestModelFittedPairs(List<InterestPointsPair> interestPointsPairs, int iterationsCount, int maxError) {//int sampleStrength
        String bestModelName = null;
        String modelName = null;
        int bestScore = 0;
        RealMatrix model;
        List<InterestPointsPair> sample;
        List<InterestPointsPair> modelFittedPairs = new ArrayList<InterestPointsPair>();
        List<InterestPointsPair> bestModelFittedPairs = null;
        for (int i = 0; i < iterationsCount; i++) {
            model = null;
            while (model == null) {
                try {
                    if (i < iterationsCount / 2) {
                        sample = getAffineSample(interestPointsPairs);
                        model = getAffineModel(sample);
                        modelName = "affine transform";
                    } else {
                        sample = getPerspectiveSample(interestPointsPairs);
                        model = getPerspectiveModel(sample);
                        modelName = "perspective transform";
                    }
                } catch (SingularMatrixException e) {
                    System.out.println("Singular matrix occured.");
                }
            }
            System.out.println(model);
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
                bestModelName = modelName;
                bestModelFittedPairs = new ArrayList<InterestPointsPair>(modelFittedPairs);
            }
            System.out.println("End of iteration: " + i);
        }
        System.out.println("The best model name: " + bestModelName);
        return bestModelFittedPairs;
    }

    public static RealMatrix getAffineModel(List<InterestPointsPair> sample) {
        RealMatrix matrixArgsXY = getAffineArgumentsXYMatrix(sample);
        RealMatrix matrixArgsUV = getAffineArgumentsUVMatrix(sample);
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

    private static RealMatrix getAffineArgumentsUVMatrix(List<InterestPointsPair> sample) {
        RealMatrix matrixArgsUV = MatrixUtils.createRealMatrix(6, 1);
        double[] matrixResultsArray = {sample.get(0).interestPoint2.x, sample.get(1).interestPoint2.x, sample.get(2).interestPoint2.x,
                sample.get(0).interestPoint2.y, sample.get(1).interestPoint2.y, sample.get(2).interestPoint2.y};
        matrixArgsUV.setColumn(0, matrixResultsArray);
        return matrixArgsUV;
    }

    private static RealMatrix getAffineArgumentsXYMatrix(List<InterestPointsPair> sample) {
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

    public static RealMatrix getPerspectiveModel(List<InterestPointsPair> sample) {
        RealMatrix matrixArgsXY = getPerspectiveArgumentsXYMatrix(sample);
        RealMatrix matrixArgsUV = getPerspectiveArgumentsUVMatrix(sample);
        System.out.println(matrixArgsXY);
        double[] affineVector = MatrixUtils.inverse(matrixArgsXY).multiply(matrixArgsUV).getColumn(0);
        RealMatrix model = MatrixUtils.createRealMatrix(3, 3);
        double[] row1 = {affineVector[0], affineVector[1], affineVector[2]};
        double[] row2 = {affineVector[3], affineVector[4], affineVector[5]};
        double[] row3 = {affineVector[6], affineVector[7], 1};
        model.setRow(0, row1);
        model.setRow(1, row2);
        model.setRow(2, row3);
        return model;
    }


    private static List<InterestPointsPair> getAffineSample(List<InterestPointsPair> allPairs) {
        int sampleStrength = 3;
        Collections.shuffle(allPairs, new Random(System.currentTimeMillis()));
        return allPairs.subList(0, sampleStrength);
    }

    private static List<InterestPointsPair> getPerspectiveSample(List<InterestPointsPair> allPairs) {
        int sampleStrength = 4;
        Collections.shuffle(allPairs, new Random(System.currentTimeMillis()));
        return allPairs.subList(0, sampleStrength);
    }

    private static RealMatrix getPerspectiveArgumentsUVMatrix(List<InterestPointsPair> sample) {
        RealMatrix matrixArgsUV = MatrixUtils.createRealMatrix(8, 1);
        double[] matrixResultsArray = {sample.get(0).interestPoint2.x, sample.get(1).interestPoint2.x,
                sample.get(2).interestPoint2.x, sample.get(3).interestPoint2.x, sample.get(0).interestPoint2.y,
                sample.get(1).interestPoint2.y, sample.get(2).interestPoint2.y, sample.get(3).interestPoint2.y};
        matrixArgsUV.setColumn(0, matrixResultsArray);
        return matrixArgsUV;
    }

    private static RealMatrix getPerspectiveArgumentsXYMatrix(List<InterestPointsPair> sample) {
        double x1 = sample.get(0).interestPoint1.x;
        double x2 = sample.get(1).interestPoint1.x;
        double x3 = sample.get(2).interestPoint1.x;
        double x4 = sample.get(3).interestPoint1.x;

        double y1 = sample.get(0).interestPoint1.y;
        double y2 = sample.get(1).interestPoint1.y;
        double y3 = sample.get(2).interestPoint1.y;
        double y4 = sample.get(3).interestPoint1.y;

        double u1 = sample.get(0).interestPoint2.x;
        double u2 = sample.get(1).interestPoint2.x;
        double u3 = sample.get(2).interestPoint2.x;
        double u4 = sample.get(3).interestPoint2.x;

        double v1 = sample.get(0).interestPoint2.y;
        double v2 = sample.get(1).interestPoint2.y;
        double v3 = sample.get(2).interestPoint2.y;
        double v4 = sample.get(3).interestPoint2.y;

        RealMatrix matrixArgsXY = MatrixUtils.createRealMatrix(8, 8);
        double[] matrixArgsColumn1 = {x1, x2, x3, x4, 0, 0, 0, 0};
        double[] matrixArgsColumn2 = {y1, y2, y3, y4, 0, 0, 0, 0};
        double[] matrixArgsColumn3 = {1, 1, 1, 1, 0, 0, 0, 0};
        double[] matrixArgsColumn4 = {0, 0, 0, 0, x1, x2, x3, x4};
        double[] matrixArgsColumn5 = {0, 0, 0, 0, y1, y2, y3, y4};
        double[] matrixArgsColumn6 = {0, 0, 0, 0, 1, 1, 1, 1};
        double[] matrixArgsColumn7 = {-u1 * x1, -u2 * x2, -u3 * x3, -u4 * x4, -v1 * x1, -v2 * x2, -v3 * x3, -v4 * x4};
        double[] matrixArgsColumn8 = {-u1 * y1, -u2 * y2, -u3 * y3, -u4 * y4, -v1 * y1, -v2 * y2, -v3 * y3, -v4 * y4};
        matrixArgsXY.setColumn(0, matrixArgsColumn1);
        matrixArgsXY.setColumn(1, matrixArgsColumn2);
        matrixArgsXY.setColumn(2, matrixArgsColumn3);
        matrixArgsXY.setColumn(3, matrixArgsColumn4);
        matrixArgsXY.setColumn(4, matrixArgsColumn5);
        matrixArgsXY.setColumn(5, matrixArgsColumn6);
        matrixArgsXY.setColumn(6, matrixArgsColumn7);
        matrixArgsXY.setColumn(7, matrixArgsColumn8);
        return matrixArgsXY;
    }

    private static double getError(RealMatrix model, InterestPointsPair pair) {
        double[] argsXYArr = {pair.interestPoint1.x, pair.interestPoint1.y, 1};
        RealMatrix argsXY = MatrixUtils.createColumnRealMatrix(argsXYArr);
        double[] result = model.multiply(argsXY).getColumn(0);
        return getDistance(result[0], result[1], pair);
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
