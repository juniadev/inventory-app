package com.wordpress.juniadev.inventoryapp;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.wordpress.juniadev.inventoryapp.data.ProductContract;

public class ProductCursorAdapter extends CursorAdapter {

    public ProductCursorAdapter(Context context, Cursor c) {
        super(context, c, 0 /* flags */);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        String name = cursor.getString(cursor.getColumnIndex(ProductContract.ProductEntry.COLUMN_NAME));
        String quantity = cursor.getString(cursor.getColumnIndex(ProductContract.ProductEntry.COLUMN_QUANTITY_AVAILABLE));
        String price = cursor.getString(cursor.getColumnIndex(ProductContract.ProductEntry.COLUMN_PRICE));

        TextView nameView = (TextView) view.findViewById(R.id.product_name);
        nameView.setText(name);

        TextView quantityView = (TextView) view.findViewById(R.id.product_quantity_available);
        quantityView.setText(context.getString(R.string.list_quantity_available, quantity));

        TextView priceView = (TextView) view.findViewById(R.id.product_price);
        priceView.setText(context.getString(R.string.list_price, price));
    }
}