package com.shen.stephen.utilplatform.widget.dialog;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;


public abstract class BaseDialog extends DialogFragment {
	public static final String ARGS_KEY_TITLE = "com.shen.stephen.utilplatform.widget.dialog.ARGS_KEY_TITLE";
	public static final String ARGS_KEY_DEFAULT_POSITION = "com.shen.stephen.utilplatform.widget.dialog.ARGS_KEY_DEFAULT_POSITION";
	public static final String ARGS_KEY_MESSAGE = "com.shen.stephen.utilplatform.widget.dialog.ARGS_KEY_MESSAGE";
	public static final String ARGS_KEY_LEFT_BUTTON = "com.shen.stephen.utilplatform.widget.dialog.ARGS_KEY_LEFT_BUTTON";
	public static final String ARGS_KEY_RIGHT_BUTTON = "com.shen.stephen.utilplatform.widget.dialog.ARGS_KEY_RIGHT_BUTTON";

	protected DialogPresentInterface.DialogButtonClickListener mBtnClickListener;

	protected DialogPresentInterface.DialogDismissListener mDismissListener;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		setRetainInstance(true);
		super.onCreate(savedInstanceState);
	}

	@Override
	public void onDestroyView() {
		Dialog dialog = getDialog();

		// Work around bug:
		// http://code.google.com/p/android/issues/detail?id=17423
		if ((dialog != null) && getRetainInstance())
			dialog.setDismissMessage(null);
		super.onDestroyView();
	}

	/**
	 * Set the dialog button clicked listener.
	 * 
	 * @param clickListener
	 *            the DialogButtonClickListener
	 */
	public void setDialogButtonClickListner(
			DialogPresentInterface.DialogButtonClickListener clickListener) {
		mBtnClickListener = clickListener;
	}

	/**
	 * Set the dialog dismiss listener.
	 * 
	 * @param dismissListener
	 *            the dialog dismiss listener.
	 */
	public void setDialogDismissListener(DialogPresentInterface.DialogDismissListener dismissListener) {
		mDismissListener = dismissListener;
	}

	@Override
	public void onDismiss(DialogInterface dialog) {
		super.onDismiss(dialog);
		if (mDismissListener != null) {
			mDismissListener.onDialogDismiss(dialog);
		}
	}
}
