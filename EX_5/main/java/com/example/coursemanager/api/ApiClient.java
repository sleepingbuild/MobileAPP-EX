package com.example.coursemanager.api;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {
    // TODO: thay YOUR_PROJECT_ID bằng ID project MockAPI của bạn (phải kết thúc bằng dấu "/")
    public static final String BASE_URL = "https://6acdfc87f338359cf1c2ceed.mockapi.io/api/v1/";
    private static ApiService service;

    public static ApiService get() {
        if (service == null) {
            service = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build().create(ApiService.class);
        }
        return service;
    }
}
