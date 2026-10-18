package com.example.coursemanager.api;

import com.example.coursemanager.model.Category;
import com.example.coursemanager.model.Course;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.*;

public interface ApiService {
    @GET("categories") Call<List<Category>> getCategories();
    @POST("categories") Call<Category> addCategory(@Body Category c);
    @PUT("categories/{id}") Call<Category> updateCategory(@Path("id") String id, @Body Category c);
    @DELETE("categories/{id}") Call<Category> deleteCategory(@Path("id") String id);

    @GET("courses") Call<List<Course>> getCourses();
    @GET("courses/{id}") Call<Course> getCourse(@Path("id") String id);
    @POST("courses") Call<Course> addCourse(@Body Course c);
    @PUT("courses/{id}") Call<Course> updateCourse(@Path("id") String id, @Body Course c);
    @DELETE("courses/{id}") Call<Course> deleteCourse(@Path("id") String id);
}
