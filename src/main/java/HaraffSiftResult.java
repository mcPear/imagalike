import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class HaraffSiftResult {
    int propertiesCount;
    int interestPointsCount;
    float x;
    float y;
    float a;
    float b;
    float c;
    List<InterestPoint> interestPoints = new ArrayList<InterestPoint>();

    private HaraffSiftResult() {
    }

    public static HaraffSiftResult loadFromFile(String path) throws FileNotFoundException {
        File file = new File(path);
        Scanner sc = new Scanner(file);
        HaraffSiftResult result = new HaraffSiftResult();
        result.propertiesCount = sc.nextInt();
        result.interestPointsCount = sc.nextInt();

        for (int i = 0; i < result.interestPointsCount; i++) {
            result.x = sc.nextFloat();
            result.y = sc.nextFloat();
            result.a = sc.nextFloat();
            result.b = sc.nextFloat();
            result.c = sc.nextFloat();

            List<Integer> interestPoint = new ArrayList<Integer>();
            for (int j = 0; j < result.propertiesCount; j++) {
                interestPoint.add(sc.nextInt());
            }
            result.interestPoints.add(new InterestPoint(interestPoint));
        }

        return result;
    }

    @Override
    public String toString() {
        return "HaraffSiftResult{" +
                "propertiesCount=" + propertiesCount +
                ", interestPointsCount=" + interestPointsCount +
                ", x=" + x +
                ", y=" + y +
                ", a=" + a +
                ", b=" + b +
                ", c=" + c +
                ", interestPoints=" + interestPoints +
                '}';
    }


    private int getNearestInterestPointIndex(InterestPoint interestPoint, List<InterestPoint> otherInterestPoints) {
        int maxSimilarity = 0;
        int index = 0;
        for (int i = 0; i < otherInterestPoints.size(); i++) {
            InterestPoint curr = otherInterestPoints.get(i);
            int similarity = curr.getSamePropertiesCount(interestPoint);
            if (similarity > maxSimilarity) {
                maxSimilarity = similarity;
                index = i;
            }
        }
        return index;
    }

    //TODO get it from two images, then compare
    public List<OrderedInterestPoint> getOrderedInterestPoints(List<InterestPoint> otherInterestPoints) {
        List<OrderedInterestPoint> orderedInterestPoints = new ArrayList<OrderedInterestPoint>();
        for (InterestPoint curr : interestPoints) {
            orderedInterestPoints.add(new OrderedInterestPoint(curr, getNearestInterestPointIndex(curr, otherInterestPoints)));
        }
        return orderedInterestPoints;
    }

}
