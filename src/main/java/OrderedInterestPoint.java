public class OrderedInterestPoint {
    final InterestPoint interestPoint;
    final int similarInterestPointIndex;

    public OrderedInterestPoint(InterestPoint interestPoint, int index) {
        this.interestPoint = interestPoint;
        this.similarInterestPointIndex = index;
    }
}
