package com.example.p4;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class DropdownPage extends AppCompatActivity {
    LinearLayout layout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dropdown_page);

        layout = (LinearLayout) findViewById(R.id.layout);
    }

    //Add new dropdown element. Called at onclick from plus button
    public void addDropdown(View v){
        TextView text = new TextView(DropdownPage.this);
        text.setText("Test");


        layout.addView(text);
    }
}