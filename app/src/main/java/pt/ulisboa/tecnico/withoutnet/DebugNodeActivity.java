package pt.ulisboa.tecnico.withoutnet;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
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
import android.widget.TextView;
import android.widget.Toast;

import java.nio.charset.StandardCharsets;
import java.util.List;
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
    private Button readUpdateButton;
    private RecyclerView updateFieldsRV;
    private UpdateFieldsAdapter updateFieldsAdapter;

    private GlobalClass globalClass;

    private BluetoothGattCharacteristic updateCharacteristic;

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

            Log.d(TAG, "Broadcast received");

            if (BleService.ACTION_GATT_CONNECTED.equals(action)) {
                connected = true;
                //updateConnectionState(R.string.connected);
                Log.d(TAG, "Connected to node");
                //.makeText(DebugNodeActivity.this, "Connected to node", Toast.LENGTH_SHORT).show();

                connectButton.setText(R.string.Disconnect);
                connectButton.setOnClickListener(v -> {
                    final boolean result = bleService.disconnect();
                    Log.d(TAG, "Connect request result=" + result);
                });


            } else if (BleService.ACTION_GATT_DISCONNECTED.equals(action)) {
                connected = false;
                //updateConnectionState(R.string.disconnected);
                Log.d(TAG, "Disconnected from node");
                //Toast.makeText(DebugNodeActivity.this, "Disconnected from node", Toast.LENGTH_SHORT).show();

                connectButton.setText(R.string.Connect);
                connectButton.setOnClickListener(v -> {
                    final boolean result = bleService.connect(bleDeviceAddress);
                    Log.d(TAG, "Disconnect request result=" + result);
                });
            } else if (BleService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                Log.d(TAG, "GATT Services discovered");

                List<BluetoothGattService> gattServiceList = bleService.getSupportedGattServices();

                BluetoothGattCharacteristic updateCharacteristic = null;

                for (BluetoothGattService service : gattServiceList) {
                    Log.d(TAG, "GATT Service: " + service.getUuid().toString());
                    if (service.getUuid().toString().equals("b19fbebe-dbd4-11ed-afa1-0242ac120002")) {
                        List<BluetoothGattCharacteristic> updateCharacteristics = service.getCharacteristics();
                        for (BluetoothGattCharacteristic characteristic : updateCharacteristics) {
                            Log.d(TAG, "GATT Characteristic:" + characteristic.getUuid());
                        }

                        updateCharacteristic = service
                                .getCharacteristic(UUID.fromString("c6283536-dbd5-11ed-afa1-0242ac120002"));
                        //bleService.readCharacteristic(updateCharacteristic);
                    }
                }

                if(updateCharacteristic == null) {
                    // TODO: Throw an exception here
                }

                BluetoothGattCharacteristic finalUpdateCharacteristic = updateCharacteristic;
                readUpdateButton.setVisibility(View.VISIBLE);
                readUpdateButton.setOnClickListener(v -> {
                    bleService.readCharacteristic(finalUpdateCharacteristic);
                });
            } else if (BleService.ACTION_CHARACTERISTIC_READ.equals(action)) {
                Log.d(TAG, "Characteristic Read");

                String updateValue = intent.getStringExtra("value");
                Log.d(TAG, "Characteristic value:" + updateValue);

                Update update = new Update(updateValue);

                globalClass.addUpdate(update);

                //updateTextView.setText(updateValue);

                updateFieldsAdapter.setUpdate(update);
                updateFieldsAdapter.notifyDataSetChanged();

            } else {
                Log.d(TAG, "Unknown action received by broadcast receiver");
            }
        }
    };

    @SuppressLint("MissingPermission")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivityDebugNodeBinding binding = ActivityDebugNodeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        globalClass = (GlobalClass) getApplicationContext();

        connectButton = binding.connectButton;
        readUpdateButton = binding.readUpdateButton;
        updateFieldsRV = binding.updateFieldList;

        updateFieldsAdapter = new UpdateFieldsAdapter();

        updateFieldsRV.setAdapter(updateFieldsAdapter);

        updateFieldsRV.setLayoutManager(new LinearLayoutManager(this));

        updateFieldsRV.addItemDecoration(new DividerItemDecoration(this,
                DividerItemDecoration.VERTICAL));

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
        intentFilter.addAction(BleService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BleService.ACTION_CHARACTERISTIC_READ);
        return intentFilter;
    }
}