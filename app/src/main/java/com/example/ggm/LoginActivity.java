package com.example.ggm;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
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
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class LoginActivity extends AppCompatActivity {
    private EditText eDT_User, eDT_Pass;
    private Button btn_Login, btn_Register;
    private DatabaseReference mDatabase;
    private static String userId; // Khai báo userId là static
    private static int login;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);
        login=0;

        mDatabase = FirebaseDatabase.getInstance().getReference().child("users");

        eDT_User = findViewById(R.id.eDT_User);
        eDT_Pass = findViewById(R.id.eDT_Pass);
        btn_Login = findViewById(R.id.btn_Login);
        eDT_Pass.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        btn_Login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String username = eDT_User.getText().toString().trim();
                final String password = eDT_Pass.getText().toString().trim();

                if (!username.isEmpty() && !password.isEmpty()) {
                    Query query = mDatabase.orderByChild("id").equalTo(username);
                    query.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {
                                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                    String savedPassword = snapshot.child("password").getValue(String.class);
                                    if (savedPassword != null && savedPassword.equals(password)) {
                                        userId = username; // Gán giá trị cho userId
                                        Toast.makeText(LoginActivity.this, "Đăng nhập thành công", Toast.LENGTH_SHORT).show();
                                        Intent intent = new Intent(LoginActivity.this, GooglemapActivity.class);
                                        intent.putExtra("USER_ID", userId);
                                        startActivity(intent);
                                    } else {
                                        Toast.makeText(LoginActivity.this, "Sai mật khẩu", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            } else {
                                Toast.makeText(LoginActivity.this, "Tài khoản không tồn tại", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            Toast.makeText(LoginActivity.this, "Lỗi đăng nhập", Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    Toast.makeText(LoginActivity.this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    // Phương thức static để truy cập userId từ một lớp khác
    public static String getUserId() {
        return userId;
    }

    public static void incrementLogin() {
        login++;
    }

    public static int getLogin() {
        return login;
    }
}
