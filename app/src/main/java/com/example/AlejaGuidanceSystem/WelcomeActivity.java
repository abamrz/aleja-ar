package com.example.AlejaGuidanceSystem;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.AlejaGuidanceSystem.Graph.ARGraphWithGrip;
import com.example.AlejaGuidanceSystem.Utility.Utility;
import com.example.AlejaGuidanceSystem.Graph.ARGraph;
import com.example.AlejaGuidanceSystem.Utility.VectorOperations;
import com.example.Database.DatabaseConnector;
import com.google.ar.core.Pose;

import org.ejml.simple.SimpleMatrix;
import org.ejml.simple.SimpleSVD;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;

public class WelcomeActivity extends Activity {

    private Button make_plan_button;
    private Button use_existing_button;
    private Button exit_button;

    private DatabaseConnector databaseConnector = new DatabaseConnector(this);

    public WelcomeActivity() {
        super();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        Pose rotationPose = Pose.makeRotation(VectorOperations.createQuaternionFromAxisAngle(0.3f, 0.2f, 0.6f, 1.4f));

        float[] matrix = new float[16];
        rotationPose.toMatrix(matrix, 0);

        SimpleMatrix sm = new SimpleMatrix(3, 3);
        sm.set(0, 0, matrix[0]);
        sm.set(1, 0, matrix[1]);
        sm.set(2, 0, matrix[2]);

        sm.set(0, 1, matrix[4]);
        sm.set(1, 1, matrix[5]);
        sm.set(2, 1, matrix[6]);

        sm.set(0, 2, matrix[8]);
        sm.set(1, 2, matrix[9]);
        sm.set(2, 2, matrix[10]);

        Log.d("Test0", sm.mult(sm.transpose()).toString());

        for(int i = 0; i < 9; i++)
            sm.set(i, sm.get(i) * 99.0);

        SimpleSVD<SimpleMatrix> svd = sm.svd();
        Log.d("Test1", svd.getU().toString());
        Log.d("Test2", svd.getW().toString());
        Log.d("Test3", svd.getV().toString());
        SimpleMatrix Ro = svd.getU().mult(svd.getV().transpose());
        SimpleMatrix t = VectorOperations.vec3(0, 0, 0);

        VectorOperations.TransformationResult tr = new VectorOperations.TransformationResult(Ro, t);


        Pose newPose = VectorOperations.poseFromTransformationResult(tr);
        Log.d("Test4", Arrays.toString(rotationPose.getRotationQuaternion()) + " " + Arrays.toString(newPose.getRotationQuaternion()));

        /*ArrayList<SimpleMatrix> a = new ArrayList<SimpleMatrix>();
        a.add(VectorOperations.vec3(1, 0, 0));
        a.add(VectorOperations.vec3(0, 3, 0));
        a.add(VectorOperations.vec3(0, 0, 2));

        ArrayList<SimpleMatrix> b = new ArrayList<SimpleMatrix>();
        b.add(VectorOperations.vec3(1, 0, 0));
        b.add(VectorOperations.vec3(0, 4, 1));
        b.add(VectorOperations.vec3(0, 0, 3));

        VectorOperations.TransformationResult tr = VectorOperations.findGoodTransformation(a, b);
        Pose pose = VectorOperations.poseFromTransformationResult(tr);

        float[] pos1 = pose.transformPoint(new float[] { 0, 0, 3});
        float[] pos2 = VectorOperations.v3matTo3f(tr.applyTo(VectorOperations.vec3(0, 0, 3)));

        Log.d("TransTest", Arrays.toString(pos1) + " " + Arrays.toString(pos2));*/


        make_plan_button = findViewById(R.id.make_plan_button);
        use_existing_button = findViewById(R.id.use_existing_plan_button);
        exit_button = findViewById(R.id.exit_button);

        registerForContextMenu(use_existing_button);
        use_existing_button.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                openContextMenu(view);
            }
        });
        make_plan_button.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                openMakePlanActivity();
            }
        });
        exit_button.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.setHeaderTitle(getResources().getString(R.string.graph_select));

        ArrayList<String> mapNames = new ArrayList<>();
        mapNames.addAll(databaseConnector.getAllGraphs().stream().map(g -> g.getGraph().getName()).collect(Collectors.toList()));

        for (String map : mapNames){
            menu.add(map);
        }
    }

    @SuppressLint("ShowToast")
    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        String graphName = item.getTitle().toString();
        ARGraphWithGrip graph = databaseConnector.getAllGraphs().stream().filter(g -> (g.getGraph().getName().equals(graphName))).findAny().get();
        if(graph != null) {
            openUseExistingPlanActivity(graph);
        } else {
            Toast.makeText(this, "No graph saved! Make a new plan!", Toast.LENGTH_LONG);
        }
        return super.onContextItemSelected(item);
    }

    public void openUseExistingPlanActivity(ARGraphWithGrip graph){
        Intent intent = new Intent(this, NavigationActivity.class);
        intent.putExtra("Graph", graph);
        startActivity(intent);
    }

    public void openMakePlanActivity(){
        Intent intent = new Intent(this, MakePlanActivity.class);
        startActivity(intent);
    }
}