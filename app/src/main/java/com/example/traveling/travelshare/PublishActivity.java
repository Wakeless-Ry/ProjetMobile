package com.example.traveling.travelshare;

import android.content.ActivityNotFoundException;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.example.traveling.R;
import com.example.traveling.travelpath.Place;
import com.example.traveling.travelshare.data.FirestoreManager;
import com.example.traveling.travelshare.model.Author;
import com.example.traveling.travelshare.model.Group;
import com.example.traveling.travelshare.model.Photo;
import com.example.traveling.travelshare.model.PhotoLocation;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

public class PublishActivity extends AppCompatActivity {

    private static final String TAG = "PublishActivity";
    private static final int VOICE_REQUEST_CODE = 1002;

    private EditText etTitle, etDescription;
    private Spinner spinnerGroup;
    private ImageView ivPreview;
    private Button btnPublish;
    private List<Group> userGroups;
    private FirestoreManager firestoreManager;

    // Place mode toggle
    private RadioGroup rgPlaceMode;
    private RadioButton rbExistingPlace, rbNewPlace;

    private LinearLayout panelExistingPlace;
    private Spinner spinnerPlace;
    private TextView tvPlaceDetails;
    private List<Place> allPlaces = new ArrayList<>();
    private Place selectedPlace = null;

    private LinearLayout panelNewPlace;
    private EditText etLocationName, etLatitude, etLongitude, etTimeSpent, etPrice;
    private Spinner spinnerCategory;

    private final List<double[]> horaireSlots = new ArrayList<>();
    private LinearLayout llHoraireSlots;
    private CheckBox cbAlwaysOpen;

    private static final String[] CATEGORY_LABELS = {
            "Loisir", "Découverte", "Culture", "Restauration", "Rue", "Commerce"
    };
    private static final String[] CATEGORY_VALUES = {
            "Loisir", "Découverte", "Culture", "Restauration", "Rue", "Commerce"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ts_activity_publish);

        firestoreManager = new FirestoreManager();
        initViews();
        setupCategorySpinner();
        setupGroupSpinner();
        setupPlaceModeToggle();
        setupHoraires();
        loadPlacesFromFirestore();

        btnPublish.setOnClickListener(v -> handlePublish());
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        View btnVoice = findViewById(R.id.btn_voice_desc);
        if (btnVoice != null) {
            btnVoice.setOnClickListener(v -> startVoiceRecognition());
        }

        findViewById(R.id.card_image).setOnClickListener(v -> {
            Toast.makeText(this, "Simulation: Photo sélectionnée", Toast.LENGTH_SHORT).show();
            ivPreview.setAlpha(1.0f);
        });
    }

    private void initViews() {
        etTitle       = findViewById(R.id.et_title);
        etDescription = findViewById(R.id.et_description);
        spinnerGroup  = findViewById(R.id.spinner_publish_group);
        ivPreview     = findViewById(R.id.iv_preview);
        btnPublish    = findViewById(R.id.btn_publish);

        rgPlaceMode      = findViewById(R.id.rg_place_mode);
        rbExistingPlace  = findViewById(R.id.rb_existing_place);
        rbNewPlace       = findViewById(R.id.rb_new_place);

        panelExistingPlace = findViewById(R.id.panel_existing_place);
        spinnerPlace       = findViewById(R.id.spinner_place);
        tvPlaceDetails     = findViewById(R.id.tv_place_details);

        panelNewPlace   = findViewById(R.id.panel_new_place);
        etLocationName  = findViewById(R.id.et_location_name);
        etLatitude      = findViewById(R.id.et_latitude);
        etLongitude     = findViewById(R.id.et_longitude);
        etTimeSpent     = findViewById(R.id.et_time_spent);
        etPrice         = findViewById(R.id.et_price);
        spinnerCategory = findViewById(R.id.spinner_category);
        llHoraireSlots  = findViewById(R.id.ll_horaire_slots);
        cbAlwaysOpen    = findViewById(R.id.cb_always_open);
    }


    private void setupHoraires() {
        // "Toujours ouvert" checkbox hides / shows the slot list
        cbAlwaysOpen.setOnCheckedChangeListener((btn, checked) -> {
            llHoraireSlots.setVisibility(checked ? View.GONE : View.VISIBLE);
            findViewById(R.id.btn_add_horaire).setVisibility(checked ? View.GONE : View.VISIBLE);
        });

        findViewById(R.id.btn_add_horaire).setOnClickListener(v -> addHoraireSlot(9.0, 18.0));

        addHoraireSlot(9.0, 18.0);
    }


    private void addHoraireSlot(double debut, double fin) {
        double[] slot = {debut, fin};
        horaireSlots.add(slot);
        int index = horaireSlots.size() - 1;

        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        rowParams.setMargins(0, 0, 0, dpToPx(8));
        row.setLayoutParams(rowParams);

        EditText etDebut = new EditText(this);
        etDebut.setHint("Ouverture");
        etDebut.setText(formatHoraire(debut));
        etDebut.setFocusable(false);
        etDebut.setCursorVisible(false);
        etDebut.setClickable(true);
        LinearLayout.LayoutParams etParams = new LinearLayout.LayoutParams(0,
                LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        etParams.setMarginEnd(dpToPx(6));
        etDebut.setLayoutParams(etParams);

        EditText etFin = new EditText(this);
        etFin.setHint("Fermeture");
        etFin.setText(formatHoraire(fin));
        etFin.setFocusable(false);
        etFin.setCursorVisible(false);
        etFin.setClickable(true);
        LinearLayout.LayoutParams etFinParams = new LinearLayout.LayoutParams(0,
                LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        etFinParams.setMarginEnd(dpToPx(6));
        etFin.setLayoutParams(etFinParams);

        Button btnRemove = new Button(this);
        btnRemove.setText("✕");
        btnRemove.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));

        row.addView(etDebut);
        row.addView(etFin);
        row.addView(btnRemove);
        llHoraireSlots.addView(row);

        View.OnClickListener pickDebut = v -> {
            int h = (int) Math.floor(slot[0]);
            int m = (int) ((slot[0] - h) * 60);
            new TimePickerDialog(this, (TimePicker tp, int hh, int mm) -> {
                slot[0] = hh + mm / 60.0;
                etDebut.setText(formatHoraire(slot[0]));
            }, h, m, true).show();
        };
        etDebut.setOnClickListener(pickDebut);
        etDebut.setOnFocusChangeListener((v, has) -> { if (has) pickDebut.onClick(v); });

        View.OnClickListener pickFin = v -> {
            int h = (int) Math.floor(slot[1]);
            int m = (int) ((slot[1] - h) * 60);
            new TimePickerDialog(this, (TimePicker tp, int hh, int mm) -> {
                slot[1] = hh + mm / 60.0;
                etFin.setText(formatHoraire(slot[1]));
            }, h, m, true).show();
        };
        etFin.setOnClickListener(pickFin);
        etFin.setOnFocusChangeListener((v, has) -> { if (has) pickFin.onClick(v); });

        btnRemove.setOnClickListener(v -> {
            int i = horaireSlots.indexOf(slot);
            if (i >= 0) horaireSlots.remove(i);
            llHoraireSlots.removeView(row);
        });
    }

    private String formatHoraire(double h) {
        return String.format(Locale.getDefault(), "%02.0f:%02.0f",
                Math.floor(h), (h - Math.floor(h)) * 60);
    }


    private List<Map<String, Double>> collectHoraires() {
        if (cbAlwaysOpen.isChecked()) {
            Map<String, Double> open = new HashMap<>();
            open.put("debut", 0.0);
            open.put("fin", 24.0);
            return Arrays.asList(open);
        }
        List<Map<String, Double>> result = new ArrayList<>();
        for (double[] slot : horaireSlots) {
            Map<String, Double> m = new HashMap<>();
            m.put("debut", slot[0]);
            m.put("fin", slot[1]);
            result.add(m);
        }
        return result;
    }

    private int dpToPx(int dp) {
        return Math.round(dp * getResources().getDisplayMetrics().density);
    }


    private void setupPlaceModeToggle() {
        rgPlaceMode.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rb_existing_place) {
                panelExistingPlace.setVisibility(View.VISIBLE);
                panelNewPlace.setVisibility(View.GONE);
            } else {
                panelExistingPlace.setVisibility(View.GONE);
                panelNewPlace.setVisibility(View.VISIBLE);
            }
        });
        rbExistingPlace.setChecked(true);
        panelExistingPlace.setVisibility(View.VISIBLE);
        panelNewPlace.setVisibility(View.GONE);
    }


    private void loadPlacesFromFirestore() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("Places").get().addOnSuccessListener(queryDocumentSnapshots -> {
            if (isFinishing()) return;
            allPlaces.clear();
            for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                Place place = document.toObject(Place.class);
                place.fillHoraires(document);
                allPlaces.add(place);
            }
            populatePlaceSpinner();
        }).addOnFailureListener(e -> {
            Log.e(TAG, "Error loading places: " + e.getMessage());
            Toast.makeText(this, "Impossible de charger les lieux existants", Toast.LENGTH_SHORT).show();
        });
    }

    private void populatePlaceSpinner() {
        if (allPlaces.isEmpty()) {
            rbNewPlace.setChecked(true);
            rbExistingPlace.setEnabled(false);
            rbExistingPlace.setText("Lieu existant (aucun disponible)");
            return;
        }

        List<String> placeNames = new ArrayList<>();
        for (Place p : allPlaces) placeNames.add(p.getName());

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, placeNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerPlace.setAdapter(adapter);

        spinnerPlace.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                selectedPlace = allPlaces.get(position);
                updatePlaceDetailsView(selectedPlace);
            }
            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {
                selectedPlace = null;
                tvPlaceDetails.setText("");
            }
        });

        if (!allPlaces.isEmpty()) {
            selectedPlace = allPlaces.get(0);
            updatePlaceDetailsView(selectedPlace);
        }
    }

    private void updatePlaceDetailsView(Place place) {
        if (place == null || tvPlaceDetails == null) return;
        String details = "📍 " + place.getFormattedLocation()
                + "  •  ⏱ " + place.getFormattedTimeSpent()
                + "  •  💶 " + place.getFormattedPrice()
                + "\n🏷️ " + place.getTagsAsString()
                + "\n🕐 " + place.getHorairesAsString();
        tvPlaceDetails.setText(details);
    }


    private void setupCategorySpinner() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, CATEGORY_LABELS);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(adapter);
    }


    private void setupGroupSpinner() {
        Log.d(TAG, "Loading groups for spinner");
        firestoreManager.getAllGroups(new FirestoreManager.OnDataLoadedListener<List<Group>>() {
            @Override
            public void onSuccess(List<Group> groups) {
                if (isFinishing()) return;

                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                userGroups = new ArrayList<>();
                if (user != null && groups != null) {
                    for (Group g : groups) {
                        if (g != null && g.getMemberIds() != null && g.getMemberIds().contains(user.getUid())) {
                            userGroups.add(g);
                        }
                    }
                }

                View label = findViewById(R.id.tv_group_label);
                if (userGroups.isEmpty()) {
                    if (label != null) label.setVisibility(View.GONE);
                    spinnerGroup.setVisibility(View.GONE);
                    return;
                }

                if (label != null) label.setVisibility(View.VISIBLE);
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
                Log.e(TAG, "Error loading groups: " + e.getMessage());
                if (!isFinishing()) {
                    View label = findViewById(R.id.tv_group_label);
                    if (label != null) label.setVisibility(View.GONE);
                    spinnerGroup.setVisibility(View.GONE);
                }
            }
        });
    }


    private void handlePublish() {
        String title = etTitle.getText().toString().trim();
        String desc  = etDescription.getText().toString().trim();

        if (title.isEmpty() || desc.isEmpty()) {
            Toast.makeText(this, "Veuillez remplir le titre et la description", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "Vous devez être connecté pour publier", Toast.LENGTH_SHORT).show();
            return;
        }

        boolean useExisting = rbExistingPlace.isChecked();

        if (useExisting) {
            if (selectedPlace == null) {
                Toast.makeText(this, "Veuillez sélectionner un lieu", Toast.LENGTH_SHORT).show();
                return;
            }
            buildAndPublishPhoto(user, selectedPlace, deriveCategoryFromTags(selectedPlace));
        } else {
            String locName = etLocationName.getText().toString().trim();
            String latStr  = etLatitude.getText().toString().trim();
            String lngStr  = etLongitude.getText().toString().trim();

            if (locName.isEmpty()) {
                Toast.makeText(this, "Veuillez entrer le nom du lieu", Toast.LENGTH_SHORT).show();
                return;
            }

            double lat = 0, lng = 0;
            try {
                if (!latStr.isEmpty()) lat = Double.parseDouble(latStr);
                if (!lngStr.isEmpty()) lng = Double.parseDouble(lngStr);
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Coordonnées invalides", Toast.LENGTH_SHORT).show();
                return;
            }

            int catPos = spinnerCategory.getSelectedItemPosition() < 0 ? 0 : spinnerCategory.getSelectedItemPosition();
            String catValue = CATEGORY_VALUES[catPos];

            btnPublish.setEnabled(false);
            saveNewPlaceAndPublish(user, locName, lat, lng, catValue);
        }
    }


    private void saveNewPlaceAndPublish(FirebaseUser user, String locName, double lat, double lng, String category) {
        double timeSpent = 1.0;
        double price     = 0.0;
        try {
            String ts = etTimeSpent.getText().toString().trim();
            if (!ts.isEmpty()) timeSpent = Double.parseDouble(ts);
        } catch (NumberFormatException ignored) {}
        try {
            String pr = etPrice.getText().toString().trim();
            if (!pr.isEmpty()) price = Double.parseDouble(pr);
        } catch (NumberFormatException ignored) {}

        List<Map<String, Double>> horaires = collectHoraires();
        if (horaires.isEmpty()) {
            Toast.makeText(this, "Veuillez ajouter au moins un créneau horaire", Toast.LENGTH_SHORT).show();
            btnPublish.setEnabled(true);
            return;
        }

        for (Map<String, Double> slot : horaires) {
            if (slot.get("debut") >= slot.get("fin")) {
                Toast.makeText(this, "L'heure d'ouverture doit être antérieure à la fermeture", Toast.LENGTH_SHORT).show();
                btnPublish.setEnabled(true);
                return;
            }
        }

        Map<String, Object> placeData = new HashMap<>();
        placeData.put("name", locName);
        placeData.put("latitude", lat);
        placeData.put("longitude", lng);
        placeData.put("usualTimeSpentHours", timeSpent);
        placeData.put("price", price);
        placeData.put("selected", true);
        placeData.put("tags", Arrays.asList(category, "voyage"));
        placeData.put("horaires", horaires);

        FirebaseFirestore.getInstance().collection("Places")
                .add(placeData)
                .addOnSuccessListener(documentReference -> {
                    Log.d(TAG, "New place saved: " + documentReference.getId());
                    buildAndPublishPhotoFromRaw(user, locName, lat, lng, category);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error saving place: " + e.getMessage());
                    if (!isFinishing()) {
                        btnPublish.setEnabled(true);
                        Toast.makeText(this, "Erreur lors de la sauvegarde du lieu : " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void buildAndPublishPhoto(FirebaseUser user, Place place, String category) {
        buildAndPublishPhotoFromRaw(user,
                place.getName(),
                place.getLatitude(),
                place.getLongitude(),
                category);
    }

    private void buildAndPublishPhotoFromRaw(FirebaseUser user, String locName, double lat, double lng, String category) {
        try {
            String title = etTitle.getText().toString().trim();
            String desc  = etDescription.getText().toString().trim();

            String userName = user.getDisplayName();
            if (userName == null || userName.isEmpty()) userName = user.getEmail();
            if (userName == null || userName.isEmpty()) userName = "Voyageur";

            Author author = new Author(user.getUid(), userName,
                    "https://api.dicebear.com/7.x/avataaars/svg?seed=" + userName);

            String id        = UUID.randomUUID().toString();
            String date      = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
            String timestamp = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault()).format(new Date());
            String imageUrl  = "https://images.unsplash.com/photo-1464822759023-fed622ff2c3b?w=800";

            String selectedGroupId = "all";
            int groupPos = spinnerGroup.getVisibility() == View.VISIBLE ? spinnerGroup.getSelectedItemPosition() : 0;
            if (groupPos > 0 && userGroups != null && groupPos - 1 < userGroups.size()) {
                selectedGroupId = userGroups.get(groupPos - 1).getId();
            }

            Photo newPhoto = new Photo(
                    id,
                    imageUrl,
                    title,
                    desc,
                    new PhotoLocation(locName, lat, lng, false),
                    date,
                    "Récemment",
                    new ArrayList<>(),
                    "Non spécifié",
                    author,
                    Arrays.asList(category, "voyage"),
                    category,
                    0,
                    false,
                    groupPos == 0,
                    timestamp,
                    selectedGroupId
            );

            btnPublish.setEnabled(false);
            firestoreManager.addPhoto(newPhoto, new FirestoreManager.OnDataLoadedListener<Void>() {
                @Override
                public void onSuccess(Void data) {
                    if (isFinishing()) return;
                    Toast.makeText(PublishActivity.this, "Photo publiée avec succès !", Toast.LENGTH_LONG).show();
                    finish();
                }
                @Override
                public void onError(Exception e) {
                    Log.e(TAG, "Publish error: " + e.getMessage());
                    if (isFinishing()) return;
                    btnPublish.setEnabled(true);
                    Toast.makeText(PublishActivity.this, "Erreur lors de la publication : " + e.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Error building photo: " + e.getMessage());
            btnPublish.setEnabled(true);
            Toast.makeText(this, "Erreur interne lors de la création de la publication", Toast.LENGTH_SHORT).show();
        }
    }

    private String deriveCategoryFromTags(Place place) {
        if (place.getTags() != null) {
            for (String tag : place.getTags()) {
                for (String val : CATEGORY_VALUES) {
                    if (val.equalsIgnoreCase(tag)) return val;
                }
            }
        }
        return "other";
    }

    private void startVoiceRecognition() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "fr-FR");
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Décrivez votre photo...");
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
                String existingText = etDescription.getText().toString().trim();
                String newText = result.get(0);
                if (existingText.isEmpty()) {
                    etDescription.setText(newText);
                } else {
                    etDescription.setText(existingText + " " + newText);
                }
            }
        }
    }
}