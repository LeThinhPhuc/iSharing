package com.example.ggm;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
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
    private static final int REQUEST_CALL_PHONE = 1;
    private EditText editTextMessage;
    private ListView listViewMessages;
    private MessageAdapter adapter;
    private ArrayList<Message> messagesList;
    private String currentUserId;
    private String friendId;
    private Button btn_Call;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        btn_Call=findViewById(R.id.buttonCall);

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
        btn_Call.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                callFriend();
            }
        });
        // Set click listener for each item in the list view
        listViewMessages.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Get the message at the clicked position
                Message clickedMessage = messagesList.get(position);
                // Show delete confirmation dialog for the clicked message
                showDeleteMessageDialog(clickedMessage.getMessageId());
            }
        });

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

    // Delete a message
    private void deleteMessage(String messageId) {
        messagesRef.child(messageId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(ChatActivity.this, "Message deleted", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(ChatActivity.this, "Failed to delete message", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    // Show confirmation dialog before deleting a message
    private void showDeleteMessageDialog(final String messageId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Bạn có muốn xóa tin nhắn này không?")
                .setPositiveButton("Có", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User confirmed delete
                        deleteMessage(messageId);
                    }
                })
                .setNegativeButton("Không", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User cancelled delete
                        dialog.dismiss();
                    }
                });
        AlertDialog dialog = builder.create();
        dialog.show();
    }
    private void callFriend() {
        // Kiểm tra xem người nhận có số điện thoại không
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference().child("users").child(friendId);
        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String phoneNumber = dataSnapshot.child("phoneNumber").getValue(String.class);
                    if (phoneNumber != null && !phoneNumber.isEmpty()) {
                        // Nếu có số điện thoại, thực hiện cuộc gọi
                        Intent callIntent = new Intent(Intent.ACTION_CALL);
                        callIntent.setData(Uri.parse("tel:" + phoneNumber));

                        // Kiểm tra quyền thực hiện cuộc gọi
                        if (ContextCompat.checkSelfPermission(ChatActivity.this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                            // Yêu cầu quyền thực hiện cuộc gọi nếu chưa được cấp
                            ActivityCompat.requestPermissions(ChatActivity.this, new String[]{Manifest.permission.CALL_PHONE}, REQUEST_CALL_PHONE);
                        } else {
                            // Thực hiện cuộc gọi nếu đã được cấp quyền
                            startActivity(callIntent);
                        }
                    } else {
                        // Hiển thị thông báo nếu không có số điện thoại
                        Toast.makeText(ChatActivity.this, "Không tìm thấy số điện thoại của người nhận", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Xử lý lỗi khi truy cập database
                Toast.makeText(ChatActivity.this, "Lỗi khi truy cập dữ liệu: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

}
