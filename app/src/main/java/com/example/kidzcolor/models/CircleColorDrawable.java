package com.example.kidzcolor.models;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.kidzcolor.R;

public class CircleColorDrawable extends Drawable {

    private Context context;

    private Bitmap patternMap;

    private Paint progressBarPaint;
    private Paint backgroundPaint;
    private Paint innerCirclePaint;
    private Paint textPaint;

    private float mRadius;
    private float mouthInset;
    private RectF mArcBounds = new RectF();

    float drawUpTo = 0;

    public CircleColorDrawable(Context context) {
        this.context = context;
        initPaints(context, null);
        progressInvisible(true);
        backgroundProgressInvisible(true);
    }

    public Bitmap getPatternMap() {
        return patternMap;
    }

    public void drawPatternMap() {
        patternMap = Bitmap.createBitmap(getIntrinsicWidth(), getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas patternCanvas = new Canvas(patternMap);
        patternCanvas.drawCircle(mArcBounds.centerX(), mArcBounds.centerY(), mouthInset * 2.3f, innerCirclePaint);
    }

    private int progressColor;
    private int backgroundColor;
    private int innerCircleColor;
    private float strokeWidthDimension;
    private float backgroundWidth;
    private boolean roundedCorners;
    private int maxValue;

    private int progressTextColor = Color.BLACK;
    private float textSize = 18;
    private String text = "";
    private String suffix = "";
    private String prefix = "";

    int defStyleAttr;

    public void setTextSize(float textSize){
        this.textSize = textSize;
        textPaint.setTextSize(textSize);
    }

    public void setRoundedCorners(boolean isTrue){
        if(isTrue)
            roundedCorners = true;
        else
            roundedCorners = false;
        invalidateSelf();
    }

    public void setInnerCircleColor(int color){
        innerCirclePaint.setColor(color);
        invalidateSelf();
    }

    public CircleColorDrawable(Context context, AttributeSet attrs, int defStyleAttr, int radius) {
        this.context = context;
        this.defStyleAttr = defStyleAttr;
        mRadius = radius;
        initPaints(context, attrs);
    }

    public CircleColorDrawable(Context context, AttributeSet attrs, int radius) {
        this(context, attrs, 0, radius);
        //initPaints(context, attrs);
    }

    private void initPaints(Context context, AttributeSet attrs) {

        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.CircleProgressBar, defStyleAttr, 0);

        progressColor = ta.getColor(R.styleable.CircleProgressBar_progressColor, Color.BLUE);
        backgroundColor = ta.getColor(R.styleable.CircleProgressBar_backgroundColor, Color.GRAY);
        innerCircleColor = ta.getColor(R.styleable.CircleProgressBar_innerCircle, Color.GRAY);
        strokeWidthDimension = ta.getFloat(R.styleable.CircleProgressBar_strokeWidthDimension, 2);
        backgroundWidth = ta.getFloat(R.styleable.CircleProgressBar_backgroundWidth, 4);
        roundedCorners = ta.getBoolean(R.styleable.CircleProgressBar_roundedCorners, false);
        maxValue = ta.getInt(R.styleable.CircleProgressBar_maxValue, 100);
        progressTextColor = ta.getColor(R.styleable.CircleProgressBar_progressTextColor, Color.BLACK);
        textSize = ta.getDimension(R.styleable.CircleProgressBar_textSize, 18);
        suffix = ta.getString(R.styleable.CircleProgressBar_suffix);
        prefix = ta.getString(R.styleable.CircleProgressBar_prefix);
        text = ta.getString(R.styleable.CircleProgressBar_progressText);

        progressBarPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        progressBarPaint.setStyle(Paint.Style.FILL);
        progressBarPaint.setColor(progressColor);
        progressBarPaint.setStyle(Paint.Style.STROKE);
        progressBarPaint.setStrokeWidth(strokeWidthDimension * context.getResources().getDisplayMetrics().density);
        if(roundedCorners){
            progressBarPaint.setStrokeCap(Paint.Cap.ROUND);
        }else{
            progressBarPaint.setStrokeCap(Paint.Cap.BUTT);
        }
        String pc = String.format("#%06X", (0xFFFFFF & progressColor));
        progressBarPaint.setColor(Color.parseColor(pc));

        backgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        backgroundPaint.setStyle(Paint.Style.FILL);
        backgroundPaint.setColor(backgroundColor);
        backgroundPaint.setStyle(Paint.Style.STROKE);
        backgroundPaint.setStrokeWidth(backgroundWidth * context.getResources().getDisplayMetrics().density);
        backgroundPaint.setStrokeCap(Paint.Cap.SQUARE);
        String bc = String.format("#%06X", (0xFFFFFF & backgroundColor));
        backgroundPaint.setColor(Color.parseColor(bc));

        innerCirclePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        innerCirclePaint.setStyle(Paint.Style.FILL);
        innerCirclePaint.setColor(innerCircleColor);

        ta.recycle();

        textPaint = new TextPaint();
        textPaint.setColor(progressTextColor);
        String c = String.format("#%06X", (0xFFFFFF & progressTextColor));
        textPaint.setColor(Color.parseColor(c));
        textPaint.setTextSize(textSize);
        textPaint.setAntiAlias(true);

        //paint.setAntiAlias(true);
    }

    @Override
    protected void onBoundsChange(Rect bounds){

        if (bounds.width() != 0 && bounds.height() != 0)
            mRadius = Math.min(bounds.width(), bounds.height()) / 2f;
        int a = 0;

    }

    public void progressInvisible(boolean isTrue){
        if(isTrue)
            progressBarPaint.setColor(Color.TRANSPARENT);
        else
            progressBarPaint.setColor(progressColor);

        invalidateSelf();
    }

    public void backgroundProgressInvisible(boolean isTrue){
        if(isTrue)
            backgroundPaint.setColor(Color.TRANSPARENT);
        else
            backgroundPaint.setColor(progressColor);

        invalidateSelf();
    }


    public void setProgress(float f){
        drawUpTo = f;
        invalidateSelf();
    }

    public float getProgress(){
        return drawUpTo;
    }

    public float getProgressPercentage(){
        return drawUpTo /getMaxValue() * 100;
    }

    public void setProgressColor(int color){
        progressColor = color;
        progressBarPaint.setColor(color);
        invalidateSelf();
    }

    public void setProgressColor(String color){
        progressBarPaint.setColor(Color.parseColor(color));
        invalidateSelf();
    }

    public void setBackgroundColor(int color){
        backgroundColor = color;
        backgroundPaint.setColor(color);
        invalidateSelf();
    }

    public void setBackgroundColor(String color){
        backgroundPaint.setColor(Color.parseColor(color));
        invalidateSelf();
    }

    public int getMaxValue(){
        return maxValue;
    }

    public void setMaxValue(int max){
        maxValue = max;
        invalidateSelf();
    }

    public void setStrokeWidthDimension(float width){
        strokeWidthDimension = width;
        progressBarPaint.setStrokeWidth(strokeWidthDimension * context.getResources().getDisplayMetrics().density);
        invalidateSelf();
    }

    public float getStrokeWidthDimension(){
        return strokeWidthDimension;
    }

    public void setBackgroundWidth(float width){
        backgroundWidth = width;
        backgroundPaint.setStrokeWidth(backgroundWidth * context.getResources().getDisplayMetrics().density);
        invalidateSelf();
    }

    public float getBackgroundWidth(){
        return backgroundWidth;
    }

    public void setText(String progressText){
        text = progressText;
        invalidateSelf();
    }

    public String getText(){
        return text;
    }

    public void setTextColor(int color){
        progressTextColor = color;
        textPaint.setColor(color);
        invalidateSelf();
    }

    public void setTextColor(String color){
        textPaint.setColor(Color.parseColor(color));
        invalidateSelf();
    }

    public int getTextColor(){
        return progressTextColor;
    }

    public void setSuffix(String suffix){
        this.suffix = suffix;
        invalidateSelf();
    }

    public String getSuffix(){
        return suffix;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
        invalidateSelf();
    }

    @Override
    public void draw(@NonNull Canvas canvas) {


        mouthInset = mRadius / 5;
        mArcBounds.set(mouthInset, mouthInset, mRadius * 2 - mouthInset, mRadius * 2 - mouthInset);

        canvas.drawCircle(mArcBounds.centerX(), mArcBounds.centerY(), mouthInset * 3.6f, innerCirclePaint);
        canvas.drawArc(mArcBounds, 0f, 360f, false, backgroundPaint);
        canvas.drawArc(mArcBounds, 270f, drawUpTo / getMaxValue() * 360, false, progressBarPaint);


        if(TextUtils.isEmpty(suffix)){
            suffix = "";
        }

        if(TextUtils.isEmpty(prefix)){
            prefix = "";
        }

        String drawnText = prefix + text + suffix;

        if (!TextUtils.isEmpty(text)) {
            float textHeight = textPaint.descent() + textPaint.ascent();
            canvas.drawText(drawnText, mArcBounds.centerX(), mArcBounds.centerY(), textPaint);
        }
    }

    @Override
    public void setAlpha(int alpha) {
        //float alphaFloat = (((float) alpha) / 255.0f);
        //setAlpha not used
    }

    @Override
    public void setColorFilter(@Nullable ColorFilter colorFilter) {

    }

    @Override
    public int getOpacity() {
        return PixelFormat.TRANSLUCENT;
    }
}
