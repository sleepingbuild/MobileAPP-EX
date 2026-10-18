package com.example.coursemanager.adapter;

import android.view.*;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.coursemanager.R;
import com.example.coursemanager.model.Category;
import java.util.*;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.VH> {
    public interface Listener { void onEdit(Category c); void onDelete(Category c); }

    private List<Category> items = new ArrayList<>();
    private final Listener listener;

    public CategoryAdapter(Listener l) { this.listener = l; }

    public void setData(List<Category> data) { this.items = data; notifyDataSetChanged(); }

    @NonNull @Override public VH onCreateViewHolder(@NonNull ViewGroup p, int t) {
        return new VH(LayoutInflater.from(p.getContext()).inflate(R.layout.item_category, p, false));
    }

    @Override public void onBindViewHolder(@NonNull VH h, int i) {
        Category c = items.get(i);
        h.name.setText(c.getName());
        h.desc.setText(c.getDescription());
        h.edit.setOnClickListener(v -> listener.onEdit(c));
        h.delete.setOnClickListener(v -> listener.onDelete(c));
    }

    @Override public int getItemCount() { return items.size(); }

    static class VH extends RecyclerView.ViewHolder {
        TextView name, desc; Button edit, delete;
        VH(View v) {
            super(v);
            name = v.findViewById(R.id.tvCatName); desc = v.findViewById(R.id.tvCatDesc);
            edit = v.findViewById(R.id.btnEditCat); delete = v.findViewById(R.id.btnDeleteCat);
        }
    }
}
