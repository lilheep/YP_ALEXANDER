package com.example.yp.fragments
import android.widget.EditText
import android.widget.CheckBox
import androidx.core.content.ContextCompat
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.example.yp.R
import com.example.yp.activity.SignInActivity
import com.example.yp.database.DBHelper
import com.example.yp.database.User
import com.example.yp.databinding.FragmentProfilBinding
import com.example.yp.utils.SessionManager

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

        binding.btnEditProfile.setOnClickListener {
            showChangeEmailDialog()
        }

        binding.btnChangePassword.setOnClickListener {
            showChangePasswordDialog()
        }

        binding.btnDeleteAccount.setOnClickListener {
            showDeleteAccountDialog()
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
        } else {
            Toast.makeText(requireContext(), "Пользователь не найден", Toast.LENGTH_SHORT).show()
            logoutUser()
        }
    }

    private fun showChangeEmailDialog() {
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_change_email, null)

        val etNewEmail = dialogView.findViewById<EditText>(R.id.etNewEmail)
        val etPassword = dialogView.findViewById<EditText>(R.id.etPassword)

        val dialog = AlertDialog.Builder(requireContext(), R.style.RedDialogTheme)
            .setView(dialogView)
            .setPositiveButton("Подтвердить") { dialog, _ ->
                val newEmail = etNewEmail.text.toString().trim()
                val password = etPassword.text.toString().trim()

                if (newEmail.isEmpty() || password.isEmpty()) {
                    Toast.makeText(requireContext(), "Заполните все поля", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                val userId = sessionManager.getUserId()
                if (userId != null) {
                    val success = dbHelper.updateEmail(userId, newEmail, password)
                    if (success) {

                        sessionManager.updateEmail(newEmail)
                        Toast.makeText(requireContext(), "Email успешно изменен", Toast.LENGTH_SHORT).show()
                        loadUserData()
                    } else {
                        Toast.makeText(requireContext(), "Неверный пароль или ошибка обновления", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton("Отмена", null)
            .create()

        dialog.show()

        dialog.getButton(AlertDialog.BUTTON_POSITIVE)?.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE)?.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
    }

    private fun showChangePasswordDialog() {
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_change_password, null)

        val etOldPassword = dialogView.findViewById<EditText>(R.id.etOldPassword)
        val etNewPassword = dialogView.findViewById<EditText>(R.id.etNewPassword)
        val etConfirmPassword = dialogView.findViewById<EditText>(R.id.etConfirmPassword)

        val dialog = AlertDialog.Builder(requireContext(), R.style.RedDialogTheme)
            .setView(dialogView)
            .setPositiveButton("Подтвердить") { dialog, _ ->
                val oldPassword = etOldPassword.text.toString().trim()
                val newPassword = etNewPassword.text.toString().trim()
                val confirmPassword = etConfirmPassword.text.toString().trim()

                if (oldPassword.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
                    Toast.makeText(requireContext(), "Заполните все поля", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                if (newPassword != confirmPassword) {
                    Toast.makeText(requireContext(), "Новые пароли не совпадают", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                val userId = sessionManager.getUserId()
                if (userId != null) {
                    val success = dbHelper.updatePassword(userId, oldPassword, newPassword)
                    if (success) {
                        Toast.makeText(requireContext(), "Пароль успешно изменен", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(requireContext(), "Неверный текущий пароль", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton("Отмена", null)
            .create()

        dialog.show()

        dialog.getButton(AlertDialog.BUTTON_POSITIVE)?.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE)?.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
    }

    private fun showDeleteAccountDialog() {
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_delete_account, null)

        val etConfirmPassword = dialogView.findViewById<EditText>(R.id.etConfirmPassword)
        val cbSoftDelete = dialogView.findViewById<CheckBox>(R.id.cbSoftDelete)

        val dialog = AlertDialog.Builder(requireContext(), R.style.RedDialogTheme)
            .setView(dialogView)
            .setPositiveButton("Подтвердить") { dialog, _ ->
                val password = etConfirmPassword.text.toString().trim()
                val isSoftDelete = cbSoftDelete.isChecked

                if (password.isEmpty()) {
                    Toast.makeText(requireContext(), "Введите пароль для подтверждения", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                val userId = sessionManager.getUserId()
                if (userId != null) {
                    val success = if (isSoftDelete) {
                        dbHelper.softDeleteUser(userId, password)
                    } else {
                        dbHelper.hardDeleteUser(userId, password)
                    }

                    if (success) {
                        val message = if (isSoftDelete) {
                            "Аккаунт помечен как удаленный. Вы можете восстановить его при следующем входе."
                        } else {
                            "Аккаунт полностью удален."
                        }

                        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
                        logoutUser()
                    } else {
                        Toast.makeText(requireContext(), "Неверный пароль", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton("Отмена", null)
            .create()

        dialog.show()

        dialog.getButton(AlertDialog.BUTTON_POSITIVE)?.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE)?.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
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