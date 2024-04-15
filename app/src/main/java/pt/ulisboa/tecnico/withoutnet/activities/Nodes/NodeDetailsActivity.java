package pt.ulisboa.tecnico.withoutnet.activities.Nodes;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.MenuItem;

import pt.ulisboa.tecnico.withoutnet.R;
import pt.ulisboa.tecnico.withoutnet.adapters.NodeFieldsListAdapter;
import pt.ulisboa.tecnico.withoutnet.databinding.ActivityNodeDetailsBinding;
import pt.ulisboa.tecnico.withoutnet.models.Node;

public class NodeDetailsActivity extends AppCompatActivity {
    private static final String TAG = "NodeDetailsActivity";

    private ActivityNodeDetailsBinding binding;

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

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        binding.nodeFieldsListRecyclerView.setLayoutManager(layoutManager);

        binding.nodeFieldsListRecyclerView.addItemDecoration(new DividerItemDecoration(this,
                DividerItemDecoration.VERTICAL));

        NodeFieldsListAdapter nodeFieldsListAdapter = new NodeFieldsListAdapter(node, this);
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