package com.example.traveling.travelshare;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.traveling.R;

public class NotificationActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ts_activity_notifications);

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        // Simulation de sauvegarde des préférences
        findViewById(R.id.switch_group_post).setOnClickListener(v -> 
            Toast.makeText(this, "Préférence mise à jour", Toast.LENGTH_SHORT).show());
    }
}
