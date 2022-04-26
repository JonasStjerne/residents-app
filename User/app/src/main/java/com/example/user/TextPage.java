package com.example.user;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.TextView;

import io.noties.markwon.Markwon;

public class TextPage extends AppCompatActivity {
    TextView contentContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_text_page);

        contentContainer = (TextView) findViewById(R.id.content);

        final Markwon markwon = Markwon.create(this);

        // set markdown
        markwon.setMarkdown(contentContainer, "# Hello there!");
    }
}