package com.shen.stephen.utilplatform.widget.calendarview.adapter;

import android.database.DataSetObserver;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ChengCn on 12/30/2015.
 */
public abstract class AbsOSEventAdapter implements OSEventAdapter {
    private List<DataSetObserver> mDataSetObservers = new ArrayList<>();

    @Override
    public void registerDataSetObserver(DataSetObserver observer) {
        if (mDataSetObservers.contains(observer)) {
            throw new IllegalStateException("Observer " + observer + " is already registered.");
        }
        mDataSetObservers.add(observer);
    }

    @Override
    public void unregisterDataSetObserver(DataSetObserver observer) {
        if (mDataSetObservers != null) {
            mDataSetObservers.remove(observer);
        }
    }

    /**
     * Notifies observers about data changing
     */
    public void notifyDataSetChanged() {
        if (mDataSetObservers != null) {
            for (DataSetObserver observer : mDataSetObservers) {
                observer.onChanged();
            }
        }
    }

    /**
     * Notifies observers about invalidating data
     */
    public void notifyDataInvalidatedEvent() {
        if (mDataSetObservers != null) {
            for (DataSetObserver observer : mDataSetObservers) {
                observer.onInvalidated();
            }
        }
    }
}
