package com.example.wpin.projettracking;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
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
import org.w3c.dom.Text;

import java.util.Iterator;
import java.util.List;

public class PlayerGlobalStatsActivity extends AppCompatActivity
{
    final static String API_KEY = "RGAPI-93a610eb-8c7f-4373-9357-f23792792394"; // à remplacer si expirée
    String strProfileIconUrl;
    private SharedPreferences mPreferences;
    private String sharedPrefFile = "com.example.wpin.projettracking";

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player_global_stats);

        ImageButton ibSummary = findViewById(R.id.ibSummary);
        ibSummary.setImageResource(R.mipmap.ic_navigation_summary_on);

        mPreferences = getSharedPreferences(sharedPrefFile, MODE_PRIVATE);

        // Récupération du username et de la région recherchés par l'utilisateur.
        String strSearchedUsername = getIntent().getStringExtra("searchedUsername");
        String strSearchedRegion = getIntent().getStringExtra("searchedRegion");

        getLastVersions(strSearchedRegion);

        String strRegion = getRegionName(strSearchedRegion);    // Récupération du nom de la région à mettre dans l'URL de requête

        TextView tvPlayerRegion = findViewById(R.id.tvPlayerRegion);
        tvPlayerRegion.setText(strSearchedRegion);  // On remplace "Région" par la région du joueur

        getPlayerGlobalInformations(strSearchedUsername, strRegion);



        /*Button btnUpdatePlayerProfile = findViewById(R.id.btnUpdatePlayerProfile);
        btnUpdatePlayerProfile.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {   // Quand on clique sur Mettre à jour le profil
                updatePlayerProfile();  // TODO : On appelle la méthode updatePlayerProfile()
            }
        });*/
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

    public void getLastVersions(String strSearchedRegion){
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

    public void getPlayerGlobalInformations(String strSearchedUsername, String strRegion){
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

                        getPlayerMostPlayedChamp();


                    }
                });
    }

    public void getPlayerMostPlayedChamp(){
        String strEncryptedSummonerId = mPreferences.getString("encryptedSummonerId", "null");

        // Requête et traitement pour savoir quel champ du joueur a le plus d'expérience
        Ion.with(this)
                .load("https://euw1.api.riotgames.com/lol/champion-mastery/v4/champion-masteries/by-summoner/" + strEncryptedSummonerId + "?api_key=" + API_KEY)
                .asJsonArray()
                .setCallback(new FutureCallback<JsonArray>() {
                    @Override
                    public void onCompleted(Exception e, JsonArray result) {
                        int i, championPoints = 0, champ = 0;
                        int[] highestChampionPointsArray = {0, 0, 0};
                        int[] mostPlayedChampArray = {0, 0, 0};

                        for(i = 0; i < result.size(); i++)  // Tant qu'il y a des objets Json à parser (tant qu'on a pas fait tous les champions ?)
                        {
                            JsonObject jObj = result.get(i).getAsJsonObject();

                            championPoints = jObj.get("championPoints").getAsInt();
                            champ = jObj.get("championId").getAsInt();

                            if(championPoints > highestChampionPointsArray[0]){
                                highestChampionPointsArray[0] = championPoints;
                                mostPlayedChampArray[0] = champ;
                            }
                            else{
                                if(championPoints > highestChampionPointsArray[1]){
                                    highestChampionPointsArray[1] = championPoints;
                                    mostPlayedChampArray[1] = champ;
                                }
                                else{
                                    if(championPoints > highestChampionPointsArray[2]){
                                        highestChampionPointsArray[2] = championPoints;
                                        mostPlayedChampArray[2] = champ;
                                    }
                                }
                            }
                        }
                        SharedPreferences.Editor preferencesEditor = mPreferences.edit();
                        preferencesEditor.putInt("mostPlayedChamp0", mostPlayedChampArray[0]);
                        preferencesEditor.putInt("championPoints0", highestChampionPointsArray[0]);
                        preferencesEditor.putInt("mostPlayedChamp1", mostPlayedChampArray[1]);
                        preferencesEditor.putInt("championPoints1", highestChampionPointsArray[1]);
                        preferencesEditor.putInt("mostPlayedChamp2", mostPlayedChampArray[2]);
                        preferencesEditor.putInt("championPoints2", highestChampionPointsArray[2]);
                        preferencesEditor.apply();

                        String strChampionVersion = mPreferences.getString("championVersion", "null");

                        getPlayerMostPlayedChampInfo();


                    }
                });
    }

    public void getPlayerMostPlayedChampInfo(){
        // Récupération du nom du champion le plus joué à partir de son id (ici, "key") TODO : à terminer
        final String strChampionVersion = mPreferences.getString("championVersion", "null");


        Ion.with(this)
                .load("http://ddragon.leagueoflegends.com/cdn/" + strChampionVersion + "/data/fr_FR/champion.json") // le "fr_FR" est rentré dans le dur. A voir pour changer (chiant)
                .asString()
                .setCallback(new FutureCallback<String>() {
                    @Override
                    public void onCompleted(Exception e, String result) {
                        JSONArray listeChampion = new JSONArray();
                        int i, j;
                        String[] mostPlayedChampName = {"", "", ""};
                        int[] mostPlayedChampArray = {0, 0, 0};
                        mostPlayedChampArray[0] = mPreferences.getInt("mostPlayedChamp0", 0);
                        mostPlayedChampArray[1] = mPreferences.getInt("mostPlayedChamp1", 0);
                        mostPlayedChampArray[2] = mPreferences.getInt("mostPlayedChamp2", 0);
                        for(j = 0; j < 3; j++)
                        {
                            try
                            {
                                JSONObject test = new JSONObject(result);
                                test = test.getJSONObject("data");
                                Iterator<String> it = test.keys();
                                while (it.hasNext())
                                {
                                    String key = it.next();
                                    if (test.get(key) instanceof JSONObject)
                                    {
                                        listeChampion.put(test.get(key));
                                    }
                                }
                                for (i = 0; i < listeChampion.length(); i++)
                                {
                                    SharedPreferences.Editor preferencesEditor = mPreferences.edit();

                                    JSONObject jObj = listeChampion.getJSONObject(i);
                                    int champId = jObj.getInt("key");

                                    if (mostPlayedChampArray[j] == champId)
                                    {
                                        String strMostPlayedChampName = jObj.get("id").toString();

                                        preferencesEditor.putString("mostPlayedChampName" + j, strMostPlayedChampName);

                                        JSONObject jObjImage = jObj.getJSONObject("image");
                                        String fullImage = jObjImage.getString("full");
                                        preferencesEditor.putString("mostPlayedChampImg" + j, fullImage);
                                    }

                                    JSONObject imageJObj = jObj.getJSONObject("image");
                                    String champIcon = imageJObj.getString("full");
                                    preferencesEditor.putString("champIcon" + champId, champIcon);

                                    preferencesEditor.apply();
                                }
                            } catch (JSONException exception)
                            {
                                exception.printStackTrace();
                            }
                            mostPlayedChampName[j] = mPreferences.getString("mostPlayedChampName" + j, "null");
                        }

                        for(j = 0; j < 3; j++){
                            ImageView[] ivMostPlayedChamp = {findViewById(R.id.ivMostPlayedChamp0),
                                                            findViewById(R.id.ivMostPlayedChamp1),
                                                            findViewById(R.id.ivMostPlayedChamp2)};

                            String champIconUrl = "http://ddragon.leagueoflegends.com/cdn/" + strChampionVersion + "/img/champion/"
                                                    + mPreferences.getString("mostPlayedChampImg" + j, "null");

                            Ion.with(ivMostPlayedChamp[j]).load(champIconUrl);
                        }

                        TextView tvMPCName = findViewById(R.id.tvMPCName);

                        tvMPCName.setText(mostPlayedChampName[0] + ", " + mostPlayedChampName[1] + ", " + mostPlayedChampName[2]);

                        getPlayerLeagueInfo();

                        getPlayerLeagueInfoFlex();

                        /* // JsonArray resultArray = result.get("data").getAsJsonArray(); // N'a pas l'air de fonctionner...
                        for(i = 0; i < resultArray.size(); i++){
                            JsonObject jObj = resultArray.get(i).getAsJsonObject();
                            if(mostPlayedChamp == jObj.get("key").getAsInt()){
                                String mostPlayedChampName = jObj.get("id").getAsString();
                                preferencesEditor.putString("mostPlayedChampName", mostPlayedChampName);
                                preferencesEditor.apply();
                            }
                        } */
                    }
                });
    }

    public void getPlayerLeagueInfo(){
        String strEncryptedSummonerId = mPreferences.getString("encryptedSummonerId", "null");

        Ion.with(this)
                .load("https://euw1.api.riotgames.com/lol/league/v4/positions/by-summoner/" + strEncryptedSummonerId + "?api_key=" + API_KEY)
                .asJsonArray()
                .setCallback(new FutureCallback<JsonArray>() {
                    @Override
                    public void onCompleted(Exception e, JsonArray result) {
                        int i;
                        boolean isRanked = false;

                        TextView tvSoloGames = findViewById(R.id.tvSoloGames);
                        TextView tvSoloVictories = findViewById(R.id.tvSoloWins);
                        TextView tvSoloDefeats = findViewById(R.id.tvSoloLosses);
                        TextView tvLeague = findViewById(R.id.tvLeague);
                        ImageView ivLeague = findViewById(R.id.ivLeague);

                        for(i = 0; i < result.size(); i++){
                            JsonObject jObj = result.get(i).getAsJsonObject();
                            if(jObj.get("queueType").getAsString().equals("RANKED_SOLO_5x5"))
                            {
                                String league = jObj.get("tier").getAsString();
                                String rank = jObj.get("rank").getAsString();
                                int wins = jObj.get("wins").getAsInt();
                                int losses = jObj.get("losses").getAsInt();
                                int leaguePoints = jObj.get("leaguePoints").getAsInt();

                                SharedPreferences.Editor preferencesEditor = mPreferences.edit();
                                preferencesEditor.putString("league", league);
                                preferencesEditor.putString("rank", rank);
                                preferencesEditor.putInt("wins", wins);
                                preferencesEditor.putInt("losses", losses);
                                preferencesEditor.putInt("leaguePoints", leaguePoints);
                                preferencesEditor.apply();

                                int soloGames = wins + losses;
                                tvSoloGames.setText(soloGames + "P");
                                tvSoloVictories.setText(wins + "V");
                                tvSoloDefeats.setText(losses + "D");
                                tvLeague.setText(league + " " + rank + " - " + leaguePoints + " LP");
                                Drawable drawLeagueEmblem = getLeagueEmblem(league);
                                ivLeague.setImageDrawable(drawLeagueEmblem);

                                isRanked = true;
                            }
                        }
                        if(!isRanked){
                            ivLeague.setImageResource(R.mipmap.ic_emblem_iron);
                            tvLeague.setText("Unranked");
                        }
                    }
                });
    }

    public Drawable getLeagueEmblem(String league){
        Drawable drawLeagueEmblem = getResources().getDrawable(R.mipmap.ic_emblem_iron);

        switch(league){
            case "UNRANKED":
                drawLeagueEmblem = getResources().getDrawable(R.mipmap.ic_emblem_iron);
                break;
            case "BRONZE":
                drawLeagueEmblem = getResources().getDrawable(R.mipmap.ic_emblem_bronze);
                break;
            case "SILVER":
                drawLeagueEmblem = getResources().getDrawable(R.mipmap.ic_emblem_silver);
                break;
            case "GOLD":
                drawLeagueEmblem = getResources().getDrawable(R.mipmap.ic_emblem_gold);
                break;
            case "PLATINIUM":
                drawLeagueEmblem = getResources().getDrawable(R.mipmap.ic_emblem_platinium);
                break;
            case "DIAMOND":
                drawLeagueEmblem = getResources().getDrawable(R.mipmap.ic_emblem_diamond);
                break;
            case "MASTER":
                drawLeagueEmblem = getResources().getDrawable(R.mipmap.ic_emblem_master);
                break;
            case "GRANDMASTER":
                drawLeagueEmblem = getResources().getDrawable(R.mipmap.ic_emblem_grandmaster);
                break;
            case "CHALLENGER":
                drawLeagueEmblem = getResources().getDrawable(R.mipmap.ic_emblem_challenger);
                break;
        }
        return drawLeagueEmblem;
    }

    public void getPlayerLeagueInfoFlex(){
        String strEncryptedSummonerId = mPreferences.getString("encryptedSummonerId", "null");

        Ion.with(this)
                .load("https://euw1.api.riotgames.com/lol/league/v4/positions/by-summoner/" + strEncryptedSummonerId + "?api_key=" + API_KEY)
                .asJsonArray()
                .setCallback(new FutureCallback<JsonArray>() {
                    @Override
                    public void onCompleted(Exception e, JsonArray result) {
                        int i;
                        boolean isRanked = false;

                        TextView tvFlexGames = findViewById(R.id.tvFlexGames);
                        TextView tvFlexVictories = findViewById(R.id.tvFlexWins);
                        TextView tvFlexDefeats = findViewById(R.id.tvFlexLosses);
                        TextView tvFlexLeague = findViewById(R.id.tvFlexLeague);
                        ImageView ivFlexLeague = findViewById(R.id.ivFlexLeague);

                        for(i = 0; i < result.size(); i++){
                            JsonObject jObj = result.get(i).getAsJsonObject();
                            if(jObj.get("queueType").getAsString().equals("RANKED_FLEX_SR"))
                            {
                                String flexLeague = jObj.get("tier").getAsString();
                                String flexRank = jObj.get("rank").getAsString();
                                int flexWins = jObj.get("wins").getAsInt();
                                int flexLosses = jObj.get("losses").getAsInt();
                                int flexLeaguePoints = jObj.get("leaguePoints").getAsInt();

                                SharedPreferences.Editor preferencesEditor = mPreferences.edit();
                                preferencesEditor.putString("league", flexLeague);
                                preferencesEditor.putString("rank", flexRank);
                                preferencesEditor.putInt("wins", flexWins);
                                preferencesEditor.putInt("losses", flexLosses);
                                preferencesEditor.putInt("leaguePoints", flexLeaguePoints);
                                preferencesEditor.apply();

                                int flexGames = flexWins + flexLosses;
                                tvFlexGames.setText(flexGames + "P");
                                tvFlexVictories.setText(flexWins + "V");
                                tvFlexDefeats.setText(flexLosses + "D");
                                tvFlexLeague.setText(flexLeague + " " + flexRank + " - " + flexLeaguePoints + " LP");
                                Drawable drawLeagueEmblemFlex = getLeagueEmblemFlex(flexLeague);
                                ivFlexLeague.setImageDrawable(drawLeagueEmblemFlex);

                                isRanked = true;
                            }
                        }
                        if(!isRanked){
                            ivFlexLeague.setImageResource(R.mipmap.ic_emblem_iron);
                            tvFlexLeague.setText("Unranked");
                        }
                    }
                });
    }

    public Drawable getLeagueEmblemFlex(String league){
        Drawable drawLeagueEmblemFlex = getResources().getDrawable(R.mipmap.ic_emblem_iron);

        switch(league){
            case "UNRANKED":
                drawLeagueEmblemFlex = getResources().getDrawable(R.mipmap.ic_emblem_iron);
                break;
            case "BRONZE":
                drawLeagueEmblemFlex = getResources().getDrawable(R.mipmap.ic_emblem_bronze);
                break;
            case "SILVER":
                drawLeagueEmblemFlex = getResources().getDrawable(R.mipmap.ic_emblem_silver);
                break;
            case "GOLD":
                drawLeagueEmblemFlex = getResources().getDrawable(R.mipmap.ic_emblem_gold);
                break;
            case "PLATINIUM":
                drawLeagueEmblemFlex = getResources().getDrawable(R.mipmap.ic_emblem_platinium);
                break;
            case "DIAMOND":
                drawLeagueEmblemFlex = getResources().getDrawable(R.mipmap.ic_emblem_diamond);
                break;
            case "MASTER":
                drawLeagueEmblemFlex = getResources().getDrawable(R.mipmap.ic_emblem_master);
                break;
            case "GRANDMASTER":
                drawLeagueEmblemFlex = getResources().getDrawable(R.mipmap.ic_emblem_grandmaster);
                break;
            case "CHALLENGER":
                drawLeagueEmblemFlex = getResources().getDrawable(R.mipmap.ic_emblem_challenger);
                break;
        }
        return drawLeagueEmblemFlex;
    }

    public void updatePlayerProfile(){
        // TODO : Implémenter la fonction
    }
}
