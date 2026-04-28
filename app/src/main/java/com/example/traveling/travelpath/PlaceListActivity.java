package com.example.traveling.travelpath;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.traveling.R;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.List;

public class PlaceListActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tp_activity_place_list);

        String mode = getIntent().getStringExtra("extra_mode");
        double currentHoraire = getIntent().getDoubleExtra("horaire_depart", 0);
        String json = getIntent().getStringExtra("extra_places");
        ArrayList<Place> places = new Gson().fromJson(json, new TypeToken<List<Place>>(){}.getType());

        for (int i = 0; i < places.size() - 1; i++) {
            Place place = places.get(i);
            place.setName(place.getName() + " (" + horaireToString(currentHoraire) + ")");
            currentHoraire += place.getUsualTimeSpentHours();
            currentHoraire += place.getTimeBetween(places.get(i + 1));
        }

        Place lastPlace = places.get(places.size() - 1);
        lastPlace.setName(lastPlace.getName() + " (" + horaireToString(currentHoraire) + ")");

        TextView title = findViewById(R.id.text_place_list_title);
        title.setText(mode != null ? mode : "Lieux");

        RecyclerView recyclerView = findViewById(R.id.recycler_places);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        PlaceAdapter adapter = new PlaceAdapter(
                places != null ? places : new ArrayList<>(),
                new PlaceAdapter.PlaceSelectionListener() {
                    @Override
                    public void onPlaceSelected(Place place, boolean isSelected) {
                    }
                    @Override
                    public void onPlaceClick(Place place) {
                    }
                }, false
        );
        recyclerView.setAdapter(adapter);
    }

    private String horaireToString(double horaire)  {
        return String.format("%02.0f:%02.0f",
                Math.floor(horaire),
                Math.floor((horaire - Math.floor(horaire)) * 4.0) * 15.0
        );
    }
}