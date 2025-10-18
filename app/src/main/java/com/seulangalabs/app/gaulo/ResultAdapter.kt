package com.seulangalabs.app.gaulo

import android.animation.ObjectAnimator
import android.content.Context
import android.util.SparseBooleanArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import android.view.animation.AccelerateDecelerateInterpolator

class ResultAdapter(private val context: Context, private var slangWords: List<String>) :
    RecyclerView.Adapter<ResultAdapter.ViewHolder>() {

    private val slangMap: Map<String, String> by lazy {
        val slangArray = context.resources.getStringArray(R.array.slang_words)
        val explanationArray = context.resources.getStringArray(R.array.slang_explanations)

        slangArray.mapIndexed { index, slang ->
            slang.lowercase() to explanationArray[index]
        }.toMap()
    }

    private val expandedState = SparseBooleanArray()

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val slangText: TextView = itemView.findViewById(R.id.slang_text)
        val dropdownIcon: ImageView = itemView.findViewById(R.id.dropdown_icon)
        val explanationText: TextView = itemView.findViewById(R.id.explanation_text)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_slang, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val slangWord = slangWords[position]
        holder.slangText.text = slangWord

        val explanation = slangMap[slangWord.lowercase()] ?: "Penjelasan tidak tersedia"
        holder.explanationText.text = explanation

        val isExpanded = expandedState.get(position, false)

        // Animasi untuk mengubah visibilitas dan ukuran penjelasan
        if (isExpanded) {
            holder.explanationText.visibility = View.VISIBLE
            holder.explanationText.alpha = 0f // Mulai dari transparan
            holder.explanationText.animate().alpha(1f).setDuration(300).start()
        } else {
            holder.explanationText.animate().alpha(0f).setDuration(300).withEndAction {
                holder.explanationText.visibility = View.GONE
            }.start()
        }

        // Animasi rotasi ikon panah
        val rotationAngle = if (isExpanded) 180f else 0f
        val rotationAnimator = ObjectAnimator.ofFloat(holder.dropdownIcon, "rotation", rotationAngle)
        rotationAnimator.duration = 300
        rotationAnimator.interpolator = AccelerateDecelerateInterpolator()
        rotationAnimator.start()

        // Klik untuk toggle expand/collapse
        val clickListener = View.OnClickListener {
            toggleExpansion(position)
        }

        holder.slangText.setOnClickListener(clickListener)
        holder.dropdownIcon.setOnClickListener(clickListener)
    }

    override fun getItemCount(): Int = slangWords.size

    // Toggle fungsi untuk expand/collapse
    private fun toggleExpansion(position: Int) {
        val newState = !expandedState.get(position, false)
        expandedState.put(position, newState)
        notifyItemChanged(position) // Update hanya item yang berubah
    }

    fun updateData(newWords: List<String>) {
        slangWords = newWords
        expandedState.clear()
        notifyDataSetChanged() // Gunakan hanya jika seluruh data perlu diperbarui
    }
}
