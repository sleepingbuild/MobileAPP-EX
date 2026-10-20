package com.example.bit240177_foodmanager.data;

import androidx.room.*;
import com.example.bit240177_foodmanager.model.DishCategory;
import com.example.bit240177_foodmanager.util.Constants;
import java.util.List;

@Dao
public interface CategoryDao {
    @Insert long insert(DishCategory c);
    @Update void update(DishCategory c);
    @Delete void delete(DishCategory c);

    @Query("SELECT * FROM " + Constants.TABLE_CATEGORY + " ORDER BY " + Constants.COL_NAME)
    List<DishCategory> getAll();

    @Query("SELECT * FROM " + Constants.TABLE_CATEGORY + " WHERE " + Constants.COL_ID + " = :id")
    DishCategory getById(int id);

    // Dùng cho ràng buộc: loại đang có bao nhiêu món
    @Query("SELECT COUNT(*) FROM " + Constants.TABLE_DISH + " WHERE " + Constants.COL_CATEGORY_ID + " = :categoryId")
    int countDishes(int categoryId);
    @Query("SELECT COUNT(*) FROM " + Constants.TABLE_CATEGORY + " WHERE "
            + "LOWER(TRIM(" + Constants.COL_NAME + ")) = LOWER(TRIM(:name)) "
            + "AND " + Constants.COL_ID + " != :excludeId")
    int countDuplicate(String name, int excludeId);

    @Query("SELECT * FROM " + Constants.TABLE_CATEGORY + " WHERE " + Constants.COL_SYNC_STATUS + " != 0")
    List<DishCategory> getPending();

    @Query("SELECT * FROM " + Constants.TABLE_CATEGORY + " WHERE " + Constants.COL_REMOTE_ID + " = :remoteId LIMIT 1")
    DishCategory getByRemoteId(String remoteId);

    @Query("UPDATE " + Constants.TABLE_CATEGORY + " SET " + Constants.COL_REMOTE_ID + " = :remoteId, "
            + Constants.COL_SYNC_STATUS + " = 0 WHERE " + Constants.COL_ID + " = :id")
    void markSynced(int id, String remoteId);

    @Query("SELECT COUNT(*) FROM " + Constants.TABLE_CATEGORY + " WHERE " + Constants.COL_SYNC_STATUS + " != 0")
    int countPending();
}