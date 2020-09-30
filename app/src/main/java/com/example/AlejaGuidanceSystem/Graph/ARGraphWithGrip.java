package com.example.AlejaGuidanceSystem.Graph;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ARGraphWithGrip implements Serializable {

    public static class WeakGrip implements Serializable {
        public String gripId;
        public float[] gripPosition;

        public WeakGrip(String gripId, float[] gripPosition) {
            this.gripId = gripId;
            this.gripPosition = gripPosition;
        }
    }

    /**
     * This is an anchor which also provides direction information (up, forward, right)
     */
    public static class StrongGrip extends WeakGrip implements Serializable {

        public float[] rotationQuaternion;

        public StrongGrip(String gripId, float[] gripPosition, float[] rotationQuaternion) {
            super(gripId, gripPosition);
            this.rotationQuaternion = rotationQuaternion;
        }
    }

    private List<WeakGrip> grips = new ArrayList<>();
    private ARGraph graph = new ARGraph();

    public ARGraphWithGrip(ARGraph graph, List<WeakGrip> grips) {
        this.graph = graph;
        this.grips = grips;
    }

    public List<WeakGrip> getGrips() {
        return this.grips;
    }

    public ARGraph getGraph() {
        return this.graph;
    }
}
