package com.example.bit240177_foodmanager.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;
import com.example.bit240177_foodmanager.util.Constants;

@Entity(
        tableName = Constants.TABLE_DISH,
        foreignKeys = @ForeignKey(
                entity = DishCategory.class,
                parentColumns = Constants.COL_ID,
                childColumns = Constants.COL_CATEGORY_ID,
                onDelete = ForeignKey.RESTRICT),
        indices = @Index(Constants.COL_CATEGORY_ID))
public class Dish {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = Constants.COL_ID)
    public int id;

    @ColumnInfo(name = Constants.COL_NAME)
    public String name;

    @ColumnInfo(name = Constants.COL_PRICE)
    public double price;

    @ColumnInfo(name = Constants.COL_PREP_TIME)
    public int preparationTime;

    @ColumnInfo(name = Constants.COL_IS_AVAILABLE)
    public boolean isAvailable;

    @ColumnInfo(name = Constants.COL_DESCRIPTION)
    public String description;

    @ColumnInfo(name = Constants.COL_CATEGORY_ID)
    public int dishCategoryId;

    @ColumnInfo(name = Constants.COL_REMOTE_ID)
    public String remoteId;

    @ColumnInfo(name = Constants.COL_SYNC_STATUS)
    public int syncStatus;
}