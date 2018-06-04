import java.util.List;

public class InterestPoint {

    public final List<Integer> properties;
    public final float x, y, a, b, c;

    public InterestPoint(List<Integer> properties, float x, float y, float a, float b, float c) {
        this.properties = properties;
        this.x = x;
        this.y = y;
        this.a = a;
        this.b = b;
        this.c = c;
    }

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

    public int getPropertiesDifferenceSum(InterestPoint other) {
        if (this.properties.size() != other.properties.size()) {
            throw new IllegalArgumentException("Properties count differs");
        }

        int result = 0;

        for (int i = 0; i < properties.size(); i++) {
            result += Math.abs(this.properties.get(i) - other.properties.get(i));
        }
        return result;
    }
}
