package pt.ulisboa.tecnico.withoutnet.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.LinkedHashMap;

import pt.ulisboa.tecnico.withoutnet.R;
import pt.ulisboa.tecnico.withoutnet.models.Node;

public class NodesListAdapter extends RecyclerView.Adapter<NodesListAdapter.ViewHolder> {
    private ArrayList<Node> nodes;

    private OnNodeClickListener onNodeClickListener;

    public NodesListAdapter(ArrayList<Node> nodes, OnNodeClickListener onNodeClickListener) {
        this.nodes = nodes;
        this.onNodeClickListener = onNodeClickListener;
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
        Node node = nodes.get(position);

        TextView nameTextView = holder.nameTextView;
        nameTextView.setText(node.getCommonName());

        TextView UUIDTextView = holder.UUIDTextView;
        UUIDTextView.setText(String.valueOf(node.getId()));

        TextView networkNameTextView = holder.networkNameTextView;

        // TODO: Replace "In" by a string in the string resource file
        if(node.getNetwork() != null) {
            networkNameTextView.setText("In " + node.getNetwork().getName());
        } else {
            networkNameTextView.setVisibility(View.GONE);
        }
        //UUIDTextView.setText(String.valueOf(node.getServiceUUID()));
    }

    @Override
    public int getItemCount() {
        return nodes.size();
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
            this.onNodeClickListener.onNodeClick(nodes.get(clickedPosition));
        }
    }

    public interface OnNodeClickListener {
        void onNodeClick(Node clickedNode);
    }
}
