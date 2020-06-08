package com.ephraim.kigamba.tasktimer.reminder;

import android.app.IntentService;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;

import com.ephraim.kigamba.tasktimer.MainActivity;
import com.ephraim.kigamba.tasktimer.R;
import com.ephraim.kigamba.tasktimer.Stopwatch;
import com.ephraim.kigamba.tasktimer.speech.Speaker;

import timber.log.Timber;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class StopwatchIntervalReminderService extends Service {

    private Thread stopwatchIntervalReminderThread;
    private boolean timerRunning = true;
    public static final Uri URI = Stopwatch.Stopwatches.CONTENT_URI;
    private long threadSleepInterval = 15000;
    private Speaker speaker;

    public StopwatchIntervalReminderService() {
    }

    @Override
    public int onStartCommand(@Nullable Intent intent, int flags, int startId) {
        startForegroundService();
        startStopwatchIntervalReminder();
        return super.onStartCommand(intent, flags, startId);
    }

    private void startStopwatchIntervalReminder() {
        if (stopwatchIntervalReminderThread == null) {
            stopwatchIntervalReminderThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    while (timerRunning) {

                        try {
                            Thread.sleep(threadSleepInterval);

                            boolean isEnabled = getSharedPreferences()
                                    .getBoolean("stopwatchReminderIntervalEnabledPref", true);
                            checkRunningStopwatches();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });

            stopwatchIntervalReminderThread.start();
        }
    }


    private void stopStopwatchIntervalReminder() {
        timerRunning = false;
        if (stopwatchIntervalReminderThread != null) {
            stopwatchIntervalReminderThread.stop();
        }

    }

    private void checkRunningStopwatches() {
        long currentTime = System.currentTimeMillis();
        long reminderIntervalInSecs = Integer.parseInt(getSharedPreferences()
                .getString("stopwatchReminderIntervalPref", "3")) * 60L;

        Cursor cursor = this.getContentResolver().query(URI
                , new String[] {Stopwatch.Stopwatches.STOPWATCH_ID,
                        Stopwatch.Stopwatches.TITLE,
                        Stopwatch.Stopwatches.START_TIME,
                        Stopwatch.Stopwatches.RUNNING},
                Stopwatch.Stopwatches.RUNNING + " = " + 1, null, null);

        if (cursor != null) {
            while (cursor.moveToNext()) {
                long startTime = cursor.getLong(cursor.getColumnIndex(Stopwatch.Stopwatches.START_TIME));
                long reminder = (currentTime - startTime) % (reminderIntervalInSecs * 1000L);

                if (reminder <= threadSleepInterval) {
                    Timber.e("Remaining time is " + reminder);
                    String title = cursor.getString(cursor.getColumnIndex(Stopwatch.Stopwatches.TITLE));
                    int stopWatchId = cursor.getInt(cursor.getColumnIndex(Stopwatch.Stopwatches.STOPWATCH_ID));
                    notifyStopwatchReminder(title, stopWatchId);
                }
            }
        }
    }

    private SharedPreferences getSharedPreferences() {
        return getSharedPreferences(MainActivity.PREFS_NAME, Context.MODE_PRIVATE);
    }

    private void notifyStopwatchReminder(@NonNull String stopWatchTitle, int stopwatchId) {
        if (speaker == null) {
            speaker = new Speaker(this);
        }

        speaker.speak("Update stopwatch Timer for:");
        speaker.speak(stopWatchTitle);
    }

    @Override
    public void onDestroy() {
        stopStopwatchIntervalReminder();
        stopForegroundService();
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /* Used to build and start foreground service. */
    private void startForegroundService() {
        /*if (Build.VERSION.SDK_INT >= 26) {
            createNotificationChannel("my_service", "My Background Service");
        } else {*/
            // Create notification default intent.
            Intent intent = new Intent();
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);

            // Create notification builder.
            NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
            builder.setWhen(System.currentTimeMillis());
            builder.setSmallIcon(R.drawable.icon);
            // Make the notification max priority.
            builder.setPriority(Notification.PRIORITY_MAX);
            // Make head-up notification.
            builder.setFullScreenIntent(pendingIntent, true);
            builder.setContentTitle("Stopwatch Interval Reminder");
            builder.setContentText("Running...");
            builder.setContentInfo("Running...");

            // Build the notification.
            Notification notification = builder.build();

            // Start foreground service.
            startForeground(1, notification);
        //}
    }
/*

    private void createNotificationChannel(String channelId, String channelName) {
        Intent resultIntent = new Intent(this, MainActivity.class);
// Create the TaskStackBuilder and add the intent, which inflates the back stack
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addNextIntentWithParentStack(resultIntent);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompatSideChannelService chan = new NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_DEFAULT);
        chan.setLightColor(Color.BLUE);
        chan.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        assert manager != null;
        manager.createNotificationChannel(chan);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, channelId);
        Notification notification = notificationBuilder.setOngoing(true)
                .setSmallIcon(R.drawable.ic_launcher_background)
                .setContentTitle("App is running in background")
                .setPriority(NotificationManager.IMPORTANCE_MIN)
                .setCategory(Notification.CATEGORY_SERVICE)
                .setContentIntent(resultPendingIntent) //intent
                .build();
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify(1, notificationBuilder.build());
        startForeground(1, notification);
    }
*/
    private void stopForegroundService() {
        // Stop foreground service and remove the notification.
        stopForeground(true);

        // Stop the foreground service.
        stopSelf();
    }
}
