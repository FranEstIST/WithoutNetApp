package pt.ulisboa.tecnico.withoutnet;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

import pt.ulisboa.tecnico.withoutnet.databinding.ActivityCachedUpdatesBinding;
import pt.ulisboa.tecnico.withoutnet.databinding.ActivityDebugNodeBinding;

public class CachedUpdatesActivity extends AppCompatActivity {
    private static final String TAG = "CachedUpdatesActivity";

    private RecyclerView cachedUpdatesRV;
    private CachedUpdatesAdapter cachedUpdatesAdapter;
    private GlobalClass globalClass;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivityCachedUpdatesBinding binding = ActivityCachedUpdatesBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        globalClass = (GlobalClass) getApplicationContext();

        cachedUpdatesRV = binding.cachedUpdatesList;

        ArrayList<Update> cachedUpdates = new ArrayList<>();

        for(TreeSet<Update> updateSet : globalClass.getAllUpdates().values()) {
            cachedUpdates.addAll(updateSet);
        }

        CachedUpdatesAdapter.OnCachedUpdateListener onCachedUpdateListener = new CachedUpdatesAdapter.OnCachedUpdateListener() {
            @Override
            public void onCachedUpdateClick(int position) {
                // Empty for now
            }
        };

        cachedUpdatesAdapter = new CachedUpdatesAdapter(cachedUpdates, onCachedUpdateListener);

        cachedUpdatesRV.setAdapter(cachedUpdatesAdapter);

        cachedUpdatesRV.setLayoutManager(new LinearLayoutManager(this));

        cachedUpdatesRV.addItemDecoration(new DividerItemDecoration(this,
                DividerItemDecoration.VERTICAL));
    }
}