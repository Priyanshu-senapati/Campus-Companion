package com.rvu.campuscompanion.ui.auth

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.rvu.campuscompanion.R
import com.rvu.campuscompanion.databinding.FragmentLoginBinding
import com.rvu.campuscompanion.utils.Resource
import com.rvu.campuscompanion.utils.isValidEmail
import com.rvu.campuscompanion.utils.toast
import com.rvu.campuscompanion.viewmodel.AuthViewModel

class LoginFragment : Fragment() {
    private var _b: FragmentLoginBinding? = null
    private val b get() = _b!!
    private val vm: AuthViewModel by viewModels()

    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?): View {
        _b = FragmentLoginBinding.inflate(i, c, false); return b.root
    }

    override fun onViewCreated(view: View, s: Bundle?) {
        b.cbRemember.isChecked = vm.rememberMe()
        b.etEmail.setText(vm.savedEmail())

        b.btnLogin.setOnClickListener { attemptLogin() }
        b.tvSignup.setOnClickListener {
            findNavController().navigate(R.id.action_login_to_register)
        }
        b.tvForgot.setOnClickListener { showResetDialog() }
        b.btnGoogle.setOnClickListener {
            context?.toast("Google Sign-In: configure SHA-1 in Firebase Console")
        }

        vm.authState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is Resource.Loading -> setLoading(true)
                is Resource.Success<*> -> {
                    setLoading(false)
                    findNavController().navigate(R.id.action_login_to_dashboard)
                }
                is Resource.Error -> {
                    setLoading(false)
                    context?.toast(state.message)
                }
                else -> setLoading(false)
            }
        }
    }

    private fun attemptLogin() {
        val email = b.etEmail.text.toString().trim()
        val password = b.etPassword.text.toString()
        b.tilEmail.error = null; b.tilPassword.error = null

        if (!email.isValidEmail()) { b.tilEmail.error = getString(R.string.error_email_invalid); return }
        if (password.length < 6) { b.tilPassword.error = getString(R.string.error_password_short); return }

        vm.login(email, password, b.cbRemember.isChecked)
    }

    private fun showResetDialog() {
        val input = EditText(requireContext()).apply { hint = "you@rvu.edu.in" }
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.forgot_password)
            .setMessage("Enter your registered email")
            .setView(input)
            .setPositiveButton("Send") { _, _ ->
                val email = input.text.toString().trim()
                if (!email.isValidEmail()) { context?.toast(getString(R.string.error_email_invalid)); return@setPositiveButton }
                vm.resetPassword(email) { res ->
                    activity?.runOnUiThread {
                        context?.toast(res.fold(
                            onSuccess = { "Reset link sent" },
                            onFailure = { it.message ?: "Failed" }
                        ))
                    }
                }
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun setLoading(loading: Boolean) {
        b.progress.visibility = if (loading) View.VISIBLE else View.GONE
        b.btnLogin.isEnabled = !loading
    }

    override fun onDestroyView() { super.onDestroyView(); _b = null }
}
