package com.example.p4;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

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
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.protobuf.Any;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class DropdownPage extends AppCompatActivity {
    LinearLayout layout;
    LayoutInflater inflater;
    View dropdownElement;
    FirebaseFirestore db;
    String pageId;
    EditText titleEl;
    ProgressBar loadingSpinner;
    ImageView iconImage;
    int dropDownAddedId;

    String iconBase64Img;
    private ActivityResultLauncher<Intent> iconLauncher;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dropdown_page);
        setTitle("Himmerland");

        layout = (LinearLayout) findViewById(R.id.layout);
        titleEl = (EditText) findViewById(R.id.title);
        loadingSpinner = (ProgressBar) findViewById(R.id.loadingSpinner);
        inflater = LayoutInflater.from(this);
        iconImage = (ImageView) findViewById(R.id.iconImage2);

        Intent intent = getIntent();
        String selectedPage = intent.getStringExtra("selectedPage");

        //start values
        dropDownAddedId = 0;

        //Get firebase instance & load data
        db = FirebaseFirestore.getInstance();
        loadDataFromFirebase(selectedPage);

        iconImage.setOnClickListener(v -> selectIcon());

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
    }

    //Load elements from firebase if pageid != "Ny Side"
    private void loadDataFromFirebase(String pageName) {
        //If new page add empty dropdown and return
        if (pageName.equals("Ny Side")) {
            addDropdown();
            loadingSpinner.setVisibility(View.GONE);
            return;
        }
        //Else load data
        db.collection("pages").whereEqualTo("name", pageName)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                pageId = document.getId().toString();
                                titleEl.setText(document.get("name").toString());
                                db.collection("pages/" + pageId + "/items")
                                        .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                if (task.isSuccessful()) {
                                                    for (QueryDocumentSnapshot document : task.getResult()) {
                                                        addDropdown(document.getId(), document.getData());
                                                    }
                                                    //CreateAndPopulateLayout(document.getData(), task.getResult());
                                                } else {
                                                    Log.w("TAG", "Error getting documents.", task.getException());
                                                }
                                            }
                                        });
                            }
                        } else {
                            Log.w("TAG", "Error getting documents.", task.getException());
                        }
                        loadingSpinner.setVisibility(View.GONE);
                    }
                });
        findViewById(R.id.deletePageButton).setVisibility(View.VISIBLE);
    }

    //Create and populate dropdown(s), header & image with data
    private void CreateAndPopulateLayout(Map<String, Object> data, QuerySnapshot
            dropdownItems) {
        titleEl.setText(data.get("name").toString());
        for (QueryDocumentSnapshot item: dropdownItems)
        {
            addDropdown(item.getId(), item.getData());
        }
    }

    //Save new page to firebase if id == "Ny side" else edit existing record
    public void createOrSavePage(View view) {
        //Save Title and pagetype in page object
        Map<String, Object> page = new HashMap<>();
        page.put("name", titleEl.getText().toString());
        //page.put("icon", base64Img);
        page.put("type", "dropdown");
        page.put("icon", iconBase64Img);

        //List to save dropdown data
        ArrayList<HashMap<String, Object>> items = new ArrayList<HashMap<String, Object>>();

        //For every dropdown add it to the list as a Hashmap
        for (int i = 0; i < layout.getChildCount(); i++) {
            View v = layout.getChildAt(i);
            HashMap<String, Object> item = new HashMap<>();
            EditText dropDownContent = (EditText) v.findViewById(R.id.dropdownContent);
            EditText dropDownTitle = (EditText) v.findViewById(R.id.titleDropdown);
            item.put("content", dropDownContent.getText().toString());
            item.put("title", dropDownTitle.getText().toString());
            items.add(item);
            Log.d("Dropdown content: ", items.get(i).get("content").toString());
        }

        //If the page exist delete it and then recreate
        if (pageId != null) {
            db.collection("pages").document(pageId)
                    .delete()
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.d("Sucess", "DocumentSnapshot successfully deleted!");
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.w("Error", "Error deleting document", e);
                        }
                    });
        }
        db.collection("pages")
                .add(page)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        String newPageId = documentReference.getId();
                        for (HashMap<String, Object> item: items) {
                            db.collection("pages")
                            .document(newPageId)
                            .collection("items").add(item)
                                    .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                        @Override
                                        public void onSuccess(DocumentReference documentReference) {
                                                Log.d("Added", "dropdown item");
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Log.w("Firebase", "Error adding dropdown item", e);
                                        }
                                    });
                        }
                        pageId = newPageId;
                        Toast.makeText(getApplicationContext(), "Successfully saved", Toast.LENGTH_LONG).show();
                        Intent intent = new Intent(getApplicationContext(), ChoosePageActivity.class);
                        intent.putExtra("pageType", "dropdown");
                        startActivity(intent);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w("Firebase", "Error adding document", e);
                        Toast.makeText(getApplicationContext(), "Failed to create page", Toast.LENGTH_LONG).show();
                    }
                });

    }


    //Add new dropdown element. Called at onclick from plus button
    public void addDropdown(View v){
        dropdownElement = getLayoutInflater().inflate(R.layout.dropdown_element, null, false);
        dropdownElement.setTag(Integer.toString(dropDownAddedId));
        dropdownElement.findViewById(R.id.deleteBtn).setTag(Integer.toString(dropDownAddedId));
        dropDownAddedId++;
        layout.addView(dropdownElement);
    }

    public void addDropdown(){
        dropdownElement = getLayoutInflater().inflate(R.layout.dropdown_element, null, false);
        dropdownElement.setTag(Integer.toString(dropDownAddedId));
        dropdownElement.findViewById(R.id.deleteBtn).setTag(Integer.toString(dropDownAddedId));
        dropDownAddedId++;
        layout.addView(dropdownElement);
    }

    //Overload method. On call populate dropdown element and append to GUI
    public void addDropdown(String itemId, Map<String, Object> data){
        //Import element
        dropdownElement = getLayoutInflater().inflate(R.layout.dropdown_element, null, false);

        //Set tag from parameter
        dropdownElement.setTag(itemId);
        //set tag for button from parameter
        dropdownElement.findViewById(R.id.deleteBtn).setTag(itemId);

        //Get title element
        EditText titleDropdown = (EditText) dropdownElement.findViewById(R.id.titleDropdown);

        //Populate data into element
        titleDropdown.setText(data.get("title").toString());
        // -||-
        EditText contentDropdown = (EditText) dropdownElement.findViewById(R.id.dropdownContent);
        contentDropdown.setText(data.get("content").toString());

        //Add element to view
        layout.addView(dropdownElement);
    }

    public void removeDropdown(View v) {
        Log.d("Removing with id", v.getTag().toString());
        layout.removeView(layout.findViewWithTag(v.getTag().toString()));
    }

    public void deletePage(View v) {
        db.collection("pages").document(pageId)
                .delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d("Sucess", "DocumentSnapshot successfully deleted!");
                        Toast.makeText(getApplicationContext(), "Page deleted", Toast.LENGTH_LONG).show();
                        Intent intent = new Intent(getApplicationContext(), ChoosePageActivity.class);
                        intent.putExtra("pageType", "dropdown");
                        startActivity(intent);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w("Error", "Error deleting document", e);
                        Toast.makeText(getApplicationContext(), "Failed to delete", Toast.LENGTH_LONG).show();
                    }
                });
    }



    // Icon
    private void selectIcon() {
        final CharSequence[] options = {"Choose from Gallery", "Cancel"};
        AlertDialog.Builder builder = new AlertDialog.Builder(DropdownPage.this);
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