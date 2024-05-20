package com.example.ggm;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Switch;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;


import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class GooglemapActivity extends AppCompatActivity implements OnMapReadyCallback {
    private GoogleMap mMap;
    private Button btnRestaurant, btnHotel, btnFriends, btnDiaHinh,btnFriend, btnCurrentLocation, btnVoice,btn_Notification,btnUser;
    private final int REQUEST_CODE_GPS_PERMISSION = 1;
    private FusedLocationProviderClient mFusedLocationClient;
    private Location currentLocation;
    private Marker localMarker;
    private SearchView mapSearchView;
    private Marker currentMarker;
    private Switch swtShareLocation;
    private ArrayList<String> searchHistoryList = new ArrayList<>();
    private String currentLocationString; // Biến chứa thông tin vị trí hiện tại dưới dạng chuỗi "Latitude,Longitude"
    public String user = LoginActivity.getUserId();
    private Button btn_Map;
    private Button btn_SearchFriend;
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.googlemap_layout);
        mapSearchView = findViewById(R.id.mapSearch);
        btnDiaHinh = findViewById(R.id.btnDiaHinh);
        btn_Map=findViewById(R.id.btn_Map);
        btn_Notification=findViewById(R.id.btn_Notification);
        btnCurrentLocation = findViewById(R.id.btnCurrentLocation);
        btnRestaurant = findViewById(R.id.button2);
        btnFriend=findViewById(R.id.btn_Friend);
        btnHotel = findViewById(R.id.button3);
        btnFriends = findViewById(R.id.button4);
        btnUser=findViewById(R.id.btn_User);
        btn_SearchFriend=findViewById(R.id.btn_SearchFriend);
        ImageButton btnVoice = findViewById(R.id.btn_voice);
        registerForContextMenu(btnDiaHinh);

        swtShareLocation=findViewById(R.id.swt_shareLocation);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        getLastLocation();
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference usersRef = database.getReference("users");
        LoginActivity.incrementLogin();
        btnUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(GooglemapActivity.this, UserInfoActivity.class);
                startActivity(intent);
            }
        });
        btn_Map.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(GooglemapActivity.this, GooglemapActivity.class);
                startActivity(intent);
            }
        });
        btnFriend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(GooglemapActivity.this, FriendActivity.class);
                startActivity(intent);
            }
        });
        btn_Notification.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(GooglemapActivity.this, NotificationActivity.class);
                startActivity(intent);
            }
        });
        btn_SearchFriend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(GooglemapActivity.this, SearchFriendActivity.class);
                startActivity(intent);
            }
        });
        // Xử lý sự kiện khi nhấn vào nút "Nhà hàng"
        btnRestaurant.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showNearbyPlaces("restaurant");
            }
        });

        // Xử lý sự kiện khi nhấn vào nút "Mở lịch sử tìm kiếm"
        Button btnOpenSearchHistory = findViewById(R.id.btnOpenSearchHistory);
        btnOpenSearchHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openSearchHistory();
            }
        });

        // Xử lý sự kiện khi nhấn vào nút "Khách sạn"
        btnHotel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showNearbyPlaces("lodging");
            }
        });

        // Xử lý sự kiện khi nhấn vào nút "Bạn bè"
        btnFriends.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showFriendsPopupMenu();
                if (currentLocation != null) {
                    showFriendsOnMap();
                } else {
                    Toast.makeText(GooglemapActivity.this, "Current location is not available", Toast.LENGTH_SHORT).show();
                }
                if (mMap != null) {
                    DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference().child("users").child(user).child("friends");
                }
                usersRef.child(user).child("friends").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        // Duyệt qua từng node con trong danh sách bạn bè
                        for (DataSnapshot friendSnapshot : dataSnapshot.getChildren()) {
                            // Lấy giá trị của id, latitude và longitude từ mỗi node bạn bè
                            String friendName = friendSnapshot.getKey(); // Tên của bạn bè
                            String friendId = friendSnapshot.child("id").getValue(String.class); // Id của bạn bè
                            double latitude = friendSnapshot.child("latitude").getValue(Double.class); // Latitude của bạn bè
                            double longitude = friendSnapshot.child("longitude").getValue(Double.class);
                            // Longitude của bạn bè
                            if (latitude != 0 && longitude != 0) {
                                LatLng friendLatLng = new LatLng(latitude, longitude);
                                mMap.addMarker(new MarkerOptions().position(friendLatLng).title(friendId).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
                            }// Sử dụng các giá trị đã lấy được ở đây (ví dụ: in ra console)
                            Log.d("FirebaseData", "Friend Name: " + friendName);
                            Log.d("FirebaseData", "Friend Id: " + friendId);
                            Log.d("FirebaseData", "Latitude: " + latitude);
                            Log.d("FirebaseData", "Longitude: " + longitude);

                            // Đối với mỗi bạn bè, bạn có thể thực hiện các hành động khác ở đây
                        }
                    }

                    public void onCancelled(DatabaseError databaseError) {
                        // Xử lý lỗi nếu có
                    }
                });
            }

        });


        // Xử lý sự kiện khi nhấn vào nút "Vị trí hiện tại"
        btnCurrentLocation.setOnClickListener(v -> {
            if (currentLocation != null && mMap != null) {
                LatLng currentLatLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
                if (currentMarker != null) {
                    currentMarker.remove();
                }
                currentMarker = mMap.addMarker(new MarkerOptions().position(currentLatLng).title("Vị trí của bạn").icon(BitmapDescriptorFactory.fromBitmap(getBitmapFromVectorDrawable(
                        getApplicationContext(),
                        R.drawable.yourlocate))));
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 25));
            }
        });

        // Xử lý sự kiện khi nhấn vào nút "Voice"
        btnVoice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startVoiceInput();
            }
        });

        // Xử lý tìm kiếm khi nhập từ khóa vào SearchView
        mapSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                String location = mapSearchView.getQuery().toString();
                List<Address> addressList = null;
                if (location != null) {
                    Geocoder geocoder = new Geocoder(GooglemapActivity.this);
                    try {
                        addressList = geocoder.getFromLocationName(location, 1);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    if (addressList != null && addressList.size() > 0) {
                        Address address = addressList.get(0);
                        LatLng latLng = new LatLng(address.getLatitude(), address.getLongitude());

                        moveMarkerToLocation(latLng, location);

                        showDirectionsDialog(latLng);

                        updateSearchHistory(location);
                    } else {
                        Toast.makeText(GooglemapActivity.this, "Không tìm thấy địa điểm", Toast.LENGTH_SHORT).show();
                    }
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                return false;
            }
        });
        swtShareLocation.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    // Switch is enabled - update user location to current location

                    if (currentLocation != null) {
                        updateUserLocationToZero();
                        buttonView.setBackgroundColor(getResources().getColor(com.google.android.libraries.places.R.color.quantum_googred));

                    }
                } else {
                    // Switch is disabled - update user location to 0,0
                    if (currentLocation != null) {
                        updateUserLocation(currentLocation);
                        buttonView.setBackgroundColor(getResources().getColor(com.google.android.libraries.places.R.color.quantum_googgreen));
                    }
                }
            }
        });
    }

    // Hàm để di chuyển đánh dấu đến vị trí mới
    private void moveMarkerToLocation(LatLng latLng, String title) {
        if (localMarker != null) {
            localMarker.remove();
        }
        localMarker = mMap.addMarker(new MarkerOptions().position(latLng).title(title));
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
    }

    // Hàm để hiển thị hộp thoại hỏi người dùng có muốn điều hướng tới địa điểm không
    private void showDirectionsDialog(final LatLng destinationLatLng) {
        if (currentLocation == null) {
            Toast.makeText(this, "Không thể xác định vị trí hiện tại", Toast.LENGTH_SHORT).show();
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(GooglemapActivity.this);
        builder.setMessage("Bạn có muốn điều hướng tới địa điểm này không?")
                .setPositiveButton("Có", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        float[] results = new float[1];
                        Location.distanceBetween(
                                currentLocation.getLatitude(), currentLocation.getLongitude(),
                                destinationLatLng.latitude, destinationLatLng.longitude,
                                results);
                        double distance = results[0] / 1000;

                        // Mở ứng dụng Google Maps và hiển thị đường đi
                        Uri gmmIntentUri = Uri.parse("google.navigation:q=" + destinationLatLng.latitude + "," + destinationLatLng.longitude);
                        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                        mapIntent.setPackage("com.google.android.apps.maps");
                        if (mapIntent.resolveActivity(getPackageManager()) != null) {
                            startActivity(mapIntent);
                        } else {
                            Toast.makeText(GooglemapActivity.this, "Không thể mở ứng dụng bản đồ", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .setNegativeButton("Hủy", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        float[] results = new float[1];
                        Location.distanceBetween(
                                currentLocation.getLatitude(), currentLocation.getLongitude(),
                                destinationLatLng.latitude, destinationLatLng.longitude,
                                results);
                        double distance = results[0] / 1000;

                        AlertDialog.Builder distanceAlertBuilder = new AlertDialog.Builder(GooglemapActivity.this);
                        distanceAlertBuilder.setMessage("Khoảng cách từ vị trí hiện tại đến địa điểm là " + distance + "km")
                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                    }
                                });
                        AlertDialog distanceAlert = distanceAlertBuilder.create();
                        distanceAlert.show();
                    }
                });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void getLastLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE_GPS_PERMISSION);
            return;
        }
        Task<Location> task = mFusedLocationClient.getLastLocation();
        task.addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null) {
                    currentLocation = location;
                    currentLocationString = getCurrentLocationString(); // Cập nhật giá trị của biến currentLocationString
                    SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
                    mapFragment.getMapAsync(GooglemapActivity.this);
                    updateUserLocation(location);
                }
            }
        });
    }

    // Phương thức để lấy thông tin vị trí hiện tại dưới dạng chuỗi "Latitude,Longitude"
    private String getCurrentLocationString() {
        if (currentLocation != null) {
            double latitude = currentLocation.getLatitude();
            double longitude = currentLocation.getLongitude();
            return String.format("%f,%f", latitude, longitude);
        } else {
            return null;
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if (currentLocation != null) {
            LatLng currentLatLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
            if (currentMarker != null) {
                currentMarker.remove();
            }
            currentMarker = mMap.addMarker(new MarkerOptions()
                    .position(currentLatLng)
                    .title(user)
                    .icon(BitmapDescriptorFactory.fromBitmap(getBitmapFromVectorDrawable(
                            getApplicationContext(),
                            R.drawable.yourlocate))));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15));

                updateUserLocation(currentLocation);

            mMap.getUiSettings().setZoomControlsEnabled(true);
            mMap.getUiSettings().setCompassEnabled(true);
        }
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference usersRef = database.getReference("users");
        usersRef.child(user).child("friends").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Duyệt qua từng node con trong danh sách bạn bè
                for (DataSnapshot friendSnapshot : dataSnapshot.getChildren()) {
                    // Lấy giá trị của id, latitude và longitude từ mỗi node bạn bè
                    String friendName = friendSnapshot.getKey(); // Tên của bạn bè
                    String friendId = friendSnapshot.child("id").getValue(String.class); // Id của bạn bè
                    double latitude = friendSnapshot.child("latitude").getValue(Double.class); // Latitude của bạn bè
                    double longitude = friendSnapshot.child("longitude").getValue(Double.class);
                    // Longitude của bạn bè
                    if (latitude != 0 && longitude != 0) {
                        LatLng friendLatLng = new LatLng(latitude, longitude);
                        mMap.addMarker(new MarkerOptions().position(friendLatLng).title(friendId).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
                    }// Sử dụng các giá trị đã lấy được ở đây (ví dụ: in ra console)
                    Log.d("FirebaseData", "Friend Name: " + friendName);
                    Log.d("FirebaseData", "Friend Id: " + friendId);
                    Log.d("FirebaseData", "Latitude: " + latitude);
                    Log.d("FirebaseData", "Longitude: " + longitude);

                    // Đối với mỗi bạn bè, bạn có thể thực hiện các hành động khác ở đây
                }
            }
            public void onCancelled(DatabaseError databaseError) {
                // Xử lý lỗi nếu có
            }
        });
    }

    public Bitmap getBitmapFromVectorDrawable(Context context, int drawableId) {
        Drawable drawable = ContextCompat.getDrawable(context, drawableId);
        if (drawable == null) return null;
        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }

    private void showNearbyPlaces(String type) {
        Uri gmmIntentUri = Uri.parse("geo:0,0?q=" + type);
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        mapIntent.setPackage("com.google.android.apps.maps");
        if (mapIntent.resolveActivity(getPackageManager()) != null) {
            startActivity(mapIntent);
        } else {
            Toast.makeText(this, "Không tìm thấy ứng dụng Google Maps", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_GPS_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLastLocation();
            } else {
                Toast.makeText(this, "Từ chối cấp quyền vị trí, vui lòng cấp quyền", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void openSearchHistory() {
        if (searchHistoryList.isEmpty()) {
            Toast.makeText(this, "Không có lịch sử tìm kiếm gần đây", Toast.LENGTH_SHORT).show();
            return;
        }

        // Tạo một RecyclerView
        RecyclerView recyclerView = new RecyclerView(this);

        // Set layout manager
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        // Tạo và đặt adapter
        SearchHistoryAdapter adapter = new SearchHistoryAdapter(searchHistoryList, new SearchHistoryAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(String item) {
                mapSearchView.setQuery(item, true);
                mapSearchView.clearFocus();
                performSearch(item);
            }
        });
        recyclerView.setAdapter(adapter);

        // Thêm item decoration
        recyclerView.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
                int position = parent.getChildAdapterPosition(view);
                if (position != RecyclerView.NO_POSITION && position != parent.getAdapter().getItemCount() - 1) {
                    int verticalSpacing = calculateVerticalSpacing(parent, position);
                    outRect.bottom = verticalSpacing; // Đặt khoảng cách giữa các item
                }
            }
        });

        // Tạo AlertDialog.Builder
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        // Đặt tiêu đề
        builder.setTitle("Lịch sử tìm kiếm");

        // Đặt view cho RecyclerView
        builder.setView(recyclerView);

        // Thêm nút positive
        builder.setPositiveButton("Đóng", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        // Hiển thị AlertDialog
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private int calculateVerticalSpacing(RecyclerView parent, int position) {
        RecyclerView.LayoutManager layoutManager = parent.getLayoutManager();
        if (layoutManager instanceof LinearLayoutManager) {
            LinearLayoutManager linearLayoutManager = (LinearLayoutManager) layoutManager;
            int height = parent.getHeight();
            int itemCount = parent.getAdapter().getItemCount();
            int lastVisiblePosition = linearLayoutManager.findLastVisibleItemPosition();

            View currentView = parent.getChildAt(position);
            int currentViewHeight = currentView.getHeight();

            int remainingHeight = height - currentViewHeight * (position + 1);
            int remainingItemCount = itemCount - position - 1;
            int spacing = remainingHeight / remainingItemCount;

            if (position == lastVisiblePosition) {
                spacing = 0; // Đặt khoảng cách là 0 cho item cuối cùng
            }

            return spacing;
        }

        return 0;
    }

    private void performSearch(String query) {
        // Xử lý logic tìm kiếm ở đây
    }

    private void updateSearchHistory(String location) {
        if (searchHistoryList.contains(location)) {
            searchHistoryList.remove(location);
        } else if (searchHistoryList.size() >= 5) {
            searchHistoryList.remove(0);
        }
        searchHistoryList.add(location);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        getMenuInflater().inflate(R.menu.menu_dia_hinh, menu);
    }

    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.macdinh) {
            // Xử lý khi người dùng chọn mục mặc định
            if (mMap != null) {
                mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
            }
            Toast.makeText(this, "Mặc định", Toast.LENGTH_SHORT).show();
            return true;
        } else if (itemId == R.id.vetinh) {
            if (mMap != null) {
                mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
            }
            // Xử lý khi người dùng chọn mục vệ tinh
            Toast.makeText(this, "Vệ tinh", Toast.LENGTH_SHORT).show();
            return true;
        } else if (itemId == R.id.diahinh) {
            // Xử lý khi người dùng chọn mục địa hình
            if (mMap != null) {
                mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
            }
            Toast.makeText(this, "Địa hình", Toast.LENGTH_SHORT).show();
            return true;
        } else {
            return super.onContextItemSelected(item);
        }
    }

    // Phương thức để bắt đầu nhập giọng nói từ người dùng
    private void startVoiceInput() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Hãy nói điều gì đó...");

        try {
            startActivityForResult(intent, REQUEST_CODE_SPEECH_INPUT);
        } catch (Exception e) {
            Toast.makeText(this, "Thiết bị của bạn không hỗ trợ chức năng này", Toast.LENGTH_SHORT).show();
        }
    }

    // Hằng số để xác định mã yêu cầu cho nhập giọng nói
    private static final int REQUEST_CODE_SPEECH_INPUT = 1000;

    // Phương thức này được gọi sau khi người dùng hoàn thành nhập giọng nói
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_SPEECH_INPUT) {
            if (resultCode == RESULT_OK && null != data) {
                ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                if (result != null && result.size() > 0) {
                    String spokenText = result.get(0);
                    mapSearchView.setQuery(spokenText, true);
                }
            }
        }
    }
    // Hàm cập nhật vị trí người dùng trong Firebase
    private void updateUserLocation(Location location) {
        String userId = user; // Giả sử user là biến chứa ID người dùng hiện tại

        // Cập nhật vị trí người dùng hiện tại
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("users").child(userId);
        userRef.child("latitude").setValue(location.getLatitude());
        userRef.child("longitude").setValue(location.getLongitude());

        // Cập nhật vị trí cho các bạn bè có ID giống với userId
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference().child("users");
        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                    String friendUserId = userSnapshot.getKey();
                    for (DataSnapshot friendSnapshot : userSnapshot.child("friends").getChildren()) {
                        String friendId = friendSnapshot.child("id").getValue(String.class);
                        if (userId.equals(friendId)) {
                            userSnapshot.getRef().child("friends").child(friendSnapshot.getKey()).child("latitude").setValue(location.getLatitude());
                            userSnapshot.getRef().child("friends").child(friendSnapshot.getKey()).child("longitude").setValue(location.getLongitude());
                        }
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle possible errors
            }
        });
    }

    private void updateUserLocationToZero() {
        String userId = user; // Giả sử user là biến chứa ID người dùng hiện tại

        // Cập nhật vị trí người dùng hiện tại
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("users").child(userId);
        userRef.child("latitude").setValue(0);
        userRef.child("longitude").setValue(0);

        // Cập nhật vị trí cho các bạn bè có ID giống với userId
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference().child("users");
        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                    String friendUserId = userSnapshot.getKey();
                    for (DataSnapshot friendSnapshot : userSnapshot.child("friends").getChildren()) {
                        String friendId = friendSnapshot.child("id").getValue(String.class);
                        if (userId.equals(friendId)) {
                            userSnapshot.getRef().child("friends").child(friendSnapshot.getKey()).child("latitude").setValue(0);
                            userSnapshot.getRef().child("friends").child(friendSnapshot.getKey()).child("longitude").setValue(0);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle possible errors
            }
        });
    }
    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        LatLng from = new LatLng(lat1, lon1);
        LatLng to = new LatLng(lat2, lon2);

        // Sử dụng phương thức `distanceBetween` của thư viện Google Maps API để tính khoảng cách
        float[] distance = new float[1];
        Location.distanceBetween(from.latitude, from.longitude, to.latitude, to.longitude, distance);

        // Khoảng cách tính bằng mét, chuyển đổi thành kilômét
        return distance[0] / 1000; // Chuyển đổi từ mét sang kilômét
    }

    private void showFriendsOnMap() {
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("users").child(user);
        userRef.child("friends").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot friendSnapshot : dataSnapshot.getChildren()) {
                    String friendId = friendSnapshot.child("id").getValue(String.class);
                    double friendLat = friendSnapshot.child("latitude").getValue(Double.class);
                    double friendLon = friendSnapshot.child("longitude").getValue(Double.class);

                    // Kiểm tra nếu lat và lon của bạn bè không phải là 0
                    if (friendLat != 0 && friendLon != 0) {
                        // Tính khoảng cách từ vị trí hiện tại đến bạn bè
                        double distance = calculateDistance(currentLocation.getLatitude(), currentLocation.getLongitude(), friendLat, friendLon);

                        // Thêm marker lên bản đồ
                        LatLng friendLatLng = new LatLng(friendLat, friendLon);
                        Marker marker = mMap.addMarker(new MarkerOptions().position(friendLatLng).title(friendId).snippet("Distance: " + distance + " kilometers"));

                        // Lưu thông tin marker để xử lý sự kiện click
                        marker.setTag(friendId);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle possible errors
            }
        });

        // Xử lý sự kiện click vào marker
        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                LatLng position = marker.getPosition();
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(position, 15));
                return false; // Return false để hiển thị info window
            }
        });
    }

    private void showFriendsPopupMenu() {
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("users").child(user);
        userRef.child("friends").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ArrayList<String> friendNames = new ArrayList<>();
                HashMap<String, LatLng> friendsLocations = new HashMap<>();

                for (DataSnapshot friendSnapshot : dataSnapshot.getChildren()) {
                    String friendId = friendSnapshot.child("id").getValue(String.class);
                    double friendLat = friendSnapshot.child("latitude").getValue(Double.class);
                    double friendLon = friendSnapshot.child("longitude").getValue(Double.class);

                    // Kiểm tra nếu lat và lon của bạn bè không phải là 0
                    if (friendLat != 0 && friendLon != 0) {
                        friendNames.add(friendId);
                        friendsLocations.put(friendId, new LatLng(friendLat, friendLon));
                    }
                }

                // Nếu không có bạn bè nào chia sẻ vị trí, hiển thị thông báo
                if (friendNames.isEmpty()) {
                    Toast.makeText(GooglemapActivity.this, "Bạn bè hiện chưa chia sẻ vị trí", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Tạo và hiển thị popup menu
                AlertDialog.Builder builder = new AlertDialog.Builder(GooglemapActivity.this);
                LayoutInflater inflater = getLayoutInflater();
                @SuppressLint("ResourceType") View convertView = inflater.inflate(R.menu.popup_friends, null);
                builder.setView(convertView);

                ListView listView = convertView.findViewById(R.id.listViewFriends);
                ArrayAdapter<String> adapter = new ArrayAdapter<>(GooglemapActivity.this, android.R.layout.simple_list_item_1, friendNames);
                listView.setAdapter(adapter);

                AlertDialog dialog = builder.create();
                dialog.show();

                // Xử lý sự kiện click vào item trong danh sách
                listView.setOnItemClickListener((parent, view, position, id) -> {
                    String selectedFriend = friendNames.get(position);
                    LatLng friendLatLng = friendsLocations.get(selectedFriend);

                    if (friendLatLng != null) {
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(friendLatLng, 15));
                    }
                    dialog.dismiss();
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle possible errors
            }
        });
    }

}
