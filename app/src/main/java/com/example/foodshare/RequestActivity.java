package com.example.foodshare;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Date;
import java.util.List;

import datatypes.Message;
import managers.MessagesManager;
import managers.RequestManager;
import utilities.MessageAdapter;
import datatypes.Request;
import managers.UserManager;

public class RequestActivity extends AppCompatActivity {

    FirebaseUser fuser;

    ImageButton btn_send;
    EditText text_send;

    MessageAdapter messageAdapter;
    List<Message> mchat;
    MessagesManager messagesManager = MessagesManager.getInstance();
    RequestManager requestManager = RequestManager.getInstance();
    MessagesManager.MessagesObserver observer;
    RequestManager.RequestObserver requestObserver;

    RecyclerView recyclerView;
    Toolbar toolbar;

    FirebaseFirestore db;

    Request request;

    private static final String TAG = "RequestActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request);

        toolbar = findViewById(R.id.requestToolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Request Chat");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        db = FirebaseFirestore.getInstance();
        String requestID = getIntent().getStringExtra(MainActivity.SELECTED_REQUEST_ID);

        if(requestID != null){ // will be null when going from OrdersActivity back to Request Activity
            messagesManager.listen(requestID);
            requestManager.listenToRequest(requestID);
        }

        mchat = messagesManager.getMessages();

        request = requestManager.getLastRetrievedRequest();

        btn_send = findViewById(R.id.btn_send);
        text_send = findViewById(R.id.text_send);

        fuser = FirebaseAuth.getInstance().getCurrentUser();

        btn_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String msg = text_send.getText().toString();
                if (!msg.equals("")) {
                    Message newMessage = new Message(fuser.getUid(), text_send.getText().toString(),
                            new Timestamp(new Date()), UserManager.getInstance().getCurrentUsername());
                    messagesManager.sendMessage(newMessage, request.getRequestID());
                } else {
                    Toast.makeText(RequestActivity.this, "You can't send an empty message", Toast.LENGTH_SHORT).show();
                }
                text_send.setText("");
            }
        });

        findViewById(R.id.recycler_view).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                hideKeyboard(v);
                return false;
            }
        });

        setupObserver();
        initRecyclerView();
        updateOptions();
    }

    @Override
    protected void onStart() {
        super.onStart();
        messagesManager.register(observer);
        requestManager.register(requestObserver);
    }

    @Override
    protected void onStop() {
        super.onStop();
        messagesManager.unregister(observer);
        requestManager.unregister(requestObserver);
    }

    private void initRecyclerView(){
        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager((getApplicationContext()));
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);
        messageAdapter = new MessageAdapter(RequestActivity.this, mchat);
        recyclerView.setAdapter(messageAdapter);
    }

    private void setupObserver(){
        observer = new MessagesManager.MessagesObserver() {
            @Override
            public void update(List<Message> messages) {
                messageAdapter.notifyDataSetChanged();
                if(mchat.size() != 0){
                    recyclerView.smoothScrollToPosition(mchat.size()-1);
                }
            }
        };

        requestObserver = new RequestManager.RequestObserver() {
            @Override
            public void update(Request newRequest) {
                if(newRequest == null){ // Check if request deleted
                    Intent intent = new Intent(RequestActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    request = newRequest;
                    updateOptions();
                }
            }
        };
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.request_menu, menu);
        updateMenuOptions(menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if(item.getItemId() == android.R.id.home){
            onBackPressed();
            return true;
        }

        if(item.getItemId() == R.id.joinBtn){
            request.addParticipant(fuser.getUid(), UserManager.getInstance().getCurrentUsername());
            RequestManager.pushRequest(request);
            return true;
        }

        if(item.getItemId() == R.id.leaveBtn){
            request.removeParticipant(fuser.getUid());
            RequestManager.pushRequest(request);
            return true;
        }

        if(item.getItemId() == R.id.deleteBtn){
            RequestManager.deleteRequest(request.getRequestID());
            MessagesManager.deleteMessages(request.getRequestID());
            return true;
        }

        if(item.getItemId() == R.id.ordersBtn){
            Intent intent = new Intent(RequestActivity.this, OrdersActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void updateOptions(){
        try{
            getSupportActionBar().setTitle(request.getRestaurant() + ", " + request.getLocation().getName());
            if(!request.getOrders().containsKey(fuser.getUid())){
                findViewById(R.id.messageInput).setVisibility(View.GONE);
            } else {
                findViewById(R.id.messageInput).setVisibility(View.VISIBLE);
            }
        } catch(NullPointerException e){
            Log.i(TAG, "Empty request");
            findViewById(R.id.messageInput).setVisibility(View.GONE);
        }
        invalidateOptionsMenu(); // force recreation of menu
    }

    private void updateMenuOptions(Menu menu){

        MenuItem deleteBtn = menu.findItem(R.id.deleteBtn);
        MenuItem leaveBtn = menu.findItem(R.id.leaveBtn);
        MenuItem joinBtn = menu.findItem(R.id.joinBtn);

        deleteBtn.setVisible(false);
        leaveBtn.setVisible(false);
        joinBtn.setVisible(false);

        try{
            if(!request.getOrders().containsKey(fuser.getUid())){
                joinBtn.setVisible(true);
            } else {
                if(request.getOwnerID().equals(fuser.getUid()))
                {
                    deleteBtn.setVisible(true);
                } else {
                    leaveBtn.setVisible(true);
                }
            }
        } catch(NullPointerException e){
            Log.i(TAG, "Empty request");
        }
    }

    // Bring user back to Home screen regardless of which activity he came from
    @Override
    public void onBackPressed() {
        super.onBackPressed();

        Intent intent = new Intent(RequestActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    public void hideKeyboard(View view) {
        InputMethodManager inputMethodManager =(InputMethodManager)getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }
}

