package com.shen.stephen.utilplatform.widget;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.shen.stephen.utilplatform.PKIApplication;
import com.shen.stephen.utilplatform.widget.dialog.DialogPresentInterface;
import com.shen.stephen.utilplatform.widget.dialog.ListSelectorDialog;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * A base fragment of the whole project, all of fragments in this project should extend this class
 * Created by chengcn on 3/22/2016.
 */
public abstract class PkiFragment extends Fragment implements DialogPresentInterface {
    protected View mContentView;
    protected Context mContext;

    protected int mRequestCode;
    protected int mResultCode = Activity.RESULT_CANCELED;
    private Intent mResultData = null;
    protected OnFinishListener mFinishListener = null;

    protected int rootHeight;
    protected ViewTreeObserver.OnGlobalLayoutListener mGlobalLayoutListener = new ViewTreeObserver.OnGlobalLayoutListener() {

        @Override
        public void onGlobalLayout() {
            if (mContentView != null) {
                int height = mContentView.getHeight();
                if (height < rootHeight) {
                    // when keyboard close
                    onShowKeyboard();

                } else if (height > rootHeight) {
                    // when keyboard open
                    onHideKeyboard();
                }
                rootHeight = height;

            }

        }
    };

    /**
     * Called when keyboard goes to hide
     */
    protected void onHideKeyboard() {
    }

    /**
     * Called when the keyboard goes to show
     */
    protected void onShowKeyboard() {
    }

    protected void attachKeyboardListener() {
        if (mContentView != null) {
            rootHeight = mContentView.getHeight();
            mContentView.getViewTreeObserver().addOnGlobalLayoutListener(
                    mGlobalLayoutListener);
        }
    }

    /**
     * Interface used to allow the creator to run some code when finish a
     * fragment.
     */
    public interface OnFinishListener {

        /**
         * Callback method when finish edit the text.
         *
         * @param requestCode the request code
         * @param resultCode  the result code same as {@link Activity#onActivityResult},
         *                    one of {@link Activity#RESULT_OK} and
         *                    {@link Activity#RESULT_CANCELED}.
         * @param resultData  the result data.
         */
        public void onFinishEdit(int requestCode, int resultCode,
                                 Intent resultData);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            mRequestCode = savedInstanceState
                    .getInt("PkiFragment:RequestCode");
            mResultCode = savedInstanceState.getInt("PkiFragment:ResultCode");
            mResultData = savedInstanceState
                    .getParcelable("PkiFragment:ResultData");
        }
    }

    /**
     * Set result code and result data. The result code and result data will
     * returned through
     * {@link OnFinishListener##onFinishEdit(int, int, Object...)}. at same time
     * it will call parent activity's
     * {@linkplain Activity#setResult(int, Intent)}
     *
     * @param resultCode
     */
    public void setResult(int resultCode, Intent resultData) {
        mResultCode = resultCode;
        mResultData = resultData;
        getActivity().setResult(resultCode, resultData);
    }

    /**
     * Set the request code. this is similar as the resultCode when
     * {@linkplain Activity#startActivityForResult(Intent, int)}.
     *
     * @param requestCode the request code, this code will be returned
     *                    {@link OnFinishListener#onFinishEdit(int, int, Intent)}.
     */
    public void setRequestCode(int requestCode) {
        mRequestCode = requestCode;
    }

    /**
     * set the fragment finish listener.
     *
     * @param listener
     */
    public void setOnFinishListener(OnFinishListener listener) {
        mFinishListener = listener;
    }

    @Override
    public void onDestroy() {
        if (mFinishListener != null) {
            mFinishListener
                    .onFinishEdit(mRequestCode, mResultCode, mResultData);
        }
        super.onDestroy();
    }

    @Override
    public final View onCreateView(LayoutInflater inflater,
                                   ViewGroup container, Bundle savedInstanceState) {

        if (isReuseRootView() && mContentView != null) {
            ViewParent parent = mContentView.getParent();
            if (parent instanceof ViewGroup) {
                ViewGroup parentView = (ViewGroup) parent;
                parentView.removeView(mContentView);
            } else if (parent != null && !(parent instanceof ViewGroup)) {
                mContentView = inflater.inflate(getContentViewResourceId(), container, false);
            }

        } else {
            mContentView = inflater.inflate(getContentViewResourceId(), container, false);
        }

        attachKeyboardListener();
        init(savedInstanceState);
        return mContentView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("PkiFragment:RequestCode", mRequestCode);
        outState.putInt("PkiFragment:ResultCode", mResultCode);
        outState.putParcelable("PkiFragment:ResultData", mResultData);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mContext = getActivity();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mContext = null;
        try {
            Field childFragmentManager = Fragment.class
                    .getDeclaredField("mChildFragmentManager");
            childFragmentManager.setAccessible(true);
            childFragmentManager.set(this, null);

        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public Context getContext() {
        if (mContext == null) {
            return PKIApplication.getContext();
        }

        return mContext;
    }

    /**
     * Specify whether allow reuse the root view when fragment recall
     * {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)}, default is true.
     *
     * @return true if allow reuse root view
     */
    protected boolean isReuseRootView() {
        return true;
    }

    protected void hideSoftKeyboard(View focusView) {
        if (focusView == null) {
            return;
        }

        InputMethodManager imm = (InputMethodManager) getContext()
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm.isActive()) {
            imm.hideSoftInputFromWindow(focusView.getWindowToken(), 0);
        }
    }

    /**
     * Get root view's resource id, e.g:R.layout.foo.
     *
     * @return
     */
    protected abstract int getContentViewResourceId();

    /**
     * Do initialize after parent activity was created.
     *
     * @param savedInstanceState If non-null, this fragment is being re-constructed from a
     *                           previous saved state as given here.
     */
    protected abstract void init(Bundle savedInstanceState);

    /**
     * Look for a child view with the given id. If this view has the given id,
     * return this view.
     *
     * @param resId id The id to search for.
     * @return The view that has the given id in the hierarchy or null
     */
    public View findViewById(int resId) {
        if (mContentView == null) {
            return null;
        }

        return mContentView.findViewById(resId);
    }

    /**
     * Called when the fragment has detected the user's press of the back key.
     * The default implementation simply calls the current activity's
     * {@link Activity#onBackPressed()} method.
     */
    public void onBackClicked() {
        getActivity().onBackPressed();
    }

    @Override
    public void showAlertDialog(int titleResId, int contentResId) {
        DialogPresentInterface dialogPresent = ((DialogPresentInterface) getActivity());
        if (dialogPresent != null && !isDetached()) {
            dialogPresent.showAlertDialog(titleResId, contentResId);
        }
    }

    @Override
    public void showAlertDialog(int titleResId, int contentResId,
                                Object... component) {
        DialogPresentInterface dialogPresent = ((DialogPresentInterface) getActivity());
        if (dialogPresent != null && !isDetached()) {
            dialogPresent.showAlertDialog(titleResId, contentResId, component);
        }
    }

    @Override
    public void showAlertDialog(String title, String content) {
        showAlertDialog(title, content, null);
    }

    @Override
    public void showAlertDialog(String title, String content,
                                DialogPresentInterface.DialogButtonClickListener listener) {
        DialogPresentInterface dialogPresent = ((DialogPresentInterface) getActivity());
        if (dialogPresent != null && !isDetached()) {
            dialogPresent.showAlertDialog(title, content, listener);
        }
    }

    @Override
    public void showAlertDialog(int titleResId, int contentResId,
                                DialogPresentInterface.DialogButtonClickListener listener) {
        String title = mContext.getString(titleResId);
        String content = mContext.getString(contentResId);
        showAlertDialog(title, content, listener);
    }

    @Override
    public void showProgressDialog(int messageResId) {
        DialogPresentInterface dialogPresent = ((DialogPresentInterface) getActivity());
        if (dialogPresent != null && !isDetached()) {
            dialogPresent.showProgressDialog(messageResId);
        }
    }

    @Override
    public void showProgressDialog(String message) {
        DialogPresentInterface dialogPresent = ((DialogPresentInterface) getActivity());
        if (dialogPresent != null && !isDetached()) {
            dialogPresent.showProgressDialog(message);
        }
    }

    @Override
    public void dismissDialog() {
        DialogPresentInterface dialogPresent = ((DialogPresentInterface) getActivity());
        if (dialogPresent != null && !isDetached()) {
            dialogPresent.dismissDialog();
        }
    }

    @Override
    public void showNormalDialog(int titleResId, int contentId, int leftBtn,
                                 int rightBtn, DialogPresentInterface.DialogButtonClickListener listener) {
        DialogPresentInterface dialogPresent = ((DialogPresentInterface) getActivity());
        if (dialogPresent != null && !isDetached()) {
            dialogPresent.showNormalDialog(titleResId, contentId, leftBtn,
                    rightBtn, listener);
        }
    }

    @Override
    public void showNormalDialog(int titleResId, int contentId,
                                 DialogPresentInterface.DialogButtonClickListener listener) {
        DialogPresentInterface dialogPresent = ((DialogPresentInterface) getActivity());
        if (dialogPresent != null && !isDetached()) {
            dialogPresent.showNormalDialog(titleResId, contentId, listener);
        }
    }

    @Override
    public void showNormalDialog(String title, String content, int leftBtn,
                                 int rightBtn, DialogPresentInterface.DialogButtonClickListener listener) {
        DialogPresentInterface dialogPresent = ((DialogPresentInterface) getActivity());
        if (dialogPresent != null && !isDetached()) {
            dialogPresent.showNormalDialog(title, content, leftBtn, rightBtn,
                    listener);
        }
    }

    @Override
    public void showListSelectDialog(int titleResId, ArrayList<ListSelectorDialog.OptionItemData> selectItems, int defaultPosition, DialogPresentInterface.DialogItemSelectListener itemSelectListener) {
        DialogPresentInterface dialogPresent = ((DialogPresentInterface) getActivity());
        if (dialogPresent != null && !isDetached()) {
            dialogPresent.showListSelectDialog(titleResId, selectItems, defaultPosition, itemSelectListener);
        }
    }

    @Override
    public void showListSelectDialog(String title, ArrayList<ListSelectorDialog.OptionItemData> selectItems, int defaultPosition, DialogPresentInterface.DialogItemSelectListener itemSelectListener) {
        DialogPresentInterface dialogPresent = ((DialogPresentInterface) getActivity());
        if (dialogPresent != null && !isDetached()) {
            dialogPresent.showListSelectDialog(title, selectItems, defaultPosition, itemSelectListener);
        }
    }

    @Override
    public void showToast(String msg) {
        Toast.makeText(getContext(), msg, Toast.LENGTH_LONG).show();
    }

    @Override
    public void showToast(int msgResId) {
        Toast.makeText(getContext(), msgResId, Toast.LENGTH_LONG).show();
    }

    /**
     * The communication listener between fragment and activity.
     */
    public interface OnFragmentComponentClickListener {
        /**
         * The callback method when click a fragment component.
         *
         * @param currentFragment the current fragment
         * @param clickedView     the clicked view component
         * @param eventKey        The communication event key between activity and fragment.
         * @param data            the data
         */
        void onComponentClicked(PkiFragment currentFragment,
                                View clickedView, String eventKey, Object... data);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        List<Fragment> mFragments = getChildFragmentManager().getFragments();
        if (mFragments != null) {
            for (Fragment f : mFragments) {
                f.onActivityResult(requestCode, resultCode, data);
            }
        }
    }
}
