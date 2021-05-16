package managers;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import datatypes.Message;

public class MessagesManager {
    private static final MessagesManager ourInstance = new MessagesManager();

    public static MessagesManager getInstance() {
        return ourInstance;
    }

    private static final String TAG = "MessagesManager";

    private List<Message> messages;
    private Set<MessagesObserver> observers;
    private ListenerRegistration registration;

    private MessagesManager() {
        messages = new ArrayList<>();
        observers = new HashSet<>();
    }

    public List<Message> getMessages() {
        return messages;
    }

    public void listen(String requestID){
        if(registration != null){
            registration.remove();
        }
        registration = FirebaseFirestore.getInstance().collection("messages").
                document(requestID).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Log.i(TAG, "Listen failed.", e);
                    return;
                } else {
                    if (documentSnapshot != null && documentSnapshot.exists()) {
                        messages.clear();
                        List newMessages = (List) documentSnapshot.getData().get("messages");
                        for(Object message:newMessages){
                            messages.add(new Message((Map) message));
                        }
                        Log.i(TAG, "Got messages");
                        notifyObservers();
                    } else {
                        Log.i(TAG, "Null received");
                    }
                }
            }
        });

    }

    public void sendMessage(Message message, String requestID){
        messages.add(message);
        FirebaseFirestore.getInstance().collection("messages")
                .document(requestID).update(Message.messagesToFirebase(messages))
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.i(TAG, "Messages updated");
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.i(TAG, "Failed to update messages");
            }
        });
    }

    public void register(MessagesObserver o){
        observers.add(o);
    }

    public void unregister(MessagesObserver o){
        observers.remove(o);
    }

    private void notifyObservers(){
        for(MessagesObserver o:observers){
            o.update(messages);
        }
    }

    public static void setEmptyMessages(String requestID){
        Map<String, Object> messages = new HashMap<>();
        messages.put("messages", new ArrayList<>());

        FirebaseFirestore.getInstance().collection("messages").document(requestID)
                .set(messages).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.i(TAG, "Successfully set empty messages");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.i(TAG, "Failed to set empty messages");
            }
        });
    }

    public static void deleteMessages(String requestID){
        FirebaseFirestore.getInstance().collection("messages").document(requestID)
                .delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.i(TAG, "Messages deleted");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.i(TAG, "Failed to delete messages");
            }
        });
    }

    public interface MessagesObserver {
        void update(List<Message> messages);
    }
}
