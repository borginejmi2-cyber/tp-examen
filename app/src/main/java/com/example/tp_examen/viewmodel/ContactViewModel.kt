package com.example.tp_examen.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.tp_examen.data.Contact
import com.example.tp_examen.data.ContactDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ContactViewModel(application: Application) : AndroidViewModel(application) {

    private val dao = ContactDao(application)

    private val _contacts = MutableLiveData<List<Contact>>()
    val contacts: LiveData<List<Contact>> = _contacts

    // Texte du filtre courant (conservé pendant la rotation)
    var filtre: String = ""
        private set

    /** (Re)charge la liste depuis la base, en tenant compte du filtre. */
    fun charger() {
        viewModelScope.launch {
            val liste = withContext(Dispatchers.IO) {
                if (filtre.isBlank()) dao.getAll() else dao.search(filtre.trim())
            }
            _contacts.value = liste
        }
    }

    fun filtrer(texte: String) {
        filtre = texte
        charger()
    }

    /** Charge un contact par son _id ; le callback reçoit null s'il est introuvable. */
    fun chargerContact(id: Long, onResult: (Contact?) -> Unit) {
        viewModelScope.launch {
            val contact = withContext(Dispatchers.IO) { dao.getById(id) }
            onResult(contact)
        }
    }

    /** Insère (id == 0) ou met à jour. Le callback reçoit true si ça a réussi. */
    fun enregistrer(contact: Contact, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val ok = withContext(Dispatchers.IO) {
                if (contact.id == 0L) dao.insert(contact) != -1L
                else dao.update(contact) > 0
            }
            if (ok) charger()
            onResult(ok)
        }
    }

    fun supprimer(id: Long, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val ok = withContext(Dispatchers.IO) { dao.delete(id) > 0 }
            if (ok) charger()
            onResult(ok)
        }
    }
}