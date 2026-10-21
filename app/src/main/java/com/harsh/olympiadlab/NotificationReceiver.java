package com.harsh.olympiadlab;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

/**
 * BroadcastReceiver triggered by AlarmManager alarms.
 * Builds and posts high-priority system notifications and handles recurring reminder scheduling.
 */
public class NotificationReceiver extends BroadcastReceiver {

    private static final String TAG = "NotificationReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null || !ReminderScheduler.ACTION_SHOW_REMINDER.equals(intent.getAction())) {
            return;
        }

        String id = intent.getStringExtra("id");
        String title = intent.getStringExtra("title");
        String body = intent.getStringExtra("body");
        String targetTab = intent.getStringExtra("targetTab");
        String category = intent.getStringExtra("category");
        boolean isRecurring = intent.getBooleanExtra("isRecurring", false);

        if (id == null) id = "reminder_" + System.currentTimeMillis();
        if (title == null) title = "Study Reminder";
        if (body == null) body = "Time for scheduled Olympiad practice session!";
        if (targetTab == null) targetTab = "my_day";

        Log.d(TAG, "Posting notification for reminder id=" + id + " tab=" + targetTab);

        // Ensure notification channel is ready
        ReminderScheduler.createNotificationChannel(context);

        // Intent to launch MainActivity with deep link targetTab
        Intent launchIntent = new Intent(context, MainActivity.class);
        launchIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        launchIntent.putExtra("targetTab", targetTab);
        launchIntent.putExtra("reminderId", id);

        int requestCode = Math.abs(id.hashCode());
        PendingIntent contentIntent = PendingIntent.getActivity(
                context,
                requestCode,
                launchIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, ReminderScheduler.CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentTitle(title)
                .setContentText(body)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(body))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_REMINDER)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setVibrate(new long[]{0, 250, 150, 250})
                .setContentIntent(contentIntent);

        try {
            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
            notificationManager.notify(requestCode, builder.build());
        } catch (SecurityException se) {
            Log.e(TAG, "Notification permission not granted to post notification", se);
        } catch (Exception e) {
            Log.e(TAG, "Failed to post notification", e);
        }

        // If recurring weekly, automatically compute and schedule next week (+7 days)
        if (isRecurring) {
            int dayOfWeek = intent.getIntExtra("dayOfWeek", 2);
            int hour = intent.getIntExtra("hour", 16);
            int minute = intent.getIntExtra("minute", 30);
            ReminderScheduler.scheduleWeeklyRecurringReminder(
                    context,
                    id,
                    title,
                    body,
                    dayOfWeek,
                    hour,
                    minute,
                    targetTab,
                    category
            );
        }
    }
}
