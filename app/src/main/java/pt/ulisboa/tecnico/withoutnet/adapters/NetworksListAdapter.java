package pt.ulisboa.tecnico.withoutnet.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import pt.ulisboa.tecnico.withoutnet.R;
import pt.ulisboa.tecnico.withoutnet.models.Network;

public class NetworksListAdapter extends RecyclerView.Adapter<NetworksListAdapter.NetworksListItemViewHolder> {
    private ArrayList<Network> networks;

    public NetworksListAdapter(ArrayList<Network> networks) {
        this.networks = networks;
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
        }

        @Override
        public void onClick(View v) {

        }
    }
}
