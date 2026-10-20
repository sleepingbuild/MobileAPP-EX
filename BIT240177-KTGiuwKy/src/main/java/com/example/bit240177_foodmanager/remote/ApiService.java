package com.example.bit240177_foodmanager.remote;

import com.example.bit240177_foodmanager.util.Constants;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface ApiService {
    @GET(Constants.TABLE_CATEGORY)
    Call<List<CategoryDto>> getCategories();
    @POST(Constants.TABLE_CATEGORY)
    Call<CategoryDto> createCategory(@Body CategoryDto body);
    @PUT(Constants.TABLE_CATEGORY + "/{id}")
    Call<CategoryDto> updateCategory(@Path("id") String id, @Body CategoryDto body);
    @DELETE(Constants.TABLE_CATEGORY + "/{id}")
    Call<Void> deleteCategory(@Path("id") String id);

    @GET(Constants.TABLE_DISH)
    Call<List<DishDto>> getDishes();
    @POST(Constants.TABLE_DISH)
    Call<DishDto> createDish(@Body DishDto body);
    @PUT(Constants.TABLE_DISH + "/{id}")
    Call<DishDto> updateDish(@Path("id") String id, @Body DishDto body);
    @DELETE(Constants.TABLE_DISH + "/{id}")
    Call<Void> deleteDish(@Path("id") String id);
}