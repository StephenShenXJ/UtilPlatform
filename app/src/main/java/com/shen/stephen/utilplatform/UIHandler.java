package com.shen.stephen.utilplatform;

import android.app.Activity;
import android.os.Handler;

import com.shen.stephen.utilplatform.widget.PkiActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * This is a utility class for some UI handle. It is hold a running activities
 * stack, and it can do the others actions, such as put some code run on UI
 * thread, get the current foreground activity etc.
 */
public class UIHandler {
    private static UIHandler mInstance;

    /**
     * the running activities stack.
     */
    private List<PkiActivity> mActivities;

    /**
     * The current foreground activity.
     */
    private PkiActivity mForegroundActivity;

    /**
     * The home activity
     */
    private PkiActivity mHomeActivity;

    private Handler mHandler;

    private long mMainThreadId;

    private ExecutorService mExecutorService = Executors.newSingleThreadExecutor();

    private UIHandler() {
        mActivities = new ArrayList<>();
        mHandler = new Handler();
    }

    public static UIHandler getInstance() {
        if (mInstance == null) {
            mInstance = new UIHandler();
        }

        return mInstance;
    }

    /**
     * Set main thread id.
     *
     * @param mainThreadId
     */
    void setMainThreadId(long mainThreadId) {
        mMainThreadId = mainThreadId;
    }

    public long getMainThreadId() {
        return mMainThreadId;
    }

    /**
     * push the activity to activities stack.
     *
     * @param activity the current foreground activity.
     */
    public void registerActivity(PkiActivity activity) {
        if (!mActivities.contains(activity)) {
            mActivities.add(activity);
        }
        mForegroundActivity = activity;
    }

    /**
     * pop the activity from the running activity stack
     *
     * @param activity
     */
    public void unregisterActivity(PkiActivity activity) {
        mActivities.remove(activity);
        if (mForegroundActivity == activity) {
            mForegroundActivity = null;
        }
    }

    /**
     * An activity comes to foreground.
     */
    public void activityToForeground(PkiActivity activity) {
        mForegroundActivity = activity;
    }

    /**
     * get current foreground activity.
     */
    public PkiActivity getForegroundActivity() {
        return mForegroundActivity;
    }

    /**
     * post the process on main thread to execute it.
     *
     * @param r The Runnable that will be executed.
     */
    public void postToMainThread(Runnable r) {
        mHandler.post(r);
    }

    /**
     * register the home activity
     *
     * @param home
     */
    public void registerHomeActivity(PkiActivity home) {
        mHomeActivity = home;
    }

    /**
     * estimate whether the activity is home activity
     *
     * @param activity
     * @return
     */
    public boolean isHomeActivity(Activity activity) {
        return activity == mHomeActivity;
    }

    /**
     * post the process on main thread to execute it.
     *
     * @param r           The Runnable that will be executed.
     * @param delayMillis The delay (in milliseconds) until the Runnable will be
     *                    executed.
     */
    public void postToMainThread(Runnable r, long delayMillis) {
        mHandler.postDelayed(r, delayMillis);
    }

    /**
     * post the process on a non-main thread to execute it.
     *
     * @param r The Runnable that will be executed.
     */
    public void postToNonMainThread(Runnable r) {
        mExecutorService.submit(r);
    }
}
