package com.example.coursemanager.ui;

import android.view.MenuItem;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class BaseActivity extends AppCompatActivity {

    /** Màn hình con có nút quay lại; MainActivity sẽ tắt đi. */
    protected boolean showBack() { return true; }

    @Override public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);
        if (showBack() && getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        final View content = findViewById(android.R.id.content);
        content.getViewTreeObserver().addOnGlobalLayoutListener(() -> fixInsets(content));
    }

    /** Đo phần nội dung bị thanh tiêu đề / thanh điều hướng che rồi đẩy nội dung ra khỏi vùng đó. */
    private void fixInsets(View content) {
        int top = 0, bottom = 0;
        int[] c = new int[2];
        content.getLocationOnScreen(c);

        View bar = findViewById(androidx.appcompat.R.id.action_bar_container);
        if (bar != null && bar.getHeight() > 0) {
            int[] b = new int[2];
            bar.getLocationOnScreen(b);
            top = Math.max(0, (b[1] + bar.getHeight()) - c[1]);
        }

        View decor = getWindow().getDecorView();
        WindowInsetsCompat ri = ViewCompat.getRootWindowInsets(decor);
        if (ri != null && decor.getHeight() > 0) {
            int nav = ri.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom;
            bottom = Math.max(0, (c[1] + content.getHeight()) - (decor.getHeight() - nav));
        }

        if (content.getPaddingTop() != top || content.getPaddingBottom() != bottom) {
            content.setPadding(content.getPaddingLeft(), top, content.getPaddingRight(), bottom);
        }
    }

    @Override public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}