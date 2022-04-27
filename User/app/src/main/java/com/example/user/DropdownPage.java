package com.example.user;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class DropdownPage extends AppCompatActivity {

    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dropdown_page);

        Intent intent = getIntent();
        String pageId = intent.getStringExtra("PageId");
        Log.d("DropdownPage" ,"PageId:" + pageId);

        db = FirebaseFirestore.getInstance();
        DocumentReference docRef = db.collection("pages").document(pageId);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Log.d("DropdownPage", "DocumentSnapshot data: " + document.getData());

                    } else {
                        Log.d("DropdownPage", "No such document");
                    }
                } else {
                    Log.d("DropdownPage", "get failed with ", task.getException());
                }
            }
        });
    }
}