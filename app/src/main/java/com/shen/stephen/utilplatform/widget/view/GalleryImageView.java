package com.shen.stephen.utilplatform.widget.view;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.OnDoubleTapListener;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

@SuppressLint("ClickableViewAccessibility")
public class GalleryImageView extends ImageView implements
		GestureDetector.OnGestureListener, OnDoubleTapListener {

	public final static float DEFAULT_ZOOM_OUT_SCALE = 0.8f;
	public final static float DEFAULT_ZOOM_IN_SCALE = 2.0f;

	public final static float MAX_ZOOM_IN_SCALE = 5.0f;
	public final static float MIN_ZOOM_OUT_SCALE = 0.5f;

	public final static int ZOOMSTATUS_NO_ZOOM = 0x0000;
	public final static int ZOOMSTATUS_ZOOM_IN = 0x0001;
	public final static int ZOOMSTATUS_ZOOM_OUT = 0x0001 << 1;
	public final static int ZOOMSTATUS_ZOOMMING = 0x0001 << 2;
	public final static int ZOOMSTATUS_DOUBLE_CLICK = 0x0001 << 3;

	private Object syncObject = new Object();

	private boolean mZooming = false;
	private int mZoomStatus = ZOOMSTATUS_NO_ZOOM;
	private boolean mZoomAble = true;

	// the bridge matrix between baseMatrix and transform matrix
	private Matrix bridgeMatrix = new Matrix();
	// The base matrix this matrix can transform an original image to fit center
	// of the screen.
	private Matrix baseMatrix = new Matrix();
	// the matrix which transform the image.
	private Matrix transformMatrix = new Matrix();

	private int originalWidth = -1;
	private int originalHeight = -1;

	private int currentWidth = -1;
	private int currentHeight = -1;

	private float originalScale = 1.0f;
	private float currentScale = 1.0f;
	private float scaleBeforeZoom = 1.0f;

	private Drawable mDrawable = null;
	private boolean mIsFinishedScroll = false;
	private Point mZoomOutPivotPoint;

	private int mScreenHeight;
	private int mScreenWidth;

	private Context mContext;

	private GestureDetector mGestureDetector;

	protected Handler mHandler = new Handler();

	@SuppressWarnings("deprecation")
	public GalleryImageView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		mContext = context;
		this.setScaleType(ImageView.ScaleType.MATRIX);
		WindowManager localWindowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
		mScreenWidth = localWindowManager.getDefaultDisplay().getWidth();
		mScreenHeight = localWindowManager.getDefaultDisplay().getHeight();

		mGestureDetector = new GestureDetector(this);
		mGestureDetector.setOnDoubleTapListener(this);
		setOnTouchListener(new OnTouchListener() {
			float baseValue;
			Point center;

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_DOWN) {
					baseValue = 0;
					if (event.getPointerCount() == 2) {
						center = new Point(
								(int) (event.getX(0) + event.getX(1)) / 2,
								(int) (event.getY(0) + event.getY(1)) / 2);
						willZoom();
						return true;
					}

				} else if (event.getAction() == MotionEvent.ACTION_MOVE) {

					// the pinch gesture recognizer
					if (event.getPointerCount() == 2) {
						float x = event.getX(0) - event.getX(1);
						float y = event.getY(0) - event.getY(1);
						// calculate the distance of two points.
						float value = (float) Math.sqrt(x * x + y * y);
						if (baseValue == 0) {
							baseValue = value;
							willZoom();

						} else {
							// Calculate the scale.
							float scale = value / baseValue;
							// scale the image
							zoom(center, scale);
						}
						// intercept TouchEvent
						return true;
					}
					if (isZooming()) {
						return true;
					}
				}

				return false;
			}

		});
	}

	public GalleryImageView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public GalleryImageView(Context context) {
		this(context, null);
	}

	@Override
	public void setImageBitmap(Bitmap bm) {
		super.setImageBitmap(bm);
	}

	@Override
	public void setImageDrawable(Drawable drawable) {
		super.setImageDrawable(drawable);
		if (drawable == null) {
			currentWidth = originalWidth = mScreenWidth;
			currentHeight = originalHeight = mScreenHeight;
		} else {
			currentWidth = originalWidth = drawable.getIntrinsicWidth();
			currentHeight = originalHeight = drawable.getIntrinsicHeight();
		}
		layoutImageLocationToFitCenter();
	}

	@Override
	public void setImageResource(int resId) {
		super.setImageResource(resId);
		currentWidth = originalWidth = super.getDrawable().getIntrinsicWidth();
		currentHeight = originalHeight = super.getDrawable().getIntrinsicHeight();
		layoutImageLocationToFitCenter();

	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
			event.startTracking();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && event.isTracking()
				&& !event.isCanceled()) {
			if (currentScale > originalScale) {
				// If we're zoomed in, pressing Back jumps out to show the
				// entire image, otherwise Back returns the user to the gallery.
				zoomToOriginal(null);
				return true;
			}
		}
		return super.onKeyUp(keyCode, event);
	}

	/**
	 * set the image of the this imageView. Using this method can avoid image
	 * jitter when gallery load async image. If you haven't this problem you
	 * didn't use it. This method called in two place, first be called after
	 * load the async image, this time the parameter isFinishedScroll should be
	 * false, and the another place is after stop the scroll, this time
	 * isFinishedScroll must be true. The image be set only after called this
	 * method in that two place. Note that you can alse call this method in one
	 * place and set isFinishedScroll to true drawable not null, then the image
	 * will be set, but we are not recommend, the method loss its role if you do
	 * like that.
	 *
	 * @param isFinishedScroll
	 *            specify whether finished the scroll.
	 * @param drawable
	 *            the image's drawable object.
	 */
	public synchronized void setImageDrawableDuringScroll(
			boolean isFinishedScroll, Drawable drawable) {
		if (isFinishedScroll) {
			mIsFinishedScroll = true;
		}
		if (drawable != null) {
			mDrawable = drawable;
		}
		if (mIsFinishedScroll && mDrawable != null) {
			setImageDrawable(mDrawable);
			mIsFinishedScroll = false;
			mDrawable = null;
		}
	}

	public int getCurrentWidth() {
		return currentWidth;
	}

	public int getCurrentHeight() {
		return currentHeight;
	}

	public boolean isZooming() {
		return mZoomStatus == ZOOMSTATUS_ZOOMMING;
	}

	public boolean isZoomAble() {
		synchronized (syncObject){
			return mZoomAble;
		}
	}

	public void doubleClick(Point fixedPoint) {
		// change the zoom status to zoom double click before zoom in the image
		mZoomStatus = ZOOMSTATUS_DOUBLE_CLICK;
		// set the bridgeMatrix to baseMatrix before zoom in the image.
		bridgeMatrix.set(baseMatrix);
		zoomTo(fixedPoint, DEFAULT_ZOOM_IN_SCALE, 200);
	}

	public void setZoomAble(boolean isZoomAble) {
		synchronized (syncObject){
			mZoomAble = isZoomAble;
		}
	}

	public int getZoomStatus() {
		return mZoomStatus;
	}

	public void removeFromParentView() {
		// this.mParent = null;
	}

	/**
	 * zoom the image to original size the original size fit center of the
	 * screen.
	 */
	public void zoomToOriginal(Point pivotPoint) {
		zoomTo(pivotPoint, 1.0f, 300);
		mZoomStatus = ZOOMSTATUS_NO_ZOOM;
	}

	/**
	 * translate the image to a new position.
	 *
	 * @param distanceX
	 *            horizontal transform distance.
	 * @param distanceY
	 *            vertical transform distance.
	 */
	public void translate(float distanceX, float distanceY) {
		if (transformMatrix.isIdentity()) {
			transformMatrix.set(baseMatrix);
		}
		transformMatrix.postTranslate(distanceX, distanceY);
		setImageMatrix(transformMatrix);
	}

	/**
	 * translate the image to a new position during the duration time.
	 *
	 * @param distanceX
	 *            horizontal transform distance.
	 * @param distanceY
	 *            vertical transform distance.
	 * @param duration
	 *            the duration time.
	 */
	public void translate(float distanceX, float distanceY, final long duration) {
		final float xRate = distanceX / duration;
		final float yRate = distanceY / duration;
		if (duration <= 0) {
			translate(distanceX, distanceY);
		} else {
			final long startTime = System.currentTimeMillis();
			mHandler.post(new Runnable() {

				float translatedDistanceX = 0;
				float translatedDistanceY = 0;

				@Override
				public void run() {
					long now = System.currentTimeMillis();
					long currentMs = Math.min(now - startTime, duration);
					translate(currentMs * xRate - translatedDistanceX,
							currentMs * yRate - translatedDistanceY);
					translatedDistanceX = currentMs * xRate;
					translatedDistanceY = currentMs * yRate;
					if (currentMs < duration) {
						mHandler.post(this);
					}
				}

			});
		}
	}

	/**
	 *
	 * zoom in the image, the center of the image is the specified fixPoint.
	 *
	 * @param fixedPoint
	 *            the image scale center point, if the point is null the center
	 *            point will be the center of the screen.
	 */
	public void zoomIn(Point fixedPoint) {
		zoom(fixedPoint, DEFAULT_ZOOM_IN_SCALE);
	}

	/**
	 *
	 * zoom out the image, the center of the image is the specified fixPoint.
	 *
	 * @param fixedPoint
	 *            the image scale center point, if the point is null the center
	 *            point will be the center of the screen.
	 */
	public void zoomOut(Point fixedPoint) {
		zoom(fixedPoint, DEFAULT_ZOOM_OUT_SCALE);
	}

	/**
	 * do the zoom initialized. change the zoom status to ZOOMSTATUS_NO_ZOOM,
	 * set the bridgeMatrix and scaleBeforeZoom.
	 */
	public void willZoom() {
		// set the bridgeMatrix before zoom.
		synchronized (syncObject) {
			mZoomStatus = ZOOMSTATUS_NO_ZOOM;
			bridgeMatrix.set(getImageMatrix());
			scaleBeforeZoom = getWidthScaleFromMatrix(bridgeMatrix);
			// Log.e("******scaleBeforeZoom init******", "Init ScaleBeforeZoom:"
			// + scaleBeforeZoom);
		}
	}

	/**
	 * finish zoom and adjust the zoom result.
	 */
	public void finishZoom() {
		synchronized (syncObject) {
			adjustZoom();
			if (currentScale < originalScale) {
				mZoomStatus = ZOOMSTATUS_ZOOM_OUT;
			} else {
				mZoomStatus = ZOOMSTATUS_ZOOM_IN;
			}
			scaleBeforeZoom = 0.0f;
		}
	}

	/**
	 *
	 * zoom out/in the image, the center of the image is the specified fixPoint.
	 *
	 * @param pivotPoint
	 *            the pivot point of the image zoom process, if the point is
	 *            null the center point will be the center of the screen.
	 * @param zoomScale
	 *            the zoom scale, the reference is current image zoom scale, so
	 *            that if zoomScale is 2 and current scale is 2 the real
	 *            zoomScale is 2 * 2 = 4.
	 */
	public void zoom(Point pivotPoint, float zoomScale) {
		synchronized (syncObject) {
			if (!mZoomAble || mZooming)
				return;

			mZooming = true;
			mZoomAble = false;

			if (null == pivotPoint/* || zoomRate * originalScale < 1 */) {
				pivotPoint = new Point(mScreenWidth / 2, mScreenHeight / 2);
			}
			// Log.e("<<<<Zoom rate>>>>", "CurrentScale:" + currentScale +
			// " ZoomRate:" + zoomRate + " ScaleBeforeZoom:" + scaleBeforeZoom);
			if (mZoomStatus != ZOOMSTATUS_DOUBLE_CLICK
					&& scaleBeforeZoom * zoomScale < MIN_ZOOM_OUT_SCALE
					* originalScale) {
				zoomScale = MIN_ZOOM_OUT_SCALE * originalScale
						/ scaleBeforeZoom;
			}
			if (mZoomStatus != ZOOMSTATUS_DOUBLE_CLICK) {
				mZoomStatus = ZOOMSTATUS_ZOOMMING;
			}

			zoomProcess(zoomScale, zoomScale, pivotPoint.x, pivotPoint.y);
			mZooming = false;
			mZoomAble = true;
		}
	}

	/**
	 * Zoom the image to the specified scale during the duration time, the scale
	 * is base on originalScale(if the scale is 2 the real scale is 2 *
	 * originalScale).
	 *
	 * @param pivotPoint
	 *            the pivot point of the image zoom process, if the point is
	 *            null the center point will be the center of the screen.
	 * @param zoomScale
	 *            the zoom scale, the reference is originalScale, so that if
	 *            zoomScale is 2 the real zoomScale is 2 * originalScale.
	 * @param duration
	 *            The transform animation duration. Note that if the duration is
	 *            0 or less 0 represent no duration.
	 */
	public void zoomTo(Point pivotPoint, float zoomScale, final long duration) {
		synchronized (syncObject) {
			if (!mZoomAble || mZooming)
				return;

			mZooming = true;
			mZoomAble = false;

			// if pivotPoint is null or the height/width of image after zoom is
			// smaller than the screen's width/height chenge the pivotPoint to
			// the center
			if (null == pivotPoint
					|| zoomScale * originalScale * originalWidth < mScreenWidth
					|| zoomScale * originalScale * originalHeight < mScreenHeight) {
				pivotPoint = new Point(mScreenWidth / 2, mScreenHeight / 2);
			}
			if (mZoomStatus != ZOOMSTATUS_DOUBLE_CLICK
					&& scaleBeforeZoom * zoomScale < MIN_ZOOM_OUT_SCALE
					* originalScale) {
				zoomScale = MIN_ZOOM_OUT_SCALE * originalScale
						/ scaleBeforeZoom;
			}

			if (mZoomStatus != ZOOMSTATUS_DOUBLE_CLICK) {
				mZoomStatus = ZOOMSTATUS_ZOOMMING;
			}
			bridgeMatrix.set(baseMatrix);
			if (duration <= 0) {
				zoomProcess(zoomScale, zoomScale, pivotPoint.x, pivotPoint.y);
				mZooming = false;
				mZoomAble = true;
			} else {

				final long startTime = System.currentTimeMillis();
				final Point tempPivotPoint = pivotPoint;
				final float startScale = getWidthScaleFromMatrix(getImageMatrix());
				final float zoomSpeed = startScale < zoomScale * originalScale ? (zoomScale - 1)
						/ duration
						: (startScale - zoomScale * originalScale) / duration;
				final float targetScale = zoomScale;
				mHandler.post(new Runnable() {
					public void run() {
						long now = System.currentTimeMillis();
						float currentMs = Math.min(duration, now - startTime);
						// get current scale
						float currentScale = startScale < targetScale
								* originalScale ? 1 + zoomSpeed * currentMs
								: startScale / originalScale - zoomSpeed
								* currentMs / originalScale;
						zoomProcess(currentScale, currentScale, tempPivotPoint.x, tempPivotPoint.y);

						if (currentMs < duration) {
							mHandler.post(this);
						} else {
							synchronized (syncObject) {
								mZooming = false;
								mZoomAble = true;
							}
						}
					}
				});
			}

		}
	}

	/**
	 * The zoom process
	 *
	 * @param scaleX
	 *            the horizontal zoom scale.
	 * @param scaleY
	 *            the vertical zoom scale.
	 * @param pivotPointXLocation
	 *            the X location of the pivot point.
	 * @param pivotPointYLocation
	 *            the Y location of the pivot point.
	 */
	private void zoomProcess(float scaleX, float scaleY, float pivotPointXLocation, float pivotPointYLocation) {
		transformMatrix.set(bridgeMatrix);
		transformMatrix.postScale(scaleX, scaleY, pivotPointXLocation, pivotPointYLocation);
		setImageMatrix(transformMatrix);

		currentScale = getWidthScaleFromMatrix(transformMatrix);
		currentHeight = (int) (currentScale * originalHeight);
		currentWidth = (int) (currentScale * originalWidth);
	}

	/**
	 * Adjust the result of the zoom. if the zoom in scale bigger than the max
	 * scale or zoom out scale smaller than the min zoom out scale change the
	 * image zoom scale to a fit scale. And set tempMatrix to baseMatrix, change
	 * the status of zoom to ZOOMSTATUS_NO_ZOOM,if the
	 */
	private void adjustZoom() {
		if (currentScale <= DEFAULT_ZOOM_OUT_SCALE * originalScale) {
			zoom(null, DEFAULT_ZOOM_OUT_SCALE * originalScale / scaleBeforeZoom);
		} else if (currentScale > MAX_ZOOM_IN_SCALE * originalScale) {
			zoom(null, MAX_ZOOM_IN_SCALE * originalScale / scaleBeforeZoom);
		}
		bridgeMatrix.set(baseMatrix);
		if (currentWidth < mScreenWidth || currentHeight < mScreenHeight) {
			center(true, true);
		} else {
			adjustScroll();
		}
	}

	/**
	 * adjust the location of the image after scroll, note that it work only
	 * when the image's size bigger than the screen view's size.
	 */
	public void adjustScroll() {
		Matrix m = transformMatrix;
		RectF rect = new RectF(0, 0, originalWidth, originalHeight);
		m.mapRect(rect);
		float deltaX = 0;
		float deltaY = 0;
		int viewHeight = getHeight();
		int viewWidth = getWidth();

		// if the size of the image smaller than the screen return,do nothing.
		if (viewHeight > rect.height() && viewWidth > rect.width()) {
			return;
		}

		// if the image's height bigger than the height of the screen and the
		// width smaller than the screen's.
		if (rect.height() >= viewHeight && rect.width() < viewWidth) {
			// adjust the image to horizontal center
			deltaX += (viewWidth - currentWidth) / 2 - rect.left;
			if (rect.top > 0) {
				deltaY -= rect.top;
			} else if (rect.bottom < viewHeight) {
				deltaY += viewHeight - rect.bottom;
			}
		}
		// if the image's width bigger than the width of the screen and the
		// height smaller than the screen's.
		else if (rect.width() >= viewWidth && rect.height() < viewHeight) {
			// adjust the image to vertical center
			deltaY += (viewHeight - currentHeight) / 2 - rect.top;
			if (rect.left > 0) {
				deltaX -= rect.left;
			} else if (rect.right < viewWidth) {
				deltaX += viewWidth - rect.right;
			}
		} else {
			// Detect the left and right if has free space.
			if (rect.left > 0) {
				deltaX -= rect.left;
			} else if (rect.right < viewWidth) {
				deltaX += viewWidth - rect.right;
			}

			// Detect the top and bottom if has free space.
			if (rect.top > 0) {
				deltaY -= rect.top;
			} else if (rect.bottom < viewHeight) {
				deltaY += viewHeight - rect.bottom;
			}
		}
		translate(deltaX, deltaY, 300);
	}

	/**
	 * Get the width scale from a matrix.
	 *
	 * @param aMatrix
	 *            the matrix to calculate.
	 * @return return return the height zoom scale.
	 */
	private float getWidthScaleFromMatrix(Matrix aMatrix) {
		float[] temp = new float[9];
		transformMatrix.getValues(temp);
		return temp[Matrix.MSCALE_X];
	}

	/**
	 * Get the width scale from a matrix.
	 *
	 * @param aMatrix
	 *            the matrix to calculate.
	 * @return return return the width zoom scale.
	 */
	protected float getHeightScaleFromMatrix(Matrix aMatrix) {
		float[] temp = new float[9];
		transformMatrix.getValues(temp);
		return temp[Matrix.MSCALE_Y];
	}

	/**
	 * Adjust the location of the image to center according the parameter if the
	 * horizontal is true specify horizontal center, vertical is specify
	 * vertical center.
	 *
	 * @param horizontal
	 *            specify is horizontal center.
	 * @param vertical
	 *            specify is vertical center.
	 */
	protected void center(boolean horizontal, boolean vertical) {

		if (!vertical && !horizontal)
			return;
		Matrix m = transformMatrix;

		RectF rect = new RectF(0, 0, originalWidth, originalHeight);
		m.mapRect(rect);

		float deltaX = 0, deltaY = 0;

		int viewHeight = getHeight();
		int viewWidth = getWidth();
		if (vertical) {
			deltaY += (viewHeight - currentHeight) / 2 - rect.top;
			// if the width of the image bigger than screen's width adjust the
			// image location if the left or right of the image has free space
			if (rect.right < viewWidth && viewWidth < currentWidth) {
				deltaX += viewWidth - rect.right;
			} else if (rect.left > 0 && viewWidth < currentWidth) {
				deltaX -= rect.left;
			}
		}

		if (horizontal) {
			deltaX += (viewWidth - currentWidth) / 2 - rect.left;
			// if the height of the image bigger than screen's height adjust the
			// image location if the top or bottom of the image has free space
			if (rect.bottom < viewHeight && viewHeight < currentHeight) {
				deltaY += viewHeight - rect.bottom;
			} else if (rect.top > 0 && viewHeight < currentHeight) {
				deltaY -= rect.top;
			}
		}
		translate(deltaX, deltaY, 200);
	}

	/**
	 * Detect the point is in the area of the image
	 *
	 * @param aPoint
	 *            the detected point
	 * @return return true if the point s in the area of the image, else return
	 *         false.
	 */
	protected boolean isPointOutOfTheImageArea(Point aPoint) {

		RectF imageRect = new RectF(0, 0, originalWidth, originalHeight);
		transformMatrix.mapRect(imageRect);
		if (aPoint.x > imageRect.left
				&& imageRect.left + currentWidth > aPoint.x
				&& aPoint.y > imageRect.top
				&& imageRect.top + currentHeight > aPoint.y) {
			return false;
		}
		return true;
	}

	/**
	 * Layout the image to fit center when the image first load. This method
	 * only invoked when set image.
	 */
	private void layoutImageLocationToFitCenter() {
		transformMatrix.set(new Matrix());

		// if the image's width or height bigger than the screen's width or
		// height scale the image to fit the screen size

		// scale the original image to fit the screen
		float scaleWidth = mScreenWidth / (float) originalWidth;
		float scaleHeight = mScreenHeight / (float) originalHeight;
		currentScale = originalScale = Math.min(scaleWidth, scaleHeight);

		transformMatrix.postScale(originalScale, originalScale,
				originalWidth / 2, originalHeight / 2);
		// }

		// adjust the location of the image to the center of the screen.
		int deltaX = (mScreenWidth - originalWidth) / 2;
		int deltaY = (mScreenHeight - originalHeight) / 2;

		transformMatrix.postTranslate(deltaX, deltaY);
		this.setImageMatrix(transformMatrix);
		baseMatrix.set(transformMatrix);

		currentWidth = (int) (originalWidth * originalScale);
		currentHeight = (int) (originalHeight * originalScale);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		// Give everything to the gesture detector
		if (mGestureDetector.onTouchEvent(event)) {
			return true;
		}

		switch (event.getAction()) {
			case MotionEvent.ACTION_UP: {
				float width = getCurrentWidth();
				float height = getCurrentHeight();

				if (isZooming()) {
					finishZoom();
					break;
				}

				// adjust the image location after scroll
				if ((int) width <= mScreenWidth && (int) height <= mScreenHeight) //
				{
					break;
				} else if ((int) width > mScreenWidth && (int) height < mScreenHeight) {
					// if the width of image bigger then the screen width and
					// height of the image smaller the height of screen.
					center(false, true);
				} else if ((int) width < mScreenWidth
						&& (int) height > mScreenHeight) {
					// if the height of image bigger then the screen height and
					// width of the image smaller the width of screen.
					center(true, false);
				} else {
					adjustScroll();
				}

			}
			break;
			case MotionEvent.ACTION_MOVE:
				if (event.getPointerCount() == 2) {
					return true;
				} else if (isZooming()) {
					return true;
				}
				break;
			case MotionEvent.ACTION_CANCEL:
				Log.e("Touch Cancel", "IsTouching = false");
		}
		return true;
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		final int height = getMeasuredHeight();
		if (height < mScreenHeight) {
			mScreenHeight = height;
		}
	}

	@Override
	public boolean onDown(MotionEvent e) {
		return false;
	}

	@Override
	public void onShowPress(MotionEvent e) {
	}

	@Override
	public boolean onSingleTapUp(MotionEvent e) {
		return false;
	}

	@Override
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
							float distanceY) {
		if (e1.getPointerCount() != 2 && e2.getPointerCount() != 2) {
			float v[] = new float[9];
			Matrix m = getImageMatrix();
			m.getValues(v);

			float left, right;
			float width = 0, height = 0;
			width = getCurrentWidth();
			height = getCurrentHeight();

			if ((int) width <= mScreenWidth && (int) height <= mScreenHeight) {
				return false;
			} else {
				left = v[Matrix.MTRANS_X];
				right = left + width;
				Rect r = new Rect();
				getGlobalVisibleRect(r);

				// Scroll to left
				if (distanceX > 0) {
					// check whether the hole image is shown or not
					if (r.left > 0) {
						return false;
					} else if (right < mScreenWidth) {
						return false;
					} else {
						translate(-distanceX, -distanceY);
					}
				} else if (distanceX < 0) {
					// Scroll to right
					if (r.right < mScreenWidth) {
						return false;
					} else if (left > 0) {
						return false;
					} else {
						translate(-distanceX, -distanceY);
					}
				}
			}

		} else if (e1.getPointerCount() != 2 && e2.getPointerCount() != 2) {
			return false;
		}

		return true;
	}

	@Override
	public void onLongPress(MotionEvent e) {
		// Nothing to do.
	}

	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
						   float velocityY) {
		if (e1.getPointerCount() == 2 || e2.getPointerCount() == 2) {
			return false;
		} else {

			if (getZoomStatus() != ZOOMSTATUS_NO_ZOOM) {
				return false;
			}
		}
		return true;
	}

	@Override
	public boolean onSingleTapConfirmed(MotionEvent e) {
		((Activity) mContext).finish();
		return false;
	}

	@Override
	public boolean onDoubleTap(MotionEvent e) {

		if (getZoomStatus() == ZOOMSTATUS_DOUBLE_CLICK) {
			zoomToOriginal(mZoomOutPivotPoint);
			mZoomOutPivotPoint = null;
		} else {
			mZoomOutPivotPoint = new Point((int) e.getX(), (int) e.getY());
			doubleClick(mZoomOutPivotPoint);
		}

		return true;
	}

	@Override
	public boolean onDoubleTapEvent(MotionEvent e) {
		return false;
	}

}
