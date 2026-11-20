package online.chantel.sim;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import dalvik.system.PathClassLoader;

import java.lang.reflect.Method;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.util.Enumeration;

public class CellularController {
    private static final String TAG = "ChantelCellular";

    public static final String TBOX_APK = "/system/priv-app/MtkTboxAfDc/MtkTboxAfDc.apk";
    public static final String TELE_SERVICE = "vendor.mediatek.hardware.tbox.V1_0.ITeleService";
    public static final String INETWORK = "vendor.mediatek.hardware.tbox.V1_0.INetwork";
    public static final String APN_INFO = "vendor.mediatek.hardware.tbox.V1_0.ApnInfo";
    public static final int PROFILE_ID = 1;

    public interface StatusCallback {
        void onProgress(String message);
        void onFinished(boolean success, String message, String ip, long pingMs);
    }

    public static class NetStatus {
        public final boolean isConnected;
        public final String ipAddress;
        public final long pingMs;
        public final boolean isDnsOk;

        public NetStatus(boolean isConnected, String ipAddress, long pingMs, boolean isDnsOk) {
            this.isConnected = isConnected;
            this.ipAddress = ipAddress;
            this.pingMs = pingMs;
            this.isDnsOk = isDnsOk;
        }
    }

    private static ClassLoader sClassLoader;

    public static synchronized ClassLoader getTboxClassLoader() {
        if (sClassLoader == null) {
            try {
                sClassLoader = new PathClassLoader(TBOX_APK, CellularController.class.getClassLoader());
            } catch (Throwable t) {
                Log.e(TAG, "Failed to load " + TBOX_APK + ": " + t.getMessage());
            }
        }
        return sClassLoader;
    }

    public static String getCcmniIp() {
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces != null && interfaces.hasMoreElements()) {
                NetworkInterface iface = interfaces.nextElement();
                if (iface.getName().startsWith("ccmni") && iface.isUp()) {
                    Enumeration<InetAddress> addrs = iface.getInetAddresses();
                    while (addrs.hasMoreElements()) {
                        InetAddress addr = addrs.nextElement();
                        if (!addr.isLoopbackAddress() && addr instanceof Inet4Address) {
                            return addr.getHostAddress();
                        }
                    }
                }
            }
        } catch (Throwable t) {
            Log.w(TAG, "getCcmniIp error: " + t.getMessage());
        }
        return null;
    }

    public static NetStatus checkLiveStatus() {
        String ip = getCcmniIp();
        long ping = -1;
        boolean dnsOk = false;

        long start = System.currentTimeMillis();
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress("8.8.8.8", 53), 2000);
            ping = System.currentTimeMillis() - start;
        } catch (Throwable ignored) {
        }

        try {
            InetAddress addr = InetAddress.getByName("ya.ru");
            dnsOk = addr != null;
        } catch (Throwable ignored) {
        }

        boolean connected = ping >= 0;
        return new NetStatus(connected, ip != null ? ip : "Не назначен", ping, dnsOk);
    }

    public static void activateAsync(final String apn, final String user, final String pass,
                                     final int pdpType, final StatusCallback callback) {
        final Handler mainHandler = new Handler(Looper.getMainLooper());

        new Thread(() -> {
            postProgress(mainHandler, callback, "1/4 Подключение к модулю MediaTek T-Box...");
            ClassLoader loader = getTboxClassLoader();
            if (loader == null) {
                postResult(mainHandler, callback, false, "Ошибка: системный APK " + TBOX_APK + " не найден", null, -1);
                return;
            }

            // 1. Установка APN
            postProgress(mainHandler, callback, "2/4 Запись точки доступа APN: " + apn + "...");
            try {
                Class<?> teleClass = Class.forName(TELE_SERVICE, true, loader);
                Method getService = teleClass.getMethod("getService", String.class, boolean.class);
                Object service = getService.invoke(null, "tele_slot1", true);
                if (service == null) {
                    postResult(mainHandler, callback, false, "Ошибка: ITeleService (tele_slot1) вернул null", null, -1);
                    return;
                }

                Class<?> apnInfoClass = Class.forName(APN_INFO, true, loader);
                Object infoObj = apnInfoClass.getConstructor().newInstance();
                apnInfoClass.getField("profile_id").set(infoObj, PROFILE_ID);
                apnInfoClass.getField("apn_name").set(infoObj, apn != null && !apn.trim().isEmpty() ? apn.trim() : "internet");
                apnInfoClass.getField("username").set(infoObj, user != null ? user.trim() : "");
                apnInfoClass.getField("password").set(infoObj, pass != null ? pass.trim() : "");
                apnInfoClass.getField("pdp_type").set(infoObj, pdpType);
                apnInfoClass.getField("auto_proto").set(infoObj, 0);

                Method setApn = teleClass.getMethod("setApn", apnInfoClass);
                Object setRes = setApn.invoke(service, infoObj);
                Log.i(TAG, "setApn result: " + setRes);

                // 2. Перезапуск сессии передачи данных
                postProgress(mainHandler, callback, "3/4 Перезапуск сессии данных (deactivate / setup)...");
                try {
                    Method deactive = teleClass.getMethod("deactiveDataCall", int.class);
                    deactive.invoke(service, PROFILE_ID);
                } catch (Throwable t) {
                    Log.w(TAG, "deactiveDataCall error: " + t.getMessage());
                }

                safeSleep(1200L);

                try {
                    Method setup = teleClass.getMethod("setupDataCall", int.class);
                    setup.invoke(service, PROFILE_ID);
                } catch (Throwable t) {
                    Log.w(TAG, "setupDataCall error: " + t.getMessage());
                }

                safeSleep(2500L);

                // 3. Маршрутизация на ГУ
                postProgress(mainHandler, callback, "4/4 Перенаправление интернет-трафика на экран ГУ...");
                switchNetworkToHu(loader);

                // 4. Проверка результата
                safeSleep(2000L);
                NetStatus status = checkLiveStatus();
                if (status.isConnected) {
                    postResult(mainHandler, callback, true, "Интернет успешно настроен и работает!", status.ipAddress, status.pingMs);
                } else {
                    // Попробуем обновить маршруты еще раз
                    switchNetworkToHu(loader);
                    safeSleep(1500L);
                    NetStatus status2 = checkLiveStatus();
                    if (status2.isConnected) {
                        postResult(mainHandler, callback, true, "Интернет успешно настроен и работает!", status2.ipAddress, status2.pingMs);
                    } else {
                        postResult(mainHandler, callback, false, "Сессия поднята (IP: " + status2.ipAddress + "), но тестовый пинг не прошел. Проверьте баланс SIM или правильность APN.", status2.ipAddress, -1);
                    }
                }

            } catch (Throwable t) {
                Log.e(TAG, "Activate exception: " + t.getMessage(), t);
                postResult(mainHandler, callback, false, "Ошибка настройки: " + t.getMessage(), null, -1);
            }
        }, "ChantelCellularThread").start();
    }

    public static void switchNetworkToHu(ClassLoader loader) {
        try {
            Class<?> netClass = Class.forName(INETWORK, true, loader);
            Method getService = netClass.getMethod("getService", String.class, boolean.class);
            Object netService = getService.invoke(null, "net_slot1", true);
            if (netService != null) {
                Method switchMethod = netClass.getMethod("networkSwitchToHU", boolean.class);
                switchMethod.invoke(netService, true);

                Method setRouteMethod = netClass.getMethod("setRoute");
                setRouteMethod.invoke(netService);

                Method setDnsMethod = netClass.getMethod("setDnsServer", String.class);
                setDnsMethod.invoke(netService, "8.8.8.8");
                Log.i(TAG, "switchNetworkToHu successfully invoked");
            }
        } catch (Throwable t) {
            Log.w(TAG, "switchNetworkToHu failed: " + t.getMessage());
        }
    }

    private static void postProgress(Handler handler, StatusCallback cb, String msg) {
        if (cb != null) {
            handler.post(() -> cb.onProgress(msg));
        }
    }

    private static void postResult(Handler handler, StatusCallback cb, boolean ok, String msg, String ip, long ping) {
        if (cb != null) {
            handler.post(() -> cb.onFinished(ok, msg, ip, ping));
        }
    }

    private static void safeSleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
