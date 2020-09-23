package com.example.AlejaGuidanceSystem;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

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


        make_plan_button.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                openMakePlanActivity();
            }

        });

        use_existing_button.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                //TODO: choose the right map/graph
                openUseExistingPlanActivity();
            }
        });

        exit_button.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    public void openMakePlanActivity(){
        Intent intent = new Intent(this, MakePlanActivity.class);
        startActivity(intent);
    }

    public void openUseExistingPlanActivity(){
        Intent intent = new Intent(this, NavigationActivity.class);
        startActivity(intent);
    }


}