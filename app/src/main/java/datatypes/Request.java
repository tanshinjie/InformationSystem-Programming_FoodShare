package datatypes;

import androidx.annotation.NonNull;

import com.google.firebase.Timestamp;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import managers.UserManager;

public class Request {

    private static final String TAG = "Request";

    public String getRequestID() {
        return requestID;
    }

    public String getRestaurant() {
        return restaurant;
    }

    public LocationGMaps getLocation() {
        return location;
    }

    public String getOrderByTime() {
        return orderByTime;
    }

    public String getRemarks() {
        return remarks;
    }

    public Map<String, OrderItem> getOrders() {
        return orders;
    }

    public String getOwnerID() {
        return ownerID;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public double getDeliveryFee() {
        return deliveryFee;
    }

    public Timestamp getTimeCreated() {
        return timeCreated;
    }

    private String requestID;
    private String restaurant;
    private LocationGMaps location;
    private String orderByTime;
    private String remarks;
    private Map<String, OrderItem> orders;
    private String ownerName;
    private Timestamp timeCreated;
    private String ownerID;
    private double deliveryFee;

    public Request (String rest, LocationGMaps loc, String t, String remarks, String ownerID, String ownerName, double deliveryFee) {
        this.requestID = UUID.randomUUID().toString(); // Random request ID
        this.restaurant = rest;
        this.location = loc;
        this.orderByTime = t;
        this.remarks = remarks;
        this.orders = new HashMap<>();
        this.ownerID = ownerID;
        this.ownerName = ownerName;
        this.timeCreated = new Timestamp(new Date());
        this.deliveryFee = deliveryFee;
    }

    public Request(Map data, String requestId){
        this.requestID = requestId;
        this.restaurant = (String) data.get("restaurant");
        this.location = new LocationGMaps((Map) data.get("location"));
        this.orderByTime = (String) data.get("orderBy");
        this.remarks = (String) data.get("remarks");
        this.orders = OrderItem.ordersFromFirebaseMap((Map<String, Object>)data.get("orders"));
        this.ownerID = (String) data.get("ownerID");
        this.timeCreated = (Timestamp) data.get("timeCreated");
        this.ownerName = (String) data.get("ownerName");
        this.deliveryFee = (double) data.get("deliveryFee");
    }

    public Map toFirebaseMap() {
        Map<String, Object> output = new HashMap<>();
        output.put("restaurant", restaurant);
        output.put("location", location.toFirebaseMap());;
        output.put("orderBy", orderByTime);
        output.put("remarks", remarks);
        output.put("orders", OrderItem.ordersToFirebaseMap(orders));
        output.put("ownerID", ownerID);
        output.put("ownerName", ownerName);
        output.put("timeCreated", timeCreated);
        output.put("deliveryFee", deliveryFee);

        return output;
    }

    public void addParticipant(String userID, String username){
        orders.put(userID, OrderItem.newParticipant(username));
    }

    public void removeParticipant(String userID){
        orders.remove(userID);
    }

    @NonNull
    @Override
    public String toString() {
        return requestID;
    }
}