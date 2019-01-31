package com.example.wpin.projettracking;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;

public class PlayerGlobalStatsActivity extends AppCompatActivity
{
    final static String API_KEY = "RGAPI-1a661ebc-0da3-4d90-9068-6253b9f6d022"; // à remplacer si deprecated

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player_global_stats);

        // Récupération du username et de la région recherchés par l'utilisateur.
        String strSearchedUsername = getIntent().getStringExtra("searchedUsername");
        String strSearchedRegion = getIntent().getStringExtra("searchedRegion");

        String strRegion = getRegionName(strSearchedRegion);    // Récupération du nom de la région à mettre dans l'URL de requête

        TextView tvPlayerRegion = findViewById(R.id.tvPlayerRegion);
        tvPlayerRegion.setText(strSearchedRegion);  // On remplace "Région" par la région du joueur

        // Appel à l'API de LoL pour récupérer les informations du joueur. (Niveau pour l'instant) / Si ça fonctionne pas -> changer l'api key dans l'url.
        Ion.with(this)
                .load("https://" + strRegion + ".api.riotgames.com/lol/summoner/v4/summoners/by-name/" + strSearchedUsername + "?api_key=" + API_KEY)
                .asJsonObject()
                .setCallback(new FutureCallback<JsonObject>() {
                    @Override
                    public void onCompleted(Exception e, JsonObject result) {
                        TextView tvPlayerUsername = findViewById(R.id.tvPlayerUsername);
                        String strPlayerUsername = result.get("name").getAsString();        // Récupération du pseudo exact (respect de la casse)
                        tvPlayerUsername.setText(strPlayerUsername);

                        ImageView ivPlayerIcon = findViewById(R.id.ivPlayerIcon);
                        String strPlayerIcon = result.get("profileIconId").getAsString();
                        // TODO : Changer l'icône (voir fin du README pour DATA DRAGON)

                        String strPlayerLevel = result.get("summonerLevel").getAsString();
                        TextView tvPlayerLevel = findViewById(R.id.tvPlayerLevel);
                        tvPlayerLevel.setText("Niveau " + strPlayerLevel);
                    }
                });

        Button btnUpdatePlayerProfile = findViewById(R.id.btnUpdatePlayerProfile);
        btnUpdatePlayerProfile.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {   // Quand on clique sur Mettre à jour le profil
                updatePlayerProfile();  // TODO : On appelle la méthode updatePlayerProfile()
            }
        });
    }

    // Fonction permettant de récupérer le nom exact de la région à mettre dans l'URL
    public String getRegionName(String strSearchedRegion){
        String strRegion;
        switch(strSearchedRegion){
            case "EUW":
                strRegion = "euw1";
                break;
            case "BR":
                strRegion = "br1";
                break;
            case "EUNE":
                strRegion = "eun1";
                break;
            case "JP":
                strRegion = "jp1";
                break;
            case "KR":
                strRegion = "kr";
                break;
            case "LAN":
                strRegion = "la1";
                break;
            case "LAS":
                strRegion = "la2";
                break;
            case "NA":
                strRegion = "na1";
                break;
            case "OCE":
                strRegion = "oc1";
                break;
            case "TR":
                strRegion = "tr1";
                break;
            case "RU":
                strRegion = "ru";
                break;
            default:
                strRegion = "euw1";
                break;
        }
        return strRegion;
    }

    public Void updatePlayerProfile(){
        // TODO : Implémenter la fonction
        return null;
    }
}
