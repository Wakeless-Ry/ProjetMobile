package com.example.traveling.travelshare;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.traveling.R;
import com.example.traveling.travelshare.data.FirestoreManager;
import com.example.traveling.travelshare.model.Author;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

public class LoginActivity extends AppCompatActivity {

    private TabLayout tabLayout;
    private View layoutLoginEmail, layoutLoginPassword;
    private View layoutSignupName, layoutSignupEmail, layoutSignupPassword;
    private EditText etLoginEmail, etLoginPassword;
    private EditText etSignupName, etSignupEmail, etSignupPassword;
    private Button btnAction;

    private FirebaseAuth mAuth;
    private FirestoreManager firestoreManager;
    private boolean isLoginMode = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ts_activity_login);

        mAuth = FirebaseAuth.getInstance();
        firestoreManager = new FirestoreManager();

        // Bouton retour
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        tabLayout             = findViewById(R.id.tab_layout);
        
        layoutLoginEmail      = findViewById(R.id.layout_login_email);
        layoutLoginPassword   = findViewById(R.id.layout_login_password);
        layoutSignupName      = findViewById(R.id.layout_signup_name);
        layoutSignupEmail     = findViewById(R.id.layout_signup_email);
        layoutSignupPassword  = findViewById(R.id.layout_signup_password);

        etLoginEmail          = findViewById(R.id.et_login_email);
        etLoginPassword       = findViewById(R.id.et_login_password);
        etSignupName          = findViewById(R.id.et_signup_name);
        etSignupEmail         = findViewById(R.id.et_signup_email);
        etSignupPassword      = findViewById(R.id.et_signup_password);
        
        btnAction             = findViewById(R.id.btn_action);

        // Onglets Connexion / Inscription
        tabLayout.addTab(tabLayout.newTab().setText("Connexion"));
        tabLayout.addTab(tabLayout.newTab().setText("Inscription"));

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override public void onTabSelected(TabLayout.Tab tab) {
                isLoginMode = (tab.getPosition() == 0);
                updateUI();
            }
            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });

        btnAction.setOnClickListener(v -> {
            if (isLoginMode) handleLogin();
            else handleSignup();
        });

        findViewById(R.id.btn_demo).setOnClickListener(v -> loginDemo());

        updateUI();
    }

    private void updateUI() {
        if (isLoginMode) {
            layoutLoginEmail.setVisibility(View.VISIBLE);
            layoutLoginPassword.setVisibility(View.VISIBLE);
            layoutSignupName.setVisibility(View.GONE);
            layoutSignupEmail.setVisibility(View.GONE);
            layoutSignupPassword.setVisibility(View.GONE);
            btnAction.setText("Se connecter");
        } else {
            layoutLoginEmail.setVisibility(View.GONE);
            layoutLoginPassword.setVisibility(View.GONE);
            layoutSignupName.setVisibility(View.VISIBLE);
            layoutSignupEmail.setVisibility(View.VISIBLE);
            layoutSignupPassword.setVisibility(View.VISIBLE);
            btnAction.setText("Créer un compte");
        }
    }

    private void handleLogin() {
        String email    = etLoginEmail.getText().toString().trim();
        String password = etLoginPassword.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            etLoginEmail.setError("Email requis");
            return;
        }
        if (TextUtils.isEmpty(password)) {
            etLoginPassword.setError("Mot de passe requis");
            return;
        }

        btnAction.setEnabled(false);
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    btnAction.setEnabled(true);
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        onLoginSuccess(user != null ? user.getDisplayName() : "Voyageur");
                    } else {
                        Toast.makeText(LoginActivity.this, "Échec de connexion : " + task.getException().getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void handleSignup() {
        String name     = etSignupName.getText().toString().trim();
        String email    = etSignupEmail.getText().toString().trim();
        String password = etSignupPassword.getText().toString().trim();

        if (TextUtils.isEmpty(name)) {
            etSignupName.setError("Nom requis");
            return;
        }
        if (TextUtils.isEmpty(email)) {
            etSignupEmail.setError("Email requis");
            return;
        }
        if (password.length() < 6) {
            etSignupPassword.setError("Mot de passe trop court (6 car. min)");
            return;
        }

        btnAction.setEnabled(false);
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                    .setDisplayName(name)
                                    .build();
                            
                            user.updateProfile(profileUpdates).addOnCompleteListener(task1 -> {
                                // Une fois le profil Auth mis à jour, on crée le document dans Firestore
                                Author newAuthor = new Author(user.getUid(), name, 
                                        "https://api.dicebear.com/7.x/avataaars/svg?seed=" + name);
                                
                                firestoreManager.createUser(newAuthor, new FirestoreManager.OnDataLoadedListener<Void>() {
                                    @Override
                                    public void onSuccess(Void data) {
                                        btnAction.setEnabled(true);
                                        onLoginSuccess(name);
                                    }

                                    @Override
                                    public void onError(Exception e) {
                                        btnAction.setEnabled(true);
                                        // On continue quand même car le compte Auth est créé
                                        onLoginSuccess(name);
                                    }
                                });
                            });
                        }
                    } else {
                        btnAction.setEnabled(true);
                        Toast.makeText(LoginActivity.this, "Échec d'inscription : " + task.getException().getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void loginDemo() {
        Toast.makeText(this, "Mode démo : Connexion Alice (Simulation)", Toast.LENGTH_SHORT).show();
        onLoginSuccess("Alice");
    }

    private void onLoginSuccess(String userName) {
        Toast.makeText(this, "Bienvenue " + userName + " !", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(this, PhotoHomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }
}
