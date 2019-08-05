package com.shen.stephen.utilplatform.widget.dialog;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.shen.stephen.utilplatform.R;
import com.shen.stephen.utilplatform.util.StrUtil;

@SuppressLint("InflateParams")
public class CommonProcessDialog extends BaseDialog {

	private boolean mIsCancelable;

	public static CommonProcessDialog newInstance(String title) {
		return newInstance(title, true);
	}

	public static CommonProcessDialog newInstance(String title,
			boolean isCancelable) {
		CommonProcessDialog dialog = new CommonProcessDialog();
		dialog.mIsCancelable = isCancelable;
		Bundle args = new Bundle();
		args.putString(ARGS_KEY_TITLE, title);
		dialog.setArguments(args);
		return dialog;
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {

		String title = getArguments().getString(ARGS_KEY_TITLE);

		Dialog dialog = new Dialog(getActivity(), R.style.DialogTheme);

		LayoutInflater mInflater = (LayoutInflater) getActivity()
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View mViewDialog = mInflater.inflate(R.layout.dialog_alert, null);
		TextView mTextView = (TextView) mViewDialog
				.findViewById(R.id.text_view_title);
		ProgressBar progressBar = (ProgressBar) mViewDialog
				.findViewById(R.id.image_view_loading);

		if (StrUtil.isEmpty(title)) {
			title = getActivity().getString(R.string.progress_loading);
		} else {
			if (title.length() > 41) {
				title = title.substring(0, 36);
			}
		}

		mTextView.setText(title);

		dialog.setContentView(mViewDialog);
		dialog.setCanceledOnTouchOutside(mIsCancelable);
		int width = getActivity().getResources().getDimensionPixelSize(
				R.dimen.alert_dialog_width);
		dialog.getWindow().setLayout(width, LayoutParams.WRAP_CONTENT);
		dialog.setCancelable(false);
		dialog.setCanceledOnTouchOutside(false);

		return dialog;
	}
}
