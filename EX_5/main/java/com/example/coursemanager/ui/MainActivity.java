package com.example.coursemanager.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.*;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.coursemanager.R;
import com.example.coursemanager.adapter.CourseAdapter;
import com.example.coursemanager.api.*;
import com.example.coursemanager.model.*;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.*;

public class MainActivity extends BaseActivity {
    private EditText etSearch;
    private Spinner spFilter;
    private ProgressBar progress;
    private TextView tvMessage;
    private Button btnRetry;
    private RecyclerView rv;
    private CourseAdapter adapter;
    @Override protected boolean showBack() { return false; }

    private final List<Course> allCourses = new ArrayList<>();
    private final List<Category> categories = new ArrayList<>();
    private final Map<String, String> categoryNames = new HashMap<>();

    @Override protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_main);
        setTitle("Khóa học");
        etSearch = findViewById(R.id.etSearch);
        spFilter = findViewById(R.id.spFilter);
        progress = findViewById(R.id.progress);
        tvMessage = findViewById(R.id.tvMessage);
        btnRetry = findViewById(R.id.btnRetry);
        rv = findViewById(R.id.rvCourses);

        adapter = new CourseAdapter(c -> {
            Intent i = new Intent(this, CourseDetailActivity.class);
            i.putExtra("courseId", c.getId());
            i.putExtra("categoryName", categoryNames.get(c.getCategoryId()));
            startActivity(i);
        });
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(adapter);

        etSearch.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int a, int b, int c) {}
            public void onTextChanged(CharSequence s, int a, int b, int c) { applyFilter(); }
            public void afterTextChanged(Editable e) {}
        });
        spFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> p, View v, int pos, long id) { applyFilter(); }
            public void onNothingSelected(AdapterView<?> p) {}
        });
        btnRetry.setOnClickListener(v -> loadData());
        findViewById(R.id.btnCategories).setOnClickListener(v -> startActivity(new Intent(this, CategoryActivity.class)));
        ((FloatingActionButton) findViewById(R.id.fabAdd)).setOnClickListener(v -> {
            Intent i = new Intent(this, CourseFormActivity.class);
            int pos = spFilter.getSelectedItemPosition();
            if (pos > 0 && pos - 1 < categories.size())
                i.putExtra("preCategoryId", categories.get(pos - 1).getId());
            startActivity(i);
        });
    }

    // Đồng bộ lại với backend mỗi khi quay về màn hình (sau thêm/sửa/xóa)
    @Override protected void onResume() { super.onResume(); loadData(); }

    private void loadData() {
        progress.setVisibility(View.VISIBLE);
        tvMessage.setVisibility(View.GONE);
        btnRetry.setVisibility(View.GONE);
        ApiClient.get().getCategories().enqueue(new ApiCallback<List<Category>>() {
            @Override public void onSuccess(List<Category> body) {
                categories.clear();
                categoryNames.clear();
                if (body != null) for (Category c : body) { categories.add(c); categoryNames.put(c.getId(), c.getName()); }
                bindFilterSpinner();
                loadCourses();
            }
            @Override public void onError(String m) { showError(m); }
        });
    }

    private void loadCourses() {
        ApiClient.get().getCourses().enqueue(new ApiCallback<List<Course>>() {
            @Override public void onSuccess(List<Course> body) {
                progress.setVisibility(View.GONE);
                allCourses.clear();
                if (body != null) allCourses.addAll(body);
                applyFilter();
            }
            @Override public void onError(String m) { showError(m); }
        });
    }

    private void bindFilterSpinner() {
        int old = spFilter.getSelectedItemPosition();
        List<String> names = new ArrayList<>();
        names.add("Tất cả danh mục");
        for (Category c : categories) names.add(c.getName());
        spFilter.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, names));
        if (old > 0 && old < names.size()) spFilter.setSelection(old);
    }

    private void applyFilter() {
        String q = etSearch.getText().toString().trim().toLowerCase();
        int pos = spFilter.getSelectedItemPosition();
        String catId = (pos > 0 && pos - 1 < categories.size()) ? categories.get(pos - 1).getId() : null;
        List<Course> result = new ArrayList<>();
        for (Course c : allCourses) {
            boolean okName = c.getTitle() != null && c.getTitle().toLowerCase().contains(q);
            boolean okCat = catId == null || catId.equals(c.getCategoryId());
            if (okName && okCat) result.add(c);
        }
        adapter.setData(result, categoryNames);
        if (result.isEmpty()) {
            tvMessage.setText("Không có khóa học nào");
            tvMessage.setVisibility(View.VISIBLE);
        } else tvMessage.setVisibility(View.GONE);
    }

    private void showError(String m) {
        progress.setVisibility(View.GONE);
        tvMessage.setText(m);
        tvMessage.setVisibility(View.VISIBLE);
        btnRetry.setVisibility(View.VISIBLE);
    }
}
