package fragments;

import android.Manifest;
import android.app.Activity;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.foodshare.MainActivity;
import com.example.foodshare.MapActivity;
import com.example.foodshare.R;
import com.example.foodshare.RequestActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;

import datatypes.LocationGMaps;
import datatypes.Request;
import managers.MessagesManager;
import managers.RequestManager;
import managers.RestaurantManager;
import managers.UserManager;

import static android.content.Context.MODE_PRIVATE;

public class CreateFragment extends Fragment {

    private static final String TAG = "CreateFragment";

    View view;

    private String placeName;
    private String placeAddress;
    private double placeLat;
    private double placeLon;

    private Spinner restaurantSpinner;
    private TextView pickupAddText;
    private TextView orderTimeButton;
    private EditText remarksEditText;
    TimePickerDialog picker;
    private EditText deliveryFee;

    private FirebaseUser firebaseUser;

    public SharedPreferences sharedPreferences;
    public SharedPreferences.Editor editor;
    public static final String SHARED_PREFS = "sharedPrefs";
    public static final String SHARED_RESTAURANT = "sharedRest";
    public static final String SHARED_TIME = "sharedTime";
    public static final String SHARED_REMARKS = "sharedRemarks";
    private static int selectedRestPosition;
    private static String timeData;
    private static String remarksData;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_create, container, false);

        try {
            Bundle bundle = this.getArguments();
            placeName = this.getArguments().getString(MapActivity.SELECTED_NAME);
            placeAddress = getArguments().getString(MapActivity.SELECTED_ADDRESS);
            placeLat = getArguments().getDouble(MapActivity.SELECTED_LAT,0);
            placeLon = getArguments().getDouble(MapActivity.SELECTED_LON,0);
            Log.i(TAG, "onCreateView: " + bundle.isEmpty());
            Log.i(TAG, "onCreateView: " + placeName);
        } catch (NullPointerException e ){
            Log.i(TAG, "onCreateView: Empty");
        }


        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        ArrayList<String> restaurantNames = new ArrayList<>();
        restaurantNames.addAll(RestaurantManager.getInstance().getRestaurants().keySet()); // Generate list of restaurants

        restaurantSpinner = view.findViewById(R.id.restaurantSpinner);
        ArrayAdapter<String> myAdapter1 = new ArrayAdapter<String>(getContext(),
                android.R.layout.simple_list_item_1, restaurantNames);
        myAdapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        restaurantSpinner.setAdapter(myAdapter1);

        Button createButton = view.findViewById(R.id.createButton);
        remarksEditText = view.findViewById(R.id.remarksEditText);
        deliveryFee = view.findViewById(R.id.deliveryFee);

        orderTimeButton = view.findViewById(R.id.orderTimeButton);
        orderTimeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Calendar calendar = Calendar.getInstance();
                final int hour = calendar.get(Calendar.HOUR_OF_DAY);
                int minutes = calendar.get(Calendar.MINUTE);
                picker = new TimePickerDialog(getContext(),
                        new TimePickerDialog.OnTimeSetListener() {
                            @Override
                            public void onTimeSet(TimePicker tp, int sHour, int sMinute) {
                                String hourString;
                                String minuteString;
                                if (sHour < 10) {
                                    hourString = "0" + sHour;
                                } else {
                                    hourString = sHour + "";
                                }
                                if (sMinute < 10) {
                                    minuteString = "0" + sMinute;
                                } else {
                                    minuteString = sMinute + "";
                                }
                                orderTimeButton.setText(hourString + ":" + minuteString);
                            }
                        }, hour, minutes, true);
                picker.show();
            }
        });

        pickupAddText = view.findViewById(R.id.pickupAddText);
        if(placeName != null){
            pickupAddText.setText(placeName);
        }
        pickupAddText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                    saveData();
                    startActivity(new Intent(getContext(), MapActivity.class));

                } else {

                    requestPermission();

                }
            }
        });

        createButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(placeName != null && placeAddress != null && placeLon != 0
                        && placeLat != 0 && !orderTimeButton.getText().toString().equals("")
                        && !deliveryFee.getText().toString().equals("")){
                    // Clear between sessions
                    sharedPreferences = getActivity().getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
                    editor = sharedPreferences.edit();
                    editor.clear();
                    editor.apply();
                    createRequest();
                } else {
                    Toast.makeText(getContext(), "Please fill in all fields", Toast.LENGTH_SHORT).show();
                }


            }
        });

        view.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                hideKeyboard(v);
                return false;
            }
        });

        loadData();


        return view;
    }

    // Request location permission()
    private void requestPermission(){
        Dexter.withActivity(getActivity())
                .withPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse response) {
                        startActivity(new Intent(getContext(), MapActivity.class));
                        getActivity().finish();
                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse response) {
                        if(response.isPermanentlyDenied()){
                            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                            builder.setTitle("Permission Denied")
                                    .setMessage("Location access is permanently denied. Please change this in your device settings")
                                    .setNegativeButton("Cancel", null)
                                    .setPositiveButton("Ok", null) // TODO: Bring user to settings page
                                    .show();
                        } else {
                            Toast.makeText(getContext(), "Permission Denied", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {
                        token.continuePermissionRequest();
                    }
                })
                .check();
    }

    // get values from UI and upload to firebase
    private void createRequest(){
        String restaurantSelected = restaurantSpinner.getSelectedItem().toString();
        LocationGMaps loc = new LocationGMaps(placeName, placeAddress, placeLat, placeLon);
        String time = orderTimeButton.getText().toString();
        double fee = Double.valueOf(deliveryFee.getText().toString());
        BigDecimal bd = BigDecimal.valueOf(fee);
        fee = bd.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue(); // Round to 2 dp

        Request newRequest = new Request(restaurantSelected, loc, time,
                remarksEditText.getText().toString(),
                firebaseUser.getUid(), UserManager.getInstance().getCurrentUsername(), fee);

        newRequest.addParticipant(firebaseUser.getUid(), UserManager.getInstance().getCurrentUsername()); // Add owner in automatically

        // Initialize new request in database
        RequestManager.newRequest(newRequest);
        MessagesManager.setEmptyMessages(newRequest.getRequestID());

        // Go straight to request page after creation
        Intent intent = new Intent(getContext(), RequestActivity.class);
        intent.putExtra(MainActivity.SELECTED_REQUEST_ID, newRequest.getRequestID());
        startActivity(intent);
        getActivity().finish();
    }

    // save shared preferences
    public void saveData() {
        Log.i(TAG, "Save shared preferences");
        sharedPreferences = getActivity().getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        editor = sharedPreferences.edit();
        editor.putInt(SHARED_RESTAURANT, restaurantSpinner.getSelectedItemPosition());
        editor.putString(SHARED_TIME, orderTimeButton.getText().toString());
        editor.putString(SHARED_REMARKS, remarksEditText.getText().toString());
        editor.apply();

        Log.i(TAG, String.valueOf(restaurantSpinner.getSelectedItemPosition()));
        Log.i(TAG, orderTimeButton.getText().toString());
        Log.i(TAG, remarksEditText.getText().toString());
    }

    // load shared preferences
    public void loadData() {
        Log.i(TAG,"Load shared preferences");
        sharedPreferences = getActivity().getSharedPreferences(SHARED_PREFS,MODE_PRIVATE);
        selectedRestPosition = sharedPreferences.getInt(SHARED_RESTAURANT,0);
        timeData = sharedPreferences.getString(SHARED_TIME,"");
        remarksData = sharedPreferences.getString(SHARED_REMARKS,"");

        Log.i(TAG,String.valueOf(selectedRestPosition));
        Log.i(TAG,timeData);
        Log.i(TAG,remarksData);

        restaurantSpinner.setSelection(selectedRestPosition);
        orderTimeButton.setText(timeData);
        remarksEditText.setText(remarksData);
    }

    public void hideKeyboard(View view) {
        InputMethodManager inputMethodManager =(InputMethodManager)getActivity().getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

}

