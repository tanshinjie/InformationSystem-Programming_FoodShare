package managers;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


import datatypes.Request;

public class RequestManager {
    private static RequestManager ourInstance;

    public static RequestManager getInstance() {
        if(ourInstance == null){
            ourInstance = new RequestManager();
        }
        return ourInstance;
    }

    private static final String TAG = "RequestManager";

    private List<Request> lastKnownRequests;
    private Request lastRetrievedRequest;
    private Set<RequestsObserver> observers;
    private Set<RequestObserver> requestObservers;
    private ListenerRegistration registration;

    private RequestManager() {
        lastKnownRequests = new ArrayList<>();
        requestObservers = new HashSet<>();
        observers = new HashSet<>();
        updateRequests();
    }

    public List<Request> getLastKnownRequests() {
        return lastKnownRequests;
    }

    public Request getLastRetrievedRequest() {
        return lastRetrievedRequest;
    }

    public void updateRequests() {
        FirebaseFirestore.getInstance().collection("nrequests")
                .orderBy("timeCreated", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if(queryDocumentSnapshots!=null){
                            lastKnownRequests.clear();
                            for(DocumentSnapshot snapshot:queryDocumentSnapshots){
                                lastKnownRequests.add(new Request(snapshot.getData(), snapshot.getId()));
                            }
                            Log.i(TAG, "Got requests");
                            notifyObservers();
                        } else {
                            Log.i(TAG, "Null requests");
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.i(TAG, "Failed getting requests with message " + e.getMessage());
            }
        });
    }

    public void listenToRequest(String requestID){

        if(registration != null){
            registration.remove();
        }

        registration = FirebaseFirestore.getInstance().collection("nrequests")
                .document(requestID).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Log.i(TAG, "Listen failed.", e);
                } else {
                    if (documentSnapshot != null && documentSnapshot.exists()) {
                        lastRetrievedRequest = new Request(documentSnapshot.getData(), documentSnapshot.getId());
                        Log.i(TAG, "Got request");
                    } else {
                        lastRetrievedRequest = null;
                        Log.i(TAG, "Null lastRetrievedRequest");
                    }
                    notifyRequestObservers();
                }
            }
        });
    }

    public void register(RequestsObserver ob){
        observers.add(ob);
    }

    public void unregister(RequestsObserver ob){
        observers.remove(ob);
    }

    private void notifyObservers(){
        for(RequestsObserver o:observers){
            o.update(lastKnownRequests);
        }
    }

    public void register(RequestObserver o){
        requestObservers.add(o);
    }

    public void unregister(RequestObserver o){
        requestObservers.remove(o);
    }

    private void notifyRequestObservers(){
        for(RequestObserver o:requestObservers){
            o.update(lastRetrievedRequest);
        }
    }

    public static void newRequest(Request request){
        FirebaseFirestore.getInstance().collection("nrequests")
                .document(request.getRequestID())
                .set(request.toFirebaseMap())
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void voidObj) {
                        Log.i(TAG, "Request added successfully");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "Error adding document", e);
                    }
                });
    }

    public static void pushRequest(Request request){
        FirebaseFirestore.getInstance().collection("nrequests")
                .document(request.getRequestID())
                .update(request.toFirebaseMap())
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void voidObj) {
                        Log.i(TAG, "Request added successfully");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "Error adding document", e);
                    }
                });
    }

    public static void deleteRequest(String requestID){
        FirebaseFirestore.getInstance().collection("nrequests").document(requestID)
                .delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.i(TAG, "Request successfully deleted");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.i(TAG, "Failed to delete request");
            }
        });
    }

    public static void deinitialize(){
        ourInstance = null;
    }

    public interface RequestObserver {
        void update(Request request);
    }

    public interface RequestsObserver{
        void update(List<Request> requests);
    }
}
