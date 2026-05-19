package com.rvu.campuscompanion.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.rvu.campuscompanion.R
import com.rvu.campuscompanion.data.model.LostFoundItem
import com.rvu.campuscompanion.databinding.ItemLostfoundBinding
import com.rvu.campuscompanion.utils.toRelativeTime

class LostFoundAdapter(
    private val onClick: (LostFoundItem) -> Unit
) : ListAdapter<LostFoundItem, LostFoundAdapter.VH>(Diff) {

    inner class VH(val b: ItemLostfoundBinding) : RecyclerView.ViewHolder(b.root) {
        fun bind(item: LostFoundItem) {
            b.tvName.text = item.name
            b.tvDescription.text = item.description
            b.tvLocation.text = item.location
            b.tvDate.text = item.timestamp.toRelativeTime()
            b.tvStatus.text = item.status
            b.tvStatus.setBackgroundResource(
                if (item.status == LostFoundItem.STATUS_RESOLVED) R.drawable.bg_badge_tutorial
                else R.drawable.bg_badge_lecture
            )
            if (item.imageUrl.isNotEmpty()) {
                Glide.with(b.ivImage).load(item.imageUrl)
                    .placeholder(R.drawable.ic_image_placeholder).into(b.ivImage)
            } else b.ivImage.setImageResource(R.drawable.ic_image_placeholder)
            b.root.setOnClickListener { onClick(item) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, vt: Int) =
        VH(ItemLostfoundBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: VH, position: Int) = holder.bind(getItem(position))

    object Diff : DiffUtil.ItemCallback<LostFoundItem>() {
        override fun areItemsTheSame(a: LostFoundItem, b: LostFoundItem) = a.id == b.id
        override fun areContentsTheSame(a: LostFoundItem, b: LostFoundItem) = a == b
    }
}
