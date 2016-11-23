package co.za.philippe.fqc.http;

public class TypeValidate {

    public static boolean isInt(Object o) {
        try {
            Integer.parseInt(o.toString());
            return true;
        } catch (NumberFormatException e) {

        }
        return false;
    }
}
