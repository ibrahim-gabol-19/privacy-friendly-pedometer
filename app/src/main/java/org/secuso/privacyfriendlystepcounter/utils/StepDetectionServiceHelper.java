package org.secuso.privacyfriendlystepcounter.utils;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import org.secuso.privacyfriendlystepcounter.Factory;
import org.secuso.privacyfriendlystepcounter.receivers.StepCountPersistenceReceiver;
import org.secuso.privacyfriendlystepcounter.services.StepPermanentNotificationService;

import java.util.Calendar;
import java.util.Date;

import privacyfriendlyexample.org.secuso.example.R;

/**
 * Helper class to start and stop the necessary services
 *
 * @author Tobias Neidig
 * @version 20160614
 */
public class StepDetectionServiceHelper {

    /**
     * Starts the step detection, persistence service and notification service if they are enabled in settings.
     *
     * @param context The application context.
     */
    public static void startAllIfEnabled(Context context) {
        // Get user preferences
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        boolean isStepDetectionEnabled = sharedPref.getBoolean(context.getString(R.string.pref_step_counter_enabled), true);
        boolean isPermanentNotificationEnabled = sharedPref.getBoolean(context.getString(R.string.pref_permanent_notification_enabled), true);

        // Start the step detection if enabled
        if (isStepDetectionEnabled) {
            StepDetectionServiceHelper.startStepDetection(context);
            // schedule stepCountPersistenceService
            StepDetectionServiceHelper.schedulePersistenceService(context);
        }
    }

    /**
     * Starts the step detection service
     *
     * @param context The application context
     */
    public static void startStepDetection(Context context) {
        Intent stepDetectorServiceIntent = new Intent(context, Factory.getStepDetectorServiceClass(context.getPackageManager()));
        context.startService(stepDetectorServiceIntent);
    }

    /**
     *  Schedules the step count persistence service.
     *
     * @param context The application context
     */
    public static void schedulePersistenceService(Context context) {
        Intent stepCountPersistenceServiceIntent = new Intent(context, StepCountPersistenceReceiver.class);
        PendingIntent sender = PendingIntent.getBroadcast(context, 2, stepCountPersistenceServiceIntent, 0);
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        // Fire at next half hour
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        int unroundedMinutes = calendar.get(Calendar.MINUTE);
        int mod = unroundedMinutes % 30;
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        calendar.add(Calendar.MINUTE, (30-mod));

        // Set repeating alarm
        am.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTime().getTime(), AlarmManager.INTERVAL_HOUR, sender);
    }
}