package pt.ulisboa.tecnico.withoutnet.ui.login;

import java.util.UUID;

public class Node {
    private String commonName;
    private String UUID;

    public Node(String commonName, String UUID) {
        this.commonName = commonName;
        this.UUID = UUID;
    }

    public String getCommonName() {
        return commonName;
    }

    public void setCommonName(String commonName) {
        this.commonName = commonName;
    }

    public String getUUID() {
        return UUID;
    }

    public void setUUID(String UUID) {
        this.UUID = UUID;
    }
}
