package com.rvu.campuscompanion.ui.events

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.tabs.TabLayout
import com.rvu.campuscompanion.R
import com.rvu.campuscompanion.adapter.AnnouncementAdapter
import com.rvu.campuscompanion.adapter.EventsAdapter
import com.rvu.campuscompanion.data.remote.FirebaseSource
import com.rvu.campuscompanion.databinding.FragmentEventsBinding
import com.rvu.campuscompanion.viewmodel.EventsViewModel

class EventsFragment : Fragment() {
    private var _b: FragmentEventsBinding? = null
    private val b get() = _b!!
    private val vm: EventsViewModel by viewModels()

    private val eventsAdapter = EventsAdapter { event ->
        val args = Bundle().apply { putString("eventId", event.id) }
        findNavController().navigate(R.id.action_events_to_detail, args)
    }
    private val announcementAdapter = AnnouncementAdapter()

    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?): View {
        _b = FragmentEventsBinding.inflate(i, c, false); return b.root
    }

    override fun onViewCreated(view: View, s: Bundle?) {
        b.rv.layoutManager = LinearLayoutManager(requireContext())
        b.tabs.addTab(b.tabs.newTab().setText("Upcoming"))
        b.tabs.addTab(b.tabs.newTab().setText("Announcements"))
        b.tabs.addTab(b.tabs.newTab().setText("Registered"))

        showEvents(false)

        b.tabs.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (tab?.position) {
                    0 -> showEvents(false)
                    1 -> showAnnouncements()
                    2 -> showEvents(true)
                }
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    private fun showEvents(onlyMine: Boolean) {
        b.rv.adapter = eventsAdapter
        vm.events.observe(viewLifecycleOwner) { all ->
            val list = if (onlyMine) all.filter { it.registeredUsers.contains(FirebaseSource.currentUserId) } else all
            eventsAdapter.submitList(list)
            b.empty.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
        }
    }

    private fun showAnnouncements() {
        b.rv.adapter = announcementAdapter
        vm.announcements.observe(viewLifecycleOwner) { list ->
            announcementAdapter.submitList(list)
            b.empty.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
        }
    }

    override fun onDestroyView() { super.onDestroyView(); _b = null }
}
