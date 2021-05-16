package fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.foodshare.R;
import com.google.firebase.firestore.GeoPoint;

import java.util.ArrayList;
import java.util.List;

import datatypes.Request;
import managers.RequestManager;
import utilities.FilterUtils;
import utilities.RecyclerViewAdapter;

public class HomeFragment extends Fragment {

    View view;

    private RecyclerView recyclerView;
    private RecyclerViewAdapter adapter;
    private static final String TAG = "HomeActivity";
    private SwipeRefreshLayout swipe;
    private Button filterBtn;

    private RequestManager requestManager;
    private List<Request> requests;
    private List<Request> databaseRequests;
    private RequestManager.RequestsObserver observer;

    // Filter state
    private String restaurant;
    private String time;
    private GeoPoint geoPoint;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_home, container, false);

        requestManager = RequestManager.getInstance();
        databaseRequests = requestManager.getLastKnownRequests();
        requests = new ArrayList<>(databaseRequests);

        setupSwipeRefresh();
        setupObserver();
        initRecyclerView();
        setupFilter();

        requestManager.updateRequests();

        return view;
    }

    private void setupSwipeRefresh(){
        swipe = view.findViewById(R.id.swipe);
        swipe.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                requestManager.updateRequests();
                swipe.setRefreshing(false);
            }
        });
    }

    private void setupObserver(){
        observer = new RequestManager.RequestsObserver() {
            @Override
            public void update(List<Request> newRequests) {
                requests.clear();
                List<Request> filteredRequests = FilterUtils.filter(databaseRequests, restaurant, time, geoPoint);
                requests.addAll(filteredRequests);
                adapter.notifyDataSetChanged();
            }
        };
    }

    private void initRecyclerView() {
        Log.i(TAG, "init recyclerView");
        recyclerView = view.findViewById(R.id.recyclerView);
        adapter = new RecyclerViewAdapter(getContext(), requests);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
    }

    private void setupFilter(){
        filterBtn = view.findViewById(R.id.filterBtn);

        final FilterDialog.FilterDialogListener listener = new FilterDialog.FilterDialogListener() {
            @Override
            public void update(String nrestaurant, String ntime, GeoPoint ngeoPoint) {
                restaurant = nrestaurant;
                time = ntime;
                geoPoint = ngeoPoint;
                requests.clear();
                List<Request> newRequests = FilterUtils.filter(databaseRequests, restaurant, time, geoPoint);
                requests.addAll(newRequests);
                adapter.notifyDataSetChanged();
            }
        };

        filterBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FilterDialog dialog = new FilterDialog(listener);
                dialog.show(getFragmentManager(), "dialog");
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        requestManager.register(observer);
    }

    @Override
    public void onStop() {
        super.onStop();
        requestManager.unregister(observer);
    }
}
