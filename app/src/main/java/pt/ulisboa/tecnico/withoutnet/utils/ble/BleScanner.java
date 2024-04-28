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

    private Context context;
    private BluetoothLeScanner bluetoothLeScanner;
    private boolean scanning;
    private ScanCallback scanCallback;
    private long scanPeriod;

    private Timer timer;

    public BleScanner(Context context, ScanCallback scanCallback, long scanPeriod) {
        this.context = context;
        this.scanCallback = scanCallback;
        this.scanning = false;
        this.scanPeriod = scanPeriod;

        BluetoothManager bluetoothManager = this.context.getSystemService(BluetoothManager.class);
        BluetoothAdapter bluetoothAdapter = bluetoothManager.getAdapter();
        bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();

        timer = new Timer("Timer");
    }

    // TODO: Is scan required to be synchronized?
    @SuppressLint("MissingPermission")
    public synchronized void scan() {
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
            synchronized (this) {
                this.notifyAll();
            }
        }

        bluetoothLeScanner.startScan(filters, settings, scanCallback);
        Log.d(TAG, "Scanning for BLE devices...\n");
        scanning = true;

        TimerTask task = new TimerTask() {
            @SuppressLint("MissingPermission")
            public void run() {
                BleScanner.this.stopScanning();
            }
        };

        //Timer timer = new Timer("Timer");

        try {
            timer.schedule(task, this.scanPeriod);
        } catch (IllegalStateException e) {
            Log.e(TAG, "Trying to schedule task on cancelled timer");
        }

    }

    @SuppressLint("MissingPermission")
    public void stopScanning() {
        Log.d(TAG, "Bluetooth scan stopping (manually)...\n");

        if(!checkBLPermissions()) {
            return;
        }

        bluetoothLeScanner.stopScan(this.scanCallback);
        scanning = false;

        TimerTask task = new TimerTask() {
            @SuppressLint("MissingPermission")
            public void run() {
                BleScanner.this.scan();
            }
        };

        //Timer timer = new Timer("Timer");

        try {
            timer.schedule(task, this.scanPeriod);
        } catch (IllegalStateException e) {
            Log.e(TAG, "Trying to schedule task on cancelled timer");
        }

    }

    @SuppressLint("MissingPermission")
    public void stopScanningDefinitely() {
        Log.d(TAG, "Bluetooth scan stopping (manually)...\n");

        if(!checkBLPermissions()) {
            return;
        }

        bluetoothLeScanner.stopScan(this.scanCallback);
        scanning = false;

        timer.cancel();

        timer = new Timer("Timer");
        //timer.purge();
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
