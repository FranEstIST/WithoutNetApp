package pt.ulisboa.tecnico.withoutnet.activities.Networks;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;

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
import pt.ulisboa.tecnico.withoutnet.activities.Nodes.ChangeNodeFieldValuePopUpActivity;
import pt.ulisboa.tecnico.withoutnet.adapters.NetworksListAdapter;
import pt.ulisboa.tecnico.withoutnet.databinding.ActivityChangeNodeNetworkBinding;
import pt.ulisboa.tecnico.withoutnet.fragments.NetworksFragment;
import pt.ulisboa.tecnico.withoutnet.models.Network;
import pt.ulisboa.tecnico.withoutnet.models.Node;

public class ChangeNodeNetworkActivity extends AppCompatActivity {
    private static final String TAG = "ChangeNodeNetworkActivity";

    private GlobalClass globalClass;

    private ActivityChangeNodeNetworkBinding binding;

    private Node node;

    private NetworksListAdapter networksListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        globalClass = (GlobalClass) getApplicationContext();

        node = (Node) getIntent().getSerializableExtra("node");

        binding = ActivityChangeNodeNetworkBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        binding.appBarTitle.setText(R.string.change_nodes_network);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        binding.networksListRecyclerView.setLayoutManager(linearLayoutManager);

        Network networkOne = new Network(1, "NetworkOne", new ArrayList<>());
        Network networkTwo = new Network(2, "NetworkTwo", new ArrayList<>());
        Network networkThree = new Network(3, "NetworkThree", new ArrayList<>());

        ArrayList<Network> networks = new ArrayList<>();

        networks.add(networkOne);
        networks.add(networkTwo);
        networks.add(networkThree);

        NetworksListAdapter.OnNetworkClickListener onNetworkClickListener = new NetworksListAdapter.OnNetworkClickListener() {
            @Override
            public void onNetworkClick(Network network) {
                node.setNetwork(network);

                Frontend.FrontendResponseListener responseListener = new Frontend.FrontendResponseListener() {
                    @Override
                    public void onResponse(Object response) {
                        if(response != null) {
                            Node updatedNode = (Node) response;

                            Intent resultIntent = new Intent();
                            resultIntent.putExtra("new-node-network", updatedNode.getNetwork());

                            setResult(RESULT_OK, resultIntent);
                            finish();
                        } else {
                            Toast.makeText(ChangeNodeNetworkActivity.this, "No Internet connection", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onError(String errorMessage) {
                        Toast.makeText(ChangeNodeNetworkActivity.this, "An error occurred", Toast.LENGTH_SHORT).show();
                        Log.e(TAG, errorMessage);
                    }
                };

                globalClass.getFrontend().updateNode(node, responseListener);
            }
        };

        networksListAdapter = new NetworksListAdapter(networks, onNetworkClickListener, true);
        binding.networksListRecyclerView.setAdapter(networksListAdapter);

        binding.networksListRecyclerView.addItemDecoration(new DividerItemDecoration(this,
                DividerItemDecoration.VERTICAL));

        binding.createNewNetworkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ChangeNodeNetworkActivity.this, CreateNewNetworkPopUpActivity.class);
                startActivity(intent);
            }
        });
    }

    /*private void filterNetworks(String query) {
        Filter.FilterListener filterListener = new Filter.FilterListener() {
            @Override
            public void onFilterComplete(int count) {
                if(count == 0) {
                    if(query.equals("")) {
                        binding.networkSearchTextView.setText(R.string.search_for_a_network_to_add_it);
                    } else {
                        binding.networkSearchTextView.setText(R.string.no_network_found);
                    }
                    binding.networkSearchTextView.setVisibility(View.VISIBLE);
                } else {
                    binding.networkSearchTextView.setVisibility(View.GONE);
                }
            }
        };

        networksListAdapter.getFilter().filter(query, filterListener);
    }*/

    private void filterNetworks(String query) {
        if(query.equals("")) {
            binding.networkSearchTextView.setText(R.string.search_for_a_network_to_view_it);
            binding.networkSearchTextView.setVisibility(View.VISIBLE);
            binding.networksListRecyclerView.setVisibility(View.GONE);
            return;
        }

        Frontend.FrontendResponseListener responseListener = new Frontend.FrontendResponseListener() {
            @Override
            public void onResponse(Object response) {
                if(response == null) {
                    Toast.makeText(ChangeNodeNetworkActivity.this, "No internet connection", Toast.LENGTH_SHORT).show();
                    return;
                }

                ArrayList<Network> filteredNetworks = (ArrayList<Network>) response;
                networksListAdapter.setFilteredNetworks(filteredNetworks);

                if(networksListAdapter.getItemCount() == 0) {
                    binding.networkSearchTextView.setText(R.string.no_nodes_found);
                    binding.networkSearchTextView.setVisibility(View.VISIBLE);
                    binding.networksListRecyclerView.setVisibility(View.GONE);
                } else {
                    binding.networkSearchTextView.setVisibility(View.GONE);
                    binding.networksListRecyclerView.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onError(String errorMessage) {
                Log.e(TAG, errorMessage);
            }
        };

        globalClass.getFrontend().getNetworksInServerContainingSubstring(query, responseListener);
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

                filterNetworks(query);

                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                Log.d(TAG, "Entered: " + newText);

                filterNetworks(newText);

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