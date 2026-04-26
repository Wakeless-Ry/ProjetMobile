package com.example.traveling.travelpath;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.widget.CheckBox;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.example.traveling.R;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TravelPathActivity extends AppCompatActivity implements PlaceAdapter.PlaceSelectionListener {
    private Map<String, CheckBox> preferences = new HashMap<>();

    private TextInputEditText editNbActivites;

    private TextInputEditText editBudgetMin;
    private TextInputEditText editBudgetMax;

    private TextInputEditText editDurationMin;
    private TextInputEditText editDurationMax;

    private TextInputEditText editLengthMin;
    private TextInputEditText editLengthMax;

    private MaterialButton btnReset;
    private MaterialButton btnSearch;

    private RecyclerView recyclerPlaces;
    private PlaceAdapter placeAdapter;

    private List<Place> allPlaces;
    private List<Place> selectedPlaces;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tp_activity_filters);

        initializeViews();
        setupRecyclerView();
        loadPlaces();
        setupListeners();
    }

    private void initializeViews() {
        editNbActivites = findViewById(R.id.edit_steps);

        editBudgetMin = findViewById(R.id.edit_budget_min);
        editBudgetMax = findViewById(R.id.edit_budget_max);

        editDurationMin = findViewById(R.id.edit_duration_min);
        editDurationMax = findViewById(R.id.edit_duration_max);

        editLengthMin = findViewById(R.id.edit_length_min);
        editLengthMax = findViewById(R.id.edit_length_max);

        btnReset = findViewById(R.id.btn_reset);
        btnSearch = findViewById(R.id.btn_search);
    }

    private void setupRecyclerView() {
        recyclerPlaces = findViewById(R.id.recycler_places);
        recyclerPlaces.setLayoutManager(new LinearLayoutManager(this));

        allPlaces = new ArrayList<>();
        selectedPlaces = new ArrayList<>();

        placeAdapter = new PlaceAdapter(allPlaces, this, true);
        recyclerPlaces.setAdapter(placeAdapter);
    }

    private void loadPlaces() {
        android.widget.LinearLayout layout = findViewById(R.id.checkbox_list);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("Places").get().addOnSuccessListener(queryDocumentSnapshots -> {
            for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                Place place = document.toObject(Place.class);
                allPlaces.add(place);
                selectedPlaces.add(place);
                for (String pref : place.getTags()) {
                    if (!preferences.containsKey(pref)) {
                        CheckBox checkBox = new CheckBox(this);
                        checkBox.setChecked(true);
                        checkBox.setText(pref);
                        layout.addView(checkBox);
                        preferences.put(pref, checkBox);
                    }
                }
            }
        });

        placeAdapter.notifyDataSetChanged();
    }

    private void setupListeners() {
        btnSearch.setOnClickListener(v -> handleSearch());
        btnReset.setOnClickListener(v -> handleReset());
    }

    private void handleSearch() {
        if (!isFormValid()) {
            return;
        }

        FilterData filterData = collectFilterData();

        if (filterData.selectedPlaces.size() < filterData.nbActivites) {
            Toast.makeText(this, "Veuillez sélectionner au moins " + filterData.nbActivites + " activité(s)", Toast.LENGTH_SHORT).show();
        } else {
            Intent intent = new Intent(this, TravelModeActivity.class);

            intent.putStringArrayListExtra("preferences", (ArrayList<String>) filterData.preferences);
            intent.putExtra("nbActivites", filterData.nbActivites);
            intent.putExtra("budgetMin", filterData.budgetMin);
            intent.putExtra("budgetMax", filterData.budgetMax);
            intent.putExtra("durationMin", filterData.durationMin);
            intent.putExtra("durationMax", filterData.durationMax);
            intent.putExtra("lengthMin", filterData.lengthMin);
            intent.putExtra("lengthMax", filterData.lengthMax);
            intent.putParcelableArrayListExtra("selectedPlaces", (ArrayList<? extends Parcelable>) filterData.selectedPlaces);

            startActivity(intent);
        }
    }

    private void handleReset() {
        clearAllFields();
        placeAdapter.selectAll();
        placeAdapter.notifyDataSetChanged();
        selectedPlaces.clear();
        Toast.makeText(this, "Filtres réinitialisés", Toast.LENGTH_SHORT).show();
    }

    private boolean isFormValid() {
        if (!editNbActivites.getText().toString().isEmpty()) {
            try {
                Integer.parseInt(editNbActivites.getText().toString());
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Format de nombre d'activités invalide", Toast.LENGTH_SHORT).show();
                return false;
            }
        }
        if (!editBudgetMin.getText().toString().isEmpty() &&
                !editBudgetMax.getText().toString().isEmpty()) {
            try {
                double minBudget = Double.parseDouble(editBudgetMin.getText().toString());
                double maxBudget = Double.parseDouble(editBudgetMax.getText().toString());
                if (minBudget > maxBudget) {
                    Toast.makeText(this, "Budget min doit être ≤ Budget max", Toast.LENGTH_SHORT).show();
                    return false;
                }
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Format de budget invalide", Toast.LENGTH_SHORT).show();
                return false;
            }
        }

        if (!editDurationMin.getText().toString().isEmpty() &&
                !editDurationMax.getText().toString().isEmpty()) {
            try {
                double minDuration = Double.parseDouble(editDurationMin.getText().toString());
                double maxDuration = Double.parseDouble(editDurationMax.getText().toString());
                if (minDuration > maxDuration) {
                    Toast.makeText(this, "Durée min doit être ≤ Durée max", Toast.LENGTH_SHORT).show();
                    return false;
                }
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Format de durée invalide", Toast.LENGTH_SHORT).show();
                return false;
            }
        }

        if (!editLengthMin.getText().toString().isEmpty() &&
                !editLengthMax.getText().toString().isEmpty()) {
            try {
                double minLength = Double.parseDouble(editLengthMin.getText().toString());
                double maxLength = Double.parseDouble(editLengthMax.getText().toString());
                if (minLength > maxLength) {
                    Toast.makeText(this, "Distance min doit être ≤ Distance max", Toast.LENGTH_SHORT).show();
                    return false;
                }
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Format de distance invalide", Toast.LENGTH_SHORT).show();
                return false;
            }
        }

        return true;
    }

    private FilterData collectFilterData() {
        FilterData data = new FilterData();

        for (Map.Entry<String, CheckBox> entry : preferences.entrySet()) {
            if (entry.getValue().isChecked()) {
                data.preferences.add(entry.getKey());
            }
        }

        if (!editNbActivites.getText().toString().isEmpty()) {
            data.nbActivites = Integer.parseInt(editNbActivites.getText().toString());
        }

        if (!editBudgetMin.getText().toString().isEmpty()) {
            data.budgetMin = Double.parseDouble(editBudgetMin.getText().toString());
        }
        if (!editBudgetMax.getText().toString().isEmpty()) {
            data.budgetMax = Double.parseDouble(editBudgetMax.getText().toString());
        }

        if (!editDurationMin.getText().toString().isEmpty()) {
            data.durationMin = Double.parseDouble(editDurationMin.getText().toString());
        }
        if (!editDurationMax.getText().toString().isEmpty()) {
            data.durationMax = Double.parseDouble(editDurationMax.getText().toString());
        }

        if (!editLengthMin.getText().toString().isEmpty()) {
            data.lengthMin = Double.parseDouble(editLengthMin.getText().toString());
        }
        if (!editLengthMax.getText().toString().isEmpty()) {
            data.lengthMax = Double.parseDouble(editLengthMax.getText().toString());
        }

        data.selectedPlaces = this.getSelectedPlaces();

        return data;
    }

    private void clearAllFields() {
        for (Map.Entry<String, CheckBox> entry : preferences.entrySet()) {
            entry.getValue().setChecked(false);
        }

        editBudgetMin.setText("");
        editBudgetMax.setText("");
        editDurationMin.setText("");
        editDurationMax.setText("");
        editLengthMin.setText("");
        editLengthMax.setText("");
    }

    @Override
    public void onPlaceSelected(Place place, boolean isSelected) {
        if (isSelected) {
            if (!selectedPlaces.contains(place)) {
                selectedPlaces.add(place);
            }
        } else {
            selectedPlaces.remove(place);
        }
    }

    @Override
    public void onPlaceClick(Place place) {
        place.setSelected(!place.isSelected());
        this.onPlaceSelected(place, place.isSelected());
        placeAdapter.notifyDataSetChanged();
    }

    public List<Place> getSelectedPlaces() {
        return selectedPlaces;
    }

    public static class FilterData {
        public List<String> preferences = new ArrayList<>();
        public int nbActivites = 0;
        public double budgetMin = 0;
        public double budgetMax = 0;
        public double durationMin = 0;
        public double durationMax = 0;
        public double lengthMin = 0;
        public double lengthMax = 0;

        public List<Place> selectedPlaces;
    }
}