package pt.ulisboa.tecnico.withoutnet.utils.ble;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.ParcelUuid;
import android.util.Log;

import androidx.core.app.ActivityCompat;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;

import pt.ulisboa.tecnico.withoutnet.GlobalClass;
import pt.ulisboa.tecnico.withoutnet.services.ble.ReceiveAndPropagateUpdatesService;

public class BleScanner {
    private static final String TAG = "BleScanner";

    // This is the time that the smartphone should
    // be listening for advertisement packages
    // i.e. it is different from the node scanning
    // interval which sets the min time between
    // scans
    private static final long SCAN_PERIOD = 2000;

    private GlobalClass globalClass;

    private Context context;
    private BluetoothLeScanner bluetoothLeScanner;
    private boolean scanning;
    private ScanCallback scanCallback;
    private OnScanEventListener onScanEventListener;
    private long scanPeriod;

    private long lastStopScanTime;

    private Timer timer;

    private Queue<String> addressQueue;

    private boolean connectionOngoing;

    public BleScanner(Context context, OnScanEventListener onScanEventListener, long scanPeriod) {
        this.context = context;
        this.globalClass = (GlobalClass) context.getApplicationContext();
        this.scanning = false;
        this.scanPeriod = scanPeriod;
        this.addressQueue = new LinkedList<>();
        this.onScanEventListener = onScanEventListener;
        this.connectionOngoing = false;

        this.scanCallback = new ScanCallback() {
            @Override
            public void onScanResult(int callbackType, ScanResult result) {
                super.onScanResult(callbackType, result);

                BluetoothDevice device = result.getDevice();

                if (Build.VERSION.SDK_INT >= 31
                        && ActivityCompat.checkSelfPermission(context.getApplicationContext(), Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                    Log.e(TAG, "Bluetooth permissions not granted");
                }

                String address = device.getAddress();

                if(!addressQueue.contains(address)) {
                    Log.d(TAG, "Found new ble device with address: " + address);
                    addressQueue.add(address);
                }
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

        BluetoothManager bluetoothManager = this.context.getSystemService(BluetoothManager.class);
        BluetoothAdapter bluetoothAdapter = bluetoothManager.getAdapter();
        bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();

        timer = new Timer("ScanTimer");
    }

    // TODO: Is scan required to be synchronized?
    @SuppressLint("MissingPermission")
    public synchronized void startScan() {
        if(connectionOngoing) {
            return;
        }

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

        if((currentTime - lastStopScanTime) < globalClass.getNodeScanningInterval()) {
            // To ensure that scans are are started only after
            // enough time has passed since the last scan was stopped

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
                timer.schedule(task, globalClass.getNodeScanningInterval() - (currentTime - lastStopScanTime));
            } catch (IllegalStateException e) {
                Log.e(TAG, "Trying to schedule task on cancelled timer");
            }

            return;
        }

        bluetoothLeScanner.startScan(filters, settings, scanCallback);
        scanning = true;
        addressQueue = new LinkedList<>();

        Log.d(TAG, "Scanning for BLE devices...");

        TimerTask task = new TimerTask() {
            @SuppressLint("MissingPermission")
            public void run() {
                pauseScan(globalClass.getNodeScanningInterval());
            }
        };

        // Clear any scheduled tasks on the timer
        timer.cancel();
        timer = new Timer("ScanTimer");

        try {
            timer.schedule(task, SCAN_PERIOD);
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

        if(scanning) {
            bluetoothLeScanner.stopScan(this.scanCallback);
            scanning = false;
            lastStopScanTime = System.currentTimeMillis();
        }

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

        onScanEventListener.onScanComplete(addressQueue);
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
            lastStopScanTime = System.currentTimeMillis();
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

    public void setConnectionOngoing(boolean connectionOngoing) {
        this.connectionOngoing = connectionOngoing;
    }

    public interface OnScanEventListener {
        void onScanComplete(Queue<String> addressQueue);
    }
}
