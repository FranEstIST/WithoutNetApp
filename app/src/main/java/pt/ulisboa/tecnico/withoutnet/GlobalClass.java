package pt.ulisboa.tecnico.withoutnet;

import android.app.Application;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Set;
import java.util.TreeSet;

public class GlobalClass extends Application {
    // Map for the updates, with the sender as the key
    private HashMap<Node, TreeSet<Update>> updates;
    @Override
    public void onCreate() {
        super.onCreate();
        updates = new HashMap<>();
    }

    public Update getMostRecentUpdate(Node node) {
        return this.updates.get(node).iterator().next();
    }

    public TreeSet<Update> getAllNodeUpdates(Node node) {
        return this.updates.get(node);
    }

    public HashMap<Node, TreeSet<Update>> getAllUpdates() {
        return this.updates;
    }

    public void addUpdate(Update update) {
        Node sender = update.getSender();

        if(!this.updates.containsKey(sender)) {
            this.updates.put(sender, new TreeSet<>(new UpdateTimestampComparator()));
        }

        this.updates.get(sender).add(update);
    }

    //public void getUpdate

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
}
