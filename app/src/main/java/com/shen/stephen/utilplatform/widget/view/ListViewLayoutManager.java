package com.shen.stephen.utilplatform.widget.view;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.BaseAdapter;
import android.widget.ListView;

public class ListViewLayoutManager extends LoadMoreRefreshLayout.LayoutManager {

	public ListViewLayoutManager(Context context) {
		super(context);
	}

	private ListView mListView = null;

	@Override
	public View setChildView(final LoadMoreRefreshLayout parent) {
		for (int i = 0; i < parent.getChildCount(); i++) {
			if (parent.getChildAt(i) instanceof ListView) {
				mListView = (ListView) parent.getChildAt(i);
				mListView.setOnScrollListener(new OnScrollListener() {
					@Override
					public void onScrollStateChanged(AbsListView view,
							int scrollState) {
					}

					@Override
					public void onScroll(AbsListView view,
							int firstVisibleItem, int visibleItemCount,
							int totalItemCount) {
						parent.onChildScroll(firstVisibleItem);
					}
				});
				if (mListView.getFooterViewsCount() == 0) {
					// Get the adapter first then reset it after add a footer
					// view for fix bug #OSPA-1049.
					BaseAdapter a = (BaseAdapter) mListView.getAdapter();
					mListView.addFooterView(createDummyFooter());
					mListView.setAdapter(a);
				}
			}
		}
		return mListView;
	}

	/**
	 * Create a dummy footer with 0 height. this use to be ignore the App
	 * crashes with android 4.2.2(17) or before. for more details please see
	 * bug: #OSPA-1049 (http://wal-jira.perkinelmer.net:8080/browse/OSPA-1049.)
	 * 
	 * @return a dummy footer view with 0 height.
	 */
	private View createDummyFooter() {
		View footer = new View(mContext);
		LayoutParams lp = new AbsListView.LayoutParams(
				LayoutParams.MATCH_PARENT, 0);
		footer.setLayoutParams(lp);
		return footer;
	}

	@Override
	public boolean isBottom() {
		if (mListView != null && mListView.getAdapter() != null) {
			if (mListView.getLastVisiblePosition() == mListView.getAdapter()
					.getCount() - 1) {
				return true;
			}
		}
		return false;
	}

	@Override
	public void setLoadingMoreView(boolean isLoading, View footerView) {
		if (isLoading) {
			LayoutParams params = new AbsListView.LayoutParams(
					LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
			footerView.setLayoutParams(params);
			mListView.addFooterView(footerView);

		} else {
			if (footerView != null) {
				mListView.removeFooterView(footerView);
			}
		}
	}

}
