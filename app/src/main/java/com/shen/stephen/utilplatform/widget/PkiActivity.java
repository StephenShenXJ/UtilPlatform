package com.shen.stephen.utilplatform.widget;

import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;
import android.widget.Toast;

import com.shen.stephen.utilplatform.BuildConfig;
import com.shen.stephen.utilplatform.PKIApplication;
import com.shen.stephen.utilplatform.R;
import com.shen.stephen.utilplatform.TabletCompat;
import com.shen.stephen.utilplatform.UIHandler;
import com.shen.stephen.utilplatform.log.PLog;
import com.shen.stephen.utilplatform.widget.dialog.BaseDialog;
import com.shen.stephen.utilplatform.widget.dialog.DialogBuilder;
import com.shen.stephen.utilplatform.widget.dialog.DialogPresentInterface;
import com.shen.stephen.utilplatform.widget.dialog.ListSelectorDialog;
import com.shen.stephen.utilplatform.widget.view.NavigationController;
import com.shen.stephen.utilplatform.util.StrUtil;

import java.util.ArrayList;

/**
 * A base activity of whole project, all of activities in this project should extends this class
 * Created by chengcn on 3/22/2016.
 */
public abstract class PkiActivity extends AppCompatActivity implements
        DialogPresentInterface, TabletCompat.ActivityInterface, Thread.UncaughtExceptionHandler {
    /**
     * The intent key for the title of the activity.
     */
    public static final String TITLE_INTENT_KEY = "com.shen.stephen.utilplatform.PkiActivity.TITLE_INTENT_KEY";

    private static final String DIALOG_TAG = "com.shen.stephen.utilplatform.PkiActivity:DIALOG_TAG";

    /**
     * The progress dialog instance.
     */
    private BaseDialog mDialog;

    private boolean mIsVisible = false;
    private boolean mIsMultiPane = false;
    private static boolean mIsTablet = !PKIApplication.getContext()
            .getResources().getBoolean(R.bool.portrait_only);

    private static short mSessionDepth = 0;
    // private Tracker tracker;
    private final static Thread.UncaughtExceptionHandler _defaultHandler = Thread
            .getDefaultUncaughtExceptionHandler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!PKIApplication.isAppLaunched()) {
            UIHandler.getInstance().postToMainThread(new Runnable() {
                @Override
                public void run() {
                    onAppRelaunch();
                }
            }, 1000);
        }

        if (savedInstanceState != null) {
            String dialogTag = savedInstanceState.getString(DIALOG_TAG, "");
            if (!StrUtil.isEmpty(dialogTag)) {
                mDialog = (BaseDialog) getSupportFragmentManager()
                        .findFragmentByTag(dialogTag);
            }
        }

        baseInit();
        // push current activity to the activities' stack
        UIHandler.getInstance().registerActivity(this);
        if (!BuildConfig.DEBUG) {
            // TODO uncomment when release
            //Thread.setDefaultUncaughtExceptionHandler(this);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mDialog != null) {
            //TODO uncomment when release
            //outState.putString(DIALOG_TAG, mDialog.getTag());
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (mSessionDepth == 0) {
            onApplicationGoesToForeground();
        }

        mSessionDepth++;
    }

    @Override
    protected void onResume() {
        super.onResume();
        mIsVisible = true;
        UIHandler.getInstance().activityToForeground(this);
    }

    @Override
    protected void onStop() {
        mIsVisible = false;
        super.onStop();
        if (mSessionDepth > 0)
            mSessionDepth--;
        if (mSessionDepth == 0) {
            onApplicationGoesToBackground();
        }
    }

    @Override
    protected void onDestroy() {
        // Pop the activity from the stack.
        UIHandler.getInstance().unregisterActivity(this);
        super.onDestroy();
    }

    /**
     * Return the activity whether is visible or not.
     */
    protected boolean isVisible() {
        return mIsVisible;
    }

    @Override
    public boolean isTablet() {
        return mIsTablet;
    }

    /**
     * Set whether the current activity is in multi-pane model or not.
     *
     * @param isMultiPane true if in multi-pane model. otherwise false.
     */
    @Override
    public void setIsMultiPane(boolean isMultiPane) {
        mIsMultiPane = isMultiPane;
    }

    /**
     * Check current activity whether in multi-pane model or not.
     *
     * @return true if in multi-pane, otherwise false.
     */
    @Override
    public boolean isInMultiPane() {
        return mIsMultiPane;
    }

    /**
     * Get the right pane's navigation controller if current activity is in
     * multi-pane mode. Return null if it is not in multi-Pane model or not
     * support navigation controller in right pane.
     */
    @Override
    public NavigationController getNavigationController() {
        return null;
    }

    @Override
    public int getRootFragmentContainerId() {
        return View.NO_ID;
    }

    /**
     * Get the right pane container id. if current activity is not support
     * multi-pane return {@link View#NO_ID}.
     */
    @Override
    public int getRightPaneContainerId() {
        return View.NO_ID;
    }

    @Override
    public FragmentTransaction getFragmentTransaction() {
        return getSupportFragmentManager().beginTransaction();
    }

    /**
     * Do base activity initialize.
     */
    private final void baseInit() {
        // Check whether support portrait and landscape, only support portrait
        // on a phone, support both portrait and landscape on a tablet
        if (!mIsTablet) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }

        if (getIntent().getExtras() != null) {
            String title = getIntent().getExtras().getString(TITLE_INTENT_KEY);
            if (!StrUtil.isEmpty(title) && isShowTitle()) {
                setTitle(title);
            }
        }

        Window window = this.getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.setStatusBarColor(0xffEAEAEA);
        }
    }

    /**
     * Check whether is show title or not.
     */
    protected boolean isShowTitle() {
        return true;
    }

    public void setTitle(int titleResId) {
        if (titleResId == View.NO_ID || titleResId == 0) {
            return;
        }
        setTitle(getString(titleResId));
    }

    public void setTitle(String title) {
        TextView titleView = (TextView) findViewById(android.R.id.text1);
        titleView.setText(title);
    }

    @Override
    public void showAlertDialog(int titleResId, int contentResId) {
        String title = titleResId == View.NO_ID || titleResId == 0 ? null
                : getString(titleResId);
        String content = contentResId == View.NO_ID || titleResId == 0 ? null
                : getString(contentResId);
        showAlertDialog(title, content);
    }

    @Override
    public void showAlertDialog(int titleResId, int contentResId,
                                Object... component) {
        String title = titleResId == View.NO_ID || titleResId == 0 ? null
                : getString(titleResId);
        String content = contentResId == View.NO_ID || titleResId == 0 ? null
                : getString(contentResId, component);
        showAlertDialog(title, content);
    }

    @Override
    public void showProgressDialog(int messageResId) {
        String msg = messageResId == View.NO_ID || messageResId == 0 ? null
                : getString(messageResId);
        showProgressDialog(msg);
    }

    @Override
    public void showProgressDialog(String message) {
        if (mDialog != null || isFinishing()) {
            return;
        }

        mDialog = DialogBuilder.buildProgressDialog(message);
        mDialog.setDialogDismissListener(new DialogDismissListener() {
            @Override
            public void onDialogDismiss(DialogInterface dialog) {
                mDialog = null;
            }
        });
        showFragmentDialog(mDialog, "ProgressDialog");
    }

    @Override
    public void dismissDialog() {
        if (mDialog != null && !isFinishing()) {
            if (this.isVisible()) {
                mDialog.dismiss();
            } else {
                mDialog.dismissAllowingStateLoss();
            }
            mDialog = null;
        }
    }

    @Override
    public void showNormalDialog(int titleResId, int contentId,
                                 int leftBtnResId, int rightBtnResId,
                                 DialogButtonClickListener listener) {
        if (isFinishing()) {
            return;
        }

        String title = titleResId == View.NO_ID || titleResId == 0 ? null
                : getString(titleResId);
        String content = contentId == View.NO_ID || titleResId == 0 ? null
                : getString(contentId);
        String leftBtn = leftBtnResId == View.NO_ID || titleResId == 0 ? getString(R.string.btn_no_text)
                : getString(leftBtnResId);
        String rightBtn = rightBtnResId == View.NO_ID || titleResId == 0 ? getString(R.string.btn_yes_text)
                : getString(rightBtnResId);

        showFragmentDialog(DialogBuilder.buildDialog(title, content, leftBtn,
                rightBtn, listener), "NormalDialog");
    }

    @Override
    public void showNormalDialog(int titleResId, int contentId,
                                 DialogButtonClickListener listener) {
        showNormalDialog(titleResId, contentId, R.string.btn_yes_text,
                R.string.btn_no_text, listener);
    }

    @Override
    public void showAlertDialog(String title, String content) {
        showAlertDialog(title, content, null);
    }

    @Override
    public void showAlertDialog(String title, String content,
                                DialogButtonClickListener listener) {
        if (isFinishing()) {
            return;
        }
        showFragmentDialog(
                DialogBuilder.buildAlertDialog(title, content, listener),
                "AlertDialog");
    }

    @Override
    public void showNormalDialog(String title, String content,
                                 int leftBtnResId, int rightBtnResId,
                                 DialogButtonClickListener listener) {
        if (isFinishing()) {
            return;
        }

        String leftBtn = leftBtnResId == View.NO_ID ? getString(R.string.btn_no_text) : getString(leftBtnResId);
        String rightBtn = rightBtnResId == View.NO_ID ? getString(R.string.btn_yes_text) : getString(rightBtnResId);

        showFragmentDialog(DialogBuilder.buildDialog(title, content, leftBtn,
                rightBtn, listener), "NormalDialog");
    }

    @Override
    public void showListSelectDialog(int titleResId,
                                     ArrayList<ListSelectorDialog.OptionItemData> selectItems, int defaultPosition,
                                     DialogItemSelectListener itemSelectListener) {
        if (selectItems == null || selectItems.isEmpty()) {
            return;
        }

        String title = titleResId == View.NO_ID || titleResId == 0 ? StrUtil.EMPTYSTRING
                : getString(titleResId);
        showListSelectDialog(title, selectItems, defaultPosition, itemSelectListener);
    }

    @Override
    public void showListSelectDialog(String title, ArrayList<ListSelectorDialog.OptionItemData> selectItems, int defaultPosition, DialogItemSelectListener itemSelectListener) {
        showFragmentDialog(DialogBuilder.buildListSelectDialog(title, selectItems, defaultPosition, itemSelectListener), "ListSelectDialog");
    }

    @Override
    public void showToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
    }

    @Override
    public void showToast(int msgResId) {
        Toast.makeText(this, msgResId, Toast.LENGTH_LONG).show();
    }

    /**
     * Lock screen orientation.
     */
    public void lockScreenOrientation() {
        // Ignored if running on the phone.
        if (getResources().getBoolean(R.bool.portrait_only)) {
            return;
        }

        int currentOrientation = getResources().getConfiguration().orientation;
        if (currentOrientation == Configuration.ORIENTATION_PORTRAIT) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
        } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
        }
    }

    /**
     * Unlock screen orientation.
     */
    public void unlockScreenOrientation() {
        // Ignored if running on the phone.
        if (getResources().getBoolean(R.bool.portrait_only)) {
            return;
        }

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
    }

    /**
     * Show a dialog, using this method to avoid app crashes when show a dialog
     * that the activity isn't in foreground.
     *
     * @param dialog the dialog that will be shown.
     * @param tag    The tag for this dialog.
     */
    public void showFragmentDialog(DialogFragment dialog, String tag) {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.add(dialog, tag);
        ft.commitAllowingStateLoss();
    }

    @SuppressWarnings("deprecation")
    @Override
    public void uncaughtException(Thread thread, Throwable ex) {
        onUncaughtException(thread, ex);
        _defaultHandler.uncaughtException(thread, ex);
    }

    /**
     * Called when a uncaught exception has been thrown
     *
     * @param thread the thread that throw the exception
     * @param ex     the uncaught exception.
     */
    protected void onUncaughtException(Thread thread, Throwable ex) {
    }

    /**
     * Hide Soft Keyboard
     *
     * @param focusView the focused view
     */
    protected void hideSoftKeyboard(View focusView) {
        InputMethodManager imm = (InputMethodManager) this
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm.isActive()) {
            imm.hideSoftInputFromWindow(focusView.getWindowToken(), 0);
        }
    }

    @Override
    public void showAlertDialog(int titleResId, int contentResId,
                                DialogButtonClickListener listener) {
        String title = titleResId == View.NO_ID || titleResId == 0 ? null
                : getString(titleResId);
        String content = contentResId == View.NO_ID || titleResId == 0 ? null
                : getString(contentResId);
        showAlertDialog(title, content, listener);

    }

    /**
     * The callback method while the application goes to background.
     */
    protected void onApplicationGoesToBackground() {
        PLog.i("OneSource", "Application goes to background.");
    }

    /**
     * The callback method while the application goes to foreground.
     */
    protected void onApplicationGoesToForeground() {
        PLog.i("OneSource", "Application goes to foreground.");
    }

    /**
     * If the app was not launched relaunch App. this will happened when the process is killed by Android system.
     * And user trying to relaunch the App
     */
    protected void onAppRelaunch() {

    }
}
