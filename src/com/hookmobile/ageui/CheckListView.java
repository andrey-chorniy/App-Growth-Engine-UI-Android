package com.hookmobile.ageui;

import java.util.Calendar;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.text.format.DateFormat;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.AbsListView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class CheckListView extends ListView implements OnScrollListener {
	
	private final static int RELEASE_TO_REFRESH = 0;
	private final static int PULL_TO_REFRESH = 1;
	private final static int REFRESHING = 2;
	private final static int DONE = 3;
	
	private Context context;
	private LinearLayout headView;
	private TextView tipsTextview;
	private TextView lastUpdatedTextView;
	private ImageView arrowImageView;
	private ProgressBar progressBar;
	private Drawable downArrowPNG;
	private RotateAnimation animation;
	private RotateAnimation reverseAnimation;

	//To make sure startY's value is recorded only once in each onTouch event.
	private boolean isRecored;
	private int headContentHeight;
	private int startY;
	private int firstItemIndex;
	private int state;

	private boolean isBack;
	
	public OnRefreshListener refreshListener;
	public CheckListView(Context context) {
		super(context);
		
		this.context = context;
		init(context);
	}

	private void init(Context context) {

		Bitmap mBitmap = BitmapFactory.decodeStream(CheckListView.class.getClassLoader().getResourceAsStream("res/drawable-mdpi/pulltorefresh_down_arrow.png"));
		downArrowPNG = new BitmapDrawable(context.getResources(), Bitmap.createScaledBitmap(mBitmap, toPixel(20), toPixel(40), true));
		
		// Set Root LinearLayout
		headView = new LinearLayout(context);
		headView.setLayoutParams(new ListView.LayoutParams(ListView.LayoutParams.WRAP_CONTENT, ListView.LayoutParams.WRAP_CONTENT));
		headView.setBackgroundColor(0xff000000);

		// Set RelativeLayout in LinearLayout
		RelativeLayout mRelativeLayout = new RelativeLayout(context);
		mRelativeLayout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT));
		mRelativeLayout.setPadding(toPixel(20), 0, 0, 0);
		headView.addView(mRelativeLayout);
		
		// Set FrameLayout in RelativeLayout
		FrameLayout mFrameLayout = new FrameLayout(context);
		RelativeLayout.LayoutParams frameLayoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
		frameLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
		frameLayoutParams.addRule(RelativeLayout.CENTER_VERTICAL);
		mFrameLayout.setLayoutParams(frameLayoutParams);
		mRelativeLayout.addView(mFrameLayout);
		
		// Set ImageView in FrameLayout
		arrowImageView = new ImageView(context);
		arrowImageView.setLayoutParams(new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT));
		arrowImageView.setMinimumWidth(toPixel(40));
		arrowImageView.setMinimumHeight(toPixel(40));		
		arrowImageView.setImageDrawable(downArrowPNG);
		arrowImageView.setContentDescription("Pull to Refresh Arrow");
		mFrameLayout.addView(arrowImageView);
		
		// Set ProgressBar in FrameLayout
		progressBar = new ProgressBar(context, null, android.R.attr.progressBarStyleSmall);
		progressBar.setLayoutParams(new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT));
		progressBar.setVisibility(View.GONE);
		mFrameLayout.addView(progressBar);
		
		// Set LinearLayout in RelativeLayout
		LinearLayout mLinearLayout = new LinearLayout(context);
		RelativeLayout.LayoutParams linearLayoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
		linearLayoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
		mLinearLayout.setOrientation(LinearLayout.VERTICAL);
		mLinearLayout.setPadding(toPixel(40), 0, 0, 0);
		mLinearLayout.setGravity(Gravity.CENTER_HORIZONTAL);
		mRelativeLayout.addView(mLinearLayout);
		
		// Set Tips TextView in LinearLayout
		tipsTextview =  new TextView(context); 
		tipsTextview.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
		tipsTextview.setText("Pull to Refresh");
		tipsTextview.setTextColor(0xffffffff);
		tipsTextview.setTextSize(16.0f);
		mLinearLayout.addView(tipsTextview);
		
		// Set Last Update TextView in LinearLayout
		lastUpdatedTextView = new TextView(context); 
		lastUpdatedTextView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
		lastUpdatedTextView.setText("Last Update");
		lastUpdatedTextView.setTextColor(0xffcc6600);
		lastUpdatedTextView.setTextSize(10.0f);
		lastUpdatedTextView.setVisibility(View.GONE);
		mLinearLayout.addView(lastUpdatedTextView);
		
		// Set headView width and height
		measureView(headView);
		headContentHeight = headView.getMeasuredHeight();
		headView.setPadding(0, toPixel(-1 * headContentHeight), 0, 0);
		headView.invalidate();
		addHeaderView(headView);
		setOnScrollListener(this);
		
		// Set Animation
		animation = new RotateAnimation(0, -180,
				RotateAnimation.RELATIVE_TO_SELF, 0.5f,
				RotateAnimation.RELATIVE_TO_SELF, 0.5f);
		animation.setInterpolator(new LinearInterpolator());
		animation.setDuration(250);
		animation.setFillAfter(true);
		reverseAnimation = new RotateAnimation(-180, 0,
				RotateAnimation.RELATIVE_TO_SELF, 0.5f,
				RotateAnimation.RELATIVE_TO_SELF, 0.5f);
		reverseAnimation.setInterpolator(new LinearInterpolator());
		reverseAnimation.setDuration(250);
		reverseAnimation.setFillAfter(true);
		
		state = DONE;
	}

	public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
		firstItemIndex = firstVisibleItem;
	}

	public void onScrollStateChanged(AbsListView view, int scrollState) {
	}

	public boolean onTouchEvent(MotionEvent event) {
		
		switch (event.getAction()) {
		
		case MotionEvent.ACTION_DOWN:
			if (firstItemIndex == 0 && !isRecored) {
				startY = (int) event.getY();
				isRecored = true;
			}
			break;
		case MotionEvent.ACTION_UP:
			if (state != REFRESHING) {
				if (state == DONE) {
					//do nothing
				}
				if (state == PULL_TO_REFRESH) {
					state = DONE;
					changeHeaderViewByState();
				}
				if (state == RELEASE_TO_REFRESH) {
					state = REFRESHING;
					changeHeaderViewByState();
					onRefresh();
				}
			}
			isRecored = false;
			isBack = false;
			break;
		case MotionEvent.ACTION_MOVE:
			int tempY = (int) event.getY();
			if (!isRecored && firstItemIndex == 0) {

				isRecored = true;
				startY = tempY;
			}
			if (state != REFRESHING && isRecored) {
				
				//Able to release to refresh
				if (state == RELEASE_TO_REFRESH) {

					if ((tempY - startY < headContentHeight * 2)
							&& (tempY - startY) > 0) {
						state = PULL_TO_REFRESH;
						changeHeaderViewByState();
					}

					else if (tempY - startY <= 0) {
						state = DONE;
						changeHeaderViewByState();
					}

					else {

					}
				}

				//Not reach to display refresh, DONE or PULL_TO_REFRESH.
				if (state == PULL_TO_REFRESH) {

					if (tempY - startY >= headContentHeight * 2) {
						state = RELEASE_TO_REFRESH;
						isBack = true;
						changeHeaderViewByState();
					}

					else if (tempY - startY <= 0) {
						state = DONE;
						changeHeaderViewByState();
					}
				}

				//DONE
				if (state == DONE) {
					if (tempY - startY > 0) {
						state = PULL_TO_REFRESH;
						changeHeaderViewByState();
					}
				}

				//Update the size of headView
				if (state == PULL_TO_REFRESH) {
					headView.setPadding(0, -1 * headContentHeight
							+ (tempY - startY), 0, 0);
					headView.invalidate();
				}

				//Update the paddingTop of headView
				if (state == RELEASE_TO_REFRESH) {
					headView.setPadding(0, (tempY - startY) / 2,
							0, 0);
					headView.invalidate();
				}
			}
			break;
		}
		return super.onTouchEvent(event);
	}

	/**
	 * Change view when state is changed.
	 * 
	 */
	private void changeHeaderViewByState() {
		switch (state) {
		case RELEASE_TO_REFRESH:
			arrowImageView.setVisibility(View.VISIBLE);
			progressBar.setVisibility(View.GONE);
			tipsTextview.setVisibility(View.VISIBLE);
			lastUpdatedTextView.setVisibility(View.VISIBLE);
			arrowImageView.clearAnimation();
			arrowImageView.startAnimation(animation);
			tipsTextview.setText("Release to Refresh");
			break;
		case PULL_TO_REFRESH:
			progressBar.setVisibility(View.GONE);
			tipsTextview.setVisibility(View.VISIBLE);
			lastUpdatedTextView.setVisibility(View.VISIBLE);
			arrowImageView.clearAnimation();
			arrowImageView.setVisibility(View.VISIBLE);
			//Changed from RELEASE_TO_REFRESH
			if (isBack) {
				isBack = false;
				arrowImageView.clearAnimation();
				arrowImageView.startAnimation(reverseAnimation);
				tipsTextview.setText("Pull to Refresh");
			} else {
				tipsTextview.setText("Pull to Refresh");
			}
			break;
		case REFRESHING:
			headView.setPadding(0, 0, 0, 0);
			headView.invalidate();
			progressBar.setVisibility(View.VISIBLE);
			arrowImageView.clearAnimation();
			arrowImageView.setVisibility(View.GONE);
			tipsTextview.setText("Refreshing...");
			lastUpdatedTextView.setVisibility(View.VISIBLE);
			break;
		case DONE:
			headView.setPadding(0, toPixel(-1 * headContentHeight), 0, 0);
			headView.invalidate();
			progressBar.setVisibility(View.GONE);
			arrowImageView.clearAnimation();
			arrowImageView.setImageDrawable(downArrowPNG);
			tipsTextview.setText("Pull to Refresh");
			lastUpdatedTextView.setVisibility(View.GONE);
			break;
		}
	}

	public void setOnRefreshListener(OnRefreshListener refreshListener) {
		this.refreshListener = refreshListener;
	}

	public interface OnRefreshListener {
		public void onRefresh();
	}

	public void onRefreshComplete() {
		state = DONE;
		changeHeaderViewByState();
		lastUpdatedTextView.setText("Last Update: " 
				+ DateFormat.getDateFormat(this.getContext()).format(Calendar.getInstance().getTime()) + " "
				+ DateFormat.getTimeFormat(this.getContext()).format(Calendar.getInstance().getTime()));		
	}

	private void onRefresh() {
		if (refreshListener != null) {
			refreshListener.onRefresh();
			onRefreshComplete();
		}
	}

	/**
	 * Estimate width and height of header
	 * 
	 * @param child
	 */
	private void measureView(View child) {
		ViewGroup.LayoutParams p = child.getLayoutParams();
		if (p == null) {
			p = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
					ViewGroup.LayoutParams.WRAP_CONTENT);
		}
		int childWidthSpec = ViewGroup.getChildMeasureSpec(0, 0 + 0, p.width);
		int lpHeight = p.height;
		int childHeightSpec;
		if (lpHeight > 0) {
			childHeightSpec = MeasureSpec.makeMeasureSpec(lpHeight,
					MeasureSpec.EXACTLY);
		} else {
			childHeightSpec = MeasureSpec.makeMeasureSpec(0,
					MeasureSpec.UNSPECIFIED);
		}
		child.measure(childWidthSpec, childHeightSpec);
	}
	
	@Override
    protected void dispatchDraw(Canvas canvas) {
        try {
            super.dispatchDraw(canvas);
        } catch (IndexOutOfBoundsException e) {
            // Ignore a Samsung bug
        }
    }
	
    @Override
    protected void layoutChildren() {
    	try {
    		super.layoutChildren();
    	} catch (IllegalStateException e) {
    		// Ignore the item count bug when notifyDataSetChanged() was called to update a ListView with a header view
    	}
    }
	
	private int toPixel(int dip) {
		float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dip,
				context.getResources().getDisplayMetrics());
		return (int)px;
	}
}