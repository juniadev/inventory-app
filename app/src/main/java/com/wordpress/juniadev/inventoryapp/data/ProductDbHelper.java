package com.wordpress.juniadev.inventoryapp.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class ProductDbHelper extends SQLiteOpenHelper {

    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "inventory.db";

    private static final String SQL_CREATE_PRODUCTS_TABLE =
            "CREATE TABLE " + ProductContract.ProductEntry.TABLE_NAME + " (" +
            ProductContract.ProductEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            ProductContract.ProductEntry.COLUMN_NAME + " TEXT NOT NULL, " +
            ProductContract.ProductEntry.COLUMN_PRICE + " REAL NOT NULL, " +
            ProductContract.ProductEntry.COLUMN_QUANTITY_AVAILABLE + " INTEGER NOT NULL DEFAULT 0, " +
            ProductContract.ProductEntry.COLUMN_SUPPLIER_EMAIL + " TEXT, " +
            ProductContract.ProductEntry.COLUMN_IMAGE + " BLOB);";

    public ProductDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.i(ProductDbHelper.class.getSimpleName(), "Creating table with SQL: " + SQL_CREATE_PRODUCTS_TABLE);
        db.execSQL(SQL_CREATE_PRODUCTS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + ProductContract.ProductEntry.TABLE_NAME);
        onCreate(db);
    }
}
