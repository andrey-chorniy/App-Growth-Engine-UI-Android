package com.hookmobile.ageui;

import java.io.InputStream;
import java.util.ArrayList;

import com.hookmobile.age.AgeException;
import com.hookmobile.age.Discoverer;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.provider.BaseColumns;
import android.provider.ContactsContract;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
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
	private Bitmap defaultContactPhoto;
	
	private int displayWidth;
	public boolean hasExtended = false;
	private boolean isDisplayPhoto = false;
	
	public CheckListAdapter(Context context, Handler handler, int displayWidth, boolean isDisplayPhoto) {
		this.context = context;
		this.handler = handler;
		this.displayWidth = displayWidth;
		this.isDisplayPhoto = isDisplayPhoto;
		
		InputStream is = CheckListAdapter.class.getClassLoader().getResourceAsStream("res/drawable-mdpi/contact.png");
		if(is != null)
			defaultContactPhoto = BitmapFactory.decodeStream(is);
	}
	
	public void setDisplayWidth(int displayWidth) {
		this.displayWidth = displayWidth;
	}

	public void addItem(String name, String phone)  {
		
		Boolean isInList = false;
		
		for(CheckListViewItem item : listItems) {
			if(item.name.equalsIgnoreCase(name.trim())) {
				item.phoneList.put(phone, true);
				isInList = true;
				break;
			}
		}
		
		if (!isInList) {

			if(isDisplayPhoto) {
				Cursor cursorContacts = null;

				try {
					Uri uriNumber2Contacts = Uri.withAppendedPath(
							ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
							Uri.encode(phone));
					cursorContacts = context.getContentResolver().query(
							uriNumber2Contacts,
							new String[] { BaseColumns._ID },
							null, null, null);

					Bitmap btContactImage = null;
					if (cursorContacts.moveToFirst()) {
						Long contactID = cursorContacts
								.getLong(cursorContacts
										.getColumnIndex(BaseColumns._ID));
						Uri uri = ContentUris.withAppendedId(
								ContactsContract.Contacts.CONTENT_URI,
								contactID);
						InputStream input = ContactsContract.Contacts
								.openContactPhotoInputStream(
										context.getContentResolver(), uri);

						if (input != null) {
							btContactImage = BitmapFactory.decodeStream(input);
						} else {
							btContactImage = defaultContactPhoto;
						}
					}

					listItems.add(new CheckListViewItem(name.trim(), phone,
							btContactImage));

				} finally {
					cursorContacts.close();
				}
			} else {
				listItems.add(new CheckListViewItem(name.trim(), phone,
						null));
			}
		}
	}

	public void clearAdapter() {
		listItems.clear();	
		hasExtended = true;
		this.notifyDataSetChanged();
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

			TextView temp = new TextView(context);
			temp.setGravity(android.view.Gravity.CENTER_VERTICAL);
			temp.setMinHeight(LIST_PREFERED_HEIGHT);
			temp.setPadding(toPixel(10), toPixel(10), toPixel(10), toPixel(10));
			temp.setTextColor(0xff00bfff);
			temp.setTextSize(20);

			temp.setText("Select from Phone Book");

			ll.addView(temp);

			ll.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					hasExtended = true;

					Thread a = new Thread() {
						@Override
						public void run() {

							handler.sendEmptyMessage(InvitationUI.HANDLE_SHOW_LOADING);
							
							extendListByAddressBook();							
							
							handler.sendEmptyMessage(InvitationUI.HANDLE_EXTEND_LIST_COMPLETE);
						}
					};
					a.start();
					a = null;
				}
			});

			return ll;	
		}
		
		// Recommended user item
		final CheckListViewItem item = (CheckListViewItem) getItem(position);

		TextView temp = null;
		context.getResources();

		if (convertView == null) {

			LinearLayout ll = new LinearLayout(context);
			ll.setOrientation(LinearLayout.HORIZONTAL);
			ll.setBackgroundColor(Color.BLACK);
			ll.setGravity(android.view.Gravity.CENTER_VERTICAL);
			ll.setPadding(0, 0, 0, 0);

			//ImageView
			ImageView imageView = new ImageView(context); 
			imageView.setAdjustViewBounds(true);
			imageView.setMaxHeight(toPixel(50));
			if (isDisplayPhoto) {
				imageView.setMaxWidth(toPixel(50));
				imageView.setMinimumWidth(toPixel(50));
			} else {
				imageView.setMaxWidth(0);
				imageView.setMinimumWidth(toPixel(0));
			}
			
			ll.addView(imageView);

			//TextView
			temp = new TextView(context);
			LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(
					displayWidth - toPixel(80) + (isDisplayPhoto ? 0 : toPixel(50)),
					LayoutParams.WRAP_CONTENT);
			temp.setLayoutParams(param);
			temp.setPadding(toPixel(10), 0, 0, 0);
			temp.setTextColor(Color.RED);
			temp.setGravity(android.view.Gravity.CENTER_VERTICAL);
			temp.setTextSize(18);

			ll.addView(temp);
			
			//CheckBox
			final CheckBox cbox = new CheckBox(context);			
			StateListDrawable d = new StateListDrawable();
			Bitmap mBitmap = BitmapFactory.decodeStream(CheckListAdapter.class.getClassLoader().getResourceAsStream("res/drawable-mdpi/checked.png"));
			Drawable png = new BitmapDrawable(context.getResources(), Bitmap.createScaledBitmap(mBitmap, toPixel(25), toPixel(25), true));
			mBitmap.setDensity(200);
			d.addState(new int[]{android.R.attr.state_checked}, png);
			cbox.setButtonDrawable(d);
			ll.addView(cbox);

			convertView = ll;
		}

		LinearLayout ll = (LinearLayout) convertView;
		
		ImageView imageView = (ImageView) ll.getChildAt(0);
		TextView textView = (TextView) ll.getChildAt(1);
		final CheckBox cbox = (CheckBox) ll.getChildAt(2);

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
		if(item.name.length() > 18)
			textView.setText(item.name.substring(0, 18) + "...");
		else
			textView.setText(item.name);
		
		boolean hasMobileNumber = item.phoneList.containsValue(true);
		
		if (hasMobileNumber)
			textView.setTextColor(Color.WHITE);
		else {
			textView.setTextColor(Color.GRAY);
			ll.setClickable(false);
			cbox.setClickable(false);
		}

		if (isDisplayPhoto) {
			if (item.btContactImage != null) 
				imageView.setImageBitmap(item.btContactImage);

		} else {
			imageView.setVisibility(View.INVISIBLE);
		}
		
		return ll;
	}
	
	private void extendListByAddressBook() {

		String lastName = null;
		String firstName = null;
		Cursor contactCursor = getContactCursor(context);

		try {
			while (contactCursor.moveToNext()) {

				String phoneNumber = contactCursor
						.getString(contactCursor
								.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
				String displayName = contactCursor
						.getString(contactCursor
								.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));

				String phone = com.hookmobile.age.utils.AgeUtils
						.normalizePhone(phoneNumber);

				if (phone.length() >= 10) {
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
    				BaseColumns._ID,
    				ContactsContract.CommonDataKinds.Phone.NUMBER,
    				ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME
    			},
    			ContactsContract.CommonDataKinds.Phone.NUMBER + " IS NOT NULL", null, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " asc");
    }   
    

	private int toPixel(int dip) {
		float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dip,
				context.getResources().getDisplayMetrics());
		return (int)px;
	}

}
