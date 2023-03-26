package pt.ulisboa.tecnico.withoutnet;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.content.pm.PackageManager;

import androidx.core.app.ActivityCompat;

import java.util.UUID;

public class Node {
    private String commonName;
    private String UUID;
    private BluetoothDevice BLDevice;

    /**
     * Returns a Node object, as long as the necessary permissions for accessing BL devices
     * are granted
     * @param BLDevice
     *
     */
    // TODO: Check if BL permissions have been granted
    @SuppressLint("MissingPermission")
    public Node(BluetoothDevice BLDevice) {
        this.setCommonName(BLDevice.getName());
        this.setUUID(BLDevice.getAddress());
        this.BLDevice = BLDevice;
    }

    public BluetoothDevice getBLDevice() {
        return BLDevice;
    }

    public void setBLDevice(BluetoothDevice BLDevice) {
        this.BLDevice = BLDevice;
    }

    public String getCommonName() {
        return commonName;
    }

    public void setCommonName(String commonName) {
        if(commonName == null) {
            this.commonName = "Unnamed";
        } else {
            this.commonName = commonName;
        }
    }

    public String getUUID() {
        return UUID;
    }

    public void setUUID(String UUID) {
        if(UUID == null) {
            this.UUID = "N/A";
        } else {
            this.UUID = UUID;
        }

    }
}
