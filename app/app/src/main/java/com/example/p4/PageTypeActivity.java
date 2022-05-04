package com.example.p4;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.google.firebase.auth.FirebaseAuth;

public class PageTypeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_site_type);
        Log.d("action", "Created");
    }

    public void logout(View v) {
        FirebaseAuth.getInstance().signOut();
        startActivity(new Intent(this, LoginActivity.class));
    }

    public void textPage(View v) {

        Intent intent = new Intent(this, ChoosePageActivity.class);
        intent.putExtra("pageType", "textPage");
        startActivity(intent);
    }

    public void dropdownPage(View v) {
        Intent intent = new Intent(this, ChoosePageActivity.class);
        intent.putExtra("pageType", "dropdown");
        startActivity(intent);
    }
}