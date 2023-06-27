package pt.ulisboa.tecnico.withoutnet;

import android.app.Application;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.TreeSet;

import pt.ulisboa.tecnico.withoutnet.models.Message;
import pt.ulisboa.tecnico.withoutnet.models.Node;
import pt.ulisboa.tecnico.withoutnet.models.Update;

public class GlobalClass extends Application {
    // Map for the updates, with the sender as the key
    // TODO: Change the update collection type to something more appropriate, like a stack
    // private HashMap<Node, ArrayDeque<Update>> updatesByNode;
    private HashMap<Node, TreeSet<Update>> updatesByNode;
    private HashMap<String, TreeSet<Message>> messagesByReceiver;

    private Frontend frontend;

    @Override
    public void onCreate() {
        super.onCreate();
        updatesByNode = new HashMap<>();
        messagesByReceiver = new HashMap<>();
        frontend = new Frontend(this);
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

    public TreeSet<Message> getAllMessagesForReceiver(String receiver) {
        return this.messagesByReceiver.get(receiver);
    }

    public HashMap<Node, TreeSet<Update>> getAllUpdates() {
        return this.updatesByNode;
    }

    public void addUpdate(Update update) {
        Node sender = update.getSender();

        if(!this.updatesByNode.containsKey(sender)) {
            this.updatesByNode.put(sender, new TreeSet<>(new UpdateTimestampComparator()));
        }

        this.updatesByNode.get(sender).add(update);
    }

    // TODO
    public void addMessage(Message message) {
        String sender = message.getSender();

        if(!this.messagesByReceiver.containsKey(sender)) {
            this.messagesByReceiver.put(sender, new TreeSet<Message>(new MessageTimestampComparator()));
        }

        this.messagesByReceiver.get(sender).add(message);
    }

    //public void getUpdate

    public Frontend getFrontend() {
        return this.frontend;
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
