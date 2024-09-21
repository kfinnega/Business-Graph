public class HaversineCalc {
    static final int earthRadius = 6371;

    public double distance(Business business1, Business business2) {

        double business1Lat = business1.latitude;
        double business1Long = business1.longitude;
        double business2Lat = business2.latitude;
        double business2Long = business2.longitude;

        double latDifferance = Math.toRadians((business2Lat - business1Lat));
        double longDifferance = Math.toRadians((business2Long - business1Long));

        business1Lat = Math.toRadians(business1Lat);
        business2Lat = Math.toRadians(business2Lat);

        double a = haversine(latDifferance) + Math.cos(business2Lat) * Math.cos(business1Lat) * haversine(longDifferance);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return earthRadius * c;
    }

    public static double haversine(double value) {
        return Math.pow(Math.sin(value / 2), 2);
    }


}
