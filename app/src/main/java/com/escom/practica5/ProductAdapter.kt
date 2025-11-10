package com.escom.practica5

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.escom.practica5.databinding.ItemProductoBinding

class ProductAdapter(private val onItemClick: (Producto) -> Unit) :
    ListAdapter<Producto, ProductAdapter.ProductViewHolder>(ProductoDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val binding = ItemProductoBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ProductViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val producto = getItem(position)
        holder.bind(producto)
    }

    fun updateList(newList: List<Producto>) {
        submitList(newList)
    }

    inner class ProductViewHolder(private val binding: ItemProductoBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(producto: Producto) {
            with(binding) {
                tvNombre.text = producto.nombre
                tvPrecio.text = "$${producto.precio}"
                tvDescripcion.text = producto.descripcion
                tvCategoria.text = producto.categoria

                if (producto.enStock) {
                    tvStock.text = "✅ En stock"
                    tvStock.setTextColor(android.graphics.Color.parseColor("#388E3C"))
                } else {
                    tvStock.text = "❌ Sin stock"
                    tvStock.setTextColor(android.graphics.Color.parseColor("#D32F2F"))
                }

                root.setOnClickListener {
                    onItemClick(producto)
                }
            }
        }
    }

    class ProductoDiffCallback : DiffUtil.ItemCallback<Producto>() {
        override fun areItemsTheSame(oldItem: Producto, newItem: Producto): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Producto, newItem: Producto): Boolean {
            return oldItem == newItem
        }
    }
}