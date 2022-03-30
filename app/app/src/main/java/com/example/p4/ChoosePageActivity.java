package com.example.p4;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_page);

        spinnerPages = findViewById(R.id.spinner);

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        loadingSpinner = (ProgressBar) findViewById(R.id.loadingSpinner);

        db.collection("pages")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Log.d("TAG", document.getId() + " => " + document.getData());
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

    private void submitPageSelection(View v) {
        //Send to new page with id of the selected page
        Log.d("Submit", "Button pressed");
    }
}