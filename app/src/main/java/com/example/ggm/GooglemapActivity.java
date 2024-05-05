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
import android.view.View;
import android.widget.Button;
import android.widget.SearchView;
import android.widget.Toast;

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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GooglemapActivity extends AppCompatActivity implements OnMapReadyCallback {
    private GoogleMap mMap;
    private Button btnRestaurant, btnHotel, btnFriends;
    private final int REQUEST_CODE_GPS_PERMISSION = 1;
    private FusedLocationProviderClient mFusedLocationClient;
    private Location currentLocation;
    private Marker localMarker;
    private SearchView mapSearchView;
    private Button btnCurrentLocation;
    private Marker currentMarker;
    private ArrayList<String> searchHistoryList = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.googlemap_layout);
        mapSearchView = findViewById(R.id.mapSearch);
        btnCurrentLocation = findViewById(R.id.btnCurrentLocation);
        btnRestaurant = findViewById(R.id.button2);
        btnHotel = findViewById(R.id.button3);
        btnFriends = findViewById(R.id.button4);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        getLastLocation();
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        btnRestaurant.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showNearbyPlaces("restaurant");
            }
        });

        Button btnOpenSearchHistory = findViewById(R.id.btnOpenSearchHistory);
        btnOpenSearchHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openSearchHistory();
            }
        });

        btnHotel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showNearbyPlaces("lodging");
            }
        });

        btnFriends.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Your logic to show nearby friends
            }
        });

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
                        Toast.makeText(GooglemapActivity.this, "Location not found", Toast.LENGTH_SHORT).show();
                    }
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                return false;
            }
        });

        btnCurrentLocation.setOnClickListener(v -> {
            if (currentLocation != null && mMap != null) {
                LatLng currentLatLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
                if (currentMarker != null) {
                    currentMarker.remove();
                }
                currentMarker = mMap.addMarker(new MarkerOptions().position(currentLatLng).title("Your Location").icon(BitmapDescriptorFactory.fromBitmap(getBitmapFromVectorDrawable(
                        getApplicationContext(),
                        R.drawable.yourlocate))));
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 25));
            }
        });
    }

    private void moveMarkerToLocation(LatLng latLng, String title) {
        if (localMarker != null) {
            localMarker.remove();
        }
        localMarker = mMap.addMarker(new MarkerOptions().position(latLng).title(title));
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
    }

    private void showDirectionsDialog(final LatLng destinationLatLng) {
        if (currentLocation == null) {
            Toast.makeText(this, "Cannot determine current location", Toast.LENGTH_SHORT).show();
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(GooglemapActivity.this);
        builder.setMessage("Do you want directions to this location?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        float[] results = new float[1];
                        Location.distanceBetween(
                                currentLocation.getLatitude(), currentLocation.getLongitude(),
                                destinationLatLng.latitude, destinationLatLng.longitude,
                                results);
                        double distance = results[0] / 1000;

                        Uri gmmIntentUri = Uri.parse("google.navigation:q=" + destinationLatLng.latitude + "," + destinationLatLng.longitude);
                        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                        mapIntent.setPackage("com.google.android.apps.maps");
                        if (mapIntent.resolveActivity(getPackageManager()) != null) {
                            startActivity(mapIntent);
                        } else {
                            Toast.makeText(GooglemapActivity.this, "Unable to open map app", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        float[] results = new float[1];
                        Location.distanceBetween(
                                currentLocation.getLatitude(), currentLocation.getLongitude(),
                                destinationLatLng.latitude, destinationLatLng.longitude,
                                results);
                        double distance = results[0] / 1000;

                        AlertDialog.Builder distanceAlertBuilder = new AlertDialog.Builder(GooglemapActivity.this);
                        distanceAlertBuilder.setMessage("The distance from your current location to the location is " + distance + "km")
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
                    SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
                    mapFragment.getMapAsync(GooglemapActivity.this);
                }
            }
        });
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
                    .title("Your current location")
                    .icon(BitmapDescriptorFactory.fromBitmap(getBitmapFromVectorDrawable(
                            getApplicationContext(),
                            R.drawable.yourlocate))));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15));
            mMap.getUiSettings().setZoomControlsEnabled(true);
            mMap.getUiSettings().setCompassEnabled(true);
        }
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
            Toast.makeText(this, "Google Maps app not found", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_GPS_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLastLocation();
            } else {
                Toast.makeText(this, "Location permission denied, please grant permission", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void openSearchHistory() {
        if (searchHistoryList.isEmpty()) {
            Toast.makeText(this, "Không có lịch sử tìm kiếm gần đây", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create a RecyclerView
        RecyclerView recyclerView = new RecyclerView(this);

        // Set layout manager
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        // Create and set adapter
        SearchHistoryAdapter adapter = new SearchHistoryAdapter(searchHistoryList, new SearchHistoryAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(String item) {
                mapSearchView.setQuery(item, true);
                mapSearchView.clearFocus();
                performSearch(item);
            }
        });
        recyclerView.setAdapter(adapter);

        // Add item decoration
        recyclerView.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
                int position = parent.getChildAdapterPosition(view);
                if (position != RecyclerView.NO_POSITION && position != parent.getAdapter().getItemCount() - 1) {
                    int verticalSpacing = calculateVerticalSpacing(parent, position);
                    outRect.bottom = verticalSpacing; // Set spacing between items
                }
            }
        });

        // Create AlertDialog.Builder
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        // Set title
        builder.setTitle("Lịch sử tìm kiếm");

        // Set view to RecyclerView
        builder.setView(recyclerView);

        // Add positive button
        builder.setPositiveButton("Close", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        // Show AlertDialog
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
                spacing = 0; // Set spacing to 0 for the last item
            }

            return spacing;
        }

        return 0;
    }

    private void performSearch(String query) {
        // Handle search logic here
    }

    private void updateSearchHistory(String location) {
        if (searchHistoryList.contains(location)) {
            searchHistoryList.remove(location);
        } else if (searchHistoryList.size() >= 5) {
            searchHistoryList.remove(0);
        }
        searchHistoryList.add(location);
    }
}
