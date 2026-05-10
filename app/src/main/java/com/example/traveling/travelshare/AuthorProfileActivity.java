package com.example.traveling.travelshare;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.traveling.R;
import com.example.traveling.travelshare.adapter.PhotoAdapter;
import com.example.traveling.travelshare.data.FirestoreManager;
import com.example.traveling.travelshare.model.Author;
import com.example.traveling.travelshare.model.Photo;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.List;

public class AuthorProfileActivity extends AppCompatActivity {

    private String authorId;
    private Author author;
    private FirestoreManager firestoreManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ts_activity_author_profile);

        firestoreManager = new FirestoreManager();
        authorId = getIntent().getStringExtra("authorId");

        if (authorId == null || "user_current".equals(authorId)) {
            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
            if (currentUser != null) {
                authorId = currentUser.getUid();
            } else {
                Toast.makeText(this, "Utilisateur non connecté", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }
        }

        loadAuthorAndPhotos();
    }

    private void loadAuthorAndPhotos() {
        firestoreManager.getUser(authorId, new FirestoreManager.OnDataLoadedListener<Author>() {
            @Override
            public void onSuccess(Author loadedAuthor) {
                author = loadedAuthor;
                loadPhotos();
            }

            @Override
            public void onError(Exception e) {
                loadPhotos();
            }
        });
    }

    private void loadPhotos() {
        firestoreManager.getPhotosByAuthor(authorId, new FirestoreManager.OnDataLoadedListener<List<Photo>>() {
            @Override
            public void onSuccess(List<Photo> photos) {
                if (author == null && !photos.isEmpty()) {
                    author = photos.get(0).getAuthor();
                }

                if (author != null) {
                    bindViews(photos);
                } else {
                    FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                    if (authorId.equals(currentUser.getUid())) {
                        author = new Author(authorId, currentUser.getDisplayName() != null ? currentUser.getDisplayName() : "Moi", 
                                "https://api.dicebear.com/7.x/avataaars/svg?seed=" + authorId);
                        bindViews(photos);
                    } else {
                        Toast.makeText(AuthorProfileActivity.this, "Auteur introuvable", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                }
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(AuthorProfileActivity.this, "Erreur lors du chargement des photos", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void bindViews(List<Photo> photos) {
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        ((TextView) findViewById(R.id.tv_toolbar_title)).setText(author.getName());
        ((TextView) findViewById(R.id.tv_author_name)).setText(author.getName());
        ((TextView) findViewById(R.id.tv_photo_count)).setText(photos.size() + " photos publiées");

        ImageView ivAvatar = findViewById(R.id.iv_author_avatar);
        Glide.with(this)
                .load(author.getAvatar())
                .placeholder(R.drawable.ts_circle_avatar)
                .into(ivAvatar);

        RecyclerView recyclerView = findViewById(R.id.recycler_author_photos);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        recyclerView.setAdapter(new PhotoAdapter(this, photos, photo -> {
            Intent intent = new Intent(this, PhotoDetailActivity.class);
            intent.putExtra("photoId", photo.getId());
            startActivity(intent);
        }));
    }
}
