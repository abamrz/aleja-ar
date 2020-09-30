package com.example.AlejaGuidanceSystem.Utility;

import android.content.Context;

import com.google.ar.core.Pose;
import com.google.ar.sceneform.Scene;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.rendering.Color;
import com.google.ar.sceneform.rendering.MaterialFactory;
import com.google.ar.sceneform.rendering.Renderable;
import com.google.ar.sceneform.rendering.ShapeFactory;

import org.jgrapht.Graph;

import java.util.ArrayList;
import java.util.HashMap;

public class GripVisualisator {
    private HashMap<String, ArrayList<ObjectInReference>> gripVisualizations;
    private Scene scene;
    private Renderable sphere;

    public GripVisualisator(Context context, Scene scene) {
        this.gripVisualizations = new HashMap<>();
        this.scene = scene;

        MaterialFactory.makeOpaqueWithColor(context, new Color(android.graphics.Color.MAGENTA))
                .thenAccept(
                        material -> sphere = ShapeFactory.makeSphere(0.01f, new Vector3(0.0f, 0.0f, 0.0f), material)
                );
    }

    public void updateGrip(String gripId, Pose newGripToWorldPose) {
        ArrayList<ObjectInReference> existingObjects = gripVisualizations.get(gripId);
        if(existingObjects == null) {
            existingObjects = new ArrayList<>();
            GraphicsUtility.createBallInReference(new float[] { 0.03f, 0, 0}, existingObjects, sphere, scene, newGripToWorldPose);
            GraphicsUtility.createBallInReference(new float[] { 0, 0.03f, 0}, existingObjects, sphere, scene, newGripToWorldPose);
            GraphicsUtility.createBallInReference(new float[] { 0, 0, 0.03f}, existingObjects, sphere, scene, newGripToWorldPose);
            GraphicsUtility.createBallInReference(new float[] { 0, 0, 0}, existingObjects, sphere, scene, newGripToWorldPose);
            this.gripVisualizations.put(gripId, existingObjects);
        } else {
            for(ObjectInReference oir : existingObjects) {
                oir.recalculatePosition(newGripToWorldPose);
            }
        }

    }
}
