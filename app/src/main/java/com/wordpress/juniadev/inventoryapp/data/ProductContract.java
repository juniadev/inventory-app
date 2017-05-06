package com.wordpress.juniadev.inventoryapp.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

public final class ProductContract {

    public static final String CONTENT_AUTHORITY = "com.wordpress.juniadev.inventoryapp";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    public static final String PATH_PRODUCTS = "products";

    // To prevent someone from accidentally instantiating the contract class, give it an empty constructor.
    private ProductContract() {}

    public static final class ProductEntry implements BaseColumns {

        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_PRODUCTS);

        /** The MIME type of the {@link #CONTENT_URI} for a list of products. */
        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_PRODUCTS;

        /** The MIME type of the {@link #CONTENT_URI} for a single product. */
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_PRODUCTS;

        public static final String TABLE_NAME = "products";
        public static final String _ID = BaseColumns._ID;
        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_PRICE = "price";
        public static final String COLUMN_QUANTITY_AVAILABLE = "quantityAvailable";
        public static final String COLUMN_SUPPLIER_EMAIL = "supplierEmail";
        public static final String COLUMN_IMAGE = "image";
    }
}
