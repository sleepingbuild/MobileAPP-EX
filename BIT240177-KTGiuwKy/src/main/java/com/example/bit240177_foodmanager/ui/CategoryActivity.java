package com.example.bit240177_foodmanager.ui;

import android.database.sqlite.SQLiteConstraintException;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.bit240177_foodmanager.R;
import com.example.bit240177_foodmanager.data.AppDatabase;
import com.example.bit240177_foodmanager.model.DishCategory;
import com.example.bit240177_foodmanager.util.Constants;
import com.example.bit240177_foodmanager.util.SyncHelper;
import java.util.List;

public class CategoryActivity extends BaseActivity {

    private AppDatabase db;
    private CategoryAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category);
        db = AppDatabase.get(this);

        RecyclerView rv = findViewById(R.id.rvCategories);
        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new CategoryAdapter(new CategoryAdapter.Listener() {
            @Override public void onEdit(DishCategory c) { showEditDialog(c); }
            @Override public void onDelete(DishCategory c) { confirmDelete(c); }
        });
        rv.setAdapter(adapter);

        findViewById(R.id.fabAddCategory).setOnClickListener(v -> showEditDialog(null));
    }

    @Override
    protected void onResume() {
        super.onResume();
        load();
    }

    private void load() {
        AppDatabase.executor.execute(() -> {
            List<DishCategory> cats = db.categoryDao().getAll();
            runOnUiThread(() -> adapter.submit(cats));
        });
    }

    // existing == null: thêm mới; ngược lại: sửa
    private void showEditDialog(DishCategory existing) {
        View view = getLayoutInflater().inflate(R.layout.dialog_category, null);
        EditText etName = view.findViewById(R.id.etCatName);
        EditText etDesc = view.findViewById(R.id.etCatDesc);
        if (existing != null) {
            etName.setText(existing.name);
            etDesc.setText(existing.description);
        }

        AlertDialog dlg = new AlertDialog.Builder(this)
                .setTitle(existing == null ? R.string.add_category : R.string.edit_category)
                .setView(view)
                .setPositiveButton(R.string.save, null)
                .setNegativeButton(R.string.cancel, null)
                .create();

        dlg.setOnShowListener(x -> dlg.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            String desc = etDesc.getText().toString().trim();
            if (name.isEmpty()) {
                etName.setError(getString(R.string.error_category_name_required));
                return;
            }
            AppDatabase.executor.execute(() -> {
                int excludeId = existing == null ? -1 : existing.id;
                if (db.categoryDao().countDuplicate(name, excludeId) > 0) {
                    runOnUiThread(() -> etName.setError(getString(R.string.error_category_duplicate)));
                    return;
                }
                if (existing == null) {
                    DishCategory c = new DishCategory();
                    c.name = name; c.description = desc;
                    c.syncStatus = Constants.SYNC_PENDING_CREATE;
                    db.categoryDao().insert(c);
                } else {
                    existing.name = name; existing.description = desc;
                    SyncHelper.markModified(existing);
                    db.categoryDao().update(existing);
                }
                runOnUiThread(() -> {
                    dlg.dismiss();
                    Toast.makeText(this, R.string.saved, Toast.LENGTH_SHORT).show();
                    load();
                });
            });
        }));
        dlg.show();
    }

    private void confirmDelete(DishCategory c) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.confirm_delete_category_title)
                .setMessage(getString(R.string.confirm_delete_category_message, c.name))
                .setPositiveButton(R.string.delete, (d, w) -> deleteCategory(c))
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void deleteCategory(DishCategory c) {
        AppDatabase.executor.execute(() -> {
            int count = db.categoryDao().countDishes(c.id);
            if (count > 0) { // ràng buộc: loại đang được sử dụng thì không cho xóa
                runOnUiThread(() -> Toast.makeText(this,
                        getString(R.string.error_category_in_use, count), Toast.LENGTH_LONG).show());
                return;
            }
            try {
                db.categoryDao().delete(c);
            } catch (SQLiteConstraintException e) { // lớp bảo vệ thứ hai từ khóa ngoại RESTRICT
                runOnUiThread(() -> Toast.makeText(this,
                        getString(R.string.error_category_in_use, 1), Toast.LENGTH_LONG).show());
                return;
            }
            runOnUiThread(() -> {
                Toast.makeText(this, R.string.deleted, Toast.LENGTH_SHORT).show();
                load();
            });
        });
    }
}