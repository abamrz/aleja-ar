package com.example.AlejaGuidanceSystem;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.TypedArrayUtils;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.AlejaGuidanceSystem.Utility.GraphicsUtility;
import com.example.AlejaGuidanceSystem.Utility.GripVisualisator;
import com.example.AlejaGuidanceSystem.Utility.Utility;
import com.example.AlejaGuidanceSystem.Utility.VectorOperations;
import com.example.AlejaGuidanceSystem.Graph.ARGraph;
import com.example.AlejaGuidanceSystem.Graph.ARGraphWithGrip;
import com.example.AlejaGuidanceSystem.Graph.Node;
import com.example.Database.DatabaseConnector;
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

import java.lang.reflect.Array;

import java.util.Arrays;
import java.util.Collection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;


public class MakePlanActivity extends AppCompatActivity implements Scene.OnUpdateListener {

	// AR-Fragment is responsible for hosting the scene.
	private CustomArFragment arFragment;
	// red- and blue-sphere-renderable.
	private ModelRenderable rsr;
	private ModelRenderable bsr;
	private ModelRenderable lbsr;

	// list of spheres representing camera positions.
	private ArrayList<AnchorNode> pathBalls;

	private ARGraph graph;
	private HashMap<String, ARGraphWithGrip.WeakGrip> gripMap;
	private boolean regenerateScene = false;

	private int nodeIdCounter = 0;

	// private final String GRAPHNAME = "schlabber";

	private float[] cameraPosition = null;
	private Node lastFocusedNode = null;

	DatabaseConnector databaseConnector;

	private GripVisualisator gripVisualisator;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_make_plan);

		databaseConnector = DatabaseConnector.getInstance(this);

		//initialization of return button
		ImageButton return_button = (ImageButton) findViewById(R.id.return_button_make_plan);
		return_button.setOnClickListener(new View.OnClickListener(){
			@Override
			public void onClick(View view){
				finish();
			}
		});

		arFragment = (CustomArFragment) getSupportFragmentManager().findFragmentById(R.id.fragment);
		// adding a listener so that this activity can react to updates in the fragment
		arFragment.getArSceneView().getScene().addOnUpdateListener(this);

		gripVisualisator = new GripVisualisator(this, arFragment.getArSceneView().getScene());


		findViewById(R.id.addToBranchButton).setOnClickListener(v -> {
			if (cameraPosition == null) return;

			Node node = new Node(cameraPosition, "node" + nodeIdCounter);

			graph.addVertex(node);

			if (lastFocusedNode != null) {
				graph.addEdge(lastFocusedNode, node);
			}
			lastFocusedNode = node;

			nodeIdCounter++;

			regenerateScene = true;
		});


		findViewById(R.id.newBranchButton).setOnClickListener(v -> {
			if (cameraPosition == null) return;

			NearestPointInfo npi = nearestPointInGraph(graph, cameraPosition);
			if (npi == null) return;

			Node source = graph.getEdgeSource(npi.bestEdge);
			Node target = graph.getEdgeTarget(npi.bestEdge);

			Node chosenNode = null;
			if (npi.interpolatingFactor < 0.0001) {
				chosenNode = source;
			}
			if (npi.interpolatingFactor > 0.9999) {
				chosenNode = target;
			}

			if (chosenNode == null) {
				graph.removeEdge(source, target);

				chosenNode = new Node(npi.nearestPosition, "node" + nodeIdCounter);
				nodeIdCounter++;

				graph.addVertex(chosenNode);

				graph.addEdge(source, chosenNode);
				graph.addEdge(chosenNode, target);
			}

			Node node = new Node(cameraPosition, "node" + nodeIdCounter);
			graph.addVertex(node);
			graph.addEdge(chosenNode, node);
			lastFocusedNode = node;

			nodeIdCounter++;

			regenerateScene = true;
		});

		findViewById(R.id.closeCircleButton).setOnClickListener(v -> {
			if (cameraPosition == null) return;
			if (lastFocusedNode == null) return;

			NearestPointInfo npi = nearestPointInGraph(graph, cameraPosition);
			if (npi == null) return;

			Node source = graph.getEdgeSource(npi.bestEdge);
			Node target = graph.getEdgeTarget(npi.bestEdge);

			Node chosenNode = null;
			if (npi.interpolatingFactor < 0.0001) {
				chosenNode = graph.getEdgeSource(npi.bestEdge);
			}
			if (npi.interpolatingFactor > 0.9999) {
				chosenNode = graph.getEdgeTarget(npi.bestEdge);
			}

			if (chosenNode == null) {
				graph.removeEdge(source, target);

				chosenNode = new Node(npi.nearestPosition, "node" + nodeIdCounter);
				nodeIdCounter++;

				graph.addVertex(chosenNode);

				graph.addEdge(source, chosenNode);
				graph.addEdge(chosenNode, target);
			}

			if (chosenNode == lastFocusedNode) return;

			graph.addEdge(chosenNode, lastFocusedNode);
			lastFocusedNode = null;

			nodeIdCounter++;

			regenerateScene = true;
		});

		findViewById(R.id.setAttributeButton).setOnClickListener(v -> {
			if (cameraPosition == null) return;

			Optional<Node> closestOpt = graph.vertexSet().stream().min((n1, n2) -> {
				return VectorOperations.v3dist(n1.getPositionF(), cameraPosition) < VectorOperations.v3dist(n2.getPositionF(), cameraPosition) ? -1 : 1;
			});
			if (!closestOpt.isPresent()) {
				return;
			}
			final Node closest = closestOpt.get();


			Context context = this;
			LinearLayout layout = new LinearLayout(context);
			layout.setOrientation(LinearLayout.VERTICAL);

			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle("Node Attributes");
			// Set up the label input
			final EditText input = new EditText(context);
			input.setHint("Label");
			input.setInputType(InputType.TYPE_CLASS_TEXT);
			if (!closest.getLabel().trim().isEmpty()) input.setText(closest.getLabel());
			layout.addView(input);

			// Set up office description
			final EditText officeDescription = new EditText(context);
			officeDescription.setHint("Description");
			officeDescription.setInputType(InputType.TYPE_CLASS_TEXT);
			if (!closest.getDescription().trim().isEmpty()) officeDescription.setText(closest.getDescription());
			officeDescription.setVisibility(View.GONE);
			layout.addView(officeDescription);

			//Decription wird in label gespeichert, wie wenn das letzte was man eingegeben hat gespeichert wird; und es wird dauern (02) hinzugefügt.

			/*String[] typeStrings = {"Waypoint", "Kitchen", "Exit", "Coffee", "Office", "Elevator", "Toilette", "Fire Extinguisher"};
			Node.NodeType[] types = {Node.NodeType.WAYPOINT, Node.NodeType.KITCHEN, Node.NodeType.EXIT, Node.NodeType.COFFEE,
					Node.NodeType.OFFICE, Node.NodeType.ELEVATOR, Node.NodeType.TOILETTE, Node.NodeType.FIRE_EXTINGUISHER};*/
			Node.NodeType[] types = Node.getTypeStrings().keySet().toArray(new Node.NodeType[0]);
			String[] typeStrings = Node.getTypeStrings().values().toArray(new String[0]);

			final ArrayAdapter<String> adp = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, typeStrings);

			final Spinner derSpinner = new Spinner(context);
			derSpinner.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
			derSpinner.setAdapter(adp);
			int typeIndex = Utility.indexOf(types, closest.getType());
			if(typeIndex != -1)
				derSpinner.setSelection(typeIndex);
			layout.addView(derSpinner);

			derSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
				@Override
				public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
					if (types[derSpinner.getSelectedItemPosition()] == Node.NodeType.OFFICE){
						officeDescription.setVisibility(View.VISIBLE);
					}
					else officeDescription.setVisibility(View.GONE);
				}

				@Override
				public void onNothingSelected(AdapterView<?> parent) {

				}
			});


			builder.setView(layout);

			// Set up the buttons
			builder.setPositiveButton("OK", (dialog, which) -> {
				closest.setLabel(input.getText().toString());
				closest.setType(types[derSpinner.getSelectedItemPosition()]);
				closest.setDescription(officeDescription.getText().toString());

				Log.d("AttributesTest", closest.getId() + ": label: " + closest.getLabel() + ", type: " + closest.getType().toString());
			});
			builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

			builder.show();
		});


		findViewById(R.id.saveButton).setOnClickListener(v -> {
			AlertDialog.Builder builder = new AlertDialog.Builder(this); //AlertDialog.THEME_DEVICE_DEFAULT_DARK
			builder.setTitle("Name your Plan");

			final EditText input = new EditText(this);
			input.setInputType(InputType.TYPE_CLASS_TEXT);
			builder.setView(input);

			builder.setPositiveButton("OK", null);
			builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

			// giving positive Button an onClickListener, to stop dialog from automatically disappearing
			MakePlanActivity context = this;
			AlertDialog nameDialog = builder.create();
			nameDialog.setOnShowListener(new DialogInterface.OnShowListener() {
				@Override
				public void onShow(DialogInterface dialogInterface) {
					Button button = nameDialog.getButton(AlertDialog.BUTTON_POSITIVE);
					button.setOnClickListener(new View.OnClickListener() {
						@Override
						public void onClick(View view) {
							String graphName = input.getText().toString();
							if(graphName != null && !graphName.equals("")) {
								graph.setName(graphName);
								long connector = databaseConnector.addGraph(graphName, new ARGraphWithGrip(graph, new ArrayList<>(gripMap.values())));
								//Utility.saveObject(getApplicationContext(), graphName, new ARGraphWithGrip(graph, new ArrayList<>(gripMap.values())));
								nameDialog.dismiss();
							}
							else {
								Toast.makeText(context, "Invalid Graph name!", Toast.LENGTH_LONG).show();
							}
						}
					});
				}
			});
			nameDialog.show();
			//builder.show();
		});

		findViewById(R.id.deleteButton).setOnClickListener(v -> {
			graph = new ARGraph();
			regenerateScene = true;
			lastFocusedNode =null;

			if(nearestPosNode != null)
				arFragment.getArSceneView().getScene().removeChild(nearestPosNode);
				nearestPosNode = null;
		});

		pathBalls = new ArrayList<>();

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

		/*graph =  (ARGraph) Utility.loadObject(this, "schlabber");
		if(graph == null) {
			graph = new ARGraph();
		}
		regenerateScene = true;*/

		graph = new ARGraph();

		regenerateScene = true;
		gripMap = new HashMap<>();
	}

	/**
	 * setting up the Augmented Images Database by adding images that should be detected.
	 */
	public void setupDatabase(Config config, Session session) {
		// test image
		Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ar_pattern1);

		AugmentedImageDatabase aid = new AugmentedImageDatabase(session);
		// adding Augmented Images to Database
		aid.addImage("ar_pattern1", bitmap, 0.2f);

		config.setAugmentedImageDatabase(aid);
	}



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

		// checking all detected images for one of the reference pictures
		for (AugmentedImage image : images) {
			if (image.getTrackingState() == TrackingState.TRACKING && image.getTrackingMethod() == AugmentedImage.TrackingMethod.FULL_TRACKING) {
				ARGraphWithGrip.StrongGrip grip  = new ARGraphWithGrip.StrongGrip(
						image.getName(),
						image.getCenterPose().getTranslation(),
						image.getCenterPose().getRotationQuaternion()
				);
				gripMap.put(image.getName(), grip);

				this.gripVisualisator.updateGrip(image.getName(), image.getCenterPose());
			}
		}

		if(frame.getCamera().getTrackingState() == TrackingState.TRACKING) {
			Pose cameraToWorld = frame.getCamera().getPose();
			cameraPosition = cameraToWorld.transformPoint(new float[] { 0, 0, 0 });

			if (regenerateScene) {
				regeneratePathBalls();
				regenerateScene = false;
			}

			int numGrips = gripMap.size();

			String x = "";
			for(ARGraphWithGrip.WeakGrip grip : gripMap.values()) {
				x += Arrays.toString(grip.gripPosition) + ", ";
			}

			String logString = String.format(Locale.GERMAN, "Camera position %.3f %.3f %.3f\nNum Grips: %d %s", cameraPosition[0], cameraPosition[1], cameraPosition[2], numGrips, x);

			Log.d("MyApp2", logString);

			TextView myAwesomeTextView = (TextView) findViewById(R.id.textView);
			myAwesomeTextView.setText(logString);


			// update nearest graph point to camera
			NearestPointInfo nearestPointInfo = nearestPointInGraph(graph, cameraPosition);

			if (nearestPointInfo != null && frame.getCamera().getTrackingState() == TrackingState.TRACKING) {
				Pose nodePose = Pose.makeTranslation(nearestPointInfo.nearestPosition);

				if (nearestPosNode == null) {
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

		for (DefaultWeightedEdge e : graph.edgeSet()) {
			Node source = graph.getEdgeSource(e);
			Node target = graph.getEdgeTarget(e);

			float[] sourcePos = source.getPositionF();
			float[] targetPos = target.getPositionF();

			float f = VectorOperations.nearestPointToLine(sourcePos, targetPos, probePosition);
			if (f < 0) f = 0;
			if (f > 1) f = 1;

			float[] pos = VectorOperations.v3interpolate(sourcePos, targetPos, f);

			float dist = VectorOperations.v3dist(probePosition, pos);
			if (bestEdge == null || dist < shortestDist) {
				bestEdge = e;
				shortestDist = dist;
				nearestPosition = pos;
				bestInterpolatingFactor = f;
			}
		}

		if (nearestPosition == null) return null;

		NearestPointInfo nearestPointInfo = new NearestPointInfo();
		nearestPointInfo.bestEdge = bestEdge;
		nearestPointInfo.distance = shortestDist;
		nearestPointInfo.interpolatingFactor = bestInterpolatingFactor;
		nearestPointInfo.nearestPosition = nearestPosition;
		return nearestPointInfo;
	}

	private AnchorNode nearestPosNode = null;


	private void regeneratePathBalls() {
		GraphicsUtility.removeMyBallsInWorld(arFragment.getArSceneView().getScene(), pathBalls);

		for (Node node : graph.vertexSet()) {
			GraphicsUtility.createBallInWorld(node.getPositionF(), pathBalls, lbsr, arFragment.getArSceneView().getScene());
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
				GraphicsUtility.createBallInWorld(pos, pathBalls, bsr, arFragment.getArSceneView().getScene());
			}
		}

	}
}
