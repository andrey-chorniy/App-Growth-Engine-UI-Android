package com.hookmobile.ageui;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Handler;

import com.hookmobile.age.AgeException;
import com.hookmobile.age.AgeUtils;
import com.hookmobile.age.Discoverer;
import com.hookmobile.age.Lead;

public class InvitationUI implements DialogInterface.OnCancelListener,
		DialogInterface.OnDismissListener {

	private static final int SHOW_LIST_DIALOG = 1;
	private static final int HIDE_LIST_DIALOG = 2;
	private static final int SHOW_MESSAGE_DIALOG = 3;
	private static final int HIDE_MESSAGE_DIALOG = 4;

	private CheckListAdapter menuAdapter = null;
	private Activity actContect = null;
	private AlertDialog dialog = null;
	private AlertDialog messageDialog;

	private InvitationListener clickHandler = null;

	private Handler handler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case SHOW_LIST_DIALOG:
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
		this.actContect = parent;
		Discoverer.activate(actContect, appKey);

		menuAdapter = new CheckListAdapter(actContect);

		createView(title);
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
	public Dialog createView(String title) {
		final AlertDialog.Builder builder = new AlertDialog.Builder(actContect);

		builder.setAdapter(menuAdapter, null);
		builder.setInverseBackgroundForced(true);

		dialog = builder.create();
		dialog.setTitle(title);
		dialog.setOnCancelListener(this);
		dialog.setOnDismissListener(this);
		dialog.setButton("Send", new DialogInterface.OnClickListener() {

			public void onClick(DialogInterface dialog, int which) {
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

				try {
					Discoverer.getInstance().newReferral(phoneList, true, null);
					if (clickHandler != null) {
						clickHandler.onClick(phoneList);
					}
				} catch (AgeException e) {
					e.printStackTrace();
				}

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
			try {
				menuAdapter.clearAdapter();
				Discoverer.getInstance().discover();
				List<Lead> lead = null;
				lead = Discoverer.getInstance().queryLeads();

				addList(lead);
				menuAdapter.notifyDataSetChanged();
			} catch (AgeException e) {
				e.printStackTrace();
			}

			handler.sendMessage(handler.obtainMessage(SHOW_LIST_DIALOG));
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

}
