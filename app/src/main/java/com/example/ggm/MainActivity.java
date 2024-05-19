package com.example.ggm;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button btnRegister = findViewById(R.id.btn_MoveRegister);

        Button buttonGoogleMap = findViewById(R.id.button_google_map);
        Button btnLogin =findViewById(R.id.btn_MoveLogin);
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference usersRef = database.getReference("users");

        // Tạo dữ liệu cho user1
        HashMap<String, Friend> friendsUser1 = new HashMap<>();
        friendsUser1.put("friend1", new Friend("user2", 21.028511, 105.804817));
        friendsUser1.put("friend2", new Friend("user3", 16.047079, 108.206230));
        friendsUser1.put("luan", new Friend("luan", 16.555555, 108.77777));
        User user1 = new User("user1", 10.762622, 106.660172, "vta12345" , friendsUser1);

        // Tạo dữ liệu cho user2
        HashMap<String, Friend> friendsUser2 = new HashMap<>();
        friendsUser2.put("friend1", new Friend("user1", 10.762622, 106.660172));
        friendsUser2.put("friend2", new Friend("user3", 16.047079, 108.206230));
        User user2 = new User("user2", 21.028511, 105.804817,"12345", friendsUser2);

        // Tạo dữ liệu cho user3
        HashMap<String, Friend> friendsUser3 = new HashMap<>();
        friendsUser3.put("friend1", new Friend("user1", 10.762622, 106.660172));
        friendsUser3.put("friend2", new Friend("user2", 21.028511, 105.804817));
        User user3 = new User("user3", 16.047079, 108.206230,"12345", friendsUser3);

        // Ghi dữ liệu vào Firebase
        usersRef.child(user1.getId()).setValue(user1);
        usersRef.child(user2.getId()).setValue(user2);
        usersRef.child(user3.getId()).setValue(user3);
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });
        buttonGoogleMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Tạo Intent để chuyển đến layout mới
                Intent intent = new Intent(MainActivity.this, GooglemapActivity.class);
                startActivity(intent);

                // Khai báo một reference đến Firebase Database của bạn
                DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference().child("users").child("user1").child("friends");

// Thực hiện lắng nghe sự kiện khi có sự thay đổi dữ liệu trong danh sách bạn bè của user1
                usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        // Duyệt qua từng node con trong danh sách bạn bè
                        for (DataSnapshot friendSnapshot : dataSnapshot.getChildren()) {
                            // Lấy giá trị của id, latitude và longitude từ mỗi node bạn bè
                            String friendName = friendSnapshot.getKey(); // Tên của bạn bè
                            String friendId = friendSnapshot.child("id").getValue(String.class); // Id của bạn bè
                            double latitude = friendSnapshot.child("latitude").getValue(Double.class); // Latitude của bạn bè
                            double longitude = friendSnapshot.child("longitude").getValue(Double.class); // Longitude của bạn bè

                            // Sử dụng các giá trị đã lấy được ở đây (ví dụ: in ra console)
                            Log.d("FirebaseData", "Friend Name: " + friendName);
                            Log.d("FirebaseData", "Friend Id: " + friendId);
                            Log.d("FirebaseData", "Latitude: " + latitude);
                            Log.d("FirebaseData", "Longitude: " + longitude);

                            // Đối với mỗi bạn bè, bạn có thể thực hiện các hành động khác ở đây
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        // Xử lý nếu có lỗi xảy ra trong quá trình đọc dữ liệu từ Firebase
                    }
                });
            }
            });

        }


}
