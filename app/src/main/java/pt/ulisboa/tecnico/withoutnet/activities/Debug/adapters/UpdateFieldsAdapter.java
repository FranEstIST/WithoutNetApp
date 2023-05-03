package pt.ulisboa.tecnico.withoutnet.activities.Debug.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import pt.ulisboa.tecnico.withoutnet.R;
import pt.ulisboa.tecnico.withoutnet.constants.ReadingTypeIDs;
import pt.ulisboa.tecnico.withoutnet.models.Update;

public class UpdateFieldsAdapter extends RecyclerView.Adapter<UpdateFieldsAdapter.ViewHolder> {
    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView fieldNameTextView;
        public TextView fieldValueTextView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            this.fieldNameTextView = (TextView) itemView.findViewById(R.id.field_name);
            this.fieldValueTextView = (TextView) itemView.findViewById(R.id.field_value);
        }
    }

    private Update update;

    public UpdateFieldsAdapter() {
        this.update = null;
    }

    public UpdateFieldsAdapter(Update update) {
        this.update = update;
    }

    public void setUpdate(Update update) {
        this.update = update;
    }

    @NonNull
    @Override
    public UpdateFieldsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        // Inflate the custom layout
        View nodeView = inflater.inflate(R.layout.item_update_field, parent, false);

        // Return a new holder instance
        UpdateFieldsAdapter.ViewHolder viewHolder = new UpdateFieldsAdapter.ViewHolder(nodeView);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull UpdateFieldsAdapter.ViewHolder holder, int position) {
        // Set item views based on your views and data model

        TextView fieldNameTextView = holder.fieldNameTextView;
        TextView fieldValueTextView = holder.fieldValueTextView;

        switch(position) {
            case 0:
                fieldNameTextView.setText(R.string.timestamp);
                fieldValueTextView.setText(this.update.getTimestamp()+ "");
                break;
            case 1:
                fieldNameTextView.setText(R.string.node_id);
                fieldValueTextView.setText(this.update.getSender().getId());
                break;
            case 2:
                fieldNameTextView.setText(R.string.common_name);
                fieldValueTextView.setText(this.update.getSender().getCommonName());
                break;
            case 3:
                fieldNameTextView.setText(R.string.reading_type);
                fieldValueTextView.setText(ReadingTypeIDs
                        .getReadingTypeName(this.update.getSender().getReadingType()));
                break;
            case 4:
                fieldNameTextView.setText(R.string.reading_value);
                fieldValueTextView.setText(this.update.getReading());
                break;
            default:
                // TODO: Throw an exception here
                break;
        }
    }

    @Override
    public int getItemCount() {
        return update == null ? 0 : 5;
    }
}
