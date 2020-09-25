package com.example.AlejaGuidanceSystem.graph;


import androidx.annotation.Nullable;

import java.io.Serializable;


public class Node implements Serializable {

    private double x, y, z;
    private String id;
    private NodeType type = NodeType.WAYPOINT;
    private String label;


    public Node(double x, double y, double z, String id) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.id = id;
    }

    public Node(float[] v, String id) {
        this.x = v[0];
        this.y = v[1];
        this.z = v[2];
        this.id = id;
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
