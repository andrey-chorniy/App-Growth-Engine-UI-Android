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
//	private static int HANDLE_VERIFICATION_STATUS_ENABLE = 7;
	private static final int HANDLE_SHOW_MESSAGE_DIALOG = 8;
//	private static int HANDLE_GET_RECOMMENDED_INVITES_BUTTON_ENABLE = 9;
//	private static int HANDLE_INSTALLS_REFERRALS_ENABLE = 10;

	private CheckListAdapter menuAdapter = null;
	private Activity actContect = null;
	private AlertDialog dialog = null;
	private AlertDialog messageDialog;
	private ProgressDialog progressDialog;
	
	private InvitationListener clickHandler = null;

	private Handler handler = new Handler() {
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
	 * 			 for sending out invitation.  If false, then use device phone number.
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
	 * 			 for sending out invitation.  If false, then use device phone number.
	 * @param customParam custom parameter such as app assigned user_id to be stored for correlation on server callback.
	 */
	public InvitationUI(Activity parent, String appKey, String title, boolean useVirtualNumber, String customParam) {
		this.actContect = parent;
		Discoverer.activate(actContect, appKey, customParam);

		menuAdapter = new CheckListAdapter(actContect);
		
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
	@SuppressWarnings("deprecation")
	public Dialog createView(String title, final boolean useVirtualNumber) {
		final AlertDialog.Builder builder = new AlertDialog.Builder(actContect);

		builder.setAdapter(menuAdapter, null);
		builder.setInverseBackgroundForced(true);

		dialog = builder.create();
		dialog.setTitle(title);
		dialog.setOnCancelListener(this);
		dialog.setOnDismissListener(this);
		dialog.setButton("Send", new DialogInterface.OnClickListener() {

			public void onClick(DialogInterface dialog, int which) {
				
				Thread a = new Thread() {
					@Override
					public void run() {
						super.run();
						handler.sendEmptyMessage(HANDLE_SHOW_LOADING);
						
						List<String> phoneList = new ArrayList<String>();
						for (int i = 0; i < menuAdapter.getCount(); i++) {
							String phoneNo = null;
							CheckListViewItem chkListItem = (CheckListViewItem) menuAdapter
									.getItem(i);
							if (chkListItem.checkState) {
								phoneNo = chkListItem.phone.toString();
								phoneList.add(phoneNo);
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
		if (AgeUtils.isOnline(actContect)) {

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

	public void onCancel(DialogInterface dialog) {
		cleanup();
	}

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

			String name = AgeUtils.lookupNameByPhone(actContect,
					lead.getPhone());
			menuAdapter.addItem(new CheckListViewItem(name, lead.getPhone()));
		}
	}

	@SuppressWarnings("deprecation")
	private void showMessageDialog(String title, String message,
			String buttonText1, String buttonText2) {

		messageDialog = new AlertDialog.Builder(actContect).create();
		messageDialog.setCancelable(false);
		messageDialog.setMessage(message);
		if (!AgeUtils.isEmptyStr(title)) {
			messageDialog.setTitle(title);
		}
		messageDialog.setButton(buttonText1,
				new DialogInterface.OnClickListener() {

					public void onClick(DialogInterface dialog, int which) {
						messageDialog.dismiss();
					}
				});
		if (buttonText2 != null)
			messageDialog.setButton2(buttonText2,
					new DialogInterface.OnClickListener() {

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
		new AlertDialog.Builder(actContect)
			.setTitle(title)
			.setMessage(message)
			.setPositiveButton(buttonText, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
    				
    			}
			})
			.show();
	}

}
