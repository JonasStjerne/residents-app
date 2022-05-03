package com.example.p4;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.protobuf.Any;

import java.util.HashMap;
import java.util.Map;

public class DropdownPage extends AppCompatActivity {
    LinearLayout layout;
    LayoutInflater inflater;
    View dropdownElement;
    FirebaseFirestore db;
    String pageId;
    EditText titleEl;
    ProgressBar loadingSpinner;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dropdown_page);

        layout = (LinearLayout) findViewById(R.id.layout);
        titleEl = (EditText) findViewById(R.id.title);
        loadingSpinner = (ProgressBar) findViewById(R.id.loadingSpinner);
        inflater = LayoutInflater.from(this);

        Intent intent = getIntent();
        String str = intent.getStringExtra("selectedPage");

        pageId = null;
        loadDataFromFirebase(str);

    }

    //Load elements from firebase if pageid != "Ny Side"
    private void loadDataFromFirebase(String selectedPage) {
        //Return if new page
        if (selectedPage.equals("Ny Side")) {
            loadingSpinner.setVisibility(View.GONE);
            return;
        }

        //Else load data
        db = FirebaseFirestore.getInstance();
        db.collection("pages").whereEqualTo("name", selectedPage)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                pageId = document.getId().toString();
                                db.collection("pages/" + pageId + "/items")
                                        .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                if (task.isSuccessful()) {
                                                    CreateAndPopulateLayout(document.getData(), task.getResult());
                                                } else {
                                                    Log.w("TAG", "Error getting documents.", task.getException());
                                                }
                                            }
                                        });
                            }
                        } else {
                            Log.w("TAG", "Error getting documents.", task.getException());
                        }
                        loadingSpinner.setVisibility(View.GONE);
                    }
                });
    }

    //Create and populate dropdown(s), header & image with data
    private void CreateAndPopulateLayout(Map<String, Object> data, QuerySnapshot
            dropdownItems) {
        titleEl.setText(data.get("name").toString());
        for (QueryDocumentSnapshot item: dropdownItems)
        {
            addDropdown(item.getId(), item.getData());
        }
    }

    //Save new page to firebase if id == "Ny side" else edit existing record



    //Add new dropdown element. Called at onclick from plus button
    public void addDropdown(){
        dropdownElement = getLayoutInflater().inflate(R.layout.dropdown_element, null, false);
        layout.addView(dropdownElement);
    }

    //Overload method. On call populate dropdown element and append to GUI
    public void addDropdown(String itemId, Map<String, Object> data){
        dropdownElement = getLayoutInflater().inflate(R.layout.dropdown_element, null, false);
        dropdownElement.setTag(itemId);
        EditText titleDropdown = (EditText) dropdownElement.findViewById(R.id.titleDropdown);
        titleDropdown.setText(data.get("title").toString());
        EditText contentDropdown = (EditText) dropdownElement.findViewById(R.id.dropdownContent);
        contentDropdown.setText(data.get("content").toString());
        layout.addView(dropdownElement);
    }

    public void removeDropdown(View v) {

    }
}