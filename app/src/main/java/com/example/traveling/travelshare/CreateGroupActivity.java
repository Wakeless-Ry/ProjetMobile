package com.example.traveling.travelshare;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.traveling.R;
import com.example.traveling.travelshare.data.FirestoreManager;
import com.example.traveling.travelshare.model.Group;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.UUID;

public class CreateGroupActivity extends AppCompatActivity {

    private EditText etName, etDescription;
    private Button btnConfirm;
    private FirestoreManager firestoreManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ts_activity_create_group);

        firestoreManager = new FirestoreManager();
        etName = findViewById(R.id.et_group_name);
        etDescription = findViewById(R.id.et_group_description);
        btnConfirm = findViewById(R.id.btn_confirm_create);

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        btnConfirm.setOnClickListener(v -> handleCreateGroup());

        findViewById(R.id.card_group_image).setOnClickListener(v -> 
            Toast.makeText(this, "Simulation: Image sélectionnée", Toast.LENGTH_SHORT).show()
        );
    }

    private void handleCreateGroup() {
        String name = etName.getText().toString().trim();
        String desc = etDescription.getText().toString().trim();

        if (name.isEmpty() || desc.isEmpty()) {
            Toast.makeText(this, "Veuillez remplir tous les champs", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "Vous devez être connecté pour créer un groupe", Toast.LENGTH_SHORT).show();
            return;
        }

        String imageUrl = "https://images.unsplash.com/photo-1527631746610-bca00a040d60?w=400";
        
        Group newGroup = new Group(
            UUID.randomUUID().toString(),
            name,
            desc,
            imageUrl
        );
        newGroup.getMemberIds().add(user.getUid());

        btnConfirm.setEnabled(false);
        firestoreManager.addGroup(newGroup, new FirestoreManager.OnDataLoadedListener<Void>() {
            @Override
            public void onSuccess(Void data) {
                Toast.makeText(CreateGroupActivity.this, "Groupe '" + name + "' créé !", Toast.LENGTH_SHORT).show();
                finish();
            }

            @Override
            public void onError(Exception e) {
                btnConfirm.setEnabled(true);
                Toast.makeText(CreateGroupActivity.this, "Erreur lors de la création du groupe", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
