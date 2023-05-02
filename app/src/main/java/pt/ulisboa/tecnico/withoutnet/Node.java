package pt.ulisboa.tecnico.withoutnet;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.content.pm.PackageManager;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import java.util.Arrays;
import java.util.UUID;

public class Node {
    private String id;
    private String commonName;
    private String readingType;

    /**
     * Returns a Node object, as long as the necessary permissions for accessing BL devices
     * are granted
     * @param BLDevice
     *
     */
    // TODO: Check if BL permissions have been granted
    @SuppressLint("MissingPermission")
    public Node(BluetoothDevice BLDevice) {
        this.setId(BLDevice.getAddress());
        this.setCommonName(BLDevice.getName());
        // TODO: Get the reading type to be part of the advertising package
        this.readingType = "";
    }

    public Node(String id, String commonName, String readingType) {
        this.setId(id);
        this.setCommonName(commonName);
        this.readingType = readingType;
    }

    public String getCommonName() {
        return this.commonName;
    }

    public void setCommonName(String commonName) {
        if(commonName == null) {
            this.commonName = "Unnamed";
        } else {
            this.commonName = commonName;
        }
    }

    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        if(id == null) {
            this.id = "N/A";
        } else {
            this.id = id;
        }
    }

    public String getReadingType() {
        return this.readingType;
    }

    public void setReadingType(String readingType) {
        this.readingType = readingType;
    }

    @NonNull
    @Override
    public String toString() {
        return "" + this.getId() + " : " + this.getCommonName() + " : " + this.getReadingType();
    }
}
