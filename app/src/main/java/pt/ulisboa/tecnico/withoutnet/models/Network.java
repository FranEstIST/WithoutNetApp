package pt.ulisboa.tecnico.withoutnet.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Network implements Serializable {
    private int id;
    private String name;
    private List<Node> nodes;

    public Network(int id, String name, List<Node> nodes) {
        this.id = id;
        this.name = name;
        this.nodes = nodes;
    }

    public Network(int id, String name) {
        this.id = id;
        this.name = name;
        this.nodes = new ArrayList<>();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Node> getNodes() {
        return nodes;
    }

    public void setNodes(List<Node> nodes) {
        this.nodes = nodes;
    }
}
