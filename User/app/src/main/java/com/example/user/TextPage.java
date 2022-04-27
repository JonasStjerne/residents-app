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

import android.widget.TextView;

import io.noties.markwon.Markwon;


public class TextPage extends AppCompatActivity {
    TextView contentContainer;

    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_text_page);
        contentContainer = (TextView) findViewById(R.id.content);

        final Markwon markwon = Markwon.create(this);

        Intent intent = getIntent();
        String pageId = intent.getStringExtra("PageId");
        Log.d("TextPage" ,"PageId:" + pageId);
        db = FirebaseFirestore.getInstance();
        DocumentReference docRef = db.collection("pages").document(pageId);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Log.d("TextPage", "DocumentSnapshot data: " + document.getData());

                        // set markdown
                        String content = document.getData().get("content").toString();
                        markwon.setMarkdown(contentContainer, content);
                    } else {
                        Log.d("TextPage", "No such document");
                    }
                } else {
                    Log.d("TextPage", "get failed with ", task.getException());
                }
            }
        });





    }
}