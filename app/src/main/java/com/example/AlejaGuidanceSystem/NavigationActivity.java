package com.example.AlejaGuidanceSystem;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import com.example.AlejaGuidanceSystem.graph.Node;
import com.google.ar.core.AugmentedImage;
import com.google.ar.core.Frame;
import com.google.ar.core.Session;
import com.google.ar.core.TrackingState;
import com.google.ar.sceneform.FrameTime;
import com.google.ar.sceneform.Scene;
import com.google.ar.sceneform.rendering.ModelRenderable;

import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.alg.DijkstraShortestPath;

import java.util.Collection;


public class NavigationActivity extends AppCompatActivity {

	// AR
	private CustomArFragment arFragment;
	private ModelRenderable renderable;

	// plan for the area
	private Graph<Node, DefaultWeightedEdge> graph;

	@Override
	@SuppressWarnings({"AndroidApiChecker", "FutureReturnValueIgnored"})
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_navigation);

		arFragment = (CustomArFragment) getSupportFragmentManager().findFragmentById(R.id.fragment);
		arFragment.getArSceneView().getScene().addOnUpdateListener(this::onUpdateFrame);
	}


	/**
	 * listener-method, called every time the camera frame is updated
	 */
	@RequiresApi(api = Build.VERSION_CODES.N)
	private void onUpdateFrame(FrameTime frameTime) {
		// finding the current fragment of the scene
		Session session = arFragment.getArSceneView().getSession();
		Frame frame = arFragment.getArSceneView().getArFrame();
		// all the items ARCore has tracked
		Collection<AugmentedImage> images = frame.getUpdatedTrackables(AugmentedImage.class);

		for(AugmentedImage image : images) {
			if(image.getTrackingState() == TrackingState.TRACKING) {
				// checking if correct image was detected
				if(image.getName().equals("ar_pattern")) {
					Log.d("NavigationActivity", "Image 'ar_pattern' was detected.");
				}
			}
		}
	}
}