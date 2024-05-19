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
    private ArrayList<FriendRequest> friendRequests;
    private ArrayAdapter<FriendRequest> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.notification);

        listViewNotifications = findViewById(R.id.listViewNotifications);
        friendRequestsRef = FirebaseDatabase.getInstance().getReference().child("friendRequests");
        usersRef = FirebaseDatabase.getInstance().getReference().child("users");

        friendRequests = new ArrayList<>();
        adapter = new ArrayAdapter<FriendRequest>(this, R.layout.notificationitem, R.id.textViewNotification, friendRequests) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                if (convertView == null) {
                    convertView = getLayoutInflater().inflate(R.layout.notificationitem, parent, false);
                }

                TextView textViewNotification = convertView.findViewById(R.id.textViewNotification);
                Button buttonAccept = convertView.findViewById(R.id.buttonAccept);
                Button buttonReject = convertView.findViewById(R.id.buttonReject);

                FriendRequest friendRequest = getItem(position);
                textViewNotification.setText("Friend request from: " + friendRequest.senderId);

                buttonAccept.setOnClickListener(v -> acceptFriendRequest(friendRequest));
                buttonReject.setOnClickListener(v -> rejectFriendRequest(friendRequest));

                return convertView;
            }
        };
        listViewNotifications.setAdapter(adapter);

        loadFriendRequests();
    }

    private void loadFriendRequests() {
        friendRequests.clear();
        friendRequestsRef.orderByChild("receiverId").equalTo(currentUserId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String requestId = snapshot.getKey(); // Lấy key của nút Firebase
                    FriendRequest friendRequest = snapshot.getValue(FriendRequest.class);
                    if (friendRequest != null && "pending".equals(friendRequest.status)) {
                        friendRequest.setKey(requestId); // Gán key cho FriendRequest
                        friendRequests.add(friendRequest);
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Failed to load friend requests: " + databaseError.getMessage());
                // Handle possible errors
            }
        });
    }

    private void acceptFriendRequest(FriendRequest friendRequest) {
        String requestId = friendRequest.getKey(); // Sử dụng key của FriendRequest
        // Cập nhật trạng thái của yêu cầu là "accepted"
        friendRequestsRef.child(requestId).child("status").setValue("accepted").addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                // Thêm bạn bè vào danh sách của cả hai người
                addFriend(friendRequest.senderId, friendRequest.receiverId);
                addFriend(friendRequest.receiverId, friendRequest.senderId);

                // Gửi thông báo cho người gửi yêu cầu
                sendNotification(friendRequest.senderId, currentUserId + " đã chấp nhận lời mời kết bạn của bạn.");

                // Gửi thông báo đến NotificationActivity


                Toast.makeText(this, "Friend request accepted", Toast.LENGTH_SHORT).show();
                // Load lại danh sách yêu cầu kết bạn
                loadFriendRequests();
            } else {
                Toast.makeText(this, "Failed to accept friend request", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void rejectFriendRequest(FriendRequest friendRequest) {
        String requestId = friendRequest.getKey(); // Sử dụng key của FriendRequest
        // Cập nhật trạng thái của yêu cầu là "rejected"
        friendRequestsRef.child(requestId).child("status").setValue("rejected").addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                // Gửi thông báo cho người gửi yêu cầu
                sendNotification(friendRequest.senderId, currentUserId + " đã từ chối lời mời kết bạn của bạn.");

                // Gửi thông báo đến NotificationActivity
                sendNotificationToNotificationActivity();

                Toast.makeText(this, "Friend request rejected", Toast.LENGTH_SHORT).show();
                // Load lại danh sách yêu cầu kết bạn
                loadFriendRequests();
            } else {
                Toast.makeText(this, "Failed to reject friend request", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void sendNotificationToNotificationActivity() {
        // Gửi thông báo đến NotificationActivity
        // Code để gửi thông báo đến NotificationActivity ở đây
    }


    private void addFriend(String userId, String friendId) {
        DatabaseReference userFriendsRef = usersRef.child(userId).child("friends").push();
        String friendKey = userFriendsRef.getKey(); // Lấy key mới tạo cho node friend
        userFriendsRef.child("id").setValue(friendId);
        userFriendsRef.child("friendKey").setValue(friendKey); // Set key của node friend
        userFriendsRef.child("latitude").setValue(0); // Thêm thuộc tính latitude và set giá trị là 0
        userFriendsRef.child("longitude").setValue(0); // Thêm thuộc tính longitude và set giá trị là 0
        // Bạn có thể thêm các thông tin khác nếu cần
    }


    private void sendNotification(String userId, String message) {
        DatabaseReference notificationsRef = usersRef.child(userId).child("notifications").push();
        notificationsRef.child("message").setValue(message);
    }

    public static class FriendRequest {
        public String senderId;
        public String receiverId;
        public String status;
        public String key; // Thêm trường key để lưu key của nút Firebase

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
}
