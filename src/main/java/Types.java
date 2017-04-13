/**
 * Created by makst on 11.03.2017.
 */
public enum Types {

    REAL, BOOLEAN, INTEGER;


    public static Types getType(String name) {
        if (name.equals(EvalVisitor.REAL_TYPE)) {
            return REAL;
        } else if (name.equals(EvalVisitor.INT_TYPE)) {
            return INTEGER;
        } else {
            return BOOLEAN;
        }
    }
}
