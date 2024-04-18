package pt.ulisboa.tecnico.withoutnet.activities.Nodes;

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
import pt.ulisboa.tecnico.withoutnet.adapters.NodesListAdapter;
import pt.ulisboa.tecnico.withoutnet.databinding.ActivityNodesListBinding;
import pt.ulisboa.tecnico.withoutnet.models.Network;
import pt.ulisboa.tecnico.withoutnet.models.Node;

public class NodesListActivity extends AppCompatActivity {
    private static final String TAG = "NodesListActivity";

    private GlobalClass globalClass;

    private ActivityNodesListBinding binding;
    private NodesListAdapter nodesListAdapter;

    private Network network;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityNodesListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Intent receivedIntent = getIntent();
        int networkId = receivedIntent.getIntExtra("network-id", -1);
        String networkName = receivedIntent.getStringExtra("network-name");

        if(networkId == -1) {
            return;
        } else if(networkName == null) {
            return;
        }

        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        binding.appBarTitle.setText(networkName +"'s nodes");

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        binding.networkNodesListRecyclerView.setLayoutManager(layoutManager);

        NodesListAdapter.OnNodeClickListener onNodeClickListener = new NodesListAdapter.OnNodeClickListener() {
            @Override
            public void onNodeClick(Node clickedNode) {
                Intent intent = new Intent(NodesListActivity.this, NodeDetailsActivity.class);
                intent.putExtra("node", clickedNode);
                startActivity(intent);
            }
        };

        ArrayList<Node> nodes = new ArrayList<>();

        network = new Network(networkId, networkName);

        /*Node nodeOne = new Node("1", "nodeOne", "TEMP", network);
        Node nodeTwo = new Node("2", "nodeTwo", "TEMP", network);
        Node nodeThree = new Node("3", "nodeThree", "TEMP", network);

        nodes.add(nodeOne);
        nodes.add(nodeTwo);
        nodes.add(nodeThree);*/

        nodesListAdapter = new NodesListAdapter(nodes, onNodeClickListener, true);

        binding.networkNodesListRecyclerView.setAdapter(nodesListAdapter);
        binding.networkNodesListRecyclerView.addItemDecoration(new DividerItemDecoration(this,
                DividerItemDecoration.VERTICAL));

        binding.nodesInNetworkSearchTextView.setVisibility(View.GONE);
        binding.networkNodesListRecyclerView.setVisibility(View.VISIBLE);

        binding.addNodeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(NodesListActivity.this, AddNodeToNetworkActivity.class);
                intent.putExtra("network", network);
                startActivity(intent);
            }
        });

        globalClass = (GlobalClass) getApplicationContext();

        getNodesFromServer();
    }

    private void updateActivityView() {
        Log.d(TAG, "Nodes in list: " + nodesListAdapter.getItemCount());
        if(nodesListAdapter.getItemCount() == 0) {
            binding.nodesInNetworkSearchTextView.setText(R.string.no_nodes_found);
            binding.nodesInNetworkSearchTextView.setVisibility(View.VISIBLE);
            binding.networkNodesListRecyclerView.setVisibility(View.GONE);
        } else {
            binding.nodesInNetworkSearchTextView.setVisibility(View.GONE);
            binding.networkNodesListRecyclerView.setVisibility(View.VISIBLE);
        }
    }

    private void getNodesFromServer() {
        Frontend.FrontendResponseListener responseListener = new Frontend.FrontendResponseListener() {
            @Override
            public void onResponse(Object response) {
                ArrayList<Node> receivedNodes = (ArrayList<Node>) response;
                nodesListAdapter.setNodes(receivedNodes);
                filterNodes("");
            }

            @Override
            public void onError(String errorMessage) {
                Log.e(TAG, errorMessage);
            }
        };

        globalClass.getFrontend().getNodesInNetwork(network, responseListener);
    }

    private void filterNodes(String query) {
        Filter.FilterListener filterListener = new Filter.FilterListener() {
            @Override
            public void onFilterComplete(int count) {
                updateActivityView();
            }
        };

        nodesListAdapter.getFilter().filter(query, filterListener);
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
                Log.d(TAG, "Entered: " + newText);

                filterNodes(newText);

                return true;
            }
        });

        /*searchView.setOnSearchClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nodesListAdapter.setShouldOnlyDiplayFilteredNodes(true);
            }
        });

        searchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                nodesListAdapter.setShouldOnlyDiplayFilteredNodes(true);
                return true
            }
        });

        menuSearchItem.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                nodesListAdapter.setShouldOnlyDiplayFilteredNodes(true);
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                nodesListAdapter.setShouldOnlyDiplayFilteredNodes(false);
                return true;
            }
        });*/

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