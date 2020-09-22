package com.example.AlejaGuidanceSystem.graph;


import androidx.annotation.Nullable;


public class Node {

    double x, y, z;
    String id;


    public Node(double x, double y, double z, String id) {
    this.x=x;
    this.y=y;
    this.z=z;
    this.id=id;
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
        return new float[] {(float)x, (float)y, (float)z};
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


    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Node node = (Node) obj;
        return x==node.x && y==node.y && z==node.z;
    }
}
