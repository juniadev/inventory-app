package com.wordpress.juniadev.inventoryapp;

import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.wordpress.juniadev.inventoryapp.data.ProductContract;
import com.wordpress.juniadev.inventoryapp.utils.ImageUtils;
import com.wordpress.juniadev.inventoryapp.utils.ValidatorUtils;

public class ProductDetailActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String LOG_TAG = ProductDetailActivity.class.getSimpleName();

    private static final int EXISTING_PRODUCT_LOADER = 0;
    private static final int DEFAULT_SHIPMENT_QUANTITY = 5;

    private Uri currentProductUri;

    private TextView nameTextView;
    private TextView priceTextView;
    private TextView quantityTextView;
    private TextView supplierTextView;
    private ImageView imageView;

    private String quantity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        currentProductUri = getIntent().getData();
        if (currentProductUri == null) {
            throw new IllegalArgumentException("Uri is null, can't open Product Detail!");
        }

        Log.i(LOG_TAG, "Received Uri: " + currentProductUri.toString());
        getLoaderManager().initLoader(EXISTING_PRODUCT_LOADER, null, this);

        setViews();

        setButtonOnClickListeners();
    }

    private void setButtonOnClickListeners() {
        setTrackSaleButtonListener();
        setReceiveShipmentButtonListener();
        setOrderFromSupplierButtonListener();
        setDeleteProductButtonListener();
    }

    private void setDeleteProductButtonListener() {
        Button button = (Button) findViewById(R.id.delete_product_button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDeleteConfirmationDialog();
            }
        });
    }

    private void showDeleteConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                deleteProduct();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void deleteProduct() {
        if (currentProductUri != null) {
            int deletedRows = getContentResolver().delete(currentProductUri, null, null);
            if (deletedRows > 0) {
                Toast.makeText(this, R.string.delete_product_successful, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, R.string.delete_product_failed, Toast.LENGTH_SHORT).show();
            }
            finish();
        }
    }

    private void setOrderFromSupplierButtonListener() {
        Button button = (Button) findViewById(R.id.order_from_supplier_button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                supplierTextView = (TextView) findViewById(R.id.display_product_supplier);
                String supplierEmail = supplierTextView.getText().toString();
                if (TextUtils.isEmpty(supplierEmail)) {
                    return;
                }

                Intent email = new Intent(Intent.ACTION_SENDTO);
                email.setData(Uri.parse("mailto:")); // only email apps should handle this
                email.putExtra(Intent.EXTRA_EMAIL, new String[]{supplierEmail});
                email.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.supplier_email_subject, nameTextView.getText().toString()));
                email.putExtra(Intent.EXTRA_TEXT, getString(R.string.supplier_email_message));
                if (email.resolveActivity(getPackageManager()) != null) {
                    startActivity(email);
                }
            }
        });
    }

    private void setTrackSaleButtonListener() {
        Button button = (Button) findViewById(R.id.track_sale_button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ValidatorUtils.isSaleValid(quantity, getApplicationContext())) {
                    int newQuantity = Integer.parseInt(quantity) - 1;
                    ContentValues values = new ContentValues();
                    values.put(ProductContract.ProductEntry.COLUMN_QUANTITY_AVAILABLE, newQuantity);
                    updateProduct(values);
                }
            }
        });
    }

    private void setReceiveShipmentButtonListener() {
        Button button = (Button) findViewById(R.id.receive_shipment_button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int newQuantity = Integer.parseInt(quantity) + DEFAULT_SHIPMENT_QUANTITY;
                ContentValues values = new ContentValues();
                values.put(ProductContract.ProductEntry.COLUMN_QUANTITY_AVAILABLE, newQuantity);
                updateProduct(values);
            }
        });
    }

    private void setViews() {
        nameTextView = (TextView) findViewById(R.id.display_product_name);
        priceTextView = (TextView) findViewById(R.id.display_product_price);
        quantityTextView = (TextView) findViewById(R.id.display_product_quantity);
        supplierTextView = (TextView) findViewById(R.id.display_product_supplier);
        imageView = (ImageView) findViewById(R.id.display_product_image);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] projection = {
                ProductContract.ProductEntry._ID,
                ProductContract.ProductEntry.COLUMN_NAME,
                ProductContract.ProductEntry.COLUMN_PRICE,
                ProductContract.ProductEntry.COLUMN_QUANTITY_AVAILABLE,
                ProductContract.ProductEntry.COLUMN_SUPPLIER_EMAIL,
                ProductContract.ProductEntry.COLUMN_IMAGE
        };

        return new CursorLoader(this, currentProductUri, projection, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data != null && data.getCount() == 1 && data.moveToFirst()) {
            String name = data.getString(data.getColumnIndex(ProductContract.ProductEntry.COLUMN_NAME));
            String price = data.getString(data.getColumnIndex(ProductContract.ProductEntry.COLUMN_PRICE));
            quantity = data.getString(data.getColumnIndex(ProductContract.ProductEntry.COLUMN_QUANTITY_AVAILABLE));
            String supplier = data.getString(data.getColumnIndex(ProductContract.ProductEntry.COLUMN_SUPPLIER_EMAIL));

            nameTextView.setText(name);
            priceTextView.setText(getString(R.string.currency, price));
            quantityTextView.setText(getString(R.string.available, quantity));
            supplierTextView.setText(supplier);

            byte[] imageBytes = data.getBlob(data.getColumnIndex(ProductContract.ProductEntry.COLUMN_IMAGE));
            if (imageBytes != null) {
                imageView.setImageBitmap(ImageUtils.getBitmap(imageBytes));
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        nameTextView.setText("");
        priceTextView.setText("");
        quantityTextView.setText("");
        supplierTextView.setText("");
        imageView.setImageBitmap(null);
    }

    private void updateProduct(ContentValues values) {
        int updated = getContentResolver().update(currentProductUri, values, null, null);
        if (updated > 0) {
            Toast.makeText(this, getString(R.string.update_product_successful), Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, getString(R.string.update_product_failed), Toast.LENGTH_SHORT).show();
        }
    }
}
