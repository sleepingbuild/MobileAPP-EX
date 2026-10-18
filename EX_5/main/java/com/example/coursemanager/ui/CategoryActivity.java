package com.example.coursemanager.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.coursemanager.R;
import com.example.coursemanager.adapter.CategoryAdapter;
import com.example.coursemanager.api.*;
import com.example.coursemanager.model.*;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.*;

public class CategoryActivity extends BaseActivity {
    private CategoryAdapter adapter;
    private ProgressBar progress;
    private TextView tvMessage;
    private Button btnRetry;

    @Override protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_category);
        setTitle("Quản lý danh mục");
        progress = findViewById(R.id.progress); tvMessage = findViewById(R.id.tvMessage);
        btnRetry = findViewById(R.id.btnRetry);
        RecyclerView rv = findViewById(R.id.rvCategories);
        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new CategoryAdapter(new CategoryAdapter.Listener() {
            @Override public void onEdit(Category c) { showDialog(c); }
            @Override public void onDelete(Category c) { tryDelete(c); }
        });
        rv.setAdapter(adapter);
        btnRetry.setOnClickListener(v -> load());
        ((FloatingActionButton) findViewById(R.id.fabAdd)).setOnClickListener(v -> showDialog(null));
        load();
    }

    private void load() {
        progress.setVisibility(View.VISIBLE);
        tvMessage.setVisibility(View.GONE); btnRetry.setVisibility(View.GONE);
        ApiClient.get().getCategories().enqueue(new ApiCallback<List<Category>>() {
            @Override public void onSuccess(List<Category> body) {
                progress.setVisibility(View.GONE);
                List<Category> list = body != null ? body : new ArrayList<>();
                adapter.setData(list);
                if (list.isEmpty()) { tvMessage.setText("Chưa có danh mục nào"); tvMessage.setVisibility(View.VISIBLE); }
            }
            @Override public void onError(String m) {
                progress.setVisibility(View.GONE);
                tvMessage.setText(m); tvMessage.setVisibility(View.VISIBLE); btnRetry.setVisibility(View.VISIBLE);
            }
        });
    }

    /** Thêm (c == null) hoặc sửa danh mục. */
    private void showDialog(Category c) {
        View v = getLayoutInflater().inflate(R.layout.dialog_category, null);
        EditText etName = v.findViewById(R.id.etName), etDesc = v.findViewById(R.id.etDesc);
        if (c != null) { etName.setText(c.getName()); etDesc.setText(c.getDescription()); }
        AlertDialog dlg = new AlertDialog.Builder(this)
                .setTitle(c == null ? "Thêm danh mục" : "Sửa danh mục").setView(v)
                .setNegativeButton("Hủy", null).setPositiveButton("Lưu", null).create();
        dlg.setOnShowListener(d -> dlg.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(x -> {
            String name = etName.getText().toString().trim();
            if (name.isEmpty()) { etName.setError("Bắt buộc nhập tên danh mục"); return; }
            Category body = c != null ? c : new Category();
            body.setName(name); body.setDescription(etDesc.getText().toString().trim());
            ApiCallback<Category> cb = new ApiCallback<Category>() {
                @Override public void onSuccess(Category r) { dlg.dismiss(); load(); }
                @Override public void onError(String m) { Toast.makeText(CategoryActivity.this, m, Toast.LENGTH_SHORT).show(); }
            };
            if (c == null) ApiClient.get().addCategory(body).enqueue(cb);
            else ApiClient.get().updateCategory(c.getId(), body).enqueue(cb);
        }));
        dlg.show();
    }

    /** Ràng buộc kiểm tra phía ứng dụng: không xóa danh mục đang có khóa học. */
    private void tryDelete(Category c) {
        progress.setVisibility(View.VISIBLE);
        ApiClient.get().getCourses().enqueue(new ApiCallback<List<Course>>() {
            @Override public void onSuccess(List<Course> courses) {
                progress.setVisibility(View.GONE);
                int count = 0;
                if (courses != null) for (Course k : courses) if (c.getId().equals(k.getCategoryId())) count++;
                if (count > 0) {
                    new AlertDialog.Builder(CategoryActivity.this).setTitle("Không thể xóa")
                            .setMessage("Danh mục \"" + c.getName() + "\" đang có " + count + " khóa học.")
                            .setPositiveButton("OK", null).show();
                    return;
                }
                new AlertDialog.Builder(CategoryActivity.this).setTitle("Xóa danh mục")
                        .setMessage("Xóa danh mục \"" + c.getName() + "\"?")
                        .setNegativeButton("Hủy", null)
                        .setPositiveButton("Xóa", (d, w) ->
                            ApiClient.get().deleteCategory(c.getId()).enqueue(new ApiCallback<Category>() {
                                @Override public void onSuccess(Category r) { load(); }
                                @Override public void onError(String m) { Toast.makeText(CategoryActivity.this, m, Toast.LENGTH_SHORT).show(); }
                            })).show();
            }
            @Override public void onError(String m) {
                progress.setVisibility(View.GONE);
                Toast.makeText(CategoryActivity.this, m, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
