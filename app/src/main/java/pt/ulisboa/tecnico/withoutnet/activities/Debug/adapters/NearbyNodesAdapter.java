package pt.ulisboa.tecnico.withoutnet.activities.Debug.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.LinkedHashMap;

import pt.ulisboa.tecnico.withoutnet.models.Node;
import pt.ulisboa.tecnico.withoutnet.R;

public class NearbyNodesAdapter extends RecyclerView.Adapter<NearbyNodesAdapter.ViewHolder> {
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        // Your holder should contain a member variable
        // for any view that will be set as you render a row
        public TextView nameTextView;
        public TextView UUIDTextView;

        private OnNearbyNodeListener onNearbyNodeListener;

        // We also create a constructor that accepts the entire item row
        // and does the view lookups to find each subview
        public ViewHolder(View itemView, OnNearbyNodeListener onNearbyNodeListener) {
            // Stores the itemView in a public final member variable that can be used
            // to access the context from any ViewHolder instance.
            super(itemView);

            this.nameTextView = (TextView) itemView.findViewById(R.id.node_cn);
            this.UUIDTextView = (TextView) itemView.findViewById(R.id.node_uuid);

            this.onNearbyNodeListener = onNearbyNodeListener;

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            this.onNearbyNodeListener.onNearbyNodeClick(getAdapterPosition());
        }
    }

    // Store a member variable for the chatroom rows
    private LinkedHashMap<String, Node> nodesByName;

    private OnNearbyNodeListener onNearbyNodeListener;

    // Pass in the contact array into the constructor
    public NearbyNodesAdapter(LinkedHashMap<String, Node> nodesByName, OnNearbyNodeListener onNearbyNodeListener) {
        this.nodesByName = nodesByName;
        this.onNearbyNodeListener = onNearbyNodeListener;
    }

    // Usually involves inflating a layout from XML and returning the holder
    @Override
    public NearbyNodesAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        // Inflate the custom layout
        View nodeView = inflater.inflate(R.layout.item_node, parent, false);

        // Return a new holder instance
        ViewHolder viewHolder = new ViewHolder(nodeView, onNearbyNodeListener);
        return viewHolder;
    }

    // Involves populating data into the item through holder
    @Override
    public void onBindViewHolder(NearbyNodesAdapter.ViewHolder holder, int position) {
        // Get the data model based on position
        Node node = new ArrayList<>(nodesByName.values()).get(position);

        //Log.d("CA", "Chatroom is available: " + chatroom.isAvailable());

        // Set item views based on your views and data model
        TextView nameTextView = holder.nameTextView;

        nameTextView.setText(node.getCommonName());

        TextView UUIDTextView = holder.UUIDTextView;
        UUIDTextView.setText(String.valueOf(node.getId()));
        //UUIDTextView.setText(String.valueOf(node.getServiceUUID()));
    }

    // Returns the total count of items in the list
    @Override
    public int getItemCount() {
        return nodesByName.size();
    }

    public interface OnNearbyNodeListener {
        void onNearbyNodeClick(int position);
    }
}
