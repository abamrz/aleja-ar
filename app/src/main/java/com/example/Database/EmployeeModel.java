package com.example.Database;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;
import androidx.room.util.TableInfo;

@Entity(tableName = "employee_model")
public class EmployeeModel {

    @PrimaryKey(autoGenerate = true)
    public final long id;

    @ColumnInfo(name = "Employee full name")
    public final String name;

    public EmployeeModel(final long id, final String name) {
        this.id = id;
        this.name = name;
    }
}
