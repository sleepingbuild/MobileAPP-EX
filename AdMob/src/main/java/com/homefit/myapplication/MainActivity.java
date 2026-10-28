package com.homefit.myapplication;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.homefit.myapplication.R;
import com.homefit.myapplication.SecondActivity;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private AdView mAdView;
    private InterstitialAd mInterstitialAd;
    private Button btnNext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        MobileAds.initialize(this, initializationStatus -> {});

        // 1. Kiểm tra mạng trước khi tải Banner Ad
        mAdView = findViewById(R.id.adView);
        if (isNetworkAvailable()) {
            AdRequest adRequest = new AdRequest.Builder().build();
            mAdView.loadAd(adRequest);
        } else {
            // Không có mạng -> Ẩn hoặc không tải banner
            Log.i(TAG, "No internet connection for Banner Ad.");
        }

        // 2. Tải trước Interstitial Ad ngầm (nếu có mạng)
        if (isNetworkAvailable()) {
            loadInterstitialAd();
        }

        // 3. Xử lý sự kiện nhấn nút chuyển trang
        btnNext = findViewById(R.id.btnNext);
        btnNext.setOnClickListener(v -> {
            // Kiểm tra nếu có mạng VÀ quảng cáo đã sẵn sàng thì mới hiện ad, ngược lại chuyển trang luôn
            if (isNetworkAvailable() && mInterstitialAd != null) {
                mInterstitialAd.show(MainActivity.this);

                mInterstitialAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                    @Override
                    public void onAdDismissedFullScreenContent() {
                        // Người dùng tắt quảng cáo xong -> Chuyển sang màn 2
                        navigateToSecondScreen();
                        loadInterstitialAd(); // Tải lại ad cho lần sau
                    }

                    @Override
                    public void onAdFailedToShowFullScreenContent(@NonNull com.google.android.gms.ads.AdError adError) {
                        // Lỗi hiển thị ad -> Chuyển trang luôn
                        navigateToSecondScreen();
                    }
                });
            } else {
                // Không có mạng hoặc ad bị lỗi/chưa tải xong -> Chuyển trang ngay lập tức
                navigateToSecondScreen();
                if (isNetworkAvailable() && mInterstitialAd == null) {
                    loadInterstitialAd();
                }
            }
        });
    }

    // Hàm kiểm tra kết nối Internet (Wi-Fi hoặc Mobile Data)
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(connectivityManager.getActiveNetwork());
            if (capabilities != null) {
                return capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                        capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                        capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET);
            }
        }
        return false;
    }

    private void loadInterstitialAd() {
        if (!isNetworkAvailable()) return;

        AdRequest interstitialAdRequest = new AdRequest.Builder().build();
        InterstitialAd.load(this, "ca-app-pub-3940256099942544/1033173712", interstitialAdRequest,
                new InterstitialAdLoadCallback() {
                    @Override
                    public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                        mInterstitialAd = interstitialAd;
                        Log.i(TAG, "Interstitial ad loaded successfully.");
                    }

                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                        mInterstitialAd = null; // Đánh dấu ad bị lỗi
                        Log.i(TAG, "Interstitial ad failed to load: " + loadAdError.getMessage());
                    }
                });
    }

    private void navigateToSecondScreen() {
        Intent intent = new Intent(MainActivity.this, SecondActivity.class);
        startActivity(intent);
    }
}