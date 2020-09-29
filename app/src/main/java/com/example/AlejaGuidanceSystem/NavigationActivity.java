package com.example.AlejaGuidanceSystem;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageButton;

import com.example.AlejaGuidanceSystem.Utility.GraphicsUtility;
import com.example.AlejaGuidanceSystem.Utility.ObjectInReference;
import com.example.AlejaGuidanceSystem.Utility.PoseAveraginator;
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
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.rendering.Color;
import com.google.ar.sceneform.rendering.MaterialFactory;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.rendering.Renderable;
import com.google.ar.sceneform.rendering.ShapeFactory;

import org.jgrapht.Graph;
import org.jgrapht.GraphPath;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.alg.DijkstraShortestPath;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static java.lang.Thread.*;


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

	private Pose referenceToWorld = Pose.IDENTITY;
	private ArrayList<ObjectInReference> pathBalls = new ArrayList<>();;
	private float[] cameraPosition;

	private ArrayList<ObjectInReference> labels = new ArrayList<>();


	private PoseAveraginator referenceToWorldAveraginator = new PoseAveraginator(200);

	@Override
	@SuppressWarnings({"AndroidApiChecker", "FutureReturnValueIgnored"})
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_navigation);

		// load the selected graph
		graph = (ARGraph) getIntent().getSerializableExtra("Graph");
		if(graph == null) graph = new ARGraph();

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
				//Use Activation of button to toggle between navigation_cancel and search
				if (search_button.isActivated()){
					endNavigation();
				}else {
					//Get allowed Search strings
					ArrayList<String> types = new ArrayList<String>(Arrays.asList(
							graph.vertexSet().stream()
									.filter((node) -> !node.getType().equals(Node.NodeType.WAYPOINT))
									.map((node) -> node.getType().toStringInContext(getApplicationContext()))
									.distinct().toArray(String[]::new)));
					ArrayList<String> labels = new ArrayList<String>(Arrays.asList(
							graph.vertexSet().stream()
									.filter((node) -> !node.getType().equals(Node.NodeType.WAYPOINT))
									.map((node) -> node.getLabel()).toArray(String[]::new)));

					showSearchDialog(types, labels);
				}

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
							Node[] sinks = (Node[]) graph.vertexSet().stream()
									.filter((node) -> node.getType().toStringInContext(getApplicationContext()).equals(target))
									.toArray(Node[]::new);
							showPath(cameraPosition, sinks);
							search_button.setActivated(true);
							searchDialog.dismiss();
						} else if (labels.contains(target)){
							Node[] sinks = (Node[]) graph.vertexSet().stream()
									.filter((node) -> node.getLabel().equals(target)).toArray(Node[]::new);
							showPath(cameraPosition, sinks);
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
		searchDialog.show();
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
				// if(image.getName().equals("dr_christian_rehn")) {
					Log.d("Navigation", "Image 'ar_pattern' was detected.");

					Pose trackableToWorld = image.getCenterPose();
					Pose trackableToReference = Pose.makeTranslation(0, 0.1f, 0);
					referenceToWorld = trackableToWorld.compose(trackableToReference.inverse());
					if(pathBalls != null) {
						Log.d("MyVectorTest", "updateBallPosition " + pathBalls.size());
						for(ObjectInReference obj : pathBalls) {
							obj.recalculatePosition(referenceToWorld);
						}
					}

					for(ObjectInReference obj : labels) {
						obj.recalculatePosition(referenceToWorld);
					}
					search_button.setEnabled(true);
				}
			}
		}

		if (referenceToWorld != null) {
			Pose cameraToWorld = frame.getCamera().getPose();
			Pose cameraToReference = referenceToWorld.inverse().compose(cameraToWorld);
			cameraPosition = cameraToReference.transformPoint(new float[]{0.0f, 0.0f, 0.0f});
		}
	}

	/**
	 * Displays a path shortest path to a destination on the screen
	 * @param startPos current (camera-)position of the user
	 * @param sinks the destination-nodes as a array
	 */
	private void showPath(float[] startPos, Node[] sinks) {
		// creating a copy of the graph to freely add and remove nodes and edges
		ARGraph graphCopy = new ARGraph(graph);
		// adding the current position of the user as a node to the copied graph
		Node user = new Node(startPos, "StartOfUser");
		graphCopy.addVertex(user);

		// removing closest edge to user and adding new edges from its endpoints to user
		DefaultWeightedEdge closestEdge = graphCopy.nearestPointInGraph(startPos).bestEdge;
		Node edgeSource = graphCopy.getEdgeSource(closestEdge), edgeTarget = graphCopy.getEdgeTarget(closestEdge);
		graphCopy.addEdge(user, edgeSource);
		graphCopy.addEdge(user, edgeTarget);
		graphCopy.removeEdge(closestEdge);

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
				}
			}
			// creating a visible path on the screen
			createMyBalls(shortestPath.getEdgeList(), graphCopy);
		}
	}

	/**
	 * Creates a visible Path.
	 * A large green sphere is displayed at the position of every node on the path.
	 * Nodes are connected by blue spheres.
	 * @param edges the path that will be displayed.
	 * @param graph the graph containing the path.
	 */
	private void createMyBalls(List<DefaultWeightedEdge> edges, Graph<Node, DefaultWeightedEdge> graph) {
		// large green balls for every node
		for(Node node : extractNodes(edges, graph)) {
			this.createBallInReference(node.getPositionF(), pathBalls, lgsr);
			Log.d("MyTest", String.format("%s: %.2f %.2f %.2f", node.getId(), node.getX(), node.getY(), node.getZ() ));
			// "nice nice nice"
			//
			//       - Lukas, 2020
		}

		// blue balls on every edge
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

	private AnchorNode createBallInReference(float[] positionInReference, List<ObjectInReference> myBalls, Renderable renderable) {
		return GraphicsUtility.createBallInReference(positionInReference, myBalls, renderable, arFragment.getArSceneView().getScene(), referenceToWorld);
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
}