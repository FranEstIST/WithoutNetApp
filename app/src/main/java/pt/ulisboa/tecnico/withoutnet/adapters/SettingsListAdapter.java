package pt.ulisboa.tecnico.withoutnet.adapters;

import android.app.Activity;
import android.content.Intent;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Set;
import java.util.zip.Inflater;

import pt.ulisboa.tecnico.withoutnet.GlobalClass;
import pt.ulisboa.tecnico.withoutnet.R;
import pt.ulisboa.tecnico.withoutnet.activities.ChangeSettingValuePopUpActivity;

public class SettingsListAdapter extends RecyclerView.Adapter<SettingsListAdapter.ViewHolder> {
    private static final int NUM_OF_SETTINGS = 4;

    private Activity activity;
    private GlobalClass globalClass;
    private ActivityResultLauncher<Intent> changeSettingValueResultLauncher;

    public SettingsListAdapter(GlobalClass globalClass, ActivityResultLauncher<Intent> changeSettingValueResultLauncher) {
        this.activity = activity;
        this.globalClass = globalClass;
        this.changeSettingValueResultLauncher = changeSettingValueResultLauncher;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        View itemView = inflater.inflate(R.layout.item_setting, parent, false);
        ViewHolder viewHolder = new ViewHolder(itemView);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        switch(position) {
            case 0:
                holder.settingNameTextView.setText(R.string.server_url);
                holder.settingValueTextView.setText(globalClass.getServerURL());
                break;
            case 1:
                holder.settingNameTextView.setText(R.string.node_scanning_interval);
                holder.settingValueTextView.setText(globalClass.getNodeScanningInterval()
                        + " "
                        + globalClass.getString(R.string.ms));
                break;
            case 2:
                holder.settingNameTextView.setText(R.string.message_transmission_to_server_interval);
                holder.settingValueTextView.setText(globalClass.getMessageTransmissionToServerInterval()
                        + " "
                        + globalClass.getString(R.string.ms));
                break;
            case 3:
                holder.settingNameTextView.setText(R.string.maximum_number_of_messages_in_cache);
                holder.settingValueTextView.setText(globalClass.getMaximumNumOfMessagesInCache() + "");
                break;
            default:
                holder.settingNameTextView.setText(R.string.not_available);
                break;
        }

        holder.editSettingValueButton.setOnClickListener(new View.OnClickListener() {
            Intent intent = new Intent(globalClass, ChangeSettingValuePopUpActivity.class);

            @Override
            public void onClick(View v) {
                switch(holder.getAdapterPosition()) {
                    case 0:
                        intent.putExtra("setting-type"
                                , ChangeSettingValuePopUpActivity.SettingType.SERVER_URL);

                        break;
                    case 1:
                        intent.putExtra("setting-type"
                                , ChangeSettingValuePopUpActivity.SettingType.NODE_SCANNING_INTERVAL);

                        break;
                    case 2:
                        intent.putExtra("setting-type"
                                , ChangeSettingValuePopUpActivity.SettingType.MESSAGE_TRANSMISSION_TO_SERVER_INTERVAL);

                        break;
                    case 3:
                        intent.putExtra("setting-type"
                                , ChangeSettingValuePopUpActivity.SettingType.MAXIMUM_NUM_OF_MESSAGES_IN_CACHE);
                        break;
                    default:
                        return;
                }

                changeSettingValueResultLauncher.launch(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return NUM_OF_SETTINGS;
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public TextView settingNameTextView;
        public TextView settingValueTextView;
        public ImageButton editSettingValueButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            settingNameTextView = itemView.findViewById(R.id.settingName);
            settingValueTextView = itemView.findViewById(R.id.settingValue);
            editSettingValueButton = itemView.findViewById(R.id.editSettingValueButton);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {

        }
    }
}
