package com.example.bit240177_foodmanager.ui;

import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.bit240177_foodmanager.R;
import com.example.bit240177_foodmanager.data.AppDatabase;
import com.example.bit240177_foodmanager.model.Dish;
import com.example.bit240177_foodmanager.model.DishCategory;
import com.example.bit240177_foodmanager.model.DishImage;
import com.example.bit240177_foodmanager.util.SyncHelper;
import com.example.bit240177_foodmanager.util.Validators;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import com.example.bit240177_foodmanager.sync.SyncManager;

public class DishDetailActivity extends BaseActivity {

    private AppDatabase db;
    private int dishId;
    private Dish dish;
    private ImageAdapter imageAdapter;
    private TextView tvName, tvCategory, tvPrice, tvPrep, tvStatus, tvDescription, tvNoImages;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dish_detail);
        db = AppDatabase.get(this);
        dishId = getIntent().getIntExtra("dishId", -1);

        tvName = findViewById(R.id.tvName);
        tvCategory = findViewById(R.id.tvCategory);
        tvPrice = findViewById(R.id.tvPrice);
        tvPrep = findViewById(R.id.tvPrep);
        tvStatus = findViewById(R.id.tvStatus);
        tvDescription = findViewById(R.id.tvDescription);
        tvNoImages = findViewById(R.id.tvNoImages);

        RecyclerView rv = findViewById(R.id.rvImages);
        rv.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        imageAdapter = new ImageAdapter(this::showImageOptions);
        rv.setAdapter(imageAdapter);

        ((Button) findViewById(R.id.btnAddImage)).setOnClickListener(v -> showAddImageDialog());
        ((Button) findViewById(R.id.btnEdit)).setOnClickListener(v -> {
            android.content.Intent i = new android.content.Intent(this, DishFormActivity.class);
            i.putExtra("dishId", dishId);
            startActivity(i);
        });
        ((Button) findViewById(R.id.btnDelete)).setOnClickListener(v -> confirmDelete());
    }

    @Override
    protected void onResume() {
        super.onResume();
        load(); // tải lại sau khi sửa ở form
    }

    private void load() {
        AppDatabase.executor.execute(() -> {
            Dish d = db.dishDao().getById(dishId);
            if (d == null) { // Id không tồn tại
                runOnUiThread(() -> {
                    Toast.makeText(this, R.string.dish_not_found, Toast.LENGTH_SHORT).show();
                    finish();
                });
                return;
            }
            DishCategory c = db.categoryDao().getById(d.dishCategoryId);
            List<DishImage> imgs = db.imageDao().getByDish(dishId);
            runOnUiThread(() -> bind(d, c, imgs));
        });
    }

    private void bind(Dish d, DishCategory c, List<DishImage> imgs) {
        dish = d;
        tvName.setText(d.name);
        tvCategory.setText(getString(R.string.category) + ": " + (c == null ? "" : c.name));
        tvPrice.setText(getString(R.string.price_format, NumberFormat.getInstance(Locale.US).format(d.price)));
        String type = getString(d.preparationTime <= 15 ? R.string.quick : R.string.regular);
        tvPrep.setText(getString(R.string.minutes_format, d.preparationTime) + " - " + type);
        tvStatus.setText(d.isAvailable ? getString(R.string.available) : getString(R.string.unavailable));
        tvDescription.setText(d.description == null ? "" : d.description);
        imageAdapter.submit(imgs);
        tvNoImages.setVisibility(imgs.isEmpty() ? View.VISIBLE : View.GONE);
    }

    // ---------- Ảnh ----------
    private void showAddImageDialog() {
        EditText input = new EditText(this);
        input.setHint(R.string.image_url);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_URI);
        AlertDialog dlg = new AlertDialog.Builder(this)
                .setTitle(R.string.add_image).setView(input)
                .setPositiveButton(R.string.save, null)
                .setNegativeButton(R.string.cancel, null).create();
        dlg.setOnShowListener(x -> dlg.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String url = input.getText().toString().trim();
            if (!Validators.isValidImageUrl(url)) {
                input.setError(getString(R.string.error_url_invalid));
                return;
            }
            dlg.dismiss();
            addImage(url);
        }));
        dlg.show();
    }

    private void addImage(String url) {
        AppDatabase.executor.execute(() -> {
            boolean first = db.imageDao().getByDish(dishId).isEmpty();
            DishImage img = new DishImage();
            img.dishId = dishId;
            img.imageUrl = url;
            img.isThumbnail = first; // ảnh đầu tiên tự thành ảnh đại diện
            db.imageDao().insert(img);
            touchDish();
            runOnUiThread(this::load);
        });
    }

    private void showImageOptions(DishImage img) {
        String[] options = { getString(R.string.set_thumbnail), getString(R.string.delete_image) };
        new AlertDialog.Builder(this).setItems(options, (d, which) -> {
            if (which == 0) setThumbnail(img); else deleteImage(img);
        }).show();
    }

    private void setThumbnail(DishImage img) {
        AppDatabase.executor.execute(() -> {
            db.imageDao().clearThumbnail(dishId);   // bảo đảm tối đa 1 ảnh đại diện
            img.isThumbnail = true;
            db.imageDao().update(img);
            touchDish();
            runOnUiThread(this::load);
        });
    }

    private void deleteImage(DishImage img) {
        AppDatabase.executor.execute(() -> {
            db.imageDao().delete(img);
            List<DishImage> rest = db.imageDao().getByDish(dishId);
            if (img.isThumbnail && !rest.isEmpty()) { // xóa ảnh đại diện thì chọn ảnh còn lại làm đại diện
                rest.get(0).isThumbnail = true;
                db.imageDao().update(rest.get(0));
            }
            touchDish();
            runOnUiThread(this::load);
        });
    }

    private void touchDish() { // gọi trong executor
        Dish d = db.dishDao().getById(dishId);
        if (d != null) {
            SyncHelper.markModified(d);
            db.dishDao().update(d);
        }
        SyncManager.get(this).syncNow();
    }

    // ---------- Xóa món ----------
    private void confirmDelete() {
        if (dish == null) return;
        new AlertDialog.Builder(this)
                .setTitle(R.string.confirm_delete_title)
                .setMessage(getString(R.string.confirm_delete_message, dish.name))
                .setPositiveButton(R.string.delete, (d, w) -> AppDatabase.executor.execute(() -> {
                    String remoteId = dish.remoteId;
                    db.dishDao().delete(dish); // ảnh tự xóa theo (CASCADE)
                    SyncManager.get(this).recordDeletion(true, remoteId);
                    runOnUiThread(() -> {
                        Toast.makeText(this, R.string.deleted, Toast.LENGTH_SHORT).show();
                        finish();
                    });
                }))
                .setNegativeButton(R.string.cancel, null)
                .show();
    }
}