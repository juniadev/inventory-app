package com.wordpress.juniadev.inventoryapp.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

/**
 * {@link ContentProvider} for Inventory App.
 */
public class ProductProvider extends ContentProvider {

    private static final String LOG_TAG = ProductProvider.class.getSimpleName();

    private ProductDbHelper dbHelper;

    /** URI matcher code for the content URI for the products table */
    private static final int PRODUCTS = 100;

    /** URI matcher code for the content URI for a single product in the products table */
    private static final int PRODUCT_ID = 101;

    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        sUriMatcher.addURI(ProductContract.CONTENT_AUTHORITY, ProductContract.PATH_PRODUCTS, PRODUCTS);
        sUriMatcher.addURI(ProductContract.CONTENT_AUTHORITY, ProductContract.PATH_PRODUCTS + "/#", PRODUCT_ID);
    }

    @Override
    public boolean onCreate() {
        dbHelper = new ProductDbHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        SQLiteDatabase database = dbHelper.getReadableDatabase();

        Cursor cursor;

        int match = sUriMatcher.match(uri);
        switch (match) {
            case PRODUCTS:
                cursor = database.query(ProductContract.ProductEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case PRODUCT_ID:
                selection = ProductContract.ProductEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                cursor = database.query(ProductContract.ProductEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Cannot query unknown URI " + uri);
        }

        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PRODUCTS:
                return insertProduct(uri, contentValues);
            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }
    }

    private Uri insertProduct(Uri uri, ContentValues values) {

        validateInsertProduct(values);

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        long id = db.insert(ProductContract.ProductEntry.TABLE_NAME, null, values);

        if (id == -1) {
            Log.e(LOG_TAG, "Failed to insert row for " + uri);
            return null;
        }

        getContext().getContentResolver().notifyChange(uri, null);

        return ContentUris.withAppendedId(uri, id);
    }

    private void validateInsertProduct(ContentValues values) {
        validateProductName(values);
        validateProductPrice(values);
        validateProductQuantity(values);
    }

    private void validateUpdateProduct(ContentValues values) {
        if (values.containsKey(ProductContract.ProductEntry.COLUMN_NAME)) {
            validateProductName(values);
        }
        if (values.containsKey(ProductContract.ProductEntry.COLUMN_PRICE)) {
            validateProductPrice(values);
        }
        if (values.containsKey(ProductContract.ProductEntry.COLUMN_QUANTITY_AVAILABLE)) {
            validateProductQuantity(values);
        }
    }

    private void validateProductName(ContentValues values) {
        String name = values.getAsString(ProductContract.ProductEntry.COLUMN_NAME);
        if (TextUtils.isEmpty(name)) {
            throw new IllegalArgumentException("Product name is empty and is required.");
        }
    }

    private void validateProductPrice(ContentValues values) {
        Double price = values.getAsDouble(ProductContract.ProductEntry.COLUMN_PRICE);
        if (price == null || price < 0) {
            throw new IllegalArgumentException("Price is invalid.");
        }
    }

    private void validateProductQuantity(ContentValues values) {
        Integer quantity = values.getAsInteger(ProductContract.ProductEntry.COLUMN_QUANTITY_AVAILABLE);
        if (quantity != null && quantity < 0) {
            throw new IllegalArgumentException("Quantity is invalid.");
        }
    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String selection,
                      String[] selectionArgs) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PRODUCTS:
                return updateProduct(uri, contentValues, selection, selectionArgs);
            case PRODUCT_ID:
                selection = ProductContract.ProductEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                return updateProduct(uri, contentValues, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);
        }
    }

    private int updateProduct(Uri uri, ContentValues values, String selection, String[] selectionArgs) {

        // If there are no values to update, then don't try to update the database
        if (values.size() == 0) {
            return 0;
        }

        validateUpdateProduct(values);

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int updatedRows = db.update(ProductContract.ProductEntry.TABLE_NAME, values, selection, selectionArgs);

        Log.i(LOG_TAG, "Updated " + updatedRows + " rows in products table.");

        if (updatedRows > 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return updatedRows;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        SQLiteDatabase database = dbHelper.getWritableDatabase();

        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PRODUCTS:
                return deleteProduct(selection, selectionArgs, database, uri);
            case PRODUCT_ID:
                selection = ProductContract.ProductEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                return deleteProduct(selection, selectionArgs, database, uri);
            default:
                throw new IllegalArgumentException("Deletion is not supported for " + uri);
        }
    }

    private int deleteProduct(String selection, String[] selectionArgs, SQLiteDatabase database, Uri uri) {
        int deletedRows = database.delete(ProductContract.ProductEntry.TABLE_NAME, selection, selectionArgs);
        if (deletedRows > 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return deletedRows;
    }

    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PRODUCTS:
                return ProductContract.ProductEntry.CONTENT_LIST_TYPE;
            case PRODUCT_ID:
                return ProductContract.ProductEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalStateException("Unknown URI " + uri + " with match " + match);
        }
    }
}