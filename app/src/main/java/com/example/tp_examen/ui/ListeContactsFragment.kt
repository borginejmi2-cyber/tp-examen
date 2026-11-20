package com.example.tp_examen.ui

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.tp_examen.R
import com.example.tp_examen.data.Contact
import com.example.tp_examen.util.PreferencesManager
import com.example.tp_examen.viewmodel.ContactViewModel
import com.google.android.material.floatingactionbutton.FloatingActionButton

class ListeContactsFragment : Fragment(R.layout.fragment_liste_contacts) {

    private val viewModel: ContactViewModel by activityViewModels()
    private lateinit var adapter: ContactAdapter
    private lateinit var prefs: PreferencesManager

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        prefs = PreferencesManager(requireContext())

        adapter = ContactAdapter(
            onClick = { contact -> ouvrirDetail(contact.id) },
            onAppeler = { contact -> appeler(contact) }
        )

        view.findViewById<RecyclerView>(R.id.recyclerContacts).apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@ListeContactsFragment.adapter
        }

        view.findViewById<EditText>(R.id.editFiltre).doAfterTextChanged { texte ->
            viewModel.filtrer(texte?.toString() ?: "")
        }

        view.findViewById<FloatingActionButton>(R.id.fabAjouter).setOnClickListener {
            ouvrirDetail(0L)
        }

        viewModel.contacts.observe(viewLifecycleOwner) { liste ->
            adapter.submitList(liste)
        }
    }

    override fun onResume() {
        super.onResume()
        // Relecture des préférences à chaque retour sur la liste (après Paramètres, par exemple)
        adapter.appliquerPreferences(
            prefs.taillePoliceSp(),
            prefs.couleurTexte(),
            prefs.appelDepuisListe()
        )
        viewModel.charger()   // la liste est relue depuis la base à chaque affichage
    }

    private fun ouvrirDetail(id: Long) {
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, DetailContactFragment.newInstance(id))
            .addToBackStack(null)
            .commit()
    }

    private fun appeler(contact: Contact) {
        val numero = contact.telephone.filter { it.isDigit() || it == '+' }
        if (numero.isEmpty()) {
            Toast.makeText(requireContext(), "Numéro de téléphone vide", Toast.LENGTH_SHORT).show()
            return
        }
        try {
            startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:$numero")))
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(requireContext(), "Aucune application téléphone disponible", Toast.LENGTH_LONG).show()
        }
    }
}