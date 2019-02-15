package com.example.wpin.projettracking;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageButton;

public class GameHistoryActivity extends AppCompatActivity
{

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_history);

        ImageButton ibHistory = findViewById(R.id.ibHistory);
        ibHistory.setImageResource(R.mipmap.ic_navigation_history_on);
    }
}
