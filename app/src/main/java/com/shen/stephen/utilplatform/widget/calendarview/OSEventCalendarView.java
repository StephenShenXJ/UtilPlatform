package com.shen.stephen.utilplatform.widget.calendarview;

import android.content.Context;
import android.database.DataSetObserver;
import android.os.Parcelable;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;

import com.shen.stephen.utilplatform.R;
import com.shen.stephen.utilplatform.adapter.RecyclePagerAdapter;
import com.shen.stephen.utilplatform.widget.calendarview.adapter.DayEventAdapter;
import com.shen.stephen.utilplatform.widget.calendarview.adapter.EventsAdapter;
import com.shen.stephen.utilplatform.widget.calendarview.adapter.OSEventListAdapter;
import com.shen.stephen.utilplatform.widget.view.swipeRefreshLayout.SwipeRefreshLayout;
import com.shen.stephen.utilplatform.util.PkiTimeUtil;

import java.util.Calendar;
import java.util.List;

/**
 * Created by ChengCn on 12/16/2015.
 */
public class OSEventCalendarView extends OSCalendarView implements OSEventTimeLineView.OnClickTimeLineItemListener, AdapterView.OnItemClickListener,
        OSEventTimeLineView.OnClickAddItemListener, OSEventTimeLineView.OnClickEventItemListener, SwipeRefreshLayout.OnRefreshListener {
    public static final int EVENT_VIEW_MODE_LIST = 0;
    public static final int EVENT_VIEW_MODE_TIMELINE = 1;

    private ViewPager mEventListViewPager;
    private ViewPagerAdapter mEventListViewPagerAdapter;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private View mFocusEventListView;

    private EventsAdapter mEventAdapter;
    private DataSetObserver mDataObserver;
    private int mEventViewMode;

    private OnClickEventListener mOnClickEventListener;
    private OnClickNewEventItemListener mOnClickNewEventItemListener;
    private OSEventTimeLineView.OnClickTimeLineItemListener mOnClickTimeLineItemListener;
    private OnRefreshEventOfDayListener mOnRefreshEventOfDayListener;

    public interface OnRefreshEventOfDayListener {
        void onRefreshEventOfDay(SwipeRefreshLayout refreshLayout, long time);
    }

    @Override
    public boolean onClickTimeLineItem(OSEventTimeLineView timeLineView, int position, long startTime, long endTime) {
        if (mOnClickTimeLineItemListener != null) {
            return mOnClickTimeLineItemListener.onClickTimeLineItem(timeLineView, position, startTime, endTime);
        }
        return false;
    }

    /**
     * Interface definition for a callback when click the event item
     */
    public interface OnClickEventListener {

        /**
         * The callback method when click the event item.
         *
         * @param view  the OSEventCalendarView instance.
         * @param event the clicked event.
         */
        void onClickEvent(OSEventCalendarView view, ICalendarEvent event);
    }

    /**
     * Interface definition for a callback when click the add new event item
     */
    public interface OnClickNewEventItemListener {

        /**
         * The callback method when click the add new event item.
         *
         * @param view             the OSEventCalendarView instance.
         * @param defaultStartTime the default start time of the event.
         */
        void onClickNewEventItem(OSEventCalendarView view, long defaultStartTime);
    }

    public OSEventCalendarView(Context context) {
        this(context, null);
    }

    public OSEventCalendarView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public OSEventCalendarView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        mEventViewMode = EVENT_VIEW_MODE_LIST;
        LayoutParams lp = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        mSwipeRefreshLayout = new VerticalSwipeRefreshLayout(getContext());
        mSwipeRefreshLayout.setLayoutParams(lp);
        mSwipeRefreshLayout.setOnRefreshListener(this);

        mEventListViewPager = new ViewPager(getContext());
        lp = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        mEventListViewPager.setLayoutParams(lp);
        mSwipeRefreshLayout.setId(R.id.event_calendar_view_swipe_refresh_layout);
        mSwipeRefreshLayout.addView(mEventListViewPager);
        mContentView.addView(mSwipeRefreshLayout);

        mEventListViewPagerAdapter = new ViewPagerAdapter();
        mEventListViewPager.setAdapter(mEventListViewPagerAdapter);
        mEventListViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                mFocusEventListView = mEventListViewPager.findViewWithTag(position);
                if (mFocusEventListView == null) {
                    return;
                }
                mTempDate.setTimeInMillis(getMinDate().getTimeInMillis());
                mTempDate.add(Calendar.DAY_OF_MONTH, position - 1);
                invalidateViewPager(mTempDate, mFocusEventListView);

                setSelectedDay(mTempDate);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        mEventListViewPager.setCurrentItem(PkiTimeUtil.getDaysSinceDate(getMinDate(), getSelectedDay()));
    }

    public void setOnClickEventListener(OnClickEventListener listener) {
        this.mOnClickEventListener = listener;
    }

    public void setOnClickNewEventItemListener(OnClickNewEventItemListener listener) {
        this.mOnClickNewEventItemListener = listener;
    }

    public void setOnClickTimelineItemListener(OSEventTimeLineView.OnClickTimeLineItemListener listener) {
        mOnClickTimeLineItemListener = listener;
    }

    public void setOnRefreshEventOfDayListener(OnRefreshEventOfDayListener listener) {
        mOnRefreshEventOfDayListener = listener;
    }

    public void setEventsAdapter(EventsAdapter adapter) {
        if (mEventAdapter != null && mDataObserver != null) {
            mEventAdapter.unregisterDataSetObserver(mDataObserver);
        }

        mEventAdapter = adapter;
        if (mEventAdapter != null) {
            mDataObserver = new AdapterDataSetObserver();
            mEventAdapter.registerDataSetObserver(mDataObserver);
        }

        invalidate();
    }

    public EventsAdapter getEventsAdapter() {
        return mEventAdapter;
    }

    // Adapter listener
    private class AdapterDataSetObserver extends DataSetObserver {
        @Override
        public void onChanged() {
            invalidateCalendarView();
        }

        @Override
        public void onInvalidated() {
            invalidateCalendarView();
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (mEventAdapter != null && mDataObserver != null) {
            mEventAdapter.unregisterDataSetObserver(mDataObserver);
            mDataObserver = null;
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (mEventAdapter != null && mDataObserver == null) {
            mDataObserver = new AdapterDataSetObserver();
            mEventAdapter.registerDataSetObserver(mDataObserver);
        }
    }

    protected void invalidateCalendarView() {
        super.invalidateCalendarView();
        invalidateViewPager(getSelectedDay(), mFocusEventListView);
    }

    private void invalidateViewPager(Calendar focusDate, View eventListView) {
        List<? extends ICalendarEvent> events = null;
        if (mEventAdapter != null) {
            events = mEventAdapter.getEvent(EventsAdapter.getKey(focusDate.getTimeInMillis()));
        }

        // The view is prepared.
        if (eventListView == null) {
            return;
        }

        if (mEventViewMode == EVENT_VIEW_MODE_LIST) {
            ListView listView = (ListView) eventListView;
            OSEventListAdapter adapter = (OSEventListAdapter) listView.getAdapter();
            adapter.updateEvents(events, focusDate);
        } else {
            OSEventTimeLineView timeLineView = (OSEventTimeLineView) eventListView;
            DayEventAdapter adapter = (DayEventAdapter) timeLineView.getEventAdapter();
            adapter.updateEvents(events, focusDate);
        }
    }

    @Override
    protected void onDaySelected(Calendar day) {
        mEventListViewPager.setCurrentItem(PkiTimeUtil.getDaysSinceDate(getMinDate(), day));
        //refreshView(false);
    }

    /**
     * Set the event view mode.
     *
     * @param eventViewMode the new event view mode.
     * @see OSEventCalendarView#EVENT_VIEW_MODE_LIST
     * @see OSEventCalendarView#EVENT_VIEW_MODE_TIMELINE
     */
    public void setEventViewMode(int eventViewMode) {
        if (mEventViewMode != eventViewMode) {
            this.mEventViewMode = eventViewMode;
            mFocusEventListView = null;
            final int currentItem = mEventListViewPager.getCurrentItem();
            mEventListViewPagerAdapter = new ViewPagerAdapter();
            mEventListViewPager.setAdapter(mEventListViewPagerAdapter);
            mEventListViewPager.setCurrentItem(currentItem);
        }
    }

    @Override
    protected boolean hasEvents(Calendar date) {
        if (mEventAdapter == null || date == null) {
            return false;
        }
        return mEventAdapter.hasEvent(date.getTimeInMillis());
    }

    /**
     * The attach state change listener for the list view.
     * <B>Comment:</B> Add this for fix a bug that, the list view not refreshed if the data has changed when the view was detached from window.
     */
    private OnAttachStateChangeListener mOnListViewAttachListener = new OnAttachStateChangeListener() {
        @Override
        public void onViewAttachedToWindow(View v) {
            mEventAdapter.notifyDataSetChanged();
        }

        @Override
        public void onViewDetachedFromWindow(View v) {

        }
    };

    private class ViewPagerAdapter extends RecyclePagerAdapter {

        @Override
        protected View getItemView(int position) {

            View v;
            LayoutParams lp = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            if (mEventViewMode == EVENT_VIEW_MODE_LIST) {
                ListView eventListView = new ListView(getContext());
                eventListView.setLayoutParams(lp);
                OSEventListAdapter mEventAdapter = new OSEventListAdapter(getContext());
                eventListView.setAdapter(mEventAdapter);
                eventListView.setOnItemClickListener(OSEventCalendarView.this);
                v = eventListView;
            } else {
                OSEventTimeLineView eventTimeLineView = new OSEventTimeLineView(getContext());
                eventTimeLineView.setLayoutParams(lp);
                eventTimeLineView.setOnClickEventItemListener(OSEventCalendarView.this);
                eventTimeLineView.setOnClickAddItemListener(OSEventCalendarView.this);
                eventTimeLineView.setOnClickTimelineItemListener(OSEventCalendarView.this);
                eventTimeLineView.setEventAdapter(new DayEventAdapter());
                v = eventTimeLineView;
            }
            if (position == mEventListViewPager.getCurrentItem()) {
                mFocusEventListView = v;
                mFocusEventListView.addOnAttachStateChangeListener(mOnListViewAttachListener);
            }
            return v;
        }

        @Override
        public int getItemPosition(Object object) {
            return POSITION_NONE;
        }

        @Override
        protected void bindItemView(View itemView, int position, boolean isReused) {
            itemView.setTag(position);
            mTempDate.setTimeInMillis(getMinDate().getTimeInMillis());
            mTempDate.add(Calendar.DAY_OF_MONTH, position - 1);
            if (!isReused) {
                invalidateViewPager(mTempDate, itemView);
            }
        }

        @Override
        public int getCount() {
            return PkiTimeUtil.getDaysSinceDate(getMinDate(), getMaxDate());
        }

    }

    @Override
    public void onClickEvent(OSEventTimeLineView timeLineView, ICalendarEvent event) {
        if (mOnClickEventListener != null) {
            mOnClickEventListener.onClickEvent(OSEventCalendarView.this, event);
        }
    }

    @Override
    public void onClickAddItem(OSEventTimeLineView timeLineView, int position) {
        long time = getSelectedDateTime();
        mTempDate.setTimeInMillis(time);
        PkiTimeUtil.changeToStartOfDay(mTempDate);
        mTempDate.add(Calendar.MILLISECOND, position * PkiTimeUtil.MILLIS_IN_HOUR);

        if (mOnClickNewEventItemListener != null) {
            mOnClickNewEventItemListener.onClickNewEventItem(OSEventCalendarView.this, mTempDate.getTimeInMillis());
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        ICalendarEvent event = (ICalendarEvent) parent.getAdapter().getItem(position);
        if (mOnClickEventListener != null) {
            mOnClickEventListener.onClickEvent(OSEventCalendarView.this, event);
        }
    }

    @Override
    public void onRefresh() {
        if (mOnRefreshEventOfDayListener != null) {
            mOnRefreshEventOfDayListener.onRefreshEventOfDay(mSwipeRefreshLayout, getSelectedDateTime());
        } else {
            setRefreshing(false);
        }
    }

    /**
     * Notify the widget that refresh state has changed. Do not call this when
     * refresh is triggered by a swipe gesture.
     *
     * @param refreshing Whether or not the view should show refresh progress.
     */
    public void setRefreshing(boolean refreshing) {
        mSwipeRefreshLayout.setRefreshing(refreshing);
    }

    public class VerticalSwipeRefreshLayout extends SwipeRefreshLayout {

        private int mTouchSlop;
        private float mPrevX;

        public VerticalSwipeRefreshLayout(Context context) {
            this(context, null);
        }

        public VerticalSwipeRefreshLayout(Context context, AttributeSet attrs) {
            super(context, attrs);

            mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        }

        @Override
        public boolean onInterceptTouchEvent(MotionEvent event) {

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN: {
                    mPrevX = event.getX();
                    break;
                }
                case MotionEvent.ACTION_MOVE:
                    final float eventX = event.getX();
                    float xDiff = Math.abs(eventX - mPrevX);

                    if (xDiff > mTouchSlop) {
                        return false;
                    }
            }

            return super.onInterceptTouchEvent(event);
        }

        @Override
        public boolean canChildScrollUp() {
            if (mFocusEventListView == null) {
                return false;
            }
            if (mEventViewMode == EVENT_VIEW_MODE_LIST) {
                if (android.os.Build.VERSION.SDK_INT < 14) {
                    final AbsListView absListView = (AbsListView) mFocusEventListView;
                    return absListView.getChildCount() > 0
                            && (absListView.getFirstVisiblePosition() > 0 || absListView.getChildAt(0)
                            .getTop() < absListView.getPaddingTop());

                } else {
                    return ViewCompat.canScrollVertically(mFocusEventListView, -1);
                }
            } else {
                return mFocusEventListView.getScrollY() > 0;
            }
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
    }
}
