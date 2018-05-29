import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class HaraffSiftResult {
    int propertiesCount;
    int interestPointsCount;
    List<InterestPoint> interestPoints = new ArrayList<InterestPoint>();

    private HaraffSiftResult() {
    }

    public static HaraffSiftResult loadFromFile(String path) throws FileNotFoundException {
        File file = new File(path);
        Scanner sc = new Scanner(file);
        HaraffSiftResult result = new HaraffSiftResult();
        result.propertiesCount = sc.nextInt();
        result.interestPointsCount = sc.nextInt();
        float x, y, a, b, c;

        for (int i = 0; i < result.interestPointsCount; i++) {
            x = sc.nextFloat();
            y = sc.nextFloat();
            a = sc.nextFloat();
            b = sc.nextFloat();
            c = sc.nextFloat();

            List<Integer> properties = new ArrayList<Integer>();
            for (int j = 0; j < result.propertiesCount; j++) {
                properties.add(sc.nextInt());
            }
            result.interestPoints.add(new InterestPoint(properties, x, y, a, b, c));
        }

        return result;
    }

    @Override
    public String toString() {
        return "HaraffSiftResult{" +
                "propertiesCount=" + propertiesCount +
                ", interestPointsCount=" + interestPointsCount +
                ", interestPointsSize=" + interestPoints.size() +
                '}';
    }

    private int getNearestInterestPointIndexBySameProperties(InterestPoint interestPoint, List<InterestPoint> otherInterestPoints) {
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

    //seems to be better
    private int getNearestInterestPointIndexBySimilarProperties(InterestPoint interestPoint, List<InterestPoint> otherInterestPoints) {
        long minDifference = Long.MAX_VALUE;
        int index = 0;
        for (int i = 0; i < otherInterestPoints.size(); i++) {
            InterestPoint curr = otherInterestPoints.get(i);
            int diff = curr.getPropertiesDifferenceSum(interestPoint);
            if (diff < minDifference) {
                minDifference = diff;
                index = i;
            }
        }
        return index;
    }

    public List<OrderedInterestPoint> getOrderedInterestPoints(List<InterestPoint> otherInterestPoints) {
        List<OrderedInterestPoint> orderedInterestPoints = new ArrayList<OrderedInterestPoint>();
        for (InterestPoint curr : interestPoints) {
            orderedInterestPoints.add(new OrderedInterestPoint(curr, getNearestInterestPointIndexBySimilarProperties(curr, otherInterestPoints)));
        }
        return orderedInterestPoints;
    }

}
