package com.example.kidzcolor.utils;

import android.content.res.Resources;
import android.graphics.Color;

public class Utils {

    public static float getFloatFromDimensionString(String value) {
        if (value.contains("dip"))
            return Float.parseFloat(value.substring(0, value.length() - 3));
        else
            return Float.parseFloat(value.substring(0, value.length() - 2));
    }

    public static int getColorFromInt(int randomInteger) {
        return getColorFromString(String.format("#%06x", randomInteger));
    }

    public static int getColorFromString(String value) {
        int color = Color.TRANSPARENT;

        if (value.length() == 4) {
            color = Color.parseColor("#" + value.charAt(1) + value.charAt(1) + value.charAt(2) + value.charAt(2) + value.charAt(3) + value.charAt(3));
        } else if (value.length() == 7 || value.length() == 9) {
            color = Color.parseColor(value);
        } else if (value.length() == 2) {
            color = Color.parseColor("#" + value.charAt(1) + value.charAt(1) + value.charAt(1) + value.charAt(1) + value.charAt(1) + value.charAt(1) + value.charAt(1) + value.charAt(1));
        }

        return color;
    }

    public static int dpToPx(int dp) {
        return (int) (dp * Resources.getSystem().getDisplayMetrics().density);
    }

    public static int pxToDP(int px){
        return (int) (px / Resources.getSystem().getDisplayMetrics().density);
    }
}
