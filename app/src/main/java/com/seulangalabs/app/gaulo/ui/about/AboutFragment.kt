package com.seulangalabs.app.gaulo.ui.about

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.seulangalabs.app.gaulo.databinding.FragmentAboutBinding

class AboutFragment : Fragment() {

    private var _binding: FragmentAboutBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentAboutBinding.inflate(inflater, container, false)
        val root: View = binding.root

        binding.emailText.setOnClickListener {
            sendEmail()
        }

        return root
    }

    private fun sendEmail() {
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:seulangalabs@gmail.com")
            putExtra(Intent.EXTRA_SUBJECT, "Halo Seulanga Labs!")
        }
        startActivity(Intent.createChooser(intent, "Send by email"))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
