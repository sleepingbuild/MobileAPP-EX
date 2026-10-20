package com.example.bit240177_foodmanager.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import com.example.bit240177_foodmanager.R;
import com.example.bit240177_foodmanager.data.AppDatabase;
import com.example.bit240177_foodmanager.model.Dish;
import com.example.bit240177_foodmanager.model.DishCategory;
import com.example.bit240177_foodmanager.model.DishImage;
import com.example.bit240177_foodmanager.util.Constants;
import com.example.bit240177_foodmanager.util.SyncHelper;
import com.example.bit240177_foodmanager.util.Validators;
import java.util.ArrayList;
import java.util.List;
import com.example.bit240177_foodmanager.sync.SyncManager;

public class DishFormActivity extends BaseActivity {

    private AppDatabase db;
    private int dishId;
    private boolean editMode;
    private boolean populated = false;
    private final List<Integer> categoryIds = new ArrayList<>();

    private EditText etName, etPrice, etPrep, etDescription, etImageUrl;
    private Spinner spCategory;
    private CheckBox cbAvailable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dish_form);
        db = AppDatabase.get(this);
        dishId = getIntent().getIntExtra("dishId", -1);
        editMode = dishId != -1;

        etName = findViewById(R.id.etName);
        etPrice = findViewById(R.id.etPrice);
        etPrep = findViewById(R.id.etPrep);
        etDescription = findViewById(R.id.etDescription);
        etImageUrl = findViewById(R.id.etImageUrl);
        spCategory = findViewById(R.id.spCategory);
        cbAvailable = findViewById(R.id.cbAvailable);

        ((TextView) findViewById(R.id.tvFormTitle)).setText(editMode ? R.string.edit_dish : R.string.add_dish);
        // Khi sửa, ảnh quản lý ở màn hình chi tiết
        if (editMode) etImageUrl.setVisibility(View.GONE);

        findViewById(R.id.btnSave).setOnClickListener(v -> save());
        findViewById(R.id.btnCancel).setOnClickListener(v -> finish());

        loadData();
    }

    private void loadData() {
        AppDatabase.executor.execute(() -> {
            List<DishCategory> cats = db.categoryDao().getAll();
            Dish d = editMode ? db.dishDao().getById(dishId) : null;
            runOnUiThread(() -> {
                if (editMode && d == null) { // Id không tồn tại
                    Toast.makeText(this, R.string.dish_not_found, Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }
                categoryIds.clear();
                List<String> names = new ArrayList<>();
                for (DishCategory c : cats) { categoryIds.add(c.id); names.add(c.name); }
                ArrayAdapter<String> a = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, names);
                a.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spCategory.setAdapter(a);

                if (editMode && !populated) {
                    populated = true;
                    etName.setText(d.name);
                    etPrice.setText(String.valueOf(d.price));
                    etPrep.setText(String.valueOf(d.preparationTime));
                    etDescription.setText(d.description);
                    cbAvailable.setChecked(d.isAvailable);
                    int idx = categoryIds.indexOf(d.dishCategoryId);
                    if (idx >= 0) spCategory.setSelection(idx);
                }
            });
        });
    }

    private void save() {
        etName.setError(null); etPrice.setError(null); etPrep.setError(null); etImageUrl.setError(null);
        boolean ok = true;

        final String name = etName.getText().toString().trim();
        if (name.isEmpty()) { etName.setError(getString(R.string.error_name_required)); ok = false; }

        double price = 0;
        try { price = Double.parseDouble(etPrice.getText().toString().trim()); } catch (NumberFormatException ignored) {}
        if (price <= 0) { etPrice.setError(getString(R.string.error_price_invalid)); ok = false; }

        int prep = 0;
        try { prep = Integer.parseInt(etPrep.getText().toString().trim()); } catch (NumberFormatException ignored) {}
        if (prep <= 0) { etPrep.setError(getString(R.string.error_prep_invalid)); ok = false; }

        int pos = spCategory.getSelectedItemPosition();
        if (categoryIds.isEmpty() || pos < 0 || pos >= categoryIds.size()) {
            Toast.makeText(this, R.string.error_category_required, Toast.LENGTH_SHORT).show();
            ok = false;
        }

        final String imageUrl = etImageUrl.getText().toString().trim();
        if (!editMode && !imageUrl.isEmpty() && !Validators.isValidImageUrl(imageUrl)) {
            etImageUrl.setError(getString(R.string.error_url_invalid));
            ok = false;
        }
        if (!ok) return;

        final double fPrice = price;
        final int fPrep = prep;
        final int categoryId = categoryIds.get(pos);
        final String desc = etDescription.getText().toString().trim();
        final boolean available = cbAvailable.isChecked();

        AppDatabase.executor.execute(() -> {
            // Loại món phải còn tồn tại
            if (db.categoryDao().getById(categoryId) == null) {
                runOnUiThread(() -> Toast.makeText(this, R.string.error_category_required, Toast.LENGTH_SHORT).show());
                return;
            }
            // Tên không trùng trong cùng loại
            if (db.dishDao().countDuplicate(name, categoryId, editMode ? dishId : -1) > 0) {
                runOnUiThread(() -> etName.setError(getString(R.string.error_duplicate_name)));
                return;
            }

            if (editMode) {
                Dish d = db.dishDao().getById(dishId);
                if (d == null) {
                    runOnUiThread(() -> {
                        Toast.makeText(this, R.string.dish_not_found, Toast.LENGTH_SHORT).show();
                        finish();
                    });
                    return;
                }
                d.name = name; d.price = fPrice; d.preparationTime = fPrep;
                d.isAvailable = available; d.description = desc; d.dishCategoryId = categoryId;
                SyncHelper.markModified(d);
                db.dishDao().update(d);
            } else {
                Dish d = new Dish();
                d.name = name; d.price = fPrice; d.preparationTime = fPrep;
                d.isAvailable = available; d.description = desc; d.dishCategoryId = categoryId;
                d.syncStatus = Constants.SYNC_PENDING_CREATE;
                int newId = (int) db.dishDao().insert(d);
                if (!imageUrl.isEmpty()) {
                    DishImage img = new DishImage();
                    img.dishId = newId; img.imageUrl = imageUrl; img.isThumbnail = true;
                    db.imageDao().insert(img);
                }
            }
            SyncManager.get(this).syncNow();
            runOnUiThread(() -> {
                Toast.makeText(this, R.string.saved, Toast.LENGTH_SHORT).show();
                finish();
            });
        });
    }
}