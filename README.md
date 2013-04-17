# Introduction

AGE Invitation UI is a drop-in Android control that you can deploy to your app within minutes.  AGE Invitation UI will help your users identify and share your app with friends & associates having compatible mobile devices and highest LTV.  For complete feature set and how it works, please visit <a href="http://www.hookmobile.com"  target="_blank">Hook Mobile</a>.

# Getting Sample App Up and Running
A sample app is included with this project that demonstrate AGE Invitation UI launched from a button.  Once you download and import the AgeUI folder into an Eclipse Android project, click on run.  You may launch the sample app in the emulator or your Android device.  Running on your Android phone is preferred because you are likely to have more contacts in your phone address book to invite from.    

[![](https://dl.dropbox.com/s/yr0wcs0dpm79552/ageui_android1.png)](https://dl.dropbox.com/s/yr0wcs0dpm79552/ageui_android1.png)
[![](https://dl.dropbox.com/s/oqk9v7ojsglgkn0/ageui_android3.png)](https://dl.dropbox.com/s/oqk9v7ojsglgkn0/ageui_android3.png)

When AGE Invitation UI is invoked for first time, it will analyze the address book.  It may take a few seconds before the list of suggested contacts is displayed.  The list of contacts shown in the list is filtered by criteria you define for your app profile in our developer portal.  

Select one or more entries from the suggested and click on the <b>Send</b> button to fire off the invitation text message.  The recipient(s) of the invitation will receive a personalized text message on their phone:

[![](https://dl.dropbox.com/s/zg3qbf5ac8om7cg/inviteSms.PNG)](https://dl.dropbox.com/s/zg3qbf5ac8om7cg/inviteSms.PNG)

The message is completely customizable by you, and it can be further personalized to include the sender and app name.

# Integration Setup
Now that you have a good understanding of the AGE Invitation UI, you are ready to integrate AGE Invitation UI into your app.  The first step is to <a href="http://www.hookmobile.com"  target="_blank">signup</a> for an App Key for your app at Hook Mobile developer portal. This App Key will be used in the next section when you start doing your integration.

Next, you need to import the <a href="https://github.com/hookmobile/App-Growth-Engine-UI-Android/raw/master/ageui-1.2.0.jar"  target="_blank">ageui-1.2.0.jar</a> and <a href="https://github.com/hookmobile/App-Growth-Engine-UI-Android/raw/master/libs/age-1.1.5.jar"  target="_blank">age-1.1.5.jar</a> into your application project and include them to the project library path. 

Next, add the following permissions into your project's Manifest file:

<pre><code>		&lt;uses-permission android:name="android.permission.INTERNET"&gt;
    	&lt;uses-permission android:name="android.permission.READ_CONTACTS"&gt;
		&lt;uses-permission android:name="android.permission.READ_PHONE_STATE"&gt;
    	&lt;uses-permission android:name="android.permission.SEND_SMS"&gt;
    	&lt;uses-permission android:name="android.permission.ACCESS_NETWORK_STATE"&gt;
    	&lt;uses-permission android:name="android.permission.ACCESS_WIFI_STATE"&gt;</code></pre>

Lastly, please copy <a href="https://github.com/hookmobile/App-Growth-Engine-UI-Android/raw/master/age_styles.xml"  target="_blank">age_styles.xml</a> to your project <code>res/values/</code> folder.

# Invoking Invitation Plug-in

InvitationUI must be added to an existing Activity within your app.  If you have multiple Activities within your Android App, you will have to decide which Activity will host the InvitationUI component.  Once you have determined the host Activity, you will need to modify the Activity Java source as follow:
* Define a class variable of type InvitationUI in the Activity class.  Name the variable invitationUI.  
* Modify the <code>onCreate()</code> within your Activity to initialize the tab variable.  In the AgeUI constructor, you will need to pass 6 parameters:
	1. Host Activity
	2. App Key assigned by Hookmobile for your app
	3. Display title of AgeUI popup
	4. SMS invitation mechanism: Phone native SMS or Virutal Number
	5. App assigned UserId.  UserId will be associated to the app install and referenced on server-callback to identify app install.
	6. Display contact photo
* Modify <code>onPause()</code> methods to pass application state change event to AgeUI component.

Below is an example of activity with modification to use the AgeUI Plug-in.


<pre><code>package com.hookmobile.ageui.sample;

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
			public void onClick(List&lt;String&gt; phoneList) {
				// Print list of phone numbers invited by user.
				System.out.println("Number of Invitations Sent: " + phoneList.size());
				for(int i=0;i&lt;phoneList.size();i++){
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
</code></pre>


We understand that you may want a look and feel that is completely different from what AGE Invitation offers.  You can still take advantage of AGE invitation API by integrating with <a href="https://github.com/hookmobile/App-Growth-Engine-Android-SDK" target="_blank">AGE SDK</a>.  