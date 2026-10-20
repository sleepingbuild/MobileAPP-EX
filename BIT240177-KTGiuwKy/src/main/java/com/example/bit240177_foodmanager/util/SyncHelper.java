package com.example.bit240177_foodmanager.util;

import com.example.bit240177_foodmanager.model.Dish;
import com.example.bit240177_foodmanager.model.DishCategory;

public class SyncHelper {
    // Món chưa từng lên MockAPI thì giữ "chờ tạo"; món đã có remoteId thì chuyển thành "chờ sửa"
    public static void markModified(Dish d) {
        d.syncStatus = (d.remoteId == null) ? Constants.SYNC_PENDING_CREATE : Constants.SYNC_PENDING_UPDATE;
    }
    public static void markModified(DishCategory c) {
        c.syncStatus = (c.remoteId == null) ? Constants.SYNC_PENDING_CREATE : Constants.SYNC_PENDING_UPDATE;
    }
}