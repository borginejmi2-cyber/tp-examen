package com.example.tp_examen.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.preference.PreferenceManager
import com.example.tp_examen.R
import com.example.tp_examen.viewmodel.ContactViewModel
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.XYTileSource
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import java.io.File

class CarteContactFragment : Fragment() {

    private val viewModel: ContactViewModel by activityViewModels()
    private var mapView: MapView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val ctx = requireContext().applicationContext

        Configuration.getInstance().apply {
            userAgentValue = "Mozilla/5.0 (Android; Mobile; rv:120.0) Gecko/120.0 Firefox/120.0"
            load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx))

            // Réinitialiser complètement le cache osmdroid sur le stockage interne
            val osmdroidDir = File(ctx.filesDir, "osmdroid")
            osmdroidDir.deleteRecursively()
            osmdroidBasePath = osmdroidDir
            osmdroidTileCache = File(osmdroidDir, "tiles_fr")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_carte_contact, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val map = view.findViewById<MapView>(R.id.mapView)
        mapView = map
        val txtMessage = view.findViewById<TextView>(R.id.txtMessageCarte)

        fun afficherMessage(texte: String) {
            map.visibility = View.GONE
            txtMessage.text = texte
            txtMessage.visibility = View.VISIBLE
        }

        val id = requireArguments().getLong(ARG_ID, 0L)
        viewModel.chargerContact(id) { c ->
            if (!isAdded) return@chargerContact
            if (c == null) {
                afficherMessage("Contact introuvable.")
                return@chargerContact
            }
            val lat = c.latitude
            val lng = c.longitude
            if (lat == null || lng == null) {
                afficherMessage("Aucune position enregistrée pour ${c.prenom} ${c.nom}.")
                return@chargerContact
            }
            if (lat !in -90.0..90.0 || lng !in -180.0..180.0) {
                afficherMessage("Les coordonnées de ce contact sont invalides.")
                return@chargerContact
            }

            map.visibility = View.VISIBLE
            txtMessage.visibility = View.GONE

            val position = GeoPoint(lat, lng)

            // Serveur OpenStreetMap France : HTTPS, gratuit, sans blocage 403, sans clé API
            val tileSourceOSMFr = XYTileSource(
                "OSMFrance",
                0, 19, 256, ".png",
                arrayOf(
                    "https://a.tile.openstreetmap.fr/osmfr/",
                    "https://b.tile.openstreetmap.fr/osmfr/",
                    "https://c.tile.openstreetmap.fr/osmfr/"
                ),
                "© OpenStreetMap contributors"
            )

            map.setTileSource(tileSourceOSMFr)
            map.setMultiTouchControls(true)
            map.controller.setZoom(15.0)
            map.controller.setCenter(position)

            val marqueur = Marker(map).apply {
                this.position = position
                title = "${c.prenom} ${c.nom}"
                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            }
            map.overlays.clear()
            map.overlays.add(marqueur)
            map.invalidate()
        }
    }

    override fun onResume() {
        super.onResume()
        mapView?.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView?.onPause()
    }

    override fun onDestroyView() {
        mapView?.onDetach()
        mapView = null
        super.onDestroyView()
    }

    companion object {
        private const val ARG_ID = "contact_id"

        fun newInstance(id: Long) = CarteContactFragment().apply {
            arguments = Bundle().apply { putLong(ARG_ID, id) }
        }
    }
}
