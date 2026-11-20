package com.example.tp_examen

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.example.tp_examen.ui.ListeContactsFragment
import com.example.tp_examen.ui.ParametresFragment

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(findViewById(R.id.toolbar))

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, ListeContactsFragment())
                .commit()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.action_parametres) {
            val actuel = supportFragmentManager.findFragmentById(R.id.fragment_container)
            if (actuel !is ParametresFragment) {   // évite d'empiler deux fois l'écran
                supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, ParametresFragment())
                    .addToBackStack(null)
                    .commit()
            }
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}