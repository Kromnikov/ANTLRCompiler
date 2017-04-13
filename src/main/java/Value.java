/**
 * Created by makst on 04.03.2017.
 */
public class Value {

    public static Value VOID = new Value(new Object());

    final Object value;

    public Value(Object value) {
        this.value = value;
    }

    public Boolean asBoolean() {
        return (Boolean) value;
    }

    public Double asDouble() {
        return Double.valueOf(value.toString());
    }

    public Integer asInteger() {
        return (Integer) value;
    }

    public String asString() {
        return String.valueOf(value);
    }

    public boolean isDouble() {
        return value instanceof Double;
    }

    public boolean isBoolean() {
        return value instanceof Boolean;
    }

    public boolean isInteger() {
        return value instanceof Integer;
    }

    public boolean lessThan(Double than) {
        return (Double) value < than;
    }

    @Override
    public int hashCode() {

        if (value == null) {
            return 0;
        }

        return this.value.hashCode();
    }

    @Override
    public boolean equals(Object o) {

        if (value == o) {
            return true;
        }

        if (value == null || o == null || o.getClass() != value.getClass()) {
            return false;
        }

        Value that = (Value) o;

        return this.value.equals(that.value);
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }
}
