package com.example.wpin.projettracking;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;
import java.util.List;

public class PlayerGlobalStatsActivity extends AppCompatActivity
{
    final static String API_KEY = "RGAPI-9b85d93d-272e-4766-a67f-c8981d1ac878"; // à remplacer si expirée
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

        getLastVersions(this, strSearchedRegion);

        String strRegion = getRegionName(strSearchedRegion);    // Récupération du nom de la région à mettre dans l'URL de requête

        TextView tvPlayerRegion = findViewById(R.id.tvPlayerRegion);
        tvPlayerRegion.setText(strSearchedRegion);  // On remplace "Région" par la région du joueur

        getPlayerGlobalInformations(this, strSearchedUsername, strRegion);

        getPlayerMostPlayedChamp(this);

        String strChampionVersion = mPreferences.getString("championVersion", "null");

        getPlayerMostPlayedChampInfo(this);

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

    public void getLastVersions(Context c, String strSearchedRegion){
        // Récupération de les dernières versions de l'API statique
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
    }

    public void getPlayerGlobalInformations(Context c, String strSearchedUsername, String strRegion){
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
    }

    public void getPlayerMostPlayedChamp(Context c){
        String strEncryptedSummonerId = mPreferences.getString("encryptedSummonerId", "null");

        // Requête et traitement pour savoir quel champ du joueur a le plus d'expérience
        Ion.with(this)
                .load("https://euw1.api.riotgames.com/lol/champion-mastery/v4/champion-masteries/by-summoner/" + strEncryptedSummonerId + "?api_key=" + API_KEY)
                .asJsonArray()
                .setCallback(new FutureCallback<JsonArray>() {
                    @Override
                    public void onCompleted(Exception e, JsonArray result) {
                        int i = 0, highestChampionPoints = 0, mostPlayedChamp = 0, championPoints = 0, champ = 0;

                        for(i = 0; i < result.size(); i++)  // Tant qu'il y a des objets Json à parser (tant qu'on a pas fait tous les champions ?)
                        {
                            JsonObject jObj = result.get(i).getAsJsonObject();

                            championPoints = jObj.get("championPoints").getAsInt();
                            champ = jObj.get("championId").getAsInt();

                            if(championPoints > highestChampionPoints){
                                highestChampionPoints = championPoints;
                                mostPlayedChamp = champ;
                            }
                        }
                        SharedPreferences.Editor preferencesEditor = mPreferences.edit();
                        preferencesEditor.putInt("mostPlayedChamp", mostPlayedChamp);
                        preferencesEditor.putInt("championPoints", highestChampionPoints);
                        preferencesEditor.apply();
                    }
                });
    }

    public void getPlayerMostPlayedChampInfo(Context c){
        // Récupération du nom du champion le plus joué à partir de son id (ici, "key") TODO : à terminer
        String strChampionVersion = mPreferences.getString("championVersion", "null");





        /* Ion.with(this)
                .load("http://ddragon.leagueoflegends.com/cdn/" + strChampionVersion + "/data/fr_FR/champion.json") // le "fr_FR" est rentré dans le dur. A voir pour changer (chiant)
                .asString()
                .setCallback(new FutureCallback<String>() {
                    @Override
                    public void onCompleted(Exception e, String result) {
                        SharedPreferences.Editor preferencesEditor = mPreferences.edit();
                        JSONArray listeChampion = new JSONArray();
                        int i;
                        int mostPlayedChamp = mPreferences.getInt("mostPlayedChamp", 0);
                        try {
                            JSONObject test = new JSONObject(result);
                            test = test.getJSONObject("data");
                            Iterator<String> it = test.keys();
                            while(it.hasNext()) {
                                String key = it.next();
                                if (test.get(key) instanceof JSONObject) {
                                    listeChampion.put(test.get(key));
                                }
                            }
                            for(i = 0; i < listeChampion.length(); i++){
                                JSONObject jObj = listeChampion.getJSONObject(i);
                                if(mostPlayedChamp == jObj.getInt("key")){
                                    String mostPlayedChampName = jObj.get("id").toString();
                                    preferencesEditor.putString("mostPlayedChampName", mostPlayedChampName);
                                    preferencesEditor.apply();
                                }
                            }
                        } catch (JSONException exception) {
                            exception.printStackTrace();
                        }




                        // JsonArray resultArray = result.get("data").getAsJsonArray(); // N'a pas l'air de fonctionner...
                        /* for(i = 0; i < resultArray.size(); i++){
                            JsonObject jObj = resultArray.get(i).getAsJsonObject();
                            if(mostPlayedChamp == jObj.get("key").getAsInt()){
                                String mostPlayedChampName = jObj.get("id").getAsString();
                                preferencesEditor.putString("mostPlayedChampName", mostPlayedChampName);
                                preferencesEditor.apply();
                            }
                        }
                    }
                }); */
    }

    public void updatePlayerProfile(){
        // TODO : Implémenter la fonction
    }
}
