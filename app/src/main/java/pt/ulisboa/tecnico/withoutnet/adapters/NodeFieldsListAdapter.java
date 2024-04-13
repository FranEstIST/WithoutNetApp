package pt.ulisboa.tecnico.withoutnet.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import pt.ulisboa.tecnico.withoutnet.R;
import pt.ulisboa.tecnico.withoutnet.activities.ChangeNodeFieldValuePopUpActivity;
import pt.ulisboa.tecnico.withoutnet.activities.ChangeNodeNetworkActivity;
import pt.ulisboa.tecnico.withoutnet.models.Node;

public class NodeFieldsListAdapter extends RecyclerView.Adapter<NodeFieldsListAdapter.NodeFieldViewHolder> {
    private static final int NUM_OF_FIELDS = 3;

    private Node node;

    private Context context;

    public NodeFieldsListAdapter(Node node, Context context) {
        super();
        this.node = node;
        this.context = context;
    }

    @NonNull
    @Override
    public NodeFieldViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_node_detail, parent, false);
        return new NodeFieldViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull NodeFieldViewHolder holder, int position) {
        if(position == 0) {
            holder.fieldNameTextView.setText(R.string.id);
            holder.fieldValueTextView.setText(node.getId() + "");
            holder.editFieldValueButton.setVisibility(View.GONE);
        } else if(position == 1) {
            holder.fieldNameTextView.setText(R.string.name);
            holder.fieldValueTextView.setText(node.getCommonName());

            holder.editFieldValueButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(NodeFieldsListAdapter.this.context, ChangeNodeFieldValuePopUpActivity.class);
                    intent.putExtra("node-field-type", ChangeNodeFieldValuePopUpActivity.NodeFieldType.NAME);
                    context.startActivity(intent);
                }
            });
        } else {
            holder.fieldNameTextView.setText(R.string.network);

            if(node.getNetwork() != null) {
                holder.fieldValueTextView.setText(node.getNetwork().getName());
            } else {
                holder.fieldValueTextView.setText(R.string.no_network);
            }

            holder.editFieldValueButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    /*Intent intent = new Intent(NodeFieldsListAdapter.this.context, ChangeNodeFieldValuePopUpActivity.class);
                    intent.putExtra("node-field-type", ChangeNodeFieldValuePopUpActivity.NodeFieldType.NETWORK);
                    context.startActivity(intent);*/

                    Intent intent = new Intent(NodeFieldsListAdapter.this.context, ChangeNodeNetworkActivity.class);
                    context.startActivity(intent);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return NUM_OF_FIELDS;
    }

    class NodeFieldViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public TextView fieldNameTextView;
        public TextView fieldValueTextView;
        public ImageButton editFieldValueButton;

        public NodeFieldViewHolder(@NonNull View itemView) {
            super(itemView);

            fieldNameTextView = itemView.findViewById(R.id.fieldName);
            fieldValueTextView = itemView.findViewById(R.id.fieldValue);
            editFieldValueButton = itemView.findViewById(R.id.editFieldValueButton);
        }

        @Override
        public void onClick(View v) {
            // TODO
        }
    }
}
