package com.example.traveling.travelshare.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.traveling.R;
import com.example.traveling.travelshare.data.FirestoreManager;
import com.example.traveling.travelshare.model.Group;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.List;

public class GroupAdapter extends RecyclerView.Adapter<GroupAdapter.GroupViewHolder> {

    private List<Group> groups;
    private Runnable onMembershipChanged;
    private FirestoreManager firestoreManager;

    public GroupAdapter(List<Group> groups, Runnable onMembershipChanged) {
        this.groups = groups;
        this.onMembershipChanged = onMembershipChanged;
        this.firestoreManager = new FirestoreManager();
    }

    public void updateGroups(List<Group> newGroups) {
        this.groups = newGroups;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public GroupViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.ts_item_group, parent, false);
        return new GroupViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GroupViewHolder holder, int position) {
        Group group = groups.get(position);
        holder.tvName.setText(group.getName());
        holder.tvDesc.setText(group.getDescription());
        
        Glide.with(holder.itemView.getContext())
                .load(group.getImageUrl())
                .centerCrop()
                .into(holder.ivGroup);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        boolean isJoined = user != null && group.getMemberIds().contains(user.getUid());
        
        holder.btnJoin.setText(isJoined ? "Quitter" : "Rejoindre");
        
        holder.btnJoin.setOnClickListener(v -> {
            if (user == null) {
                Toast.makeText(v.getContext(), "Veuillez vous connecter", Toast.LENGTH_SHORT).show();
                return;
            }

            holder.btnJoin.setEnabled(false);
            if (isJoined) {
                firestoreManager.leaveGroup(group.getId(), new FirestoreManager.OnDataLoadedListener<Void>() {
                    @Override
                    public void onSuccess(Void data) {
                        Toast.makeText(v.getContext(), "Vous avez quitté " + group.getName(), Toast.LENGTH_SHORT).show();
                        if (onMembershipChanged != null) onMembershipChanged.run();
                    }

                    @Override
                    public void onError(Exception e) {
                        holder.btnJoin.setEnabled(true);
                        Toast.makeText(v.getContext(), "Erreur", Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                firestoreManager.joinGroup(group.getId(), new FirestoreManager.OnDataLoadedListener<Void>() {
                    @Override
                    public void onSuccess(Void data) {
                        Toast.makeText(v.getContext(), "Vous avez rejoint " + group.getName(), Toast.LENGTH_SHORT).show();
                        if (onMembershipChanged != null) onMembershipChanged.run();
                    }

                    @Override
                    public void onError(Exception e) {
                        holder.btnJoin.setEnabled(true);
                        Toast.makeText(v.getContext(), "Erreur", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    @Override
    public int getItemCount() {
        return groups.size();
    }

    static class GroupViewHolder extends RecyclerView.ViewHolder {
        ImageView ivGroup;
        TextView tvName, tvDesc;
        Button btnJoin;

        public GroupViewHolder(@NonNull View itemView) {
            super(itemView);
            ivGroup = itemView.findViewById(R.id.iv_group);
            tvName = itemView.findViewById(R.id.tv_group_name);
            tvDesc = itemView.findViewById(R.id.tv_group_desc);
            btnJoin = itemView.findViewById(R.id.btn_join);
        }
    }
}
