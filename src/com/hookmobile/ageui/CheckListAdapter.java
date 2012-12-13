package com.hookmobile.ageui;

import java.util.ArrayList;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.Resources.Theme;
import android.graphics.Color;
import android.util.TypedValue;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * List adapter with Check Box
 */
class CheckListAdapter extends BaseAdapter {

	private static final int LIST_PREFERED_HEIGHT = 65;
	private Context context = null;
	private ArrayList<CheckListViewItem> listItems = new ArrayList<CheckListViewItem>();

	public CheckListAdapter(Context context) {
		this.context = context;
	}

	public void addItem(CheckListViewItem listItem) {
		listItems.add(listItem);
	}

	public void clearAdapter() {
		listItems.clear();
	}

	@Override
	public int getCount() {
		return listItems.size();
	}

	@Override
	public Object getItem(int position) {
		return listItems.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		final CheckListViewItem item = (CheckListViewItem) getItem(position);

		TextView temp = null;
		Resources res = context.getResources();

		if (convertView == null) {

			LinearLayout ll = new LinearLayout(context);
			ll.setOrientation(LinearLayout.HORIZONTAL);
			ll.setBackgroundColor(Color.BLACK);

			final CheckBox cbox = new CheckBox(context);
			ll.addView(cbox);

			temp = new TextView(context);
			AbsListView.LayoutParams param = new AbsListView.LayoutParams(
					AbsListView.LayoutParams.FILL_PARENT,
					AbsListView.LayoutParams.WRAP_CONTENT);
			temp.setLayoutParams(param);
			temp.setPadding((int) toPixel(res, 15), 0, (int) toPixel(res, 15),
					0);
			temp.setTextColor(Color.RED);
			temp.setGravity(android.view.Gravity.CENTER_VERTICAL);

			Theme th = context.getTheme();
			TypedValue tv = new TypedValue();

			if (th.resolveAttribute(android.R.attr.textAppearanceLargeInverse,
					tv, true)) {
				temp.setTextAppearance(context, tv.resourceId);
			}

			temp.setMinHeight(LIST_PREFERED_HEIGHT);
			temp.setCompoundDrawablePadding((int) toPixel(res, 14));

			ll.addView(temp);

			convertView = ll;
		}

		LinearLayout ll = (LinearLayout) convertView;
		final CheckBox cbox = (CheckBox) ll.getChildAt(0);
		TextView textView = (TextView) ll.getChildAt(1);

		ll.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				if (cbox.isChecked()) {
					item.checkState = false;
					cbox.setChecked(false);
				} else {
					item.checkState = true;
					cbox.setChecked(true);
				}
			}
		});

		cbox.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				item.checkState = isChecked;
			}
		});

		if (item.checkState)
			cbox.setChecked(true);
		else
			cbox.setChecked(false);
		cbox.setTag(item);

		textView.setTag(item);
		textView.setText(item.name);
		textView.setTextColor(Color.WHITE);
		
		return ll;
	}

	private float toPixel(Resources res, int dip) {
		float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dip,
				res.getDisplayMetrics());
		return px;
	}
}
