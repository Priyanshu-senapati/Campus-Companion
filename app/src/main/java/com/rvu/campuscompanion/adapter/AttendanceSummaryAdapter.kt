package com.rvu.campuscompanion.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.rvu.campuscompanion.data.local.SubjectAttendance
import com.rvu.campuscompanion.databinding.ItemAttendanceSummaryBinding
import com.rvu.campuscompanion.utils.toAttendanceColor

class AttendanceSummaryAdapter(
    private val onClick: (SubjectAttendance) -> Unit,
    private val onMarkPresent: (SubjectAttendance) -> Unit
) : ListAdapter<SubjectAttendance, AttendanceSummaryAdapter.VH>(Diff) {

    inner class VH(val b: ItemAttendanceSummaryBinding) : RecyclerView.ViewHolder(b.root) {
        fun bind(s: SubjectAttendance) {
            b.tvSubject.text = s.subject
            b.tvCount.text = "${s.present} / ${s.total} classes"
            b.tvPct.text = "%.0f%%".format(s.percentage)
            val color = s.percentage.toAttendanceColor(b.root.context)
            b.tvPct.setTextColor(color)
            b.progress.progress = s.percentage.toInt()
            b.progress.progressTintList = android.content.res.ColorStateList.valueOf(color)
            b.root.setOnClickListener { onClick(s) }
            b.btnMark.setOnClickListener { onMarkPresent(s) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, vt: Int) =
        VH(ItemAttendanceSummaryBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: VH, position: Int) = holder.bind(getItem(position))

    object Diff : DiffUtil.ItemCallback<SubjectAttendance>() {
        override fun areItemsTheSame(a: SubjectAttendance, b: SubjectAttendance) = a.subject == b.subject
        override fun areContentsTheSame(a: SubjectAttendance, b: SubjectAttendance) = a == b
    }
}
