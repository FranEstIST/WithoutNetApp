package pt.ulisboa.tecnico.withoutnet.fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;

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

import pt.ulisboa.tecnico.withoutnet.Frontend;
import pt.ulisboa.tecnico.withoutnet.GlobalClass;
import pt.ulisboa.tecnico.withoutnet.R;
import pt.ulisboa.tecnico.withoutnet.activities.Networks.CreateNewNetworkPopUpActivity;
import pt.ulisboa.tecnico.withoutnet.activities.Main.MainActivity;
import pt.ulisboa.tecnico.withoutnet.activities.Nodes.NodesListActivity;
import pt.ulisboa.tecnico.withoutnet.adapters.NetworksListAdapter;
import pt.ulisboa.tecnico.withoutnet.databinding.FragmentNetworksBinding;
import pt.ulisboa.tecnico.withoutnet.models.Network;
import pt.ulisboa.tecnico.withoutnet.models.Node;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link NetworksFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class NetworksFragment extends Fragment {
    private static final String TAG = "NetworksFragment";

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private GlobalClass globalClass;

    private FragmentNetworksBinding binding;

    private NetworksListAdapter networksListAdapter;

    public NetworksFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment NetworksFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static NetworksFragment newInstance(String param1, String param2) {
        NetworksFragment fragment = new NetworksFragment();
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
        binding = FragmentNetworksBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        MainActivity  mainActivity = (MainActivity) getActivity();

        if(mainActivity != null && mainActivity.binding != null) {
            TextView appBarTitleTextView = mainActivity.binding.appBarTitle;
            appBarTitleTextView.setText(R.string.networks);
        }

        setHasOptionsMenu(true);

        globalClass = (GlobalClass) getActivity().getApplicationContext();

        binding.createNetworkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(NetworksFragment.this.getActivity(), CreateNewNetworkPopUpActivity.class);
                startActivity(intent);
            }
        });

        Network networkOne = new Network(1, "NetworkOne", new ArrayList<>());
        Network networkTwo = new Network(2, "NetworkTwo", new ArrayList<>());
        Network networkThree = new Network(3, "NetworkThree", new ArrayList<>());

        ArrayList<Network> networks = new ArrayList<>();

        networks.add(networkOne);
        networks.add(networkTwo);
        networks.add(networkThree);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this.getContext());
        binding.networksListRecyclerView.setLayoutManager(linearLayoutManager);

        NetworksListAdapter.OnNetworkClickListener onNetworkClickListener = new NetworksListAdapter.OnNetworkClickListener() {
            @Override
            public void onNetworkClick(Network network) {
                Toast.makeText(NetworksFragment.this.getActivity(), "Clicked on network " + network.getName(), Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(NetworksFragment.this.getActivity(), NodesListActivity.class);
                intent.putExtra("network-id", network.getId());
                intent.putExtra("network-name", network.getName());
                startActivity(intent);
            }
        };

        networksListAdapter = new NetworksListAdapter(networks, onNetworkClickListener, true);
        binding.networksListRecyclerView.setAdapter(networksListAdapter);

        binding.networksListRecyclerView.addItemDecoration(new DividerItemDecoration(this.getContext(),
                DividerItemDecoration.VERTICAL));

        binding.networksListRecyclerView.setVisibility(View.VISIBLE);
        /*binding.networkSearchTextView.setVisibility(View.GONE);*/
    }

    /*private void filterNetworks(String query) {
        Filter.FilterListener filterListener = new Filter.FilterListener() {
            @Override
            public void onFilterComplete(int count) {
                if(count == 0) {
                    if(query.equals("")) {
                        binding.networkSearchTextView.setText(R.string.search_for_a_network_to_view_it);
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
            binding.networkSearchTextView.setText(R.string.search_for_a_node_to_view_it);
            binding.networkSearchTextView.setVisibility(View.VISIBLE);
            binding.networksListRecyclerView.setVisibility(View.GONE);
            return;
        }

        Frontend.FrontendResponseListener responseListener = new Frontend.FrontendResponseListener() {
            @Override
            public void onResponse(Object response) {
                if(response == null) {
                    Toast.makeText(NetworksFragment.this.getContext(), "No internet connection", Toast.LENGTH_SHORT).show();
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
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.menu_search, menu);

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

        super.onCreateOptionsMenu(menu, inflater);
    }
}