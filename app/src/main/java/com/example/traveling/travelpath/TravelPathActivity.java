package com.example.traveling.travelpath;

import android.content.Intent;
import android.os.Bundle;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.example.traveling.R;
import com.example.traveling.travelpath.PlaceAdapter;
import com.example.traveling.travelpath.Place;
import java.util.ArrayList;
import java.util.List;

public class TravelPathActivity extends AppCompatActivity implements PlaceAdapter.PlaceSelectionListener {
    private CheckBox checkboxRestauration;
    private CheckBox checkboxLoisirs;
    private CheckBox checkboxDecouvertes;
    private CheckBox checkboxCulture;

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
    private TextView textResultCount;

    private List<Place> allPlaces;
    private List<Place> filteredPlaces;
    private List<Place> selectedPlaces;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tp_activity_filters);

        initializeViews();
        setupRecyclerView();
        loadSamplePlaces();
        setupListeners();
    }

    private void initializeViews() {
        checkboxRestauration = findViewById(R.id.checkbox_restauration);
        checkboxLoisirs = findViewById(R.id.checkbox_loisirs);
        checkboxDecouvertes = findViewById(R.id.checkbox_decouvertes);
        checkboxCulture = findViewById(R.id.checkbox_culture);

        editBudgetMin = findViewById(R.id.edit_budget_min);
        editBudgetMax = findViewById(R.id.edit_budget_max);

        editDurationMin = findViewById(R.id.edit_duration_min);
        editDurationMax = findViewById(R.id.edit_duration_max);

        editLengthMin = findViewById(R.id.edit_length_min);
        editLengthMax = findViewById(R.id.edit_length_max);

        btnReset = findViewById(R.id.btn_reset);
        btnSearch = findViewById(R.id.btn_search);

        textResultCount = findViewById(R.id.text_result_count);
    }

    private void setupRecyclerView() {
        recyclerPlaces = findViewById(R.id.recycler_places);
        recyclerPlaces.setLayoutManager(new LinearLayoutManager(this));

        allPlaces = new ArrayList<>();
        filteredPlaces = new ArrayList<>();
        selectedPlaces = new ArrayList<>();

        placeAdapter = new PlaceAdapter(filteredPlaces, this);
        recyclerPlaces.setAdapter(placeAdapter);
    }

    private void loadSamplePlaces() {
        // Sample places with tags
        Place place1 = new Place("1", "Restaurant Le Petit Plateau", 25.50, 43.6108, 3.8767, 1.5);
        place1.addTag("restauration");

        Place place2 = new Place("2", "Parc de la Mosson", 0, 43.6200, 3.8800, 2.0);
        place2.addTag("loisirs");
        place2.addTag("decouvertes");

        Place place3 = new Place("3", "Musée Fabre", 8.00, 43.6100, 3.8700, 2.5);
        place3.addTag("culture");
        place3.addTag("decouvertes");

        Place place4 = new Place("4", "Café du Coin", 12.50, 43.6150, 3.8750, 1.0);
        place4.addTag("restauration");
        place4.addTag("loisirs");

        Place place5 = new Place("5", "Promenade Peyrou", 0, 43.6120, 3.8690, 1.5);
        place5.addTag("loisirs");
        place5.addTag("decouvertes");

        Place place6 = new Place("6", "Opéra Comédie", 15.00, 43.6090, 3.8800, 2.0);
        place6.addTag("culture");

        allPlaces.add(place1);
        allPlaces.add(place2);
        allPlaces.add(place3);
        allPlaces.add(place4);
        allPlaces.add(place5);
        allPlaces.add(place6);

        filteredPlaces.addAll(allPlaces);
        placeAdapter.notifyDataSetChanged();
        updateResultCount();
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
        applyFilters(filterData);

        if (!filteredPlaces.isEmpty()) {
            Intent intent = new Intent(this, TravelModeActivity.class);
            // Optional: pass the filtered places so TravelMode can use them
            // instead of its hardcoded sample data
            intent.putParcelableArrayListExtra(
                    TravelModeActivity.EXTRA_PLACES,
                    new ArrayList<>(filteredPlaces)
            );
            startActivity(intent);
        } else {
            Toast.makeText(this, "Aucun lieu trouvé", Toast.LENGTH_SHORT).show();
        }
    }

    private void applyFilters(FilterData filterData) {
        filteredPlaces.clear();

        for (Place place : allPlaces) {
            if (!filterData.preferences.isEmpty()) {
                boolean matchesPreference = false;
                for (String pref : filterData.preferences) {
                    if (place.hasTag(pref)) {
                        matchesPreference = true;
                        break;
                    }
                }
                if (!matchesPreference) continue;
            }

            if (filterData.budgetMin > 0 && place.getPrice() < filterData.budgetMin) {
                continue;
            }
            if (filterData.budgetMax > 0 && place.getPrice() > filterData.budgetMax) {
                continue;
            }

            if (filterData.durationMin > 0 && place.getUsualTimeSpentHours() < filterData.durationMin) {
                continue;
            }
            if (filterData.durationMax > 0 && place.getUsualTimeSpentHours() > filterData.durationMax) {
                continue;
            }

            filteredPlaces.add(place);
        }

        placeAdapter.notifyDataSetChanged();
        updateResultCount();

        String message = filteredPlaces.isEmpty() ? "Aucun lieu trouvé" : filteredPlaces.size() + " lieu(x) trouvé(s)";
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void handleReset() {
        clearAllFields();
        filteredPlaces.clear();
        filteredPlaces.addAll(allPlaces);
        placeAdapter.selectAll();
        placeAdapter.notifyDataSetChanged();
        updateResultCount();
        selectedPlaces.clear();
        Toast.makeText(this, "Filtres réinitialisés", Toast.LENGTH_SHORT).show();
    }

    private boolean isFormValid() {
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

        if (checkboxRestauration.isChecked()) data.preferences.add("restauration");
        if (checkboxLoisirs.isChecked()) data.preferences.add("loisirs");
        if (checkboxDecouvertes.isChecked()) data.preferences.add("decouvertes");
        if (checkboxCulture.isChecked()) data.preferences.add("culture");

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

        return data;
    }

    private void clearAllFields() {
        checkboxRestauration.setChecked(false);
        checkboxLoisirs.setChecked(false);
        checkboxDecouvertes.setChecked(false);
        checkboxCulture.setChecked(false);

        editBudgetMin.setText("");
        editBudgetMax.setText("");
        editDurationMin.setText("");
        editDurationMax.setText("");
        editLengthMin.setText("");
        editLengthMax.setText("");
    }

    private void updateResultCount() {
        int count = filteredPlaces.size();
        String text = count + (count == 1 ? " lieu" : " lieux");
        textResultCount.setText(text);
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
        android.util.Log.d("PlaceSelection", "Selected places: " + selectedPlaces.size());
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
        public double budgetMin = 0;
        public double budgetMax = 0;
        public double durationMin = 0;
        public double durationMax = 0;
        public double lengthMin = 0;
        public double lengthMax = 0;
    }
}