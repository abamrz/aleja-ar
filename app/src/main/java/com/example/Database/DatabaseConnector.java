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

import java.util.ArrayList;


public class DatabaseConnector extends SQLiteOpenHelper {

        public static String DATABASE_NAME = "graph_db";
        private static final int DATABASE_VERSION = 1;
        private static final String TABLE_GRAPH = "graphs";
        private static final String KEY_ID = "id";
        private static final String KEY_NAME = "name";

        private static final String CREATE_TABLE_GRAPHS = "CREATE TABLE "
                    + TABLE_GRAPH + "(" + KEY_ID
                    + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    KEY_NAME + " VARCHAR NOT NULL "+
                    "); ";

        public DatabaseConnector(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
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
        public long addGraph(@NonNull String name) {
            SQLiteDatabase db = this.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(KEY_NAME, name);
            long insert = db.insert(TABLE_GRAPH, null, values);
            return insert;
        }

        // read -> choose existing plan
        @NonNull
        public ArrayList<ARGraph> getAllGraphs() {
            ArrayList<ARGraph> graphsModelArrayList = new ArrayList<ARGraph>();

            String selectQuery = "SELECT  * FROM " + TABLE_GRAPH;
            SQLiteDatabase db = this.getReadableDatabase();
            @SuppressLint("Recycle") Cursor c = db.rawQuery(selectQuery, null);
            // looping through all rows and adding to list
            if (c.moveToFirst()) {
                do {
                    ARGraph graphModel = new ARGraph();
                    graphModel.setName(c.getString(c.getColumnIndex(KEY_NAME)));
                    graphsModelArrayList.add(graphModel);
                } while (c.moveToNext());
            }
            c.close();
            return graphsModelArrayList;
        }

        // update by name, if needed
        public int updateGraph(@NonNull String name) {
            SQLiteDatabase db = this.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(KEY_NAME, name);
            // update row
            return db.update(TABLE_GRAPH, values, KEY_NAME + " = ?",
                    new String[]{String.valueOf(name)});
        }

        // delete
        public void deleteGraph(@NonNull String name) {
            SQLiteDatabase db = this.getWritableDatabase();
            db.delete(TABLE_GRAPH, KEY_NAME + " = ?",
                    new String[]{String.valueOf(name)});
            db.close();
        }
}
