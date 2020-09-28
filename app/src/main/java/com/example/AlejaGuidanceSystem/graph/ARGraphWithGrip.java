package com.example.AlejaGuidanceSystem.graph;

import java.util.ArrayList;
import java.util.List;

public class ARGraphWithGrip {

    public static class Grip {
        public String gripId;
        public float[] gripPosition;

        public Grip(String gripId, float[] gripPosition) {
            this.gripId = gripId;
            this.gripPosition = gripPosition;
        }
    }

    private List<Grip> grips = new ArrayList<>();
    private ARGraph graph = new ARGraph();

    private ARGraphWithGrip() {
    }
}
