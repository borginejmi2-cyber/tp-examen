package com.example.tp_examen.ui

import android.graphics.Color
import android.net.Uri
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.tp_examen.R
import com.example.tp_examen.data.Contact
import java.io.File

class ContactAdapter(
    private val onClick: (Contact) -> Unit,
    private val onAppeler: (Contact) -> Unit
) : ListAdapter<Contact, ContactAdapter.VH>(DIFF) {

    private var tailleSp = 16f
    private var couleur = Color.BLACK
    private var appelActif = false

    /** Appelé quand les préférences changent : on redessine toutes les lignes. */
    fun appliquerPreferences(tailleSp: Float, couleur: Int, appelActif: Boolean) {
        this.tailleSp = tailleSp
        this.couleur = couleur
        this.appelActif = appelActif
        notifyDataSetChanged()
    }

    class VH(view: View) : RecyclerView.ViewHolder(view) {
        val img: ImageView = view.findViewById(R.id.imgPhoto)
        val nom: TextView = view.findViewById(R.id.txtNom)
        val prenom: TextView = view.findViewById(R.id.txtPrenom)
        val tel: TextView = view.findViewById(R.id.txtTelephone)
        val btnAppeler: ImageButton = view.findViewById(R.id.btnAppeler)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_contact, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val c = getItem(position)

        holder.nom.text = c.nom
        holder.prenom.text = c.prenom
        holder.tel.text = c.telephone

        // Préférences : taille pour les 3 textes, couleur pour nom et prénom
        holder.nom.setTextSize(TypedValue.COMPLEX_UNIT_SP, tailleSp)
        holder.prenom.setTextSize(TypedValue.COMPLEX_UNIT_SP, tailleSp)
        holder.tel.setTextSize(TypedValue.COMPLEX_UNIT_SP, tailleSp)
        holder.nom.setTextColor(couleur)
        holder.prenom.setTextColor(couleur)

        // Photo
        val p = c.photoUri
        if (p != null && File(p).exists()) {
            holder.img.setImageURI(Uri.fromFile(File(p)))
        } else {
            holder.img.setImageResource(android.R.drawable.ic_menu_myplaces)
        }

        // Bouton Appeler : visible seulement si la préférence est activée
        holder.btnAppeler.visibility = if (appelActif) View.VISIBLE else View.GONE
        holder.btnAppeler.setOnClickListener { onAppeler(c) }   // ne déclenche pas le clic de la ligne

        holder.itemView.setOnClickListener { onClick(c) }
    }

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<Contact>() {
            override fun areItemsTheSame(oldItem: Contact, newItem: Contact): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: Contact, newItem: Contact): Boolean {
                return oldItem == newItem
            }
        }
    }
}