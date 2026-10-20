package com.example.bit240177_foodmanager.ui;

import android.content.Context;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.example.bit240177_foodmanager.R;
import com.example.bit240177_foodmanager.util.LocaleHelper;

public abstract class BaseActivity extends AppCompatActivity {

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.wrap(newBase));
    }

    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);

        // Chừa chỗ cho thanh trạng thái, thanh điều hướng, tai thỏ và bàn phím
        View content = findViewById(android.R.id.content);
        ViewCompat.setOnApplyWindowInsetsListener(content, (v, insets) -> {
            Insets bars = insets.getInsets(
                    WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.displayCutout());
            Insets ime = insets.getInsets(WindowInsetsCompat.Type.ime());
            v.setPadding(bars.left, bars.top, bars.right, Math.max(bars.bottom, ime.bottom));
            return WindowInsetsCompat.CONSUMED;
        });

        // Nút Quay lại (nếu layout có btnBack)
        View back = findViewById(R.id.btnBack);
        if (back != null) back.setOnClickListener(v -> finish());
    }
}