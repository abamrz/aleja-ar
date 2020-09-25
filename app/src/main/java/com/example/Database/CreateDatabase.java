package com.example.Database;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = {EmployeeModel.class}, version = 1)
public abstract class CreateDatabase extends RoomDatabase {
    public abstract EmployeeDao employeeDao();
}
