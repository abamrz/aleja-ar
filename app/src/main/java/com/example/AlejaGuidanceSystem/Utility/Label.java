package com.example.AlejaGuidanceSystem.Utility;

import android.view.View;

import com.example.AlejaGuidanceSystem.Graph.Node;

public abstract class Label {

    private Node node;
    private ObjectInReference objectInReference;

    public Label(Node node) {
        this.node = node;
    }

    public ObjectInReference getObjectInReference() {
        return this.objectInReference;
    }

    public void setObjectInReference(final ObjectInReference objectInReference) {
        this.objectInReference = objectInReference;
    }

    public Node getNode() {
        return this.node;
    }

    public void setEnabled(boolean enable){
        objectInReference.setEnable(enable);
    }
}
