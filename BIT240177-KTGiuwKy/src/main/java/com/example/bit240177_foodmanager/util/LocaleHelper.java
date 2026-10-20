package com.example.bit240177_foodmanager.util;

import android.content.Context;
import android.content.res.Configuration;
import java.util.Locale;

public class LocaleHelper {
    private static final String PREFS = Constants.MSSV + "prefs";
    private static final String KEY_LANG = "lang";

    public static String getLanguage(Context c) {
        return c.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(KEY_LANG, "en");
    }

    public static void setLanguage(Context c, String lang) {
        c.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putString(KEY_LANG, lang).apply();
    }

    public static void toggle(Context c) {
        setLanguage(c, getLanguage(c).equals("en") ? "vi" : "en");
    }

    public static Context wrap(Context base) {
        Locale locale = new Locale(getLanguage(base));
        Locale.setDefault(locale);
        Configuration config = new Configuration(base.getResources().getConfiguration());
        config.setLocale(locale);
        return base.createConfigurationContext(config);
    }
}