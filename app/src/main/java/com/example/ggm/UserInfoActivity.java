package com.example.ggm;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class UserInfoActivity extends AppCompatActivity {

    private EditText editTextId, editTextPassword, editTextPhoneNumber;
    private Button buttonUpdate;
    private DatabaseReference usersRef;
    private String userId;
    private String  user=LoginActivity.getUserId();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_info_layout);

        // Initialize views
        TextView textViewTitle = findViewById(R.id.textViewTitle);
        editTextId = findViewById(R.id.editTextId);
        editTextPassword = findViewById(R.id.editTextPassword);
        editTextPhoneNumber = findViewById(R.id.editTextPhoneNumber);
        buttonUpdate = findViewById(R.id.buttonUpdate);

        // Set title
        textViewTitle.setText("Thông tin người dùng");

        // Initialize Firebase
        usersRef = FirebaseDatabase.getInstance().getReference().child("users");

        // Get userId from intent
        userId = user;

        // Load user info
        loadUserInfo();

        // Set click listener for update button
        buttonUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateUserInfo();
            }
        });
    }

    private void loadUserInfo() {
        usersRef.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String id = dataSnapshot.child("id").getValue(String.class);
                    String password = dataSnapshot.child("password").getValue(String.class);
                    String phoneNumber = dataSnapshot.child("phoneNumber").getValue(String.class);

                    // Set loaded info to EditTexts
                    editTextId.setText(id);
                    editTextPassword.setText(password);
                    editTextPhoneNumber.setText(phoneNumber);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle possible errors
            }
        });
    }

    private void updateUserInfo() {
        final String id = editTextId.getText().toString().trim();
        final String password = editTextPassword.getText().toString().trim();
        final String phoneNumber = editTextPhoneNumber.getText().toString().trim();

        // Check if any field is empty
        if (TextUtils.isEmpty(id) || TextUtils.isEmpty(password) || TextUtils.isEmpty(phoneNumber)) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Update user info in Firebase
        usersRef.child(userId).child("id").setValue(id);
        usersRef.child(userId).child("password").setValue(password);
        usersRef.child(userId).child("phoneNumber").setValue(phoneNumber);
        Toast.makeText(UserInfoActivity.this, "User info updated successfully", Toast.LENGTH_SHORT).show();
    }
}
