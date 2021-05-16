package com.example.foodshare;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

import java.util.List;
import java.util.Map;

import datatypes.Request;
import managers.RequestManager;
import managers.RestaurantManager;
import managers.UserManager;

public class LoadingActivity extends AppCompatActivity {

    private boolean gotUsername = false;
    private boolean gotRestaurants = false;
    private boolean gotRequests = false;

    private UserManager.UserObserver userObserver;
    private RestaurantManager.RestaurantsObserver restaurantsObserver;
    private RequestManager.RequestsObserver requestsObserver;

    private UserManager userManager;
    private RestaurantManager restaurantManager;
    private RequestManager requestManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading);

        // New session
        UserManager.deinitialize();
        RestaurantManager.deinitilize();
        RequestManager.deinitialize();

        userManager = UserManager.getInstance();
        restaurantManager = RestaurantManager.getInstance();
        requestManager = RequestManager.getInstance();

        setupObservers();
    }

    @Override
    protected void onStart() {
        super.onStart();

        userManager.register(userObserver);
        restaurantManager.register(restaurantsObserver);
        requestManager.register(requestsObserver);

    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    private void setupObservers(){
        userObserver = new UserManager.UserObserver() {
            @Override
            public void update(String s) {
                gotUsername = true;
                checkDone();
            }
        };

        restaurantsObserver = new RestaurantManager.RestaurantsObserver() {
            @Override
            public void update(Map<String, Map<String, Double>> restaurants) {
                gotRestaurants = true;
                checkDone();
            }
        };

        requestsObserver = new RequestManager.RequestsObserver() {
            @Override
            public void update(List<Request> requests) {
                gotRequests = true;
                checkDone();
            }
        };
    }

    private void checkDone(){
        if(gotUsername && gotRequests && gotRestaurants){

            userManager.unregister(userObserver);
            restaurantManager.unregister(restaurantsObserver);
            requestManager.unregister(requestsObserver);

            Intent intent = new Intent(LoadingActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }
    }
}
