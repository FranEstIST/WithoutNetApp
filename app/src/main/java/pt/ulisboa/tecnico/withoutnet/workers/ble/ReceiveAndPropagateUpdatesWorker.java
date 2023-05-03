package pt.ulisboa.tecnico.withoutnet.workers.ble;

import android.Manifest;
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
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import java.util.List;
import java.util.UUID;

import pt.ulisboa.tecnico.withoutnet.GlobalClass;
import pt.ulisboa.tecnico.withoutnet.models.Update;
import pt.ulisboa.tecnico.withoutnet.services.ble.BleService;
import pt.ulisboa.tecnico.withoutnet.utils.ble.BleScanner;

public class ReceiveAndPropagateUpdatesWorker extends Worker {
    private static final long SCAN_PERIOD = 10000;

    private BleScanner scanner;
    private ScanCallback scanCallback;

    private static final String TAG = "ReceiveAndPropagateUpdatesWorker";

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

    public ReceiveAndPropagateUpdatesWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);

        this.globalClass = (GlobalClass) getApplicationContext();

        this.scanner = new BleScanner(getApplicationContext());

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
                final boolean connectResult = bleService.connect(device.getAddress());
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

        getApplicationContext().registerReceiver(gattUpdateReceiver, makeGattUpdateIntentFilter());

        /*Intent gattServiceIntent = new Intent(getApplicationContext(), BleService.class);
        getApplicationContext().startService(gattServiceIntent);*/

        // TODO: Should the service be started instead of being bound to the context?
        Intent gattServiceIntent = new Intent(getApplicationContext(), BleService.class);
        getApplicationContext().bindService(gattServiceIntent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BleService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BleService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BleService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BleService.ACTION_CHARACTERISTIC_READ);
        return intentFilter;
    }

    @NonNull
    @Override
    public Result doWork() {
        this.scanner.scan(SCAN_PERIOD, this.scanCallback);

        synchronized (this.scanner) {
            try {
                this.scanner.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        return Result.success();
    }
}
