package com.example.traveling.travelshare;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.util.Log;
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

import com.example.traveling.R;
import com.example.traveling.travelpath.TravelPathActivity;
import com.example.traveling.travelshare.adapter.PhotoAdapter;
import com.example.traveling.travelshare.data.FirestoreManager;
import com.example.traveling.travelshare.model.Group;
import com.example.traveling.travelshare.model.Photo;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class PhotoHomeActivity extends AppCompatActivity {

    private static final String TAG = "PhotoHomeActivity";
    private static final int VOICE_REQUEST_CODE = 1001;
    private EditText etSearch;
    private Spinner spinnerFilter, spinnerFilterGroup;
    private Button btnSearch, btnRandom, btnLogin, btnFilters;
    private TextView tvPhotoCount;
    private RecyclerView recyclerView;
    private View filterPanel;
    private View fabPublish;
    private View travelPathButton;
    private BottomNavigationView bottomNav;

    private PhotoAdapter adapter;
    private FirestoreManager firestoreManager;
    private List<Photo> allPhotos = new ArrayList<>();
    private String selectedLocationType = "all";
    private String selectedGroupId = "all";
    private boolean filtersVisible = false;

    private static final String[] LOCATION_LABELS = {
            "Tous les lieux", "Loisir", "Découverte", "Culture", "Restauration", "Rue", "Commerce", "Nature"
    };
    private static final String[] LOCATION_VALUES = {
            "all", "Loisir", "Découverte", "Culture", "Restauration", "Rue", "Commerce", "Nature"
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
        travelPathButton    = findViewById(R.id.travelpath_button);
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
                Log.d(TAG, "Location filter changed to: " + selectedLocationType);
                performSearch();
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });

        setupGroupFilterSpinner();

        adapter = new PhotoAdapter(this, new ArrayList<>(), photo -> {
            if (photo != null && photo.getId() != null) {
                Intent intent = new Intent(this, PhotoDetailActivity.class);
                intent.putExtra("photoId", photo.getId());
                startActivity(intent);
            }
        });
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        recyclerView.setAdapter(adapter);

        updatePhotoCount(0);

        btnSearch.setOnClickListener(v -> performSearch());
        btnRandom.setOnClickListener(v -> performRandom());
        
        View btnVoice = findViewById(R.id.btn_voice);
        if (btnVoice != null) {
            btnVoice.setOnClickListener(v -> startVoiceRecognition());
        }
        
        btnFilters.setOnClickListener(v -> toggleFilters());
        fabPublish.setOnClickListener(v -> startActivity(new Intent(this, PublishActivity.class)));
        travelPathButton.setOnClickListener(v -> startActivity(new Intent(this, TravelPathActivity.class)));

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
        Log.d(TAG, "Setting up group filter spinner");
        firestoreManager.getAllGroups(new FirestoreManager.OnDataLoadedListener<List<Group>>() {
            @Override
            public void onSuccess(List<Group> groups) {
                if (isFinishing()) return;
                
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                List<Group> joinedGroups = new ArrayList<>();
                if (user != null && groups != null) {
                    for (Group g : groups) {
                        if (g != null && g.getMemberIds() != null && g.getMemberIds().contains(user.getUid())) {
                            joinedGroups.add(g);
                        }
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
                        else if (position - 1 < joinedGroups.size()) {
                            selectedGroupId = joinedGroups.get(position - 1).getId();
                        }
                        Log.d(TAG, "Group filter changed to: " + selectedGroupId);
                        performSearch();
                    }
                    @Override public void onNothingSelected(AdapterView<?> parent) {}
                });
            }

            @Override
            public void onError(Exception e) {
                Log.e(TAG, "Error loading groups for filter: " + e.getMessage());
                if (!isFinishing()) {
                    spinnerFilterGroup.setVisibility(View.GONE);
                }
            }
        });
    }

    private void setupBottomNav() {
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                recyclerView.smoothScrollToPosition(0);
                return true;
            }

            boolean loggedIn = FirebaseAuth.getInstance().getCurrentUser() != null;

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
        boolean loggedIn = user != null;

        fabPublish.setVisibility(loggedIn ? View.VISIBLE : View.GONE);

        if (loggedIn) {
            btnLogin.setText("Déconnexion");
            btnLogin.setOnClickListener(v -> {
                FirebaseAuth.getInstance().signOut();
                updateLoginButton();
                Toast.makeText(this, "Déconnecté", Toast.LENGTH_SHORT).show();
                performSearch();
            });
        } else {
            btnLogin.setText("Connexion");
            btnLogin.setOnClickListener(v ->
                    startActivity(new Intent(this, LoginActivity.class)));
        }
    }

    private void performSearch() {
        if (firestoreManager == null) return;
        
        String queryText = etSearch.getText().toString().trim();
        Log.d(TAG, "Performing search with query: " + queryText + ", group: " + selectedGroupId + ", type: " + selectedLocationType);
        
        firestoreManager.getPhotos(selectedGroupId, selectedLocationType, new FirestoreManager.OnDataLoadedListener<List<Photo>>() {
            @Override
            public void onSuccess(List<Photo> photos) {
                if (isFinishing()) return;
                
                allPhotos = (photos != null) ? photos : new ArrayList<>();
                Log.d(TAG, "Firestore returned " + allPhotos.size() + " photos");
                
                List<Photo> filtered = new ArrayList<>();
                if (queryText.isEmpty()) {
                    filtered = allPhotos;
                } else {
                    String q = queryText.toLowerCase();
                    for (Photo p : allPhotos) {
                        if (p == null) continue;
                        boolean matchesTitle = p.getTitle() != null && p.getTitle().toLowerCase().contains(q);
                        boolean matchesDesc = p.getDescription() != null && p.getDescription().toLowerCase().contains(q);
                        boolean matchesTags = p.getTags() != null && p.getTags().toString().toLowerCase().contains(q);
                        
                        if (matchesTitle || matchesDesc || matchesTags) {
                            filtered.add(p);
                        }
                    }
                }
                
                adapter.updatePhotos(filtered);
                updatePhotoCount(filtered.size());
            }

            @Override
            public void onError(Exception e) {
                if (isFinishing()) return;
                
                Log.e(TAG, "Search error: " + e.getMessage());
                Toast.makeText(PhotoHomeActivity.this, "Impossible de charger les photos : " + e.getMessage(), Toast.LENGTH_LONG).show();
                
                adapter.updatePhotos(new ArrayList<>());
                updatePhotoCount(0);
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
        } else {
            Toast.makeText(this, "Aucune photo à mélanger", Toast.LENGTH_SHORT).show();
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

    private void startVoiceRecognition() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "fr-FR");
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Parlez maintenant...");
        try {
            startActivityForResult(intent, VOICE_REQUEST_CODE);
        } catch (ActivityNotFoundException a) {
            Toast.makeText(this, "La reconnaissance vocale n'est pas supportée sur votre appareil", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == VOICE_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            if (result != null && !result.isEmpty()) {
                etSearch.setText(result.get(0));
                performSearch();
            }
        }
    }
}
