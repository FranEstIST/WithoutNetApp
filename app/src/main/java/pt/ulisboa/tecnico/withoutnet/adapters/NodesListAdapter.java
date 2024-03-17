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
        //UUIDTextView.setText(String.valueOf(node.getServiceUUID()));
    }

    @Override
    public int getItemCount() {
        return nodes.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public TextView nameTextView;
        public TextView UUIDTextView;

        private OnNodeClickListener onNodeClickListener;

        public ViewHolder(View itemView, OnNodeClickListener onNodeClickListener) {
            super(itemView);

            this.nameTextView = (TextView) itemView.findViewById(R.id.node_cn);
            this.UUIDTextView = (TextView) itemView.findViewById(R.id.node_uuid);

            this.onNodeClickListener = onNodeClickListener;

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            this.onNodeClickListener.onNodeClick(getAdapterPosition());
        }
    }

    public interface OnNodeClickListener {
        void onNodeClick(int position);
    }
}
