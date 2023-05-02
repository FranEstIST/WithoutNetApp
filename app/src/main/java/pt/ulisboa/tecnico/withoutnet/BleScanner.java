package pt.ulisboa.tecnico.withoutnet;

import static pt.ulisboa.tecnico.withoutnet.RequestCodes.REQUEST_ACCESS_FINE_LOCATION;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.ParcelUuid;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Timer;
import java.util.TimerTask;

import pt.ulisboa.tecnico.withoutnet.databinding.ActivityDebugBinding;

public class BleScanner {
    private Context context;
    private BluetoothLeScanner bluetoothLeScanner;
    private boolean scanning;
    private ScanCallback scanCallback;
    //private Handler handler;

    public BleScanner(Context context) {
        this.context = context;
        this.scanning = false;

        BluetoothManager bluetoothManager = this.context.getSystemService(BluetoothManager.class);
        BluetoothAdapter bluetoothAdapter = bluetoothManager.getAdapter();
        bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();
        //this.handler = new Handler();
    }

    // TODO: Is scan required to be synchronized?
    public synchronized void scan(long scanPeriod, ScanCallback scanCallback) {
        if (Build.VERSION.SDK_INT >= 31
                && ActivityCompat.checkSelfPermission(this.context, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }

        if(ActivityCompat.checkSelfPermission(this.context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Move these permission checks to the activity where the scan is carried out
            //ActivityCompat.requestPermissions(activity, new String[]{ Manifest.permission.ACCESS_FINE_LOCATION }, REQUEST_ACCESS_FINE_LOCATION);
            return;
        }

        this.scanCallback = scanCallback;

        ScanSettings settings = new ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).build();
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
        //bluetoothLeScanner.startScan(leScanCallback);
        Log.d("DEBUG", "Scanning for BLE devices...\n");
        scanning = true;

        /*handler.postDelayed(new Runnable() {
            @SuppressLint("MissingPermission")
            @Override
            public void run() {
                Log.d("DEBUG", "Bluetooth scan stopping...\n");
                bluetoothLeScanner.stopScan(scanCallback);
                scanning = false;
            }
        }, scanPeriod);*/

        TimerTask task = new TimerTask() {
            @SuppressLint("MissingPermission")
            public void run() {
                Log.d("DEBUG", "Bluetooth scan stopping...\n");
                bluetoothLeScanner.stopScan(scanCallback);
                scanning = false;
            }
        };

        /*Timer timer = new Timer("Timer");
        timer.schedule(task, scanPeriod);*/

    }

    @SuppressLint("MissingPermission")
    public void stopScanning() {
        Log.d("DEBUG", "Bluetooth scan stopping (manually)...\n");
        bluetoothLeScanner.stopScan(this.scanCallback);
        scanning = false;
    }
}
