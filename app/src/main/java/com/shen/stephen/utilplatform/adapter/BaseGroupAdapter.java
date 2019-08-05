package com.shen.stephen.utilplatform.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * 
 * The adapter that can make the list view looks like a group list view
 *
 */
public abstract class BaseGroupAdapter extends BaseAdapter {

	public static final int ITEM_VIEW_TYPE_CONTENT = 0;
	public static final int ITEM_VIEW_TYPE_DIVIDER = 1;

	private Map<GroupListItem, List<? extends GroupListItem>> mData;
	private boolean mIsShowFirstGroupHeader = true;
    private Context mContext;

	public BaseGroupAdapter(Context context) {
		mData = new LinkedHashMap<GroupListItem, List<? extends GroupListItem>>();
        mContext = context;
	}

    public Context getContext() {
        return mContext;
    }

	public BaseGroupAdapter(GroupListItem groupHeader, List<? extends GroupListItem> groupChildren) {
		mData = new LinkedHashMap<GroupListItem, List<? extends GroupListItem>>();
		mData.put(groupHeader, groupChildren);
	}

	public BaseGroupAdapter(Map<GroupListItem, List<? extends GroupListItem>> data) {
		if (data != null) {
			mData = data;
		} else {
			mData = new LinkedHashMap<GroupListItem, List<? extends GroupListItem>>();
		}
	}

	/**
	 * @return the mIsShowFirstGroupHeader
	 */
	public boolean isShowFirstGroupHeader() {
		return mIsShowFirstGroupHeader;
	}

	/**
	 * @param isShowFirstGroupHeader
	 *            the mIsShowFirstGroupHeader to set
	 */
	public void setIsShowFirstGroupHeader(boolean isShowFirstGroupHeader) {
		this.mIsShowFirstGroupHeader = isShowFirstGroupHeader;
	}

	public Map<GroupListItem, List<? extends GroupListItem>> getData() {
		return mData;
	}

	/**
	 * Add a group items data into the list.
	 * 
	 * @param groupChildren
	 *            the group items data.
	 */
	public void addGroup(List<? extends GroupListItem> groupChildren) {
		if (groupChildren == null || groupChildren.isEmpty()) {
			return;
		}
		addGroup(null, groupChildren);
	}

	public void addGroup(GroupListItem groupHeader,
			List<? extends GroupListItem> groupChildren) {
		if (groupChildren == null || groupChildren.isEmpty()) {
			return;
		}

		if (groupHeader == null) {
			groupHeader = createEmptyGroupHeaderItem();
		}

		mData.put(groupHeader, groupChildren);
		notifyDataSetChanged();
	}

	/**
	 * Add a group with one item into the list.
	 * 
	 * @param groupItem
	 *            the group items data.
	 */
	public void addGroup(GroupListItem groupItem) {
		if (groupItem == null) {
			return;
		}
		List<GroupListItem> groupChildren = new ArrayList<GroupListItem>();
		groupChildren.add(groupItem);
		addGroup(null, groupChildren);
	}

	private GroupListItem createEmptyGroupHeaderItem() {
		GroupListItem item = new GroupListItem();
		item._id = mData.size();
		return item;
	}

	/**
	 * Remove a group from the list by the specified key.
	 * 
	 * @param groupHeader
	 *            the group header item
	 */
	public void removeGroup(GroupListItem groupHeader) {
		mData.remove(groupHeader);

		notifyDataSetChanged();
	}

	/**
	 * Clear data.
	 */
	public void clear() {
		clearData();
		notifyDataSetChanged();
	}

	protected void clearData() {
		mData.clear();
	}

	@Override
	public int getCount() {
		int count = 0;

		if (mData.size() == 0) {
			return count;
		}

		for (Entry<GroupListItem, List<? extends GroupListItem>> entry : mData
				.entrySet()) {
			count += entry.getValue().size();
			count++;
		}

		if (!mIsShowFirstGroupHeader) {
			count--;
		}
		return count;
	}

	/**
	 * Get the group children item data by the position.
	 * 
	 * @param position
	 *            The position of the item within the adapter's data set whose
	 *            row id we want.
	 * @return the group children item data, null if the position is the group
	 *         header item position.
	 */
	@Override
	public Object getItem(int position) {
		Object ret = null;
		if ((position <= 0 && mIsShowFirstGroupHeader) || mData.isEmpty()) {
			return ret;
		}

		if (mIsShowFirstGroupHeader) {
			position--;
		}

		Set<GroupListItem> keys = mData.keySet();
		int size = 0;
		for (GroupListItem key : keys) {
			List<? extends GroupListItem> group = mData.get(key);
			size = group.size();
			if (position < size) {
				ret = group.get(position);
				break;
			}
			// The group item.
			else if (position == size) {
				ret = null;
				break;
			}

			position -= (size + 1);
			if (position < 0) {
				break;
			}
		}

		return ret;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public boolean isEnabled(int position) {
		if (isGroupHeaderItem(position)) {
			return false;
		}
		return true;
	}

	@Override
	public int getItemViewType(int position) {
		if (isGroupHeaderItem(position)) {
			return ITEM_VIEW_TYPE_DIVIDER;
		} else {
			return ITEM_VIEW_TYPE_CONTENT;
		}
	}

	@Override
	public int getViewTypeCount() {
		return 2;
	}

	/**
	 * Get group header item data by the position.
	 * 
	 * @param position
	 *            The position of the item within the adapter's data set whose
	 *            row id we want.
	 * @return the group header item data, null if the position is not the group
	 *         header item position.
	 */
	public GroupListItem getGroupHeaderItem(int position) {
		GroupListItem ret = null;
		if (position < 0 || mData.isEmpty()) {
			return ret;
		}

		Iterator<GroupListItem> keys = mData.keySet().iterator();
		if (position == 0) {
			ret = keys.next();
			return ret;
		}

		// Remove the first header
		if (mIsShowFirstGroupHeader) {
			position--;
		}

		int size = 0;
		while (keys.hasNext() && position >= 0) {
			GroupListItem key = keys.next();
			size = mData.get(key).size();
			if (position < size) {
				ret = null;
				// The position is not the group header end the loop
				break;
			}
			// The group item.
			else if (position == size) {
				if (keys.hasNext()) {
					ret = keys.next();
				}

				// Find the group header
				break;
			}

			position -= (size + 1);
			if (position < 0) {
				break;
			}
		}

		return ret;
	}

	protected boolean isGroupHeaderItem(int position) {
		if (position == 0 && mIsShowFirstGroupHeader) {
			return true;
		}

		boolean ret = false;
		Set<GroupListItem> keys = mData.keySet();
		int size = 0;

		if (mIsShowFirstGroupHeader) {
			position--;
		}

		for (GroupListItem key : keys) {
			size = mData.get(key).size();
			if (position < size) {
				ret = false;
				break;
			}
			// The group item.
			else if (position == size) {
				ret = true;
				break;
			}

			position -= (size + 1);
			if (position < 0) {
				break;
			}
		}

		return ret;
	}

	@Override
	public final View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		if (isGroupHeaderItem(position)) {
			convertView = getGroupHeaderItemView(inflater, position, getGroupHeaderItem(position), convertView, parent);
		} else {
			convertView = getGroupItemView(inflater, position, convertView, parent);
		}
		return convertView;
	}

	protected abstract View getGroupHeaderItemView(LayoutInflater inflater, int position,
			GroupListItem groupHeader, View convertView, ViewGroup parent);

	protected abstract View getGroupItemView(LayoutInflater inflater, int position, View convertView,
			ViewGroup parent);

	public static class GroupListItem {
		public int _id;
		public String _title;

		public void setId(int id) {
			_id = id;
		}

		public int getId() {
			return _id;
		}

		public void setTitle(String title) {
			_title = title;
		}

		public String getTitle() {
			return _title;
		}
	}

}
