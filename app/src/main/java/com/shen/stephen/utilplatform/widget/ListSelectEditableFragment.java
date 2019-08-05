package com.shen.stephen.utilplatform.widget;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.google.zxing.client.android.CaptureActivity;
import com.shen.stephen.utilplatform.R;
import com.shen.stephen.utilplatform.util.StrUtil;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;

import edu.sfsu.cs.orange.ocr.CaptureActivityOCR;
import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.OnPermissionDenied;
import permissions.dispatcher.RuntimePermissions;

@RuntimePermissions
public class ListSelectEditableFragment extends ListSelectFragment implements View.OnClickListener {

	public static final String MAX_LENGTH_LIMIT_KEY = "com.shen.stephen.utilplatform.widget.details:MAX_LENGTH_LIMIT_KEY";
	public static final String EDIT_CONTENT = "com.shen.stephen.utilplatform.widget.details.EDIT_CONTENT";
	public static final String TITLE_INTENT_KEY = "com.shen.stephen.utilplatform.widget.details.TITLE_INTENT_KEY";
	public static final String IS_SHOE_SCAN_INTENT_KEY = "com.shen.stephen.utilplatform.widget.common.IS_CAN_SCAN_INTENT_KEY";

	private static final int START_BARCODE_REQUEST_CODE = 0;
	private static final int START_OCR_REQUEST_CODE = 1;

	private TextView numCountView, mScanBtn;
	private int mMaxLength;
	private String mContent;
	private String mTitle;
	private boolean mIsShowScanBtn;

	private TextWatcher mTextWatcher = new TextWatcher() {
		@Override
		public void beforeTextChanged(CharSequence s, int start, int count,
									  int after) {
		}

		@Override
		public void onTextChanged(CharSequence s, int start, int before,
								  int count) {
			mContent = s.toString();
			onInputTextChanged(mContent);
		}

		@Override
		public void afterTextChanged(Editable s) {
			numCountView.setText(mMaxLength - (s == null ? 0 : s.length()) + "");
		}

	};

	@Override
	protected int getContentViewResourceId() {
		return R.layout.list_select_editable_fragment_layout;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Bundle bundle = this.getArguments();
		if (bundle != null) {
			mMaxLength = bundle.getInt(MAX_LENGTH_LIMIT_KEY, -1);
			mContent = bundle.getString(EDIT_CONTENT);
			mTitle = bundle.getString(TITLE_INTENT_KEY);
			mIsShowScanBtn = bundle.getBoolean(IS_SHOE_SCAN_INTENT_KEY, false);
		}

	}

	@Override
	protected void init(Bundle savedInstanceState) {
		super.init(savedInstanceState);
		numCountView = (TextView) findViewById(R.id.charatcer_number_label);
		if (mMaxLength != -1) {
			numCountView.setText(Integer.toString(mMaxLength));
			mFilterEditText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(mMaxLength)});
			numCountView.setVisibility(View.VISIBLE);
			numCountView.setText(mMaxLength - mFilterEditText.getText().length() + "");
		} else {
			numCountView.setVisibility(View.GONE);
		}

		mScanBtn = (TextView) findViewById(R.id.list_select_scan_btn);
		mScanBtn.setOnClickListener(this);
		if (mIsShowScanBtn) {
			mScanBtn.setVisibility(View.VISIBLE);
		} else {
			mScanBtn.setVisibility(View.GONE);
		}
	}

	@Override
	protected void setTextWatcher() {
		mFilterEditText.setHint(mTitle);
		mFilterEditText.setText(mContent);
		if (!StrUtil.isEmpty(mContent)) {
			mFilterEditText.setSelection(mContent.length());
		}
		mFilterEditText.addTextChangedListener(mTextWatcher);
	}

	@Override
	protected void finishSelect(Serializable... items) {
		ArrayList<Serializable> selectedItems = new ArrayList<Serializable>();
		if (items != null) {
			Collections.addAll(selectedItems, items);
		}
		mFilterEditText.setText((String) selectedItems.get(0));
	}

	@Override
	public void onBackClicked() {
		setResult(Activity.RESULT_CANCELED, null);
		super.onBackClicked();
	}

	@Override
	protected void showHideOptionMenu() {
		setHasOptionsMenu(true);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.action_done) {
			Intent data = new Intent();
			data.putExtra(LIST_SELECT_RESULT_KEY, mContent);
			setResult(Activity.RESULT_OK, data);
			super.onBackClicked();
			return true;
		}

		return false;
	}

	@Override
	public void onClick(View v) {
		scanBarcode();
	}

	@NeedsPermission(android.Manifest.permission.CAMERA)
	public void scanBarcode() {
		try {
			Intent intent = new Intent(mContext, CaptureActivity.class);
			intent.setAction("com.google.zxing.client.android.SCAN");
			// for Qr code only , use "QR_CODE_MODE" instead of "PRODUCT_MODE"
			intent.putExtra("SCAN_MODE", "QR_CODE_MODE,PRODUCT_MODE");
			// this stops saving ur barcode in barcode scanner app history
			intent.putExtra("SAVE_HISTORY", false);
			startActivityForResult(intent, START_BARCODE_REQUEST_CODE);


		} catch (Exception e) {
			e.printStackTrace();
			showToast("ERROR:" + e.getMessage());
		}
	}

	private void initOCR() {
		try {
			Intent intent = new Intent(mContext, CaptureActivityOCR.class);
			// this stops saving ur barcode in barcode scanner app history
			// intent.putExtra("SAVE_HISTORY", false);
			startActivityForResult(intent, START_OCR_REQUEST_CODE);

		} catch (Exception e) {
			e.printStackTrace();
			showToast("ERROR:" + e.getMessage());
		}
	}

	@OnPermissionDenied(android.Manifest.permission.CAMERA)
	void showDeniedForCamera() {
		showToast(R.string.barcode_search_camera_permission_denied);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		if (resultCode == 100) {
			String switchTo = intent.getStringExtra("switch_to");

			if (switchTo.equalsIgnoreCase("OCR")) {
				initOCR();
			} else {
				scanBarcode();
			}
		} else if (requestCode == START_BARCODE_REQUEST_CODE) {

			if (resultCode == Activity.RESULT_OK) {
				String result = "";
				//String scanFormat = intent.getStringExtra("SCAN_RESULT_FORMAT").trim();

				// get SCAN_RESULT from resultIntent
				if (intent.hasExtra("SCAN_RESULT")) {
					result = StrUtil.strNotNull(intent.getStringExtra("SCAN_RESULT"));
					result = result.trim();
				}
				if (!StrUtil.isEmpty(result)) {
					mFilterEditText.setText(result);
				}
			}
		} else if (requestCode == START_OCR_REQUEST_CODE) {
			if (resultCode == Activity.RESULT_OK) {

				String result = StrUtil.EMPTYSTRING;
				if (intent.hasExtra("OCR_RESULT")) {
					result = StrUtil.strNotNull(intent.getStringExtra("OCR_RESULT"));
					result = result.trim();
				}

				if (!StrUtil.isEmpty(result)) {
					mFilterEditText.setText(result);
				}
			}
		}
	}
}
