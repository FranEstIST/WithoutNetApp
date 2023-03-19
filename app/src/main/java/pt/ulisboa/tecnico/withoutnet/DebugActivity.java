package pt.ulisboa.tecnico.withoutnet;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import pt.ulisboa.tecnico.withoutnet.databinding.ActivityDebugBinding;

public class DebugActivity extends AppCompatActivity {
    public final static int REQUEST_ENABLE_BT = 123;
    public final static int REQUEST_ENABLE_BT_SCAN = 124;
    private  ActivityResultLauncher<String> requestPermissionLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivityDebugBinding binding = ActivityDebugBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Toast.makeText(getApplicationContext(), "Debug activity created", Toast.LENGTH_LONG);

        Log.d("DEBUG", "Activity started\n");

        BluetoothManager bluetoothManager = getSystemService(BluetoothManager.class);
        BluetoothAdapter bluetoothAdapter = bluetoothManager.getAdapter();

        Toast.makeText(getApplicationContext(), "Debug activity created 2", Toast.LENGTH_SHORT);

        if (bluetoothAdapter == null) {
            // Device doesn't support Bluetooth
            Toast.makeText(this, "Bluetooth not supported", Toast.LENGTH_SHORT);
            Log.d("DEBUG", "Bluetooth not supported\n");
        }

        if (!bluetoothAdapter.isEnabled()) {
            Toast.makeText(this, "Bluetooth not turned on", Toast.LENGTH_SHORT);
            Log.d("DEBUG", "Bluetooth not turned on\n");

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                Log.d("DEBUG", "Bluetooth permissions not yet granted\n");

                requestPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                            if (isGranted) {
                                // Permission is granted. Continue the action or workflow in your
                                // app.

                                Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                                startActivityForResult(intent, REQUEST_ENABLE_BT);
                                Log.d("DEBUG", "Bluetooth permissions not yet granted\n");
                            } else {
                                // Permission not granted.
                                Toast.makeText(this.getApplicationContext(), "BT permissions not granted", Toast.LENGTH_SHORT);
                                Log.d("DEBUG", "Bluetooth permissions not granted\n");
                            }
                        });

                requestPermissionLauncher.launch(Manifest.permission.BLUETOOTH_CONNECT);

                return;
            }



            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(intent, REQUEST_ENABLE_BT);
        }

        Toast.makeText(this, "Bluetooth turned on", Toast.LENGTH_SHORT);
        Log.d("DEBUG", "Bluetooth turned on\n");
        /*if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            ActivityCompat.requestPermissions(this, new String[] { Manifest.permission.BLUETOOTH_CONNECT }, REQUEST_ENABLE_BT_SCAN);
            return
        }*/

    }

    private void scanForBLEDevices() {

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case REQUEST_ENABLE_BT:
                if (grantResults.length > 0 &&
                        grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission is granted. Continue the action or workflow
                    // in your app.

                }  else {
                    // Explain to the user that the feature is unavailable because
                    // the feature requires a permission that the user has denied.
                    // At the same time, respect the user's decision. Don't link to
                    // system settings in an effort to convince the user to change
                    // their decision.
                }
                return;
            case REQUEST_ENABLE_BT_SCAN:
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQUEST_ENABLE_BT:

        }
    }
}