package pt.ulisboa.tecnico.withoutnet.fragments;

import static android.app.Activity.RESULT_OK;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import pt.ulisboa.tecnico.withoutnet.Frontend;
import pt.ulisboa.tecnico.withoutnet.GlobalClass;
import pt.ulisboa.tecnico.withoutnet.R;
import pt.ulisboa.tecnico.withoutnet.activities.Main.MainActivity;
import pt.ulisboa.tecnico.withoutnet.activities.Nodes.AddNodeToNetworkActivity;
import pt.ulisboa.tecnico.withoutnet.activities.Nodes.CreateNewNodePopUpActivity;
import pt.ulisboa.tecnico.withoutnet.activities.Nodes.NodeDetailsActivity;
import pt.ulisboa.tecnico.withoutnet.adapters.NodesListAdapter;
import pt.ulisboa.tecnico.withoutnet.constants.ErrorMessages;
import pt.ulisboa.tecnico.withoutnet.databinding.FragmentNodesBinding;
import pt.ulisboa.tecnico.withoutnet.models.Network;
import pt.ulisboa.tecnico.withoutnet.models.Node;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link NodesFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class NodesFragment extends Fragment {
    private static final String TAG = "NodesFragment";

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private GlobalClass globalClass;

    private FragmentNodesBinding binding;

    private SearchView searchView;

    private NodesListAdapter nodesListAdapter;

    public NodesFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment NodesFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static NodesFragment newInstance(String param1, String param2) {
        NodesFragment fragment = new NodesFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentNodesBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        MainActivity mainActivity = (MainActivity) getActivity();

        if(mainActivity != null && mainActivity.binding != null) {
            TextView appBarTitleTextView = mainActivity.binding.appBarTitle;
            appBarTitleTextView.setText(R.string.nodes);
            globalClass = (GlobalClass) getActivity().getApplicationContext();
        }

        setHasOptionsMenu(true);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this.getContext());
        binding.nodesListRecyclerView.setLayoutManager(layoutManager);

        Network network = new Network(1, "Alameda Garden 1");

        Node nodeOne = new Node("1", "nodeOne", "TEMP", network);
        Node nodeTwo = new Node("2", "nodeTwo", "TEMP", network);
        Node nodeThree = new Node("3", "nodeThree", "TEMP", network);

        ArrayList<Node> nodes = new ArrayList<>();

        nodes.add(nodeOne);
        nodes.add(nodeTwo);
        nodes.add(nodeThree);

        nodesListAdapter = new NodesListAdapter(nodes, new NodesListAdapter.OnNodeClickListener() {
            @Override
            public void onNodeClick(Node clickedNode) {
                Toast.makeText(NodesFragment.this.getContext(), "Clicked on node " + clickedNode.getCommonName(), Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(NodesFragment.this.getActivity(), NodeDetailsActivity.class);
                intent.putExtra("node", clickedNode);
                startActivity(intent);
            }
        }, true);

        //getNodesFromServer();

        binding.nodesListRecyclerView.setAdapter(nodesListAdapter);
        binding.nodesListRecyclerView.addItemDecoration(new DividerItemDecoration(this.getContext(),
                DividerItemDecoration.VERTICAL));

        //binding.nodesSearchTextView.setVisibility(View.GONE);
        binding.nodesListRecyclerView.setVisibility(View.VISIBLE);

        ActivityResultLauncher<Intent> createNewNodeResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {
                refreshNodesList();

                if(result.getResultCode() == RESULT_OK) {
                    Intent returnIntent = result.getData();

                    if(!returnIntent.hasExtra("added-node")) {
                        Toast.makeText(NodesFragment.this.getActivity()
                                        , ErrorMessages.ERROR_CREATING_NODE
                                        , Toast.LENGTH_SHORT)
                                .show();
                    }

                } else {
                    Toast.makeText(NodesFragment.this.getActivity()
                                    , ErrorMessages.ERROR_CREATING_NODE
                                    , Toast.LENGTH_SHORT)
                            .show();
                }
            }
        });

        binding.createNodeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(NodesFragment.this.getActivity(), CreateNewNodePopUpActivity.class);
                //startActivity(intent);
                createNewNodeResultLauncher.launch(intent);
            }
        });
    }

    private void refreshNodesList() {
        String query = "";

        if(searchView != null) {
            CharSequence currentQueryCharSeq = searchView.getQuery();

            if (currentQueryCharSeq != null) {
                query = currentQueryCharSeq.toString();
            }
        }

        filterNodes(query);
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshNodesList();
    }

    private void getNodesFromServer() {
        Frontend.FrontendResponseListener responseListener = new Frontend.FrontendResponseListener() {
            @Override
            public void onResponse(Object response) {
                ArrayList<Node> receivedNodes = (ArrayList<Node>) response;
                nodesListAdapter.setNodes(receivedNodes);
            }

            @Override
            public void onError(String errorMessage) {
                Log.e(TAG, errorMessage);
            }
        };

        if(globalClass != null) {
            globalClass.getFrontend().getAllNodesInServer(responseListener);
        }
    }

    /*private void filterNodes(String query) {
        Filter.FilterListener filterListener = new Filter.FilterListener() {
            @Override
            public void onFilterComplete(int count) {
                if(query.equals("")) {
                    binding.nodesSearchTextView.setText(R.string.search_for_a_node_to_view_it);
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
    }*/

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
                    Toast.makeText(NodesFragment.this.getContext(), "No internet connection", Toast.LENGTH_SHORT).show();
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
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.menu_search, menu);

        MenuItem menuSearchItem = menu.findItem(R.id.action_search);
        searchView = (SearchView) menuSearchItem.getActionView();

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

        super.onCreateOptionsMenu(menu, inflater);
    }
}