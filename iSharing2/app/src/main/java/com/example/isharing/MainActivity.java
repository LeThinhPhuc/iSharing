package com.example.isharing;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {
    FirebaseFirestore db;
    FirebaseAuth auth;
    ShapeableImageView imageView;
    TextView name, mail;

    GoogleSignInClient googleSignInClient;
    private final ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
        @Override
        public void onActivityResult(ActivityResult result) {
            if(result.getResultCode() ==RESULT_OK){
                Task<GoogleSignInAccount> accountTask = GoogleSignIn.getSignedInAccountFromIntent(result.getData());
                try{
                    GoogleSignInAccount signInAccount = accountTask.getResult(ApiException.class);
                    AuthCredential authCredential= GoogleAuthProvider.getCredential(signInAccount.getIdToken(),null);
                    auth.signInWithCredential(authCredential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful()){
                                auth = FirebaseAuth.getInstance();
                                String uid = Objects.requireNonNull(auth.getCurrentUser()).getUid();
                                db = FirebaseFirestore.getInstance();

                                Map<String, Object> user = new HashMap<>();
                                user.put("Name", auth.getCurrentUser().getDisplayName());
                                user.put("Email", auth.getCurrentUser().getEmail());
                                user.put("Photo", Objects.requireNonNull(auth.getCurrentUser()).getPhotoUrl());
                                db.collection("users").add(user).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                    @Override
                                    public void onSuccess(DocumentReference documentReference) {
                                        Toast.makeText(MainActivity.this, "luu thanh cong goi", Toast.LENGTH_SHORT).show();

                                    }
                                });
                                // Kiểm tra xem tài liệu người dùng đã tồn tại hay chưa
                                db.collection("users").document(uid).get()
                                        .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {

                                            @Override
                                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                                Toast.makeText(MainActivity.this, "cb ne ne them", Toast.LENGTH_SHORT).show();

                                                if (documentSnapshot.exists()) {
                                                    // Người dùng đã tồn tại, có thể cập nhật thông tin nếu cần thiết
                                                    Toast.makeText(MainActivity.this, "cb update", Toast.LENGTH_SHORT).show();

                                                    updateUserInfo(uid, auth.getCurrentUser().getDisplayName(), auth.getCurrentUser().getEmail(), Objects.requireNonNull(auth.getCurrentUser().getPhotoUrl()));
                                                } else {
                                                    // Người dùng chưa tồn tại, lưu thông tin mới
                                Toast.makeText(MainActivity.this, "cb them", Toast.LENGTH_SHORT).show();

                                                    saveUserInfo(uid, auth.getCurrentUser().getDisplayName(), auth.getCurrentUser().getEmail(), Objects.requireNonNull(auth.getCurrentUser().getPhotoUrl()));
                                                }
                                            }
                                        });

                                // Cập nhật giao diện người dùng
                                Glide.with(MainActivity.this).load(Objects.requireNonNull(auth.getCurrentUser()).getPhotoUrl()).into(imageView);
                                name.setText(auth.getCurrentUser().getDisplayName());
                                mail.setText(auth.getCurrentUser().getEmail());
//                                Toast.makeText(MainActivity.this, "Signed in success", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(MainActivity.this, "Fail "+ task.getException(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }catch(ApiException e){
                    e.printStackTrace();
                }
            }
        }
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        FirebaseApp.initializeApp(this);
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        imageView = findViewById(R.id.profileImage);
        name = findViewById(R.id.nameTV);
        mail = findViewById(R.id.mailTV);
        GoogleSignInOptions options = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestIdToken(getString(R.string.client_id)).requestEmail().build();
        googleSignInClient = GoogleSignIn.getClient(MainActivity.this, options);

        SignInButton signInButton = findViewById(R.id.signIn);
        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = googleSignInClient.getSignInIntent();
                activityResultLauncher.launch(intent);
            }
        });

        MaterialButton signOut = findViewById(R.id.signout);
        signOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseAuth.getInstance().addAuthStateListener(new FirebaseAuth.AuthStateListener() {
                    @Override
                    public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                        if (firebaseAuth.getCurrentUser() == null) {
                            googleSignInClient.signOut().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    Toast.makeText(MainActivity.this, "Signed out successfully", Toast.LENGTH_SHORT).show();
                                    startActivity(new Intent(MainActivity.this, MainActivity.class));
                                }
                            });
                        }
                    }
                });
                FirebaseAuth.getInstance().signOut();
            }
        });

        if (auth.getCurrentUser() != null) {
            Glide.with(MainActivity.this).load(Objects.requireNonNull(auth.getCurrentUser()).getPhotoUrl()).into(imageView);
            name.setText(auth.getCurrentUser().getDisplayName());
            mail.setText(auth.getCurrentUser().getEmail());
        }
    }

    // Hàm để lưu thông tin người dùng vào Firestore lần đầu tiên
    private void saveUserInfo(String uid, String displayName, String email, Uri photoUrl) {
        Map<String, Object> user = new HashMap<>();
        user.put("Name", displayName);
        user.put("Email", email);
        user.put("Photo", photoUrl.toString());
        // Thêm các thông tin khác của người dùng nếu cần

        db.collection("users").document(uid)
                .set(user)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // Thành công, hiển thị thông tin của người dùng và các hành động khác
                        Toast.makeText(MainActivity.this, "Save successfully", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Lỗi khi lưu thông tin người dùng vào Firestore
                        Toast.makeText(MainActivity.this, "Save fail", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // Hàm để cập nhật thông tin người dùng trong Firestore
    private void updateUserInfo(String uid, String displayName, String email, Uri photoUrl) {
        DocumentReference userRef = db.collection("users").document(uid);

        // Cập nhật thông tin người dùng trong tài liệu
        userRef.update("Name", displayName,
                        "Email", email,
                        "Photo", photoUrl.toString())
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // Thành công, hiển thị thông tin của người dùng và các hành động khác
                        Toast.makeText(MainActivity.this, "Update successfully", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Lỗi khi cập nhật thông tin người dùng trong Firestore
                        Toast.makeText(MainActivity.this, "Update Fail", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
