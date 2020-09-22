package com.example.AlejaGuidanceSystem;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

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

import java.util.Collection;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements Scene.OnUpdateListener {

	// AR-Fragment is responsible for hosting the scene.
	private CustomArFragment arFragment;
	// red- and blue-sphere-renderable.
	private ModelRenderable rsr;
	private ModelRenderable bsr;
	// list of spheres on display, so that they can be removed from the scene when new ones are loaded.
	private ArrayList<AnchorNode> balls;
	// list of spheres representing camera positions.
	private ArrayList<AnchorNode> pathBalls;

	private List<Node> allMapNodes;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		arFragment = (CustomArFragment) getSupportFragmentManager().findFragmentById(R.id.fragment);
		// adding a listener so that this activity can react to updates in the fragment
		arFragment.getArSceneView().getScene().addOnUpdateListener(this);


		findViewById(R.id.addToBranchButton).setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				if(cameraPosition == null) return;

				Node node = new Node();
				node.connectedTo = new ArrayList();
				if(lastFocusedNode != null) node.connectedTo.add(lastFocusedNode);
				node.x = cameraPosition[0];
				node.y = cameraPosition[1];
				node.z = cameraPosition[2];

				addMapNode(node);

				lastFocusedNode = node;
			}
		});

		balls = new ArrayList<>();
		pathBalls = new ArrayList<>();

		// creating the spheres
		MaterialFactory.makeOpaqueWithColor(this, new Color(android.graphics.Color.RED))
				.thenAccept(
						material -> rsr = ShapeFactory.makeSphere(0.02f, new Vector3(0.0f, 0.0f, 0.0f), material)
				);
		MaterialFactory.makeOpaqueWithColor(this, new Color(android.graphics.Color.BLUE))
				.thenAccept(
						material -> bsr = ShapeFactory.makeSphere(0.01f, new Vector3(0.0f, 0.0f, 0.0f), material)
				);

	}

	/**
	 *
	 * @param config
	 * @param session
	 */
	public void setupDatabase(Config config, Session session) {
		Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ar_pattern);
		AugmentedImageDatabase aid = new AugmentedImageDatabase(session);
		aid.addImage("ar_pattern", bitmap, 0.2f);
		config.setAugmentedImageDatabase(aid);
	}

	private int counter = 0;

	private float[] lastPosition = null;


	private float[] cameraPosition = null;
	private Node lastFocusedNode = null;

	private Session trackable = null;
	private Pose trackableToWorld = null;
	private Pose trackableToReference = null;


	private float[] v3diff(float[] a, float[] b) {
		return new float[] { a[0] - b[0], a[1] - b[1], a[2] - b[2] };
	}

	private float[] v3div(float[] a, float f) {
		return new float[] { a[0] / f, a[1] / f, a[2] / f };
	}

	private float v3dot(float[] a, float[] b) {
		return a[0] * b[0] + a[1] * b[1] + a[2] * b[2];
	}

	private float v3dist(float[] a, float[] b) {
		float[] dif = v3diff(a, b);
		return (float) Math.sqrt(v3dot(dif, dif));
	}

	private float v3length(float[] a) {
		return (float) Math.sqrt(v3dot(a, a));
	}

	private float[] v3normalize(float[] a) {
		float length = v3length(a);
		return v3div(a, length + 0.001f);
	}

	private static class Node {
		public String id;
		public List<Node> connectedTo;
		public float x, y, z;
	}

	@Override
	public void onUpdate(FrameTime frameTime) {

		Session session = arFragment.getArSceneView().getSession();
		Frame frame = arFragment.getArSceneView().getArFrame();
		Collection<AugmentedImage> images = frame.getUpdatedTrackables(AugmentedImage.class);

		Log.d("MyApp", "onUpdate");


		Pose cameraToWorld = frame.getCamera().getPose();

		if (trackable != null) {
			Pose referenceToWorld = trackableToWorld.compose(trackableToReference.inverse());

			Pose cameraToReference = referenceToWorld.inverse().compose(cameraToWorld);
			cameraPosition = cameraToReference.transformPoint(new float[]{0.0f, 0.0f, 0.0f});

			if(lastPosition == null || v3dist(cameraPosition, lastPosition) > 0.2) {
				lastPosition = new float[] { cameraPosition[0], cameraPosition[1], cameraPosition[2] } ;

				createBall(trackable.createAnchor(referenceToWorld.compose(Pose.makeTranslation(lastPosition))), pathBalls, bsr);
			}

			String logString = String.format("Camera position %.3f %.3f %.3f", cameraPosition[0], cameraPosition[1], cameraPosition[2]);
			Log.d("MyApp2", logString);


			TextView myAwesomeTextView = (TextView) findViewById(R.id.textView);
			myAwesomeTextView.setText(logString);

			counter = 0;
		}

		counter++;


		for (AugmentedImage image : images) {
			if (image.getTrackingState() == TrackingState.TRACKING) {
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



				for (int i = 0; i < 40; i++) {
					Pose upPose = Pose.makeTranslation(0, i * 0.2f, 0);
					//Pose combinedPose = upPose.compose(anchorToWorld); // Pose.makeTranslation(0, i * 0.1f, 0);
					//Anchor anchor = session.createAnchor(combinedPose);
					Anchor anchor = trackable.createAnchor(trackableToWorld.compose(upPose));
					createBall(anchor, balls, rsr);
				}
			}
		}
	}

	private void createBall(Anchor anchor, List<AnchorNode> myBalls, Renderable renderable) {
		AnchorNode anchorNode = new AnchorNode(anchor);
		anchorNode.setRenderable(renderable);
		arFragment.getArSceneView().getScene().addChild(anchorNode);
		myBalls.add(anchorNode);

	}

	private void removeBalls(List<AnchorNode> myBallsToRemove) {
		for (AnchorNode ball : myBallsToRemove) {
			arFragment.getArSceneView().getScene().removeChild(ball);
		}
	}


	private void addMapNode(Node node) {
		allMapNodes.add(node);
	}
}