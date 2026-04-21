package com.example.traveling.travelshare;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.example.traveling.travelshare.data.SampleData;
import com.example.traveling.travelshare.model.Photo;
import com.example.traveling.R;

import java.util.List;

public class PhotoDetailActivity extends AppCompatActivity {

    private Photo photo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_detail);

        // Récupère le photo via son id
        String photoId = getIntent().getStringExtra("photoId");
        List<Photo> allPhotos = SampleData.getAllPhotos();
        for (Photo p : allPhotos) {
            if (p.getId().equals(photoId)) {
                photo = p;
                break;
            }
        }

        if (photo == null) {
            Toast.makeText(this, "Photo introuvable", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        bindViews();
    }

    private void bindViews() {
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        ImageView ivPhoto = findViewById(R.id.iv_photo);
        Glide.with(this)
                .load(photo.getImageUrl())
                .centerCrop()
                .into(ivPhoto);

        ((TextView) findViewById(R.id.tv_title)).setText(photo.getTitle());
        ((TextView) findViewById(R.id.tv_description)).setText(photo.getDescription());

        Button btnLike = findViewById(R.id.btn_like);
        updateLikeButton(btnLike);
        btnLike.setOnClickListener(v -> {
            if (photo.isLiked()) {
                photo.setLikes(photo.getLikes() - 1);
                photo.setLiked(false);
            } else {
                photo.setLikes(photo.getLikes() + 1);
                photo.setLiked(true);
            }
            updateLikeButton(btnLike);
        });


        findViewById(R.id.btn_share).setOnClickListener(v -> {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_TEXT,
                    photo.getTitle() + " - " + photo.getLocation().getName()
                            + "\n\n" + photo.getDescription());
            startActivity(Intent.createChooser(shareIntent, "Partager via"));
        });

        findViewById(R.id.btn_report).setOnClickListener(v ->
                Toast.makeText(this, "Photo signalée. Merci pour votre vigilance.", Toast.LENGTH_SHORT).show());

        String initial = photo.getAuthor().getName().substring(0, 1).toUpperCase();
        ((TextView) findViewById(R.id.tv_author_initial)).setText(initial);
        ((TextView) findViewById(R.id.tv_author_name)).setText(photo.getAuthor().getName());
        ((TextView) findViewById(R.id.tv_created_at)).setText(
                "Publié le " + formatDate(photo.getCreatedAt()));

        ((TextView) findViewById(R.id.tv_location_name)).setText(photo.getLocation().getName());
        ((TextView) findViewById(R.id.tv_location_type)).setText(
                photo.getLocation().isApproximate() ? "📍 Position approximative" : "📍 Position exacte");
        ((TextView) findViewById(R.id.tv_date_period)).setText(
                photo.getDate() + " • " + photo.getPeriod());

        ((TextView) findViewById(R.id.tv_coordinates)).setText(
                String.format("%.4f, %.4f",
                        photo.getLocation().getLat(),
                        photo.getLocation().getLng()));

        if (photo.getDirections() != null && !photo.getDirections().isEmpty()) {
            ((TextView) findViewById(R.id.tv_directions)).setText(photo.getDirections());
            findViewById(R.id.btn_navigate).setOnClickListener(v -> openMaps());
        } else {
            findViewById(R.id.tv_directions_label).setVisibility(android.view.View.GONE);
            ((TextView) findViewById(R.id.tv_directions)).setVisibility(android.view.View.GONE);
            findViewById(R.id.btn_navigate).setVisibility(android.view.View.GONE);
        }

        ChipGroup chipGroup = findViewById(R.id.chip_group_tags);
        for (String tag : photo.getTags()) {
            Chip chip = new Chip(this);
            chip.setText("#" + tag);
            chip.setClickable(false);
            chipGroup.addView(chip);
        }

        LinearLayout commentsContainer = findViewById(R.id.comments_container);
        ((TextView) findViewById(R.id.tv_comments_count))
                .setText("Commentaires (" + photo.getComments().size() + ")");

        for (String comment : photo.getComments()) {
            addCommentView(commentsContainer, comment);
        }
    }

    private void updateLikeButton(Button btnLike) {
        btnLike.setText(photo.isLiked() ? "❤️ " + photo.getLikes() : "🤍 " + photo.getLikes());
    }

    private void addCommentView(LinearLayout container, String comment) {
        // Inflate dynamiquement une ligne de commentaire
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setPadding(0, 8, 0, 8);

        TextView initial = new TextView(this);
        initial.setText("U");
        initial.setTextSize(16f);
        initial.setPadding(0, 0, 16, 0);

        TextView text = new TextView(this);
        text.setText(comment);
        text.setTextSize(14f);

        row.addView(initial);
        row.addView(text);
        container.addView(row);
    }

    private void openMaps() {
        double lat = photo.getLocation().getLat();
        double lng = photo.getLocation().getLng();
        Uri gmmIntentUri = Uri.parse("geo:" + lat + "," + lng + "?q=" + lat + "," + lng
                + "(" + Uri.encode(photo.getLocation().getName()) + ")");
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        mapIntent.setPackage("com.google.android.apps.maps");
        if (mapIntent.resolveActivity(getPackageManager()) != null) {
            startActivity(mapIntent);
        } else {
            // Fallback navigateur
            Uri webUri = Uri.parse("https://maps.google.com/?q=" + lat + "," + lng);
            startActivity(new Intent(Intent.ACTION_VIEW, webUri));
        }
    }

    private String formatDate(String isoDate) {
        try {
            String[] parts = isoDate.split("T")[0].split("-");
            String[] months = {"jan", "fév", "mar", "avr", "mai", "juin",
                    "juil", "août", "sep", "oct", "nov", "déc"};
            int month = Integer.parseInt(parts[1]) - 1;
            return parts[2] + " " + months[month] + " " + parts[0];
        } catch (Exception e) {
            return isoDate;
        }
    }
}
