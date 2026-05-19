package com.rvu.campuscompanion.ui.map

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.rvu.campuscompanion.data.model.CampusLocation
import com.rvu.campuscompanion.databinding.SheetLocationBinding

class LocationSheet(
    private val location: CampusLocation,
    private val onDirections: () -> Unit
) : BottomSheetDialogFragment() {

    private var _b: SheetLocationBinding? = null
    private val b get() = _b!!

    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?): View {
        _b = SheetLocationBinding.inflate(i, c, false); return b.root
    }

    override fun onViewCreated(view: View, s: Bundle?) {
        b.tvName.text = location.name
        b.tvCategory.text = location.category
        b.tvDescription.text = location.description
        b.tvTimings.text = location.timings
        b.tvCoords.text = "%.4f, %.4f".format(location.latitude, location.longitude)
        b.btnDirections.setOnClickListener { onDirections(); dismiss() }
    }

    override fun onDestroyView() { super.onDestroyView(); _b = null }
}
