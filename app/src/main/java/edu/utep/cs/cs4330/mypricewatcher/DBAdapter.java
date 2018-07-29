package edu.utep.cs.cs4330.mypricewatcher;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DBAdapter {
    static final String KEY_ROWID = "_id";
    static final String KEY_NAME = "name";
    static final String KEY_CURRENTPRICE = "currentprice";
    static final String KEY_INITIALPRICE = "initialprice";
    static final String KEY_URL = "url";

    static final String TAG = "DBAdapter";
    static final String DATABASE_NAME = "MyDB";
    static final String DATABASE_TABLE = "products";
    static final int DATABASE_VERSION = 1;
    static final String DATABASE_CREATE =
            "create table products (name VARCHAR, "
                    + "initialprice REAL, currentprice REAL, url VARCHAR);";
    final Context context;
    DatabaseHelper DBHelper;
    SQLiteDatabase db;

    public DBAdapter(Context ctx) {
        this.context = ctx;
        DBHelper = new DatabaseHelper(context);
    }

    private static class DatabaseHelper extends SQLiteOpenHelper {
        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            try {
                db.execSQL(DATABASE_CREATE);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
                    + newVersion + ", which will destroy all old data");
            db.execSQL("DROP TABLE IF EXISTS products");
            onCreate(db);
        }
    }

    //---opens the database---
    public DBAdapter open() throws SQLException {
        db = DBHelper.getWritableDatabase();
        return this;
    }

    //---closes the database---
    public void close() {
        DBHelper.close();
    }

    //---insert a contact into the database---
    public long insertProduct(String name, double iPrice, double cPrice, String url) {
        ContentValues initialValues = new ContentValues();
        initialValues.put(KEY_NAME, name);
        initialValues.put(KEY_INITIALPRICE, iPrice);
        initialValues.put(KEY_CURRENTPRICE, cPrice);
        initialValues.put(KEY_URL, url);
        return db.insert(DATABASE_TABLE, null, initialValues);
    }

    public boolean removeProduct(String position) {
        //position = position+1;
        return db.delete(DATABASE_TABLE, KEY_NAME + "='" + position+ "'", null) > 0;

    }

    public Cursor getAllProducts() {
        return db.query(DATABASE_TABLE, new String[]{KEY_NAME,
                KEY_CURRENTPRICE,
                KEY_INITIALPRICE,
                KEY_URL
        }, null, null, null, null, null);
    }

    public boolean updateProduct(String position, String name, double initialPrice, double currentPrice, String url) {
        ContentValues args = new ContentValues();
        args.put(KEY_NAME, name);
        args.put(KEY_INITIALPRICE, initialPrice);
        args.put(KEY_CURRENTPRICE, currentPrice);
        args.put(KEY_URL, url);
        return db.update(DATABASE_TABLE, args, KEY_NAME + "='" + position+ "'", null) > 0;
    }

}
