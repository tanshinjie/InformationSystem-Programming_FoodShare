package managers;

import android.util.Log;

import androidx.annotation.Nullable;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class RestaurantManager {

    private static final String TAG = "RestaurantManager";

    private static RestaurantManager ourInstance;

    public static RestaurantManager getInstance() {
        if(ourInstance == null){
            ourInstance = new RestaurantManager();
        }
        return ourInstance;
    }

    private Map<String, Map<String, Double>> restaurants;
    private Set<RestaurantsObserver> observers;

    private RestaurantManager() {
        restaurants = new HashMap<>();
        observers = new HashSet<>();

        FirebaseFirestore.getInstance().collection("restaurants")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                if(e != null){
                    Log.i(TAG, "Listen failed.", e);
                } else {
                    if(queryDocumentSnapshots != null){
                        restaurants.clear();
                        for(DocumentSnapshot snapshot:queryDocumentSnapshots){
                            Map<String, Double> menu = (Map<String, Double>) snapshot.get("items");
                            restaurants.put(snapshot.getId(), menu);
                        }
                        Log.i(TAG, "Got restaurants");
                        notifyObservers();
                    } else {
                        Log.i(TAG, "null received");
                    }
                }
            }
        });
    }

    public Map<String, Map<String, Double>> getRestaurants() {
        return restaurants;
    }

    public void register(RestaurantsObserver o){
        observers.add(o);
    }

    public void unregister(RestaurantsObserver o){
        observers.remove(o);
    }

    public void notifyObservers(){
        for(RestaurantsObserver o:observers){
            o.update(restaurants);
        }
    }

    public static void deinitilize(){
        ourInstance = null;
    }

    public interface RestaurantsObserver {
        void update(Map<String, Map<String, Double>> restaurants);
    }

}
