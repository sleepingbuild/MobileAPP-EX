package com.example.coursemanager.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AlertDialog;
import com.bumptech.glide.Glide;
import com.example.coursemanager.R;
import com.example.coursemanager.api.*;
import com.example.coursemanager.model.*;
import java.text.NumberFormat;
import java.util.*;

public class CourseDetailActivity extends BaseActivity {
    private String courseId;
    private Course course;
    private List<Category> categories;
    private String catName = "Đang tải...";
    private ImageView img;
    private TextView tvTitle, tvCategory, tvInstructor, tvPrice, tvDuration, tvCreated, tvDesc, tvError;
    private ProgressBar progress;
    private View content;

    @Override protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_detail);
        setTitle("Chi tiết khóa học");
        courseId = getIntent().getStringExtra("courseId");
        String extraCat = getIntent().getStringExtra("categoryName");
        if (extraCat != null) catName = extraCat;

        img = findViewById(R.id.imgCover); tvTitle = findViewById(R.id.tvTitle);
        tvCategory = findViewById(R.id.tvCategory); tvInstructor = findViewById(R.id.tvInstructor);
        tvPrice = findViewById(R.id.tvPrice); tvDuration = findViewById(R.id.tvDuration);
        tvCreated = findViewById(R.id.tvCreated); tvDesc = findViewById(R.id.tvDesc);
        tvError = findViewById(R.id.tvError); progress = findViewById(R.id.progress);
        content = findViewById(R.id.content);

        findViewById(R.id.btnTrailer).setOnClickListener(v -> {
            if (course == null) return;
            Intent i = new Intent(this, TrailerActivity.class);
            i.putExtra("url", course.getTrailerUrl());
            startActivity(i);
        });
        findViewById(R.id.btnEdit).setOnClickListener(v -> {
            Intent i = new Intent(this, CourseFormActivity.class);
            i.putExtra("courseId", courseId);
            startActivity(i);
        });
        findViewById(R.id.btnDelete).setOnClickListener(v -> confirmDelete());
    }

    @Override protected void onResume() { super.onResume(); load(); }

    private void load() {
        if (course == null) progress.setVisibility(View.VISIBLE);
        tvError.setVisibility(View.GONE);

        // Gọi 1: khóa học
        ApiClient.get().getCourse(courseId).enqueue(new ApiCallback<Course>() {
            @Override public void onSuccess(Course c) {
                if (c == null) { onError("Không tìm thấy khóa học"); return; }
                course = c;
                progress.setVisibility(View.GONE);
                content.setVisibility(View.VISIBLE);
                render();
            }
            @Override public void onError(String m) {
                progress.setVisibility(View.GONE);
                if (course == null) content.setVisibility(View.GONE);
                tvError.setText(m);
                tvError.setVisibility(View.VISIBLE);
            }
        });

        // Gọi 2 (song song): danh mục
        ApiClient.get().getCategories().enqueue(new ApiCallback<List<Category>>() {
            @Override public void onSuccess(List<Category> list) {
                categories = list;
                if (course != null) render();
            }
            @Override public void onError(String m) { }
        });
    }

    /** Vẽ lại giao diện từ dữ liệu hiện có; gọi được nhiều lần, thứ tự nào cũng đúng. */
    private void render() {
        if (course == null) return;
        if (categories != null) {
            catName = "Chưa phân loại";
            for (Category c : categories)
                if (c.getId() != null && c.getId().equals(course.getCategoryId())) catName = c.getName();
        }
        tvTitle.setText(course.getTitle());
        tvCategory.setText("Danh mục: " + catName);
        tvInstructor.setText("Giảng viên: " + course.getInstructor());
        tvPrice.setText("Học phí: " + NumberFormat.getInstance(new Locale("vi", "VN")).format(course.getPrice()) + " đ");
        tvDuration.setText("Thời lượng: " + course.getDuration() + " giờ");
        tvCreated.setText("Ngày tạo: " + course.getCreatedAt());
        tvDesc.setText(course.getDescription());
        if (!isDestroyed()) {
            Glide.with(this).load(course.getThumbnail())
                    .placeholder(android.R.drawable.ic_menu_gallery)
                    .error(android.R.drawable.ic_delete).into(img);
        }
    }

    private void confirmDelete() {
        new AlertDialog.Builder(this)
                .setTitle("Xóa khóa học")
                .setMessage("Bạn có chắc muốn xóa khóa học này?")
                .setNegativeButton("Hủy", null)
                .setPositiveButton("Xóa", (d, w) ->
                        ApiClient.get().deleteCourse(courseId).enqueue(new ApiCallback<Course>() {
                            @Override public void onSuccess(Course c) {
                                Toast.makeText(CourseDetailActivity.this, "Đã xóa", Toast.LENGTH_SHORT).show();
                                finish();
                            }
                            @Override public void onError(String m) {
                                Toast.makeText(CourseDetailActivity.this, m, Toast.LENGTH_SHORT).show();
                            }
                        })).show();
    }
}