package com.example.coursemanager.adapter;

import android.view.*;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.coursemanager.R;
import com.example.coursemanager.model.Course;
import java.text.NumberFormat;
import java.util.*;

public class CourseAdapter extends RecyclerView.Adapter<CourseAdapter.VH> {
    public interface OnItemClick { void onClick(Course c); }

    private List<Course> items = new ArrayList<>();
    private Map<String, String> categoryNames = new HashMap<>();
    private final OnItemClick listener;

    public CourseAdapter(OnItemClick l) { this.listener = l; }

    public void setData(List<Course> data, Map<String, String> names) {
        this.items = data; this.categoryNames = names; notifyDataSetChanged();
    }

    @NonNull @Override public VH onCreateViewHolder(@NonNull ViewGroup p, int t) {
        return new VH(LayoutInflater.from(p.getContext()).inflate(R.layout.item_course, p, false));
    }

    @Override public void onBindViewHolder(@NonNull VH h, int i) {
        Course c = items.get(i);
        h.title.setText(c.getTitle());
        h.instructor.setText("GV: " + c.getInstructor());
        String cat = categoryNames.get(c.getCategoryId());
        h.category.setText(cat != null ? cat : "Chưa phân loại");
        h.price.setText(NumberFormat.getInstance(new Locale("vi", "VN")).format(c.getPrice()) + " đ");
        Glide.with(h.itemView).load(c.getThumbnail())
                .placeholder(android.R.drawable.ic_menu_gallery)
                .error(android.R.drawable.ic_delete).centerCrop().into(h.img);
        h.itemView.setOnClickListener(v -> listener.onClick(c));
    }

    @Override public int getItemCount() { return items.size(); }

    static class VH extends RecyclerView.ViewHolder {
        ImageView img; TextView title, instructor, category, price;
        VH(View v) {
            super(v);
            img = v.findViewById(R.id.imgThumb); title = v.findViewById(R.id.tvTitle);
            instructor = v.findViewById(R.id.tvInstructor); category = v.findViewById(R.id.tvCategory);
            price = v.findViewById(R.id.tvPrice);
        }
    }
}
