package com.wordpress.juniadev.inventoryapp.utils;

import android.content.Context;
import android.text.TextUtils;
import android.widget.Toast;

import com.wordpress.juniadev.inventoryapp.R;

public class ValidatorUtils {

    public static boolean isSaleValid(String quantity, Context context) {
        if (TextUtils.isEmpty(quantity) || quantity.equals("0")) {
            Toast.makeText(context, context.getString(R.string.product_not_available), Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }
}
