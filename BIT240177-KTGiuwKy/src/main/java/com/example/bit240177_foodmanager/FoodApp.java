package com.example.bit240177_foodmanager;

import android.app.Application;
import com.example.bit240177_foodmanager.sync.SyncManager;

public class FoodApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        SyncManager.get(this).startMonitoring(); // theo dõi mạng, có mạng là tự đồng bộ
    }
}