package com.example.foodshare;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import fragments.CreateFragment;
import fragments.HomeFragment;
import fragments.ProfileFragment;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private String placeName;
    private String placeAddress;
    private double placeLat;
    private double placeLon;

    public static String SELECTED_REQUEST_ID = "SELECTED_REQUEST_ID";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BottomNavigationView bottomNav = findViewById(R.id.nav_bar);
        bottomNav.setOnNavigationItemSelectedListener(navListener);

        Intent currentIntent = getIntent();

        placeName = currentIntent.getStringExtra(MapActivity.SELECTED_NAME);
        placeAddress = currentIntent.getStringExtra(MapActivity.SELECTED_ADDRESS);
        placeLat = currentIntent.getDoubleExtra(MapActivity.SELECTED_LAT, 0);
        placeLon = currentIntent.getDoubleExtra(MapActivity.SELECTED_LON, 0);

        if(placeName != null && placeAddress != null && placeLat != 0 && placeLon != 0){
            Fragment createFragment = new CreateFragment();
            Bundle bundle = new Bundle();
            bundle.putString(MapActivity.SELECTED_NAME, placeName);
            bundle.putString(MapActivity.SELECTED_ADDRESS, placeAddress);
            bundle.putDouble(MapActivity.SELECTED_LAT, placeLat);
            bundle.putDouble(MapActivity.SELECTED_LON, placeLon);
            createFragment.setArguments(bundle);
            getSupportFragmentManager().beginTransaction().replace(R.id.container, createFragment).commit();
        } else {
            if (savedInstanceState == null) {
                bottomNav.getMenu().getItem(1).setChecked(true);
                getSupportFragmentManager().beginTransaction().replace(R.id.container,
                        new HomeFragment()).commit();
            }
        }
    }

    private BottomNavigationView.OnNavigationItemSelectedListener navListener =
            new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    Fragment selectedFragment = null;

                    switch (item.getItemId()) {
                        case R.id.nav_home:
                            selectedFragment = new HomeFragment();
                            break;
                        case R.id.nav_create:
                            selectedFragment = new CreateFragment();
                            break;
                        case R.id.nav_profile:
                            selectedFragment = new ProfileFragment();
                            break;
                    }

                    getSupportFragmentManager().beginTransaction().replace(R.id.container,
                            selectedFragment).commit();

                    return true;
                }
            };
}