package com.example.bit240177_foodmanager.util;

public final class Constants {
    private Constants() {}

    public static final String MSSV = "BIT240177_";

    // Tên bảng / resource
    public static final String TABLE_CATEGORY = MSSV + "DishCategory";
    public static final String TABLE_DISH     = MSSV + "Dish";
    public static final String TABLE_IMAGE    = MSSV + "DishImage";

    // Cột chung
    public static final String COL_ID = MSSV + "Id";
    public static final String COL_NAME = MSSV + "Name";
    public static final String COL_DESCRIPTION = MSSV + "Description";

    // Cột của Dish
    public static final String COL_PRICE = MSSV + "Price";
    public static final String COL_PREP_TIME = MSSV + "PreparationTime";
    public static final String COL_IS_AVAILABLE = MSSV + "IsAvailable";
    public static final String COL_CATEGORY_ID = MSSV + "DishCategoryId";

    // Cột của DishImage
    public static final String COL_DISH_ID = MSSV + "DishId";
    public static final String COL_IMAGE_URL = MSSV + "ImageUrl";
    public static final String COL_IS_THUMBNAIL = MSSV + "IsThumbnail";

    // Cột phục vụ đồng bộ (chỉ ở local)
    public static final String COL_REMOTE_ID = MSSV + "RemoteId";
    public static final String COL_SYNC_STATUS = MSSV + "SyncStatus";
    // Trạng thái đồng bộ (chỉ ở local)
    public static final int SYNC_SYNCED = 0;          // đã khớp với MockAPI
    public static final int SYNC_PENDING_CREATE = 1;  // tạo ở local, chưa lên MockAPI
    public static final int SYNC_PENDING_UPDATE = 2;  // sửa ở local, chưa lên MockAPI

    // MockAPI: dán endpoint ở Bước 1, kết thúc bằng dấu "/"
    public static final String BASE_URL = "https://XXXXXXXX.mockapi.io/api/v1/";
    public static final String COL_DISH_IMAGES = MSSV + "DishImages"; // chỉ dùng cho MockAPI
}