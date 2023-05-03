package pt.ulisboa.tecnico.withoutnet;

import android.Manifest;
import android.app.Activity;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Binder;
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

    private LocalBinder binder = new LocalBinder();

    private Activity activity;

    private BleScanner scanner;
    private ScanCallback scanCallback;

    private static final String TAG = "ReceiveAndPropagateUpdatesService";

    private boolean connected = false;

    private BleService bleService;

    private GlobalClass globalClass;

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            if(bleService == null) {
                bleService = ((BleService.LocalBinder) service).getService();
            }
            if (bleService != null) {
                if (!bleService.initialize()) {
                    Log.e(TAG, "Unable to initialize Bluetooth");
                    return;
                }

                ReceiveAndPropagateUpdatesService.this.scanCallback = new ScanCallback() {
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
                        final boolean connectResult = bleService.connect(device.getAddress());
                        Log.d(TAG, "Connect request result = " + connectResult);
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

                ReceiveAndPropagateUpdatesService.this.scanner.scan(SCAN_PERIOD, ReceiveAndPropagateUpdatesService.this.scanCallback);

            }
            Log.d(TAG, "bleService is null.");
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            bleService = null;
            ReceiveAndPropagateUpdatesService.this.scanner.stopScanning();
            /*ReceiveAndPropagateUpdatesService.this.scanCallback = new ScanCallback() {
                @Override
                public void onScanResult(int callbackType, ScanResult result) {
                    super.onScanResult(callbackType, result);
                    Log.d(TAG, "Found device but BleService is not yet initialized");
                }

                @Override
                public void onBatchScanResults(List<ScanResult> results) {
                    super.onBatchScanResults(results);
                }

                @Override
                public void onScanFailed(int errorCode) {
                    super.onScanFailed(errorCode);
                }
            };*/
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

                BluetoothGattCharacteristic readCharacteristic = null;
                BluetoothGattCharacteristic writeCharacteristic = null;
                BluetoothGattCharacteristic interestedSensorCharacteristic = null;

                for (BluetoothGattService service : gattServiceList) {
                    Log.d(TAG, "GATT Service: " + service.getUuid().toString());
                    if (service.getUuid().toString().equals("b19fbebe-dbd4-11ed-afa1-0242ac120002")) {
                        List<BluetoothGattCharacteristic> updateCharacteristics = service.getCharacteristics();
                        for (BluetoothGattCharacteristic characteristic : updateCharacteristics) {
                            Log.d(TAG, "GATT Read Characteristic: " + characteristic.getUuid());
                        }

                        readCharacteristic = service
                                .getCharacteristic(UUID.fromString(BleGattIDs.READ_UPDATE_CHARACTERISTIC_ID));

                        writeCharacteristic = service
                                .getCharacteristic(UUID.fromString(BleGattIDs.WRITE_UPDATE_CHARACTERISTIC_ID));

                        interestedSensorCharacteristic = service
                                .getCharacteristic(UUID.fromString(BleGattIDs.RELEVANT_NODE_CHARACTERISTIC_ID));
                    }
                }

                if(readCharacteristic != null) {
                    Log.d(TAG, "Read characteristic is not null");
                    bleService.readCharacteristic(readCharacteristic);
                }

                if(writeCharacteristic != null) {
                    // Discover which sensors the actuator is interested in, and then write the most recent
                    // cached update from that sensor
                    Log.d(TAG, "Write characteristic is not null");

                    if(interestedSensorCharacteristic != null) {
                        bleService.setWriteUpdateCharacteristic(writeCharacteristic);
                        bleService.readCharacteristic(interestedSensorCharacteristic);
                    } else {
                        // TODO: Throw exception
                    }

                    //bleService.writeCharacteristic(writeCharacteristic);
                }

            } else if (BleService.ACTION_CHARACTERISTIC_READ.equals(action)) {
                Log.d(TAG, "Characteristic Read");

                String characteristicId = intent.getStringExtra("id");

                if(characteristicId.equals(BleGattIDs.READ_UPDATE_CHARACTERISTIC_ID)) {
                    String updateValue = intent.getStringExtra("value");
                    Log.d(TAG, "Characteristic value:" + updateValue);

                    Update update = new Update(updateValue);

                    globalClass.addUpdate(update);
                } else if(characteristicId.equals(BleGattIDs.RELEVANT_NODE_CHARACTERISTIC_ID)) {
                    String relevantNodeValue = intent.getStringExtra("value");
                    Log.d(TAG, "Characteristic value:" + relevantNodeValue);

                    Node node = new Node(relevantNodeValue);

                    Update updateToBeWritten = globalClass.getMostRecentUpdate(node);

                    BluetoothGattCharacteristic writeUpdateCharacteristic = bleService.getWriteUpdateCharacteristic();

                    writeUpdateCharacteristic.setValue(updateToBeWritten.toString());

                    bleService.writeCharacteristic(writeUpdateCharacteristic);
                } else {
                    // TODO: Throw an exception
                    Log.d(TAG, "Unknown characteristic id:" + characteristicId);
                }

                // Disconnect from node
                final boolean result = bleService.disconnect();
                Log.d(TAG, "Disconnect request result = " + result);

            } else {
                Log.d(TAG, "Unknown action received by broadcast receiver");
            }
        }
    };

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BleService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BleService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BleService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BleService.ACTION_CHARACTERISTIC_READ);
        return intentFilter;
    }

    public void initialize() {
        this.globalClass = (GlobalClass) getApplicationContext();

        this.scanner = new BleScanner(getApplicationContext());

        this.scanCallback = new ScanCallback() {
            @Override
            public void onScanResult(int callbackType, ScanResult result) {
                super.onScanResult(callbackType, result);
                Log.d(TAG, "Found device but BleService is not yet initialized");
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
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    class LocalBinder extends Binder {
        public ReceiveAndPropagateUpdatesService getService() {
            return ReceiveAndPropagateUpdatesService.this;
        }
    }

    @Override
    public boolean onUnbind(Intent intent) {
        getApplicationContext().unbindService(serviceConnection);
        return super.onUnbind(intent);
    }

    public boolean start() {
        registerReceiver(gattUpdateReceiver, makeGattUpdateIntentFilter());

        // TODO: Should the service be started instead of being bound to the context?
        Intent gattServiceIntent = new Intent(getApplicationContext(), BleService.class);
        getApplicationContext().bindService(gattServiceIntent, serviceConnection, Context.BIND_AUTO_CREATE);

        return true;
    }

    public boolean stop() {
        unregisterReceiver(gattUpdateReceiver);

        getApplicationContext().unbindService(serviceConnection);

        return true;
    }
}
