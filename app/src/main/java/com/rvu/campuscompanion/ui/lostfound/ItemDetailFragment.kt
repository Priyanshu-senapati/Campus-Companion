package com.rvu.campuscompanion.ui.lostfound

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.bumptech.glide.Glide
import com.rvu.campuscompanion.R
import com.rvu.campuscompanion.data.model.LostFoundItem
import com.rvu.campuscompanion.databinding.FragmentItemDetailBinding
import com.rvu.campuscompanion.utils.toFormattedDate
import com.rvu.campuscompanion.utils.toast
import com.rvu.campuscompanion.viewmodel.LostFoundViewModel

class ItemDetailFragment : Fragment() {
    private var _b: FragmentItemDetailBinding? = null
    private val b get() = _b!!
    private val vm: LostFoundViewModel by viewModels()

    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?): View {
        _b = FragmentItemDetailBinding.inflate(i, c, false); return b.root
    }

    override fun onViewCreated(view: View, s: Bundle?) {
        val id = requireArguments().getString("itemId") ?: return
        b.toolbar.setNavigationOnClickListener { requireActivity().onBackPressedDispatcher.onBackPressed() }
        vm.loadItem(id)
        vm.item.observe(viewLifecycleOwner) { item ->
            if (item == null) return@observe
            b.toolbar.title = item.name
            b.tvName.text = item.name
            b.tvDescription.text = item.description
            b.tvLocation.text = item.location
            b.tvDate.text = item.date.toFormattedDate()
            b.tvContact.text = item.contactPhone
            b.tvType.text = item.type
            b.tvStatus.text = item.status
            b.tvPoster.text = "Posted by ${item.postedByName.ifBlank { "Student" }}"
            if (item.imageUrl.isNotEmpty()) Glide.with(this).load(item.imageUrl).into(b.ivImage)
            else b.ivImage.setImageResource(R.drawable.ic_image_placeholder)

            b.btnResolve.text = if (item.type == LostFoundItem.TYPE_FOUND) "This is mine!" else "I found it!"
            b.btnResolve.setOnClickListener { vm.markResolved(item.id) }
            b.btnCall.setOnClickListener {
                startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:${item.contactPhone}")))
            }
            b.btnWhatsapp.setOnClickListener {
                val url = "https://wa.me/${item.contactPhone.filter { ch -> ch.isDigit() }}"
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
            }
        }
        vm.statusResult.observe(viewLifecycleOwner) { r ->
            r.onSuccess { context?.toast("Marked resolved"); vm.loadItem(id) }
                .onFailure { context?.toast(it.message ?: "Failed") }
        }
    }

    override fun onDestroyView() { super.onDestroyView(); _b = null }
}
