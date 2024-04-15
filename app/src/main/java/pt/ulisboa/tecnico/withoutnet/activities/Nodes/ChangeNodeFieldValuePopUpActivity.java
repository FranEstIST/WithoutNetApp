package pt.ulisboa.tecnico.withoutnet.activities.Nodes;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;

import pt.ulisboa.tecnico.withoutnet.R;
import pt.ulisboa.tecnico.withoutnet.databinding.ActivityChangeNodeFieldValuePopUpBinding;

public class ChangeNodeFieldValuePopUpActivity extends AppCompatActivity {
    private ActivityChangeNodeFieldValuePopUpBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityChangeNodeFieldValuePopUpBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        DisplayMetrics dm = new DisplayMetrics();
        this.getWindowManager().getDefaultDisplay().getMetrics(dm);

        this.getWindow().setLayout((int) (dm.widthPixels * 0.5), (int) (dm.heightPixels * 0.5));

        Intent receivedIntent = getIntent();

        NodeFieldType nodeFieldType = (NodeFieldType) receivedIntent.getSerializableExtra("node-field-type");

        switch (nodeFieldType) {
            case NAME:
                binding.changeNodeFieldTextView.setText(R.string.change_nodes_name);
                binding.newNodeFieldValueInputText.setHint(R.string.enter_new_name);
                break;
            case NETWORK:
                binding.changeNodeFieldTextView.setText(getResources().getText(R.string.change_nodes)
                        + " "
                        + getResources().getText(R.string.network));
                binding.newNodeFieldValueInputText.setHint(getResources().getText(R.string.enter_new)
                        + " "
                        + getResources().getText(R.string.network_name));
                break;
        }

        binding.submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO
            }
        });
    }

    public enum NodeFieldType {
        NAME,
        NETWORK
    }
}