package com.example.AlejaGuidanceSystem;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.example.AlejaGuidanceSystem.graph.Node;
import com.google.ar.core.Anchor;
import com.google.ar.core.AugmentedImage;
import com.google.ar.core.AugmentedImageDatabase;
import com.google.ar.core.Config;
import com.google.ar.core.Frame;
import com.google.ar.core.Pose;
import com.google.ar.core.Session;
import com.google.ar.core.Trackable;
import com.google.ar.core.TrackingState;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.FrameTime;
import com.google.ar.sceneform.Scene;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.rendering.Color;
import com.google.ar.sceneform.rendering.MaterialFactory;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.rendering.Renderable;
import com.google.ar.sceneform.rendering.ShapeFactory;

import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;

import java.util.Collection;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements Scene.OnUpdateListener {

	// AR-Fragment is responsible for hosting the scene.
	private CustomArFragment arFragment;
	// red- and blue-sphere-renderable.
	private ModelRenderable rsr;
	private ModelRenderable bsr;
	private ModelRenderable lbsr;
	// list of spheres on display, so that they can be removed from the scene when new ones are loaded.
	private ArrayList<AnchorNode> balls;
	// list of spheres representing camera positions.
	private ArrayList<AnchorNode> pathBalls;

	private Graph<Node, DefaultWeightedEdge> graph;

	private int nodeIdCounter = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		arFragment = (CustomArFragment) getSupportFragmentManager().findFragmentById(R.id.fragment);
		// adding a listener so that this activity can react to updates in the fragment
		arFragment.getArSceneView().getScene().addOnUpdateListener(this);


		findViewById(R.id.addToBranchButton).setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				if (cameraPosition == null) return;


				Node node = new Node(
						cameraPosition[0],
						cameraPosition[1],
						cameraPosition[2],
						"node" + nodeIdCounter
				);

				graph.addVertex(node);

				if (lastFocusedNode != null) {
					graph.addEdge(lastFocusedNode, node);
				}
				lastFocusedNode = node;

				nodeIdCounter++;

				regeneratePathBalls();
			}
		});

		balls = new ArrayList<>();
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

//		Log.d("MyApp", "onUpdate");

		Pose cameraToWorld = frame.getCamera().getPose();

		if (trackable != null) {
			Pose referenceToWorld = trackableToWorld.compose(trackableToReference.inverse());

			Pose cameraToReference = referenceToWorld.inverse().compose(cameraToWorld);
			cameraPosition = cameraToReference.transformPoint(new float[]{0.0f, 0.0f, 0.0f});

			/*if (lastPosition == null || v3dist(cameraPosition, lastPosition) > 0.2) {
				lastPosition = new float[]{cameraPosition[0], cameraPosition[1], cameraPosition[2]};

				createBall(trackable.createAnchor(referenceToWorld.compose(Pose.makeTranslation(lastPosition))), pathBalls, bsr);
			}*/

			// printing the camera position to console and to screen of device running the app
			String logString = String.format(Locale.GERMAN, "Camera position %.3f %.3f %.3f", cameraPosition[0], cameraPosition[1], cameraPosition[2]);
			Log.d("MyApp2", logString);

			TextView myAwesomeTextView = (TextView) findViewById(R.id.textView);
			myAwesomeTextView.setText(logString);
		}

		// checking all detected images for one of the reference pictures
		for (AugmentedImage image : images) {
			if (image.getTrackingState() == TrackingState.TRACKING) {
				// removing old balls from screen so that they don't have to be rendered in
				removeBalls(balls);

				//Trackable bla =  (Trackable)session;
				Log.d("MyApp", "tracked " + image.getName());
				Log.d("MyApp", "Pose: " + image.getCenterPose().toString());

				/*trackable = image;
				trackableToWorld = image.getCenterPose();
				trackableToReference = Pose.makeTranslation(0, 100, 0);*/

				trackable = session;
				trackableToWorld = image.getCenterPose();
				trackableToReference = Pose.makeTranslation(0, 100, 0);

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
	}

	/**
	 * Method to place a sphere to a certain anchor
	 * @param anchor where sphere is placed
	 * @param myBalls the list of balls this sphere belongs to
	 * @param renderable the sphere that should be added to the anchor
	 */
	private void createBall(Anchor anchor, List<AnchorNode> myBalls, Renderable renderable) {
		AnchorNode anchorNode = new AnchorNode(anchor);
		anchorNode.setRenderable(renderable);
		arFragment.getArSceneView().getScene().addChild(anchorNode);
		myBalls.add(anchorNode);

	}

	/**
	 * Method to remove all renderable spheres from a list of balls
	 */
	private void removeBalls(List<AnchorNode> myBallsToRemove) {
		for (AnchorNode ball : myBallsToRemove) {
			arFragment.getArSceneView().getScene().removeChild(ball);
		}
	}

	private void createBallInReference(float[] positionInReference, List<AnchorNode> myBalls, Renderable renderable) {
		Pose referenceToWorld = trackableToWorld.compose(trackableToReference.inverse());
		createBall(trackable.createAnchor(referenceToWorld.compose(Pose.makeTranslation(positionInReference))), myBalls, renderable);
	}




	private void regeneratePathBalls() {
		removeBalls(pathBalls);

		for(Node node : graph.vertexSet()) {
			createBallInReference(node.getPositionF(), pathBalls, lbsr);
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
				createBallInReference(pos, pathBalls, bsr);
			}
		}

	}
}