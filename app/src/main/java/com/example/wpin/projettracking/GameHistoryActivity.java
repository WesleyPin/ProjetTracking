package com.example.wpin.projettracking;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;

import static com.example.wpin.projettracking.PlayerGlobalStatsActivity.API_KEY;

public class GameHistoryActivity extends AppCompatActivity
{
    private SharedPreferences mPreferences;
    private String sharedPrefFile = "com.example.wpin.projettracking";

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_history);

        ImageButton ibHistory = findViewById(R.id.ibHistory);
        ibHistory.setImageResource(R.mipmap.ic_navigation_history_on);

        mPreferences = getSharedPreferences(sharedPrefFile, MODE_PRIVATE);

        String encryptedAccountId = mPreferences.getString("encryptedAccountId", "null");
        Log.d("test", encryptedAccountId);

        String region = mPreferences.getString("region", "euw1");
        Log.d("test", region);
        Log.d("test", API_KEY);

        Ion.with(this)
                .load("https://" + region + ".api.riotgames.com/lol/match/v4/matchlists/by-account/" + encryptedAccountId + "?api_key=" + API_KEY)
                .asJsonObject()
                .setCallback(new FutureCallback<JsonObject>()
                {
                    @Override
                    public void onCompleted(Exception e, JsonObject result)
                    {
                        TableLayout tableLayout = findViewById(R.id.table);
                        Context context = getApplicationContext();
                        JsonArray matchesJArr = result.get("matches").getAsJsonArray();
                        int i;
                        for(i = 0; i < 20; i++){
                            SharedPreferences.Editor preferencesEditor = mPreferences.edit();
                            JsonObject jObj = matchesJArr.get(i).getAsJsonObject();
                            String champId = jObj.get("champion").getAsString();


                            preferencesEditor.putString("thisChampId", champId);
                            preferencesEditor.apply();

                            TableRow tableRow = new TableRow(context);

                            ImageView ivChamp = new ImageView(context);
                            getChampImgUrl();   // TODO : BUG : ne retourne pas le bon champion
                            Ion.with(ivChamp).load(mPreferences.getString("thisChampImgUrl", "null"));
                            Log.d("testURL", mPreferences.getString("thisChampImgUrl", "null"));

                            tableRow.addView(ivChamp);


                            //getMatchInfo();

                            tableLayout.addView(tableRow, i);

                            preferencesEditor.apply();
                        }

                    }
                });
    }

    public void getChampImgUrl(){
        final String strChampionVersion = mPreferences.getString("championVersion", "null");

        Ion.with(this)
                .load("http://ddragon.leagueoflegends.com/cdn/" + strChampionVersion + "/data/fr_FR/champion.json") // le "fr_FR" est rentré dans le dur. A voir pour changer (chiant)
                .asString()
                .setCallback(new FutureCallback<String>() {
                    @Override
                    public void onCompleted(Exception e, String result) {
                        JSONArray listeChampion = new JSONArray();
                        int i;
                        String thisChampId = mPreferences.getString("thisChampId", "0");
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

                                if (thisChampId.equals(Integer.toString(champId)))
                                {
                                    String thisChampName = jObj.get("id").toString();

                                    preferencesEditor.putString("thisChampName", thisChampName);

                                    JSONObject jObjImage = jObj.getJSONObject("image");
                                    String fullImage = jObjImage.getString("full");
                                    preferencesEditor.putString("thisChampImg", fullImage);
                                }

                                String champIconUrl = "http://ddragon.leagueoflegends.com/cdn/" + strChampionVersion + "/img/champion/"
                                        + mPreferences.getString("thisChampImg", "null");
                                preferencesEditor.putString("thisChampImgUrl", champIconUrl);

                                preferencesEditor.apply();
                            }
                        } catch (JSONException exception)
                        {
                            exception.printStackTrace();
                        }
                    }
                });
    }

//    public void getMatchInfo(){
//        Ion.with(this)
//                .load()
//                .asJson
//    }
}
