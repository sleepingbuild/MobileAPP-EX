package com.example.bit240177_foodmanager.util;

import android.util.Patterns;

public class Validators {
    public static boolean isValidImageUrl(String url) {
        if (url == null) return false;
        String u = url.trim().toLowerCase();
        return (u.startsWith("http://") || u.startsWith("https://"))
                && Patterns.WEB_URL.matcher(url.trim()).matches();
    }
}