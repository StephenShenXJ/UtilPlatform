package com.shen.stephen.utilplatform.widget.seekbar;

/**
 * Created by JiangTx on 2015/10/28.
 */

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.util.TypedValue;

import com.shen.stephen.utilplatform.widget.seekbar.CustomizedSeekBar.Dot;

import java.util.List;

public class CustomizedBgDrawable extends GradientDrawable {
    private final int SELECTED_STROCKE_WIDTH = 4;
    private final int UNSELECTED_STROCKE_WIDTH = 3;
    private final int BAR_DOT_RADIUS = 4;
    private final int TEXT_MARGIN = 5;
    private final int DEFAULT_BAR_SELECTED_COLOR = Color.BLACK;
    private final int DEFAULT_BAR_UNSELECTED_COLOR = Color.BLACK;
    private final int DEFAULT_TEXT_COLOR = Color.BLACK;

    private Context mContext;
    private Drawable myBase;

    private Paint textUnselected;
    private Paint textSelected;
    private Paint unselectLinePaint;
    private Paint selectLinePaint;
    private Paint circleLinePaint;

    private List<Dot> mDots;
    private float mThumbRadius;
    private float mDotRadius;
    private int mTextSize;
    private float mTextMargin;
    private float mTextHeight;
    private boolean mIsMultiline;
    private int mXPosition;
    private int mTextAlign;

    private int mBarSelectedColor[];
    private int mBarUnselectedColor;
    private int mTextColor;

    public CustomizedBgDrawable(Drawable base, Context context, float thumbRadius, List<Dot> dots, int textSize, boolean isMultiline) {
        mContext = context;
        myBase = base;
        mDots = dots;
        mThumbRadius = thumbRadius;
        mTextSize = textSize;
        mIsMultiline = isMultiline;
        mXPosition = 0;
        if (dots.size() == 0) {

        } else {
            mXPosition = mDots.get(0).mX;
            for (Dot d : mDots) {
                if (d.isSelected) {
                    mXPosition = d.mX;
                }
            }

        }

        mBarSelectedColor = new int[]{DEFAULT_BAR_SELECTED_COLOR, DEFAULT_BAR_SELECTED_COLOR};
        mBarUnselectedColor = DEFAULT_BAR_UNSELECTED_COLOR;
        mTextColor = DEFAULT_TEXT_COLOR;

        initPaints();

        mDotRadius = toPix(BAR_DOT_RADIUS);
        mTextMargin = toPix(TEXT_MARGIN);
    }

    private void initPaints() {
        Rect textBounds = new Rect();
        textUnselected = new Paint(Paint.ANTI_ALIAS_FLAG);
        textUnselected.setColor(mTextColor);
        textUnselected.setAlpha(255);
        textUnselected.setTextSize(mTextSize);
        textUnselected.getTextBounds("M", 0, 1, textBounds);
        mTextHeight = textBounds.height();

        textSelected = new Paint(Paint.ANTI_ALIAS_FLAG);
        textSelected.setTypeface(Typeface.DEFAULT_BOLD);
        textSelected.setColor(mTextColor);
        textSelected.setAlpha(255);
        textSelected.setTextSize(mTextSize);

        unselectLinePaint = new Paint();
        unselectLinePaint.setColor(mBarUnselectedColor);
        unselectLinePaint.setStrokeWidth(toPix(UNSELECTED_STROCKE_WIDTH));

        selectLinePaint = new Paint();
        //selectLinePaint.setColor(mBarSelectedColor);
        selectLinePaint.setStrokeWidth(toPix(SELECTED_STROCKE_WIDTH));

        circleLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    }

    public void setTextAlign(int textAlign) {
        this.mTextAlign = textAlign;
    }

    public void setBarSelectedColor(int[] color) {
        mBarSelectedColor = color;

        invalidateSelf();
    }

    public void setBarUnselectedColor(int color) {
        mBarUnselectedColor = color;
        unselectLinePaint.setColor(mBarUnselectedColor);
        invalidateSelf();
    }

    public void setTextColor(int color) {
        mTextColor = color;
        textSelected.setColor(mTextColor);
        textUnselected.setColor(mTextColor);
        invalidateSelf();
    }

    public void setXPosition(int x) {
        mXPosition = x;
        invalidateSelf();
    }

    private float toPix(int size) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, size, mContext.getResources().getDisplayMetrics());
    }

    @Override
    protected final void onBoundsChange(Rect bounds) {
        myBase.setBounds(bounds);
    }

    @Override
    protected final boolean onStateChange(int[] state) {
        invalidateSelf();
        return false;
    }

    @Override
    public final boolean isStateful() {
        return true;
    }

    @Override
    public final void draw(Canvas canvas) {
        if (selectLinePaint.getShader() == null) {
            selectLinePaint.setShader(new LinearGradient(0, 0, getBounds().width(), 0, mBarSelectedColor[0], mBarSelectedColor[1], Shader.TileMode.MIRROR));
            circleLinePaint.setShader(new LinearGradient(0, 0, getBounds().width(), 0, mBarSelectedColor[0], mBarSelectedColor[1], Shader.TileMode.MIRROR));
        }

        int height = this.getIntrinsicHeight() / 2;
        if (mDots.size() == 0) {
            canvas.drawLine(0, height, getBounds().right, height, unselectLinePaint);
            return;
        }

        for (Dot dot : mDots) {
            if (dot.isSelected) {
                mXPosition = dot.mX;
                break;
            }
        }


        canvas.drawLine(mDots.get(0).mX, height, mXPosition, height, selectLinePaint);
        canvas.drawLine(mXPosition, height, mDots.get(mDots.size() - 1).mX, height, unselectLinePaint);

        for (Dot dot : mDots) {
            drawText(canvas, dot, dot.mX, height);
            if (mXPosition < dot.mX) {
                canvas.drawCircle(dot.mX, height, mDotRadius, unselectLinePaint);
            } else {
                canvas.drawCircle(dot.mX, height, mDotRadius, circleLinePaint);
            }
        }
    }

    /**
     * @param canvas canvas.
     * @param dot    current dot.
     * @param x      x cor.
     * @param y      y cor.
     */
    private void drawText(Canvas canvas, Dot dot, float x, float y) {
        final Rect textBounds = new Rect();
        textSelected.getTextBounds(dot.text, 0, dot.text.length(), textBounds);
        float xres = x - (textBounds.width() / 2);

        float yres;
        if (mIsMultiline) {
            if ((dot.id % 2) == 0) {
                yres = y - mTextMargin - mThumbRadius;
            } else {
                yres = y + mTextMargin + mThumbRadius;
            }
        } else {
            if (mTextAlign == CustomizedSeekBar.TEXT_ALIGN_BOTTOM) {
                yres = y + mTextMargin + mThumbRadius - mTextHeight / 2;
            } else {
                yres = y - mThumbRadius - mTextMargin + mTextHeight / 2;
            }
        }

        yres = yres < 0 ? 0 : yres;
        if (dot.isSelected) {
            canvas.drawText(dot.text, xres, yres, textSelected);
        } else {
            canvas.drawText(dot.text, xres, yres, textUnselected);
        }
    }

    @Override
    public final int getIntrinsicHeight() {
        if (mIsMultiline) {
            return (int) (mThumbRadius * 2 + mTextMargin * 2 + mTextHeight * 2);
        } else {
            return (int) (mThumbRadius * 2 + mTextMargin * 2 + mTextHeight);
        }
    }

    @Override
    public final int getOpacity() {
        return PixelFormat.TRANSLUCENT;
    }

    @Override
    public void setAlpha(int alpha) {
    }

    @Override
    public void setColorFilter(ColorFilter cf) {
    }
}
