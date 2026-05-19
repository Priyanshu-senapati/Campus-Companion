package com.rvu.campuscompanion.ui.lostfound

import android.app.DatePickerDialog
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.rvu.campuscompanion.data.model.LostFoundItem
import com.rvu.campuscompanion.data.remote.FirebaseSource
import com.rvu.campuscompanion.databinding.SheetPostItemBinding
import com.rvu.campuscompanion.utils.toFormattedDate
import com.rvu.campuscompanion.utils.toast
import java.util.Calendar

class PostItemSheet(
    private val defaultType: String,
    private val onPost: (LostFoundItem, Uri?) -> Unit
) : BottomSheetDialogFragment() {

    private var _b: SheetPostItemBinding? = null
    private val b get() = _b!!
    private var date: Long = System.currentTimeMillis()
    private var imageUri: Uri? = null

    private val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) { imageUri = uri; Glide.with(this).load(uri).into(b.ivImage) }
    }

    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?): View {
        _b = SheetPostItemBinding.inflate(i, c, false); return b.root
    }

    override fun onViewCreated(view: View, s: Bundle?) {
        b.toggleType.check(if (defaultType == LostFoundItem.TYPE_LOST) b.btnLost.id else b.btnFound.id)
        b.tvDate.text = date.toFormattedDate()
        b.tvDate.setOnClickListener {
            val cal = Calendar.getInstance().apply { timeInMillis = date }
            DatePickerDialog(requireContext(), { _, y, m, d ->
                cal.set(y, m, d); date = cal.timeInMillis
                b.tvDate.text = date.toFormattedDate()
            }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
        }
        b.ivImage.setOnClickListener { pickImage.launch("image/*") }

        b.btnSubmit.setOnClickListener {
            val name = b.etName.text.toString().trim()
            val desc = b.etDescription.text.toString().trim()
            val loc = b.etLocation.text.toString().trim()
            val phone = b.etPhone.text.toString().trim()
            if (name.isBlank() || desc.isBlank() || loc.isBlank() || phone.isBlank()) {
                context?.toast("Fill all fields"); return@setOnClickListener
            }
            val type = if (b.toggleType.checkedButtonId == b.btnLost.id)
                LostFoundItem.TYPE_LOST else LostFoundItem.TYPE_FOUND
            val item = LostFoundItem(
                type = type, name = name, description = desc, location = loc,
                date = date, contactPhone = phone,
                postedBy = FirebaseSource.currentUserId ?: "anonymous",
                postedByName = "Student"
            )
            onPost(item, imageUri)
            dismiss()
        }
    }

    override fun onDestroyView() { super.onDestroyView(); _b = null }
}
