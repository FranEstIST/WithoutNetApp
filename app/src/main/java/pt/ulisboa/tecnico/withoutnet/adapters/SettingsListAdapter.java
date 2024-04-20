package pt.ulisboa.tecnico.withoutnet.adapters;

import android.app.Activity;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.zip.Inflater;

import pt.ulisboa.tecnico.withoutnet.GlobalClass;
import pt.ulisboa.tecnico.withoutnet.R;

public class SettingsListAdapter extends RecyclerView.Adapter<SettingsListAdapter.ViewHolder> {
    private static final int NUM_OF_SETTINGS = 3;

    private Activity activity;
    private GlobalClass globalClass;

    public SettingsListAdapter(GlobalClass globalClass) {
        this.activity = activity;
        this.globalClass = globalClass;
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
                break;
            case 1:
                holder.settingNameTextView.setText(R.string.node_scanning_interval);
                break;
            case 2:
                holder.settingNameTextView.setText(R.string.message_transmission_to_server_interval);
                break;
            case 3:
                holder.settingNameTextView.setText(R.string.maximum_number_of_messages_in_cache);
                break;
            default:
                holder.settingNameTextView.setText(R.string.not_available);
                break;
        }
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
