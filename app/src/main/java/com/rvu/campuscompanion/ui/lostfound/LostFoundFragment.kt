package com.rvu.campuscompanion.ui.lostfound

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
import com.rvu.campuscompanion.adapter.LostFoundAdapter
import com.rvu.campuscompanion.data.model.LostFoundItem
import com.rvu.campuscompanion.databinding.FragmentLostfoundBinding
import com.rvu.campuscompanion.viewmodel.LostFoundViewModel

class LostFoundFragment : Fragment() {
    private var _b: FragmentLostfoundBinding? = null
    private val b get() = _b!!
    private val vm: LostFoundViewModel by viewModels()

    private val adapter = LostFoundAdapter { item ->
        val args = Bundle().apply { putString("itemId", item.id) }
        findNavController().navigate(R.id.action_lostfound_to_detail, args)
    }

    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?): View {
        _b = FragmentLostfoundBinding.inflate(i, c, false); return b.root
    }

    override fun onViewCreated(view: View, s: Bundle?) {
        b.toolbar.setNavigationOnClickListener { requireActivity().onBackPressedDispatcher.onBackPressed() }
        b.tabs.addTab(b.tabs.newTab().setText("Lost Items"))
        b.tabs.addTab(b.tabs.newTab().setText("Found Items"))
        b.rv.layoutManager = LinearLayoutManager(requireContext())
        b.rv.adapter = adapter

        b.tabs.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                vm.setType(if (tab?.position == 0) LostFoundItem.TYPE_LOST else LostFoundItem.TYPE_FOUND)
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })

        b.fabAdd.setOnClickListener {
            PostItemSheet(vm.currentType()) { item, uri -> vm.post(item, uri) }
                .show(childFragmentManager, "post")
        }

        vm.items.observe(viewLifecycleOwner) { list ->
            adapter.submitList(list)
            b.empty.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
        }
        vm.postResult.observe(viewLifecycleOwner) { res ->
            res.onFailure { e ->
                view.findViewById<View>(android.R.id.content)
                com.google.android.material.snackbar.Snackbar.make(b.root,
                    e.message ?: "Failed", com.google.android.material.snackbar.Snackbar.LENGTH_LONG).show()
            }
        }
    }

    override fun onDestroyView() { super.onDestroyView(); _b = null }
}
