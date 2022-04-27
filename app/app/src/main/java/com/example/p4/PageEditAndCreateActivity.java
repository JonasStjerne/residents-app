package com.example.p4;

import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.util.Size;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
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
    ImageView iconImage;

    String base64Img;
    private ActivityResultLauncher<Intent> launcher;

    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_page_edit_and_create);

        pageTitleEl = (EditText) findViewById(R.id.pageTitle);
        pageContentEl = (EditText) findViewById(R.id.pageContent);
        loadingSpinner = (ProgressBar) findViewById(R.id.loadingSpinner);
        submitButton = (Button) findViewById(R.id.submitButton);
        deletePageButton = (TextView) findViewById(R.id.deletePageButton);
        iconImage = (ImageView) findViewById(R.id.iconImage);
        pageId = null;

        Intent intent = getIntent();
        String str = intent.getStringExtra("selectedPage");

        iconImage.setOnClickListener(v -> selectImage());

        db = FirebaseFirestore.getInstance();
        db.collection("pages").whereEqualTo("name", str)
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
        // used when image is selected in photo album
        launcher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == AppCompatActivity.RESULT_OK) {
                        Intent data = result.getData();
                        if (data != null) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                onImageSelected(data);
                            }

                        }
                    }
                }
        );
    }

    private void setPageFields(String name, String content, String icon) {
        pageTitleEl.setText(name);
        pageContentEl.setText(content);
        submitButton.setText("Gem");
    }

    public void handlePageSubmit(View v) {

        Map<String, Object> page = new HashMap<>();
        page.put("name", pageTitleEl.getText().toString());
        page.put("icon", base64Img);
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

    private void selectImage() {
        final CharSequence[] options = {"Choose from Gallery", "Cancel"};
        AlertDialog.Builder builder = new AlertDialog.Builder(PageEditAndCreateActivity.this);
        builder.setTitle("Add Photo!");
        builder.setItems(options, (dialog, item) -> {
            if (item == 0) {
                Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                launcher.launch(intent);
            } else {
                dialog.dismiss();
            }
        });
        builder.show();
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    private void onImageSelected(Intent data) {
        Uri selectedImage = data.getData();
        Log.w("Selected Image: ", selectedImage.toString());

        try {
            Size size = getImageSize(selectedImage);
            Log.w("Original size: ", size.toString());

            size = resizeToMax(size, 400);
            Log.w("Image dimension", size.toString());

            ContentResolver resolver = getApplicationContext().getContentResolver();
            Bitmap thumbnail = resolver.loadThumbnail(selectedImage, size, null);
            iconImage.setImageBitmap(thumbnail);
            base64Img = convertBitMapToString(thumbnail);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String convertBitMapToString(Bitmap img) {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        img.compress(Bitmap.CompressFormat.PNG, 60, os);
        byte[] b = os.toByteArray();
        return Base64.encodeToString(b, Base64.DEFAULT);
    }

    private Size getImageSize(Uri imagePath) throws FileNotFoundException {
        ContentResolver resolver = getApplicationContext().getContentResolver();
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(resolver.openInputStream(imagePath), null, options);
        return new Size(options.outWidth, options.outHeight);
    }

    private Size resizeToMax(Size size, int maxSize) {
        int width = size.getWidth();
        int height = size.getHeight();

        float ratio = (float) width / (float) height;
        if (ratio > 1) {
            width = maxSize;
            height = (int) (width / ratio);
        } else {
            height = maxSize;
            width = (int) (height * ratio);
        }
        return new Size(width, height);
    }
}