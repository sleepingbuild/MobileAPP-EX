package com.example.bit240177_foodmanager.remote;

import com.example.bit240177_foodmanager.util.Constants;
import com.google.gson.annotations.SerializedName;

public class ImageJson {
    @SerializedName(Constants.COL_IMAGE_URL) public String imageUrl;
    @SerializedName(Constants.COL_IS_THUMBNAIL) public boolean isThumbnail;
}