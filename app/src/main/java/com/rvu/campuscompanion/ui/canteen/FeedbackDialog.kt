package com.rvu.campuscompanion.ui.canteen

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.rvu.campuscompanion.databinding.SheetFeedbackBinding
import com.rvu.campuscompanion.utils.toast

class FeedbackDialog(
    private val onSubmit: (item: String, rating: Int, comment: String) -> Unit
) : BottomSheetDialogFragment() {

    private var _b: SheetFeedbackBinding? = null
    private val b get() = _b!!

    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?): View {
        _b = SheetFeedbackBinding.inflate(i, c, false); return b.root
    }

    override fun onViewCreated(view: View, s: Bundle?) {
        b.btnSubmit.setOnClickListener {
            val item = b.etItem.text.toString().trim().ifBlank { "General" }
            val rating = b.ratingBar.rating.toInt()
            val comment = b.etComment.text.toString().trim()
            if (rating == 0) { context?.toast("Please rate"); return@setOnClickListener }
            onSubmit(item, rating, comment)
            context?.toast("Thanks for your feedback!")
            dismiss()
        }
    }

    override fun onDestroyView() { super.onDestroyView(); _b = null }
}
