package com.rvu.campuscompanion.ui.canteen

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.rvu.campuscompanion.R
import com.rvu.campuscompanion.RVUApplication
import com.rvu.campuscompanion.databinding.FragmentMenuDetailBinding
import com.rvu.campuscompanion.viewmodel.CanteenViewModel
import com.rvu.campuscompanion.viewmodel.ViewModelFactory

class MenuDetailFragment : Fragment() {
    private var _b: FragmentMenuDetailBinding? = null
    private val b get() = _b!!
    private val vm: CanteenViewModel by viewModels {
        val app = requireActivity().application as RVUApplication
        ViewModelFactory(canteenRepo = app.canteenRepository)
    }

    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?): View {
        _b = FragmentMenuDetailBinding.inflate(i, c, false); return b.root
    }

    override fun onViewCreated(view: View, s: Bundle?) {
        val id = requireArguments().getLong("itemId")
        b.toolbar.setNavigationOnClickListener { requireActivity().onBackPressedDispatcher.onBackPressed() }
        vm.allItems.observe(viewLifecycleOwner) { all ->
            val item = all.firstOrNull { it.id == id } ?: return@observe
            b.toolbar.title = item.name
            b.tvName.text = item.name
            b.tvPrice.text = "₹${item.price}"
            b.tvCategory.text = item.category
            b.tvDescription.text = item.description
            b.tvCalories.text = "${item.calories} kcal"
            b.tvAllergens.text = "Allergens: ${item.allergens.ifBlank { "None" }}"
            b.dotVeg.setBackgroundResource(if (item.isVeg) R.drawable.dot_veg else R.drawable.dot_nonveg)
            b.tvVegLabel.text = if (item.isVeg) "Vegetarian" else "Non-vegetarian"
        }
    }

    override fun onDestroyView() { super.onDestroyView(); _b = null }
}
