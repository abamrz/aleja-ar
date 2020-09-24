package com.example.AlejaGuidanceSystem;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.example.AlejaGuidanceSystem.Utility.GraphicsUtility;
import com.example.AlejaGuidanceSystem.Utility.ObjectInReference;
import com.example.AlejaGuidanceSystem.Utility.VectorOperations;
import com.example.AlejaGuidanceSystem.graph.Node;
import com.google.ar.core.AugmentedImage;
import com.google.ar.core.AugmentedImageDatabase;
import com.google.ar.core.Config;
import com.google.ar.core.Frame;
import com.google.ar.core.Pose;
import com.google.ar.core.Session;
import com.google.ar.core.TrackingState;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.FrameTime;
import com.google.ar.sceneform.Scene;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.rendering.Color;
import com.google.ar.sceneform.rendering.MaterialFactory;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.rendering.ShapeFactory;

import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;

import java.util.Collection;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MakePlanActivity extends AppCompatActivity implements Scene.OnUpdateListener {

	// AR-Fragment is responsible for hosting the scene.
	private CustomArFragment arFragment;
	// red- and blue-sphere-renderable.
	private ModelRenderable rsr;
	private ModelRenderable bsr;
	private ModelRenderable lbsr;

	// list of spheres representing camera positions.
	private ArrayList<ObjectInReference> pathBalls;

	private Graph<Node, DefaultWeightedEdge> graph;
	private boolean regenerateScene = false;

	private int nodeIdCounter = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_make_plan);

		arFragment = (CustomArFragment) getSupportFragmentManager().findFragmentById(R.id.fragment);
		// adding a listener so that this activity can react to updates in the fragment
		arFragment.getArSceneView().getScene().addOnUpdateListener(this);


		findViewById(R.id.addToBranchButton).setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				if (cameraPosition == null) return;

				Node node = new Node(cameraPosition, "node" + nodeIdCounter);

				graph.addVertex(node);

				if (lastFocusedNode != null) {
					graph.addEdge(lastFocusedNode, node);
				}
				lastFocusedNode = node;

				nodeIdCounter++;

				regenerateScene = true;
			}
		});


		findViewById(R.id.newBranchButton).setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				if (cameraPosition == null) return;

				NearestPointInfo npi = nearestPointInGraph(graph, cameraPosition);
				if(npi == null) return;

				Node source = graph.getEdgeSource(npi.bestEdge);
				Node target = graph.getEdgeTarget(npi.bestEdge);

				Node chosenNode = null;
				if(npi.interpolatingFactor < 0.0001) {
					chosenNode = source;
				}
				if(npi.interpolatingFactor > 0.9999) {
					chosenNode = target;
				}

				if(chosenNode == null) {
					graph.removeEdge(source, target);

					chosenNode = new Node(npi.nearestPosition, "node" + nodeIdCounter);
					nodeIdCounter++;

					graph.addVertex(chosenNode);

					graph.addEdge(source, chosenNode);
					graph.addEdge(chosenNode, target);
				}

				Node node = new Node( cameraPosition, "node" + nodeIdCounter );
				graph.addVertex(node);
				graph.addEdge(chosenNode, node);
				lastFocusedNode = node;

				nodeIdCounter++;

				regenerateScene = true;
			}
		});

		findViewById(R.id.closeCircleButton).setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				if (cameraPosition == null) return;
				if (lastFocusedNode == null) return;

				NearestPointInfo npi = nearestPointInGraph(graph, cameraPosition);
				if(npi == null) return;

				Node source = graph.getEdgeSource(npi.bestEdge);
				Node target = graph.getEdgeTarget(npi.bestEdge);

				Node chosenNode = null;
				if(npi.interpolatingFactor < 0.0001) {
					chosenNode = graph.getEdgeSource(npi.bestEdge);
				}
				if(npi.interpolatingFactor > 0.9999) {
					chosenNode = graph.getEdgeTarget(npi.bestEdge);
				}

				if(chosenNode == null) {
					graph.removeEdge(source, target);

					chosenNode = new Node(npi.nearestPosition, "node" + nodeIdCounter);
					nodeIdCounter++;

					graph.addVertex(chosenNode);

					graph.addEdge(source, chosenNode);
					graph.addEdge(chosenNode, target);
				}

				if(chosenNode == lastFocusedNode) return;

				graph.addEdge(chosenNode, lastFocusedNode);
				lastFocusedNode = null;

				nodeIdCounter++;

				regenerateScene = true;
			}
		});

		pathBalls = new ArrayList<>();
		graph = new SimpleWeightedGraph<Node, DefaultWeightedEdge>(DefaultWeightedEdge.class);

		// creating the spheres
		MaterialFactory.makeOpaqueWithColor(this, new Color(android.graphics.Color.RED))
				.thenAccept(
						material -> rsr = ShapeFactory.makeSphere(0.02f, new Vector3(0.0f, 0.0f, 0.0f), material)
				);
		MaterialFactory.makeOpaqueWithColor(this, new Color(android.graphics.Color.BLUE))
				.thenAccept(
						material -> bsr = ShapeFactory.makeSphere(0.005f, new Vector3(0.0f, 0.0f, 0.0f), material)
				);
		MaterialFactory.makeOpaqueWithColor(this, new Color(android.graphics.Color.GREEN))
				.thenAccept(
						material -> lbsr = ShapeFactory.makeSphere(0.02f, new Vector3(0.0f, 0.0f, 0.0f), material)
				);


	}


	/**
	 * setting up the Augmented Images Database by adding images that should be detected.
	 */
	public void setupDatabase(Config config, Session session) {
		// test image
		Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ar_pattern);

		AugmentedImageDatabase aid = new AugmentedImageDatabase(session);
		// adding Augmented Images to Database
		aid.addImage("ar_pattern", bitmap, 0.2f);

		config.setAugmentedImageDatabase(aid);
	}

	private float[] lastPosition = null;
	private float[] cameraPosition = null;
	private Node lastFocusedNode = null;

	private Session trackable = null;
	private Pose trackableToWorld = null;
	private Pose trackableToReference = null;
	private Pose referenceToWorld = null;


	/**
	 * function that will be called every time the camera frame updates
	 */
	@Override
	public void onUpdate(FrameTime frameTime) {
		// finding the current fragment of the scene
		Session session = arFragment.getArSceneView().getSession();
		Frame frame = arFragment.getArSceneView().getArFrame();
		// all the items ARCore has tracked
		Collection<AugmentedImage> images = frame.getUpdatedTrackables(AugmentedImage.class);

		// Log.d("MyApp", "onUpdate");



		// checking all detected images for one of the reference pictures
		for (AugmentedImage image : images) {
			if (image.getTrackingState() == TrackingState.TRACKING) {

				//Trackable bla =  (Trackable)session;
				Log.d("MyApp", "tracked " + image.getName());
				Log.d("MyApp", "Pose: " + image.getCenterPose().toString());

				/*trackable = image;
				trackableToWorld = image.getCenterPose();
				trackableToReference = Pose.makeTranslation(0, 100, 0);*/

				trackable = session;
				trackableToWorld = image.getCenterPose();
				trackableToReference = Pose.makeTranslation(0, 100, 0);
				referenceToWorld = trackableToWorld.compose(trackableToReference.inverse());

				for(ObjectInReference obj : pathBalls) {
					obj.recalculatePosition(referenceToWorld);
				}

				// displaying a straight line of spheres
				/*for (int i = 0; i < 40; i++) {
					Pose upPose = Pose.makeTranslation(0, i * 0.2f, 0);
					//Pose combinedPose = upPose.compose(anchorToWorld); // Pose.makeTranslation(0, i * 0.1f, 0);
					//Anchor anchor = session.createAnchor(combinedPose);
					Anchor anchor = trackable.createAnchor(trackableToWorld.compose(upPose));
					createBall(anchor, balls, rsr);
				}*/
			}
		}

		if(trackable != null && regenerateScene && frame.getCamera().getTrackingState() == TrackingState.TRACKING) {
			regeneratePathBalls();
			regenerateScene = false;
		}

		if (trackable != null) {
			Pose cameraToWorld = frame.getCamera().getPose();
			Pose cameraToReference = referenceToWorld.inverse().compose(cameraToWorld);
			cameraPosition = cameraToReference.transformPoint(new float[]{0.0f, 0.0f, 0.0f});

			/*if (lastPosition == null || v3dist(cameraPosition, lastPosition) > 0.2) {
				lastPosition = new float[]{cameraPosition[0], cameraPosition[1], cameraPosition[2]};

				createBall(trackable.createAnchor(referenceToWorld.compose(Pose.makeTranslation(lastPosition))), pathBalls, bsr);
			}*/

			// printing the camera position to console and to screen of device running the app


			int sceneformChildren = 			arFragment.getArSceneView().getScene().getChildren().size();
			int numAnchors = session.getAllAnchors().size();


			String logString = String.format(Locale.GERMAN, "Camera position %.3f %.3f %.3f %d %d", cameraPosition[0], cameraPosition[1], cameraPosition[2], sceneformChildren, numAnchors);
			Log.d("MyApp2", logString);

			TextView myAwesomeTextView = (TextView) findViewById(R.id.textView);
			myAwesomeTextView.setText(logString);
		}


		if(cameraPosition != null) {
			NearestPointInfo nearestPointInfo = nearestPointInGraph(graph, cameraPosition);

			if(nearestPointInfo != null && frame.getCamera().getTrackingState() == TrackingState.TRACKING) {
				Pose nodePose = referenceToWorld.compose(Pose.makeTranslation(nearestPointInfo.nearestPosition));

				if(nearestPosNode == null) {
					nearestPosNode = new AnchorNode();
					nearestPosNode.setRenderable(rsr);
					VectorOperations.applyPoseToAnchorNode(nearestPosNode, nodePose);
					arFragment.getArSceneView().getScene().addChild(nearestPosNode);
				} else {
					VectorOperations.applyPoseToAnchorNode(nearestPosNode, nodePose);
				}
			}
		}

	}

	private static class NearestPointInfo {
		float distance;
		DefaultWeightedEdge bestEdge;
		float interpolatingFactor;
		float[] nearestPosition;
	}

	private NearestPointInfo nearestPointInGraph(Graph<Node, DefaultWeightedEdge> graph, float[] probePosition) {

		float shortestDist = Float.MAX_VALUE;
		DefaultWeightedEdge bestEdge = null;
		float bestInterpolatingFactor = 0;
		float[] nearestPosition = null;

		for(DefaultWeightedEdge e : graph.edgeSet()) {
			Node source = graph.getEdgeSource(e);
			Node target = graph.getEdgeTarget(e);

			float[] sourcePos = source.getPositionF();
			float[] targetPos = target.getPositionF();

			float f = VectorOperations.nearestPointToLine(sourcePos, targetPos, probePosition);
			if(f < 0) f = 0;
			if(f > 1) f = 1;

			float[] pos = VectorOperations.v3interpolate(sourcePos, targetPos, f);

			float dist = VectorOperations.v3dist(probePosition, pos);
			if(bestEdge == null || dist < shortestDist) {
				bestEdge = e;
				shortestDist = dist;
				nearestPosition = pos;
				bestInterpolatingFactor = f;
			}
		}

		if(nearestPosition == null) return null;

		NearestPointInfo nearestPointInfo = new NearestPointInfo();
		nearestPointInfo.bestEdge = bestEdge;
		nearestPointInfo.distance = shortestDist;
		nearestPointInfo.interpolatingFactor = bestInterpolatingFactor;
		nearestPointInfo.nearestPosition = nearestPosition;
		return nearestPointInfo;
	}

	private AnchorNode nearestPosNode = null;


	private void regeneratePathBalls() {
		 GraphicsUtility.removeMyBalls(arFragment.getArSceneView().getScene(), pathBalls);

		for(Node node : graph.vertexSet()) {
			GraphicsUtility.createBallInReference(node.getPositionF(), pathBalls, lbsr, arFragment.getArSceneView().getScene(), referenceToWorld);
		}


		for(DefaultWeightedEdge e : graph.edgeSet()) {
			Node source = graph.getEdgeSource(e);
			Node target = graph.getEdgeTarget(e);

			float dist = VectorOperations.v3dist(source.getPositionF(), target.getPositionF());
			float sepDist = 0.025f;
			int numSep = (int)Math.ceil(dist / sepDist);

			float stepDist = dist / (numSep);
			float[] sourcePos = source.getPositionF();
			float[] targetPos = target.getPositionF();
			float[] dir = VectorOperations.v3normalize(VectorOperations.v3diff(targetPos, sourcePos));
			for(int i = 1; i < numSep; i++) {
				float[] pos = VectorOperations.v3add(sourcePos, VectorOperations.v3mulf(dir, stepDist * i));
				GraphicsUtility.createBallInReference(pos, pathBalls, bsr, arFragment.getArSceneView().getScene(), referenceToWorld);
			}
		}

	}
}