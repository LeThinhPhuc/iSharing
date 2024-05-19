package com.example.ggm;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class RegisterActivity extends AppCompatActivity {
    private EditText eDT_userRegister, eDT_passRegister;
    private Button btn_Register;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register);

        mDatabase = FirebaseDatabase.getInstance().getReference().child("users");

        eDT_userRegister = findViewById(R.id.eDT_userRegister);
        eDT_passRegister = findViewById(R.id.eDT_passRegister);
        btn_Register = findViewById(R.id.btn_Register);

        btn_Register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String username = eDT_userRegister.getText().toString().trim();
                final String password = eDT_passRegister.getText().toString().trim();

                if (!username.isEmpty() && !password.isEmpty()) {
                    final String userId = username; // Lấy ID của người dùng từ tên người dùng

                    mDatabase.child(userId).child("id").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {
                                // Id đã tồn tại, thông báo cho người dùng
                                Toast.makeText(RegisterActivity.this, "ID đã được sử dụng", Toast.LENGTH_SHORT).show();
                            } else {
                                // Id chưa tồn tại, tiến hành tạo người dùng mới
                                mDatabase.child(userId).child("id").setValue(userId);
                                mDatabase.child(userId).child("password").setValue(password);
                                mDatabase.child(userId).child("latitude").setValue(0);
                                mDatabase.child(userId).child("longitude").setValue(0)
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    Toast.makeText(RegisterActivity.this, "Đăng ký thành công", Toast.LENGTH_SHORT).show();
                                                } else {
                                                    Toast.makeText(RegisterActivity.this, "Lỗi khi đăng ký", Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            // Xử lý lỗi nếu cần
                        }
                    });
                } else {
                    Toast.makeText(RegisterActivity.this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }
}
