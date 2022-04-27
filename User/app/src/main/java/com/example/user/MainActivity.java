package com.example.user;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Base64;

public class MainActivity extends AppCompatActivity {

    private ImageView icons[] = new ImageView[4];
    private TextView texts[] = new TextView[4];

    private int pageNumber = 0;

    FirebaseFirestore db;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        icons[0] = findViewById(R.id.imageView1);
        icons[1] = findViewById(R.id.imageView2);
        icons[2] = findViewById(R.id.imageView3);
        icons[3] = findViewById(R.id.imageView4);

        texts[0] = findViewById(R.id.textView1);
        texts[1] = findViewById(R.id.textView2);
        texts[2] = findViewById(R.id.textView3);
        texts[3] = findViewById(R.id.textView4);

        Log.d("Main", "Retriving documents from firebase");

        db = FirebaseFirestore.getInstance();
        CollectionReference pagesCollection = db.collection("pages");
        pagesCollection.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    int i = 0;
                    for (QueryDocumentSnapshot document: task.getResult()) {
                        if (i < pageNumber * 4) {
                            continue;
                        }

                        String pageId = document.getId().toString();
                        //Log.d("TAG", pageId + " => " + document.getData());
                        Log.d("Main", "Got document: " + document.getData().get("name").toString());

                        if (document.getData().get("name") != null) {
                            texts[i].setText(document.getData().get("name").toString());
                            texts[i].setVisibility(View.VISIBLE);
                        }

                        if (document.getData().get("icon") != null) {
                            String base64Img = document.getData().get("icon").toString();
                            Log.d("Main", "Decode icon: " + (base64Img.length() < 60 ? base64Img : base64Img.substring(0, 59)));
                            if (base64Img.length()>0 && ! "Not implemented".equalsIgnoreCase(base64Img)) {
                                if (base64Img.startsWith("data:image/jpeg;base64,")) {
                                    base64Img = base64Img.substring("data:image/jpeg;base64,".length());
                                }

                                try {
                                    base64Img = base64Img.replaceAll("\n", "");
                                    byte[] imgData = Base64.getDecoder().decode(base64Img);
                                    Bitmap image = BitmapFactory.decodeByteArray(imgData, 0, imgData.length);
                                    icons[i].setImageBitmap(image);
                                    icons[i].setVisibility(View.VISIBLE);

                                }
                                catch (Exception e) {
                                    Log.e("Main", "Error decoding image", e);
                                }

                                icons[i].setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        // Goto page ...
                                        if (document.getData().get("type") != null) {
                                            String pageType = document.getData().get("type").toString();
                                            Log.d("Main", "Navigate to page with type " + pageType + " and id " + pageId);
                                            if ("textPage".equalsIgnoreCase(pageType)) {
                                                Intent intent = new Intent(MainActivity.this, TextPage.class);
                                                intent.putExtra("PageId", pageId);
                                                startActivity(intent);

                                            }
                                            else if ("dropdown".equalsIgnoreCase(pageType)) {
                                                Intent intent = new Intent(MainActivity.this, DropdownPage.class);
                                                intent.putExtra("PageId", pageId);
                                                startActivity(intent);
                                            }
                                        }
                                    }
                                });
                            }
                        }

                        i++;
                        if (i == 4) {
                            // more than more page
                            break;
                        }

                    }
                }
                else {
                    Log.w("Main", "Error getting firebase documents", task.getException());
                }
        }});
    }
}
