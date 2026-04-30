package com.example.traveling.travelshare;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.traveling.R;
import com.example.traveling.travelshare.data.FirestoreManager;
import com.example.traveling.travelshare.model.Author;
import com.example.traveling.travelshare.model.Group;
import com.example.traveling.travelshare.model.Photo;
import com.example.traveling.travelshare.model.PhotoLocation;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class PublishActivity extends AppCompatActivity {

    private EditText etTitle, etDescription, etLocationName;
    private Spinner spinnerCategory, spinnerGroup;
    private ImageView ivPreview;
    private Button btnPublish;
    private List<Group> userGroups;
    private FirestoreManager firestoreManager;

    private static final String[] CATEGORY_LABELS = {
            "🌳 Nature", "🗿 Monument", "🚶 Rue", "🛍️ Commerce", "🍽️ Restaurant", "📍 Autre"
    };
    private static final String[] CATEGORY_VALUES = {
            "nature", "monument", "street", "shop", "restaurant", "other"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ts_activity_publish);

        firestoreManager = new FirestoreManager();
        initViews();
        setupSpinner();
        setupGroupSpinner();

        btnPublish.setOnClickListener(v -> handlePublish());
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
        findViewById(R.id.btn_voice_desc).setOnClickListener(v -> {
            Toast.makeText(this, "Simulation: Écoute en cours...", Toast.LENGTH_SHORT).show();
            etDescription.setText("C'était un moment inoubliable passé ici.");
        });
        findViewById(R.id.card_image).setOnClickListener(v -> {
            Toast.makeText(this, "Simulation: Photo sélectionnée", Toast.LENGTH_SHORT).show();
            ivPreview.setAlpha(1.0f);
            // Dans un vrai cas, on ouvrirait la galerie ici
        });
    }

    private void initViews() {
        etTitle        = findViewById(R.id.et_title);
        etDescription  = findViewById(R.id.et_description);
        etLocationName = findViewById(R.id.et_location_name);
        spinnerCategory = findViewById(R.id.spinner_category);
        spinnerGroup   = findViewById(R.id.spinner_publish_group);
        ivPreview      = findViewById(R.id.iv_preview);
        btnPublish     = findViewById(R.id.btn_publish);
    }

    private void setupGroupSpinner() {
        firestoreManager.getAllGroups(new FirestoreManager.OnDataLoadedListener<List<Group>>() {
            @Override
            public void onSuccess(List<Group> groups) {
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                userGroups = new ArrayList<>();
                if (user != null) {
                    for (Group g : groups) {
                        if (g.getMemberIds().contains(user.getUid())) userGroups.add(g);
                    }
                }

                if (userGroups.isEmpty()) {
                    findViewById(R.id.tv_group_label).setVisibility(View.GONE);
                    spinnerGroup.setVisibility(View.GONE);
                    return;
                }

                findViewById(R.id.tv_group_label).setVisibility(View.VISIBLE);
                spinnerGroup.setVisibility(View.VISIBLE);

                List<String> groupNames = new ArrayList<>();
                groupNames.add("Aucun (Public)");
                for (Group g : userGroups) groupNames.add(g.getName());

                ArrayAdapter<String> adapter = new ArrayAdapter<>(PublishActivity.this,
                        android.R.layout.simple_spinner_item, groupNames);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerGroup.setAdapter(adapter);
            }

            @Override
            public void onError(Exception e) {
                findViewById(R.id.tv_group_label).setVisibility(View.GONE);
                spinnerGroup.setVisibility(View.GONE);
            }
        });
    }

    private void setupSpinner() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, CATEGORY_LABELS);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(adapter);
    }

    private void handlePublish() {
        String title = etTitle.getText().toString().trim();
        String desc  = etDescription.getText().toString().trim();
        String loc   = etLocationName.getText().toString().trim();

        if (title.isEmpty() || desc.isEmpty() || loc.isEmpty()) {
            Toast.makeText(this, "Veuillez remplir tous les champs", Toast.LENGTH_SHORT).show();
            return;
        }

        // Récupérer l'utilisateur connecté
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        
        if (user == null) {
            Toast.makeText(this, "Vous devez être connecté pour publier", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        String userName = user.getDisplayName() != null && !user.getDisplayName().isEmpty() ? user.getDisplayName() : user.getEmail();
        String userId = user.getUid();

        Author author = new Author(userId, userName, "https://api.dicebear.com/7.x/avataaars/svg?seed=" + userName);

        String id = UUID.randomUUID().toString();
        String date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        String timestamp = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault()).format(new Date());

        // Image par défaut pour la simulation
        String imageUrl = "https://images.unsplash.com/photo-1464822759023-fed622ff2c3b?w=800";

        String selectedGroupId = null;
        int groupPos = spinnerGroup.getVisibility() == View.VISIBLE ? spinnerGroup.getSelectedItemPosition() : 0;
        if (groupPos > 0) {
            selectedGroupId = userGroups.get(groupPos - 1).getId();
        }

        Photo newPhoto = new Photo(
                id,
                imageUrl,
                title,
                desc,
                new PhotoLocation(loc, 43.6108, 3.8767, false), // Coordonnées par défaut (Montpellier)
                date,
                "Récemment",
                new ArrayList<>(),
                "Non spécifié",
                author,
                Arrays.asList(CATEGORY_VALUES[spinnerCategory.getSelectedItemPosition()], "voyage"),
                CATEGORY_VALUES[spinnerCategory.getSelectedItemPosition()],
                0,
                false,
                groupPos == 0, // Public si aucun groupe sélectionné
                timestamp,
                selectedGroupId
        );

        btnPublish.setEnabled(false);
        firestoreManager.addPhoto(newPhoto, new FirestoreManager.OnDataLoadedListener<Void>() {
            @Override
            public void onSuccess(Void data) {
                Toast.makeText(PublishActivity.this, "Photo publiée avec succès !", Toast.LENGTH_LONG).show();
                finish();
            }

            @Override
            public void onError(Exception e) {
                btnPublish.setEnabled(true);
                Toast.makeText(PublishActivity.this, "Erreur lors de la publication", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
