package com.example.ggm;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button buttonGoogleMap = findViewById(R.id.button_google_map);
        buttonGoogleMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Tạo Intent để chuyển đến layout mới
                Intent intent = new Intent(MainActivity.this, GooglemapActivity.class);
                startActivity(intent);
            }
        });
    }
}
