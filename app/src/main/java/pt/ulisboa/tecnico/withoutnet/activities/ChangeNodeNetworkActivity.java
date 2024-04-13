package pt.ulisboa.tecnico.withoutnet.activities;

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

import java.util.ArrayList;

import pt.ulisboa.tecnico.withoutnet.R;
import pt.ulisboa.tecnico.withoutnet.adapters.NetworksListAdapter;
import pt.ulisboa.tecnico.withoutnet.databinding.ActivityChangeNodeNetworkBinding;
import pt.ulisboa.tecnico.withoutnet.models.Network;

public class ChangeNodeNetworkActivity extends AppCompatActivity {
    private static final String TAG = "ChangeNodeNetworkActivity";

    private ActivityChangeNodeNetworkBinding binding;

    private NetworksListAdapter networksListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityChangeNodeNetworkBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

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

    private void filterNetworks(String query) {
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
                //Toast.makeText(SearchActivity.this.getBaseContext(), "Entered: " + newText, Toast.LENGTH_SHORT).show();
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