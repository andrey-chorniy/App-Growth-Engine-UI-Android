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

	private String appKey = "4992ca90-fcb9-4250-b0e1-947e611555a0";
	private Button showButton;
	private InvitationUI agepopupView;
	
	// set to false to send invitation via native SMS from the phone
	private boolean useVirtualNumber = false;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_agepopup);

		// agepopupView = new InvitationUI(this, appKey, "Get Points", useVirtualNumber);

		// *** USE following constructor to pass custom parameter such as app assigned user id to AGE to be correlated to specific app install. ***
		agepopupView = new InvitationUI(this, appKey, "Get Points", useVirtualNumber, "custom parameter");

		showButton = (Button) findViewById(R.id.button1);
		showButton.setText("Show AGE Popup");

		InvitationListener sendlistener = new InvitationListener() {

			@Override
			public void onClick(List<String> phoneList) {
				System.out.println(phoneList.size());
				for(int i=0;i<phoneList.size();i++){
					System.out.println(phoneList.get(i));
				}
			}
		};
		
		agepopupView.setInvitationListener(sendlistener);

		showButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				agepopupView.showView();

			}
		});

	}

	@Override
	protected void onPause() {
		super.onPause();

		agepopupView.cleanup();
	}

}
