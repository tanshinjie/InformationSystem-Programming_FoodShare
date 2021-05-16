package fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Spinner;
import android.widget.TimePicker;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialogFragment;
import androidx.fragment.app.FragmentManager;

import com.example.foodshare.MapActivity;
import com.example.foodshare.R;
import com.google.android.gms.common.api.Status;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.firebase.firestore.GeoPoint;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Set;

import managers.RestaurantManager;

public class FilterDialog extends AppCompatDialogFragment {

    private Spinner spinner;
    private Button orderTimeButton;
    private TimePickerDialog picker;
    private CheckBox restaurantCheckbox;
    private CheckBox timeCheckbox;
    private CheckBox locationCheckbox;
    private View locationFragment;

    private View createdView;

    private GeoPoint resultGeoPoint;
    private FilterDialogListener listener;

    private static final String TAG = "FilterDialog";

    public FilterDialog(FilterDialogListener listener){
        this.listener = listener;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();
        createdView = inflater.inflate(R.layout.filter_options, null);
        builder.setView(createdView)
                .setTitle("Choose Filters")
                .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String outRestaurant = spinner.getSelectedItem().toString();
                        if(!restaurantCheckbox.isChecked()){
                            outRestaurant = null;
                        }
                        String outTime = orderTimeButton.getText().toString();
                        if(!timeCheckbox.isChecked() || outTime.equals("Select Time")){
                            outTime = null;
                        }
                        GeoPoint outGeoPoint = resultGeoPoint;
                        if(!locationCheckbox.isChecked()){
                            outGeoPoint = null;
                        }
                        listener.update(outRestaurant, outTime, outGeoPoint);
                    }
                }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });

        // Setup spinner
        spinner = createdView.findViewById(R.id.restaurantSpinner);
        spinner.setVisibility(View.INVISIBLE);

        Set<String> items = RestaurantManager.getInstance().getRestaurants().keySet();
        List<String> itemsList = new ArrayList<>(items);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1,
                itemsList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        // Setup order time button
        orderTimeButton = createdView.findViewById(R.id.orderTimeButton);
        orderTimeButton.setVisibility(View.INVISIBLE);

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

        // Setup checkboxes
        restaurantCheckbox = createdView.findViewById(R.id.restaurantCheckbox);
        restaurantCheckbox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(((CheckBox)v).isChecked()){
                    spinner.setVisibility(View.VISIBLE);
                } else {
                    spinner.setVisibility(View.INVISIBLE);
                }
            }
        });

        timeCheckbox = createdView.findViewById(R.id.timeCheckbox);
        timeCheckbox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(((CheckBox)v).isChecked()){
                    orderTimeButton.setVisibility(View.VISIBLE);
                } else {
                    orderTimeButton.setVisibility(View.INVISIBLE);
                }
            }
        });

        locationCheckbox = createdView.findViewById(R.id.locationCheckbox);
        locationCheckbox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(((CheckBox)v).isChecked()){
                    locationFragment.setVisibility(View.VISIBLE);
                } else {
                    locationFragment.setVisibility(View.INVISIBLE);
                }
            }
        });

        return builder.create();
    }

    // Nested fragments stuff
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return createdView;
    }

    // Nested fragments stuff
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        setupAutocomplete();
        super.onActivityCreated(savedInstanceState);
    }

    // Autocomplete fragment
    private void setupAutocomplete(){
        locationFragment = createdView.findViewById(R.id.autocomplete_fragment);
        locationFragment.setVisibility(View.INVISIBLE);

        Places.initialize(getActivity().getApplicationContext(), getResources().getString(R.string.google_api_key));

        FragmentManager fm = getChildFragmentManager();

        AutocompleteSupportFragment autocompleteFragment = AutocompleteSupportFragment.newInstance();
        autocompleteFragment.setPlaceFields(MapActivity.DEFAULT_FIELDS);
        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(@NonNull Place place) {
                resultGeoPoint = new GeoPoint(place.getLatLng().latitude, place.getLatLng().longitude);
                Log.i(TAG, String.format("Name: %s, Address: %s, %s",
                        place.getName(), place.getAddress(),
                        place.getLatLng().toString()));
            }

            @Override
            public void onError(@NonNull Status status) {
                Log.i(TAG, "ERROR: " + status);
            }
        });

        fm.beginTransaction().replace(R.id.autocomplete_fragment, autocompleteFragment).commit();
    }

    public interface FilterDialogListener {
        void update(String restaurant, String time, GeoPoint geoPoint);
    }

}
