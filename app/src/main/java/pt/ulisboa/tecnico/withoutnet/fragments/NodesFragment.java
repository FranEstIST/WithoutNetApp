package pt.ulisboa.tecnico.withoutnet.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import pt.ulisboa.tecnico.withoutnet.R;
import pt.ulisboa.tecnico.withoutnet.activities.Main.MainActivity;
import pt.ulisboa.tecnico.withoutnet.adapters.NodesListAdapter;
import pt.ulisboa.tecnico.withoutnet.databinding.FragmentNetworksBinding;
import pt.ulisboa.tecnico.withoutnet.databinding.FragmentNodesBinding;
import pt.ulisboa.tecnico.withoutnet.models.Network;
import pt.ulisboa.tecnico.withoutnet.models.Node;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link NodesFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class NodesFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private FragmentNodesBinding binding;

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
            appBarTitleTextView.setText(R.string.networks);
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

        NodesListAdapter nodesListAdapter = new NodesListAdapter(nodes, new NodesListAdapter.OnNodeClickListener() {
            @Override
            public void onNodeClick(int position) {
                Toast.makeText(NodesFragment.this.getContext(), "Clicked on node " + position, Toast.LENGTH_SHORT).show();
            }
        });

        binding.nodesListRecyclerView.setAdapter(nodesListAdapter);
        binding.nodesListRecyclerView.addItemDecoration(new DividerItemDecoration(this.getContext(),
                DividerItemDecoration.VERTICAL));

        binding.nodesSearchTextView.setVisibility(View.GONE);
        binding.nodesListRecyclerView.setVisibility(View.VISIBLE);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.menu_search, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }
}