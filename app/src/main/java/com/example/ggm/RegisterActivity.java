package com.example.ggm;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
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
    private EditText eDT_userRegister, eDT_passRegister, eDT_conFirmPassWord, eDT_PhoneNumber;
    private Button btn_Register,btn_Login,btn_Back;
    private DatabaseReference mDatabase;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register);

        mDatabase = FirebaseDatabase.getInstance().getReference().child("users");
        btn_Login=findViewById(R.id.btn_Login);
        btn_Back=findViewById(R.id.btn_Back);
        eDT_userRegister = findViewById(R.id.eDT_userRegister);
        eDT_passRegister = findViewById(R.id.eDT_passRegister);
        eDT_conFirmPassWord = findViewById(R.id.eDT_ConfirmPass);
        btn_Register = findViewById(R.id.btn_Register);
        eDT_PhoneNumber = findViewById(R.id.eDT_PhoneNumber);
        // Ẩn mật khẩu và xác nhận mật khẩu
        eDT_passRegister.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        eDT_conFirmPassWord.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        btn_Back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });
        btn_Login.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });
        btn_Register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String username = eDT_userRegister.getText().toString().trim();
                final String password = eDT_passRegister.getText().toString().trim();
                final String confirmPassword = eDT_conFirmPassWord.getText().toString().trim();
                final String phoneNumber = eDT_PhoneNumber.getText().toString().trim(); // Lấy số điện thoại từ EditText

                if (TextUtils.isEmpty(username) || TextUtils.isEmpty(password) || TextUtils.isEmpty(confirmPassword) || TextUtils.isEmpty(phoneNumber)) {
                    Toast.makeText(RegisterActivity.this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Kiểm tra mật khẩu và xác nhận mật khẩu
                if (!password.equals(confirmPassword)) {
                    Toast.makeText(RegisterActivity.this, "Mật khẩu và xác nhận mật khẩu không khớp", Toast.LENGTH_SHORT).show();
                    eDT_passRegister.setText("");
                    eDT_conFirmPassWord.setText("");
                    return;
                }

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
                            mDatabase.child(userId).child("phoneNumber").setValue(phoneNumber); // Lưu số điện thoại vào dữ liệu của người dùng
                            mDatabase.child(userId).child("latitude").setValue(0);
                            mDatabase.child(userId).child("longitude").setValue(0)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                Toast.makeText(RegisterActivity.this, "Đăng ký thành công", Toast.LENGTH_SHORT).show();
                                                Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                                                startActivity(intent);
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
            }
        });

    }
}
