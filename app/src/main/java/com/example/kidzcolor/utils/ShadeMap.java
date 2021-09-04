package com.example.kidzcolor.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;

public class ShadeMap {

    public static final ShadeMap instance = new ShadeMap();

    private final BitmapShader bitmapShader;

    private ShadeMap () {
        final int size = 10;
        final int colorOdd = 0xFFC2C2C2;
        final int colorEven = 0xFFF3F3F3;
        final Bitmap shadeMapBitmap;

        Paint bitmapPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        bitmapPaint.setStyle(Paint.Style.FILL);

        shadeMapBitmap = Bitmap.createBitmap(size * 2, size * 2, Bitmap.Config.ARGB_8888);
        Canvas aCanvas = new Canvas(shadeMapBitmap);

        Rect rect = new Rect(0, 0, size, size);
        bitmapPaint.setColor(colorOdd);
        aCanvas.drawRect(rect, bitmapPaint);

        rect.offset(size, size);
        aCanvas.drawRect(rect, bitmapPaint);

        bitmapPaint.setColor(colorEven);
        rect.offset(-size, 0);
        aCanvas.drawRect(rect, bitmapPaint);

        rect.offset(size, -size);
        aCanvas.drawRect(rect, bitmapPaint);

        bitmapShader =  new BitmapShader(shadeMapBitmap,
                BitmapShader.TileMode.REPEAT,
                BitmapShader.TileMode.REPEAT);
    }

    public BitmapShader getBitmapShader() {
        return bitmapShader;
    }

}
