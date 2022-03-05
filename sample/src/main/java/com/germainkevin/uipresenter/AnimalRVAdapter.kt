package com.germainkevin.uipresenter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.germainkevin.uipresenter.databinding.AnimalRowBinding

/**
 * The [RecyclerView.Adapter] for the [MainFragment]'s [RecyclerView]
 * */
class AnimalRVAdapter : RecyclerView.Adapter<AnimalRVAdapter.AnimalViewHolder>() {

    private var animalsList: List<String> = emptyList()

    fun submitList(list: List<String>) {
        animalsList = list
    }

    class AnimalViewHolder(private val itemBinding: AnimalRowBinding) :
        RecyclerView.ViewHolder(itemBinding.root) {
        fun bind(animalName: String) {
            itemBinding.animalName.text = animalName
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AnimalViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = AnimalRowBinding.inflate(inflater, parent, false)
        return AnimalViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AnimalViewHolder, position: Int) {
        val animalName = animalsList[position]
        holder.bind(animalName)
    }

    override fun getItemCount(): Int = animalsList.size

}