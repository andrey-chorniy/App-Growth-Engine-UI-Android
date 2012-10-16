# Introduction

AGE Invitation UI is a drop-in Android control that you can deploy to your app within minutes.  AGE Invitation UI will help your users identify and share your app with friends & associates having compatible mobile devices and highest LTV.  For complete feature set and how it works, please visit <a href="http://www.hookmobile.com"  target="_blank">Hook Mobile</a>.

# Getting Sample App Up and Running
A sample app is included with this project that demonstrate AGE Invitation UI launched from a button.  Once you download and import the AgeUI folder into an Eclipse Android project, click on run.  You may launch the sample app in the emulator or your Android device.  Running on your Android phone is preferred because you are likely to have more contacts in your phone address book to invite from.    

[![](https://dl.dropbox.com/s/yr0wcs0dpm79552/ageui_android1.png)](https://dl.dropbox.com/s/yr0wcs0dpm79552/ageui_android1.png)
[![](https://dl.dropbox.com/s/6vq3rktozsin91w/ageui_android2.png)](https://dl.dropbox.com/s/6vq3rktozsin91w/ageui_android2.png)

When AGE Invitation UI is invoked for first time, it will analyze the address book.  It may take a few seconds before the list of suggested contacts is displayed.  The list of contacts shown in the list is filtered by criteria you define for your app profile in our developer portal.  

Select one or more entries from the suggested and click on the <b>Send</b> button to fire off the invitation text message.  The recipient(s) of the invitation will receive a personalized text message on their phone:

[![](https://dl.dropbox.com/s/zg3qbf5ac8om7cg/inviteSms.PNG)](https://dl.dropbox.com/s/zg3qbf5ac8om7cg/inviteSms.PNG)

The message is completely customizable by you, and it can be further personalized to include the sender and app name.

# Integration Setup
Now that you have a good understanding of the AGE Invitation UI, you are ready to integrate AGE Invitation UI into your app.  Import the Agepopup.jar file into your application project and add it to the project buildpath. 

Next, add the following permissions into your project's Manifest file:

<pre><code>	&lt;uses-permission android:name="android.permission.INTERNET"&gt;
    	&lt;uses-permission android:name="android.permission.READ_CONTACTS"&gt;
	&lt;uses-permission android:name="android.permission.READ_PHONE_STATE"&gt;
    	&lt;uses-permission android:name="android.permission.SEND_SMS"&gt;
    	&lt;uses-permission android:name="android.permission.ACCESS_NETWORK_STATE"&gt;
    	&lt;uses-permission android:name="android.permission.ACCESS_WIFI_STATE"&gt;</code></pre>


# Use the Invitation Plug-in

Popup must be added to an existing Activity within your app.  If you have multiple Activities within your Android App, you will have to decide which Activity will host the Popup component.  Once you have determined the host Activity, you will need to modify the Activity Java source as follow:
* Define a class variable of type InvitationUI in the Activity class.  Name the variable invitationUI.  
* Modify the <code>onCreate()</code> within your Activity to initialize the tab variable.  In the AgeUI constructor, you will need to pass 3 parameters:
1. Host Activity
2. App Key assigned by Hookmobile for your app
3. Name for the AgeUI
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

	private String appKey = "b9ef3007-c9a9-459d-977a-a62125cf6b1e";
	private Button showButton;
	private InvitationUI invitationUI;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_agepopup);

		invitationUI = new InvitationUI(this, appKey, "Get Points");

		showButton = (Button) findViewById(R.id.button1);
		showButton.setText("Show AGE UI");

		InvitationListener sendlistener = new InvitationListener() {

			@Override
			public void onClick(List&lt;String&gt; phoneList) {
				System.out.println(phoneList.size());
				for(int i=0;i&lt;phoneList.size();i++){
					System.out.println(phoneList.get(i));
				}
			}
		};
		
		invitationUI.setInvitationListener(sendlistener);

		showButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

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


We understand that you may want a look and feel that is completely different from what AGE Invitation offers.  You can still take advantage of AGE invitation API by integrating with <a href="https://github.com/hookmobile/App-Growth-Engine-iOS-SDK" target="_blank">AGE SDK</a>.  