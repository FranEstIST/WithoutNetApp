package pt.ulisboa.tecnico.withoutnet.models;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.content.pm.PackageManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;

import java.io.Serializable;
import java.util.Arrays;
import java.util.UUID;

public class Node implements Serializable {
    private String id;
    private String commonName;
    private String readingType;
    private Network network;

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

    public Node(String id, String commonName, Network network) {
        this.setId(id);
        this.setCommonName(commonName);
        this.readingType = "";
        this.network = network;
    }

    public Node(String id, String commonName, String readingType) {
        this.setId(id);
        this.setCommonName(commonName);
        this.readingType = readingType;
    }

    public Node(String id, String commonName, String readingType, Network network) {
        this.setId(id);
        this.setCommonName(commonName);
        this.readingType = readingType;
        this.network = network;
    }

    public Node(String relevantNodeValue) {
        String[] relevantNodeValueComponents = relevantNodeValue.split("#");

        if(relevantNodeValueComponents.length != 3) {
            // TODO: Throw an exception
            return;
        }

        this.setId(relevantNodeValueComponents[0]);
        this.setCommonName(relevantNodeValueComponents[1]);
        this.setReadingType(relevantNodeValueComponents[2]);
    }

    public Node(int id, String commonName, Network network) {
        this.setId(id + "");
        this.setCommonName(commonName);
        this.readingType = "";
        this.network = network;
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

    public Network getNetwork() {
        return network;
    }

    public void setNetwork(Network network) {
        this.network = network;
    }

    @NonNull
    @Override
    public String toString() {
        return "" + this.getId() + "#" + this.getCommonName() + "#" + this.getReadingType();
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if(obj != null && obj instanceof Node) {
            Node node = (Node) obj;
            return node.getId().equals(this.getId())
                    && node.getCommonName().equals(this.getCommonName())
                    && node.getReadingType().equals(this.getReadingType());
        }

        return false;
    }

    // TODO: Check if this method is correctly overridden
    @Override
    public int hashCode() {
        return (this.getId()+this.getCommonName()+this.getReadingType()).hashCode();
    }
}
