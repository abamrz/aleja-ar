package com.example.AlejaGuidanceSystem;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class UseExistingPlanActivity extends AppCompatActivity {
    private Button closeButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_use_existing_plan);

        closeButton = (Button) findViewById(R.id.close_button);

        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openWelcomeActivity();
            }
        });
    }

    public void openWelcomeActivity(){
        Intent intent = new Intent(this, WelcomeActivity.class);
        startActivity(intent);
    }


}