package com.rvu.campuscompanion.ui.attendance

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.rvu.campuscompanion.RVUApplication
import com.rvu.campuscompanion.databinding.FragmentAttendanceDetailBinding
import com.rvu.campuscompanion.utils.toAttendanceColor
import com.rvu.campuscompanion.utils.toFormattedDate
import com.rvu.campuscompanion.viewmodel.AttendanceViewModel
import com.rvu.campuscompanion.viewmodel.ViewModelFactory

class AttendanceDetailFragment : Fragment() {
    private var _b: FragmentAttendanceDetailBinding? = null
    private val b get() = _b!!
    private val vm: AttendanceViewModel by viewModels {
        val app = requireActivity().application as RVUApplication
        ViewModelFactory(attendanceRepo = app.attendanceRepository)
    }

    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?): View {
        _b = FragmentAttendanceDetailBinding.inflate(i, c, false); return b.root
    }

    override fun onViewCreated(view: View, s: Bundle?) {
        val subject = requireArguments().getString("subject") ?: return
        b.toolbar.title = subject
        b.toolbar.setNavigationOnClickListener { requireActivity().onBackPressedDispatcher.onBackPressed() }

        vm.selectSubject(subject)
        vm.recordsForSubject.observe(viewLifecycleOwner) { records ->
            val total = records.size
            val present = records.count { it.status == "PRESENT" }
            val pct = if (total == 0) 0f else (present.toFloat() / total) * 100f
            b.tvCount.text = "$present / $total classes attended"
            b.tvPct.text = "%.1f%%".format(pct)
            b.tvPct.setTextColor(pct.toAttendanceColor(requireContext()))

            val needed = vm.classesNeededFor(75, present, total)
            b.tvProjection.text = if (pct >= 75) "On track — keep it up"
                else "Attend next $needed classes to reach 75%"

            b.tvLog.text = records.take(15).joinToString("\n") {
                "${it.date.toFormattedDate()} — ${it.status}"
            }.ifEmpty { "No records yet" }
        }
    }

    override fun onDestroyView() { super.onDestroyView(); _b = null }
}
