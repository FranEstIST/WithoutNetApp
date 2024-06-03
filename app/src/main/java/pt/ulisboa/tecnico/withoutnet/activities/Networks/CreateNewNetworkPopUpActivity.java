package pt.ulisboa.tecnico.withoutnet.activities.Networks;

import android.os.Bundle;

import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;

import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import pt.ulisboa.tecnico.withoutnet.Frontend;
import pt.ulisboa.tecnico.withoutnet.GlobalClass;
import pt.ulisboa.tecnico.withoutnet.R;
import pt.ulisboa.tecnico.withoutnet.activities.Nodes.CreateNewNodePopUpActivity;
import pt.ulisboa.tecnico.withoutnet.databinding.ActivityCreateNewNetworkPopUpBinding;
import pt.ulisboa.tecnico.withoutnet.models.Network;
import pt.ulisboa.tecnico.withoutnet.models.Node;

public class CreateNewNetworkPopUpActivity extends AppCompatActivity {
    private static final String TAG = "CreateNewNetworkPopUpActivity";

    private AppBarConfiguration appBarConfiguration;
    private ActivityCreateNewNetworkPopUpBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityCreateNewNetworkPopUpBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        DisplayMetrics dm = new DisplayMetrics();
        this.getWindowManager().getDefaultDisplay().getMetrics(dm);

        this.getWindow().setLayout((int) (dm.widthPixels * 0.5), (int) (dm.heightPixels * 0.5));

        GlobalClass globalClass = (GlobalClass) getApplicationContext();

        binding.createNetworkSubmitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String networkName = binding.editNetworkNameTextView.getText().toString();

                if(networkName.equals("") || networkName.contains(" ")) {
                    Toast.makeText(CreateNewNetworkPopUpActivity.this, "Invalid network name. Should not contain spaces.", Toast.LENGTH_SHORT).show();
                    return;
                }

                Frontend.FrontendResponseListener responseListener = new Frontend.FrontendResponseListener() {
                    @Override
                    public void onResponse(Object response) {
                        if(response == null) {
                            Log.e(TAG, "No connection to the internet");
                            return;
                        }

                        Network addedNetwork = (Network) response;

                        //TODO: return this node to the calling activity
                    }

                    @Override
                    public void onError(String errorMessage) {
                        Log.e(TAG, errorMessage);
                    }
                };

                globalClass.getFrontend().addNetwork(networkName, responseListener);
            }
        });
    }

}