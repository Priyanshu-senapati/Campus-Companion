package com.rvu.campuscompanion.ui.canteen

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.tabs.TabLayout
import com.rvu.campuscompanion.R
import com.rvu.campuscompanion.RVUApplication
import com.rvu.campuscompanion.adapter.MenuItemAdapter
import com.rvu.campuscompanion.data.local.MenuItemEntity
import com.rvu.campuscompanion.databinding.FragmentCanteenBinding
import com.rvu.campuscompanion.viewmodel.CanteenViewModel
import com.rvu.campuscompanion.viewmodel.ViewModelFactory

class CanteenFragment : Fragment() {
    private var _b: FragmentCanteenBinding? = null
    private val b get() = _b!!
    private val vm: CanteenViewModel by viewModels {
        val app = requireActivity().application as RVUApplication
        ViewModelFactory(canteenRepo = app.canteenRepository)
    }

    private val adapter = MenuItemAdapter { item ->
        val args = Bundle().apply { putLong("itemId", item.id) }
        findNavController().navigate(R.id.action_canteen_to_detail, args)
    }

    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?): View {
        _b = FragmentCanteenBinding.inflate(i, c, false); return b.root
    }

    override fun onViewCreated(view: View, s: Bundle?) {
        b.toolbar.setNavigationOnClickListener { requireActivity().onBackPressedDispatcher.onBackPressed() }
        b.rv.layoutManager = GridLayoutManager(requireContext(), 2)
        b.rv.adapter = adapter

        val categories = listOf("All",
            MenuItemEntity.CAT_BREAKFAST, MenuItemEntity.CAT_LUNCH,
            MenuItemEntity.CAT_SNACKS, MenuItemEntity.CAT_BEVERAGES)
        categories.forEach { b.tabs.addTab(b.tabs.newTab().setText(it)) }

        b.tabs.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                val pos = tab?.position ?: 0
                vm.filterByCategory(if (pos == 0) null else categories[pos])
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })

        vm.filteredItems.observe(viewLifecycleOwner) { list ->
            adapter.submitList(list)
            b.empty.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
        }

        b.fabFeedback.setOnClickListener {
            FeedbackDialog { item, rating, comment ->
                vm.submitFeedback(item, rating, comment)
            }.show(childFragmentManager, "feedback")
        }
    }

    override fun onDestroyView() { super.onDestroyView(); _b = null }
}
