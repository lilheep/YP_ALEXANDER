package com.example.yp.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.yp.activity.SignInActivity
import com.example.yp.database.DBHelper
import com.example.yp.database.User
import com.example.yp.databinding.FragmentProfilBinding
import com.example.yp.utils.SessionManager
import java.text.SimpleDateFormat
import java.util.*

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfilBinding? = null
    private val binding get() = _binding!!
    private lateinit var sessionManager: SessionManager
    private lateinit var dbHelper: DBHelper

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentProfilBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sessionManager = SessionManager(requireContext())
        dbHelper = DBHelper(requireContext(), null)

        if (!sessionManager.isLoggedIn()) {

            goToSignInActivity()
            return
        }

        setupClickListeners()
        loadUserData()
    }

    private fun setupClickListeners() {
        binding.btnLogout.setOnClickListener {
            logoutUser()
        }
    }

    private fun loadUserData() {
        val email = sessionManager.getUserEmail()

        if (email != null) {
            showUserInfo(email)
        } else {
            goToSignInActivity()
        }
    }

    private fun showUserInfo(email: String) {
        val user: User? = dbHelper.getByEmail(email)

        if (user != null) {

            binding.tvEmail.text = "Email: ${user.email}"

            binding.tvEmail.visibility = View.VISIBLE
            binding.btnLogout.visibility = View.VISIBLE

            binding.tvNotLoggedIn.visibility = View.GONE
            binding.btnLogin.visibility = View.GONE

        } else {

            Toast.makeText(requireContext(), "Пользователь не найден", Toast.LENGTH_SHORT).show()
            logoutUser()
        }
    }

    private fun logoutUser() {

        sessionManager.logout()

        Toast.makeText(
            requireContext(),
            "Вы вышли из аккаунта",
            Toast.LENGTH_SHORT
        ).show()

        goToSignInActivity()
    }

    private fun goToSignInActivity() {

        val intent = Intent(requireContext(), SignInActivity::class.java)

        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK

        startActivity(intent)
        requireActivity().finish()
    }

    override fun onResume() {
        super.onResume()
        if (sessionManager.isLoggedIn()) {
            loadUserData()
        } else {
            goToSignInActivity()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        @JvmStatic
        fun newInstance() = ProfileFragment()
    }
}