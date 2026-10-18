package com.example.coursemanager.api;

import android.util.Log;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public abstract class ApiCallback<T> implements Callback<T> {
    public abstract void onSuccess(T body);
    public abstract void onError(String message);

    @Override public void onResponse(Call<T> call, Response<T> r) {
        if (r.isSuccessful()) onSuccess(r.body());
        else if (r.code() == 404) onSuccess(null);
        else onError("Lỗi máy chủ (" + r.code() + ")");
    }

    @Override public void onFailure(Call<T> call, Throwable t) {
        Log.e("API", "Request lỗi: " + call.request().url(), t);
        onError("Lỗi: " + t.getClass().getSimpleName() + " - " + t.getMessage());
    }
}