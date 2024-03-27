package pt.ulisboa.tecnico.withoutnet.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import java.util.ArrayList;

import pt.ulisboa.tecnico.withoutnet.R;
import pt.ulisboa.tecnico.withoutnet.adapters.NodesListAdapter;
import pt.ulisboa.tecnico.withoutnet.databinding.ActivityNodesListBinding;
import pt.ulisboa.tecnico.withoutnet.models.Network;
import pt.ulisboa.tecnico.withoutnet.models.Node;

public class NodesListActivity extends AppCompatActivity {

    private ActivityNodesListBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nodes_list);

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

        binding.appBarTitle.setText(networkName +"'s Nodes");

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        binding.networkNodesListRecyclerView.setLayoutManager(layoutManager);

        NodesListAdapter.OnNodeClickListener onNodeClickListener = new NodesListAdapter.OnNodeClickListener() {
            @Override
            public void onNodeClick(int position) {

            }
        };

        ArrayList<Node> nodes = new ArrayList<>();

        Network network = new Network(networkId, networkName);

        Node nodeOne = new Node("1", "nodeOne", "TEMP", network);
        Node nodeTwo = new Node("2", "nodeTwo", "TEMP", network);
        Node nodeThree = new Node("3", "nodeThree", "TEMP", network);

        nodes.add(nodeOne);
        nodes.add(nodeTwo);
        nodes.add(nodeThree);

        NodesListAdapter nodesListAdapter = new NodesListAdapter(nodes, onNodeClickListener);

        binding.networkNodesListRecyclerView.setAdapter(nodesListAdapter);
        binding.networkNodesListRecyclerView.addItemDecoration(new DividerItemDecoration(this,
                DividerItemDecoration.VERTICAL));

        binding.nodesInNetworkSearchTextView.setVisibility(View.GONE);
        binding.networkNodesListRecyclerView.setVisibility(View.VISIBLE);
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