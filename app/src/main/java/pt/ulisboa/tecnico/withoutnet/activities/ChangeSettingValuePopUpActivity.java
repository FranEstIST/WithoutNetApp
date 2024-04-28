package pt.ulisboa.tecnico.withoutnet.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Toast;

import pt.ulisboa.tecnico.withoutnet.GlobalClass;
import pt.ulisboa.tecnico.withoutnet.R;
import pt.ulisboa.tecnico.withoutnet.activities.Nodes.ChangeNodeFieldValuePopUpActivity;
import pt.ulisboa.tecnico.withoutnet.databinding.ActivityChangeNodeFieldValuePopUpBinding;
import pt.ulisboa.tecnico.withoutnet.databinding.ActivityChangeSettingValuePopUpBinding;
import pt.ulisboa.tecnico.withoutnet.models.Node;

public class ChangeSettingValuePopUpActivity extends AppCompatActivity {
    private ActivityChangeSettingValuePopUpBinding binding;

    private GlobalClass globalClass;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityChangeSettingValuePopUpBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        DisplayMetrics dm = new DisplayMetrics();
        this.getWindowManager().getDefaultDisplay().getMetrics(dm);

        this.getWindow().setLayout((int) (dm.widthPixels * 0.5), (int) (dm.heightPixels * 0.5));

        globalClass = (GlobalClass) getApplicationContext();

        Intent receivedIntent = getIntent();

        if(!receivedIntent.hasExtra("setting-type")) {
            finish();
            return;
        }

        SettingType settingType = (SettingType) receivedIntent.getSerializableExtra("setting-type");

        switch (settingType) {
            case SERVER_URL:
                binding.changeSettingValueTextView.setText(getResources().getText(R.string.change)
                    + " "
                    + getResources().getText(R.string.server_url));
                binding.newSettingValueInputText.setHint(getResources().getText(R.string.enter_new)
                        + " "
                        + getResources().getText(R.string.server_url));
                binding.newSettingValueInputText.setText(globalClass.getServerURL());
                break;
            case NODE_SCANNING_INTERVAL:
                binding.changeSettingValueTextView.setText(getResources().getText(R.string.change)
                        + " "
                        + getResources().getText(R.string.node_scanning_interval));
                binding.newSettingValueInputText.setHint(getResources().getText(R.string.enter_new)
                        + " "
                        + getResources().getText(R.string.node_scanning_interval));
                binding.newSettingValueInputText.setText(globalClass.getNodeScanningInterval() + "");
                break;
            case MESSAGE_TRANSMISSION_TO_SERVER_INTERVAL:
                binding.changeSettingValueTextView.setText(getResources().getText(R.string.change)
                        + " "
                        + getResources().getText(R.string.message_transmission_to_server_interval));
                binding.newSettingValueInputText.setHint(getResources().getText(R.string.enter_new)
                        + " "
                        + getResources().getText(R.string.message_transmission_to_server_interval));
                binding.newSettingValueInputText.setText(globalClass.getMessageTransmissionToServerInterval() + "");
                break;
            case MAXIMUM_NUM_OF_MESSAGES_IN_CACHE:
                binding.changeSettingValueTextView.setText(getResources().getText(R.string.change)
                        + " "
                        + getResources().getText(R.string.maximum_number_of_messages_in_cache));
                binding.newSettingValueInputText.setHint(getResources().getText(R.string.enter_new)
                        + " "
                        + getResources().getText(R.string.maximum_number_of_messages_in_cache));
                binding.newSettingValueInputText.setText(globalClass.getMaximumNumOfMessagesInCache() + "");
                break;
        }

        binding.submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(binding.newSettingValueInputText.getText() == null) {
                    return;
                }

                String newSettingValueString = binding.newSettingValueInputText.getText().toString();

                switch (settingType) {
                    case SERVER_URL:
                        globalClass.setServerURL(newSettingValueString);
                        break;
                    case NODE_SCANNING_INTERVAL:
                        try {
                            int newNodeScanningInterval = Integer.valueOf(newSettingValueString);
                            globalClass.setNodeScanningInterval(newNodeScanningInterval);
                        } catch (NumberFormatException e) {
                            Toast.makeText(ChangeSettingValuePopUpActivity.this, "Invalid interval. It should be an integer.", Toast.LENGTH_SHORT).show();
                        }
                        break;
                    case MESSAGE_TRANSMISSION_TO_SERVER_INTERVAL:
                        try {
                            int newMessageTransmissionToServerInterval = Integer.valueOf(newSettingValueString);
                            globalClass.setMessageTransmissionToServerInterval(newMessageTransmissionToServerInterval);
                        } catch (NumberFormatException e) {
                            Toast.makeText(ChangeSettingValuePopUpActivity.this, "Invalid interval. It should be an integer.", Toast.LENGTH_SHORT).show();
                        }
                        break;
                    case MAXIMUM_NUM_OF_MESSAGES_IN_CACHE:
                        try {
                            int newMaximumNumOfMessagesInCache = Integer.valueOf(newSettingValueString);
                            globalClass.setMaximumNumOfMessagesInCache(newMaximumNumOfMessagesInCache);
                        } catch (NumberFormatException e) {
                            Toast.makeText(ChangeSettingValuePopUpActivity.this, "Invalid number. It should be an integer.", Toast.LENGTH_SHORT).show();
                        }
                        break;
                }

                Intent resultIntent = new Intent();

                setResult(RESULT_OK, resultIntent);
                finish();
            }
        });
    }

    public enum SettingType {
        SERVER_URL,
        NODE_SCANNING_INTERVAL,
        MESSAGE_TRANSMISSION_TO_SERVER_INTERVAL,
        MAXIMUM_NUM_OF_MESSAGES_IN_CACHE
    }
}