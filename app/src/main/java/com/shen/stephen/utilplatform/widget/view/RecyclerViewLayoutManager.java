package com.shen.stephen.utilplatform.widget.view;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.OnScrollListener;
import android.view.View;
import android.view.ViewGroup.LayoutParams;

import com.shen.stephen.utilplatform.adapter.BaseRecyclerViewAdapter;

public class RecyclerViewLayoutManager extends LoadMoreRefreshLayout.LayoutManager {
	private RecyclerView mRecyclerView;
	private BaseRecyclerViewAdapter<?> adapter;
	private LinearLayoutManager mManager;

	public RecyclerViewLayoutManager(Context context) {
		super(context);
	}

	@Override
	public View setChildView(final LoadMoreRefreshLayout parent) {
		for (int i = 0; i < parent.getChildCount(); i++) {
			if (parent.getChildAt(i) instanceof RecyclerView) {
				mRecyclerView = (RecyclerView) parent.getChildAt(i);
				mRecyclerView.addOnScrollListener(new OnScrollListener() {
					@Override
					public void onScrolled(RecyclerView recyclerView, int dx,
										   int dy) {
						super.onScrolled(recyclerView, dx, dy);
						parent.onChildScroll();
					}
				});
				adapter = (BaseRecyclerViewAdapter<?>) mRecyclerView
						.getAdapter();
				mManager = (LinearLayoutManager) mRecyclerView
						.getLayoutManager();
			}

		}
		return mRecyclerView;
	}

	@Override
	public boolean isBottom() {
		if (mManager != null) {
			int lastPosition = mManager.findLastVisibleItemPosition();
			if (adapter != null && lastPosition == adapter.getItemViewCount() - 1) {
				return true;
			}
		}
		return false;
	}

	@Override
	public void setLoadingMoreView(boolean isLoading, View footerView) {
		if (adapter == null) {
			return;
		}

		if (isLoading) {
			LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT,
					LayoutParams.WRAP_CONTENT);
			footerView.setLayoutParams(params);
			adapter.setFooterView(footerView);
		} else {
			if (adapter.hasFooterView()) {
				adapter.removeFooterView();
			}
		}
	}

}
