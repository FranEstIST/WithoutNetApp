package pt.ulisboa.tecnico.withoutnet.activities.Nodes;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import pt.ulisboa.tecnico.withoutnet.Frontend;
import pt.ulisboa.tecnico.withoutnet.GlobalClass;
import pt.ulisboa.tecnico.withoutnet.R;
import pt.ulisboa.tecnico.withoutnet.activities.Networks.ChangeNodeNetworkActivity;
import pt.ulisboa.tecnico.withoutnet.adapters.NodeFieldsListAdapter;
import pt.ulisboa.tecnico.withoutnet.databinding.ActivityNodeDetailsBinding;
import pt.ulisboa.tecnico.withoutnet.models.Network;
import pt.ulisboa.tecnico.withoutnet.models.Node;
import pt.ulisboa.tecnico.withoutnet.network.StatusCodes;

public class NodeDetailsActivity extends AppCompatActivity {
    private static final String TAG = "NodeDetailsActivity";

    private ActivityNodeDetailsBinding binding;
    private GlobalClass globalClass;
    private NodeFieldsListAdapter nodeFieldsListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityNodeDetailsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        globalClass = (GlobalClass) getApplicationContext();

        Node node = (Node) getIntent().getSerializableExtra("node");

        if (node == null) {
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
                if (result.getResultCode() == RESULT_OK) {
                    Intent data = result.getData();

                    if (!data.hasExtra("new-node-name")) {
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
                if (result.getResultCode() == RESULT_OK) {
                    Intent data = result.getData();

                    if (!data.hasExtra("new-node-network")) {
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

        binding.deleteNodeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(NodeDetailsActivity.this);

                builder.setMessage(R.string.delete_node_warning_message)
                        .setTitle(R.string.delete_node)
                        .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                Frontend.FrontendResponseListener responseListener = new Frontend.FrontendResponseListener() {
                                    @Override
                                    public void onResponse(Object response) {
                                        int responseCode = (int) response;
                                        if(responseCode == StatusCodes.OK) {
                                            Toast.makeText(NodeDetailsActivity.this
                                                    , getResources().getText(R.string.deletion_success_message) + node.getCommonName()
                                                    , Toast.LENGTH_SHORT)
                                                    .show();
                                            finish();
                                        } else {
                                            Toast.makeText(NodeDetailsActivity.this
                                                            , getResources().getText(R.string.deletion_failure_message) + node.getCommonName()
                                                            , Toast.LENGTH_SHORT)
                                                    .show();
                                        }
                                    }

                                    @Override
                                    public void onError(String errorMessage) {
                                        Toast.makeText(NodeDetailsActivity.this
                                                        , getResources().getText(R.string.deletion_failure_message) + node.getCommonName()
                                                        , Toast.LENGTH_SHORT)
                                                .show();
                                    }
                                };

                                globalClass.getFrontend().deleteNode(node, responseListener);
                            }
                        })
                        .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.dismiss();
                            }
                        });

                AlertDialog dialog = builder.create();

                dialog.show();
            }
        });
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