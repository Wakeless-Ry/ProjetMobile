package com.example.traveling.travelpath;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.traveling.R;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class TravelModeActivity extends AppCompatActivity {
    TravelPathActivity.FilterData filterData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tp_parcours_viewer);

        filterData = this.getFilterData();

        TroisParcours troisParcours = buildParcours(filterData);

        bindModeCard(
            R.id.card_economique,
            R.id.text_economique_distance,
            R.id.text_economique_price,
            R.id.text_economique_time,
                troisParcours.economique,
                "Économique"
        );

        bindModeCard(
            R.id.card_equilibre,
            R.id.text_equilibre_distance,
            R.id.text_equilibre_price,
            R.id.text_equilibre_time,
                troisParcours.equilibre,
                "Équilibré"
        );

        bindModeCard(
            R.id.card_confort,
            R.id.text_confort_distance,
            R.id.text_confort_price,
            R.id.text_confort_time,
                troisParcours.confort,
                "Confort"
        );
    }

    private TravelPathActivity.FilterData getFilterData() {
        Intent intent = this.getIntent();
        TravelPathActivity.FilterData filterData = new TravelPathActivity.FilterData();

        filterData.preferences = intent.getStringArrayListExtra("preferences");
        filterData.nbActivites = intent.getIntExtra("nbActivites", 0);
        filterData.horaireDepart = intent.getDoubleExtra("horaireDepart", 0);
        filterData.horaireMax = intent.getDoubleExtra("horaireMax", 0);
        filterData.budgetMin = intent.getDoubleExtra("budgetMin", 0);
        filterData.budgetMax = intent.getDoubleExtra("budgetMax", 0);
        filterData.durationMin = intent.getDoubleExtra("durationMin", 0);
        filterData.durationMax = intent.getDoubleExtra("durationMax", 0);
        filterData.lengthMin = intent.getDoubleExtra("lengthMin", 0);
        filterData.lengthMax = intent.getDoubleExtra("lengthMax", 0);
        String json = intent.getStringExtra("selectedPlaces");
        List<Place> places = new Gson().fromJson(json, new TypeToken<List<Place>>(){}.getType());
        filterData.selectedPlaces = new ArrayList<>();

        for (Place place : places) {
            for (String tag : filterData.preferences) {
                if (place.getTags().contains(tag) && !filterData.selectedPlaces.contains(place)) {
                    filterData.selectedPlaces.add(place);
                }
            }
        }

        return filterData;
    }

    private void bindModeCard(int cardId,
                               int activitiesId,
                               int priceId,
                               int timeId,
                               List<Place> places,
                               String mode) {

        View card = findViewById(cardId);

        Parcours parcours = new Parcours(places);

        double length = parcours.getDistance();
        double totalPrice = parcours.getPrice();
        double totalTime  = parcours.getDuration();

        ((TextView) findViewById(activitiesId))
            .setText(String.format("%.2f", length) + " km");
        ((TextView) findViewById(priceId))
            .setText(String.format("%.0f €", totalPrice));
        ((TextView) findViewById(timeId))
            .setText(formatTotalTime(totalTime));

        card.setOnClickListener(v -> {
            Intent intent = new Intent(this, PlaceListActivity.class);
            intent.putExtra("extra_mode", mode);
            intent.putExtra("horaire_depart", filterData.horaireDepart);
            intent.putExtra("extra_places", new Gson().toJson(places)); // ← JSON instead
            startActivity(intent);
        });
    }

    private String formatTotalTime(double hours) {
        if (hours < 1) return String.format("%.0f min", hours * 60);
        int h = (int) hours;
        int m = (int) Math.round((hours - h) * 60);
        return m > 0 ? h + "h" + m : h + "h";
    }

    private Parcours generateParcours(TravelPathActivity.FilterData filterData) {
        Parcours parcours = new Parcours();

        if (filterData.selectedPlaces.size() < filterData.nbActivites) {
            filterData.nbActivites = filterData.selectedPlaces.size();
        }

        List<Place> places = filterData.selectedPlaces;
        Collections.shuffle(places);

        for (int i = 0; i < filterData.nbActivites; i++) {
            parcours.add(places.get(i));
        }

        return parcours;
    }

    private TroisParcours buildParcours(TravelPathActivity.FilterData filterData) {
        TroisParcours troisParcours = new TroisParcours();

        for (int i = 0; i < 10000; i++) {
            Parcours parcours = generateParcours(filterData);

            if (parcours.isValid(filterData)) {
                if (troisParcours.economique.isEmpty() || troisParcours.economique.getPrice() > parcours.getPrice()) {
                    troisParcours.economique = parcours;
                }

                if (troisParcours.equilibre.isEmpty() || troisParcours.equilibre.getMixedScore() > parcours.getMixedScore()) {
                    troisParcours.equilibre = parcours;
                }

                if (troisParcours.confort.isEmpty() || troisParcours.confort.getDistance() > parcours.getDistance()) {
                    troisParcours.confort = parcours;
                }
            }
        }

        return troisParcours;
    }

    private static class Parcours extends ArrayList<Place> {

        public Parcours() {
            super();
        }

        public Parcours(List<Place> places) {
            super();
            this.addAll(places);
        }

        public double getDistance() {
            double total = 0;

            for (int i = 0; i < this.size() - 1; i++) {
                total += this.get(i).getDistance(this.get(i + 1));
            }

            return total;
        }

        public double getPrice() {
            double total = 0;

            for (Place place : this) {
                total += place.getPrice();
            }

            return total;
        }

        public double getDuration() {
            double total = 0;

            for (Place place : this) {
                total += place.getUsualTimeSpentHours();
            }

            total += this.getDistance() / 4;

            return total;
        }

        public double getMixedScore() {
            return getDistance() * 20 + getPrice();
        }

        public boolean isValid(TravelPathActivity.FilterData filterData) {
            boolean horairesValid = true;

            double currentTime = filterData.horaireDepart;

            for (int i = 0; i < this.size() - 1; i++) {
                Place place = this.get(i);

                if (!place.isValidTime(currentTime)){
                    horairesValid = false;
                    break;
                }

                currentTime += place.getUsualTimeSpentHours();
                currentTime += place.getTimeBetween(this.get(i + 1));
            }

            Place lastPlace = this.get(this.size() - 1);

            if (!lastPlace.isValidTime(currentTime)){
                horairesValid = false;
            }
            currentTime += lastPlace.getUsualTimeSpentHours();

            horairesValid &= currentTime <= filterData.horaireMax;

            return horairesValid && this.getPrice() >= filterData.budgetMin
                    && this.getPrice() <= filterData.budgetMax
                    && this.getDuration() >= filterData.durationMin
                    && this.getDuration() <= filterData.durationMax
                    && this.getDistance() >= filterData.lengthMin
                    && this.getDistance() <= filterData.lengthMax;
        }
    }

    private static class TroisParcours {
        public Parcours economique = new Parcours();
        public Parcours equilibre = new Parcours();
        public Parcours confort = new Parcours();
    }
}