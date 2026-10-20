package com.example.bit240177_foodmanager.data;

import androidx.room.*;
import com.example.bit240177_foodmanager.model.Dish;
import com.example.bit240177_foodmanager.util.Constants;
import java.util.List;

@Dao
public interface DishDao {
    @Insert long insert(Dish d);
    @Update void update(Dish d);
    @Delete void delete(Dish d);

    @Query("SELECT * FROM " + Constants.TABLE_DISH + " WHERE " + Constants.COL_ID + " = :id")
    Dish getById(int id);

    // sort: 0 = mặc định, 1 = giá tăng, 2 = giá giảm, 3 = thời gian chế biến tăng, 4 = giảm
    @Query("SELECT * FROM " + Constants.TABLE_DISH + " WHERE "
            + "(:keyword IS NULL OR " + Constants.COL_NAME + " LIKE '%' || :keyword || '%') "
            + "AND (:categoryId IS NULL OR " + Constants.COL_CATEGORY_ID + " = :categoryId) "
            + "AND (:onlyAvailable = 0 OR " + Constants.COL_IS_AVAILABLE + " = 1) "
            + "AND (:minPrice IS NULL OR " + Constants.COL_PRICE + " >= :minPrice) "
            + "AND (:maxPrice IS NULL OR " + Constants.COL_PRICE + " <= :maxPrice) "
            + "ORDER BY "
            + "CASE WHEN :sort = 1 THEN " + Constants.COL_PRICE + " END ASC, "
            + "CASE WHEN :sort = 2 THEN " + Constants.COL_PRICE + " END DESC, "
            + "CASE WHEN :sort = 3 THEN " + Constants.COL_PREP_TIME + " END ASC, "
            + "CASE WHEN :sort = 4 THEN " + Constants.COL_PREP_TIME + " END DESC, "
            + Constants.COL_ID + " ASC")
    List<Dish> search(String keyword, Integer categoryId, int onlyAvailable,
                      Double minPrice, Double maxPrice, int sort);

    // Kiểm tra trùng tên trong cùng loại (không phân biệt hoa thường), loại trừ chính món đang sửa
    @Query("SELECT COUNT(*) FROM " + Constants.TABLE_DISH + " WHERE "
            + "LOWER(TRIM(" + Constants.COL_NAME + ")) = LOWER(TRIM(:name)) "
            + "AND " + Constants.COL_CATEGORY_ID + " = :categoryId "
            + "AND " + Constants.COL_ID + " != :excludeId")
    int countDuplicate(String name, int categoryId, int excludeId);

    @Query("SELECT * FROM " + Constants.TABLE_DISH + " WHERE " + Constants.COL_SYNC_STATUS + " != 0")
    List<Dish> getPending();

    @Query("SELECT * FROM " + Constants.TABLE_DISH + " WHERE " + Constants.COL_REMOTE_ID + " = :remoteId LIMIT 1")
    Dish getByRemoteId(String remoteId);

    @Query("UPDATE " + Constants.TABLE_DISH + " SET " + Constants.COL_REMOTE_ID + " = :remoteId, "
            + Constants.COL_SYNC_STATUS + " = 0 WHERE " + Constants.COL_ID + " = :id")
    void markSynced(int id, String remoteId);

    @Query("SELECT COUNT(*) FROM " + Constants.TABLE_DISH + " WHERE " + Constants.COL_SYNC_STATUS + " != 0")
    int countPending();
}