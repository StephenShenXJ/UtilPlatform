package com.shen.stephen.utilplatform.widget.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Parcelable;
import android.support.v4.view.MotionEventCompat;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;

import com.shen.stephen.utilplatform.R;
import com.shen.stephen.utilplatform.widget.view.swipeRefreshLayout.SwipeRefreshLayout;

public class LoadMoreRefreshLayout extends SwipeRefreshLayout {

	private View mChildView;
	private View mLoadFooter;
	private float mYDown = 0, mLastY = 0;
	private int mTouchSlop;
	private boolean mIsLoading = false;
	private OnLoadMoreListener mListener;
	private int mFirstVisiblePosition = 0;
	private boolean mIsBeingDragged = false;
	private boolean needRefresh = true;
	private boolean needLoadMore = true;

	private LayoutManager mLayoutManager;
	private int mTouchPointId = -1;

	public LoadMoreRefreshLayout(Context context) {
		this(context, null);
	}

	@SuppressLint("InflateParams")
	public LoadMoreRefreshLayout(Context context, AttributeSet attr) {
		super(context, attr);
		this.setColorSchemeResources(R.color.perkin_selected_dark_blue, R.color.white);
		mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
		LayoutInflater li = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mLoadFooter = li.inflate(R.layout.load_more_footer, null, false);
	}

	@Override
	protected Parcelable onSaveInstanceState() {
		if (isRefreshing()) {
			clearAnimation();
			super.setRefreshing(false);
			dismissSpinner();
		}
		return super.onSaveInstanceState();
	}

	public void setLayoutManager(LayoutManager layoutManager) {
		this.mLayoutManager = layoutManager;
	}

	public void setNeedRefresh(boolean needRefresh) {
		this.needRefresh = needRefresh;
	}

	public void setNeedLoadMore(boolean needLoadMore) {
		this.needLoadMore = needLoadMore;
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
		if (mChildView == null && mLayoutManager != null) {
			mChildView = mLayoutManager.setChildView(this);
		}
		super.onLayout(changed, left, top, right, bottom);
	}

	public void setOnLoadMoreListener(OnLoadMoreListener listener) {
		this.mListener = listener;
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent event) {
		int action = event.getAction();
		final int pointerIndex = (action & MotionEvent.ACTION_POINTER_INDEX_MASK)
				>> MotionEvent.ACTION_POINTER_INDEX_SHIFT;
		if (mTouchPointId != -1 && mTouchPointId != event.getPointerId(pointerIndex)) {
			return true;
		}
		mTouchPointId = event.getPointerId(0);
		switch (action) {
			case MotionEvent.ACTION_DOWN:
				mTouchPointId = event.getPointerId(0);
				final float yDown = getMotionEventY(event, mTouchPointId);
				if (yDown < 0) {
					return true;
				}

				mYDown = yDown;
				mLastY = mYDown;
				mIsBeingDragged = false;
				break;
			case MotionEvent.ACTION_MOVE: {
				final float lastY = getMotionEventY(event, mTouchPointId);
				if (lastY < 0) {
					return true;
				}
				mLastY = lastY;
				float yDiff = mYDown - mLastY;
				if (yDiff > mTouchSlop && !mIsBeingDragged) {
					mYDown = mYDown + mTouchSlop;
					mIsBeingDragged = true;
					//super.dispatchTouchEvent(event);
					return canDoLoad();
				}

				break;
			}
			case MotionEvent.ACTION_UP: {
				final float lastY = getMotionEventY(event, mTouchPointId);
				if (lastY < 0) {
					return true;
				}
				if (canDoLoad()) {
					doLoad();
					return true;
				}
				break;
			}
			default:
				break;
		}

		return super.dispatchTouchEvent(event);
	}

	private float getMotionEventY(MotionEvent ev, int activePointerId) {
		final int index = MotionEventCompat.findPointerIndex(ev, activePointerId);
		if (index < 0) {
			return -1;
		}
		return MotionEventCompat.getY(ev, index);
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent event) {
		if (needRefresh) {
			return super.onInterceptTouchEvent(event);
		} else {
            /*if (!isEnabled() || canChildScrollDown() || mIsLoading) {
                return false;
            }*/
			return false;
		}
	}

	@SuppressLint("ClickableViewAccessibility")
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		int action = event.getAction();
		switch (event.getAction()) {
			case MotionEvent.ACTION_CANCEL:
			case MotionEvent.ACTION_UP:
				mTouchPointId = -1;
				break;
			case MotionEvent.ACTION_POINTER_UP: {
				// Extract the index of the pointer that left the touch sensor
				final int pointerIndex = (action & MotionEvent.ACTION_POINTER_INDEX_MASK)
						>> MotionEvent.ACTION_POINTER_INDEX_SHIFT;
				final int pointerId = event.getPointerId(pointerIndex);
				if (pointerId == mTouchPointId) {
					mTouchPointId = -1;
				}
				break;
			}
		}


		if (needRefresh) {
			return super.onTouchEvent(event);
		} else {
			//super.onTouchEvent(arg0);
			return true;
		}

	}

	/**
	 * @return the mFirstVisiblePosition
	 */
	public int getFirstVisiblePosition() {
		return mFirstVisiblePosition;
	}

	/**
	 * @param firstVisiblePosition the mFirstVisiblePosition to set
	 */
	public void setFirstVisiblePosition(int firstVisiblePosition) {
		this.mFirstVisiblePosition = firstVisiblePosition;
	}

	private boolean canDoLoad() {
		if (isBottom() && isPullUp() && !mIsLoading && !isRefreshing()) {
			return true;
		}
		return false;
	}

	private boolean isBottom() {
		if (mLayoutManager != null) {
			return mLayoutManager.isBottom();
		}
		return false;

	}

	private boolean isPullUp() {
		return mYDown - mLastY > mTouchSlop;
	}

	private void doLoad() {
		if (mListener != null && needLoadMore) {
			setLoadingMore(true);
			mListener.onLoadMore();
		}
	}

	public void setLoadingMore(boolean isLoading) {
		if ( mIsLoading == isLoading )
			return;

		mIsLoading = isLoading;
		if (mLayoutManager != null) {
			mLayoutManager.setLoadingMoreView(isLoading, mLoadFooter);
		}

		if (!mIsLoading) {
			mYDown = 0;
		}
	}

	public void onChildScroll() {
		if (canDoLoad()) {
			doLoad();
		}
	}

	public void onChildScroll(int firstVisibleItem) {
		mFirstVisiblePosition = firstVisibleItem;
		if (canDoLoad()) {
			doLoad();
		}
	}
/*
    protected boolean canChildScrollDown() {
        if (android.os.Build.VERSION.SDK_INT < 14) {
            if (mChildView instanceof AbsListView) {
                final AbsListView absListView = (AbsListView) mChildView;
                return absListView.getChildCount() > 0 && absListView.getAdapter() != null
                        && absListView.getLastVisiblePosition() < ((AbsListView) mChildView).getAdapter().getCount() - 1;
            } else {
                return ViewCompat.canScrollVertically(mChildView, 1);
            }
        } else {
            return ViewCompat.canScrollVertically(mChildView, 1);
        }
    }
*/

	/**
	 * Classes that wish to be notified when the swipe gesture correctly
	 * triggers a loading more should implement this interface.
	 */
	public interface OnLoadMoreListener {
		public void onLoadMore();
	}

	public static abstract class LayoutManager {
		protected Context mContext;

		public LayoutManager(Context context) {
			this.mContext = context;
		}

		public abstract View setChildView(LoadMoreRefreshLayout parent);

		public abstract boolean isBottom();

		public abstract void setLoadingMoreView(boolean isLoading, View footerView);
	}
}
