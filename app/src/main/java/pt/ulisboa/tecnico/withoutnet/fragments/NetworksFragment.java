package pt.ulisboa.tecnico.withoutnet.fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import pt.ulisboa.tecnico.withoutnet.R;
import pt.ulisboa.tecnico.withoutnet.activities.CreateNewNetworkPopUpActivity;
import pt.ulisboa.tecnico.withoutnet.activities.Main.MainActivity;
import pt.ulisboa.tecnico.withoutnet.databinding.FragmentNetworksBinding;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link NetworksFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class NetworksFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private FragmentNetworksBinding binding;

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

        binding.createNetworkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(NetworksFragment.this.getActivity(), CreateNewNetworkPopUpActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.menu_search, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }
}