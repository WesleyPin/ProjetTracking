package com.example.wpin.projettracking;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;

public class PlayerGlobalStatsActivity extends AppCompatActivity
{

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player_global_stats);

        // Récupération du pseudo et de la région choisit par le joueur/utilisateur.
        String pseudo = getIntent().getStringExtra("Pseudo");
        String region = getIntent().getStringExtra("Region");
        TextView titrePseudoRegion = findViewById(R.id.tvPlayerName);
        titrePseudoRegion.setText(pseudo + " - " + region);

        // Appel à l'API de LoL pour récupérer les informations du joueur. (Niveau pour l'instant)
        Ion.with(this)
                .load("https://" + region + "1.api.riotgames.com/lol/summoner/v4/summoners/by-name/" + pseudo + "?api_key=RGAPI-b763a53e-0613-44da-abc1-37aa4acd3dd0")
                .asJsonObject()
                .setCallback(new FutureCallback<JsonObject>() {
                    @Override
                    public void onCompleted(Exception e, JsonObject result) {
                        TextView niveau = findViewById(R.id.tvPlayerLevel);
                        niveau.setText("Niveau d'invocateur : " + result.get("summonerLevel").getAsString());
                    }
                });
    }
}
