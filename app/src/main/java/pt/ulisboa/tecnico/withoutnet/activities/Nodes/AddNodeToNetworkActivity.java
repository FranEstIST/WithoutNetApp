package pt.ulisboa.tecnico.withoutnet.activities.Nodes;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Filter;
import android.widget.Toast;

import java.util.ArrayList;

import pt.ulisboa.tecnico.withoutnet.Frontend;
import pt.ulisboa.tecnico.withoutnet.GlobalClass;
import pt.ulisboa.tecnico.withoutnet.R;
import pt.ulisboa.tecnico.withoutnet.activities.Networks.ChangeNodeNetworkActivity;
import pt.ulisboa.tecnico.withoutnet.adapters.NodesListAdapter;
import pt.ulisboa.tecnico.withoutnet.constants.ErrorMessages;
import pt.ulisboa.tecnico.withoutnet.databinding.ActivityAddNodeToNetworkBinding;
import pt.ulisboa.tecnico.withoutnet.fragments.NodesFragment;
import pt.ulisboa.tecnico.withoutnet.models.Network;
import pt.ulisboa.tecnico.withoutnet.models.Node;

public class AddNodeToNetworkActivity extends AppCompatActivity {
    private static final String TAG = "AddNodeToNetworkActivity";

    private GlobalClass globalClass;

    private ActivityAddNodeToNetworkBinding binding;
    private NodesListAdapter nodesListAdapter;

    private Network network;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityAddNodeToNetworkBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        binding.appBarTitle.setText(R.string.add_a_node);
        binding.nodesSearchTextView.setText(R.string.search_for_a_node_to_add_it);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        binding.nodesListRecyclerView.setLayoutManager(layoutManager);

        //Network network = new Network(1, "Alameda Garden 1");

        /*Node nodeOne = new Node("1", "nodeOne", "TEMP", network);
        Node nodeTwo = new Node("2", "nodeTwo", "TEMP", network);
        Node nodeThree = new Node("3", "nodeThree", "TEMP", network);*/

        ArrayList<Node> nodes = new ArrayList<>();

        /*nodes.add(nodeOne);
        nodes.add(nodeTwo);
        nodes.add(nodeThree);*/

        NodesListAdapter.OnNodeClickListener onNodeClickListener = new NodesListAdapter.OnNodeClickListener() {
            @Override
            public void onNodeClick(Node clickedNode) {
                clickedNode.setNetwork(network);

                Frontend.FrontendResponseListener responseListener = new Frontend.FrontendResponseListener() {
                    @Override
                    public void onResponse(Object response) {
                        if(response != null) {
                            Node addedNode = (Node) response;

                            Intent returnIntent = new Intent();
                            returnIntent.putExtra("added-node", addedNode);

                            AddNodeToNetworkActivity.this.setResult(RESULT_OK, returnIntent);
                            AddNodeToNetworkActivity.this.finish();
                        } else {
                            Toast.makeText(AddNodeToNetworkActivity.this
                                    , ErrorMessages.NO_INTERNET_CONNECTION
                                    , Toast.LENGTH_SHORT)
                                    .show();
                        }
                    }

                    @Override
                    public void onError(String errorMessage) {
                        Toast.makeText(AddNodeToNetworkActivity.this
                                , ErrorMessages.AN_ERROR_OCCURRED
                                , Toast.LENGTH_SHORT)
                                .show();
                        Log.e(TAG, errorMessage);
                    }
                };

                globalClass.getFrontend().updateNode(clickedNode, responseListener);
            }
        };

        nodesListAdapter = new NodesListAdapter(nodes, onNodeClickListener, true);

        binding.nodesListRecyclerView.setAdapter(nodesListAdapter);

        binding.nodesListRecyclerView.addItemDecoration(new DividerItemDecoration(this,
                DividerItemDecoration.VERTICAL));

        ActivityResultLauncher<Intent> createNewNodeResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {
                if(result.getResultCode() != RESULT_OK) {
                    /*Toast.makeText(AddNodeToNetworkActivity.this
                                    , ErrorMessages.ERROR_CREATING_NODE
                                    , Toast.LENGTH_SHORT)
                            .show();*/
                }

                Intent returnIntent = result.getData();

                AddNodeToNetworkActivity.this.setResult(RESULT_OK, returnIntent);
                AddNodeToNetworkActivity.this.finish();
            }
        });

        binding.createNewNodeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AddNodeToNetworkActivity.this, CreateNewNodePopUpActivity.class);
                intent.putExtra("network", network);
                createNewNodeResultLauncher.launch(intent);
            }
        });

        network = (Network) getIntent().getSerializableExtra("network");

        globalClass = (GlobalClass) getApplicationContext();
    }

    private void filterNodes(String query) {
        if(query.equals("")) {
            binding.nodesSearchTextView.setText(R.string.search_for_a_node_to_view_it);
            binding.nodesSearchTextView.setVisibility(View.VISIBLE);
            binding.nodesListRecyclerView.setVisibility(View.GONE);
            return;
        }

        Frontend.FrontendResponseListener responseListener = new Frontend.FrontendResponseListener() {
            @Override
            public void onResponse(Object response) {
                if(response == null) {
                    Toast.makeText(AddNodeToNetworkActivity.this, "No internet connection", Toast.LENGTH_SHORT).show();
                    return;
                }

                ArrayList<Node> filteredNodes = (ArrayList<Node>) response;
                nodesListAdapter.setFilteredNodes(filteredNodes);

                if(nodesListAdapter.getItemCount() == 0) {
                    binding.nodesSearchTextView.setText(R.string.no_nodes_found);
                    binding.nodesSearchTextView.setVisibility(View.VISIBLE);
                    binding.nodesListRecyclerView.setVisibility(View.GONE);
                } else {
                    binding.nodesSearchTextView.setVisibility(View.GONE);
                    binding.nodesListRecyclerView.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onError(String errorMessage) {
                Log.e(TAG, errorMessage);
            }
        };

        globalClass.getFrontend().getNodesInServerContainingSubstring(query, responseListener);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_search, menu);

        MenuItem menuSearchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) menuSearchItem.getActionView();

        //searchView.setQueryHint("...");

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                Log.d(TAG, "Entered: " + query);

                filterNodes(query);

                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                //Toast.makeText(SearchActivity.this.getBaseContext(), "Entered: " + newText, Toast.LENGTH_SHORT).show();
                Log.d(TAG, "Entered: " + newText);

                filterNodes(newText);

                return true;
            }
        });
        //menu.add
        return super.onCreateOptionsMenu(menu);
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