package com.rvu.campuscompanion.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.rvu.campuscompanion.R
import com.rvu.campuscompanion.RVUApplication
import com.rvu.campuscompanion.adapter.TimetableAdapter
import com.rvu.campuscompanion.databinding.FragmentDashboardBinding
import com.rvu.campuscompanion.utils.toAttendanceColor
import com.rvu.campuscompanion.viewmodel.DashboardViewModel
import com.rvu.campuscompanion.viewmodel.ViewModelFactory

class DashboardFragment : Fragment() {
    private var _b: FragmentDashboardBinding? = null
    private val b get() = _b!!

    private val vm: DashboardViewModel by viewModels {
        val app = requireActivity().application as RVUApplication
        ViewModelFactory(app.timetableRepository, app.attendanceRepository)
    }

    private lateinit var todayAdapter: TimetableAdapter

    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?): View {
        _b = FragmentDashboardBinding.inflate(i, c, false); return b.root
    }

    override fun onViewCreated(view: View, s: Bundle?) {
        todayAdapter = TimetableAdapter()
        b.rvTodayClasses.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = todayAdapter
            isNestedScrollingEnabled = false
        }

        configureQuickAction(b.cardTimetable.root, R.drawable.ic_schedule, "Timetable") {
            findNavController().navigate(R.id.action_dashboard_to_timetable)
        }
        configureQuickAction(b.cardAttendance.root, R.drawable.ic_attendance, "Attendance") {
            findNavController().navigate(R.id.action_dashboard_to_attendance)
        }
        configureQuickAction(b.cardMap.root, R.drawable.ic_map, "Map") {
            findNavController().navigate(R.id.mapFragment)
        }
        configureQuickAction(b.cardEvents.root, R.drawable.ic_event, "Events") {
            findNavController().navigate(R.id.eventsFragment)
        }
        configureQuickAction(b.cardLostfound.root, R.drawable.ic_lostfound, "Lost & Found") {
            findNavController().navigate(R.id.action_dashboard_to_lostfound)
        }
        configureQuickAction(b.cardCanteen.root, R.drawable.ic_food, "Canteen") {
            findNavController().navigate(R.id.action_dashboard_to_canteen)
        }

        b.fabAssistant.setOnClickListener {
            findNavController().navigate(R.id.action_dashboard_to_assistant)
        }

        vm.loadUser()
        vm.user.observe(viewLifecycleOwner) { u ->
            b.tvName.text = u?.name ?: "Student"
            b.tvBranch.text = if (u != null) "${u.branch} • Sem ${u.semester}" else ""
            if (!u?.photoUrl.isNullOrEmpty()) {
                Glide.with(this).load(u!!.photoUrl).circleCrop().placeholder(R.drawable.ic_default_avatar).into(b.ivAvatar)
            }
        }

        vm.todayClasses.observe(viewLifecycleOwner) { list ->
            todayAdapter.submitList(list.take(2))
            b.tvEmptyToday.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
        }

        vm.attendanceSummary.observe(viewLifecycleOwner) { summary ->
            val pct = vm.overallAttendance(summary)
            b.tvAttendancePct.text = "%.1f%%".format(pct)
            b.tvAttendancePct.setTextColor(pct.toAttendanceColor(requireContext()))
            renderDonut(pct)
        }

        vm.quote.observe(viewLifecycleOwner) { q ->
            b.tvQuote.text = "“${q.text}”"
            b.tvQuoteAuthor.text = "— ${q.author}"
        }

        vm.upcomingEvents.observe(viewLifecycleOwner) { events ->
            b.tvUpcomingEventTitle.text = events.firstOrNull()?.title ?: "No upcoming events"
            b.tvUpcomingEventVenue.text = events.firstOrNull()?.venue ?: ""
        }

        vm.announcements.observe(viewLifecycleOwner) { ann ->
            b.tvAnnouncement.text = ann.firstOrNull()?.title ?: "No announcements yet"
        }
    }

    private fun renderDonut(percentage: Float) {
        val entries = listOf(
            PieEntry(percentage, "Present"),
            PieEntry((100f - percentage).coerceAtLeast(0f), "Absent")
        )
        val dataSet = PieDataSet(entries, "").apply {
            colors = listOf(
                requireContext().getColor(R.color.chart_1),
                requireContext().getColor(R.color.outline)
            )
            sliceSpace = 2f; setDrawValues(false)
        }
        b.donutChart.apply {
            data = PieData(dataSet)
            description.isEnabled = false
            legend.isEnabled = false
            isDrawHoleEnabled = true
            holeRadius = 70f
            setHoleColor(android.graphics.Color.TRANSPARENT)
            setUsePercentValues(false)
            invalidate()
        }
    }

    private fun configureQuickAction(root: View, iconRes: Int, label: String, onClick: () -> Unit) {
        root.findViewById<ImageView>(R.id.ivQuickIcon).setImageResource(iconRes)
        root.findViewById<TextView>(R.id.tvQuickLabel).text = label
        root.setOnClickListener { onClick() }
    }

    override fun onDestroyView() { super.onDestroyView(); _b = null }
}
