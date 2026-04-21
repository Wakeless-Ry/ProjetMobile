package com.example.traveling.travelshare;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.traveling.travelshare.adapter.PhotoAdapter;
import com.example.traveling.travelshare.data.SampleData;
import com.example.traveling.travelshare.model.Photo;
import com.example.traveling.R;
import java.util.List;

public class PhotoHomeActivity extends AppCompatActivity {

    private EditText etSearch;
    private Spinner spinnerFilter;
    private Button btnSearch, btnRandom, btnLogin, btnFilters;
    private TextView tvPhotoCount;
    private RecyclerView recyclerView;
    private View filterPanel;

    private PhotoAdapter adapter;
    private List<Photo> allPhotos;
    private String selectedLocationType = "all";
    private boolean filtersVisible = false;

    private static final String[] LOCATION_LABELS = {
            "Tous les lieux", "🌳 Nature", "🗿 Monument",
            "🚶 Rue", "🛍️ Commerce", "🍽️ Restaurant", "📍 Autre"
    };
    private static final String[] LOCATION_VALUES = {
            "all", "nature", "monument", "street", "shop", "restaurant", "other"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_home);

        etSearch      = findViewById(R.id.et_search);
        spinnerFilter = findViewById(R.id.spinner_filter);
        btnSearch     = findViewById(R.id.btn_search);
        btnRandom     = findViewById(R.id.btn_random);
        btnLogin      = findViewById(R.id.btn_login);
        btnFilters    = findViewById(R.id.btn_filters);
        tvPhotoCount  = findViewById(R.id.tv_photo_count);
        recyclerView  = findViewById(R.id.recycler_view);
        filterPanel   = findViewById(R.id.filter_panel);

         ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, LOCATION_LABELS);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerFilter.setAdapter(spinnerAdapter);
        spinnerFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedLocationType = LOCATION_VALUES[position];
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });

        // Données
        allPhotos = SampleData.getAllPhotos();

        // RecyclerView grille 2 colonnes
        adapter = new PhotoAdapter(this, allPhotos, photo -> {
            Intent intent = new Intent(this, PhotoDetailActivity.class);
            intent.putExtra("photoId", photo.getId());
            startActivity(intent);
        });
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        recyclerView.setAdapter(adapter);

        updatePhotoCount(allPhotos.size());

        // Boutons
        btnSearch.setOnClickListener(v -> performSearch());
        btnRandom.setOnClickListener(v -> performRandom());
        btnFilters.setOnClickListener(v -> toggleFilters());

        // Recherche sur Entrée
        etSearch.setOnEditorActionListener((v, actionId, event) -> {
            performSearch();
            return true;
        });

        updateLoginButton();
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateLoginButton();
    }

    private void updateLoginButton() {
        SharedPreferences prefs = getSharedPreferences("travelshare", MODE_PRIVATE);
        boolean loggedIn = prefs.getBoolean("isLoggedIn", false);
        String userName = prefs.getString("userName", "");

        if (loggedIn) {
            btnLogin.setText("👤 " + userName);
            btnLogin.setOnClickListener(v -> {
                // Déconnexion
                prefs.edit().clear().apply();
                updateLoginButton();
                Toast.makeText(this, "Déconnecté", Toast.LENGTH_SHORT).show();
            });
        } else {
            btnLogin.setText("Se connecter");
            btnLogin.setOnClickListener(v ->
                    startActivity(new Intent(this, LoginActivity.class)));
        }
    }

    private void performSearch() {
        String query = etSearch.getText().toString().trim();
        List<Photo> filtered = SampleData.filterPhotos(allPhotos, query, selectedLocationType, false);
        adapter.updatePhotos(filtered);
        updatePhotoCount(filtered.size());
    }

    private void performRandom() {
        List<Photo> shuffled = SampleData.filterPhotos(allPhotos, null, null, true);
        adapter.updatePhotos(shuffled);
        updatePhotoCount(shuffled.size());
        Toast.makeText(this, "Photos mélangées !", Toast.LENGTH_SHORT).show();
    }

    private void toggleFilters() {
        filtersVisible = !filtersVisible;
        filterPanel.setVisibility(filtersVisible ? View.VISIBLE : View.GONE);
        btnFilters.setText(filtersVisible ? "▲ Filtres" : "▼ Filtres");
    }

    private void updatePhotoCount(int count) {
        tvPhotoCount.setText(count + " photo" + (count > 1 ? "s" : ""));
    }
}
