package com.example.ggm;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class FriendActivity extends AppCompatActivity {

    private DatabaseReference usersRef;
    private String currentUserId;
    private ArrayList<String> friendsList;
    private FriendAdapter adapter;
    private Button btn_Map, btn_Notification,btn_SearchFriend,btn_Friend;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend);
        btn_Friend=findViewById(R.id.btn_Friend);
        btn_Map=findViewById(R.id.btn_Map);
        btn_Notification=findViewById(R.id.btn_Notification);
        btn_SearchFriend=findViewById(R.id.btn_SearchFriend);
        btn_Map.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(FriendActivity.this, GooglemapActivity.class);
                startActivity(intent);
            }
        });
        btn_Friend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(FriendActivity.this, FriendActivity.class);
                startActivity(intent);
            }
        });
        btn_Notification.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(FriendActivity.this, NotificationActivity.class);
                startActivity(intent);
            }
        });
        btn_SearchFriend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(FriendActivity.this, SearchFriendActivity.class);
                startActivity(intent);
            }
        });

        // Get current user ID
        currentUserId = LoginActivity.getUserId(); // You need to replace this with your logic to get the current user ID

        // Initialize Firebase
        usersRef = FirebaseDatabase.getInstance().getReference().child("users");

        // Initialize RecyclerView
        RecyclerView recyclerViewFriends = findViewById(R.id.recyclerViewFriends);
        recyclerViewFriends.setLayoutManager(new LinearLayoutManager(this));

        // Initialize friends list
        friendsList = new ArrayList<>();

        // Initialize adapter
        adapter = new FriendAdapter();

        // Set adapter to RecyclerView
        recyclerViewFriends.setAdapter(adapter);

        // Load friends
        loadFriends();
    }

    private void loadFriends() {
        usersRef.child(currentUserId).child("friends").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                friendsList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String friendId = snapshot.child("id").getValue(String.class);
                    // You can also fetch additional information about the friend if needed
                    // and display it in the list view
                    friendsList.add(friendId);
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(FriendActivity.this, "Failed to load friends: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getUserInfo(String userId, ValueEventListener listener) {
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("users").child(userId);
        userRef.addListenerForSingleValueEvent(listener);
    }

    private class FriendAdapter extends RecyclerView.Adapter<FriendAdapter.FriendViewHolder> {

        @NonNull
        @Override
        public FriendViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.friend_item, parent, false);
            return new FriendViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull FriendViewHolder holder, int position) {
            String friendId = friendsList.get(position);
            holder.textViewFriendName.setText(friendId);

            holder.buttonChat.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Handle chat button click
                    getUserInfo(friendId, new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {
                                String userName = dataSnapshot.child("name").getValue(String.class);
                                Intent intent = new Intent(FriendActivity.this, ChatActivity.class);
                                intent.putExtra("currentUserId", currentUserId);
                                intent.putExtra("friendId", friendId);
                                intent.putExtra("friendName", userName);
                                startActivity(intent);
                            } else {
                                Toast.makeText(FriendActivity.this, "User does not exist", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            Toast.makeText(FriendActivity.this, "Failed to get user info: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            });

            holder.buttonDeleteFriend.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Handle delete button click
                    deleteFriend(friendId);
                }
            });
        }

        @Override
        public int getItemCount() {
            return friendsList.size();
        }

        class FriendViewHolder extends RecyclerView.ViewHolder {
            TextView textViewFriendName;
            Button buttonChat;
            Button buttonDeleteFriend;

            public FriendViewHolder(@NonNull View itemView) {
                super(itemView);
                textViewFriendName = itemView.findViewById(R.id.textViewFriendName);
                buttonChat = itemView.findViewById(R.id.buttonChat);
                buttonDeleteFriend = itemView.findViewById(R.id.buttonDeleteFriend);
            }
        }
    }

    private void deleteFriend(String friendId) {
        // Remove friend from current user's friend list
        usersRef.child(currentUserId).child("friends").orderByChild("id").equalTo(friendId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    snapshot.getRef().removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                // Remove friend from local list
                                friendsList.remove(friendId);
                                // Notify adapter that data set has changed
                                adapter.notifyDataSetChanged();

                                // Remove current user from friend's friend list
                                usersRef.child(friendId).child("friends").orderByChild("id").equalTo(currentUserId).addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                            snapshot.getRef().removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful()) {
                                                        Toast.makeText(FriendActivity.this, "Friend deleted", Toast.LENGTH_SHORT).show();
                                                    } else {
                                                        Toast.makeText(FriendActivity.this, "Failed to delete friend", Toast.LENGTH_SHORT).show();
                                                    }
                                                }
                                            });
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {
                                        Toast.makeText(FriendActivity.this, "Failed to delete friend: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                            } else {
                                Toast.makeText(FriendActivity.this, "Failed to delete friend", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(FriendActivity.this, "Failed to delete friend: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
