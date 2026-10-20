package com.example.bit240177_foodmanager.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import com.example.bit240177_foodmanager.util.Constants;

@Entity(tableName = Constants.TABLE_CATEGORY)
public class DishCategory {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = Constants.COL_ID)
    public int id;

    @ColumnInfo(name = Constants.COL_NAME)
    public String name;

    @ColumnInfo(name = Constants.COL_DESCRIPTION)
    public String description;

    @ColumnInfo(name = Constants.COL_REMOTE_ID)
    public String remoteId;

    @ColumnInfo(name = Constants.COL_SYNC_STATUS)
    public int syncStatus;
}