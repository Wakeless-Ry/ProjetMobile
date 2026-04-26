package com.example.traveling.travelpath;

import static android.view.View.INVISIBLE;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.traveling.R;
import com.example.traveling.travelpath.Place;
import java.util.ArrayList;
import java.util.List;

public class PlaceAdapter extends RecyclerView.Adapter<PlaceAdapter.PlaceViewHolder> {

    private List<Place> places;
    private PlaceSelectionListener selectionListener;

    private boolean setCheckboxes;

    public interface PlaceSelectionListener {
        void onPlaceSelected(Place place, boolean isSelected);
        void onPlaceClick(Place place);
    }

    public PlaceAdapter(List<Place> places) {
        this.places = places != null ? places : new ArrayList<>();
    }

    public PlaceAdapter(List<Place> places, PlaceSelectionListener listener, boolean setCheckboxes) {
        this.places = places != null ? places : new ArrayList<>();
        this.selectionListener = listener;
        this.setCheckboxes = setCheckboxes;
    }

    @NonNull
    @Override
    public PlaceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.tp_item_place, parent, false);
        return new PlaceViewHolder(view, this.setCheckboxes);
    }

    @Override
    public void onBindViewHolder(@NonNull PlaceViewHolder holder, int position) {
        Place place = places.get(position);
        holder.bind(place);
    }

    @Override
    public int getItemCount() {
        return places.size();
    }

    public void setPlaces(List<Place> places) {
        this.places = places != null ? places : new ArrayList<>();
        notifyDataSetChanged();
    }

    public void addPlace(Place place) {
        this.places.add(place);
        notifyItemInserted(this.places.size() - 1);
    }

    public void removePlace(Place place) {
        int index = this.places.indexOf(place);
        if (index >= 0) {
            this.places.remove(index);
            notifyItemRemoved(index);
        }
    }

    public List<Place> getSelectedPlaces() {
        List<Place> selected = new ArrayList<>();
        for (Place place : places) {
            if (place.isSelected()) {
                selected.add(place);
            }
        }
        return selected;
    }

    public void clearSelections() {
        for (Place place : places) {
            place.setSelected(false);
        }
        notifyDataSetChanged();
    }

    public void selectAll() {
        for (Place place : places) {
            place.setSelected(true);
        }
        notifyDataSetChanged();
    }

    public void setSelectionListener(PlaceSelectionListener listener) {
        this.selectionListener = listener;
    }

    public class PlaceViewHolder extends RecyclerView.ViewHolder {
        private CheckBox checkboxPlace;
        private TextView textPlaceName;
        private TextView textTags;
        private TextView textPrice;
        private TextView textLocation;
        private TextView textTimeSpent;
        private Place currentPlace;

        public PlaceViewHolder(@NonNull View itemView, boolean setCheckboxes) {
            super(itemView);
            checkboxPlace = itemView.findViewById(R.id.checkbox_place);
            if (!setCheckboxes) {
                checkboxPlace.setVisibility(INVISIBLE);
            }
            textPlaceName = itemView.findViewById(R.id.text_place_name);
            textTags = itemView.findViewById(R.id.text_tags);
            textPrice = itemView.findViewById(R.id.text_price);
            textLocation = itemView.findViewById(R.id.text_location);
            textTimeSpent = itemView.findViewById(R.id.text_time_spent);

            checkboxPlace.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (currentPlace != null) {
                    currentPlace.setSelected(isChecked);
                    if (selectionListener != null) {
                        selectionListener.onPlaceSelected(currentPlace, isChecked);
                    }
                }
            });

            itemView.setOnClickListener(v -> {
                if (currentPlace != null) {
                    if (selectionListener != null) {
                        selectionListener.onPlaceClick(currentPlace);
                    }
                }
            });
        }

        public void bind(Place place) {
            this.currentPlace = place;

            textPlaceName.setText(place.getName());
            textTags.setText(place.getTagsAsString().isEmpty() ? "Pas de catégories" : place.getTagsAsString());
            textPrice.setText(place.getFormattedPrice());
            textLocation.setText(place.getFormattedLocation());
            textTimeSpent.setText(place.getFormattedTimeSpent());

            checkboxPlace.setOnCheckedChangeListener(null);
            checkboxPlace.setChecked(place.isSelected());
            checkboxPlace.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (currentPlace != null) {
                    currentPlace.setSelected(isChecked);
                    if (selectionListener != null) {
                        selectionListener.onPlaceSelected(currentPlace, isChecked);
                    }
                }
            });
        }
    }
}
