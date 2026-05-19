package com.rvu.campuscompanion.ui.timetable

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.viewModels
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.tabs.TabLayoutMediator
import com.rvu.campuscompanion.RVUApplication
import com.rvu.campuscompanion.data.local.TimetableEntry
import com.rvu.campuscompanion.databinding.FragmentTimetableBinding
import com.rvu.campuscompanion.viewmodel.TimetableViewModel
import com.rvu.campuscompanion.viewmodel.ViewModelFactory

class TimetableFragment : Fragment() {
    private var _b: FragmentTimetableBinding? = null
    private val b get() = _b!!
    private val vm: TimetableViewModel by viewModels {
        val app = requireActivity().application as RVUApplication
        ViewModelFactory(timetableRepo = app.timetableRepository)
    }

    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?): View {
        _b = FragmentTimetableBinding.inflate(i, c, false); return b.root
    }

    override fun onViewCreated(view: View, s: Bundle?) {
        b.toolbar.setNavigationOnClickListener { requireActivity().onBackPressedDispatcher.onBackPressed() }

        b.viewpager.adapter = DayPagerAdapter(childFragmentManager, viewLifecycleOwner.lifecycle.let { lifecycle })
        TabLayoutMediator(b.tabs, b.viewpager) { tab, pos ->
            tab.text = TimetableEntry.DAYS[pos].take(3)
        }.attach()

        b.fabAdd.setOnClickListener {
            AddClassBottomSheet { entry -> vm.add(entry) }
                .show(childFragmentManager, "add_class")
        }
    }

    private class DayPagerAdapter(
        fm: FragmentManager,
        lifecycle: androidx.lifecycle.Lifecycle
    ) : FragmentStateAdapter(fm, lifecycle) {
        override fun getItemCount() = TimetableEntry.DAYS.size
        override fun createFragment(position: Int): Fragment =
            DayPageFragment.newInstance(TimetableEntry.DAYS[position])
    }

    override fun onDestroyView() { super.onDestroyView(); _b = null }
}
