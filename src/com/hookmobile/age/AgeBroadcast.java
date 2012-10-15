package com.hookmobile.age;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.util.Log;

public class AgeBroadcast extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {

		try {
			final Bundle extras = intent.getExtras();
			if (extras != null) {
				extras.containsKey(null);
			}
		} catch (final Exception e) {
			return;
		}

		Map<String, String> referralParams = new HashMap<String, String>();

		if (!intent.getAction().equals("com.android.vending.INSTALL_REFERRER")) {
			return;
		}

		String referrer = intent.getStringExtra("referrer");
		if (referrer == null || referrer.length() == 0) {
			return;
		}

		try {
			referrer = URLDecoder.decode(referrer, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			return;
		}

		String[] params = referrer.split("&");
		for (String param : params) {
			System.out.println(param);
			String[] pair = param.split("=");
			referralParams.put(pair[0], pair[1]);
		}

		storeReferralParams(context, referralParams);
		/*
		 * 
		 * 
		 * AGE has to span a thread to send the referrer information over network to AGE server.
		 * 
		 * 
		 */
		String packageName = "";
		try {

			ActivityInfo receiverInfo;
			receiverInfo = context.getPackageManager().getReceiverInfo(
					new ComponentName(context, AgeBroadcast.class),
					PackageManager.GET_META_DATA);

			Bundle bundle = receiverInfo.metaData;
			if (bundle != null)
				packageName = bundle.getString("packageName");
		} catch (NameNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		if (packageName != null && packageName.length() != 0) {
			Log.d("Package Name is :", packageName);

			try {
				((BroadcastReceiver) Class.forName(packageName).newInstance())
						.onReceive(context, intent);
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} catch (InstantiationException e) {
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
		}
	}

	public void storeReferralParams(Context context, Map<String, String> params) {
		SharedPreferences storage = context.getSharedPreferences(
				"AgeConstants.AGE_PREFERENCES", Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = storage.edit();

		for (String key : AgeConstants.EXPECTED_PARAMETERS) {
			String value = params.get(key);
			if (value != null) {
				// System.out.println(value);
				editor.putString(key, value);
			}
		}
		editor.commit();
	}
}