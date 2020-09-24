package com.example.Database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface EmployeeDao {

    @Insert
    void insert(EmployeeModel employeeModels);

    @Delete
    void delete(EmployeeModel employeeModel);

    @Update
    void update(EmployeeModel employeeModel);

    @Query("SELECT * FROM employee_model")
    List<EmployeeModel> getAll();

    @Query("SELECT * FROM employee_model WHERE id = :id")
    EmployeeModel getById(long id);

}
