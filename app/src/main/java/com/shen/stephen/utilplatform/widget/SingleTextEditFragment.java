package com.shen.stephen.utilplatform.widget;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;

import com.shen.stephen.utilplatform.R;
import com.shen.stephen.utilplatform.widget.view.ClearableEditText;
import com.shen.stephen.utilplatform.util.StrUtil;

public class SingleTextEditFragment extends PkiFragment {
    /**
     * The title intent key.
     */
    public static final String TITLE_INTENT_KEY = "com.shen.stephen.utilplatform.widget.details.TITLE_INTENT_KEY";
    /**
     * The intent key for the edit content.
     */
    public static final String EDIT_CONTENT = "com.shen.stephen.utilplatform.widget.details.EDIT_CONTENT";
    public static final String MAX_LENGTH_LIMIT_KEY = "com.shen.stephen.utilplatform.widget.details.MAX_LENGTH_LIMIT_KEY";
    public static final String EDIT_CONTENT_TYPE_KEY = "com.shen.stephen.utilplatform.widget.details.EDIT_CONTENT_TYPE_KEY";
    public static final String IS_MULTILINE_KEY = "com.shen.stephen.utilplatform.widget.details.IS_MULTILINE_KEY";

    /**
     * The single text edit result key
     */
    public static final String SINGLE_TEXT_EDIT_RESULT_KEY = "com.shen.stephen.utilplatform.widget.details:SINGLE_TEXT_EDIT_RESULT_KEY";
    public static final int CONTENT_TYPE_STRING = 0;
    public static final int CONTENT_TYPE_NUMBER = 1;
    public static final int CONTENT_TYPE_PHONE = 2;

    private ClearableEditText mInputTextView;
    private TextView mLimitLengthView;

    /**
     * content type: 0 is string 1 is number
     */
    private int contentType = 0;
    private int mMaxLength = -1;
    private boolean mIsMultiLine;
    private String mContent = StrUtil.EMPTYSTRING;
    private String mTitle = StrUtil.EMPTYSTRING;

    @Override
    protected int getContentViewResourceId() {
        return R.layout.single_text_edit_fragment_layout;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        Bundle args = getArguments();
        if (args != null) {
            mContent = args.getString(EDIT_CONTENT);
            mMaxLength = args.getInt(MAX_LENGTH_LIMIT_KEY, -1);
            contentType = args.getInt(EDIT_CONTENT_TYPE_KEY, 0);
            mTitle = args.getString(TITLE_INTENT_KEY);
            mIsMultiLine = args.getBoolean(IS_MULTILINE_KEY, false);
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        ((PkiActivity) getActivity()).setTitle(mTitle);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onDestroyView() {
        ((PkiActivity) getActivity()).setTitle(StrUtil.EMPTYSTRING);
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        hideSoftKeyboard(mInputTextView);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.action_done_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_done) {
            Intent data = new Intent();
            data.putExtra(SINGLE_TEXT_EDIT_RESULT_KEY, mContent);
            setResult(Activity.RESULT_OK, data);
            getActivity().onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void init(Bundle savedInstanceState) {
        mLimitLengthView = (TextView) findViewById(R.id.single_text_edit_left_length_view);
        mInputTextView = (ClearableEditText) findViewById(R.id.single_text_edit_input_view);
        mInputTextView.setSingleLine(!mIsMultiLine);

        if (contentType == CONTENT_TYPE_NUMBER) {
            mInputTextView.setInputType(EditorInfo.TYPE_CLASS_NUMBER);
        } else if (contentType == CONTENT_TYPE_PHONE) {
            mInputTextView.setInputType(EditorInfo.TYPE_CLASS_PHONE);
        }
        mInputTextView.setText(mContent);
        if (!StrUtil.isEmpty(mContent)) {
            mInputTextView.setSelection(mContent.length());
        }
        if (mMaxLength != -1) {
            mInputTextView.setFilters(new InputFilter[]{new InputFilter.LengthFilter(mMaxLength)});
            mLimitLengthView.setVisibility(View.VISIBLE);
            mLimitLengthView.setText(mMaxLength - mInputTextView.getText().length() + "");
        } else {
            mLimitLengthView.setVisibility(View.GONE);
        }

        mInputTextView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before,
                                      int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {

                mLimitLengthView.setText(mMaxLength
                        - (s == null ? 0 : s.length()) + "");
                mContent = s.toString();
            }

        });
    }

}
