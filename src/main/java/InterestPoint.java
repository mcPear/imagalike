import java.util.List;

public class InterestPoint {
    public List<Integer> properties;

    public InterestPoint(List<Integer> properties) {
        this.properties = properties;
    }

    //TODO ogarnać czy liczyć różnicę czy liczbę takich samych
    public int getSamePropertiesCount(InterestPoint other) {
        if (this.properties.size() != other.properties.size()) {
            throw new IllegalArgumentException("Properties count differs");
        }

        int result = 0;

        for (int i = 0; i < properties.size(); i++) {
            if (this.properties.get(i).equals(other.properties.get(i))) {
                result++;
            }
        }
        return result;
    }
}
