package com.example.wpin.projettracking;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;

import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;

public class ChampionsActivity extends AppCompatActivity
{
    private SharedPreferences mPreferences;
    private String sharedPrefFile = "com.example.wpin.projettracking";

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_champions);

        ImageButton ibChampions = findViewById(R.id.ibChampions);
        ibChampions.setImageResource(R.mipmap.ic_navigation_champion_on);

        mPreferences = getSharedPreferences(sharedPrefFile, MODE_PRIVATE);

        final String strChampionVersion = mPreferences.getString("championVersion", "null");

        Ion.with(this)
                .load("http://ddragon.leagueoflegends.com/cdn/" + strChampionVersion + "/data/fr_FR/champion.json") // le "fr_FR" est rentré dans le dur. A voir pour changer (chiant)
                .asString()
                .setCallback(new FutureCallback<String>(){
                    @Override
                    public void onCompleted(Exception e, String result){
                        JSONArray listeChampion = new JSONArray();
                        int i;
                        int j = 0, k = 0;
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

                            TableLayout tableChamp = findViewById(R.id.table);
                            final Context context = getApplicationContext();

                            TableRow tableRow = new TableRow(context);
                            tableRow.setGravity(Gravity.CENTER);
                            ImageButton ibChampIcon;

                            // Pour chaque champion
                            for (i = 0; i < listeChampion.length(); i++)
                            {
                                j++;

                                // On récupère les données relatives à ce champion dans un JSONObject
                                JSONObject jObj = listeChampion.getJSONObject(i);

                                // On récupère l'id pour les mettre dans l'ordre (et avoir l'index)
                                final int champId = jObj.getInt("key");
                                final String champName = jObj.getString("id");

                                // On récupère l'icône du champion en fonction de l'index champId
                                String champIcon = mPreferences.getString("champIcon" + champId, "null");


                                ibChampIcon = new ImageButton(context);

                                TableRow.LayoutParams lp = new TableRow.LayoutParams(240, 240);

                                ibChampIcon.setBackgroundDrawable(null);

                                Ion.with(ibChampIcon).load("http://ddragon.leagueoflegends.com/cdn/" + strChampionVersion + "/img/champion/" + champIcon);

                                ibChampIcon.setOnClickListener(new View.OnClickListener() {
                                    public void onClick(View v) {
                                        Intent i = new Intent(context, ChampionDetailsActivity.class);
                                        i.putExtra("champId", champId);
                                        i.putExtra("champName", champName);
                                        startActivity(i);
                                    }
                                });

                                ibChampIcon.setAdjustViewBounds(true);

                                ibChampIcon.setLayoutParams(lp);
                                tableRow.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT, 1));

                                Log.d("DEBUG", "row.addView(image " + i + ")");
                                tableRow.addView(ibChampIcon);

                                if(j == 4 || i + 1 == listeChampion.length()){
                                    Log.d("DEBUG", "table.addView(row " + k + ")");
                                    tableChamp.addView(tableRow, k);
                                    j = 0;
                                    k++;
                                    tableRow = new TableRow(context);
                                    tableRow.setGravity(Gravity.CENTER);
                                }
                            }
                        } catch (JSONException exception)
                        {
                            exception.printStackTrace();
                        }
                    }
                });
    }
}
