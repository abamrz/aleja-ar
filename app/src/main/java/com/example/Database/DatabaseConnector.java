package com.example.Database;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import androidx.annotation.NonNull;

import com.example.AlejaGuidanceSystem.Graph.ARGraph;
import com.example.AlejaGuidanceSystem.Graph.ARGraphWithGrip;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;


public class DatabaseConnector extends SQLiteOpenHelper {

    private static DatabaseConnector instance;

    public static String DATABASE_NAME = "graph_db";
    private static final int DATABASE_VERSION = 1;
    private static final String TABLE_GRAPH = "graphs";
    private static final String KEY_ID = "id";
    private static final String KEY_NAME = "name";
    private static final String GRAPH_DATA = "graph_data";

    private static final String CREATE_TABLE_GRAPHS = "CREATE TABLE "
                + TABLE_GRAPH + "(" + KEY_ID
                + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                KEY_NAME + " VARCHAR NOT NULL, "+
                GRAPH_DATA + " BLOB NOT NULL" +
                "); ";

    public DatabaseConnector(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        instance = this;
        Log.d("table", CREATE_TABLE_GRAPHS);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_GRAPHS);
    }

    @Override
    public void onUpgrade(@NonNull SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS '" + TABLE_GRAPH + "'");
        onCreate(db);
    }

    // create -> make a new plan
    public long addGraph(@NonNull String name, ARGraphWithGrip graph_data) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_NAME, name);
        values.put(GRAPH_DATA, makebyte(graph_data));
        long insert = db.insert(TABLE_GRAPH, null, values);
        return insert;
    }

    // read -> choose existing plan
    @NonNull
    public ArrayList<ARGraphWithGrip> getAllGraphs() {
        ArrayList<ARGraphWithGrip> graphsModelArrayList = new ArrayList<>();

        String selectQuery = "SELECT  * FROM " + TABLE_GRAPH;
        SQLiteDatabase db = this.getReadableDatabase();
        @SuppressLint("Recycle") Cursor c = db.rawQuery(selectQuery, null);
        // looping through all rows and adding to list
        if (c.moveToFirst()) {
            do {
                ARGraphWithGrip graphModel = readGraphFromByte(c.getBlob(c.getColumnIndex(GRAPH_DATA)));
                graphsModelArrayList.add(graphModel);
            } while (c.moveToNext());
        }
        c.close();
        return graphsModelArrayList;
    }

    // delete, if needed
    public void deleteGraph(@NonNull String name) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_GRAPH, KEY_NAME + " = ?",
                new String[]{String.valueOf(name)});
        db.close();
    }

    /**
     * @return instance.
     */
    public static synchronized DatabaseConnector getInstance(Context context) {
        if (instance == null) {
            instance = new DatabaseConnector(context);
        }

        return instance;
    }

    public byte[] makebyte(ARGraphWithGrip modeldata) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(modeldata);
            byte[] employeeAsBytes = baos.toByteArray();
            ByteArrayInputStream bais = new ByteArrayInputStream(employeeAsBytes);

            return employeeAsBytes;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    public ARGraphWithGrip readGraphFromByte(byte[] data) {
        try {
            ByteArrayInputStream baip = new ByteArrayInputStream(data);
            ObjectInputStream ois = new ObjectInputStream(baip);
            ARGraphWithGrip dataobj = (ARGraphWithGrip) ois.readObject();

            return dataobj ;

        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }
}
