package com.wordpress.juniadev.inventoryapp;

import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.wordpress.juniadev.inventoryapp.data.ProductContract;
import com.wordpress.juniadev.inventoryapp.utils.ImageUtils;

import java.io.InputStream;

/**
 * Activity to add a new product to the inventory.
 */
public class AddProductActivity extends AppCompatActivity {

    private static final String LOG_TAG = AddProductActivity.class.getSimpleName();

    private static final int SELECT_IMAGE = 1;
    private Uri imageUri;

    private EditText nameEditText;
    private EditText priceEditText;
    private EditText quantityEditText;
    private EditText supplierEditText;
    private ImageView imageView;

    private boolean productHasChanged = false;

    /**
     * Listener to identify that the user has changed a view's value.
     */
    private View.OnTouchListener touchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            productHasChanged = true;
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add);

        setLoadImageButtonListener();

        setViews();

        setViewsTouchListener();
    }

    private void setViewsTouchListener() {
        nameEditText.setOnTouchListener(touchListener);
        priceEditText.setOnTouchListener(touchListener);
        quantityEditText.setOnTouchListener(touchListener);
        supplierEditText.setOnTouchListener(touchListener);
    }

    private void setViews() {
        nameEditText = (EditText) findViewById(R.id.edit_product_name);
        priceEditText = (EditText) findViewById(R.id.edit_product_price);
        quantityEditText = (EditText) findViewById(R.id.edit_product_quantity);
        supplierEditText = (EditText) findViewById(R.id.edit_product_supplier);
        imageView = (ImageView) findViewById(R.id.product_image_view);
    }

    /**
     * Create intent so the user can select an image from gallery.
     */
    private void setLoadImageButtonListener() {
        Button loadImageButton = (Button) findViewById(R.id.load_image_button);
        loadImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent selectImageIntent = new Intent(Intent.ACTION_PICK);
                selectImageIntent.setType("image/*");
                startActivityForResult(selectImageIntent, SELECT_IMAGE);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SELECT_IMAGE && resultCode == RESULT_OK && data != null) {
            displayProductImage(data.getData());
        }
    }

    /**
     * Retrieve the user's image, store it in a variable and display it in the ImageView.
     */
    public void displayProductImage(Uri imageUri) {
        if (imageUri == null) {
            Log.e(LOG_TAG, "Image URI is null.");
            return;
        }

        try {
            this.imageUri = imageUri;
            final InputStream imageStream = getContentResolver().openInputStream(imageUri);
            final Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
            imageView.setImageBitmap(selectedImage);
        } catch(Exception e) {
            Log.e(LOG_TAG, "Error retrieving product image.", e);
        }
    }

    @Override
    public void onBackPressed() {
        if (!productHasChanged) {
            super.onBackPressed();
            return;
        }

        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        finish();
                    }
                };

        showUnsavedChangesDialog(discardButtonClickListener);
    }

    /**
     * Display a dialog to check if the user wants to discard or keep the changes in form.
     * @param discardButtonClickListener
     */
    private void showUnsavedChangesDialog(DialogInterface.OnClickListener discardButtonClickListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        builder.create().show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_add, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_save:
                saveProduct();
                finish();
                return true;
            case android.R.id.home:
                return navigateToHome();
        }
        return super.onOptionsItemSelected(item);
    }

    private boolean navigateToHome() {
        if (!productHasChanged) {
            NavUtils.navigateUpFromSameTask(AddProductActivity.this);
            return true;
        }

        DialogInterface.OnClickListener discardButtonClickListener = getDiscardButtonClickListener();
        showUnsavedChangesDialog(discardButtonClickListener);
        return true;
    }

    @NonNull
    private DialogInterface.OnClickListener getDiscardButtonClickListener() {
        return new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                NavUtils.navigateUpFromSameTask(AddProductActivity.this);
            }
        };
    }

    private void saveProduct() {
        String name = nameEditText.getText().toString().trim();
        String price = priceEditText.getText().toString().trim();
        String quantity = quantityEditText.getText().toString().trim();
        String supplier = supplierEditText.getText().toString().trim();

        // Need to resize so image will fit in SQLite DB (default size for CursorWindow is 2MB).
        byte[] image = ImageUtils.getResizedImageBytes(imageUri, this.getContentResolver());

        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(price)) {
            return;
        }

        ContentValues values = new ContentValues();
        values.put(ProductContract.ProductEntry.COLUMN_NAME, name);
        values.put(ProductContract.ProductEntry.COLUMN_PRICE, Double.parseDouble(price));
        values.put(ProductContract.ProductEntry.COLUMN_QUANTITY_AVAILABLE, TextUtils.isEmpty(quantity) ? 0 : Integer.parseInt(quantity));
        values.put(ProductContract.ProductEntry.COLUMN_SUPPLIER_EMAIL, supplier);
        if (image != null) {
            values.put(ProductContract.ProductEntry.COLUMN_IMAGE, image);
        }

        Uri newUri = getContentResolver().insert(ProductContract.ProductEntry.CONTENT_URI, values);
        if (newUri == null) {
            Toast.makeText(this, getString(R.string.insert_product_failed), Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, getString(R.string.insert_product_successful), Toast.LENGTH_SHORT).show();
        }
    }
}
