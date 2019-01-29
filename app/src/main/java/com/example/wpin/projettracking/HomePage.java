package com.example.wpin.projettracking;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Spinner;
import android.widget.TextView;

public class HomePage extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);
    }

    public void onSearch(View v) {
        TextView textPseudo = findViewById(R.id.tvPseudo);
        Spinner spinRegion = findViewById(R.id.spRegion);
        Intent i = new Intent(this, PlayerGlobalStatsActivity.class);
        i.putExtra("Pseudo", textPseudo.getText().toString());
        i.putExtra("Region", spinRegion.getSelectedItem().toString());
        startActivityForResult(i,100);
    }
}
