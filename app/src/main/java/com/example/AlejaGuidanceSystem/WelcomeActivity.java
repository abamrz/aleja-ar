package com.example.AlejaGuidanceSystem;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import androidx.annotation.NonNull;
import com.example.AlejaGuidanceSystem.graph.Node;
import java.util.ArrayList;

public class WelcomeActivity extends Activity {

    private Button make_plan_button;
    private Button use_existing_button;
    private Button exit_button;

    public WelcomeActivity() {
        super();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

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

        //TODO: load graph names from database
        ArrayList<String> mapNames = new ArrayList<>();
        mapNames.add("Dummy1");
        mapNames.add("Dummy2");

        for (String map : mapNames){
            menu.add(map);
        }
    }

    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        String graphName = item.getTitle().toString();

        //TODO: load graph by name from database
        ARGraph graph = null;
        //DUmmy Graph
        {
            graph = new ARGraph();
            Node a = new Node(0, 0, 0, "a");
            Node b = new Node(0, 10, 0, "b");
            Node c = new Node(5, 5, 0, "c");
            graph.addVertex(a);
            graph.addVertex(b);
            graph.addVertex(c);
            graph.addEdge(a, b);
            graph.addEdge(a, c);
        }
        openUseExistingPlanActivity(graph);

        return super.onContextItemSelected(item);
    }

    public void openUseExistingPlanActivity(ARGraph graph){
        Intent intent = new Intent(this, NavigationActivity.class);
        intent.putExtra("Graph", graph);
        startActivity(intent);
    }

    public void openMakePlanActivity(){
        Intent intent = new Intent(this, MakePlanActivity.class);
        startActivity(intent);
    }
}