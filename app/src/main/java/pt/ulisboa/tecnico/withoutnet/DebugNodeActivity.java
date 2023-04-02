package pt.ulisboa.tecnico.withoutnet;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothProfile;
import android.os.Bundle;
import android.util.Log;

import java.util.UUID;

import pt.ulisboa.tecnico.withoutnet.databinding.ActivityDebugBinding;

public class DebugNodeActivity extends AppCompatActivity {

    private BluetoothGattCallback bluetoothGattCallback;

    private int connectionState = BluetoothProfile.STATE_DISCONNECTED;

    @SuppressLint("MissingPermission")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivityDebugBinding binding = ActivityDebugBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        BluetoothDevice BLEDevice = (BluetoothDevice)getIntent().getParcelableExtra("BLEDevice");

        bluetoothGattCallback = new BluetoothGattCallback() {
            @Override
            public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    // successfully connected to the GATT Server
                    connectionState = BluetoothProfile.STATE_CONNECTED;
                } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                    // disconnected from the GATT Server
                    connectionState = BluetoothProfile.STATE_DISCONNECTED;
                }
            }

            @Override
            public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
                super.onCharacteristicRead(gatt, characteristic, status);
                Log.d("DEBUG", characteristic.getStringValue(0));
            }

            @Override
            public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
                super.onCharacteristicWrite(gatt, characteristic, status);
            }
        };

        // TODO: Check if permissions have been granted
        BluetoothGatt BLEGatt = BLEDevice.connectGatt(this, false, bluetoothGattCallback);
        BLEGatt.readCharacteristic
                (new BluetoothGattCharacteristic
                        (UUID.fromString("beb5483e-36e1-4688-b7f5-ea07361b26a8"),
                                BluetoothGattCharacteristic.PROPERTY_READ,
                                BluetoothGattCharacteristic.PERMISSION_READ));


    }
}