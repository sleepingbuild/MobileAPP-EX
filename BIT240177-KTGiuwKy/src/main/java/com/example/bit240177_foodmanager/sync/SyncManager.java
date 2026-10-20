package com.example.bit240177_foodmanager.sync;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import com.example.bit240177_foodmanager.data.AppDatabase;
import com.example.bit240177_foodmanager.model.Dish;
import com.example.bit240177_foodmanager.model.DishCategory;
import com.example.bit240177_foodmanager.model.DishImage;
import com.example.bit240177_foodmanager.remote.ApiClient;
import com.example.bit240177_foodmanager.remote.ApiService;
import com.example.bit240177_foodmanager.remote.CategoryDto;
import com.example.bit240177_foodmanager.remote.DishDto;
import com.example.bit240177_foodmanager.remote.ImageJson;
import com.example.bit240177_foodmanager.util.Constants;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import retrofit2.Response;

public class SyncManager {
    private static final String TAG = "FoodSync";
    private static final String PREFS = Constants.MSSV + "sync";
    private static final String KEY_DEL_DISH = "deleted_dish";
    private static final String KEY_DEL_CAT = "deleted_category";

    private static SyncManager INSTANCE;

    public static synchronized SyncManager get(Context c) {
        if (INSTANCE == null) INSTANCE = new SyncManager(c.getApplicationContext());
        return INSTANCE;
    }

    private final Context app;
    private final AppDatabase db;
    private final ApiService api = ApiClient.get();
    private final Gson gson = new Gson();
    private final ConnectivityManager cm;
    private final Handler main = new Handler(Looper.getMainLooper());
    private final AtomicBoolean running = new AtomicBoolean(false);
    private final AtomicBoolean again = new AtomicBoolean(false);
    private final List<Runnable> listeners = new CopyOnWriteArrayList<>();
    private boolean lastOnline = false;

    private SyncManager(Context app) {
        this.app = app;
        this.db = AppDatabase.get(app);
        this.cm = (ConnectivityManager) app.getSystemService(Context.CONNECTIVITY_SERVICE);
    }

    // ================= Theo dõi mạng =================

    public boolean isOnline() {
        Network n = cm.getActiveNetwork();
        if (n == null) return false;
        NetworkCapabilities caps = cm.getNetworkCapabilities(n);
        return caps != null
                && caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                && caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED);
    }

    // Gọi một lần khi app khởi động: có mạng trở lại thì tự đồng bộ
    public void startMonitoring() {
        cm.registerDefaultNetworkCallback(new ConnectivityManager.NetworkCallback() {
            @Override public void onAvailable(Network n) { handleNetworkChange(); }
            @Override public void onCapabilitiesChanged(Network n, NetworkCapabilities c) { handleNetworkChange(); }
            @Override public void onLost(Network n) { handleNetworkChange(); }
        });
    }

    private synchronized void handleNetworkChange() {
        boolean now = isOnline();
        if (now && !lastOnline) {   // chuyển từ offline sang online
            lastOnline = true;
            Log.d(TAG, "Network is back -> auto sync");
            syncNow();
        } else if (!now) {
            lastOnline = false;
        }
        notifyChanged();
    }

    // ================= Lắng nghe thay đổi (để UI cập nhật) =================

    public void addListener(Runnable r) { listeners.add(r); }
    public void removeListener(Runnable r) { listeners.remove(r); }

    private void notifyChanged() {
        for (Runnable r : listeners) main.post(r);
    }

    public int getPendingCount() { // gọi trong executor
        return db.categoryDao().countPending() + db.dishDao().countPending();
    }

    // ================= Đồng bộ =================

    public void syncNow() {
        if (!isOnline()) { notifyChanged(); return; }
        if (!running.compareAndSet(false, true)) { again.set(true); return; } // đang chạy: chạy thêm 1 vòng sau
        AppDatabase.executor.execute(() -> {
            try {
                do {
                    again.set(false);
                    doSync();
                } while (again.get());
            } catch (Exception e) {
                Log.e(TAG, "Sync failed, will retry later", e); // thay đổi vẫn nằm ở local
            } finally {
                running.set(false);
                notifyChanged();
            }
        });
    }

    private void doSync() throws IOException {
        processDeletes();
        pushCategories();
        pushDishes();
        pullCategories();
        pullDishes();
        Log.d(TAG, "Sync finished");
    }

    // ---------- Xóa ----------
    // Món/loại đã xóa ở local mà có RemoteId: nhớ lại để xóa trên MockAPI (kể cả khi lúc xóa đang offline)
    public void recordDeletion(boolean isDish, String remoteId) {
        if (remoteId != null) addToSet(isDish ? KEY_DEL_DISH : KEY_DEL_CAT, remoteId);
        syncNow();
    }

    private void processDeletes() throws IOException {
        for (String id : getSet(KEY_DEL_DISH)) {
            if (isDone(api.deleteDish(id).execute())) removeFromSet(KEY_DEL_DISH, id);
        }
        for (String id : getSet(KEY_DEL_CAT)) {
            if (isDone(api.deleteCategory(id).execute())) removeFromSet(KEY_DEL_CAT, id);
        }
    }

    private boolean isDone(Response<?> r) { return r.isSuccessful() || r.code() == 404; }

    // ---------- Đẩy lên ----------
    private void pushCategories() throws IOException {
        for (DishCategory c : db.categoryDao().getPending()) {
            CategoryDto dto = new CategoryDto();
            dto.name = c.name;
            dto.description = c.description;
            String remoteId;
            if (c.remoteId == null) {
                remoteId = body(api.createCategory(dto).execute()).id;   // tạo mới -> nhận id
            } else {
                body(api.updateCategory(c.remoteId, dto).execute());     // đã có id -> cập nhật, không tạo trùng
                remoteId = c.remoteId;
            }
            db.categoryDao().markSynced(c.id, remoteId);
        }
    }

    private void pushDishes() throws IOException {
        for (Dish d : db.dishDao().getPending()) {
            DishCategory cat = db.categoryDao().getById(d.dishCategoryId);
            if (cat == null || cat.remoteId == null) continue; // loại chưa lên MockAPI, để lần sau
            DishDto dto = new DishDto();
            dto.name = d.name;
            dto.price = d.price;
            dto.preparationTime = d.preparationTime;
            dto.isAvailable = d.isAvailable;
            dto.description = d.description;
            dto.dishCategoryId = cat.remoteId;
            dto.dishImages = imagesToJson(db.imageDao().getByDish(d.id));
            String remoteId;
            if (d.remoteId == null) {
                remoteId = body(api.createDish(dto).execute()).id;
            } else {
                body(api.updateDish(d.remoteId, dto).execute());
                remoteId = d.remoteId;
            }
            db.dishDao().markSynced(d.id, remoteId);
        }
    }

    // ---------- Kéo về ----------
    private void pullCategories() throws IOException {
        List<CategoryDto> remote = body(api.getCategories().execute());
        Set<String> deleted = getSet(KEY_DEL_CAT);
        for (CategoryDto r : remote) {
            if (r.id == null || deleted.contains(r.id)) continue;
            DishCategory local = db.categoryDao().getByRemoteId(r.id);
            if (local == null) {
                DishCategory c = new DishCategory();
                c.name = r.name; c.description = r.description;
                c.remoteId = r.id; c.syncStatus = Constants.SYNC_SYNCED;
                db.categoryDao().insert(c);
            } else if (local.syncStatus == Constants.SYNC_SYNCED
                    && (!Objects.equals(local.name, r.name) || !Objects.equals(local.description, r.description))) {
                local.name = r.name; local.description = r.description;
                db.categoryDao().update(local);
            } // local đang chờ đồng bộ: giữ bản local
        }
    }

    private void pullDishes() throws IOException {
        List<DishDto> remote = body(api.getDishes().execute());
        Set<String> deleted = getSet(KEY_DEL_DISH);
        for (DishDto r : remote) {
            if (r.id == null || deleted.contains(r.id)) continue;
            DishCategory cat = r.dishCategoryId == null ? null : db.categoryDao().getByRemoteId(r.dishCategoryId);
            if (cat == null) continue; // không xác định được loại
            Dish local = db.dishDao().getByRemoteId(r.id);
            if (local == null) {
                Dish d = new Dish();
                applyRemote(d, r, cat.id);
                d.remoteId = r.id; d.syncStatus = Constants.SYNC_SYNCED;
                int newId = (int) db.dishDao().insert(d);
                replaceImages(newId, r.dishImages);
            } else if (local.syncStatus == Constants.SYNC_SYNCED) {
                String localImages = imagesToJson(db.imageDao().getByDish(local.id));
                boolean changed = !Objects.equals(local.name, r.name)
                        || local.price != (r.price == null ? 0 : r.price)
                        || local.preparationTime != (r.preparationTime == null ? 0 : r.preparationTime)
                        || local.isAvailable != Boolean.TRUE.equals(r.isAvailable)
                        || !Objects.equals(local.description, r.description)
                        || local.dishCategoryId != cat.id
                        || !Objects.equals(localImages, r.dishImages);
                if (changed) {
                    applyRemote(local, r, cat.id);
                    db.dishDao().update(local);
                    replaceImages(local.id, r.dishImages);
                }
            } // local đang chờ đồng bộ: giữ bản local
        }
    }

    private void applyRemote(Dish d, DishDto r, int localCategoryId) {
        d.name = r.name;
        d.price = r.price == null ? 0 : r.price;
        d.preparationTime = r.preparationTime == null ? 0 : r.preparationTime;
        d.isAvailable = Boolean.TRUE.equals(r.isAvailable);
        d.description = r.description;
        d.dishCategoryId = localCategoryId;
    }

    // ---------- Ảnh <-> JSON ----------
    private String imagesToJson(List<DishImage> images) {
        List<ImageJson> list = new ArrayList<>();
        for (DishImage i : images) {
            ImageJson j = new ImageJson();
            j.imageUrl = i.imageUrl; j.isThumbnail = i.isThumbnail;
            list.add(j);
        }
        return gson.toJson(list);
    }

    private void replaceImages(int dishId, String json) {
        db.imageDao().deleteByDish(dishId);
        List<ImageJson> list = new ArrayList<>();
        if (json != null && !json.isEmpty()) {
            try {
                Type type = new TypeToken<List<ImageJson>>() {}.getType();
                List<ImageJson> parsed = gson.fromJson(json, type);
                if (parsed != null) list = parsed;
            } catch (JsonSyntaxException ignored) {}
        }
        boolean hasThumb = false;
        for (ImageJson j : list) {
            if (j == null || j.imageUrl == null) continue;
            DishImage img = new DishImage();
            img.dishId = dishId;
            img.imageUrl = j.imageUrl;
            img.isThumbnail = j.isThumbnail && !hasThumb; // tối đa 1 ảnh đại diện
            if (img.isThumbnail) hasThumb = true;
            db.imageDao().insert(img);
        }
    }

    // ---------- Tiện ích ----------
    private <T> T body(Response<T> r) throws IOException {
        if (!r.isSuccessful() || r.body() == null) throw new IOException("HTTP " + r.code());
        return r.body();
    }

    private SharedPreferences prefs() { return app.getSharedPreferences(PREFS, Context.MODE_PRIVATE); }

    private Set<String> getSet(String key) {
        return new HashSet<>(prefs().getStringSet(key, new HashSet<>()));
    }

    private synchronized void addToSet(String key, String value) {
        Set<String> s = getSet(key); s.add(value);
        prefs().edit().putStringSet(key, s).apply();
    }

    private synchronized void removeFromSet(String key, String value) {
        Set<String> s = getSet(key); s.remove(value);
        prefs().edit().putStringSet(key, s).apply();
    }
}