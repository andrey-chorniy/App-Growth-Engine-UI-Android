package com.hookmobile.ageui;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Handler;
import android.os.Message;
import android.view.Gravity;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.hookmobile.age.AgeException;
import com.hookmobile.age.utils.AgeUtils;
import com.hookmobile.age.Discoverer;
import com.hookmobile.age.Lead;

public class InvitationUI implements DialogInterface.OnCancelListener,
		DialogInterface.OnDismissListener {

	private static final int SHOW_LIST_DIALOG = 1;
	private static final int HIDE_LIST_DIALOG = 2;
	private static final int SHOW_MESSAGE_DIALOG = 3;
	private static final int HIDE_MESSAGE_DIALOG = 4;
	
	private static final int HANDLE_SHOW_LOADING = 5;
	private static final int HANDLE_HIDE_LOADING = 6; 
//	private static final int HANDLE_VERIFICATION_STATUS_ENABLE = 7;
	public static final int HANDLE_SHOW_MESSAGE_DIALOG = 8;
//	private static final int HANDLE_GET_RECOMMENDED_INVITES_BUTTON_ENABLE = 9;
//	private static final int HANDLE_INSTALLS_REFERRALS_ENABLE = 10;
	public static final int HANDLE_DISABLE_CHECK = 11;

	private CheckListAdapter menuAdapter = null;
	private Activity actContext = null;
	private AlertDialog dialog = null;
	private AlertDialog messageDialog;
	private ProgressDialog progressDialog;
	
	private InvitationListener clickHandler = null;

	private Handler handler = new Handler() {
		@Override
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case SHOW_LIST_DIALOG:
				menuAdapter.notifyDataSetChanged();
				dialog.show();
				break;
			case HIDE_LIST_DIALOG:
				dialog.dismiss();
				handler.sendMessage(handler.obtainMessage(HIDE_MESSAGE_DIALOG));
				break;
			case SHOW_MESSAGE_DIALOG:
				showMessageDialog("No Internet Connection",
						"AGE needs Internet Connection to be present",
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
			case HANDLE_DISABLE_CHECK:
				Toast toast = Toast.makeText(actContext, 
						"No mobile phone number found for selected contact",
						Toast.LENGTH_LONG);
				
				LinearLayout linearLayout = null;
			    linearLayout = (LinearLayout) toast.getView();
			    TextView messageTextView = (TextView) linearLayout.getChildAt(0);
			    messageTextView.setTextSize(20);
			    messageTextView.setGravity(Gravity.CENTER);
			    messageTextView.setTextColor(0xff00bfff);
				
				toast.setGravity(Gravity.CENTER, 0, 0);
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
		this(parent, appKey, title, true);
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
	 * @param customParam custom parameter such as app assigned user_id to be stored for correlation on server callback.
	 */
	public InvitationUI(Activity parent, String appKey, String title, String customParam) {
		this(parent, appKey, title, true, customParam);
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
		this(parent, appKey, title, useVirtualNumber, null);
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
	 * @param customParam custom parameter such as app assigned user_id to be stored for correlation on server callback.
	 */
	public InvitationUI(Activity parent, String appKey, String title, boolean useVirtualNumber, String customParam) {
		this.actContext = parent;
		Discoverer.activate(actContext, appKey, customParam);

		menuAdapter = new CheckListAdapter(actContext, handler);
		
		progressDialog = new ProgressDialog(parent);
		progressDialog.setMessage("Please Wait...");
		progressDialog.setCancelable(false);

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
	 * Create Dialog
	 * 
	 * @return
	 */
	public Dialog createView(String title, final boolean useVirtualNumber) {
		final AlertDialog.Builder builder = new AlertDialog.Builder(actContext);

		builder.setAdapter(menuAdapter, null);
		builder.setInverseBackgroundForced(true);

		dialog = builder.create();
		dialog.setTitle(title);
		dialog.setOnCancelListener(this);
		dialog.setOnDismissListener(this);
		dialog.setButton("Send", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				
				Thread a = new Thread() {
					@Override
					public void run() {
						super.run();
						handler.sendEmptyMessage(HANDLE_SHOW_LOADING);
						
						List<String> phoneList = new ArrayList<String>();
						for (int i = 0; i < menuAdapter.getCount(); i++) {
		
							CheckListViewItem chkListItem = (CheckListViewItem) menuAdapter
									.getItem(i);
							if (chkListItem.checkState) {
								for(String phone : chkListItem.phoneList.keySet()) {
									if(chkListItem.phoneList.get(phone)) 
										phoneList.add(phone);
								}
							}
						}
						
						if(phoneList.size() > 0) {
							try {
								Discoverer.getInstance().newReferral(phoneList, useVirtualNumber, null);
								
								showMessage(new String[] {"Finished", "Referral Success.", "Dismiss"});
							}
							catch(AgeException e) {
								showMessage(new String[] {"Error", e.getMessage() != null ? e.getMessage() : "Referral Error", "Dismiss"});
							}
						}
						handler.sendEmptyMessage(HANDLE_HIDE_LOADING);
					}
				};
				a.start();
				a = null;

			}
		});

		dialog.setButton2("Cancel", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				cleanup();
			}
		});

		return dialog;
	}

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

					handler.sendEmptyMessage(HANDLE_HIDE_LOADING);
					handler.sendMessage(handler.obtainMessage(SHOW_LIST_DIALOG));
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
		// handler.sendMessage(handler.obtainMessage(HIDE_LIST_DIALOG));
		dialog.dismiss();
		if (messageDialog != null)
			messageDialog.dismiss();
	}

	private void addList(List<Lead> leadList) {

		for (Lead lead : leadList) {

			String name = AgeUtils.lookupNameByPhone(actContext,
					lead.getPhone());
			if(name.length() > 18)
				name = name.substring(0, 18) + "...";
			
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
		messageDialog.setButton(buttonText1,
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						messageDialog.dismiss();
					}
				});
		if (buttonText2 != null)
			messageDialog.setButton2(buttonText2,
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

}
