package edu.utep.cs.cs4330.mypricewatcher;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.Serializable;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private Button seeProductsButton;
    private TextView welcomeText;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        seeProductsButton = findViewById(R.id.viewListButton);
        seeProductsButton.setOnClickListener(this::listButtonClicked);
        seeProductsButton.setText(String.format("%s", "View Products"));
        welcomeText = findViewById(R.id.welcomeText);
        welcomeText.setText(String.format("%s", "Welcome to the Price Watcher app"));

    }

    public void listButtonClicked(View view) {
        Intent i = new Intent(this, ProductListActivity.class);
        startActivity(i);
    }

}