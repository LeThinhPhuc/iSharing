package com.example.ggm;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ChatActivity extends AppCompatActivity {

    private DatabaseReference messagesRef;
    private EditText editTextMessage;
    private ListView listViewMessages;
    private MessageAdapter adapter;
    private ArrayList<Message> messagesList;
    private String currentUserId;
    private String friendId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        // Get user IDs from intent
        currentUserId = getIntent().getStringExtra("currentUserId");
        friendId = getIntent().getStringExtra("friendId");

        // Initialize Firebase
        messagesRef = FirebaseDatabase.getInstance().getReference().child("messages").child(getChatId(currentUserId, friendId));

        // Initialize views
        editTextMessage = findViewById(R.id.editTextMessage);
        listViewMessages = findViewById(R.id.listViewMessages);

        // Initialize messages list
        messagesList = new ArrayList<>();

        // Initialize adapter
        adapter = new MessageAdapter(this, messagesList);
        listViewMessages.setAdapter(adapter);

        // Load messages
        loadMessages();
    }

    // Load messages only once
    private void loadMessages() {
        messagesRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, String previousChildName) {
                Message message = dataSnapshot.getValue(Message.class);
                messagesList.add(message);
                adapter.notifyDataSetChanged();
                listViewMessages.smoothScrollToPosition(messagesList.size() - 1);
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, String previousChildName) {}

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {}

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, String previousChildName) {}

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(ChatActivity.this, "Failed to load messages: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void sendMessage(View view) {
        // Send a new message
        String text = editTextMessage.getText().toString().trim();
        if (!text.isEmpty()) {
            String messageId = String.valueOf(System.currentTimeMillis()); // Use timestamp as messageId
            Message message = new Message(currentUserId, friendId, text, messageId);
            messagesRef.child(messageId).setValue(message);

            // Insert notification for the receiver
            DatabaseReference notificationsRef = FirebaseDatabase.getInstance().getReference().child("users").child(friendId).child("notifications").push();
            Map<String, Object> notificationData = new HashMap<>();
            notificationData.put("senderId", currentUserId);
            notificationData.put("message", "Bạn đã nhận được tin nhắn từ " + currentUserId); // Update message value
            notificationsRef.setValue(notificationData);

            editTextMessage.setText("");
        }
    }



    // Generate unique chat ID based on user IDs
    private String getChatId(String userId1, String userId2) {
        if (userId1.compareTo(userId2) < 0) {
            return userId1 + "_" + userId2;
        } else {
            return userId2 + "_" + userId1;
        }
    }

}
