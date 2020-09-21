package com.example.alejatest1;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;

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
import com.google.ar.sceneform.rendering.ShapeFactory;

import java.util.Collection;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity implements Scene.OnUpdateListener {

	private CustomArFragment arFragment;
	private ModelRenderable rsr;
	private ArrayList<AnchorNode> balls;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		arFragment = (CustomArFragment) getSupportFragmentManager().findFragmentById(R.id.fragment);
		arFragment.getArSceneView().getScene().addOnUpdateListener(this);

		balls = new ArrayList<AnchorNode>();

		MaterialFactory.makeOpaqueWithColor(this, new Color(android.graphics.Color.RED))
				.thenAccept(
						material -> {
							rsr = ShapeFactory.makeSphere(0.05f, new Vector3(0.0f, 0.0f,0.0f), material);
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

	@Override
	public void onUpdate(FrameTime frameTime) {

		Frame frame = arFragment.getArSceneView().getArFrame();
		Collection<AugmentedImage> images = frame.getUpdatedTrackables(AugmentedImage.class);

		Log.d("MyApp", "onUpdate");



		if(anchorToWorld != null && counter > 10) {

			Pose cameraToWorld = frame.getCamera().getPose();
			Pose referenceToAnchor = Pose.makeTranslation(0,0.3f, 0);

			Pose cameraToReference = cameraToWorld.compose(anchorToWorld.inverse());

			float[] cameraPosition = cameraToReference.transformPoint(new float[]{0.0f, 0.0f, 0.0f});

			Log.d("MyApp2", String.format("Camera position %.3f %.3f %.3f", cameraPosition[0], cameraPosition[1], cameraPosition[2]));

			counter = 0;
		}

		counter++;



		for(AugmentedImage image : images) {
			if(image.getTrackingState() == TrackingState.TRACKING) {
				for (AnchorNode ball : balls) {
					arFragment.getArSceneView().getScene().removeChild(ball);
				}

				Log.d("MyApp", "tracked " + image.getName());
				Log.d("MyApp", "Pose: " + image.getCenterPose().toString());

				Anchor anchorx = image.createAnchor(image.getCenterPose());

				anchorToWorld  = anchorx.getPose();

				for(int i=0; i<15; i++) {
					Pose upPose = Pose.makeTranslation(0, i*0.1f, 0);
					Anchor anchor = image.createAnchor(image.getCenterPose().compose(upPose));
					createBall(anchor);
				}
			}
		}
	}

	private void createBall(Anchor anchor) {
			AnchorNode anchorNode = new AnchorNode(anchor);
			anchorNode.setRenderable(rsr);
			arFragment.getArSceneView().getScene().addChild(anchorNode);
			balls.add(anchorNode);

	}
}