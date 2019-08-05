package com.shen.stephen.utilplatform.widget;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.shen.stephen.utilplatform.R;
import com.shen.stephen.utilplatform.widget.view.ClearableEditText;
import com.shen.stephen.utilplatform.util.StrUtil;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class ListSelectFragment extends PkiFragment implements
		OnItemClickListener {
	/**
	 * The title intent key.
	 */
	public static final String TITLE_INTENT_KEY = "com.shen.stephen.utilplatform.widget.details.TITLE_INTENT_KEY";
	public static final String LIST_SELECT_RESULT_KEY = "com.shen.stephen.utilplatform.widget.details.ListSelectActivity";
	/**
	 * The intent key of selected items.
	 */
	public static final String LIST_SELECTED_ITEMS = "com.shen.stephen.utilplatform.widget.details:LIST_SELECTED_ITEMS";

	/**
	 * The intent key of choice mode. the values of the choice mode are
	 * {@linkplain #CHOICE_MODE_MULTI} and {@linkplain #CHOICE_MODE_SINGLE}
	 */
	public static final String CHOICE_MODE_KEY = "com.shen.stephen.utilplatform.widget.details:SELECT_MODE_KEY";

	/**
	 * The intent key for the specify whether need filter or not.
	 */
	public static final String NEED_FILTER_KEY = "com.shen.stephen.utilplatform.widget.details:NEED_FILTER_KEY";

	/**
	 * The intent key for the on selected item listener.
	 */
	public static final String ON_SELECTITEM_LISTENER_KEY = "com.shen.stephen.utilplatform.widget.details:ON_SELECTITEM_LISTENER";

	/**
	 * The list selection mode: Single choice mode.
	 */
	public static final int CHOICE_MODE_SINGLE = 0;

	/**
	 * The list selection mode: multi-choice mode.
	 */
	public static final int CHOICE_MODE_MULTI = 1;
	/**
	 * The title of the previous Fragment
	 */
	private String mPreTitle;
	private String mTitle;
	protected ClearableEditText mFilterEditText;

	private ListView mItemsListView;
	private List<SelectedItem> mSelectionItems;

	private ListSelectAdapter mListAdapter;
	private int mChoiceMode = CHOICE_MODE_SINGLE;
	private boolean mNeedFilter = true;
	private OnSelectItemListener mSelectListener;

	/**
	 * The interface definition for a callback to be invoked when a item is
	 * clicked.
	 */
	public interface OnSelectItemListener extends Serializable {
		/**
		 * Called when a item is clicked.
		 *
		 * @param listView              the list view instance.
		 * @param adapter               the adapter of the list view.
		 * @param selectedItem          the current selected item.
		 * @param previousSelectedItems the previous selected items.
		 * @return true if consume this click event. otherwise false.
		 */
		public boolean onSelectItem(ListView listView,
									ListSelectAdapter adapter, SelectedItem selectedItem,
									List<SelectedItem> previousSelectedItems);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Bundle bundle = this.getArguments();
		mChoiceMode = bundle.getInt(CHOICE_MODE_KEY, CHOICE_MODE_SINGLE);
		mNeedFilter = bundle.getBoolean(NEED_FILTER_KEY, true);
		mTitle = bundle.getString(TITLE_INTENT_KEY);
		mSelectionItems = bundle.getParcelableArrayList(LIST_SELECTED_ITEMS);
		mSelectListener = (OnSelectItemListener) bundle.getSerializable(ON_SELECTITEM_LISTENER_KEY);
		showHideOptionMenu();
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		mPreTitle = (String) ((PkiActivity) getActivity()).getTitle();
		((PkiActivity) getActivity()).setTitle(mTitle);
		super.onActivityCreated(savedInstanceState);
	}

	protected void showHideOptionMenu() {
		setHasOptionsMenu(mChoiceMode == CHOICE_MODE_MULTI);
	}

	@Override
	protected int getContentViewResourceId() {
		return R.layout.list_select_fragment_layout;
	}

	protected TextWatcher mFilterTextWatcher = new TextWatcher() {
		String content;

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count,
									  int after) {
		}

		@Override
		public void onTextChanged(CharSequence s, int start, int before,
								  int count) {
			content = s.toString();
			onInputTextChanged(content);
		}

		@Override
		public void afterTextChanged(Editable s) {
		}
	};

	@Override
	protected void init(Bundle savedInstanceState) {
		mFilterEditText = (ClearableEditText) findViewById(R.id.list_select_filter);
		mItemsListView = (ListView) findViewById(R.id.list_select_items_listview);
		mListAdapter = new ListSelectAdapter(mSelectionItems,
				this.getActivity());
		mItemsListView.setAdapter(mListAdapter);
		mItemsListView.setOnItemClickListener(this);
		setTextWatcher();
	}

	/**
	 * Refresh the list selector data.
	 *
	 * @param selectedItems the new selectors.
	 */
	protected void refreshSelectorData(ArrayList<SelectedItem> selectedItems) {
		mSelectionItems = selectedItems;
		if (mListAdapter != null) {
			mListAdapter.refresh(selectedItems);
		}
		return;
	}

	protected void setTextWatcher() {
		if (mNeedFilter) {
			mFilterEditText.addTextChangedListener(mFilterTextWatcher);
			mFilterEditText.setVisibility(View.VISIBLE);
		} else {
			mFilterEditText.setVisibility(View.GONE);
		}
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.action_done_menu, menu);
	}

	@Override
	public void onResume() {
		super.onResume();
		setListFocusedPosition();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.action_done) {
			finishSelect(mListAdapter.getSelectedItemsData());
			return true;
		}
		return false;
	}

	@Override
	public void onDestroyView() {
		((PkiActivity) getActivity()).setTitle(mPreTitle);
		super.onDestroyView();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		hideSoftKeyboard(mFilterEditText);
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
							long id) {
		SelectedItem selectedItem = (SelectedItem) mListAdapter
				.getItem(position);
		if (mSelectListener != null) {
			if (mSelectListener.onSelectItem(mItemsListView, mListAdapter,
					selectedItem, mListAdapter.getSelectedItems())) {
				return;
			}
		}

		if (!selectedItem._isSelected && mChoiceMode == CHOICE_MODE_SINGLE) {
			finishSelect((Serializable) selectedItem._data);
		} else if (mChoiceMode == CHOICE_MODE_MULTI) {
			selectedItem._isSelected = !selectedItem._isSelected;
			mListAdapter.notifyDataSetChanged();
		}
	}

	protected void finishSelect(Serializable... items) {
		ArrayList<Serializable> selectedItems = new ArrayList<Serializable>();
		if (items != null) {
			Collections.addAll(selectedItems, items);
		}
		Intent data = new Intent();
		data.putExtra(LIST_SELECT_RESULT_KEY, selectedItems);
		setResult(Activity.RESULT_OK, data);
		onBackClicked();
	}

	/**
	 * finish the selection page by clicking the done button in the option menu
	 *
	 * @param selectedItems
	 */
	protected void finishSelect(List<Serializable> selectedItems) {
		if (selectedItems == null || selectedItems.isEmpty()) {
			finishSelect();
			return;
		}

		finishSelect(selectedItems.toArray(new Serializable[1]));
	}

	/**
	 * Called when the input text has been changed.
	 *
	 * @param newText the new inputted text.
	 */
	protected void onInputTextChanged(String newText) {
		List<SelectedItem> locationListFiltered;
		if (StrUtil.isEmpty(newText)) {
			locationListFiltered = mSelectionItems;
		} else {
			newText = newText.trim();
			locationListFiltered = searchItem(newText.toLowerCase(Locale.US));
		}
		mListAdapter.refresh(locationListFiltered);
	}

	private List<SelectedItem> searchItem(String searchKey) {
		List<SelectedItem> result = new ArrayList<SelectedItem>();
		if (mSelectionItems == null || mSelectionItems.isEmpty()) {
			return result;
		}

		for (SelectedItem location : mSelectionItems) {
			if (location._displayName.toLowerCase(Locale.US).contains(searchKey)) {
				result.add(location);
			}
		}
		return result;
	}

	/**
	 * The selected item data struct
	 */
	public static final class SelectedItem implements Serializable, Parcelable {
		private static final long serialVersionUID = -7642702840116589903L;

		/**
		 * The display name that will displayed on the select list.
		 */
		public String _displayName;

		/**
		 * Specify whether the item is selected or not.
		 */
		public boolean _isSelected;

		/**
		 * The original data of the item.
		 */
		public Serializable _data;

		@Override
		public int describeContents() {
			return 0;
		}

		@Override
		public void writeToParcel(Parcel dest, int flags) {
			dest.writeString(_displayName);
			dest.writeInt(_isSelected ? 1 : 0);
			dest.writeSerializable(_data);
		}

		public static final Creator<SelectedItem> CREATOR = new Creator<SelectedItem>() {
			@Override
			public SelectedItem createFromParcel(Parcel source) {
				SelectedItem item = new SelectedItem();
				item._displayName = source.readString();
				item._isSelected = source.readInt() == 1;
				item._data = source.readSerializable();
				return item;
			}

			@Override
			public SelectedItem[] newArray(int size) {
				return new SelectedItem[0];
			}
		};
	}

	public static class ListSelectAdapter extends BaseAdapter {
		private List<SelectedItem> mData;
		private Context context;

		public ListSelectAdapter(List<SelectedItem> l, Context c) {
			this.mData = l;
			this.context = c;
		}

		public void refresh(List<SelectedItem> data) {
			mData = data;
			notifyDataSetChanged();
		}

		/**
		 * Clear the current selected items.
		 */
		public void clearSelectedItems() {
			if (mData == null) {
				return;
			}

			for (SelectedItem item : mData) {
				item._isSelected = false;
			}
		}

		/**
		 * Get currently selected items
		 */
		public List<SelectedItem> getSelectedItems() {
			List<SelectedItem> selectedItems = new ArrayList<SelectedItem>();
			if (mData == null) {
				return selectedItems;
			}

			for (SelectedItem item : mData) {
				if (item._isSelected) {
					selectedItems.add(item);
				}
			}
			return selectedItems;
		}

		public List<Serializable> getSelectedItemsData() {

			List<Serializable> selectedItems = new ArrayList<Serializable>();
			if (mData == null) {
				return selectedItems;
			}

			for (SelectedItem item : mData) {
				if (item._isSelected) {
					selectedItems.add((Serializable) item._data);
				}
			}
			return selectedItems;
		}

		@Override
		public int getCount() {
			if (mData != null) {
				return mData.size();
			}
			return 0;
		}

		@Override
		public Object getItem(int position) {
			if (mData != null) {
				return mData.get(position);
			}
			return null;
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			TextView view = null;
			if (convertView == null) {
				view = (TextView) LayoutInflater.from(context).inflate(
						android.R.layout.simple_list_item_1, parent, false);
			} else {
				view = (TextView) convertView;
			}

			SelectedItem item = (SelectedItem) getItem(position);
			view.setText(item._displayName);
			view.setTextSize(TypedValue.COMPLEX_UNIT_PX, context.getResources()
					.getDimension(R.dimen.default_font_size));
			view.setTextColor(context.getResources().getColor(
					R.color.perkin_selected_dark_blue));
			if (item._isSelected) {
				view.setCompoundDrawablesWithIntrinsicBounds(0, 0,
						R.drawable.ic_checked, 0);
			} else {
				view.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
			}
			return view;
		}
	}

	private void setListFocusedPosition() {
		int positionFocused = 0;
		if (mSelectionItems != null) {
			for (int i = 0; i < mSelectionItems.size(); i++) {
				if ((mSelectionItems.get(i))._isSelected) {
					positionFocused = i;
					break;
				}
			}
			mItemsListView.setSelection(positionFocused);
		}
	}

	public static ArrayList<SelectedItem> createStringSelectedItems(List<String> selectedItems, String defaultSelectedItem) {
		ArrayList<SelectedItem> selectItems = new ArrayList<SelectedItem>();
		if (selectedItems == null) {
			return selectItems;
		}

		for (String selectedItem : selectedItems) {
			SelectedItem item = new SelectedItem();
			item._displayName = selectedItem;
			item._data = selectedItem;
			item._isSelected = selectedItem.equals(defaultSelectedItem);
			selectItems.add(item);
		}

		return selectItems;
	}
}
