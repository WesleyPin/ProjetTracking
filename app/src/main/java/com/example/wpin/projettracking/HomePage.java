package com.example.wpin.projettracking;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

public class HomePage extends AppCompatActivity {

    EditText etSearchPlayer;    // Input pour entrer un nom d'invocateur
    Spinner spinRegion;         // Select des différentes régions
    Intent searchingIntent;     // Intention de recherche
    Button btnSearch;           // Bouton "Rechercher"

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);

        btnSearch = findViewById(R.id.btnSearch);

        btnSearch.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {   // Quand on clique sur Rechercher
                search();  // On appelle la méthode search()
            }
        });
    }

    public void search() {
        etSearchPlayer = findViewById(R.id.etSearchPlayer);
        spinRegion = findViewById(R.id.spRegion);

        searchingIntent = new Intent(this, PlayerGlobalStatsActivity.class);    // On précise l'activité à lancer

        String strSearchedUsername = etSearchPlayer.getText().toString();  // Récupération du nom de joueur rentré par l'utilisateur
        searchingIntent.putExtra("searchedUsername", strSearchedUsername);  // On met en extra strSearchedUsername pour pouvoir le récupérer dans l'autre activité

        String strRegion = spinRegion.getSelectedItem().toString(); // Récupération de la région sélectionnée
        searchingIntent.putExtra("searchedRegion", strRegion);
        startActivity(searchingIntent);
    }
}
