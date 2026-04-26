package com.example.traveling.travelpath;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.traveling.R;
import java.util.ArrayList;

public class PlaceListActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tp_activity_place_list);

        // Retrieve data from intent
        String mode = getIntent().getStringExtra("extra_mode");
        ArrayList<Place> places = getIntent()
                .getParcelableArrayListExtra("extra_places");

        // Toolbar title
        TextView title = findViewById(R.id.text_place_list_title);
        title.setText(mode != null ? mode : "Lieux");

        // RecyclerView
        RecyclerView recyclerView = findViewById(R.id.recycler_places);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        PlaceAdapter adapter = new PlaceAdapter(
                places != null ? places : new ArrayList<>(),
                new PlaceAdapter.PlaceSelectionListener() {
                    @Override
                    public void onPlaceSelected(Place place, boolean isSelected) {
                        // handle selection change if needed
                    }
                    @Override
                    public void onPlaceClick(Place place) {
                        // handle click if needed
                    }
                }, false
        );
        recyclerView.setAdapter(adapter);
    }
}