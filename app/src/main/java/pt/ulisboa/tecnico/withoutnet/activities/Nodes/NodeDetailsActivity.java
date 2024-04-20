package pt.ulisboa.tecnico.withoutnet.activities.Nodes;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import pt.ulisboa.tecnico.withoutnet.R;
import pt.ulisboa.tecnico.withoutnet.activities.Networks.ChangeNodeNetworkActivity;
import pt.ulisboa.tecnico.withoutnet.adapters.NodeFieldsListAdapter;
import pt.ulisboa.tecnico.withoutnet.databinding.ActivityNodeDetailsBinding;
import pt.ulisboa.tecnico.withoutnet.models.Network;
import pt.ulisboa.tecnico.withoutnet.models.Node;

public class NodeDetailsActivity extends AppCompatActivity {
    private static final String TAG = "NodeDetailsActivity";

    private ActivityNodeDetailsBinding binding;
    private NodeFieldsListAdapter nodeFieldsListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityNodeDetailsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Node node = (Node) getIntent().getSerializableExtra("node");

        if(node == null) {
            finish();
        }

        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        binding.appBarTitle.setText(node.getCommonName());

        ActivityResultLauncher<Intent> changeNodeFieldNodeResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {
                if(result.getResultCode() == RESULT_OK) {
                    Intent data = result.getData();

                    if(!data.hasExtra("new-node-name")) {
                        return;
                    }

                    String newNodeName = data.getStringExtra("new-node-name");

                    node.setCommonName(newNodeName);

                    nodeFieldsListAdapter.setNode(node);
                }
            }
        });

        ActivityResultLauncher<Intent> changeNodeNetworkResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {
                if(result.getResultCode() == RESULT_OK) {
                    Intent data = result.getData();

                    if(!data.hasExtra("new-node-network")) {
                        return;
                    }

                    Network newNodeNetwork = (Network) data.getSerializableExtra("new-node-network");

                    node.setNetwork(newNodeNetwork);

                    nodeFieldsListAdapter.setNode(node);
                }
            }
        });

        NodeFieldsListAdapter.OnChangeFieldButtonClick onChangeFieldButtonClick = new NodeFieldsListAdapter.OnChangeFieldButtonClick() {
            @Override
            public void onChangeNameButtonClick() {
                Intent intent = new Intent(NodeDetailsActivity.this, ChangeNodeFieldValuePopUpActivity.class);
                intent.putExtra("node-field-type", ChangeNodeFieldValuePopUpActivity.NodeFieldType.NAME);
                intent.putExtra("node", node);
                changeNodeFieldNodeResultLauncher.launch(intent);
            }

            @Override
            public void onChangeNetworkButtonClick() {
                Intent intent = new Intent(NodeDetailsActivity.this, ChangeNodeNetworkActivity.class);
                intent.putExtra("node", node);
                changeNodeNetworkResultLauncher.launch(intent);
            }
        };

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        binding.nodeFieldsListRecyclerView.setLayoutManager(layoutManager);

        binding.nodeFieldsListRecyclerView.addItemDecoration(new DividerItemDecoration(this,
                DividerItemDecoration.VERTICAL));

        nodeFieldsListAdapter = new NodeFieldsListAdapter(node, onChangeFieldButtonClick);
        binding.nodeFieldsListRecyclerView.setAdapter(nodeFieldsListAdapter);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) // Press Back Icon
        {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}