package datatypes;

import com.google.firebase.Timestamp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Message {

    private String userID;
    private String text;
    private Timestamp timestamp;
    private String username;

    public String getUserID() {
        return userID;
    }

    public String getText() {
        return text;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public String getUsername() {
        return username;
    }

    public Message(Map<String, Object> data){
        userID = (String) data.get("userID");
        text = (String) data.get("text");
        timestamp = (Timestamp) data.get("timestamp");
        username = (String) data.get("username");
    }

    public Message(String userID, String text, Timestamp timestamp, String username){
        this.userID = userID;
        this.text = text;
        this.timestamp = timestamp;
        this.username = username;
    }

    public Map<String, Object> toFirebase(){
        Map<String, Object> output = new HashMap<>();
        output.put("userID", userID);
        output.put("text", text);
        output.put("username", username);
        output.put("timestamp", timestamp);

        return output;
    }

    public static Map<String, Object> messagesToFirebase(List<Message> messages){
        Map<String, Object> output = new HashMap<>();
        List<Object> messagesList = new ArrayList<>();
        for(Message message:messages){
            messagesList.add(message.toFirebase());
        }
        output.put("messages", messagesList);
        return output;
    }
}
