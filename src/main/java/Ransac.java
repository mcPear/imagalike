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
    private static double[] argsXYHelperInitialArray = {0, 0, 0};
    private static RealMatrix argsXYHelper = MatrixUtils.createColumnRealMatrix(argsXYHelperInitialArray);
    private static RealMatrix model = MatrixUtils.createRealMatrix(3, 3);
    private static RealMatrix affineMatrixArgsUV = MatrixUtils.createRealMatrix(6, 1);
    private static RealMatrix affineMatrixArgsXY = MatrixUtils.createRealMatrix(6, 6);
    private static RealMatrix perspectiveMatrixArgsUV = MatrixUtils.createRealMatrix(8, 1);
    private static RealMatrix perspectiveMatrixArgsXY = MatrixUtils.createRealMatrix(8, 8);

    public static List<InterestPointsPair> getBestModelFittedPairs(List<InterestPointsPair> interestPointsPairs, int iterationsCount, int maxError) {//int sampleStrength
        String bestModelName = null;
        String modelName = null;
        boolean modelFound = false;
        int bestScore = 0;
        List<InterestPointsPair> sample;
        List<InterestPointsPair> modelFittedPairs = new ArrayList<InterestPointsPair>();
        List<InterestPointsPair> bestModelFittedPairs = null;
        int switchTransformTreshold = iterationsCount / 2;
        for (int i = 0; i < iterationsCount; i++) {
            modelFound = false;
            while (!modelFound) {
                try {
                    if (i < switchTransformTreshold) {
                        sample = getAffineSample(interestPointsPairs);
                        setAffineModel(sample);
                        modelName = "affine transform";
                    } else {
                        sample = getPerspectiveSample(interestPointsPairs);
                        setPerspectiveModel(sample);
                        modelName = "perspective transform";
                    }
                    modelFound = true;
                } catch (SingularMatrixException e) {
//                    System.out.println("Singular matrix occured.");
                }
            }
//            System.out.println(model);
            modelFittedPairs.clear();
            int score = 0;
            double error;
            for (InterestPointsPair currentPair : interestPointsPairs) {
                error = getError(currentPair);
//                System.out.println("Error: " + error);
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
//            System.out.println("End of iteration: " + i);
        }
        System.out.println("The best model name: " + bestModelName);
        return bestModelFittedPairs;
    }

    public static void setAffineModel(List<InterestPointsPair> sample) {
        setAffineArgumentsUVMatrix(sample);
        setAffineArgumentsXYMatrix(sample);
//        System.out.println(matrixArgsXY);
        double[] affineVector = MatrixUtils.inverse(affineMatrixArgsXY).multiply(affineMatrixArgsUV).getColumn(0);
        double[] row1 = {affineVector[0], affineVector[1], affineVector[2]};
        double[] row2 = {affineVector[3], affineVector[4], affineVector[5]};
        double[] row3 = {0, 0, 1};
        model.setRow(0, row1);
        model.setRow(1, row2);
        model.setRow(2, row3);
    }

    private static void setAffineArgumentsUVMatrix(List<InterestPointsPair> sample) {
        double[] matrixResultsArray = {sample.get(0).interestPoint2.x, sample.get(1).interestPoint2.x, sample.get(2).interestPoint2.x,
                sample.get(0).interestPoint2.y, sample.get(1).interestPoint2.y, sample.get(2).interestPoint2.y};
        affineMatrixArgsUV.setColumn(0, matrixResultsArray);
    }

    private static void setAffineArgumentsXYMatrix(List<InterestPointsPair> sample) {
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
        affineMatrixArgsXY.setColumn(0, matrixArgsColumn1);
        affineMatrixArgsXY.setColumn(1, matrixArgsColumn2);
        affineMatrixArgsXY.setColumn(2, matrixArgsColumn3);
        affineMatrixArgsXY.setColumn(3, matrixArgsColumn4);
        affineMatrixArgsXY.setColumn(4, matrixArgsColumn5);
        affineMatrixArgsXY.setColumn(5, matrixArgsColumn6);
    }

    public static void setPerspectiveModel(List<InterestPointsPair> sample) {
        setPerspectiveArgumentsXYMatrix(sample);
        setPerspectiveArgumentsUVMatrix(sample);
//        System.out.println(matrixArgsXY);
        double[] affineVector = MatrixUtils.inverse(perspectiveMatrixArgsXY).multiply(perspectiveMatrixArgsUV).getColumn(0);
        double[] row1 = {affineVector[0], affineVector[1], affineVector[2]};
        double[] row2 = {affineVector[3], affineVector[4], affineVector[5]};
        double[] row3 = {affineVector[6], affineVector[7], 1};
        model.setRow(0, row1);
        model.setRow(1, row2);
        model.setRow(2, row3);
    }


    private static List<InterestPointsPair> getAffineSample(List<InterestPointsPair> allPairs) {
        int sampleStrength = 3;
        List<InterestPointsPair> sample = null;
//        boolean wrongSampleOccured = false;
        do {
//            if(wrongSampleOccured){
//                System.out.println("Wrong sample occured");
//            }
            Collections.shuffle(allPairs, new Random(System.currentTimeMillis()));
            sample = allPairs.subList(0, sampleStrength);
//            System.out.println("Wrong sample occured");
//        } while (!isAffineSampleValid(sample, 6, 200));
        } while (false);
        return sample;
    }

    private static boolean isAffineSampleValid(List<InterestPointsPair> sample, double r, double R) {
        double x1 = sample.get(0).interestPoint1.x;
        double x2 = sample.get(1).interestPoint1.x;
        double x3 = sample.get(2).interestPoint1.x;

        double y1 = sample.get(0).interestPoint1.y;
        double y2 = sample.get(1).interestPoint1.y;
        double y3 = sample.get(2).interestPoint1.y;

        double u1 = sample.get(0).interestPoint2.x;
        double u2 = sample.get(1).interestPoint2.x;
        double u3 = sample.get(2).interestPoint2.x;

        double v1 = sample.get(0).interestPoint2.y;
        double v2 = sample.get(1).interestPoint2.y;
        double v3 = sample.get(2).interestPoint2.y;

        double XYDistance12 = Math.sqrt(Math.pow(x1 - x2, 2) + Math.pow(y1 - y2, 2));
        double XYDistance23 = Math.sqrt(Math.pow(x2 - x3, 2) + Math.pow(y2 - y3, 2));
        double XYDistance13 = Math.sqrt(Math.pow(x1 - x3, 2) + Math.pow(y1 - y3, 2));
        double UVDistance12 = Math.sqrt(Math.pow(u1 - u2, 2) + Math.pow(v1 - v2, 2));
        double UVDistance23 = Math.sqrt(Math.pow(u2 - u3, 2) + Math.pow(v2 - v3, 2));
        double UVDistance13 = Math.sqrt(Math.pow(u1 - u3, 2) + Math.pow(v1 - v3, 2));

        return XYDistance12 > r && XYDistance23 > r && XYDistance13 > r && UVDistance12 > r && UVDistance13 > r && UVDistance23 > r &&
                XYDistance12 < R && XYDistance23 < R && XYDistance13 < R && UVDistance12 < R && UVDistance13 < R && UVDistance23 < R;
    }

    private static List<InterestPointsPair> getPerspectiveSample(List<InterestPointsPair> allPairs) {
        int sampleStrength = 4;
        List<InterestPointsPair> sample = null;
//        boolean wrongSampleOccured = false;
        do {
//            if(wrongSampleOccured){
//                System.out.println("Wrong sample occured");
//            }
            Collections.shuffle(allPairs, new Random(System.currentTimeMillis()));
            sample = allPairs.subList(0, sampleStrength);
//            wrongSampleOccured = true;
//        } while (!isPerspectiveSampleValid(sample, 6, 200));
        } while (false);
        return sample;
    }

    private static boolean isPerspectiveSampleValid(List<InterestPointsPair> sample, double r, double R) {
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

        double XYDistance12 = Math.sqrt(Math.pow(x1 - x2, 2) + Math.pow(y1 - y2, 2));
        double XYDistance23 = Math.sqrt(Math.pow(x2 - x3, 2) + Math.pow(y2 - y3, 2));
        double XYDistance13 = Math.sqrt(Math.pow(x1 - x3, 2) + Math.pow(y1 - y3, 2));
        double XYDistance14 = Math.sqrt(Math.pow(x1 - x4, 2) + Math.pow(y1 - y4, 2));
        double XYDistance24 = Math.sqrt(Math.pow(x2 - x4, 2) + Math.pow(y2 - y4, 2));
        double XYDistance34 = Math.sqrt(Math.pow(x3 - x4, 2) + Math.pow(y3 - y4, 2));
        double UVDistance12 = Math.sqrt(Math.pow(u1 - u2, 2) + Math.pow(v1 - v2, 2));
        double UVDistance23 = Math.sqrt(Math.pow(u2 - u3, 2) + Math.pow(v2 - v3, 2));
        double UVDistance13 = Math.sqrt(Math.pow(u1 - u3, 2) + Math.pow(v1 - v3, 2));
        double UVDistance14 = Math.sqrt(Math.pow(u1 - u4, 2) + Math.pow(v1 - v4, 2));
        double UVDistance24 = Math.sqrt(Math.pow(u2 - u4, 2) + Math.pow(v2 - v4, 2));
        double UVDistance34 = Math.sqrt(Math.pow(u3 - u4, 2) + Math.pow(v3 - v4, 2));

        return XYDistance12 > r && XYDistance23 > r && XYDistance13 > r && UVDistance12 > r && UVDistance13 > r
                && UVDistance23 > r && XYDistance14 > r && XYDistance24 > r && XYDistance34 > r &&
                XYDistance12 < R && XYDistance23 < R && XYDistance13 < R && UVDistance12 < R && UVDistance13 < R && UVDistance23 < R
                && UVDistance14 < R && UVDistance24 < R && UVDistance34 < R;
    }

    private static void setPerspectiveArgumentsUVMatrix(List<InterestPointsPair> sample) {
        double[] matrixResultsArray = {sample.get(0).interestPoint2.x, sample.get(1).interestPoint2.x,
                sample.get(2).interestPoint2.x, sample.get(3).interestPoint2.x, sample.get(0).interestPoint2.y,
                sample.get(1).interestPoint2.y, sample.get(2).interestPoint2.y, sample.get(3).interestPoint2.y};
        perspectiveMatrixArgsUV.setColumn(0, matrixResultsArray);
    }

    private static void setPerspectiveArgumentsXYMatrix(List<InterestPointsPair> sample) {
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

        double[] matrixArgsColumn1 = {x1, x2, x3, x4, 0, 0, 0, 0};
        double[] matrixArgsColumn2 = {y1, y2, y3, y4, 0, 0, 0, 0};
        double[] matrixArgsColumn3 = {1, 1, 1, 1, 0, 0, 0, 0};
        double[] matrixArgsColumn4 = {0, 0, 0, 0, x1, x2, x3, x4};
        double[] matrixArgsColumn5 = {0, 0, 0, 0, y1, y2, y3, y4};
        double[] matrixArgsColumn6 = {0, 0, 0, 0, 1, 1, 1, 1};
        double[] matrixArgsColumn7 = {-u1 * x1, -u2 * x2, -u3 * x3, -u4 * x4, -v1 * x1, -v2 * x2, -v3 * x3, -v4 * x4};
        double[] matrixArgsColumn8 = {-u1 * y1, -u2 * y2, -u3 * y3, -u4 * y4, -v1 * y1, -v2 * y2, -v3 * y3, -v4 * y4};
        perspectiveMatrixArgsXY.setColumn(0, matrixArgsColumn1);
        perspectiveMatrixArgsXY.setColumn(1, matrixArgsColumn2);
        perspectiveMatrixArgsXY.setColumn(2, matrixArgsColumn3);
        perspectiveMatrixArgsXY.setColumn(3, matrixArgsColumn4);
        perspectiveMatrixArgsXY.setColumn(4, matrixArgsColumn5);
        perspectiveMatrixArgsXY.setColumn(5, matrixArgsColumn6);
        perspectiveMatrixArgsXY.setColumn(6, matrixArgsColumn7);
        perspectiveMatrixArgsXY.setColumn(7, matrixArgsColumn8);
    }

    private static double getError(InterestPointsPair pair) {
        double[] argsXYArr = {pair.interestPoint1.x, pair.interestPoint1.y, 1};
        argsXYHelper.setColumn(0, argsXYArr);
        double[] result = model.multiply(argsXYHelper).getColumn(0);
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
