package com.shen.stephen.utilplatform;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;

/**
 * Created by chengcn on 3/22/2016.
 */
public class PKIApplication extends Application {
    private static Context mContext;
    /**
     * The broadcast action key for refresh view data.
     */
    public static final String ACTION_REFRESH_VIEW = "com.shen.stephen.utilplatform.PKIApplication.ACTION_REFRESH_VIEW";

    /**
     * Specify the app whether is launched
     */
    private static boolean mIsAppLaunched;

    public PKIApplication() {
        mContext = this;
        UIHandler.getInstance().setMainThreadId(Thread.currentThread().getId());
    }

    public static Context getContext() {
        return mContext;
    }

    public static String getResString(int resId) {
        return getContext().getString(resId);
    }

    /**
     * Called when App was launched. It must be called on Splash activity.
     */
    public static void appLaunched() {
        mIsAppLaunched = true;
    }

    /**
     * Check whether the app is launched or not.
     *
     */
    public static boolean isAppLaunched() {
        return mIsAppLaunched;
    }

    /**
     * Send refresh views' data broadcast.
     */
    public static void sendRefreshViewBroadcast(Bundle data, String... category) {
        Intent intent = new Intent(ACTION_REFRESH_VIEW);
        if (category != null) {
            for (String s : category) {
                intent.addCategory(s);
            }
        }
        if (data != null) {
            intent.putExtras(data);
        }
        mContext.sendBroadcast(intent);
    }

    /**
     * Register refresh views' data broadcast.
     *
     * @param receiver
     *            the broadcast receiver.
     * @param categories
     *            the broadcast's categories.
     */
    public static void registerRefreshViewBroadcast(BroadcastReceiver receiver,
                                                    String... categories) {
        IntentFilter filter = new IntentFilter(ACTION_REFRESH_VIEW);
        if (categories != null) {
            for (String category : categories) {
                filter.addCategory(category);
            }
        }
        getContext().registerReceiver(receiver, filter);
    }

    /**
     * Unregister a previously registered refresh view BroadcastReceiver.
     *
     * @param receiver
     *            the BroadcastReceiver.
     */
    public static void unregisterRefreshViewBroadcast(BroadcastReceiver receiver) {
        getContext().unregisterReceiver(receiver);
    }
}
