package pt.ulisboa.tecnico.withoutnet.activities.Main;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.work.WorkManager;
import androidx.work.WorkRequest;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.bluetooth.BluetoothAdapter;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import pt.ulisboa.tecnico.withoutnet.GlobalClass;
import pt.ulisboa.tecnico.withoutnet.activities.Debug.CachedUpdatesActivity;
import pt.ulisboa.tecnico.withoutnet.activities.Debug.DebugActivity;
import pt.ulisboa.tecnico.withoutnet.R;
import pt.ulisboa.tecnico.withoutnet.databinding.ActivityMainBinding;
import pt.ulisboa.tecnico.withoutnet.models.Node;
import pt.ulisboa.tecnico.withoutnet.models.Update;
import pt.ulisboa.tecnico.withoutnet.services.ble.BleService;
import pt.ulisboa.tecnico.withoutnet.services.ble.ReceiveAndPropagateUpdatesService;
import pt.ulisboa.tecnico.withoutnet.services.ble.TestService;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private static final int REQUEST_BLUETOOTH_PERMISSIONS = 1;

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

    private ActivityResultLauncher<Intent> bluetoothAdapterActivityResultLauncher;

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

        //Log.d(TAG, "Started service");

        binding.startStopParticipatingButton.setOnClickListener(v -> {
            if(isParticipating) {
                stopParticipating();
            } else {
                startParticipating();
            }
        });

        bluetoothAdapterActivityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == Activity.RESULT_OK) {
                            MainActivity.this.startParticipating();
                        } else {
                            Toast.makeText(MainActivity.this, "WithoutNet participation cannot be enabled due to Bluetooth not being enabled", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void startParticipating() {
        ImageButton imageButton = (ImageButton) findViewById(R.id.start_stop_participating_button);
        TextView participationStatusTextView = (TextView) findViewById(R.id.participation_status_text_view);
        TextView pressButtonTextView = (TextView) findViewById(R.id.press_button_text_view);

        ArrayList<String> permissionsList = new ArrayList<>();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "Bluetooth connect permission not granted. Asking for permission.");
            permissionsList.add(Manifest.permission.BLUETOOTH_CONNECT);
            return;
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED){
            Log.d(TAG, "Bluetooth scan permission not granted. Asking for permission.");
            permissionsList.add(Manifest.permission.BLUETOOTH_SCAN);

        } else if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "Fine location permissions not granted. Asking user for permission.");
            permissionsList.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }

        if(!permissionsList.isEmpty()) {
            String[] permissionsArray = new String[permissionsList.size()];
            permissionsList.toArray(permissionsArray);
            ActivityCompat.requestPermissions(MainActivity.this, permissionsArray, REQUEST_BLUETOOTH_PERMISSIONS);
            return;
        }

        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if(bluetoothAdapter == null) {
            // This device does not support Bluetooth
            Log.d(TAG, "Bluetooth is unavailable");
            Toast.makeText(this, "WithoutNet participation cannot be enabled due to Bluetooth being unavailable on this device", Toast.LENGTH_SHORT).show();
            return;
        } else if (!bluetoothAdapter.isEnabled()) {
            // Bluetooth is not enabled
            // Ask user to enable Bluetooth
            Log.d(TAG, "Bluetooth is not enabled. Asking user to enable Bluetooth.");
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            bluetoothAdapterActivityResultLauncher.launch(enableBtIntent);
            return;
        }


        isParticipating = true;

        imageButton.setForeground(getDrawable(R.drawable.wn_button_on));
        participationStatusTextView.setText(R.string.participating);
        pressButtonTextView.setText(R.string.press_to_stop_participating);

        if(!isServiceRunning(ReceiveAndPropagateUpdatesService.class)) {
            Intent intent = new Intent(this, ReceiveAndPropagateUpdatesService.class);
            startService(intent);
        }
    }

    private void stopParticipating() {
        ImageButton imageButton = (ImageButton) findViewById(R.id.start_stop_participating_button);
        TextView participationStatusTextView = (TextView) findViewById(R.id.participation_status_text_view);
        TextView pressButtonTextView = (TextView) findViewById(R.id.press_button_text_view);

        isParticipating = false;

        imageButton.setForeground(getDrawable(R.drawable.wn_button_off));
        participationStatusTextView.setText(R.string.not_participating);
        pressButtonTextView.setText(R.string.press_to_start_participating);

        if (isServiceRunning(ReceiveAndPropagateUpdatesService.class)) {
            Intent intent = new Intent(this, ReceiveAndPropagateUpdatesService.class);
            stopService(intent);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;

        switch(item.getItemId()) {
            case R.id.action_start_test_service:
                return true;
            case R.id.action_scan_nodes:
                intent = new Intent(getApplicationContext(), DebugActivity.class);
                startActivity(intent);
                return true;
            case R.id.action_upload_updates:
                new Thread(() -> {
                    GlobalClass globalClass = MainActivity.this.globalClass;
                    for (Node node : globalClass.getAllUpdates().keySet()) {
                        int status = globalClass.getFrontend().sendUpdateToServer(globalClass.getMostRecentUpdate(node));
                        Log.d(TAG, "Upload updates status: " + status);
                    }
                }).start();
                return true;
            case R.id.action_download_updates:
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
                return true;
            case R.id.action_view_cached_updates:
                intent = new Intent(getApplicationContext(), CachedUpdatesActivity.class);
                startActivity(intent);
                return true;
            default:
                return false;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case REQUEST_BLUETOOTH_PERMISSIONS:
                for(int grantResult : grantResults) {
                    if(grantResult == PackageManager.PERMISSION_DENIED) {
                        Toast.makeText(this, "WithoutNet participation cannot be enabled due to insufficient permissions", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
                startParticipating();
                break;
        }
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