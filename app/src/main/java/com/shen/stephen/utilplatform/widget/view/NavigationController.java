package com.shen.stephen.utilplatform.widget.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.shen.stephen.utilplatform.PKIApplication;
import com.shen.stephen.utilplatform.R;
import com.shen.stephen.utilplatform.util.FileUtils;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Stack;

public class NavigationController extends LinearLayout {
	/**
	 * The normal mode of push a fragment. The fragment add to the top of the
	 * current fragment stack.
	 */
	public static final int PUSH_MODE_NORMAL = 0;

	/**
	 * The single top mode of push a fragment. It will clear all of the
	 * fragments in current fragment stack and then add the fragment to the top
	 * of the current fragment stack.
	 */
	public static final int PUSH_MODE_SINGLE_TOP = 1;

	/**
	 * The single instance mode of push a fragment. It will check the current
	 * pushed fragment whether exist in the stack, if not exist just like
	 * {@linkplain NavigationController#PUSH_MODE_NORMAL}, otherwise will pop
	 * all of the fragments which cover the pushed fragment.
	 */
	public static final int PUSH_MODE_SINGLE_INSTANCE = 2;

	/**
	 * Default navigation bar item count is set 2.
	 */
	private static final int DEFAULT_NAV_BAR_ITEM_COUNT = 2;

	/**
	 * The action id of push fragment.
	 */
	private static final int ACTION_PUSH_FRAGMENT = 1;

	/**
	 * The action id of pop fragment.
	 */
	private static final int ACTION_POP_FRAGMENT = 2;

	/**
	 * The action id of pop fragment.
	 */
	private static final int ACTION_POP_TO_FRAGMENT = 3;

	/**
	 * The action id of clear fragment.
	 */
	private static final int ACTION_CLEAR_FRAGMENT = 4;

	/**
	 * the pending message data key of fragment class full name.
	 */
	private static final String MESSAGE_DATA_FRAGMENT_NAME = "NavigationController:fragemntName";

	/**
	 * the pending message data key of fragment title.
	 */
	private static final String MESSAGE_DATA_FRAGMENT_TITLE = "NavigationController:fragemntTitle";

	/**
	 * the pending message data key of fragment tag.
	 */
	private static final String MESSAGE_DATA_FRAGMENT_TAG = "NavigationController:fragemntTag";

	/**
	 * the pending message data key of fragment arguments.
	 */
	private static final String MESSAGE_DATA_FRAGMENT_ARGS = "NavigationController:fragemntArgs";

	private final String EMPTY_FRAGMENT_TAG = "NavigationController:emptyFragment";

	private ViewGroup mContentContainer;
	private LinearLayout mNavBar;

	private int mNavBarBackground;
	private int mNavBarItemResId;
	private int mNavBarHeight;
	private int mNavBarWidth;
	private int mMaxNavBarItemCount;
	private boolean mIsShowDivider;
	private int mDividerResId;
	private int mContentContainerId;
	private int mNavBarItemTextViewId;
	private int mNavBarItemTextSize;
	private int mNavBarItemTextColor;

	private EmptyFragment mEmptyFragment;
	private FragmentManager mFragmentManager;
	private Stack<FragmentInfo> mFragmentsStack;

	private int mHiddenNavBarItemIndex = 0;
	private int mCurrentStackCount = 0;

	/**
	 * Specify whether system has called
	 * {@linkplain NavigationController#onSaveInstanceState()}.
	 */
	private boolean mIsSavedState = false;

	private Handler mExecutePendingHanler;

	/**
	 * The pending execution handler queue.
	 */
	private ArrayList<Message> mPendingQueue;

	static class ExecutePendingHandler extends Handler {
		private WeakReference<NavigationController> mNavigitionController;

		ExecutePendingHandler(NavigationController navCtl) {
			mNavigitionController = new WeakReference<NavigationController>(
					navCtl);
		}

		@Override
		public void handleMessage(Message msg) {

			switch (msg.what) {
				case ACTION_PUSH_FRAGMENT:
					Bundle data = msg.getData();
					if (data == null) {
						break;
					}

					String framentName = data.getString(MESSAGE_DATA_FRAGMENT_NAME);
					Bundle args = data.getBundle(MESSAGE_DATA_FRAGMENT_ARGS);
					Fragment fragment = Fragment.instantiate(
							PKIApplication.getContext(), framentName, args);

					String tag = data.getString(MESSAGE_DATA_FRAGMENT_TAG);
					String title = data.getString(MESSAGE_DATA_FRAGMENT_TITLE);
					mNavigitionController.get().pushFragment(fragment, tag, title,
							msg.arg1);
					break;
				case ACTION_POP_FRAGMENT:
					mNavigitionController.get().popFragment();
					break;
				case ACTION_POP_TO_FRAGMENT:
					mNavigitionController.get().pop2Fragment((String) msg.obj,
							msg.arg1);
					break;
				case ACTION_CLEAR_FRAGMENT:
					mNavigitionController.get().clearAllFragments();
					break;

				default:
					break;
			}
		}
	}

	static final class FragmentInfo implements Parcelable {
		Fragment fragment;
		String tag;
		String title;

		FragmentInfo(Fragment fm, String tag, String title) {
			fragment = fm;
			this.tag = tag;
			this.title = title;
		}

		@Override
		public int describeContents() {
			return 1;
		}

		@Override
		public void writeToParcel(Parcel dest, int flags) {
			dest.writeString(tag);
			dest.writeString(title);
		}

		public FragmentInfo(Parcel in) {
			tag = in.readString();
			title = in.readString();
		}

		public static final Parcelable.Creator<FragmentInfo> CREATOR = new Parcelable.Creator<FragmentInfo>() {
			public FragmentInfo createFromParcel(Parcel in) {
				return new FragmentInfo(in);
			}

			public FragmentInfo[] newArray(int size) {
				return new FragmentInfo[size];
			}
		};
	}

	public NavigationController(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public NavigationController(Context context, AttributeSet attrs,
								int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		initController(context, attrs);
	}

	public NavigationController(Context context) {
		super(context);
		initController(context, null);
	}

	static class SavedState extends BaseSavedState {
		List<FragmentInfo> mFragments;
		ArrayList<Message> mPendingMsg;

		SavedState(Parcelable superState) {
			super(superState);
		}

		private SavedState(Parcel in) {
			super(in);

			int size = in.readInt();
			if (size > 0) {
				mFragments = new ArrayList<FragmentInfo>(size);
			}
			for (int i = 0; i < size; i++) {
				mFragments.add((FragmentInfo) in
						.readParcelable(FragmentInfo.class.getClassLoader()));
			}

			mPendingMsg = new ArrayList<Message>();
			in.readTypedList(mPendingMsg, Message.CREATOR);
		}

		@Override
		public void writeToParcel(Parcel out, int flags) {
			super.writeToParcel(out, flags);

			if (mFragments != null && mFragments.size() > 0) {
				int size = mFragments.size();
				out.writeInt(size);
				for (int i = 0; i < size; i++) {
					out.writeParcelable(mFragments.get(i), flags);
				}

			} else {
				out.writeInt(0);
			}

			out.writeTypedList(mPendingMsg);
		}

		public static final Parcelable.Creator<SavedState> CREATOR = new Parcelable.Creator<SavedState>() {
			public SavedState createFromParcel(Parcel in) {
				return new SavedState(in);
			}

			public SavedState[] newArray(int size) {
				return new SavedState[size];
			}
		};
	}

	private void initController(Context context, AttributeSet attrs) {
		TypedArray a = context.obtainStyledAttributes(attrs,
				R.styleable.NavigationController, 0, 0);
		setOrientation(LinearLayout.VERTICAL);
		mNavBarItemResId = a.getResourceId(
				R.styleable.NavigationController_navbarItem, NO_ID);
		mMaxNavBarItemCount = a.getInt(
				R.styleable.NavigationController_maxNavbarItemCount,
				DEFAULT_NAV_BAR_ITEM_COUNT);
		mIsShowDivider = a.getBoolean(
				R.styleable.NavigationController_isShowDivider, true);
		mDividerResId = a.getResourceId(
				R.styleable.NavigationController_navdivider, NO_ID);
		mNavBarBackground = a.getResourceId(
				R.styleable.NavigationController_navbarBackground,
				android.R.color.transparent);
		mNavBarItemTextViewId = a.getResourceId(
				R.styleable.NavigationController_navbarItemTextViewId, NO_ID);
		mNavBarItemTextSize = a.getDimensionPixelSize(
				R.styleable.NavigationController_navbarItemTextSize, 14);
		mNavBarItemTextColor = a.getColor(
				R.styleable.NavigationController_navbarItemTextColor, context
						.getResources().getColor(R.color.white));
		mNavBarHeight = a.getDimensionPixelSize(
				R.styleable.NavigationController_navbarHeight,
				LayoutParams.WRAP_CONTENT);
		mNavBarWidth = a.getDimensionPixelSize(
				R.styleable.NavigationController_navbarWidth,
				LayoutParams.MATCH_PARENT);
		a.recycle();

		mFragmentsStack = new Stack<FragmentInfo>();
		mPendingQueue = new ArrayList<Message>();
		mExecutePendingHanler = new ExecutePendingHandler(this);

		initNavigationBar(context);
	}

	private void ensureHierarchy(Context context) {
		if (mContentContainerId <= 0) {
			mContentContainerId = R.id.navigation_controller_content;
			mContentContainer = new FrameLayout(context);
			mContentContainer.setId(mContentContainerId);
			mContentContainer.setBackgroundResource(R.color.black);
			addView(mContentContainer, new LinearLayout.LayoutParams(
					LayoutParams.MATCH_PARENT, 0, 1));
			requestLayout();
		} else {
			mContentContainer = (ViewGroup) findViewById(mContentContainerId);
			setUpEmptyView();
		}
	}

	private void setUpEmptyView() {
		FragmentTransaction ft = mFragmentManager.beginTransaction();
		if (mFragmentManager.findFragmentByTag(EMPTY_FRAGMENT_TAG) == null) {
			ft.add(mContentContainerId, mEmptyFragment, EMPTY_FRAGMENT_TAG);
			ft.commit();
		}

	}

	/**
	 * Called when back key pressed.
	 *
	 * @return true if have consume the back key
	 */
	public boolean onBackPressed() {
		if (mCurrentStackCount > 0) {
			popFragment();
			return true;
		}

		return false;
	}

	private void initNavigationBar(Context context) {
		mNavBar = new LinearLayout(context);
		mNavBar.setOrientation(LinearLayout.HORIZONTAL);
		mNavBar.setBackgroundResource(mNavBarBackground);
		mNavBar.setId(R.id.navigation_controller_nav_bar);
		mNavBar.setGravity(Gravity.CENTER_VERTICAL);

		addView(mNavBar, 0, new FrameLayout.LayoutParams(mNavBarWidth,
				mNavBarHeight));
		mNavBar.setVisibility(View.GONE);
	}

	public void setup(FragmentManager fmanager, int containerId) {
		mFragmentManager = fmanager;
		mContentContainerId = containerId;
		mEmptyFragment = EmptyFragment.newInstance();
		ensureHierarchy(getContext());
	}

	public void dispatchResume() {
		mIsSavedState = false;
		executeActionsAddedInBackground();
	}

	/**
	 * Push a fragment to current stack.
	 *
	 * @param fragment the new fragment
	 * @param tag      the tag of the fragment
	 * @param title    the title of the fragment
	 */
	public void pushFragment(Fragment fragment, String tag, String title) {
		pushFragment(fragment, tag, title, PUSH_MODE_NORMAL);
	}

	/**
	 * Push a fragment to current stack according the specified mode.
	 *
	 * @param fragment the new fragment
	 * @param tag      the tag of the fragment
	 * @param title    the title of the fragment
	 * @param mode     the mode of when push the fragment
	 * @see {@linkplain NavigationController#PUSH_MODE_NORMAL},
	 * {@linkplain NavigationController#PUSH_MODE_SINGLE_INSTANCE},
	 * {@linkplain NavigationController#PUSH_MODE_SINGLE_TOP}
	 */
	public void pushFragment(Fragment fragment, String tag, String title,
							 int mode) {
		if (fragment == null) {
			return;
		}

		// Pending the action to the queue when the controller is not in
		// foreground
		if (mIsSavedState) {
			enqueuePushFragmentAction(fragment, tag, title, mode);
			return;
		}

		FragmentInfo fragmentInfo = new FragmentInfo(fragment, tag, title);
		switch (mode) {
			case PUSH_MODE_SINGLE_INSTANCE:
				pop2Fragment(tag, FragmentManager.POP_BACK_STACK_INCLUSIVE);
				break;
			case PUSH_MODE_SINGLE_TOP:
				clearAllFragments();
				break;
			case PUSH_MODE_NORMAL:
			default:
				break;
		}

		FragmentTransaction ft = mFragmentManager.beginTransaction();
		ft.setCustomAnimations(R.animator.fragment_enter_animation,
				R.animator.fragment_exit_animation,
				R.animator.fragment_pop_enter_animation,
				R.animator.fragment_pop_exit_animation);
		ft.replace(mContentContainerId, fragment, tag);
		ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE);
		ft.addToBackStack(tag);
		ft.commit();

		addNavBarItem(title, tag);
		mFragmentsStack.push(fragmentInfo);
		mCurrentStackCount++;
	}

	private void enqueuePushFragmentAction(Fragment fragment, String tag,
										   String title, int mode) {
		Message msg = Message.obtain();
		Bundle data = new Bundle();
		data.putBundle(MESSAGE_DATA_FRAGMENT_ARGS, fragment.getArguments());
		data.putString(MESSAGE_DATA_FRAGMENT_NAME, fragment.getClass()
				.getName());
		data.putString(MESSAGE_DATA_FRAGMENT_TAG, tag);
		data.putString(MESSAGE_DATA_FRAGMENT_TITLE, title);
		msg.setData(data);
		msg.what = ACTION_PUSH_FRAGMENT;
		msg.arg1 = mode;
		mPendingQueue.add(msg);
	}

	public void popFragment() {
		// Pending the action to the queue when the controller is not in
		// foreground
		if (mIsSavedState) {
			enqueuePopFragmentAction();
			return;
		}

		if (mCurrentStackCount > 0) {
			mFragmentManager.popBackStack();
			mFragmentsStack.pop();
			removeNavBarItem();
			mCurrentStackCount--;
		}
	}

	private void enqueuePopFragmentAction() {
		Message msg = Message.obtain();
		msg.what = ACTION_POP_FRAGMENT;
		mPendingQueue.add(msg);
	}

	/**
	 * Pop to specified fragment.
	 *
	 * @param tag the tag of the fragment.
	 */
	public void pop2Fragment(String tag) {
		pop2Fragment(tag, 0);
	}

	/**
	 * Pop to specified fragment.
	 *
	 * @param tag   the tag of the fragment. {@link #POP_BACK_STACK_INCLUSIVE}
	 *              flag can be used to control whether the named fragment itself
	 *              is popped.
	 * @param flag Either 0 or {@link FragmentManager#POP_BACK_STACK_INCLUSIVE}.
	 */
	public void pop2Fragment(String tag, int flag) {

		if (mCurrentStackCount <= 0) {
			enqueuePop2FragmentAction(tag, flag);
			return;
		}

		Fragment fm = mFragmentManager.findFragmentByTag(tag);
		if (fm == null) {
			return;
		}

		// Pending the action to the queue when the controller is not in
		// foreground
		if (mIsSavedState) {

			return;
		}

		mFragmentManager.popBackStackImmediate(tag, flag);

		// pop all of the fragment up the specified fragment.
		while (mCurrentStackCount > 0 && mFragmentsStack.peek() != null
				&& !mFragmentsStack.peek().tag.equals(tag)) {
			popOneNavBarItem();
		}

		// If the flag is POP_BACK_STACK_INCLUSIVE, pop the specified fragment.
		if (flag == FragmentManager.POP_BACK_STACK_INCLUSIVE
				&& mFragmentsStack.peek().tag.equals(tag)) {
			popOneNavBarItem();
		}
	}

	private void enqueuePop2FragmentAction(String tag, int flag) {
		Message msg = Message.obtain();
		msg.what = ACTION_POP_TO_FRAGMENT;
		msg.arg1 = flag;
		msg.obj = tag;
		mPendingQueue.add(msg);
	}

	private void popOneNavBarItem() {
		mFragmentsStack.pop();
		removeNavBarItem();
		mCurrentStackCount--;
	}

	/**
	 * clear all the fragment in the stack
	 *
	 */
	public void clearAllFragments() {
		if (mCurrentStackCount == 0 && mFragmentsStack.size() == 0) {
			return;
		}

		// Pending the action to the queue when the controller is not in
		// foreground
		if (mIsSavedState) {
			enqueueClearAllFragmentAction();
			return;
		}

		FragmentInfo root = mFragmentsStack.elementAt(0);
		mFragmentsStack.clear();
		mCurrentStackCount = 0;
		mHiddenNavBarItemIndex = 0;

		if (root != null) {
			mFragmentManager.popBackStack(root.tag,
					FragmentManager.POP_BACK_STACK_INCLUSIVE);
		}
		clearAllBarItem();
	}

	private void enqueueClearAllFragmentAction() {
		Message msg = Message.obtain();
		msg.what = ACTION_CLEAR_FRAGMENT;
		mPendingQueue.add(msg);
	}

	/**
	 * Check whether have fragment in the controller.
	 *
	 * @return true if have fragment otherwise false.
	 */
	public boolean hasFragment() {
		return mFragmentsStack != null && mFragmentsStack.size() > 0;
	}

	/**
	 * get fragment stack size.
	 */
	public int getFragmentStackSize() {
		if (mFragmentsStack == null) {
			return 0;
		}
		return mFragmentsStack.size();
	}

	private void clearAllBarItem() {
		mNavBar.removeAllViews();
		mNavBar.setVisibility(GONE);
	}

	private void addNavBarItem(String title, String tag) {
		if (mIsShowDivider && hasFragment()) {
			mNavBar.addView(genDivider(), new FrameLayout.LayoutParams(
					ViewGroup.LayoutParams.WRAP_CONTENT,
					ViewGroup.LayoutParams.WRAP_CONTENT));
		}

		mNavBar.addView(genBarItem(title, tag));
		mNavBar.setVisibility(VISIBLE);
		hiddenPreNavbarItem();
	}

	/**
	 * Remove a navigation bar item
	 */
	private void removeNavBarItem() {
		if (mCurrentStackCount <= 0) {
			mNavBar.setVisibility(GONE);
			return;
		}

		int childCount = mNavBar.getChildCount();
		if (childCount > 0) {
			mNavBar.removeViewAt(--childCount);
			if (mIsShowDivider && childCount > 0) {
				mNavBar.removeViewAt(--childCount);
			}

			if (childCount == 0) {
				mNavBar.setVisibility(GONE);
			}
		}

		showPreNavbarItem();
	}

	private void hiddenPreNavbarItem() {
		if (mCurrentStackCount >= mMaxNavBarItemCount) {
			// Hidden the bar item
			mNavBar.getChildAt(mHiddenNavBarItemIndex++).setVisibility(
					View.GONE);

			// Hidden the divider if exists
			if (mIsShowDivider) {
				mNavBar.getChildAt(mHiddenNavBarItemIndex++).setVisibility(
						View.GONE);
			}
		}
	}

	private void showPreNavbarItem() {
		if (mCurrentStackCount >= 0 && mHiddenNavBarItemIndex > 0) {
			if (mIsShowDivider) {
				mNavBar.getChildAt(--mHiddenNavBarItemIndex).setVisibility(
						View.VISIBLE);
			}

			mNavBar.getChildAt(--mHiddenNavBarItemIndex).setVisibility(
					View.VISIBLE);
		}
	}

	private View genDivider() {
		View divider = null;
		if (mDividerResId != NO_ID) {
			divider = LayoutInflater.from(getContext()).inflate(mDividerResId,
					this, false);
		} else {
			divider = new TextView(getContext());
			divider.setBackgroundResource(R.drawable.ic_action_next);
		}
		return divider;
	}

	private View genBarItem(String label, String tag) {
		View item = null;
		if (mNavBarItemResId == NO_ID) {
			item = genDefaultBarItem(label);
		} else {
			item = LayoutInflater.from(getContext()).inflate(mNavBarItemResId,
					this, false);
			TextView tv = (TextView) item.findViewById(mNavBarItemTextViewId);
			if (tv == null) {
				tv = (TextView) item;
			}

			tv.setText(label);
		}

		item.setTag(tag);
		item.setId(R.id.navigation_controller_nav_bar_item1);
		item.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				String tag = (String) v.getTag();
				pop2Fragment(tag);
			}
		});

		return item;
	}

	private View genDefaultBarItem(String label) {
		TextView textView = new TextView(getContext());
		textView.setGravity(Gravity.CENTER);
		textView.setTextColor(getContext().getResources().getColor(
				R.color.white));
		int padding = FileUtils.dip2px(getContext(), 10);
		textView.setPadding(padding, padding, padding, padding);
		textView.setTextColor(mNavBarItemTextColor);
		textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, mNavBarItemTextSize);
		textView.setText(label);
		return textView;
	}

	@SuppressWarnings("unchecked")
	@Override
	protected Parcelable onSaveInstanceState() {
		mIsSavedState = true;
		Parcelable superState = super.onSaveInstanceState();
		SavedState ss = new SavedState(superState);
		ss.mFragments = (List<FragmentInfo>) (Object) Arrays
				.asList(mFragmentsStack.toArray());
		ss.mPendingMsg = mPendingQueue;
		return ss;
	}

	@Override
	protected void onRestoreInstanceState(Parcelable state) {
		mIsSavedState = false;
		SavedState ss = (SavedState) state;
		super.onRestoreInstanceState(ss.getSuperState());
		if (ss.mFragments != null && ss.mFragments.size() > 0) {
			for (int i = 0; i < ss.mFragments.size(); i++) {
				FragmentInfo fi = ss.mFragments.get(i);
				addNavBarItem(fi.title, fi.tag);
				mFragmentsStack.push(fi);
				mCurrentStackCount++;
			}
		}
//        mFragmentManager.getBackStackEntryCount();

		mPendingQueue = ss.mPendingMsg;
		if (mPendingQueue != null && !mPendingQueue.isEmpty()) {
			executeActionsAddedInBackground();
		}
	}

	/**
	 * Execute the actions that added when the controller in the background.
	 */
	private void executeActionsAddedInBackground() {
		if (mExecutePendingHanler == null) {
			return;
		}

		for (Message msg : mPendingQueue) {
			mExecutePendingHanler.sendMessage(msg);
		}

		// Clear the pending executions
		mPendingQueue.clear();
	}

	public static class EmptyFragment extends Fragment {

		public static EmptyFragment mFragment;
		private View mContent;

		public EmptyFragment() {

		}

		public static EmptyFragment newInstance() {
			if (mFragment == null) {
				mFragment = new EmptyFragment();
			}
			return mFragment;
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
								 Bundle savedInstanceState) {
			mContent = inflater.inflate(R.layout.activity_empty_fragment, container, false);
			return mContent;
		}

	}
}
