package com.rvu.campuscompanion.ui.auth

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.rvu.campuscompanion.R
import com.rvu.campuscompanion.databinding.FragmentRegisterBinding
import com.rvu.campuscompanion.utils.Constants
import com.rvu.campuscompanion.utils.Resource
import com.rvu.campuscompanion.utils.isValidRvuEmail
import com.rvu.campuscompanion.utils.toast
import com.rvu.campuscompanion.viewmodel.AuthViewModel

class RegisterFragment : Fragment() {
    private var _b: FragmentRegisterBinding? = null
    private val b get() = _b!!
    private val vm:
            AuthViewModel by viewModels()
    private var photoUri: Uri? = null

    private val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            photoUri = uri
            Glide.with(this).load(uri).circleCrop().into(b.ivAvatar)
        }
    }

    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?): View {
        _b = FragmentRegisterBinding.inflate(i, c, false); return b.root
    }

    override fun onViewCreated(view: View, s: Bundle?) {
        val branchAdapter = ArrayAdapter(requireContext(),
            android.R.layout.simple_list_item_1, Constants.BRANCHES)
        b.actBranch.setAdapter(branchAdapter)

        val semAdapter = ArrayAdapter(requireContext(),
            android.R.layout.simple_list_item_1, (1..8).map { "Semester $it" })
        b.actSemester.setAdapter(semAdapter)

        b.ivAvatar.setOnClickListener { pickImage.launch("image/*") }
        b.btnRegister.setOnClickListener { attemptRegister() }
        b.tvSignIn.setOnClickListener { findNavController().popBackStack() }

        vm.authState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is Resource.Loading -> setLoading(true)
                is Resource.Success<*> -> {
                    setLoading(false)
                    context?.toast("Account created — welcome!")
                    findNavController().navigate(R.id.action_register_to_dashboard)
                }
                is Resource.Error -> { setLoading(false); context?.toast(state.message) }
                else -> setLoading(false)
            }
        }
    }

    private fun attemptRegister() {
        val name = b.etName.text.toString().trim()
        val email = b.etEmail.text.toString().trim()
        val prn = b.etPrn.text.toString().trim()
        val branch = b.actBranch.text.toString().trim()
        val semStr = b.actSemester.text.toString().trim()
        val pass = b.etPassword.text.toString()
        val conf = b.etConfirm.text.toString()

        listOf(b.tilName, b.tilEmail, b.tilPrn, b.tilBranch, b.tilSemester, b.tilPassword, b.tilConfirm).forEach { it.error = null }

        if (name.isBlank()) { b.tilName.error = getString(R.string.error_required); return }
        if (!email.isValidRvuEmail()) { b.tilEmail.error = getString(R.string.error_email_invalid); return }
        if (prn.isBlank()) { b.tilPrn.error = getString(R.string.error_required); return }
        if (branch.isBlank()) { b.tilBranch.error = getString(R.string.error_required); return }
        if (semStr.isBlank()) { b.tilSemester.error = getString(R.string.error_required); return }
        if (pass.length < 6) { b.tilPassword.error = getString(R.string.error_password_short); return }
        if (pass != conf) { b.tilConfirm.error = getString(R.string.error_password_mismatch); return }

        val semester = semStr.filter { it.isDigit() }.toIntOrNull() ?: 1
        vm.register(email, pass, name, prn, branch, semester, photoUri)
    }

    private fun setLoading(loading: Boolean) {
        b.progress.visibility = if (loading) View.VISIBLE else View.GONE
        b.btnRegister.isEnabled = !loading
    }

    override fun onDestroyView() { super.onDestroyView(); _b = null }
}
