import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class InterestPointPairsFinder {

    public static List<InterestPointsPair> getInterestPointsPairs(HaraffSiftResult haraffSiftResult1, HaraffSiftResult haraffSiftResult2) {
        List<OrderedInterestPoint> orderedInterestPoints1 = haraffSiftResult1.getOrderedInterestPoints(haraffSiftResult2.interestPoints);
        List<OrderedInterestPoint> orderedInterestPoints2 = haraffSiftResult2.getOrderedInterestPoints(haraffSiftResult1.interestPoints);
        List<InterestPointsPair> interestPointsPairs = new ArrayList<InterestPointsPair>();

        for (int i = 0; i < orderedInterestPoints1.size(); i++) {
            OrderedInterestPoint current = orderedInterestPoints1.get(i);
            OrderedInterestPoint similarToCurrent = orderedInterestPoints2.get(current.similarInterestPointIndex);
            if (similarToCurrent.similarInterestPointIndex == i) {
                interestPointsPairs.add(new InterestPointsPair(current.interestPoint, similarToCurrent.interestPoint));
            }
        }
        return interestPointsPairs;
    }

    public static List<InterestPointsPair> getCohesiveInterestPointsPairs(List<InterestPointsPair> interestPointsPairs,
                                                                          int neighbourhoodCount, double cohesionTreshold) {
        List<InterestPointsPair> cohesiveInterestPointsPairs = new ArrayList<InterestPointsPair>();
        for (InterestPointsPair pair : interestPointsPairs) {
            if (getCohesion(pair, interestPointsPairs, neighbourhoodCount) >= cohesionTreshold) {
                cohesiveInterestPointsPairs.add(pair);
            }
        }
        return cohesiveInterestPointsPairs;
    }

    public static double getCohesion(InterestPointsPair argPair, List<InterestPointsPair> interestPointsPairs, int neighbourhoodCount) {
        List<InterestPointsPair> neighbours = getNeighbours(argPair, interestPointsPairs, neighbourhoodCount);
        return (double) neighbours.size() / (double) neighbourhoodCount;
    }

    public static List<InterestPointsPair> getNeighbours(InterestPointsPair pair,
                                                         List<InterestPointsPair> interestPointsPairs,
                                                         int neighbourhoodCount) {
        List<InterestPointsPair> neighbours = new ArrayList<InterestPointsPair>();
        List<InterestPointsPair> image1NeighbourPair = getImage1NeighbourPairs(pair, interestPointsPairs, neighbourhoodCount);
        List<InterestPointsPair> image2NeighbourPair = getImage2NeighbourPairs(pair, interestPointsPairs, neighbourhoodCount);
        for (int i = 0; i < neighbourhoodCount; i++) {
            InterestPointsPair currentPair = image1NeighbourPair.get(i);
            if (image2NeighbourPair.contains(currentPair)) {
                neighbours.add(currentPair);
            }
        }
        return neighbours;
    }

    public static List<InterestPointsPair> getImage1NeighbourPairs(final InterestPointsPair pair,
                                                                   List<InterestPointsPair> interestPointsPairs,
                                                                   int neighbourhoodCount) {
        List<InterestPointsPair> image1NeighbourPairs = new ArrayList<InterestPointsPair>(interestPointsPairs);
        Comparator<InterestPointsPair> comparator = new Comparator<InterestPointsPair>() {
            public int compare(InterestPointsPair o1, InterestPointsPair o2) {
                Double distance1 = Math.sqrt(Math.pow(pair.interestPoint1.x - o1.interestPoint1.x, 2) + Math.pow(pair.interestPoint1.y - o1.interestPoint1.y, 2));
                Double distance2 = Math.sqrt(Math.pow(pair.interestPoint1.x - o2.interestPoint1.x, 2) + Math.pow(pair.interestPoint1.y - o2.interestPoint1.y, 2));
                return distance1.compareTo(distance2);
            }
        };

        Collections.sort(image1NeighbourPairs, comparator);
        return image1NeighbourPairs.subList(0, neighbourhoodCount);
    }

    public static List<InterestPointsPair> getImage2NeighbourPairs(final InterestPointsPair pair,
                                                                   List<InterestPointsPair> interestPointsPairs,
                                                                   int neighbourhoodCount) {
        List<InterestPointsPair> image2NeighbourPairs = new ArrayList<InterestPointsPair>(interestPointsPairs);
        Comparator<InterestPointsPair> comparator = new Comparator<InterestPointsPair>() {
            public int compare(InterestPointsPair o1, InterestPointsPair o2) {
                Double distance1 = Math.sqrt(Math.pow(pair.interestPoint2.x - o1.interestPoint2.x, 2) + Math.pow(pair.interestPoint2.y - o1.interestPoint2.y, 2));
                Double distance2 = Math.sqrt(Math.pow(pair.interestPoint2.x - o2.interestPoint2.x, 2) + Math.pow(pair.interestPoint2.y - o2.interestPoint2.y, 2));
                return distance1.compareTo(distance2);
            }
        };

        Collections.sort(image2NeighbourPairs, comparator);
        return image2NeighbourPairs.subList(0, neighbourhoodCount);
    }

}
