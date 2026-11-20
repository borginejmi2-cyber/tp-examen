package com.example.tp_examen.ui

import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import com.example.tp_examen.R

class ParametresFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)
    }
}