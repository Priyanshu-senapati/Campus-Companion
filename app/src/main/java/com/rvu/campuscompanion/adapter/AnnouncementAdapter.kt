package com.rvu.campuscompanion.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.rvu.campuscompanion.data.model.Announcement
import com.rvu.campuscompanion.databinding.ItemAnnouncementBinding
import com.rvu.campuscompanion.utils.toRelativeTime

class AnnouncementAdapter : ListAdapter<Announcement, AnnouncementAdapter.VH>(Diff) {

    inner class VH(val b: ItemAnnouncementBinding) : RecyclerView.ViewHolder(b.root) {
        private var expanded = false
        fun bind(a: Announcement) {
            b.tvTitle.text = a.title
            b.tvCategory.text = a.category
            b.tvAuthor.text = "by ${a.postedBy}"
            b.tvTime.text = a.timestamp.toRelativeTime()
            b.tvDescription.text = a.description
            b.tvDescription.maxLines = if (expanded) Int.MAX_VALUE else 2
            b.root.setOnClickListener {
                expanded = !expanded
                b.tvDescription.maxLines = if (expanded) Int.MAX_VALUE else 2
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, vt: Int) =
        VH(ItemAnnouncementBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: VH, position: Int) = holder.bind(getItem(position))

    object Diff : DiffUtil.ItemCallback<Announcement>() {
        override fun areItemsTheSame(a: Announcement, b: Announcement) = a.id == b.id
        override fun areContentsTheSame(a: Announcement, b: Announcement) = a == b
    }
}
