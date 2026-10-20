package com.example.bit240177_foodmanager.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.bit240177_foodmanager.R;
import com.example.bit240177_foodmanager.model.DishListItem;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class DishAdapter extends RecyclerView.Adapter<DishAdapter.VH> {

    public interface OnItemClick { void onClick(DishListItem item); }

    private final List<DishListItem> items = new ArrayList<>();
    private final OnItemClick listener;

    public DishAdapter(OnItemClick listener) { this.listener = listener; }

    public void submit(List<DishListItem> newItems) {
        items.clear();
        items.addAll(newItems);
        notifyDataSetChanged();
    }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_dish, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        DishListItem it = items.get(position);
        Context ctx = h.itemView.getContext();

        android.util.Log.d("FoodManager", "bind position=" + position);

        h.tvName.setText(it.dish.name);
        h.tvCategory.setText(it.categoryName);
        h.tvPrice.setText(ctx.getString(R.string.price_format,
                NumberFormat.getInstance(Locale.US).format(it.dish.price)));

        // Quick khi PreparationTime <= 15 phút, ngược lại Regular
        String type = ctx.getString(it.dish.preparationTime <= 15 ? R.string.quick : R.string.regular);
        h.tvPrep.setText(ctx.getString(R.string.minutes_format, it.dish.preparationTime) + " - " + type);

        h.tvStatus.setText(it.dish.isAvailable ? "" : ctx.getString(R.string.unavailable));
        h.tvStatus.setVisibility(it.dish.isAvailable ? View.GONE : View.VISIBLE);

        Glide.with(ctx).load(it.thumbnailUrl)
                .placeholder(android.R.drawable.ic_menu_gallery)
                .error(android.R.drawable.ic_menu_report_image)
                .centerCrop().into(h.imgThumb);

        h.itemView.setOnClickListener(v -> listener.onClick(it));
    }

    @Override public int getItemCount() { return items.size(); }

    static class VH extends RecyclerView.ViewHolder {
        ImageView imgThumb;
        TextView tvName, tvCategory, tvPrice, tvPrep, tvStatus;
        VH(View v) {
            super(v);
            imgThumb = v.findViewById(R.id.imgThumb);
            tvName = v.findViewById(R.id.tvName);
            tvCategory = v.findViewById(R.id.tvCategory);
            tvPrice = v.findViewById(R.id.tvPrice);
            tvPrep = v.findViewById(R.id.tvPrep);
            tvStatus = v.findViewById(R.id.tvStatus);
        }
    }
}