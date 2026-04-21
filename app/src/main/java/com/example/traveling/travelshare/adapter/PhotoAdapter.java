package com.example.traveling.travelshare.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.example.traveling.R;
import com.example.traveling.travelshare.model.Photo;

import java.util.List;

public class PhotoAdapter extends RecyclerView.Adapter<PhotoAdapter.PhotoViewHolder> {

    public interface OnPhotoClickListener {
        void onPhotoClick(Photo photo);
    }

    private final Context context;
    private List<Photo> photos;
    private final OnPhotoClickListener listener;

    public PhotoAdapter(Context context, List<Photo> photos, OnPhotoClickListener listener) {
        this.context = context;
        this.photos = photos;
        this.listener = listener;
    }

    public void updatePhotos(List<Photo> newPhotos) {
        this.photos = newPhotos;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public PhotoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_photo, parent, false);
        return new PhotoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PhotoViewHolder holder, int position) {
        Photo photo = photos.get(position);

        // Image avec Glide
        Glide.with(context)
                .load(photo.getImageUrl())
                .transition(DrawableTransitionOptions.withCrossFade())
                .centerCrop()
                .placeholder(R.color.purple_light)
                .into(holder.ivPhoto);

        holder.tvTitle.setText(photo.getTitle());
        holder.tvDescription.setText(photo.getDescription());
        holder.tvLocation.setText(photo.getLocation().getName());
        holder.tvLikes.setText("❤️ " + photo.getLikes());

        // Initiale auteur
        String initial = photo.getAuthor().getName().substring(0, 1).toUpperCase();
        holder.tvAuthorInitial.setText(initial);
        holder.tvAuthorName.setText(photo.getAuthor().getName());

        // Tags (max 3)
        holder.chipGroupTags.removeAllViews();
        List<String> tags = photo.getTags();
        int count = Math.min(tags.size(), 3);
        for (int i = 0; i < count; i++) {
            Chip chip = new Chip(context);
            chip.setText("#" + tags.get(i));
            chip.setTextSize(10f);
            chip.setClickable(false);
            holder.chipGroupTags.addView(chip);
        }

        holder.itemView.setOnClickListener(v -> listener.onPhotoClick(photo));
    }

    @Override
    public int getItemCount() {
        return photos != null ? photos.size() : 0;
    }

    static class PhotoViewHolder extends RecyclerView.ViewHolder {
        ImageView ivPhoto;
        TextView tvTitle, tvDescription, tvLocation, tvLikes;
        TextView tvAuthorInitial, tvAuthorName;
        ChipGroup chipGroupTags;

        PhotoViewHolder(@NonNull View itemView) {
            super(itemView);
            ivPhoto          = itemView.findViewById(R.id.iv_photo);
            tvTitle          = itemView.findViewById(R.id.tv_title);
            tvDescription    = itemView.findViewById(R.id.tv_description);
            tvLocation       = itemView.findViewById(R.id.tv_location);
            tvLikes          = itemView.findViewById(R.id.tv_likes);
            tvAuthorInitial  = itemView.findViewById(R.id.tv_author_initial);
            tvAuthorName     = itemView.findViewById(R.id.tv_author_name);
            chipGroupTags    = itemView.findViewById(R.id.chip_group_tags);
        }
    }
}
