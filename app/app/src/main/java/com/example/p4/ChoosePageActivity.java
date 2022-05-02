package com.example.p4;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.Spinner;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class ChoosePageActivity extends AppCompatActivity {
    Spinner spinnerPages;
    ProgressBar loadingSpinner;
    ArrayList<String> pagesArr = new ArrayList<>();
    String pagetype;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_page);

        spinnerPages = findViewById(R.id.spinner);

        pagetype = getIntent().getStringExtra("pageType");
        Log.d("pagetype", pagetype);

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        loadingSpinner = (ProgressBar) findViewById(R.id.loadingSpinner);

        pagesArr.add("Ny Side");

        db.collection("pages")
                .whereEqualTo("type", pagetype)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                pagesArr.add(document.getData().get("name").toString());
                            }
                            LoadPagesIntoSpinner(pagesArr);
                        } else {
                            Log.w("TAG", "Error getting documents.", task.getException());
                        }
                    }
                });

    }

    private void LoadPagesIntoSpinner(ArrayList<String> arr) {
        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, arr);
        spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item); // The drop down view
        spinnerPages.setAdapter(spinnerArrayAdapter);

        loadingSpinner.setVisibility(View.GONE);
    }

    public void submitPageSelection(View v) {
        //Send to new page with id of the selected page
        String selectedPage = spinnerPages.getSelectedItem().toString();
        Intent intent;
        if ( pagetype.equals("textPage") ) {
            intent = new Intent(this, PageEditAndCreateActivity.class);
        } else if (pagetype.equals("dropdown")) {
            intent = new Intent(this, DropdownPage.class);
        } else {
            Log.w("ERROR", "Pagetype not found");
            return;
        }
        intent.putExtra("selectedPage", selectedPage);
        startActivity(intent);
    }
}