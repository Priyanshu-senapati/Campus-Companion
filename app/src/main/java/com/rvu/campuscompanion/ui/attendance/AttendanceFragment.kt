package com.rvu.campuscompanion.ui.attendance

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.rvu.campuscompanion.R
import com.rvu.campuscompanion.RVUApplication
import com.rvu.campuscompanion.adapter.AttendanceSummaryAdapter
import com.rvu.campuscompanion.data.local.AttendanceEntity
import com.rvu.campuscompanion.databinding.FragmentAttendanceBinding
import com.rvu.campuscompanion.utils.toAttendanceColor
import com.rvu.campuscompanion.utils.toast
import com.rvu.campuscompanion.viewmodel.AttendanceViewModel
import com.rvu.campuscompanion.viewmodel.ViewModelFactory

class AttendanceFragment : Fragment() {
    private var _b: FragmentAttendanceBinding? = null
    private val b get() = _b!!
    private val vm: AttendanceViewModel by viewModels {
        val app = requireActivity().application as RVUApplication
        ViewModelFactory(attendanceRepo = app.attendanceRepository)
    }

    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?): View {
        _b = FragmentAttendanceBinding.inflate(i, c, false); return b.root
    }

    override fun onViewCreated(view: View, s: Bundle?) {
        b.toolbar.setNavigationOnClickListener { requireActivity().onBackPressedDispatcher.onBackPressed() }

        val adapter = AttendanceSummaryAdapter(
            onClick = { subj ->
                val args = Bundle().apply { putString("subject", subj.subject) }
                findNavController().navigate(R.id.action_attendance_to_detail, args)
            },
            onMarkPresent = { subj ->
                vm.mark(AttendanceEntity(0, subj.subject, System.currentTimeMillis(), AttendanceEntity.PRESENT, 5))
                context?.toast("Marked present for ${subj.subject}")
            }
        )
        b.rv.layoutManager = LinearLayoutManager(requireContext())
        b.rv.adapter = adapter

        b.fabAdd.setOnClickListener {
            MarkAttendanceDialog { entry -> vm.mark(entry) }
                .show(childFragmentManager, "mark")
        }

        vm.summary.observe(viewLifecycleOwner) { list ->
            adapter.submitList(list)
            b.empty.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
            val pct = vm.overallPercentage(list)
            b.tvOverall.text = "%.1f%%".format(pct)
            b.tvOverall.setTextColor(pct.toAttendanceColor(requireContext()))
        }
    }

    override fun onDestroyView() { super.onDestroyView(); _b = null }
}
