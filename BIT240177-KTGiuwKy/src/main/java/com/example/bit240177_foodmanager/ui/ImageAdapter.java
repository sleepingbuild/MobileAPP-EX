package com.example.bit240177_foodmanager.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.bit240177_foodmanager.R;
import com.example.bit240177_foodmanager.model.DishImage;
import java.util.ArrayList;
import java.util.List;

public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.VH> {

    public interface OnImageClick { void onClick(DishImage image); }

    private final List<DishImage> items = new ArrayList<>();
    private final OnImageClick listener;

    public ImageAdapter(OnImageClick listener) { this.listener = listener; }

    public void submit(List<DishImage> newItems) {
        items.clear();
        items.addAll(newItems);
        notifyDataSetChanged();
    }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new VH(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_image, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        DishImage img = items.get(position);
        Glide.with(h.itemView.getContext()).load(img.imageUrl)
                .placeholder(android.R.drawable.ic_menu_gallery)
                .error(android.R.drawable.ic_menu_report_image)
                .centerCrop().into(h.imgPhoto);
        h.tvBadge.setVisibility(img.isThumbnail ? View.VISIBLE : View.GONE);
        h.itemView.setOnClickListener(v -> listener.onClick(img));
    }

    @Override public int getItemCount() { return items.size(); }

    static class VH extends RecyclerView.ViewHolder {
        ImageView imgPhoto;
        TextView tvBadge;
        VH(View v) {
            super(v);
            imgPhoto = v.findViewById(R.id.imgPhoto);
            tvBadge = v.findViewById(R.id.tvBadge);
        }
    }
}