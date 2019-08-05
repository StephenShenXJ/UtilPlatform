/**
 *
 * DialogPresentInterface.java
 * PIVOT
 * Copyright @2014 PerkinElmer. All rights reserved.
 * */
package com.shen.stephen.utilplatform.widget.dialog;

import android.content.DialogInterface;

import java.util.ArrayList;

public interface DialogPresentInterface {
	/**
	 * 
	 * The dialog's button clicked event listener.
	 *
	 */
	interface DialogButtonClickListener {
		/**
		 * The callback method when the left button was clicked.
		 * 
		 * @param dialog
		 *            the dialog that was shown.
		 * @param data
		 *            maybe null, the data passed from the dialog.
		 */
		void onClickLeftButton(DialogInterface dialog, Object data);

		/**
		 * The callback method when the right button was clicked.
		 * 
		 * @param dialog
		 *            the dialog that was shown.
		 * @param data
		 *            maybe null, the data passed from the dialog.
		 */
		void onClickRightButton(DialogInterface dialog, Object data);

		/**
		 * The callback method when the confirm(OK) button was clicked in the
		 * Alert dialog
		 * 
		 * @param dialog
		 *            the dialog that was shown
		 * @param data
		 *            maybe null, the data passed from the dialog.
		 */
		void onClickConfirmButton(DialogInterface dialog, Object data);
	}

	/**
	 * Interface definition for a callback to be invoked when a item was
	 * selected.
	 *
	 */
	interface DialogItemSelectListener {
		/**
		 * The callback method when a item was selected.
		 * 
		 * @param dialog
		 *            the dialog that was shown
		 * @param selectedObj
		 *            the selected object.
		 * @param position
		 *            the selected position.
		 */
		void onSelectItem(DialogInterface dialog, Object selectedObj,
								 int position);
	}

	/**
	 * Interface definition for a callback to be invoked when the dialog is
	 * dismissed.
	 *
	 */
	interface DialogDismissListener {

		/**
		 * Called when the dialog is dismissed.
		 * 
		 * @param dialog
		 *            the dialog that is dismissed.
		 */
		void onDialogDismiss(DialogInterface dialog);
	}

	/**
	 * Show an alert dialog. the dialog contain a title, content and a 'OK' button
	 * button.
	 * 
	 * @param titleResId
	 *            The title string resource id, e.g:R.string.foo
	 * @param contentResId
	 *            The dialog content string resource id.
	 */
	void showAlertDialog(int titleResId, int contentResId);

	/**
	 * Show an alert dialog. the dialog contain a title, content and a 'OK' button
	 * button.
	 * 
	 * @param titleResId
	 *            The title string resource id, e.g:R.string.foo
	 * @param contentResId
	 *            The dialog content string resource id.
	 */
	void showAlertDialog(int titleResId, int contentResId,
								Object... component);

	void showAlertDialog(String title, String content);

	void showAlertDialog(String title, String content,
								DialogButtonClickListener listener);
	
	void showAlertDialog(int titleResId, int contentResId, DialogButtonClickListener listener);

	/**
	 * show a normal dialog that contain title, content and two buttons.
	 * 
	 * @param titleResId
	 *            The title string resource id, e.g:R.string.foo
	 * @param contentId
	 *            The dialog content string resource id.
	 * @param leftBtn
	 *            the left button title string resource id
	 * @param rightBtn
	 *            the right button title string resource id.
	 * @param listener
	 *            the button clicked listener.
	 */
	void showNormalDialog(int titleResId, int contentId, int leftBtn, int rightBtn, DialogButtonClickListener listener);

	/**
	 * show a normal dialog that contain title, content and two buttons.
	 * 
	 * @param titleResId
	 *            The title string resource id, e.g:R.string.foo
	 * @param contentId
	 *            The dialog content string resource id.
	 * @param leftBtn
	 *            the left button title string resource id
	 * @param rightBtn
	 *            the right button title string resource id.
	 * @param listener
	 *            the button clicked listener.
	 */
	void showNormalDialog(String titleResId, String contentId, int leftBtn, int rightBtn, DialogButtonClickListener listener);

	/**
	 * show a normal dialog that contain title, content and two buttons. the
	 * title of the buttons are "Yes"(right button) and "No"(left button).
	 * 
	 * @param titleResId
	 *            The title string resource id, e.g:R.string.foo
	 * @param contentId
	 *            The dialog content string resource id.
	 * @param listener
	 *            the button clicked listener.
	 */

	void showNormalDialog(int titleResId, int contentId, DialogButtonClickListener listener);

	/**
	 * show a progress dialog.
	 * 
	 * @param messageResId
	 *            The title string resource id, e.g:R.string.foo
	 */
	void showProgressDialog(int messageResId);

	/**
	 * show a progress dialog.
	 * 
	 * @param message
	 *            The progress dialog message
	 */
	void showProgressDialog(String message);

	/**
	 * Show a list selected dialog.
	 * 
	 * @param titleResId
	 *            the resource id of the dialog.
	 * @param selectItems
	 *            the selected items.
	 * @param itemSelectListener
	 *            the item selected listener.
	 */
	void showListSelectDialog(int titleResId, ArrayList<ListSelectorDialog.OptionItemData> selectItems, int defaultPosition, DialogItemSelectListener itemSelectListener);

	/**
	 * Show a list selected dialog.
	 * 
	 * @param title
	 *            the title of the dialog.
	 * @param selectItems
	 *            the selected items.
	 * @param itemSelectListener
	 *            the item selected listener.
	 */
	void showListSelectDialog(String title, ArrayList<ListSelectorDialog.OptionItemData> selectItems, int defaultPosition, DialogItemSelectListener itemSelectListener);

	/**
	 * Dismiss the showing progress dialog.
	 */
	void dismissDialog();

	/**
	 * Show toast
	 * @param msg the prompt message.
	 */
	void showToast(String msg);

	/**
	 * Show toast
	 * @param msgResId the prompt message's string resource id, e.g:R.string.foo.
	 */
	void showToast(int msgResId);

}
