package com.rvu.campuscompanion.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.rvu.campuscompanion.R
import com.rvu.campuscompanion.data.local.MenuItemEntity
import com.rvu.campuscompanion.databinding.ItemMenuBinding

class MenuItemAdapter(
    private val onClick: (MenuItemEntity) -> Unit
) : ListAdapter<MenuItemEntity, MenuItemAdapter.VH>(Diff) {

    inner class VH(
        val b: ItemMenuBinding
    ) : RecyclerView.ViewHolder(b.root) {

        fun bind(item: MenuItemEntity) {

            b.tvName.text = item.name
            b.tvPrice.text = "₹${item.price}"
            b.tvCategory.text = item.category

            b.dotVeg.setBackgroundResource(
                if (item.isVeg)
                    R.drawable.dot_veg
                else
                    R.drawable.dot_nonveg
            )

            b.tvAvailable.text =
                if (item.available)
                    "Available"
                else
                    "Sold out"

            b.root.alpha =
                if (item.available)
                    1f
                else
                    0.5f

            b.root.setOnClickListener {
                onClick(item)
            }
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): VH {

        val binding = ItemMenuBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )

        return VH(binding)
    }

    override fun onBindViewHolder(
        holder: VH,
        position: Int
    ) {

        holder.bind(getItem(position))
    }

    object Diff : DiffUtil.ItemCallback<MenuItemEntity>() {

        override fun areItemsTheSame(
            oldItem: MenuItemEntity,
            newItem: MenuItemEntity
        ): Boolean {

            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(
            oldItem: MenuItemEntity,
            newItem: MenuItemEntity
        ): Boolean {

            return oldItem == newItem
        }
    }
}