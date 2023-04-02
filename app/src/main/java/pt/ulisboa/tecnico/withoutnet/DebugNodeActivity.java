package pt.ulisboa.tecnico.withoutnet;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.util.UUID;

import pt.ulisboa.tecnico.withoutnet.databinding.ActivityDebugNodeBinding;

public class DebugNodeActivity extends AppCompatActivity {

    private static final String TAG = "DebugNodeActivity";

    private BluetoothGattCallback bluetoothGattCallback;

    private int connectionState = BluetoothProfile.STATE_DISCONNECTED;
    private boolean connected = false;

    private BleService bleService;

    private String bleDeviceAddress = null;

    private Button connectButton;

    //private View.OnClickListener connectOnClickListener

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            bleService = ((BleService.LocalBinder) service).getService();
            if (bleService != null) {
                if (!bleService.initialize()) {
                    Log.e(TAG, "Unable to initialize Bluetooth");
                    finish();
                }

                // perform device connection
                connectButton.setOnClickListener(v -> {
                    final boolean result = bleService.connect(bleDeviceAddress);
                    Log.d(TAG, "Connect request result=" + result);
                });

            }
            Log.d(TAG, "bleService is null.");
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            bleService = null;
        }
    };

    private final BroadcastReceiver gattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (BleService.ACTION_GATT_CONNECTED.equals(action)) {
                connected = true;
                //updateConnectionState(R.string.connected);
                Log.d(TAG, "Connected to node");
                Toast.makeText(DebugNodeActivity.this, "Connected to node", Toast.LENGTH_SHORT).show();

                connectButton.setText(R.string.Disconnect);
                connectButton.setOnClickListener(v -> {
                    final boolean result = bleService.disconnect();
                    Log.d(TAG, "Connect request result=" + result);
                });
            } else if (BleService.ACTION_GATT_DISCONNECTED.equals(action)) {
                connected = false;
                //updateConnectionState(R.string.disconnected);
                Log.d(TAG, "Disconnected from node");
                Toast.makeText(DebugNodeActivity.this, "Disconnected from node", Toast.LENGTH_SHORT).show();

                connectButton.setText(R.string.Connect);
                connectButton.setOnClickListener(v -> {
                    final boolean result = bleService.connect(bleDeviceAddress);
                    Log.d(TAG, "Disconnect request result=" + result);
                });
            }
        }
    };

    @SuppressLint("MissingPermission")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivityDebugNodeBinding binding = ActivityDebugNodeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        connectButton = binding.connectButton;

        bleDeviceAddress = getIntent().getStringExtra("Address");

        Log.d(TAG, "Received addressed: " + bleDeviceAddress);

        Intent gattServiceIntent = new Intent(this, BleService.class);
        bindService(gattServiceIntent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onResume() {
        super.onResume();

        registerReceiver(gattUpdateReceiver, makeGattUpdateIntentFilter());
        if (bleService != null) {
            final boolean result = bleService.connect(bleDeviceAddress);
            Log.d(TAG, "Connect request result=" + result);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(gattUpdateReceiver);
    }

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BleService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BleService.ACTION_GATT_DISCONNECTED);
        return intentFilter;
    }
}