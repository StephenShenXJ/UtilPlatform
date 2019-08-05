package com.shen.stephen.utilplatform.adapter;

import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.view.View;

public abstract class BaseRecyclerViewAdapter<VH extends ViewHolder> extends
		RecyclerView.Adapter<VH> {
	protected final int VIEW_TYPE_ITEM_DEFAULT = 0;
	protected final int VIEW_TYPE_FOOTER = -1;

	protected OnRecyclerViewItemClickListener mItemClickListener;
	protected boolean hasFooterView = false;
	protected View mFooterView;

	/**
	 * 
	 * Interface definition for a callback to be invoked when one of item was
	 * clicked.
	 *
	 */
	public interface OnRecyclerViewItemClickListener {

		/**
		 * The callback method when the item was clicked.
		 * 
		 * @param viewHolder
		 *            the item view holder
		 * @param clickedView
		 *            the clicked view.
		 * @param position
		 *            the position of the item.
		 * @param data
		 *            the data that associate with this item.
		 */
		void OnRecyclerViewItemClick(ViewHolder viewHolder,
											View clickedView, int position, Object data);
	}

	public void setOnItemClickListener(OnRecyclerViewItemClickListener l) {
		mItemClickListener = l;
	}

	public void setFooterView(View view) {
		hasFooterView = true;
		mFooterView = view;
		notifyDataSetChanged();
	}

	public void removeFooterView() {
		hasFooterView = false;
		mFooterView = null;
		notifyDataSetChanged();
	}

	public boolean hasFooterView() {
		return mFooterView == null ? false : true;
	}

	/**
	 * return the item view count besides footer
	 * @return
	 */
	public int getItemViewCount(){
		return 0;
	}

	@Override
	public int getItemCount() {
		int count = getItemViewCount();
		if (hasFooterView) {
			++count;
		}
		return count;
	}
	
	public int getAllItemViewType(int position){
		return VIEW_TYPE_ITEM_DEFAULT;
	}
	
	@Override
	public int getItemViewType(int position) {
		if (hasFooterView &&  position == getItemCount() - 1) {
			return VIEW_TYPE_FOOTER;
		} else {
			return  getAllItemViewType(position);
		}
	}
}
