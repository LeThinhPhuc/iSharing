package com.example.ggm;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SearchView;
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

public class SearchFriendActivity extends AppCompatActivity {
    private SearchView searchView;
    private ListView listViewUsers;
    private DatabaseReference usersRef;
    private DatabaseReference friendRequestsRef;
    private String currentUserId = LoginActivity.getUserId(); // Giả sử người dùng hiện tại là user1
    private ArrayList<String> userIds;
    private ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search_friend);

        searchView = findViewById(R.id.searchView);
        listViewUsers = findViewById(R.id.listViewUsers);
        usersRef = FirebaseDatabase.getInstance().getReference().child("users");
        friendRequestsRef = FirebaseDatabase.getInstance().getReference().child("friendRequests");

        userIds = new ArrayList<>();
        adapter = new ArrayAdapter<>(this, R.layout.user_item, R.id.textViewUserId, userIds);
        listViewUsers.setAdapter(adapter);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchUsers(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                searchUsers(newText);
                return false;
            }
        });

        listViewUsers.setOnItemClickListener((parent, view, position, id) -> {
            String selectedUserId = userIds.get(position);
            checkFriendStatus(selectedUserId, view);
        });
    }

    private void searchUsers(String query) {
        userIds.clear();
        usersRef.orderByChild("id").startAt(query).endAt(query + "\uf8ff").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String userId = snapshot.child("id").getValue(String.class);
                    if (userId != null && !userId.equals(currentUserId)) {
                        userIds.add(userId);
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle possible errors
            }
        });
    }

    private void checkFriendStatus(String userId, View view) {
        DatabaseReference friendsRef = usersRef.child(currentUserId).child("friends");
        friendsRef.orderByChild("id").equalTo(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Button buttonAddFriend = view.findViewById(R.id.buttonAddFriend);
                TextView textViewYourFriend = view.findViewById(R.id.textViewYourFriend);

                if (dataSnapshot.exists()) {
                    buttonAddFriend.setVisibility(View.GONE);
                    textViewYourFriend.setVisibility(View.VISIBLE);
                    textViewYourFriend.setText("Your Friend");
                } else {
                    checkFriendRequestStatus(userId, buttonAddFriend, textViewYourFriend);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle possible errors
            }
        });
    }

    private void checkFriendRequestStatus(String userId, Button buttonAddFriend, TextView textViewYourFriend) {
        friendRequestsRef.orderByChild("senderId").equalTo(currentUserId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                boolean requestSent = false;
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    FriendRequest friendRequest = snapshot.getValue(FriendRequest.class);
                    if (friendRequest != null && friendRequest.receiverId.equals(userId)) {
                        requestSent = true;
                        if ("pending".equals(friendRequest.status) || "accepted".equals(friendRequest.status)) {
                            // Chỉ cho phép gửi yêu cầu kết bạn nếu trạng thái là "pending" hoặc "accepted"
                            buttonAddFriend.setVisibility(View.GONE);
                            textViewYourFriend.setVisibility(View.VISIBLE);
                            textViewYourFriend.setText("Sent");
                        } else if ("rejected".equals(friendRequest.status)) {
                            // Hiển thị buttonAddFriend khi yêu cầu đã bị từ chối
                            buttonAddFriend.setVisibility(View.VISIBLE);
                            textViewYourFriend.setVisibility(View.GONE);
                            buttonAddFriend.setOnClickListener(v -> sendFriendRequest(currentUserId, userId, buttonAddFriend, textViewYourFriend));
                        }
                        break;
                    }
                }
                if (!requestSent) {
                    // Hiển thị buttonAddFriend khi không có yêu cầu nào được gửi
                    buttonAddFriend.setVisibility(View.VISIBLE);
                    textViewYourFriend.setVisibility(View.GONE);
                    buttonAddFriend.setOnClickListener(v -> sendFriendRequest(currentUserId, userId, buttonAddFriend, textViewYourFriend));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle possible errors
            }
        });
    }

    private void sendFriendRequest(String senderId, String receiverId, Button buttonAddFriend, TextView textViewYourFriend) {
        DatabaseReference newRequestRef = friendRequestsRef.push();
        FriendRequest friendRequest = new FriendRequest(senderId, receiverId, "pending");
        newRequestRef.setValue(friendRequest).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(this, "Friend request sent", Toast.LENGTH_SHORT).show();
                buttonAddFriend.setVisibility(View.GONE);
                textViewYourFriend.setVisibility(View.VISIBLE);
                textViewYourFriend.setText("Sent");
            } else {
                Toast.makeText(this, "Failed to send friend request", Toast.LENGTH_SHORT).show();
            }
        });
    }


    public static class FriendRequest {
        public String senderId;
        public String receiverId;
        public String status;

        public FriendRequest() {
            // Default constructor required for calls to DataSnapshot.getValue(FriendRequest.class)
        }

        public FriendRequest(String senderId, String receiverId, String status) {
            this.senderId = senderId;
            this.receiverId = receiverId;
            this.status = status;
        }
    }
}