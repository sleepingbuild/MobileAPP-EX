package com.example.coursemanager.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.example.coursemanager.R;

public class TrailerActivity extends BaseActivity {
    private VideoView video;
    private ProgressBar progress;
    private TextView tvError;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private boolean prepared = false;

    @Override protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_trailer);
        setTitle("Trailer");
        video = findViewById(R.id.video);
        progress = findViewById(R.id.progress);
        tvError = findViewById(R.id.tvError);
        String url = getIntent().getStringExtra("url");

        if (url == null || url.trim().isEmpty()) { showError("Khóa học chưa có URL trailer."); return; }

        // YouTube: VideoView không phát được -> mở app/trình duyệt
        if (url.contains("youtube.com") || url.contains("youtu.be")) {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
            finish();
            return;
        }

        MediaController mc = new MediaController(this);
        mc.setAnchorView(video);
        video.setMediaController(mc);
        video.setVideoURI(Uri.parse(url));
        video.setOnPreparedListener(mp -> {
            prepared = true;
            handler.removeCallbacksAndMessages(null);
            progress.setVisibility(View.GONE);
            video.start();
        });
        video.setOnErrorListener((mp, what, extra) -> {
            showError("Không phát được video. Kiểm tra URL MP4 / kết nối mạng.");
            return true;
        });
        // Quá 20 giây chưa tải được thì báo lỗi
        handler.postDelayed(() -> {
            if (!prepared) showError("Tải video quá lâu. Kiểm tra mạng hoặc dùng URL MP4 nhỏ hơn.");
        }, 20000);
    }

    private void showError(String msg) {
        progress.setVisibility(View.GONE);
        tvError.setText(msg);
        tvError.setVisibility(View.VISIBLE);
    }

    @Override public boolean onSupportNavigateUp() { finish(); return true; }

    @Override protected void onPause() { super.onPause(); if (video != null) video.pause(); }

    @Override protected void onDestroy() { super.onDestroy(); handler.removeCallbacksAndMessages(null); }
}