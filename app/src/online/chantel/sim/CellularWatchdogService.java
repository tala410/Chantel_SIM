package online.chantel.sim;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import java.util.concurrent.atomic.AtomicBoolean;

public class CellularWatchdogService extends Service {
    private static final String TAG = "ChantelWatchdog";
    private static final String CHANNEL_ID = "chantel_sim_watchdog";
    private static final int NOTIF_ID = 40702;

    public static final String PREFS_NAME = "chantel_sim_prefs";
    public static final String KEY_APN = "key_apn";
    public static final String KEY_USER = "key_user";
    public static final String KEY_PASS = "key_pass";
    public static final String KEY_PDP = "key_pdp";
    public static final String KEY_WATCHDOG_ENABLED = "key_watchdog_enabled";

    private final AtomicBoolean running = new AtomicBoolean(false);

    @Override
    public void onCreate() {
        super.onCreate();
        startMinimalForeground();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startMinimalForeground();
        boolean stop = intent != null && intent.getBooleanExtra("stop", false);
        if (stop) {
            running.set(false);
            stopSelf();
            return START_NOT_STICKY;
        }

        if (!running.compareAndSet(false, true)) {
            Log.i(TAG, "Watchdog loop already running");
            return START_STICKY;
        }

        new Thread(() -> {
            Log.i(TAG, "CellularWatchdog loop started");
            int healthyCycles = 0;

            while (running.get()) {
                try {
                    SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
                    boolean enabled = prefs.getBoolean(KEY_WATCHDOG_ENABLED, true);
                    if (!enabled) {
                        Log.i(TAG, "Watchdog disabled in settings, exiting loop");
                        break;
                    }

                    String apn = prefs.getString(KEY_APN, "internet");
                    String user = prefs.getString(KEY_USER, "");
                    String pass = prefs.getString(KEY_PASS, "");
                    int pdp = prefs.getInt(KEY_PDP, 0);

                    CellularController.NetStatus status = CellularController.checkLiveStatus();
                    if (!status.isConnected) {
                        Log.w(TAG, "Internet dropped! Auto-recovering APN [" + apn + "]...");
                        CellularController.activateAsync(apn, user, pass, pdp, null);
                        healthyCycles = 0;
                        Thread.sleep(15000L);
                    } else {
                        healthyCycles++;
                        // Раз в 2-3 минуты повторно гарантируем маршрутизацию на ГУ
                        if (healthyCycles % 6 == 0) {
                            ClassLoader loader = CellularController.getTboxClassLoader();
                            if (loader != null) {
                                CellularController.switchNetworkToHu(loader);
                            }
                        }
                    }

                    Thread.sleep(20000L);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                } catch (Throwable t) {
                    Log.e(TAG, "Watchdog iteration error: " + t.getMessage());
                    try {
                        Thread.sleep(10000L);
                    } catch (InterruptedException ignored) {}
                }
            }
            running.set(false);
        }, "ChantelWatchdogThread").start();

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        running.set(false);
        super.onDestroy();
    }

    private void startMinimalForeground() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager nm = getSystemService(NotificationManager.class);
            if (nm != null && nm.getNotificationChannel(CHANNEL_ID) == null) {
                NotificationChannel ch = new NotificationChannel(
                        CHANNEL_ID, "Chantel SIM Watchdog", NotificationManager.IMPORTANCE_MIN);
                nm.createNotificationChannel(ch);
            }
            Notification notif = new Notification.Builder(this, CHANNEL_ID)
                    .setContentTitle("Chantel SIM")
                    .setContentText("Мониторинг 4G интернета...")
                    .setSmallIcon(android.R.drawable.stat_sys_data_bluetooth)
                    .build();
            startForeground(NOTIF_ID, notif);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
