package com.example.bit240177_foodmanager.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.bit240177_foodmanager.R;
import com.example.bit240177_foodmanager.model.DishCategory;
import java.util.ArrayList;
import java.util.List;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.VH> {

    public interface Listener {
        void onEdit(DishCategory c);
        void onDelete(DishCategory c);
    }

    private final List<DishCategory> items = new ArrayList<>();
    private final Listener listener;

    public CategoryAdapter(Listener listener) { this.listener = listener; }

    public void submit(List<DishCategory> newItems) {
        items.clear();
        items.addAll(newItems);
        notifyDataSetChanged();
    }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new VH(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_category, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        DishCategory c = items.get(position);
        h.tvName.setText(c.name);
        h.tvDesc.setText(c.description == null ? "" : c.description);
        h.itemView.setOnClickListener(v -> listener.onEdit(c));
        h.btnDelete.setOnClickListener(v -> listener.onDelete(c));
    }

    @Override public int getItemCount() { return items.size(); }

    public static class VH extends RecyclerView.ViewHolder {
        TextView tvName, tvDesc;
        Button btnDelete;
        VH(View v) {
            super(v);
            tvName = v.findViewById(R.id.tvCatName);
            tvDesc = v.findViewById(R.id.tvCatDesc);
            btnDelete = v.findViewById(R.id.btnCatDelete);
        }
    }
}