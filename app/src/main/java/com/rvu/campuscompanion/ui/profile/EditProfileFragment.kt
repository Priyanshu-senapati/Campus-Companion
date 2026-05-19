package com.rvu.campuscompanion.ui.profile

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.rvu.campuscompanion.databinding.FragmentEditProfileBinding
import com.rvu.campuscompanion.utils.toast
import com.rvu.campuscompanion.viewmodel.ProfileViewModel

class EditProfileFragment : Fragment() {
    private var _b: FragmentEditProfileBinding? = null
    private val b get() = _b!!
    private val vm: ProfileViewModel by viewModels()
    private var photoUri: Uri? = null

    private val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) { photoUri = uri; Glide.with(this).load(uri).circleCrop().into(b.ivAvatar) }
    }

    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?): View {
        _b = FragmentEditProfileBinding.inflate(i, c, false); return b.root
    }

    override fun onViewCreated(view: View, s: Bundle?) {
        b.toolbar.setNavigationOnClickListener { requireActivity().onBackPressedDispatcher.onBackPressed() }
        vm.load()
        vm.user.observe(viewLifecycleOwner) { u ->
            if (u == null) return@observe
            b.etName.setText(u.name)
            b.etBio.setText(u.bio)
            b.etPhone.setText(u.phone)
            if (u.photoUrl.isNotEmpty()) Glide.with(this).load(u.photoUrl).circleCrop().into(b.ivAvatar)
        }
        b.ivAvatar.setOnClickListener { pickImage.launch("image/*") }
        b.btnSave.setOnClickListener {
            val current = vm.user.value ?: return@setOnClickListener
            val updated = current.copy(
                name = b.etName.text.toString().trim(),
                bio = b.etBio.text.toString().trim(),
                phone = b.etPhone.text.toString().trim()
            )
            vm.updateProfile(updated, photoUri)
        }
        vm.updateResult.observe(viewLifecycleOwner) { r ->
            r.fold(
                onSuccess = { context?.toast("Profile updated"); findNavController().popBackStack() },
                onFailure = { context?.toast(it.message ?: "Failed") }
            )
        }
    }

    override fun onDestroyView() { super.onDestroyView(); _b = null }
}
