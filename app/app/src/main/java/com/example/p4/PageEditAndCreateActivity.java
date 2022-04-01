package com.example.p4;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Map;

public class PageEditAndCreateActivity extends AppCompatActivity {
    EditText pageTitleEl;
    ProgressBar loadingSpinner;
    EditText pageContentEl;
    Button submitButton;
    String pageId;
    FirebaseFirestore db;
    TextView deletePageButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_page_edit_and_create);

        pageTitleEl = (EditText) findViewById(R.id.pageTitle);
        pageContentEl = (EditText) findViewById(R.id.pageContent);
        loadingSpinner = (ProgressBar) findViewById(R.id.loadingSpinner);
        submitButton = (Button) findViewById(R.id.submitButton);
        deletePageButton = (TextView) findViewById(R.id.deletePageButton);
        pageId = null;

        Intent intent = getIntent();
        String str = intent.getStringExtra("selectedPage");

        db = FirebaseFirestore.getInstance();
        db.collection("pages").whereEqualTo("name", str )
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Log.d("TAG", document.getId() + " => " + document.getData());
                                setPageFields(
                                        document.getData().get("name").toString(),
                                        document.getData().get("content").toString(),
                                        document.getData().get("icon").toString()
                                );
                                pageId = document.getId().toString();
                                deletePageButton.setVisibility(View.VISIBLE);
                            }
                        } else {
                            Log.w("TAG", "Error getting documents.", task.getException());
                        }
                        loadingSpinner.setVisibility(View.GONE);
                    }
                });
    }


    private void setPageFields(String name, String content, String icon) {
            pageTitleEl.setText(name);
            pageContentEl.setText(content);
            submitButton.setText("Gem");
    }

    public void handlePageSubmit(View v) {

        Map<String, Object> page = new HashMap<>();
        page.put("name", pageTitleEl.getText().toString());
        page.put("icon", "Not implemented");
        page.put("content", pageContentEl.getText().toString());
        page.put("type", "textPage");

        if (pageId == null) {
            Log.d("Firebase", "Create new page");
            db.collection("pages")
                    .add(page)
                    .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                        @Override
                        public void onSuccess(DocumentReference documentReference) {
                            Log.d("Firebase", "DocumentSnapshot written with ID: " + documentReference.getId());
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.w("Firebase", "Error adding document", e);
                        }
                    });
        } else {
            Log.d("Firebase", "Edit existing page");
            db.collection("pages")
                    .document(pageId)
                    .set(page)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.d("Firebase", "DocumentSnapshot successfully written!");
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.w("Firebase", "Error writing document", e);
                        }
                    });
        }

    }

    public void deletePage(View v) {
        db.collection("pages").document(pageId)
                .delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d("Firebase", "DocumentSnapshot successfully deleted!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w("Firebase", "Error deleting document", e);
                    }
                });
    }
}