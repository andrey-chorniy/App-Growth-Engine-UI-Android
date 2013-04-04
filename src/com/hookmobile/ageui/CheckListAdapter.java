package com.hookmobile.ageui;

import java.util.ArrayList;

import com.hookmobile.age.AgeException;
import com.hookmobile.age.Discoverer;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.Resources.Theme;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.provider.ContactsContract;
import android.util.TypedValue;
import android.view.Gravity;
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
	private static final int CONVERT_VIEW_TYPE_CLICK = 0;
	private static final int CONVERT_VIEW_TYPE_ITEM = 1;
	
	private Context context = null;
	private Handler handler;
	private ArrayList<CheckListViewItem> listItems = new ArrayList<CheckListViewItem>();
	
	public boolean hasExtended = false;

	public CheckListAdapter(Context context, Handler handler) {
		this.context = context;
		this.handler = handler;
	}

	public void addItem(String name, String phone) {
		Boolean isInList = false;
		
		for(CheckListViewItem item : listItems) {
			if(item.name.equalsIgnoreCase(name.trim())) {
				item.phoneList.put(phone, true);
				isInList = true;
				break;
			}
		}
		
		if(!isInList) 
			listItems.add(new CheckListViewItem(name.trim(), phone));

	}

	public void clearAdapter() {
		listItems.clear();		
		hasExtended = false;
	}

	@Override
	public int getCount() {
		if(hasExtended)
			return listItems.size();
		else 
			return listItems.size() + 1;
	}

	@Override
	public Object getItem(int position) {
		if(position < listItems.size())
			return listItems.get(position);
		else
			return null;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}
	
	@Override
	public int getItemViewType(int position) {
		if(!hasExtended && position == listItems.size())
			return CONVERT_VIEW_TYPE_CLICK;
		else
			return CONVERT_VIEW_TYPE_ITEM;
	}
	
	@Override
	public int getViewTypeCount() {
		return 2;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		
		int convertViewType = getItemViewType(position);
		
		// the button in the bottom for reading address book
		if (convertViewType == CONVERT_VIEW_TYPE_CLICK) {

			LinearLayout ll = new LinearLayout(context);
			convertView = new LinearLayout(context);
			ll.setOrientation(LinearLayout.HORIZONTAL);
			ll.setBackgroundColor(Color.BLACK);
			ll.setGravity(Gravity.CENTER);

			Resources res = context.getResources();

			TextView temp = new TextView(context);
			temp.setGravity(android.view.Gravity.CENTER_VERTICAL);
			temp.setMinHeight(LIST_PREFERED_HEIGHT);
			temp.setPadding((int)toPixel(res, 10), (int)toPixel(res, 10), (int)toPixel(res, 10), (int)toPixel(res, 10));
			temp.setTextColor(0xff00bfff);
			temp.setTextSize(20);

			temp.setText("Select from Phone Book");

			ll.addView(temp);

			ll.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					hasExtended = true;
					extendListByAddressBook();					
					CheckListAdapter.this.notifyDataSetChanged();					
				}
			});

			return ll;	
		}
		
		// Recommended user item
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
			public void onCheckedChanged(final CompoundButton buttonView,
					final boolean isChecked) {

				if(isChecked == false) {
					item.checkState = isChecked;
					return;
				}					
				
				Thread a = new Thread() {
					@Override
					public void run() {
						super.run();
						try {
							if (!Discoverer.getInstance().queryPhoneNumberType(item.phoneList)) {

								Message msg = handler.obtainMessage(InvitationUI.HANDLE_DISABLE_CHECK, buttonView);
								handler.sendMessage(msg);

							} else {
								item.checkState = isChecked;
							}
						} catch (AgeException e) {
							Message msg = handler.obtainMessage();
							msg.what = InvitationUI.HANDLE_SHOW_MESSAGE_DIALOG;
							msg.obj = new String[]{"Finished", "Check Mobile Number error!", "Dismiss"};
							handler.sendMessage(msg);
						}
					}
				};
				a.start();
				a = null;

			}
		});

		if (item.checkState)
			cbox.setChecked(true);
		else
			cbox.setChecked(false);
		cbox.setTag(item);

		textView.setTag(item);
		textView.setText(item.name);
		
		boolean hasMobileNumber = item.phoneList.containsValue(true);
		
		if (hasMobileNumber)
			textView.setTextColor(Color.WHITE);
		else {
			textView.setTextColor(Color.GRAY);
			ll.setClickable(false);
			cbox.setClickable(false);
		}

		return ll;
	}
	
	private void extendListByAddressBook(){
		
		String lastName = null;
		String firstName = null;
		Cursor contactCursor = getContactCursor(context);

		try {
			while (contactCursor.moveToNext()) {

				String phone = contactCursor
						.getString(contactCursor
								.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
				String displayName = contactCursor
						.getString(contactCursor
								.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));

				if (phone.length() >= 7) {
					if (displayName != null) {
						int pos = displayName.lastIndexOf(" ");

						if (pos >= 0) {
							firstName = displayName.substring(0, pos).trim();
							lastName = displayName.substring(pos).trim();
						} else {
							firstName = displayName;
							lastName = "";
						}
					} else {
						firstName = "";
						lastName = "";
					}

					String name = firstName + " " + lastName;
					if(name.length() > 18)
						name = name.substring(0, 18) + "...";
					
					addItem(name, phone);
				}

			}
		} finally {
			contactCursor.close();
		}
	}
	
    private Cursor getContactCursor(Context context) {
    	return context.getContentResolver().query(
    			ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
    			new String[] {
    				ContactsContract.CommonDataKinds.Phone._ID,
    				ContactsContract.CommonDataKinds.Phone.NUMBER,
    				ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME
    			},
    			ContactsContract.CommonDataKinds.Phone.NUMBER + " IS NOT NULL", null, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " asc");
    }   
    
	private float toPixel(Resources res, int dip) {
		float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dip,
				res.getDisplayMetrics());
		return px;
	}
	
}
