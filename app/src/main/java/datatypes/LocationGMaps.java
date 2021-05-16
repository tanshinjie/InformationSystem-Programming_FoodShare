package datatypes;

import com.google.firebase.firestore.GeoPoint;

import java.util.HashMap;
import java.util.Map;

public class LocationGMaps {

    public String getName() {
        return name;
    }

    public GeoPoint getGeoPoint() {
        return geoPoint;
    }

    public String getAddress() {
        return address;
    }

    private String name;
    private GeoPoint geoPoint;
    private String address;



    public LocationGMaps(String name, String address, double lat, double lon) {
        this.name = name;
        this.geoPoint = new GeoPoint(lat, lon);
        this.address = address;
    }

    public LocationGMaps(String name, String address, GeoPoint geoPoint){
        this.name = name;
        this.geoPoint = geoPoint;
        this.address = address;
    }

    public LocationGMaps(Map<String, Object> map){
        this.name = (String) map.get("name");
        this.address = (String) map.get("address");
        this.geoPoint = (GeoPoint) map.get("geoPoint");
    }

    public Map toFirebaseMap(){
        Map<String, Object> output = new HashMap<>();
        output.put("name", name);
        output.put("geoPoint", geoPoint);
        output.put("address", address);

        return output;
    }
}