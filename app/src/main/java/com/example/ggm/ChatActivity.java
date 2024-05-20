package com.example.ggm;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ChatActivity extends AppCompatActivity {

    private static final int REQUEST_CALL_PHONE = 1;
    private String friendId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        // Lấy ID của người nhận từ Intent
        friendId = getIntent().getStringExtra("friendId");

        // Thêm sự kiện click cho button call
        findViewById(R.id.buttonCall).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callFriend();
            }
        });
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
                            // Yêu cầu quyền thực hiện cuộc gọi
                            ActivityCompat.requestPermissions(ChatActivity.this, new String[]{Manifest.permission.CALL_PHONE}, REQUEST_CALL_PHONE);
                        } else {
                            // Thực hiện cuộc gọi
                            startActivity(callIntent);
                        }
                    } else {
                        // Nếu không có số điện thoại, hiển thị thông báo
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

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CALL_PHONE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Thực hiện cuộc gọi nếu đã được cấp quyền
                callFriend();
            } else {
                // Hiển thị thông báo nếu người dùng không cấp quyền
                Toast.makeText(this, "Ứng dụng cần quyền để thực hiện cuộc gọi.", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
