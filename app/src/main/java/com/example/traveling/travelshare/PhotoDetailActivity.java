package com.example.traveling.travelshare;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.traveling.R;
import com.example.traveling.travelshare.adapter.PhotoAdapter;
import com.example.traveling.travelshare.data.FirestoreManager;
import com.example.traveling.travelshare.model.Photo;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;

public class PhotoDetailActivity extends AppCompatActivity {

    private Photo photo;
    private FirestoreManager firestoreManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ts_activity_photo_detail);

        firestoreManager = new FirestoreManager();

        // Récupère le photo via son id depuis Firestore
        String photoId = getIntent().getStringExtra("photoId");
        if (photoId == null) {
            finish();
            return;
        }

        firestoreManager.getPhoto(photoId, new FirestoreManager.OnDataLoadedListener<Photo>() {
            @Override
            public void onSuccess(Photo loadedPhoto) {
                photo = loadedPhoto;
                bindViews();
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(PhotoDetailActivity.this, "Photo introuvable", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
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
            boolean wasLiked = photo.isLiked();
            photo.setLiked(!wasLiked);
            photo.setLikes(photo.getLikes() + (wasLiked ? -1 : 1));
            updateLikeButton(btnLike);
            
            firestoreManager.likePhoto(photo.getId(), !wasLiked, new FirestoreManager.OnDataLoadedListener<Void>() {
                @Override public void onSuccess(Void data) {}
                @Override public void onError(Exception e) {
                    // Revert if error
                    photo.setLiked(wasLiked);
                    photo.setLikes(photo.getLikes() + (wasLiked ? 1 : -1));
                    updateLikeButton(btnLike);
                }
            });
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

        String name = photo.getAuthor().getName();
        String initial = (name != null && !name.isEmpty()) ? name.substring(0, 1).toUpperCase() : "?";
        ((TextView) findViewById(R.id.tv_author_initial)).setText(initial);
        TextView tvAuthorName = findViewById(R.id.tv_author_name);
        tvAuthorName.setText(name);
        ((TextView) findViewById(R.id.tv_created_at)).setText(
                "Publié le " + formatDate(photo.getCreatedAt()));

        View.OnClickListener openProfile = v -> {
            Intent intent = new Intent(this, AuthorProfileActivity.class);
            intent.putExtra("authorId", photo.getAuthor().getId());
            startActivity(intent);
        };
        tvAuthorName.setOnClickListener(openProfile);
        findViewById(R.id.tv_author_initial).setOnClickListener(openProfile);

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
        chipGroup.removeAllViews();
        if (photo.getTags() != null) {
            for (String tag : photo.getTags()) {
                Chip chip = new Chip(this);
                chip.setText("#" + tag);
                chip.setClickable(false);
                chipGroup.addView(chip);
            }
        }

        LinearLayout commentsContainer = findViewById(R.id.comments_container);
        commentsContainer.removeAllViews();
        ((TextView) findViewById(R.id.tv_comments_count))
                .setText("Commentaires (" + (photo.getComments() != null ? photo.getComments().size() : 0) + ")");

        if (photo.getComments() != null) {
            for (String comment : photo.getComments()) {
                addCommentView(commentsContainer, comment);
            }
        }

        setupSimilarPhotos();
        setupCommentSection();
    }

    private void setupSimilarPhotos() {
        RecyclerView recyclerSimilar = findViewById(R.id.recycler_similar_photos);
        firestoreManager.getPhotos("all", photo.getLocationType(), new FirestoreManager.OnDataLoadedListener<List<Photo>>() {
            @Override
            public void onSuccess(List<Photo> photos) {
                List<Photo> similar = new ArrayList<>();
                for (Photo p : photos) {
                    if (!p.getId().equals(photo.getId())) {
                        similar.add(p);
                        if (similar.size() >= 5) break;
                    }
                }

                if (similar.isEmpty()) {
                    recyclerSimilar.setVisibility(View.GONE);
                    return;
                }

                PhotoAdapter adapter = new PhotoAdapter(PhotoDetailActivity.this, similar, p -> {
                    Intent intent = new Intent(PhotoDetailActivity.this, PhotoDetailActivity.class);
                    intent.putExtra("photoId", p.getId());
                    startActivity(intent);
                    finish();
                });

                recyclerSimilar.setLayoutManager(new LinearLayoutManager(PhotoDetailActivity.this, LinearLayoutManager.HORIZONTAL, false));
                recyclerSimilar.setAdapter(adapter);
            }

            @Override
            public void onError(Exception e) {
                recyclerSimilar.setVisibility(View.GONE);
            }
        });
    }

    private void setupCommentSection() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        boolean loggedIn = user != null;

        View layoutAddComment = findViewById(R.id.layout_add_comment);
        View tvLoginToComment = findViewById(R.id.tv_login_to_comment);
        EditText etComment = findViewById(R.id.et_comment);
        View btnSend = findViewById(R.id.btn_send_comment);

        if (loggedIn) {
            layoutAddComment.setVisibility(View.VISIBLE);
            tvLoginToComment.setVisibility(View.GONE);

            btnSend.setOnClickListener(v -> {
                String text = etComment.getText().toString().trim();
                if (!text.isEmpty()) {
                    if (photo.getComments() == null) photo.setComments(new ArrayList<>());
                    photo.getComments().add(text);
                    addCommentView(findViewById(R.id.comments_container), text);
                    etComment.setText("");
                    ((TextView) findViewById(R.id.tv_comments_count))
                            .setText("Commentaires (" + photo.getComments().size() + ")");
                    
                    firestoreManager.addComment(photo.getId(), text, new FirestoreManager.OnDataLoadedListener<Void>() {
                        @Override public void onSuccess(Void data) {}
                        @Override public void onError(Exception e) {
                            Toast.makeText(PhotoDetailActivity.this, "Erreur lors de l'envoi du commentaire", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            });
        } else {
            layoutAddComment.setVisibility(View.GONE);
            tvLoginToComment.setVisibility(View.VISIBLE);
        }
    }

    private void updateLikeButton(Button btnLike) {
        btnLike.setText(photo.isLiked() ? "❤️ " + photo.getLikes() : "🤍 " + photo.getLikes());
    }

    private void addCommentView(LinearLayout container, String comment) {
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
            Uri webUri = Uri.parse("https://maps.google.com/?q=" + lat + "," + lng);
            startActivity(new Intent(Intent.ACTION_VIEW, webUri));
        }
    }

    private String formatDate(String isoDate) {
        if (isoDate == null) return "Récemment";
        try {
            String datePart = isoDate.contains("T") ? isoDate.split("T")[0] : isoDate;
            String[] parts = datePart.split("-");
            String[] months = {"jan", "fév", "mar", "avr", "mai", "juin",
                    "juil", "août", "sep", "oct", "nov", "déc"};
            int month = Integer.parseInt(parts[1]) - 1;
            return parts[2] + " " + months[month] + " " + parts[0];
        } catch (Exception e) {
            return isoDate;
        }
    }
}
