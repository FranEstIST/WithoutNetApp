package pt.ulisboa.tecnico.withoutnet.models;

import java.util.List;

public class Network {
    private int id;
    private String name;
    private List<Node> nodes;

    public Network(int id, String name, List<Node> nodes) {
        this.id = id;
        this.name = name;
        this.nodes = nodes;
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
