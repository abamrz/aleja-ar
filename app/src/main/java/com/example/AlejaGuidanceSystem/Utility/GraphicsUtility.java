package com.example.AlejaGuidanceSystem.Utility;

import com.google.ar.core.Anchor;
import com.google.ar.core.Pose;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.Scene;
import com.google.ar.sceneform.rendering.Renderable;

import java.util.List;

public class GraphicsUtility {

	public static AnchorNode createBallInWorld(float[] positionInWorld, List<AnchorNode> myBalls, Renderable renderable,
												   Scene scene) {

		AnchorNode anchorNode = new AnchorNode();
		anchorNode.setRenderable(renderable);
		anchorNode.setWorldPosition(VectorOperations.vectorFromArray(positionInWorld));
		scene.addChild(anchorNode);
		myBalls.add(anchorNode);

		return anchorNode;
	}

	public static AnchorNode createBallInReference(float[] positionInReference, List<ObjectInReference> myBalls, Renderable renderable,
											Scene scene, Pose referenceToWorld) {
		Pose nodePose = Pose.makeTranslation(positionInReference);

		AnchorNode anchorNode = new AnchorNode();
		anchorNode.setRenderable(renderable);
		scene.addChild(anchorNode);

		ObjectInReference obj = new ObjectInReference(anchorNode, nodePose);
		obj.recalculatePosition(referenceToWorld);
		myBalls.add(obj);

		return anchorNode;
	}

	public static void removeMyBalls(Scene scene, List<ObjectInReference> myBallsToRemove) {

		for (ObjectInReference ball : myBallsToRemove) {
			scene.removeChild(ball.getNode());
		}
		myBallsToRemove.clear();
	}

	public static void removeMyBallsInWorld(Scene scene, List<AnchorNode> myBallsToRemove) {

		for (AnchorNode ball : myBallsToRemove) {
			scene.removeChild(ball);
		}
		myBallsToRemove.clear();
	}

}
