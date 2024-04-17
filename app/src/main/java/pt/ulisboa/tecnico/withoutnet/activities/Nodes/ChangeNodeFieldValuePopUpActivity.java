package pt.ulisboa.tecnico.withoutnet.activities.Nodes;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Toast;

import java.io.Serializable;

import pt.ulisboa.tecnico.withoutnet.Frontend;
import pt.ulisboa.tecnico.withoutnet.GlobalClass;
import pt.ulisboa.tecnico.withoutnet.R;
import pt.ulisboa.tecnico.withoutnet.databinding.ActivityChangeNodeFieldValuePopUpBinding;
import pt.ulisboa.tecnico.withoutnet.models.Node;

public class ChangeNodeFieldValuePopUpActivity extends AppCompatActivity {
    private static final String TAG = "ChangeNodeFieldValuePopUpActivity";

    private GlobalClass globalClass;

    private ActivityChangeNodeFieldValuePopUpBinding binding;

    private Node node;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityChangeNodeFieldValuePopUpBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        DisplayMetrics dm = new DisplayMetrics();
        this.getWindowManager().getDefaultDisplay().getMetrics(dm);

        this.getWindow().setLayout((int) (dm.widthPixels * 0.5), (int) (dm.heightPixels * 0.5));

        globalClass = (GlobalClass) getApplicationContext();

        Intent receivedIntent = getIntent();

        if(!receivedIntent.hasExtra("node-field-type") || !receivedIntent.hasExtra("node")) {
            finish();
            return;
        }

        NodeFieldType nodeFieldType = (NodeFieldType) receivedIntent.getSerializableExtra("node-field-type");

        node = (Node) receivedIntent.getSerializableExtra("node");

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

                Frontend.FrontendResponseListener responseListener = new Frontend.FrontendResponseListener() {
                    @Override
                    public void onResponse(Object response) {
                        if(response != null) {
                            Node updatedNode = (Node) response;
                            Toast.makeText(ChangeNodeFieldValuePopUpActivity.this, "Changed node's name to: " + updatedNode.getCommonName(), Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(ChangeNodeFieldValuePopUpActivity.this, "No Internet connection", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onError(String errorMessage) {

                    }
                };

                String newName = binding.newNodeFieldValueInputText.getText().toString();

                if(newName.equals("") || newName.contains(" ")) {
                    Toast.makeText(ChangeNodeFieldValuePopUpActivity.this, "Invalid name", Toast.LENGTH_SHORT).show();
                    return;
                }

                node.setCommonName(newName);

                globalClass.getFrontend().updateNode(node, responseListener);
            }
        });
    }

    public enum NodeFieldType {
        NAME,
        NETWORK
    }
}