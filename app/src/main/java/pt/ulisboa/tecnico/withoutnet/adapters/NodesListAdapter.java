package pt.ulisboa.tecnico.withoutnet.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;

import pt.ulisboa.tecnico.withoutnet.Frontend;
import pt.ulisboa.tecnico.withoutnet.R;
import pt.ulisboa.tecnico.withoutnet.models.Network;
import pt.ulisboa.tecnico.withoutnet.models.Node;

public class NodesListAdapter extends RecyclerView.Adapter<NodesListAdapter.ViewHolder> implements Filterable {
    private ArrayList<Node> nodes;
    private ArrayList<Node> filteredNodes;

    private boolean shouldOnlyDiplayFilteredNodes;

    private OnNodeClickListener onNodeClickListener;

    public NodesListAdapter(ArrayList<Node> nodes, OnNodeClickListener onNodeClickListener, boolean shouldOnlyDiplayFilteredNodes) {
        this.nodes = nodes;
        this.filteredNodes = new ArrayList<>();
        this.onNodeClickListener = onNodeClickListener;
        this.shouldOnlyDiplayFilteredNodes = shouldOnlyDiplayFilteredNodes;
    }

    public NodesListAdapter(ArrayList<Node> nodes, OnNodeClickListener onNodeClickListener) {
        this.nodes = nodes;
        this.filteredNodes = new ArrayList<>();
        this.onNodeClickListener = onNodeClickListener;
        this.shouldOnlyDiplayFilteredNodes = false;
    }

    @Override
    public NodesListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        View nodeView = inflater.inflate(R.layout.item_node, parent, false);

        ViewHolder viewHolder = new ViewHolder(nodeView, onNodeClickListener);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(NodesListAdapter.ViewHolder holder, int position) {
        Node node;

        if(shouldOnlyDiplayFilteredNodes) {
            node = filteredNodes.get(position);
        } else {
            node = nodes.get(position);
        }

        TextView nameTextView = holder.nameTextView;
        nameTextView.setText(node.getCommonName());

        TextView UUIDTextView = holder.UUIDTextView;
        UUIDTextView.setText(String.valueOf(node.getId()));

        TextView networkNameTextView = holder.networkNameTextView;

        // TODO: Replace "In" by a string in the string resource file
        if(node.getNetwork() != null && !node.getNetwork().getName().equals("")) {
            networkNameTextView.setText("In " + node.getNetwork().getName());
        } else {
            networkNameTextView.setVisibility(View.GONE);
        }
        //UUIDTextView.setText(String.valueOf(node.getServiceUUID()));
    }

    @Override
    public int getItemCount() {
        return shouldOnlyDiplayFilteredNodes ? filteredNodes.size() : nodes.size();
    }

    /*public void filterNodesByQueryingServer(Frontend frontend, String query, Network network, OnServerQueryCompletedListener onServerQueryCompletedListener) {
        Frontend.FrontendResponseListener responseListener = new Frontend.FrontendResponseListener() {
            @Override
            public void onResponse(Object response) {

            }

            @Override
            public void onError(String errorMessage) {

            }
        };

        if(network == null) {
            frontend.getNodesInServerContainingSubstring();
        } else {
            frontend.getNodesInServerContainingSubstringInNetwork(query, network, );
        }

    }*/

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults results = new FilterResults();
                ArrayList resultingList;

                if(constraint == null || constraint.equals("")) {
                    resultingList = nodes;
                } else {
                    resultingList = new ArrayList(nodes.stream().filter(node -> {
                        return node.getCommonName().toLowerCase().contains(constraint.toString().toLowerCase());
                    }).collect(Collectors.toList()));
                }

                results.values = resultingList;
                results.count = resultingList.size();

                return results;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                filteredNodes = (ArrayList<Node>)results.values;
                notifyDataSetChanged();
            }
        };
    }

    public void setNodes(ArrayList<Node> nodes) {
        this.nodes = nodes;
        notifyDataSetChanged();
    }

    public void setFilteredNodes(ArrayList<Node> filteredNodes) {
        this.filteredNodes = filteredNodes;
        notifyDataSetChanged();
    }

    public void setShouldOnlyDiplayFilteredNodes(boolean shouldOnlyDiplayFilteredNodes) {
        this.shouldOnlyDiplayFilteredNodes = shouldOnlyDiplayFilteredNodes;
        notifyDataSetChanged();
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public TextView nameTextView;
        public TextView UUIDTextView;
        public TextView networkNameTextView;

        private OnNodeClickListener onNodeClickListener;

        public ViewHolder(View itemView, OnNodeClickListener onNodeClickListener) {
            super(itemView);

            this.nameTextView = (TextView) itemView.findViewById(R.id.node_cn);
            this.UUIDTextView = (TextView) itemView.findViewById(R.id.node_uuid);
            this.networkNameTextView = (TextView) itemView.findViewById(R.id.network_name_text_view);

            this.onNodeClickListener = onNodeClickListener;

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int clickedPosition = getAdapterPosition();
            Node clickedNode = shouldOnlyDiplayFilteredNodes ? filteredNodes.get(clickedPosition) : nodes.get(clickedPosition);
            this.onNodeClickListener.onNodeClick(clickedNode);
        }
    }

    public interface OnNodeClickListener {
        void onNodeClick(Node clickedNode);
    }

    public interface OnServerQueryCompletedListener {
        void onServerQueryCompleted(int returnedNodes);
    }
}
