package com.shen.stephen.utilplatform;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Point;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;

import com.shen.stephen.utilplatform.widget.PkiFragment;
import com.shen.stephen.utilplatform.widget.PkiActivity;
import com.shen.stephen.utilplatform.widget.view.NavigationController;
import com.shen.stephen.utilplatform.util.StrUtil;

/**
 * The tablet Compatible utility class
 */
public class TabletCompat {
    private TabletCompat() {
        // Avoid construct outside.
    }

    /**
     * The utility method to start a new screen, using this method you do not
     * need to concerned whether current device is a tablet or not. It will
     * start a new activity if the device is phone. or add the new started
     * screen to the right pane if the device is tablet.
     *
     * @param currentScreen the current screen Activity instance.
     * @param nextActivity  the next activity that will started, maybe null, if this set
     *                      to null the {@code nextFragment} must not be null, or it will
     *                      throw a {@linkplain IllegalArgumentException}
     * @param nextFragment  the next fragment that will started
     * @param args          the arguments that will pass to the next activity or fragment.
     * @param tag           the tag of fragment, if next screen is a fragment.
     * @param title         the title of next screen.
     * @param flags         the start mode of the next screen. -1 specify no flags or use
     *                      default flags.
     * @param requestCode   the request code it will be returned when activity or fragment
     *                      is finished.
     * @param listener      the fragment finish listener.
     * @see {@linkplain Intent#FLAG_ACTIVITY_CLEAR_TASK},
     * {@link Intent#FLAG_ACTIVITY_CLEAR_TOP} and etc.
     * <p/>
     * Or
     * @see {@link NavigationController#PUSH_MODE_SINGLE_TOP},
     * {@link NavigationController#PUSH_MODE_NORMAL},
     * {@link NavigationController#PUSH_MODE_SINGLE_INSTANCE}
     */
    public static void startNextScreen(Activity currentScreen, PkiFragment currentFragment,
                                       Class<? extends PkiActivity> nextActivity,
                                       Class<? extends PkiFragment> nextFragment, Bundle args,
                                       String tag, String title, int flags, boolean isForResult, int requestCode, PkiFragment.OnFinishListener listener) {
        if (currentScreen == null) {
            throw new IllegalArgumentException(
                    "You must provide current screen!");
        }

        if (nextActivity == null && nextFragment == null) {
            throw new IllegalArgumentException(
                    "You must provide one of next screen component (Activity or fragment)!");
        }

        if (!(currentScreen instanceof ActivityInterface)) {
            throw new IllegalArgumentException(
                    "The current screen must be a TabletCompatInterface!");
        }

        final ActivityInterface tabletInterface = (ActivityInterface) currentScreen;

        // If current activity is not support multi-Pane and only provides
        // fragment, then just add the fragment to current screen.
        if (!tabletInterface.isInMultiPane() && nextActivity == null) {
            PkiFragment fragment = generateFragment(currentScreen,
                    nextFragment, args);
            FragmentTransaction ft = tabletInterface.getFragmentTransaction();
            ft.add(fragment, null);
            ft.addToBackStack(null);
            ft.commit();
            return;
        }

        // if not provide fragment just start a new activity silently.
        if (nextFragment == null) {
            startNewActivity(currentScreen, nextActivity, args, flags);
            return;
        }

        if (tabletInterface.isInMultiPane()) {
            PkiFragment fragment = generateFragment(currentScreen,
                    nextFragment, args);
            fragment.setOnFinishListener(listener);
            fragment.setRequestCode(requestCode);
            if (tabletInterface.getNavigationController() != null) {

                // if not specify flags use default flags
                if (flags == -1) {
                    flags = NavigationController.PUSH_MODE_NORMAL;
                }

                tabletInterface.getNavigationController().pushFragment(
                        fragment, tag, StrUtil.strNotNull(title), flags);
            } else {
                // if not support navigation controller in right pane, just
                // replace the right pane silently.
                FragmentTransaction ft = tabletInterface
                        .getFragmentTransaction();
                ft.replace(tabletInterface.getRightPaneContainerId(), fragment);
                ft.addToBackStack(tag);
                ft.commit();
            }

        } else {
            if (args != null && !StrUtil.isEmpty(title)) {
                args.putString(PkiActivity.TITLE_INTENT_KEY, title);
            }
            if (isForResult) {
                startNewActivityForResult(currentScreen, currentFragment, nextActivity, requestCode, args, flags);
            } else {
                startNewActivity(currentScreen, nextActivity, args, flags);
            }
        }
    }

    public static void startNextScreen(Activity currentScreen, Class<? extends PkiActivity> nextActivity,
                                       Class<? extends PkiFragment> nextFragment, Bundle args,
                                       String tag, String title, int flags) {
        startNextScreen(currentScreen, null, nextActivity, nextFragment, args, tag, title, flags, false, -1, null);
    }

    /**
     * The utility method to start a new screen, using this method you do not
     * need to concerned whether current device is a tablet or not. It will
     * start a new activity if the device is phone. or add the new started
     * screen to the right pane if the device is tablet.
     *
     * @param currentScreen the current screen Activity instance.
     * @param nextActivity  the next activity that will started, maybe null, if this set
     *                      to null the {@code nextFragment} must not be null, or it will
     *                      throw a {@linkplain IllegalArgumentException}
     * @param nextFragment  the next fragment that will started
     * @param args          the arguments that will pass to the next activity or fragment.
     * @param tag           the tag of fragment, if next screen is a fragment.
     * @param titleResId    the title resource id of next screen. e.g: R.string.foo.
     * @param flags         the start mode of the next screen. -1 specify no flags or use
     *                      default flags.
     * @see {@linkplain Intent#FLAG_ACTIVITY_CLEAR_TASK},
     * {@link Intent#FLAG_ACTIVITY_CLEAR_TOP} and etc.
     * <p/>
     * Or
     * @see {@link NavigationController#PUSH_MODE_SINGLE_TOP},
     * {@link NavigationController#PUSH_MODE_NORMAL},
     * {@link NavigationController#PUSH_MODE_SINGLE_INSTANCE}
     */
    public static void startNextScreen(Activity currentScreen,
                                       Class<? extends PkiActivity> nextActivity,
                                       Class<? extends PkiFragment> nextFragment, Bundle args,
                                       String tag, int titleResId, int flags) {
        String title = titleResId == View.NO_ID ? StrUtil.EMPTYSTRING
                : currentScreen.getString(titleResId);
        startNextScreen(currentScreen, null, nextActivity, nextFragment, args, tag,
                title, flags, false, -1, null);
    }

    /**
     * Call
     * {@link TabletCompat#startNextScreen(Activity, Class, Class, Bundle, String, int, int)}
     * with no flags.
     */
    public static void startNextScreen(Activity currentScreen,
                                       Class<? extends PkiActivity> nextActivity,
                                       Class<? extends PkiFragment> nextFragment, Bundle args,
                                       String tag, int titleResId) {
        startNextScreen(currentScreen, nextActivity, nextFragment, args, tag,
                titleResId, -1);
    }

    /**
     * Call
     * {@link TabletCompat#startNextScreen(Activity, Class, Class, Bundle, String, int, int)}
     * with no tag and no title resource id.
     */
    public static void startNextScreen(Activity currentScreen,
                                       Class<? extends PkiActivity> nextActivity,
                                       Class<? extends PkiFragment> nextFragment, Bundle args, int flags) {
        startNextScreen(currentScreen, nextActivity, nextFragment, args, null,
                View.NO_ID, flags);
    }

    /**
     * Call
     * {@link TabletCompat#startNextScreen(Activity, Class, Class, Bundle, String, int, int)}
     * with no tag , no title resource id and no flags.
     */
    public static void startNextScreen(Activity currentScreen,
                                       Class<? extends PkiActivity> nextActivity,
                                       Class<? extends PkiFragment> nextFragment, Bundle args) {
        startNextScreen(currentScreen, nextActivity, nextFragment, args, null,
                View.NO_ID, -1);
    }

    /**
     * The utility method to start a new screen, using this method you do not
     * need to concerned whether current device is a tablet or not. It will
     * start a new activity if the device is phone. or add the new started
     * screen to the right pane if the device is tablet.
     *
     * @param currentScreen the current screen Activity instance.
     * @param nextActivity  the next activity that will started, maybe null, if this set
     *                      to null the {@code nextFragment} must not be null, or it will
     *                      throw a {@linkplain IllegalArgumentException}
     * @param nextFragment  the next fragment that will started
     * @param args          the arguments that will pass to the next activity or fragment.
     * @param tag           the tag of fragment, if next screen is a fragment.
     * @param titleResId    the title resource id of next screen. e.g: R.string.foo.
     * @param flags         the start mode of the next screen. -1 specify no flags or use
     *                      default flags.
     * @param requestCode   the request code it will be returned when activity or fragment
     *                      is finished.
     * @param listener      the fragment finish listener.
     * @see {@linkplain Intent#FLAG_ACTIVITY_CLEAR_TASK},
     * {@link Intent#FLAG_ACTIVITY_CLEAR_TOP} and etc.
     * <p/>
     * Or
     * @see {@link NavigationController#PUSH_MODE_SINGLE_TOP},
     * {@link NavigationController#PUSH_MODE_NORMAL},
     * {@link NavigationController#PUSH_MODE_SINGLE_INSTANCE}
     */
    public static void startNextScreenForResult(Activity currentScreen, PkiFragment currentFragment,
                                                Class<? extends PkiActivity> nextActivity,
                                                Class<? extends PkiFragment> nextFragment, Bundle args,
                                                String tag, int titleResId, int flags, int requestCode, PkiFragment.OnFinishListener listener) {
        String title = titleResId == View.NO_ID ? StrUtil.EMPTYSTRING
                : currentScreen.getString(titleResId);
        startNextScreen(currentScreen, currentFragment, nextActivity, nextFragment, args, tag,
                title, flags, true, requestCode, listener);
    }

    public static void startNextScreenForResult(Activity currentScreen, PkiFragment currentFragment,
                                                Class<? extends PkiActivity> nextActivity,
                                                Class<? extends PkiFragment> nextFragment, Bundle args,
                                                String tag, int titleResId, int requestCode, PkiFragment.OnFinishListener listener) {
        startNextScreenForResult(currentScreen, currentFragment, nextActivity, nextFragment, args, tag, titleResId, -1, requestCode, listener);
    }

    private static PkiFragment generateFragment(Activity currentScreen,
                                                 Class<? extends PkiFragment> fragmentClazz, Bundle arguments) {
        PkiFragment fragment = (PkiFragment) PkiFragment.instantiate(
                currentScreen, fragmentClazz.getName(), arguments);
        return fragment;
    }

    private static void startNewActivity(Activity currentScreen,
                                         Class<? extends PkiActivity> nextActivity, Bundle args, int flags) {
        Intent intent = new Intent(currentScreen, nextActivity);
        if (args != null) {
            intent.putExtras(args);
        }

        if (flags != -1) {
            intent.setFlags(flags);
        }

        currentScreen.startActivity(intent);
    }

    private static void startNewActivityForResult(Activity currentScreen,
                                                  PkiFragment currentFragment,
                                                  Class<? extends PkiActivity> nextActivity, int requestCode,
                                                  Bundle args, int flags) {
        Intent intent = new Intent(currentScreen, nextActivity);
        if (args != null) {
            intent.putExtras(args);
        }

        if (flags != -1) {
            intent.setFlags(flags);
        }

        if (currentFragment != null) {
            currentFragment.startActivityForResult(intent, requestCode);
        } else {
            currentScreen.startActivityForResult(intent, requestCode);
        }
    }

    /**
     * Show an activity as a modal view.
     *
     * @param activity the activity that will show.
     */
    public static void showAsModalView(Activity activity) {
        // To show activity as dialog and dim the background, you need to
        // declare android:theme="@style/PopupTheme" on for the chosen activity
        // on the manifest
        activity.getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_DIM_BEHIND,
                WindowManager.LayoutParams.FLAG_DIM_BEHIND);

        // Get screen size of current device
        Point size = new Point();
        activity.getWindowManager().getDefaultDisplay().getSize(size);

        LayoutParams params = activity.getWindow().getAttributes();
        // make the width and height of the modal view 70% of screen size
        params.height = size.y * 7 / 10;
        params.width = size.x * 7 / 10;
        params.alpha = 1.0f;
        params.dimAmount = 0.5f;
        activity.getWindow().setAttributes(
                (android.view.WindowManager.LayoutParams) params);
    }

    /**
     * Start a new screen from a modal view. The current screen view maybe shown
     * as a modal on a tablet, but not shown as a modal view on a phone.
     *
     * @param currentScreen the current screen, the view must be a modal view.
     * @param nextActivity  the next activity that will be started.
     * @param nextFragment  the next fragment that will be started.
     * @param args          the arguments for the next screen
     */
    public static void startScreenFromModalView(Activity currentScreen,
                                                Class<? extends PkiActivity> nextActivity,
                                                Class<? extends PkiFragment> nextFragment, Bundle args) {

        startScreenFromModalView(currentScreen, null, nextActivity,
                nextFragment, args, false, 0, null);
    }

    /**
     * Start a new screen from a modal view. this is same as
     * {@link Activity#startActivityForResult(Intent, int, Bundle)}. The current
     * screen view maybe shown as a modal on a tablet, but not shown as a modal
     * view on a phone.
     *
     * @param currentScreen   the current screen, the view must be a modal view.
     * @param currentFragment the current fragment that in front page, maybe null.
     * @param nextActivity    the next activity that will be started.
     * @param nextFragment    the next fragment that will be started.
     * @param args            the arguments for the next screen
     * @param requestCode     the request code it will be returned when activity or fragment
     *                        is finished.
     * @param listener        the fragment finish listener.
     */
    public static void startScreenFromModalViewForResult(
            Activity currentScreen, PkiFragment currentFragment,
            Class<? extends PkiActivity> nextActivity,
            Class<? extends PkiFragment> nextFragment, Bundle args,
            int requestCode, PkiFragment.OnFinishListener listener) {
        startScreenFromModalView(currentScreen, currentFragment, nextActivity,
                nextFragment, args, true, requestCode, listener);
    }

    private static void startScreenFromModalView(Activity currentScreen,
                                                 PkiFragment currentFragment,
                                                 Class<? extends PkiActivity> nextActivity,
                                                 Class<? extends PkiFragment> nextFragment, Bundle args,
                                                 boolean isForResult, int requestCode, PkiFragment.OnFinishListener listener) {
        if (currentScreen == null) {
            throw new IllegalArgumentException(
                    "You must provide current screen and current modalview!");
        }

        if (nextActivity == null || nextFragment == null) {
            throw new IllegalArgumentException(
                    "You must provide one of next screen component (Activity or fragment)!");
        }

        if (!(currentScreen instanceof ModalViewInterface)) {
            throw new IllegalArgumentException(
                    "The current screen must be a ModalViewInterface!");
        }

        ModalViewInterface modalView = (ModalViewInterface) currentScreen;

        // If current screen is start as modal view, just add the next screen
        // fragment to current fragment stack.
        if (modalView.isStartAsModal()) {
            PkiFragment f = generateFragment(currentScreen, nextFragment, args);
            f.setRequestCode(requestCode);
            f.setOnFinishListener(listener);
            if (modalView.getRootFragmentContainerId() == View.NO_ID) {
                throw new IllegalArgumentException("Please override method getRootFragmentContainerId() in Activity " + modalView.getClass().getSimpleName() + " to return the fragment container id.");
            }
            modalView
                    .getFragmentTransaction()
                    .setCustomAnimations(R.animator.fragment_enter_animation,
                            R.animator.fragment_exit_animation,
                            R.animator.fragment_pop_enter_animation,
                            R.animator.fragment_pop_exit_animation)
                    .replace(modalView.getRootFragmentContainerId(), f)
                    .addToBackStack(null).commit();
        } else {
            if (isForResult) {
                startNewActivityForResult(currentScreen, currentFragment,
                        nextActivity, requestCode, args, -1);
            } else {
                startNewActivity(currentScreen, nextActivity, args, -1);
            }
        }
    }

    public interface TabletCompatInterface {
        /**
         * Check whether current device is a tablet or not.
         */
        public boolean isTablet();

        /**
         * Set whether the current activity is in multi-pane model or not.
         *
         * @param isMultiPane true if in multi-pane model. otherwise false.
         */
        public void setIsMultiPane(boolean isMultiPane);

        /**
         * Check current activity whether in multi-pane model or not.
         *
         * @return true if in multi-pane, otherwise false.
         */
        public boolean isInMultiPane();
    }

    public interface ActivityInterface extends TabletCompatInterface {

        /**
         * Get the right pane's navigation controller if current activity is in
         * multi-pane mode. Return null if it is not in multi-Pane model or not
         * support navigation controller in right pane.
         */
        public NavigationController getNavigationController();

        /**
         * Get the right pane container id. if current activity is not support
         * multi-pane return {@link View#NO_ID}.
         */
        public int getRightPaneContainerId();

        /**
         * Get the root fragment container id
         */
        public int getRootFragmentContainerId();

        /**
         * Get fragment transaction.
         */
        public FragmentTransaction getFragmentTransaction();
    }

    /**
     * The modal view interface definition for some activity show as a model
     * view on tablet
     */
    public interface ModalViewInterface extends ActivityInterface {
        /**
         * Check whether current activity show as a modal view in the tablet.
         */
        public boolean isStartAsModal();
    }
}
