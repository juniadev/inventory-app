package com.wordpress.juniadev.inventoryapp.utils;

import android.content.ContentResolver;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Utilities methods to handle the product image.
 */
public class ImageUtils {

    private static final String LOG_TAG = ImageUtils.class.getSimpleName();
    private static final int BUFFER_SIZE = 1024;
    private static final double DEFAULT_SCALE = 0.1;
    private static final int DEFAULT_QUALITY = 100;

    public static byte[] getResizedImageBytes(Uri imageUri, ContentResolver contentResolver) {
        if (imageUri == null) {
            return null;
        }

        byte[] originalBytes = getImageBytes(imageUri, contentResolver);
        Bitmap originalBitmap = getBitmap(originalBytes);
        Bitmap resizedBitmap = resizeImage(originalBitmap);

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        resizedBitmap.compress(Bitmap.CompressFormat.PNG, DEFAULT_QUALITY, stream);
        return stream.toByteArray();
    }

    private static byte[] getImageBytes(Uri imageUri, ContentResolver contentResolver) {
        byte[] bytesResult = null;
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
        try {
            InputStream iStream = contentResolver.openInputStream(imageUri);
            byte[] buffer = new byte[BUFFER_SIZE];
            int len;
            while ((len = iStream.read(buffer)) != -1) {
                byteBuffer.write(buffer, 0, len);
            }
            bytesResult = byteBuffer.toByteArray();
        } catch(Exception e) {
            Log.e(LOG_TAG, "Error getting image bytes.", e);
        } finally {
            try {
                byteBuffer.close();
            } catch (IOException e) {
                Log.e(LOG_TAG, "Error closing image buffer.", e);
            }
        }
        return bytesResult;
    }

    private static Bitmap resizeImage(Bitmap original) {
        int originalWidth = original.getWidth();
        int originalHeight = original.getHeight();

        return Bitmap.createScaledBitmap(original, (int) (originalWidth * DEFAULT_SCALE), (int) (originalHeight * DEFAULT_SCALE), true);
    }

    public static Bitmap getBitmap(byte[] imageBytes) {
        return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
    }
}
