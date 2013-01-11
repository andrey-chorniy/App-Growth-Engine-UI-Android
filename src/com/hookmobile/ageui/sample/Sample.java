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

	private String appKey = "b9ef3007-c9a9-459d-977a-a62125cf6b1e";
	private Button showButton;
	private InvitationUI agepopupView;
	private boolean useVirtualNumber = true;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_agepopup);

		agepopupView = new InvitationUI(this, appKey, "Get Points", useVirtualNumber);

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
