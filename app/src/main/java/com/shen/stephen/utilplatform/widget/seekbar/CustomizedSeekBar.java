package com.shen.stephen.utilplatform.widget.seekbar;

/**
 * Created by JiangTx on 2015/10/28.
 */

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.SeekBar;

import com.shen.stephen.utilplatform.R;

import java.util.ArrayList;
import java.util.List;

public class CustomizedSeekBar extends SeekBar {

    public static final int TEXT_ALIGN_TOP = 0;
    public static final int TEXT_ALIGN_BOTTOM = 1;

    private CustomizedThumbDrawable mThumb;
    private CustomizedBgDrawable mProgressDrawable;
    private List<Dot> mDots = new ArrayList<Dot>();
    private OnItemClickListener mItemClickListener;
    private Dot prevSelected = null;
    private boolean isSelected = false;
    private boolean isTouchFinished = false;
    private int mTextColor;
    private int mBarSelectedStartColor;
    private int mBarSelectedEndColor;
    private int mBarUnselectedColor;
    private int mThumbColor;
    private int mTextSize;
    private boolean mIsMultiline;
    private int mTextAlign = TEXT_ALIGN_BOTTOM;

    /**
     * @param context context.
     */
    public CustomizedSeekBar(Context context) {
        super(context);
    }

    /**
     * @param context context.
     * @param attrs   attrs.
     */
    public CustomizedSeekBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.CustomizedSeekBar);
        mTextColor = a.getColor(R.styleable.CustomizedSeekBar_textColor, Color.BLACK);
        mBarSelectedStartColor = a.getColor(R.styleable.CustomizedSeekBar_barSelectedStartColor, Color.WHITE);
        mBarSelectedEndColor = a.getColor(R.styleable.CustomizedSeekBar_barSelectedEndColor, Color.BLACK);
        mBarUnselectedColor = a.getColor(R.styleable.CustomizedSeekBar_barUnselectedColor, Color.BLACK);
        mThumbColor = a.getColor(R.styleable.CustomizedSeekBar_thrumbColor, Color.BLACK);
        mTextSize = a.getDimensionPixelSize(R.styleable.CustomizedSeekBar_textSize, 5);
        mIsMultiline = a.getBoolean(R.styleable.CustomizedSeekBar_multiline, false);
        mTextAlign = a.getInt(R.styleable.CustomizedSeekBar_textAlign, TEXT_ALIGN_TOP);
        a.recycle();

        mThumb = new CustomizedThumbDrawable(context, mThumbColor);
        setThumb(mThumb);

        mProgressDrawable = new CustomizedBgDrawable(this.getProgressDrawable(), this.getContext(), mThumb.getRadius(), mDots, mTextSize, mIsMultiline);
        mProgressDrawable.setTextColor(mTextColor);
        mProgressDrawable.setBarSelectedColor(new int[] {mBarSelectedStartColor, mBarSelectedEndColor});
        mProgressDrawable.setBarUnselectedColor(mBarUnselectedColor);
        mProgressDrawable.setTextAlign(mTextAlign);
        setProgressDrawable(mProgressDrawable);

        setPadding(0, 0, 0, 0);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                isTouchFinished = false;
                break;
            case MotionEvent.ACTION_MOVE:
                break;
            case MotionEvent.ACTION_UP:
                isTouchFinished = true;
                break;
        }
        isSelected = false;

        return super.onTouchEvent(event);
    }

    /**
     * @param color color.
     */
    public void setColor(int color) {
        mTextColor = color;
        mThumb.setColor(color);
        setProgressDrawable(new CustomizedBgDrawable((CustomizedBgDrawable) this.getProgressDrawable(), this.getContext(), mThumb.getRadius(), mDots, mTextSize, mIsMultiline));
    }

    public void setSelection(int position) {
        if ((position < 0) || (position >= mDots.size())) {
            throw new IllegalArgumentException("Position is out of bounds:" + position +  "  Dot size:" + mDots.size());
        }
        for (Dot dot : mDots) {
            if (dot.id == position) {
                dot.isSelected = true;
                if (mItemClickListener != null) {
                    mItemClickListener.onItemClick(null, this, dot.id, dot.id);
                }
            } else {
                dot.isSelected = false;
            }
        }

        isSelected = true;
        invalidate();
    }

    public void setAdapter(List<String> dots) {
        mDots.clear();
        int index = 0;
        for (String dotName : dots) {
            Dot dot = new Dot();
            dot.text = dotName;
            dot.id = index++;
            mDots.add(dot);
        }
        initDotsCoordinates();
    }

    @Override
    public void setThumb(Drawable thumb) {
        if (thumb instanceof CustomizedThumbDrawable) {
            mThumb = (CustomizedThumbDrawable) thumb;
        }
        super.setThumb(thumb);
    }

    @Override
    protected void onDraw(Canvas canvas) {

        if ((mThumb != null) && (mDots.size() > 1)) {
            if (isSelected) {
                for (Dot dot : mDots) {
                    if (dot.isSelected) {
                        Rect bounds = mThumb.copyBounds();
                        bounds.right = dot.mX;
                        bounds.left = dot.mX;
                        mThumb.setBounds(bounds);
                        break;
                    }
                }

            } else {
                int intervalWidth = mDots.get(1).mX - mDots.get(0).mX;
                Rect bounds = mThumb.copyBounds();

                // find nearest dot
                if ((mDots.get(mDots.size() - 1).mX - bounds.centerX()) < 0) {
                    bounds.right = mDots.get(mDots.size() - 1).mX;
                    bounds.left = mDots.get(mDots.size() - 1).mX;
                    mThumb.setBounds(bounds);

                    for (Dot dot : mDots) {
                        dot.isSelected = false;
                    }
                    mDots.get(mDots.size() - 1).isSelected = true;
                    handleClick(mDots.get(mDots.size() - 1));
                } else {
                    for (int i = 0; i < mDots.size(); i++) {
                        if (Math.abs(mDots.get(i).mX - bounds.centerX()) <= (intervalWidth / 2)) {
                            if (isTouchFinished) {
                                bounds.right = mDots.get(i).mX;
                                bounds.left = mDots.get(i).mX;
                                mThumb.setBounds(bounds);
                            }
                            mDots.get(i).isSelected = true;
                            mProgressDrawable.setXPosition(bounds.centerX());
                            handleClick(mDots.get(i));

                        } else {
                            mDots.get(i).isSelected = false;
                        }
                    }
                }
            }
        }
        super.onDraw(canvas);

    }

    private void handleClick(Dot selected) {
        if ((prevSelected == null) || (prevSelected.equals(selected) == false)) {
            if (mItemClickListener != null) {
                mItemClickListener.onItemClick(null, this, selected.id, selected.id);
            }
            prevSelected = selected;
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        CustomizedBgDrawable d = (CustomizedBgDrawable) getProgressDrawable();

        int thumbHeight = mThumb == null ? 0 : mThumb.getIntrinsicHeight();
        int dw = 0;
        int dh = 0;
        if (d != null) {
            dw = d.getIntrinsicWidth();
            dh = Math.max(thumbHeight, d.getIntrinsicHeight());
        }

        dw += getPaddingLeft() + getPaddingRight();
        dh += getPaddingTop() + getPaddingBottom();

        setMeasuredDimension(resolveSize(dw, widthMeasureSpec), resolveSize(dh, heightMeasureSpec));
    }

    /**
     * dot coordinates.
     */
    private void initDotsCoordinates() {
        float intervalWidth = (getWidth() - (mThumb.getRadius() * 2)) / (mDots.size() - 1);
        for (Dot dot : mDots) {
            dot.mX = (int) (mThumb.getRadius() + intervalWidth * (dot.id));
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        initDotsCoordinates();
    }

    /**
     * Sets a listener to receive events when a list item is clicked.
     *
     * @param clickListener Listener to register
     * @see ListView#setOnItemClickListener(android.widget.AdapterView.OnItemClickListener)
     */
    public void setOnItemClickListener(AdapterView.OnItemClickListener clickListener) {
        mItemClickListener = clickListener;
    }

    public static class Dot {
        public int id;
        public int mX;
        public String text;
        public boolean isSelected = false;

        @Override
        public boolean equals(Object o) {
            return ((Dot) o).id == id;
        }
    }
}
