package pt.ulisboa.tecnico.withoutnet.services.ble;

import static pt.ulisboa.tecnico.withoutnet.constants.Responses.EMPTY_BYTE_ARRAY;

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
import android.database.sqlite.SQLiteConstraintException;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeSet;
import java.util.UUID;
import java.util.stream.Collectors;

import io.reactivex.rxjava3.core.Scheduler;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.functions.Action;
import io.reactivex.rxjava3.schedulers.Schedulers;
import pt.ulisboa.tecnico.withoutnet.Frontend;
import pt.ulisboa.tecnico.withoutnet.R;
import pt.ulisboa.tecnico.withoutnet.constants.BleGattIDs;
import pt.ulisboa.tecnico.withoutnet.constants.Responses;
import pt.ulisboa.tecnico.withoutnet.constants.StatusCodes;
import pt.ulisboa.tecnico.withoutnet.db.WithoutNetAppDatabase;
import pt.ulisboa.tecnico.withoutnet.models.Message;
import pt.ulisboa.tecnico.withoutnet.models.MessageType;
import pt.ulisboa.tecnico.withoutnet.utils.ble.BleScanner;
import pt.ulisboa.tecnico.withoutnet.GlobalClass;
import pt.ulisboa.tecnico.withoutnet.models.Node;
import pt.ulisboa.tecnico.withoutnet.models.Update;

public class ReceiveAndPropagateUpdatesService extends Service {
    private static final String TAG = "ReceiveAndPropagateUpdatesService";

    private static final long SCAN_PERIOD = 10000;

    private static final long MIN_CONNECTION_INTERVAL = 1000;

    private LocalBinder binder = new LocalBinder();

    private BleScanner scanner;

    private BleService bleService;

    private GlobalClass globalClass;

    private boolean connected = false;

    private HashMap<String, Long> lastConnectionTimesByAddress = new HashMap<>();

    private Queue<String> connectAddressQueue = new LinkedList<>();

    private boolean allOutgoingMessagesRead = false;

    private boolean allIncomingMessagesWritten = false;

    private ArrayList<byte[]> currentOutgoingMessageChunks = new ArrayList<>();

    private ArrayList<byte[]> currentIncomingMessageChunks = new ArrayList<>();

    private Message messageToBeWritten = null;

    private List<Message> messagesToBeWritten = new ArrayList<>();
    private List<Message> messagesToBeDeleted = new ArrayList<>();

    private final CompositeDisposable disposable = new CompositeDisposable();

    /*private ScanCallback scanCallback = new ScanCallback() {
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

            if (enoughTimeHasPassedSinceLastConnection(address)) {
                // Connect to node
                final boolean connectResult = bleService.connect(address);
                Log.d(TAG, "Connect request result = " + connectResult);

                if(connectResult == true) {
                    ReceiveAndPropagateUpdatesService.this.lastConnectionTimesByAddress.put(address, System.currentTimeMillis());
                }
            } else {
                Log.d(TAG, "Can't connect to a device because not enough time between connections has passed");
            }

        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            super.onBatchScanResults(results);
            Log.d(TAG, "Batch scan results: " + results);
        }

        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);
        }
    };*/

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            bleService = ((BleService.LocalBinder) service).getService();

            if (bleService != null) {
                if (!bleService.initialize()) {
                    Log.e(TAG, "Unable to initialize Bluetooth");
                    return;
                }

                ReceiveAndPropagateUpdatesService.this.scanner.startScan();

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
                // This should not be done, because the scan is going to have to
                // be resumed after the current node is diconnected, and
                // "BLE scan may not be called more than 5 times per 30 seconds"
                //ReceiveAndPropagateUpdatesService.this.scanner.pauseScan(BleService.CONNECTION_TIMEOUT);
                //ReceiveAndPropagateUpdatesService.this.scanner.stopScan();

                // These lists, which allow for the collection of message chunks, must
                // be cleared upon a new connection, in case a previous connection
                // was interrupted while a message was being sent/received

                currentIncomingMessageChunks.clear();
                currentOutgoingMessageChunks.clear();

                connected = true;
                Log.d(TAG, "Connected to node");

            } else if (BleService.ACTION_GATT_DISCONNECTED.equals(action)) {
                // Resume scanning after a connection is closed
                //ReceiveAndPropagateUpdatesService.this.scanner.startScan();
                connected = false;
                Log.d(TAG, "Disconnected from node");

                connectToNextNodeInQueue();
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
                            Log.d(TAG, "GATT Read Characteristic: " + characteristic.getUuid());
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

                if (nodeUuidCharacteristic != null
                        && incomingMessageCharacteristic != null
                        && outgoingMessageCharacteristic != null) {
                    // TODO: Is it possible for a characteristic to be read before
                    //  the incoming and outgoing message characteristics to be set at the ble service?
                    bleService.setIncomingMessageCharacteristic(incomingMessageCharacteristic);
                    bleService.setOutgoingMessageCharacteristic(outgoingMessageCharacteristic);
                    // TODO: Why isn't this characteristic being read alongside with the one above?
                    bleService.readCharacteristic(nodeUuidCharacteristic);
                    //bleService.readCharacteristic(outgoingMessageCharacteristic);
                } else {
                    // Error
                    Log.d(TAG, "Protocol error: Expected characteristics not present in node");
                }
            } else if (BleService.ACTION_CHARACTERISTIC_READ.equals(action)) {
                Log.d(TAG, "Characteristic read");

                String characteristicId = intent.getStringExtra("id");

                if (characteristicId.equals(BleGattIDs.NODE_UUID_CHARACTERISTIC_ID)) {
                    Log.d(TAG, "Node UUID read");

                    int nodeUuid = intent.getIntExtra("int-value", -1);

                    Log.d(TAG, "Node UUID: " + nodeUuid);

                    bleService.setCurrentNodeUuid(nodeUuid);

                    // TODO: Read the node's pending messages in the database and the server here
                    globalClass.getWithoutNetAppDatabase()
                            .messageDao()
                            .findByReceiver(nodeUuid)
                            .observeOn(Schedulers.newThread())
                            .subscribeOn(Schedulers.newThread())
                            .subscribe(messagesInTheDB -> {

                                Frontend.FrontendResponseListener getMessagesResponseListener = new Frontend.FrontendResponseListener() {
                                    @Override
                                    public void onResponse(Object response) {
                                        List<Message> messagesInTheServer = (List<Message>) response;

                                        // Remove the messages that are already in the server from the local message list
                                        messagesToBeWritten = messagesInTheDB.stream().filter(message -> !message.isInServer()).collect(Collectors.toList());

                                        // Add the messages in the server to the list of messages to be written
                                        messagesToBeWritten.addAll(messagesInTheServer);

                                        writeNextChunk();
                                    }

                                    @Override
                                    public void onError(String errorMessage) {
                                        messagesToBeWritten = messagesInTheDB;

                                        writeNextChunk();
                                    }
                                };

                                globalClass.getFrontend().getMessagesByReceiver(nodeUuid, getMessagesResponseListener);
                            });

                    //writeNextChunk();
                } else if (characteristicId.equals(BleGattIDs.OUTGOING_MESSAGE_CHARACTERISTIC_ID)) {
                    Log.d(TAG, "Message read");

                    byte[] chunkByteArray = intent.getByteArrayExtra("byte-array-value");

                    if (chunkByteArray.length >= 4 && chunkByteArray[0] == 0 && chunkByteArray[1] == 0) {
                        if (!ReceiveAndPropagateUpdatesService.this.currentOutgoingMessageChunks.isEmpty()) {
                            // The message chunks' list is not empty and the current chunk's length is 0,
                            // which means all of the current message's chunks have been received

                            Log.d(TAG, "All message chunks have been read");

                            // Add the current message to the cache
                            Message message = new Message(ReceiveAndPropagateUpdatesService.this.currentOutgoingMessageChunks);

                            WithoutNetAppDatabase withoutNetAppDatabase = globalClass.getWithoutNetAppDatabase();

                            withoutNetAppDatabase.messageDao()
                                    .insertAll(message)
                                    .subscribeOn(Schedulers.newThread())
                                    .observeOn(Schedulers.newThread())
                                    .onErrorComplete(throwable -> {
                                        if (!throwable.getClass().equals(SQLiteConstraintException.class)) {
                                            throwable.printStackTrace();
                                        }
                                        return true;
                                    })
                                    .subscribe(() -> Log.d(TAG, "Added message"));

                            //globalClass.addMessage(message);

                            Log.d(TAG, "Message added to message list: " + message);
                            Log.d(TAG, "Message byte array: " + message.byteArrayToString());

                            // Reset the current incoming message chunk list, in order to collect
                            // the next message's chunks
                            ReceiveAndPropagateUpdatesService.this.currentOutgoingMessageChunks.clear();
                        }

                        // If the message chunk's list is empty, then the node must not have any messages
                        // to send, meaning the code in the if clause below should be executed

                        if (!(chunkByteArray[2] == 0 && chunkByteArray[3] == 0)) {
                            // The "isLastMessage" flag on this end chunk has been set to true, meaning
                            // there are no more messages to be read from the node

                            Log.d(TAG, "No more messages to be read from node");
                            Log.d(TAG, "Session with node complete. Disconnecting...");

                            final boolean result = bleService.disconnect();
                            Log.d(TAG, "Disconnect request result = " + result);

                            /*if(result == true) {
                                ReceiveAndPropagateUpdatesService.this.scanner.startScan();
                            }*/

                            return;
                        }
                    } else {
                        // Append the new chunk to the other chunks
                        ReceiveAndPropagateUpdatesService.this.currentOutgoingMessageChunks.add(chunkByteArray);

                        Log.d(TAG, "Read chunk " + ReceiveAndPropagateUpdatesService.this.currentOutgoingMessageChunks.size());
                    }

                    // Read the next chunk
                    BluetoothGattCharacteristic outgoingMessageCharacteristic = bleService.getOutgoingMessageCharacteristic();
                    bleService.readCharacteristic(outgoingMessageCharacteristic);
                } else {
                    // TODO: Throw an exception
                    Log.d(TAG, "Unknown characteristic id:" + characteristicId);
                }
            } else if (BleService.ACTION_CHARACTERISTIC_WRITTEN.equals(action)) {
                // TODO: Write debug message to log
                writeNextChunk();
            } else {
                Log.d(TAG, "Unknown action received by broadcast receiver");
            }
        }
    };

    private void connectToNextNodeInQueue() {
        if(connectAddressQueue.size() > 0) {
            scanner.setConnectionOngoing(true);

            String address = connectAddressQueue.poll();
            boolean connectionSuccessful = bleService.connect(address);

            Log.d(TAG, "Connect request result = " + connectionSuccessful);

            if(!connectionSuccessful) {
                connectToNextNodeInQueue();
            }
        } else {
            scanner.setConnectionOngoing(false);
            scanner.startScan();
        }
    }

    public String byteArrayToString(byte[] byteArray) {
        byte[] messageByteArray = byteArray;
        String messageByteArrayString = "";

        for (byte messageByte : messageByteArray) {
            messageByteArrayString += messageByte + " # ";
        }

        return messageByteArrayString;
    }

    private Thread exchangeMessagesWithServerThread = new Thread(new Runnable() {
        @Override
        public void run() {
            while (true) {
                Log.d(TAG, "Sending messages to the server.");
                Log.d(TAG, "Sending messages to the server..");
                Log.d(TAG, "Sending messages to the server...");
                ReceiveAndPropagateUpdatesService.this.exchangeMessagesWithServer();
                try {
                    Thread.sleep(globalClass.getMessageTransmissionToServerInterval());
                } catch (InterruptedException e) {
                    // TODO: Handle this exception properly
                    e.printStackTrace();
                    break;
                    //Thread.currentThread().interrupt();
                }
            }
        }
    });

    private void writeNextChunkToBLCharacteristic() {
        BluetoothGattCharacteristic incomingMessageCharacteristic = bleService.getIncomingMessageCharacteristic();

        byte[] chunk = currentIncomingMessageChunks.get(0);
        currentIncomingMessageChunks.remove(chunk);

        incomingMessageCharacteristic.setValue(chunk);

        Log.d(TAG, "Next chunk to be written to node: " + byteArrayToString(chunk));

        bleService.writeCharacteristic(incomingMessageCharacteristic);
    }

    private void deleteMessage(Message message) {
        globalClass.getWithoutNetAppDatabase().messageDao()
                .delete(message)
                .observeOn(Schedulers.newThread())
                .subscribeOn(Schedulers.newThread())
                .subscribe(() -> {
                    Log.d(TAG, "Deleted message in database: " + message);
                });

        if (message.isInServer()) {
            Frontend.FrontendResponseListener deleteMessageResponseListener = new Frontend.FrontendResponseListener() {
                @Override
                public void onResponse(Object response) {
                    Log.d(TAG, "Deleted message in server: " + message);
                }

                @Override
                public void onError(String errorMessage) {
                    Log.e(TAG, "Delete message in server: " + errorMessage);
                }
            };

            globalClass.getFrontend().deleteMessage(message, deleteMessageResponseListener);
        }
    }

    private void writeNextChunk() {
        if (currentIncomingMessageChunks.isEmpty()) {
            // The previous incoming message's chunks have all been sent,
            // so now the next messages chunks must be sent

            if (messageToBeWritten != null && messagesToBeWritten.contains(messageToBeWritten)) {
                // The previous message to be written to the node has been written, so now
                // it should be removed from the list of messages to be written, the db
                // and the server
                messagesToBeWritten.remove(messageToBeWritten);

                deleteMessage(messageToBeWritten);
            }

            if (messagesToBeWritten != null && messagesToBeWritten.size() > 0) {
                // There are still messages left to write to the node

                messageToBeWritten = messagesToBeWritten.get(0);

                Log.d(TAG, "Next message to be written to node: " + messageToBeWritten.toString());

                currentIncomingMessageChunks = messageToBeWritten.toChunks();

                writeNextChunkToBLCharacteristic();
            } else {
                // All messages meant for the node have been written
                // Time to read the node's pending messages

                Log.d(TAG, "No more messages to be written to node");

                BluetoothGattCharacteristic outgoingMessageCharacteristic = bleService.getOutgoingMessageCharacteristic();
                bleService.readCharacteristic(outgoingMessageCharacteristic);
            }
        } else {
            writeNextChunkToBLCharacteristic();
        }
    }

    private void writeNextMessage() {
        int receiverUuid = bleService.getCurrentNodeUuid();

        // Write every message intended for this node
        TreeSet<Message> messagesToBeWritten = globalClass.getAllMessagesForReceiver(receiverUuid);

        if (messagesToBeWritten != null && messagesToBeWritten.size() > 0) {
            BluetoothGattCharacteristic incomingMessageCharacteristic = bleService.getIncomingMessageCharacteristic();

            // TODO: It should be checked if incomingMessageCharacteristic != null

            // "Pop" a message from this set
            Message message = messagesToBeWritten.first();
            incomingMessageCharacteristic.setValue(message.toByteArray());
            //incomingMessageCharacteristic.setValue("12345678901111111111111111111111111111111111111111111111111");

            Log.d(TAG, "Next message to be written to node: " + message.toString());

            Log.d(TAG, "Characteristic ID: " + incomingMessageCharacteristic.getUuid());

            bleService.writeCharacteristic(incomingMessageCharacteristic);
            messagesToBeWritten.remove(message);

            return;
        }

        Log.d(TAG, "No more messages to be written to node");

        // All messages meant for the node have been written
        // Time to read the node's pending messages
        BluetoothGattCharacteristic outgoingMessageCharacteristic = bleService.getOutgoingMessageCharacteristic();
        bleService.readCharacteristic(outgoingMessageCharacteristic);

        //allIncomingMessagesWritten = true;


        /*if(allOutgoingMessagesRead) {
            Log.d(TAG, "Session with node complete. Disconnecting...");

            // Disconnect from node
            final boolean result = bleService.disconnect();
            Log.d(TAG, "Disconnect request result = " + result);

            allIncomingMessagesWritten = false;
            allOutgoingMessagesRead = false;
        }*/
    }

    private void exchangeMessagesWithServer() {
        Frontend frontend = globalClass.getFrontend();
        WithoutNetAppDatabase withoutNetAppDatabase = globalClass.getWithoutNetAppDatabase();

        // Write the messages in cache to the server
        // TODO: Messages should be sent to the server in bulk
        HashMap<Integer, TreeSet<Message>> messagesByReceiver = globalClass.getAllMessages();

        //List<Integer> receivers = new ArrayList<>(messagesByReceiver.keySet());

        disposable.add(withoutNetAppDatabase.messageDao().getAll()
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.newThread())
                .subscribe(messages -> {
                    for (Message message : messages) {
                        if (!message.isInServer()) {
                            Frontend.FrontendResponseListener sendMessageToServerResponseListener = new Frontend.FrontendResponseListener() {
                                @Override
                                public void onResponse(Object response) {
                                    Log.d(TAG, "Received a response to add message request");

                                    int status = (int) response;

                                    if (status == StatusCodes.OK) {
                                        message.setInServer(true);
                                        Log.d(TAG, "Added message to server");

                                        withoutNetAppDatabase.messageDao()
                                                .update(message)
                                                .subscribeOn(Schedulers.newThread())
                                                .observeOn(Schedulers.newThread())
                                                .subscribe(() -> Log.d(TAG, "Updated message in database"));
                                    }
                                }

                                @Override
                                public void onError(String errorMessage) {
                                    Log.e(TAG, errorMessage);
                                }
                            };

                            frontend.sendMessageToServerViaVolley(message, sendMessageToServerResponseListener);

                            /*int status = frontend.sendMessageToServer(message);

                            if(status == StatusCodes.OK) {
                                message.setInServer(true);
                            }*/
                        }
                    }
                }));
    }

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

        BleScanner.OnScanEventListener onScanEventListener = new BleScanner.OnScanEventListener() {
            @Override
            public void onScanComplete(Queue<String> addressQueue) {
                connectAddressQueue = addressQueue;
                connectToNextNodeInQueue();
            }
        };

        this.scanner = new BleScanner(getApplicationContext()
                , onScanEventListener
                , globalClass.getNodeScanningInterval());

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
        this.stop();
        //getApplicationContext().unbindService(serviceConnection);
        return super.onUnbind(intent);
    }

    public boolean start() {
        registerReceiver(gattUpdateReceiver, makeGattUpdateIntentFilter());
        // TODO: Should the service be started instead of being bound to the context?
        Intent gattServiceIntent = new Intent(getApplicationContext(), BleService.class);
        getApplicationContext().bindService(gattServiceIntent, serviceConnection, Context.BIND_AUTO_CREATE);
        // TODO: Start uploading and downloading messages to the central server
        //this.exchangeMessagesWithServerThread.start();
        return true;
    }

    public boolean stop() {
        this.scanner.stopScan();
        unregisterReceiver(gattUpdateReceiver);
        getApplicationContext().unbindService(serviceConnection);
        this.exchangeMessagesWithServerThread.interrupt();
        return true;
    }

    private boolean enoughTimeHasPassedSinceLastConnection(String address) {
        /*Long lastConnectionTime = this.lastConnectionTimesByAddress.get(address);

        if(lastConnectionTime == null) {
            return true;
        }

        return (System.currentTimeMillis() - lastConnectionTime) >= MIN_CONNECTION_INTERVAL;*/

        return true;
    }
}
