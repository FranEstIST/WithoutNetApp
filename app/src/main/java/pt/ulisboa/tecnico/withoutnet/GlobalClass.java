package pt.ulisboa.tecnico.withoutnet;

import android.app.Application;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.TreeSet;

import pt.ulisboa.tecnico.withoutnet.db.WithoutNetAppDatabase;
import pt.ulisboa.tecnico.withoutnet.models.Message;
import pt.ulisboa.tecnico.withoutnet.models.Node;
import pt.ulisboa.tecnico.withoutnet.models.Update;

public class GlobalClass extends Application {
    // Map for the updates, with the sender as the key
    // TODO: Change the update collection type to something more appropriate, like a stack
    // private HashMap<Node, ArrayDeque<Update>> updatesByNode;
    private HashMap<Node, TreeSet<Update>> updatesByNode;
    private HashMap<Integer, TreeSet<Message>> messagesByReceiver;

    private String serverURL;
    private int nodeScanPeriod;
    private int serverMessageExchangePeriod;

    private SharedPreferences WNAppSharedPrefs;

    private Frontend frontend;
    private RequestQueue requestQueue;
    private WithoutNetAppDatabase withoutNetAppDatabase;

    private static final String DEFAULT_SERVER_URL = "http://192.168.1.102:8081/";
    private static final int DEFAULT_NODE_SCANNING_INTERVAL = 10;
    private static final int DEFAULT_MESSAGE_TRANSMISSION_TO_SERVER_INTERVAL = 10;
    private static final int DEFAULT_MAXIMUM_NUM_OF_MESSAGES_IN_CACHE = 1000;

    @Override
    public void onCreate() {
        super.onCreate();
        updatesByNode = new HashMap<>();
        messagesByReceiver = new HashMap<>();
        frontend = new Frontend(this);

        WNAppSharedPrefs = getSharedPreferences("WNAppSharedPrefs", MODE_PRIVATE);
        getSharedPreferences("serverURL", MODE_PRIVATE).getString("serverURL", "http");
    }

    public Update getMostRecentUpdate(Node node) {
        TreeSet<Update> updates = this.updatesByNode.get(node);

        if(updates != null) {
            Iterator<Update> updateIterator = updates.iterator();
            Update update = null;

            while(updateIterator.hasNext()) {
                update = updateIterator.next();
            }

            return update;
        }

        return null;
    }

    public TreeSet<Update> getAllNodeUpdates(Node node) {
        return this.updatesByNode.get(node);
    }

    public synchronized TreeSet<Message> getAllMessagesForReceiver(int receiver) {
        return this.messagesByReceiver.get(receiver);
    }

    public synchronized HashMap<Integer, TreeSet<Message>> getAllMessages() {
        HashMap<Integer, TreeSet<Message>> messagesByReceiverCopy = new HashMap<>();

        for(Integer receiver : messagesByReceiver.keySet()) {
            TreeSet<Message> messages = messagesByReceiver.get(receiver);
            TreeSet<Message> messagesCopy = new TreeSet<Message>(new TreeSet<Message>(new MessageTimestampComparator()));

            for(Message message : messages) {
                // TODO: Should each message be cloned as well?
                messagesCopy.add(message);
            }

            messagesByReceiverCopy.put(receiver, messagesCopy);
        }

        return messagesByReceiverCopy;
    }

    public HashMap<Node, TreeSet<Update>> getAllUpdates() {
        return this.updatesByNode;
    }

    public WithoutNetAppDatabase getWithoutNetAppDatabase() {
        return withoutNetAppDatabase;
    }

    public void addUpdate(Update update) {
        Node sender = update.getSender();

        if(!this.updatesByNode.containsKey(sender)) {
            this.updatesByNode.put(sender, new TreeSet<>(new UpdateTimestampComparator()));
        }

        this.updatesByNode.get(sender).add(update);
    }

    // TODO
    public synchronized void addMessage(Message message) {
        int receiver = message.getReceiver();

        if(!this.messagesByReceiver.containsKey(receiver)) {
            this.messagesByReceiver.put(receiver, new TreeSet<Message>(new MessageTimestampComparator()));
        }

        // TODO: It should be checked if the message to be added is a duplicate -V

        TreeSet<Message> messageTreeSet = this.messagesByReceiver.get(receiver);

        if(!containsMessage(messageTreeSet, message)) {
            messageTreeSet.add(message);
        }
    }

    public void setWithoutNetAppDatabase(WithoutNetAppDatabase withoutNetAppDatabase) {
        this.withoutNetAppDatabase = withoutNetAppDatabase;
    }

    private boolean containsMessage(TreeSet<Message> messageTreeSet, Message message) {
        for(Message messageInTreeSet : messageTreeSet) {
            if(messageInTreeSet.getTimestamp() == message.getTimestamp()) {
                return true;
            }
        }

        return false;
    }

    //public void getUpdate

    public Frontend getFrontend() {
        return this.frontend;
    }

    public RequestQueue getRequestQueue() {
        if(this.requestQueue == null) {
            this.requestQueue = Volley.newRequestQueue(this.getApplicationContext());
        }

        return this.requestQueue;
    }

    public String getServerURL() {
        return WNAppSharedPrefs
                .getString("serverURL"
                        , DEFAULT_SERVER_URL);
    }

    public void setServerURL(String serverURL) {
        WNAppSharedPrefs
                .edit()
                .putString("serverURL"
                        , serverURL)
                .apply();
    }

    public int getNodeScanningInterval() {
        return WNAppSharedPrefs
                .getInt("nodeScanningInterval"
                        , DEFAULT_NODE_SCANNING_INTERVAL);
    }

    public void setNodeScanningInterval(int nodeScanningInterval) {
        WNAppSharedPrefs
                .edit()
                .putInt("nodeScanningInterval"
                        , nodeScanningInterval)
                .apply();
    }

    public int getMessageTransmissionToServerInterval() {
        return WNAppSharedPrefs
                .getInt("messageTransmissionToServerInterval"
                        , DEFAULT_MESSAGE_TRANSMISSION_TO_SERVER_INTERVAL);
    }

    public void setMessageTransmissionToServerInterval(int messageTransmissionToServerInterval) {
        WNAppSharedPrefs
                .edit()
                .putInt("messageTransmissionToServerInterval"
                        , messageTransmissionToServerInterval)
                .apply();
    }

    public int getMaximumNumOfMessagesInCache() {
        return WNAppSharedPrefs
                .getInt("maximumNumOfMessagesInCache"
                        , DEFAULT_MAXIMUM_NUM_OF_MESSAGES_IN_CACHE);
    }

    public void setMaximumNumOfMessagesInCache(int maximumNumOfMessagesInCache) {
        WNAppSharedPrefs
                .edit()
                .putInt("maximumNumOfMessagesInCache"
                        , maximumNumOfMessagesInCache)
                .apply();
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
    }

    class UpdateTimestampComparator implements Comparator<Update> {
        @Override
        public int compare(Update o1, Update o2) {
            if(o1.getTimestamp() > o2.getTimestamp()) {
                return 1;
            } else if (o1.getTimestamp() == o2.getTimestamp()) {
                return 0;
            } else {
                return -1;
            }
        }
    }

    class MessageTimestampComparator implements Comparator<Message> {
        @Override
        public int compare(Message o1,Message o2) {
            if(o1.getTimestamp() > o2.getTimestamp()) {
                return 1;
            } else if (o1.getTimestamp() == o2.getTimestamp()) {
                return 0;
            } else {
                return -1;
            }
        }
    }
}
