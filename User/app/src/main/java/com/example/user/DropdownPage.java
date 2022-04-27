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

public class DropdownPage extends AppCompatActivity {

    FirebaseFirestore db;
    private ConstraintLayout layout;

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
                            buildContent(document);
                    } else {
                        Log.d("DropdownPage", "No such document");
                    }
                } else {
                    Log.d("DropdownPage", "get failed with ", task.getException());
                }
            }
        });
    }

    private void buildContent(DocumentSnapshot document) {

        View lastView = null;
        int cardsNo = 8;

        for (int i = 1; i <= cardsNo; i++) {

            CardView card = new CardView(this);
            card.setId(i + 10);
            card.setPadding(15,5,15,5);
            card.setCardBackgroundColor(Color.BLUE);
            card.setCardElevation(2);
            card.setContentPadding(5, 5, 5, 5);
            card.setRadius(20);

            card.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

            TextView tv = new TextView(this);
            tv.setText("Hello world " + (String.valueOf(i)));
            tv.setId(100 + i);
            tv.setTextSize(18);
            tv.setPadding(5, 5, 0, 5);
            tv.setBackgroundResource(R.color.purple_500);

            TextView body = new TextView(this);
            body.setId(200 + i);
            body.setText("Lorem ipsum dolor sit amet, consectetur adipiscing elit. Etiam nec risus ultrices, pharetra eros pulvinar, auctor metus. Suspendisse porta interdum velit nec volutpat");
            body.setTextSize(12);
            body.setPadding(5, 3, 0, 3);
            body.setBackgroundResource(R.color.purple_200);

            ImageView img = new ImageView(this);
            img.setId(300 + i);
            img.setImageIcon(Icon.createWithResource(this, android.R.drawable.arrow_down_float));
            img.setMinimumWidth(30);
            img.setMinimumHeight(30);

            // Create laout for card and add content
            ConstraintLayout cardLayout = new ConstraintLayout(this);
            cardLayout.addView(tv);
            cardLayout.addView(body);
            cardLayout.addView(img);

            card.addView(cardLayout);

            // Constraints for content in new card (title, body, image)

            ConstraintSet cardConstraints = new ConstraintSet();
            cardConstraints.clone(cardLayout);

            cardConstraints.connect(tv.getId(), ConstraintSet.TOP, cardLayout.getId(), ConstraintSet.TOP, 5);
            cardConstraints.connect(tv.getId(), ConstraintSet.LEFT, cardLayout.getId(), ConstraintSet.LEFT, 5);
            cardConstraints.connect(tv.getId(), ConstraintSet.RIGHT, cardLayout.getId(), ConstraintSet.RIGHT, 5);

            cardConstraints.connect(img.getId(), ConstraintSet.TOP, cardLayout.getId(), ConstraintSet.TOP, 5);
            cardConstraints.connect(img.getId(), ConstraintSet.RIGHT, cardLayout.getId(), ConstraintSet.RIGHT, 5);

            cardConstraints.connect(body.getId(), ConstraintSet.LEFT, cardLayout.getId(), ConstraintSet.LEFT, 5);
            cardConstraints.connect(body.getId(), ConstraintSet.RIGHT, cardLayout.getId(), ConstraintSet.RIGHT, 5);
            cardConstraints.connect(body.getId(), ConstraintSet.TOP, tv.getId(), ConstraintSet.BOTTOM, 20);

            cardConstraints.applyTo(cardLayout);

            // Add new card to outer layout
            layout.addView(card);

            // Constraints for new card
            ConstraintSet constraints = new ConstraintSet();
            constraints.clone(layout);

            constraints.connect(card.getId(), ConstraintSet.LEFT, layout.getId(), ConstraintSet.LEFT, 10);
            constraints.connect(card.getId(), ConstraintSet.RIGHT, layout.getId(), ConstraintSet.RIGHT, 10);

            if (lastView != null) {
                Log.d("Detail", "From: " + String.valueOf(card.getId()) + " -> " + String.valueOf(lastView.getId()));
                constraints.connect(card.getId(), ConstraintSet.TOP, lastView.getId(), ConstraintSet.BOTTOM, 20);
            } else {
                constraints.connect(card.getId(), ConstraintSet.TOP, layout.getId(), ConstraintSet.TOP, 5);
            }
            lastView = card;

            constraints.applyTo(layout);

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

        }

        // Start all card with hidden body
        // must be done after layout above
        for (int i = 1; i <= cardsNo; i++) {
            View v = findViewById(200 + i);
            if (v != null) {
                v.setVisibility(View.GONE);
            }
            else {
                Log.d("Detail", "Unable to find view with id " + String.valueOf(200 + i));
            }
        }
    }
}