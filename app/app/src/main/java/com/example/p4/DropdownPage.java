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
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.protobuf.Any;

import java.lang.reflect.Array;
import java.util.ArrayList;
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
    int dropDownAddedId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dropdown_page);

        layout = (LinearLayout) findViewById(R.id.layout);
        titleEl = (EditText) findViewById(R.id.title);
        loadingSpinner = (ProgressBar) findViewById(R.id.loadingSpinner);
        inflater = LayoutInflater.from(this);

        Intent intent = getIntent();
        String selectedPage = intent.getStringExtra("selectedPage");

        //start values
        dropDownAddedId = 0;

        //Get firebase instance & load data
        db = FirebaseFirestore.getInstance();
        loadDataFromFirebase(selectedPage);

    }

    //Load elements from firebase if pageid != "Ny Side"
    private void loadDataFromFirebase(String pageName) {
        //If new page add empty dropdown and return
        if (pageName.equals("Ny Side")) {
            addDropdown();
            loadingSpinner.setVisibility(View.GONE);
            return;
        }
        //Else load data
        db.collection("pages").whereEqualTo("name", pageName)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                pageId = document.getId().toString();
                                titleEl.setText(document.get("name").toString());
                                db.collection("pages/" + pageId + "/items")
                                        .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                if (task.isSuccessful()) {
                                                    for (QueryDocumentSnapshot document : task.getResult()) {
                                                        addDropdown(document.getId(), document.getData());
                                                    }
                                                    //CreateAndPopulateLayout(document.getData(), task.getResult());
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
    public void createOrSavePage(View view) {
        Map<String, Object> page = new HashMap<>();
        page.put("name", titleEl.getText().toString());
        //page.put("icon", base64Img);
        page.put("type", "dropdown");

        ArrayList<Object> items = new ArrayList<Object>();

        for (int i = 0; i < layout.getChildCount(); i++) {
            View v = layout.getChildAt(i);
            Map<String, Object> item = new HashMap<>();
            item.put("content", v.findViewById(R.id.dropdownContent));
            item.put("title", v.findViewById(R.id.titleDropdown));
            items.add(item);
        }
        if (pageId == null) {
            db.collection("pages")
                    .add(page)
                    .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                        @Override
                        public void onSuccess(DocumentReference documentReference) {
                            String newPageId = documentReference.getId();

                            for (Object item: items) {
                                db.collection("pages/" + newPageId + "/items").add(item)
                                        .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                            @Override
                                            public void onSuccess(DocumentReference documentReference) {
                                                    Log.d("Added", "dropdown item");
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Log.w("Firebase", "Error adding dropdown item", e);
                                            }
                                        });
                            }
                            pageId = newPageId;
                            Toast.makeText(getApplicationContext(), "New page created successfully", Toast.LENGTH_LONG).show();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.w("Firebase", "Error adding document", e);
                            Toast.makeText(getApplicationContext(), "Failed to create page", Toast.LENGTH_LONG).show();
                        }
                    });
        } else {

        }
    }


    //Add new dropdown element. Called at onclick from plus button
    public void addDropdown(View v){
        dropdownElement = getLayoutInflater().inflate(R.layout.dropdown_element, null, false);
        dropdownElement.setTag(dropDownAddedId);
        dropDownAddedId++;
        layout.addView(dropdownElement);
    }

    public void addDropdown(){
        dropdownElement = getLayoutInflater().inflate(R.layout.dropdown_element, null, false);
        dropdownElement.setTag(dropDownAddedId);
        dropDownAddedId++;
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