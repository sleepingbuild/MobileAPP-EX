package com.example.bit240177_foodmanager.data;

import androidx.room.*;
import com.example.bit240177_foodmanager.model.DishImage;
import com.example.bit240177_foodmanager.util.Constants;
import java.util.List;

@Dao
public interface ImageDao {
    @Insert long insert(DishImage img);
    @Update void update(DishImage img);
    @Delete void delete(DishImage img);

    @Query("SELECT * FROM " + Constants.TABLE_IMAGE + " WHERE " + Constants.COL_DISH_ID + " = :dishId")
    List<DishImage> getByDish(int dishId);

    // Bỏ cờ đại diện của mọi ảnh của món (gọi trước khi đặt ảnh đại diện mới)
    @Query("UPDATE " + Constants.TABLE_IMAGE + " SET " + Constants.COL_IS_THUMBNAIL + " = 0 WHERE "
            + Constants.COL_DISH_ID + " = :dishId")
    void clearThumbnail(int dishId);

    @Query("DELETE FROM " + Constants.TABLE_IMAGE + " WHERE " + Constants.COL_DISH_ID + " = :dishId")
    void deleteByDish(int dishId);
}