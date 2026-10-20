package com.example.bit240177_foodmanager.remote;

import com.example.bit240177_foodmanager.util.Constants;
import com.google.gson.annotations.SerializedName;

public class CategoryDto {
    @SerializedName("id") public String id; // MockAPI tự sinh, không có tiền tố
    @SerializedName(Constants.COL_NAME) public String name;
    @SerializedName(Constants.COL_DESCRIPTION) public String description;
}