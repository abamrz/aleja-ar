package com.example.AlejaGuidanceSystem;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;

import com.example.AlejaGuidanceSystem.Utility.GraphicsUtility;
import com.example.AlejaGuidanceSystem.Utility.ObjectInReference;
import com.example.AlejaGuidanceSystem.Utility.VectorOperations;
import com.example.AlejaGuidanceSystem.graph.Node;
import com.google.ar.core.AugmentedImage;
import com.google.ar.core.Frame;
import com.google.ar.core.Pose;
import com.google.ar.core.Session;
import com.google.ar.core.TrackingState;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.FrameTime;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.rendering.Color;
import com.google.ar.sceneform.rendering.MaterialFactory;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.rendering.Renderable;
import com.google.ar.sceneform.rendering.ShapeFactory;

import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.alg.DijkstraShortestPath;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


public class NavigationActivity extends AppCompatActivity {
	// Buttons
	private ImageButton return_button, search_button;

	// AR
	private CustomArFragment arFragment;
	private ModelRenderable renderable;

	// plan for the area
	private ARGraph graph;

	// red-, blue- and largeGreen-sphere
	private ModelRenderable rsr;
	private ModelRenderable bsr;
	private ModelRenderable lgsr;

	private Pose referenceToWorld = null;
	private ArrayList<ObjectInReference> pathBalls;

	@Override
	@SuppressWarnings({"AndroidApiChecker", "FutureReturnValueIgnored"})
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_navigation);

		//initialize the buttons
		return_button = (ImageButton) findViewById(R.id.return_button);
		search_button = (ImageButton) findViewById(R.id.search_button);

		return_button.setOnClickListener(new View.OnClickListener(){
			@Override
			public void onClick(View view){
				//TODO: Go back to WelcomeActivity
			}
		});
		search_button.setOnClickListener(new View.OnClickListener(){
			@Override
			public void onClick(View view){
				//TODO: Search functionality
			}
		});


		graph = new ARGraph();
		Node a = new Node(0,0,0,"a");
		Node b = new Node(2,0,0,"b");
		Node c = new Node(0,2,0,"c");

		graph.addVertex(a);
		graph.addVertex(b);
		graph.addVertex(c);

		graph.addEdge(a,b);
		graph.addEdge(a,c);


		arFragment = (CustomArFragment) getSupportFragmentManager().findFragmentById(R.id.fragment);
		arFragment.getArSceneView().getScene().addOnUpdateListener(this::onUpdateFrame);

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
						material -> lgsr = ShapeFactory.makeSphere(0.02f, new Vector3(0.0f, 0.0f, 0.0f), material)
				);
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
			if(image.getTrackingState() == TrackingState.TRACKING && image.getTrackingMethod() == AugmentedImage.TrackingMethod.FULL_TRACKING) {
				// checking if correct image was detected
				if(image.getName().equals("ar_pattern")) {
					Log.d("NavigationActivity", "Image 'ar_pattern' was detected.");
				}
			}
		}
	}

	private void showPath(float[] startPos, Node sink) {
		ARGraph temp = new ARGraph(graph);
		Node pos = new Node(startPos, "temp");
		temp.addVertex(pos);

		DefaultWeightedEdge toDel = temp.nearestPointInGraph(startPos).bestEdge;
		Node src = temp.getEdgeSource(toDel), target = temp.getEdgeTarget(toDel);
		temp.addEdge(src, pos);
		temp.addEdge(pos, target);
		temp.removeEdge(toDel);

		List<DefaultWeightedEdge> path = DijkstraShortestPath.findPathBetween(temp, pos, sink);

		createMyBalls(path, temp);
	}

	private void createMyBalls(List<DefaultWeightedEdge> edges, Graph<Node, DefaultWeightedEdge> graph) {
		for(Node node : extractNodes(edges, graph)) {
			this.createBallInReference(node.getPositionF(), pathBalls, lgsr);
		}

		for(DefaultWeightedEdge e : edges) {
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
				this.createBallInReference(pos, pathBalls, bsr);
			}
		}
	}

	private List<Node> extractNodes(List<DefaultWeightedEdge> edges, Graph<Node, DefaultWeightedEdge> graph) {
		List<Node> nodes = new ArrayList<>();

		if(edges.size() > 0) {
			nodes.add(graph.getEdgeSource(edges.get(0)));

			for(DefaultWeightedEdge e : edges) {
				nodes.add(graph.getEdgeTarget(e));
			}
		}

		return nodes;
	}

	private AnchorNode createBallInReference(float[] positionInReference, List<ObjectInReference> myBalls, Renderable renderable) {
		return GraphicsUtility.createBallInReference(positionInReference, myBalls, renderable, arFragment.getArSceneView().getScene(), referenceToWorld);
	}
}