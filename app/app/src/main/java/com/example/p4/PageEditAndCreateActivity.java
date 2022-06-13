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
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.coordinatorlayout.widget.CoordinatorLayout;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;

import io.noties.markwon.Markwon;
import io.noties.markwon.editor.MarkwonEditor;
import io.noties.markwon.editor.MarkwonEditorTextWatcher;

public class PageEditAndCreateActivity extends AppCompatActivity {
    EditText pageTitleEl;
    ProgressBar loadingSpinner;
    EditText pageContentEl;
    Button submitButton;
    String pageId;
    FirebaseFirestore db;
    TextView deletePageButton;
    ImageView iconImage;
    ImageView pictureImage;

    String iconBase64Img;
    private ActivityResultLauncher<Intent> iconLauncher;

    String pictureBase64Img;
    private ActivityResultLauncher<Intent> pictureLauncher;

    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_page_edit_and_create);
        setTitle("Himmerland");

        pageTitleEl = (EditText) findViewById(R.id.pageTitle);
        pageContentEl = (EditText) findViewById(R.id.pageContent);
        loadingSpinner = (ProgressBar) findViewById(R.id.loadingSpinner);
        submitButton = (Button) findViewById(R.id.submitButton);
        deletePageButton = (TextView) findViewById(R.id.deletePageButton);
        iconImage = (ImageView) findViewById(R.id.iconImage);
        pictureImage = (ImageView) findViewById(R.id.pictureImage);
        pageId = null;

        Intent intent = getIntent();
        String str = intent.getStringExtra("selectedPage");

        iconImage.setOnClickListener(v -> selectIcon());
        pictureImage.setOnClickListener(v -> selectPicture());

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
                                        document.getString("name"),
                                        document.getString("content"),
                                        document.getString("icon"),
                                        document.getString("picture")
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
        // used when icon is selected in photo album
        iconLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == AppCompatActivity.RESULT_OK) {
                        Intent data = result.getData();
                        if (data != null) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                onIconSelected(data);
                            }

                        }
                    }
                }
        );

        // used when picture is selected in photo album
        pictureLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == AppCompatActivity.RESULT_OK) {
                        Intent data = result.getData();
                        if (data != null) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                onPictureSelected(data);
                            }

                        }
                    }
                }
        );

        // obtain Markwon instance
        final Markwon markwon = Markwon.create(this);

        // create editor
        final MarkwonEditor editor = MarkwonEditor.create(markwon);

        // set edit listener
        pageContentEl.addTextChangedListener(MarkwonEditorTextWatcher.withProcess(editor));
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void setPageFields(String name, String content, String icon, String picture) {
        pageTitleEl.setText(name);
        pageContentEl.setText(content);

        if (picture != null) {
            if (picture.length()>0) {
                if (picture.startsWith("data:image/jpeg;base64,")) {
                    picture = picture.substring("data:image/jpeg;base64,".length());
                }

                try {
                    picture = picture.replaceAll("\n", "");
                    byte[] imgData = java.util.Base64.getDecoder().decode(picture);
                    Bitmap image = BitmapFactory.decodeByteArray(imgData, 0, imgData.length);
                    pictureImage.setImageBitmap(image);

                }
                catch (Exception e) {
                    Log.e("Main", "Error decoding image", e);
                }

            }
        }

        if (icon != null) {
            if (icon.length()>0) {
                if (icon.startsWith("data:image/jpeg;base64,")) {
                    icon = icon.substring("data:image/jpeg;base64,".length());
                }

                try {
                    icon = icon.replaceAll("\n", "");
                    byte[] imgData = java.util.Base64.getDecoder().decode(icon);
                    Bitmap image = BitmapFactory.decodeByteArray(imgData, 0, imgData.length);
                    iconImage.setImageBitmap(image);

                }
                catch (Exception e) {
                    Log.e("Main", "Error decoding image", e);
                }

            }
        }

        submitButton.setText("Gem");
    }

    public void handlePageSubmit(View v) {

        Map<String, Object> page = new HashMap<>();
        page.put("name", pageTitleEl.getText().toString());
        page.put("icon", iconBase64Img);
        page.put("picture", pictureBase64Img);
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
                            Toast.makeText(getApplicationContext(), "New page created successfully", Toast.LENGTH_LONG).show();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.w("Firebase", "Error adding document", e);
                            Toast.makeText(getApplicationContext(), "Failed to create page", Toast.LENGTH_LONG).show();
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
                            Toast.makeText(getApplicationContext(), "Page changes saved", Toast.LENGTH_LONG).show();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.w("Firebase", "Error writing document", e);
                            Toast.makeText(getApplicationContext(), "Failed to edit page", Toast.LENGTH_LONG).show();
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
                        Toast.makeText(getApplicationContext(), "Successfully deleted page", Toast.LENGTH_LONG).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w("Firebase", "Error deleting document", e);
                        Toast.makeText(getApplicationContext(), "Failed to delete page", Toast.LENGTH_LONG).show();
                    }
                });
    }


    // Icon
    private void selectIcon() {
        final CharSequence[] options = {"Choose from Gallery", "Cancel"};
        AlertDialog.Builder builder = new AlertDialog.Builder(PageEditAndCreateActivity.this);
        builder.setTitle("Add Photo!");
        builder.setItems(options, (dialog, item) -> {
            if (item == 0) {
                Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                iconLauncher.launch(intent);
            } else {
                dialog.dismiss();
            }
        });
        builder.show();
    }


    //Picture
    private void selectPicture() {
        final CharSequence[] options = {"Choose from Gallery", "Cancel"};
        AlertDialog.Builder builder = new AlertDialog.Builder(PageEditAndCreateActivity.this);
        builder.setTitle("Add Photo!");
        builder.setItems(options, (dialog, item) -> {
            if (item == 0) {
                Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                pictureLauncher.launch(intent);
            } else {
                dialog.dismiss();
            }
        });
        builder.show();
    }


    @RequiresApi(api = Build.VERSION_CODES.Q)
    private void onIconSelected(Intent data) {
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
            iconBase64Img = convertBitMapToString(thumbnail);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    private void onPictureSelected(Intent data) {
        Uri selectedImage = data.getData();
        Log.w("Selected Image: ", selectedImage.toString());

        try {
            Size size = getImageSize(selectedImage);
            Log.w("Original size: ", size.toString());

            size = resizeToMax(size, 400);
            Log.w("Image dimension", size.toString());

            ContentResolver resolver = getApplicationContext().getContentResolver();
            Bitmap thumbnail = resolver.loadThumbnail(selectedImage, size, null);
            pictureImage.setImageBitmap(thumbnail);
            pictureBase64Img = convertBitMapToString(thumbnail);

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