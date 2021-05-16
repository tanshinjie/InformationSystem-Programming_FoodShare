package com.example.foodshare;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import datatypes.OrderItem;
import datatypes.Request;
import fragments.OrderDialog;
import managers.RequestManager;
import managers.RestaurantManager;
import utilities.OrderAdapter;

public class OrdersActivity extends AppCompatActivity {

    private static final String TAG = "OrdersActivity";

    Request request;
    List<OrderItem> orders;
    RequestManager.RequestObserver observer;
    RequestManager requestManager;
    RestaurantManager restaurantManager;

    Button addOrderBtn;
    Button removeOrderBtn;
    RecyclerView recyclerView;
    OrderAdapter orderAdapter;
    Toolbar toolbar;
    TextView requestTotal;

    FirebaseUser fuser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_orders);

        toolbar = findViewById(R.id.requestToolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Orders");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        fuser = FirebaseAuth.getInstance().getCurrentUser();

        restaurantManager = RestaurantManager.getInstance();
        requestManager =  RequestManager.getInstance();
        request = requestManager.getLastRetrievedRequest();
        orders = new ArrayList<>(request.getOrders().values());

        addOrderBtn = findViewById(R.id.addOrderBtn);
        addOrderBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openAddOrderDialog();
            }
        });
        removeOrderBtn = findViewById(R.id.removeOrderBtn);
        removeOrderBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openRemoveOrderDialog();
            }
        });

        requestTotal = findViewById(R.id.requestTotal);

        initRecyclerView();
        setupObserver();

        updateUI(); // Initial call
    }

    @Override
    protected void onStart() {
        super.onStart();
        requestManager.register(observer);
    }

    @Override
    protected void onStop() {
        super.onStop();
        requestManager.unregister(observer);
    }

    private void initRecyclerView(){
        recyclerView = findViewById(R.id.ordersRecycler);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager((getApplicationContext()));
        recyclerView.setLayoutManager(linearLayoutManager);
        orderAdapter = new OrderAdapter(OrdersActivity.this, orders);
        recyclerView.setAdapter(orderAdapter);
    }

    private void setupObserver(){
        observer = new RequestManager.RequestObserver() {
            @Override
            public void update(Request newRequest) {
                if(newRequest == null){
                    Intent intent = new Intent(OrdersActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    request = newRequest;
                    orders.clear();
                    orders.addAll(newRequest.getOrders().values());
                    orderAdapter.notifyDataSetChanged();
                    updateUI();
                }
            }
        };
    }

    private void openAddOrderDialog(){
        List<String> dialogOptions = new ArrayList<>();
        Map<String, Double> menu = restaurantManager.getRestaurants().get(request.getRestaurant());
        for(Map.Entry<String, Double> entry:menu.entrySet()){
            dialogOptions.add(entry.getKey() + " - " + entry.getValue());
        }

        OrderDialog.DialogListener callback = new OrderDialog.DialogListener() {
            @Override
            public void onResult(String s) {
                String[] split = s.split(" - ");
                request.getOrders()
                        .get(FirebaseAuth.getInstance().getUid())
                        .addItem(split[0], request.getRestaurant());
                RequestManager.pushRequest(request);
            }
        };

        OrderDialog dialog = new OrderDialog(callback, dialogOptions, "Add Order");

        dialog.show(getSupportFragmentManager(), "dialog");
    }

    private void openRemoveOrderDialog(){
        Set<String> unique = new HashSet<>(request.getOrders().get(fuser.getUid()).getOrders());
        List<String> dialogOptions = new ArrayList<>(unique);

        OrderDialog.DialogListener callback = new OrderDialog.DialogListener() {
            @Override
            public void onResult(String s) {
                request.getOrders().get(fuser.getUid()).removeItem(s, request.getRestaurant());
                RequestManager.pushRequest(request);
            }
        };

        OrderDialog dialog = new OrderDialog(callback, dialogOptions, "Remove Order");

        dialog.show(getSupportFragmentManager(), "dialog");
    }

    private void updateUI(){

        // Default to visible
        addOrderBtn.setVisibility(View.VISIBLE);
        removeOrderBtn.setVisibility(View.VISIBLE);

        // Update the total order sum
        double sum = request.getDeliveryFee();
        for(Map.Entry<String, OrderItem> entry:request.getOrders().entrySet()){
            sum += entry.getValue().getTotalPrice();
        }
        BigDecimal bd = BigDecimal.valueOf(sum);
        bd.setScale(2, RoundingMode.HALF_UP);
        requestTotal.setText(String.format("%.2f", bd));

        // User is not in the request
        if(!request.getOrders().containsKey(fuser.getUid())){
           addOrderBtn.setVisibility(View.GONE);
           removeOrderBtn.setVisibility(View.GONE);
        } else {
            // User is in the request, but has no orders yet
            if(request.getOrders().get(fuser.getUid()).getOrders().isEmpty()){
                removeOrderBtn.setVisibility(View.GONE);
            }
        }
    }
}
