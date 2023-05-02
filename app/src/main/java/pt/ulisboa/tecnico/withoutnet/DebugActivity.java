package pt.ulisboa.tecnico.withoutnet;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.ParcelUuid;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.UUID;

import pt.ulisboa.tecnico.withoutnet.databinding.ActivityDebugBinding;
import pt.ulisboa.tecnico.withoutnet.databinding.ActivityDebugNodeBinding;

public class DebugActivity extends AppCompatActivity {
    public final static int REQUEST_ENABLE_BT = 123;
    public final static int REQUEST_ENABLE_BT_SCAN = 124;
    public final static int REQUEST_ACCESS_FINE_LOCATION = 125;

    private ActivityResultLauncher<String> requestPermissionLauncher;

    private BluetoothManager bluetoothManager;
    private BluetoothAdapter bluetoothAdapter;

    private BluetoothLeScanner bluetoothLeScanner;

    private Handler handler = new Handler();

    // Stops scanning after 100 seconds.
    private static final long SCAN_PERIOD = 10000;

    private boolean scanning = false;

    private int connectionState = BluetoothProfile.STATE_DISCONNECTED;

    private LinkedHashMap<String, Node> nearbyNodesByName = new LinkedHashMap<String, Node>();

    private BluetoothGatt BLEGatt;

    private NearbyNodesAdapter.OnNearbyNodeListener onNearbyNodeListener = new NearbyNodesAdapter.OnNearbyNodeListener() {
        @SuppressLint("MissingPermission")
        @Override
        public void onNearbyNodeClick(int position) {
            Log.d("DEBUG", "Clicked on node " + position + "\n");
            //Toast.makeText(DebugActivity.this, "Click!", Toast.LENGTH_SHORT).show();
            Node clickedNode = new ArrayList<>(nearbyNodesByName.values()).get(position);
            // TODO: Does it make sense to have a node's id being its physical address?
            //BluetoothDevice BLEDevice = clickedNode.getBLDevice();
            Intent intent = new Intent(DebugActivity.this, DebugNodeActivity.class);
            Log.d("DEBUG", "Address: " + clickedNode.getId());
            intent.putExtra("Address", clickedNode.getId());
            startActivity(intent);
            /*if(connectionState == BluetoothProfile.STATE_DISCONNECTED) {
                DebugActivity.this.BLEGatt = BLEDevice.connectGatt(DebugActivity.this, false, bluetoothGattCallback);
                BLEGatt.readCharacteristic
                        (new BluetoothGattCharacteristic
                                (UUID.fromString("beb5483e-36e1-4688-b7f5-ea07361b26a8"),
                                        BluetoothGattCharacteristic.PROPERTY_READ,
                                        BluetoothGattCharacteristic.PERMISSION_READ));
            } else {
                DebugActivity.this.BLEGatt.close();
            }*/
        }
    };

    private NearbyNodesAdapter nearbyNodesAdapter = new NearbyNodesAdapter(nearbyNodesByName, onNearbyNodeListener);

    // Device scan callback.
    private ScanCallback leScanCallback;

    // private BluetoothGattCallback bluetoothGattCallback;

    private RecyclerView rvNearbyNodes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivityDebugBinding binding = ActivityDebugBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        bluetoothManager = getSystemService(BluetoothManager.class);
        bluetoothAdapter = bluetoothManager.getAdapter();
        bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();

        leScanCallback =
                new ScanCallback() {
                    @Override
                    public void onScanResult(int callbackType, ScanResult result) {
                        super.onScanResult(callbackType, result);

                        //Log.d("DEBUG", "Found BLE device\n");

                        BluetoothDevice device = result.getDevice();

                        if (Build.VERSION.SDK_INT >= 31
                                && ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                            // TODO: Consider calling
                            //    ActivityCompat#requestPermissions
                            return;
                        }

                       /*Log.d("DEBUG", "BLE device services UUIDs: \n");
                        List<ParcelUuid> serviceUUIDs = result.getScanRecord().getServiceUuids();

                        for(ParcelUuid serviceUUID : serviceUUIDs) {
                            Log.d("DEBUG", "    " + serviceUUID.toString() + "\n");
                        }*/

                        Node newNode = new Node(device);

                        nearbyNodesByName.put(newNode.getCommonName(), newNode);

                        nearbyNodesAdapter.notifyDataSetChanged();
                    }
                };

        /*bluetoothGattCallback = new BluetoothGattCallback() {
            @Override
            public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    // successfully connected to the GATT Server
                    connectionState = BluetoothProfile.STATE_CONNECTED;
                    DebugActivity.this.runOnUiThread(() -> {
                        Toast.makeText(DebugActivity.this, "Connected to node", Toast.LENGTH_SHORT).show();
                    });
                } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                    // disconnected from the GATT Server
                    connectionState = BluetoothProfile.STATE_DISCONNECTED;
                    DebugActivity.this.runOnUiThread(() -> {
                            Toast.makeText(DebugActivity.this, "Disconnected from node", Toast.LENGTH_SHORT).show();
                    });
                }
            }

            @Override
            public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
                super.onCharacteristicRead(gatt, characteristic, status);
                Log.d("DEBUG", characteristic.getStringValue(0));
                DebugActivity.this.runOnUiThread(() -> {
                    Toast.makeText(DebugActivity.this, "Read characteristic", Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
                super.onCharacteristicWrite(gatt, characteristic, status);
            }
        };*/

        rvNearbyNodes = binding.nearbyNodes;
        rvNearbyNodes.setAdapter(nearbyNodesAdapter);

        rvNearbyNodes.setLayoutManager(new LinearLayoutManager(this));

        rvNearbyNodes.addItemDecoration(new DividerItemDecoration(this,
                DividerItemDecoration.VERTICAL));

        binding.scanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                scanForBLEDevices();
            }
        });

        Toast.makeText(getApplicationContext(), "Debug activity created", Toast.LENGTH_LONG);

        Log.d("DEBUG", "Activity started\n");

        Toast.makeText(getApplicationContext(), "Debug activity created 2", Toast.LENGTH_SHORT);

        if (bluetoothAdapter == null) {
            // Device doesn't support Bluetooth
            Toast.makeText(this, "Bluetooth not supported", Toast.LENGTH_SHORT);
            Log.d("DEBUG", "Bluetooth not supported\n");
        }

        if (!bluetoothAdapter.isEnabled()) {
            Toast.makeText(this, "Bluetooth not turned on", Toast.LENGTH_SHORT);
            Log.d("DEBUG", "Bluetooth not turned on\n");

            if (Build.VERSION.SDK_INT >= 31 && ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED) {
                Log.d("DEBUG", "Bluetooth permissions not yet granted\n");

                requestPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                    if (isGranted) {
                        // Permission is granted. Continue the action or workflow in your
                        // app.

                        Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                        startActivityForResult(intent, REQUEST_ENABLE_BT);
                        Log.d("DEBUG", "Bluetooth permissions not yet granted\n");
                    } else {
                        // Permission not granted.
                        Toast.makeText(this.getApplicationContext(), "BT permissions not granted", Toast.LENGTH_SHORT);
                        Log.d("DEBUG", "Bluetooth permissions not granted\n");
                    }
                });

                requestPermissionLauncher.launch(Manifest.permission.BLUETOOTH_CONNECT);

                return;
            }

            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(intent, REQUEST_ENABLE_BT);
        }

        Toast.makeText(this, "Bluetooth turned on", Toast.LENGTH_SHORT);
        Log.d("DEBUG", "Bluetooth turned on\n");

        scanForBLEDevices();

        /*if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            ActivityCompat.requestPermissions(this, new String[] { Manifest.permission.BLUETOOTH_CONNECT }, REQUEST_ENABLE_BT_SCAN);
            return;
        }*/

    }

    /*private void scanForBLEDevicesRecurr() {
        if (!scanning) {
            // Stops scanning after a predefined scan period.
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    scanning = false;
                    bluetoothLeScanner.stopScan(leScanCallback);
                }
            }, SCAN_PERIOD);

            scanning = true;
            bluetoothLeScanner.startScan(leScanCallback);
        } else {
            scanning = false;
            bluetoothLeScanner.stopScan(leScanCallback);
        }
    }*/

    private void scanForBLEDevices() {
        if (Build.VERSION.SDK_INT >= 31
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }

        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{ Manifest.permission.ACCESS_FINE_LOCATION }, REQUEST_ACCESS_FINE_LOCATION);
            return;
        }

        ScanSettings settings = new ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).build();
        ArrayList<ScanFilter> filters = new ArrayList();
        ScanFilter filter = new ScanFilter
                .Builder()
                .setServiceUuid(ParcelUuid
                        .fromString("b19fbebe-dbd4-11ed-afa1-0242ac120002"))
                .build();

        ScanFilter filter2 = new ScanFilter
                .Builder()
                .setDeviceName("MyESP32")
                .build();

        filters.add(filter);

        if(scanning) {
            bluetoothLeScanner.stopScan(leScanCallback);
        }

        bluetoothLeScanner.startScan(filters, settings, leScanCallback);
        //bluetoothLeScanner.startScan(leScanCallback);
        Log.d("DEBUG", "Scanning for BLE devices...\n");
        scanning = true;

        handler.postDelayed(new Runnable() {
            @SuppressLint("MissingPermission")
            @Override
            public void run() {
                Log.d("DEBUG", "Bluetooth scan stopping...\n");
                bluetoothLeScanner.stopScan(leScanCallback);
                scanning = false;
            }
        }, SCAN_PERIOD);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case REQUEST_ENABLE_BT:
                if (grantResults.length > 0 &&
                        grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission is granted. Continue the action or workflow
                    // in your app.
                    Log.d("DEBUG", "Bluetooth permissions granted\n");

                }  else {
                    // Explain to the user that the feature is unavailable because
                    // the feature requires a permission that the user has denied.
                    // At the same time, respect the user's decision. Don't link to
                    // system settings in an effort to convince the user to change
                    // their decision.
                    Log.d("DEBUG", "Bluetooth permissions not granted\n");
                }
                return;
            case REQUEST_ENABLE_BT_SCAN:
                return;
            case REQUEST_ACCESS_FINE_LOCATION:
                if (grantResults.length > 0 &&
                        grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission is granted. Continue the action or workflow
                    // in your app.
                    Log.d("DEBUG", "Access fine location permissions granted\n");
                    scanForBLEDevices();
                }  else {
                    // Explain to the user that the feature is unavailable because
                    // the feature requires a permission that the user has denied.
                    // At the same time, respect the user's decision. Don't link to
                    // system settings in an effort to convince the user to change
                    // their decision.
                    Log.d("DEBUG", "Access fine location permissions not granted\n");
                }
                return;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQUEST_ENABLE_BT:

        }
    }
}