package com.hookmobile.ageui;

import java.util.ArrayList;
import java.util.List;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Typeface;
import android.graphics.drawable.PaintDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RectShape;
import android.os.Handler;
import android.os.Message;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.hookmobile.age.AgeException;
import com.hookmobile.age.utils.AgeUtils;

import com.hookmobile.age.Discoverer;
import com.hookmobile.age.Lead;

public class InvitationUI implements DialogInterface.OnCancelListener,
		DialogInterface.OnDismissListener{

	public static final int SHOW_LIST_DIALOG = 1;
	private static final int HIDE_LIST_DIALOG = 2;
	private static final int SHOW_MESSAGE_DIALOG = 3;
	private static final int HIDE_MESSAGE_DIALOG = 4;	
	public static final int HANDLE_SHOW_LOADING = 5;
	private static final int HANDLE_HIDE_LOADING = 6; 
	private static final int HANDLE_SHOW_TOAST = 7;
	public static final int HANDLE_SHOW_MESSAGE_DIALOG = 8;
	public static final int HANDLE_EXTEND_LIST_COMPLETE = 9;
	public static final int HANDLE_DISABLE_CHECK = 11;
	private static final int HANDLE_SHOW_TOAST_WARNING = 12;

	private CheckListAdapter menuAdapter = null;
	private Activity actContext = null;
	private Dialog dialog = null;
	private AlertDialog messageDialog;
	private Dialog progressDialog;
	private Toast toast;
	private int displayWidth;
	private int displayHeight;
	// Message shown when the contact's device doesn't support the app or the number is unavailable.
	private static String contactDeviceNotSupportedMsg = "This user's device doesn't support this app.";

	public static void setContactDeviceNotSupportedMsg(
			String contactDeviceNotSupportedMsg) {
		InvitationUI.contactDeviceNotSupportedMsg = contactDeviceNotSupportedMsg;
	}

	private InvitationListener clickHandler = null;

	private Handler handler = new Handler() {
		@Override
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case SHOW_LIST_DIALOG:
				
				menuAdapter.notifyDataSetChanged();	
				//Set dialog size
				dialog.getWindow().setLayout(displayWidth, displayHeight);
				dialog.show();
				break;
			case HIDE_LIST_DIALOG:
				dialog.dismiss();
				handler.sendMessage(handler.obtainMessage(HIDE_MESSAGE_DIALOG));
				break;
			case HANDLE_SHOW_TOAST:
				toast.setText("Referral Success!");
				toast.setDuration(Toast.LENGTH_SHORT);
				toast.show();
				dialog.dismiss();
				handler.sendMessage(handler.obtainMessage(HIDE_MESSAGE_DIALOG));
				break;
			case HANDLE_SHOW_TOAST_WARNING:
				toast.setText("No Contact Selected!");
				toast.setDuration(Toast.LENGTH_LONG);
				toast.show();
				break;
			case SHOW_MESSAGE_DIALOG:
				showMessageDialog("No Internet Connection",
						"Wifi or data service not available",
						"Dismiss", null);
				break;
			case HIDE_MESSAGE_DIALOG:
				if (messageDialog != null)
					messageDialog.dismiss();
				break;
			case HANDLE_SHOW_LOADING:
				progressDialog.show();
				break;
			case HANDLE_HIDE_LOADING:
				progressDialog.cancel();				
				break;
			case HANDLE_SHOW_MESSAGE_DIALOG:
				String[] content = (String[])msg.obj;
				showErrorDialog(content[0], content[1], content[2]);
				break;
			case HANDLE_EXTEND_LIST_COMPLETE:
				menuAdapter.notifyDataSetChanged();	
				dialog.show();
				progressDialog.cancel();
				break;
			case HANDLE_DISABLE_CHECK:
				toast.setText(contactDeviceNotSupportedMsg);
				toast.setDuration(Toast.LENGTH_LONG);
				toast.show();
				
				CompoundButton buttonView = (CompoundButton) msg.obj;
				buttonView.setChecked(false);
				
				menuAdapter.notifyDataSetChanged();
				break;
			default:
				break;
			}
		};
	};


	/**
	 * constructor
	 * 
	 * @param parent
	 *            context the Android Activity.
	 * @param appKey
	 *            Application Key provided by AGE.
	 * @param title
	 *            title for the Popup
	 */
	public InvitationUI(Activity parent, String appKey, String title) {
		this(parent, appKey, title, true, true);
	}

	/**
	 * constructor
	 * 
	 * @param parent
	 *            context the Android Activity.
	 * @param appKey
	 *            Application Key provided by AGE.
	 * @param title
	 *            title for the Popup
	 * @param customParam 
	 *            custom parameter such as app assigned user_id to be stored for correlation on server callback.
	 */
	public InvitationUI(Activity parent, String appKey, String title, String customParam) {
		this(parent, appKey, title, true, customParam, true);
	}
	
	/**
	 * constructor
	 * 
	 * @param parent
	 *            context the Android Activity.
	 * @param appKey
	 *            Application Key provided by AGE.
	 * @param title
	 *            title for the Popup
	 * @param customParam 
	 *           custom parameter such as app assigned user_id to be stored for correlation on server callback.
	 * @param isDisplayPhoto
	 *           whether display contact's photo in the list or not.
	 */
	public InvitationUI(Activity parent, String appKey, String title, String customParam, boolean isDisplayPhoto) {
		this(parent, appKey, title, true, customParam, isDisplayPhoto);
	}

	/**
	 * constructor
	 * 
	 * @param parent
	 *            context the Android Activity.
	 * @param appKey
	 *            Application Key provided by AGE.
	 * @param title
	 *            title for the Popup
	 * @param useVirtualNumber 
	 * 			 for sending out invitation.  If false, then use device phoneList number.
	 */
	public InvitationUI(Activity parent, String appKey, String title, boolean useVirtualNumber) {
		this(parent, appKey, title, useVirtualNumber, null, true);
	}

	/**
	 * constructor
	 * 
	 * @param parent
	 *            context the Android Activity.
	 * @param appKey
	 *            Application Key provided by AGE.
	 * @param title
	 *            title for the Popup
	 * @param useVirtualNumber 
	 * 			 for sending out invitation.  If false, then use device phoneList number.
	 * @param isDisplayPhoto
	 *           whether display contact's photo in the list or not.
	 */
	public InvitationUI(Activity parent, String appKey, String title, boolean useVirtualNumber, boolean isDisplayPhoto) {
		this(parent, appKey, title, useVirtualNumber, null, isDisplayPhoto);
	}
	
	/**
	 * constructor
	 * 
	 * @param parent
	 *            context the Android Activity.
	 * @param appKey
	 *            Application Key provided by AGE.
	 * @param title
	 *            title for the Popup
	 * @param useVirtualNumber 
	 * 			 for sending out invitation.  If false, then use device phoneList number.
	 * @param customParam 
	 *           custom parameter such as app assigned user_id to be stored for correlation on server callback.
	 * @param isDisplayPhoto
	 *           whether display contact's photo in the list or not.
	 */
	public InvitationUI(Activity parent, String appKey, String title, boolean useVirtualNumber, String customParam, boolean isDisplayPhoto) {
		this.actContext = parent;
		
		Discoverer.activate(actContext, appKey, customParam);

		//Set width and height of dialog
		WindowManager wm = actContext.getWindowManager();
		displayWidth = wm.getDefaultDisplay().getWidth() * 5 / 6;
		displayHeight = wm.getDefaultDisplay().getHeight() * 5 / 6;
		
		menuAdapter = new CheckListAdapter(actContext, handler, displayWidth, isDisplayPhoto);
		
		progressDialog = new Dialog(parent, getResourceIdByName(actContext.getPackageName(), "style", "SpinnerDialog"));
		progressDialog.setCancelable(true);
		ProgressBar mProgressBar = new ProgressBar(parent);
		progressDialog.setContentView(mProgressBar, new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));

		createView(title, useVirtualNumber);
	}

	/**
	 * Set List onclick listener
	 * 
	 * @param listener
	 */
	public void setInvitationListener(InvitationListener listener) {
		clickHandler = listener;
	}

	/**
	 * Create Dialog.
	 * 
	 * @return
	 */
	@SuppressLint("NewApi")
	public Dialog createView(String title, final boolean useVirtualNumber) {

		dialog = new Dialog(actContext, getResourceIdByName(actContext.getPackageName(), "style", "ListDialog"));
		dialog.setOnCancelListener(this);
		dialog.setOnDismissListener(this);
		dialog.setCanceledOnTouchOutside(true);
		
		// Set layout, alpha and corner shape
		Window window = dialog.getWindow();
		WindowManager.LayoutParams layoutParams = window.getAttributes();
		layoutParams.alpha = 0.8f;
		window.setAttributes(layoutParams);
		ShapeDrawable dialogShape = new ShapeDrawable(new RectShape());
		dialogShape.setAlpha(0);
		window.setBackgroundDrawable(dialogShape);
				
		// Set LinearLayout
		LinearLayout ll = new LinearLayout(actContext);
		ll.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
		ll.setGravity(Gravity.CENTER_HORIZONTAL);
		ll.setOrientation(LinearLayout.VERTICAL);
		PaintDrawable listShape = new PaintDrawable();
		listShape.setCornerRadius(10.0f);
		ll.setBackgroundDrawable(listShape);

		// Set Title TextView
		TextView titleTextView = new TextView(actContext);
		titleTextView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, toPixel(45)));
		titleTextView.setPadding(toPixel(10), toPixel(10), toPixel(10), toPixel(5));
		titleTextView.setText("Suggested Contacts");
		titleTextView.setTextSize(22.0f);
		titleTextView.setTextColor(0xff1080ee);
		ll.addView(titleTextView);
		
		// Set Line TextView
		TextView lineTextView = new TextView(actContext);
		LinearLayout.LayoutParams titleLayoutParams = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, toPixel(2));
		titleLayoutParams.setMargins(toPixel(2), toPixel(2), toPixel(2), toPixel(2));
		lineTextView.setLayoutParams(titleLayoutParams);
		lineTextView.setPadding(0, 0, 0, 0);
		lineTextView.setBackgroundColor(0xff1080ee);
		ll.addView(lineTextView);
		
		// Set ListView
		CheckListView listView = new CheckListView(actContext);
		listView.setLayoutParams(new ListView.LayoutParams(LayoutParams.MATCH_PARENT, displayHeight - toPixel(120)));
		listView.setAdapter(menuAdapter);
		listView.setFadingEdgeLength(0);
        listView.setOnRefreshListener(new CheckListView.OnRefreshListener() {
			
			@Override
			public void onRefresh() {
				showView();
			}
        });
		ll.addView(listView);
		

		// Set invite button
		Button inviteButton = new Button(actContext);
		LinearLayout.LayoutParams buttonLayoutParams = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, toPixel(40));
		buttonLayoutParams.setMargins(toPixel(10), toPixel(10), toPixel(10), toPixel(10));
		inviteButton.setLayoutParams(buttonLayoutParams);
		inviteButton.setPadding(toPixel(5), toPixel(5), toPixel(5), toPixel(5));
		inviteButton.setText("Invite");
		inviteButton.setTextColor(0xff191970);
		inviteButton.setTextSize(18);
		inviteButton.setTypeface(null, Typeface.BOLD);
		PaintDrawable buttonShape = new PaintDrawable();
		buttonShape.setCornerRadius(20.0f);
		buttonShape.getPaint().setColor(0xffffffff);
		inviteButton.setBackgroundDrawable(buttonShape);
		inviteButton.setOnClickListener( new View.OnClickListener() {

			@Override
			public void onClick(View inviteButton) {
				
				Thread a = new Thread() {

					@Override
					public void run() {
						super.run();

						//save checked phone numbers
						List<String> phoneList = new ArrayList<String>();
						for (int i = 0; i < (menuAdapter.hasExtended ? menuAdapter.getCount() : menuAdapter.getCount() - 1); i++) {

							CheckListViewItem chkListItem = (CheckListViewItem) menuAdapter
									.getItem(i);
							if (chkListItem.checkState) {
								for(String phone : chkListItem.phoneList.keySet()) {
									if(chkListItem.phoneList.get(phone)) 
										phoneList.add(phone);
								}
							}
						}
						
						//newReferral
						if(phoneList.size() > 0) {
							
							handler.sendEmptyMessage(HANDLE_SHOW_LOADING);
							
							try {
								Discoverer.getInstance().newReferral(phoneList, useVirtualNumber, "John");

								if (clickHandler != null)
									clickHandler.onClick(phoneList);
								
								handler.sendEmptyMessage(HANDLE_SHOW_TOAST);
							}
							catch(Exception e) {
								showMessage(new String[] {"Error", e.getMessage() != null ? e.getMessage() : "Referral Error", "Dismiss"});
							}
						} else {
							handler.sendEmptyMessage(HANDLE_SHOW_TOAST_WARNING);
						}
						
						handler.sendEmptyMessage(HANDLE_HIDE_LOADING);
						
					}
				};
				a.start();
				a = null;
				
				

			}
		});	
		ll.addView(inviteButton);
		
		// Add LinearLayout to dialog
		dialog.setContentView(ll);
		
		// Create toast
		toast = Toast.makeText(actContext, 
				contactDeviceNotSupportedMsg,
				Toast.LENGTH_LONG);
		
		LinearLayout linearLayout = null;
	    linearLayout = (LinearLayout) toast.getView();
	    TextView messageTextView = (TextView) linearLayout.getChildAt(0);
	    messageTextView.setTextSize(20);
	    messageTextView.setGravity(Gravity.CENTER);
	    messageTextView.setTextColor(0xff00bfff);
	    toast.setGravity(Gravity.CENTER, 0, 0);

		return dialog;
	}

	/**
	 * Discover and query leads. Populate ListView.
	 * 
	 */
	public void showView() {
		if (AgeUtils.isOnline(actContext)) {

			menuAdapter.clearAdapter();

			Thread a = new Thread() {
				@Override
				public void run() {
					super.run();					
					
					handler.sendEmptyMessage(HANDLE_SHOW_LOADING);
					
					try {
						Discoverer.getInstance().discover();
						addList(Discoverer.getInstance().queryLeads());
						
					} catch (AgeException e) {
						 displayError(e);
					}
				
					handler.sendMessage(handler.obtainMessage(SHOW_LIST_DIALOG));
					handler.sendEmptyMessage(HANDLE_HIDE_LOADING);
				}
			};
			a.start();
			a = null;
			
		} else {
			handler.sendMessage(handler.obtainMessage(SHOW_MESSAGE_DIALOG));
		}
	}

	@Override
	public void onCancel(DialogInterface dialog) {
		cleanup();
	}

	@Override
	public void onDismiss(DialogInterface dialog) {
		cleanup();
	}

	public void cleanup() {

		dialog.dismiss();
		if (messageDialog != null)
			messageDialog.dismiss();
	}

	/**
	 * Populate ListView's adapter with data from queryLeads().
	 * 
	 * @param leadList
	 */
	private void addList(List<Lead> leadList) {
		
		for (Lead lead : leadList) {

			String name = AgeUtils.lookupNameByPhone(actContext,
					lead.getPhone());

				menuAdapter.addItem(name, lead.getPhone());

		}
	}

	private void showMessageDialog(String title, String message,
			String buttonText1, String buttonText2) {

		messageDialog = new AlertDialog.Builder(actContext).create();
		messageDialog.setCancelable(false);
		messageDialog.setMessage(message);
		if (!AgeUtils.isEmptyStr(title)) {
			messageDialog.setTitle(title);
		}
		messageDialog.setButton(DialogInterface.BUTTON_POSITIVE, buttonText1,
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						messageDialog.dismiss();
					}
				});
		if (buttonText2 != null)
			messageDialog.setButton(DialogInterface.BUTTON_NEGATIVE, buttonText2,
					new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {

						}
					});
		messageDialog.show();
	}
	
	private void displayError(AgeException e) {
		String body = "Hook Mobile server encountered a problem: ";
		
		if(e.getMessage() != null) {
			body += e.getMessage();
		}
		else {
			body += "Unknown Error";
		}
		
		showMessage(new String[] {"Finished", body, "Dismiss"});
	}
	
	private void showMessage(String[] content) {
		Message msg = handler.obtainMessage();
		msg.what = HANDLE_SHOW_MESSAGE_DIALOG;
		msg.obj = content;
		handler.sendMessage(msg);
	}
	
	private void showErrorDialog(String title, String message, String buttonText) {
		new AlertDialog.Builder(actContext)
			.setTitle(title)
			.setMessage(message)
			.setPositiveButton(buttonText, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
    				
    			}
			})
			.show();
	}
	
	private int toPixel(int dip) {
		float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dip,
				actContext.getResources().getDisplayMetrics());
		return (int)px;
	}
	
	@SuppressWarnings("rawtypes")
	public static int getResourceIdByName(String packageName, String className, String name) {
		   Class r = null;
		   int id = 0;
		try {
		    r = Class.forName(packageName + ".R");

		    Class[] classes = r.getClasses();
		    Class desireClass = null;

		    for (int i = 0; i < classes.length; i++) {
		        if(classes[i].getName().split("\\$")[1].equals(className)) {
		            desireClass = classes[i];

		            break;
		        }
		    }

		    if(desireClass != null)
		        id = desireClass.getField(name).getInt(desireClass);
		} catch (ClassNotFoundException e) {
		    e.printStackTrace();
		} catch (IllegalArgumentException e) {
		    e.printStackTrace();
		} catch (SecurityException e) {
		    e.printStackTrace();
		} catch (IllegalAccessException e) {
		    e.printStackTrace();
		} catch (NoSuchFieldException e) {
		    e.printStackTrace();
		}

		return id;
	}

}
