package com.example.AlejaGuidanceSystem;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class WelcomeActivity extends AppCompatActivity {

    private Button make_plan_button;
    private Button use_existing_button;

    public WelcomeActivity() {
        super();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        make_plan_button = findViewById(R.id.make_plan_button);
        use_existing_button = findViewById(R.id.use_existing_plan_button);


        make_plan_button.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                openMakePlanActivity();
            }

        });

        use_existing_button.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                openUseExistingPlanActivity();
            }
        });
    }

    public void openMakePlanActivity(){
        Intent intent = new Intent(this, MakePlanActivity.class);
        startActivity(intent);
    }

    public void openUseExistingPlanActivity(){
        Intent intent = new Intent(this, UseExistingPlanActivity.class);
        startActivity(intent);
    }


}