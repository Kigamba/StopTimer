package com.ephraim.kigamba.tasktimer.util;

import android.app.Activity;
import android.content.Context;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.ephraim.kigamba.tasktimer.Stopwatch;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by Kigamba (nek.eam@gmail.com) on 01-May-2020
 */
public class Utils {

    public static void openKeyboard(@NonNull Context context, @NonNull IBinder windowToken) {
        InputMethodManager inputMethodManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.toggleSoftInputFromWindow(windowToken, InputMethodManager.SHOW_FORCED, 0);
    }

    public static void hideKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        //Find the currently focused view, so we can grab the correct window token from it.
        View view = activity.getCurrentFocus();
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = new View(activity);
        }

        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    public static String getDisplayString(@NonNull Stopwatch stopwatch) {
        String friendlyDate = formatDate(stopwatch.startTime);

        return String.format("%s at %s", stopwatch.title, friendlyDate);
    }

    public static String formatDate(long timeInMilliseconds) {
        timeInMilliseconds = timeInMilliseconds == 0 ? System.currentTimeMillis() : timeInMilliseconds;

        Calendar startTime = Calendar.getInstance();
        startTime.setTime(new Date(timeInMilliseconds));

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat();

        if (isToday(startTime)) {
            simpleDateFormat.applyLocalizedPattern("h:mm a");
        } else if (isThisYear(startTime)) {
            simpleDateFormat.applyLocalizedPattern("MMM d");
        } else {
            simpleDateFormat.applyLocalizedPattern("MMM d, yyyy");
        }

        return simpleDateFormat.format(startTime.getTime());
    }

    public static boolean isToday(Calendar timeToCheck) {
        return isSameDay(Calendar.getInstance(), timeToCheck);
    }

    public static boolean isThisYear(Calendar timeToCheck) {
        return isSameYear(Calendar.getInstance(), timeToCheck);
    }

    public static boolean isSameYear(Calendar cal1, Calendar cal2) {
        if (cal1 == null || cal2 == null) {
            throw new IllegalArgumentException("The dates must not be null");
        }

        return (cal1.get(Calendar.ERA) == cal2.get(Calendar.ERA) &&
                cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR));
    }

    public static boolean isSameDay(Calendar cal1, Calendar cal2) {
        if (cal1 == null || cal2 == null) {
            throw new IllegalArgumentException("The dates must not be null");
        }

        return (cal1.get(Calendar.ERA) == cal2.get(Calendar.ERA) &&
                cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR));
    }
}
