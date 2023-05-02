package pt.ulisboa.tecnico.withoutnet;

import android.Manifest;
import android.app.Activity;
import android.app.Service;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.work.WorkerParameters;

import java.util.List;
import java.util.UUID;

public class ReceiveAndPropagateUpdatesService extends Service {
    private static final long SCAN_PERIOD = 10000;

    private Activity activity;

    private BleScanner scanner;
    private ScanCallback scanCallback;

    private static final String TAG = "ReceiveAndPropagateUpdatesService";

    private boolean connected = false;

    private BleService bleService;

    private String bleDeviceAddress = null;

    private GlobalClass globalClass;

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            bleService = ((BleService.LocalBinder) service).getService();
            if (bleService != null) {
                if (!bleService.initialize()) {
                    Log.e(TAG, "Unable to initialize Bluetooth");
                }

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
                Log.d(TAG, "Connected to node");

            } else if (BleService.ACTION_GATT_DISCONNECTED.equals(action)) {
                connected = false;
                Log.d(TAG, "Disconnected from node");
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
                    }
                }

                if(updateCharacteristic == null) {
                    // TODO: Throw an exception here
                }

                // Read the update characteristic
                bleService.readCharacteristic(updateCharacteristic);

            } else if (BleService.ACTION_CHARACTERISTIC_READ.equals(action)) {
                Log.d(TAG, "Characteristic Read");

                String updateValue = intent.getStringExtra("value");
                Log.d(TAG, "Characteristic value:" + updateValue);

                Update update = new Update(updateValue);

                globalClass.addUpdate(update);

                // Disconnect from node
                final boolean result = bleService.disconnect();
                Log.d(TAG, "Connect request result = " + result);
            } else {
                Log.d(TAG, "Unknown action received by broadcast receiver");
            }
        }
    };

    public ReceiveAndPropagateUpdatesService(Activity activity) {
        this.activity = activity;

        this.globalClass = (GlobalClass) this.activity.getApplicationContext();

        this.scanner = new BleScanner(this.activity);

        this.scanCallback = new ScanCallback() {
            @Override
            public void onScanResult(int callbackType, ScanResult result) {
                super.onScanResult(callbackType, result);

                BluetoothDevice device = result.getDevice();

                if (Build.VERSION.SDK_INT >= 31
                        && ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    return;
                }

                // Connect to node
                final boolean connectResult = bleService.connect(bleDeviceAddress);
                Log.d(TAG, "Disconnect request result = " + connectResult);
            }

            @Override
            public void onBatchScanResults(List<ScanResult> results) {
                super.onBatchScanResults(results);
            }

            @Override
            public void onScanFailed(int errorCode) {
                super.onScanFailed(errorCode);
            }
        };

        Intent gattServiceIntent = new Intent(this.activity, BleService.class);
        this.activity.startService(gattServiceIntent);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
