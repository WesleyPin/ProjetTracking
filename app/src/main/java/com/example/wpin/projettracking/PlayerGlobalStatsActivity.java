package com.example.wpin.projettracking;

import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;

public class PlayerGlobalStatsActivity extends AppCompatActivity
{
    final static String API_KEY = "RGAPI-883a1df5-1e6d-4d94-9947-d5131168fce4"; // à remplacer si expirée
    String strProfileIconUrl;
    private SharedPreferences mPreferences;
    private String sharedPrefFile = "com.example.wpin.projettracking";

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player_global_stats);

        mPreferences = getSharedPreferences(sharedPrefFile, MODE_PRIVATE);

        // Récupération du username et de la région recherchés par l'utilisateur.
        String strSearchedUsername = getIntent().getStringExtra("searchedUsername");
        String strSearchedRegion = getIntent().getStringExtra("searchedRegion");

        // Récupération de la dernière version "profileicon"
        Ion.with(this)
                .load("https://ddragon.leagueoflegends.com/realms/" + strSearchedRegion.toLowerCase() + ".json")
                .asJsonObject()
                .setCallback(new FutureCallback<JsonObject>() {
                    @Override
                    public void onCompleted(Exception e, JsonObject result) {
                        SharedPreferences.Editor preferencesEditor = mPreferences.edit();
                        JsonObject jObj;
                        jObj = result.get("n").getAsJsonObject();

                        String strProfileIconVersion = jObj.get("profileicon").getAsString();
                        preferencesEditor.putString("profileIconVersion", strProfileIconVersion);

                        String strChampionVersion = jObj.get("champion").getAsString();
                        preferencesEditor.putString("championVersion", strChampionVersion);

                        preferencesEditor.apply();
                    }
                });

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
                        SharedPreferences.Editor preferencesEditor = mPreferences.edit();

                        TextView tvPlayerUsername = findViewById(R.id.tvPlayerUsername);
                        String strPlayerUsername = result.get("name").getAsString();        // Récupération du pseudo exact (respect de la casse)
                        tvPlayerUsername.setText(strPlayerUsername);
                        preferencesEditor.putString("playerUsername", strPlayerUsername);

                        ImageView ivPlayerIcon = findViewById(R.id.ivPlayerIcon);
                        String strPlayerIconId = result.get("profileIconId").getAsString();

                        String strProfileIconVersion = mPreferences.getString("profileIconVersion", "null");

                        strProfileIconUrl = "http://ddragon.leagueoflegends.com/cdn/" + strProfileIconVersion + "/img/profileicon/" + strPlayerIconId + ".png";
                        Ion.with(ivPlayerIcon).load(strProfileIconUrl);
                        preferencesEditor.putString("playerIconId", strPlayerIconId);

                        String strEncryptedSummonerId = result.get("id").getAsString();
                        preferencesEditor.putString("encryptedSummonerId", strEncryptedSummonerId); // Sauvegarde du encrypted summoner id (on en a besoin pour autre requête)

                        String strPlayerLevel = result.get("summonerLevel").getAsString();
                        TextView tvPlayerLevel = findViewById(R.id.tvPlayerLevel);
                        tvPlayerLevel.setText("Niveau " + strPlayerLevel);
                        preferencesEditor.putString("playerLevel", strPlayerLevel);

                        preferencesEditor.apply();
                    }
                });

        String strEncryptedSummonerId = mPreferences.getString("encryptedSummonerId", "null");

        // Requête et traitement pour savoir quel champ du joueur a le plus d'expérience
        Ion.with(this)
                .load("https://euw1.api.riotgames.com/lol/champion-mastery/v4/champion-masteries/by-summoner/" + strEncryptedSummonerId + "?api_key=" + API_KEY)
                .asJsonArray()
                .setCallback(new FutureCallback<JsonArray>() {
                    @Override
                    public void onCompleted(Exception e, JsonArray result) {
                        int i = 0, highestChampionLevel = 0, highestChampionExp = 0, mostPlayedChamp = 0, championExp = 0, championLevel = 0, champ = 0;

                        for(i = 0; i < result.size(); i++)  // Tant qu'il y a des objets Json à parser (tant qu'on a pas fait tous les champions ?)
                        {
                            JsonObject jObj = result.get(i).getAsJsonObject();

                            championLevel = jObj.get("championLevel").getAsInt();
                            championExp = jObj.get("championPointsSinceLastLevel").getAsInt();
                            champ = jObj.get("championId").getAsInt();

                            if(championLevel > highestChampionLevel){
                                highestChampionLevel = championLevel;
                                highestChampionExp = championExp;
                                mostPlayedChamp = champ;
                            }

                            if(championLevel == highestChampionLevel){
                                if(championExp > highestChampionExp){
                                    highestChampionExp = championExp;
                                    mostPlayedChamp = champ;
                                }
                            }
                        }
                        SharedPreferences.Editor preferencesEditor = mPreferences.edit();
                        preferencesEditor.putInt("mostPlayedChamp", mostPlayedChamp);
                        preferencesEditor.putInt("highestChampionLevel", highestChampionLevel);
                        preferencesEditor.putInt("highestChampionExp", highestChampionExp);
                        preferencesEditor.apply();
                    }
                });

        String strChampionVersion = mPreferences.getString("championVersion", "null");

        // Récupération du nom du champion le plus joué à partir de son id (ici, "key") TODO : à terminer
//        Ion.with(this)
//                .load("http://ddragon.leagueoflegends.com/cdn/" + strChampionVersion + "/data/fr_FR/champion.json") // le "fr_FR" est rentré dans le dur. A voir pour changer (chiant)
//                .asJsonObject()
//                .setCallback(new FutureCallback<JsonObject>() {
//                    @Override
//                    public void onCompleted(Exception e, JsonObject result) {
//                        SharedPreferences.Editor preferencesEditor = mPreferences.edit();
//                        int i;
//                        int mostPlayedChamp = mPreferences.getInt("mostPlayedChamp", 0);
//                        JsonArray resultArray = result.get("data").getAsJsonArray();    // N'a pas l'air de fonctionner...
//                        for(i = 0; i < resultArray.size(); i++){
//                            JsonObject jObj = resultArray.get(i).getAsJsonObject();
//                            if(mostPlayedChamp == jObj.get("key").getAsInt()){
//                                String mostPlayedChampName = jObj.get("id").getAsString();
//                                preferencesEditor.putString("mostPlayedChampName", mostPlayedChampName);
//                                preferencesEditor.apply();
//                            }
//                        }
//                    }
//                });

        TextView tvPlayerMostPlayedChamp = findViewById(R.id.tvPlayerMostPlayedChamp);
        String temp = tvPlayerMostPlayedChamp.getText().toString();

        String mostPlayedChampName = mPreferences.getString("mostPlayedChampName", "null");

        tvPlayerMostPlayedChamp.setText(temp + mostPlayedChampName);

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
