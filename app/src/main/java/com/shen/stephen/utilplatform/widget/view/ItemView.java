package com.shen.stephen.utilplatform.widget.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.Switch;
import android.widget.TextView;

import com.shen.stephen.utilplatform.R;
import com.shen.stephen.utilplatform.util.StrUtil;

public class ItemView extends FrameLayout implements View.OnClickListener {
	/**
	 * The orientation enum definition
	 */
	public static final int HORIZONTAL = 0;
	public static final int VERTICAL = 1;

	private TextView textTitle;
	private TextView textContent;
	private ImageView mArrowView, mHelperView;
	private Switch mSwitcher;
	private LinearLayout mContentViewContainer;

	private OnClickListener mOnContentClickListener;
	private OnCheckedChangeListener mOnCheckListener;

	private CharSequence mTitle;
	private String mContent;
	private String mHelperDes;
	private float mTitleSize;
	private float mContentSize;
	private int mTitleColor;
	private int mContentColor;
	private int mOrientation;

	private boolean mIsEditMode;
	/**
	 * Specify whether the itemView is checkable or not.
	 */
	private boolean mIsCheckable;

	public ItemView(Context context) {
		this(context, null);
	}

	public ItemView(Context context, AttributeSet attrs) {
		super(context, attrs, 0);
		setParamsByAttrs(context, attrs);
		initView(context);
	}

	private void setParamsByAttrs(Context context, AttributeSet attrs) {
		TypedArray mTypedArray = context.obtainStyledAttributes(attrs,
				R.styleable.ItemView);
		int defaultTextColor = getContext().getResources().getColor(
				R.color.black);
		mTitle = mTypedArray.getString(R.styleable.ItemView_itemTitle);
		mContent = mTypedArray.getString(R.styleable.ItemView_itemContent);
		mTitleSize = mContentSize = mTypedArray.getDimension(
				R.styleable.ItemView_itemSize, 20);
		mTitleSize = mTypedArray.getDimension(
				R.styleable.ItemView_titleTextSize, mTitleSize);
		mContentSize = mTypedArray.getDimension(
				R.styleable.ItemView_contentTextSize, mContentSize);
		mTitleColor = mTypedArray.getColor(R.styleable.ItemView_titleColor,
				defaultTextColor);
		mContentColor = mTypedArray.getColor(R.styleable.ItemView_contentColor,
				defaultTextColor);
		mOrientation = mTypedArray.getInteger(
				R.styleable.ItemView_aligneOrientation, HORIZONTAL);
		mIsCheckable = mTypedArray.getBoolean(R.styleable.ItemView_isCheckable,
				false);
		mTypedArray.recycle();
	}

	private void initView(Context c) {
		View viewParent;
		LayoutInflater li = (LayoutInflater) c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		switch (mOrientation) {
			case HORIZONTAL:
			default:
				viewParent = li.inflate(R.layout.view_item_horizontal, this, false);
				break;
			case VERTICAL:
				viewParent = li.inflate(R.layout.view_item_vertical, this, false);
				break;
		}

		mContentViewContainer = (LinearLayout) viewParent.findViewById(R.id.item_view_content_container);
		textTitle = (TextView) viewParent.findViewById(R.id.item_view_title);
		textContent = (TextView) viewParent.findViewById(R.id.item_view_content);
		mArrowView = (ImageView) viewParent.findViewById(R.id.item_view_next_arrow);
		mSwitcher = (Switch) viewParent.findViewById(R.id.item_view_switcher);

		mArrowView.setVisibility(mIsEditMode && !mIsCheckable ? View.VISIBLE : View.GONE);
		mSwitcher.setVisibility(mIsCheckable ? View.VISIBLE : View.GONE);
		mSwitcher.setOnCheckedChangeListener(mOnCheckListener);

		textTitle.setTextSize(TypedValue.COMPLEX_UNIT_PX, mTitleSize);
		textTitle.setTextColor(mTitleColor);
		textTitle.setText(mTitle);

		textContent.setTextSize(TypedValue.COMPLEX_UNIT_PX, mContentSize);
		textContent.setTextColor(mContentColor);
		textContent.setText(mContent);
		textContent.setOnClickListener(mOnContentClickListener);
		if (mOnContentClickListener == null) {
			textContent.setClickable(false);
		}
		mHelperView = (ImageView) viewParent.findViewById(R.id.item_view_helper);
		addView(viewParent);
	}

	/**
	 * Add a custom content view to the item content view container.
	 *
	 * @param customView the custom content view
	 */
	public void addCustomContentView(View customView) {
		if (mContentViewContainer != null) {
			mContentViewContainer.addView(customView);
		}
	}

	/**
	 * clear the custom content view that added by the {@link #addCustomContentView(View)}
	 */
	public void clearCustomContentView() {
		if (mContentViewContainer != null && mContentViewContainer.getChildCount() > 1) {
			mContentViewContainer.removeViews(1, mContentViewContainer.getChildCount() - 1);
		}
	}

	/**
	 * Set the item view showing mode. vertical or horizontal.
	 *
	 * @param orientation the orientation of the showing mode. must one of
	 *                    {@linkplain #VERTICAL} and {@link #HORIZONTAL}.
	 */
	public void setOrientation(int orientation) {
		if (orientation == mOrientation) {
			return;
		}
		mOrientation = orientation;
		removeAllViews();
		initView(getContext());
	}

	public void setTitle(CharSequence title) {
		this.mTitle = title;
		textTitle.setText(mTitle);
	}

	public CharSequence getTitle() {
		return this.mTitle;
	}

	public void setHelperDescription(String helperDescription) {
		mHelperDes = helperDescription;
		if (StrUtil.isEmptyWithoutBlank(helperDescription)) {
			mHelperView.setOnClickListener(null);
			mHelperView.setVisibility(GONE);
		} else {
			mHelperView.setVisibility(VISIBLE);
			mHelperView.setOnClickListener(this);
		}
	}

	/**
	 * Set the itemView checkable. if it is checkable the {@linkplain Switch}
	 * view will showing.
	 *
	 * @param checkable true for checkable otherwise false.
	 */
	public void setCheckable(boolean checkable) {
		if (mIsCheckable != checkable) {
			mIsCheckable = checkable;
			if (mIsCheckable) {
				mSwitcher.setVisibility(View.VISIBLE);
				mArrowView.setVisibility(View.GONE);
			} else {
				mSwitcher.setVisibility(View.GONE);
				mArrowView
						.setVisibility(mIsEditMode ? View.VISIBLE : View.GONE);
			}
		}
	}

	/**
	 * Changes the checked state of this itemView.
	 *
	 * @param isChecked true to check the button, false to uncheck it.
	 */
	public void setChecked(boolean isChecked) {
		mSwitcher.setChecked(isChecked);
	}

	/**
	 * @return The current checked state of the view
	 */
	public boolean isChecked() {
		return mSwitcher.isChecked();
	}

	/**
	 * Set title text size.
	 *
	 * @param sizeInPixel the text size in pixel.
	 */
	public void setTitleSize(float sizeInPixel) {
		textTitle.setTextSize(TypedValue.COMPLEX_UNIT_PX, sizeInPixel);
		mTitleSize = textTitle.getTextSize();
	}

	/**
	 * Set the default text size to a given unit and value.  See {@link
	 * TypedValue} for the possible dimension units.
	 *
	 * @param unit The desired dimension unit.
	 * @param size The desired size in the given units.
	 * @attr ref android.R.styleable#TextView_textSize
	 */
	public void setTitleSize(int unit, int size) {
		textTitle.setTextSize(unit, size);
		mTitleSize = textTitle.getTextSize();
	}

	public void setContent(String content) {
		this.mContent = content;
		textContent.setText(mContent);
		if (StrUtil.isEmpty(content)) {
			textContent.setVisibility(View.GONE);
		} else {
			textContent.setVisibility(View.VISIBLE);
		}
	}

	public String getContent() {
		return this.mContent;
	}

	/**
	 * Set content text size.
	 *
	 * @param sizeInPixel the text size in pixel.
	 */
	public void setContentSize(float sizeInPixel) {
		textContent.setTextSize(TypedValue.COMPLEX_UNIT_PX, sizeInPixel);
		mContentSize = textContent.getTextSize();
	}

	/**
	 * Set the default text size to a given unit and value.  See {@link
	 * TypedValue} for the possible dimension units.
	 *
	 * @param unit The desired dimension unit.
	 * @param size The desired size in the given units.
	 * @attr ref android.R.styleable#TextView_textSize
	 */
	public void setContentSize(int unit, int size) {
		textContent.setTextSize(unit, size);
		mContentSize = textContent.getTextSize();
	}

	@Override
	public boolean isInEditMode() {
		return mIsEditMode;
	}

	public void activeEditMode() {
		mIsEditMode = true;
		mArrowView.setVisibility(View.VISIBLE);
	}

	public void inactiveEditMode() {
		mIsEditMode = false;
		mArrowView.setVisibility(View.GONE);
	}

	/**
	 * @return the mSize
	 */
	public float getItemSize() {
		return mTitleSize;
	}

	/**
	 * @param sizeInPixel the item size to set, it's in pixel unit.
	 */
	public void setItemTextSize(float sizeInPixel) {
		this.mTitleSize = sizeInPixel;
		textTitle.setTextSize(TypedValue.COMPLEX_UNIT_PX, mTitleSize);
		textContent.setTextSize(TypedValue.COMPLEX_UNIT_PX, mTitleSize);
	}

	/**
	 * @return the mTitleColor
	 */
	public int getTitleColor() {
		return mTitleColor;
	}

	/**
	 * @param titleColor the mTitleColor to set. e.g:R.color.foo.
	 */
	public void setTitleColor(int titleColor) {
		this.mTitleColor = titleColor;
		textTitle.setTextColor(mTitleColor);

	}

	/**
	 * @return the mContentColor
	 */
	public int getContentColor() {
		return mContentColor;
	}

	/**
	 * @param contentColor the mContentColor to set
	 */
	public void setContentColor(int contentColor) {
		this.mContentColor = contentColor;
		textContent.setTextColor(mContentColor);
	}

	/**
	 * Register a callback to be invoked when the checked state of this button
	 * changes.
	 *
	 * @param listener the callback to call on checked state change
	 */
	public void setOnCheckedChangeListener(OnCheckedChangeListener listener) {
		mOnCheckListener = listener;
		mSwitcher.setOnCheckedChangeListener(listener);
	}

	/**
	 * Register a callback to be invoked when the content view is clicked.
	 *
	 * @param onContentClickListener The callback that will run
	 */
	public void setOnContentClickListener(OnClickListener onContentClickListener) {
		mOnContentClickListener = onContentClickListener;
		textContent.setOnClickListener(mOnContentClickListener);

		if (mOnContentClickListener == null) {
			textContent.setClickable(false);
		}
	}

	@Override
	public void onClick(View v) {
		if (v == mHelperView) {
			showHelper();
		}
	}

	public void showHelper() {

		if (StrUtil.isEmptyWithoutBlank(mHelperDes)) {
			return;
		}
		TextView tv = new TextView(getContext());
		tv.setText(mHelperDes);
		tv.setTextColor(getContext().getResources().getColor(R.color.white));
		tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
		PopupWindow mPopupWindow = new PopupWindow(tv);
		mPopupWindow.setWindowLayoutMode(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
		mPopupWindow.getContentView().measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);

		mPopupWindow.setFocusable(true);
		mPopupWindow.setOutsideTouchable(true);
		mPopupWindow.setBackgroundDrawable(getContext().getResources().getDrawable(R.drawable.popover_background));
		int xPos = (mHelperView.getWidth() - mPopupWindow.getContentView().getMeasuredWidth()) / 2;

		mPopupWindow.showAsDropDown(mHelperView, xPos, 0);

	}
}
