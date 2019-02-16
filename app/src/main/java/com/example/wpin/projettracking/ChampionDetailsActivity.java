package com.example.wpin.projettracking;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;

public class ChampionDetailsActivity extends AppCompatActivity
{

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_champion_details);

        ImageButton ibChampions = findViewById(R.id.ibChampions);
        ibChampions.setImageResource(R.mipmap.ic_navigation_champion_on);

        int champId = getIntent().getIntExtra("champId", 1);
        String champName = getIntent().getStringExtra("champName");

        TextView tvChampName = findViewById(R.id.tvChampName);
        tvChampName.setText("ID : " + champId + " / Nom : " + champName);
    }
}
