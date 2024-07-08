package com.example.whisper.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Filter
import android.widget.TextView
import com.example.whisper.R

class SearchAdapter(context: Context, suggestions: MutableList<String>) :
    ArrayAdapter<String>(context, R.layout.dropdown_item, suggestions) {

    private val suggestions: MutableList<String> = suggestions
    private var filteredSuggestions: List<String> = suggestions

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.dropdown_item, parent, false)
        val suggestionText = view.findViewById<TextView>(R.id.dropdownItemText)
        suggestionText.text = getItem(position)
        return view
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val filterResults = FilterResults()
                if (constraint != null && constraint.isNotEmpty()) {
                    val filteredList = suggestions.filter {
                        it.contains(constraint, ignoreCase = true)
                    }
                    filterResults.values = filteredList
                    filterResults.count = filteredList.size
                } else {
                    filterResults.values = suggestions
                    filterResults.count = suggestions.size
                }
                return filterResults
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                if (results != null && results.values is List<*>) {
                    filteredSuggestions = results.values as List<String>
                    notifyDataSetChanged()
                }
            }
        }
    }

    override fun getItem(position: Int): String? {
        return filteredSuggestions[position]
    }

    override fun getCount(): Int {
        return filteredSuggestions.size
    }

    fun setSuggestions(newSuggestions: List<String>) {
        suggestions.clear()
        suggestions.addAll(newSuggestions)
        notifyDataSetChanged()
    }
}
