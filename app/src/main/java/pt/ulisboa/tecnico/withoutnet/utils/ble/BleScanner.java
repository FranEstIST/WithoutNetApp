package pt.ulisboa.tecnico.withoutnet.utils.ble;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.ParcelUuid;
import android.util.Log;

import androidx.core.app.ActivityCompat;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import pt.ulisboa.tecnico.withoutnet.services.ble.ReceiveAndPropagateUpdatesService;

public class BleScanner {
    private static final String TAG = "BleScanner";

    //private static final long SCAN_TIMEOUT = 10000;

    private Context context;
    private BluetoothLeScanner bluetoothLeScanner;
    private boolean scanning;
    private ScanCallback scanCallback;
    private long scanPeriod;

    private long lastStartScanTime;

    private Timer timer;

    public BleScanner(Context context, ScanCallback scanCallback, long scanPeriod) {
        this.context = context;
        this.scanCallback = scanCallback;
        this.scanning = false;
        this.scanPeriod = scanPeriod;

        BluetoothManager bluetoothManager = this.context.getSystemService(BluetoothManager.class);
        BluetoothAdapter bluetoothAdapter = bluetoothManager.getAdapter();
        bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();

        timer = new Timer("ScanTimer");
    }

    // TODO: Is scan required to be synchronized?
    @SuppressLint("MissingPermission")
    public synchronized void startScan() {
        if(!checkBLPermissions()) {
            return;
        }

        ScanSettings settings = new ScanSettings
                .Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_POWER)
                .build();

        ArrayList<ScanFilter> filters = new ArrayList();

        ScanFilter filter = new ScanFilter
                .Builder()
                .setServiceUuid(ParcelUuid
                        .fromString("b19fbebe-dbd4-11ed-afa1-0242ac120002"))
                .build();

        filters.add(filter);

        if(scanning) {
            bluetoothLeScanner.stopScan(scanCallback);
        }

        long currentTime = System.currentTimeMillis();

        if((currentTime - lastStartScanTime) >= 6000) {
            bluetoothLeScanner.startScan(filters, settings, scanCallback);
            scanning = true;
            lastStartScanTime = System.currentTimeMillis();
        } else {
            // To ensure that scans are are started at most every 6s
            // scan only after enough time has passed since last scan
            // was started

            TimerTask task = new TimerTask() {
                @SuppressLint("MissingPermission")
                public void run() {
                    startScan();
                }
            };

            // Clear any scheduled tasks on the timer
            timer.cancel();
            timer = new Timer("ScanTimer");

            try {
                timer.schedule(task, 6000 - (currentTime - lastStartScanTime));
            } catch (IllegalStateException e) {
                Log.e(TAG, "Trying to schedule task on cancelled timer");
            }

            return;
        }

        bluetoothLeScanner.startScan(filters, settings, scanCallback);
        scanning = true;

        Log.d(TAG, "Scanning for BLE devices...");

        TimerTask task = new TimerTask() {
            @SuppressLint("MissingPermission")
            public void run() {
                pauseScan(scanPeriod);
            }
        };

        // Clear any scheduled tasks on the timer
        timer.cancel();
        timer = new Timer("ScanTimer");

        try {
            timer.schedule(task, scanPeriod);
        } catch (IllegalStateException e) {
            Log.e(TAG, "Trying to schedule task on cancelled timer");
        }

    }

    @SuppressLint("MissingPermission")
    public void pauseScan(long pausePeriod) {
        Log.d(TAG, "Bluetooth scan pausing...");

        if(!checkBLPermissions()) {
            return;
        }

        bluetoothLeScanner.stopScan(this.scanCallback);
        scanning = false;

        TimerTask task = new TimerTask() {
            @SuppressLint("MissingPermission")
            public void run() {
                startScan();
            }
        };

        // Clear any scheduled tasks on the timer
        timer.cancel();
        timer = new Timer("ScanTimer");

        try {
            timer.schedule(task, pausePeriod);
        } catch (IllegalStateException e) {
            Log.e(TAG, "Trying to schedule task on cancelled timer");
        }

    }

    @SuppressLint("MissingPermission")
    public void stopScan() {
        Log.d(TAG, "Bluetooth scan stopping...");

        if(!checkBLPermissions()) {
            return;
        }

        if(scanning) {
            bluetoothLeScanner.stopScan(scanCallback);
            scanning = false;
        }

        // Clear any scheduled tasks on the timer
        timer.cancel();
        timer = new Timer("ScanTimer");
    }

    private boolean checkBLPermissions() {
        if (Build.VERSION.SDK_INT >= 31
                && ActivityCompat.checkSelfPermission(this.context, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
            Log.e(TAG, "Bluetooth scan permissions not granted");
            return false;
        } else if (Build.VERSION.SDK_INT < 31
                && ActivityCompat.checkSelfPermission(this.context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.e(TAG, "Fine location permissions not granted");
            return false;
        }

        return true;
    }
}
