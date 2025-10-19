package com.example.myapplication.domain.service;

import android.app.Application;
import android.util.Log;

import com.onesignal.Continue;
import com.onesignal.OneSignal;
import com.onesignal.debug.LogLevel;

public class MyApplication extends Application {

    // STAVI OVDE TVOJ ONESIGNAL APP ID (dobićeš ga sa OneSignal dashboard-a)
    private static final String ONESIGNAL_APP_ID = "fa970c3c-325d-4ba7-a997-b456799b9376";

    @Override
    public void onCreate() {
        super.onCreate();

        // Enable verbose OneSignal logging to debug issues if needed.
        OneSignal.getDebug().setLogLevel(LogLevel.VERBOSE);

        // OneSignal Initialization
        OneSignal.initWithContext(this, ONESIGNAL_APP_ID);

        // Traži dozvolu za notifikacije
        OneSignal.getNotifications().requestPermission(true, Continue.none());
    }
}
