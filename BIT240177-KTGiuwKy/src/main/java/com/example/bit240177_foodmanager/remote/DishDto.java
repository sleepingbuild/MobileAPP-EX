package com.example.bit240177_foodmanager.remote;

import com.example.bit240177_foodmanager.util.Constants;
import com.google.gson.annotations.SerializedName;

public class DishDto {
    @SerializedName("id") public String id;
    @SerializedName(Constants.COL_NAME) public String name;
    @SerializedName(Constants.COL_PRICE) public Double price;
    @SerializedName(Constants.COL_PREP_TIME) public Integer preparationTime;
    @SerializedName(Constants.COL_IS_AVAILABLE) public Boolean isAvailable;
    @SerializedName(Constants.COL_DESCRIPTION) public String description;
    @SerializedName(Constants.COL_CATEGORY_ID) public String dishCategoryId; // id của loại trên MockAPI
    @SerializedName(Constants.COL_DISH_IMAGES) public String dishImages;     // chuỗi JSON danh sách ảnh
}