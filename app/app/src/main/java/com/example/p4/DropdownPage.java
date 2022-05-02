package com.example.p4;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class DropdownPage extends AppCompatActivity {
    LinearLayout layout;
    LayoutInflater inflater;
    View dropdownElement;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dropdown_page);

        layout = (LinearLayout) findViewById(R.id.layout);

        inflater = LayoutInflater.from(this);



    }

    //Add new dropdown element. Called at onclick from plus button
    public void addDropdown(View v){
        TextView text = new TextView(DropdownPage.this);
        text.setText("Test");
        dropdownElement = getLayoutInflater().inflate(R.layout.dropdown_element, null, false);
        layout.addView(dropdownElement);
    }

    public void removeDropdown(View v) {

    }
}