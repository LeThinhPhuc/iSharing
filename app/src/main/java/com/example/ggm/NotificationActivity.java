package com.example.ggm;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class NotificationActivity extends AppCompatActivity {
    private static final String TAG = "NotificationActivity";
    private ListView listViewNotifications;
    private DatabaseReference friendRequestsRef;
    private DatabaseReference usersRef;
    private String currentUserId = LoginActivity.getUserId();
    private ArrayList<Notification> notifications;
    private ArrayAdapter<Notification> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.notification);

        listViewNotifications = findViewById(R.id.listViewNotifications);
        friendRequestsRef = FirebaseDatabase.getInstance().getReference().child("friendRequests");
        usersRef = FirebaseDatabase.getInstance().getReference().child("users");

        notifications = new ArrayList<>();
        adapter = new ArrayAdapter<Notification>(this, R.layout.notificationitem, R.id.textViewNotification, notifications) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                if (convertView == null) {
                    convertView = getLayoutInflater().inflate(R.layout.notificationitem, parent, false);
                }

                TextView textViewNotification = convertView.findViewById(R.id.textViewNotification);
                Button buttonAccept = convertView.findViewById(R.id.buttonAccept);
                Button buttonReject = convertView.findViewById(R.id.buttonReject);
                Button buttonDeleteNotification = convertView.findViewById(R.id.buttonDelete);
                TextView textViewStatus = convertView.findViewById(R.id.textViewStatus);

                Notification notification = getItem(position);
                textViewNotification.setText(notification.message);

                if (notification.isFriendRequest) {
                    buttonAccept.setVisibility(View.VISIBLE);
                    buttonReject.setVisibility(View.VISIBLE);
                    buttonDeleteNotification.setVisibility(View.GONE);
                    textViewStatus.setVisibility(View.GONE);

                    buttonAccept.setOnClickListener(v -> {
                        acceptFriendRequest(notification.friendRequest);
                        buttonAccept.setVisibility(View.GONE);
                        buttonReject.setVisibility(View.GONE);
                        textViewStatus.setVisibility(View.VISIBLE);
                        textViewStatus.setText("Accepted");
                    });

                    buttonReject.setOnClickListener(v -> {
                        rejectFriendRequest(notification.friendRequest);
                        buttonAccept.setVisibility(View.GONE);
                        buttonReject.setVisibility(View.GONE);
                        textViewStatus.setVisibility(View.VISIBLE);
                        textViewStatus.setText("Rejected");
                    });
                } else {
                    buttonAccept.setVisibility(View.GONE);
                    buttonReject.setVisibility(View.GONE);
                    buttonDeleteNotification.setVisibility(View.VISIBLE);
                    textViewStatus.setVisibility(View.GONE);

                    buttonDeleteNotification.setOnClickListener(v -> {
                        deleteNotification(notification);
                        notifications.remove(position);
                        notifyDataSetChanged();
                    });
                }

                return convertView;
            }
        };
        listViewNotifications.setAdapter(adapter);

        loadNotifications();
    }

    private void loadNotifications() {
        // Load friend requests
        friendRequestsRef.orderByChild("receiverId").equalTo(currentUserId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String requestId = snapshot.getKey();
                    FriendRequest friendRequest = snapshot.getValue(FriendRequest.class);
                    if (friendRequest != null && "pending".equals(friendRequest.status)) {
                        friendRequest.setKey(requestId);
                        notifications.add(new Notification("Friend request from: " + friendRequest.senderId, true, friendRequest));
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Failed to load friend requests: " + databaseError.getMessage());
            }
        });

        // Load notifications from user's node
        usersRef.child(currentUserId).child("notifications").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String message = snapshot.child("message").getValue(String.class);
                    String key = snapshot.getKey();
                    if (message != null) {
                        Notification notification = new Notification(message, false, null);
                        notification.setKey(key);
                        notifications.add(notification);
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Failed to load user notifications: " + databaseError.getMessage());
            }
        });
    }

    private void acceptFriendRequest(FriendRequest friendRequest) {
        String requestId = friendRequest.getKey();
        friendRequestsRef.child(requestId).child("status").setValue("accepted").addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                addFriend(friendRequest.senderId, friendRequest.receiverId);
                addFriend(friendRequest.receiverId, friendRequest.senderId);
                sendNotification(friendRequest.senderId, currentUserId + " đã chấp nhận lời mời kết bạn của bạn.");
                Toast.makeText(this, "Friend request accepted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Failed to accept friend request", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void rejectFriendRequest(FriendRequest friendRequest) {
        String requestId = friendRequest.getKey();
        friendRequestsRef.child(requestId).child("status").setValue("rejected").addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                sendNotification(friendRequest.senderId, currentUserId + " đã từ chối lời mời kết bạn của bạn.");
                Toast.makeText(this, "Friend request rejected", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Failed to reject friend request", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addFriend(String userId, String friendId) {
        DatabaseReference userFriendsRef = usersRef.child(userId).child("friends").push();
        String friendKey = userFriendsRef.getKey();
        userFriendsRef.child("id").setValue(friendId);
        userFriendsRef.child("friendKey").setValue(friendKey);
        userFriendsRef.child("latitude").setValue(0);
        userFriendsRef.child("longitude").setValue(0);
    }

    private void sendNotification(String userId, String message) {
        DatabaseReference notificationsRef = usersRef.child(userId).child("notifications").push();
        notificationsRef.child("message").setValue(message);
    }

    private void deleteNotification(Notification notification) {
        String notificationId = notification.getKey();
        if (notificationId != null) {
            usersRef.child(currentUserId).child("notifications").child(notificationId).removeValue().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(this, "Notification deleted", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Failed to delete notification", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    public static class FriendRequest {
        public String senderId;
        public String receiverId;
        public String status;
        public String key;

        public FriendRequest() {
            // Default constructor required for calls to DataSnapshot.getValue(FriendRequest.class)
        }

        public FriendRequest(String senderId, String receiverId, String status) {
            this.senderId = senderId;
            this.receiverId = receiverId;
            this.status = status;
        }

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }
    }

    public static class Notification {
        public String message;
        public boolean isFriendRequest;
        public FriendRequest friendRequest;
        private String key;

        public Notification() {
            // Default constructor required for calls to DataSnapshot.getValue(Notification.class)
        }

        public Notification(String message, boolean isFriendRequest, FriendRequest friendRequest) {
            this.message = message;
            this.isFriendRequest = isFriendRequest;
            this.friendRequest = friendRequest;
        }

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }
    }
}
