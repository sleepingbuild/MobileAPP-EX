package com.example.bit240177_foodmanager.data;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;
import com.example.bit240177_foodmanager.model.*;
import com.example.bit240177_foodmanager.util.Constants;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(entities = {DishCategory.class, Dish.class, DishImage.class},
        version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {
    public abstract CategoryDao categoryDao();
    public abstract DishDao dishDao();
    public abstract ImageDao imageDao();

    private static volatile AppDatabase INSTANCE;
    // Room không cho truy cập CSDL trên main thread, nên dùng executor này
    public static final ExecutorService executor = Executors.newFixedThreadPool(2);

    public static AppDatabase get(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    AppDatabase.class, Constants.MSSV + "FoodManager.db")
                            .addCallback(seedCallback)
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    private static final RoomDatabase.Callback seedCallback = new RoomDatabase.Callback() {
        @Override
        public void onCreate(@NonNull SupportSQLiteDatabase db) {
            super.onCreate(db);
            executor.execute(() -> seed(INSTANCE));
        }
    };

    private static void seed(AppDatabase db) {
        int main = (int) db.categoryDao().insert(cat("Main dishes", "Rice, noodles and soups"));
        int drink = (int) db.categoryDao().insert(cat("Drinks", "Tea, coffee and juices"));
        int dessert = (int) db.categoryDao().insert(cat("Desserts", "Sweet dishes and puddings"));

        addDish(db, "Pho bo", 45000, 20, true, "Beef noodle soup", main, "pho");
        addDish(db, "Bun cha", 40000, 15, true, "Grilled pork with noodles", main, "buncha");
        addDish(db, "Com tam", 35000, 15, false, "Broken rice with pork chop", main, "comtam");
        addDish(db, "Tra dao", 25000, 5, true, "Peach tea", drink, "tradao");
        addDish(db, "Che khuc bach", 30000, 10, true, "Panna cotta style dessert", dessert, "che");
    }

    private static DishCategory cat(String name, String desc) {
        DishCategory c = new DishCategory();
        c.name = name; c.description = desc;
        c.syncStatus = Constants.SYNC_PENDING_CREATE; // sẽ được đẩy lên MockAPI sau
        return c;
    }

    private static void addDish(AppDatabase db, String name, double price, int prep,
                                boolean available, String desc, int catId, String seed) {
        Dish d = new Dish();
        d.name = name; d.price = price; d.preparationTime = prep;
        d.isAvailable = available; d.description = desc; d.dishCategoryId = catId;
        d.syncStatus = Constants.SYNC_PENDING_CREATE;
        int dishId = (int) db.dishDao().insert(d);

        DishImage a = new DishImage();
        a.dishId = dishId; a.isThumbnail = true;
        a.imageUrl = "https://picsum.photos/seed/" + seed + "1/600/400";
        db.imageDao().insert(a);

        DishImage b = new DishImage();
        b.dishId = dishId; b.isThumbnail = false;
        b.imageUrl = "https://picsum.photos/seed/" + seed + "2/600/400";
        db.imageDao().insert(b);
    }
}