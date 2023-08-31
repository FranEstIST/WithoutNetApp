package pt.ulisboa.tecnico.withoutnet.services.ble;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;

import pt.ulisboa.tecnico.withoutnet.utils.ble.BleScanner;

public class BleService extends Service {
    // Connections can last at most 10s
    private static final long CONNECTION_TIMEOUT = 10000;

    private Binder binder = new LocalBinder();
    private BluetoothAdapter bluetoothAdapter;
    public static final String TAG = "BleService";
    private BluetoothGatt bluetoothGatt;

    public final static String ACTION_GATT_CONNECTED =
            "com.example.bluetooth.le.ACTION_GATT_CONNECTED";
    public final static String ACTION_GATT_DISCONNECTED =
            "com.example.bluetooth.le.ACTION_GATT_DISCONNECTED";
    public final static String ACTION_GATT_SERVICES_DISCOVERED =
            "com.example.bluetooth.le.ACTION_GATT_SERVICES_DISCOVERED";
    public final static String ACTION_CHARACTERISTIC_READ =
            "com.example.bluetooth.le.ACTION_CHARACTERISTIC_READ";
    public final static String ACTION_CHARACTERISTIC_WRITTEN =
            "com.example.bluetooth.le.ACTION_CHARACTERISTIC_WRITTEN";

    private static final int STATE_DISCONNECTED = 0;
    private static final int STATE_CONNECTED = 2;
    private static final int STATE_CHAR_READ = 3;
    private static final int STATE_CHAR_WRITTEN = 4;

    private int connectionState;

    private boolean hasAttemptedToConnect = false;

    private String currentConnectionAddress = null;

    // TODO: There might be a better way to store these variables
    private BluetoothGattCharacteristic writeUpdateCharacteristic = null;
    private BluetoothGattCharacteristic incomingMessageCharacteristic = null;
    private BluetoothGattCharacteristic outgoingMessageCharacteristic = null;

    private Queue<String> addressQueue = new LinkedList<>();

    Timer connectionTimeoutTimer = new Timer("connectionTimeoutTimer");

    private int currentNodeUuid = -1;

    private boolean mtuSet = false;

    private boolean firstWrite = true;

    private Runnable ongoingOperation;

    private final BluetoothGattCallback bluetoothGattCallback = new BluetoothGattCallback() {
        // TODO: Check if the necessary permissions have been granted
        @SuppressLint("MissingPermission")
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                // successfully connected to the GATT Server
                connectionState = STATE_CONNECTED;
                Log.d(TAG, "Connected to node");
                broadcastUpdate(ACTION_GATT_CONNECTED);

                if(!checkBLPermissions()) {
                    return;
                }

                bluetoothGatt.discoverServices();
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                // disconnected from the GATT Server
                connectionState = STATE_DISCONNECTED;
                Log.d(TAG, "Disconnected from node");
                broadcastUpdate(ACTION_GATT_DISCONNECTED);

                //BleService.this.close();

                //BleService.this.hasAttemptedToConnect = false;

                String nextAddress = addressQueue.poll();

                if (nextAddress != null) {
                    BleService.this.connect(nextAddress);
                }
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.w(TAG, "onServicesDiscovered received: " + status);
                broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED);
            } else {
                Log.w(TAG, "onServicesDiscovered received: " + status);
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            // TODO: Is this call to the superclass necessary?
            super.onCharacteristicRead(gatt, characteristic, status);
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.w(TAG, "onCharacteristicRead received: " + status);
                broadcastUpdate(ACTION_CHARACTERISTIC_READ, characteristic);
            } else {
                Log.w(TAG, "onCharacteristicRead received: " + status);
            }
        }

        //TODO: Implement this
        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicWrite(gatt, characteristic, status);
            Log.w(TAG, "onCharacteristicWrite received: " + status);
            if (status == BluetoothGatt.GATT_SUCCESS) {
                broadcastUpdate(ACTION_CHARACTERISTIC_WRITTEN, characteristic);
            } else {
                // TODO
            }

        }

        @Override
        public void onMtuChanged(BluetoothGatt gatt, int mtu, int status) {
            super.onMtuChanged(gatt, mtu, status);

            Log.d(TAG, "MTU set to " + mtu);

            mtuSet = true;
            ongoingOperation.run();
        }

    };

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    public class LocalBinder extends Binder {
        public BleService getService() {
            return BleService.this;
        }
    }

    public boolean initialize() {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            Log.e(TAG, "Unable to obtain a BluetoothAdapter.");
            return false;
        }
        Log.d(TAG, "Initialized ble service.");
        return true;
    }

    /*private boolean isConnected(final String address) {
        bluetoothAdapter.getBondedDevices()
    }*/

    // TODO: Check if permission is granted
    @SuppressLint("MissingPermission")
    public boolean connect(final String address) {
        if (bluetoothAdapter == null || address == null) {
            Log.w(TAG, "BluetoothAdapter not initialized or unspecified address.");
            return false;
        }

        /*if(connectionState == STATE_CONNECTED) {
            Log.w(TAG, "Already connected to a node.");
            addressQueue.add(address);
            return false;
        }*/

        if (hasAttemptedToConnect) {
            // TODO: See if this can be fixed (i.e. if there is a way to stop too many reconnection attempts from happening)
            //addressQueue.add(address);
            return false;
        }

        hasAttemptedToConnect = true;

        TimerTask task = new TimerTask() {
            public void run() {
                BleService.this.disconnect();
            }
        };

        this.connectionTimeoutTimer = new Timer("connectionTimeoutTimer");
        this.connectionTimeoutTimer.schedule(task, CONNECTION_TIMEOUT);

        try {
            final BluetoothDevice device = bluetoothAdapter.getRemoteDevice(address);
            // connect to the GATT server on the device
            if(!checkBLPermissions()) {
                return false;
            }
            bluetoothGatt = device.connectGatt(this, false, bluetoothGattCallback);
            return true;
        } catch (IllegalArgumentException exception) {
            Log.w(TAG, "Device not found with provided address.  Unable to connect.");
            return false;
        }
    }

    // TODO: Should this method really be synchronized?
    @SuppressLint("MissingPermission")
    public synchronized boolean disconnect() {
        this.hasAttemptedToConnect = false;
        this.connectionTimeoutTimer.cancel();

        if (bluetoothGatt == null) {
            Log.d(TAG, "BLGATT is null.");
        }

        if (connectionState != STATE_CONNECTED) {
            Log.d(TAG, "Can't close connection because device is not connected.");
        }

        if (bluetoothGatt == null || connectionState != STATE_CONNECTED) {
            return false;
        }

        if(!checkBLPermissions()) {
            return false;
        }

        bluetoothGatt.disconnect();
        bluetoothGatt.close();

        mtuSet = false;
        firstWrite = true;

        return true;
    }

    public List<BluetoothGattService> getSupportedGattServices() {
        if (bluetoothGatt == null) return null;
        return bluetoothGatt.getServices();
    }

    // TODO: Check if user has granted the necessary permissions
    @SuppressLint("MissingPermission")
    public void readCharacteristic(BluetoothGattCharacteristic characteristic) {
        if(!checkBLPermissions()) {
            return;
        }

        if (bluetoothGatt == null) {
            Log.w(TAG, "BluetoothGatt not initialized");
            return;
        }

        if (!mtuSet) {
            ongoingOperation = new Runnable() {
                @Override
                public void run() {
                    readCharacteristic(characteristic);
                }
            };

            bluetoothGatt.requestMtu(23);

            return;
        }

        bluetoothGatt.readCharacteristic(characteristic);
    }

    public BluetoothGattCharacteristic getWriteUpdateCharacteristic() {
        return writeUpdateCharacteristic;
    }

    public BluetoothGattCharacteristic getIncomingMessageCharacteristic() {
        return incomingMessageCharacteristic;
    }

    public BluetoothGattCharacteristic getOutgoingMessageCharacteristic() {
        return outgoingMessageCharacteristic;
    }

    public int getCurrentNodeUuid() {
        return currentNodeUuid;
    }

    public void setWriteUpdateCharacteristic(BluetoothGattCharacteristic writeUpdateCharacteristic) {
        this.writeUpdateCharacteristic = writeUpdateCharacteristic;
    }

    public void setIncomingMessageCharacteristic(BluetoothGattCharacteristic incomingMessageCharacteristic) {
        this.incomingMessageCharacteristic = incomingMessageCharacteristic;
    }

    public void setOutgoingMessageCharacteristic(BluetoothGattCharacteristic outgoingMessageCharacteristic) {
        this.outgoingMessageCharacteristic = outgoingMessageCharacteristic;
    }

    public void setCurrentNodeUuid(int currentNodeUuid) {
        this.currentNodeUuid = currentNodeUuid;
    }

    public void isMtuSet(boolean mtuSet) {
        this.mtuSet = mtuSet;
    }

    // TODO: Check if user has granted the necessary permissions
    @SuppressLint("MissingPermission")
    public void writeCharacteristic(BluetoothGattCharacteristic characteristic) {
        if (bluetoothGatt == null) {
            Log.w(TAG, "BluetoothGatt not initialized");
            return;
        }

        if(!checkBLPermissions()) {
            return;
        }

        if (firstWrite) {
            ongoingOperation = new Runnable() {
                @Override
                public void run() {
                    BleService.this.firstWrite = false;
                    writeCharacteristic(characteristic);
                }
            };

            bluetoothGatt.requestMtu(23);

            return;
        }

        bluetoothGatt.writeCharacteristic(characteristic);
    }

    private void broadcastUpdate(final String action) {
        final Intent intent = new Intent(action);
        sendBroadcast(intent);
    }

    private void broadcastUpdate(final String action, final BluetoothGattCharacteristic characteristic) {
        final Intent intent = new Intent(action);
        intent.putExtra("id", characteristic.getUuid().toString());
        intent.putExtra("byte-array-value", characteristic.getValue());
        intent.putExtra("int-value", characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_SINT16, 0));
        sendBroadcast(intent);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        close();
        return super.onUnbind(intent);
    }

    // TODO: Check if permission is granted
    @SuppressLint("MissingPermission")
    private void close() {
        this.connectionTimeoutTimer.cancel();

        if (bluetoothGatt == null) {
            return;
        }

        if(!checkBLPermissions()) {
            return;
        }

        bluetoothGatt.close();
        bluetoothGatt = null;
    }

    private boolean checkBLPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            Log.e(TAG, "Bluetooth connect permissions not granted");
            return false;
        } else if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.e(TAG, "Access fine location permissions not granted");
            return false;
        }

        return true;
    }
}
