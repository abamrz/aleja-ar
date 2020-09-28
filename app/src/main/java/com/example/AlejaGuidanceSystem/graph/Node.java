package com.example.AlejaGuidanceSystem.graph;


import androidx.annotation.Nullable;

import java.io.Serializable;

import java.util.HashMap;
import java.util.Map;


public class Node implements Serializable {

    private double x, y, z;
    private String id;
    private NodeType type = NodeType.WAYPOINT;
    private String label;
    private String description;

    public static final Map<NodeType, String> typeStrings = new HashMap();


    public Node(double x, double y, double z, String id) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.id = id;

        typeStrings.put(NodeType.WAYPOINT, "Waypoint");
        typeStrings.put(NodeType.KITCHEN, "Kitchen");
        typeStrings.put(NodeType.EXIT, "Exit");
        typeStrings.put(NodeType.COFFEE, "Coffee");
        typeStrings.put(NodeType.OFFICE, "Office");
        typeStrings.put(NodeType.ELEVATOR, "Elevator");
        typeStrings.put(NodeType.TOILETTE, "Toilette");
        typeStrings.put(NodeType.FIRE_EXTINGUISHER, "Fire Extinguisher");
    }

    public Node(float[] v, String id) {
        this(v[0], v[1], v[2], id);
    }


    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getZ() {
        return z;
    }

    public float[] getPositionF() {
        return new float[]{(float) x, (float) y, (float) z};
    }

    public String getId() {
        return id;
    }

    public void setX(double x) {
        this.x = x;
    }

    public void setY(double y) {
        this.y = y;
    }

    public void setZ(double z) {
        this.z = z;
    }

    public void setId(String id) {
        this.id = id;
    }

    public NodeType getType() {
        return type;
    }

    public void setType(NodeType type) {
        this.type = type;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Node node = (Node) obj;
        return x == node.getX() && y == node.getY() && z == node.getZ() && id.equals(((Node) obj).getId());
    }

    public static enum NodeType {
        WAYPOINT, KITCHEN, EXIT, COFFEE, OFFICE, ELEVATOR, TOILETTE, FIRE_EXTINGUISHER;
    }

}
