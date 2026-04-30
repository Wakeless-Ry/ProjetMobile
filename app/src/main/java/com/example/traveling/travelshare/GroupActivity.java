package com.example.traveling.travelshare;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.traveling.R;
import com.example.traveling.travelshare.adapter.GroupAdapter;
import com.example.traveling.travelshare.data.FirestoreManager;
import com.example.traveling.travelshare.model.Group;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;

public class GroupActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private GroupAdapter adapter;
    private TabLayout tabLayout;
    private FirestoreManager firestoreManager;
    private boolean showingMyGroups = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ts_activity_groups);

        firestoreManager = new FirestoreManager();
        tabLayout = findViewById(R.id.tab_layout_groups);
        recyclerView = findViewById(R.id.recycler_groups);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        
        adapter = new GroupAdapter(new ArrayList<>(), this::refreshList);
        recyclerView.setAdapter(adapter);

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
        
        findViewById(R.id.btn_create_group).setOnClickListener(v -> 
            startActivity(new Intent(this, CreateGroupActivity.class))
        );

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                showingMyGroups = (tab.getPosition() == 0);
                refreshList();
            }
            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void refreshList() {
        firestoreManager.getAllGroups(new FirestoreManager.OnDataLoadedListener<List<Group>>() {
            @Override
            public void onSuccess(List<Group> groups) {
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                List<Group> filtered = new ArrayList<>();
                
                if (showingMyGroups) {
                    if (user != null) {
                        for (Group g : groups) {
                            if (g.getMemberIds().contains(user.getUid())) filtered.add(g);
                        }
                    }
                } else {
                    for (Group g : groups) {
                        if (user == null || !g.getMemberIds().contains(user.getUid())) {
                            filtered.add(g);
                        }
                    }
                }
                adapter.updateGroups(filtered);
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(GroupActivity.this, "Erreur chargement groupes", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshList();
    }
}
