package pt.ulisboa.tecnico.withoutnet.fragments;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.bluetooth.BluetoothAdapter;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.work.WorkManager;
import androidx.work.WorkRequest;

import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import pt.ulisboa.tecnico.withoutnet.GlobalClass;
import pt.ulisboa.tecnico.withoutnet.R;
import pt.ulisboa.tecnico.withoutnet.activities.Main.MainActivity;
import pt.ulisboa.tecnico.withoutnet.databinding.FragmentHomeBinding;
import pt.ulisboa.tecnico.withoutnet.services.ble.ReceiveAndPropagateUpdatesService;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link HomeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HomeFragment extends Fragment {
    private static final String TAG = "MainActivity";
    private static final int REQUEST_BLUETOOTH_PERMISSIONS = 1;

    private GlobalClass globalClass;

    private FragmentHomeBinding binding;

    private ActivityResultLauncher<Intent> bluetoothAdapterActivityResultLauncher;

    public HomeFragment() {
        // Required empty public constructor
    }

    public static HomeFragment newInstance() {
        HomeFragment fragment = new HomeFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);

        WorkManager.getInstance(getActivity().getApplicationContext()).cancelAllWork();

        globalClass = (GlobalClass) getActivity().getApplicationContext();

        updateStartStopParticipatingButton();

        binding.startStopParticipatingButton.setOnClickListener(v -> {
            if(globalClass.isParticipating()) {
                stopParticipating();
            } else {
                startParticipating();
            }
        });

        bluetoothAdapterActivityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == Activity.RESULT_OK) {
                            HomeFragment.this.startParticipating();
                        } else {
                            Toast.makeText(getActivity().getApplicationContext(), "WithoutNet participation cannot be enabled due to Bluetooth not being enabled", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        MainActivity  mainActivity = (MainActivity) getActivity();

        if(mainActivity != null && mainActivity.binding != null) {
            TextView appBarTitleTextView = mainActivity.binding.appBarTitle;
            appBarTitleTextView.setText("");
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        MainActivity  mainActivity = (MainActivity) getActivity();

        if(mainActivity != null && mainActivity.binding != null) {
            TextView appBarTitleTextView = mainActivity.binding.appBarTitle;
            appBarTitleTextView.setText(R.string.home);
        }
    }

    private void updateStartStopParticipatingButton() {
        ImageButton imageButton = binding.startStopParticipatingButton;
        TextView participationStatusTextView = binding.participationStatusTextView;
        TextView pressButtonTextView = binding.pressButtonTextView;

        if(globalClass.isParticipating()) {
            imageButton.setForeground(getActivity().getDrawable(R.drawable.wn_button_on));
            participationStatusTextView.setText(R.string.participating);
            pressButtonTextView.setText(R.string.press_to_stop_participating);
        } else {
            imageButton.setForeground(getActivity().getDrawable(R.drawable.wn_button_off));
            participationStatusTextView.setText(R.string.not_participating);
            pressButtonTextView.setText(R.string.press_to_start_participating);
        }
    }

    private void startParticipating() {
        ArrayList<String> permissionsList = new ArrayList<>();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
                && ActivityCompat.checkSelfPermission(getActivity().getApplicationContext(), Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "Bluetooth connect permission not granted. Asking for permission.");
            permissionsList.add(Manifest.permission.BLUETOOTH_CONNECT);
            return;
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
                && ActivityCompat.checkSelfPermission(getActivity().getApplicationContext(), Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED){
            Log.d(TAG, "Bluetooth scan permission not granted. Asking for permission.");
            permissionsList.add(Manifest.permission.BLUETOOTH_SCAN);

        } else if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S
                && ActivityCompat.checkSelfPermission(getActivity().getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "Fine location permissions not granted. Asking user for permission.");
            permissionsList.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }

        if(!permissionsList.isEmpty()) {
            String[] permissionsArray = new String[permissionsList.size()];
            permissionsList.toArray(permissionsArray);
            ActivityCompat.requestPermissions(getActivity(), permissionsArray, REQUEST_BLUETOOTH_PERMISSIONS);
            return;
        }

        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if(bluetoothAdapter == null) {
            // This device does not support Bluetooth
            Log.d(TAG, "Bluetooth is unavailable");
            Toast.makeText(getActivity().getApplicationContext()
                            , "WithoutNet participation cannot be enabled due to Bluetooth being unavailable on this device"
                            , Toast.LENGTH_SHORT)
                    .show();
            return;
        } else if (!bluetoothAdapter.isEnabled()) {
            // Bluetooth is not enabled
            // Ask user to enable Bluetooth
            Log.d(TAG, "Bluetooth is not enabled. Asking user to enable Bluetooth.");
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            bluetoothAdapterActivityResultLauncher.launch(enableBtIntent);
            return;
        }

        globalClass.setParticipating(true);

        updateStartStopParticipatingButton();

        if(!isServiceRunning(ReceiveAndPropagateUpdatesService.class)) {
            Intent intent = new Intent(getActivity(), ReceiveAndPropagateUpdatesService.class);
            getActivity().startService(intent);
        }
    }

    private void stopParticipating() {
        globalClass.setParticipating(false);

        updateStartStopParticipatingButton();

        if (isServiceRunning(ReceiveAndPropagateUpdatesService.class)) {
            Intent intent = new Intent(getActivity(), ReceiveAndPropagateUpdatesService.class);
            getActivity().stopService(intent);
        }
    }

    public boolean isServiceRunning(Class<?> cls){
        ActivityManager activityManager = (ActivityManager) getActivity().getSystemService(Context.ACTIVITY_SERVICE);
        for(ActivityManager.RunningServiceInfo service: activityManager.getRunningServices(Integer.MAX_VALUE)) {
            if(cls.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case REQUEST_BLUETOOTH_PERMISSIONS:
                for(int grantResult : grantResults) {
                    if(grantResult == PackageManager.PERMISSION_DENIED) {
                        Toast.makeText(getActivity().getApplicationContext()
                                , "WithoutNet participation cannot be enabled due to insufficient permissions"
                                , Toast.LENGTH_SHORT)
                                .show();
                        return;
                    }
                }
                startParticipating();
                break;
        }
    }
}