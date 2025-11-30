package com.example.yp

import DBHelper
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

class SignInActivity : AppCompatActivity() {

    private lateinit var dbHelper: DBHelper
    private lateinit var signInButton: Button
    private lateinit var signUpText: TextView
    private lateinit var email: EditText
    private lateinit var password: EditText
    fun authUser(email: String, password: String): Boolean{
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Заполните все поля", Toast.LENGTH_SHORT).show()
            return false
        }
        if(dbHelper.getByEmail(email) == null){
            Toast.makeText(this, "Пользователь с таким email не существует", Toast.LENGTH_SHORT).show()
            return false
        }
        if(!HashingPassword.verifyPassword(password, dbHelper.getByEmail(email)!!.password)){
            Toast.makeText(this, "Неправильно введен пароль", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        dbHelper = DBHelper(this, null)
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
            val succes = authUser(
                email.text.toString().trim().lowercase(),
                password.text.toString().trim()
            )
            if (succes){
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }
        }
        signUpText.setOnClickListener { v ->
            startActivity(Intent(this, SignUpActivity::class.java))
        }
    }
}