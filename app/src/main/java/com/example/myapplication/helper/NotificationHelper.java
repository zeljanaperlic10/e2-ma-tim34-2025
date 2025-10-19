package com.example.myapplication.helper;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONObject;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class NotificationHelper {

    private static final String TAG = "NotificationHelper";
    private static final String ONESIGNAL_APP_ID = "fa970c3c-325d-4ba7-a997-b456799b9376";
    private static final String ONESIGNAL_REST_API_KEY = "os_v2_app_7klqypbslvf2pkmxwrlhtg4toz256wlf4gmudbu2bleeu44qyj5sfty3olmvkzgv7bzg3zqqzmvlritv4ucw7wnpx2l3e3rw6j7klcy"; // Sa OneSignal Settings

    public static void sendNotificationToUser(String targetUserId, String title, String message) {
        new SendNotificationTask(targetUserId, title, message).execute();
    }

    private static class SendNotificationTask extends AsyncTask<Void, Void, Void> {
        private final String targetUserId;
        private final String title;
        private final String message;

        public SendNotificationTask(String targetUserId, String title, String message) {
            this.targetUserId = targetUserId;
            this.title = title;
            this.message = message;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                JSONObject notificationContent = new JSONObject();
                notificationContent.put("app_id", ONESIGNAL_APP_ID);

                // Šalji korisniku preko External User ID (Firebase UID)
                notificationContent.put("include_external_user_ids", new org.json.JSONArray().put(targetUserId));

                JSONObject headings = new JSONObject();
                headings.put("en", title);
                notificationContent.put("headings", headings);

                JSONObject contents = new JSONObject();
                contents.put("en", message);
                notificationContent.put("contents", contents);

                // Pošalji REST API poziv ka OneSignal
                URL url = new URL("https://onesignal.com/api/v1/notifications");
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setRequestProperty("Authorization", "Basic " + ONESIGNAL_REST_API_KEY);
                connection.setDoOutput(true);

                OutputStream os = connection.getOutputStream();
                os.write(notificationContent.toString().getBytes("UTF-8"));
                os.flush();
                os.close();

                int responseCode = connection.getResponseCode();
                Log.d(TAG, "OneSignal Response Code: " + responseCode);

                connection.disconnect();

            } catch (Exception e) {
                Log.e(TAG, "Error sending notification: " + e.getMessage(), e);
            }
            return null;
        }
    }
}
