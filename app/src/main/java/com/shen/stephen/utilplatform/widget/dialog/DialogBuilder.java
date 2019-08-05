/**
 * DialogBuilder.java
 * PIVOT
 * Copyright @2014 PerkinElmer. All rights reserved.
 */
package com.shen.stephen.utilplatform.widget.dialog;

import java.util.ArrayList;

public class DialogBuilder {
    public static BaseDialog buildDialog(String title, String content,
                                         String leftBtn, String rightBtn, DialogPresentInterface.DialogButtonClickListener listener) {
        return CommonDialog.newInstance(title, content, listener, leftBtn,
                rightBtn);

    }

    public static BaseDialog buildAlertDialog(String title, String message,
                                              DialogPresentInterface.DialogButtonClickListener listener) {

        return CommonAlertDialog.newInstance(title, message, listener);

    }

    // Create a custom dialog
    public static BaseDialog buildProgressDialog(String title) {
        return CommonProcessDialog.newInstance(title);
    }

    /**
     * Create a list selected dialog.
     *
     * @param title
     *            the title of the dialog.
     * @param selectItems
     *            selected items.
     * @param itemSelectListener
     *            the item selected listener.
     */
    public static BaseDialog buildListSelectDialog(String title,
                                                   ArrayList<ListSelectorDialog.OptionItemData> selectItems, int defaultPosition,
                                                   DialogPresentInterface.DialogItemSelectListener itemSelectListener) {
        return ListSelectorDialog.newInstance(title, selectItems, defaultPosition,
                itemSelectListener);
    }

    // Create a custom dialog with setCanceledOnTouchOutside as FALSE
    public static BaseDialog buildProgressDialogNonCancellable(String title) {

        return CommonProcessDialog.newInstance(title, false);
    }

    public static BaseDialog buildDateTimePickDialog(int mode, long defaultTime, boolean isfurture, DialogPresentInterface.DialogButtonClickListener listener) {
        return DateTimePickerDialog.newInstance(mode, defaultTime, isfurture, listener);
    }
}
