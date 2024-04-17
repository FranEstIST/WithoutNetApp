package pt.ulisboa.tecnico.withoutnet.adapters;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.stream.Collectors;

import pt.ulisboa.tecnico.withoutnet.R;
import pt.ulisboa.tecnico.withoutnet.models.Network;

public class NetworksListAdapter extends RecyclerView.Adapter<NetworksListAdapter.NetworksListItemViewHolder> implements Filterable {
    public static final String TAG = "NetworksListAdapter";

    private ArrayList<Network> networks;
    private ArrayList<Network> filteredNetworks;
    private OnNetworkClickListener onNetworkClickListener;

    private boolean shouldOnlyDisplayFilteredNetworks;

    public NetworksListAdapter(ArrayList<Network> networks, OnNetworkClickListener onNetworkClickListener) {
        this.networks = networks;
        this.filteredNetworks = new ArrayList<>();
        this.onNetworkClickListener = onNetworkClickListener;
        this.shouldOnlyDisplayFilteredNetworks = false;
    }

    public NetworksListAdapter(ArrayList<Network> networks, OnNetworkClickListener onNetworkClickListener, boolean shouldOnlyDisplayFilteredNetworks) {
        this.networks = networks;
        this.filteredNetworks = new ArrayList<>();
        this.onNetworkClickListener = onNetworkClickListener;
        this.shouldOnlyDisplayFilteredNetworks = shouldOnlyDisplayFilteredNetworks;
    }

    @NonNull
    @Override
    public NetworksListItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_network, parent, false);
        return new NetworksListItemViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull NetworksListItemViewHolder holder, int position) {
        Network network;

        if(shouldOnlyDisplayFilteredNetworks) {
            network = filteredNetworks.get(position);
        } else {
            network = networks.get(position);
        }

        holder.networkNameTextView.setText(network.getName());

        int nodeQuantity = network.getNodes().size();
        String nodeQuantityString = nodeQuantity == 1 ? nodeQuantity + " node" : nodeQuantity + " nodes";

        holder.nodeQuantityTextView.setText(nodeQuantityString);
    }

    @Override
    public int getItemCount() {
        return shouldOnlyDisplayFilteredNetworks ? filteredNetworks.size() : networks.size();
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults filterResults = new FilterResults();
                ArrayList<Network> resultingList;

                if(constraint == null || constraint.equals("")) {
                    resultingList = new ArrayList<>();
                } else {
                    resultingList = new ArrayList<>(networks.stream().filter(network -> {
                        return network.getName().toLowerCase().contains(constraint.toString().toLowerCase());
                    }).collect(Collectors.toList()));
                }

                filterResults.values = resultingList;
                filterResults.count = resultingList.size();

                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                filteredNetworks = (ArrayList<Network>)results.values;
                notifyDataSetChanged();
            }
        };
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
            Network clickedNetwork = shouldOnlyDisplayFilteredNetworks ? filteredNetworks.get(clickedPosition) : networks.get(clickedPosition);

            Log.d(TAG, "Clicked on network: " + clickedPosition);

            onNetworkClickListener.onNetworkClick(clickedNetwork);
        }
    }

    public interface OnNetworkClickListener {
        void onNetworkClick(Network network);
    }
}
