package com.example.user;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Icon;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class DropdownPage extends AppCompatActivity {

    FirebaseFirestore db;
    private ConstraintLayout layout;
    private View lastView = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dropdown_page);

        layout = (ConstraintLayout) findViewById(R.id.dropdownlayout);



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
                        setTitle(document.getString("name"));
                        buildContent(pageId);
                    } else {
                        Log.d("DropdownPage", "No such document");
                    }
                } else {
                    Log.d("DropdownPage", "get failed with ", task.getException());
                }
            }
        });
    }

    private void buildContent(String pageId) {
        db.collection("pages/" + pageId + "/items")
                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        addCard(document.getString("title"), document.getString("content"));
                    }

                } else {
                    Log.w("TAG", "Error getting documents.", task.getException());
                }
            }
        });

    }

    private void addCard(String title, String content) {
        // Title bar with title and image
        ConstraintLayout titleBar = new ConstraintLayout(this);
        titleBar.setId(View.generateViewId());
        titleBar.setBackgroundColor(Color.rgb(0,157,72));
        titleBar.setPadding(5, 5, 5, 5);
        titleBar.setLayoutParams(new ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.MATCH_PARENT, ConstraintLayout.LayoutParams.WRAP_CONTENT));

        TextView titleView = new TextView(this);
        titleView.setText(title);
        titleView.setId(View.generateViewId());
        titleView.setTextSize(18);
        titleView.setTextColor(Color.WHITE);
        titleView.setPadding(5, 5, 0, 5);

        ImageView img = new ImageView(this);
        img.setId(View.generateViewId());
        img.setImageIcon(Icon.createWithResource(this, android.R.drawable.arrow_down_float));
        img.setMinimumWidth(60);
        img.setMinimumHeight(60);

        titleBar.addView(titleView);
        titleBar.addView(img);

        // Constrain title to left and image to right
        ConstraintSet titleBarConstraints = new ConstraintSet();
        titleBarConstraints.clone(titleBar);
        titleBarConstraints.connect(titleView.getId(), ConstraintSet.LEFT, titleBar.getId(), ConstraintSet.LEFT, 20);
        titleBarConstraints.connect(img.getId(), ConstraintSet.RIGHT, titleBar.getId(), ConstraintSet.RIGHT, 20);

        // Center image between top and bottom
        titleBarConstraints.connect(img.getId(), ConstraintSet.TOP, titleBar.getId(), ConstraintSet.TOP);
        titleBarConstraints.connect(img.getId(), ConstraintSet.BOTTOM, titleBar.getId(), ConstraintSet.BOTTOM);
        titleBarConstraints.applyTo(titleBar);

        // Card content with title bar and body text
        ConstraintLayout cardLayout = new ConstraintLayout(this);
        cardLayout.setId(View.generateViewId());
        cardLayout.setPadding(5, 5, 5, 5);
        cardLayout.setLayoutParams(new ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.MATCH_PARENT, ConstraintLayout.LayoutParams.WRAP_CONTENT));

        TextView body = new TextView(this);
        body.setId(View.generateViewId());
        body.setText(content);
        body.setTextSize(16);
        body.setPadding(5, 3, 0, 3);
        body.setTextColor(Color.WHITE);
        body.setVisibility(View.GONE); // start out as hidden

        cardLayout.addView(titleBar);
        cardLayout.addView(body);

        // Constrain title bar to top and body to follow title bar
        ConstraintSet cardConstraints = new ConstraintSet();
        cardConstraints.clone(cardLayout);
        cardConstraints.connect(titleBar.getId(), ConstraintSet.TOP, cardLayout.getId(), ConstraintSet.TOP);
        cardConstraints.connect(body.getId(), ConstraintSet.TOP, titleBar.getId(), ConstraintSet.BOTTOM, 10);
        cardConstraints.applyTo(cardLayout);


        // Finally a Card view with card content (titleBar (title and image) and body)
        CardView card = new CardView(this);
        card.setId(View.generateViewId());
        card.setPadding(15,5,15,5);
        card.setCardBackgroundColor(Color.rgb(0,157,72));
        card.setCardElevation(5);
        card.setContentPadding(5, 5, 5, 5);
        card.setRadius(20);
        card.setLayoutParams(new ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.MATCH_PARENT, ConstraintLayout.LayoutParams.WRAP_CONTENT));
        card.addView(cardLayout);

        // On click listener that will show or hide the card body and change icon
        card.setOnClickListener(v -> {
            boolean visible = body.getVisibility() == View.VISIBLE;
            if (visible) {
                body.setVisibility(View.GONE);
                img.setImageIcon(Icon.createWithResource(this, android.R.drawable.arrow_down_float));
            }
            else {
                body.setVisibility(View.VISIBLE);
                img.setImageIcon(Icon.createWithResource(this, android.R.drawable.arrow_up_float));
            }
        });

        // Add card to outer layout
        layout.addView(card);

        // Constrain card to outer layout and other cards on outer layout
        ConstraintSet constraints = new ConstraintSet();
        constraints.clone(layout);

        // Add margins to cards
        constraints.connect(card.getId(), ConstraintSet.LEFT, layout.getId(), ConstraintSet.LEFT, 20);
        constraints.connect(card.getId(), ConstraintSet.RIGHT, layout.getId(), ConstraintSet.RIGHT, 20);

        if (lastView != null) {
            Log.d("Detail", "From: " + String.valueOf(card.getId()) + " -> " + String.valueOf(lastView.getId()));
            constraints.connect(card.getId(), ConstraintSet.TOP, lastView.getId(), ConstraintSet.BOTTOM, 50);
        } else {
            constraints.connect(card.getId(), ConstraintSet.TOP, layout.getId(), ConstraintSet.TOP, 50);
        }
        lastView = card;

        constraints.applyTo(layout);

    }
}