package pt.ulisboa.tecnico.withoutnet.services.ble;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import pt.ulisboa.tecnico.withoutnet.R;

/* This is a service used to test a service's lifecycle */
public class TestService extends Service {
    private final static String TAG = "TestService";
    private Thread thread;
    private boolean threadIsRunning;

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        thread = new Thread(
                new Runnable() {
                    @Override
                    public void run() {
                        int counter = 0;
                        while (threadIsRunning) {
                            Log.d(TAG, "Test Service is running... " + counter);
                            counter++;
                            try {
                                Thread.sleep(2000);
                            } catch (InterruptedException e) {
                                //e.printStackTrace();
                                Log.d(TAG, "Thread interrupted while sleeping...");
                            }

                        }
                    }
                }
        );

        threadIsRunning = true;

        thread.start();

        final String CHANNELID = "Foreground Service ID";

        NotificationChannel channel = new NotificationChannel(
                CHANNELID,
                CHANNELID,
                NotificationManager.IMPORTANCE_LOW
        );

        getSystemService(NotificationManager.class).createNotificationChannel(channel);
        Notification.Builder notification = new Notification.Builder(this, CHANNELID)
                .setContentText("Test Service is running")
                .setContentTitle("Test Service enabled")
                .setSmallIcon(R.drawable.ic_launcher_background);

        startForeground(1001, notification.build());

        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        if(thread != null) {
            try {
                thread.interrupt();
                thread.join(1000);
                threadIsRunning = false;
            } catch (InterruptedException | SecurityException e) {
                e.printStackTrace();
            }
        }
        super.onDestroy();
        Log.d(TAG, "Test Service has been stopped.");
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
