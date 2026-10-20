package com.example.bit240177_foodmanager.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;
import com.example.bit240177_foodmanager.util.Constants;

@Entity(
        tableName = Constants.TABLE_IMAGE,
        foreignKeys = @ForeignKey(
                entity = Dish.class,
                parentColumns = Constants.COL_ID,
                childColumns = Constants.COL_DISH_ID,
                onDelete = ForeignKey.CASCADE),
        indices = @Index(Constants.COL_DISH_ID))
public class DishImage {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = Constants.COL_ID)
    public int id;

    @ColumnInfo(name = Constants.COL_DISH_ID)
    public int dishId;

    @ColumnInfo(name = Constants.COL_IMAGE_URL)
    public String imageUrl;

    @ColumnInfo(name = Constants.COL_IS_THUMBNAIL)
    public boolean isThumbnail;
}