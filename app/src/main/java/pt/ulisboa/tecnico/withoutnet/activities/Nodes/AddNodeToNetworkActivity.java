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

import java.util.ArrayList;

import pt.ulisboa.tecnico.withoutnet.R;
import pt.ulisboa.tecnico.withoutnet.adapters.NodesListAdapter;
import pt.ulisboa.tecnico.withoutnet.databinding.ActivityAddNodeToNetworkBinding;
import pt.ulisboa.tecnico.withoutnet.models.Network;
import pt.ulisboa.tecnico.withoutnet.models.Node;

public class AddNodeToNetworkActivity extends AppCompatActivity {
    private static final String TAG = "AddNodeToNetworkActivity";

    private ActivityAddNodeToNetworkBinding binding;
    private NodesListAdapter nodesListAdapter;

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

        Network network = new Network(1, "Alameda Garden 1");

        Node nodeOne = new Node("1", "nodeOne", "TEMP", network);
        Node nodeTwo = new Node("2", "nodeTwo", "TEMP", network);
        Node nodeThree = new Node("3", "nodeThree", "TEMP", network);

        ArrayList<Node> nodes = new ArrayList<>();

        nodes.add(nodeOne);
        nodes.add(nodeTwo);
        nodes.add(nodeThree);

        NodesListAdapter.OnNodeClickListener onNodeClickListener = new NodesListAdapter.OnNodeClickListener() {
            @Override
            public void onNodeClick(Node clickedNode) {
                // TODO
            }
        };

        nodesListAdapter = new NodesListAdapter(nodes, onNodeClickListener, true);

        binding.nodesListRecyclerView.setAdapter(nodesListAdapter);

        binding.nodesListRecyclerView.addItemDecoration(new DividerItemDecoration(this,
                DividerItemDecoration.VERTICAL));

        binding.createNewNodeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AddNodeToNetworkActivity.this, CreateNewNodePopUpActivity.class);
                startActivity(intent);
            }
        });
    }

    private void filterNodes(String query) {
        Filter.FilterListener filterListener = new Filter.FilterListener() {
            @Override
            public void onFilterComplete(int count) {
                if(query.equals("")) {
                    binding.nodesSearchTextView.setText(R.string.search_for_a_node_to_add_it);
                    binding.nodesSearchTextView.setVisibility(View.VISIBLE);
                } else if(nodesListAdapter.getItemCount() == 0) {
                    binding.nodesSearchTextView.setText(R.string.no_nodes_found);
                    binding.nodesSearchTextView.setVisibility(View.VISIBLE);
                } else {
                    binding.nodesSearchTextView.setVisibility(View.INVISIBLE);
                }

                Log.d(TAG, "Finished filtering");
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