package com.example.wpin.projettracking;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;

import org.w3c.dom.Text;

import java.lang.reflect.Type;
import java.util.ArrayList;

public class ChampionDetailsActivity extends AppCompatActivity
{

    private SharedPreferences mPreferences;
    private String sharedPrefFile = "com.example.wpin.projettracking";

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_champion_details);

        mPreferences = getSharedPreferences(sharedPrefFile, MODE_PRIVATE);

        ImageButton ibChampions = findViewById(R.id.ibChampions);
        ibChampions.setImageResource(R.mipmap.ic_navigation_champion_on);

        int champId = getIntent().getIntExtra("champId", 1);
        final String champName = getIntent().getStringExtra("champName");

        final String strChampionVersion = mPreferences.getString("championVersion", "null");

        Ion.with(this)
                .load("http://ddragon.leagueoflegends.com/cdn/" + strChampionVersion + "/data/fr_FR/champion/" + champName + ".json")
                .asJsonObject()
                .setCallback(new FutureCallback<JsonObject>()
                {
                    @Override
                    public void onCompleted(Exception e, JsonObject result)
                    {
                        Context context = getApplicationContext();

                        JsonObject dataJObj = result.get("data").getAsJsonObject();
                        JsonObject globalJObj = dataJObj.get(champName).getAsJsonObject();
                        JsonObject imageJObj = globalJObj.get("image").getAsJsonObject();
                        JsonArray tagsJArr = globalJObj.get("tags").getAsJsonArray();
                        JsonArray spellsJArr = globalJObj.get("spells").getAsJsonArray();

                        String champIcon = imageJObj.get("full").getAsString();
                        ImageView ivChampIcon = findViewById(R.id.ivChampIcon);
                        Ion.with(ivChampIcon).load("http://ddragon.leagueoflegends.com/cdn/9.3.1/img/champion/" + champIcon);

                        TextView tvChampName = findViewById(R.id.tvChampName);
                        String strChampName = globalJObj.get("name").getAsString();
                        tvChampName.setText(strChampName);

                        TextView tvChampTitle = findViewById(R.id.tvChampTitle);
                        String champTitle = globalJObj.get("title").getAsString();
                        tvChampTitle.setText(champTitle);

                        TextView tvChampTags = findViewById(R.id.tvChampTags);
                        String[] champTags = new String[10];
                        int i;
                        for(i = 0; i < tagsJArr.size(); i++){
                            champTags[i] = tagsJArr.get(i).getAsString();
                            if(i == 0){
                                tvChampTags.setText(champTags[i]);
                            } else {
                                tvChampTags.append(", " + champTags[i]);
                            }
                        }
                        ImageView ivPassiveIcon = findViewById(R.id.ivPassive);
                        TextView tvPassiveName = findViewById(R.id.tvPassiveName);
                        TextView tvDescPassive = findViewById(R.id.tvDescPassive);

                        JsonObject passiveJObj = globalJObj.get("passive").getAsJsonObject();
                        String passiveName = passiveJObj.get("name").getAsString();
                        String descPassive = passiveJObj.get("description").getAsString();
                        JsonObject imagePassiveJObj = passiveJObj.get("image").getAsJsonObject();
                        String passiveImage = imagePassiveJObj.get("full").getAsString();
                        String passiveImageUrl = "http://ddragon.leagueoflegends.com/cdn/" + strChampionVersion + "/img/passive/" + passiveImage;

                        Ion.with(ivPassiveIcon).load(passiveImageUrl);
                        tvPassiveName.setText(passiveName);
                        tvDescPassive.setText(descPassive);

                        ImageView[] ivSpellIcons = {
                                findViewById(R.id.ivSpell0),
                                findViewById(R.id.ivSpell1),
                                findViewById(R.id.ivSpell2),
                                findViewById(R.id.ivSpell3)};

                        TextView[] tvSpellNames = {
                                findViewById(R.id.tvSpellName0),
                                findViewById(R.id.tvSpellName1),
                                findViewById(R.id.tvSpellName2),
                                findViewById(R.id.tvSpellName3)
                        };
                        TextView[] tvSpellDesc = {
                                findViewById(R.id.tvDescSpell0),
                                findViewById(R.id.tvDescSpell1),
                                findViewById(R.id.tvDescSpell2),
                                findViewById(R.id.tvDescSpell3)
                        };

                        for(i = 0; i < 4; i++){
                            JsonObject spellJObj = spellsJArr.get(i).getAsJsonObject();
                            String spellName = spellJObj.get("name").getAsString();
                            String descSpell = spellJObj.get("description").getAsString();
                            JsonObject imageSpellJObj = spellJObj.get("image").getAsJsonObject();
                            String spellImage = imageSpellJObj.get("full").getAsString();
                            String spellImageUrl = "http://ddragon.leagueoflegends.com/cdn/" + strChampionVersion + "/img/spell/" + spellImage;

                            Ion.with(ivSpellIcons[i]).load(spellImageUrl);
                            tvSpellNames[i].setText(spellName);
                            tvSpellDesc[i].setText(descSpell);
                        }
                    }
                });
    }
}
