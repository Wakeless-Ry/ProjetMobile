package com.example.traveling.travelshare;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.traveling.R;
import com.google.android.material.tabs.TabLayout;

public class LoginActivity extends AppCompatActivity {

    private TabLayout tabLayout;
    private EditText etLoginEmail, etLoginPassword;
    private EditText etSignupName, etSignupEmail, etSignupPassword;
    private Button btnAction;
    private TextView tvSwitchMode;

    private boolean isLoginMode = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Bouton retour
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        tabLayout         = findViewById(R.id.tab_layout);
        etLoginEmail      = findViewById(R.id.et_login_email);
        etLoginPassword   = findViewById(R.id.et_login_password);
        etSignupName      = findViewById(R.id.et_signup_name);
        etSignupEmail     = findViewById(R.id.et_signup_email);
        etSignupPassword  = findViewById(R.id.et_signup_password);
        btnAction         = findViewById(R.id.btn_action);
        tvSwitchMode      = findViewById(R.id.tv_switch_mode);

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

        updateUI();
    }

    private void updateUI() {
        if (isLoginMode) {
            etLoginEmail.setVisibility(android.view.View.VISIBLE);
            etLoginPassword.setVisibility(android.view.View.VISIBLE);
            etSignupName.setVisibility(android.view.View.GONE);
            etSignupEmail.setVisibility(android.view.View.GONE);
            etSignupPassword.setVisibility(android.view.View.GONE);
            btnAction.setText("Se connecter");
        } else {
            etLoginEmail.setVisibility(android.view.View.GONE);
            etLoginPassword.setVisibility(android.view.View.GONE);
            etSignupName.setVisibility(android.view.View.VISIBLE);
            etSignupEmail.setVisibility(android.view.View.VISIBLE);
            etSignupPassword.setVisibility(android.view.View.VISIBLE);
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

        // Simulation login (pas de vrai backend)
        String userName = email.contains("@") ? email.split("@")[0] : email;
        saveSession(userName);
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

        saveSession(name);
    }

    private void saveSession(String userName) {
        SharedPreferences prefs = getSharedPreferences("travelshare", MODE_PRIVATE);
        prefs.edit()
                .putBoolean("isLoggedIn", true)
                .putString("userName", userName)
                .apply();

        Toast.makeText(this, "Bienvenue " + userName + " !", Toast.LENGTH_SHORT).show();

        Intent intent = new Intent(this, PhotoHomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }
}