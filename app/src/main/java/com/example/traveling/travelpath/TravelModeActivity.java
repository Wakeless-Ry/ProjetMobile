package com.example.traveling.travelpath;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.traveling.R;
import java.util.ArrayList;
import java.util.List;

public class TravelModeActivity extends AppCompatActivity {

    public static final String EXTRA_MODE = "extra_mode";
    public static final String EXTRA_PLACES = "extra_places";

    public static final String MODE_ECONOMIQUE = "Économique";
    public static final String MODE_EQUILIBRE = "Équilibré";
    public static final String MODE_CONFORT = "Confort";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tp_parcours_viewer);

        // --- Build your place lists per mode ---
        List<Place> economiquePlaces = buildEconomiquePlaces();
        List<Place> equilibrePlaces = buildEquilibrePlaces();
        List<Place> confortPlaces   = buildConfortPlaces();

        // --- Wire up the Économique card ---
        bindModeCard(
            R.id.card_economique,
            R.id.text_economique_activities,
            R.id.text_economique_price,
            R.id.text_economique_time,
            economiquePlaces,
            MODE_ECONOMIQUE
        );

        // --- Wire up the Équilibré card ---
        bindModeCard(
            R.id.card_equilibre,
            R.id.text_equilibre_activities,
            R.id.text_equilibre_price,
            R.id.text_equilibre_time,
            equilibrePlaces,
            MODE_EQUILIBRE
        );

        // --- Wire up the Confort card ---
        bindModeCard(
            R.id.card_confort,
            R.id.text_confort_activities,
            R.id.text_confort_price,
            R.id.text_confort_time,
            confortPlaces,
            MODE_CONFORT
        );
    }

    private void bindModeCard(int cardId,
                               int activitiesId,
                               int priceId,
                               int timeId,
                               List<Place> places,
                               String mode) {

        View card = findViewById(cardId);

        // Compute stats
        int count = places.size();
        double totalPrice = 0;
        double totalTime  = 0;
        for (Place p : places) {
            totalPrice += p.getPrice();
            totalTime  += p.getUsualTimeSpentHours();
        }

        // Fill labels
        ((TextView) findViewById(activitiesId))
            .setText(count + " activité" + (count > 1 ? "s" : ""));
        ((TextView) findViewById(priceId))
            .setText(String.format("%.0f €", totalPrice));
        ((TextView) findViewById(timeId))
            .setText(formatTotalTime(totalTime));

        // Navigate on click
        card.setOnClickListener(v -> {
            Intent intent = new Intent(this, PlaceListActivity.class);
            intent.putExtra(EXTRA_MODE, mode);
            intent.putParcelableArrayListExtra(EXTRA_PLACES, new ArrayList<>(places));
            startActivity(intent);
        });
    }

    private String formatTotalTime(double hours) {
        if (hours < 1) return String.format("%.0f min", hours * 60);
        int h = (int) hours;
        int m = (int) Math.round((hours - h) * 60);
        return m > 0 ? h + " h " + m + " min" : h + " h";
    }

    // -------------------------------------------------------------------------
    // Sample data — replace with your real data source
    // -------------------------------------------------------------------------

    private List<Place> buildEconomiquePlaces() {
        List<Place> list = new ArrayList<>();
        Place p1 = new Place("e1", "Jardin des Plantes", 0, 43.6108, 3.8767, 1.5);
        p1.addTag("Nature"); p1.addTag("Loisirs");
        Place p2 = new Place("e2", "Marché du Lez", 5, 43.6203, 3.9012, 1.0);
        p2.addTag("Shopping");
        list.add(p1); list.add(p2);
        return list;
    }

    private List<Place> buildEquilibrePlaces() {
        List<Place> list = new ArrayList<>();
        Place p1 = new Place("q1", "Musée Fabre", 10, 43.6117, 3.8802, 2.0);
        p1.addTag("Culture");
        Place p2 = new Place("q2", "Place de la Comédie", 0, 43.6085, 3.8796, 0.75);
        p2.addTag("Loisirs");
        Place p3 = new Place("q3", "Le Petit Jardin", 25, 43.6089, 3.8771, 1.5);
        p3.addTag("Restauration");
        list.add(p1); list.add(p2); list.add(p3);
        return list;
    }

    private List<Place> buildConfortPlaces() {
        List<Place> list = new ArrayList<>();
        Place p1 = new Place("c1", "Spa Nuxe", 80, 43.6120, 3.8750, 2.5);
        p1.addTag("Bien-être");
        Place p2 = new Place("c2", "Restaurant Maison", 60, 43.6095, 3.8780, 2.0);
        p2.addTag("Restauration");
        Place p3 = new Place("c3", "Domaine de Verchant", 50, 43.6200, 3.9100, 3.0);
        p3.addTag("Loisirs"); p3.addTag("Bien-être");
        list.add(p1); list.add(p2); list.add(p3);
        return list;
    }
}