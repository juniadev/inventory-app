package com.wordpress.juniadev.inventoryapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.wordpress.juniadev.inventoryapp.data.ProductContract;
import com.wordpress.juniadev.inventoryapp.utils.ValidatorUtils;

public class ProductCursorAdapter extends CursorAdapter {

    public ProductCursorAdapter(Context context, Cursor c) {
        super(context, c, 0 /* flags */);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    @Override
    public void bindView(View view, final Context context, Cursor cursor) {
        final int id = cursor.getInt(cursor.getColumnIndex(ProductContract.ProductEntry._ID));
        String name = cursor.getString(cursor.getColumnIndex(ProductContract.ProductEntry.COLUMN_NAME));
        final String quantity = cursor.getString(cursor.getColumnIndex(ProductContract.ProductEntry.COLUMN_QUANTITY_AVAILABLE));
        String price = cursor.getString(cursor.getColumnIndex(ProductContract.ProductEntry.COLUMN_PRICE));

        TextView nameView = (TextView) view.findViewById(R.id.product_name);
        nameView.setText(name);

        TextView quantityView = (TextView) view.findViewById(R.id.product_quantity_available);
        quantityView.setText(context.getString(R.string.list_quantity_available, quantity));

        TextView priceView = (TextView) view.findViewById(R.id.product_price);
        priceView.setText(context.getString(R.string.list_price, price));

        Button button = (Button) view.findViewById(R.id.list_track_sale_button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ValidatorUtils.isSaleValid(quantity, context)) {
                    int newQuantity = Integer.parseInt(quantity) - 1;
                    ContentValues values = new ContentValues();
                    values.put(ProductContract.ProductEntry.COLUMN_QUANTITY_AVAILABLE, newQuantity);
                    updateProduct(values, context, id);
                }
            }
        });
    }

    private void updateProduct(ContentValues values, Context context, int id) {

        Uri currentProductUri = Uri.withAppendedPath(ProductContract.ProductEntry.CONTENT_URI, id + "");

        int updated = context.getContentResolver().update(currentProductUri, values, null, null);
        if (updated > 0) {
            Toast.makeText(context, context.getString(R.string.update_product_successful), Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(context, context.getString(R.string.update_product_failed), Toast.LENGTH_SHORT).show();
        }
    }
}