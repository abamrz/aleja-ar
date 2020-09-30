package com.example.AlejaGuidanceSystem;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.AlejaGuidanceSystem.Graph.ARGraphWithGrip;
import com.example.AlejaGuidanceSystem.Utility.GraphicsUtility;
import com.example.AlejaGuidanceSystem.Utility.GripVisualisator;
import com.example.AlejaGuidanceSystem.Utility.LabelView;
import com.example.AlejaGuidanceSystem.Utility.ObjectInReference;
import com.example.AlejaGuidanceSystem.Utility.VectorOperations;
import com.example.AlejaGuidanceSystem.Graph.ARGraph;
import com.example.AlejaGuidanceSystem.Graph.Node;
import com.google.ar.core.AugmentedImage;
import com.google.ar.core.Frame;
import com.google.ar.core.Pose;
import com.google.ar.core.Session;
import com.google.ar.core.TrackingState;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.FrameTime;
import com.google.ar.sceneform.math.Quaternion;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.rendering.Color;
import com.google.ar.sceneform.rendering.DpToMetersViewSizer;
import com.google.ar.sceneform.rendering.MaterialFactory;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.rendering.Renderable;
import com.google.ar.sceneform.rendering.ShapeFactory;
import com.google.ar.sceneform.rendering.ViewRenderable;


import org.ejml.simple.SimpleMatrix;
import org.jgrapht.Graph;
import org.jgrapht.GraphPath;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.alg.DijkstraShortestPath;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static java.lang.Thread.*;


public class NavigationActivity extends AppCompatActivity {
	// Buttons
	private ImageButton return_button, search_button;

	// AR
	private CustomArFragment arFragment;
	private ModelRenderable renderable;

	// plan for the area
	private ARGraphWithGrip graphWithGrip;

	// red-, blue- and largeGreen-sphere
	private ModelRenderable rsr;
	private ModelRenderable bsr;
	private ModelRenderable lgsr;

	private Pose graphToWorld = Pose.IDENTITY;
	private ArrayList<ObjectInReference> pathBalls = new ArrayList<>();;
	private float[] cameraPositionInGraph;
	private Node sink = null;

	private ArrayList<ObjectInReference> labels = new ArrayList<>();
	private ArrayList<LabelView> labelViews = new ArrayList<>();

	private HashMap<String, ARGraphWithGrip.WeakGrip> gripMap;

	private GripVisualisator gripVisualisator;

	@Override
	@SuppressWarnings({"AndroidApiChecker", "FutureReturnValueIgnored"})
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_navigation);

		gripVisualisator = new GripVisualisator(this, arFragment.getArSceneView().getScene());

// Example Graph
		/*
		Node a = new Node (1, 0, 0, "a");
		Node b = new Node (0, 0,0, "b");
		a.setType(Node.NodeType.OFFICE);
		b.setType(Node.NodeType.COFFEE);
		a.setDescription("Linux Installation Instructions");
		 */

		// load the selected graph
		graphWithGrip = (ARGraphWithGrip) getIntent().getSerializableExtra("Graph");
		if(graphWithGrip == null) throw new IllegalStateException("No graph was supplied!");

		arFragment = (CustomArFragment) getSupportFragmentManager().findFragmentById(R.id.fragment);
		arFragment.getArSceneView().getScene().addOnUpdateListener(this::onUpdateFrame);

		gripMap = new HashMap<>();

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

		//initialize the buttons
		return_button = (ImageButton) findViewById(R.id.return_button);
		search_button = (ImageButton) findViewById(R.id.search_button);

		search_button.setEnabled(false);

		return_button.setOnClickListener(new View.OnClickListener(){
			@Override
			public void onClick(View view){
				finish();
			}
		});
		search_button.setOnClickListener(new View.OnClickListener(){
			@Override
			public void onClick(View view){
				//showFullGraph();

	    		//	if(false) {

				//Use Activation of button to toggle between navigation_cancel and search
				if (search_button.isActivated()){
					endNavigation();
				}else {
					//Get allowed Search strings
					ArrayList<String> types = new ArrayList<String>(Arrays.asList(
							graphWithGrip.getGraph().vertexSet().stream()
									.filter((node) -> !node.getType().equals(Node.NodeType.WAYPOINT))
									.map((node) -> node.getType().toStringInContext(getApplicationContext()))
									.distinct().toArray(String[]::new)));
					ArrayList<String> labels = new ArrayList<String>(Arrays.asList(
							graphWithGrip.getGraph().vertexSet().stream()
									.filter((node) -> !node.getType().equals(Node.NodeType.WAYPOINT))
									.map((node) -> node.getLabel()).toArray(String[]::new)));

					showSearchDialog(types, labels);
				}
				//	}

//			final TextView input = new TextView(NavigationActivity. this);
//			input.setText("Des is a bayrisches Label!");
//			input.setInputType(InputType.TYPE_CLASS_TEXT);
//			input.setTextColor(android.graphics.Color.WHITE);
//			input.setBackgroundColor(android.graphics.Color.BLACK);
//
//			CompletableFuture<ViewRenderable>
//					future = ViewRenderable
//					.builder()
//					.setView((Context) NavigationActivity.this, input)
//					.build();
//			future.thenAccept(viewRenderable -> {
//
//				viewRenderable.setHorizontalAlignment(ViewRenderable.HorizontalAlignment.CENTER);
//				viewRenderable.setVerticalAlignment(ViewRenderable.VerticalAlignment.CENTER);
//				viewRenderable.setSizer( new DpToMetersViewSizer(550) );
//
//				AnchorNode x = new AnchorNode();
//				x.setRenderable(viewRenderable);
//				arFragment.getArSceneView().getScene().addChild(x);
//
//				float[] quat = VectorOperations.createQuaternionFromAxisAngle(1, 0, 0, -(float)Math.PI / 2.0f);
//				ObjectInReference obj = new ObjectInReference(x, Pose.makeRotation(quat));
//				obj.recalculatePosition(referenceToWorld);
//				labels.add(obj);
			}
		});


	}

	/**
	 * Builds a alert dialog with search functionality, to start navigation
	 * @param types: all possible types of targets to navigate to
	 * @param labels: all possible labels of targets to navigate to
	 */
	private void showSearchDialog(ArrayList<String> types, ArrayList<String> labels){
		ArrayAdapter<String> searchAdapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_dropdown_item_1line);
		searchAdapter.addAll(types);
		searchAdapter.addAll(labels);

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(R.string.search_title);
		AutoCompleteTextView searchView = new AutoCompleteTextView(this);
		searchView.setThreshold(1);
		searchView.setAdapter(searchAdapter);
		builder.setView(searchView);
		builder.setPositiveButton("Go", null);
		builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int i) {
				dialog.cancel();
			}
		});
		AlertDialog searchDialog = builder.create();

		searchDialog.setOnShowListener(new DialogInterface.OnShowListener() {
			@Override
			public void onShow(DialogInterface dialogInterface) {
				Button button = ((AlertDialog) searchDialog).getButton(AlertDialog.BUTTON_POSITIVE);
				button.setOnClickListener(new View.OnClickListener() {

					@Override
					public void onClick(View view) {
						GraphicsUtility.removeMyBalls(arFragment.getArSceneView().getScene(), pathBalls);

						//Start navigation to destination
						String target = searchView.getText().toString();
						if (types.contains(target)) {
							Node[] sinks = (Node[]) graphWithGrip.getGraph().vertexSet().stream()
									.filter((node) -> node.getType().toStringInContext(getApplicationContext()).equals(target))
									.toArray(Node[]::new);
							showPath(cameraPositionInGraph, sinks);
							search_button.setActivated(true);
							searchDialog.dismiss();
						} else if (labels.contains(target)){
							Node[] sinks = (Node[]) graphWithGrip.getGraph().vertexSet().stream()
									.filter((node) -> node.getLabel().equals(target)).toArray(Node[]::new);
							showPath(cameraPositionInGraph, sinks);
							search_button.setActivated(true);
							searchDialog.dismiss();
						} else {
							int current = searchView.getShadowColor();
							searchView.setBackgroundColor(android.graphics.Color.argb(50, 255,0,0));
							new Thread(new Runnable() {
								@Override
								public void run() {
									try {
										sleep(1000);
									} catch (InterruptedException e) {
										e.printStackTrace();
									}
									searchView.setBackgroundColor(current);
								}
							}).start();
						}
					}
				});
			}
		});
		searchView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
			@Override
			public void onFocusChange(View view, boolean b) {
				if (b){
					searchView.showDropDown();
				}
			}
		});
		searchDialog.show();
	}

	/**
	 * listener-method, called every time the camera frame is updated
	 */
	private void onUpdateFrame(FrameTime frameTime) {
		// finding the current fragment of the scene
		Session session = arFragment.getArSceneView().getSession();
		Frame frame = arFragment.getArSceneView().getArFrame();
		// all the items ARCore has tracked
		Collection<AugmentedImage> images = frame.getUpdatedTrackables(AugmentedImage.class);

		for(AugmentedImage image : images) {
			if(image.getTrackingState() == TrackingState.TRACKING && image.getTrackingMethod() == AugmentedImage.TrackingMethod.FULL_TRACKING) {
				ARGraphWithGrip.StrongGrip grip  = new ARGraphWithGrip.StrongGrip(
						image.getName(),
						image.getCenterPose().getTranslation(),
						image.getCenterPose().getRotationQuaternion()
				);
				gripMap.put(image.getName(), grip);
				this.gripVisualisator.updateGrip(image.getName(), image.getCenterPose());
				this.updateGraphToWorldByGrip();
			}
		}

		String x = "";
		for(ARGraphWithGrip.WeakGrip grip : gripMap.values()) {
			x += Arrays.toString(grip.gripPosition) + ", ";
		}

		TextView myAwesomeTextView = (TextView) findViewById(R.id.textView2);
		myAwesomeTextView.setText(String.format("NumGrips: %d %s", gripMap.size(), x));

		if (graphToWorld != null) {
			Pose cameraToWorld = frame.getCamera().getPose();
			Pose cameraToReference = graphToWorld.inverse().compose(cameraToWorld);
			cameraPositionInGraph = cameraToReference.transformPoint(new float[]{0.0f, 0.0f, 0.0f});
		}

		//TODO: Wo soll diese Methode hin? :)
		showLabels(graphWithGrip.getGraph());

		//during navigation
		if (search_button.isActivated()) {
			Log.d("Size", Integer.toString(pathBalls.size()));
			float[] closestBallPos = pathBalls.get(0).getPoseInReference().transformPoint(new float[]{0.0f, 0.0f, 0.0f});

			//collect balls
			if (pathBalls.size() > 1 &&
					VectorOperations.v3dist(cameraPositionInGraph, closestBallPos) >
					VectorOperations.v3dist(cameraPositionInGraph, pathBalls.get(1).getPoseInReference().transformPoint(new float[]{0.0f, 0.0f, 0.0f}))) {
				GraphicsUtility.removeMyBalls(arFragment.getArSceneView().getScene(), new ArrayList<ObjectInReference>(){{add(pathBalls.get(0));}});
				pathBalls.remove(0);

				Log.d("Collect", Integer.toString(pathBalls.size()));
			}

			//re-calc route
			if (VectorOperations.v3dist(cameraPositionInGraph, closestBallPos) > 2f){
				GraphicsUtility.removeMyBalls(arFragment.getArSceneView().getScene(), pathBalls);
				showPath(cameraPositionInGraph, new Node[] {this.sink});

				Log.d("recalc", Integer.toString(pathBalls.size()));
			}

			//end navigation when goal is reached
			if (VectorOperations.v3dist(cameraPositionInGraph, this.sink.getPositionF()) < 0.2f) {
				AlertDialog goalReachedDialog = new AlertDialog.Builder(this).create();
				goalReachedDialog.setMessage(getString(R.string.reached_goal));
				goalReachedDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int which) {
								dialog.dismiss();
							}
						});
				goalReachedDialog.show();
				endNavigation();
			}
		}
	}

	private void updateGraphToWorldByGrip() {

		// checking if correct image was detected
        ArrayList<ARGraphWithGrip.WeakGrip> foundGrips = new ArrayList<>();
		ArrayList<ARGraphWithGrip.WeakGrip> graphGrips = new ArrayList<>();

		for(ARGraphWithGrip.WeakGrip foundGrip : this.gripMap.values()) {
			for(ARGraphWithGrip.WeakGrip gripInGraph : graphWithGrip.getGrips()) {
			    if(foundGrip.gripId.equals(gripInGraph.gripId)) {
			    	foundGrips.add(foundGrip);
			    	graphGrips.add(gripInGraph);
			    	break;
				}
			}
		}

		ArrayList<SimpleMatrix> referencePoints = new ArrayList<>();
		ArrayList<SimpleMatrix> pointsToTransform = new ArrayList<>();

		SimpleMatrix numericalStabilityMatrix = new SimpleMatrix(3, 3);

		for(int i = 0; i < foundGrips.size(); i++) {
			ARGraphWithGrip.WeakGrip foundGrip = foundGrips.get(i);
			ARGraphWithGrip.WeakGrip graphGrip = graphGrips.get(i);

			if(foundGrip instanceof ARGraphWithGrip.StrongGrip && graphGrip instanceof ARGraphWithGrip.StrongGrip) {
				float[] foundQuat = ((ARGraphWithGrip.StrongGrip) foundGrip).rotationQuaternion;
				float[] graphQuat = ((ARGraphWithGrip.StrongGrip) graphGrip).rotationQuaternion;

				Pose foundPose = Pose.makeRotation(foundQuat);
				Pose graphPose = Pose.makeRotation(graphQuat);

				Pose transformation = foundPose.compose(graphPose.inverse());

				SimpleMatrix transSM = VectorOperations.simpleMatrixFromRotationPose(transformation);
				numericalStabilityMatrix = numericalStabilityMatrix.plus(transSM);
			}

			referencePoints.add(VectorOperations.vec3(foundGrip.gripPosition));
			pointsToTransform.add(VectorOperations.vec3(graphGrip.gripPosition));

		//TODO: Wo sollen diese Methoden hin?
			updateLabelOrientation();

			updateLabelVisibility();
		}

		VectorOperations.scaleMatrix(numericalStabilityMatrix, 0.2);

		VectorOperations.TransformationResult tr = VectorOperations.findGoodTransformation(referencePoints, pointsToTransform, numericalStabilityMatrix);
		graphToWorld = VectorOperations.poseFromTransformationResult(tr);

		if(pathBalls != null) {
			Log.d("MyVectorTest", "updateBallPosition " + pathBalls.size());
			for(ObjectInReference obj : pathBalls) {
				obj.recalculatePosition(graphToWorld);
			}
		}

		for(ObjectInReference obj : labels) {
			obj.recalculatePosition(graphToWorld);
		}
		search_button.setEnabled(true);
	}

	/**
	 * Displays a path shortest path to a destination on the screen
	 * @param startPos current (camera-)position of the user
	 * @param sinks the destination-nodes as a array
	 */
	private void showPath(float[] startPos, Node[] sinks) {
		// creating a copy of the graph to freely add and remove nodes and edges
		ARGraph graphCopy = new ARGraph(graphWithGrip.getGraph());
		// adding the current position of the user as a node to the copied graph
		Node user = new Node(startPos, "StartOfUser");
		graphCopy.addVertex(user);

		// removing closest edge to user and adding new edges from its endpoints to user
		DefaultWeightedEdge closestEdge = graphCopy.nearestPointInGraph(startPos).bestEdge;
		Node edgeSource = graphCopy.getEdgeSource(closestEdge), edgeTarget = graphCopy.getEdgeTarget(closestEdge);
		graphCopy.addEdge(user, edgeSource);
		graphCopy.addEdge(user, edgeTarget);
		graphCopy.removeEdge(closestEdge);

		// setting edge weights
		for(DefaultWeightedEdge e : graphCopy.edgeSet()) {
			float[] sourcePos = graphCopy.getEdgeSource(e).getPositionF();
			float[] targetPos = graphCopy.getEdgeTarget(e).getPositionF();
			graphCopy.setEdgeWeight(e, VectorOperations.v3dist(sourcePos, targetPos));

			Log.d("MyTestE", graphCopy.getEdgeSource(e).getId() + " to " + graphCopy.getEdgeTarget(e).getId());
		}

		// List of edges on the shortest path from user to destination
		if (sinks != null && sinks.length > 0){
			DijkstraShortestPath dijkstraShortestPath;
			GraphPath shortestPath = null;
			double smallestWeight = Double.POSITIVE_INFINITY;

			for (Node sink : sinks){
				dijkstraShortestPath = new DijkstraShortestPath(graphCopy, user, sink);
				if (dijkstraShortestPath.getPathLength() < smallestWeight) {
					smallestWeight = dijkstraShortestPath.getPathLength();
					shortestPath = dijkstraShortestPath.getPath();
					this.sink = sink;
				}
			}
			// creating a visible path on the screen
			createMyBalls(shortestPath.getVertexList(), graphCopy);
		}
	}


// creates LabelView objects and adds as anchors to scene
	private void showLabels(ARGraph graph){
		for (Node node: graph.vertexSet()){

			LabelView labelView = new LabelView(this, node);

			CompletableFuture<ViewRenderable>
					future = ViewRenderable
					.builder()
					.setView((Context) NavigationActivity.this, labelView.getLayoutView())
					.build();
			future.thenAccept(viewRenderable -> {

				viewRenderable.setHorizontalAlignment(ViewRenderable.HorizontalAlignment.CENTER);
				viewRenderable.setVerticalAlignment(ViewRenderable.VerticalAlignment.CENTER);
				viewRenderable.setSizer( new DpToMetersViewSizer(550) );

				AnchorNode anchorNode = new AnchorNode();
				anchorNode.setRenderable(viewRenderable);
				arFragment.getArSceneView().getScene().addChild(anchorNode);


				float[] quat = VectorOperations.createQuaternionFromAxisAngle(1, 0, 0, -(float)Math.PI / 2.0f);
				Pose pose = Pose.makeTranslation(node.getPositionF()).compose(Pose.makeRotation(quat));
				ObjectInReference objectInReference = new ObjectInReference(anchorNode, pose);

				//TODO: ist graphTOWorld gleich referenceToWOrld??
				objectInReference.recalculatePosition(graphToWorld);
				labels.add(objectInReference);

				labelView.setObjectInReference(objectInReference);
				labelViews.add(labelView);
			});
		}
	}

	private void updateLabelOrientation(){
		//update anchorNodes! :) mit folgendem Link
		//https://creativetech.blog/home/ui-elements-for-arcore-renderable


		// calculate current rotation for labels
		for (ObjectInReference label : labels) {
			float[] labelPosition = label.getPoseInReference().getTranslation();

			Pose translation = label.getPoseInReference().extractTranslation();

			Vector3 labelPosition3 = new Vector3(labelPosition[0], labelPosition[1], labelPosition[2]);
			Vector3 cameraPosition3 = new Vector3(cameraPositionInGraph[0], cameraPositionInGraph[1], cameraPositionInGraph[2]);

			Vector3 direction3 = labelPosition3.subtract(cameraPosition3, labelPosition3);
			//direction3.z=0.0f;

			Quaternion lookRotation = Quaternion.lookRotation(direction3, new Vector3(0, 0, -1));
			Pose rotation = Pose.makeRotation(lookRotation.x, lookRotation.y, lookRotation.z, lookRotation.w);

			label.setPoseInReference(translation.compose(rotation));
			label.recalculatePosition(graphToWorld);
		}

	}

	private void updateLabelVisibility(){
		for (LabelView label: labelViews){
			if (inRadius(label, 1.5)){
				label.setVisible(true);
			}
			else label.setVisible(false);
		}
	}

	// check if label in Radius
	private boolean inRadius(LabelView labelView, double radius){
		float[] position = labelView.getObjectInReference().getPoseInReference().getTranslation();
		double distanceFromCamera = VectorOperations.v3dist(position, cameraPositionInGraph);

		return distanceFromCamera<=radius;
	}

	/**
	 * Creates a visible Path.
	 * A large green sphere is displayed at the position of every node on the path.
	 * Nodes are connected by blue spheres.
	 * @param edges the path that will be displayed.
	 * @param graph the graph containing the path.
	 */
	private void createMyBalls(List<Node> nodes, Graph<Node, DefaultWeightedEdge> graph) {

		Node source = nodes.get(0);
		for (int index = 1; index < nodes.size(); index++){
			Node target = nodes.get(index);
			Log.d("createPath",  String.format("%s to %s", source.getId(), target.getId()));
			// large green balls for every node
			this.createBallInReference(source.getPositionF(), pathBalls, lgsr);

			float dist = VectorOperations.v3dist(source.getPositionF(), target.getPositionF());
			float sepDist = 0.025f;
			int numSep = (int)Math.ceil(dist / sepDist);

			float stepDist = dist / (numSep);
			float[] sourcePos = source.getPositionF();
			float[] targetPos = target.getPositionF();
			float[] dir = VectorOperations.v3normalize(VectorOperations.v3diff(targetPos, sourcePos));
			for(int i = 1; i < numSep; i++) {
				// blue balls on every edge
				float[] pos = VectorOperations.v3add(sourcePos, VectorOperations.v3mulf(dir, stepDist * i));
				this.createBallInReference(pos, pathBalls, bsr);
			}

			source = target;
		}
		this.createBallInReference(source.getPositionF(), pathBalls, lgsr);
	}

	private AnchorNode createBallInReference(float[] positionInReference, List<ObjectInReference> myBalls, Renderable renderable) {
		return GraphicsUtility.createBallInReference(positionInReference, myBalls, renderable, arFragment.getArSceneView().getScene(), graphToWorld);
	}


	/**
	 * extracts all nodes that appear in a list of edges
	 * @param edges the path containing the nodes
	 * @param graph the graph containing the path
	 * @return list of all nodes
	 */
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

	/**
	 * Used to end a navigation and delete all shown paths
	 */
	private void endNavigation(){
		GraphicsUtility.removeMyBalls(arFragment.getArSceneView().getScene(), pathBalls);
		search_button.setActivated(false);
	}

	private void showFullGraph() {
		GraphicsUtility.removeMyBalls(arFragment.getArSceneView().getScene(), pathBalls);

		ARGraph graph = graphWithGrip.getGraph();

		for (Node node : graph.vertexSet()) {
			GraphicsUtility.createBallInReference(node.getPositionF(), pathBalls, lgsr, arFragment.getArSceneView().getScene(), graphToWorld);
		}


		for (DefaultWeightedEdge e : graph.edgeSet()) {
			Node source = graph.getEdgeSource(e);
			Node target = graph.getEdgeTarget(e);

			float dist = VectorOperations.v3dist(source.getPositionF(), target.getPositionF());
			float sepDist = 0.025f;
			int numSep = (int) Math.ceil(dist / sepDist);

			float stepDist = dist / (numSep);
			float[] sourcePos = source.getPositionF();
			float[] targetPos = target.getPositionF();
			float[] dir = VectorOperations.v3normalize(VectorOperations.v3diff(targetPos, sourcePos));
			for (int i = 1; i < numSep; i++) {
				float[] pos = VectorOperations.v3add(sourcePos, VectorOperations.v3mulf(dir, stepDist * i));
				GraphicsUtility.createBallInReference(pos, pathBalls, bsr, arFragment.getArSceneView().getScene(), graphToWorld);
			}
		}

	}
}