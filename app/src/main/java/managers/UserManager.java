package managers;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import java.util.HashSet;
import java.util.Set;

public class UserManager {

    private static final String TAG = "UserManager";

    private static UserManager ourInstance;

    public static UserManager getInstance() {
        if(ourInstance == null){
            ourInstance = new UserManager();
        }
        return ourInstance;
    }

    private String currentUsername;
    private Set<UserObserver> observers;

    public UserManager(){

        observers = new HashSet<>();

        FirebaseFirestore.getInstance().collection("users")
                .document(FirebaseAuth.getInstance().getUid())
                .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                        if(e != null){
                            Log.i(TAG, "Listen failed." + e);
                        } else {
                            if(documentSnapshot != null){
                                currentUsername = (String) documentSnapshot.get("username");
                                Log.i(TAG, "Got username");
                                notifyObservers();
                            } else {
                                Log.i(TAG, "Null received");
                            }
                        }
                    }
                });
    }

    public String getCurrentUsername(){
        return currentUsername;
    }

    public void register(UserObserver o){
        observers.add(o);
    }

    public void unregister(UserObserver o){
        observers.remove(o);
    }

    public void notifyObservers(){
        for(UserObserver o:observers){
            o.update(currentUsername);
        }
    }

    public static void deinitialize(){
        ourInstance = null;
    }

    public interface UserObserver {
        void update(String s);
    }
}
