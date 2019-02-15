package com.example.wpin.projettracking;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageButton;

public class ChampionsActivity extends AppCompatActivity
{

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_champions);

        ImageButton ibChampions = findViewById(R.id.ibChampions);
        ibChampions.setImageResource(R.mipmap.ic_navigation_champion_on);
    }
}
