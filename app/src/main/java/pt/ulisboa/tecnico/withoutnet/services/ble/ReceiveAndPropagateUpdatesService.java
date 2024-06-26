package pt.ulisboa.tecnico.withoutnet.services.ble;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.database.sqlite.SQLiteConstraintException;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.UUID;
import java.util.stream.Collectors;

import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import pt.ulisboa.tecnico.withoutnet.Frontend;
import pt.ulisboa.tecnico.withoutnet.R;
import pt.ulisboa.tecnico.withoutnet.constants.BleGattIDs;
import pt.ulisboa.tecnico.withoutnet.constants.StatusCodes;
import pt.ulisboa.tecnico.withoutnet.db.WithoutNetAppDatabase;
import pt.ulisboa.tecnico.withoutnet.models.Message;
import pt.ulisboa.tecnico.withoutnet.utils.ble.BleScanner;
import pt.ulisboa.tecnico.withoutnet.GlobalClass;

public class ReceiveAndPropagateUpdatesService extends Service {
    private static final String TAG = "ReceiveAndPropagateUpdatesService";

    private LocalBinder binder = new LocalBinder();

    private BleScanner scanner;

    private BleService bleService;

    private GlobalClass globalClass;

    private Queue<String> connectAddressQueue = new LinkedList<>();

    private ArrayList<byte[]> currentOutgoingMessageChunks = new ArrayList<>();

    private ArrayList<byte[]> currentIncomingMessageChunks = new ArrayList<>();

    private Message messageToBeWritten = null;

    private List<Message> messagesToBeWritten = new ArrayList<>();
    private List<Message> messagesToBeDeleted = new ArrayList<>();

    private final CompositeDisposable disposable = new CompositeDisposable();

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
                // These lists, which allow for the collection of message chunks, must
                // be cleared upon a new connection, in case a previous connection
                // was interrupted while a message was being sent/received
                currentIncomingMessageChunks.clear();
                currentOutgoingMessageChunks.clear();

                Log.d(TAG, "Connected to node");

            } else if (BleService.ACTION_GATT_DISCONNECTED.equals(action)) {
                Log.d(TAG, "Disconnected from node");

                connectToNextNodeInQueue();
            } else if (BleService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                Log.d(TAG, "GATT Services discovered");

                List<BluetoothGattService> gattServiceList = bleService.getSupportedGattServices();

                BluetoothGattCharacteristic nodeUuidCharacteristic = null;
                BluetoothGattCharacteristic outgoingMessageCharacteristic = null;
                BluetoothGattCharacteristic incomingMessageCharacteristic = null;

                for (BluetoothGattService service : gattServiceList) {
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

                    bleService.setIncomingMessageCharacteristic(incomingMessageCharacteristic);
                    bleService.setOutgoingMessageCharacteristic(outgoingMessageCharacteristic);

                    bleService.readCharacteristic(nodeUuidCharacteristic);
                } else {
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

                                        // Remove the messages that are already in the server from
                                        // the local message list
                                        messagesToBeWritten = messagesInTheDB.stream()
                                                .filter(message -> !message.isInServer())
                                                .collect(Collectors.toList());

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

                            boolean result = bleService.disconnect();
                            Log.d(TAG, "Disconnect request result = " + result);

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
                    Log.e(TAG, "Error: Unknown characteristic with id:" + characteristicId);
                }
            } else if (BleService.ACTION_CHARACTERISTIC_WRITTEN.equals(action)) {
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

    private Thread sendMessagesInCacheToServerThread = new Thread(new Runnable() {
        @Override
        public void run() {
            while (true) {
                Log.d(TAG, "Sending messages to the server.");
                Log.d(TAG, "Sending messages to the server..");
                Log.d(TAG, "Sending messages to the server...");
                ReceiveAndPropagateUpdatesService.this.sendMessagesInCacheToServer();
                try {
                    Thread.sleep(globalClass.getMessageTransmissionToServerInterval());
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    break;
                }
            }
        }
    });

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

    private void writeNextChunkToBLCharacteristic() {
        BluetoothGattCharacteristic incomingMessageCharacteristic = bleService.getIncomingMessageCharacteristic();

        byte[] chunk = currentIncomingMessageChunks.get(0);
        currentIncomingMessageChunks.remove(chunk);

        incomingMessageCharacteristic.setValue(chunk);

        Log.d(TAG, "Next chunk to be written to node: " + byteArrayToString(chunk));

        bleService.writeCharacteristic(incomingMessageCharacteristic);
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

    private void sendMessagesInCacheToServer() {
        Frontend frontend = globalClass.getFrontend();
        WithoutNetAppDatabase withoutNetAppDatabase = globalClass.getWithoutNetAppDatabase();

        // TODO: Messages should be sent to the server in bulk
        // Write the messages in cache to the server
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
        Intent gattServiceIntent = new Intent(getApplicationContext(), BleService.class);
        getApplicationContext().bindService(gattServiceIntent, serviceConnection, Context.BIND_AUTO_CREATE);
        this.sendMessagesInCacheToServerThread.start();
        return true;
    }

    public boolean stop() {
        this.scanner.stopScan();
        unregisterReceiver(gattUpdateReceiver);
        getApplicationContext().unbindService(serviceConnection);
        this.sendMessagesInCacheToServerThread.interrupt();
        return true;
    }
}
