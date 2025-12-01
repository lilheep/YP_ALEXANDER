package com.example.yp.activity

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.yp.HashingPassword
import com.example.yp.R
import com.example.yp.database.DBHelper
import com.example.yp.utils.SessionManager

class SignInActivity : AppCompatActivity() {

    private lateinit var dbHelper: DBHelper
    private lateinit var sessionManager: SessionManager
    private lateinit var signInButton: Button
    private lateinit var signUpText: TextView
    private lateinit var email: EditText
    private lateinit var password: EditText

    fun authUser(email: String, password: String): Boolean {
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Заполните все поля", Toast.LENGTH_SHORT).show()
            return false
        }

        val user = dbHelper.getByEmail(email)
        if (user == null) {
            Toast.makeText(this, "Пользователь с таким email не существует", Toast.LENGTH_SHORT).show()
            return false
        }

        if (!HashingPassword.verifyPassword(password, user.password)) {
            Toast.makeText(this, "Неправильно введен пароль", Toast.LENGTH_SHORT).show()
            return false
        }

        sessionManager.createSession(user.id, user.email)
        Toast.makeText(this, "Вход выполнен успешно!", Toast.LENGTH_SHORT).show()

        return true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        dbHelper = DBHelper(this, null)
        sessionManager = SessionManager(this)

        if (sessionManager.isLoggedIn()) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return
        }

        setContentView(R.layout.activity_sign_in)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        signInButton = findViewById(R.id.signInButton)
        signUpText = findViewById(R.id.signUpText)
        email = findViewById(R.id.email)
        password = findViewById(R.id.password)

        signInButton.setOnClickListener { v ->
            val success = authUser(
                email.text.toString().trim().lowercase(),
                password.text.toString().trim()
            )
            if (success) {
                val intent = Intent(this, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            }
        }

        signUpText.setOnClickListener { v ->
            startActivity(Intent(this, SignUpActivity::class.java))
        }
    }
}