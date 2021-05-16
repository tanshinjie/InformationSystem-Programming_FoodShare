package com.example.foodshare;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.rengwuxian.materialedittext.MaterialEditText;

import java.util.HashMap;
import java.util.regex.Pattern;

public class RegisterActivity extends AppCompatActivity {

    MaterialEditText username, email, password;
    Button btn_register;

    private FirebaseAuth auth;
    FirebaseFirestore db;

    private static final String TAG = "RegisterActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Register");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        username = findViewById(R.id.username);
        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        btn_register = findViewById(R.id.btn_register);

        auth = FirebaseAuth.getInstance();

        btn_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String txt_username = username.getText().toString();
                String txt_email = email.getText().toString();
                String txt_password = password.getText().toString();
                if (TextUtils.isEmpty(txt_username) || TextUtils.isEmpty(txt_email) || TextUtils.isEmpty(txt_password)) {
                    Toast.makeText(RegisterActivity.this, "All fields are required", Toast.LENGTH_SHORT).show();
                } else if (txt_password.length() < 6) {
                    Toast.makeText(RegisterActivity.this, "password must be at least 6 characters", Toast.LENGTH_SHORT).show();
                } else if (txt_password.contains(" ")) {
                    Toast.makeText(RegisterActivity.this, "password must not contain spaces", Toast.LENGTH_SHORT).show();
                } else if (!Patterns.EMAIL_ADDRESS.matcher(txt_email).matches()) {
                    Toast.makeText(RegisterActivity.this, "Please enter a valid email", Toast.LENGTH_SHORT).show();
                } else if (txt_username.contains(" ")) {
                    Toast.makeText(RegisterActivity.this, "Username must not contain spaces", Toast.LENGTH_SHORT).show();
                } else {
                    btn_register.setEnabled(false);
                    register(txt_username, txt_email, txt_password);
                }
            }
        });

    }

    private void register(final String username, String email, String password) {

        auth.createUserWithEmailAndPassword(email,password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        System.out.println(task.getException());
                        System.out.println(task.isCanceled());
                        if (task.isSuccessful()) {
                            FirebaseUser firebaseUser = auth.getCurrentUser();
                            assert firebaseUser != null;
                            String userid = firebaseUser.getUid();

                            HashMap<String, String> hashMap = new HashMap<>();
                            hashMap.put("id", userid);
                            hashMap.put("username", username);

                            db = FirebaseFirestore.getInstance();
                            db.collection("users").document(userid).set(hashMap)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Intent intent = new Intent(RegisterActivity.this, StartActivity.class);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                        startActivity(intent);
                                        finish();
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.i(TAG, "creating user on firestore failed");
                                    }
                            });
                        } else {
                            btn_register.setEnabled(true);
                            Toast.makeText(RegisterActivity.this, "You can't register with this email or password", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

    }
}
