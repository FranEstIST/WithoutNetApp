package pt.ulisboa.tecnico.withoutnet;

import androidx.appcompat.app.AppCompatActivity;
import androidx.work.OneTimeWorkRequest;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import androidx.work.WorkRequest;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.util.concurrent.TimeUnit;

import pt.ulisboa.tecnico.withoutnet.databinding.ActivityMain2Binding;

public class MainActivity2 extends AppCompatActivity {

    private static final String TAG = "MainActivity2";

    private ActivityMain2Binding binding;
    private boolean isParticipating;
    private ReceiveAndPropagateUpdatesService receiveAndPropagateUpdatesService;

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d(TAG, "Service was connected.");

            receiveAndPropagateUpdatesService = ((ReceiveAndPropagateUpdatesService.LocalBinder) service).getService();

            if (receiveAndPropagateUpdatesService != null) {

                receiveAndPropagateUpdatesService.initialize();

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

        binding = ActivityMain2Binding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        isParticipating = false;

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

        Intent receiveAndPropagateUpdatesServiceIntent = new Intent(this, ReceiveAndPropagateUpdatesService.class);
        bindService(receiveAndPropagateUpdatesServiceIntent, serviceConnection, Context.BIND_AUTO_CREATE);

        Log.d(TAG, "Started service");
    }
}