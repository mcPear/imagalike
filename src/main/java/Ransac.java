import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;

import java.util.Collections;
import java.util.List;
import java.util.Random;

public class Ransac {

    public RealMatrix getBestModel(List<InterestPointsPair> interestPointsPairs, int iterationsCount, int maxError) {//int sampleStrength
        int sampleStrength = 3;
        RealMatrix bestModel = null;
        int bestScore = 0;
        RealMatrix model = null;
        List<InterestPointsPair> sample = null;
        for (int i = 0; i < iterationsCount; i++) {
            sample = getAffineSample(interestPointsPairs);
            model = getAffineModel(sample);
            int score = 0;
            long error;
            for (InterestPointsPair currentPair : interestPointsPairs) {
                error = getError(model, currentPair); //FIXME pair or many pairs ?
                if (error < maxError) {
                    score++;
                }
            }
            if (score > bestScore) {
                bestScore = score;
                bestModel = model;
            }
        }
        return bestModel;
    }

    public static RealMatrix getAffineModel(List<InterestPointsPair> sample) { //sampleStrength is specified for each transform ?
        RealMatrix matrix1 = MatrixUtils.createRealMatrix(6, 6);
        double[] matrix1Column1 = {sample.get(0).interestPoint1.x, sample.get(1).interestPoint1.x, sample.get(2).interestPoint1.x,
                0, 0, 0};
        double[] matrix1Column2 = {sample.get(0).interestPoint1.y, sample.get(1).interestPoint1.y, sample.get(2).interestPoint1.y,
                0, 0, 0};
        double[] matrix1Column3 = {1, 1, 1, 0, 0, 0};
        double[] matrix1Column4 = {0, 0, 0,
                sample.get(0).interestPoint1.x, sample.get(1).interestPoint1.x, sample.get(2).interestPoint1.x};
        double[] matrix1Column5 = {0, 0, 0,
                sample.get(0).interestPoint1.y, sample.get(1).interestPoint1.y, sample.get(2).interestPoint1.y};
        double[] matrix1Column6 = {0, 0, 0, 1, 1, 1};
        matrix1.setColumn(0, matrix1Column1);
        matrix1.setColumn(1, matrix1Column2);
        matrix1.setColumn(2, matrix1Column3);
        matrix1.setColumn(3, matrix1Column4);
        matrix1.setColumn(4, matrix1Column5);
        matrix1.setColumn(5, matrix1Column6);

        RealMatrix matrix2 = MatrixUtils.createRealMatrix(6, 1);
        double[] matrix2Array = {sample.get(0).interestPoint2.x, sample.get(1).interestPoint2.x, sample.get(2).interestPoint2.x,
                sample.get(0).interestPoint2.y, sample.get(1).interestPoint2.y, sample.get(2).interestPoint2.y};
        matrix2.setColumn(0, matrix2Array);
        return matrix1.power(-1).multiply(matrix2);
    }

    private List<InterestPointsPair> getAffineSample(List<InterestPointsPair> allPairs) {
        int sampleStrength = 3;
        System.out.println("Shuffle start");
        Collections.shuffle(allPairs, new Random(System.currentTimeMillis()));
        System.out.println("Shuffle end");
        return allPairs.subList(0, sampleStrength);
    }

    private int getError(RealMatrix model, InterestPointsPair pair) {
        return 0;
    }

}
