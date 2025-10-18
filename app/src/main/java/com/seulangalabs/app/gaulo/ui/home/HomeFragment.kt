package com.seulangalabs.app.gaulo.ui.home

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.database.FirebaseDatabase
import com.seulangalabs.app.gaulo.R
import com.seulangalabs.app.gaulo.ResultAdapter
import com.seulangalabs.app.gaulo.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var slangWordsList: List<String>
    private lateinit var adapter: ResultAdapter

    // Levenshtein Distance Function
    private fun levenshtein(lhs: CharSequence, rhs: CharSequence): Int {
        val lhsLength = lhs.length
        val rhsLength = rhs.length

        var cost = IntArray(rhsLength + 1) { it }
        var newCost = IntArray(rhsLength + 1)

        for (i in 1..lhsLength) {
            newCost[0] = i
            for (j in 1..rhsLength) {
                val match = if (lhs[i - 1].lowercaseChar() == rhs[j - 1].lowercaseChar()) 0 else 1
                val costReplace = cost[j - 1] + match
                val costInsert = cost[j] + 1
                val costDelete = newCost[j - 1] + 1
                newCost[j] = minOf(costInsert, costDelete, costReplace)
            }
            val swap = cost
            cost = newCost
            newCost = swap
        }
        return cost[rhsLength]
    }

    // Hybrid Exact + Levenshtein Detection
    private fun isSimilar(word: String, slang: String): Boolean {
        // Exact match case-insensitive dulu
        if (word.equals(slang, ignoreCase = true)) return true

        // Kalau slang 2 huruf atau kurang → wajib exact
        if (slang.length <= 2) return false

        // Kalau slang 3 huruf ke atas → boleh beda 1 huruf
        val distance = levenshtein(word, slang)
        return distance <= 1
    }

    private fun sendFeedbackToFirebase(userInput: String) {
        val database = FirebaseDatabase.getInstance()
        val feedbackRef = database.getReference("feedbacks")

        val feedbackData = mapOf(
            "inputText" to userInput,
            "timestamp" to System.currentTimeMillis()
        )

        feedbackRef.push().setValue(feedbackData)
    }

    private fun hideKeyboard() {
        val inputMethodManager = requireContext().getSystemService(android.content.Context.INPUT_METHOD_SERVICE)
                as android.view.inputmethod.InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(binding.editText.windowToken, 0)
    }


    @SuppressLint("ClickableViewAccessibility")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        slangWordsList = resources.getStringArray(R.array.slang_words).toList()

        adapter = ResultAdapter(requireContext(), emptyList())
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter

        binding.wordCountText.text = getString(R.string.word_count_format, 0)
        binding.slangCountText.visibility = View.GONE

        // TextWatcher untuk hitung kata
        binding.editText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val words = s.toString().trim().split("\\s+".toRegex()).filter { it.isNotEmpty() }
                val wordCount = words.size

                if (wordCount > 256) {
                    val limitedText = words.take(256).joinToString(" ")
                    binding.editText.setText(limitedText)
                    binding.editText.setSelection(limitedText.length)
                }

                binding.wordCountText.text = getString(R.string.word_count_format, minOf(wordCount, 256))
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        // Tombol Deteksi
        binding.detectButton.setOnClickListener {
            hideKeyboard()

            val inputText = binding.editText.text.toString()

            sendFeedbackToFirebase(inputText)

            val words = inputText.trim().split("\\s+".toRegex()).filter { it.isNotEmpty() }

            val detectedWords = mutableSetOf<String>()

            for (word in words) {
                for (slang in slangWordsList) {
                    if (isSimilar(word, slang)) {
                        detectedWords.add(slang)
                    }
                }
            }

            adapter.updateData(detectedWords.toList())
            binding.slangCountText.visibility = View.VISIBLE
            binding.slangCountText.text = getString(R.string.slang_count_format, detectedWords.size)
        }

        return root
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
