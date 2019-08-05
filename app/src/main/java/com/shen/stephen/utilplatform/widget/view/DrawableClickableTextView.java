package com.shen.stephen.utilplatform.widget.view;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

/**
 * Created by ChengCn on 4/12/2016.
 */
public class DrawableClickableTextView extends TextView implements View.OnTouchListener {

    private OnDrawableClickListener mDrawableClickListener;
    private Drawable[] mCompoungDrawbles;

    /**
     *  Interface definition for a callback to be invoked when user click the drawable of the TextView.
     */
    public interface OnDrawableClickListener {

        /**
         * Called when the drawable of the TextView has been clicked.
         * @param textView
         */
        void onClickDrawable(DrawableClickableTextView textView);
    }

    public DrawableClickableTextView(Context context) {
        this(context, null);
    }

    public DrawableClickableTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        super.setOnTouchListener(this);
    }

    public DrawableClickableTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        super.setOnTouchListener(this);
    }

    public void setOnDrawableClickListener(OnDrawableClickListener listener) {
        mDrawableClickListener = listener;
    }

    @Override
    public void setOnTouchListener(OnTouchListener l) {
        this.mOnTouchListener = l;
    }

    private OnTouchListener mOnTouchListener;

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        final float xPos = event.getX();
        if (mCompoungDrawbles[0] != null) {
            boolean tappedX = xPos < (getPaddingLeft() + mCompoungDrawbles[0].getIntrinsicWidth());
            if (tappedX) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    clickDrawable();
                }
                return true;
            }
        }

        if (mCompoungDrawbles[2] != null) {
            boolean tappedX = event.getX() > (getWidth() - getPaddingRight() - mCompoungDrawbles[2].getIntrinsicWidth());
            if (tappedX) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    clickDrawable();
                }
                return true;
            }
        }

        final float yPos = event.getY();
        if (mCompoungDrawbles[1] != null) {
            boolean tappedX = yPos < (getPaddingTop() + mCompoungDrawbles[1].getIntrinsicHeight());
            if (tappedX) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    clickDrawable();
                }
                return true;
            }
        }
        if (mCompoungDrawbles[3] != null) {
            boolean tappedX = yPos > (getHeight() - getPaddingBottom() - mCompoungDrawbles[3].getIntrinsicHeight());
            if (tappedX) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    clickDrawable();
                }
                return true;
            }
        }

        if (mOnTouchListener != null) {
            return mOnTouchListener.onTouch(v, event);
        }

        return false;
    }

    private void clickDrawable() {
        setText("");
        if (mDrawableClickListener != null) {
            mDrawableClickListener.onClickDrawable(this);
        }
    }

    @Override
    public void setCompoundDrawables(Drawable left, Drawable top, Drawable right, Drawable bottom) {
        super.setCompoundDrawables(left, top, right, bottom);
        mCompoungDrawbles = getCompoundDrawables();
    }

    @Override
    public void setCompoundDrawablesRelative(Drawable start, Drawable top, Drawable end, Drawable bottom) {
        super.setCompoundDrawablesRelative(start, top, end, bottom);
        mCompoungDrawbles = getCompoundDrawables();
    }
}
