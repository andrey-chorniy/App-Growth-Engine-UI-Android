package com.hookmobile.ageui.sample;

import java.util.List;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.hookmobile.ageui.sample.R;
import com.hookmobile.ageui.InvitationUI;
import com.hookmobile.ageui.InvitationListener;

public class Sample extends Activity {
	private Button showButton;

	// AGE App key assigned to developer app at http://hookmobile.com developer portal.
	private String appKey = "4992ca90-fcb9-4250-b0e1-947e611555a0";
	// AGE Invitation UI Popup View
	private InvitationUI invitationUI;
	// Decide if invitation will be sent from Virtual Number or Phone Native SMS.
	private boolean useVirtualNumber = true;
	// Decide if contact photo will appear in the invitation list.
	private boolean displayContactPhoto = false;
	// Optional assignment of app generated user id to be associated to the app install.  
	// This app generated user id will be referenced in server to server callback.
	private String appUserId = "App-Assigned-User-Id-Here";
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_agepopup);
		
		// Instantiate the class variable.
		invitationUI = new InvitationUI(this, appKey, "Suggested Contacts", useVirtualNumber, appUserId, displayContactPhoto);

		showButton = (Button) findViewById(R.id.show_button);

		InvitationListener sendlistener = new InvitationListener() {

			@Override
			public void onClick(List<String> phoneList) {
				// Print list of phone numbers invited by user.
				System.out.println("Number of Invitations Sent: " + phoneList.size());
				for(int i=0;i<phoneList.size();i++){
					System.out.println("Invited Phone: " + phoneList.get(i));
				}
			}
		};
		
		// Register callback of successful invitation completion.
		invitationUI.setInvitationListener(sendlistener);

		// Show Invitation UI when button is clicked.
		showButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// display the invitation UI.
				invitationUI.showView();
			}
		});

	}

	@Override
	protected void onPause() {
		super.onPause();

		invitationUI.cleanup();
	}

}
