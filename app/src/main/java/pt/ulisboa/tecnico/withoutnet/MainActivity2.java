package pt.ulisboa.tecnico.withoutnet;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import pt.ulisboa.tecnico.withoutnet.databinding.ActivityMain2Binding;

public class MainActivity2 extends AppCompatActivity {
    private ActivityMain2Binding binding;
    private boolean isParticipating;

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
                startActivity(intent);*/

                Button button = (Button) view;

                if(isParticipating) {
                    isParticipating = false;
                    button.setText(R.string.start_participating);
                } else {
                    isParticipating = true;
                    button.setText(R.string.stop_participating);
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