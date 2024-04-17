package pt.ulisboa.tecnico.withoutnet.activities.Nodes;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import pt.ulisboa.tecnico.withoutnet.Frontend;
import pt.ulisboa.tecnico.withoutnet.GlobalClass;
import pt.ulisboa.tecnico.withoutnet.R;
import pt.ulisboa.tecnico.withoutnet.databinding.ActivityCreateNewNodePopUpBinding;
import pt.ulisboa.tecnico.withoutnet.models.Network;
import pt.ulisboa.tecnico.withoutnet.models.Node;

public class CreateNewNodePopUpActivity extends AppCompatActivity {
    private static final String TAG = "CreateNewNodePopUpActivity";

    private ActivityCreateNewNodePopUpBinding binding;

    private GlobalClass globalClass;

    private Network network;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityCreateNewNodePopUpBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        DisplayMetrics dm = new DisplayMetrics();
        this.getWindowManager().getDefaultDisplay().getMetrics(dm);

        this.getWindow().setLayout((int) (dm.widthPixels * 0.5), (int) (dm.heightPixels * 0.5));

        this.globalClass = (GlobalClass) getApplicationContext();

        Intent receivedIntent = getIntent();

        if(receivedIntent.hasExtra("network")) {
            network = (Network) receivedIntent.getSerializableExtra("network");
        } else {
            network = null;
        }

        binding.createNodeSubmitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String nodeCommonName = binding.editNodeNameTextView.getText().toString();

                if(nodeCommonName.equals("") || nodeCommonName.contains(" ")) {
                    Toast.makeText(CreateNewNodePopUpActivity.this, "Invalid node name", Toast.LENGTH_SHORT).show();
                    return;
                }

                if(network == null) {
                    network = new Network(-1, "");
                }

                Node nodeToBeAdded = new Node(-1, nodeCommonName, network);

                Frontend.FrontendResponseListener responseListener = new Frontend.FrontendResponseListener() {
                    @Override
                    public void onResponse(Object response) {
                        if(response == null) {
                            Log.e(TAG, "No connection to the internet");
                            return;
                        }

                        Node addedNode = (Node) response;

                        //TODO: return this node to the calling activity
                    }

                    @Override
                    public void onError(String errorMessage) {
                        Log.e(TAG, errorMessage);
                    }
                };

                globalClass.getFrontend().addNode(nodeToBeAdded, responseListener);
            }
        });
    }
}