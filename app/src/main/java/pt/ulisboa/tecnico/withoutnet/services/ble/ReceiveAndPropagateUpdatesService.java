package pt.ulisboa.tecnico.withoutnet.services.ble;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
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
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;

import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeSet;
import java.util.UUID;

import pt.ulisboa.tecnico.withoutnet.R;
import pt.ulisboa.tecnico.withoutnet.constants.BleGattIDs;
import pt.ulisboa.tecnico.withoutnet.models.Message;
import pt.ulisboa.tecnico.withoutnet.utils.ble.BleScanner;
import pt.ulisboa.tecnico.withoutnet.GlobalClass;
import pt.ulisboa.tecnico.withoutnet.models.Node;
import pt.ulisboa.tecnico.withoutnet.models.Update;

public class ReceiveAndPropagateUpdatesService extends Service {
    private static final String TAG = "ReceiveAndPropagateUpdatesService";

    private static final long SCAN_PERIOD = 10000;

    private static final long CONNECTION_INTERVAL = 1000;

    private LocalBinder binder = new LocalBinder();

    private BleScanner scanner;

    private BleService bleService;

    private GlobalClass globalClass;

    private boolean connected = false;

    private HashMap<String, Long> lastConnectionTimesByAddress = new HashMap<>();

    private ScanCallback scanCallback  = new ScanCallback() {
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

            // Stop scanning while a connection is ongoing
            //ReceiveAndPropagateUpdatesService.this.scanner.stopScanning();

            String address = device.getAddress();

            Log.d(TAG, "Found ble device with address: " + address);

            if(enoughTimeHasPassedSinceLastConnection(address)) {
                // Connect to node
                final boolean connectResult = bleService.connect(address);
                Log.d(TAG, "Connect request result = " + connectResult);

                ReceiveAndPropagateUpdatesService.this.lastConnectionTimesByAddress.put(address, System.currentTimeMillis());
            } else {
                Log.d(TAG, "Can't connect to a device because not enough time between connections has passed");
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

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            bleService = ((BleService.LocalBinder) service).getService();

            if (bleService != null) {
                if (!bleService.initialize()) {
                    Log.e(TAG, "Unable to initialize Bluetooth");
                    return;
                }

                ReceiveAndPropagateUpdatesService.this.scanner.scan();

                Log.d(TAG, "bleService successfully initialized.");

            }
            //Log.d(TAG, "bleService is null.");
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            bleService = null;
            //ReceiveAndPropagateUpdatesService.this.scanner.stopScanning();
        }
    };

    private final BroadcastReceiver gattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            if (BleService.ACTION_GATT_CONNECTED.equals(action)) {
                // Stop scanning while a connection is ongoing
                // ReceiveAndPropagateUpdatesService.this.scanner.stopScanning();

                connected = true;
                Log.d(TAG, "Connected to node");

            } else if (BleService.ACTION_GATT_DISCONNECTED.equals(action)) {
                // Resume scanning after a connection is closed
                //ReceiveAndPropagateUpdatesService.this.scanner.scan();

                connected = false;
                Log.d(TAG, "Disconnected from node");
            } else if (BleService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                Log.d(TAG, "GATT Services discovered");

                List<BluetoothGattService> gattServiceList = bleService.getSupportedGattServices();

                BluetoothGattCharacteristic nodeUuidCharacteristic = null;
                BluetoothGattCharacteristic outgoingMessageCharacteristic = null;
                BluetoothGattCharacteristic incomingMessageCharacteristic = null;

                for (BluetoothGattService service : gattServiceList) {
                    //Log.d(TAG, "GATT Service: " + service.getUuid().toString());
                    if (service.getUuid().toString().equals("b19fbebe-dbd4-11ed-afa1-0242ac120002")) {
                        List<BluetoothGattCharacteristic> updateCharacteristics = service.getCharacteristics();
                        for (BluetoothGattCharacteristic characteristic : updateCharacteristics) {
                            //Log.d(TAG, "GATT Read Characteristic: " + characteristic.getUuid());
                        }

                        nodeUuidCharacteristic = service
                                .getCharacteristic(UUID.fromString(BleGattIDs.NODE_UUID_CHARACTERISTIC_ID));

                        outgoingMessageCharacteristic = service
                                .getCharacteristic(UUID.fromString(BleGattIDs.OUTGOING_MESSAGE_CHARACTERISTIC_ID));

                        incomingMessageCharacteristic = service
                                .getCharacteristic(UUID.fromString(BleGattIDs.INCOMING_MESSAGE_CHARACTERISTIC_ID));
                    }
                }

                // Step 1: Read the node's uuid
                // Step 2: Write the messages meant for this node to the respective characteristic
                // Step 3: Read the node's pending messages

                if(nodeUuidCharacteristic!= null
                        && incomingMessageCharacteristic != null
                        && outgoingMessageCharacteristic != null) {
                    bleService.setIncomingMessageCharacteristic(incomingMessageCharacteristic);
                    bleService.setOutgoingMessageCharacteristic(outgoingMessageCharacteristic);
                    bleService.readCharacteristic(nodeUuidCharacteristic);
                    bleService.readCharacteristic(outgoingMessageCharacteristic);
                } else {
                    // Error
                }

                /*if(readCharacteristic != null) {
                    //Log.d(TAG, "Read characteristic is not null");
                    bleService.readCharacteristic(readCharacteristic);
                }

                if(writeCharacteristic != null) {
                    // Discover which sensors the actuator is interested in, and then write the most recent
                    // cached update from that sensor
                    //Log.d(TAG, "Write characteristic is not null");

                    if(interestedSensorCharacteristic != null) {
                        bleService.setWriteUpdateCharacteristic(writeCharacteristic);
                        bleService.readCharacteristic(interestedSensorCharacteristic);
                    } else {
                        // TODO: Throw exception
                    }

                    //bleService.writeCharacteristic(writeCharacteristic);
                }*/

            } else if (BleService.ACTION_CHARACTERISTIC_READ.equals(action)) {
                Log.d(TAG, "Characteristic Read");

                String characteristicId = intent.getStringExtra("id");

                if(characteristicId.equals(BleGattIDs.READ_UPDATE_CHARACTERISTIC_ID)) {
                    String updateValue = intent.getStringExtra("value");
                    Log.d(TAG, "Characteristic value:" + updateValue);

                    Update update = new Update(updateValue);

                    globalClass.addUpdate(update);

                    if(updateValue.equals("")) {
                        // Disconnect from node
                        final boolean result = bleService.disconnect();
                        Log.d(TAG, "Disconnect request result = " + result);
                    }
                } else if(characteristicId.equals(BleGattIDs.RELEVANT_NODE_CHARACTERISTIC_ID)) {
                    String relevantNodeValue = intent.getStringExtra("value");
                    Log.d(TAG, "Characteristic value:" + relevantNodeValue);

                    Node node = new Node(relevantNodeValue);

                    Update updateToBeWritten = globalClass.getMostRecentUpdate(node);

                    if (updateToBeWritten != null) {
                        BluetoothGattCharacteristic writeUpdateCharacteristic = bleService.getWriteUpdateCharacteristic();
                        writeUpdateCharacteristic.setValue(updateToBeWritten.toString());
                        bleService.writeCharacteristic(writeUpdateCharacteristic);

                        return;
                    }
                } else if(characteristicId.equals(BleGattIDs.NODE_UUID_CHARACTERISTIC_ID)) {
                    String nodeUuid = intent.getStringExtra("value");
                    Log.d(TAG, "Characteristic value:" + nodeUuid);

                    // Write every message intended for this node
                    TreeSet<Message> messagesToBeWritten = globalClass.getAllMessagesForReceiver(nodeUuid);

                    if (messagesToBeWritten != null) {
                        BluetoothGattCharacteristic incomingMessageCharacteristic = bleService.getIncomingMessageCharacteristic();

                        // TODO: It should be checked if incomingMessageCharacteristic != null

                        for(Message message : messagesToBeWritten) {
                            incomingMessageCharacteristic.setValue(message.toString());
                            bleService.writeCharacteristic(incomingMessageCharacteristic);
                        }
                    }
                } else if(characteristicId.equals(BleGattIDs.OUTGOING_MESSAGE_CHARACTERISTIC_ID)) {
                    String messageString = intent.getStringExtra("value");

                    if(messageString.equals("")) {
                        // All pending messages have been read
                    }

                    // Add the current message to the cache
                    Message message = new Message(messageString);
                    globalClass.addMessage(message);

                    // Read the next message
                    BluetoothGattCharacteristic outgoingMessageCharacteristic = bleService.getOutgoingMessageCharacteristic();
                    bleService.readCharacteristic(outgoingMessageCharacteristic);
                } else {
                    // TODO: Throw an exception
                    Log.d(TAG, "Unknown characteristic id:" + characteristicId);
                }
            } else if (BleService.ACTION_CHARACTERISTIC_WRITTEN.equals(action)) {
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
        intentFilter.addAction(BleService.ACTION_CHARACTERISTIC_WRITTEN);
        return intentFilter;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        this.globalClass = (GlobalClass) getApplicationContext();
        this.scanner = new BleScanner(getApplicationContext(), this.scanCallback, SCAN_PERIOD);
        /*handler.postDelayed(new Runnable() {
            @SuppressLint("MissingPermission")
            @Override
            public void run() {
                Log.d("DEBUG", "Bluetooth scan stopping...\n");
                bluetoothLeScanner.stopScan(scanCallback);
                scanning = false;
            }
        }, scanPeriod);*/

        /*TimerTask task = new TimerTask() {
            @SuppressLint("MissingPermission")
            public void run() {
                ReceiveAndPropagateUpdatesService.this.scanner.stopScanning();
            }
        };

        Timer timer = new Timer("Timer");
        timer.scheduleAtFixedRate(task, SCAN_PERIOD, SCAN_PERIOD);*/
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        this.start();

        final String CHANNELID = TAG;
        NotificationChannel channel = new NotificationChannel(
                CHANNELID,
                CHANNELID,
                NotificationManager.IMPORTANCE_LOW
        );

        getSystemService(NotificationManager.class).createNotificationChannel(channel);
        Notification.Builder notification = new Notification.Builder(this, CHANNELID)
                .setContentTitle(getApplicationContext().getString(R.string.withoutnet_participation_enabled))
                .setContentText(getApplicationContext().getString(R.string.withoutnet_is_scanning_for_and_connecting_to_nodes))
                .setSmallIcon(R.drawable.ic_launcher_background);

        startForeground(123, notification.build());
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        this.stop();
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    public class LocalBinder extends Binder {
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
        this.scanner.stopScanningDefinitely();
        unregisterReceiver(gattUpdateReceiver);
        getApplicationContext().unbindService(serviceConnection);
        return true;
    }

    private boolean enoughTimeHasPassedSinceLastConnection(String address) {
        Long lastConnectionTime = this.lastConnectionTimesByAddress.get(address);

        if(lastConnectionTime == null) {
            return true;
        }

        return (System.currentTimeMillis() - lastConnectionTime) >= CONNECTION_INTERVAL;
    }
}
