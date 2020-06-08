package com.ephraim.kigamba.tasktimer;

import android.app.Application;
import android.content.Intent;

import com.ephraim.kigamba.tasktimer.reminder.StopwatchIntervalReminderService;

import timber.log.Timber;

/**
 * Created by Kigamba (nek.eam@gmail.com) on 07-June-2020
 */
public class StopwatchApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        Timber.plant(new Timber.DebugTree());
        startService(new Intent(this, StopwatchIntervalReminderService.class));
    }
}
