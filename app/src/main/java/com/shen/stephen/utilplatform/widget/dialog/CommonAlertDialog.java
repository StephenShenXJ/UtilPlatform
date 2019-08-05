package com.shen.stephen.utilplatform.widget.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;

import com.shen.stephen.utilplatform.R;

public class CommonAlertDialog extends BaseDialog {

	public static CommonAlertDialog newInstance(String title, String message,
			DialogPresentInterface.DialogButtonClickListener listener) {
		CommonAlertDialog dialog = new CommonAlertDialog();
		dialog.mBtnClickListener = listener;
		Bundle args = new Bundle();
		args.putString(ARGS_KEY_TITLE, title);
		args.putString(ARGS_KEY_MESSAGE, message);
		dialog.setArguments(args);
		return dialog;
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {

		String title = getArguments().getString(ARGS_KEY_TITLE);
		String message = getArguments().getString(ARGS_KEY_MESSAGE);

		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		// set title
		builder.setTitle(title);
		// set dialog message
		builder.setMessage(message)
				.setCancelable(false)
				.setPositiveButton(
						getActivity().getString(R.string.btn_ok_text),
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								// if this button is clicked, close current
								// activity
								dialog.dismiss();
								if (mBtnClickListener != null) {
									mBtnClickListener
											.onClickConfirmButton(dialog, null);
								}
							}
						});

		Dialog dialog = builder.create();
		dialog.setCanceledOnTouchOutside(false);
		return dialog;
	}

}
