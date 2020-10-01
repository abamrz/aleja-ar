package com.example.AlejaGuidanceSystem.Utility;

import android.content.Context;
import android.net.Uri;
import android.view.Gravity;
import android.widget.Toast;

import com.example.AlejaGuidanceSystem.Graph.Node;
import com.google.ar.core.Pose;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.Scene;
import com.google.ar.sceneform.assets.RenderableSource;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.rendering.Renderable;

import java.util.concurrent.CompletableFuture;

public class Label3D extends Label{



    private Label3D(Node node, ObjectInReference objectInReference) {
        super(node);
    }

    static public CompletableFuture<Label3D> createDuck(Scene scene, Pose pose, Node node, Context context){
        final String GLTF_ASSET = "525 Toilet Roll.gltf";

        /* When you build a Renderable, Sceneform loads model and related resources
         * in the background while returning a CompletableFuture.
         * Call thenAccept(), handle(), or check isDone() before calling get().
         */
         return ModelRenderable.builder()
                .setSource(context, RenderableSource.builder().setSource(
                        context,
                        Uri.parse(GLTF_ASSET),
                        RenderableSource.SourceType.GLTF2)
                        .setScale(0.5f)  // Scale the original model to 50%.
                        .setRecenterMode(RenderableSource.RecenterMode.ROOT)
                        .build())
                .setRegistryId(GLTF_ASSET)
                .build()
                .thenApply((Renderable renderable) -> {

                    AnchorNode anchor = new AnchorNode();
                    anchor.setRenderable(renderable);
                    scene.addChild(anchor);
                    ObjectInReference objectInReference = new ObjectInReference(anchor, pose);

                    return new Label3D(node, objectInReference);
                })
                .exceptionally(
                        throwable -> {
                            Toast toast =
                                    Toast.makeText(context, "Unable to load renderable " +
                                            GLTF_ASSET, Toast.LENGTH_LONG);
                            toast.setGravity(Gravity.CENTER, 0, 0);
                            toast.show();
                            return null;
                        });
    }
}
