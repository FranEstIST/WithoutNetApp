package pt.ulisboa.tecnico.withoutnet.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.content.Intent;
import android.os.Bundle;

import pt.ulisboa.tecnico.withoutnet.R;
import pt.ulisboa.tecnico.withoutnet.databinding.ActivityAddOrCreateNewNodePopUpBinding;
import pt.ulisboa.tecnico.withoutnet.fragments.AddOrCreateNewNodeFragment;
import pt.ulisboa.tecnico.withoutnet.fragments.CreateNewNodeFragment;

public class AddOrCreateNewNodePopUpActivity extends AppCompatActivity implements AddOrCreateNewNodeFragment.OnAddOrCreateNewNodeClickListener {
    private ActivityAddOrCreateNewNodePopUpBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityAddOrCreateNewNodePopUpBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.fragmentContainerView.getId();
    }

    @Override
    public void onAddNodeClick() {
        /*Intent intent = new Intent(this, SearchActivity.class);
        intent.putExtra("mode", mode);

        if(mode == ViewModes.DEVICE_MODE) {
            intent.putExtra("deviceTypeString", deviceTypeString);
        }

        startActivity(intent);

        finish();*/
    }

    @Override
    public void onCreateNewNodeClick() {
        Fragment createNewNodeFragment;

        createNewNodeFragment = CreateNewNodeFragment.newInstance();

        getSupportFragmentManager()
                .beginTransaction()
                .replace(binding.fragmentContainerView.getId(), createNewNodeFragment)
                .commit();
    }
}