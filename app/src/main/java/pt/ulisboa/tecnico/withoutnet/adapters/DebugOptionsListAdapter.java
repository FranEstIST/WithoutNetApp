package pt.ulisboa.tecnico.withoutnet.adapters;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Set;
import java.util.zip.Inflater;

import io.reactivex.rxjava3.schedulers.Schedulers;
import pt.ulisboa.tecnico.withoutnet.Frontend;
import pt.ulisboa.tecnico.withoutnet.GlobalClass;
import pt.ulisboa.tecnico.withoutnet.R;
import pt.ulisboa.tecnico.withoutnet.activities.ChangeSettingValuePopUpActivity;
import pt.ulisboa.tecnico.withoutnet.activities.Nodes.NodeDetailsActivity;
import pt.ulisboa.tecnico.withoutnet.network.StatusCodes;

public class DebugOptionsListAdapter extends RecyclerView.Adapter<DebugOptionsListAdapter.ViewHolder> {
    private static final int NUM_OF_OPTIONS = 2;

    private Activity activity;
    private GlobalClass globalClass;


    public DebugOptionsListAdapter(Activity activity) {
        this.activity = activity;
        this.globalClass = (GlobalClass) activity.getApplicationContext();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        View itemView = inflater.inflate(R.layout.item_debug_option, parent, false);
        ViewHolder viewHolder = new ViewHolder(itemView);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        switch(position) {
            case 0:
                holder.debugOptionNameTextView.setText(R.string.delete_all_messages_in_database);
                break;
            case 1:
                holder.debugOptionNameTextView.setText(R.string.delete_all_messages_in_server);
                break;
            default:
                holder.debugOptionNameTextView.setText(R.string.not_available);
                break;
        }
    }

    @Override
    public int getItemCount() {
        return NUM_OF_OPTIONS;
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public TextView debugOptionNameTextView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            debugOptionNameTextView = itemView.findViewById(R.id.debugOptionName);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            switch (getAdapterPosition()) {
                case 0:
                    AlertDialog.Builder builder = new AlertDialog.Builder(activity);

                    builder.setMessage(R.string.delete_all_messages_in_database_explanation)
                            .setTitle(R.string.delete_all_messages_in_database)
                            .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    globalClass.getWithoutNetAppDatabase()
                                            .messageDao()
                                            .deleteAll()
                                            .observeOn(Schedulers.newThread())
                                            .subscribeOn(Schedulers.newThread())
                                            .subscribe(() -> activity.runOnUiThread(() -> Toast.makeText(activity
                                                            , activity.getResources().getText(R.string.deletion_success_message)
                                                                    + " "
                                                                    + activity.getResources().getText(R.string.all_messages_in_database)
                                                            , Toast.LENGTH_SHORT)
                                                    .show()));
                                }
                            })
                            .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.dismiss();
                                }
                            });

                    AlertDialog dialog = builder.create();

                    dialog.show();
                    break;
                case 1:
                    //globalClass.getFrontend().
                    break;
            }

        }
    }
}
