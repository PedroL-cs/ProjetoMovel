package com.example.estacaocultural

import android.graphics.BitmapFactory
import android.util.Base64
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.estacaocultural.databinding.ItemPaintingBinding

class PaintingAdapter(
    private var paintings: List<Painting>,
    private val onClick: (Painting) -> Unit
) : RecyclerView.Adapter<PaintingAdapter.PaintingViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PaintingViewHolder {
        val binding = ItemPaintingBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PaintingViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PaintingViewHolder, position: Int) {
        val painting = paintings[position]
        holder.bind(painting, onClick)
    }

    override fun getItemCount() = paintings.size

    fun updateList(newPaintings: List<Painting>) {
        paintings = newPaintings
        notifyDataSetChanged()
    }

    class PaintingViewHolder(private val binding: ItemPaintingBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(painting: Painting, onClick: (Painting) -> Unit) {
            val imageBytes = Base64.decode(painting.imagem, Base64.DEFAULT)
            val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
            binding.paintingImage.setImageBitmap(bitmap)
            binding.paintingTitle.text = painting.nomeObra
            binding.paintingAuthor.text = painting.autor
            binding.root.setOnClickListener { onClick(painting) }
        }
    }
}
