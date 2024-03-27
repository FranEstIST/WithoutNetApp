package pt.ulisboa.tecnico.withoutnet.adapters;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import pt.ulisboa.tecnico.withoutnet.R;
import pt.ulisboa.tecnico.withoutnet.models.Network;

public class NetworksListAdapter extends RecyclerView.Adapter<NetworksListAdapter.NetworksListItemViewHolder> {
    public static final String TAG = "NetworksListAdapter";

    private ArrayList<Network> networks;
    private OnNetworkClickListener onNetworkClickListener;

    public NetworksListAdapter(ArrayList<Network> networks, OnNetworkClickListener onNetworkClickListener) {
        this.networks = networks;
        this.onNetworkClickListener = onNetworkClickListener;
    }

    @NonNull
    @Override
    public NetworksListItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_network, parent, false);
        return new NetworksListItemViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull NetworksListItemViewHolder holder, int position) {
        Network network = networks.get(position);

        holder.networkNameTextView.setText(network.getName());

        int nodeQuantity = network.getNodes().size();
        String nodeQuantityString = nodeQuantity == 1 ? nodeQuantity + " node" : nodeQuantity + " nodes";

        holder.nodeQuantityTextView.setText(nodeQuantityString);
    }

    @Override
    public int getItemCount() {
        return networks.size();
    }

    public ArrayList<Network> getNetworks() {
        return networks;
    }

    public void setNetworks(ArrayList<Network> networks) {
        this.networks = networks;
    }

    class NetworksListItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public TextView networkNameTextView;
        public TextView nodeQuantityTextView;

        public NetworksListItemViewHolder(@NonNull View itemView) {
            super(itemView);
            networkNameTextView = itemView.findViewById(R.id.networkNameTextView);
            nodeQuantityTextView = itemView.findViewById(R.id.nodeQuantityTextView);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int clickedPosition = getAdapterPosition();
            Network clickedNetwork = networks.get(clickedPosition);

            Log.d(TAG, "Clicked on network: " + clickedPosition);

            onNetworkClickListener.onNetworkClick(clickedNetwork);
        }
    }

    public interface OnNetworkClickListener {
        void onNetworkClick(Network network);
    }
}
