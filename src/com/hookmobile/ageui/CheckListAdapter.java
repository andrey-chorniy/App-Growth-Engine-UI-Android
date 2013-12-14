package com.hookmobile.ageui;

import java.io.InputStream;
import java.util.ArrayList;

import com.hookmobile.age.AgeException;
import com.hookmobile.age.Discoverer;
import com.hookmobile.ageui.sample.R;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.provider.BaseColumns;
import android.provider.ContactsContract;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
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
    private LayoutInflater layoutInflater;

    public CheckListAdapter(Context context, Handler handler, int displayWidth, boolean isDisplayPhoto) {
		this.context = context;
		this.handler = handler;
		this.displayWidth = displayWidth;
		this.isDisplayPhoto = isDisplayPhoto;

        layoutInflater = LayoutInflater.from(context);

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
        ViewHolder viewHolder;
		if (convertView == null) {
            View layout = layoutInflater.inflate(R.layout.contact_list_item, null);
            viewHolder = new ViewHolder(item);
//			LinearLayout ll = new LinearLayout(context);
//			ll.setOrientation(LinearLayout.HORIZONTAL);
//			ll.setBackgroundColor(Color.BLACK);
//			ll.setGravity(android.view.Gravity.CENTER_VERTICAL);
//			ll.setPadding(0, 0, 0, 0);

			//ImageView
			ImageView imageView = (ImageView) layout.findViewById(R.id.contact_image);
            viewHolder.contactImage = imageView;
			if (isDisplayPhoto) {
				imageView.setMaxWidth(toPixel(50));
				imageView.setMinimumWidth(toPixel(50));
			} else {
				imageView.setVisibility(View.GONE);
			}

			//TextView
            viewHolder.contactName = (TextView) layout.findViewById(R.id.contact_name);

			//CheckBox
            viewHolder.cbox = (CheckBox) layout.findViewById(R.id.cbox);

			convertView = layout;
            convertView.setTag(viewHolder);
		} else {
            viewHolder = (ViewHolder)convertView.getTag();
        }

		ViewGroup ll = (ViewGroup) convertView;
		
//		ImageView imageView = viewHolder.contactImage;
//		TextView textView = (TextView) ll.getChildAt(1);
		final CheckBox cbox = viewHolder.cbox;

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

//		textView.setTag(item);
        TextView textView = viewHolder.contactName;
		textView.setText(item.name);
		
		boolean hasMobileNumber = item.phoneList.containsValue(true);
		
		if (hasMobileNumber) {
			textView.setTextColor(Color.BLACK);
        } else {
			textView.setTextColor(Color.GRAY);
			ll.setClickable(false);
			cbox.setClickable(false);
		}

		if (isDisplayPhoto) {
			if (item.btContactImage != null)
                viewHolder.contactImage.setImageBitmap(item.btContactImage);

		} else {
            viewHolder.contactImage.setVisibility(View.INVISIBLE);
		}
		
		return ll;
	}
	
	void extendListByAddressBook() {

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
			this.hasExtended = true;
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

    private static class ViewHolder {
        ImageView contactImage;
        TextView contactName;
        CheckBox cbox;
        CheckListViewItem item;

        private ViewHolder(CheckListViewItem item) {
            this.item = item;
        }
    }
}
