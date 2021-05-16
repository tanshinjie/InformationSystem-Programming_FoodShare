package utilities;

import android.util.Log;

import com.google.firebase.firestore.GeoPoint;

import java.util.ArrayList;
import java.util.List;

import datatypes.Request;

public class FilterUtils {

    private static final String TAG = "FilterUtils";

    public static boolean distanceCheck(Request request, GeoPoint g2, int max) {
        GeoPoint g1 = request.getLocation().getGeoPoint();
        double dist = distFrom(g1.getLatitude(), g1.getLongitude(), g2.getLatitude(), g2.getLongitude());

        if(dist <= max){
            return true;
        } else {
            return false;
        }
    }

    public static boolean restaurantCheck(Request request, String s){
        if(request.getRestaurant().equals(s)){
            return true;
        } else {
            return false;
        }
    }

    public static boolean timeCheck(Request request, String s){
        String[] split = s.split(":");
        String[] rSplit = request.getOrderByTime().split(":");
        int minute = Integer.valueOf(split[1]);
        int rMinute = Integer.valueOf(rSplit[1]);
        int hour = Integer.valueOf(split[0]);
        int hourBefore = hour - 1;
        if(hourBefore == -1){
            hourBefore = 23;
        }
        int hourAfter = hour + 1;
        if(hourAfter == 24){
            hourAfter = 0;
        }

        int rHour = Integer.valueOf(rSplit[0]);

        if(rHour == hour){
            return true;
        } else if(rHour == hourBefore){
            if(rMinute >= minute){
                return true;
            }
        } else if (rHour == hourAfter){
            if(rMinute <= minute){
                return true;
            }
        }
        return false;
    }

    public static List<Request> filter(List<Request> requests, String restaurant, String time, GeoPoint geoPoint){
        List<Request> output = new ArrayList<>();
        for(Request r:requests){
            boolean check = true;
            if(restaurant != null){
                check = restaurantCheck(r, restaurant);
            }
            if(check && time != null){
                check = timeCheck(r, time);
            }
            if(check && geoPoint != null){
                check = distanceCheck(r, geoPoint, 300);
            }
            if(check){
                output.add(r);
            }
        }
        return output;
    }

    private static double distFrom(double lat1, double lng1, double lat2, double lng2) {
        double earthRadius = 6371000; //meters
        double dLat = Math.toRadians(lat2-lat1);
        double dLng = Math.toRadians(lng2-lng1);
        double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLng/2) * Math.sin(dLng/2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        return (earthRadius * c);
    }
}