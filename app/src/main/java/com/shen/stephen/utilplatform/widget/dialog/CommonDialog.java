package com.shen.stephen.utilplatform.widget.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import com.shen.stephen.utilplatform.util.StrUtil;

public class CommonDialog extends BaseDialog {

    public static CommonDialog newInstance(String title, String message, DialogPresentInterface.DialogButtonClickListener listener) {
        return newInstance(title, message, listener, null, null);
    }

    public static CommonDialog newInstance(String title, String message, DialogPresentInterface.DialogButtonClickListener listener, String leftBtn, String rightBtn) {
        CommonDialog dialog = new CommonDialog();
        dialog.mBtnClickListener = listener;
        Bundle args = new Bundle();
        args.putString(ARGS_KEY_TITLE, title);
        args.putString(ARGS_KEY_MESSAGE, message);
        args.putString(ARGS_KEY_LEFT_BUTTON, leftBtn);
        args.putString(ARGS_KEY_RIGHT_BUTTON, rightBtn);
        dialog.setArguments(args);
        return dialog;
    }

    boolean mDismissed;
    boolean mShownByMe;

    public void show(FragmentManager manager, String tag) {
        mDismissed = false;
        mShownByMe = true;
        FragmentTransaction ft = manager.beginTransaction();
        ft.add(this, tag);
        ft.commitAllowingStateLoss();
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        String title = getArguments().getString(ARGS_KEY_TITLE);
        String message = getArguments().getString(ARGS_KEY_MESSAGE);
        String leftBtnText = getArguments().getString(ARGS_KEY_LEFT_BUTTON);
        String rightBtnText = getArguments().getString(ARGS_KEY_RIGHT_BUTTON);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // set title
        if (!StrUtil.isEmpty(title)) {
            builder.setTitle(title);
        }
        // set dialog message
        builder.setMessage(message)
                .setCancelable(false)
                .setNegativeButton(leftBtnText,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.dismiss();
                                if (mBtnClickListener != null) {
                                    mBtnClickListener.onClickLeftButton(dialog,
                                            null);
                                }
                            }
                        })
                .setPositiveButton(rightBtnText,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.dismiss();
                                if (mBtnClickListener != null) {
                                    mBtnClickListener.onClickRightButton(
                                            dialog, null);
                                }
                            }
                        });

        Dialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);
        return dialog;
    }

}
