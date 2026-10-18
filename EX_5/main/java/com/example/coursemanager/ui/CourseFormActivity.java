package com.example.coursemanager.ui;

import android.os.Bundle;
import android.text.*;
import android.util.Patterns;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AlertDialog;
import com.bumptech.glide.Glide;
import com.example.coursemanager.R;
import com.example.coursemanager.api.*;
import com.example.coursemanager.model.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class CourseFormActivity extends BaseActivity {
    private EditText etTitle, etDesc, etInstructor, etPrice, etDuration, etThumb, etTrailer;
    private TextView tvCategoryBox, tvCatHint;
    private ImageView imgPreview;
    private Button btnSave;
    private ProgressBar progress;

    private final List<Category> categories = new ArrayList<>();
    private int selectedIndex = -1;   // -1 = chưa chọn
    private String courseId;          // null = thêm mới
    private String preCategoryId;     // danh mục chọn sẵn khi thêm mới
    private Course editing;
    private boolean loadingCourse = false;

    @Override protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_form);
        courseId = getIntent().getStringExtra("courseId");
        preCategoryId = getIntent().getStringExtra("preCategoryId");
        setTitle(courseId == null ? "Thêm khóa học" : "Sửa khóa học");

        etTitle = findViewById(R.id.etTitle); etDesc = findViewById(R.id.etDesc);
        etInstructor = findViewById(R.id.etInstructor); etPrice = findViewById(R.id.etPrice);
        etDuration = findViewById(R.id.etDuration); etThumb = findViewById(R.id.etThumb);
        etTrailer = findViewById(R.id.etTrailer);
        tvCategoryBox = findViewById(R.id.tvCategoryBox); tvCatHint = findViewById(R.id.tvCatHint);
        imgPreview = findViewById(R.id.imgPreview); btnSave = findViewById(R.id.btnSave);
        progress = findViewById(R.id.progress);

        etThumb.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int a, int b, int c) {}
            public void onTextChanged(CharSequence s, int a, int b, int c) {}
            public void afterTextChanged(Editable e) {
                String url = e.toString().trim();
                if (Patterns.WEB_URL.matcher(url).matches())
                    Glide.with(CourseFormActivity.this).load(url)
                            .placeholder(android.R.drawable.ic_menu_gallery)
                            .error(android.R.drawable.ic_delete).into(imgPreview);
                else imgPreview.setImageResource(android.R.drawable.ic_menu_gallery);
            }
        });
        tvCategoryBox.setOnClickListener(v -> showCategoryDialog());
        btnSave.setOnClickListener(v -> save());
    }

    @Override protected void onResume() { super.onResume(); loadCategories(); }

    /** Bấm vào ô danh mục -> hiện danh sách tất cả danh mục để chọn / đổi. */
    private void showCategoryDialog() {
        if (categories.isEmpty()) {
            Toast.makeText(this, "Chưa có danh mục. Hãy thêm ở màn Quản lý danh mục.", Toast.LENGTH_LONG).show();
            return;
        }
        String[] names = new String[categories.size()];
        for (int i = 0; i < names.length; i++) names[i] = categories.get(i).getName();
        new AlertDialog.Builder(this)
                .setTitle("Chọn danh mục")
                .setSingleChoiceItems(names, selectedIndex, (d, which) -> {
                    selectedIndex = which;
                    updateCategoryBox();
                    d.dismiss();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void updateCategoryBox() {
        if (selectedIndex >= 0 && selectedIndex < categories.size())
            tvCategoryBox.setText(categories.get(selectedIndex).getName());
        else tvCategoryBox.setText("Chọn danh mục");
    }

    private String selectedCategoryId() {
        return (selectedIndex >= 0 && selectedIndex < categories.size())
                ? categories.get(selectedIndex).getId() : null;
    }

    private void selectCategory(String id) {
        selectedIndex = -1;
        if (id != null)
            for (int i = 0; i < categories.size(); i++)
                if (id.equals(categories.get(i).getId())) { selectedIndex = i; break; }
        updateCategoryBox();
    }

    private void loadCategories() {
        progress.setVisibility(View.VISIBLE);
        ApiClient.get().getCategories().enqueue(new ApiCallback<List<Category>>() {
            @Override public void onSuccess(List<Category> body) {
                String keep = selectedCategoryId();
                categories.clear();
                if (body != null) categories.addAll(body);
                tvCatHint.setText("Chưa có danh mục nào. Hãy thêm ở màn Quản lý danh mục.");
                tvCatHint.setVisibility(categories.isEmpty() ? View.VISIBLE : View.GONE);

                if (keep != null) selectCategory(keep);
                else if (editing != null) selectCategory(editing.getCategoryId());
                else selectCategory(preCategoryId);

                if (courseId != null && editing == null && !loadingCourse) loadCourse();
                else progress.setVisibility(View.GONE);
            }
            @Override public void onError(String m) {
                progress.setVisibility(View.GONE);
                tvCatHint.setText(m + " (không tải được danh mục)");
                tvCatHint.setVisibility(View.VISIBLE);
            }
        });
    }

    private void loadCourse() {
        loadingCourse = true;
        ApiClient.get().getCourse(courseId).enqueue(new ApiCallback<Course>() {
            @Override public void onSuccess(Course c) {
                loadingCourse = false;
                progress.setVisibility(View.GONE);
                if (c == null) { Toast.makeText(CourseFormActivity.this, "Không tìm thấy khóa học", Toast.LENGTH_SHORT).show(); finish(); return; }
                editing = c;
                etTitle.setText(c.getTitle()); etDesc.setText(c.getDescription());
                etInstructor.setText(c.getInstructor()); etPrice.setText(String.valueOf(c.getPrice()));
                etDuration.setText(String.valueOf(c.getDuration()));
                etThumb.setText(c.getThumbnail()); etTrailer.setText(c.getTrailerUrl());
                selectCategory(c.getCategoryId());
            }
            @Override public void onError(String m) {
                loadingCourse = false;
                progress.setVisibility(View.GONE);
                Toast.makeText(CourseFormActivity.this, m, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void save() {
        String title = etTitle.getText().toString().trim();
        String desc = etDesc.getText().toString().trim();
        String ins = etInstructor.getText().toString().trim();
        String priceS = etPrice.getText().toString().trim();
        String durS = etDuration.getText().toString().trim();
        String thumb = etThumb.getText().toString().trim();
        String trailer = etTrailer.getText().toString().trim();

        if (title.isEmpty()) { etTitle.setError("Bắt buộc nhập tên khóa học"); return; }
        if (desc.isEmpty()) { etDesc.setError("Bắt buộc nhập mô tả"); return; }
        String categoryId = selectedCategoryId();
        if (categoryId == null) {
            Toast.makeText(this, "Hãy bấm vào ô Danh mục để chọn danh mục", Toast.LENGTH_LONG).show(); return;
        }
        if (ins.isEmpty()) { etInstructor.setError("Bắt buộc nhập giảng viên"); return; }
        long price; int duration;
        try { price = Long.parseLong(priceS); } catch (Exception e) { etPrice.setError("Học phí không hợp lệ"); return; }
        if (price < 0) { etPrice.setError("Học phí không được âm"); return; }
        try { duration = Integer.parseInt(durS); } catch (Exception e) { etDuration.setError("Thời lượng không hợp lệ"); return; }
        if (duration <= 0) { etDuration.setError("Thời lượng phải > 0"); return; }
        if (!Patterns.WEB_URL.matcher(thumb).matches()) { etThumb.setError("URL ảnh bìa không hợp lệ"); return; }
        if (!Patterns.WEB_URL.matcher(trailer).matches()) { etTrailer.setError("URL trailer không hợp lệ"); return; }
        if (courseId != null && editing == null) {
            Toast.makeText(this, "Đang tải dữ liệu khóa học, vui lòng đợi...", Toast.LENGTH_SHORT).show(); return;
        }

        Course c = editing != null ? editing : new Course();
        c.setTitle(title); c.setDescription(desc); c.setInstructor(ins);
        c.setPrice(price); c.setDuration(duration); c.setThumbnail(thumb); c.setTrailerUrl(trailer);
        c.setCategoryId(categoryId);
        if (editing == null) c.setCreatedAt(new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(new Date()));

        btnSave.setEnabled(false);
        progress.setVisibility(View.VISIBLE);
        ApiCallback<Course> cb = new ApiCallback<Course>() {
            @Override public void onSuccess(Course body) {
                Toast.makeText(CourseFormActivity.this, "Lưu thành công", Toast.LENGTH_SHORT).show();
                finish();
            }
            @Override public void onError(String m) {
                btnSave.setEnabled(true); progress.setVisibility(View.GONE);
                Toast.makeText(CourseFormActivity.this, m, Toast.LENGTH_LONG).show();
            }
        };

        ApiClient.get().getCategories().enqueue(new ApiCallback<List<Category>>() {
            @Override public void onSuccess(List<Category> list) {
                boolean exists = false;
                if (list != null) for (Category k : list) if (categoryId.equals(k.getId())) exists = true;
                if (!exists) {
                    btnSave.setEnabled(true); progress.setVisibility(View.GONE);
                    Toast.makeText(CourseFormActivity.this, "Danh mục không còn tồn tại, hãy chọn lại", Toast.LENGTH_LONG).show();
                    loadCategories();
                    return;
                }
                if (editing == null) ApiClient.get().addCourse(c).enqueue(cb);
                else ApiClient.get().updateCourse(courseId, c).enqueue(cb);
            }
            @Override public void onError(String m) { cb.onError(m); }
        });
    }
}