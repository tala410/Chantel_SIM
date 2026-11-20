package online.chantel.sim;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.Log;

public class BootReceiver extends BroadcastReceiver {
    private static final String TAG = "ChantelBoot";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent != null ? intent.getAction() : "";
        Log.i(TAG, "Received broadcast action: " + action);

        SharedPreferences prefs = context.getSharedPreferences(
                CellularWatchdogService.PREFS_NAME, Context.MODE_PRIVATE);
        boolean enabled = prefs.getBoolean(CellularWatchdogService.KEY_WATCHDOG_ENABLED, true);

        if (!enabled) {
            Log.i(TAG, "Watchdog is disabled by user, skipping start on boot");
            return;
        }

        try {
            Intent serviceIntent = new Intent(context, CellularWatchdogService.class);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(serviceIntent);
            } else {
                context.startService(serviceIntent);
            }
            Log.i(TAG, "CellularWatchdogService launched from BootReceiver");
        } catch (Throwable t) {
            Log.e(TAG, "Failed to start service from BootReceiver: " + t.getMessage(), t);
        }
    }
}
