package com.example.foodshare;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;

import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.PlaceLikelihood;
import com.google.android.libraries.places.api.net.FindCurrentPlaceRequest;
import com.google.android.libraries.places.api.net.FindCurrentPlaceResponse;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;

import java.util.ArrayList;
import java.util.Arrays;

public class MapActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap; // Variable to reference the map returned by google
    private FusedLocationProviderClient mFusedLocationProviderClient; // Client for getting device location
    private PlacesClient placesClient; // Client for Places API

    private Location mLastKnownLocation; // Last known location of the device
    private LocationCallback locationCallback; // Callback for location request
    private String SelectedMarker = "DEFAULT";

    private Button nearbyBtn; // Button for searching nearby

    private final float DEFAULT_ZOOM = 18; // Default zoom level for the map
    private final String TAG = "MapActivity";
    public static final ArrayList<Place.Field> DEFAULT_FIELDS = // Default fields for places
            new ArrayList<>(Arrays.asList(Place.Field.NAME, Place.Field.ADDRESS, Place.Field.LAT_LNG));

    public static final String SELECTED_NAME = "SELECTED_NAME";
    public static final String SELECTED_LAT = "SELECTED_LAT";
    public static final String SELECTED_LON = "SELECTED_LON";
    public static final String SELECTED_ADDRESS = "SELECTED_ADDRESS";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        setupMapsPlaces();

        setupAutocomplete();

        nearbyBtn = findViewById(R.id.nearbyBtn);
        nearbyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchNearby();
            }
        });
        nearbyBtn.setVisibility(View.INVISIBLE);
    }

    // Makes changes to the map once it is available
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // When marker is clicked twice in a row, choose that location
        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                if(!SelectedMarker.equals(marker.getTitle())){
                    SelectedMarker = marker.getTitle();
                } else {
                    Intent intent = new Intent(MapActivity.this, MainActivity.class);
                    Place markerPlace = (Place) marker.getTag();
                    intent.putExtra(SELECTED_NAME, markerPlace.getName());
                    intent.putExtra(SELECTED_LAT, markerPlace.getLatLng().latitude);
                    intent.putExtra(SELECTED_LON, markerPlace.getLatLng().longitude);
                    intent.putExtra(SELECTED_ADDRESS, markerPlace.getAddress());
                    startActivity(intent);
                    finish();
                }

                return false;
            }
        });

        getDeviceLocation();
    }

    // Get the location of the device and display on the map if available
    private void getDeviceLocation(){
        mFusedLocationProviderClient.getLastLocation()
                .addOnSuccessListener(new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        mLastKnownLocation = location;
                        if (mLastKnownLocation != null) {
                            mMap.setMyLocationEnabled(true); // Enable UI Elements
                            mMap.getUiSettings().setMyLocationButtonEnabled(true);
                            nearbyBtn.setVisibility(View.VISIBLE);

                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude()), DEFAULT_ZOOM));
                        } else {
                            Toast.makeText(MapActivity.this,
                                    "Unable to get location. Please turn on location services",
                                    Toast.LENGTH_SHORT)
                                    .show();
                            //Keep checking for location if not available
                            final LocationRequest locationRequest = LocationRequest.create();
                            locationRequest.setInterval(10000);
                            locationRequest.setFastestInterval(5000);
                            locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
                            locationCallback = new LocationCallback(){ // function to call when locationRequest is updated
                                @Override
                                public void onLocationResult(LocationResult locationResult) {
                                    super.onLocationResult(locationResult);
                                    if(locationResult != null){
                                        mMap.setMyLocationEnabled(true); // Enable UI Elements
                                        mMap.getUiSettings().setMyLocationButtonEnabled(true);
                                        nearbyBtn.setVisibility(View.VISIBLE);

                                        mLastKnownLocation = locationResult.getLastLocation();
                                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude()), DEFAULT_ZOOM ));
                                        mFusedLocationProviderClient.removeLocationUpdates(locationCallback); // Prevent further checking if a valid location is returned
                                    }
                                }
                            };
                            mFusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, null);
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.i(TAG, "Failed getting last location");
                    }
                });
    }

    private void setupAutocomplete(){
        AutocompleteSupportFragment autocompleteFragment = (AutocompleteSupportFragment)
                getSupportFragmentManager().findFragmentById(R.id.autocomplete_fragment);
        autocompleteFragment.setPlaceFields(DEFAULT_FIELDS);
        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(@NonNull Place place) {
                Log.i(TAG, String.format("Name: %s, Address: %s, %s",
                        place.getName(), place.getAddress(),
                        place.getLatLng().toString()));

                mMap.clear(); // Clear the map of previous markers
                mMap.addMarker(new MarkerOptions() // Add new marker at searched place
                        .position(place.getLatLng())
                        .title(place.getName() + ", " + place.getAddress()))
                .setTag(place);
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(place.getLatLng(), DEFAULT_ZOOM));
            }

            @Override
            public void onError(@NonNull Status status) {
                Log.i(TAG, "ERROR: " + status);
            }
        });
    }

    private void setupMapsPlaces(){
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        Places.initialize(getApplicationContext(), getResources().getString(R.string.google_api_key));
        placesClient = Places.createClient(this);
    }

    private void searchNearby(){
        // Refresh last known location first before searching for nearby places to allow proper map centering
        mFusedLocationProviderClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if(location != null){
                    mLastKnownLocation = location;

                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng( // Move camera to last known location
                                    mLastKnownLocation.getLatitude(),
                                    mLastKnownLocation.getLongitude()),
                            DEFAULT_ZOOM));

                    markNearbyPlaces();

                } else {
                    Toast.makeText(MapActivity.this,
                            "Unable to get location. Please turn on location services",
                            Toast.LENGTH_SHORT).show();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.i(TAG, "Failed getting last location");
            }
        });
    }

    private void markNearbyPlaces(){
        mMap.clear(); // Remove existing markers

        // Add markers for every place suggested by Google
        FindCurrentPlaceRequest request = FindCurrentPlaceRequest.newInstance(DEFAULT_FIELDS);
        placesClient.findCurrentPlace(request).addOnSuccessListener(new OnSuccessListener<FindCurrentPlaceResponse>() {
            @Override
            public void onSuccess(FindCurrentPlaceResponse findCurrentPlaceResponse) {
                for(PlaceLikelihood placeLikelihood : findCurrentPlaceResponse.getPlaceLikelihoods()){
                    mMap.addMarker(new MarkerOptions()
                            .position(placeLikelihood.getPlace().getLatLng())
                            .title(placeLikelihood.getPlace().getName() + ", " + placeLikelihood.getPlace().getAddress()))
                    .setTag(placeLikelihood.getPlace()); // Store the place object in the marker itself to retrieve data later
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.i(TAG, "Failed to get current places");
                if (e instanceof ApiException) {
                    ApiException apiException = (ApiException) e;
                    Log.i(TAG, "Place not found: " + apiException.getStatusCode());
                }
            }
        });
    }
}
