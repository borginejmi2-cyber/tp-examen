package com.example.tp_examen.ui
import android.util.TypedValue
import com.example.tp_examen.util.PreferencesManager
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.example.tp_examen.R
import com.example.tp_examen.data.Contact
import com.example.tp_examen.util.PhotoUtils
import com.example.tp_examen.viewmodel.ContactViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class DetailContactFragment : Fragment(R.layout.fragment_detail_contact) {

    private val viewModel: ContactViewModel by activityViewModels()

    private var contactId: Long = 0L
    private var photoPath: String? = null     // chemin de la photo (stockage interne)
    private var cameraPath: String? = null    // fichier temporaire en cours de prise de vue

    private lateinit var imgPhoto: ImageView
    private lateinit var editNom: EditText
    private lateinit var editPrenom: EditText
    private lateinit var editTelephone: EditText
    private lateinit var editEmail: EditText
    private lateinit var editLatitude: EditText
    private lateinit var editLongitude: EditText

    // --- Galerie ---
    private val galerieLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            if (uri != null) {
                importerPhoto(uri, PhotoUtils.nouveauFichier(requireContext()))
            }
            // uri == null : l'utilisateur a annulé, on ne fait rien
        }

    // --- Caméra ---
    private val cameraLauncher =
        registerForActivityResult(ActivityResultContracts.TakePicture()) { succes ->
            val path = cameraPath
            if (succes && path != null) {
                val f = File(path)
                importerPhoto(Uri.fromFile(f), f)   // on réduit l'image sur place
            } else {
                path?.let { File(it).delete() }     // annulé : on supprime le fichier vide
            }
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        contactId = requireArguments().getLong(ARG_ID, 0L)

        imgPhoto = view.findViewById(R.id.imgPhotoDetail)
        editNom = view.findViewById(R.id.editNom)
        editPrenom = view.findViewById(R.id.editPrenom)
        editTelephone = view.findViewById(R.id.editTelephone)
        editEmail = view.findViewById(R.id.editEmail)
        editLatitude = view.findViewById(R.id.editLatitude)
        editLongitude = view.findViewById(R.id.editLongitude)
        val prefs = PreferencesManager(requireContext())
        val taille = prefs.taillePoliceSp()
        val couleur = prefs.couleurTexte()
        listOf(editNom, editPrenom, editTelephone).forEach {
            it.setTextSize(TypedValue.COMPLEX_UNIT_SP, taille)
        }
        editNom.setTextColor(couleur)
        editPrenom.setTextColor(couleur)
        // Restauration après rotation
        cameraPath = savedInstanceState?.getString(KEY_CAMERA)
        if (savedInstanceState?.containsKey(KEY_PHOTO) == true) {
            photoPath = savedInstanceState.getString(KEY_PHOTO)
        }
        afficherPhoto()

        val btnSupprimer = view.findViewById<Button>(R.id.btnSupprimer)

        if (contactId == 0L) {
            btnSupprimer.visibility = View.GONE        // mode création
        } else {
            val remplir = savedInstanceState == null
            val photoDejaChoisie = savedInstanceState?.containsKey(KEY_PHOTO) == true
            viewModel.chargerContact(contactId) { c ->
                if (!isAdded) return@chargerContact
                if (c == null) {
                    Toast.makeText(requireContext(), "Contact introuvable", Toast.LENGTH_LONG).show()
                    parentFragmentManager.popBackStack()
                    return@chargerContact
                }
                if (!photoDejaChoisie) {
                    photoPath = c.photoUri
                    afficherPhoto()
                }
                if (remplir) {
                    editNom.setText(c.nom)
                    editPrenom.setText(c.prenom)
                    editTelephone.setText(c.telephone)
                    editEmail.setText(c.email ?: "")
                    editLatitude.setText(c.latitude?.toString() ?: "")
                    editLongitude.setText(c.longitude?.toString() ?: "")
                }
            }
        }

        view.findViewById<Button>(R.id.btnGalerie).setOnClickListener { ouvrirGalerie() }
        view.findViewById<Button>(R.id.btnCamera).setOnClickListener { ouvrirCamera() }
        view.findViewById<Button>(R.id.btnAppeler).setOnClickListener { appeler() }
        view.findViewById<Button>(R.id.btnEmail).setOnClickListener { envoyerEmail() }
        view.findViewById<Button>(R.id.btnEnregistrer).setOnClickListener { enregistrer() }
        view.findViewById<Button>(R.id.btnAnnuler).setOnClickListener {
            parentFragmentManager.popBackStack()
        }
        btnSupprimer.setOnClickListener { confirmerSuppression() }
        view.findViewById<Button>(R.id.btnCarte).setOnClickListener { voirSurLaCarte() }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        photoPath?.let { outState.putString(KEY_PHOTO, it) }
        cameraPath?.let { outState.putString(KEY_CAMERA, it) }
    }

    // ---------- Photo ----------

    private fun afficherPhoto() {
        val p = photoPath
        if (p != null && File(p).exists()) {
            imgPhoto.setImageURI(Uri.fromFile(File(p)))
        } else {
            imgPhoto.setImageResource(android.R.drawable.ic_menu_myplaces)
        }
    }

    private fun importerPhoto(source: Uri, dest: File) {
        val appContext = requireContext().applicationContext
        viewLifecycleOwner.lifecycleScope.launch {
            val ok = withContext(Dispatchers.IO) { PhotoUtils.importer(appContext, source, dest) }
            if (ok) {
                photoPath = dest.absolutePath
                afficherPhoto()
            } else {
                dest.delete()
                Toast.makeText(requireContext(), "Impossible de lire cette image", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun ouvrirGalerie() {
        try {
            galerieLauncher.launch("image/*")
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(requireContext(), "Aucune galerie disponible", Toast.LENGTH_LONG).show()
        }
    }

    private fun ouvrirCamera() {
        try {
            val fichier = PhotoUtils.nouveauFichier(requireContext())
            cameraPath = fichier.absolutePath
            val uri = FileProvider.getUriForFile(
                requireContext(), "${requireContext().packageName}.fileprovider", fichier
            )
            cameraLauncher.launch(uri)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(requireContext(), "Aucune application caméra disponible", Toast.LENGTH_LONG).show()
        }
    }

    // ---------- Appeler / E-mail ----------

    private fun appeler() {
        val numero = editTelephone.text.toString().filter { it.isDigit() || it == '+' }
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

    private fun envoyerEmail() {
        val adresse = editEmail.text.toString().trim()
        if (adresse.isEmpty()) {
            Toast.makeText(requireContext(), "Adresse e-mail vide", Toast.LENGTH_SHORT).show()
            return
        }
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:")
            putExtra(Intent.EXTRA_EMAIL, arrayOf(adresse))
        }
        try {
            startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(requireContext(), "Aucune application e-mail disponible", Toast.LENGTH_LONG).show()
        }
    }
    private fun voirSurLaCarte() {
        if (contactId == 0L) {
            Toast.makeText(requireContext(), "Enregistrez d'abord le contact", Toast.LENGTH_SHORT).show()
            return
        }
        viewModel.chargerContact(contactId) { c ->
            if (!isAdded) return@chargerContact
            if (c == null) {
                Toast.makeText(requireContext(), "Contact introuvable", Toast.LENGTH_LONG).show()
                return@chargerContact
            }
            val lat = c.latitude
            val lng = c.longitude
            if (lat == null || lng == null) {
                Toast.makeText(requireContext(), "Aucune position enregistrée pour ce contact", Toast.LENGTH_LONG).show()
                return@chargerContact
            }
            if (lat !in -90.0..90.0 || lng !in -180.0..180.0) {
                Toast.makeText(requireContext(), "Coordonnées invalides", Toast.LENGTH_LONG).show()
                return@chargerContact
            }

            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, CarteContactFragment.newInstance(contactId))
                .addToBackStack(null)
                .commit()
        }
    }

    // ---------- Enregistrer / Supprimer ----------

    private fun enregistrer() {
        val nom = editNom.text.toString().trim()
        val prenom = editPrenom.text.toString().trim()
        val tel = editTelephone.text.toString().trim()
        val email = editEmail.text.toString().trim().ifEmpty { null }
        val latTxt = editLatitude.text.toString().trim().replace(',', '.')
        val lngTxt = editLongitude.text.toString().trim().replace(',', '.')

        if (nom.isEmpty()) { editNom.error = "Le nom est obligatoire"; return }
        if (prenom.isEmpty()) { editPrenom.error = "Le prénom est obligatoire"; return }
        if (tel.isEmpty()) { editTelephone.error = "Le téléphone est obligatoire"; return }

        var lat: Double? = null
        var lng: Double? = null
        if (latTxt.isNotEmpty() || lngTxt.isNotEmpty()) {
            lat = latTxt.toDoubleOrNull()
            lng = lngTxt.toDoubleOrNull()
            if (lat == null) { editLatitude.error = "Latitude invalide (nombre attendu)"; return }
            if (lng == null) { editLongitude.error = "Longitude invalide (nombre attendu)"; return }
            if (lat < -90.0 || lat > 90.0) { editLatitude.error = "Latitude entre -90 et 90"; return }
            if (lng < -180.0 || lng > 180.0) { editLongitude.error = "Longitude entre -180 et 180"; return }
        }

        val contact = Contact(
            id = contactId,
            nom = nom,
            prenom = prenom,
            telephone = tel,
            email = email,
            photoUri = photoPath,
            latitude = lat,
            longitude = lng
        )

        viewModel.enregistrer(contact) { ok ->
            if (!isAdded) return@enregistrer
            if (ok) {
                parentFragmentManager.popBackStack()
            } else {
                Toast.makeText(requireContext(), "Échec de l'enregistrement", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun confirmerSuppression() {
        AlertDialog.Builder(requireContext())
            .setTitle("Supprimer le contact")
            .setMessage("Voulez-vous vraiment supprimer ce contact ?")
            .setPositiveButton("Supprimer") { _, _ ->
                viewModel.supprimer(contactId) { ok ->
                    if (!isAdded) return@supprimer
                    if (ok) {
                        parentFragmentManager.popBackStack()
                    } else {
                        Toast.makeText(requireContext(), "Suppression impossible : contact introuvable", Toast.LENGTH_LONG).show()
                    }
                }
            }
            .setNegativeButton("Annuler", null)
            .show()
    }

    companion object {
        private const val ARG_ID = "contact_id"
        private const val KEY_PHOTO = "photo_path"
        private const val KEY_CAMERA = "camera_path"

        fun newInstance(id: Long = 0L) = DetailContactFragment().apply {
            arguments = Bundle().apply { putLong(ARG_ID, id) }
        }
    }
}