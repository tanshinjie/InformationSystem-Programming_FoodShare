package datatypes;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import managers.RestaurantManager;

public class OrderItem {

    private String username;

    private List<String> orders;

    private double totalPrice = 0;

    private OrderItem(String username, List<String> orders, double totalPrice){
        this.username = username;
        this.orders = orders;
        this.totalPrice = totalPrice;
    }

    public static OrderItem newParticipant(String username){
        return new OrderItem(username, new ArrayList<String>(), 0);
    }

    public OrderItem(Map<String, Object> data){
        this.username = (String) data.get("username");
        this.orders = (List<String>) data.get("orders");
        this.totalPrice = (double) data.get("totalPrice");
    }

    public void addItem(String name, String restaurantName){
        totalPrice += RestaurantManager.getInstance()
                .getRestaurants().get(restaurantName).get(name).doubleValue();
        BigDecimal bd = BigDecimal.valueOf(totalPrice);
        bd = bd.setScale(2, RoundingMode.HALF_UP);
        totalPrice = bd.doubleValue();
        orders.add(name);
    }

    public void removeItem(String name, String restaurantName){
        totalPrice -= RestaurantManager.getInstance()
                .getRestaurants().get(restaurantName).get(name).doubleValue();
        BigDecimal bd = BigDecimal.valueOf(totalPrice);
        bd = bd.setScale(2, RoundingMode.HALF_UP);
        totalPrice = bd.doubleValue();
        orders.remove(name);
    }

    public String getUsername() {
        return username;
    }

    public List<String> getOrders() {
        return orders;
    }

    public double getTotalPrice() {
        return totalPrice;
    }

    public Map<String, Object> toFirebaseMap() {
        Map<String, Object> output = new HashMap<>();

        output.put("username", username);
        output.put("orders", orders);
        output.put("totalPrice", totalPrice);

        return output;
    }

    public static Map<String, Object> ordersToFirebaseMap(Map<String, OrderItem> orders){
        Map<String, Object> output = new HashMap<>();

        for(Map.Entry<String, OrderItem> order:orders.entrySet()){
            output.put(order.getKey(), order.getValue().toFirebaseMap());
        }

        return output;
    }

    public static Map<String, OrderItem> ordersFromFirebaseMap(Map<String, Object> data){
        Map<String, OrderItem> output = new HashMap<>();
        for(Map.Entry<String, Object> entry:data.entrySet()){
            output.put(entry.getKey(), new OrderItem((Map<String, Object>) entry.getValue()));
        }
        return output;
    }
}
