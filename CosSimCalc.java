import java.util.HashMap;

public class CosSimCalc {

    public double compareBusiness(Business biz1, Business biz2) {
        double dot = dotProduct(biz1.tfidfValues, biz2.tfidfValues);
        double mag = multiplyMagnitudes(biz1.tfidfValues, biz2.tfidfValues);

        return calculateCosSim(dot, mag);
    }

    public static double calculateCosSim(double num, double den) {
        return num/den;
    }

    public static double magnitude(HashMap<String,Double> map) {
        double mag= 0.0;
        for (double value : map.values()) {
            mag += value * value;
        }

        return Math.sqrt(mag);
    }

    public static double multiplyMagnitudes(HashMap<String, Double> biz1, HashMap<String, Double> biz2) {
        double magBiz1 = magnitude(biz1);
        double magBiz2 = magnitude(biz2);
        return magBiz1 * magBiz2;
    }

    public static double dotProduct(HashMap<String,Double> map1, HashMap<String, Double> map2) {
        double result = 0.0;

        for (String key : map1.keySet()) {
            if (map2.containsKey(key)) {
                result += map1.get(key) * map2.get(key);
            }
        }
        return result;
    }
}