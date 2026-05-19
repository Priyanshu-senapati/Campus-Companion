package com.rvu.campuscompanion.ui.timetable

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.rvu.campuscompanion.RVUApplication
import com.rvu.campuscompanion.adapter.TimetableAdapter
import com.rvu.campuscompanion.databinding.FragmentDayPageBinding
import com.rvu.campuscompanion.viewmodel.TimetableViewModel
import com.rvu.campuscompanion.viewmodel.ViewModelFactory

class DayPageFragment : Fragment() {
    private var _b: FragmentDayPageBinding? = null
    private val b get() = _b!!
    private val vm: TimetableViewModel by viewModels(ownerProducer = { requireParentFragment() }) {
        val app = requireActivity().application as RVUApplication
        ViewModelFactory(timetableRepo = app.timetableRepository)
    }

    private val day: String get() = requireArguments().getString("day", "Monday")

    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?): View {
        _b = FragmentDayPageBinding.inflate(i, c, false); return b.root
    }

    override fun onViewCreated(view: View, s: Bundle?) {
        val adapter = TimetableAdapter()
        b.rv.layoutManager = LinearLayoutManager(requireContext())
        b.rv.adapter = adapter

        val app = requireActivity().application as RVUApplication
        app.timetableRepository.getByDay(day).observe(viewLifecycleOwner, Observer { list ->
            adapter.submitList(list)
            b.empty.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
        })
    }

    override fun onDestroyView() { super.onDestroyView(); _b = null }

    companion object {
        fun newInstance(day: String) = DayPageFragment().apply {
            arguments = Bundle().apply { putString("day", day) }
        }
    }
}
