package com.example.bit240177_foodmanager;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.bit240177_foodmanager.data.AppDatabase;
import com.example.bit240177_foodmanager.model.Dish;
import com.example.bit240177_foodmanager.model.DishCategory;
import com.example.bit240177_foodmanager.model.DishImage;
import com.example.bit240177_foodmanager.model.DishListItem;
import com.example.bit240177_foodmanager.ui.BaseActivity;
import com.example.bit240177_foodmanager.ui.CategoryActivity;
import com.example.bit240177_foodmanager.ui.DishAdapter;
import com.example.bit240177_foodmanager.ui.DishDetailActivity;
import com.example.bit240177_foodmanager.ui.DishFormActivity;
import com.example.bit240177_foodmanager.util.LocaleHelper;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends BaseActivity {

    private AppDatabase db;
    private EditText etSearch, etMinPrice, etMaxPrice;
    private Spinner spCategory, spSort;
    private CheckBox cbAvailable;
    private TextView tvEmpty;
    private RecyclerView rvDishes;
    private DishAdapter adapter;

    private final List<Integer> categoryIds = new ArrayList<>(); // phần tử đầu = -1 (tất cả)
    private int selectedCategoryId = -1;
    private int requestCounter = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        db = AppDatabase.get(this);

        etSearch = findViewById(R.id.etSearch);
        etMinPrice = findViewById(R.id.etMinPrice);
        etMaxPrice = findViewById(R.id.etMaxPrice);
        spCategory = findViewById(R.id.spCategory);
        spSort = findViewById(R.id.spSort);
        cbAvailable = findViewById(R.id.cbAvailable);
        tvEmpty = findViewById(R.id.tvEmpty);
        rvDishes = findViewById(R.id.rvDishes);

        if (savedInstanceState != null) {
            selectedCategoryId = savedInstanceState.getInt("catId", -1);
        }

        // ----- Danh sách: layout manager + adapter (phải có đủ cả hai) -----
        adapter = new DishAdapter(item -> {
            Intent i = new Intent(this, DishDetailActivity.class);
            i.putExtra("dishId", item.dish.id);
            startActivity(i);
        });
        rvDishes.setLayoutManager(new LinearLayoutManager(this));
        rvDishes.setAdapter(adapter);

        // ----- Các nút -----
        findViewById(R.id.btnLanguage).setOnClickListener(v -> {
            LocaleHelper.toggle(this);
            recreate();
        });
        findViewById(R.id.btnCategories).setOnClickListener(v ->
                startActivity(new Intent(this, CategoryActivity.class)));
        findViewById(R.id.fabAdd).setOnClickListener(v ->
                startActivity(new Intent(this, DishFormActivity.class)));

        // ----- Spinner sắp xếp -----
        ArrayAdapter<CharSequence> sortAdapter = ArrayAdapter.createFromResource(
                this, R.array.sort_options, android.R.layout.simple_spinner_item);
        sortAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spSort.setAdapter(sortAdapter);

        // ----- Bộ lọc -----
        TextWatcher watcher = new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int a, int b, int c) {}
            public void onTextChanged(CharSequence s, int a, int b, int c) {}
            public void afterTextChanged(Editable s) { loadDishes(); }
        };
        etSearch.addTextChangedListener(watcher);
        etMinPrice.addTextChangedListener(watcher);
        etMaxPrice.addTextChangedListener(watcher);
        cbAvailable.setOnCheckedChangeListener((b, checked) -> loadDishes());

        spSort.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> p, View v, int pos, long id) { loadDishes(); }
            public void onNothingSelected(AdapterView<?> p) {}
        });
        spCategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> p, View v, int pos, long id) {
                if (pos < categoryIds.size() && categoryIds.get(pos) != selectedCategoryId) {
                    selectedCategoryId = categoryIds.get(pos);
                    loadDishes();
                }
            }
            public void onNothingSelected(AdapterView<?> p) {}
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadCategories(); // loại món có thể đã thay đổi ở màn hình khác
    }

    @Override
    protected void onSaveInstanceState(Bundle out) {
        super.onSaveInstanceState(out);
        out.putInt("catId", selectedCategoryId);
    }

    private void loadCategories() {
        AppDatabase.executor.execute(() -> {
            List<DishCategory> cats = db.categoryDao().getAll();
            runOnUiThread(() -> {
                categoryIds.clear();
                List<String> names = new ArrayList<>();
                categoryIds.add(-1);
                names.add(getString(R.string.all_categories));
                for (DishCategory c : cats) {
                    categoryIds.add(c.id);
                    names.add(c.name);
                }
                ArrayAdapter<String> a = new ArrayAdapter<>(this,
                        android.R.layout.simple_spinner_item, names);
                a.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spCategory.setAdapter(a);

                int index = categoryIds.indexOf(selectedCategoryId);
                if (index < 0) { index = 0; selectedCategoryId = -1; } // loại đã bị xóa
                spCategory.setSelection(index);
                loadDishes();
            });
        });
    }

    private Double parsePrice(EditText et) {
        String s = et.getText().toString().trim();
        if (s.isEmpty()) return null;
        try { return Double.parseDouble(s); } catch (NumberFormatException e) { return null; }
    }

    private void loadDishes() {
        String kw = etSearch.getText().toString().trim();
        final String keyword = kw.isEmpty() ? null : kw;
        final Integer categoryId = selectedCategoryId < 0 ? null : selectedCategoryId;
        final int onlyAvailable = cbAvailable.isChecked() ? 1 : 0;
        final Double min = parsePrice(etMinPrice);
        final Double max = parsePrice(etMaxPrice);
        final int sort = spSort.getSelectedItemPosition();

        if (min != null && max != null && min > max) {
            etMaxPrice.setError(getString(R.string.error_price_range));
            return;
        }
        etMaxPrice.setError(null);

        final int myRequest = ++requestCounter;
        AppDatabase.executor.execute(() -> {
            try {
                List<Dish> dishes = db.dishDao().search(keyword, categoryId, onlyAvailable, min, max, sort);
                List<DishListItem> items = new ArrayList<>();
                for (Dish d : dishes) {
                    DishListItem it = new DishListItem();
                    it.dish = d;
                    DishCategory c = db.categoryDao().getById(d.dishCategoryId);
                    it.categoryName = c == null ? "" : c.name;
                    for (DishImage img : db.imageDao().getByDish(d.id)) {
                        if (it.thumbnailUrl == null || img.isThumbnail) it.thumbnailUrl = img.imageUrl;
                        if (img.isThumbnail) break;
                    }
                    items.add(it);
                }
                runOnUiThread(() -> {
                    if (myRequest != requestCounter) return; // đã có truy vấn mới hơn
                    adapter.submit(items);
                    tvEmpty.setText(R.string.empty_list);
                    tvEmpty.setVisibility(items.isEmpty() ? View.VISIBLE : View.GONE);
                });
            } catch (Exception e) {
                android.util.Log.e("FoodManager", "loadDishes failed", e);
            }
        });
    }
}