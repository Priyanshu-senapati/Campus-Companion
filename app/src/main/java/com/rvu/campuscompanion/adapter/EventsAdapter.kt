package com.rvu.campuscompanion.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.rvu.campuscompanion.R
import com.rvu.campuscompanion.data.model.Event
import com.rvu.campuscompanion.databinding.ItemEventBinding
import com.rvu.campuscompanion.utils.toFormattedDateTime

class EventsAdapter(
    private val onClick: (Event) -> Unit
) : ListAdapter<Event, EventsAdapter.VH>(Diff) {

    inner class VH(val b: ItemEventBinding) : RecyclerView.ViewHolder(b.root) {
        fun bind(e: Event) {
            b.tvTitle.text = e.title
            b.tvVenue.text = e.venue
            b.tvDate.text = e.date.toFormattedDateTime()
            b.tvCategory.text = e.category
            b.tvOrganizer.text = "by ${e.organizer}"
            b.tvAttendees.text = "${e.registeredUsers.size} going"
            if (e.posterUrl.isNotEmpty()) {
                Glide.with(b.ivPoster).load(e.posterUrl)
                    .placeholder(R.drawable.ic_image_placeholder).into(b.ivPoster)
            } else b.ivPoster.setImageResource(R.drawable.ic_image_placeholder)
            b.root.setOnClickListener { onClick(e) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, vt: Int) =
        VH(ItemEventBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: VH, position: Int) = holder.bind(getItem(position))

    object Diff : DiffUtil.ItemCallback<Event>() {
        override fun areItemsTheSame(a: Event, b: Event) = a.id == b.id
        override fun areContentsTheSame(a: Event, b: Event) = a == b
    }
}
