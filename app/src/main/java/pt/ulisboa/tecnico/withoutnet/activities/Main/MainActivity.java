package pt.ulisboa.tecnico.withoutnet.activities.Main;

import androidx.appcompat.app.AppCompatActivity;
import androidx.work.WorkManager;
import androidx.work.WorkRequest;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import pt.ulisboa.tecnico.withoutnet.GlobalClass;
import pt.ulisboa.tecnico.withoutnet.activities.Debug.CachedUpdatesActivity;
import pt.ulisboa.tecnico.withoutnet.activities.Debug.DebugActivity;
import pt.ulisboa.tecnico.withoutnet.R;
import pt.ulisboa.tecnico.withoutnet.databinding.ActivityMainBinding;
import pt.ulisboa.tecnico.withoutnet.models.Node;
import pt.ulisboa.tecnico.withoutnet.models.Update;
import pt.ulisboa.tecnico.withoutnet.services.ble.ReceiveAndPropagateUpdatesService;
import pt.ulisboa.tecnico.withoutnet.services.ble.TestService;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    private GlobalClass globalClass;

    private ActivityMainBinding binding;
    private boolean isParticipating;
    private boolean testServiceIsOn;
    private ReceiveAndPropagateUpdatesService receiveAndPropagateUpdatesService;

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d(TAG, "Service was connected.");

            receiveAndPropagateUpdatesService = ((ReceiveAndPropagateUpdatesService.LocalBinder) service).getService();

            if (receiveAndPropagateUpdatesService != null) {

                binding.startStopParticipatingButton.setOnClickListener(v -> {
                    Button button = (Button) v;

                    if(isParticipating) {
                        isParticipating = false;
                        button.setText(R.string.start_participating);

                        boolean result = receiveAndPropagateUpdatesService.stop();
                        Log.d(TAG, "Stop request result = " + result);
                    } else {
                        isParticipating = true;
                        button.setText(R.string.stop_participating);

                        boolean result = receiveAndPropagateUpdatesService.start();
                        Log.d(TAG, "Start request result = " + result);
                    }

                });

            }
            Log.d(TAG, "bleService is null.");
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            receiveAndPropagateUpdatesService = null;
        }
    };

    private WorkRequest receiveAndPropagateUpdatesWorkReq;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_main2);
        WorkManager.getInstance(getApplicationContext()).cancelAllWork();

        globalClass = (GlobalClass) getApplicationContext();

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        isParticipating = false;

        testServiceIsOn = false;


        binding.debugButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), DebugActivity.class);
                startActivity(intent);
            }
        });

        binding.cachedUpdatesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), CachedUpdatesActivity.class);
                startActivity(intent);
            }
        });

        /*
        //Intent receiveAndPropagateUpdatesServiceIntent = new Intent(this, ReceiveAndPropagateUpdatesService.class);
        //bindService(receiveAndPropagateUpdatesServiceIntent, serviceConnection, Context.BIND_AUTO_CREATE);
        */

        //Log.d(TAG, "Started service");

        binding.startStopParticipatingButton.setOnClickListener(v -> {
            Button button = (Button) v;

            if(isParticipating) {
                isParticipating = false;
                button.setText(R.string.start_participating);

                if(isServiceRunning(ReceiveAndPropagateUpdatesService.class)) {
                    Intent intent = new Intent(this, ReceiveAndPropagateUpdatesService.class);
                    stopService(intent);
                }
            } else {
                isParticipating = true;
                button.setText(R.string.stop_participating);

                if(!isServiceRunning(ReceiveAndPropagateUpdatesService.class)) {
                    Intent intent = new Intent(this, ReceiveAndPropagateUpdatesService.class);
                    startService(intent);
                }
            }

        });

        binding.startStopTestService.setOnClickListener(v -> {
            Button button = (Button) v;

            if(testServiceIsOn) {
                testServiceIsOn = false;
                button.setText(R.string.start_test_service);

                if(isServiceRunning(TestService.class)) {
                    Intent intent = new Intent(this, TestService.class);
                    stopService(intent);
                }
            } else {
                testServiceIsOn = true;
                button.setText(R.string.stop_test_service);

                if(!isServiceRunning(TestService.class)) {
                    Intent intent = new Intent(this, TestService.class);
                    startService(intent);
                }
            }

        });

        binding.uploadUpdatesButton.setOnClickListener(v -> {
            new Thread(() -> {
                GlobalClass globalClass = MainActivity.this.globalClass;
                for (Node node : globalClass.getAllUpdates().keySet()) {
                    int status = globalClass.getFrontend().sendUpdateToServer(globalClass.getMostRecentUpdate(node));
                    Log.d(TAG, "Upload updates status: " + status);
                }
            }).start();
        });

        binding.downloadUpdatesButton.setOnClickListener(v -> {
            new Thread(() -> {
                GlobalClass globalClass = MainActivity.this.globalClass;
                for (Node node : globalClass.getAllUpdates().keySet()) {
                    Update update = globalClass.getFrontend().getMostRecentUpdateByNodeFromServer(node);
                    if(update != null) {
                        globalClass.addUpdate(update);
                        Log.d(TAG, "Downloaded update from server: " + update);
                    } else {
                        Log.d(TAG, "No update was found on the server for node: " + node);
                    }
                }
            }).start();
        });
    }

    public boolean isServiceRunning(Class<?> cls){
        ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for(ActivityManager.RunningServiceInfo service: activityManager.getRunningServices(Integer.MAX_VALUE)) {
            if(cls.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
}