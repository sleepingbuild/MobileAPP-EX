package com.example.studentmanager.data

import androidx.room.*

@Dao
interface StudentDao {

    @Query("SELECT * FROM students ORDER BY name ASC")
    suspend fun getAll(): List<Student>

    @Query("SELECT * FROM students WHERE id = :id")
    suspend fun getById(id: Int): Student?

    @Insert
    suspend fun insert(student: Student): Long

    @Update
    suspend fun update(student: Student): Int

    @Delete
    suspend fun delete(student: Student): Int
}