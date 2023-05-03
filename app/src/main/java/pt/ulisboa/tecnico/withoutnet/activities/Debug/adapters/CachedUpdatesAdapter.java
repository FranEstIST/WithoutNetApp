package pt.ulisboa.tecnico.withoutnet.activities.Debug.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import pt.ulisboa.tecnico.withoutnet.R;
import pt.ulisboa.tecnico.withoutnet.models.Update;

public class CachedUpdatesAdapter  extends RecyclerView.Adapter<CachedUpdatesAdapter.ViewHolder> {
    private final ArrayList<Update> cachedUpdates;

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        // Your holder should contain a member variable
        // for any view that will be set as you render a row
        public TextView updateTextView;

        private CachedUpdatesAdapter.OnCachedUpdateListener onCachedUpdateListener;

        // We also create a constructor that accepts the entire item row
        // and does the view lookups to find each subview
        public ViewHolder(View itemView, CachedUpdatesAdapter.OnCachedUpdateListener onCachedUpdateListener) {
            // Stores the itemView in a public final member variable that can be used
            // to access the context from any ViewHolder instance.
            super(itemView);

            this.updateTextView = (TextView) itemView.findViewById(R.id.update_text);

            this.onCachedUpdateListener = onCachedUpdateListener;

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            this.onCachedUpdateListener.onCachedUpdateClick(getAdapterPosition());
        }
    }

    private CachedUpdatesAdapter.OnCachedUpdateListener onCachedUpdateListener;

    // Pass in the contact array into the constructor
    public CachedUpdatesAdapter(ArrayList<Update> cachedUpdates, CachedUpdatesAdapter.OnCachedUpdateListener onCachedUpdateListener) {
        this.cachedUpdates = cachedUpdates;
        this.onCachedUpdateListener = onCachedUpdateListener;
    }

    // Usually involves inflating a layout from XML and returning the holder
    @Override
    public CachedUpdatesAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        // Inflate the custom layout
        View nodeView = inflater.inflate(R.layout.item_cached_update, parent, false);

        // Return a new holder instance
        CachedUpdatesAdapter.ViewHolder viewHolder = new CachedUpdatesAdapter.ViewHolder(nodeView, onCachedUpdateListener);
        return viewHolder;
    }

    // Involves populating data into the item through holder
    @Override
    public void onBindViewHolder(CachedUpdatesAdapter.ViewHolder holder, int position) {
        // Get the data model based on position
        Update update = cachedUpdates.get(position);

        //Log.d("CA", "Chatroom is available: " + chatroom.isAvailable());

        // Set item views based on your views and data model
        TextView updateTextView = holder.updateTextView;

        updateTextView.setText(update.toString());

    }

    // Returns the total count of items in the list
    @Override
    public int getItemCount() {
        return cachedUpdates.size();
    }

    public interface OnCachedUpdateListener {
        void onCachedUpdateClick(int position);
    }
}
