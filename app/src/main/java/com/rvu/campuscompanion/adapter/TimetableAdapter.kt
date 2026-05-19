package com.rvu.campuscompanion.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.rvu.campuscompanion.R
import com.rvu.campuscompanion.data.local.TimetableEntry
import com.rvu.campuscompanion.databinding.ItemClassBinding

class TimetableAdapter(
    private val onClick: (TimetableEntry) -> Unit = {}
) : ListAdapter<TimetableEntry, TimetableAdapter.VH>(Diff) {

    inner class VH(val b: ItemClassBinding) : RecyclerView.ViewHolder(b.root) {
        fun bind(e: TimetableEntry) {
            b.tvSubject.text = e.subject
            b.tvProfessor.text = e.professor
            b.tvRoom.text = e.room
            b.tvTime.text = "${e.startTime} – ${e.endTime}"
            b.tvType.text = e.type
            b.tvType.setBackgroundResource(
                when (e.type) {
                    TimetableEntry.TYPE_LAB -> R.drawable.bg_badge_lab
                    TimetableEntry.TYPE_TUTORIAL -> R.drawable.bg_badge_tutorial
                    else -> R.drawable.bg_badge_lecture
                }
            )
            b.root.setOnClickListener { onClick(e) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, vt: Int): VH {
        val b = ItemClassBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(b)
    }

    override fun onBindViewHolder(holder: VH, position: Int) = holder.bind(getItem(position))

    object Diff : DiffUtil.ItemCallback<TimetableEntry>() {
        override fun areItemsTheSame(a: TimetableEntry, b: TimetableEntry) = a.id == b.id
        override fun areContentsTheSame(a: TimetableEntry, b: TimetableEntry) = a == b
    }
}
