package com.harsh.olympiadlab;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.Map;

/**
 * Robust Native Reminder Scheduler for Harsh's Olympiad Lab.
 * Manages notification channels, exact alarms via AlarmManager,
 * SharedPreferences persistence, and reboot re-registration.
 */
public class ReminderScheduler {

    private static final String TAG = "ReminderScheduler";
    public static final String CHANNEL_ID = "harsh_olympiad_reminders";
    public static final String ACTION_SHOW_REMINDER = "com.harsh.olympiadlab.ACTION_SHOW_REMINDER";
    private static final String PREFS_NAME = "harsh_reminders_storage";

    /**
     * Initializes the High-Priority Android Notification Channel (Android 8.0+)
     */
    public static void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            if (notificationManager != null) {
                NotificationChannel existing = notificationManager.getNotificationChannel(CHANNEL_ID);
                if (existing == null) {
                    NotificationChannel channel = new NotificationChannel(
                            CHANNEL_ID,
                            "Olympiad Lab Study & Timetable Reminders",
                            NotificationManager.IMPORTANCE_HIGH
                    );
                    channel.setDescription("Alerts for weekly timetable classes, study tasks, and calendar events");
                    channel.enableVibration(true);
                    channel.enableLights(true);
                    channel.setShowBadge(true);
                    notificationManager.createNotificationChannel(channel);
                    Log.d(TAG, "Notification channel created: " + CHANNEL_ID);
                }
            }
        }
    }

    /**
     * Schedules a one-time exact reminder at triggerTimeMs.
     */
    public static boolean scheduleExactReminder(
            Context context,
            String id,
            String title,
            String body,
            long triggerTimeMs,
            String targetTab,
            String category
    ) {
        try {
            createNotificationChannel(context);
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            if (alarmManager == null) return false;

            // Intent for broadcast receiver
            Intent intent = new Intent(context, NotificationReceiver.class);
            intent.setAction(ACTION_SHOW_REMINDER);
            intent.putExtra("id", id);
            intent.putExtra("title", title);
            intent.putExtra("body", body);
            intent.putExtra("targetTab", targetTab != null ? targetTab : "my_day");
            intent.putExtra("category", category != null ? category : "study");
            intent.putExtra("isRecurring", false);

            int requestCode = Math.abs(id.hashCode());
            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    context,
                    requestCode,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );

            // Check exact alarm permissions on Android 12+ (API 31+)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTimeMs, pendingIntent);
                } else {
                    alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTimeMs, pendingIntent);
                }
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTimeMs, pendingIntent);
            } else {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerTimeMs, pendingIntent);
            }

            // Persist reminder details in SharedPreferences
            saveReminderRecord(context, id, title, body, triggerTimeMs, false, 0, 0, 0, targetTab, category);
            Log.d(TAG, "Scheduled exact alarm for id=" + id + " at timestamp=" + triggerTimeMs);
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Failed to schedule exact reminder: " + id, e);
            return false;
        }
    }

    /**
     * Schedules a weekly recurring reminder (e.g. for weekly timetable slots).
     * @param dayOfWeek Calendar day (Calendar.SUNDAY = 1, Calendar.MONDAY = 2, ..., Calendar.SATURDAY = 7)
     */
    public static boolean scheduleWeeklyRecurringReminder(
            Context context,
            String id,
            String title,
            String body,
            int dayOfWeek,
            int hour,
            int minute,
            String targetTab,
            String category
    ) {
        try {
            createNotificationChannel(context);
            long triggerTimeMs = calculateNextTriggerTime(dayOfWeek, hour, minute);

            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            if (alarmManager == null) return false;

            Intent intent = new Intent(context, NotificationReceiver.class);
            intent.setAction(ACTION_SHOW_REMINDER);
            intent.putExtra("id", id);
            intent.putExtra("title", title);
            intent.putExtra("body", body);
            intent.putExtra("targetTab", targetTab != null ? targetTab : "timetable");
            intent.putExtra("category", category != null ? category : "timetable");
            intent.putExtra("isRecurring", true);
            intent.putExtra("dayOfWeek", dayOfWeek);
            intent.putExtra("hour", hour);
            intent.putExtra("minute", minute);

            int requestCode = Math.abs(id.hashCode());
            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    context,
                    requestCode,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTimeMs, pendingIntent);
                } else {
                    alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTimeMs, pendingIntent);
                }
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTimeMs, pendingIntent);
            } else {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerTimeMs, pendingIntent);
            }

            // Persist reminder details
            saveReminderRecord(context, id, title, body, triggerTimeMs, true, dayOfWeek, hour, minute, targetTab, category);
            Log.d(TAG, "Scheduled weekly reminder id=" + id + " next trigger=" + triggerTimeMs);
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Failed to schedule weekly reminder: " + id, e);
            return false;
        }
    }

    /**
     * Calculates next upcoming millisecond timestamp for dayOfWeek at hour:minute.
     */
    public static long calculateNextTriggerTime(int dayOfWeek, int hour, int minute) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, hour);
        cal.set(Calendar.MINUTE, minute);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

        int currentDayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
        int daysUntilTarget = dayOfWeek - currentDayOfWeek;
        if (daysUntilTarget < 0 || (daysUntilTarget == 0 && cal.getTimeInMillis() <= System.currentTimeMillis())) {
            daysUntilTarget += 7;
        }
        cal.add(Calendar.DAY_OF_YEAR, daysUntilTarget);
        return cal.getTimeInMillis();
    }

    /**
     * Cancels a scheduled alarm and removes it from storage.
     */
    public static boolean cancelReminder(Context context, String id) {
        try {
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            if (alarmManager != null) {
                Intent intent = new Intent(context, NotificationReceiver.class);
                intent.setAction(ACTION_SHOW_REMINDER);
                int requestCode = Math.abs(id.hashCode());
                PendingIntent pendingIntent = PendingIntent.getBroadcast(
                        context,
                        requestCode,
                        intent,
                        PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
                );
                alarmManager.cancel(pendingIntent);
                pendingIntent.cancel();
            }

            // Remove from SharedPreferences
            SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            prefs.edit().remove("reminder_" + id).apply();
            Log.d(TAG, "Cancelled reminder: " + id);
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Failed to cancel reminder: " + id, e);
            return false;
        }
    }

    /**
     * Clears all alarms and saved records.
     */
    public static boolean cancelAllReminders(Context context) {
        try {
            SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            Map<String, ?> all = prefs.getAll();
            for (String key : all.keySet()) {
                if (key.startsWith("reminder_")) {
                    String id = key.substring("reminder_".length());
                    cancelReminder(context, id);
                }
            }
            prefs.edit().clear().apply();
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Failed to cancel all reminders", e);
            return false;
        }
    }

    /**
     * Returns JSON array string representing all currently stored reminders.
     */
    public static String getAllRemindersJson(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        JSONArray arr = new JSONArray();
        Map<String, ?> all = prefs.getAll();
        for (Map.Entry<String, ?> entry : all.entrySet()) {
            if (entry.getKey().startsWith("reminder_") && entry.getValue() instanceof String) {
                try {
                    JSONObject obj = new JSONObject((String) entry.getValue());
                    arr.put(obj);
                } catch (JSONException ignored) {}
            }
        }
        return arr.toString();
    }

    /**
     * Reschedules all saved active reminders after device reboot.
     */
    public static void rescheduleAllOnBoot(Context context) {
        Log.d(TAG, "Rescheduling all active reminders on system boot...");
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        Map<String, ?> all = prefs.getAll();
        long now = System.currentTimeMillis();

        for (Map.Entry<String, ?> entry : all.entrySet()) {
            if (entry.getKey().startsWith("reminder_") && entry.getValue() instanceof String) {
                try {
                    JSONObject json = new JSONObject((String) entry.getValue());
                    String id = json.getString("id");
                    String title = json.getString("title");
                    String body = json.getString("body");
                    boolean isRecurring = json.optBoolean("isRecurring", false);
                    String targetTab = json.optString("targetTab", "my_day");
                    String category = json.optString("category", "study");

                    if (isRecurring) {
                        int dayOfWeek = json.getInt("dayOfWeek");
                        int hour = json.getInt("hour");
                        int minute = json.getInt("minute");
                        scheduleWeeklyRecurringReminder(context, id, title, body, dayOfWeek, hour, minute, targetTab, category);
                    } else {
                        long triggerTimeMs = json.getLong("triggerTimeMs");
                        if (triggerTimeMs > now) {
                            scheduleExactReminder(context, id, title, body, triggerTimeMs, targetTab, category);
                        } else {
                            // Expired one-time alarm: remove record
                            prefs.edit().remove(entry.getKey()).apply();
                        }
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Failed to restore reminder on boot: " + entry.getKey(), e);
                }
            }
        }
    }

    private static void saveReminderRecord(
            Context context,
            String id,
            String title,
            String body,
            long triggerTimeMs,
            boolean isRecurring,
            int dayOfWeek,
            int hour,
            int minute,
            String targetTab,
            String category
    ) {
        try {
            SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            JSONObject obj = new JSONObject();
            obj.put("id", id);
            obj.put("title", title);
            obj.put("body", body);
            obj.put("triggerTimeMs", triggerTimeMs);
            obj.put("isRecurring", isRecurring);
            obj.put("dayOfWeek", dayOfWeek);
            obj.put("hour", hour);
            obj.put("minute", minute);
            obj.put("targetTab", targetTab);
            obj.put("category", category);

            prefs.edit().putString("reminder_" + id, obj.toString()).apply();
        } catch (Exception e) {
            Log.e(TAG, "Failed to save reminder record: " + id, e);
        }
    }
}
