package pt.ulisboa.tecnico.withoutnet;

import androidx.appcompat.app.AppCompatActivity;
import androidx.work.OneTimeWorkRequest;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import androidx.work.WorkRequest;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.util.concurrent.TimeUnit;

import pt.ulisboa.tecnico.withoutnet.databinding.ActivityMain2Binding;

public class MainActivity2 extends AppCompatActivity {
    private ActivityMain2Binding binding;
    private boolean isParticipating;

    private WorkRequest receiveAndPropagateUpdatesWorkReq;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_main2);

        binding = ActivityMain2Binding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        isParticipating = false;

        binding.startStopParticipatingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /*NavHostFragment.findNavController(FirstFragment.this)
                        .navigate(R.id.action_FirstFragment_to_SecondFragment);*/
                //Toast.makeText(getApplicationContext(), "Button pressed", Toast.LENGTH_LONG).show();
                /*Intent intent = new Intent(getApplicationContext(), MainActivity2.class);
                starActivity(intent);*/

                MainActivity2.this.receiveAndPropagateUpdatesWorkReq = new PeriodicWorkRequest
                        .Builder(ReceiveAndPropagateUpdatesWorker.class, 900000, TimeUnit.MILLISECONDS)
                        .build();

                Button button = (Button) view;

                if(isParticipating) {
                    isParticipating = false;
                    button.setText(R.string.start_participating);

                    //Stop scanning for nodes and propagating updates
                    WorkManager.getInstance(MainActivity2.this).cancelWorkById(MainActivity2.this.receiveAndPropagateUpdatesWorkReq.getId());

                } else {
                    isParticipating = true;
                    button.setText(R.string.stop_participating);

                    // Start scanning for nodes and propagating updates
                    WorkManager.getInstance(MainActivity2.this).enqueue(MainActivity2.this.receiveAndPropagateUpdatesWorkReq);
                }

            }
        });

        binding.debugButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), DebugActivity.class);
                startActivity(intent);
            }
        });
    }
}