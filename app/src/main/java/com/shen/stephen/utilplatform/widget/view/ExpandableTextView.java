package com.shen.stephen.utilplatform.widget.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.text.SpannableStringBuilder;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import com.shen.stephen.utilplatform.R;

class ExpandableTextView extends TextView implements View.OnTouchListener {
    private static final int DEFAULT_TRIM_LENGTH = 60;
    private static final String ELLIPSIS = ".....";

    private CharSequence originalText = null;
    private CharSequence trimmedText = null;
    private BufferType bufferType;
    private boolean trim = true;
    private int trimLength;

    private OnTouchListener mOnTouchlistener;

    public ExpandableTextView(Context context) {
        this(context, null);
    }

    public ExpandableTextView(Context context, AttributeSet attrs) {
        super(context, attrs);

        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.ExpandableTextView);
        this.trimLength = typedArray.getInt(R.styleable.ExpandableTextView_trimLength, DEFAULT_TRIM_LENGTH);
        typedArray.recycle();

        super.setOnTouchListener(this);
    }

    @Override
    public void setOnTouchListener(OnTouchListener l) {
        mOnTouchlistener = l;
    }

    private void setText() {
        super.setText(getDisplayableText(), bufferType);
    }

    private CharSequence getDisplayableText() {
        return trim ? trimmedText : originalText;
    }

    @Override
    public void setText(CharSequence text, BufferType type) {
        originalText = text;
        trimmedText = getTrimmedText(text);
        bufferType = type;
        setText();
    }

    /**
     * Check whether the text should trim.
     */
    private boolean textShouldTrim() {
        return originalText != trimmedText;
    }

    private CharSequence getTrimmedText(CharSequence text) {
        if (originalText != null && originalText.length() > trimLength) {
            return new SpannableStringBuilder(originalText, 0, trimLength + 1).append(ELLIPSIS);
        } else {
            return originalText;
        }
    }

    public CharSequence getOriginalText() {
        return originalText;
    }

    public void setTrimLength(int trimLength) {
        this.trimLength = trimLength;
        trimmedText = getTrimmedText(originalText);
        setText();
    }

    public int getTrimLength() {
        return trimLength;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (!textShouldTrim()) {
            if (mOnTouchlistener != null) {
                return mOnTouchlistener.onTouch(v, event);
            }
            return false;
        }

        if (event.getAction() == MotionEvent.ACTION_UP) {
            trim = !trim;
            setText();
            requestFocusFromTouch();
            return false;
        } else if (event.getAction() == MotionEvent.ACTION_DOWN) {
            return true;
        }
        return true;
    }
}
