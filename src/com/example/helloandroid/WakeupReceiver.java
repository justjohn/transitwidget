package com.example.helloandroid;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class WakeupReceiver extends BroadcastReceiver {

	private static final String TAG = WakeupReceiver.class.getName();

	@Override
	public void onReceive(Context arg0, Intent intent) {
		Log.i(TAG, "onReceive: " + intent.getAction());

	}

}
