package com.example.alejatest1;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.google.ar.core.Anchor;
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
import com.google.ar.sceneform.rendering.Renderable;
import com.google.ar.sceneform.rendering.ShapeFactory;

import java.util.Collection;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity implements Scene.OnUpdateListener {

	// AR-Fragment is responsible for hosting the scene.
	private CustomArFragment arFragment;
	// red-sphere-Renderable.
	private ModelRenderable rsr;
	private ModelRenderable bsr;
	// list of spheres on display, so that they can be removed from the scene when new ones are loaded
	private ArrayList<AnchorNode> balls;

	private ArrayList<AnchorNode> pathBalls;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		arFragment = (CustomArFragment) getSupportFragmentManager().findFragmentById(R.id.fragment);
		arFragment.getArSceneView().getScene().addOnUpdateListener(this);

		balls = new ArrayList<>();
		pathBalls = new ArrayList<>();

		MaterialFactory.makeOpaqueWithColor(this, new Color(android.graphics.Color.RED))
				.thenAccept(
						material -> {
							rsr = ShapeFactory.makeSphere(0.02f, new Vector3(0.0f, 0.0f, 0.0f), material);
						}
				);

		MaterialFactory.makeOpaqueWithColor(this, new Color(android.graphics.Color.BLUE))
				.thenAccept(
						material -> {
							bsr = ShapeFactory.makeSphere(0.01f, new Vector3(0.0f, 0.0f, 0.0f), material);
						}
				);

	}

	public void setupDatabase(Config config, Session session) {
		Bitmap foxBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ar_pattern);
		AugmentedImageDatabase aid = new AugmentedImageDatabase(session);
		aid.addImage("ar_pattern", foxBitmap, 0.2f);
		config.setAugmentedImageDatabase(aid);
	}

	private Pose anchorToWorld = null;
	private int counter = 0;

	private float[] lastPosition = null;

	private float dist(float[] a, float[] b) {
		return (float) Math.sqrt((a[0] - b[0]) * (a[0] - b[0]) + (a[1] - b[1]) * (a[1] - b[1])  + (a[2] - b[2]) * (a[2] - b[2]));
	}

	@Override
	public void onUpdate(FrameTime frameTime) {

		Session session = arFragment.getArSceneView().getSession();
		Frame frame = arFragment.getArSceneView().getArFrame();
		Collection<AugmentedImage> images = frame.getUpdatedTrackables(AugmentedImage.class);

		Log.d("MyApp", "onUpdate");

		TextView myAwesomeTextView = (TextView) findViewById(R.id.textView);


		Pose cameraToWorld = frame.getCamera().getPose();
		float[] cameraPosition2 = cameraToWorld.transformPoint(new float[]{0.0f, 0.0f, 0.0f});

		Anchor a = null;
		if(frame.getCamera().getTrackingState() == TrackingState.TRACKING) {

			a = session.createAnchor(Pose.makeTranslation(cameraPosition2[0], cameraPosition2[1], cameraPosition2[2]));
		}


		if (anchorToWorld != null && counter > 0) {

			Pose referenceToAnchor = Pose.makeTranslation(0, 0.3f, 0);

			Pose cameraToReference = anchorToWorld.inverse().compose(cameraToWorld);

			float[] cameraPosition = cameraToReference.transformPoint(new float[]{0.0f, 0.0f, 0.0f});

			if(lastPosition == null || dist(cameraPosition, lastPosition) > 0.2) {
				lastPosition = new float[] { cameraPosition[0], cameraPosition[1], cameraPosition[2] + 0.2f } ;

				createBall(session.createAnchor(anchorToWorld.compose(Pose.makeTranslation(lastPosition))), pathBalls, bsr);
			}

			String logString = String.format("Camera position %.3f %.3f %.3f", cameraPosition[0], cameraPosition[1], cameraPosition[2]);
			Log.d("MyApp2", logString);


			myAwesomeTextView.setText(logString);

			counter = 0;
		}

		counter++;


		for (AugmentedImage image : images) {
			if (image.getTrackingState() == TrackingState.TRACKING) {
				removeBalls(balls);

				Log.d("MyApp", "tracked " + image.getName());
				Log.d("MyApp", "Pose: " + image.getCenterPose().toString());

				Anchor anchorx = image.createAnchor(image.getCenterPose());

				anchorToWorld = anchorx.getPose();

				for (int i = 0; i < 40; i++) {
					Pose upPose = Pose.makeTranslation(0, i * 0.2f, 0);
					Pose combinedPose = upPose.compose(anchorToWorld); // Pose.makeTranslation(0, i * 0.1f, 0);
					//Anchor anchor = session.createAnchor(combinedPose);
					Anchor anchor = image.createAnchor(image.getCenterPose().compose(upPose));
					createBall(anchor, balls, rsr);
				}
			}
		}

		//if(a != null) createBall(a);
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
}