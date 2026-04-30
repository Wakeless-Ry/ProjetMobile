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
import com.example.traveling.travelshare.data.FirestoreManager;
import com.example.traveling.travelshare.model.Group;
import com.example.traveling.travelshare.model.Photo;
import com.example.traveling.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PhotoHomeActivity extends AppCompatActivity {

    private EditText etSearch;
    private Spinner spinnerFilter, spinnerFilterGroup;
    private Button btnSearch, btnRandom, btnLogin, btnFilters;
    private TextView tvPhotoCount;
    private RecyclerView recyclerView;
    private View filterPanel;
    private View fabPublish;
    private BottomNavigationView bottomNav;

    private PhotoAdapter adapter;
    private FirestoreManager firestoreManager;
    private List<Photo> allPhotos = new ArrayList<>();
    private String selectedLocationType = "all";
    private String selectedGroupId = "all";
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
        setContentView(R.layout.ts_activity_photo_home);

        etSearch      = findViewById(R.id.et_search);
        spinnerFilter = findViewById(R.id.spinner_filter);
        spinnerFilterGroup = findViewById(R.id.spinner_filter_group);
        btnSearch     = findViewById(R.id.btn_search);
        btnRandom     = findViewById(R.id.btn_random);
        btnLogin      = findViewById(R.id.btn_login);
        btnFilters    = findViewById(R.id.btn_filters);
        tvPhotoCount  = findViewById(R.id.tv_photo_count);
        recyclerView  = findViewById(R.id.recycler_view);
        filterPanel   = findViewById(R.id.filter_panel);
        fabPublish    = findViewById(R.id.fab_publish);
        bottomNav     = findViewById(R.id.bottom_navigation);

        firestoreManager = new FirestoreManager();
        setupBottomNav();

        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, LOCATION_LABELS);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerFilter.setAdapter(spinnerAdapter);
        spinnerFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedLocationType = LOCATION_VALUES[position];
                performSearch(); // Auto-refresh
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });

        setupGroupFilterSpinner();

        // RecyclerView grille 2 colonnes
        adapter = new PhotoAdapter(this, new ArrayList<>(), photo -> {
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
        findViewById(R.id.btn_voice).setOnClickListener(v -> {
            Toast.makeText(this, "Simulation: Écoute vocale...", Toast.LENGTH_SHORT).show();
            etSearch.setText("Tour Eiffel");
            performSearch();
        });
        btnFilters.setOnClickListener(v -> toggleFilters());
        fabPublish.setOnClickListener(v -> startActivity(new Intent(this, PublishActivity.class)));

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
        setupGroupFilterSpinner();
        performSearch();
    }

    private void setupGroupFilterSpinner() {
        firestoreManager.getAllGroups(new FirestoreManager.OnDataLoadedListener<List<Group>>() {
            @Override
            public void onSuccess(List<Group> groups) {
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                List<Group> joinedGroups = new ArrayList<>();
                if (user != null) {
                    for (Group g : groups) {
                        if (g.getMemberIds().contains(user.getUid())) joinedGroups.add(g);
                    }
                }

                if (joinedGroups.isEmpty()) {
                    spinnerFilterGroup.setVisibility(View.GONE);
                    selectedGroupId = "all";
                    return;
                }

                spinnerFilterGroup.setVisibility(View.VISIBLE);
                List<String> groupNames = new ArrayList<>();
                groupNames.add("Tous les flux (Public)");
                for (Group g : joinedGroups) groupNames.add("Groupe : " + g.getName());

                ArrayAdapter<String> groupAdapter = new ArrayAdapter<>(PhotoHomeActivity.this,
                        android.R.layout.simple_spinner_item, groupNames);
                groupAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerFilterGroup.setAdapter(groupAdapter);

                spinnerFilterGroup.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        if (position == 0) selectedGroupId = "all";
                        else selectedGroupId = joinedGroups.get(position - 1).getId();
                        performSearch();
                    }
                    @Override public void onNothingSelected(AdapterView<?> parent) {}
                });
            }

            @Override
            public void onError(Exception e) {
                spinnerFilterGroup.setVisibility(View.GONE);
            }
        });
    }

    private void setupBottomNav() {
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                // Déjà ici, on peut scroller en haut ou rafraîchir
                recyclerView.smoothScrollToPosition(0);
                return true;
            }

            SharedPreferences prefs = getSharedPreferences("travelshare", MODE_PRIVATE);
            boolean loggedIn = FirebaseAuth.getInstance().getCurrentUser() != null || prefs.getBoolean("isLoggedIn", false);

            if (!loggedIn) {
                Toast.makeText(this, "Veuillez vous connecter pour accéder à cette section", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(this, LoginActivity.class));
                return false;
            }

            if (id == R.id.nav_groups) {
                startActivity(new Intent(this, GroupActivity.class));
                return true;
            } else if (id == R.id.nav_notifications) {
                startActivity(new Intent(this, NotificationActivity.class));
                return true;
            } else if (id == R.id.nav_profile) {
                Intent intent = new Intent(this, AuthorProfileActivity.class);
                intent.putExtra("authorId", "user_current");
                startActivity(intent);
                return true;
            }
            return false;
        });
    }

    private void updateLoginButton() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        SharedPreferences prefs = getSharedPreferences("travelshare", MODE_PRIVATE);
        boolean loggedIn = user != null || prefs.getBoolean("isLoggedIn", false);

        fabPublish.setVisibility(loggedIn ? View.VISIBLE : View.GONE);

        if (loggedIn) {
            btnLogin.setText("Déconnexion");
            btnLogin.setOnClickListener(v -> {
                // Déconnexion réelle
                FirebaseAuth.getInstance().signOut();
                // Déconnexion simulation
                prefs.edit().clear().apply();
                updateLoginButton();
                Toast.makeText(this, "Déconnecté", Toast.LENGTH_SHORT).show();
            });
        } else {
            btnLogin.setText("Connexion");
            btnLogin.setOnClickListener(v ->
                    startActivity(new Intent(this, LoginActivity.class)));
        }
    }

    private void performSearch() {
        if (firestoreManager == null) return;
        
        String query = etSearch.getText().toString().trim();
        
        // On affiche un indicateur de chargement si nécessaire, ou on vide la liste
        adapter.updatePhotos(new ArrayList<>()); 

        firestoreManager.getPhotos(selectedGroupId, selectedLocationType, new FirestoreManager.OnDataLoadedListener<List<Photo>>() {
            @Override
            public void onSuccess(List<Photo> photos) {
                allPhotos = photos;
                
                // Filtrage local pour la recherche textuelle (titre/description)
                List<Photo> filtered = new ArrayList<>();
                if (query.isEmpty()) {
                    filtered = photos;
                } else {
                    String q = query.toLowerCase();
                    for (Photo p : photos) {
                        if (p.getTitle().toLowerCase().contains(q) || 
                            p.getDescription().toLowerCase().contains(q) ||
                            (p.getTags() != null && p.getTags().toString().toLowerCase().contains(q))) {
                            filtered.add(p);
                        }
                    }
                }
                
                adapter.updatePhotos(filtered);
                updatePhotoCount(filtered.size());
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(PhotoHomeActivity.this, "Erreur Firestore : " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void performRandom() {
        if (allPhotos != null && !allPhotos.isEmpty()) {
            List<Photo> shuffled = new ArrayList<>(allPhotos);
            Collections.shuffle(shuffled);
            adapter.updatePhotos(shuffled);
            updatePhotoCount(shuffled.size());
            Toast.makeText(this, "Photos mélangées !", Toast.LENGTH_SHORT).show();
        }
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
