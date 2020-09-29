package com.example.AlejaGuidanceSystem.graph;

import java.util.ArrayList;
import java.util.List;

public class ARGraphWithGrip {

    public static class WeakGrip {
        public float[] gripPosition;

        public WeakGrip(float[] gripPosition) {
            this.gripPosition = gripPosition;
        }
    }

    /**
     * This is an anchor which also provides direction information (up, forward, right)
     */
    public static class StrongGrip extends WeakGrip {

        public float[] rotationQuaternion;

        public StrongGrip(float[] gripPosition, float[] rotationQuaternion) {
            super(gripPosition);
            this.rotationQuaternion = rotationQuaternion;
        }
    }

    private List<WeakGrip> grips = new ArrayList<>();
    private ARGraph graph = new ARGraph();

    private ARGraphWithGrip() {
    }
}
