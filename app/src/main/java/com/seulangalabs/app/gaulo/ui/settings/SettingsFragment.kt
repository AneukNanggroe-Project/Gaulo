package com.seulangalabs.app.gaulo.ui.settings

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import com.seulangalabs.app.gaulo.R
import com.seulangalabs.app.gaulo.databinding.FragmentSettingsBinding


class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        val root: View = binding.root

        updateCurrentThemeText()

        binding.themeSetting.setOnClickListener {
            showThemeDialog(requireContext())
        }

        return root
    }

    private fun updateCurrentThemeText() {
        val sharedPreferences = requireContext().getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val currentTheme = sharedPreferences.getInt("selected_theme", AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)

        val themeText = when (currentTheme) {
            AppCompatDelegate.MODE_NIGHT_NO -> getString(R.string.theme_light)
            AppCompatDelegate.MODE_NIGHT_YES -> getString(R.string.theme_dark)
            else -> getString(R.string.theme_auto)
        }

        binding.currentTheme.text = themeText
    }

    private fun showThemeDialog(context: Context) {
        val options = resources.getStringArray(R.array.theme_options)

        val sharedPreferences = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val currentTheme = sharedPreferences.getInt("selected_theme", AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)

        val selectedIndex = when (currentTheme) {
            AppCompatDelegate.MODE_NIGHT_NO -> 1
            AppCompatDelegate.MODE_NIGHT_YES -> 2
            else -> 0
        }

        AlertDialog.Builder(context)
            .setTitle(getString(R.string.theme_title))
            .setSingleChoiceItems(options, selectedIndex) { dialog, which ->
                val newTheme = when (which) {
                    1 -> AppCompatDelegate.MODE_NIGHT_NO
                    2 -> AppCompatDelegate.MODE_NIGHT_YES
                    else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
                }

                with(sharedPreferences.edit()) {
                    putInt("selected_theme", newTheme)
                    apply()
                }
                AppCompatDelegate.setDefaultNightMode(newTheme)
                updateCurrentThemeText()
                dialog.dismiss()
            }
            .setNegativeButton(getString(R.string.theme_cancel), null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

