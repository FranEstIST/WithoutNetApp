package pt.ulisboa.tecnico.withoutnet.adapters;

import static android.app.Activity.RESULT_OK;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.telephony.NetworkRegistrationInfo;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import pt.ulisboa.tecnico.withoutnet.R;
import pt.ulisboa.tecnico.withoutnet.activities.Nodes.ChangeNodeFieldValuePopUpActivity;
import pt.ulisboa.tecnico.withoutnet.activities.Networks.ChangeNodeNetworkActivity;
import pt.ulisboa.tecnico.withoutnet.activities.Nodes.NodesListActivity;
import pt.ulisboa.tecnico.withoutnet.models.Network;
import pt.ulisboa.tecnico.withoutnet.models.Node;

public class NodeFieldsListAdapter extends RecyclerView.Adapter<NodeFieldsListAdapter.NodeFieldViewHolder> {
    private static final int NUM_OF_FIELDS = 3;

    private Node node;

    private AppCompatActivity activity;

    private OnChangeFieldButtonClick onChangeFieldButtonClick;

    public NodeFieldsListAdapter(Node node, OnChangeFieldButtonClick onChangeFieldButtonClick) {
        super();
        this.node = node;
        this.onChangeFieldButtonClick = onChangeFieldButtonClick;
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
                    onChangeFieldButtonClick.onChangeNameButtonClick();
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
                    onChangeFieldButtonClick.onChangeNetworkButtonClick();
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return NUM_OF_FIELDS;
    }

    public void setNode(Node node) {
        this.node = node;
        notifyDataSetChanged();
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

    public interface OnChangeFieldButtonClick {
        void onChangeNameButtonClick();
        void onChangeNetworkButtonClick();
    }
}
