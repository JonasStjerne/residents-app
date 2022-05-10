package com.example.user;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;

import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Base64;

import io.noties.markwon.Markwon;
import io.noties.markwon.html.HtmlPlugin;


public class TextPage extends AppCompatActivity {
    TextView contentContainer;
    ImageView pictureImage;

    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_text_page);
        contentContainer = (TextView) findViewById(R.id.content);
        pictureImage = (ImageView) findViewById(R.id.pictureImage);

        final Markwon markwon = Markwon.builder(this).usePlugin(HtmlPlugin.create()).build();
        //final Markwon markwon = Markwon.create(this);

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
                        setTitle(document.getString("name"));

                        if (document.getData().get("picture") != null) {
                            String base64Img = document.getData().get("picture").toString();
                            Log.d("Main", "Decode picture: " + (base64Img.length() < 60 ? base64Img : base64Img.substring(0, 59)));
                            if (base64Img.length()>0) {
                                if (base64Img.startsWith("data:image/jpeg;base64,")) {
                                    base64Img = base64Img.substring("data:image/jpeg;base64,".length());
                                }

                                try {
                                    base64Img = base64Img.replaceAll("\n", "");
                                    byte[] imgData = Base64.getDecoder().decode(base64Img);
                                    Bitmap image = BitmapFactory.decodeByteArray(imgData, 0, imgData.length);
                                    pictureImage.setImageBitmap(image);

                                }
                                catch (Exception e) {
                                    Log.e("Main", "Error decoding image", e);
                                }

                            }
                        }



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