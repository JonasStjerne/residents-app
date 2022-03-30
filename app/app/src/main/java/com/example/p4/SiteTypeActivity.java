package com.example.p4;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SiteTypeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d("action", "Created");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sitetype);
    }

    public void logout(View v) {
        v.setEnabled(false);
        Log.d("action", "Log Out");
    }
}